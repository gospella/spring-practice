package com.example.springpractice.task2.service;

import com.example.springpractice.task1.domain.Movie;
import com.example.springpractice.task1.event.MovieCreatedEvent;
import com.example.springpractice.task1.repository.MovieRepository;
import io.micrometer.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

/**
 * Новый сервис для фильмов.
 *
 * @author Klim Ross
 * @since 2023.12.02
 */
@Service
@Profile("task2")
@Slf4j
public class NewMovieService {

    private final MovieRepository movieRepository;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final NewMovieService self;

    public NewMovieService(MovieRepository movieRepository,
                           ApplicationEventPublisher applicationEventPublisher,
                           @Lazy NewMovieService self) {
        this.movieRepository = movieRepository;
        this.applicationEventPublisher = applicationEventPublisher;
        this.self = self;
    }

    /**
     * Сохранение фильма в рамках транзакции с публикаций события.
     */
    @Transactional
    public Movie createMovie(String name) {
        final Movie newMovie = movieRepository.save(new Movie("Movie 1"));
        final MovieCreatedEvent event = new MovieCreatedEvent(newMovie);
        applicationEventPublisher.publishEvent(event);

        //сервис, через который можно получить статус текущей транзакции
        TransactionStatus transactionStatus = TransactionAspectSupport.currentTransactionStatus();
        log.info("TransactionStatus: " + transactionStatus.getTransactionName());
        return newMovie;
    }

    /**
     * Создание фильма с последующим присвоением лейбла.
     * Реализовано неправильно: ожидается, что операции сохранения и присвоения лейбла будут выполняться
     * в рамках отдельных транзакций, что неверно - аннотации над методами не отработают, т.к. методы вызваны в обход прокси.
     */
    public Movie createMovieWithLabel(String name, String label) {
        // сохраняем фильм
        final Movie newMovie = createMovie(name);
        // вызываем код присвоения с брошенным исключением - транзакция не будет откатана
        assignLabelWithExeption(newMovie, label);
        return newMovie;
    }

    /**
     * Присвоение лейбла в рамках транзакции.
     * Логика по проверке запрещенного названия лейбла реализована неправильно,
     * т.к. лейбл проставляется до проверки - при фиксации транзакции данные будут закомичены даже без прямого вызова save().
     */
    @Transactional
    public void changeLabel(long id, String label) {
        Movie movie = movieRepository.getById(id);
        movie.setLabel(label);
        if ("forbidden".equals(label)) {
            movieRepository.save(movie);
        }
    }

    /**
     * Операция присвоения лейбла, в рамках которой бросается непроверяемое исключение.
     */
    @Transactional
    public void assignLabelWithExeption(Movie newMovie, String label) {
        throw new RuntimeException("Ошибка присвоения лейбла");
    }

    /**
     * Операция присвоения лейбла в рамках транзакции.
     * Реализовано неправильно: когда присвоения лейбла пройдет с иключением (передано невалидное название)
     * транзакция все равно будет зафиксирована, т.к. бросается непроверяемое исключение.
     */
    @Transactional
    public void assignLabelInTxn(Movie newMovie, String label) throws Exception {
        newMovie.setLabel(label);
        movieRepository.save(newMovie);
        if ("label".equals(label)) {
            throw new Exception("Нельзя присвоить лейбл - label");
        }
    }

    /*@Transactional
    private void privateTxnMethod() {
        // метод приватный, прокси создан не будет
    }*/

    /**
     * Операция сохранения фильма в рамках новой транзакции.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Movie createMovieInSeparateTxn(String name) {
        final Movie newMovie = movieRepository.save(new Movie(name));
        final MovieCreatedEvent event = new MovieCreatedEvent(newMovie);
        applicationEventPublisher.publishEvent(event);

        TransactionStatus transactionStatus = TransactionAspectSupport.currentTransactionStatus();
        return newMovie;
    }

    /**
     * Операция присвоения лейбла в рамках транзакции.
     * Реализовано неправильно: несмотря на то что используется self inject для вызова тразакционных методов -
     * одна из операций сервиса выполняется в рамках новой транзакции - таким образом при возникновении исключения
     * в рамках общего метода сохранение все равно будет закомичено.
     */
    @Transactional
    public Movie createMovieAndAssignLabelSeparatelyWithEx(String name, String label) throws Exception {
        Movie movie = self.createMovieInSeparateTxn(name);
        self.assignLabelInTxn(movie, label);
        if (name.equals(label)) {
            throw new RuntimeException("Название и лейбл не могут совпадать.");
        }
        return movie;
    }
}