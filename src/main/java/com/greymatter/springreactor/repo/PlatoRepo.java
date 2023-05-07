package com.greymatter.springreactor.repo;

import com.greymatter.springreactor.model.Plato;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface PlatoRepo extends ReactiveMongoRepository<Plato, String> {

}
