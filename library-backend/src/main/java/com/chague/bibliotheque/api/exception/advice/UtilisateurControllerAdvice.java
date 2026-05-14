package com.chague.bibliotheque.api.exception.advice;

import com.chague.bibliotheque.api.exception.user.DeleteUtilisateurImpossibleException;
import com.chague.bibliotheque.api.exception.user.UtilisateurIsNotExistsException;
import com.chague.bibliotheque.api.exception.user.UtilisateurIsPresentException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class UtilisateurControllerAdvice {

    @ExceptionHandler(UtilisateurIsPresentException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String, Object> utilisateurIsExists(UtilisateurIsPresentException e) {
        return ApiErrorFactory.build(409, "Conflict", e.getMessage());
    }

    @ExceptionHandler(UtilisateurIsNotExistsException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, Object> utilisateurIsNotExists(UtilisateurIsNotExistsException e) {
        return ApiErrorFactory.build(404, "Not Found", e.getMessage());
    }

    @ExceptionHandler(DeleteUtilisateurImpossibleException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> deleteUtilisateurImpossible(DeleteUtilisateurImpossibleException e) {
        return ApiErrorFactory.build(400, "Bad Request", e.getMessage());
    }
}
