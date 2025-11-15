package com.example.bankcards.controller;

import com.example.bankcards.config.WebSecurityConfig;
import com.example.bankcards.dto.TransferRequest;
import com.example.bankcards.dto.TransferResponse;
import com.example.bankcards.security.JwtUtils;
import com.example.bankcards.security.UserDetailsServiceImpl;
import com.example.bankcards.service.TransferService;
import com.example.bankcards.util.SecurityUtils;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TransferController.class)
@Import(WebSecurityConfig.class)
class TransferControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TransferService transferService;

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
    void transfer_ValidData_ShouldReturnSuccessResponse() throws Exception {
        // Arrange
        Long userId = 1L;
        TransferRequest request = new TransferRequest(1L, 2L, new BigDecimal("100.00"));
        TransferResponse response = new TransferResponse(true, "Transfer completed successfully", new BigDecimal("100.00"), 1L, 2L);
        when(securityUtils.getCurrentUserId()).thenReturn(userId);
        when(transferService.transfer(any(TransferRequest.class), eq(userId))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/user/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "fromCardId": 1,
                                  "toCardId": 2,
                                  "amount": 100.00
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Transfer completed successfully"))
                .andExpect(jsonPath("$.amount").value(100.00))
                .andExpect(jsonPath("$.fromCardId").value(1))
                .andExpect(jsonPath("$.toCardId").value(2));

        verify(securityUtils, times(1)).getCurrentUserId();
        verify(transferService, times(1)).transfer(any(TransferRequest.class), eq(userId));
    }

    @Test
    @WithMockUser(roles = "USER")
    void transfer_InvalidData_ShouldReturnBadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/user/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "fromCardId": 1,
                                  "toCardId": 2,
                                  "amount": -50.00
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void transfer_AsAdmin_ShouldReturnForbidden() throws Exception {
        mockMvc.perform(post("/api/user/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "fromCardId": 1,
                                  "toCardId": 2,
                                  "amount": 100.00
                                }
                                """))
                .andExpect(status().isForbidden());
    }
}