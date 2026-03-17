package com.example.bankcards.repository;

import java.math.BigDecimal;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.bankcards.entity.CardHolder;

public interface CardHolderRepository extends JpaRepository<CardHolder, Long> {
    Optional<CardHolder> findByEmail(String email);
    BigDecimal findBalanceById(Long id);
}
