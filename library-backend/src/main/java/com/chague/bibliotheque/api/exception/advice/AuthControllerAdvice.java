package com.chague.bibliotheque.api.exception.advice;

import com.chague.bibliotheque.api.exception.auth.InvalidCredentialsException;
import com.chague.bibliotheque.api.exception.auth.UnauthorizedException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class AuthControllerAdvice {

    @ExceptionHandler(InvalidCredentialsException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Map<String, Object> invalidCredentials(InvalidCredentialsException e) {
        return ApiErrorFactory.build(401, "Unauthorized", e.getMessage());
    }

    @ExceptionHandler(UnauthorizedException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Map<String, Object> unauthorized(UnauthorizedException e) {
        return ApiErrorFactory.build(401, "Unauthorized", e.getMessage());
    }
}
