package pl.jakubkonkol.tasteitserver.controller;

import org.apache.coyote.BadRequestException;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import pl.jakubkonkol.tasteitserver.exception.AccountDoesNotExistException;
import pl.jakubkonkol.tasteitserver.exception.ApiRequestException;
import pl.jakubkonkol.tasteitserver.exception.IncorrectPasswordException;
import pl.jakubkonkol.tasteitserver.exception.ResourceNotFoundException;
import pl.jakubkonkol.tasteitserver.model.ErrorResponse;

import java.util.NoSuchElementException;

@RestControllerAdvice
public class ErrorController {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception e) {
        return buildErrorResponse(e, 500);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(ResourceNotFoundException e) {
        return buildErrorResponse(e, 404);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> handleBadRequestException(BadRequestException e) {
        return buildErrorResponse(e, 400);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException e) {
        return buildErrorResponse(e, 400);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> handleAccountDoesNotExistException(AccountDoesNotExistException e) {
        return buildErrorResponse(e, 400);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> handleNoSuchElementException(NoSuchElementException e) {
        return buildErrorResponse(e, 400);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> handleApiRequestException(ApiRequestException e) {
        return buildErrorResponse(e, 400);
    }
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> handleIncorrectPasswordException(IncorrectPasswordException e) {
        return buildErrorResponse(e, 400);
    }

    private ResponseEntity<ErrorResponse> buildErrorResponse(Exception e, int status) {
        ErrorResponse errorResponse = new ErrorResponse(status, e.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatusCode.valueOf(status));
    }
}
