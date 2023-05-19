package com.greymatter.springreactor.controller;

import static org.springframework.hateoas.server.reactive.WebFluxLinkBuilder.linkTo;
import static org.springframework.hateoas.server.reactive.WebFluxLinkBuilder.methodOn;
import static reactor.function.TupleUtils.function;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.util.Map;
import com.cloudinary.utils.ObjectUtils;
import com.cloudinary.Cloudinary;
import com.greymatter.springreactor.model.Cliente;
import com.greymatter.springreactor.pagination.PageSupport;
import com.greymatter.springreactor.service.ClienteService;
import org.cloudinary.json.JSONObject;
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
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


@RestController
@RequestMapping("/clientes")
public class ClienteController {

	@Autowired
	private ClienteService service;
	
	@GetMapping
	public Mono<ResponseEntity<Flux<Cliente>>> listar(){
		Flux<Cliente> fxClientes = service.listar();
		
		return Mono.just(ResponseEntity
				.ok()
				.contentType(MediaType.APPLICATION_JSON)
				.body(fxClientes));				
	}
	
	@GetMapping("/{id}")
	public Mono<ResponseEntity<Cliente>> listarPorId(@PathVariable("id") String id){
		return service.listarPorId(id) //Mono<Cliente>
				.map(p -> ResponseEntity
						.ok()
						.contentType(MediaType.APPLICATION_JSON)
						.body(p)
						);
	}
	
	@PostMapping
	public Mono<ResponseEntity<Cliente>> registrar(@Valid @RequestBody Cliente c, final ServerHttpRequest req){
		return service.registrar(c) //Mono<Cliente>
				.map(pl -> ResponseEntity.created(URI.create(req.getURI().toString().concat("/").concat(pl.getId())))
						.contentType(MediaType.APPLICATION_JSON)
						.body(pl)
					);
	}
	
	@PutMapping("/{id}")
	public Mono<ResponseEntity<Cliente>> modificar(@Valid @PathVariable("id") String id, @RequestBody Cliente c){
		
		Mono<Cliente> monoBody = Mono.just(c);
		Mono<Cliente> monoBD = service.listarPorId(id);
		
		return monoBD
				.zipWith(monoBody, (bd, cl) -> {
					bd.setId(id);
					bd.setNombres(cl.getNombres());
					bd.setApellidos(cl.getApellidos());
					bd.setFechaNac(cl.getFechaNac());
					bd.setUrlFoto(cl.getUrlFoto());	
					return bd;
				})
				.flatMap(service::modificar) //Mono<Cliente> //x -> service.modificar(x)
				.map(y -> ResponseEntity.ok()
						.contentType(MediaType.APPLICATION_JSON)
						.body(y))
				.defaultIfEmpty(new ResponseEntity<Cliente>(HttpStatus.NOT_FOUND));
		
	}
	
	
	@DeleteMapping("/{id}")
	public Mono<ResponseEntity<Void>> eliminar(@PathVariable("id") String id){
		return service.listarPorId(id) //Mono<Cliente>
				.flatMap(p -> {
					return service.eliminar(p.getId()) //Mono<Void>
							//.then(Mono.just(new ResponseEntity<Void>(HttpStatus.NO_CONTENT)));
							.thenReturn(new ResponseEntity<Void>(HttpStatus.NO_CONTENT));
				})
				.defaultIfEmpty(new ResponseEntity<Void>(HttpStatus.NOT_FOUND));
	}
	
	private Cliente platoHateoas;
	
	@GetMapping("/hateoas/{id}")
	public Mono<EntityModel<Cliente>> listarHateoasPorId(@PathVariable("id") String id){		
		//"localhost:8080/platos/61fc9529ac67627898c6c5fd"
		Mono<Link> link1 = linkTo(methodOn(ClienteController.class).listarPorId(id)).withSelfRel().toMono();
		Mono<Link> link2 = linkTo(methodOn(ClienteController.class).listarPorId(id)).withSelfRel().toMono();
		
		//PRACTICA NO RECOMENDADA
		/*return service.listarPorId(id) //Mono<Cliente>
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
	public Mono<ResponseEntity<PageSupport<Cliente>>> listarPageable(
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

	@PostMapping("/v1/subir/{id}")
	public Mono<ResponseEntity<Cliente>> subirV1(@PathVariable("id") String id, @RequestPart FilePart file) throws IOException {

		Cloudinary cloudinary = new Cloudinary(ObjectUtils.asMap(
				"cloud_name", "dbjk4qqkd",
				"api_key", "662432927813842",
				"api_secret", "Aj9AJ-bYuQtdpnbMryjY9s5FiWk"));

																																																																File f = Files.createTempFile("temp", file.filename()).toFile();

		return file.transferTo(f)
				.then(service.listarPorId(id)
						.flatMap(c -> {
							Map response;
							try {
								response = cloudinary.uploader().upload(f , ObjectUtils.asMap("resource_type", "auto"));

								JSONObject json=new JSONObject(response);
								String url=json.getString("url");

								c.setUrlFoto(url);

							} catch (IOException e) {
								e.printStackTrace();
							}
							return service.modificar(c).thenReturn(ResponseEntity.ok().body(c));
						})
						.defaultIfEmpty(ResponseEntity.notFound().build())
				);
	}

	@PostMapping("/v2/subir/{id}")
	public Mono<ResponseEntity<Cliente>> subirV2(@PathVariable String id, @RequestPart FilePart file) {

		Cloudinary cloudinary = new Cloudinary(ObjectUtils.asMap(
				"cloud_name", "dbjk4qqkd",
				"api_key", "662432927813842",
				"api_secret", "Aj9AJ-bYuQtdpnbMryjY9s5FiWk"));

		return service.listarPorId(id)
				.flatMap(c -> {
					try {
						File f = Files.createTempFile("temp", file.filename()).toFile();
						file.transferTo(f).block();

						Map response= cloudinary.uploader().upload(f, ObjectUtils.asMap("resource_type", "auto"));
						JSONObject json = new JSONObject(response);
						String url = json.getString("url");

						c.setUrlFoto(url);
						return service.modificar(c).thenReturn(ResponseEntity.ok().body(c));
					}catch(Exception e) {
						System.out.println(e.getMessage());
					}
					return Mono.just(ResponseEntity.ok().body(c));
				})
				.defaultIfEmpty(ResponseEntity.notFound().build());

	}
}
