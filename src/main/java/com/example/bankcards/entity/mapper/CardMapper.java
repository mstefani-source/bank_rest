package com.example.bankcards.entity.mapper;

import org.springframework.stereotype.Component;

import com.example.bankcards.dto.BankCardDto;
import com.example.bankcards.entity.BankCard;

@Component
public class CardMapper {
    public BankCardDto toDto(BankCard card) {
        if (card == null) return null;
        
        BankCardDto dto = new BankCardDto();
        dto.setCardNumber(maskNumber(card.getNumber()));
        dto.setCustomerId(card.getCustomer().getId());
        return dto;
    }
    
    private String maskNumber(String number) {
        if (number == null || number.length() < 4) return number;
        return "**** **** **** " + number.substring(number.length() - 4);
    }
}
