package com.example.springpractice.task1.service;

import com.example.springpractice.task1.domain.Movie;

/**
 * Сервис для фильмов.
 *
 * @author Klim Ross
 * @since 2023.12.02
 */
public interface LabelService {

    void assignLabel(Movie movie);
}