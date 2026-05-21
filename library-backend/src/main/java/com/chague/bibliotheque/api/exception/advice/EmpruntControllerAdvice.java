package com.chague.bibliotheque.api.exception.advice;

import com.chague.bibliotheque.api.exception.emprunt.EmpruntImpossibleException;
import com.chague.bibliotheque.api.exception.emprunt.EmpruntIsNotExistsException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class EmpruntControllerAdvice {

    @ExceptionHandler(EmpruntIsNotExistsException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, Object> empruntIsNotExists(EmpruntIsNotExistsException e) {
        return ApiErrorFactory.build(404, "Not Found", e.getMessage());
    }

    @ExceptionHandler(EmpruntImpossibleException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String, Object> empruntImpossible(EmpruntImpossibleException e) {
        return ApiErrorFactory.build(409, "Conflict", e.getMessage());
    }
}
