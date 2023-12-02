package com.example.springpractice.task1.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Сервис для авторов.
 *
 * @author Klim Ross
 * @since 2023.12.02
 */
@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class Author {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String name;
}
