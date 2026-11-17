package com.attendease.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@ControllerAdvice
public class WebErrors {
    @ExceptionHandler(MissingException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    String missing(MissingException ex, Model model) { return page(model, ex.getMessage()); }

    @ExceptionHandler(DomainException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    String invalid(DomainException ex, Model model) { return page(model, ex.getMessage()); }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    String forbidden(Model model) { return page(model, "You do not have access to this page."); }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    String badInput(Model model) { return page(model, "Check the selected date, status, or identifier."); }

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    String conflict(Model model) { return page(model, "The change conflicts with existing data. Review the details and try again."); }

    private String page(Model model, String message) {
        model.addAttribute("message", message);
        return "error";
    }
}
