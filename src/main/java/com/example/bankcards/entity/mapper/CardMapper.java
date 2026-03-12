package com.example.bankcards.entity.mapper;

import com.example.bankcards.dto.BankCardDto;
import com.example.bankcards.dto.CardHolderDto;
import com.example.bankcards.entity.BankCard;
import com.example.bankcards.entity.CardHolder;
import com.example.bankcards.entity.enums.CardStatus;
import com.example.bankcards.entity.enums.Role;
import com.example.bankcards.exception.CardHolderException;
import com.example.bankcards.exception.CardNotFoundException;
import com.example.bankcards.repository.CardHoldersRepository;
import com.example.bankcards.service.CardHolderService;
import com.example.bankcards.service.EncryptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
@Slf4j
public class CardMapper {

    private final EncryptionService encryptionService;
    private final CardHoldersRepository cardHoldersRepository;

    /**
     * Преобразует Entity в DTO (для ответа клиенту)
     * Использует текущего аутентифицированного пользователя
     */
    public BankCardDto toDto(BankCard card) {
        if (card == null) {
            return null;
        }

        return BankCardDto.builder()
            .cardNumber(formatMaskedCardNumber(card.getLastFourDigits()))
            .cardHolderId(getCurrentUserId()) // Получаем ID текущего пользователя
            .build();
    }

    /**
     * Преобразует DTO в Entity с указанным владельцем
     */
    public BankCard toEntity(BankCardDto dto) { //, CardHolderDto cardHolderDto) {
        if (dto == null) {
            return null;
        }

        try {
            BankCard card = new BankCard();
            
            // Очищаем номер карты
            String cleanCardNumber = cleanCardNumber(dto.getCardNumber());
            
            // Валидация
            if (!isValidCardNumber(cleanCardNumber)) {
                throw new IllegalArgumentException("Invalid card number format");
            }
            
            // Устанавливаем поля
            card.setPlainCardNumber(cleanCardNumber);
            card.setCardNumberHash(encryptionService.hashForSearch(cleanCardNumber));
            card.setCardNumberEncrypted(encryptionService.encrypt(cleanCardNumber));
            card.setLastFourDigits(extractLastFour(cleanCardNumber));
            
            // Устанавливаем статус и баланс по умолчанию
            card.setStatus(CardStatus.ACTIVE);
            card.setBalance(BigDecimal.ZERO);
            
            // проверяем есть ли такой CardHolder через отдельное поле/таблицу
            CardHolder cardHolder = cardHoldersRepository.findById(dto.getCardHolderId()).orElseThrow(()-> new CardHolderException(dto.getCardHolderId()));
            card.setCardHolder(cardHolder);
            
            log.debug("Mapped card for user: {}. Last four: {}", 
                cardHolder.getName(), card.getLastFourDigits());
            
            return card;
            
        } catch (Exception e) {
            log.error("Failed to map BankCardDto to Entity: {}", e.getMessage());
            throw new RuntimeException("Failed to map bank card", e);
        }
    }

    /**
     * Преобразует DTO в Entity (использует текущего пользователя из SecurityContext)
     */
    // public BankCard toEntity(BankCardDto dto) {
    //     CardHolderDto currentUser = getCurrentUser();
    //     if (currentUser == null) {
    //         throw new IllegalStateException("No authenticated user found");
    //     }
    //     return toEntity(dto, currentUser);
    // }

    /**
     * Создает DTO с маскированным номером для безопасного отображения
     */
    public BankCardDto toMaskedDto(BankCard card) {
        if (card == null) {
            return null;
        }

        return BankCardDto.builder()
            .cardNumber(formatMaskedCardNumber(card.getLastFourDigits()))
            .cardHolderId(card.getCardHolder().getId())
            .build();
    }

    /**
     * Создает DTO с полным номером (только для внутреннего использования!)
     * Требует проверки прав доступа
     */
    public BankCardDto toFullDto(BankCard card) {
        if (card == null) {
            return null;
        }

        // Проверяем права доступа (только ADMIN или владелец)
        checkAccessRights(card);

        // Расшифровываем номер
        String decryptedNumber = encryptionService.decrypt(card.getCardNumberEncrypted());

        return BankCardDto.builder()
            .cardNumber(decryptedNumber)
            .cardHolderId(getCurrentUserId())
            .build();
    }

    /**
     * Обновляет существующую сущность из DTO
     */
    public void updateEntityFromDto(BankCardDto dto, BankCard card) {
        if (dto == null || card == null) {
            return;
        }

        // Проверяем права доступа
        checkAccessRights(card);

        String cleanCardNumber = cleanCardNumber(dto.getCardNumber());
        
        if (!isValidCardNumber(cleanCardNumber)) {
            throw new IllegalArgumentException("Invalid card number format");
        }

        // Обновляем только если номер изменился
        String currentPlain = card.getPlainCardNumber();
        if (currentPlain == null || !currentPlain.equals(cleanCardNumber)) {
            
            card.setPlainCardNumber(cleanCardNumber);
            card.setCardNumberHash(encryptionService.hashForSearch(cleanCardNumber));
            card.setCardNumberEncrypted(encryptionService.encrypt(cleanCardNumber));
            card.setLastFourDigits(extractLastFour(cleanCardNumber));
            
            log.debug("Updated card for user: {}. New last four: {}", 
                getCurrentUsername(), card.getLastFourDigits());
        }
    }

    // Приватные вспомогательные методы

    private String cleanCardNumber(String cardNumber) {
        if (cardNumber == null) {
            return null;
        }
        return cardNumber.replaceAll("[\\s-]", "");
    }

    private boolean isValidCardNumber(String cardNumber) {
        return cardNumber != null && cardNumber.matches("^[0-9]{13,19}$");
    }

    private String extractLastFour(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 4) {
            return null;
        }
        return cardNumber.substring(cardNumber.length() - 4);
    }

    private String formatMaskedCardNumber(String lastFour) {
        if (lastFour == null) {
            return null;
        }
        return "****-****-****-" + lastFour;
    }

    /**
     * Получает текущего аутентифицированного пользователя
     */
    private CardHolderDto getCurrentUser() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CardHolderDto) {
            return (CardHolderDto) authentication.getPrincipal();
        }
        return null;
    }

    /**
     * Получает ID текущего пользователя
     */
    private Long getCurrentUserId() {
        CardHolderDto user = getCurrentUser();
        return user != null ? user.getId() : null;
    }

    /**
     * Получает username текущего пользователя
     */
    private String getCurrentUsername() {
        CardHolderDto user = getCurrentUser();
        return user != null ? user.getUsername() : "anonymous";
    }

    /**
     * Проверяет права доступа к карте
     */
    private void checkAccessRights(BankCard card) {
        CardHolderDto currentUser = getCurrentUser();
        
        if (currentUser == null) {
            throw new SecurityException("No authenticated user");
        }

        // ADMIN может всё
        if (currentUser.getRole() == Role.ROLE_ADMIN) {
            return;
        }

        // Обычный пользователь может работать только со своими картами
        // Здесь нужно добавить логику проверки, что карта принадлежит пользователю
        // Например: if (!card.getCardHolderId().equals(currentUser.getId())) {
        //     throw new SecurityException("Access denied to this card");
        // }
        
        log.debug("Access granted for user: {} to card: {}", 
            currentUser.getUsername(), card.getLastFourDigits());
    }
}