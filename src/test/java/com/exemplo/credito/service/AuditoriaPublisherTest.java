package com.exemplo.credito.service;

import com.exemplo.credito.event.ConsultaAuditoriaEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class AuditoriaPublisherTest {

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private AuditoriaPublisher auditoriaPublisher;

    @BeforeEach
    void setUp() {
        // Configurar o topic para testes usando reflection
        ReflectionTestUtils.setField(auditoriaPublisher, "topicAuditoria", "test-topic");
    }

    @Test
    void deveEnviarEventoComSucesso() throws JsonProcessingException {
        // Arrange
        ConsultaAuditoriaEvent evento = new ConsultaAuditoriaEvent();
        evento.setId("test-id-123");
        evento.setEndpoint("/api/creditos/123");
        evento.setParametro("numeroCredito=123");

        String eventoJson = "{\"id\":\"test-id-123\",\"endpoint\":\"/api/creditos/123\",\"parametro\":\"numeroCredito=123\"}";
        
        CompletableFuture<SendResult<String, String>> future = new CompletableFuture<>();
        SendResult<String, String> sendResult = mock(SendResult.class);
        future.complete(sendResult);

        when(objectMapper.writeValueAsString(evento)).thenReturn(eventoJson);
        when(kafkaTemplate.send(eq("test-topic"), anyString(), eq(eventoJson))).thenReturn(future);

        // Act
        auditoriaPublisher.publicarEventoAuditoria(evento);

        // Assert
        verify(objectMapper).writeValueAsString(evento);
        verify(kafkaTemplate).send(eq("test-topic"), anyString(), eq(eventoJson));
    }

    @Test
    void deveEnviarEventoComParametroNulo() throws JsonProcessingException {
        // Arrange
        ConsultaAuditoriaEvent evento = new ConsultaAuditoriaEvent();
        evento.setId("test-id-456");
        evento.setEndpoint("/api/creditos");
        evento.setParametro(null);

        String eventoJson = "{\"id\":\"test-id-456\",\"endpoint\":\"/api/creditos\",\"parametro\":null}";
        
        CompletableFuture<SendResult<String, String>> future = new CompletableFuture<>();
        SendResult<String, String> sendResult = mock(SendResult.class);
        future.complete(sendResult);

        when(objectMapper.writeValueAsString(evento)).thenReturn(eventoJson);
        when(kafkaTemplate.send(eq("test-topic"), anyString(), eq(eventoJson))).thenReturn(future);

        // Act
        auditoriaPublisher.publicarEventoAuditoria(evento);

        // Assert
        verify(objectMapper).writeValueAsString(evento);
        verify(kafkaTemplate).send(eq("test-topic"), anyString(), eq(eventoJson));
    }

    @Test
    void deveLidarComErroSerializacaoGraciosamente() throws JsonProcessingException {
        // Arrange
        ConsultaAuditoriaEvent evento = new ConsultaAuditoriaEvent();
        evento.setId("test-id-error");

        when(objectMapper.writeValueAsString(any(ConsultaAuditoriaEvent.class)))
                .thenThrow(new JsonProcessingException("Erro de serialização") {});

        // Act & Assert - não deve lançar exceção
        auditoriaPublisher.publicarEventoAuditoria(evento);

        // Verificar que tentou serializar mas não enviou para Kafka
        verify(objectMapper).writeValueAsString(evento);
        verify(kafkaTemplate, never()).send(anyString(), anyString(), anyString());
    }
} 