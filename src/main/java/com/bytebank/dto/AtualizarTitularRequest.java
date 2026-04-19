package com.bytebank.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AtualizarTitularRequest {

    @NotBlank(message = "Titular é obrigatório")
    @Size(min = 3, max = 120, message = "Titular deve ter entre 3 e 120 caracteres")
    private String titular;
}