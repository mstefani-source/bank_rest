package com.example.bankcards.entity.mapper;

import com.example.bankcards.dto.CardHolderDto;
import com.example.bankcards.dto.CardHolderRequestDto;
import com.example.bankcards.dto.CardHolderResponseDto;
import com.example.bankcards.entity.CardHolder;
import org.springframework.stereotype.Component;

@Component
public class CardHolderMapper {

    public CardHolderResponseDto ToDto(CardHolder cardHolder) {

        CardHolderResponseDto cardHolderDto = new CardHolderResponseDto();

        cardHolderDto.setId(cardHolder.getId());
        cardHolderDto.setName(cardHolder.getName());
        cardHolderDto.setEmail(cardHolder.getEmail());
        cardHolderDto.setRole(cardHolder.getRole());
        return cardHolderDto;
    }

    public CardHolder ToEntity(CardHolderRequestDto cardHolderDto) {

        CardHolder cardHolder = new CardHolder();

        cardHolder.setId(cardHolderDto.getId());
        cardHolder.setName(cardHolderDto.getName());
        cardHolder.setEmail(cardHolderDto.getEmail());
        cardHolder.setPassword(cardHolderDto.getPassword());

        return cardHolder;
    }

    public CardHolderDto toUserDetails(CardHolder entity) {
        if (entity == null) {
            return null;
        }

        return CardHolderDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .email(entity.getEmail())
                .password(entity.getPassword())
                .role(entity.getRole())
                .build();
    }

    public CardHolderDto toUserDetails(CardHolderResponseDto entity) {
        if (entity == null) {
            return null;
        }

        return CardHolderDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .email(entity.getEmail())
                .role(entity.getRole())
                .build();
    }

}
