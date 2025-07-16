package com.exemplo.credito.controller;

import com.exemplo.credito.config.TestConfig;
import com.exemplo.credito.entity.Credito;
import com.exemplo.credito.service.CreditoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import jakarta.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CreditoController.class)
@ActiveProfiles("test")
@Import(TestConfig.class)
class CreditoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CreditoService creditoService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void deveRetornarListaDeCreditosPorNfseComSucesso() throws Exception {
        // Arrange
        List<Credito> creditos = Arrays.asList(
                criarCredito("123456", "NFSE789", LocalDate.of(2024, 2, 15)),
                criarCredito("789123", "NFSE789", LocalDate.of(2024, 2, 20))
        );

        when(creditoService.buscarPorNfse("NFSE789")).thenReturn(creditos);

        // Act & Assert
        mockMvc.perform(get("/api/creditos/NFSE789")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].numeroCredito").value("123456"))
                .andExpect(jsonPath("$[1].numeroCredito").value("789123"));
    }

    @Test
    void deveRetornarListaVaziaQuandoNaoEncontrarCreditos() throws Exception {
        // Arrange
        when(creditoService.buscarPorNfse("NFSE999")).thenReturn(Collections.emptyList());

        // Act & Assert
        mockMvc.perform(get("/api/creditos/NFSE999")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void deveRetornarCreditoEspecificoComSucesso() throws Exception {
        // Arrange
        Credito credito = criarCredito("123456", "NFSE789", LocalDate.of(2024, 2, 15));
        when(creditoService.buscarPorNumeroCredito("123456")).thenReturn(credito);

        // Act & Assert
        mockMvc.perform(get("/api/creditos/credito/123456")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.numeroCredito").value("123456"))
                .andExpect(jsonPath("$.numeroNfse").value("NFSE789"));
    }

    @Test
    void deveRetornar404QuandoCreditoNaoEncontrado() throws Exception {
        // Arrange
        when(creditoService.buscarPorNumeroCredito(anyString()))
                .thenThrow(new EntityNotFoundException("Crédito não encontrado"));

        // Act & Assert
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