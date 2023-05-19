package com.greymatter.springreactor.handler;

import static org.springframework.web.reactive.function.BodyInserters.fromValue;

import java.net.URI;

import com.greymatter.springreactor.model.Plato;
import com.greymatter.springreactor.service.PlatoService;
import com.greymatter.springreactor.validators.RequestValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;


@Component
public class PlatoHandler {

	private PlatoService service;

	private RequestValidator validadorGeneral;

	public PlatoHandler(PlatoService service, RequestValidator validadorGeneral) {
		this.service = service;
		this.validadorGeneral = validadorGeneral;
	}

	public Mono<ServerResponse> listar(ServerRequest req){
		return ServerResponse
				.ok()
				.contentType(MediaType.APPLICATION_JSON)
				.body(service.listar(), Plato.class);
	}
	
	public Mono<ServerResponse> listarPorId(ServerRequest req){
		String id = req.pathVariable("id");
		
		return service.listarPorId(id)
				.flatMap(p -> ServerResponse
							.ok()
							.contentType(MediaType.APPLICATION_JSON)
							.body(fromValue(p))
				)
				.switchIfEmpty(ServerResponse.notFound().build());

	}
	
	public Mono<ServerResponse> registrar(ServerRequest req){
		Mono<Plato> monoPlato = req.bodyToMono(Plato.class);
		
		/*return monoPlato
				.flatMap(p -> {
					Errors errores = new BeanPropertyBindingResult(p, Plato.class.getName());
					validador.validate(p, errores);
					
					if(errores.hasErrors()) {
						return Flux.fromIterable(errores.getFieldErrors())
								.map(error -> new ValidacionDTO(error.getField(), error.getDefaultMessage()))						
								.collectList() //Mono<List<ValidacionDTO>
								.flatMap(listaErrores -> {							
									return ServerResponse.badRequest()
											.contentType(MediaType.APPLICATION_JSON)
											.body(fromValue(listaErrores));	
											}
										); 
			
					}else {
						return service.registrar(p)
								.flatMap(pdb -> ServerResponse
								.created(URI.create(req.uri().toString().concat(p.getId())))
								.contentType(MediaType.APPLICATION_JSON)
								.body(fromValue(pdb))
								);
					}
					
				});
		*/
		
		return monoPlato
				.flatMap(validadorGeneral::validate)
				.flatMap(service::registrar) //p -> service.registrar(p)
				.flatMap(p -> ServerResponse.created(URI.create(req.uri().toString().concat(p.getId())))
						.contentType(MediaType.APPLICATION_JSON)
						.body(fromValue(p))
				);
				
	}
	
	public Mono<ServerResponse> modificar(ServerRequest req){
		Mono<Plato> monoPlato = req.bodyToMono(Plato.class);
		Mono<Plato> monoBD = service.listarPorId(req.pathVariable("id"));
		
		return monoBD
				.zipWith(monoPlato, (bd, p) -> {
					bd.setId(p.getId());
					bd.setNombe(p.getNombe());
					bd.setEstado(p.getEstado());
					return bd;
				})
				.flatMap(validadorGeneral::validate)
				.flatMap(service::modificar)
				.flatMap(p -> ServerResponse.ok()
							.contentType(MediaType.APPLICATION_JSON)
							.body(fromValue(p))
				)
				.switchIfEmpty(ServerResponse.notFound().build());
	}
	
	public Mono<ServerResponse> eliminar(ServerRequest req){
		String id = req.pathVariable("id");
		
		return service.listarPorId(id)
				.flatMap(p -> service.eliminar(p.getId())
							.then(ServerResponse.noContent().build()))
				.switchIfEmpty(ServerResponse.notFound().build());
	}

}
