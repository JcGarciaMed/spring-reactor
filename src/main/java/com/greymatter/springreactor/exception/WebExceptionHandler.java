package com.greymatter.springreactor.exception;

import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.autoconfigure.web.reactive.error.AbstractErrorWebExceptionHandler;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserter;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.*;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@Component
@Order(-1)
@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
public class WebExceptionHandler extends AbstractErrorWebExceptionHandler {

    public WebExceptionHandler(ErrorAttributes errorAttributes, WebProperties.Resources resourceProperties,
                               ApplicationContext applicationContext, ServerCodecConfigurer configurer) {
        super(errorAttributes, resourceProperties, applicationContext);

        this.setMessageWriters(configurer.getWriters());
    }

    @Override
    protected RouterFunction<ServerResponse> getRoutingFunction(ErrorAttributes errorAttributes) {
        return RouterFunctions.route(RequestPredicates.all(), this::renderErrorResponse);
    }

    private Mono<ServerResponse> renderErrorResponse(ServerRequest request) {
        Map<String, Object> errorGeneral = getErrorAttributes(request, ErrorAttributeOptions.defaults());
        Map<String, Object> mapException = new HashMap<>();


        var httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        String statusCode = String.valueOf(errorGeneral.get("status"));

        switch(statusCode) {
            case "500":
                mapException.put("status", "500");
                mapException.put("exception", "ERROR INTERNO DEL SERVIDOR");
                httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
                break;
            case "400":
                mapException.put("status", "400");
                mapException.put("exception", "PETICION INCORRECTA, VERIFICA LOS DATOS");
                httpStatus = HttpStatus.BAD_REQUEST;
                break;
            case "406":
                mapException.put("status", "406");
                mapException.put("exception", "OTRO TIPO DE ERROR");
                httpStatus = HttpStatus.NOT_ACCEPTABLE;
                break;
            default:
                mapException.put("status", "418");
                mapException.put("exception", "ERROR POR DEFAULT");
                httpStatus = HttpStatus.I_AM_A_TEAPOT;
                break;
        }

        return ServerResponse
                .status(httpStatus)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(errorGeneral));

    }

}