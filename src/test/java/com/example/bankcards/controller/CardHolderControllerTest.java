package com.example.bankcards.controller;

import static org.mockito.ArgumentMatchers.any;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureDataJpa;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.example.bankcards.dto.CardHolderDto;
import com.example.bankcards.dto.CardHolderRequestDto;
import com.example.bankcards.dto.CardHolderResponseDto;
import com.example.bankcards.service.CardHolderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import java.util.HashMap;
import java.util.Map;

import org.springframework.test.context.bean.override.mockito.MockitoBean;
import com.example.bankcards.entity.CardHolder;
import com.example.bankcards.entity.enums.Role;
import com.example.bankcards.repository.CardHolderRepository;
import com.example.bankcards.security.JwtService;
import com.example.bankcards.security.TestSecurityConfig;
import com.example.bankcards.service.BankCardService;

@WebMvcTest(CardHolderController.class)
@AutoConfigureMockMvc
@ExtendWith(MockitoExtension.class)
@Import(TestSecurityConfig.class)
@AutoConfigureDataJpa
public class CardHolderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CardHolderService cardHolderService;

    @MockitoBean
    private BankCardService bankCardService;

    @MockitoBean
    private JwtService jwtService;

    @Autowired
    private CardHolderRepository cardHoldersRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private final String BASE_URL = "/api/v1/card-holders";
    private String amdinEmail = "admin@email.com";
    private String adminPassword = "admin123456";
    private String adminName = "Admin";

    @BeforeEach
    void setUp() {
        // Очищаем тестовые данные перед каждым тестом
        cardHoldersRepository.deleteAll();
        // Создаем тестового пользователя для аутентификации
        CardHolder adminUser = new CardHolder();
        adminUser.setEmail(amdinEmail);
        adminUser.setName(adminName);
        adminUser.setPassword(passwordEncoder.encode(adminPassword));
        adminUser.setRole(Role.ROLE_ADMIN);
        cardHoldersRepository.save(adminUser);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createCardHolder_WithUserAdmin_ShouldReturnCreated() throws Exception {

        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("name", "John Doe");
        requestBody.put("email", "john.doe@example.com");
        requestBody.put("password", "password123");

        CardHolderResponseDto responseDto = CardHolderResponseDto.builder()
                .id(1L)
                .name("John Doe")
                .email("john.doe@example.com")
                .build();

        when(cardHolderService.createCardHolder(any(CardHolderRequestDto.class))).thenReturn(responseDto);

        mockMvc.perform(post(BASE_URL)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody))) // Используем Map
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.email").value("john.doe@example.com"))
                .andExpect(jsonPath("$.role").value("ROLE_USER"));
        verify(cardHolderService, times(1)).createCardHolder(any(CardHolderRequestDto.class));
    }

    @Test
    @WithMockUser(roles = "USER")
    void createCardHolder_WithUserRole_ShouldReturnForbidden() throws Exception {
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("name", "John Doe");
        requestBody.put("email", "john.doe@example.com");
        requestBody.put("password", "password123");

        mockMvc.perform(post(BASE_URL)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isForbidden());

        verify(cardHolderService, never()).createCardHolder(any());
    }
}