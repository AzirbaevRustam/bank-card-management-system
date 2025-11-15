package com.example.bankcards.controller;

import com.example.bankcards.config.WebSecurityConfig;
import com.example.bankcards.dto.CardDto;
import com.example.bankcards.dto.CreateCardRequest;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.security.UserDetailsServiceImpl;
import com.example.bankcards.service.CardService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(AdminCardController.class)
@Import(WebSecurityConfig.class)
class AdminCardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CardService cardService;

    @MockBean
    private AuthenticationManager authenticationManager;

    @MockBean
    private UserDetailsServiceImpl userDetailsService;

    @MockBean
    private com.example.bankcards.security.JwtUtils jwtUtils;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllCards_ShouldReturnCardsList() throws Exception {
        // Arrange
        CardDto card1 = new CardDto(1L, "**** **** **** 1111", "John Doe", LocalDate.now().plusYears(2), new BigDecimal("1000.00"), CardStatus.ACTIVE, 1L);
        CardDto card2 = new CardDto(2L, "**** **** **** 2222", "Jane Smith", LocalDate.now().plusYears(3), new BigDecimal("2000.00"), CardStatus.BLOCKED, 2L);
        when(cardService.getAllCards()).thenReturn(List.of(card1, card2));

        // Act & Assert
        mockMvc.perform(get("/api/admin/cards"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].maskedCardNumber").value("**** **** **** 1111"))
                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$[1].maskedCardNumber").value("**** **** **** 2222"));

        verify(cardService, times(1)).getAllCards();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createCard_ValidData_ShouldReturnCreatedCard() throws Exception {
        // Arrange
        CreateCardRequest request = new CreateCardRequest("1234567812345678", "New Owner", LocalDate.now().plusYears(2), new BigDecimal("500.00"), 1L);
        CardDto createdCard = new CardDto(3L, "**** **** **** 5678", "New Owner", LocalDate.now().plusYears(2), new BigDecimal("500.00"), CardStatus.ACTIVE, 1L);
        when(cardService.createCard(any(CreateCardRequest.class))).thenReturn(createdCard);

        // Act & Assert
        mockMvc.perform(post("/api/admin/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "cardNumber": "1234567812345678",
                                  "ownerName": "New Owner",
                                  "expiryDate": "2027-11-15",
                                  "balance": 500.00,
                                  "userId": 1
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(3L))
                .andExpect(jsonPath("$.maskedCardNumber").value("**** **** **** 5678"))
                .andExpect(jsonPath("$.ownerName").value("New Owner"));

        verify(cardService, times(1)).createCard(any(CreateCardRequest.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createCard_InvalidData_ShouldReturnBadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/admin/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "cardNumber": "",
                                  "ownerName": "New Owner",
                                  "expiryDate": "2027-11-15",
                                  "balance": -100.00,
                                  "userId": 1
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void blockCard_ShouldReturnSuccessMessage() throws Exception {
        // Arrange
        Long cardId = 1L;

        // Act & Assert
        mockMvc.perform(put("/api/admin/cards/{cardId}/block", cardId))
                .andExpect(status().isOk())
                .andExpect(content().string("Card blocked successfully"));

        verify(cardService, times(1)).blockCard(eq(cardId));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void activateCard_ShouldReturnSuccessMessage() throws Exception {
        // Arrange
        Long cardId = 2L;

        // Act & Assert
        mockMvc.perform(put("/api/admin/cards/{cardId}/activate", cardId))
                .andExpect(status().isOk())
                .andExpect(content().string("Card activated successfully"));

        verify(cardService, times(1)).activateCard(eq(cardId));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteCard_ShouldReturnSuccessMessage() throws Exception {
        // Arrange
        Long cardId = 3L;

        // Act & Assert
        mockMvc.perform(delete("/api/admin/cards/{cardId}", cardId))
                .andExpect(status().isOk())
                .andExpect(content().string("Card deleted successfully"));

        verify(cardService, times(1)).deleteCard(eq(cardId));
    }

    @Test
    @WithMockUser(roles = "USER")
    void getAllCards_AsUser_ShouldReturnForbidden() throws Exception {
        mockMvc.perform(get("/api/admin/cards"))
                .andExpect(status().isForbidden());
    }
}