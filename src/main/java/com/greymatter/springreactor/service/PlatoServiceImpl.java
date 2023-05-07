package com.greymatter.springreactor.service;

import com.greymatter.springreactor.model.Plato;
import com.greymatter.springreactor.repo.PlatoRepo;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class PlatoServiceImpl implements PlatoService {

    private PlatoRepo repo;

    public PlatoServiceImpl(PlatoRepo repo) {
        this.repo = repo;
    }

    @Override
    public Mono<Plato> registrar(Plato plato) {
        return repo.save(plato);
    }

    @Override
    public Mono<Plato> modificar(Plato plato) {
        return repo.save(plato);
    }

    @Override
    public Flux<Plato> listar() {
        return repo.findAll();
    }

    @Override
    public Mono<Plato> listarPorId(String id) {
        return repo.findById(id);
    }

    @Override
    public Mono<Void> eliminar(String id) {
        return repo.deleteById(id);
    }
}
