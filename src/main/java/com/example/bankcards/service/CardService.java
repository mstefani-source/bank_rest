package com.example.bankcards.service;

import java.math.BigDecimal;
import java.util.Optional;

import com.example.bankcards.entity.BankCard;
import com.example.bankcards.entity.enums.CardStatus;
import com.example.bankcards.exception.CardNotFoundException;
import com.example.bankcards.exception.InsufficientFundsException;
import com.example.bankcards.repository.CardRepository;

// - Просматривает свои карты (поиск + пагинация)
// - Запрашивает блокировку карты
// - Делает переводы между своими картами
// - Смотрит баланс

public class CardService {

    private final CardRepository cardRepository;

    public CardService(CardRepository cardRepository) {
        this.cardRepository = cardRepository;
    }

    public BigDecimal updateBalance(Long cardNumber, BigDecimal amount) {
        
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

    public BigDecimal checkCardBalance(Long cardNumber) {

        BankCard card = cardRepository.findByCardNumber(cardNumber)
                .orElseThrow(() -> new CardNotFoundException(cardNumber));

        return card.getBalance();
    }

}
