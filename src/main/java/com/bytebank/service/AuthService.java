package com.bytebank.service;

import com.bytebank.dto.AuthResponse;
import com.bytebank.dto.LoginRequest;
import com.bytebank.dto.RegisterRequest;
import com.bytebank.exception.RegraDeNegocioException;
import com.bytebank.model.Role;
import com.bytebank.model.Usuario;
import com.bytebank.repository.UsuarioRepository;
import com.bytebank.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthResponse registrar(RegisterRequest request) {
        if (repository.existsByEmail(request.getEmail())) {
            throw new RegraDeNegocioException("Email já cadastrado: " + request.getEmail());
        }

        Usuario usuario = Usuario.builder()
                .email(request.getEmail())
                .senha(passwordEncoder.encode(request.getSenha()))
                .role(Role.USER)
                .build();

        repository.save(usuario);
        return new AuthResponse(jwtService.gerarToken(usuario));
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getSenha())
        );
        Usuario usuario = repository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RegraDeNegocioException("Usuário não encontrado"));
        return new AuthResponse(jwtService.gerarToken(usuario));
    }
}
