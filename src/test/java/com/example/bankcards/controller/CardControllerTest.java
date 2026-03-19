package com.example.bankcards.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
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
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.web.config.EnableSpringDataWebSupport;

import com.example.bankcards.dto.BankCardDto;
import com.example.bankcards.dto.BankCardResponseDto;
import com.example.bankcards.dto.TransferRequest;
import com.example.bankcards.security.JwtService;
import com.example.bankcards.service.BankCardService;

@WebMvcTest(CardController.class)
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc(addFilters = false)
@AutoConfigureDataJpa
@EnableSpringDataWebSupport(pageSerializationMode = EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO)
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

                when(cardService.createCard(any(BankCardDto.class))).thenReturn(outputDto);

                mockMvc.perform(post("/api/v1/cards")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(inputDto)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.cardNumber").value("1234567890123456"))
                                .andExpect(jsonPath("$.cardHolderId").value(inputDto.getCardHolderId()));
        }

        @Test
        void deleteCard_ShouldReturnNoContent() throws Exception {
                doNothing().when(cardService).deleteCard("1234567890123456");

                mockMvc.perform(delete("/api/v1/cards/1234567890123456"))
                                .andExpect(status().isNoContent());
        }

        @Test
        void getCard_ShouldReturnBalance() throws Exception {
                when(cardService.getCardBalanse("1234567890123456")).thenReturn(new BigDecimal("100.50"));

                mockMvc.perform(get("/api/v1/cards/1234567890123456"))
                                .andExpect(status().isOk())
                                .andExpect(content().string("100.50"));
        }

        @Test
        void getCards_ShouldReturnPage() throws Exception {
                BankCardResponseDto card1 = BankCardResponseDto.builder()
                                .cardNumber("1234567890123456")
                                .build();
                BankCardResponseDto card2 = BankCardResponseDto.builder()
                                .cardNumber("9876543210987654")
                                .build();

                when(cardService.getCardsWithAccessCheck(any(Pageable.class)))
                                .thenReturn(new PageImpl<>(List.of(card1, card2), PageRequest.of(0, 10), 2));

                mockMvc.perform(get("/api/v1/my/cards")
                                .param("page", "0")
                                .param("size", "10"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.content").isArray())
                                .andExpect(jsonPath("$.content[0].cardNumber").value("1234567890123456"))
                                .andExpect(jsonPath("$.content[1].cardNumber").value("9876543210987654"))
                                .andExpect(jsonPath("$.page.totalElements").value(2))
                                .andExpect(jsonPath("$.page.number").value(0))
                                .andExpect(jsonPath("$.page.size").value(10));
        }

        @Test
        void transferBetweenOwnCards_ShouldReturnSuccessMessage() throws Exception {
                TransferRequest request = TransferRequest.builder()
                                .fromCardNumber("1234567890123456")
                                .toCardNumber("9876543210987654")
                                .amount(new BigDecimal("50.00"))
                                .build();

                doNothing().when(cardService).transfer(any(TransferRequest.class));

                mockMvc.perform(post("/api/v1/my/cards")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk())
                                .andExpect(content().string("Перевод успешно выполнен"));
        }
}
