package com.tyse.scrutiny.micro.divipol.domain;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.time.Instant;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

/**
 * Entidad TestigoPuesto - Relación M:N entre testigos y puestos.
 * Un testigo puede estar asignado a múltiples puestos de votación.
 */
@Table("testigo_puesto")
public class TestigoPuesto implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column("id")
    private Long id;

    @NotNull
    @Column("testigo_id")
    private Long testigoId;

    @NotNull
    @Column("puesto_id")
    private Integer puestoId;

    @Column("assigned_date")
    private Instant assignedDate;

    @Size(max = 50)
    @Column("assigned_by")
    private String assignedBy;

    @NotNull
    @Column("activo")
    private Boolean activo = true;

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public TestigoPuesto id(Long id) {
        this.id = id;
        return this;
    }

    public Long getTestigoId() {
        return testigoId;
    }

    public void setTestigoId(Long testigoId) {
        this.testigoId = testigoId;
    }

    public TestigoPuesto testigoId(Long testigoId) {
        this.testigoId = testigoId;
        return this;
    }

    public Integer getPuestoId() {
        return puestoId;
    }

    public void setPuestoId(Integer puestoId) {
        this.puestoId = puestoId;
    }

    public TestigoPuesto puestoId(Integer puestoId) {
        this.puestoId = puestoId;
        return this;
    }

    public Instant getAssignedDate() {
        return assignedDate;
    }

    public void setAssignedDate(Instant assignedDate) {
        this.assignedDate = assignedDate;
    }

    public TestigoPuesto assignedDate(Instant assignedDate) {
        this.assignedDate = assignedDate;
        return this;
    }

    public String getAssignedBy() {
        return assignedBy;
    }

    public void setAssignedBy(String assignedBy) {
        this.assignedBy = assignedBy;
    }

    public TestigoPuesto assignedBy(String assignedBy) {
        this.assignedBy = assignedBy;
        return this;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    public TestigoPuesto activo(Boolean activo) {
        this.activo = activo;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof TestigoPuesto)) {
            return false;
        }
        return id != null && id.equals(((TestigoPuesto) o).id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return (
            "TestigoPuesto{" +
            "id=" +
            id +
            ", testigoId=" +
            testigoId +
            ", puestoId=" +
            puestoId +
            ", assignedDate=" +
            assignedDate +
            ", assignedBy='" +
            assignedBy +
            "'" +
            ", activo=" +
            activo +
            "}"
        );
    }
}
