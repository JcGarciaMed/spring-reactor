package com.greymatter.springreactor.controller;

import com.greymatter.springreactor.model.Plato;
import com.greymatter.springreactor.service.PlatoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.awt.*;
import java.net.URI;

@RestController
@RequestMapping("/platos")
public class PlatoController {

    private PlatoService service;

    public PlatoController(PlatoService service) {
        this.service = service;
    }

    @GetMapping
    public Mono<ResponseEntity<Flux<Plato>>> listar(){
        return Mono.just(ResponseEntity
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(service.listar()));
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<Plato>> listarPorId(@PathVariable("id") String id){
        return service.listarPorId(id)
                .map(plato -> ResponseEntity
                        .ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(plato));
    }

    @PostMapping
    public Mono<ResponseEntity<Plato>> registrar(@RequestBody Plato plato, final ServerHttpRequest request){
        return service.registrar(plato)
                .map(element -> ResponseEntity
                        .created(URI.create(request.getURI().toString().concat("/").concat(element.getId())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(element));
    }

    @PutMapping
    public Mono<ResponseEntity<Plato>> modificar(@RequestBody Plato plato){
        return service.listarPorId(plato.getId())
                .flatMap(element -> service.modificar(plato))
                .map(element -> ResponseEntity
                        .ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(element))
                .defaultIfEmpty(new ResponseEntity<Plato>(HttpStatus.NOT_FOUND));
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> eliminar(@PathVariable("id") String id){
        return service.listarPorId(id)
                .flatMap(plato -> {
                    return service.eliminar(id)
                            //.then(Mono.just(new ResponseEntity<Void>(HttpStatus.NO_CONTENT)));
                            .thenReturn(new ResponseEntity<Void>(HttpStatus.NO_CONTENT));
                })
                .defaultIfEmpty(new ResponseEntity<Void>(HttpStatus.NOT_FOUND));
    }

    
}
