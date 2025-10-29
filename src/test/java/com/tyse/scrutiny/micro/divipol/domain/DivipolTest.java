package com.tyse.scrutiny.micro.divipol.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link Divipol} entity.
 * Prueba equals, hashCode, toString, builders y getters/setters.
 */
class DivipolTest {

    @Test
    void equalsVerifier() {
        // Test equals with same object
        Divipol divipol1 = new Divipol();
        assertThat(divipol1).isEqualTo(divipol1);

        // Test equals with null
        assertThat(divipol1).isNotEqualTo(null);

        // Test equals with different class
        assertThat(divipol1).isNotEqualTo(new Object());

        // Test equals with null id
        Divipol divipol2 = new Divipol();
        assertThat(divipol1).isNotEqualTo(divipol2);

        // Test equals with same id
        divipol1.setIddivipol(1);
        divipol2.setIddivipol(1);
        assertThat(divipol1).isEqualTo(divipol2);

        // Test equals with different id
        divipol2.setIddivipol(2);
        assertThat(divipol1).isNotEqualTo(divipol2);

        // Test equals with one null id
        divipol1.setIddivipol(null);
        assertThat(divipol1).isNotEqualTo(divipol2);
    }

    @Test
    void hashCodeVerifier() {
        Divipol divipol1 = new Divipol();
        Divipol divipol2 = new Divipol();

        // HashCode should be consistent
        assertThat(divipol1.hashCode()).isEqualTo(divipol1.hashCode());

        // HashCode based on class, not on id
        assertThat(divipol1.hashCode()).isEqualTo(divipol2.hashCode());

        // HashCode should remain same with different ids
        divipol1.setIddivipol(1);
        divipol2.setIddivipol(2);
        assertThat(divipol1.hashCode()).isEqualTo(divipol2.hashCode());
    }

    @Test
    void toStringVerifier() {
        Divipol divipol = new Divipol()
            .iddivipol(1)
            .clase("D")
            .coddepto(5)
            .nomdepto("BOLIVAR")
            .codmipio(1)
            .nommipio("CARTAGENA")
            .codzona(1)
            .codpuesto("P001")
            .nompuesto("PUESTO 1")
            .potfemenino(500)
            .potmasculino(480)
            .pottotal(980)
            .nummesas(10);

        String result = divipol.toString();

        // Verify toString contains key information
        assertThat(result).contains("Divipol{");
        assertThat(result).contains("iddivipol=1");
        assertThat(result).contains("clase='D'");
        assertThat(result).contains("coddepto=5");
        assertThat(result).contains("nomdepto='BOLIVAR'");
        assertThat(result).contains("codmipio=1");
        assertThat(result).contains("nommipio='CARTAGENA'");
        assertThat(result).contains("codzona=1");
        assertThat(result).contains("codpuesto='P001'");
        assertThat(result).contains("nompuesto='PUESTO 1'");
        assertThat(result).contains("potfemenino=500");
        assertThat(result).contains("potmasculino=480");
        assertThat(result).contains("pottotal=980");
        assertThat(result).contains("nummesas=10");
    }

    @Test
    void testIddivipolGetterSetter() {
        Divipol divipol = new Divipol();
        Integer id = 123;

        divipol.setIddivipol(id);
        assertThat(divipol.getIddivipol()).isEqualTo(id);
    }

    @Test
    void testIddivipolFluentSetter() {
        Divipol divipol = new Divipol();
        Integer id = 456;

        Divipol result = divipol.iddivipol(id);

        assertThat(result).isSameAs(divipol);
        assertThat(divipol.getIddivipol()).isEqualTo(id);
    }

    @Test
    void testClaseGetterSetter() {
        Divipol divipol = new Divipol();
        String clase = "D";

        divipol.setClase(clase);
        assertThat(divipol.getClase()).isEqualTo(clase);
    }

    @Test
    void testClaseFluentSetter() {
        Divipol divipol = new Divipol();
        String clase = "M";

        Divipol result = divipol.clase(clase);

        assertThat(result).isSameAs(divipol);
        assertThat(divipol.getClase()).isEqualTo(clase);
    }

