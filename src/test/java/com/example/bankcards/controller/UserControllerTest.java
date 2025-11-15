package com.example.bankcards.controller;

import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import com.example.bankcards.security.JwtUtils;
import com.example.bankcards.security.UserDetailsServiceImpl;
import com.example.bankcards.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.example.bankcards.config.WebSecurityConfig;

@WebMvcTest(UserController.class)
@Import(WebSecurityConfig.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private AuthenticationManager authenticationManager;

    @MockBean
    private UserDetailsServiceImpl userDetailsService;

    @MockBean
    private JwtUtils jwtUtils;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllUsers_ShouldReturnUsersList() throws Exception {
        // Arrange
        User user1 = new User(1L, "admin", "$2a$10$...", "admin@bank.com", Set.of(Role.ROLE_ADMIN, Role.ROLE_USER));
        User user2 = new User(2L, "user1", "$2a$10$...", "user1@example.com", Set.of(Role.ROLE_USER));
        when(userRepository.findAll()).thenReturn(List.of(user1, user2));

        // Act & Assert
        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].username").value("admin"))
                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$[1].username").value("user1"));

        verify(userRepository, times(1)).findAll();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getUser_ExistingUser_ShouldReturnUser() throws Exception {
        // Arrange
        Long userId = 1L;
        User user = new User(userId, "admin", "$2a$10$...", "admin@bank.com", Set.of(Role.ROLE_ADMIN, Role.ROLE_USER));
        when(userRepository.findById(eq(userId))).thenReturn(Optional.of(user));

        // Act & Assert
        mockMvc.perform(get("/api/admin/users/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.username").value("admin"));

        verify(userRepository, times(1)).findById(eq(userId));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getUser_NonExistentUser_ShouldReturnNotFound() throws Exception {
        // Arrange
        Long userId = 999L;
        when(userRepository.findById(eq(userId))).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/api/admin/users/{userId}", userId))
                .andExpect(status().isNotFound());

        verify(userRepository, times(1)).findById(eq(userId));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteUser_ExistingUser_ShouldReturnSuccessMessage() throws Exception {
        // Arrange
        Long userId = 1L;
        when(userRepository.existsById(eq(userId))).thenReturn(true);

        // Act & Assert
        mockMvc.perform(delete("/api/admin/users/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(content().string("User deleted successfully"));

        verify(userRepository, times(1)).existsById(eq(userId));
        verify(userRepository, times(1)).deleteById(eq(userId));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteUser_NonExistentUser_ShouldReturnNotFound() throws Exception {
        // Arrange
        Long userId = 999L;
        when(userRepository.existsById(eq(userId))).thenReturn(false);

        // Act & Assert
        mockMvc.perform(delete("/api/admin/users/{userId}", userId))
                .andExpect(status().isNotFound());

        verify(userRepository, times(1)).existsById(eq(userId));
        verify(userRepository, times(0)).deleteById(eq(userId));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void blockUser_ExistingUser_ShouldReturnSuccessMessage() throws Exception {
        // Arrange
        Long userId = 1L;
        User user = new User(userId, "admin", "$2a$10$...", "admin@bank.com", Set.of(Role.ROLE_ADMIN, Role.ROLE_USER));
        when(userRepository.findById(eq(userId))).thenReturn(Optional.of(user));

        // Act & Assert
        mockMvc.perform(put("/api/admin/users/{userId}/block", userId))
                .andExpect(status().isOk())
                .andExpect(content().string("User blocked successfully"));

        verify(userRepository, times(1)).findById(eq(userId));
    }

    @Test
    @WithMockUser(roles = "USER")
    void getAllUsers_AsUser_ShouldReturnForbidden() throws Exception {
        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isForbidden());
    }
}