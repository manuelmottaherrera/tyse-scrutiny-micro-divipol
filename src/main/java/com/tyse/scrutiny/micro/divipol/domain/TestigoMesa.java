package com.tyse.scrutiny.micro.divipol.domain;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.time.Instant;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

/**
 * Entidad TestigoMesa - Asignación de testigos a mesas de votación.
 * Contiene el tipo de testigo (PRINCIPAL/REMANENTE) ya que es una propiedad
 * del rol asignado, no de la persona.
 */
@Table("testigo_mesa")
public class TestigoMesa implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column("id")
    private Long id;

    @NotNull
    @Column("testigo_id")
    private Long testigoId;

    @NotNull
    @Column("mesa_id")
    private Long mesaId;

    @NotNull
    @Column("puesto_id")
    private Integer puestoId;

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

    public TestigoMesa id(Long id) {
        this.id = id;
        return this;
    }

    public Long getTestigoId() {
        return testigoId;
    }

    public void setTestigoId(Long testigoId) {
        this.testigoId = testigoId;
    }

    public TestigoMesa testigoId(Long testigoId) {
        this.testigoId = testigoId;
        return this;
    }

    public Long getMesaId() {
        return mesaId;
    }

    public void setMesaId(Long mesaId) {
        this.mesaId = mesaId;
    }

    public TestigoMesa mesaId(Long mesaId) {
        this.mesaId = mesaId;
        return this;
    }

    public Integer getPuestoId() {
        return puestoId;
    }

    public void setPuestoId(Integer puestoId) {
        this.puestoId = puestoId;
    }

    public TestigoMesa puestoId(Integer puestoId) {
        this.puestoId = puestoId;
        return this;
    }

    public Long getOrganizacionId() {
        return organizacionId;
    }

    public void setOrganizacionId(Long organizacionId) {
        this.organizacionId = organizacionId;
    }

    public TestigoMesa organizacionId(Long organizacionId) {
        this.organizacionId = organizacionId;
        return this;
    }

    public String getTipoTestigo() {
        return tipoTestigo;
    }

    public void setTipoTestigo(String tipoTestigo) {
        this.tipoTestigo = tipoTestigo;
    }

    public TestigoMesa tipoTestigo(String tipoTestigo) {
        this.tipoTestigo = tipoTestigo;
        return this;
    }

    public Instant getAssignedDate() {
        return assignedDate;
    }

    public void setAssignedDate(Instant assignedDate) {
        this.assignedDate = assignedDate;
    }

    public TestigoMesa assignedDate(Instant assignedDate) {
        this.assignedDate = assignedDate;
        return this;
    }

    public String getAssignedBy() {
        return assignedBy;
    }

    public void setAssignedBy(String assignedBy) {
        this.assignedBy = assignedBy;
    }

    public TestigoMesa assignedBy(String assignedBy) {
        this.assignedBy = assignedBy;
        return this;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    public TestigoMesa activo(Boolean activo) {
        this.activo = activo;
        return this;
    }

    public Instant getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Instant createdDate) {
        this.createdDate = createdDate;
    }

    public TestigoMesa createdDate(Instant createdDate) {
        this.createdDate = createdDate;
        return this;
    }

    public Instant getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(Instant lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    public TestigoMesa lastModifiedDate(Instant lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
        return this;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public TestigoMesa createdBy(String createdBy) {
        this.createdBy = createdBy;
        return this;
    }

    public String getLastModifiedBy() {
        return lastModifiedBy;
    }

    public void setLastModifiedBy(String lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
    }

    public TestigoMesa lastModifiedBy(String lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof TestigoMesa)) {
            return false;
        }
        return id != null && id.equals(((TestigoMesa) o).id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return (
            "TestigoMesa{" +
            "id=" +
            id +
            ", testigoId=" +
            testigoId +
            ", mesaId=" +
            mesaId +
            ", puestoId=" +
            puestoId +
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
