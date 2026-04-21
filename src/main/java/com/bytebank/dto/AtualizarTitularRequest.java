package com.bytebank.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Dados para atualização do titular da conta")
public class AtualizarTitularRequest {

    @NotBlank(message = "Titular é obrigatório")
    @Size(min = 3, max = 120, message = "Titular deve ter entre 3 e 120 caracteres")
    @Schema(description = "Nome completo do titular", example = "Maria Souza")
    private String titular;
}
