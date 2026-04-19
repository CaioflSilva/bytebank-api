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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class ContaService {

    private final ContaRepository repository;

    public ContaService(ContaRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public ContaResponse criar(ContaRequest request) {
        Conta conta = Conta.builder()
                .titular(request.getTitular().trim())
                .saldo(normalizarValor(request.getSaldo()))
                .build();

        return toResponse(repository.save(conta));
    }

    @Transactional(readOnly = true)
    public List<ContaResponse> listar() {
        return repository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ContaResponse buscarPorId(Long id) {
        return toResponse(buscarEntidadePorId(id));
    }

    @Transactional
    public ContaResponse atualizarTitular(Long id, AtualizarTitularRequest request) {
        Conta conta = buscarEntidadePorId(id);
        conta.setTitular(request.getTitular().trim());

        return toResponse(repository.save(conta));
    }

    @Transactional
    public ContaResponse depositar(Long id, BigDecimal valor) {
        validarValorPositivo(valor);

        Conta conta = buscarEntidadePorId(id);
        conta.setSaldo(conta.getSaldo().add(normalizarValor(valor)));

        return toResponse(repository.save(conta));
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
        return toResponse(repository.save(conta));
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

        repository.save(origem);
        repository.save(destino);

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

    private ContaResponse toResponse(Conta conta) {
        return new ContaResponse(
                conta.getId(),
                conta.getTitular(),
                conta.getSaldo()
        );
    }
}