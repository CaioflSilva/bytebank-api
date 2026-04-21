package com.bytebank.controller;

import com.bytebank.dto.ContaRequest;
import com.bytebank.dto.ContaResponse;
import com.bytebank.dto.MensagemResponse;
import com.bytebank.exception.RecursoNaoEncontradoException;
import com.bytebank.exception.RegraDeNegocioException;
import com.bytebank.service.ContaService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import com.bytebank.config.SecurityConfig;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ContaController.class)
@Import(SecurityConfig.class)
class ContaControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean private ContaService service;

    // Mocks de dependências do SecurityConfig
    @MockBean private com.bytebank.security.JwtAuthFilter jwtAuthFilter;
    @MockBean private com.bytebank.repository.UsuarioRepository usuarioRepository;
    @MockBean private com.bytebank.security.JwtService jwtService;

    private ContaResponse contaResponse() {
        return new ContaResponse(1L, "529.982.247-25", "João Silva", new BigDecimal("1000.00"));
    }

    // ==================== POST /contas ====================

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("POST /contas — deve criar conta e retornar 201")
    void deveCriarConta() throws Exception {
        ContaRequest request = new ContaRequest("529.982.247-25", "João Silva", new BigDecimal("1000.00"));
        when(service.criar(any())).thenReturn(contaResponse());

        mockMvc.perform(post("/contas").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.cpf").value("529.982.247-25"))
                .andExpect(jsonPath("$.titular").value("João Silva"))
                .andExpect(jsonPath("$.saldo").value(1000.00));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("POST /contas — deve retornar 400 com dados inválidos")
    void deveRetornar400DadosInvalidos() throws Exception {
        ContaRequest request = new ContaRequest("", "", null);

        mockMvc.perform(post("/contas").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("POST /contas — deve retornar 400 quando CPF duplicado")
    void deveRetornar400CpfDuplicado() throws Exception {
        ContaRequest request = new ContaRequest("529.982.247-25", "João Silva", new BigDecimal("500.00"));
        when(service.criar(any())).thenThrow(new RegraDeNegocioException("CPF já cadastrado"));

        mockMvc.perform(post("/contas").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /contas — deve retornar 403 sem autenticação")
    void deveRetornar403SemAutenticacao() throws Exception {
        mockMvc.perform(post("/contas").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
    }

    // ==================== GET /contas ====================

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("GET /contas — deve listar contas paginadas")
    void deveListarContasPaginadas() throws Exception {
        Page<ContaResponse> page = new PageImpl<>(List.of(contaResponse()), PageRequest.of(0, 10), 1);
        when(service.listar(any())).thenReturn(page);

        mockMvc.perform(get("/contas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].titular").value("João Silva"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    // ==================== GET /contas/{id} ====================

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("GET /contas/{id} — deve retornar conta")
    void deveBuscarPorId() throws Exception {
        when(service.buscarPorId(1L)).thenReturn(contaResponse());

        mockMvc.perform(get("/contas/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.cpf").value("529.982.247-25"));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("GET /contas/{id} — deve retornar 404 para conta inexistente")
    void deveRetornar404() throws Exception {
        when(service.buscarPorId(99L)).thenThrow(new RecursoNaoEncontradoException("Conta não encontrada"));

        mockMvc.perform(get("/contas/99"))
                .andExpect(status().isNotFound());
    }

    // ==================== PUT deposito ====================

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("PUT /contas/{id}/deposito — deve depositar")
    void deveDepositar() throws Exception {
        ContaResponse atualizada = new ContaResponse(1L, "529.982.247-25", "João Silva", new BigDecimal("1500.00"));
        when(service.depositar(eq(1L), any())).thenReturn(atualizada);

        mockMvc.perform(put("/contas/1/deposito").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"valor\": 500.00}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.saldo").value(1500.00));
    }

    // ==================== DELETE ====================

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("DELETE /contas/{id} — ADMIN deve excluir conta")
    void adminDeveExcluir() throws Exception {
        when(service.excluir(1L)).thenReturn(new MensagemResponse("Conta excluída com sucesso"));

        mockMvc.perform(delete("/contas/1").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensagem").value("Conta excluída com sucesso"));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("DELETE /contas/{id} — USER não deve excluir (403)")
    void userNaoDeveExcluir() throws Exception {
        mockMvc.perform(delete("/contas/1").with(csrf()))
                .andExpect(status().isForbidden());
    }
}
