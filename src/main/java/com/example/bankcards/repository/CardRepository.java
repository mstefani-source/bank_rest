package com.example.bankcards.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.bankcards.entity.BankCard;
import com.example.bankcards.entity.enums.CardStatus;


/*
  - Просматривает свои карты (поиск + пагинация)
  - Запрашивает блокировку карты
  - Делает переводы между своими картами
  - Смотрит баланс
*/ 

public interface CardRepository extends JpaRepository<BankCard, Long> {
    // Optional<BankCard> findByCardNumber(String cardNumber);
    List<BankCard> findAllById(Long holderId);
    Page<BankCard> findByCardHolderId(Long customerId, Pageable pageable);
    Optional<BankCard> findByCardNumberHash(String cardNumberHash);
    Optional<BankCard> findByLastFourDigits(String lastFourDigits);
    List<BankCard> findByStatus(CardStatus status);
    // List<BankCard> findByCardHolderId(Long cardHolderId);
}
