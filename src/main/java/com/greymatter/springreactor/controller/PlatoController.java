package com.greymatter.springreactor.controller;

import com.greymatter.springreactor.model.Cliente;
import com.greymatter.springreactor.model.Plato;
import com.greymatter.springreactor.pagination.PageSupport;
import com.greymatter.springreactor.service.PlatoService;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
        //service.listar().repeat(3).publishOn(Schedulers.single()).subscribe(i -> log.info(i.toString()));
        //service.listar().repeat(3).parallel().runOn(Schedulers.parallel()).subscribe(i -> log.info(i.toString()));

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

    @PutMapping("/{id}")
    public Mono<ResponseEntity<Plato>> modificar(@Valid @PathVariable("id") String id, @RequestBody Plato p){

        Mono<Plato> monoBody = Mono.just(p);
        Mono<Plato> monoBD = service.listarPorId(id);

        return monoBD
                .zipWith(monoBody, (bd, pl) -> {
                    bd.setId(id);
                    bd.setNombe(pl.getNombe());
                    bd.setPrecio(pl.getPrecio());
                    bd.setEstado(pl.getEstado());
                    return bd;
                })
                .flatMap(service::modificar) //Mono<Plato> //x -> service.modificar(x)
                .map(y -> ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(y))
                .defaultIfEmpty(new ResponseEntity<Plato>(HttpStatus.NOT_FOUND));

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

    @GetMapping("/pageable")
    public Mono<ResponseEntity<PageSupport<Plato>>> listarPageable(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "5") int size
    ){

        Pageable pageRequest = PageRequest.of(page, size);

        return service.listarPage(pageRequest)
                .map(pag -> ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(pag)
                )
                .defaultIfEmpty(ResponseEntity.noContent().build());
    }



    
}
