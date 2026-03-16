package com.example.bankcards.security.auth;

import com.example.bankcards.security.JwtService;
import com.example.bankcards.service.CardHolderService;
import com.example.bankcards.dto.AuthRequest;
import com.example.bankcards.dto.CardHolderDto;
import com.example.bankcards.dto.CardHolderRequestDto;
import com.example.bankcards.dto.CardHolderResponseDto;
import com.example.bankcards.dto.RegistrationRequest;
import com.example.bankcards.entity.enums.Role;
import com.example.bankcards.entity.mapper.CardHolderMapper;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final CardHolderService cardHolderService;
    private final CardHolderMapper cardHolderMapper;



    /**
     * Регистрация пользователя
     *
     * @param request данные пользователя
     * @return токен
     */
    public JwtAuthenticationResponse signUp(RegistrationRequest request) {

        var customerDto = CardHolderRequestDto.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.ROLE_USER)
                .build();

        CardHolderResponseDto createdHolderResponseDto = cardHolderService.createCardHolder(customerDto);


        var jwt = jwtService.generateToken(cardHolderMapper.toUserDetails(createdHolderResponseDto));
        return new JwtAuthenticationResponse(jwt);
    }

    /**
     * Аутентификация пользователя
     *
     * @param request данные пользователя
     * @return токен
     */
    public JwtAuthenticationResponse signIn(AuthRequest request) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                request.getEmail(),
                request.getPassword()
        ));

        var user = cardHolderService
                .userDetailsService()
                .loadUserByUsername(request.getEmail());

        var jwt = jwtService.generateToken(user);
        return new JwtAuthenticationResponse(jwt);
    }
}
