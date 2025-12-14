package com.tyse.scrutiny.micro.divipol.domain;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.time.Instant;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

/**
 * Entidad Jurado - Jurados de votación asignados a puestos.
 * Solo lectura - los datos vienen de integración externa.
 */
@Table("jurado")
public class Jurado implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column("id")
    private Long id;

    @NotNull
    @Size(max = 10)
    @Column("tipo_documento")
    private String tipoDocumento;

    @NotNull
    @Size(max = 20)
    @Column("numero_documento")
    private String numeroDocumento;

    @NotNull
    @Size(max = 100)
    @Column("nombres")
    private String nombres;

    @NotNull
    @Size(max = 100)
    @Column("apellidos")
    private String apellidos;

    @NotNull
    @Column("puesto_id")
    private Integer puestoId;

    @Column("created_date")
    private Instant createdDate;

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Jurado id(Long id) {
        this.id = id;
        return this;
    }

    public String getTipoDocumento() {
        return tipoDocumento;
    }

    public void setTipoDocumento(String tipoDocumento) {
        this.tipoDocumento = tipoDocumento;
    }

    public Jurado tipoDocumento(String tipoDocumento) {
        this.tipoDocumento = tipoDocumento;
        return this;
    }

    public String getNumeroDocumento() {
        return numeroDocumento;
    }

    public void setNumeroDocumento(String numeroDocumento) {
        this.numeroDocumento = numeroDocumento;
    }

    public Jurado numeroDocumento(String numeroDocumento) {
        this.numeroDocumento = numeroDocumento;
        return this;
    }

    public String getNombres() {
        return nombres;
    }

    public void setNombres(String nombres) {
        this.nombres = nombres;
    }

    public Jurado nombres(String nombres) {
        this.nombres = nombres;
        return this;
    }

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    public Jurado apellidos(String apellidos) {
        this.apellidos = apellidos;
        return this;
    }

    public Integer getPuestoId() {
        return puestoId;
    }

    public void setPuestoId(Integer puestoId) {
        this.puestoId = puestoId;
    }

    public Jurado puestoId(Integer puestoId) {
        this.puestoId = puestoId;
        return this;
    }

    public Instant getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Instant createdDate) {
        this.createdDate = createdDate;
    }

    public Jurado createdDate(Instant createdDate) {
        this.createdDate = createdDate;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Jurado)) {
            return false;
        }
        return id != null && id.equals(((Jurado) o).id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return (
            "Jurado{" +
            "id=" +
            id +
            ", tipoDocumento='" +
            tipoDocumento +
            "'" +
            ", numeroDocumento='" +
            numeroDocumento +
            "'" +
            ", nombres='" +
            nombres +
            "'" +
            ", apellidos='" +
            apellidos +
            "'" +
            ", puestoId=" +
            puestoId +
            "}"
        );
    }
}
