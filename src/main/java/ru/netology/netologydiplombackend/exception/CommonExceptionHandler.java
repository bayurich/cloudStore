package ru.netology.netologydiplombackend.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.netology.netologydiplombackend.model.ErrorResponse;

@RestControllerAdvice
public class CommonExceptionHandler {

    private static final Logger LOG = LoggerFactory.getLogger(CommonExceptionHandler.class);

    @ExceptionHandler(InputDataException.class)
    public ResponseEntity<ErrorResponse> inputDataHandler(InputDataException e) {
        return getResponseEntity(e, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorResponse> invalidCredentialsHandler(InvalidCredentialsException e) {
        return getResponseEntity(e, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> runtimeExceptionHandler(Exception e) {
        return getResponseEntity(e, HttpStatus.INTERNAL_SERVER_ERROR);
    }


    private ResponseEntity<ErrorResponse> getResponseEntity(Exception e, HttpStatus status) {
        System.out.println("!!!!!!!!!!!!!!!!!!!!!!!! error: " + e);
        return new ResponseEntity<>(new ErrorResponse(e.getMessage(), status.value()), status);
    }
}
