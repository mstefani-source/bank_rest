package com.example.bankcards.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureDataJpa;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import com.fasterxml.jackson.databind.ObjectMapper;


import com.example.bankcards.dto.BankCardDto;
import com.example.bankcards.security.JwtService;
import com.example.bankcards.service.BankCardService;

@WebMvcTest(CardController.class)
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc(addFilters = false)
@AutoConfigureDataJpa
class CardControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private BankCardService cardService;

    @Test
    void createCard_ShouldReturnCreatedCard() throws Exception {
        // Подготовка тестовых данных
        BankCardDto inputDto = BankCardDto.builder()
                .cardNumber("1234567890123456")
                .cardHolderId(1L)
                .build();

        BankCardDto outputDto = BankCardDto.builder()
                .cardNumber("1234567890123456")
                .cardHolderId(1L)
                .build();

        // Настраиваем мок: когда вызывают createCard с любым BankCardDto, возвращаем
        // outputDto
        when(cardService.createCard(any(BankCardDto.class))).thenReturn(outputDto);

        // Выполняем POST запрос и проверяем результат
        mockMvc.perform(post("/api/v1/cards")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isCreated()) // Проверяем статус 201
                .andExpect(jsonPath("$.cardNumber").value("1234567890123456"))
                .andExpect(jsonPath("$.cardHolderId").value(inputDto.getCardHolderId()));
    }
}