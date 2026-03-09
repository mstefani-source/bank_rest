package com.example.bankcards.entity.mapper;

import com.example.bankcards.dto.CardHolderDto;
import com.example.bankcards.entity.CardHolder;
import org.springframework.stereotype.Component;


@Component
public class CardHolderMapper {

    public CardHolderDto ToDto(CardHolder cardHolder) {

        CardHolderDto customerDto = new CardHolderDto();

        customerDto.setId(cardHolder.getId());
        customerDto.setName(cardHolder.getName());
        customerDto.setEmail(cardHolder.getEmail());
        customerDto.setPassword(cardHolder.getPassword());
        customerDto.setRole(cardHolder.getRole());

        return customerDto;
    }

    public CardHolder ToEntity (CardHolderDto cardHolderDto) {

        CardHolder cardHolder = new CardHolder();

        cardHolder.setId(cardHolderDto.getId());
        cardHolder.setName(cardHolderDto.getName());
        cardHolder.setEmail(cardHolderDto.getEmail());
        cardHolder.setPassword(cardHolderDto.getPassword());

        return cardHolder;
    }

}
