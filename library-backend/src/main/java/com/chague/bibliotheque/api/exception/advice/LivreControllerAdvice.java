package com.chague.bibliotheque.api.exception.advice;

import com.chague.bibliotheque.api.exception.livre.DeleteLivreImpossibleException;
import com.chague.bibliotheque.api.exception.livre.LivreIsNotExistsException;
import com.chague.bibliotheque.api.exception.livre.LivreIsPresentException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class LivreControllerAdvice {

    @ExceptionHandler(LivreIsPresentException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String, Object> livreIsExists(LivreIsPresentException e) {
        return ApiErrorFactory.build(409, "Conflict", e.getMessage());
    }

    @ExceptionHandler(LivreIsNotExistsException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, Object> livreIsNotExists(LivreIsNotExistsException e) {
        return ApiErrorFactory.build(404, "Not Found", e.getMessage());
    }

    @ExceptionHandler(DeleteLivreImpossibleException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> deleteLivreImpossible(DeleteLivreImpossibleException e) {
        return ApiErrorFactory.build(400, "Bad Request", e.getMessage());
    }
}
