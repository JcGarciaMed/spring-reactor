package com.greymatter.springreactor.controller;

import com.greymatter.springreactor.model.Plato;
import com.greymatter.springreactor.repo.PlatoRepo;
import com.greymatter.springreactor.service.PlatoServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Resources;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;

@ExtendWith(SpringExtension.class)
@WebFluxTest(controllers = PlatoController.class)
@Import(PlatoServiceImpl.class)
class PlatoControllerTest {
    @MockBean
    private PlatoRepo platoRepo;

    @MockBean
    private WebProperties.Resources resources;

    @Autowired
    private WebTestClient client;

    @Test
    void listarTest(){
        List<Plato> platos = new ArrayList<>();
        platos.add(new Plato());
        platos.add(new Plato());
        platos.add(new Plato());

        Mockito.when(platoRepo.findAll()).thenReturn(Flux.fromIterable(platos));


        client.get()
                .uri("/platos")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(Plato.class)
                .hasSize(3);
    }

    @Test
    void registrarTest(){
        Plato plato = new Plato();
        plato.setId("1");
        plato.setNombe("Jorge Carlos");
        plato.setPrecio(125.52);
        plato.setEstado(true);

        Mockito.when(platoRepo.save(any())).thenReturn(Mono.just(plato));


        client.post()
                .uri("/platos")
                .body(Mono.just(plato), Plato.class)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.nombe").isNotEmpty()
                .jsonPath("$.precio").isNumber();
    }

    @Test
    void modificarTest() {
        Plato plato = new Plato();
        plato.setId("1");
        plato.setNombe("pachamancax");
        plato.setPrecio(30.0);
        plato.setEstado(true);

        Mockito.when(platoRepo.findById("1")).thenReturn(Mono.just(plato));
        Mockito.when(platoRepo.save(any())).thenReturn(Mono.just(plato));

        client.put()
                .uri("/platos/" + plato.getId())
                .body(Mono.just(plato), Plato.class)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.nombe").isNotEmpty()
                .jsonPath("$.precio").isNumber();
    }

    @Test
    void eliminarTest() {
        Plato plato = new Plato();
        plato.setId("1");

        Mockito.when(platoRepo.findById("1")).thenReturn(Mono.just(plato));
        Mockito.when(platoRepo.deleteById("1")).thenReturn(Mono.empty());

        client.delete()
                .uri("/platos/" + plato.getId())
                .exchange()
                .expectStatus().isNoContent();
    }

}