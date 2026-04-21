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
@Schema(description = "Dados para realizar uma transferência entre contas")
public class TransferenciaRequest {

    @NotNull(message = "Conta de origem é obrigatória")
    @Schema(description = "ID da conta de origem", example = "1")
    private Long origem;

    @NotNull(message = "Conta de destino é obrigatória")
    @Schema(description = "ID da conta de destino", example = "2")
    private Long destino;

    @NotNull(message = "Valor é obrigatório")
    @DecimalMin(value = "0.01", message = "Valor deve ser maior que zero")
    @Schema(description = "Valor a transferir", example = "150.00")
    private BigDecimal valor;
}
