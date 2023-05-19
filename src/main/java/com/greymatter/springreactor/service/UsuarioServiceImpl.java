package com.greymatter.springreactor.service;

import com.greymatter.springreactor.model.Usuario;
import com.greymatter.springreactor.repo.GenericRepo;
import com.greymatter.springreactor.repo.UsuarioRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UsuarioServiceImpl extends CrudServiceImpl<Usuario, String> implements UsuarioService {

	@Autowired
	private UsuarioRepo repo;

	@Override
	protected GenericRepo<Usuario, String> getRepo() {
		return repo;
	}
	
	
}
