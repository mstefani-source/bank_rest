package com.example.bankcards.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.example.bankcards.dto.CardHolderDto;
import com.example.bankcards.entity.CardHolder;
import com.example.bankcards.repository.CardHolderRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    // Проверьте, правильный ли импорт
    private final CardHolderRepository cardHolderRepository;
    
    @Override
    public UserDetails loadUserByUsername(String email) {
        // Этот метод вызывает findByEmail
        CardHolder holder = cardHolderRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        
        return new CardHolderDto(holder.getId(), holder.getName(), 
                                holder.getEmail(), holder.getRole());
    }
}
