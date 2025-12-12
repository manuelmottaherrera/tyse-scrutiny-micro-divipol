package com.tyse.scrutiny.micro.divipol.cucumber.stepdefs;

import org.springframework.test.web.reactive.server.WebTestClient;

public abstract class StepDefs {

    // Static para compartir entre todas las instancias de step definitions
    protected static WebTestClient.ResponseSpec actions;

    // Estado de autenticación compartido entre step definitions
    protected static boolean authenticated = true;

    // Bytes de respuesta compartidos (para validaciones de exportación)
    protected static byte[] responseBytes;

    // Content-Type de respuesta compartido
    protected static String responseContentType;
}
