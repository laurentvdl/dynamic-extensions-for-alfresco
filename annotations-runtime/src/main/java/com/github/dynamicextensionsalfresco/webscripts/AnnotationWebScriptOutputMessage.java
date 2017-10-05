package com.github.dynamicextensionsalfresco.webscripts;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpOutputMessage;

import java.io.IOException;
import java.io.OutputStream;

public class AnnotationWebScriptOutputMessage implements HttpOutputMessage {

    private final AnnotationWebScriptRequest request;
    private final AnnotationWebscriptResponse response;

    private final HttpHeadersWrapper headers;

    public AnnotationWebScriptOutputMessage(AnnotationWebScriptRequest request, AnnotationWebscriptResponse response) {
        this.request = request;
        this.response = response;

        this.headers = new HttpHeadersWrapper(response);
    }

    @Override
    public OutputStream getBody() throws IOException {
        return response.getOutputStream();
    }

    @Override
    public HttpHeaders getHeaders() {
        return this.headers;
    }

    public class HttpHeadersWrapper extends HttpHeaders {
        private final AnnotationWebscriptResponse response;

        public HttpHeadersWrapper(AnnotationWebscriptResponse response) {
            this.response = response;
        }

        @Override
        public void add(String headerName, String headerValue) {
            super.add(headerName, headerValue);

            response.addHeader(headerName, headerValue);
        }

        @Override
        public void set(String headerName, String headerValue) {
            super.set(headerName, headerValue);

            response.setHeader(headerName, headerValue);
        }
    }

}
