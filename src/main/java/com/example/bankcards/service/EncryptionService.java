package com.example.bankcards.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.example.bankcards.exception.EncryptionException;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Base64;

@Service
@Slf4j
public class EncryptionService {

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_TAG_LENGTH = 128;
    private static final int GCM_IV_LENGTH = 12;
    private static final int PBKDF2_ITERATIONS = 65536;
    private static final int KEY_LENGTH = 256;
    
    @Value("${encryption.master-secret:defaultMasterSecretKey123!}")
    private String masterSecret;
    
    @Value("${encryption.salt:defaultSalt123456789}")
    private String salt;
    
    private final SecureRandom secureRandom = new SecureRandom();
    private SecretKey cachedKey;

    /**
     * Шифрует данные (для номеров карт и других чувствительных данных)
     */
    public String encrypt(String data) {
        try {
            if (data == null) return null;
            
            SecretKey key = getOrCreateKey();
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            
            // Генерируем случайный IV (Initialization Vector)
            byte[] iv = new byte[GCM_IV_LENGTH];
            secureRandom.nextBytes(iv);
            
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, key, spec);
            
            byte[] encryptedData = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
            
            // Объединяем IV и зашифрованные данные
            ByteBuffer byteBuffer = ByteBuffer.allocate(iv.length + encryptedData.length);
            byteBuffer.put(iv);
            byteBuffer.put(encryptedData);
            
            // Кодируем в Base64 для хранения в БД
            return Base64.getEncoder().encodeToString(byteBuffer.array());
            
        } catch (Exception e) {
            log.error("Encryption error: {}", e.getMessage(), e);
            throw new EncryptionException("Failed to encrypt data", e);
        }
    }

    /**
     * Расшифровывает данные
     */
    public String decrypt(String encryptedData) {
        try {
            if (encryptedData == null) return null;
            
            SecretKey key = getOrCreateKey();
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            
            // Декодируем из Base64
            byte[] decodedData = Base64.getDecoder().decode(encryptedData);
            
            // Извлекаем IV и зашифрованные данные
            ByteBuffer byteBuffer = ByteBuffer.wrap(decodedData);
            byte[] iv = new byte[GCM_IV_LENGTH];
            byteBuffer.get(iv);
            
            byte[] cipherText = new byte[byteBuffer.remaining()];
            byteBuffer.get(cipherText);
            
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, key, spec);
            
            byte[] decryptedData = cipher.doFinal(cipherText);
            
            return new String(decryptedData, StandardCharsets.UTF_8);
            
        } catch (Exception e) {
            log.error("Decryption error: {}", e.getMessage(), e);
            throw new EncryptionException("Failed to decrypt data", e);
        }
    }

    /**
     * Создает хеш для поиска (без возможности обратной расшифровки)
     * Используется для поиска карт по номеру
     */
    public String hashForSearch(String data) {
        try {
            if (data == null) return null;
            
            // Используем PBKDF2 для создания хеша
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            KeySpec spec = new PBEKeySpec(
                data.toCharArray(), 
                salt.getBytes(StandardCharsets.UTF_8), 
                PBKDF2_ITERATIONS, 
                KEY_LENGTH
            );
            
            byte[] hash = factory.generateSecret(spec).getEncoded();
            
            // Добавляем информацию об итерациях и соли для будущей проверки
            String hashStr = Base64.getEncoder().encodeToString(hash);
            
            // Храним версию алгоритма для будущей совместимости
            return "PBKDF2$" + PBKDF2_ITERATIONS + "$" + hashStr;
            
        } catch (Exception e) {
            log.error("Hashing error: {}", e.getMessage(), e);
            throw new EncryptionException("Failed to hash data", e);
        }
    }

    /**
     * Проверяет, соответствует ли открытый текст хешу
     */
    public boolean verifyHash(String plainText, String storedHash) {
        try {
            if (plainText == null || storedHash == null) return false;
            
            // Парсим сохраненный хеш
            String[] parts = storedHash.split("\\$");
            if (parts.length != 3 || !"PBKDF2".equals(parts[0])) {
                log.error("Invalid hash format");
                return false;
            }
            
            int iterations = Integer.parseInt(parts[1]);
            String storedHashStr = parts[2];
            
            // Создаем хеш из переданного текста с теми же параметрами
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            KeySpec spec = new PBEKeySpec(
                plainText.toCharArray(),
                salt.getBytes(StandardCharsets.UTF_8),
                iterations,
                KEY_LENGTH
            );
            
            byte[] computedHash = factory.generateSecret(spec).getEncoded();
            String computedHashStr = Base64.getEncoder().encodeToString(computedHash);
            
            return computedHashStr.equals(storedHashStr);
            
        } catch (Exception e) {
            log.error("Hash verification error: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Маскирует номер карты для логирования и отображения
     */
    public String maskCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 8) {
            return "****";
        }
        
        String digits = cardNumber.replaceAll("\\s", "");
        if (digits.length() < 8) return "****";
        
        return digits.substring(0, 4) + "********" + 
               digits.substring(digits.length() - 4);
    }

    /**
     * Получает или создает ключ шифрования
     */
    private SecretKey getOrCreateKey() throws Exception {
        if (cachedKey != null) {
            return cachedKey;
        }
        
        // Используем PBKDF2 для генерации ключа из мастер-пароля
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        KeySpec spec = new PBEKeySpec(
            masterSecret.toCharArray(),
            salt.getBytes(StandardCharsets.UTF_8),
            PBKDF2_ITERATIONS,
            KEY_LENGTH
        );
        
        SecretKey tmp = factory.generateSecret(spec);
        cachedKey = new SecretKeySpec(tmp.getEncoded(), "AES");
        
        return cachedKey;
    }
}