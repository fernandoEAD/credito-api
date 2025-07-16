package com.exemplo.credito.interceptor;

import com.exemplo.credito.event.ConsultaAuditoriaEvent;
import com.exemplo.credito.service.AuditoriaPublisher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuditoriaInterceptor implements HandlerInterceptor {

    private final AuditoriaPublisher auditoriaPublisher;
    private static final String START_TIME_ATTRIBUTE = "startTime";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // Marca o tempo de início da requisição
        request.setAttribute(START_TIME_ATTRIBUTE, System.currentTimeMillis());
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, 
                               Object handler, Exception ex) {
        
        // Só audita se for um endpoint da API de créditos
        if (!request.getRequestURI().startsWith("/api/creditos")) {
            return;
        }

        try {
            Long startTime = (Long) request.getAttribute(START_TIME_ATTRIBUTE);
            Long tempoProcessamento = startTime != null ? 
                System.currentTimeMillis() - startTime : null;

            String parametro = extrairParametro(request);
            Integer quantidadeResultados = extrairQuantidadeResultados(response);

            ConsultaAuditoriaEvent evento = ConsultaAuditoriaEvent.builder()
                .id(UUID.randomUUID().toString())
                .endpoint(request.getRequestURI())
                .parametro(parametro)
                .metodo(request.getMethod())
                .statusResposta(response.getStatus())
                .quantidadeResultados(quantidadeResultados)
                .timestamp(LocalDateTime.now())
                .userAgent(request.getHeader("User-Agent"))
                .ipOrigemString(obterIpReal(request))
                .tempoProcessamento(tempoProcessamento)
                .build();

            // Publica o evento de forma assíncrona
            auditoriaPublisher.publicarEventoAuditoria(evento);

        } catch (Exception e) {
            log.error("Erro ao processar auditoria: {}", e.getMessage());
        }
    }

    private String extrairParametro(HttpServletRequest request) {
        String uri = request.getRequestURI();
        
        // Extrai o parâmetro da URL (último segmento do path)
        String[] segments = uri.split("/");
        if (segments.length > 0) {
            return segments[segments.length - 1];
        }
        
        return null;
    }

    private Integer extrairQuantidadeResultados(HttpServletResponse response) {
        // Em um cenário real, poderíamos interceptar o response body
        // Por simplicidade, estamos retornando null aqui
        // Poderia ser implementado com um ResponseBodyAdvice
        return null;
    }

    private String obterIpReal(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
} 