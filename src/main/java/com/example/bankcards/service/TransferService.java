package com.example.bankcards.service;

import com.example.bankcards.dto.TransferRequest;
import com.example.bankcards.dto.TransferResponse;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.exception.AccessDeniedException;
import com.example.bankcards.exception.DestinationCardNotFoundException;
import com.example.bankcards.repository.CardRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransferService {
    private final CardRepository cardRepository;

    @Transactional
    public TransferResponse transfer(TransferRequest request, Long userId) {
        Card fromCard = cardRepository.findByUserIdAndId(userId, request.getFromCardId())
                .orElseThrow(() -> new AccessDeniedException("Source card not found or access denied"));
        Card toCard = cardRepository.findById(request.getToCardId())
                .orElseThrow(() -> new DestinationCardNotFoundException("Destination card not found"));

        if (fromCard.getStatus() != CardStatus.ACTIVE) {
            return new TransferResponse(false, "Source card is not active",
                    request.getAmount(), request.getFromCardId(), request.getToCardId());
        }
        if (toCard.getStatus() != CardStatus.ACTIVE) {
            return new TransferResponse(false, "Destination card is not active",
                    request.getAmount(), request.getFromCardId(), request.getToCardId());
        }
        if (fromCard.getBalance().compareTo(request.getAmount()) < 0) {
            return new TransferResponse(false, "Insufficient funds",
                    request.getAmount(), request.getFromCardId(), request.getToCardId());
        }
        if (fromCard.getId().equals(toCard.getId())) {
            return new TransferResponse(false, "Cannot transfer to the same card",
                    request.getAmount(), request.getFromCardId(), request.getToCardId());
        }

        try {
            fromCard.setBalance(fromCard.getBalance().subtract(request.getAmount()));
            toCard.setBalance(toCard.getBalance().add(request.getAmount()));

            cardRepository.save(fromCard);
            cardRepository.save(toCard);

            return new TransferResponse(true, "Transfer completed successfully",
                    request.getAmount(), request.getFromCardId(), request.getToCardId());

        } catch (Exception e) {
            return new TransferResponse(false, "Transfer failed: " + e.getMessage(),
                    request.getAmount(), request.getFromCardId(), request.getToCardId());
        }
    }
}