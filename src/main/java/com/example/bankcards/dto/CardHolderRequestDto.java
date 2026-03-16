package com.example.bankcards.dto;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class CardHolderRequestDto {
    private String name;
    private String email;
    private String password;
}
