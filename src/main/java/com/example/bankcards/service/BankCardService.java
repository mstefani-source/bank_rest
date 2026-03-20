package com.example.bankcards.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.example.bankcards.dto.BankCardDto;
import com.example.bankcards.dto.BankCardResponseDto;
import com.example.bankcards.dto.CardHolderDto;
import com.example.bankcards.dto.TransferRequest;
import com.example.bankcards.entity.BankCard;
import com.example.bankcards.entity.CardHolder;
import com.example.bankcards.entity.enums.CardStatus;
import com.example.bankcards.entity.enums.Role;
import com.example.bankcards.entity.mapper.CardHolderMapper;
import com.example.bankcards.entity.mapper.CardMapper;
import com.example.bankcards.exception.CardHolderException;
import com.example.bankcards.exception.CardNotFoundException;
import com.example.bankcards.exception.InsufficientFundsException;
import com.example.bankcards.repository.CardHolderRepository;
import com.example.bankcards.repository.CardRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class BankCardService {

    private final CardRepository cardRepository;
    private final CardHolderRepository cardHoldersRepository;
    private final CardMapper cardMapper;
    private final EncryptionService encryptionService;
    private final CardHolderMapper cardHolderMapper;

    @Transactional
    public BankCardDto createCard(BankCardDto request) {
        log.info("Creating card for cardHolder ID: {}", request.getCardHolderId());

        CardHolder cardHolder = cardHoldersRepository.findById(request.getCardHolderId())
                .orElseThrow(() -> new CardHolderException(request.getCardHolderId()));
        CardHolderDto cardHolderDto = cardHolderMapper.fromEntityToDto(cardHolder);

        BankCard card = cardMapper.toEntity(request, cardHolderDto);

        if (card == null) {
            log.error("Card mapper returned null for request: {}", request);
            throw new IllegalStateException("Card mapper returned null");
        }

        BankCard savedCard = cardRepository.save(card);
        log.info("Card created for user: {}, last four: {}",
                getCurrentUsername(), savedCard.getLastFourDigits());
        return cardMapper.toMaskedDto(savedCard);
    }

    @Transactional(readOnly = true)
    public BigDecimal getCardBalanse(String cardNumber) {

        String searchHash = encryptionService.hashForSearch(cardNumber);

        BankCard card = cardRepository.findByCardNumberHash(searchHash)
                .orElseThrow(() -> new CardNotFoundException("Card not found"));

        checkCardOwnership(card);
        return card.getBalance() != null ? card.getBalance() : BigDecimal.ZERO;
    }

    @Transactional(readOnly = true)
    public List<BankCardDto> getUserCards(Pageable pageable) {
        Long userId = getCurrentUserId();
        Page<BankCard> cards = cardRepository.findByCardHolderId(userId, pageable);

        return cards.stream()
                .map(cardMapper::toMaskedDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public String getCardNumberForPayment(Long cardId) {
        BankCard card = cardRepository.findById(cardId)
                .orElseThrow(() -> new CardNotFoundException("Card not found"));
        checkCardOwnership(card);
        return encryptionService.decrypt(card.getCardNumberEncrypted());
    }

    @Transactional
    public void deleteCard(String cardNumber) {
        String searchHash = encryptionService.hashForSearch(cardNumber);
        BankCard card = cardRepository.findByCardNumberHash(searchHash)
                .orElseThrow(() -> new CardNotFoundException("Card not found"));

        checkCardOwnership(card);
        card.setStatus(CardStatus.BLOCKED);
        log.info("Card blocked for user: {}, last four: {}",
                getCurrentUsername(), card.getLastFourDigits());
    }

    public Page<BankCardResponseDto> getCardsWithAccessCheck(Pageable pageable) {

        CardHolderDto currentUser = getCurrentUser();

        Page<BankCard> cardPage;

        if (currentUser.getRole() == Role.ROLE_ADMIN) {
            cardPage = cardRepository.findAll(pageable);
            return cardPage.map(card -> {
                BankCardResponseDto dto = cardMapper.toMaskedAdminRespDto(card);
                return dto;
            });
        } else {
            cardPage = cardRepository.findByCardHolderId(currentUser.getId(), pageable);
        }

        return cardPage.map(card -> {
            BankCardResponseDto dto = cardMapper.toMaskedRespDto(card);
            return dto;
        });
    }

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

        if (currentUser.getRole() == Role.ROLE_ADMIN) {
            return;
        }

        if (card == null || card.getCardHolder() == null || !currentUser.getId().equals(card.getCardHolder().getId())) {
            throw new SecurityException("Access denied to this card");
        }
    }

    private String cleanCardNumber(String cardNumber) {
        if (cardNumber == null) {
            return null;
        }
        return cardNumber.replaceAll("[\\s-]", "");
    }

    @Transactional
    public void transfer(TransferRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Transfer request cannot be null");
        }

        String fromCardNumber = cleanCardNumber(request.getFromCardNumber());
        String toCardNumber = cleanCardNumber(request.getToCardNumber());
        BigDecimal amount = request.getAmount();

        if (fromCardNumber == null || fromCardNumber.isBlank() || toCardNumber == null || toCardNumber.isBlank()) {
            throw new IllegalArgumentException("Source and destination card numbers must be provided");
        }
        if (fromCardNumber.equals(toCardNumber)) {
            throw new IllegalArgumentException("Source and destination cards must be different");
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Transfer amount must be greater than zero");
        }

        String fromHash = encryptionService.hashForSearch(fromCardNumber);
        String toHash = encryptionService.hashForSearch(toCardNumber);

        BankCard fromCard = cardRepository.findByCardNumberHash(fromHash)
                .orElseThrow(() -> new CardNotFoundException(fromCardNumber));
        BankCard toCard = cardRepository.findByCardNumberHash(toHash)
                .orElseThrow(() -> new CardNotFoundException(toCardNumber));

        // Ensure the caller can operate on both cards
        checkCardOwnership(fromCard);
        checkCardOwnership(toCard);

        // Only allow transfers between active cards
        if (fromCard.getStatus() != CardStatus.ACTIVE) {
            throw new IllegalStateException("Source card is not active");
        }
        if (toCard.getStatus() != CardStatus.ACTIVE) {
            throw new IllegalStateException("Destination card is not active");
        }

        BigDecimal fromBalance = fromCard.getBalance() != null ? fromCard.getBalance() : BigDecimal.ZERO;
        if (fromBalance.compareTo(amount) < 0) {
            throw new InsufficientFundsException("Not enough funds on source card");
        }

        fromCard.setBalance(fromBalance.subtract(amount));

        BigDecimal toBalance = toCard.getBalance() != null ? toCard.getBalance() : BigDecimal.ZERO;
        toCard.setBalance(toBalance.add(amount));

        cardRepository.save(fromCard);
        cardRepository.save(toCard);

        log.info("Transfer {} from card {} to card {} for user {}", amount, fromCard.getLastFourDigits(),
                toCard.getLastFourDigits(), getCurrentUsername());
    }
}
