package com.exemplo.credito.controller;

import com.exemplo.credito.entity.Credito;
import com.exemplo.credito.service.CreditoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/creditos")
@RequiredArgsConstructor
public class CreditoController {

    private final CreditoService service;

    @GetMapping("/{numeroNfse}")
    public ResponseEntity<List<Credito>> getByNumeroNfse(@PathVariable String numeroNfse) {
        return ResponseEntity.ok(service.buscarPorNfse(numeroNfse));
    }

    @GetMapping("/credito/{numeroCredito}")
    public ResponseEntity<Credito> getByNumeroCredito(@PathVariable String numeroCredito) {
        return ResponseEntity.ok(service.buscarPorNumeroCredito(numeroCredito));
    }
}
