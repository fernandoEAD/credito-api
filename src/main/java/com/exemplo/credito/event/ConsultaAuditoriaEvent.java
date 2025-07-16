package com.exemplo.credito.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConsultaAuditoriaEvent {
    
    private String id;
    private String endpoint;
    private String parametro;
    private String metodo;
    private Integer statusResposta;
    private Integer quantidadeResultados;
    private LocalDateTime timestamp;
    private String userAgent;
    private String ipOrigemString;
    private Long tempoProcessamento;
} 