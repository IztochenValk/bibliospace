package com.chague.bibliotheque.api.exception.advice;

import com.chague.bibliotheque.api.exception.categorie.CategorieIsNotExistsException;
import com.chague.bibliotheque.api.exception.categorie.CategorieIsPresentException;
import com.chague.bibliotheque.api.exception.categorie.DeleteCategorieImpossibleException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class CategorieControllerAdvice {

    @ExceptionHandler(CategorieIsPresentException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String, Object> categorieIsExists(CategorieIsPresentException e) {
        return ApiErrorFactory.build(409, "Conflict", e.getMessage());
    }

    @ExceptionHandler(CategorieIsNotExistsException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, Object> categorieIsNotExists(CategorieIsNotExistsException e) {
        return ApiErrorFactory.build(404, "Not Found", e.getMessage());
    }

    @ExceptionHandler(DeleteCategorieImpossibleException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> deleteCategorieImpossible(DeleteCategorieImpossibleException e) {
        return ApiErrorFactory.build(400, "Bad Request", e.getMessage());
    }
}
