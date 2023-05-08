package com.greymatter.springreactor.service;

import com.greymatter.springreactor.model.Plato;
import com.greymatter.springreactor.repo.GenericRepo;
import com.greymatter.springreactor.repo.PlatoRepo;
import org.springframework.stereotype.Service;


@Service
public class PlatoServiceImpl extends CrudServiceImpl<Plato, String> implements PlatoService{

    private PlatoRepo repo;

    public PlatoServiceImpl(PlatoRepo repo) {
        this.repo = repo;
    }

    @Override
    protected GenericRepo<Plato, String> getRepo() {
        return repo;
    }

}
