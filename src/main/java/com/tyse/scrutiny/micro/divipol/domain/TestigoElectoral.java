package com.tyse.scrutiny.micro.divipol.domain;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.time.Instant;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

/**
 * Entidad TestigoElectoral - Testigos electorales (personas externas al sistema).
 * Pueden ser asignados a múltiples puestos de votación.
 */
@Table("testigo_electoral")
public class TestigoElectoral implements Serializable {

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

    @Size(max = 20)
    @Column("telefono")
    private String telefono;

    @Size(max = 100)
    @Column("email")
    private String email;

    @Column("organizacion_id")
    private Long organizacionId;

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

    public TestigoElectoral id(Long id) {
        this.id = id;
        return this;
    }

    public String getTipoDocumento() {
        return tipoDocumento;
    }

    public void setTipoDocumento(String tipoDocumento) {
        this.tipoDocumento = tipoDocumento;
    }

    public TestigoElectoral tipoDocumento(String tipoDocumento) {
        this.tipoDocumento = tipoDocumento;
        return this;
    }

    public String getNumeroDocumento() {
        return numeroDocumento;
    }

    public void setNumeroDocumento(String numeroDocumento) {
        this.numeroDocumento = numeroDocumento;
    }

    public TestigoElectoral numeroDocumento(String numeroDocumento) {
        this.numeroDocumento = numeroDocumento;
        return this;
    }

    public String getNombres() {
        return nombres;
    }

    public void setNombres(String nombres) {
        this.nombres = nombres;
    }

    public TestigoElectoral nombres(String nombres) {
        this.nombres = nombres;
        return this;
    }

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    public TestigoElectoral apellidos(String apellidos) {
        this.apellidos = apellidos;
        return this;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public TestigoElectoral telefono(String telefono) {
        this.telefono = telefono;
        return this;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public TestigoElectoral email(String email) {
        this.email = email;
        return this;
    }

    public Long getOrganizacionId() {
        return organizacionId;
    }

    public void setOrganizacionId(Long organizacionId) {
        this.organizacionId = organizacionId;
    }

    public TestigoElectoral organizacionId(Long organizacionId) {
        this.organizacionId = organizacionId;
        return this;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    public TestigoElectoral activo(Boolean activo) {
        this.activo = activo;
        return this;
    }

    public Instant getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Instant createdDate) {
        this.createdDate = createdDate;
    }

    public TestigoElectoral createdDate(Instant createdDate) {
        this.createdDate = createdDate;
        return this;
    }

    public Instant getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(Instant lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    public TestigoElectoral lastModifiedDate(Instant lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
        return this;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public TestigoElectoral createdBy(String createdBy) {
        this.createdBy = createdBy;
        return this;
    }

    public String getLastModifiedBy() {
        return lastModifiedBy;
    }

    public void setLastModifiedBy(String lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
    }

    public TestigoElectoral lastModifiedBy(String lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof TestigoElectoral)) {
            return false;
        }
        return id != null && id.equals(((TestigoElectoral) o).id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return (
            "TestigoElectoral{" +
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
            ", telefono='" +
            telefono +
            "'" +
            ", email='" +
            email +
            "'" +
            ", organizacionId=" +
            organizacionId +
            ", activo=" +
            activo +
            "}"
        );
    }
}
