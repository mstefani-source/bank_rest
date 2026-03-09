package com.example.bankcards.exception;

public class CardAlreadyExistsException extends RuntimeException {
    public CardAlreadyExistsException(String cardNumber) {
        super(String.format("Card %s already exist", cardNumber));
    }
}
