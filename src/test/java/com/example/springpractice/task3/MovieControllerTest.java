package com.example.springpractice.task3;

import com.example.springpractice.task3.controller.MovieController;
import com.example.springpractice.task3.dto.Response;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;

/**
 * Тест для проверки ендпоинтов для фильмов
 *
 * @author Klim Ross
 * @since 2023.12.02
 */
@SpringBootTest
//@WebMvcTest(MovieController.class)
@AutoConfigureMockMvc
@ActiveProfiles("task3")
public class MovieControllerTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    /**
     * Вызывается эндпоинт для проверки обработки клиентской ошибки.
     * При возникновении ошибки в ответ присылается кастомным Response с информацией по ошибке и месседжу от сервера.
     */
    @Test
    @WithMockUser(value = "spring")
    public void testClientException() throws Exception {
        // отправляем запрос и ожидаем ответ OK
        String url = "/movie/exception";
        String resultJson = mockMvc.perform(MockMvcRequestBuilders.get(url)
                        .param("exception", "false")
                        .accept(APPLICATION_JSON_VALUE)
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        Response response = objectMapper.readValue(resultJson, Response.class);
        assertEquals("OK", response.getMessage());

        // отправляем запрос с требованием получить исключение
        resultJson = mockMvc.perform(MockMvcRequestBuilders.get(url)
                        .param("exception", "true")
                        .accept(APPLICATION_JSON_VALUE)
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().is4xxClientError())
                .andReturn()
                .getResponse()
                .getContentAsString();

        response = objectMapper.readValue(resultJson, Response.class);
        assertNotNull(response.getMessage());
        assertTrue(response.getMessageAdditional().contains("Ваши ожидания - Ваши проблемы"));
    }


    /**
     * Вызывается эндпоинт для сохранения фильма с последующем вызовом сервиса для присвоения лейбла.
     * Сервис для лейблон бросает исключение - ожидается что соотвествующий, обработчик словит ошибку и вернет response
     * с информацией по оишбке, а также данным по фильму.
     */
    @WithMockUser(value = "spring")
    @Test
    public void testServerException() throws Exception {
        // отправляем запрос на сохранение фильма
        String url = "/movie";
        String resultJson = mockMvc.perform(MockMvcRequestBuilders.post(url)
                        .with(csrf())
                        .content(objectMapper.writeValueAsString("какой то фильм"))
                        .accept(APPLICATION_JSON_VALUE)
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                // ожидаем что произойдет ошибка сервера, т.к. присвоение лейбла не работает
                .andExpect(status().is5xxServerError())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Response response = objectMapper.readValue(resultJson, Response.class);
        assertNotNull(response.getMessage());
        assertTrue(response.getMessage().contains("Фильм то сохранили, но лейбл присвоить не удалось."));
        assertTrue(response.getMessageAdditional().contains("Id фильма, если пригодится"));
    }
}
