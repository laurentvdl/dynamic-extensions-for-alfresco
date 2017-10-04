package com.github.dynamicextensionsalfresco.webscripts;

import com.github.dynamicextensionsalfresco.spring.Spied;
import com.github.dynamicextensionsalfresco.webscripts.annotations.RequestParam;
import com.github.dynamicextensionsalfresco.webscripts.annotations.Uri;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ResponseBody;

@Component
@Spied
public class ResponseBodyReturnValueHandler {

    @Uri(value = "/handleDefaultResponse", defaultFormat = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Person handleDefaultResponse(@RequestParam final String firstName, @RequestParam final String lastName) {
        return new Person(firstName, lastName);
    }

    @Uri(value = "/handleDefaultXmlResponse", defaultFormat = MediaType.APPLICATION_XML_VALUE)
    @ResponseBody
    public PersonXml handleDefaultXmlResponse(@RequestParam final String firstName, @RequestParam final String lastName) {
        return new PersonXml(firstName, lastName);
    }

    @Uri("/handleResponse")
    @ResponseBody
    public Person handleResponse(@RequestParam final String firstName, @RequestParam final String lastName) {
        return new Person(firstName, lastName);
    }

    @Uri("/handleXmlResponse")
    @ResponseBody
    public PersonXml handleXmlResponse(@RequestParam final String firstName, @RequestParam final String lastName) {
        return new PersonXml(firstName, lastName);
    }

    @Uri("/handleNoResponseBody")
    public Person handleNoResponseBody(@RequestParam final String firstName, @RequestParam final String lastName) {
        return new Person(firstName, lastName);
    }

    @Uri("/handleAddCustomHeader")
    @ResponseBody
    public Person handleCustomHeader(@RequestParam final String firstName, @RequestParam final String lastName, WebScriptResponse response) {
        response.setHeader(firstName, lastName);
        return new Person(firstName, lastName);
    }
}
