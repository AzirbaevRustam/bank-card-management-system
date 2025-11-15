package com.example.bankcards.controller;

import com.example.bankcards.dto.CardDto;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.security.JwtUtils;
import com.example.bankcards.security.UserDetailsServiceImpl;
import com.example.bankcards.service.CardService;
import com.example.bankcards.util.SecurityUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.example.bankcards.config.WebSecurityConfig;

@WebMvcTest(UserCardController.class)
@Import(WebSecurityConfig.class)
class UserCardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CardService cardService;

    @MockBean
    private SecurityUtils securityUtils;

    @MockBean
    private AuthenticationManager authenticationManager;

    @MockBean
    private UserDetailsServiceImpl userDetailsService;

    @MockBean
    private JwtUtils jwtUtils;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @Test
    @WithMockUser(roles = "USER")
    void getMyCards_ShouldReturnUserCards() throws Exception {
        // Arrange
        Long userId = 1L;
        CardDto card1 = new CardDto(1L, "**** **** **** 1111",
                "John Doe", LocalDate.now().plusYears(2),
                new BigDecimal("1000.00"), CardStatus.ACTIVE, userId);
        CardDto card2 = new CardDto(2L, "**** **** **** 2222",
                "John Doe", LocalDate.now().plusYears(3),
                new BigDecimal("2000.00"), CardStatus.ACTIVE, userId);
        when(securityUtils.getCurrentUserId()).thenReturn(userId);
        when(cardService.getUserCards(eq(userId))).thenReturn(List.of(card1, card2));

        // Act & Assert
        mockMvc.perform(get("/api/user/cards"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].maskedCardNumber").value("**** **** **** 1111"))
                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$[1].maskedCardNumber").value("**** **** **** 2222"));

        verify(securityUtils, times(1)).getCurrentUserId();
        verify(cardService, times(1)).getUserCards(eq(userId));
    }

    @Test
    @WithMockUser(roles = "USER")
    void getMyCard_ShouldReturnCard() throws Exception {
        // Arrange
        Long userId = 1L;
        Long cardId = 1L;
        CardDto card = new CardDto(cardId, "**** **** **** 1111", "John Doe", LocalDate.now().plusYears(2), new BigDecimal("1000.00"), CardStatus.ACTIVE, userId);
        when(securityUtils.getCurrentUserId()).thenReturn(userId);
        when(cardService.getUserCard(eq(userId), eq(cardId))).thenReturn(card);

        // Act & Assert
        mockMvc.perform(get("/api/user/cards/{cardId}", cardId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(cardId))
                .andExpect(jsonPath("$.maskedCardNumber").value("**** **** **** 1111"));

        verify(securityUtils, times(1)).getCurrentUserId();
        verify(cardService, times(1)).getUserCard(eq(userId), eq(cardId));
    }

    @Test
    @WithMockUser(roles = "USER")
    void blockMyCard_ShouldReturnSuccessMessage() throws Exception {
        // Arrange
        Long cardId = 1L;
        Long userId = 1L;
        when(securityUtils.getCurrentUserId()).thenReturn(userId);

        // Act & Assert
        mockMvc.perform(put("/api/user/cards/{cardId}/block", cardId))
                .andExpect(status().isOk())
                .andExpect(content().string("Card blocked successfully"));

        verify(securityUtils, times(1)).getCurrentUserId();
        verify(cardService, times(1)).blockUserCard(eq(userId), eq(cardId));
    }

    @Test
    @WithMockUser(roles = "USER")
    void getMyCardBalance_ShouldReturnBalance() throws Exception {
        // Arrange
        Long cardId = 1L;
        Long userId = 1L;
        BigDecimal balance = new BigDecimal("1000.00");
        when(securityUtils.getCurrentUserId()).thenReturn(userId);
        when(cardService.getCardBalance(eq(userId), eq(cardId))).thenReturn(balance);

        // Act & Assert
        mockMvc.perform(get("/api/user/cards/{cardId}/balance", cardId))
                .andExpect(status().isOk())
                .andExpect(content().string("Card balance: " + balance));

        verify(securityUtils, times(1)).getCurrentUserId();
        verify(cardService, times(1)).getCardBalance(eq(userId), eq(cardId));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getMyCards_AsAdmin_ShouldReturnForbidden() throws Exception {
        mockMvc.perform(get("/api/user/cards"))
                .andExpect(status().isForbidden());
    }
}