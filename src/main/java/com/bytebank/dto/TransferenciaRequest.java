package com.bytebank.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TransferenciaRequest {

    @NotNull(message = "Conta de origem é obrigatória")
    private Long origem;

    @NotNull(message = "Conta de destino é obrigatória")
    private Long destino;

    @NotNull(message = "Valor é obrigatório")
    @DecimalMin(value = "0.01", message = "Valor deve ser maior que zero")
    private BigDecimal valor;
}