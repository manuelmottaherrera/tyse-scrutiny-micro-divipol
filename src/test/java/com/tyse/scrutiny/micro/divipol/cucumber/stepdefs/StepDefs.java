package com.tyse.scrutiny.micro.divipol.cucumber.stepdefs;

import org.springframework.test.web.reactive.server.WebTestClient;

public abstract class StepDefs {

    // Static para compartir entre todas las instancias de step definitions
    protected static WebTestClient.ResponseSpec actions;
}
