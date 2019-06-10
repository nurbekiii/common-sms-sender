package com.beeline.sms.smssender;

import com.beeline.sms.model.Response;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * @author NIsaev on 04.06.2019
 */
@RestControllerAdvice
public class RestResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {
    private static Logger logger = LogManager.getLogger(RestResponseEntityExceptionHandler.class);

    @ExceptionHandler(value = {IllegalArgumentException.class, IllegalStateException.class, InvalidFormatException.class, Exception.class})
    public ResponseEntity<?> handleConflict(RuntimeException ex) {
        logger.error("RuntimeException {0}", ex);
        return new ResponseEntity<>(new Response("ERROR", ex.toString()), HttpStatus.valueOf(400));
    }

    private ResponseEntity<Response> getErrorResponseEx(String errorMessage) {
        return new ResponseEntity<>(new Response("ERROR", errorMessage), HttpStatus.valueOf(400));
    }
}
