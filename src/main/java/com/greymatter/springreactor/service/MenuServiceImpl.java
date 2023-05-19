package com.greymatter.springreactor.service;

import com.greymatter.springreactor.model.Menu;
import com.greymatter.springreactor.repo.GenericRepo;
import com.greymatter.springreactor.repo.MenuRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MenuServiceImpl extends CrudServiceImpl<Menu, String> implements MenuService {

	@Autowired
	private MenuRepo repo;

	@Override
	protected GenericRepo<Menu, String> getRepo() {
		return repo;
	}
	
	
}
