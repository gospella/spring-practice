package com.example.springpractice.task3.controller;

import com.example.springpractice.task1.domain.Movie;
import com.example.springpractice.task1.repository.MovieRepository;
import com.example.springpractice.task3.dto.Response;
import com.example.springpractice.task3.exception.MovieLabelException;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.security.Principal;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Обработчик ошибок для фильмов
 *
 * @author Klim Ross
 * @since 2023.12.02
 */
@ControllerAdvice(basePackageClasses = MovieController.class)
@Slf4j
public class MovieExceptionHandler extends ResponseEntityExceptionHandler {

    @Autowired
    private MovieRepository movieRepository;

    /**
     * Обработка ошибок типа {@link IllegalArgumentException}.
     * В рамках тестого примера возвращаем кастомный ответ клиенту, где в качетсве сообщения рассказываем ему в чем же он не прав.
     * <p>
     * В рамках обработчика есть доступ к исключениию, запросу в рамках которого случилась проблема.
     */
    @ExceptionHandler(value = {IllegalArgumentException.class})
    protected ResponseEntity<Object> handleIllegalArgs(RuntimeException ex, WebRequest request, HttpServletResponse resp, HttpSession session, Principal principle) {
        log.error("Обрабатываем ошибку", ex);

        //из обработчика есть доступ к изначальному запросу - достаним все параметры и вернем их обратно
        String params = request.getParameterMap().keySet().stream()
                .map(key -> key + ": " + request.getParameter(key))
                .collect(Collectors.joining("\n"));

        // аналогично при обращении к securityContext можно получить данные аутенцифицировавшегося пользователя (либо через аргументы)
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentPrincipalName = authentication.getName();

        // составляем кастомный респонс, где опишем ошибку клиента
        String customMessage = currentPrincipalName + ", Ваши ожидания - Ваши проблемы. Не нужно было присылать параметры: \n" + params;
        Response response = new Response(ex.getMessage(), customMessage);

        // при необходимости добавляем необходимые атрибуты http-ответа
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Content-Type", "application/json;charset=utf8");

        // используем сприговый сервис для построения ResponseEntity
        return handleExceptionInternal(ex, response, httpHeaders, HttpStatus.CONFLICT, request);
    }

    /**
     * Обработка ошибок типа {@link MovieLabelException} - ошибок присвоения лейбла для фильма.
     * В рамках тестового примера возвращаем кастомный ответ клиенту, где в качетсве сообщения описываем случившуюся проблему.
     * <p>
     * В рамках обработчика есть доступ к исключениию, запросу в рамках которого случилась проблема.
     */
    @ExceptionHandler(value = {MovieLabelException.class})
    protected ResponseEntity<Object> handleServerException(MovieLabelException ex, WebRequest request) {
        // составляем кастомный респонс, где опишем ошибку сервера
        Response response = new Response(ex.getMessage());
        response.setMessageAdditional("Id фильма, если пригодится: " + ex.getMovieId());

        // при необходимости добавляем необходимые атрибуты http-ответа
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Content-Type", "application/json;charset=utf8");

        // используем сприговый сервис для построения ResponseEntity
        return handleExceptionInternal(ex, response, httpHeaders, HttpStatus.INTERNAL_SERVER_ERROR, request);
    }
}