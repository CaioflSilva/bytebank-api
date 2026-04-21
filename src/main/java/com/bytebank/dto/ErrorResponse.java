package com.bytebank.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Resposta de erro padronizada")
public class ErrorResponse {

    @Schema(description = "Código HTTP do erro", example = "400")
    private int status;

    @Schema(description = "Mensagem de erro", example = "CPF já cadastrado")
    private String message;

    @Schema(description = "Momento do erro")
    private LocalDateTime timestamp;

    @Schema(description = "Erros de validação por campo (apenas para erros 400 de validação)")
    private Map<String, String> errors;
}
