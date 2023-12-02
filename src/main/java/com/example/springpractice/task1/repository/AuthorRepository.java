package com.example.springpractice.task1.repository;

import com.example.springpractice.task1.domain.Author;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Сервис для авторов.
 *
 * @author Klim Ross
 * @since 2023.12.02
 */
public interface AuthorRepository extends JpaRepository<Author, Long> {
}
