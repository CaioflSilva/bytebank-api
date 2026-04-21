package com.bytebank.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Dados para criação de uma nova conta bancária")
public class ContaRequest {

    @NotBlank(message = "CPF é obrigatório")
    @Pattern(regexp = "\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2}", message = "CPF deve estar no formato 000.000.000-00")
    @Schema(description = "CPF do titular no formato 000.000.000-00", example = "529.982.247-25")
    private String cpf;

    @NotBlank(message = "Titular é obrigatório")
    @Size(min = 3, max = 120, message = "Titular deve ter entre 3 e 120 caracteres")
    @Schema(description = "Nome completo do titular", example = "João Silva")
    private String titular;

    @NotNull(message = "Saldo é obrigatório")
    @DecimalMin(value = "0.0", inclusive = true, message = "Saldo inicial não pode ser negativo")
    @Schema(description = "Saldo inicial da conta", example = "500.00")
    private BigDecimal saldo;
}
