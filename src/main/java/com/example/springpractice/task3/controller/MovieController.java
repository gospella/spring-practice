package com.example.springpractice.task3.controller;

import com.example.springpractice.task1.domain.Movie;
import com.example.springpractice.task1.service.MovieService;
import com.example.springpractice.task3.dto.Response;
import com.example.springpractice.task3.exception.MovieLabelException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * Контроллер для фильмов.
 *
 * @author Klim Ross
 * @since 2023.12.02
 */
@RestController
@RequestMapping("/movie")
public class MovieController {

    @Autowired
    private MovieService movieService;

    /**
     * Эндпоинт для проверки обработки ошибки.
     * Бросается исключение при совпадении значения параметра, ожидается последующая обработка и отправка ответа через обработчик.
     *
     * @param exception если true, то бросаем исключением и ожидаем, что хендлер обработает исключение и отправит response с ошибкой клиента.
     */
    @GetMapping(value = "/exception", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<Response> movieWithException(@RequestParam(required = true, defaultValue = "false") boolean exception) {
        // Если прислали флаг exception = true, то кидаем исключение с описанием "Ваши проблемы"
        if (exception) {
            throw new IllegalArgumentException("Что-то не так");
        }
        return new ResponseEntity<>(new Response("OK"), HttpStatus.OK);
    }

    /**
     * Ендпоинт для проверки сохранения фильма.
     * Вызывается сервис сохранения фильма с последующим присвоением лейбла.
     * Ожидается, что при возникновении серверных ошибок отработаем excepptionHandler с отправкой соответвующего response.
     *
     * @param movieName название фильма.
     */
    @Transactional
    @PostMapping(consumes = {MediaType.APPLICATION_JSON_VALUE}, produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<Response> create(@RequestBody final String movieName) {
        TransactionStatus transactionStatus = TransactionAspectSupport.currentTransactionStatus();
        Movie movie = movieService.createMovie(movieName);

        Response created = new Response("CREATED");
        created.setMessageAdditional(movie.getId().toString());
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }
}