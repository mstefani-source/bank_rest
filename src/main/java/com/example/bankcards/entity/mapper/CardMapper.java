package com.example.bankcards.entity.mapper;

import org.springframework.stereotype.Component;
import com.example.bankcards.dto.BankCardDto;
import com.example.bankcards.entity.BankCard;

@Component
public class CardMapper {

    public BankCardDto toDto(BankCard card) {
        if (card == null) {
            return null;
        }

        return BankCardDto.builder()
                .cardNumber(card.getCardNumber())
                .cardHolderId(card.getCardHolder().getId())
                .build();
    }

    // public BankCard toEntity(BankCardDto cardDto) {
    //     if (cardDto == null) {
    //         return null;
    //     }

    //     BankCard bankCard = cardRepository
    //             .findByCardNumber(cardDto.getCardNumber())
    //             .orElseThrow(() -> new CardNotFoundException("no such card"));

    //     return bankCard;
    // }
}
