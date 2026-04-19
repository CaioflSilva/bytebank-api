package com.bytebank.controller;

import com.bytebank.dto.AtualizarTitularRequest;
import com.bytebank.dto.ContaRequest;
import com.bytebank.dto.ContaResponse;
import com.bytebank.dto.MensagemResponse;
import com.bytebank.dto.TransferenciaRequest;
import com.bytebank.dto.ValorRequest;
import com.bytebank.service.ContaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/contas")
public class ContaController {

    private final ContaService service;

    public ContaController(ContaService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<ContaResponse> criar(@Valid @RequestBody ContaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.criar(request));
    }

    @GetMapping
    public ResponseEntity<List<ContaResponse>> listar() {
        return ResponseEntity.ok(service.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ContaResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    @PatchMapping("/{id}/titular")
    public ResponseEntity<ContaResponse> atualizarTitular(@PathVariable Long id,
                                                          @Valid @RequestBody AtualizarTitularRequest request) {
        return ResponseEntity.ok(service.atualizarTitular(id, request));
    }

    @PutMapping("/{id}/deposito")
    public ResponseEntity<ContaResponse> depositar(@PathVariable Long id,
                                                   @Valid @RequestBody ValorRequest request) {
        return ResponseEntity.ok(service.depositar(id, request.getValor()));
    }

    @PutMapping("/{id}/saque")
    public ResponseEntity<ContaResponse> sacar(@PathVariable Long id,
                                               @Valid @RequestBody ValorRequest request) {
        return ResponseEntity.ok(service.sacar(id, request.getValor()));
    }

    @PutMapping("/transferencia")
    public ResponseEntity<MensagemResponse> transferir(@Valid @RequestBody TransferenciaRequest request) {
        return ResponseEntity.ok(service.transferir(request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<MensagemResponse> excluir(@PathVariable Long id) {
        return ResponseEntity.ok(service.excluir(id));
    }
}