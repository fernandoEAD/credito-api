package com.exemplo.credito.service;

import com.exemplo.credito.event.ConsultaAuditoriaEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditoriaPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${kafka.topic.auditoria:consultas-auditoria}")
    private String topicAuditoria;

    public void publicarEventoAuditoria(ConsultaAuditoriaEvent evento) {
        try {
            String eventoJson = objectMapper.writeValueAsString(evento);
            
            kafkaTemplate.send(topicAuditoria, evento.getId(), eventoJson)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        log.info("Evento de auditoria enviado com sucesso para o tópico {}: {}", 
                                topicAuditoria, evento.getId());
                    } else {
                        log.error("Falha ao enviar evento de auditoria para o tópico {}: {}", 
                                topicAuditoria, ex.getMessage());
                    }
                });
                
        } catch (JsonProcessingException e) {
            log.error("Erro ao serializar evento de auditoria: {}", e.getMessage());
        }
    }
} 