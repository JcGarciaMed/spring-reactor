package com.greymatter.springreactor.handler;

import static org.springframework.web.reactive.function.BodyInserters.fromValue;

import java.net.URI;

import com.greymatter.springreactor.model.Factura;
import com.greymatter.springreactor.service.FacturaService;
import com.greymatter.springreactor.validators.RequestValidator;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;


@Component
public class FacturaHandler {

	private FacturaService service;
	
	private RequestValidator validadorGeneral;

	public FacturaHandler(FacturaService service, RequestValidator validadorGeneral) {
		this.service = service;
		this.validadorGeneral = validadorGeneral;
	}

	public Mono<ServerResponse> listar(ServerRequest req){
		return ServerResponse
				.ok()
				.contentType(MediaType.APPLICATION_JSON)
				.body(service.listar(), Factura.class);
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
		Mono<Factura> monoFactura = req.bodyToMono(Factura.class);
		
		/*return monoFactura
				.flatMap(p -> {
					Errors errores = new BeanPropertyBindingResult(p, Factura.class.getName());
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
		
		return monoFactura
				.flatMap(validadorGeneral::validate)
				.flatMap(service::registrar) //p -> service.registrar(p)
				.flatMap(p -> ServerResponse.created(URI.create(req.uri().toString().concat(p.getId())))
						.contentType(MediaType.APPLICATION_JSON)
						.body(fromValue(p))
				);
				
	}
	
	public Mono<ServerResponse> modificar(ServerRequest req){
		Mono<Factura> monoFactura = req.bodyToMono(Factura.class);
		Mono<Factura> monoBD = service.listarPorId(req.pathVariable("id"));
		
		return monoBD
				.zipWith(monoFactura, (bd, f) -> {
					bd.setId(req.pathVariable("id"));
					bd.setCliente(f.getCliente());
					bd.setDescripcion(f.getDescripcion());
					bd.setObservacion(f.getObservacion());
					bd.setItems(f.getItems());	
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