    @Test
    void testCoddeptoGetterSetter() {
        Divipol divipol = new Divipol();
        Integer coddepto = 5;

        divipol.setCoddepto(coddepto);
        assertThat(divipol.getCoddepto()).isEqualTo(coddepto);
    }

    @Test
    void testCoddeptoFluentSetter() {
        Divipol divipol = new Divipol();
        Integer coddepto = 8;

        Divipol result = divipol.coddepto(coddepto);

        assertThat(result).isSameAs(divipol);
        assertThat(divipol.getCoddepto()).isEqualTo(coddepto);
    }

    @Test
    void testNomdeptoGetterSetter() {
        Divipol divipol = new Divipol();
        String nomdepto = "ANTIOQUIA";

        divipol.setNomdepto(nomdepto);
        assertThat(divipol.getNomdepto()).isEqualTo(nomdepto);
    }

    @Test
    void testNomdeptoFluentSetter() {
        Divipol divipol = new Divipol();
        String nomdepto = "BOLIVAR";

        Divipol result = divipol.nomdepto(nomdepto);

        assertThat(result).isSameAs(divipol);
        assertThat(divipol.getNomdepto()).isEqualTo(nomdepto);
    }

    @Test
    void testCodmipioGetterSetter() {
        Divipol divipol = new Divipol();
        Integer codmipio = 1;

        divipol.setCodmipio(codmipio);
        assertThat(divipol.getCodmipio()).isEqualTo(codmipio);
    }

    @Test
    void testCodmipioFluentSetter() {
        Divipol divipol = new Divipol();
        Integer codmipio = 2;

        Divipol result = divipol.codmipio(codmipio);

        assertThat(result).isSameAs(divipol);
        assertThat(divipol.getCodmipio()).isEqualTo(codmipio);
    }

    @Test
    void testNommipioGetterSetter() {
        Divipol divipol = new Divipol();
        String nommipio = "MEDELLIN";

        divipol.setNommipio(nommipio);
        assertThat(divipol.getNommipio()).isEqualTo(nommipio);
    }

    @Test
    void testNommipioFluentSetter() {
        Divipol divipol = new Divipol();
        String nommipio = "CARTAGENA";

        Divipol result = divipol.nommipio(nommipio);

        assertThat(result).isSameAs(divipol);
        assertThat(divipol.getNommipio()).isEqualTo(nommipio);
    }

    @Test
    void testCodzonaGetterSetter() {
        Divipol divipol = new Divipol();
        Integer codzona = 1;

        divipol.setCodzona(codzona);
        assertThat(divipol.getCodzona()).isEqualTo(codzona);
    }

    @Test
    void testCodzonaFluentSetter() {
        Divipol divipol = new Divipol();
        Integer codzona = 3;

        Divipol result = divipol.codzona(codzona);

        assertThat(result).isSameAs(divipol);
        assertThat(divipol.getCodzona()).isEqualTo(codzona);
    }

    @Test
    void testCodpuestoGetterSetter() {
        Divipol divipol = new Divipol();
        String codpuesto = "P001";

        divipol.setCodpuesto(codpuesto);
        assertThat(divipol.getCodpuesto()).isEqualTo(codpuesto);
    }

    @Test
    void testCodpuestoFluentSetter() {
        Divipol divipol = new Divipol();
        String codpuesto = "P002";

        Divipol result = divipol.codpuesto(codpuesto);

        assertThat(result).isSameAs(divipol);
        assertThat(divipol.getCodpuesto()).isEqualTo(codpuesto);
    }

    @Test
    void testNompuestoGetterSetter() {
        Divipol divipol = new Divipol();
        String nompuesto = "PUESTO DE PRUEBA";

        divipol.setNompuesto(nompuesto);
        assertThat(divipol.getNompuesto()).isEqualTo(nompuesto);
    }

    @Test
    void testNompuestoFluentSetter() {
        Divipol divipol = new Divipol();
        String nompuesto = "PUESTO CENTRAL";

        Divipol result = divipol.nompuesto(nompuesto);

        assertThat(result).isSameAs(divipol);
        assertThat(divipol.getNompuesto()).isEqualTo(nompuesto);
    }

