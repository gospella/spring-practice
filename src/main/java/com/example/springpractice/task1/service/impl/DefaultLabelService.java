package com.example.springpractice.task1.service.impl;

import com.example.springpractice.task1.domain.Movie;
import com.example.springpractice.task1.repository.MovieRepository;
import com.example.springpractice.task1.service.LabelService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.transaction.TransactionDefinition.PROPAGATION_REQUIRES_NEW;

/**
 * Имплементация сервиса для фильмов.
 *
 * @author Klim Ross
 * @since 2023.12.02
 */
@Service
public class DefaultLabelService implements LabelService {

    private final MovieRepository movieRepository;

    public DefaultLabelService(MovieRepository movieRepository) {
        this.movieRepository = movieRepository;
    }

    @Override
    @Transactional
    //@Transactional(PROPAGATION_REQUIRES_NEW)
    public void assignLabel(Movie movie) {
        final String label = "movie company label";
        movie.setLabel(label);
        movieRepository.save(movie);
    }
}
