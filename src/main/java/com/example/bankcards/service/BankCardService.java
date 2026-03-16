package com.example.bankcards.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.example.bankcards.dto.BankCardDto;
import com.example.bankcards.dto.CardHolderDto;
import com.example.bankcards.dto.TransferRequest;
import com.example.bankcards.entity.BankCard;
import com.example.bankcards.entity.enums.CardStatus;
import com.example.bankcards.entity.enums.Role;
import com.example.bankcards.entity.mapper.CardMapper;
import com.example.bankcards.exception.CardNotFoundException;
import com.example.bankcards.repository.CardRepository;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class BankCardService {

    private final CardRepository cardRepository;
    private final CardMapper cardMapper;
    private final EncryptionService encryptionService;

    @Transactional
    public BankCardDto createCard(BankCardDto request) {

        // Маппим DTO в Entity (автоматически использует текущего пользователя)
        BankCard card = cardMapper.toEntity(request);

        // Здесь нужно сохранить связь с CardHolder
        // card.setCardHolderId(getCurrentUserId()); // если есть такое поле

        BankCard savedCard = cardRepository.save(card);

        log.info("Card created for user: {}, last four: {}",
                getCurrentUsername(), savedCard.getLastFourDigits());

        return cardMapper.toMaskedDto(savedCard);
    }

    @Transactional(readOnly = true)
    public BankCardDto getCard(String cardNumber) {
        // Ищем по хешу
        String searchHash = encryptionService.hashForSearch(cardNumber);

        BankCard card = cardRepository.findByCardNumberHash(searchHash)
                .orElseThrow(() -> new CardNotFoundException("Card not found"));

        // Маппер сам проверит права доступа
        return cardMapper.toMaskedDto(card);
    }

    @Transactional(readOnly = true)
    public List<BankCardDto> getUserCards(Pageable pageable) {
        Long userId = getCurrentUserId();

        // Ищем все карты пользователя
        Page<BankCard> cards = cardRepository.findByCardHolderId(userId, pageable);

        return cards.stream()
                .map(cardMapper::toMaskedDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public String getCardNumberForPayment(Long cardId) {
        BankCard card = cardRepository.findById(cardId)
                .orElseThrow(() -> new CardNotFoundException("Card not found"));

        // Проверяем права доступа
        checkCardOwnership(card);

        // Расшифровываем для платежа
        return encryptionService.decrypt(card.getCardNumberEncrypted());
    }

    @Transactional
    public void deleteCard(String cardNumber) {
        String searchHash = encryptionService.hashForSearch(cardNumber);

        BankCard card = cardRepository.findByCardNumberHash(searchHash)
                .orElseThrow(() -> new CardNotFoundException("Card not found"));

        // Проверяем права доступа
        checkCardOwnership(card);

        // Мягкое удаление или блокировка
        card.setStatus(CardStatus.BLOCKED);

        log.info("Card blocked for user: {}, last four: {}",
                getCurrentUsername(), card.getLastFourDigits());
    }

    public Page<BankCardDto> getCardsWithAccessCheck(Long cardHolderId, Pageable pageable) {

        CardHolderDto currentUser = getCurrentUser();

        Page<BankCard> cardPage;

        if (currentUser.getRole() == Role.ROLE_ADMIN) {
            if (cardHolderId != null) {
                cardPage = cardRepository.findByCardHolderId(cardHolderId, pageable);
            } else {
                cardPage = cardRepository.findAll(pageable);
            }
        }
        else {
            cardPage = cardRepository.findByCardHolderId(currentUser.getId(), pageable);
        }

        return cardPage.map(card -> {
            BankCardDto dto = cardMapper.toMaskedDto(card);
            return dto;
        });
    }

    // Вспомогательные методы

    private CardHolderDto getCurrentUser() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CardHolderDto) {
            return (CardHolderDto) authentication.getPrincipal();
        }
        throw new IllegalStateException("No authenticated user");
    }

    private Long getCurrentUserId() {
        return getCurrentUser().getId();
    }

    private String getCurrentUsername() {
        return getCurrentUser().getUsername();
    }

    private void checkCardOwnership(BankCard card) {
        CardHolderDto currentUser = getCurrentUser();

        // ADMIN может всё
        if (currentUser.getRole() == Role.ROLE_ADMIN) {
            return;
        }

        // Проверяем, что карта принадлежит пользователю
        // if (!card.getCardHolderId().equals(currentUser.getId())) {
        // throw new SecurityException("You don't have permission to access this card");
        // }
    }

    public void transfer(TransferRequest request) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'transfer'");
    }
}
