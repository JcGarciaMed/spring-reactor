package com.greymatter.springreactor.service;

import com.greymatter.springreactor.model.Plato;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface PlatoService {
    Mono<Plato> registrar(Plato plato);
    Mono<Plato> modificar(Plato plato);
    Flux<Plato> listar();
    Mono<Plato> listarPorId(String id);
    Mono<Void> eliminar(String id);
}

