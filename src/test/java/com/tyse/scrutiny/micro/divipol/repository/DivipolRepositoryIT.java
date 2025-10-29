package com.tyse.scrutiny.micro.divipol.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.tyse.scrutiny.micro.divipol.TyseScrutinyMicroDivipolApp;
import com.tyse.scrutiny.micro.divipol.config.AsyncSyncConfiguration;
import com.tyse.scrutiny.micro.divipol.config.EmbeddedSQL;
import com.tyse.scrutiny.micro.divipol.domain.Divipol;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Integration tests for {@link DivipolRepository}.
 * Prueba las operaciones de consulta contra una base de datos PostgreSQL real usando Testcontainers.
 * Nota: Las pruebas usan los datos pre-cargados por Liquibase (18,016 registros de divipol).
 * Solo usa PostgreSQL Testcontainer (sin Kafka para simplificar).
 */
@SpringBootTest(classes = { TyseScrutinyMicroDivipolApp.class, AsyncSyncConfiguration.class })
@EmbeddedSQL
class DivipolRepositoryIT {

    @Autowired
    private DivipolRepository divipolRepository;

    @Test
    void countDivipols() {
        // Count should be greater than 18,000 (data loaded by Liquibase)
        Long count = divipolRepository.count().block();
        assertThat(count).isGreaterThan(18000);
    }

    @Test
    void findAllDivipols() {
        // Get all divipol records (will be paginated in real scenario)
        List<Divipol> divipolList = divipolRepository.findAll().take(100).collectList().block();

        assertThat(divipolList).isNotEmpty();
        assertThat(divipolList).hasSizeGreaterThan(50);
        assertThat(divipolList).allMatch(
            divipol -> divipol.getClase() != null && divipol.getCoddepto() != null && divipol.getCodmipio() != null
        );
    }

    @Test
    void findDivipolById() {
        // Get the first record to have a valid ID
        Divipol firstDivipol = divipolRepository.findAll().blockFirst();
        assertThat(firstDivipol).isNotNull();
        assertThat(firstDivipol.getIddivipol()).isNotNull();

        // Get it by ID
        Divipol divipol = divipolRepository.findById(firstDivipol.getIddivipol()).block();

        assertThat(divipol).isNotNull();
        assertThat(divipol.getIddivipol()).isEqualTo(firstDivipol.getIddivipol());
        assertThat(divipol.getClase()).isNotNull();
        assertThat(divipol.getCoddepto()).isNotNull();
    }

    @Test
    void findNonExistingDivipol() {
        // Get the divipol with non-existent ID
        Divipol divipol = divipolRepository.findById(Integer.MAX_VALUE).block();
        assertThat(divipol).isNull();
    }

    @Test
    void existsByIdWhenExists() {
        // Get the first record
        Divipol firstDivipol = divipolRepository.findAll().blockFirst();
        assertThat(firstDivipol).isNotNull();

        // Verify exists
        Boolean exists = divipolRepository.existsById(firstDivipol.getIddivipol()).block();
        assertThat(exists).isTrue();
    }

    @Test
    void existsByIdWhenNotExists() {
        // Verify not exists
        Boolean exists = divipolRepository.existsById(Integer.MAX_VALUE).block();
        assertThat(exists).isFalse();
    }

    @Test
    void findDivipolsByClase() {
        // Find departamentos (clase = 'D')
        List<Divipol> departamentos = divipolRepository.findAll().filter(d -> "D".equals(d.getClase())).take(50).collectList().block();

        assertThat(departamentos).isNotEmpty();
        assertThat(departamentos).allMatch(d -> "D".equals(d.getClase()));
        assertThat(departamentos).allMatch(d -> d.getNomdepto() != null && !d.getNomdepto().isBlank());
    }

    @Test
    void findDivipolsByDepartamento() {
        // Find records for Bolivar (codigo 5)
        List<Divipol> bolivar = divipolRepository
            .findAll()
            .filter(d -> Integer.valueOf(5).equals(d.getCoddepto()))
            .take(100)
            .collectList()
            .block();

        assertThat(bolivar).isNotEmpty();
        assertThat(bolivar).allMatch(d -> Integer.valueOf(5).equals(d.getCoddepto()));
        assertThat(bolivar).allMatch(d -> "BOLIVAR".equals(d.getNomdepto()));
    }

    @Test
    void verifyDivipolDataIntegrity() {
        // Verify that records have consistent data
        List<Divipol> sample = divipolRepository.findAll().take(100).collectList().block();

        assertThat(sample).isNotEmpty();
        assertThat(sample).allMatch(d -> {
            // All records must have these mandatory fields
            return (
                d.getClase() != null &&
                d.getCoddepto() != null &&
                d.getCodmipio() != null &&
                d.getCodzona() != null &&
                d.getCodpuesto() != null
            );
        });

        // Verify numeric fields are reasonable
        assertThat(sample).allMatch(d -> d.getPotfemenino() == null || d.getPotfemenino() >= 0);
        assertThat(sample).allMatch(d -> d.getPotmasculino() == null || d.getPotmasculino() >= 0);
        assertThat(sample).allMatch(d -> d.getPottotal() == null || d.getPottotal() >= 0);
    }

    @Test
    void findDivipolsWithPuestos() {
        // Find puestos (clase = 'P')
        List<Divipol> puestos = divipolRepository.findAll().filter(d -> "P".equals(d.getClase())).take(50).collectList().block();

        assertThat(puestos).isNotEmpty();
        assertThat(puestos).allMatch(d -> "P".equals(d.getClase()));
        assertThat(puestos).allMatch(d -> d.getNompuesto() != null);
    }
}
