package com.greymatter.springreactor.controller;

import com.greymatter.springreactor.model.Plato;
import com.greymatter.springreactor.service.PlatoService;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/platos")
public class PlatoController {

    private PlatoService service;

    public PlatoController(PlatoService service) {
        this.service = service;
    }

    @GetMapping
    public Flux<Plato> listar(){
        return service.listar();
    }

    @GetMapping("/{id}")
    public Mono<Plato> listarPorId(@PathVariable("id") String id){
        return service.listarPorId(id);
    }

    @PostMapping
    public Mono<Plato> registrar(@RequestBody Plato plato){
        return service.registrar(plato);
    }

    @PutMapping
    public Mono<Plato> modificar(@RequestBody Plato plato){
        return service.modificar(plato);
    }

    @DeleteMapping("/{id}")
    public Mono<Void> eliminar(@PathVariable("id") String id){
        return service.eliminar(id);
    }

    
}
