package com.tyse.scrutiny.micro.divipol.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

public class KafkaTestContainer implements InitializingBean, DisposableBean {

    private KafkaContainer kafkaContainer;
    private static final Logger LOG = LoggerFactory.getLogger(KafkaTestContainer.class);

    @Override
    public void destroy() {
        if (null != kafkaContainer && kafkaContainer.isRunning()) {
            LOG.debug("Closing Kafka test container...");
            // Note: Spring will close Kafka connections before this method is called
            // because DisposableBean has a higher precedence than JVM shutdown hooks
            kafkaContainer.close();
            LOG.debug("Kafka test container closed successfully");
        }
    }

    @Override
    public void afterPropertiesSet() {
        if (null == kafkaContainer) {
            // Use apache/kafka:3.9.0 with explicit listeners configuration for KRaft mode
            kafkaContainer = new KafkaContainer(DockerImageName.parse("apache/kafka:3.9.0"))
                .withLogConsumer(new Slf4jLogConsumer(LOG))
                .withEnv("KAFKA_LISTENERS", "PLAINTEXT://:9092,BROKER://:9093,CONTROLLER://:9094")
                .withReuse(true);
        }
        if (!kafkaContainer.isRunning()) {
            kafkaContainer.start();
        }
    }

    public KafkaContainer getKafkaContainer() {
        return kafkaContainer;
    }
}
