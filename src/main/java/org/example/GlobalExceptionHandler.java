package org.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.io.PrintWriter;
import java.io.StringWriter;

@Order(1)
@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @Autowired
    private EmailService emailService;

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)

    //is called whenever an error is thrown
    public ResponseEntity<String> handleAllExceptions(Exception ex, WebRequest request) {
        // Log the exception stack trace
        StringWriter sw = new StringWriter();
        ex.printStackTrace(new PrintWriter(sw));
        String stackTrace = sw.toString();

        // Send the stack trace via email
        emailService.send("support@arterialjs.org", "delaineabner47@gmail.com", "Elephant Application Error", stackTrace);

        return new ResponseEntity<>("An error occurred.", HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
