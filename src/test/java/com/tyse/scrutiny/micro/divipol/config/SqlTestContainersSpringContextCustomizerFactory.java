package com.tyse.scrutiny.micro.divipol.config;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.test.context.ContextConfigurationAttributes;
import org.springframework.test.context.ContextCustomizer;
import org.springframework.test.context.ContextCustomizerFactory;
import org.springframework.test.context.MergedContextConfiguration;

public class SqlTestContainersSpringContextCustomizerFactory implements ContextCustomizerFactory {

    private Logger log = LoggerFactory.getLogger(SqlTestContainersSpringContextCustomizerFactory.class);

    private static SqlTestContainer prodTestContainer;

    @Override
    public ContextCustomizer createContextCustomizer(Class<?> testClass, List<ContextConfigurationAttributes> configAttributes) {
        return new ContextCustomizer() {
            @Override
            public void customizeContext(ConfigurableApplicationContext context, MergedContextConfiguration mergedConfig) {
                ConfigurableListableBeanFactory beanFactory = context.getBeanFactory();
                TestPropertyValues testValues = TestPropertyValues.empty();
                EmbeddedSQL sqlAnnotation = AnnotatedElementUtils.findMergedAnnotation(testClass, EmbeddedSQL.class);
                if (null != sqlAnnotation) {
                    log.debug("detected the EmbeddedSQL annotation on class {}", testClass.getName());
                    log.info("Warming up the sql database");
                    if (null == prodTestContainer) {
                        try {
                            Class<? extends SqlTestContainer> containerClass = (Class<? extends SqlTestContainer>) Class.forName(
                                this.getClass().getPackageName() + ".PostgreSqlTestContainer"
                            );
                            prodTestContainer = beanFactory.createBean(containerClass);
                            beanFactory.registerSingleton(containerClass.getName(), prodTestContainer);
                            /**
                             * ((DefaultListableBeanFactory)beanFactory).registerDisposableBean(containerClass.getName(), prodTestContainer);
                             */
                        } catch (ClassNotFoundException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    // Construir URL usando getHost() para compatibilidad con GitHub Actions
                    // getHost() devuelve el host correcto según el entorno (localhost, host.docker.internal, etc.)
                    String host = prodTestContainer.getTestContainer().getHost();
                    Integer port = prodTestContainer.getTestContainer().getMappedPort(5432);
                    String database = prodTestContainer.getTestContainer().getDatabaseName();

                    String r2dbcUrl = String.format("r2dbc:postgresql://%s:%d/%s", host, port, database);
                    String jdbcUrl = String.format("jdbc:postgresql://%s:%d/%s", host, port, database);

                    log.info("Testcontainers PostgreSQL - Host: {}, Port: {}, R2DBC URL: {}", host, port, r2dbcUrl);

                    testValues = testValues.and("spring.r2dbc.url=" + r2dbcUrl);
                    testValues = testValues.and("spring.r2dbc.username=" + prodTestContainer.getTestContainer().getUsername());
                    testValues = testValues.and("spring.r2dbc.password=" + prodTestContainer.getTestContainer().getPassword());
                    testValues = testValues.and("spring.liquibase.url=" + jdbcUrl);
                }
                testValues.applyTo(context);
            }

            @Override
            public int hashCode() {
                return SqlTestContainer.class.getName().hashCode();
            }

            @Override
            public boolean equals(Object obj) {
                return this.hashCode() == obj.hashCode();
            }
        };
    }
}
