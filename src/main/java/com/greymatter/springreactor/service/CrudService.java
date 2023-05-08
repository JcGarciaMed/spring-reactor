package com.greymatter.springreactor.service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CrudService<T, ID> {
    Mono<T> registrar(T object);
    Mono<T> modificar(T object);
    Flux<T> listar();
    Mono<T> listarPorId(ID id);
    Mono<Void> eliminar(ID id);
}
