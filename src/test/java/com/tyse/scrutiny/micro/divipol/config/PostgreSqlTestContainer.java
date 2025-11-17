package com.tyse.scrutiny.micro.divipol.config;

import java.time.Duration;
import java.util.Collections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.Wait;

public class PostgreSqlTestContainer implements SqlTestContainer {

    private static final Logger LOG = LoggerFactory.getLogger(PostgreSqlTestContainer.class);

    private PostgreSQLContainer<?> postgreSQLContainer;

    @Override
    public void destroy() {
        if (null != postgreSQLContainer && postgreSQLContainer.isRunning()) {
            postgreSQLContainer.stop();
        }
    }

    @Override
    public void afterPropertiesSet() {
        if (null == postgreSQLContainer) {
            postgreSQLContainer = new PostgreSQLContainer<>("postgres:17.4")
                .withDatabaseName("tyseScrutinyMicroDivipol")
                .withTmpFs(Collections.singletonMap("/testtmpfs", "rw"))
                .withLogConsumer(new Slf4jLogConsumer(LOG))
                .withReuse(true)
                // Espera explícita: Wait.forLogMessage es más confiable que forListeningPort
                // porque garantiza que PostgreSQL ha terminado su inicialización completa
                .waitingFor(
                    Wait.forLogMessage(".*database system is ready to accept connections.*\\n", 2).withStartupTimeout(
                        Duration.ofSeconds(60)
                    )
                );
        }
        if (!postgreSQLContainer.isRunning()) {
            postgreSQLContainer.start();
        }
    }

    @Override
    public JdbcDatabaseContainer<?> getTestContainer() {
        return postgreSQLContainer;
    }
}
