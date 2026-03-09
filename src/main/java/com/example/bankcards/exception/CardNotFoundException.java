package com.example.bankcards.exception;

public class CardNotFoundException extends RuntimeException{
    public CardNotFoundException(String cardNumber) {
        super(String.format("Card %s not found", cardNumber));
    }

}
