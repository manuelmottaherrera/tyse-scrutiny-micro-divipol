package com.tyse.scrutiny.micro.divipol.domain;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.time.Instant;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

/**
 * Entidad TestigoComision - Asignación de testigos a comisiones escrutadoras.
 * Contiene el tipo de testigo (PRINCIPAL/REMANENTE) ya que es una propiedad
 * del rol asignado, no de la persona.
 */
@Table("testigo_comision")
public class TestigoComision implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column("id")
    private Long id;

    @NotNull
    @Column("testigo_id")
    private Long testigoId;

    @NotNull
    @Column("comision_id")
    private Long comisionId;

    @Column("organizacion_id")
    private Long organizacionId;

    @NotNull
    @Size(max = 20)
    @Column("tipo_testigo")
    private String tipoTestigo;

    @Column("assigned_date")
    private Instant assignedDate;

    @Size(max = 50)
    @Column("assigned_by")
    private String assignedBy;

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

    public TestigoComision id(Long id) {
        this.id = id;
        return this;
    }

    public Long getTestigoId() {
        return testigoId;
    }

    public void setTestigoId(Long testigoId) {
        this.testigoId = testigoId;
    }

    public TestigoComision testigoId(Long testigoId) {
        this.testigoId = testigoId;
        return this;
    }

    public Long getComisionId() {
        return comisionId;
    }

    public void setComisionId(Long comisionId) {
        this.comisionId = comisionId;
    }

    public TestigoComision comisionId(Long comisionId) {
        this.comisionId = comisionId;
        return this;
    }

    public Long getOrganizacionId() {
        return organizacionId;
    }

    public void setOrganizacionId(Long organizacionId) {
        this.organizacionId = organizacionId;
    }

    public TestigoComision organizacionId(Long organizacionId) {
        this.organizacionId = organizacionId;
        return this;
    }

    public String getTipoTestigo() {
        return tipoTestigo;
    }

    public void setTipoTestigo(String tipoTestigo) {
        this.tipoTestigo = tipoTestigo;
    }

    public TestigoComision tipoTestigo(String tipoTestigo) {
        this.tipoTestigo = tipoTestigo;
        return this;
    }

    public Instant getAssignedDate() {
        return assignedDate;
    }

    public void setAssignedDate(Instant assignedDate) {
        this.assignedDate = assignedDate;
    }

    public TestigoComision assignedDate(Instant assignedDate) {
        this.assignedDate = assignedDate;
        return this;
    }

    public String getAssignedBy() {
        return assignedBy;
    }

    public void setAssignedBy(String assignedBy) {
        this.assignedBy = assignedBy;
    }

    public TestigoComision assignedBy(String assignedBy) {
        this.assignedBy = assignedBy;
        return this;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    public TestigoComision activo(Boolean activo) {
        this.activo = activo;
        return this;
    }

    public Instant getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Instant createdDate) {
        this.createdDate = createdDate;
    }

    public TestigoComision createdDate(Instant createdDate) {
        this.createdDate = createdDate;
        return this;
    }

    public Instant getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(Instant lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    public TestigoComision lastModifiedDate(Instant lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
        return this;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public TestigoComision createdBy(String createdBy) {
        this.createdBy = createdBy;
        return this;
    }

    public String getLastModifiedBy() {
        return lastModifiedBy;
    }

    public void setLastModifiedBy(String lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
    }

    public TestigoComision lastModifiedBy(String lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof TestigoComision)) {
            return false;
        }
        return id != null && id.equals(((TestigoComision) o).id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return (
            "TestigoComision{" +
            "id=" +
            id +
            ", testigoId=" +
            testigoId +
            ", comisionId=" +
            comisionId +
            ", organizacionId=" +
            organizacionId +
            ", tipoTestigo='" +
            tipoTestigo +
            "'" +
            ", activo=" +
            activo +
            "}"
        );
    }
}
