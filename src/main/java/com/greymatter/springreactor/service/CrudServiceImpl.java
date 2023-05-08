package com.greymatter.springreactor.service;

import com.greymatter.springreactor.repo.GenericRepo;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public abstract class  CrudServiceImpl<T, ID> implements CrudService<T, ID> {

    protected abstract GenericRepo<T, ID> getRepo();

    @Override
    public Mono<T> registrar(T t) {
        return getRepo().save(t);
    }

    @Override
    public Mono<T> modificar(T t) {
        return getRepo().save(t);
    }

    @Override
    public Flux<T> listar() {
        return getRepo().findAll();
    }

    @Override
    public Mono<T> listarPorId(ID id) {
        return getRepo().findById(id);
    }

    @Override
    public Mono<Void> eliminar(ID id) {
        return getRepo().deleteById(id);
    }

}
