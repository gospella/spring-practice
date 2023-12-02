package com.example.springpractice.task2;

import com.example.springpractice.task1.domain.Movie;
import com.example.springpractice.task1.repository.MovieRepository;
import com.example.springpractice.task2.repository.NewMovieRepository;
import com.example.springpractice.task2.service.NewMovieService;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.EventListener;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Тестирование нового сервиса для фильмов. Рассмотрение вариантов ошибочного использования транзакций.
 *
 * @author Klim Ross
 * @since 2023.12.02
 */
@SpringBootTest
@Slf4j
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@ActiveProfiles("task2")
public class NewMovieServiceTest {

    public static final String TEST_MOVIE_NAME = "Movie 1";
    public static final String TEST_MOVIE_LABEL = "label";
    @Autowired
    private NewMovieService movieService;

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private NewMovieRepository newMovieRepository;

    /**
     * Вызов сервиса для сохранение фильма.
     * Проверка работы с сущностями в состоянии managed
     * Объекты, полученные в рамках одной транзакции, находятся в managed - все изменения для них будут фиксироваться.
     */
    @Test
    public void saveIsNotCalledButEntityUpdated() {
        final Movie createdMovie = movieService.createMovie(TEST_MOVIE_NAME);

        // вызов сервиса сохранения фильмов
        movieService.changeLabel(createdMovie.getId(), "forbidden");
        final Optional<Movie> updatedMovie = movieRepository.findById(createdMovie.getId());
        assertTrue(updatedMovie.isPresent());
        // роверяем, что переданный лейбл был присвоен
        assertEquals("forbidden", updatedMovie.get().getLabel());
    }

    /**
     * Создание фильма с последующим присвоением лейбла.
     * Исключение случится, но отката сохранения не будет т.к. аннотированный метод д.б. общедоступным
     * и вызываться из другого компонента, либо через self inject, а не в обход прокси.
     */
    @Test
    public void callTransactionMethodBehindProxy() {
        // сохранение фильма с исключением
        assertThrows(RuntimeException.class, () -> movieService.createMovieWithLabel(TEST_MOVIE_NAME, "label 1"));

        final Optional<Movie> updatedMovie = movieRepository.findById(1L);
        // сущность сохранена несмотря на исключение
        assertTrue(updatedMovie.isPresent(), "ошибка внутри транзакции, но сущность сохранена");
    }

    /**
     * Вызов сервиса для сохранение фильма.
     * Неправильная обработка исключений может привести к фиксированию транзакци, т.е.
     * если обрабатываем исключение, тобросаме его повторно как непроверяемое,
     * либо устанавливаем параметр rollbackOn для соответсвующих непроверяемых исключений.
     */
    @Test
    public void checkedExceptionInTxnMethod() {
        // сохранение фильма
        final Movie createdMovie = movieService.createMovie(TEST_MOVIE_NAME);
        // присвоение лейбла с исключением
        assertThrows(Exception.class, () -> movieService.assignLabelInTxn(createdMovie, TEST_MOVIE_LABEL));

        final Optional<Movie> updatedMovie = movieRepository.findById(1L);
        assertTrue(updatedMovie.isPresent());
        // несмотря на исключение лейбл присовен
        assertEquals(TEST_MOVIE_LABEL, updatedMovie.get().getLabel(), "лейбл присвоен, т.к. исключение было проверяемым");
    }

    /**
     * Вызов сервиса для сохранение фильма.
     * если обернуть часть кода в другой метод, с тразакцией с типом {@link Propagation#REQUIRES_NEW}
     * то создается новая транзакция и после окончания метода сброс/фиксация выполняется сразу.
     */
    @Test
    public void saveLogicInMultipleTxns() {
        // сохранение фильма и присвоение лейбла с исключением
        assertThrows(RuntimeException.class, () -> movieService.createMovieAndAssignLabelSeparatelyWithEx(TEST_MOVIE_NAME, TEST_MOVIE_NAME));

        final Optional<Movie> updatedMovie = movieRepository.findById(1L);
        // фильм ошибочно сохранен, т.к. выполнялся в рамках отдельной тразнакции, которая была ролбекнута
        assertTrue(updatedMovie.isPresent());
        assertEquals(TEST_MOVIE_NAME, updatedMovie.get().getName(), "фильм сохранен, т.к. выполнение шло в отдельной транзакции");
        // лейбл не присвоен
        assertNull(updatedMovie.get().getLabel(), "лейбл не присвоен, т.к. эта часть не была зафиксирована");
    }

    /**
     * Для дальнейшего анализа информацию по транзакции можно получить через соответсвующий статус
     * либо через логи, при простановке соответсвующего проперти logging.level.org.springframework.transaction=DEBUG
     */
    @Test
    @Transactional
    public void testTransactionStatus() {
        assertTrue(TransactionSynchronizationManager.isActualTransactionActive());
        assertTrue(TestTransaction.isActive());
        assertTrue(TestTransaction.isFlaggedForRollback());

        // сохранение фильма
        final Movie createdMovie = movieService.createMovie(TEST_MOVIE_NAME);
        final Optional<Movie> updatedMovie = movieRepository.findById(createdMovie.getId());
        assertTrue(updatedMovie.isPresent());
    }

    @Test
    @Transactional
    public void saveInMultipleDataSource() {
        // Два разных источника данных, например, мы создали новую версию хранилища данных, но все еще должны некоторое время поддерживать старую.
        // в этом случае только один save будет обрабатываться транзакционно
        // use ChainedTransactionManager или JtaTransactionManager

        final Movie newMovieA = movieRepository.save(new Movie(TEST_MOVIE_NAME));
        final Movie newMovieB = newMovieRepository.save(new Movie(TEST_MOVIE_NAME));
        //todo
    }
}
