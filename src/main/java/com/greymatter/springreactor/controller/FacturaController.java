package com.greymatter.springreactor.controller;

import static org.springframework.hateoas.server.reactive.WebFluxLinkBuilder.linkTo;
import static org.springframework.hateoas.server.reactive.WebFluxLinkBuilder.methodOn;
import static reactor.function.TupleUtils.function;

import java.net.URI;
import com.greymatter.springreactor.model.Factura;
import com.greymatter.springreactor.pagination.PageSupport;
import com.greymatter.springreactor.service.FacturaService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Links;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/facturas")
public class FacturaController {

	@Autowired
	private FacturaService service;
	
	@GetMapping
	public Mono<ResponseEntity<Flux<Factura>>> listar(){
		Flux<Factura> fxFacturas = service.listar();
		
		return Mono.just(ResponseEntity
				.ok()
				.contentType(MediaType.APPLICATION_JSON)
				.body(fxFacturas));				
	}
	
	@GetMapping("/{id}")
	public Mono<ResponseEntity<Factura>> listarPorId(@PathVariable("id") String id){
		return service.listarPorId(id) //Mono<Factura>
				.map(p -> ResponseEntity
						.ok()
						.contentType(MediaType.APPLICATION_JSON)
						.body(p)
						);
	}
	
	@PostMapping
	public Mono<ResponseEntity<Factura>> registrar(@Valid @RequestBody Factura f, final ServerHttpRequest req){
		return service.registrar(f) //Mono<Factura>
				.map(pl -> ResponseEntity.created(URI.create(req.getURI().toString().concat("/").concat(pl.getId())))
						.contentType(MediaType.APPLICATION_JSON)
						.body(pl)
					);
	}
	
	@PutMapping("/{id}")
	public Mono<ResponseEntity<Factura>> modificar(@Valid @PathVariable("id") String id, @RequestBody Factura fac){
		
		Mono<Factura> monoBody = Mono.just(fac);
		Mono<Factura> monoBD = service.listarPorId(id);
		
		return monoBD
				.zipWith(monoBody, (bd, f) -> {
					bd.setId(id);
					bd.setCliente(f.getCliente());
					bd.setDescripcion(f.getDescripcion());
					bd.setObservacion(f.getObservacion());
					bd.setItems(f.getItems());	
					return bd;
				})
				.flatMap(service::modificar) //Mono<Factura> //x -> service.modificar(x)
				.map(y -> ResponseEntity.ok()
						.contentType(MediaType.APPLICATION_JSON)
						.body(y))
				.defaultIfEmpty(new ResponseEntity<Factura>(HttpStatus.NOT_FOUND));
		
	}
	
	
	@DeleteMapping("/{id}")
	public Mono<ResponseEntity<Void>> eliminar(@PathVariable("id") String id){
		return service.listarPorId(id) //Mono<Factura>
				.flatMap(p -> {
					return service.eliminar(p.getId()) //Mono<Void>
							//.then(Mono.just(new ResponseEntity<Void>(HttpStatus.NO_CONTENT)));
							.thenReturn(new ResponseEntity<Void>(HttpStatus.NO_CONTENT));
				})
				.defaultIfEmpty(new ResponseEntity<Void>(HttpStatus.NOT_FOUND));
	}
	
	private Factura platoHateoas;
	
	@GetMapping("/hateoas/{id}")
	public Mono<EntityModel<Factura>> listarHateoasPorId(@PathVariable("id") String id){		
		//"localhost:8080/platos/61fc9529ac67627898c6c5fd"
		Mono<Link> link1 = linkTo(methodOn(FacturaController.class).listarPorId(id)).withSelfRel().toMono();
		Mono<Link> link2 = linkTo(methodOn(FacturaController.class).listarPorId(id)).withSelfRel().toMono();
		
		//PRACTICA NO RECOMENDADA
		/*return service.listarPorId(id) //Mono<Factura>
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
	public Mono<ResponseEntity<PageSupport<Factura>>> listarPageable(
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

	@GetMapping("/generarReporte/{id}")
	public Mono<ResponseEntity<byte[]>> generarReporte(@PathVariable("id") String id){

		Mono<byte[]> monoReporte = service.generarReporte(id);

		return monoReporte
				.map(bytes -> ResponseEntity.ok()
						.contentType(MediaType.APPLICATION_OCTET_STREAM) //APPLICATION_PDF
						.body(bytes)
				).defaultIfEmpty(new ResponseEntity<byte[]>(HttpStatus.NO_CONTENT));
	}
}
