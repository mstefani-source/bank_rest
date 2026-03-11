package com.example.bankcards.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import com.example.bankcards.dto.BankCardDto;
import com.example.bankcards.service.BankCardService;
import org.springframework.web.bind.annotation.RequestBody;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/api/v1/cards/customers")
@RequiredArgsConstructor
public class CardHoldersController {
    private final BankCardService cardService;

    @PostMapping
    @ResponseStatus(code = HttpStatus.CREATED)
    public BankCardDto createCard(@Valid @RequestBody BankCardDto cardDto) {
        return cardService.createCard(cardDto);
    }

    @DeleteMapping("{cardNumber}")
    public ResponseEntity<Void> deleteCard(@PathVariable String cardNumber) {
        cardService.deleteCard(cardNumber);
        return ResponseEntity.noContent().build();
    }
}
