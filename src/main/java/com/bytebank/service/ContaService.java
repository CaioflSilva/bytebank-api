package com.bytebank.service;

import com.bytebank.dto.AtualizarTitularRequest;
import com.bytebank.dto.ContaRequest;
import com.bytebank.dto.ContaResponse;
import com.bytebank.dto.MensagemResponse;
import com.bytebank.dto.TransferenciaRequest;
import com.bytebank.exception.RecursoNaoEncontradoException;
import com.bytebank.exception.RegraDeNegocioException;
import com.bytebank.model.Conta;
import com.bytebank.repository.ContaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
public class ContaService {

    private final ContaRepository repository;

    @Transactional
    public ContaResponse criar(ContaRequest request) {
        validarCpf(request.getCpf());

        if (repository.existsByCpf(request.getCpf())) {
            throw new RegraDeNegocioException("Já existe uma conta cadastrada com o CPF: " + request.getCpf());
        }

        Conta conta = Conta.builder()
                .cpf(request.getCpf())
                .titular(request.getTitular().trim())
                .saldo(normalizarValor(request.getSaldo()))
                .build();

        return toResponse(repository.save(conta));
    }

    @Transactional(readOnly = true)
    public Page<ContaResponse> listar(Pageable pageable) {
        return repository.findAll(pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public ContaResponse buscarPorId(Long id) {
        return toResponse(buscarEntidadePorId(id));
    }

    @Transactional
    public ContaResponse atualizarTitular(Long id, AtualizarTitularRequest request) {
        Conta conta = buscarEntidadePorId(id);
        conta.setTitular(request.getTitular().trim());
        return toResponse(conta);
    }

    @Transactional
    public ContaResponse depositar(Long id, BigDecimal valor) {
        validarValorPositivo(valor);
        Conta conta = buscarEntidadePorId(id);
        conta.setSaldo(conta.getSaldo().add(normalizarValor(valor)));
        return toResponse(conta);
    }

    @Transactional
    public ContaResponse sacar(Long id, BigDecimal valor) {
        validarValorPositivo(valor);
        Conta conta = buscarEntidadePorId(id);
        BigDecimal valorNormalizado = normalizarValor(valor);

        if (conta.getSaldo().compareTo(valorNormalizado) < 0) {
            throw new RegraDeNegocioException("Saldo insuficiente");
        }

        conta.setSaldo(conta.getSaldo().subtract(valorNormalizado));
        return toResponse(conta);
    }

    @Transactional
    public MensagemResponse transferir(TransferenciaRequest request) {
        validarValorPositivo(request.getValor());

        if (request.getOrigem().equals(request.getDestino())) {
            throw new RegraDeNegocioException("A conta de origem deve ser diferente da conta de destino");
        }

        Conta origem = buscarEntidadePorId(request.getOrigem());
        Conta destino = buscarEntidadePorId(request.getDestino());
        BigDecimal valorNormalizado = normalizarValor(request.getValor());

        if (origem.getSaldo().compareTo(valorNormalizado) < 0) {
            throw new RegraDeNegocioException("Saldo insuficiente para transferência");
        }

        origem.setSaldo(origem.getSaldo().subtract(valorNormalizado));
        destino.setSaldo(destino.getSaldo().add(valorNormalizado));

        return new MensagemResponse("Transferência realizada com sucesso");
    }

    @Transactional
    public MensagemResponse excluir(Long id) {
        Conta conta = buscarEntidadePorId(id);

        if (conta.getSaldo().compareTo(BigDecimal.ZERO) != 0) {
            throw new RegraDeNegocioException("A conta só pode ser excluída com saldo zerado");
        }

        repository.delete(conta);
        return new MensagemResponse("Conta excluída com sucesso");
    }

    // ==================== PRIVADOS ====================

    private ContaResponse toResponse(Conta conta) {
        return new ContaResponse(
                conta.getId(),
                conta.getCpf(),
                conta.getTitular(),
                conta.getSaldo()
        );
    }

    private Conta buscarEntidadePorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Conta não encontrada para o id: " + id));
    }

    private void validarValorPositivo(BigDecimal valor) {
        if (valor == null || valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RegraDeNegocioException("O valor deve ser maior que zero");
        }
    }

    private BigDecimal normalizarValor(BigDecimal valor) {
        return valor.setScale(2, RoundingMode.HALF_EVEN);
    }

    private void validarCpf(String cpf) {
        String digitos = cpf.replaceAll("[^0-9]", "");

        if (digitos.length() != 11 || digitos.chars().distinct().count() == 1) {
            throw new RegraDeNegocioException("CPF inválido: " + cpf);
        }

        int soma1 = 0;
        for (int i = 0; i < 9; i++) soma1 += Character.getNumericValue(digitos.charAt(i)) * (10 - i);
        int digito1 = 11 - (soma1 % 11);
        if (digito1 >= 10) digito1 = 0;

        int soma2 = 0;
        for (int i = 0; i < 10; i++) soma2 += Character.getNumericValue(digitos.charAt(i)) * (11 - i);
        int digito2 = 11 - (soma2 % 11);
        if (digito2 >= 10) digito2 = 0;

        if (Character.getNumericValue(digitos.charAt(9)) != digito1 ||
                Character.getNumericValue(digitos.charAt(10)) != digito2) {
            throw new RegraDeNegocioException("CPF inválido: " + cpf);
        }
    }
}