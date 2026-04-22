package org.example.ums.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class ErrorHandler {
    @ExceptionHandler
    @ResponseStatus(HttpStatus.UNAUTHORIZED) // 401
    public ErrorResponse handle(UnauthorizedException e) {
        log.warn(e.getMessage());
        return new ErrorResponse("Authorization required.", e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.FORBIDDEN) // 403
    public ErrorResponse handle(ForbiddenAccessException e) {
        log.warn(e.getMessage());
        return new ErrorResponse("Access denied.", e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND) // 404
    public ErrorResponse handle(NotFoundException e) {
        log.warn(e.getMessage());
        return new ErrorResponse("Object not found.", e.getMessage());
    }
}
