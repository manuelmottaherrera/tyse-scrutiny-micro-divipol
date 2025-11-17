package com.tyse.scrutiny.micro.divipol.service.util;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.tyse.scrutiny.micro.divipol.web.rest.errors.BadRequestAlertException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Tests unitarios para {@link DivipolValidationUtil}
 *
 * Valida las reglas de negocio para códigos de departamento, municipio y zona:
 * - Los códigos no pueden ser null
 * - Los códigos deben ser >= 0
 * - Los mensajes de error deben ser descriptivos
 */
@DisplayName("DivipolValidationUtil - Validaciones de parámetros")
class DivipolValidationUtilTest {

    @Nested
    @DisplayName("Validación de código de departamento")
    class ValidateCodDeptoTests {

        @Test
        @DisplayName("Debe aceptar código de departamento 0 (válido)")
        void shouldAcceptZeroAsValidCodDepto() {
            assertThatCode(() -> DivipolValidationUtil.validateCodDepto(0)).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Debe aceptar códigos de departamento positivos")
        void shouldAcceptPositiveCodDepto() {
            assertThatCode(() -> DivipolValidationUtil.validateCodDepto(5)).doesNotThrowAnyException();
            assertThatCode(() -> DivipolValidationUtil.validateCodDepto(99)).doesNotThrowAnyException();
            assertThatCode(() -> DivipolValidationUtil.validateCodDepto(Integer.MAX_VALUE)).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Debe rechazar código de departamento null")
        void shouldRejectNullCodDepto() {
            assertThatThrownBy(() -> DivipolValidationUtil.validateCodDepto(null))
                .isInstanceOf(BadRequestAlertException.class)
                .hasMessageContaining("código de departamento es requerido")
                .hasFieldOrPropertyWithValue("entityName", "divipol")
                .hasFieldOrPropertyWithValue("errorKey", "coddepto.required");
        }

        @Test
        @DisplayName("Debe rechazar códigos de departamento negativos")
        void shouldRejectNegativeCodDepto() {
            assertThatThrownBy(() -> DivipolValidationUtil.validateCodDepto(-1))
                .isInstanceOf(BadRequestAlertException.class)
                .hasMessageContaining("debe ser mayor o igual a 0")
                .hasFieldOrPropertyWithValue("entityName", "divipol")
                .hasFieldOrPropertyWithValue("errorKey", "coddepto.invalid");

            assertThatThrownBy(() -> DivipolValidationUtil.validateCodDepto(-99))
                .isInstanceOf(BadRequestAlertException.class)
                .hasMessageContaining("debe ser mayor o igual a 0");

            assertThatThrownBy(() -> DivipolValidationUtil.validateCodDepto(Integer.MIN_VALUE))
                .isInstanceOf(BadRequestAlertException.class)
                .hasMessageContaining("debe ser mayor o igual a 0");
        }
    }

    @Nested
    @DisplayName("Validación de código de municipio")
    class ValidateCodMunicipioTests {

        @Test
        @DisplayName("Debe aceptar código de municipio 0 (válido)")
        void shouldAcceptZeroAsValidCodMunicipio() {
            assertThatCode(() -> DivipolValidationUtil.validateCodMunicipio(0)).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Debe aceptar códigos de municipio positivos")
        void shouldAcceptPositiveCodMunicipio() {
            assertThatCode(() -> DivipolValidationUtil.validateCodMunicipio(1)).doesNotThrowAnyException();
            assertThatCode(() -> DivipolValidationUtil.validateCodMunicipio(835)).doesNotThrowAnyException();
            assertThatCode(() -> DivipolValidationUtil.validateCodMunicipio(Integer.MAX_VALUE)).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Debe rechazar código de municipio null")
        void shouldRejectNullCodMunicipio() {
            assertThatThrownBy(() -> DivipolValidationUtil.validateCodMunicipio(null))
                .isInstanceOf(BadRequestAlertException.class)
                .hasMessageContaining("código de municipio es requerido")
                .hasFieldOrPropertyWithValue("entityName", "divipol")
                .hasFieldOrPropertyWithValue("errorKey", "codmipio.required");
        }

        @Test
        @DisplayName("Debe rechazar códigos de municipio negativos")
        void shouldRejectNegativeCodMunicipio() {
            assertThatThrownBy(() -> DivipolValidationUtil.validateCodMunicipio(-1))
                .isInstanceOf(BadRequestAlertException.class)
                .hasMessageContaining("debe ser mayor o igual a 0")
                .hasFieldOrPropertyWithValue("entityName", "divipol")
                .hasFieldOrPropertyWithValue("errorKey", "codmipio.invalid");

            assertThatThrownBy(() -> DivipolValidationUtil.validateCodMunicipio(-50))
                .isInstanceOf(BadRequestAlertException.class)
                .hasMessageContaining("debe ser mayor o igual a 0");
        }
    }

    @Nested
    @DisplayName("Validación de código de zona")
    class ValidateCodZonaTests {

        @Test
        @DisplayName("Debe aceptar código de zona 0 (sin zonificación)")
        void shouldAcceptZeroAsValidCodZona() {
            assertThatCode(() -> DivipolValidationUtil.validateCodZona(0)).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Debe aceptar códigos de zona positivos")
        void shouldAcceptPositiveCodZona() {
            assertThatCode(() -> DivipolValidationUtil.validateCodZona(1)).doesNotThrowAnyException();
            assertThatCode(() -> DivipolValidationUtil.validateCodZona(99)).doesNotThrowAnyException();
            assertThatCode(() -> DivipolValidationUtil.validateCodZona(Integer.MAX_VALUE)).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Debe rechazar código de zona null")
        void shouldRejectNullCodZona() {
            assertThatThrownBy(() -> DivipolValidationUtil.validateCodZona(null))
                .isInstanceOf(BadRequestAlertException.class)
                .hasMessageContaining("código de zona es requerido")
                .hasFieldOrPropertyWithValue("entityName", "divipol")
                .hasFieldOrPropertyWithValue("errorKey", "codzona.required");
        }

        @Test
        @DisplayName("Debe rechazar códigos de zona negativos")
        void shouldRejectNegativeCodZona() {
            assertThatThrownBy(() -> DivipolValidationUtil.validateCodZona(-1))
                .isInstanceOf(BadRequestAlertException.class)
                .hasMessageContaining("debe ser mayor o igual a 0")
                .hasFieldOrPropertyWithValue("entityName", "divipol")
                .hasFieldOrPropertyWithValue("errorKey", "codzona.invalid");

            assertThatThrownBy(() -> DivipolValidationUtil.validateCodZona(-10))
                .isInstanceOf(BadRequestAlertException.class)
                .hasMessageContaining("debe ser mayor o igual a 0");
        }
    }
}
