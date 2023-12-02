package com.example.springpractice.task1.repository;

import com.example.springpractice.task1.domain.Movie;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Репозиторий для фильмов.
 *
 * @author Klim Ross
 * @since 2023.12.02
 */
public interface MovieRepository extends JpaRepository<Movie, Long> {
}
