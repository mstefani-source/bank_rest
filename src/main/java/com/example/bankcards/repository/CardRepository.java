package com.example.bankcards.repository;


import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.bankcards.entity.BankCard;


/*
  - Просматривает свои карты (поиск + пагинация)
  - Запрашивает блокировку карты
  - Делает переводы между своими картами
  - Смотрит баланс
*/ 

public interface CardRepository extends JpaRepository<BankCard, Long> {
    List<BankCard> findBycardNumber(Long cardNumber);    
}