    @Test
    void testNummesasGetterSetter() {
        Divipol divipol = new Divipol();
        Integer nummesas = 10;

        divipol.setNummesas(nummesas);
        assertThat(divipol.getNummesas()).isEqualTo(nummesas);
    }

    @Test
    void testNummesasFluentSetter() {
        Divipol divipol = new Divipol();
        Integer nummesas = 15;

        Divipol result = divipol.nummesas(nummesas);

        assertThat(result).isSameAs(divipol);
        assertThat(divipol.getNummesas()).isEqualTo(nummesas);
    }

    @Test
    void testPotfemeninoGetterSetter() {
        Divipol divipol = new Divipol();
        Integer potfemenino = 500;

        divipol.setPotfemenino(potfemenino);
        assertThat(divipol.getPotfemenino()).isEqualTo(potfemenino);
    }

    @Test
    void testPotfemeninoFluentSetter() {
        Divipol divipol = new Divipol();
        Integer potfemenino = 600;

        Divipol result = divipol.potfemenino(potfemenino);

        assertThat(result).isSameAs(divipol);
        assertThat(divipol.getPotfemenino()).isEqualTo(potfemenino);
    }

    @Test
    void testPotmasculinoGetterSetter() {
        Divipol divipol = new Divipol();
        Integer potmasculino = 480;

        divipol.setPotmasculino(potmasculino);
        assertThat(divipol.getPotmasculino()).isEqualTo(potmasculino);
    }

    @Test
    void testPotmasculinoFluentSetter() {
        Divipol divipol = new Divipol();
        Integer potmasculino = 550;

        Divipol result = divipol.potmasculino(potmasculino);

        assertThat(result).isSameAs(divipol);
        assertThat(divipol.getPotmasculino()).isEqualTo(potmasculino);
    }

    @Test
    void testPottotalGetterSetter() {
        Divipol divipol = new Divipol();
        Integer pottotal = 980;

        divipol.setPottotal(pottotal);
        assertThat(divipol.getPottotal()).isEqualTo(pottotal);
    }

    @Test
    void testPottotalFluentSetter() {
        Divipol divipol = new Divipol();
        Integer pottotal = 1150;

        Divipol result = divipol.pottotal(pottotal);

        assertThat(result).isSameAs(divipol);
        assertThat(divipol.getPottotal()).isEqualTo(pottotal);
    }

    @Test
    void testJalGetterSetter() {
        Divipol divipol = new Divipol();
        Integer jal = 1;

        divipol.setJal(jal);
        assertThat(divipol.getJal()).isEqualTo(jal);
    }

    @Test
    void testJalFluentSetter() {
        Divipol divipol = new Divipol();
        Integer jal = 0;

        Divipol result = divipol.jal(jal);

        assertThat(result).isSameAs(divipol);
        assertThat(divipol.getJal()).isEqualTo(jal);
    }

    @Test
    void testNomjalGetterSetter() {
        Divipol divipol = new Divipol();
        String nomjal = "JAL TEST";

        divipol.setNomjal(nomjal);
        assertThat(divipol.getNomjal()).isEqualTo(nomjal);
    }

    @Test
    void testNomjalFluentSetter() {
        Divipol divipol = new Divipol();
        String nomjal = "JAL PRINCIPAL";

        Divipol result = divipol.nomjal(nomjal);

        assertThat(result).isSameAs(divipol);
        assertThat(divipol.getNomjal()).isEqualTo(nomjal);
    }

    @Test
    void testIndicadorGetterSetter() {
        Divipol divipol = new Divipol();
        Integer indicador = 1;

        divipol.setIndicador(indicador);
        assertThat(divipol.getIndicador()).isEqualTo(indicador);
    }

    @Test
    void testIndicadorFluentSetter() {
        Divipol divipol = new Divipol();
        Integer indicador = 0;

        Divipol result = divipol.indicador(indicador);

        assertThat(result).isSameAs(divipol);
        assertThat(divipol.getIndicador()).isEqualTo(indicador);
    }

