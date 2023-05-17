package com.greymatter.springreactor.service;

import com.greymatter.springreactor.pagination.PageSupport;
import com.greymatter.springreactor.repo.GenericRepo;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.stream.Collectors;

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

    @Override
    public Mono<PageSupport<T>> listarPage(Pageable page){
        return getRepo().findAll() //Flux<T>
                .collectList() //Mono<List<T>>
                .map(list -> new PageSupport<>(
                                list
                                        .stream()
                                        .skip(page.getPageNumber() * page.getPageSize())
                                        .limit(page.getPageSize())
                                        .collect(Collectors.toList()),
                                page.getPageNumber(), page.getPageSize(), list.size()
                        )
                );
    }
}
