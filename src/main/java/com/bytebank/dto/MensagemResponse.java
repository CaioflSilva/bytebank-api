package com.bytebank.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Resposta com mensagem de confirmação")
public class MensagemResponse {

    @Schema(description = "Mensagem de retorno da operação", example = "Operação realizada com sucesso")
    private String mensagem;
}
