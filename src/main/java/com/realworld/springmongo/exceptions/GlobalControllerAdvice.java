package com.realworld.springmongo.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;

import java.util.*;

@RestControllerAdvice
public class GlobalControllerAdvice {

    @ExceptionHandler(InvalidRequestException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    public InvalidRequestExceptionResponse invalidRequestExceptionHandler(InvalidRequestException e) {
        var subject = e.getSubject();
        var violation = e.getViolation();
        var errors = Map.of(subject, List.of(violation));
        return new InvalidRequestExceptionResponse(errors);
    }

    @ExceptionHandler(WebExchangeBindException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    public InvalidRequestExceptionResponse handleConstraintViolationException(WebExchangeBindException ex) {
        Map<String, List<String>> body = new HashMap<>();
        for (FieldError fieldError : ex.getFieldErrors()) {
            body.putIfAbsent(fieldError.getField(), new ArrayList<>());
            var errors = body.get(fieldError.getField());
            errors.add(fieldError.getDefaultMessage());
        }
        return new InvalidRequestExceptionResponse(body);
    }

    public class InvalidRequestExceptionResponse {
        public final Map<String, List<String>> errors;

        public InvalidRequestExceptionResponse(Map<String, List<String>> errors) {
            this.errors = errors;
        }

        public Map<String, List<String>> getErrors() {
            return errors;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            InvalidRequestExceptionResponse that = (InvalidRequestExceptionResponse) obj;
            return errors.equals(that.errors);
        }

        @Override
        public int hashCode() {
            return Objects.hash(errors);
        }

        @Override
        public String toString() {
            return "InvalidRequestExceptionResponse{" +
                    "errors=" + errors +
                    '}';
        }
    }

}
