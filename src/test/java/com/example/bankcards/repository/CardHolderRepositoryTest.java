package com.example.bankcards.repository;

import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.example.bankcards.entity.CardHolder;
import com.example.bankcards.entity.enums.Role;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class CardHolderRepositoryTest {

    @Autowired
    private CardHolderRepository cardHolderRepository;

    @Test
    void testFindByEmail() {
        // Given
        CardHolder holder = new CardHolder();
        holder.setName("Test User");
        holder.setEmail("test@example.com");
        holder.setPassword("password");
        holder.setRole(Role.ROLE_USER);
        
        cardHolderRepository.save(holder);
        
        // When
        Optional<CardHolder> found = cardHolderRepository.findByEmail("test@example.com");
        
        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("test@example.com");
    }
}