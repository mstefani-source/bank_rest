package com.example.bankcards.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.example.bankcards.dto.AuthRequest;
import com.example.bankcards.dto.BankCardDto;
import com.example.bankcards.dto.CardHolderDto;
import com.example.bankcards.entity.BankCard;
import com.example.bankcards.entity.CardHolder;
import com.example.bankcards.entity.enums.Role;
import com.example.bankcards.entity.mapper.CardMapper;
import com.example.bankcards.exception.CardNotFoundException;
import com.example.bankcards.repository.CardHolderRepository;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.security.auth.JwtAuthenticationResponse;

import lombok.extern.slf4j.Slf4j;

@Slf4j
// @ExtendWith(MockitoExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class BankCardServiceTest {

    @Autowired
    private CardHolderRepository cardHoldersRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private CardMapper cardMapper;

    @Autowired
    private EncryptionService encryptionService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private BankCardService bankCardService;

    private Long adminId;
    private Long userId;
    private String adminToken;
    private String userToken;

    private final String adminEmail = "admin@test.com";
    private final String adminPassword = "V04pvZe3cMJs8s";
    private final String adminName = "Test Admin";

    private final String userEmail = "user@test.com";
    private final String userPassword = "V04pvZe3cMJs8s";
    private final String userName = "Test User";

    private final String cardNumber = "1111222233334444";

    @BeforeAll
    void setUpOnce() throws Exception {
        // Очищаем базу один раз перед всеми тестами
        cardHoldersRepository.deleteAll();

        // Создаем администратора
        CardHolder admin = new CardHolder();
        admin.setEmail(adminEmail);
        admin.setName(adminName);
        admin.setPassword(passwordEncoder.encode(adminPassword));
        admin.setRole(Role.ROLE_ADMIN);
        admin = cardHoldersRepository.save(admin);
        adminId = admin.getId();

        AuthRequest adminAuthRequest = new AuthRequest();
        adminAuthRequest.setEmail(adminEmail);
        adminAuthRequest.setPassword(adminPassword);

        MvcResult adminRegisterResult = mockMvc.perform(post("/auth/sign-in")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(adminAuthRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andReturn();

        JwtAuthenticationResponse adminAuthenticationResponse = objectMapper.readValue(
                adminRegisterResult.getResponse().getContentAsString(),
                JwtAuthenticationResponse.class);
        adminToken = adminAuthenticationResponse.getToken();

        // Создаем обычного пользователя
        CardHolder user = new CardHolder();
        user.setEmail(userEmail);
        user.setName(userName);
        user.setPassword(passwordEncoder.encode(userPassword));
        user.setRole(Role.ROLE_USER);
        user = cardHoldersRepository.save(user);
        userId = user.getId();

        AuthRequest userAuthRequest = new AuthRequest();
        userAuthRequest.setEmail(userEmail);
        userAuthRequest.setPassword(userPassword);

        MvcResult userRegisterResult = mockMvc.perform(post("/auth/sign-in")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userAuthRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andReturn();

        userToken = objectMapper.readValue(
                userRegisterResult.getResponse().getContentAsString(),
                JwtAuthenticationResponse.class).getToken();

        log.info("created Admin ID: {}", adminId);
        log.info("created Admin token: {}", adminToken);
        log.info("created User ID: {}", userId);
        log.info("created User token: {}", userToken);

    }

    @BeforeEach
    void setUp() {
        // originalContext = SecurityContextHolder.getContext();
        // SecurityContextHolder.setContext(mock(SecurityContext.class));
        // cardHoldersRepository.deleteAll();
        CardHolderDto testUser = CardHolderDto.builder()
                .id(1L)
                .name("Test User")
                .email("test@example.com")
                .password("password")
                .role(Role.ROLE_USER)
                .build();

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(testUser, null,
                testUser.getAuthorities());

        SecurityContextHolder.getContext().setAuthentication(auth);

        BankCardDto createdCard = BankCardDto.builder()
                .cardNumber(cardNumber)
                .cardHolderId(userId)
                .build();

        BankCardDto card = bankCardService.createCard(createdCard);
        Pageable pageable = PageRequest.of(0, 10);

        Page<BankCard> cardPage = cardRepository.findByCardHolderId(userId, pageable);

        cardPage.getContent().forEach(crd -> log.info("Card in DB: {}, Holder ID: {}", crd.getPlainCardNumber(),
                crd.getCardHolder().getId()));
        log.info("+++++++++++++++++++++++++++++++++++++++++++++++++++");

        // BankCard cardEntity = cardPage.getContent().stream()
        // .filter(crd -> crd.getPlainCardNumber().equals(createdCard.getCardNumber()))
        // .findFirst()
        // .orElseThrow(() -> new RuntimeException("Card not found"));

        // cardEntity.setBalance(new BigDecimal("10.50"));
        // cardRepository.save(cardEntity);
        log.info("created card {} for Holder: {}", card.getCardNumber(), card.getCardHolderId());

    }

    // @AfterEach
    // void tearDown() {
    // SecurityContextHolder.setContext(originalContext);
    // reset(cardRepository, cardMapper, encryptionService);
    // }

    @Test
    void getCardBalanse_ReturnsBalance_WhenCardBelongsToCurrentUser() {

        BigDecimal result = bankCardService.getCardBalanse(cardNumber);

        assertEquals(new BigDecimal("10.50"), result);
    }

    // @Test
    // void getCardBalanse_ReturnsZero_WhenBalanceIsNull() {
    // String cardNumber = "1111222233334444";
    // String hash = "test-hash";

    // CardHolderDto currentUser = CardHolderDto.builder()
    // .id(1L)
    // .email("user@example.com")
    // .role(Role.ROLE_USER)
    // .build();

    // Authentication auth = mock(Authentication.class);
    // when(auth.getPrincipal()).thenReturn(currentUser);
    // when(SecurityContextHolder.getContext().getAuthentication()).thenReturn(auth);

    // BankCard card = new BankCard();
    // card.setCardHolder(new CardHolder());
    // card.getCardHolder().setId(1L);
    // card.setBalance(null);

    // when(encryptionService.hashForSearch(cardNumber)).thenReturn(hash);
    // when(cardRepository.findByCardNumberHash(hash)).thenReturn(Optional.of(card));

    // BigDecimal result = bankCardService.getCardBalanse(cardNumber);

    // assertEquals(BigDecimal.ZERO, result);
    // }

    // @Test
    // void getCardBalanse_ThrowsWhenCardNotFound() {
    // String cardNumber = "1111222233334444";
    // String hash = "test-hash";
    // when(encryptionService.hashForSearch(cardNumber)).thenReturn(hash);
    // when(cardRepository.findByCardNumberHash(hash)).thenReturn(Optional.empty());
    // assertThrows(CardNotFoundException.class, () ->
    // bankCardService.getCardBalanse(cardNumber));
    // }
}
