package com.example.bankcards.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;
import com.example.bankcards.dto.CardHolderDto;

import com.example.bankcards.service.CardHolderService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class CardHolderController {
    private final CardHolderService cardHolderService;

    @PostMapping("/card-holders")
    @ResponseStatus(code = HttpStatus.CREATED)
    public CardHolderDto createCardHolder(@Valid @RequestBody CardHolderDto cardDto) {
        return cardHolderService.createCardHolder(cardDto);
    }

    @DeleteMapping("/card-holders/{cardHolderId}")
    public ResponseEntity<Void> deleteCardHolder(@PathVariable Long cardHolderId) {
        cardHolderService.deleteCardHolder(cardHolderId);
        return ResponseEntity.noContent().build();
    }
}
