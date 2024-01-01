package io.github.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import javax.ws.rs.NotAuthorizedException;
import java.io.IOException;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;

@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
    
    @ExceptionHandler(NotAuthorizedException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ResponseEntity<Object> handleUnauthorizedException(NotAuthorizedException notAuthorizedException){
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Hello");
    }

    @ExceptionHandler(IOException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<Object> handleIOException(IOException iOException){
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Hello");
    }

    @ExceptionHandler(ResponseStatusException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<Object> handleResponseStatusException(ResponseStatusException responseStatusException){
        return ResponseEntity.status(responseStatusException.getStatusCode()).body(responseStatusException.getBody());
    }

    // @ExceptionHandler(UnrecognizedPropertyException.class)
    // @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    // public ResponseEntity<Object> handleUnrecognizedPropertyException(UnrecognizedPropertyException unrecognizedPropertyException){
    //     // if(unrecognizedPropertyException.getMessage().contains("\"code\": 401")){
    //     //     System.out.println("HELLOHELLOHELLOHELLOHELLO");
    //     //     throw new NotAuthorizedException();
    //     // }
    //     return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Hello");
    // }

    // @AllArgsConstructor
    // @NoArgsConstructor
    // public static class GoogleErrorsWrapper {
    //     @Getter private 
    // }

    // @AllArgsConstructor
    // @NoArgsConstructor
    // public static class GoogleErrorsWrapper {
    //     @Getter private 
    // }
}