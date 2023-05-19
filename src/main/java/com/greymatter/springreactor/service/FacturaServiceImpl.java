package com.greymatter.springreactor.service;

import com.greymatter.springreactor.model.Factura;
import com.greymatter.springreactor.repo.FacturaRepo;
import com.greymatter.springreactor.repo.GenericRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class FacturaServiceImpl extends CrudServiceImpl<Factura, String> implements FacturaService {

	@Autowired
	private FacturaRepo repo;

	@Override
	protected GenericRepo<Factura, String> getRepo() {
		return repo;
	}
	
	
}
