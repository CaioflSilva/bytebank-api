package com.bytebank.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ContaResponse {
    private Long id;
    private String titular;
    private BigDecimal saldo;
}