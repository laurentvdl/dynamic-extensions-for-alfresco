package nl.runnable.alfresco.extensions.controlpanel;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import nl.runnable.alfresco.extensions.controlpanel.template.Variables;

import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.extensions.webscripts.WebScriptSession;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * Helper for handling responses.
 * 
 * @author Laurens Fridael
 * 
 */
class ResponseHelper {
	private final WebScriptRequest request;

	private final WebScriptResponse response;

	ResponseHelper(final WebScriptRequest request, final WebScriptResponse response) {
		this.request = request;
		this.response = response;
	}

	public ResponseHelper redirectToService(String path) {
		Assert.hasText(path);
		if (path.startsWith("/") == false) {
			path = "/" + path;
		}
		response.setStatus(HttpServletResponse.SC_MOVED_TEMPORARILY); // 302
		response.setHeader("Location", request.getServiceContextPath() + path);
		return this;
	}

	public void setFlashVariable(final String name, final Object value) {
		request.getRuntime().getSession().setValue(name, value);
	}

	public void flashErrorMessage(final String errorMessage) {
		setFlashVariable(Variables.ERROR_MESSAGE, errorMessage);
	}

	public void flashSuccessMessage(final String successMessage) {
		setFlashVariable(Variables.SUCCESS_MESSAGE, successMessage);
	}

	@SuppressWarnings("unchecked")
	public <T> T getFlashVariable(final String name) {
		final WebScriptSession session = request.getRuntime().getSession();
		final T value = (T) session.getValue(name);
		session.removeValue(name);
		return value;
	}

	public ResponseHelper redirectToIndex() {
		redirectToService("/dynamic-extensions/");
		return this;
	}

	public ResponseHelper redirectToBundle(long bundleId) {
		redirectToService("/dynamic-extensions/bundles/" + bundleId);
		return this;
	}

	public ResponseHelper status(final int status, final String message) throws IOException {
		response.setStatus(status);
		if (StringUtils.hasText(message)) {
			response.getWriter().write(message);
		}
		return this;
	}

	public ResponseHelper status(final int status) throws IOException {
		return status(status, null);
	}

	public ResponseHelper noCache() {
		response.setHeader("Cache-Control", "no-cache, nostore");
		return this;
	}
}
