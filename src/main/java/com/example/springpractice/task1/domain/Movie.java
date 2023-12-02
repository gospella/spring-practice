package com.example.springpractice.task1.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Сущность фильма.
 *
 * @author Klim Ross
 * @since 2023.12.02
 */
@Data
@Entity
@EntityListeners({AuditingEntityListener.class})
public class Movie {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotNull
    @NotBlank
    private String name;

    private String label;

    @CreatedDate
    private LocalDateTime createdAt;

    @ManyToMany(targetEntity = Author.class, fetch = FetchType.EAGER)
    private List<Author> authors;

    public Movie() {
    }

    public Movie(String name) {
        this.name = name;
    }
}