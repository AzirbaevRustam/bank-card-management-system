package com.example.bankcards.repository;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CardRepository extends JpaRepository<Card, Long> {

    Optional<Card> findByUserIdAndId(Long userId, Long cardId);

    List<Card> findByUserId(Long userId);

    List<Card> findByUserIdAndStatus(Long userId, CardStatus status);

    boolean existsByCardNumber(String cardNumber);
}
