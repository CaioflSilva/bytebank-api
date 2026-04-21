package com.bytebank.integration;

import com.bytebank.dto.ContaRequest;
import com.bytebank.dto.LoginRequest;
import com.bytebank.dto.RegisterRequest;
import com.bytebank.repository.ContaRepository;
import com.bytebank.repository.UsuarioRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ContaIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private ContaRepository contaRepository;
    @Autowired private UsuarioRepository usuarioRepository;

    private String tokenUser;
    private String tokenAdmin;

    // CPFs válidos para testes
    private static final String CPF_1 = "529.982.247-25";
    private static final String CPF_2 = "852.455.270-63";

    @BeforeEach
    void setUp() throws Exception {
        contaRepository.deleteAll();
        usuarioRepository.deleteAll();

        // Registrar e logar como USER
        mockMvc.perform(post("/auth/registrar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new RegisterRequest("user@test.com", "senha123"))))
                .andExpect(status().isCreated());

        MvcResult loginResult = mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new LoginRequest("user@test.com", "senha123"))))
                .andExpect(status().isOk())
                .andReturn();

        String body = loginResult.getResponse().getContentAsString();
        tokenUser = objectMapper.readTree(body).get("token").asText();
    }

    private String authHeader(String token) {
        return "Bearer " + token;
    }

    @Test
    @DisplayName("Fluxo completo: criar, depositar, sacar e listar conta")
    void fluxoCompletoConta() throws Exception {
        // Criar conta
        ContaRequest request = new ContaRequest(CPF_1, "João Silva", new BigDecimal("500.00"));
        MvcResult criarResult = mockMvc.perform(post("/contas")
                        .header("Authorization", authHeader(tokenUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.cpf").value(CPF_1))
                .andExpect(jsonPath("$.saldo").value(500.00))
                .andReturn();

        Long id = objectMapper.readTree(criarResult.getResponse().getContentAsString()).get("id").asLong();

        // Depositar
        mockMvc.perform(put("/contas/" + id + "/deposito")
                        .header("Authorization", authHeader(tokenUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"valor\": 300.00}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.saldo").value(800.00));

        // Sacar
        mockMvc.perform(put("/contas/" + id + "/saque")
                        .header("Authorization", authHeader(tokenUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"valor\": 100.00}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.saldo").value(700.00));

        // Listar
        mockMvc.perform(get("/contas")
                        .header("Authorization", authHeader(tokenUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1));
    }

    @Test
    @DisplayName("Deve rejeitar requisição sem token")
    void deveRejeitarSemToken() throws Exception {
        mockMvc.perform(get("/contas"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Deve rejeitar CPF duplicado")
    void deveRejeitarCpfDuplicado() throws Exception {
        ContaRequest request = new ContaRequest(CPF_1, "João Silva", new BigDecimal("100.00"));
        String body = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/contas")
                        .header("Authorization", authHeader(tokenUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/contas")
                        .header("Authorization", authHeader(tokenUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Deve retornar 404 para conta inexistente")
    void deveRetornar404ContaInexistente() throws Exception {
        mockMvc.perform(get("/contas/9999")
                        .header("Authorization", authHeader(tokenUser)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Deve realizar transferência entre contas")
    void deveTransferir() throws Exception {
        // Criar conta origem
        MvcResult r1 = mockMvc.perform(post("/contas")
                        .header("Authorization", authHeader(tokenUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ContaRequest(CPF_1, "João", new BigDecimal("1000.00")))))
                .andExpect(status().isCreated()).andReturn();
        Long idOrigem = objectMapper.readTree(r1.getResponse().getContentAsString()).get("id").asLong();

        // Criar conta destino
        MvcResult r2 = mockMvc.perform(post("/contas")
                        .header("Authorization", authHeader(tokenUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ContaRequest(CPF_2, "Maria", new BigDecimal("200.00")))))
                .andExpect(status().isCreated()).andReturn();
        Long idDestino = objectMapper.readTree(r2.getResponse().getContentAsString()).get("id").asLong();

        // Transferir
        String transferBody = String.format("{\"origem\": %d, \"destino\": %d, \"valor\": 300.00}", idOrigem, idDestino);
        mockMvc.perform(put("/contas/transferencia")
                        .header("Authorization", authHeader(tokenUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(transferBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensagem").value("Transferência realizada com sucesso"));

        // Verificar saldos
        mockMvc.perform(get("/contas/" + idOrigem).header("Authorization", authHeader(tokenUser)))
                .andExpect(jsonPath("$.saldo").value(700.00));
        mockMvc.perform(get("/contas/" + idDestino).header("Authorization", authHeader(tokenUser)))
                .andExpect(jsonPath("$.saldo").value(500.00));
    }

    @Test
    @DisplayName("USER não pode excluir conta (403)")
    void userNaoPodeExcluir() throws Exception {
        MvcResult r = mockMvc.perform(post("/contas")
                        .header("Authorization", authHeader(tokenUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ContaRequest(CPF_1, "João", BigDecimal.ZERO))))
                .andExpect(status().isCreated()).andReturn();
        Long id = objectMapper.readTree(r.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(delete("/contas/" + id)
                        .header("Authorization", authHeader(tokenUser)))
                .andExpect(status().isForbidden());
    }
}
