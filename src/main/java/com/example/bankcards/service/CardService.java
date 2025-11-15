package com.example.bankcards.service;

import com.example.bankcards.dto.CardDto;
import com.example.bankcards.dto.CreateCardRequest;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.*;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CardService {

    private final CardRepository cardRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<CardDto> getUserCards(Long userId) {
        List<Card> cards = cardRepository.findByUserId(userId);
        return cards.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CardDto getUserCard(Long userId, Long cardId) {
        Card card = cardRepository.findByUserIdAndId(userId, cardId)
                .orElseThrow(() -> new CardNotFoundException("Card not found or access denied"));
        return convertToDto(card);
    }

    @Transactional
    public CardDto createCard(CreateCardRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        Card card = new Card();
        card.setCardNumber(request.getCardNumber());
        card.setOwnerName(request.getOwnerName());
        card.setExpiryDate(request.getExpiryDate());
        card.setBalance(request.getBalance());
        card.setStatus(CardStatus.ACTIVE);
        card.setUser(user);

        Card savedCard = cardRepository.save(card);
        return convertToDto(savedCard);
    }

    @Transactional
    public void blockUserCard(Long userId, Long cardId) {
        Card card = cardRepository.findByUserIdAndId(userId, cardId)
                .orElseThrow(() -> new AccessDeniedException("Card not found or access denied"));

        if (card.getStatus() == CardStatus.BLOCKED) {
            throw new CardAlreadyBlockedException("Card is already blocked");
        }

        card.setStatus(CardStatus.BLOCKED);
        cardRepository.save(card);
    }

    @Transactional
    public void blockCard(Long cardId) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new CardNotFoundException("Card not found"));

        card.setStatus(CardStatus.BLOCKED);
        cardRepository.save(card);
    }

    @Transactional
    public void activateCard(Long cardId) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new CardNotFoundException("Card not found"));

        if (card.getStatus() == CardStatus.ACTIVE) {
            throw new CardAlreadyActiveException("Card is already active");
        }

        card.setStatus(CardStatus.ACTIVE);
        cardRepository.save(card);
    }

    @Transactional(readOnly = true)
    public BigDecimal getCardBalance(Long userId, Long cardId) {
        Card card = cardRepository.findByUserIdAndId(userId, cardId)
                .orElseThrow(() -> new AccessDeniedException("Card not found or access denied"));
        return card.getBalance();
    }

    @Transactional(readOnly = true)
    public List<CardDto> getAllCards() {
        List<Card> cards = cardRepository.findAll();
        return cards.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteCard(Long cardId) {
        if (!cardRepository.existsById(cardId)) {
            throw new CardNotFoundException("Card not found");
        }
        cardRepository.deleteById(cardId);
    }

    private CardDto convertToDto(Card card) {
        CardDto dto = new CardDto();
        dto.setId(card.getId());

        String cardNumber = card.getCardNumber();
        String masked = "**** **** **** " + cardNumber.substring(cardNumber.length() - 4);
        dto.setMaskedCardNumber(masked);

        dto.setOwnerName(card.getOwnerName());
        dto.setExpiryDate(card.getExpiryDate());
        dto.setBalance(card.getBalance());
        dto.setStatus(card.getStatus());
        dto.setUserId(card.getUser().getId());
        return dto;
    }
}