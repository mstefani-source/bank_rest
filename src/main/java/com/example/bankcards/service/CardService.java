package com.example.bankcards.service;

import java.math.BigDecimal;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import com.example.bankcards.dto.BankCardDto;
import com.example.bankcards.entity.BankCard;
import com.example.bankcards.entity.enums.CardStatus;
import com.example.bankcards.entity.mapper.CardMapper;
import com.example.bankcards.exception.CardNotFoundException;
import com.example.bankcards.exception.InsufficientFundsException;
import com.example.bankcards.repository.CardRepository;

// - Просматривает свои карты (поиск + пагинация)
// - Запрашивает блокировку карты
// - Делает переводы между своими картами
// - Смотрит баланс

@Service
public class CardService {

    private final CardRepository cardRepository;
    private final CardMapper cardMapper;

    public CardService(CardRepository cardRepository, CardMapper cardMapper) {
        this.cardRepository = cardRepository;
        this.cardMapper = cardMapper;
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

    public void delete(Long id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'delete'");
    }

    public Page<BankCardDto> findAll(Long customerId, Pageable pageable) {

        // if (customerId == null) { 
        //     return Page.empty(pageable);
        // }
        
        Page<BankCard> cards = cardRepository.findByCustomerId(customerId, pageable);

        return cards.map(cardMapper::toDto);
    }

    public BankCardDto create(BankCardDto request) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'create'");
    }

}
