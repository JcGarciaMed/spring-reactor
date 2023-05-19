package com.greymatter.springreactor.service;

import com.greymatter.springreactor.model.Factura;
import reactor.core.publisher.Mono;

public interface FacturaService extends CrudService<Factura, String>{
    Mono<byte[]> generarReporte(String idFactura);
}
