package com.bytebank.service;

import com.bytebank.dto.AuthResponse;
import com.bytebank.dto.LoginRequest;
import com.bytebank.dto.RegisterRequest;
import com.bytebank.exception.RegraDeNegocioException;
import com.bytebank.model.Role;
import com.bytebank.model.Usuario;
import com.bytebank.repository.UsuarioRepository;
import com.bytebank.security.JwtService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UsuarioRepository repository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtService jwtService;
    @Mock private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService service;

    @Test
    @DisplayName("Deve registrar novo usuário com sucesso")
    void deveRegistrar() {
        RegisterRequest request = new RegisterRequest("joao@email.com", "senha123");
        Usuario usuario = Usuario.builder().id(1L).email("joao@email.com").senha("hash").role(Role.USER).build();

        when(repository.existsByEmail("joao@email.com")).thenReturn(false);
        when(passwordEncoder.encode("senha123")).thenReturn("hash");
        when(repository.save(any())).thenReturn(usuario);
        when(jwtService.gerarToken(any())).thenReturn("token123");

        AuthResponse response = service.registrar(request);

        assertThat(response.getToken()).isEqualTo("token123");
        assertThat(response.getTipo()).isEqualTo("Bearer");
        verify(repository).save(any());
    }

    @Test
    @DisplayName("Deve lançar exceção ao registrar email duplicado")
    void deveLancarExcecaoEmailDuplicado() {
        when(repository.existsByEmail("joao@email.com")).thenReturn(true);

        assertThatThrownBy(() -> service.registrar(new RegisterRequest("joao@email.com", "senha123")))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("joao@email.com");

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Deve fazer login e retornar token")
    void deveLogin() {
        LoginRequest request = new LoginRequest("joao@email.com", "senha123");
        Usuario usuario = Usuario.builder().id(1L).email("joao@email.com").senha("hash").role(Role.USER).build();

        when(repository.findByEmail("joao@email.com")).thenReturn(Optional.of(usuario));
        when(jwtService.gerarToken(usuario)).thenReturn("jwt-token");

        AuthResponse response = service.login(request);

        assertThat(response.getToken()).isEqualTo("jwt-token");
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }
}
