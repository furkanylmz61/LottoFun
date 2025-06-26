package com.assesment.lottofun.service;

import com.assesment.lottofun.presentation.dto.request.AuthRequest;
import com.assesment.lottofun.presentation.dto.request.RegisterRequest;
import com.assesment.lottofun.presentation.dto.response.AuthResponse;
import com.assesment.lottofun.entity.User;
import com.assesment.lottofun.exception.BusinessException;
import com.assesment.lottofun.infrastructure.repository.UserRepository;
import com.assesment.lottofun.infrastructure.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest sampleRegisterRequest;
    private AuthRequest sampleAuthRequest;
    private User sampleUser;

    @BeforeEach
    void setup() {
        sampleRegisterRequest = new RegisterRequest();
        sampleRegisterRequest.setEmail("test@email.com");
        sampleRegisterRequest.setPassword("password123");
        sampleRegisterRequest.setFirstName("John");
        sampleRegisterRequest.setLastName("Doe");

        sampleAuthRequest = new AuthRequest();
        sampleAuthRequest.setEmail("test@email.com");
        sampleAuthRequest.setPassword("password123");

        sampleUser = User.builder()
                .id(1L)
                .email("test@email.com")
                .password("encodedPassword123")
                .firstName("John")
                .lastName("Doe")
                .build();
    }

    @Test
    void register_ShouldReturnAuthResponse_WhenValidRequest() {
        when(userRepository.existsByEmail(sampleRegisterRequest.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(sampleRegisterRequest.getPassword())).thenReturn("encodedPassword123");
        when(userRepository.save(any(User.class))).thenReturn(sampleUser);
        when(jwtService.generateTokenFromEmail(sampleRegisterRequest.getEmail())).thenReturn("jwt-token-123");

        AuthResponse response = authService.register(sampleRegisterRequest);

        assertNotNull(response);
        assertEquals("jwt-token-123", response.getToken());
        assertEquals("test@email.com", response.getEmail());

        verify(userRepository).existsByEmail(sampleRegisterRequest.getEmail());
        verify(passwordEncoder).encode(sampleRegisterRequest.getPassword());
        verify(userRepository).save(any(User.class));
        verify(jwtService).generateTokenFromEmail(sampleRegisterRequest.getEmail());
    }

    @Test
    void register_ShouldThrowBusinessException_WhenEmailAlreadyExists() {
        when(userRepository.existsByEmail(sampleRegisterRequest.getEmail())).thenReturn(true);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            authService.register(sampleRegisterRequest);
        });

        assertTrue(exception.getMessage().contains("Email already registered"));
        assertTrue(exception.getMessage().contains(sampleRegisterRequest.getEmail()));

        verify(userRepository).existsByEmail(sampleRegisterRequest.getEmail());
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
        verify(jwtService, never()).generateTokenFromEmail(anyString());
    }

    @Test
    void register_ShouldCreateUserWithCorrectData_WhenValidRequest() {
        when(userRepository.existsByEmail(sampleRegisterRequest.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(sampleRegisterRequest.getPassword())).thenReturn("encodedPassword123");
        when(jwtService.generateTokenFromEmail(sampleRegisterRequest.getEmail())).thenReturn("jwt-token-123");

        authService.register(sampleRegisterRequest);

        verify(userRepository).save(argThat(user ->
            user.getEmail().equals("test@email.com") &&
            user.getPassword().equals("encodedPassword123") &&
            user.getFirstName().equals("John") &&
            user.getLastName().equals("Doe")
        ));
    }

    @Test
    void authenticate_ShouldReturnAuthResponse_WhenValidCredentials() {
        Authentication mockAuthentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(mockAuthentication);
        when(jwtService.generateTokenFromEmail(sampleAuthRequest.getEmail())).thenReturn("jwt-token-456");

        AuthResponse response = authService.authenticate(sampleAuthRequest);

        assertNotNull(response);
        assertEquals("jwt-token-456", response.getToken());
        assertEquals("test@email.com", response.getEmail());

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtService).generateTokenFromEmail(sampleAuthRequest.getEmail());
    }

    @Test
    void authenticate_ShouldThrowBadCredentialsException_WhenInvalidCredentials() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Authentication failed"));

        BadCredentialsException exception = assertThrows(BadCredentialsException.class, () -> {
            authService.authenticate(sampleAuthRequest);
        });

        assertEquals("Invalid email or password", exception.getMessage());

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtService, never()).generateTokenFromEmail(anyString());
    }

    @Test
    void authenticate_ShouldPassCorrectCredentials_ToAuthenticationManager() {
        Authentication mockAuthentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(mockAuthentication);
        when(jwtService.generateTokenFromEmail(sampleAuthRequest.getEmail())).thenReturn("jwt-token-456");

        authService.authenticate(sampleAuthRequest);

        verify(authenticationManager).authenticate(argThat(auth -> {
            UsernamePasswordAuthenticationToken token = (UsernamePasswordAuthenticationToken) auth;
            return token.getPrincipal().equals("test@email.com") &&
                   token.getCredentials().equals("password123");
        }));
    }

    @Test
    void register_ShouldNotGenerateToken_WhenRepositorySaveFails() {
        when(userRepository.existsByEmail(sampleRegisterRequest.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(sampleRegisterRequest.getPassword())).thenReturn("encodedPassword123");
        when(userRepository.save(any(User.class))).thenThrow(new RuntimeException("Database error"));

        assertThrows(RuntimeException.class, () -> {
            authService.register(sampleRegisterRequest);
        });

        verify(jwtService, never()).generateTokenFromEmail(anyString());
    }

    @Test
    void register_ShouldEncodePassword_BeforeSavingUser() {
        String rawPassword = "myRawPassword";
        String encodedPassword = "encodedMyRawPassword";
        
        sampleRegisterRequest.setPassword(rawPassword);
        
        when(userRepository.existsByEmail(sampleRegisterRequest.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(rawPassword)).thenReturn(encodedPassword);
        when(jwtService.generateTokenFromEmail(sampleRegisterRequest.getEmail())).thenReturn("token");

        authService.register(sampleRegisterRequest);

        verify(passwordEncoder).encode(rawPassword);
        verify(userRepository).save(argThat(user -> user.getPassword().equals(encodedPassword)));
    }
} 