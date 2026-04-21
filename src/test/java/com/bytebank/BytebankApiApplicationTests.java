package com.bytebank;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class BytebankApiApplicationTests {

    @Test
    void contextLoads() {
        // Valida que o contexto Spring sobe corretamente com o profile de teste
    }
}
