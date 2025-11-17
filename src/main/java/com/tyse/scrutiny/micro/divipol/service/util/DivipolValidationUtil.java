package com.tyse.scrutiny.micro.divipol.service.util;

import com.tyse.scrutiny.micro.divipol.web.rest.errors.BadRequestAlertException;

/**
 * Utilidad para validar parámetros de entrada de la API DIVIPOL
 */
public final class DivipolValidationUtil {

    private DivipolValidationUtil() {
        // Clase de utilidad, no debe instanciarse
    }

    /**
     * Valida que el código de departamento sea válido (>= 0)
     *
     * @param codDepto código de departamento a validar
     * @throws BadRequestAlertException si el código es inválido (negativo o null)
     */
    public static void validateCodDepto(Integer codDepto) {
        if (codDepto == null) {
            throw new BadRequestAlertException("El código de departamento es requerido", "divipol", "coddepto.required");
        }
        if (codDepto < 0) {
            throw new BadRequestAlertException("El código de departamento debe ser mayor o igual a 0", "divipol", "coddepto.invalid");
        }
    }

    /**
     * Valida que el código de municipio sea válido (>= 0)
     *
     * @param codMpio código de municipio a validar
     * @throws BadRequestAlertException si el código es inválido (negativo o null)
     */
    public static void validateCodMunicipio(Integer codMpio) {
        if (codMpio == null) {
            throw new BadRequestAlertException("El código de municipio es requerido", "divipol", "codmipio.required");
        }
        if (codMpio < 0) {
            throw new BadRequestAlertException("El código de municipio debe ser mayor o igual a 0", "divipol", "codmipio.invalid");
        }
    }

    /**
     * Valida que el código de zona sea válido (>= 0)
     *
     * @param codZona código de zona a validar
     * @throws BadRequestAlertException si el código es inválido (negativo o null)
     */
    public static void validateCodZona(Integer codZona) {
        if (codZona == null) {
            throw new BadRequestAlertException("El código de zona es requerido", "divipol", "codzona.required");
        }
        if (codZona < 0) {
            throw new BadRequestAlertException("El código de zona debe ser mayor o igual a 0", "divipol", "codzona.invalid");
        }
    }
}
