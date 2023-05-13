package com.greymatter.springreactor.controller;

import com.greymatter.springreactor.model.Plato;
import com.greymatter.springreactor.service.PlatoService;
import jakarta.validation.Valid;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.hateoas.Links;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


import java.net.URI;

import static org.springframework.hateoas.server.reactive.WebFluxLinkBuilder.linkTo;
import static org.springframework.hateoas.server.reactive.WebFluxLinkBuilder.methodOn;
import static reactor.function.TupleUtils.function;

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
    public Mono<ResponseEntity<Plato>> registrar(@Valid @RequestBody Plato plato, final ServerHttpRequest request){
        return service.registrar(plato)
                .map(element -> ResponseEntity
                        .created(URI.create(request.getURI().toString().concat("/").concat(element.getId())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(element));
    }

    @PutMapping
    public Mono<ResponseEntity<Plato>> modificar( @Valid @RequestBody Plato plato){
        return service.listarPorId(plato.getId())
                .flatMap(element -> service.modificar(plato))
                //.flatMap(service::modificar) // esto ta mal
                .map(element -> ResponseEntity
                        .ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(element))
                .defaultIfEmpty(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> eliminar(@PathVariable("id") String id){
        return service.listarPorId(id)
                .flatMap(plato -> service.eliminar(id).thenReturn(new ResponseEntity<Void>(HttpStatus.NO_CONTENT))
                            //.then(Mono.just(new ResponseEntity<Void>(HttpStatus.NO_CONTENT)));
                            //.thenReturn(new ResponseEntity<Void>(HttpStatus.NO_CONTENT));
                )
                .defaultIfEmpty(new ResponseEntity<Void>(HttpStatus.NOT_FOUND));
    }

    @GetMapping("/hateoas/{id}")
    public Mono<EntityModel<Plato>> listarHateoasPorId(@PathVariable("id") String id){
        Mono<Link> link1 = linkTo(methodOn(PlatoController.class).listarPorId(id)).withSelfRel().toMono();
        Mono<Link> link2 = linkTo(methodOn(PlatoController.class).listarPorId(id)).withSelfRel().toMono();

        //PRACTICA NO RECOMENDADA
		/*return service.listarPorId(id) //Mono<Plato>
				.flatMap(p -> {
					platoHateoas = p;
					return link1;
				})
				.map(lk -> EntityModel.of(platoHateoas, lk));
				*/
		/*return service.listarPorId(id)
				.map(p -> EntityModel.of(p))
				.map(e -> e.add(link1.block()))
				.flatMap(pl-> Mono.just(pl));*/

        //PRACTICA INTERMEDIA
		/*return service.listarPorId(id)
					.flatMap(p -> {
						return link1.map(lk -> EntityModel.of(p, lk));
					});*/

        //PRACTICA IDEAL
		/*return service.listarPorId(id)
				.zipWith(link1, (p, lk) -> EntityModel.of(p, lk));*/

        //Más de 1 link
        return link1
                .zipWith(link2)
                .map(function((lk1, lk2) -> Links.of(lk1, lk2)))
                .zipWith(service.listarPorId(id), (lk3, p) -> EntityModel.of(p, lk3));
    }



    
}
