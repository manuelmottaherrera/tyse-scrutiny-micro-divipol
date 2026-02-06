package com.tyse.scrutiny.micro.divipol.domain;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.time.Instant;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

/**
 * Entidad OrganizacionPolitica - Catálogo de organizaciones políticas
 * (partidos, movimientos, coaliciones, grupos significativos, comités de voto en blanco).
 */
@Table("organizacion_politica")
public class OrganizacionPolitica implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column("id")
    private Long id;

    @NotNull
    @Size(max = 200)
    @Column("nombre")
    private String nombre;

    @Size(max = 20)
    @Column("sigla")
    private String sigla;

    @NotNull
    @Size(max = 30)
    @Column("tipo")
    private String tipo;

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

    public OrganizacionPolitica id(Long id) {
        this.id = id;
        return this;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public OrganizacionPolitica nombre(String nombre) {
        this.nombre = nombre;
        return this;
    }

    public String getSigla() {
        return sigla;
    }

    public void setSigla(String sigla) {
        this.sigla = sigla;
    }

    public OrganizacionPolitica sigla(String sigla) {
        this.sigla = sigla;
        return this;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public OrganizacionPolitica tipo(String tipo) {
        this.tipo = tipo;
        return this;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    public OrganizacionPolitica activo(Boolean activo) {
        this.activo = activo;
        return this;
    }

    public Instant getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Instant createdDate) {
        this.createdDate = createdDate;
    }

    public OrganizacionPolitica createdDate(Instant createdDate) {
        this.createdDate = createdDate;
        return this;
    }

    public Instant getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(Instant lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    public OrganizacionPolitica lastModifiedDate(Instant lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
        return this;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public OrganizacionPolitica createdBy(String createdBy) {
        this.createdBy = createdBy;
        return this;
    }

    public String getLastModifiedBy() {
        return lastModifiedBy;
    }

    public void setLastModifiedBy(String lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
    }

    public OrganizacionPolitica lastModifiedBy(String lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof OrganizacionPolitica)) {
            return false;
        }
        return id != null && id.equals(((OrganizacionPolitica) o).id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return (
            "OrganizacionPolitica{" +
            "id=" +
            id +
            ", nombre='" +
            nombre +
            "'" +
            ", sigla='" +
            sigla +
            "'" +
            ", tipo='" +
            tipo +
            "'" +
            ", activo=" +
            activo +
            "}"
        );
    }
}
