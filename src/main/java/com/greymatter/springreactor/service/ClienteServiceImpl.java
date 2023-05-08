package com.greymatter.springreactor.service;

import com.greymatter.springreactor.model.Cliente;
import com.greymatter.springreactor.repo.ClienteRepo;
import com.greymatter.springreactor.repo.GenericRepo;
import org.springframework.stereotype.Service;

@Service
public class ClienteServiceImpl extends CrudServiceImpl<Cliente, String> implements ClienteService{


    private ClienteRepo repo;

    public ClienteServiceImpl(ClienteRepo repo) {
        this.repo = repo;
    }

    @Override
    protected GenericRepo<Cliente, String> getRepo() {
        return repo;
    }


}