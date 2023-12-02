package com.example.springpractice.task1.event;

import com.example.springpractice.task1.domain.Movie;
import org.springframework.context.ApplicationEvent;

/**
 * Событие создания фильма.
 *
 * @author Klim Ross
 * @since 2023.12.02
 */
public class MovieCreatedEvent extends ApplicationEvent {

    private final Movie movie;

    public MovieCreatedEvent(Movie movie) {
        super(movie);
        this.movie = movie;
    }

    public Movie getMovie() {
        return movie;
    }
}