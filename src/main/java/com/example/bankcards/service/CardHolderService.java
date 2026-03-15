package com.example.bankcards.service;

import com.example.bankcards.dto.CardHolderDto;
import com.example.bankcards.entity.CardHolder;
import com.example.bankcards.entity.mapper.CardHolderMapper;
import com.example.bankcards.repository.CardHolderRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import java.util.List;
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

    public CardHolderDto createCardHolder(CardHolderDto cardHolderDto) {

        CardHolder cardHolder = cardHolderMapper.ToEntity(cardHolderDto);
        CardHolder savedCardHolder = cardHoldersRepository.save(cardHolder);

        return cardHolderMapper.ToDto(savedCardHolder);
    }

    public List<CardHolderDto> findAllCustomers() {
        List<CardHolder> cardHolders = cardHoldersRepository.findAll();

        return cardHolders
                .stream()
                .map((cardHolder) -> cardHolderMapper.ToDto(cardHolder))
                .toList();
    }

    public Optional<CardHolderDto> findById(Long id) {
        return Optional.of(cardHolderMapper.ToDto(cardHoldersRepository.findById(id).orElseThrow()));
    }

    public void deleteCardHolder(Long id) {
        cardHoldersRepository.deleteById(id);
    }

    public CardHolderDto updateCardHolder(Long id, CardHolderDto customerDto) {
        CardHolder customer = cardHoldersRepository
                .findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Customer_id not exist"));
        customer.setName(customerDto.getName());
        return cardHolderMapper.ToDto(cardHoldersRepository.save(customer));
    }

    public CardHolderDto findByEmail(String email) {
        return cardHolderMapper.ToDto(
                cardHoldersRepository.findByEmail(email)
                        .orElseThrow(() -> new NoSuchElementException("No such customer")));
    }

    public CardHolderDto getCurrentUser() {
        // Получение имени пользователя из контекста Spring Security
        var email = SecurityContextHolder.getContext().getAuthentication().getName();
        return findByEmail(email);
    }

    public UserDetailsService userDetailsService() {
        return this::findByEmail;
    }
}
