package com.example.bankcards.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BankCardDto {
    
    @NotBlank
    @Pattern(regexp = "^[0-9]{13,19}$", message = "Invalid card number")
    private String cardNumber; 
    
    @NotBlank
    private Long cardHolderId;
    
    public String getMaskedCardNumber() {
        if (cardNumber == null) return null;
        String digits = cardNumber.replaceAll("\\s", "");
        if (digits.length() < 8) return "****";
        return digits.substring(0, 4) + "********" + 
               digits.substring(digits.length() - 4);
    }
}