package com.example.springpractice.task1;

import com.example.springpractice.task1.domain.Movie;
import com.example.springpractice.task1.event.MovieCreatedEvent;
import com.example.springpractice.task1.listener.MovieCreatedEventProcessor;
import com.example.springpractice.task1.repository.MovieRepository;
import com.example.springpractice.task1.service.MovieService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.event.ApplicationEventMulticaster;
import org.springframework.context.event.SmartApplicationListener;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EventListener;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Тестирование сервиса для работы с сущностями фильмов.
 * Рассмотрены варианты использования @TransactionalEventListener
 *
 * @author Klim Ross
 * @since 2023.12.02
 */
@SpringBootTest
@Slf4j
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@ActiveProfiles("task1")
public class MovieServiceTest {

    public static final String TEST_MOVIE_NAME = "Movie 1";
    @Autowired
    private MovieService movieService;

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private ApplicationEventMulticaster applicationEventMulticaster;

    @SpyBean
    private MovieCreatedEventProcessor movieCreatedEventProcessor;

    /**
     * Сохранение фильма с последующей публикацией события, которое будет обрабатываться стандартным слушателем {@link EventListener}.
     * Сервис присовения лейбла отрабатывает в рамках той же транзакции.
     * При возникновении исключений откатится также создание фильма.
     */
    @Test
    public void shouldPersistMovieWithDefaultListener() {
        //отработает только processMovieCreatedEventSameTxn
        workingListeners("processMovieCreatedEventSameTxn");

        final Movie createdMovie = movieService.createMovie(TEST_MOVIE_NAME);

        // ожидаем, что фильм сохранился
        final Optional<Movie> persistedMovieOpt = movieRepository.findById(createdMovie.getId());
        assertEquals(TEST_MOVIE_NAME, createdMovie.getName());
        verify(movieCreatedEventProcessor, times(1))
                .processMovieCreatedEventSameTxn(any(MovieCreatedEvent.class));
        assertNotNull(createdMovie.getLabel());

        assertTrue(persistedMovieOpt.isPresent());
        // ожидаем, что отработает проставление лейбла
        assertEquals(createdMovie.getLabel(), persistedMovieOpt.get().getLabel(), "лейбл сохранен");
    }

    /**
     * Сохранение фильма с последующей публикацией события, которое будет обрабатываться через {@link TransactionalEventListener}.
     * По умолчанию {@link TransactionPhase#AFTER_COMMIT} - сервис присовения лейбла отрабатывает после фиксации транзакции.
     * При исключениях сохранение фильма откатываться не будет.
     */
    @Test
    public void shouldPersistMovieWithTxnAfterCommit() {
        //отработает только processMovieCreatedEventTxnAfterCommit
        workingListeners("processMovieCreatedEventTxnAfterCommit");

        final Movie createdMovie = movieService.createMovie(TEST_MOVIE_NAME);

        // ожидаем, что фильм сохранился
        final Optional<Movie> persistedMovieOpt = movieRepository.findById(createdMovie.getId());
        assertEquals(TEST_MOVIE_NAME, createdMovie.getName());
        verify(movieCreatedEventProcessor, times(1))
                .processMovieCreatedEventTxnAfterCommit(any(MovieCreatedEvent.class));
        assertNotNull(createdMovie.getLabel());

        assertTrue(persistedMovieOpt.isPresent());
        // лейбл не присвоен т.к. сохранение было уже после фиксации транзакции
        assertNotEquals(createdMovie.getLabel(), persistedMovieOpt.get().getLabel(), "лейбл не был сохранен");
    }

