package com.example.springpractice.task1.listener;


import com.example.springpractice.task1.event.MovieCreatedEvent;
import com.example.springpractice.task1.service.LabelService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;


/**
 * Обработчик создания фильма. Вызывается сервис простановки лейбла.
 *
 * @author Klim Ross
 * @since 2023.12.02
 */
@Component
@Slf4j
@Profile("task1")
public class MovieCreatedEventProcessor {

    private final LabelService labelService;

    public MovieCreatedEventProcessor(LabelService labelService) {
        this.labelService = labelService;
    }

    /**
     * Отрабатывает синхронно внутри той же транзакции, что и сервис публикующий событие,
     * но если что-то пойдет не так с присвоением лейбла - фильм не будет создан.
     */
    @EventListener
    public void processMovieCreatedEventSameTxn(MovieCreatedEvent event) {
        log.info("processMovieCreatedEventSameTxn: Event is " + event);
        labelService.assignLabel(event.getMovie());
    }

    /**
     * Обработка лейбла после с фиксации транзакции - лейбл сохранен не будет, т.к. активная транзакция уже зафиксирована.
     * Для сохранений д.б. новая транзакция для сервиса ниже
     */
    @TransactionalEventListener
    public void processMovieCreatedEventTxnAfterCommit(MovieCreatedEvent event) {
        // обработка лейбла после с фиксации транзакции - лейбл сохранен не будет, т.к. активная транзакция уже зафиксирована.
        // для сохранений дб новая транзакция для сервиса ниже
        log.info("processMovieCreatedEventAfterCommit: Event is " + event);
        labelService.assignLabel(event.getMovie());
    }

    /**
     * Обработка только после отката транзакции.
     * Может использоваться для составления нотификаций об ошибке пользователям.
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_ROLLBACK)
    public void processMovieCreatedEventTxnAfterRollback(MovieCreatedEvent event) {
        log.info("processMovieCreatedEventAfterRollback: Event is " + event);
        log.info("sending notifications...");
    }

    /**
     * Обработка только после завершения транзакции.
     * Может использоваться для некоторой очиски ресурсов после того как фильм создан.
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMPLETION)
    public void processMovieCreatedEventTxnAfterCompletion(MovieCreatedEvent event) {
        log.info("processMovieCreatedEventAfterCompletion: Event is " + event);
        log.info("free resources...");
    }

    /**
     * Обработка внутри тразакции до ее фиксации - лейбл будет успешно проставлен и сохранен
     * Походит в ситуации, когда присвоение лейбла д.б. в рамках той же транзакции что и сохранение фильма.
     */
    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void processMovieCreatedEventTxnBeforeCommit(MovieCreatedEvent event) {
        log.info("processMovieCreatedEventBeforeCommit: Event is " + event);
        labelService.assignLabel(event.getMovie());
    }
}
