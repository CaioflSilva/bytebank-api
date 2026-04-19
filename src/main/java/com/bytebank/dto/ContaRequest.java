package com.bytebank.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ContaRequest {

    @NotBlank(message = "Titular é obrigatório")
    @Size(min = 3, max = 120, message = "Titular deve ter entre 3 e 120 caracteres")
    private String titular;

    @NotNull(message = "Saldo é obrigatório")
    @DecimalMin(value = "0.0", inclusive = true, message = "Saldo não pode ser negativo")
    private BigDecimal saldo;
}