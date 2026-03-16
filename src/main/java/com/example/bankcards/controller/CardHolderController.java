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
import com.example.bankcards.dto.CardHolderRequestDto;
import com.example.bankcards.dto.CardHolderResponseDto;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.example.bankcards.service.CardHolderService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Управление держателями карт")
public class CardHolderController {
    private final CardHolderService cardHolderService;

    @PostMapping("/card-holders")
    @ResponseStatus(code = HttpStatus.CREATED)
    @Operation(summary = "Создание нового держателя карт")
    public ResponseEntity<CardHolderResponseDto>  createCardHolder(@Valid @RequestBody CardHolderRequestDto cardDto) {
        CardHolderResponseDto createdCardHolder = cardHolderService.createCardHolder(cardDto);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(createdCardHolder);
    }

    @DeleteMapping("/card-holders/{cardHolderId}")
    @Operation(summary = "Удаление держателя карт по ID")
    public ResponseEntity<Void> deleteCardHolder(@PathVariable Long cardHolderId) {
        cardHolderService.deleteCardHolder(cardHolderId);
        return ResponseEntity.noContent().build();
    }
}
