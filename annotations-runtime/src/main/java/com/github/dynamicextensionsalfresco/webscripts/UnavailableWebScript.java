package com.github.dynamicextensionsalfresco.webscripts;

import org.springframework.extensions.webscripts.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ResourceBundle;

/**
 * {@link WebScript} that generates a "503 Service Unavailable" response when request is issued to an
 * {@link AnnotationWebScript} that is currently unavailable.
 * 
 * @author Laurens Fridael
 * 
 */
class UnavailableWebScript implements WebScript {

	private Description description;

	@Override
	public void init(final Container container, final Description description) {
		this.description = description;
	}

	@Override
	public void setURLModelFactory(final URLModelFactory urlModelFactory) {
	}

	@Override
	public Description getDescription() {
		return description;
	}

	@Override
	public ResourceBundle getResources() {
		return null;
	}

	@Override
	public void execute(final WebScriptRequest request, final WebScriptResponse response) throws IOException {
		response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
		response.setHeader("Cache-Control", "no-cache,no-store");
		response.getWriter()
				.write("This Web Script is currently unavailable. It may have been undeployed temporarily.");
	}
}
