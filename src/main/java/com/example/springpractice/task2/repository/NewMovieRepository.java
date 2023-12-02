package com.example.springpractice.task2.repository;

import com.example.springpractice.task1.domain.Movie;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Новый репозиторий для фильмов.*
 * @author Klim Ross
 * @since 2023.12.02
 */
@Profile("task2")
public interface NewMovieRepository extends JpaRepository<Movie, Long> {
}
