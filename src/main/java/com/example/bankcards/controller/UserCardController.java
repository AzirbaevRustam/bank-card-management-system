package com.example.bankcards.controller;


import com.example.bankcards.dto.CardDto;
import com.example.bankcards.service.CardService;
import com.example.bankcards.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user/cards")
@PreAuthorize("hasRole('USER')")
@RequiredArgsConstructor
public class UserCardController {

    private final CardService cardService;
    private final SecurityUtils securityUtils;

    @GetMapping
    public ResponseEntity<List<CardDto>> getMyCards() {
        Long userId = securityUtils.getCurrentUserId();
        List<CardDto> cards = cardService.getUserCards(userId);
        return ResponseEntity.ok(cards);
    }

    @GetMapping("/{cardId}")
    public ResponseEntity<CardDto> getMyCard(@PathVariable Long cardId) {
        Long userId = securityUtils.getCurrentUserId();
        CardDto card = cardService.getUserCard(userId, cardId);
        return ResponseEntity.ok(card);
    }

    @PutMapping("/{cardId}/block")
    public ResponseEntity<?> blockMyCard(@PathVariable Long cardId) {
        Long userId = securityUtils.getCurrentUserId();
        cardService.blockUserCard(userId, cardId);
        return ResponseEntity.ok("Card blocked successfully");
    }

    @GetMapping("/{cardId}/balance")
    public ResponseEntity<?> getMyCardBalance(@PathVariable Long cardId) {
        Long userId = securityUtils.getCurrentUserId();
        var balance = cardService.getCardBalance(userId, cardId);
        return ResponseEntity.ok().body("Card balance: " + balance);
    }
}