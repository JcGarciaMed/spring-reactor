package com.greymatter.springreactor.validators;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import reactor.core.publisher.Mono;

import java.util.Set;

@Component
@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
public class RequestValidator {

	private Validator validador;

	public RequestValidator(Validator validador) {
		this.validador = validador;
	}

	public <T> Mono<T> validate(T obj){
		
		if(obj == null) {
			return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST));
		}
		
		Set<ConstraintViolation<T>> violations = this.validador.validate(obj);
		
		if(violations == null || violations.isEmpty()) {
			return Mono.just(obj);
		}
		
		return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST));
	}
}
