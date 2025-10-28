package com.tyse.scrutiny.micro.divipol.domain;

import jakarta.validation.constraints.*;
import java.io.Serializable;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

/**
 * Entidad Divipol - División Política de Colombia
 * Contiene información de departamentos, municipios, zonas y puestos de votación.
 */
@Table("divipol")
public class Divipol implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column("iddivipol")
    private Integer iddivipol;

    @NotNull
    @Column("clase")
    private String clase; // N=Nacional, D=Departamento, M=Municipio, P=Puesto

    @NotNull
    @Column("coddepto")
    private Integer coddepto;

    @NotNull
    @Column("codmipio")
    private Integer codmipio;

    @NotNull
    @Column("codzona")
    private Integer codzona;

    @NotNull
    @Column("codpuesto")
    private String codpuesto;

    @Column("nomdepto")
    private String nomdepto;

    @Column("nommipio")
    private String nommipio;

    @Column("nompuesto")
    private String nompuesto;

    @Column("nummesas")
    private Integer nummesas;

    @Column("potfemenino")
    private Integer potfemenino;

    @Column("potmasculino")
    private Integer potmasculino;

    @Column("pottotal")
    private Integer pottotal;

    @Column("jal")
    private Integer jal;

    @Column("nomjal")
    private String nomjal;

    @Column("indicador")
    private Integer indicador;

    @Column("expandida")
    private Integer expandida;

    @Column("direccion")
    private String direccion;

    // Coordenadas geográficas (tipo POINT de PostgreSQL)
    // Se maneja como String en formato "POINT(lon lat)" para simplicidad con R2DBC
    @Column("cordenadas")
    private String cordenadas;

    // Getters and Setters

    public Integer getIddivipol() {
        return iddivipol;
    }

    public void setIddivipol(Integer iddivipol) {
        this.iddivipol = iddivipol;
    }

    public Divipol iddivipol(Integer iddivipol) {
        this.iddivipol = iddivipol;
        return this;
    }

    public String getClase() {
        return clase;
    }

    public void setClase(String clase) {
        this.clase = clase;
    }

    public Divipol clase(String clase) {
        this.clase = clase;
        return this;
    }

    public Integer getCoddepto() {
        return coddepto;
    }

    public void setCoddepto(Integer coddepto) {
        this.coddepto = coddepto;
    }

    public Divipol coddepto(Integer coddepto) {
        this.coddepto = coddepto;
        return this;
    }

    public Integer getCodmipio() {
        return codmipio;
    }

    public void setCodmipio(Integer codmipio) {
        this.codmipio = codmipio;
    }

    public Divipol codmipio(Integer codmipio) {
        this.codmipio = codmipio;
        return this;
    }

    public Integer getCodzona() {
        return codzona;
    }

    public void setCodzona(Integer codzona) {
        this.codzona = codzona;
    }

    public Divipol codzona(Integer codzona) {
        this.codzona = codzona;
        return this;
    }

    public String getCodpuesto() {
        return codpuesto;
    }

    public void setCodpuesto(String codpuesto) {
        this.codpuesto = codpuesto;
    }

    public Divipol codpuesto(String codpuesto) {
        this.codpuesto = codpuesto;
        return this;
    }

    public String getNomdepto() {
        return nomdepto;
    }

    public void setNomdepto(String nomdepto) {
        this.nomdepto = nomdepto;
    }

    public Divipol nomdepto(String nomdepto) {
        this.nomdepto = nomdepto;
        return this;
    }

    public String getNommipio() {
        return nommipio;
    }

    public void setNommipio(String nommipio) {
        this.nommipio = nommipio;
    }

    public Divipol nommipio(String nommipio) {
        this.nommipio = nommipio;
        return this;
    }

    public String getNompuesto() {
        return nompuesto;
    }

    public void setNompuesto(String nompuesto) {
        this.nompuesto = nompuesto;
    }

    public Divipol nompuesto(String nompuesto) {
        this.nompuesto = nompuesto;
        return this;
    }

    public Integer getNummesas() {
        return nummesas;
    }

    public void setNummesas(Integer nummesas) {
        this.nummesas = nummesas;
    }

    public Divipol nummesas(Integer nummesas) {
        this.nummesas = nummesas;
        return this;
    }

    public Integer getPotfemenino() {
        return potfemenino;
    }

    public void setPotfemenino(Integer potfemenino) {
        this.potfemenino = potfemenino;
    }

    public Divipol potfemenino(Integer potfemenino) {
        this.potfemenino = potfemenino;
        return this;
    }

    public Integer getPotmasculino() {
        return potmasculino;
    }

    public void setPotmasculino(Integer potmasculino) {
        this.potmasculino = potmasculino;
    }

    public Divipol potmasculino(Integer potmasculino) {
        this.potmasculino = potmasculino;
        return this;
    }

    public Integer getPottotal() {
        return pottotal;
    }

    public void setPottotal(Integer pottotal) {
        this.pottotal = pottotal;
    }

    public Divipol pottotal(Integer pottotal) {
        this.pottotal = pottotal;
        return this;
    }

    public Integer getJal() {
        return jal;
    }

    public void setJal(Integer jal) {
        this.jal = jal;
    }

    public Divipol jal(Integer jal) {
        this.jal = jal;
        return this;
    }

    public String getNomjal() {
        return nomjal;
    }

    public void setNomjal(String nomjal) {
        this.nomjal = nomjal;
    }

    public Divipol nomjal(String nomjal) {
        this.nomjal = nomjal;
        return this;
    }

    public Integer getIndicador() {
        return indicador;
    }

    public void setIndicador(Integer indicador) {
        this.indicador = indicador;
    }

    public Divipol indicador(Integer indicador) {
        this.indicador = indicador;
        return this;
    }

    public Integer getExpandida() {
        return expandida;
    }

    public void setExpandida(Integer expandida) {
        this.expandida = expandida;
    }

    public Divipol expandida(Integer expandida) {
        this.expandida = expandida;
        return this;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public Divipol direccion(String direccion) {
        this.direccion = direccion;
        return this;
    }

    public String getCordenadas() {
        return cordenadas;
    }

    public void setCordenadas(String cordenadas) {
        this.cordenadas = cordenadas;
    }

    public Divipol cordenadas(String cordenadas) {
        this.cordenadas = cordenadas;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Divipol)) {
            return false;
        }
        return iddivipol != null && iddivipol.equals(((Divipol) o).iddivipol);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return (
            "Divipol{" +
            "iddivipol=" +
            iddivipol +
            ", clase='" +
            clase +
            "'" +
            ", coddepto=" +
            coddepto +
            ", nomdepto='" +
            nomdepto +
            "'" +
            ", codmipio=" +
            codmipio +
            ", nommipio='" +
            nommipio +
            "'" +
            ", codzona=" +
            codzona +
            ", codpuesto='" +
            codpuesto +
            "'" +
            ", nompuesto='" +
            nompuesto +
            "'" +
            ", potfemenino=" +
            potfemenino +
            ", potmasculino=" +
            potmasculino +
            ", pottotal=" +
            pottotal +
            ", nummesas=" +
            nummesas +
            "}"
        );
    }
}
