package com.example.bankcards.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.example.bankcards.dto.BankCardDto;
import com.example.bankcards.service.BankCardService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/cards/")
@RequiredArgsConstructor
public class CardController {

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

    @GetMapping
    public ResponseEntity<Page<BankCardDto>> getCards(
            @RequestParam(required = false) Long customerId, Pageable pageable) {
        // Spring автоматически заполнит pageable из параметров ?page=0&size=10
        return ResponseEntity.ok(cardService.getCardsWithAccessCheck(customerId, pageable));
    }

    // --- Переводы для ПОЛЬЗОВАТЕЛЯ ---
    
    // @PostMapping("/transfer")
    // public ResponseEntity<String> transferBetweenOwnCards(@Valid @RequestBody TransferRequest request) {
    //     cardService.transfer(request);
    //     return ResponseEntity.ok("Перевод успешно выполнен");
    // }
}