package com.greymatter.springreactor.service;

import com.greymatter.springreactor.model.Rol;
import com.greymatter.springreactor.repo.GenericRepo;
import com.greymatter.springreactor.repo.RolRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RolServiceImpl extends CrudServiceImpl<Rol, String> implements RolService {

	@Autowired
	private RolRepo repo;

	@Override
	protected GenericRepo<Rol, String> getRepo() {
		return repo;
	}
	
	
}
