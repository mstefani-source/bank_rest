package com.example.bankcards.exception;

public class CardHolderException extends RuntimeException{
    public CardHolderException(Long id) {
        super(String.format("CardHolderID = %d, not found", id));
    }
}
