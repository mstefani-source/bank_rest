package com.example.bankcards.entity.mapper;

import com.example.bankcards.dto.CardHolderDto;
import com.example.bankcards.dto.CardHolderRequestDto;
import com.example.bankcards.dto.CardHolderResponseDto;
import com.example.bankcards.entity.CardHolder;
import org.springframework.stereotype.Component;

@Component
public class CardHolderMapper {

    public CardHolderResponseDto ToDto(CardHolder cardHolder) {

        return CardHolderResponseDto.builder()
                .id(cardHolder.getId())
                .build();
    }

    public CardHolder ToEntity(CardHolderRequestDto cardHolderDto) {

        CardHolder cardHolder = new CardHolder();

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

    public CardHolder toEntity(CardHolderDto dto) {
        if (dto == null) {
            return null;
        }

        CardHolder cardHolder = new CardHolder();
        cardHolder.setId(dto.getId());
        cardHolder.setName(dto.getName());
        cardHolder.setEmail(dto.getEmail());
        cardHolder.setPassword(dto.getPassword());
        cardHolder.setRole(dto.getRole());

        return cardHolder;
    }

    // public CardHolderDto toUserDetails(CardHolderResponseDto entity) {
    // if (entity == null) {
    // return null;
    // }

    // return CardHolderDto.builder()
    // .id(entity.getId())
    // .name(entity.getName())
    // .email(entity.getEmail())
    // .role(entity.getRole())
    // .build();
    // }

}
