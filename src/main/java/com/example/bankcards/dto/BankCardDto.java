package com.example.bankcards.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

import com.example.bankcards.entity.enums.CardStatus;

@Data
public class BankCardDto {
    private Long cardNumber;
    private LocalDate expireDate;
    private CardStatus status;
    private BigDecimal balance;
}