    /**
     * Сохранение фильма с последующей публикацией события, которое будет обрабатываться через {@link TransactionalEventListener}.
     * {@link TransactionPhase#AFTER_ROLLBACK} - лисенер который отработает только после фиксации транзакции
     * При исключениях сохранение фильма откатываться не будет.
     */
    @Test
    public void shouldPersistMovieWithTxnAfterRollback() {
        // отработает только processMovieCreatedEventTxnAfterRollback
        workingListeners("processMovieCreatedEventTxnAfterRollback");

        //when
        final Movie createdMovie = movieService.createMovie(TEST_MOVIE_NAME);

        // ожидаем, что фильм сохранился
        final Optional<Movie> persistedMovieOpt = movieRepository.findById(createdMovie.getId());
        assertEquals(TEST_MOVIE_NAME, createdMovie.getName());

        // исключений не было, лисенер не отработал
        verify(movieCreatedEventProcessor, times(0))
                .processMovieCreatedEventTxnAfterRollback(any(MovieCreatedEvent.class));
        assertNull(createdMovie.getLabel(), "простановщик лейбла не был вызван");
        assertTrue(persistedMovieOpt.isPresent());
    }

    /**
     * Сохранение фильма с последующей публикацией события, которое будет обрабатываться через {@link TransactionalEventListener}.
     * {@link TransactionPhase#AFTER_ROLLBACK} - лисенер который отработает только после фиксации транзакции
     * При исключениях сохранение фильма откатываться не будет.
     */
    @Test
    public void shouldPersistMovieWithTxnAfterCompletion() {
        //отработает только processMovieCreatedEventTxnAfterCompletion
        workingListeners("processMovieCreatedEventTxnAfterCompletion");

        final Movie createdMovie = movieService.createMovie(TEST_MOVIE_NAME);

        // ожидаем, что фильм сохранился
        final Optional<Movie> persistedMovieOpt = movieRepository.findById(createdMovie.getId());
        assertEquals(TEST_MOVIE_NAME, createdMovie.getName());
        verify(movieCreatedEventProcessor, times(1))
                .processMovieCreatedEventTxnAfterCompletion(any(MovieCreatedEvent.class));
        //assertNotNull(createdMovie.getLabel());

        assertTrue(persistedMovieOpt.isPresent());
        // лейбл не присвоен т.к. сохранение было уже после фиксации транзакции
        assertNull(persistedMovieOpt.get().getLabel(), "лейбл не был сохранен");
    }

    /**
     * Сохранение фильма с последующей публикацией события, которое будет обрабатываться через {@link TransactionalEventListener}.
     * {@link TransactionPhase#BEFORE_COMMIT} - лисенер отработает только до фиксации транзакции
     * При исключениях присвоения лейбла будет откат.
     */
    @Test
    public void shouldPersistMovieWithTxnBeforeCommit() {
        //отработает только processMovieCreatedEventTxnBeforeCommit
        workingListeners("processMovieCreatedEventTxnBeforeCommit");

        //when
        final Movie createdMovie = movieService.createMovie(TEST_MOVIE_NAME);

        //then
        final Optional<Movie> persistedMovieOpt = movieRepository.findById(createdMovie.getId());
        assertEquals(TEST_MOVIE_NAME, createdMovie.getName());
        verify(movieCreatedEventProcessor, times(1))
                .processMovieCreatedEventTxnBeforeCommit(any(MovieCreatedEvent.class));
        assertNotNull(createdMovie.getLabel());

        assertTrue(persistedMovieOpt.isPresent());
        // ожидаем, что отработает проставление лейбла
        assertEquals(createdMovie.getLabel(), persistedMovieOpt.get().getLabel(), "лейбл сохранен");
    }

    /**
     * Очищает неиспользуемые в тесте обработчики.
     */
    private void workingListeners(String... methodNames) {
        List<String> listenersToDelete = new ArrayList<>(Arrays.asList("processMovieCreatedEventSameTxn",
                "processMovieCreatedEventTxnAfterCommit",
                "processMovieCreatedEventTxnAfterRollback",
                "processMovieCreatedEventTxnAfterCompletion",
                "processMovieCreatedEventTxnBeforeCommit"));
        listenersToDelete.removeAll(Arrays.asList(methodNames));
        listenersToDelete.forEach(name -> {
            applicationEventMulticaster.removeApplicationListeners(l -> l instanceof SmartApplicationListener
                    && ((SmartApplicationListener) l).getListenerId().contains(name));
        });
    }
}
