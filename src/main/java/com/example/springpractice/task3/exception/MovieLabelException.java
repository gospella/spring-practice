package com.example.springpractice.task3.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Ошибка простановки лейбла
 *
 * @author Klim Ross
 * @since 2023.12.02
 */
@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
@Getter
public class MovieLabelException extends RuntimeException {
    private static final String MESSAGE = "Ошибка присвоения лейбла для фильма";

    private String movieId;

    public MovieLabelException() {
        super(MESSAGE);
    }

    public MovieLabelException(String message, Throwable cause) {
        super(message, cause);
    }

    public MovieLabelException(String message) {
        super(message);
    }

    public MovieLabelException(Throwable cause) {
        super(cause);
    }

    public MovieLabelException(String message, String movieId) {
        super(message);
        this.movieId = movieId;
    }
}