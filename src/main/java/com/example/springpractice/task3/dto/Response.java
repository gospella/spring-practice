package com.example.springpractice.task3.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Дто для респонсов
 *
 * @author Klim Ross
 * @since 2023.12.02
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Response {

    private String message;

    private String messageAdditional;

    public Response(String message) {
        this.message = message;
    }
}