package com.bytebank.repository;

import com.bytebank.model.Conta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ContaRepository extends JpaRepository<Conta, Long> {

    boolean existsByCpf(String cpf);

    Optional<Conta> findByCpf(String cpf);
}
