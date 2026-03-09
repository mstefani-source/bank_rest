package com.example.bankcards.exception;

public class CardHolderException extends RuntimeException{
    public CardHolderException(Long id) {
        super(String.format("Card %d not found", id));
    }
}
