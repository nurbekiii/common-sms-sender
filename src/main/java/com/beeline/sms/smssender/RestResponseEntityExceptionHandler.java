package com.beeline.sms.smssender;

import com.beeline.sms.model.Response;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * @author NIsaev on 04.06.2019
 */
@RestControllerAdvice
public class RestResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(value = {IllegalArgumentException.class, IllegalStateException.class, InvalidFormatException.class, Exception.class})
    protected ResponseEntity<Response> handleConflict(
            RuntimeException ex, WebRequest request) {
        String bodyOfResponse = "Bad request";
        return getErrorResponse(bodyOfResponse + "\n\r" + request.toString() + "\n\r" + ex.toString());
    }


    private ResponseEntity<Response> getErrorResponse(String errorMessage) {
        return new ResponseEntity<>(new Response("ERROR", errorMessage), HttpStatus.valueOf(400));
    }
}
