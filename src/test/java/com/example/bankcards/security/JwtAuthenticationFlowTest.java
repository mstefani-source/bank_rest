package com.example.bankcards.security;

import com.example.bankcards.dto.AuthRequest;
import com.example.bankcards.dto.BankCardDto;
import com.example.bankcards.entity.CardHolder;
import com.example.bankcards.entity.enums.Role;
import com.example.bankcards.repository.CardHolderRepository;
import com.example.bankcards.security.auth.JwtAuthenticationResponse;
import com.example.bankcards.service.CardHolderService;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class JwtAuthenticationFlowTest {
        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @Autowired
        private CardHolderRepository cardHoldersRepository;

        @Autowired
        CardHolderService cardHolderService;

        @Autowired
        private PasswordEncoder passwordEncoder;

        private String testEmail = "test@example.com";
        private String testPassword = "password123";
        private String testName = "Test User";
        private Long testUserId;

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
        void fullJwtFlow_ShouldWork() throws Exception {

                // Шаг 1: Аутентификация пользователя admin

                AuthRequest adminAuthRequest = new AuthRequest();
                adminAuthRequest.setEmail(amdinEmail);
                adminAuthRequest.setPassword(adminPassword);

                MvcResult adminAuthResult = mockMvc.perform(post("/auth/sign-in")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(adminAuthRequest)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.token").exists())
                                .andReturn();

                JwtAuthenticationResponse adminAuthResponse = objectMapper.readValue(
                                adminAuthResult.getResponse().getContentAsString(),
                                JwtAuthenticationResponse.class);
                String adminAuthToken = adminAuthResponse.getToken();

                // Шаг 2: Используем токен для доступа к защищенному эндпоинту

                // Создаем тестового пользователя для аутентификации
                CardHolder testUser = new CardHolder();
                testUser.setEmail(testEmail);
                testUser.setName(testName);
                testUser.setPassword(passwordEncoder.encode(testPassword));
                CardHolder savedUser = cardHoldersRepository.save(testUser);
                testUserId = savedUser.getId();

                // Шаг 3: Создаем карту для тестового пользователя, используя админский токен

                BankCardDto cardDto = BankCardDto.builder()
                                .cardNumber("1111222233334444")
                                .cardHolderId(testUserId)
                                .build();

                mockMvc.perform(post("/api/v1/cards")
                                .header("Authorization", "Bearer " + adminAuthToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(cardDto)))
                                .andExpect(status().isCreated());

                // Шаг 4: Аутентификация User'a (логин)
                AuthRequest userAuthRequest = new AuthRequest();
                userAuthRequest.setEmail(testEmail);
                userAuthRequest.setPassword(testPassword);

                MvcResult authUserResult = mockMvc.perform(post("/auth/sign-in")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(userAuthRequest)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.token").exists())
                                .andReturn();

                // Получаем новый токен после аутентификации
                JwtAuthenticationResponse authResponse = objectMapper.readValue(
                                authUserResult.getResponse().getContentAsString(),
                                JwtAuthenticationResponse.class);
                String userLoginToken = authResponse.getToken();

                // Шаг 4: Используем новый токен для доступа к картам пользователя
                mockMvc.perform(get("/api/v1/my/cards", testUserId)
                                .header("Authorization", "Bearer " + userLoginToken)
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk());
        }
}
