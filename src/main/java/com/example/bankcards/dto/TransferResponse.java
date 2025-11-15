package com.example.bankcards.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TransferResponse {
    private boolean success;
    private String message;
    private BigDecimal amount;
    private Long fromCardId;
    private Long toCardId;
    private LocalDateTime timestamp;

    public TransferResponse(boolean success, String message, BigDecimal amount, Long fromCardId, Long toCardId) {
        this.success = success;
        this.message = message;
        this.amount = amount;
        this.fromCardId = fromCardId;
        this.toCardId = toCardId;
        this.timestamp = LocalDateTime.now();
    }
}
