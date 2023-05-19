package com.greymatter.springreactor.service;

import com.greymatter.springreactor.model.Factura;
import com.greymatter.springreactor.repo.ClienteRepo;
import com.greymatter.springreactor.repo.FacturaRepo;
import com.greymatter.springreactor.repo.GenericRepo;
import com.greymatter.springreactor.repo.PlatoRepo;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;


@Service
public class FacturaServiceImpl extends CrudServiceImpl<Factura, String> implements FacturaService {


	private FacturaRepo repo;
	private ClienteRepo clienteRepo;
	private PlatoRepo platoRepo;


	public FacturaServiceImpl(FacturaRepo repo, ClienteRepo clienteRepo, PlatoRepo platoRepo) {
		this.repo = repo;
		this.clienteRepo = clienteRepo;
		this.platoRepo = platoRepo;
	}

	@Override
	protected GenericRepo<Factura, String> getRepo() {
		return repo;
	}

	@Override
	public Mono<byte[]> generarReporte(String idFactura) {
		return repo.findById(idFactura) //Mono<Factura>
				//Obteniendo Cliente
				.flatMap(f  -> {
					return Mono.just(f)
							.zipWith(clienteRepo.findById(f.getCliente().getId()), (fa, cl) -> {
								fa.setCliente(cl);
								return fa;
							});
				})
				//Obteniendo cada Plato
				.flatMap(f -> {
					return Flux.fromIterable(f.getItems()).flatMap(it -> {
						return platoRepo.findById(it.getPlato().getId())
								.map(p -> {
									it.setPlato(p);
									return it;
								});
					}).collectList().flatMap(list -> {
						//Seteando la nueva lista a factura
						f.setItems(list);
						return Mono.just(f);
					});
				})
				.map(f -> {
					InputStream stream;
					try {
						Map<String, Object> parametros = new HashMap<>();
						parametros.put("txt_cliente", f.getCliente().getNombres() + " " + f.getCliente().getApellidos());

						stream = getClass().getResourceAsStream("/facturas.jrxml");
						JasperReport report = JasperCompileManager.compileReport(stream);
						JasperPrint print = JasperFillManager.fillReport(report, parametros, new JRBeanCollectionDataSource(f.getItems()));
						return JasperExportManager.exportReportToPdf(print);
					} catch (Exception e) {
						e.printStackTrace();
					}
					return new byte[0];
				});

	}
}
