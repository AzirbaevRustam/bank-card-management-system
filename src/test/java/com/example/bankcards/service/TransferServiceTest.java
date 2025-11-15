package com.example.bankcards.service;


import com.example.bankcards.dto.TransferRequest;
import com.example.bankcards.dto.TransferResponse;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.AccessDeniedException;
import com.example.bankcards.exception.DestinationCardNotFoundException;
import com.example.bankcards.repository.CardRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransferServiceTest {

    @Mock
    private CardRepository cardRepository;

    @InjectMocks
    private TransferService transferService;

    private User testUser;
    private Card sourceCard;
    private Card destinationCard;
    private TransferRequest transferRequest;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");

        sourceCard = new Card();
        sourceCard.setId(1L);
        sourceCard.setCardNumber("1111222233334444");
        sourceCard.setBalance(new BigDecimal("1000.00"));
        sourceCard.setStatus(CardStatus.ACTIVE);
        sourceCard.setUser(testUser);

        destinationCard = new Card();
        destinationCard.setId(2L);
        destinationCard.setCardNumber("5555666677778888");
        destinationCard.setBalance(new BigDecimal("500.00"));
        destinationCard.setStatus(CardStatus.ACTIVE);

        transferRequest = new TransferRequest();
        transferRequest.setFromCardId(1L);
        transferRequest.setToCardId(2L);
        transferRequest.setAmount(new BigDecimal("200.00"));
    }

    @Test
    void transfer_WithValidData_ShouldCompleteSuccessfully() {

        when(cardRepository.findByUserIdAndId(1L, 1L)).thenReturn(Optional.of(sourceCard));
        when(cardRepository.findById(2L)).thenReturn(Optional.of(destinationCard));
        when(cardRepository.save(any(Card.class))).thenReturn(sourceCard, destinationCard);

        TransferResponse result = transferService.transfer(transferRequest, 1L);

        assertTrue(result.isSuccess());
        assertEquals("Transfer completed successfully", result.getMessage());
        assertEquals(new BigDecimal("800.00"), sourceCard.getBalance());
        assertEquals(new BigDecimal("700.00"), destinationCard.getBalance());
        verify(cardRepository, times(2)).save(any(Card.class));
    }

    @Test
    void transfer_WithInsufficientFunds_ShouldReturnFailedResponse() {

        transferRequest.setAmount(new BigDecimal("2000.00"));
        when(cardRepository.findByUserIdAndId(1L, 1L)).thenReturn(Optional.of(sourceCard));
        when(cardRepository.findById(2L)).thenReturn(Optional.of(destinationCard));

        TransferResponse result = transferService.transfer(transferRequest, 1L);

        assertFalse(result.isSuccess());
        assertEquals("Insufficient funds", result.getMessage());
        verify(cardRepository, never()).save(any(Card.class));
    }

    @Test
    void transfer_WithNonExistentSourceCard_ShouldThrowException() {

        when(cardRepository.findByUserIdAndId(1L, 1L)).thenReturn(Optional.empty());

        assertThrows(AccessDeniedException.class, () -> {
            transferService.transfer(transferRequest, 1L);
        });
    }

    @Test
    void transfer_WithNonExistentDestinationCard_ShouldThrowException() {

        when(cardRepository.findByUserIdAndId(1L, 1L)).thenReturn(Optional.of(sourceCard));
        when(cardRepository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(DestinationCardNotFoundException.class, () -> {
            transferService.transfer(transferRequest, 1L);
        });
    }

    @Test
    void transfer_WithBlockedSourceCard_ShouldReturnFailedResponse() {

        sourceCard.setStatus(CardStatus.BLOCKED);
        when(cardRepository.findByUserIdAndId(1L, 1L)).thenReturn(Optional.of(sourceCard));
        when(cardRepository.findById(2L)).thenReturn(Optional.of(destinationCard));

        TransferResponse result = transferService.transfer(transferRequest, 1L);

        assertFalse(result.isSuccess());
        assertEquals("Source card is not active", result.getMessage());
        verify(cardRepository, never()).save(any(Card.class));
    }

    @Test
    void transfer_WithBlockedDestinationCard_ShouldReturnFailedResponse() {

        destinationCard.setStatus(CardStatus.BLOCKED);
        when(cardRepository.findByUserIdAndId(1L, 1L)).thenReturn(Optional.of(sourceCard));
        when(cardRepository.findById(2L)).thenReturn(Optional.of(destinationCard));

        TransferResponse result = transferService.transfer(transferRequest, 1L);

        assertFalse(result.isSuccess());
        assertEquals("Destination card is not active", result.getMessage());
        verify(cardRepository, never()).save(any(Card.class));
    }

    @Test
    void transfer_ToSameCard_ShouldReturnFailedResponse() {

        Card sameCard = new Card();
        sameCard.setId(1L);
        sameCard.setCardNumber("1111222233334444");
        sameCard.setBalance(new BigDecimal("1000.00"));
        sameCard.setStatus(CardStatus.ACTIVE);
        sameCard.setUser(testUser);

        TransferRequest sameCardRequest = new TransferRequest();
        sameCardRequest.setFromCardId(1L);
        sameCardRequest.setToCardId(1L);
        sameCardRequest.setAmount(new BigDecimal("200.00"));

        when(cardRepository.findByUserIdAndId(1L, 1L)).thenReturn(Optional.of(sameCard));
        when(cardRepository.findById(1L)).thenReturn(Optional.of(sameCard));

        TransferResponse result = transferService.transfer(sameCardRequest, 1L);

        assertFalse(result.isSuccess());
        assertEquals("Cannot transfer to the same card", result.getMessage());
        verify(cardRepository, never()).save(any(Card.class));
    }
}