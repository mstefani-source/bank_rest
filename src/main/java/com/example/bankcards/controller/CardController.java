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
import com.example.bankcards.service.CardService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/customers/card/")
@RequiredArgsConstructor
public class CardController {

    private final CardService cardService;

    @PostMapping
    @ResponseStatus(code = HttpStatus.CREATED)
    public BankCardDto createCard(@Valid @RequestBody BankCardDto cardDto) {
        return cardService.create(cardDto);
    }

    @DeleteMapping("{id}")
    public ResponseEntity<Void> deleteCard(@PathVariable Long id) {
        cardService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("{customerId}")
    public ResponseEntity<Page<BankCardDto>> getCards(
            @RequestParam(required = false) Long customerId, Pageable pageable) {
        // Spring автоматически заполнит pageable из параметров ?page=0&size=10
        return ResponseEntity.ok(cardService.findAll(customerId, pageable));
    }

    // --- Переводы для ПОЛЬЗОВАТЕЛЯ ---
    
    // @PostMapping("/transfer")
    // public ResponseEntity<String> transferBetweenOwnCards(@Valid @RequestBody TransferRequest request) {
    //     cardService.transfer(request);
    //     return ResponseEntity.ok("Перевод успешно выполнен");
    // }
}