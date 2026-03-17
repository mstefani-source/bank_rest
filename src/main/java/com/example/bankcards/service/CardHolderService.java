package com.example.bankcards.service;

import com.example.bankcards.dto.CardHolderDto;
import com.example.bankcards.dto.CardHolderRequestDto;
import com.example.bankcards.dto.CardHolderResponseDto;
import com.example.bankcards.entity.CardHolder;
import com.example.bankcards.entity.mapper.CardHolderMapper;
import com.example.bankcards.repository.CardHolderRepository;
import lombok.extern.log4j.Log4j2;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@Log4j2
public class CardHolderService {

    private final CardHolderRepository cardHoldersRepository;
    private final CardHolderMapper cardHolderMapper;

    public CardHolderService(CardHolderRepository cardHoldersRepository, CardHolderMapper cardHolderMapper) {
        this.cardHoldersRepository = cardHoldersRepository;
        this.cardHolderMapper = cardHolderMapper;
    }

    public CardHolderResponseDto createCardHolder(CardHolderRequestDto cardHolderRequestDto) {

        CardHolder cardHolder = cardHolderMapper.ToEntity(cardHolderRequestDto);
        CardHolder savedCardHolder = cardHoldersRepository.save(cardHolder);

        return cardHolderMapper.ToDto(savedCardHolder);
    }

    public Optional<CardHolderDto> findById(Long id) {
        return Optional.of(cardHolderMapper.toUserDetails(cardHoldersRepository.findById(id).orElseThrow()));
    }

    public void deleteCardHolder(Long id) {
        cardHoldersRepository.deleteById(id);
    }

    public CardHolderResponseDto updateCardHolder(Long id, CardHolderDto customerDto) {
        CardHolder customer = cardHoldersRepository
                .findById(id)
                .orElseThrow(() -> new IllegalArgumentException("CardHolder_id not exist"));
        customer.setName(customerDto.getName());
        return cardHolderMapper.ToDto(cardHoldersRepository.save(customer));
    }

    public CardHolderResponseDto findByEmail(String email) {
        return cardHolderMapper.ToDto(
                cardHoldersRepository.findByEmail(email)
                        .orElseThrow(() -> new NoSuchElementException("No such CardHolder")));
    }

    public CardHolderResponseDto getCurrentUser() {
        // Получение имени пользователя из контекста Spring Security
        var email = SecurityContextHolder.getContext().getAuthentication().getName();
        return findByEmail(email);
    }

    public UserDetailsService userDetailsService() {
        return new UserDetailsService() {
            @Override
            public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
                CardHolder cardHolder = cardHoldersRepository.findByEmail(email)
                        .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

                return cardHolderMapper.toUserDetails(cardHolder);
            }
        };
    }
}
