package com.example.springpractice.task1.service;

import com.example.springpractice.task1.domain.Movie;
import com.example.springpractice.task1.event.MovieCreatedEvent;
import com.example.springpractice.task1.repository.MovieRepository;
import jakarta.transaction.Transactional;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

/**
 * Сервис для фильмов.
 *
 * @author Klim Ross
 * @since 2023.12.02
 */
@Service
public class MovieService {

    private final MovieRepository movieRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    public MovieService(MovieRepository movieRepository, ApplicationEventPublisher applicationEventPublisher) {
        this.movieRepository = movieRepository;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Transactional
    public Movie createMovie(String name) {
        final Movie newMovie = movieRepository.save(new Movie("Movie 1"));
        final MovieCreatedEvent event = new MovieCreatedEvent(newMovie);
        applicationEventPublisher.publishEvent(event);
        return newMovie;
    }
}