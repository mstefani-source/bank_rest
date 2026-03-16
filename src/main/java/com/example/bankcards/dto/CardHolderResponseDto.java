package com.example.bankcards.dto;

import com.example.bankcards.entity.enums.Role;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class CardHolderResponseDto {
    private Long id;
    private String name;
    private String email;
    @Builder.Default
    private Role role = Role.ROLE_USER;
}
