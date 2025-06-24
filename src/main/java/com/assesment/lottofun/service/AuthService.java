package com.assesment.lottofun.service;

import com.assesment.lottofun.controller.request.AuthRequest;
import com.assesment.lottofun.controller.request.RegisterRequest;
import com.assesment.lottofun.controller.response.AuthResponse;
import com.assesment.lottofun.entity.User;
import com.assesment.lottofun.exception.BusinessException;
import com.assesment.lottofun.repository.UserRepository;
import com.assesment.lottofun.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("Email already registered: " + request.getEmail());
        }

        User newUser = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .build();

        userRepository.save(newUser);

        String token = jwtService.generateTokenFromEmail(newUser.getEmail());

        return new AuthResponse(token, newUser.getEmail());
    }

    @Transactional(readOnly = true)
    public AuthResponse authenticate(AuthRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );

            String token = jwtService.generateTokenFromEmail(request.getEmail());

            return new AuthResponse(token, request.getEmail());

        } catch (BadCredentialsException ex) {
            throw new BadCredentialsException("Invalid email or password");
        }
    }
}
