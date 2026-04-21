package com.bytebank.service;

import com.bytebank.dto.AtualizarTitularRequest;
import com.bytebank.dto.ContaRequest;
import com.bytebank.dto.ContaResponse;
import com.bytebank.dto.MensagemResponse;
import com.bytebank.dto.TransferenciaRequest;
import com.bytebank.exception.RecursoNaoEncontradoException;
import com.bytebank.exception.RegraDeNegocioException;
import com.bytebank.mapper.ContaMapper;
import com.bytebank.model.Conta;
import com.bytebank.repository.ContaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContaServiceTest {

    @Mock
    private ContaRepository repository;

    @Mock
    private ContaMapper mapper;

    @InjectMocks
    private ContaService service;

    private Conta contaExemplo;
    private ContaResponse responseExemplo;

    // CPF válido real: 529.982.247-25
    private static final String CPF_VALIDO = "529.982.247-25";

    @BeforeEach
    void setUp() {
        contaExemplo = Conta.builder()
                .id(1L).cpf(CPF_VALIDO).titular("João Silva")
                .saldo(new BigDecimal("1000.00")).build();

        responseExemplo = new ContaResponse(1L, CPF_VALIDO, "João Silva", new BigDecimal("1000.00"));
    }

    // ==================== CRIAR ====================

    @Test
    @DisplayName("Deve criar conta com CPF válido")
    void deveCriarContaComSucesso() {
        ContaRequest request = new ContaRequest(CPF_VALIDO, "João Silva", new BigDecimal("1000.00"));
        when(repository.existsByCpf(CPF_VALIDO)).thenReturn(false);
        when(repository.save(any())).thenReturn(contaExemplo);
        when(mapper.toResponse(any())).thenReturn(responseExemplo);

        ContaResponse response = service.criar(request);

        assertThat(response.getCpf()).isEqualTo(CPF_VALIDO);
        verify(repository).save(any());
    }

    @Test
    @DisplayName("Deve lançar exceção com CPF inválido")
    void deveLancarExcecaoComCpfInvalido() {
        ContaRequest request = new ContaRequest("111.111.111-11", "João", new BigDecimal("100.00"));

        assertThatThrownBy(() -> service.criar(request))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("CPF inválido");
    }

    @Test
    @DisplayName("Deve lançar exceção com CPF duplicado")
    void deveLancarExcecaoComCpfDuplicado() {
        ContaRequest request = new ContaRequest(CPF_VALIDO, "João", new BigDecimal("100.00"));
        when(repository.existsByCpf(CPF_VALIDO)).thenReturn(true);

        assertThatThrownBy(() -> service.criar(request))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining(CPF_VALIDO);

        verify(repository, never()).save(any());
    }

    // ==================== LISTAR ====================

    @Test
    @DisplayName("Deve listar contas paginadas")
    void deveListarContasPaginadas() {
        PageRequest pageable = PageRequest.of(0, 10);
        Page<Conta> page = new PageImpl<>(List.of(contaExemplo));
        when(repository.findAll(pageable)).thenReturn(page);
        when(mapper.toResponse(any())).thenReturn(responseExemplo);

        Page<ContaResponse> resultado = service.listar(pageable);

        assertThat(resultado.getContent()).hasSize(1);
        assertThat(resultado.getTotalElements()).isEqualTo(1);
    }

    // ==================== BUSCAR ====================

    @Test
    @DisplayName("Deve buscar conta por id")
    void deveBuscarPorId() {
        when(repository.findById(1L)).thenReturn(Optional.of(contaExemplo));
        when(mapper.toResponse(contaExemplo)).thenReturn(responseExemplo);

        ContaResponse response = service.buscarPorId(1L);
        assertThat(response.getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Deve lançar exceção quando conta não encontrada")
    void deveLancarExcecaoContaNaoEncontrada() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.buscarPorId(99L))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }

    // ==================== ATUALIZAR TITULAR ====================

    @Test
    @DisplayName("Deve atualizar titular sem chamar save()")
    void deveAtualizarTitularSemSave() {
        when(repository.findById(1L)).thenReturn(Optional.of(contaExemplo));
        when(mapper.toResponse(any())).thenReturn(new ContaResponse(1L, CPF_VALIDO, "Maria Souza", new BigDecimal("1000.00")));

        ContaResponse response = service.atualizarTitular(1L, new AtualizarTitularRequest("Maria Souza"));

        assertThat(response.getTitular()).isEqualTo("Maria Souza");
        verify(repository, never()).save(any());
    }

    // ==================== DEPOSITAR ====================

    @Test
    @DisplayName("Deve depositar e retornar saldo atualizado")
    void deveDepositar() {
        when(repository.findById(1L)).thenReturn(Optional.of(contaExemplo));
        when(mapper.toResponse(any())).thenAnswer(inv -> {
            Conta c = inv.getArgument(0);
            return new ContaResponse(c.getId(), c.getCpf(), c.getTitular(), c.getSaldo());
        });

        ContaResponse response = service.depositar(1L, new BigDecimal("500.00"));

        assertThat(response.getSaldo()).isEqualByComparingTo("1500.00");
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar exceção ao depositar valor zero ou negativo")
    void deveLancarExcecaoDepositoInvalido() {
        assertThatThrownBy(() -> service.depositar(1L, BigDecimal.ZERO))
                .isInstanceOf(RegraDeNegocioException.class);
        assertThatThrownBy(() -> service.depositar(1L, new BigDecimal("-1")))
                .isInstanceOf(RegraDeNegocioException.class);
    }

    // ==================== SACAR ====================

    @Test
    @DisplayName("Deve sacar e retornar saldo atualizado")
    void deveSacar() {
        when(repository.findById(1L)).thenReturn(Optional.of(contaExemplo));
        when(mapper.toResponse(any())).thenAnswer(inv -> {
            Conta c = inv.getArgument(0);
            return new ContaResponse(c.getId(), c.getCpf(), c.getTitular(), c.getSaldo());
        });

        ContaResponse response = service.sacar(1L, new BigDecimal("300.00"));
        assertThat(response.getSaldo()).isEqualByComparingTo("700.00");
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar exceção ao sacar com saldo insuficiente")
    void deveLancarExcecaoSaldoInsuficiente() {
        when(repository.findById(1L)).thenReturn(Optional.of(contaExemplo));

        assertThatThrownBy(() -> service.sacar(1L, new BigDecimal("9999.00")))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("Saldo insuficiente");
    }

    // ==================== TRANSFERIR ====================

    @Test
    @DisplayName("Deve transferir entre contas com sucesso")
    void deveTransferir() {
        // CPF válido para conta destino: 987.654.321-00 NÃO é válido, usando outro
        Conta destino = Conta.builder().id(2L).cpf("852.455.270-63")
                .titular("Maria").saldo(new BigDecimal("200.00")).build();

        when(repository.findById(1L)).thenReturn(Optional.of(contaExemplo));
        when(repository.findById(2L)).thenReturn(Optional.of(destino));

        MensagemResponse response = service.transferir(new TransferenciaRequest(1L, 2L, new BigDecimal("300.00")));

        assertThat(response.getMensagem()).contains("sucesso");
        assertThat(contaExemplo.getSaldo()).isEqualByComparingTo("700.00");
        assertThat(destino.getSaldo()).isEqualByComparingTo("500.00");
    }

    @Test
    @DisplayName("Deve lançar exceção ao transferir para mesma conta")
    void deveLancarExcecaoTransferenciaParaMesmaConta() {
        assertThatThrownBy(() -> service.transferir(new TransferenciaRequest(1L, 1L, new BigDecimal("100.00"))))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("diferente");
    }

    // ==================== EXCLUIR ====================

    @Test
    @DisplayName("Deve excluir conta com saldo zero")
    void deveExcluirContaComSaldoZero() {
        contaExemplo.setSaldo(BigDecimal.ZERO);
        when(repository.findById(1L)).thenReturn(Optional.of(contaExemplo));

        MensagemResponse response = service.excluir(1L);

        assertThat(response.getMensagem()).contains("sucesso");
        verify(repository).delete(contaExemplo);
    }

    @Test
    @DisplayName("Deve lançar exceção ao excluir conta com saldo")
    void deveLancarExcecaoExcluirContaComSaldo() {
        when(repository.findById(1L)).thenReturn(Optional.of(contaExemplo));

        assertThatThrownBy(() -> service.excluir(1L))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("saldo zerado");

        verify(repository, never()).delete(any());
    }
}
