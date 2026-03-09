package com.example.bankcards.service;

import java.math.BigDecimal;
import org.springframework.data.domain.Pageable;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.bankcards.dto.BankCardDto;
import com.example.bankcards.entity.BankCard;
import com.example.bankcards.entity.CardHolder;
import com.example.bankcards.entity.enums.CardStatus;
import com.example.bankcards.entity.mapper.CardMapper;
import com.example.bankcards.exception.CardAlreadyExistsException;
import com.example.bankcards.exception.CardNotFoundException;
import com.example.bankcards.exception.InsufficientFundsException;
import com.example.bankcards.exception.CardHolderException;
import com.example.bankcards.repository.CardHoldersRepository;
import com.example.bankcards.repository.CardRepository;

import lombok.extern.log4j.Log4j2;

// - Просматривает свои карты (поиск + пагинация)
// - Запрашивает блокировку карты
// - Делает переводы между своими картами
// - Смотрит баланс

@Service
@Log4j2
public class CardService {

    private final CardRepository cardRepository;
    private final CardHoldersRepository cardHoldersRepository;

    private final CardMapper cardMapper;

    public CardService(CardRepository cardRepository, CardHoldersRepository cardHoldersRepository,
            CardMapper cardMapper) {
        this.cardRepository = cardRepository;
        this.cardHoldersRepository = cardHoldersRepository;
        this.cardMapper = cardMapper;
    }

    public BigDecimal changeBalance(String cardNumber, BigDecimal amount) {

        BankCard card = cardRepository.findByCardNumber(cardNumber)
                .orElseThrow(() -> new CardNotFoundException(cardNumber));

        if (CardStatus.ACTIVE.compareTo(card.getStatus()) != 0) {
            throw new IllegalStateException("Card is not active. Status: " + card.getStatus());
        }

        BigDecimal newBalance = card.getBalance().add(amount);
        if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new InsufficientFundsException(
                    "Insufficient funds. Current balance: " + card.getBalance());
        }
        card.setBalance(card.getBalance().add(amount));
        return cardRepository.save(card).getBalance();
    }

    public BigDecimal checkCardBalance(String cardNumber) {

        BankCard card = cardRepository.findByCardNumber(cardNumber)
                .orElseThrow(() -> new CardNotFoundException(cardNumber));

        return card.getBalance();
    }

    @Transactional
    public void delete(String cardNumber) {
        BankCard card = cardRepository
                .findByCardNumber(cardNumber)
                .orElseThrow(() -> new CardNotFoundException("No such cardNumber"));
        card.setStatus(CardStatus.BLOCKED);
        cardRepository.save(card);
    }

    public Page<BankCardDto> findAll(Long customerId, Pageable pageable) {
        Page<BankCard> cards = cardRepository.findByCustomerId(customerId, pageable);
        return cards.map(cardMapper::toDto);
    }

    @Transactional
    public BankCardDto create(BankCardDto cardDto) {

        CardHolder cardHolder = cardHoldersRepository
                    .findById(cardDto.getCardHolderId())
                    .orElseThrow(() -> new CardHolderException(cardDto.getCardHolderId()));
    
        BankCard newCard = new BankCard();
        newCard.setCardHolder(cardHolder);
        newCard.setCardNumber(cardDto.getCardNumber());
        try {
            BankCard savedCard = cardRepository.save(newCard);
            log.info("BankCard saved successfully with id: {}", savedCard.getId());
            return cardMapper.toDto(savedCard);

        } catch (DataAccessException e) {
            log.error("Database error while saving BankCard: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to save bank card due to database error", e);

        } catch (RuntimeException e) {
            log.error("Unexpected error while saving BankCard: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to save bank card", e);
        }
    }

}
