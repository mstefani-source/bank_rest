package com.example.bankcards.repository;


import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.bankcards.entity.BankCard;


/*
  - Просматривает свои карты (поиск + пагинация)
  - Запрашивает блокировку карты
  - Делает переводы между своими картами
  - Смотрит баланс
*/ 

public interface CardRepository extends JpaRepository<BankCard, Long> {
    Optional<BankCard> findByCardNumber(Long cardNumber);
    BigDecimal findByBalance(Long cardNumber);
}
