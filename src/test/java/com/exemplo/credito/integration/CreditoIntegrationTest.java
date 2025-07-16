package com.exemplo.credito.integration;

import com.exemplo.credito.config.TestConfig;
import com.exemplo.credito.entity.Credito;
import com.exemplo.credito.repository.CreditoRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestConfig.class)
@Transactional
class CreditoIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private CreditoRepository creditoRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        creditoRepository.deleteAll();
        
        // Preparar dados de teste
        Credito credito1 = criarCredito("123456", "NFSE789", LocalDate.of(2024, 2, 25));
        Credito credito2 = criarCredito("789123", "NFSE789", LocalDate.of(2024, 2, 26));
        Credito credito3 = criarCredito("456789", "NFSE999", LocalDate.of(2024, 3, 1));
        
        creditoRepository.save(credito1);
        creditoRepository.save(credito2);
        creditoRepository.save(credito3);
    }

    @Test
    void deveRetornarListaDeCreditosPorNfseIntegracao() throws Exception {
        mockMvc.perform(get("/api/creditos/NFSE789")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].numeroNfse").value("NFSE789"))
                .andExpect(jsonPath("$[1].numeroNfse").value("NFSE789"));
    }

    @Test
    void deveRetornarListaVaziaQuandoNfseNaoExisteIntegracao() throws Exception {
        mockMvc.perform(get("/api/creditos/NFSE_INEXISTENTE")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void deveRetornarCreditoEspecificoIntegracao() throws Exception {
        mockMvc.perform(get("/api/creditos/credito/123456")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.numeroCredito").value("123456"))
                .andExpect(jsonPath("$.numeroNfse").value("NFSE789"))
                .andExpect(jsonPath("$.dataConstituicao").value("2024-02-25"))
                .andExpect(jsonPath("$.tipoCredito").value("PRINCIPAL"));
    }

    @Test
    void deveRetornar404QuandoCreditoNaoExisteIntegracao() throws Exception {
        mockMvc.perform(get("/api/creditos/credito/999999")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Crédito não encontrado"));
    }

    private Credito criarCredito(String numeroCredito, String numeroNfse, LocalDate dataConstituicao) {
        Credito credito = new Credito();
        credito.setNumeroCredito(numeroCredito);
        credito.setNumeroNfse(numeroNfse);
        credito.setDataConstituicao(dataConstituicao);
        credito.setTipoCredito("PRINCIPAL");
        credito.setValorFaturado(new BigDecimal("1000.00"));
        credito.setBaseCalculo(new BigDecimal("1000.00"));
        credito.setAliquota(new BigDecimal("5.00"));
        credito.setValorIssqn(new BigDecimal("50.00"));
        credito.setValorDeducao(new BigDecimal("0.00"));
        credito.setSimplesNacional(false);
        return credito;
    }
} 