package com.tyse.scrutiny.micro.divipol.repository.rowmapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.r2dbc.spi.Row;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.r2dbc.convert.R2dbcConverter;
import org.springframework.data.r2dbc.convert.R2dbcCustomConversions;

/**
 * Tests unitarios para {@link ColumnConverter}
 *
 * Valida la conversión de tipos desde Row (R2DBC) a tipos Java,
 * incluyendo conversiones customizadas y manejo de Enums.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ColumnConverter - Conversión de tipos R2DBC")
class ColumnConverterTest {

    @Mock
    private R2dbcCustomConversions customConversions;

    @Mock
    private R2dbcConverter r2dbcConverter;

    @Mock
    private ConversionService conversionService;

    private ColumnConverter columnConverter;

    @BeforeEach
    void setUp() {
        when(r2dbcConverter.getConversionService()).thenReturn(conversionService);
        columnConverter = new ColumnConverter(customConversions, r2dbcConverter);
    }

    @Nested
    @DisplayName("convert() - Conversión de valores")
    class ConvertTests {

        @Test
        @DisplayName("Debe retornar null si el valor es null")
        void shouldReturnNullWhenValueIsNull() {
            String result = columnConverter.convert(null, String.class);

            assertThat(result).isNull();
        }

        @Test
        @DisplayName("Debe retornar null si el target es null")
        void shouldReturnNullWhenTargetIsNull() {
            Object result = columnConverter.convert("test", null);

            assertThat(result).isEqualTo("test");
        }

        @Test
        @DisplayName("Debe retornar el mismo valor si ya es del tipo correcto")
        void shouldReturnSameValueWhenAlreadyCorrectType() {
            String input = "test";

            String result = columnConverter.convert(input, String.class);

            assertThat(result).isSameAs(input);
        }

        @Test
        @DisplayName("Debe retornar el mismo valor si es asignable al tipo target")
        void shouldReturnSameValueWhenAssignableToTarget() {
            Integer input = 42;

            Number result = columnConverter.convert(input, Number.class);

            assertThat(result).isSameAs(input);
        }

        @Test
        @DisplayName("Debe usar conversión customizada si está disponible")
        void shouldUseCustomConversionWhenAvailable() {
            Long input = 1234567890L;
            Instant expected = Instant.ofEpochMilli(input);

            when(customConversions.hasCustomReadTarget(Long.class, Instant.class)).thenReturn(true);
            when(conversionService.convert(input, Instant.class)).thenReturn(expected);

            Instant result = columnConverter.convert(input, Instant.class);

            assertThat(result).isEqualTo(expected);
            verify(customConversions).hasCustomReadTarget(Long.class, Instant.class);
            verify(conversionService).convert(input, Instant.class);
        }

        @Test
        @DisplayName("Debe convertir String a Enum usando valueOf()")
        void shouldConvertStringToEnum() {
            String input = "PENDING";

            TestStatus result = columnConverter.convert(input, TestStatus.class);

            assertThat(result).isEqualTo(TestStatus.PENDING);
        }

        @Test
        @DisplayName("Debe usar ConversionService para conversiones estándar")
        void shouldUseConversionServiceForStandardConversions() {
            String input = "42";
            Integer expected = 42;

            when(customConversions.hasCustomReadTarget(String.class, Integer.class)).thenReturn(false);
            when(conversionService.convert(input, Integer.class)).thenReturn(expected);

            Integer result = columnConverter.convert(input, Integer.class);

            assertThat(result).isEqualTo(expected);
            verify(conversionService).convert(input, Integer.class);
        }
    }

    @Nested
    @DisplayName("fromRow() - Conversión desde Row")
    class FromRowTests {

        @Mock
        private Row row;

        @Test
        @DisplayName("Debe obtener valor directamente del driver si es posible")
        void shouldGetValueDirectlyFromDriverWhenPossible() {
            String expected = "test_value";
            when(row.get("column_name", String.class)).thenReturn(expected);

            String result = columnConverter.fromRow(row, "column_name", String.class);

            assertThat(result).isEqualTo(expected);
            verify(row).get("column_name", String.class);
            verify(row, never()).get(eq("column_name"));
        }

        @Test
        @DisplayName("Debe usar convert() si el driver falla")
        void shouldUseConvertWhenDriverFails() {
            when(row.get("column_name", Integer.class)).thenThrow(new RuntimeException("Driver error"));
            when(row.get("column_name")).thenReturn("42");
            when(customConversions.hasCustomReadTarget(String.class, Integer.class)).thenReturn(false);
            when(conversionService.convert("42", Integer.class)).thenReturn(42);

            Integer result = columnConverter.fromRow(row, "column_name", Integer.class);

            assertThat(result).isEqualTo(42);
            verify(row).get("column_name", Integer.class); // Intento directo
            verify(row).get("column_name"); // Fallback
            verify(conversionService).convert("42", Integer.class);
        }

        @Test
        @DisplayName("Debe manejar conversión de null desde Row")
        void shouldHandleNullValueFromRow() {
            when(row.get("nullable_column", String.class)).thenReturn(null);

            String result = columnConverter.fromRow(row, "nullable_column", String.class);

            assertThat(result).isNull();
        }
    }

    @Nested
    @DisplayName("Casos edge - Conversiones especiales")
    class EdgeCaseTests {

        @Test
        @DisplayName("Debe manejar conversión de Long a Integer")
        void shouldConvertLongToInteger() {
            Long input = 100L;
            Integer expected = 100;

            when(customConversions.hasCustomReadTarget(Long.class, Integer.class)).thenReturn(false);
            when(conversionService.convert(input, Integer.class)).thenReturn(expected);

            Integer result = columnConverter.convert(input, Integer.class);

            assertThat(result).isEqualTo(expected);
        }

        @Test
        @DisplayName("Debe manejar conversión de Integer a Long")
        void shouldConvertIntegerToLong() {
            Integer input = 100;
            Long expected = 100L;

            when(customConversions.hasCustomReadTarget(Integer.class, Long.class)).thenReturn(false);
            when(conversionService.convert(input, Long.class)).thenReturn(expected);

            Long result = columnConverter.convert(input, Long.class);

            assertThat(result).isEqualTo(expected);
        }

        @Test
        @DisplayName("Debe manejar múltiples conversiones en cadena")
        void shouldHandleMultipleConversions() {
            // Simula conversión String -> Integer -> Long
            String input1 = "42";
            Integer intermediate = 42;
            Long expected = 42L;

            when(customConversions.hasCustomReadTarget(String.class, Integer.class)).thenReturn(false);
            when(conversionService.convert(input1, Integer.class)).thenReturn(intermediate);

            Integer result1 = columnConverter.convert(input1, Integer.class);
            assertThat(result1).isEqualTo(intermediate);

            when(customConversions.hasCustomReadTarget(Integer.class, Long.class)).thenReturn(false);
            when(conversionService.convert(intermediate, Long.class)).thenReturn(expected);

            Long result2 = columnConverter.convert(result1, Long.class);
            assertThat(result2).isEqualTo(expected);
        }
    }

    /**
     * Enum de prueba para tests de conversión
     */
    private enum TestStatus {
        PENDING,
        COMPLETED,
        FAILED,
    }
}
