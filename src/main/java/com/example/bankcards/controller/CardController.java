package com.example.bankcards.controller;

import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;

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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.example.bankcards.dto.BankCardDto;
import com.example.bankcards.dto.TransferRequest;
import com.example.bankcards.service.BankCardService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/*
  - Просматривает свои карты (поиск + пагинация) +
  - Запрашивает блокировку карты
  - Делает переводы между своими картами +
  - Смотрит баланс
*/ 

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Управление банковскими картами")
public class CardController {

    private final BankCardService cardService;

    @PostMapping("/cards")
    @ResponseStatus(code = HttpStatus.CREATED)
    @Operation(summary = "Создание новой банковской карты")
    public BankCardDto createCard(@Valid @RequestBody BankCardDto cardDto) {
        return cardService.createCard(cardDto);
    }

    @Operation(summary = "Удаление банковской карты по номеру")
    @DeleteMapping("/cards/{cardNumber}")
    public ResponseEntity<Void> deleteCard(@PathVariable String cardNumber) {
        cardService.deleteCard(cardNumber);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/cards/{cardNumber}")
    @Operation(summary = "Получение информации о карте по номеру с учетом прав доступа")
    public ResponseEntity<BigDecimal> getCard(@PathVariable String cardNumber) {
        return ResponseEntity.ok(cardService.getCardBalanse(cardNumber));
    }

    @GetMapping("/{cardHolderId}/cards")
    @Operation(summary = "Получение списка карт для держателя с учетом прав доступа")
    public ResponseEntity<Page<BankCardDto>> getCards(
            @RequestParam(required = false) Long cardHolderId, Pageable pageable) {
        return ResponseEntity.ok(cardService.getCardsWithAccessCheck(cardHolderId, pageable));
    }

    @PostMapping("/{cardHolderId}/cards")
    @Operation(summary = "Перевод между своими картами")
    public ResponseEntity<String> transferBetweenOwnCards(@Valid @RequestBody TransferRequest request) {
        cardService.transfer(request);
        return ResponseEntity.ok("Перевод успешно выполнен");
    }
}