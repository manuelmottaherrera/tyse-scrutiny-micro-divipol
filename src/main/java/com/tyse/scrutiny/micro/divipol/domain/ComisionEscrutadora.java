package com.tyse.scrutiny.micro.divipol.domain;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

/**
 * Entidad ComisionEscrutadora - Comisiones escrutadoras de diferentes niveles
 * (auxiliar, municipal, distrital, general).
 */
@Table("comision_escrutadora")
public class ComisionEscrutadora implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column("id")
    private Long id;

    @NotNull
    @Size(max = 20)
    @Column("tipo")
    private String tipo;

    @NotNull
    @Size(max = 200)
    @Column("nombre")
    private String nombre;

    @Size(max = 300)
    @Column("ubicacion")
    private String ubicacion;

    @Column("departamento_id")
    private Integer departamentoId;

    @Column("municipio_id")
    private Integer municipioId;

    @Column("fecha_inicio")
    private LocalDate fechaInicio;

    @NotNull
    @Column("activo")
    private Boolean activo = true;

    @Column("created_date")
    private Instant createdDate;

    @Column("last_modified_date")
    private Instant lastModifiedDate;

    @Size(max = 50)
    @Column("created_by")
    private String createdBy;

    @Size(max = 50)
    @Column("last_modified_by")
    private String lastModifiedBy;

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ComisionEscrutadora id(Long id) {
        this.id = id;
        return this;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public ComisionEscrutadora tipo(String tipo) {
        this.tipo = tipo;
        return this;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public ComisionEscrutadora nombre(String nombre) {
        this.nombre = nombre;
        return this;
    }

    public String getUbicacion() {
        return ubicacion;
    }

    public void setUbicacion(String ubicacion) {
        this.ubicacion = ubicacion;
    }

    public ComisionEscrutadora ubicacion(String ubicacion) {
        this.ubicacion = ubicacion;
        return this;
    }

    public Integer getDepartamentoId() {
        return departamentoId;
    }

    public void setDepartamentoId(Integer departamentoId) {
        this.departamentoId = departamentoId;
    }

    public ComisionEscrutadora departamentoId(Integer departamentoId) {
        this.departamentoId = departamentoId;
        return this;
    }

    public Integer getMunicipioId() {
        return municipioId;
    }

    public void setMunicipioId(Integer municipioId) {
        this.municipioId = municipioId;
    }

    public ComisionEscrutadora municipioId(Integer municipioId) {
        this.municipioId = municipioId;
        return this;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDate fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public ComisionEscrutadora fechaInicio(LocalDate fechaInicio) {
        this.fechaInicio = fechaInicio;
        return this;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    public ComisionEscrutadora activo(Boolean activo) {
        this.activo = activo;
        return this;
    }

    public Instant getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Instant createdDate) {
        this.createdDate = createdDate;
    }

    public ComisionEscrutadora createdDate(Instant createdDate) {
        this.createdDate = createdDate;
        return this;
    }

    public Instant getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(Instant lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    public ComisionEscrutadora lastModifiedDate(Instant lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
        return this;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public ComisionEscrutadora createdBy(String createdBy) {
        this.createdBy = createdBy;
        return this;
    }

    public String getLastModifiedBy() {
        return lastModifiedBy;
    }

    public void setLastModifiedBy(String lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
    }

    public ComisionEscrutadora lastModifiedBy(String lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ComisionEscrutadora)) {
            return false;
        }
        return id != null && id.equals(((ComisionEscrutadora) o).id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return (
            "ComisionEscrutadora{" +
            "id=" +
            id +
            ", tipo='" +
            tipo +
            "'" +
            ", nombre='" +
            nombre +
            "'" +
            ", ubicacion='" +
            ubicacion +
            "'" +
            ", departamentoId=" +
            departamentoId +
            ", municipioId=" +
            municipioId +
            ", activo=" +
            activo +
            "}"
        );
    }
}
