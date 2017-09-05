package com.github.dynamicextensionsalfresco.webscripts;

import com.github.dynamicextensionsalfresco.webscripts.annotations.Attribute;
import com.github.dynamicextensionsalfresco.webscripts.arguments.HandlerMethodArgumentsResolver;
import com.github.dynamicextensionsalfresco.webscripts.resolutions.DefaultResolutionParameters;
import com.github.dynamicextensionsalfresco.webscripts.resolutions.Resolution;
import com.github.dynamicextensionsalfresco.webscripts.resolutions.TemplateResolution;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.extensions.webscripts.*;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.xml.Jaxb2RootElementHttpMessageConverter;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.HttpMediaTypeNotSupportedException;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class AnnotationWebScript implements WebScript {
	/* Dependencies */

	private HandlerMethodArgumentsResolver argumentsResolver;

	/* Configuration */

	private final Description description;

	private final Object handler;

	private final HandlerMethods handlerMethods;

	private final String id;

    private final List<HttpMessageConverter<?>> messageConverters = new ArrayList<HttpMessageConverter<?>>();

	/* Main operations */

	public AnnotationWebScript(final Description description, final Object handler,
			final HandlerMethods handlerMethods, final HandlerMethodArgumentsResolver argumentsResolver) {
		Assert.notNull(description, "Description cannot be null.");
		Assert.hasText(description.getId(), "No ID provided in Description.");
		Assert.notNull(handler, "Handler cannot be null.");
		Assert.notNull(handlerMethods, "Methods cannot be null.");

		this.description = description;
		this.handler = handler;
		this.handlerMethods = handlerMethods;
		this.argumentsResolver = argumentsResolver;
		this.id = description.getId();

        this.messageConverters.add(new MappingJackson2HttpMessageConverter());
        this.messageConverters.add(new Jaxb2RootElementHttpMessageConverter());

	}

	public Object getHandler() {
		return handler;
	}

	public HandlerMethods getHandlerMethods() {
		return handlerMethods;
	}

	@Override
	public final void execute(final WebScriptRequest request, final WebScriptResponse response) throws IOException {
		final AnnotationWebScriptRequest annotationRequest = new AnnotationWebScriptRequest(request);
		final AnnotationWebscriptResponse wrappedResponse = new AnnotationWebscriptResponse(response);
		try {
			invokeAttributeHandlerMethods(annotationRequest, wrappedResponse);
			invokeBeforeHandlerMethods(annotationRequest, wrappedResponse);
			final Object returnValue = invokeUriHandlerMethod(annotationRequest, wrappedResponse);
			handleUriMethodReturnValue(handlerMethods, annotationRequest, wrappedResponse, returnValue);
		} catch (final Throwable e) {
			invokeExceptionHandlerMethods(e, annotationRequest, wrappedResponse);
		}
	}

	/*
	 * This method appears to be new in the Web Scripts 1.0.0 API. This implementation does nothing, because we want to
	 * retain backwards-compatibility.
	 */
	@Override
	public void init(final Container container, final Description description) {
	}

	@Override
	public Description getDescription() {
		return description;
	}

	@Override
	public ResourceBundle getResources() {
		/* Not yet supported. */
		return null;
	}

	@Override
	public void setURLModelFactory(final URLModelFactory arg0) {
		/* Not yet implemented. */
	}

	/* Handler operations */

	protected boolean invokeBeforeHandlerMethods(final AnnotationWebScriptRequest request,
			final WebScriptResponse response) {
		for (final Method method : handlerMethods.getBeforeMethods()) {
			method.setAccessible(true);
			final Object[] arguments = getArgumentsResolver().resolveHandlerMethodArguments(method, handler, request,
					response);
			final Object returnValue = ReflectionUtils.invokeMethod(method, handler, arguments);
			if (Boolean.FALSE.equals(returnValue)) {
				return false;
			}
		}
		return true;

	}

	protected void invokeAttributeHandlerMethods(final AnnotationWebScriptRequest request,
			final WebScriptResponse response) {
		for (final Method method : handlerMethods.getAttributeMethods()) {
			method.setAccessible(true);
			final Object[] arguments = getArgumentsResolver().resolveHandlerMethodArguments(method, handler, request,
					response);
			final Object attribute = ReflectionUtils.invokeMethod(method, handler, arguments);
			if (attribute == null) {
				continue;
			}
			final Attribute annotation = AnnotationUtils.findAnnotation(method, Attribute.class);
			final Map<String, Object> model = request.getModel();
			if (StringUtils.hasText(annotation.value())) {
				model.put(annotation.value(), attribute);
			} else {
				String name = method.getName();
				if (name.startsWith("get") && name.length() > 3) {
					name = name.substring(3, 4).toLowerCase() + name.substring(4);
				}
				model.put(name, attribute);
			}
		}
	}

	protected Object invokeUriHandlerMethod(final AnnotationWebScriptRequest request,
			final AnnotationWebscriptResponse response) {
		final Method uriMethod = handlerMethods.getUriMethod();
		final Object[] arguments = argumentsResolver.resolveHandlerMethodArguments(uriMethod, handler, request,
				response);
		uriMethod.setAccessible(true);
		return ReflectionUtils.invokeMethod(uriMethod, handler, arguments);
	}

	@SuppressWarnings("unchecked")
	protected void handleUriMethodReturnValue(HandlerMethods handlerMethods, final AnnotationWebScriptRequest request,
                                              final AnnotationWebscriptResponse response, final Object returnValue) throws Exception {
        Resolution resolution = null;
        if (returnValue instanceof Map) { // returning a map will result in response template by default.
            resolution = new TemplateResolution((Map<String, Object>) returnValue);
        } else if (returnValue instanceof String) { // returning a string will also result in a template response.
            resolution = new TemplateResolution((String)returnValue);
        } else if (returnValue instanceof Resolution) {
            resolution = (Resolution) returnValue;
        }

        /**
         * If the method is annotated with {@link org.springframework.web.bind.annotation.ResponseBody}
         * The response should be serialized automatically by what the request asked in the accept header.
         * If the accept header is not present, the default format value of the methode is used.
         * If there is no default format available, an exception is thrown.
         */
        if (handlerMethods.useResponseBody()){
            String[] responseTypes = request.getHeaderValues("Accept");

            MediaType acceptResponseType = MediaType.ALL; // default

            if (responseTypes != null){
                acceptResponseType = MediaType.parseMediaType(responseTypes[0]);
            }

            HttpMessageConverter converter = null;

            for(HttpMessageConverter messageConverter : this.messageConverters){
                if (messageConverter.canWrite(returnValue.getClass(), acceptResponseType)){
                    converter = messageConverter;
                    break;
                }
            }

            if(converter == null) {
                // unsupported type requested
                // should throw error.

                List<MediaType> supported = new ArrayList<MediaType>();

                for(HttpMessageConverter messageConverter : this.messageConverters){
                    supported.addAll(messageConverter.getSupportedMediaTypes());
                }

                throw new HttpMediaTypeNotSupportedException(acceptResponseType, supported);
            }


            HttpOutputMessage outputMessage = new AnnotationWebScriptOutputMessage(request, response);
            converter.write(returnValue, acceptResponseType, outputMessage);
        }
        /**
         * If no {@link org.springframework.web.bind.annotation.ResponseBody} annotation is present,
         * check if a template needs to be used.
         */
        else if (this.handlerMethods.useResponseTemplate()) {
            final String responseTemplateName = handlerMethods.getResponseTemplateName();
            if (responseTemplateName != null) {
                if (resolution instanceof TemplateResolution) {
                    if (((TemplateResolution) resolution).getTemplate() == null) {
                        ((TemplateResolution) resolution).setTemplate(responseTemplateName);
                    }
                } else if (resolution == null) {
                    resolution = new TemplateResolution(responseTemplateName);
                }
            }
        }

        /**
         *
         */
        if (resolution != null) {
            if (resolution instanceof TemplateResolution) {
                final TemplateResolution templateResolution = (TemplateResolution)resolution;
                final Map<String, Object> model = request.getModel();

                if (templateResolution.getModel() != null && templateResolution.getModel() != model) {
                    model.putAll(templateResolution.getModel());
                }
                templateResolution.setModel(model);
            }

            resolution.resolve(request, response,
                new DefaultResolutionParameters(handlerMethods.getUriMethod(), description, handler)
            );
        }
        else {


		}
    }

	protected void invokeExceptionHandlerMethods(final Throwable exception, final AnnotationWebScriptRequest request,
			final WebScriptResponse response) throws IOException {
		final List<Method> exceptionHandlerMethods = handlerMethods.findExceptionHandlers(exception);
		if (exceptionHandlerMethods.isEmpty()) {
			translateException(exception);
		}
		try {
			request.setThrownException(exception);
			for (final Method exceptionHandler : exceptionHandlerMethods) {
				final Object[] arguments = getArgumentsResolver().resolveHandlerMethodArguments(exceptionHandler,
						handler, request, response);
				exceptionHandler.setAccessible(true);
				ReflectionUtils.invokeMethod(exceptionHandler, handler, arguments);
			}
		} catch (final Throwable e) {
			translateException(e);
		} finally {
			request.setThrownException(null);
		}

	}

	/* Utility operations */

	protected void translateException(final Throwable e) throws IOException {
		if (e instanceof IOException) {
			throw (IOException) e;
		} else if (e instanceof RuntimeException) {
			throw (RuntimeException) e;
		} else {
			throw new RuntimeException(e);
		}
	}

	/* Dependencies */

	public void setArgumentsResolver(final HandlerMethodArgumentsResolver handlerMethodArgumentsResolver) {
		Assert.notNull(handlerMethodArgumentsResolver);
		this.argumentsResolver = handlerMethodArgumentsResolver;
	}

	protected HandlerMethodArgumentsResolver getArgumentsResolver() {
		return argumentsResolver;
	}

	/* Equality operations */

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final AnnotationWebScript other = (AnnotationWebScript) obj;
		if (id == null) {
			if (other.id != null) {
				return false;
			}
		} else if (!id.equals(other.id)) {
			return false;
		}
		return true;
	}

}
