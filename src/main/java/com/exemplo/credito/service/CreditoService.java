package com.exemplo.credito.service;

import com.exemplo.credito.entity.Credito;
import com.exemplo.credito.repository.CreditoRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CreditoService {

    private final CreditoRepository repository;

    public List<Credito> buscarPorNfse(String numeroNfse) {
        return repository.findByNumeroNfse(numeroNfse);
    }

    public Credito buscarPorNumeroCredito(String numeroCredito) {
        return repository.findByNumeroCredito(numeroCredito)
                .orElseThrow(() -> new EntityNotFoundException("Crédito não encontrado"));
    }
}
