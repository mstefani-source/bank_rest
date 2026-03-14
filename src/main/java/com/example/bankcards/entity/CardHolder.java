package com.example.bankcards.entity;

import static jakarta.persistence.GenerationType.IDENTITY;

import java.util.ArrayList;
import java.util.List;

import com.example.bankcards.entity.enums.Role;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.ToString;

@Entity
@Data
@ToString
@Table(name = "cardholders")
public class CardHolder  {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotBlank
    @Column
    private String name;

    @NotBlank
    @Column(unique = true, nullable = false)
    private String email;

    @NotBlank
    @Column
    private String password;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role = Role.ROLE_USER;

    @Column
    @OneToMany(mappedBy = "cardHolder", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<BankCard> cards = new ArrayList<>();

}
