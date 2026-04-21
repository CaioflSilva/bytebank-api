package com.bytebank.controller;

import com.bytebank.dto.AtualizarTitularRequest;
import com.bytebank.dto.ContaRequest;
import com.bytebank.dto.ContaResponse;
import com.bytebank.dto.MensagemResponse;
import com.bytebank.dto.TransferenciaRequest;
import com.bytebank.dto.ValorRequest;
import com.bytebank.service.ContaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/contas")
@RequiredArgsConstructor
@Tag(name = "Contas", description = "Gerenciamento de contas bancárias")
@SecurityRequirement(name = "bearerAuth")
public class ContaController {

    private final ContaService service;

    @Operation(summary = "Criar nova conta", description = "Cria uma nova conta bancária. O CPF deve ser válido e único.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Conta criada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos ou CPF já cadastrado")
    })
    @PostMapping
    public ResponseEntity<ContaResponse> criar(@Valid @RequestBody ContaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.criar(request));
    }

    @Operation(summary = "Listar contas com paginação")
    @ApiResponse(responseCode = "200", description = "Lista paginada de contas")
    @GetMapping
    public ResponseEntity<Page<ContaResponse>> listar(
            @PageableDefault(size = 10, sort = "titular")
            @Parameter(hidden = true) Pageable pageable) {
        return ResponseEntity.ok(service.listar(pageable));
    }

    @Operation(summary = "Buscar conta por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Conta encontrada"),
            @ApiResponse(responseCode = "404", description = "Conta não encontrada")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ContaResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    @Operation(summary = "Atualizar nome do titular")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Titular atualizado"),
            @ApiResponse(responseCode = "404", description = "Conta não encontrada")
    })
    @PatchMapping("/{id}/titular")
    public ResponseEntity<ContaResponse> atualizarTitular(
            @PathVariable Long id,
            @Valid @RequestBody AtualizarTitularRequest request) {
        return ResponseEntity.ok(service.atualizarTitular(id, request));
    }

    @Operation(summary = "Realizar depósito")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Depósito realizado"),
            @ApiResponse(responseCode = "400", description = "Valor inválido"),
            @ApiResponse(responseCode = "404", description = "Conta não encontrada")
    })
    @PutMapping("/{id}/deposito")
    public ResponseEntity<ContaResponse> depositar(
            @PathVariable Long id,
            @Valid @RequestBody ValorRequest request) {
        return ResponseEntity.ok(service.depositar(id, request.getValor()));
    }

    @Operation(summary = "Realizar saque")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Saque realizado"),
            @ApiResponse(responseCode = "400", description = "Saldo insuficiente ou valor inválido"),
            @ApiResponse(responseCode = "404", description = "Conta não encontrada")
    })
    @PutMapping("/{id}/saque")
    public ResponseEntity<ContaResponse> sacar(
            @PathVariable Long id,
            @Valid @RequestBody ValorRequest request) {
        return ResponseEntity.ok(service.sacar(id, request.getValor()));
    }

    @Operation(summary = "Realizar transferência entre contas")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Transferência realizada"),
            @ApiResponse(responseCode = "400", description = "Saldo insuficiente, valor inválido ou contas iguais"),
            @ApiResponse(responseCode = "404", description = "Conta origem ou destino não encontrada")
    })
    @PutMapping("/transferencia")
    public ResponseEntity<MensagemResponse> transferir(@Valid @RequestBody TransferenciaRequest request) {
        return ResponseEntity.ok(service.transferir(request));
    }

    @Operation(summary = "Excluir conta (apenas ADMIN)", description = "Somente administradores podem excluir contas. O saldo deve ser zero.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Conta excluída"),
            @ApiResponse(responseCode = "400", description = "Conta com saldo"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Conta não encontrada")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MensagemResponse> excluir(@PathVariable Long id) {
        return ResponseEntity.ok(service.excluir(id));
    }
}
