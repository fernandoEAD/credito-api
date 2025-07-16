package com.exemplo.credito.service;

import com.exemplo.credito.entity.Credito;
import com.exemplo.credito.repository.CreditoRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreditoServiceTest {

    @Mock
    private CreditoRepository repository;

    @InjectMocks
    private CreditoService service;

    private Credito credito1;
    private Credito credito2;

    @BeforeEach
    void setUp() {
        credito1 = new Credito();
        credito1.setId(1L);
        credito1.setNumeroCredito("123456");
        credito1.setNumeroNfse("7891011");
        credito1.setDataConstituicao(LocalDate.of(2024, 2, 25));
        credito1.setValorIssqn(new BigDecimal("1500.75"));
        credito1.setTipoCredito("ISSQN");
        credito1.setSimplesNacional(true);
        credito1.setAliquota(new BigDecimal("5.0"));
        credito1.setValorFaturado(new BigDecimal("30000.00"));
        credito1.setValorDeducao(new BigDecimal("5000.00"));
        credito1.setBaseCalculo(new BigDecimal("25000.00"));

        credito2 = new Credito();
        credito2.setId(2L);
        credito2.setNumeroCredito("789012");
        credito2.setNumeroNfse("7891011");
        credito2.setDataConstituicao(LocalDate.of(2024, 2, 26));
        credito2.setValorIssqn(new BigDecimal("1200.50"));
        credito2.setTipoCredito("ISSQN");
        credito2.setSimplesNacional(false);
        credito2.setAliquota(new BigDecimal("4.5"));
        credito2.setValorFaturado(new BigDecimal("25000.00"));
        credito2.setValorDeducao(new BigDecimal("4000.00"));
        credito2.setBaseCalculo(new BigDecimal("21000.00"));
    }

    @Test
    @DisplayName("Deve buscar créditos por número da NFS-e com sucesso")
    void deveBuscarCreditosPorNfseComSucesso() {
        // Given
        String numeroNfse = "7891011";
        List<Credito> creditosEsperados = Arrays.asList(credito1, credito2);
        when(repository.findByNumeroNfse(numeroNfse)).thenReturn(creditosEsperados);

        // When
        List<Credito> creditosRetornados = service.buscarPorNfse(numeroNfse);

        // Then
        assertNotNull(creditosRetornados);
        assertEquals(2, creditosRetornados.size());
        assertEquals(creditosEsperados, creditosRetornados);
        verify(repository, times(1)).findByNumeroNfse(numeroNfse);
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando não encontrar créditos por NFS-e")
    void deveRetornarListaVaziaQuandoNaoEncontrarCreditosPorNfse() {
        // Given
        String numeroNfse = "inexistente";
        when(repository.findByNumeroNfse(numeroNfse)).thenReturn(Arrays.asList());

        // When
        List<Credito> creditosRetornados = service.buscarPorNfse(numeroNfse);

        // Then
        assertNotNull(creditosRetornados);
        assertTrue(creditosRetornados.isEmpty());
        verify(repository, times(1)).findByNumeroNfse(numeroNfse);
    }

    @Test
    @DisplayName("Deve buscar crédito por número do crédito com sucesso")
    void deveBuscarCreditoPorNumeroCreditoComSucesso() {
        // Given
        String numeroCredito = "123456";
        when(repository.findByNumeroCredito(numeroCredito)).thenReturn(Optional.of(credito1));

        // When
        Credito creditoRetornado = service.buscarPorNumeroCredito(numeroCredito);

        // Then
        assertNotNull(creditoRetornado);
        assertEquals(credito1, creditoRetornado);
        verify(repository, times(1)).findByNumeroCredito(numeroCredito);
    }

    @Test
    @DisplayName("Deve lançar EntityNotFoundException quando não encontrar crédito por número")
    void deveLancarExcecaoQuandoNaoEncontrarCreditoPorNumero() {
        // Given
        String numeroCredito = "inexistente";
        when(repository.findByNumeroCredito(numeroCredito)).thenReturn(Optional.empty());

        // When & Then
        EntityNotFoundException exception = assertThrows(
            EntityNotFoundException.class,
            () -> service.buscarPorNumeroCredito(numeroCredito)
        );

        assertEquals("Crédito não encontrado", exception.getMessage());
        verify(repository, times(1)).findByNumeroCredito(numeroCredito);
    }
} 