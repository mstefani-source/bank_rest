package com.example.bankcards.entity.enums;

public enum CardStatus {
    ACTIVE("ACTIVE"),
    BLOCKED("BLOCKED"),
    EXPIRED("EXPIRED");

    String value = null;

    private CardStatus(String value) {
        this.value = value;
    }

    public String getValue(){
        return this.value;
    }
}
