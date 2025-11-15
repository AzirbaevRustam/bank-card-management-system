package com.example.bankcards.dto;

import com.example.bankcards.entity.CardStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CardDto {
    private Long id;
    private String maskedCardNumber;
    private String ownerName;
    private LocalDate expiryDate;
    private BigDecimal balance;
    private CardStatus status;
    private Long userId;
}
