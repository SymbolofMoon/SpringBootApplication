package com.practice.project.SpringBootApplication.service;

import com.practice.project.SpringBootApplication.entity.User;
import com.practice.project.SpringBootApplication.repository.UserRepository;
import com.practice.project.SpringBootApplication.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User mockUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Setup a mock user object
        mockUser = new User();
        mockUser.setUsername("testUser");
        mockUser.setEmail("test@example.com");
        mockUser.setPassword("plainPassword");
    }

    @Test
    void shouldRegisterUserSuccessfully() {
        // Mock repository and password encoder behavior
        when(userRepository.existsByUsername("testUser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(passwordEncoder.encode(mockUser.getPassword())).thenReturn("mockEncodedPassword");

        String result = userService.registerUser(mockUser);

        // Assertions
        assertEquals("User registered successfully!", result);

        verify(userRepository).save(mockUser); // Verify that the user is saved
    }

    @Test
    void shouldReturnUsernameAlreadyTaken() {
        // Mock repository to indicate username is taken
        when(userRepository.existsByUsername("testUser")).thenReturn(true);

        String result = userService.registerUser(mockUser);

        // Verify behavior and assertions
        assertEquals("Username is already taken.", result);
        verify(userRepository, never()).save(any(User.class));
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void shouldReturnEmailAlreadyInUse() {
        // Mock repository to indicate email is in use
        when(userRepository.existsByUsername("testUser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        String result = userService.registerUser(mockUser);

        // Verify behavior and assertions
        assertEquals("Email is already in use.", result);
        verify(userRepository, never()).save(any(User.class));
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void shouldHandleExceptionDuringRegistration() {
        // Mock repository to throw an exception
        when(userRepository.existsByUsername("testUser")).thenThrow(new RuntimeException("Database error"));

        String result = userService.registerUser(mockUser);

        // Verify behavior and assertions
        assertEquals("An error occurred during registration.", result);
        verify(userRepository, never()).save(any(User.class));
    }
}
