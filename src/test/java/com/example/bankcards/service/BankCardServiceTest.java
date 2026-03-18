package com.example.bankcards.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
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
import org.springframework.data.domain.PageImpl;
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
import com.example.bankcards.entity.enums.CardStatus;
import com.example.bankcards.entity.enums.Role;
import com.example.bankcards.entity.mapper.CardHolderMapper;
import com.example.bankcards.entity.mapper.CardMapper;
import com.example.bankcards.exception.CardNotFoundException;
import com.example.bankcards.repository.CardHolderRepository;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.security.auth.JwtAuthenticationResponse;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class BankCardServiceTest {
        @Mock
        private CardRepository cardRepository;
        @Mock
        private CardHolderRepository cardHolderRepository;
        @Mock
        private CardMapper cardMapper;
        @Mock
        private EncryptionService encryptionService;
        @Mock
        private CardHolderMapper cardHolderMapper;
        @InjectMocks
        private BankCardService bankCardService;

        private Long userId = 1L;
        private String cardNumber = "1111222233334444";
        private String hash = "test-hash";

        @BeforeEach
        void setUp() {
                CardHolderDto cardHolderDto = CardHolderDto.builder()
                                .id(1L)
                                .name("Test User")
                                .email("test@example.com")
                                .password("V04pvZe3cMJstqL")
                                .role(Role.ROLE_USER)
                                .build();

                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(cardHolderDto, null,
                                cardHolderDto.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(auth);

                CardHolder cardHolderEntity = new CardHolder();
                cardHolderEntity.setId(cardHolderDto.getId());
                cardHolderEntity.setEmail("test@example.com");

                when(cardHolderRepository.findById(cardHolderDto.getId())).thenReturn(Optional.of(cardHolderEntity));
                when(cardHolderMapper.fromEntityToDto(cardHolderEntity)).thenReturn(cardHolderDto);

                BankCardDto bankCardDto = BankCardDto.builder()
                                .cardNumber(cardNumber)
                                .cardHolderId(cardHolderDto.getId())
                                .build();

                BankCard cardFromMapper = new BankCard();
                cardFromMapper.setId(1L);
                cardFromMapper.setPlainCardNumber(cardNumber);
                cardFromMapper.setCardNumberHash("test-hash");
                cardFromMapper.setCardNumberEncrypted("encrypted");
                cardFromMapper.setLastFourDigits("4444");
                cardFromMapper.setStatus(CardStatus.ACTIVE);
                cardFromMapper.setBalance(BigDecimal.valueOf(10.50));
                cardFromMapper.setCardHolder(cardHolderEntity);

                BankCard savedCard = new BankCard();
                savedCard.setId(1L);
                savedCard.setCardNumberHash("test-hash");
                savedCard.setPlainCardNumber(cardNumber);
                savedCard.setCardNumberEncrypted("encrypted");
                savedCard.setLastFourDigits("4444");
                savedCard.setStatus(CardStatus.ACTIVE);
                savedCard.setBalance(BigDecimal.valueOf(10.50));
                savedCard.setCardHolder(cardHolderEntity);

                BankCardDto expectedBankCardDto = BankCardDto.builder()
                                .cardNumber("1111222233334444")
                                .cardHolderId(userId)
                                .build();

                log.info("CardHolderDto in setUp: {}", cardHolderDto);
                when(cardMapper.toEntity(bankCardDto, cardHolderDto)).thenReturn(cardFromMapper);
                when(cardRepository.save(cardFromMapper)).thenReturn(savedCard);
                when(cardMapper.toMaskedDto(savedCard)).thenReturn(expectedBankCardDto);
                BankCardDto card = bankCardService.createCard(bankCardDto);

                Pageable pageable = PageRequest.of(0, 10);
                List<BankCard> cardList = new ArrayList<>();
                cardList.add(savedCard);
                Page<BankCard> mockPage = new PageImpl<>(cardList, pageable, cardList.size());

                // МОК ДЛЯ findByCardHolderId
                when(cardRepository.findByCardHolderId(eq(userId), any(Pageable.class)))
                                .thenReturn(mockPage);

                Page<BankCard> cardPage = cardRepository.findByCardHolderId(userId, pageable);

                cardPage.getContent()
                                .forEach(crd -> log.info("Card in DB: {}, Holder ID: {}", crd.getPlainCardNumber(),
                                                crd.getCardHolder().getId()));
                log.info("created card {} for Holder: {}", card.getCardNumber(), card.getCardHolderId());
        }


        @Test
        void getCardBalanse_ReturnsBalance_WhenCardBelongsToCurrentUser() {
                // String hash = "test-hash";
                when(encryptionService.hashForSearch(cardNumber)).thenReturn(hash);

                // 2. Создаем карту для возврата
                BankCard mockCard = new BankCard();
                mockCard.setId(1L);
                mockCard.setPlainCardNumber(cardNumber);
                mockCard.setCardNumberHash(hash);
                mockCard.setCardNumberEncrypted("encrypted");
                mockCard.setLastFourDigits("4444");
                mockCard.setStatus(CardStatus.ACTIVE);
                mockCard.setCardHolder(new CardHolder());
                mockCard.getCardHolder().setId(userId);
                mockCard.setBalance(new BigDecimal("10.50"));

                when(cardRepository.findByCardNumberHash(hash)).thenReturn(Optional.of(mockCard));

                BigDecimal result = bankCardService.getCardBalanse(cardNumber);
                assertEquals(0, new BigDecimal("10.50").compareTo(result));
                verify(encryptionService).hashForSearch(cardNumber);
                verify(cardRepository).findByCardNumberHash(hash);
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
