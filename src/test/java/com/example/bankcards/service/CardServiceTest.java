package com.example.bankcards.service;

import com.example.bankcards.dto.CardDto;
import com.example.bankcards.dto.CreateCardRequest;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.CardNotFoundException;
import com.example.bankcards.exception.UserNotFoundException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardServiceTest {

    @Mock
    private CardRepository cardRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CardService cardService;

    @Test
    void getUserCards_ShouldReturnUserCards() {
        Long userId = 1L;
        User user = new User(1L, "user1", "pass", "user@test.com", null);
        Card card = new Card(1L, "1234567812345678", "Test User",
                LocalDate.now().plusYears(2), new BigDecimal("1000.00"),
                CardStatus.ACTIVE, user);

        when(cardRepository.findByUserId(userId)).thenReturn(List.of(card));

        List<CardDto> result = cardService.getUserCards(userId);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("**** **** **** 5678", result.get(0).getMaskedCardNumber());
        verify(cardRepository, times(1)).findByUserId(userId);
    }

    @Test
    void createCard_WithValidData_ShouldCreateCard() {
        CreateCardRequest request = new CreateCardRequest();
        request.setCardNumber("1234567812345678");
        request.setOwnerName("Test User");
        request.setExpiryDate(LocalDate.now().plusYears(2));
        request.setBalance(new BigDecimal("1000.00"));
        request.setUserId(1L);

        User user = new User(1L, "user1", "pass", "user@test.com", null);
        Card savedCard = new Card(1L, "1234567812345678", "Test User",
                request.getExpiryDate(), request.getBalance(),
                CardStatus.ACTIVE, user);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(cardRepository.save(any(Card.class))).thenReturn(savedCard);

        CardDto result = cardService.createCard(request);

        assertNotNull(result);
        assertEquals("**** **** **** 5678", result.getMaskedCardNumber());
        assertEquals("Test User", result.getOwnerName());
        verify(cardRepository, times(1)).save(any(Card.class));
    }

    @Test
    void createCard_WithNonExistentUser_ShouldThrowException() {

        CreateCardRequest request = new CreateCardRequest();
        request.setUserId(999L);

        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> {
            cardService.createCard(request);
        });
    }

    @Test
    void blockCard_ShouldBlockCard() {

        Long cardId = 1L;
        Card card = new Card();
        card.setId(cardId);
        card.setStatus(CardStatus.ACTIVE);

        when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));
        when(cardRepository.save(any(Card.class))).thenReturn(card);

        cardService.blockCard(cardId);

        assertEquals(CardStatus.BLOCKED, card.getStatus());
        verify(cardRepository, times(1)).save(card);
    }

    @Test
    void blockCard_WithNonExistentCard_ShouldThrowException() {

        Long cardId = 999L;
        when(cardRepository.findById(cardId)).thenReturn(Optional.empty());

        assertThrows(CardNotFoundException.class, () -> {
            cardService.blockCard(cardId);
        });
    }
}