package com.example.springpractice.task3.listener;


import com.example.springpractice.task1.event.MovieCreatedEvent;
import com.example.springpractice.task1.service.LabelService;
import com.example.springpractice.task3.exception.MovieLabelException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;


/**
 * Обработчик создания фильма.
 *
 * @author Klim Ross
 * @since 2023.12.02
 */
@Component
@Slf4j
@Profile("task3")
public class MovieCreatedEventProcessor {

    private final LabelService labelService;

    public MovieCreatedEventProcessor(LabelService labelService) {
        this.labelService = labelService;
    }

    /**
     * Операция присвоения лейбла для фильма, которая по каким то причинам не реализована.
     * Бросает кастомное исключение {@link MovieLabelException}
     */
    @EventListener
    public void processMovieCreated(MovieCreatedEvent event) {
        String message = "Фильм то сохранили, но лейбл присвоить не удалось.";
        throw new MovieLabelException(message, event.getMovie().getId().toString());
    }
}
