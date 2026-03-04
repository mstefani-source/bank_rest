package com.example.bankcards.service;

import java.math.BigDecimal;

import com.example.bankcards.repository.CardRepository;

// - Просматривает свои карты (поиск + пагинация)
// - Запрашивает блокировку карты
// - Делает переводы между своими картами
// - Смотрит баланс

public class CardService {

    private CardRepository cardRepository;

    public CardService (CardRepository cardRepository){
        this.cardRepository = cardRepository;
    }

    public void addBalance(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
      
    }
}