    @Test
    void testExpandidaGetterSetter() {
        Divipol divipol = new Divipol();
        Integer expandida = 1;

        divipol.setExpandida(expandida);
        assertThat(divipol.getExpandida()).isEqualTo(expandida);
    }

    @Test
    void testExpandidaFluentSetter() {
        Divipol divipol = new Divipol();
        Integer expandida = 0;

        Divipol result = divipol.expandida(expandida);

        assertThat(result).isSameAs(divipol);
        assertThat(divipol.getExpandida()).isEqualTo(expandida);
    }

    @Test
    void testDireccionGetterSetter() {
        Divipol divipol = new Divipol();
        String direccion = "Calle 123 #45-67";

        divipol.setDireccion(direccion);
        assertThat(divipol.getDireccion()).isEqualTo(direccion);
    }

    @Test
    void testDireccionFluentSetter() {
        Divipol divipol = new Divipol();
        String direccion = "Carrera 10 #20-30";

        Divipol result = divipol.direccion(direccion);

        assertThat(result).isSameAs(divipol);
        assertThat(divipol.getDireccion()).isEqualTo(direccion);
    }

    @Test
    void testCordenadasGetterSetter() {
        Divipol divipol = new Divipol();
        String cordenadas = "POINT(-75.5636 6.2442)";

        divipol.setCordenadas(cordenadas);
        assertThat(divipol.getCordenadas()).isEqualTo(cordenadas);
    }

    @Test
    void testCordenadasFluentSetter() {
        Divipol divipol = new Divipol();
        String cordenadas = "POINT(-74.0721 4.7110)";

        Divipol result = divipol.cordenadas(cordenadas);

        assertThat(result).isSameAs(divipol);
        assertThat(divipol.getCordenadas()).isEqualTo(cordenadas);
    }

    @Test
    void testCompleteEntityCreation() {
        // Test creating a complete entity using fluent API
        Divipol divipol = new Divipol()
            .iddivipol(1)
            .clase("P")
            .coddepto(5)
            .nomdepto("BOLIVAR")
            .codmipio(1)
            .nommipio("CARTAGENA")
            .codzona(1)
            .codpuesto("P001")
            .nompuesto("PUESTO CENTRAL")
            .nummesas(10)
            .potfemenino(500)
            .potmasculino(480)
            .pottotal(980)
            .jal(0)
            .nomjal("")
            .indicador(0)
            .expandida(0)
            .direccion("Calle Falsa 123")
            .cordenadas("POINT(-75.5636 6.2442)");

        assertThat(divipol.getIddivipol()).isEqualTo(1);
        assertThat(divipol.getClase()).isEqualTo("P");
        assertThat(divipol.getCoddepto()).isEqualTo(5);
        assertThat(divipol.getNomdepto()).isEqualTo("BOLIVAR");
        assertThat(divipol.getCodmipio()).isEqualTo(1);
        assertThat(divipol.getNommipio()).isEqualTo("CARTAGENA");
        assertThat(divipol.getCodzona()).isEqualTo(1);
        assertThat(divipol.getCodpuesto()).isEqualTo("P001");
        assertThat(divipol.getNompuesto()).isEqualTo("PUESTO CENTRAL");
        assertThat(divipol.getNummesas()).isEqualTo(10);
        assertThat(divipol.getPotfemenino()).isEqualTo(500);
        assertThat(divipol.getPotmasculino()).isEqualTo(480);
        assertThat(divipol.getPottotal()).isEqualTo(980);
        assertThat(divipol.getJal()).isEqualTo(0);
        assertThat(divipol.getNomjal()).isEqualTo("");
        assertThat(divipol.getIndicador()).isEqualTo(0);
        assertThat(divipol.getExpandida()).isEqualTo(0);
        assertThat(divipol.getDireccion()).isEqualTo("Calle Falsa 123");
        assertThat(divipol.getCordenadas()).isEqualTo("POINT(-75.5636 6.2442)");
    }

    @Test
    void testSerializable() {
        // Verify that Divipol implements Serializable
        Divipol divipol = new Divipol();
        assertThat(divipol).isInstanceOf(java.io.Serializable.class);
    }
}
