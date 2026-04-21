package com.bytebank.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Dados da conta bancária retornados pela API")
public class ContaResponse {

    @Schema(description = "ID único da conta", example = "1")
    private Long id;

    @Schema(description = "CPF do titular", example = "529.982.247-25")
    private String cpf;

    @Schema(description = "Nome do titular", example = "João Silva")
    private String titular;

    @Schema(description = "Saldo atual da conta", example = "1250.00")
    private BigDecimal saldo;
}
