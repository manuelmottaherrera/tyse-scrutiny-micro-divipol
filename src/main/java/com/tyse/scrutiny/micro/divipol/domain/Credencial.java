package com.tyse.scrutiny.micro.divipol.domain;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.time.Instant;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

/**
 * Entidad Credencial - Credenciales electorales para testigos.
 *
 * Tipos de credencial:
 * - E15: Credencial para testigo de mesa (Art. 108 Código Electoral)
 * - E16: Credencial para testigo de comisión escrutadora (Art. 163)
 *
 * Estados:
 * - PENDIENTE: Credencial registrada pero no emitida
 * - EMITIDA: Credencial generada y lista para entrega
 * - ENTREGADA: Credencial entregada al testigo
 * - ANULADA: Credencial invalidada
 */
@Table("credencial")
public class Credencial implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column("id")
    private Long id;

    @NotNull
    @Column("testigo_id")
    private Long testigoId;

    @NotNull
    @Size(max = 10)
    @Column("tipo")
    private String tipo;

    @NotNull
    @Size(max = 20)
    @Column("estado")
    private String estado;

    @Size(max = 100)
    @Column("codigo_verificacion")
    private String codigoVerificacion;

    @Column("testigo_mesa_id")
    private Long testigoMesaId;

    @Column("testigo_comision_id")
    private Long testigoComisionId;

    @Column("fecha_emision")
    private Instant fechaEmision;

    @Column("fecha_entrega")
    private Instant fechaEntrega;

    @Size(max = 100)
    @Column("emitido_por")
    private String emitidoPor;

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

    public Credencial id(Long id) {
        this.id = id;
        return this;
    }

    public Long getTestigoId() {
        return testigoId;
    }

    public void setTestigoId(Long testigoId) {
        this.testigoId = testigoId;
    }

    public Credencial testigoId(Long testigoId) {
        this.testigoId = testigoId;
        return this;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public Credencial tipo(String tipo) {
        this.tipo = tipo;
        return this;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public Credencial estado(String estado) {
        this.estado = estado;
        return this;
    }

    public String getCodigoVerificacion() {
        return codigoVerificacion;
    }

    public void setCodigoVerificacion(String codigoVerificacion) {
        this.codigoVerificacion = codigoVerificacion;
    }

    public Credencial codigoVerificacion(String codigoVerificacion) {
        this.codigoVerificacion = codigoVerificacion;
        return this;
    }

    public Long getTestigoMesaId() {
        return testigoMesaId;
    }

    public void setTestigoMesaId(Long testigoMesaId) {
        this.testigoMesaId = testigoMesaId;
    }

    public Credencial testigoMesaId(Long testigoMesaId) {
        this.testigoMesaId = testigoMesaId;
        return this;
    }

    public Long getTestigoComisionId() {
        return testigoComisionId;
    }

    public void setTestigoComisionId(Long testigoComisionId) {
        this.testigoComisionId = testigoComisionId;
    }

    public Credencial testigoComisionId(Long testigoComisionId) {
        this.testigoComisionId = testigoComisionId;
        return this;
    }

    public Instant getFechaEmision() {
        return fechaEmision;
    }

    public void setFechaEmision(Instant fechaEmision) {
        this.fechaEmision = fechaEmision;
    }

    public Credencial fechaEmision(Instant fechaEmision) {
        this.fechaEmision = fechaEmision;
        return this;
    }

    public Instant getFechaEntrega() {
        return fechaEntrega;
    }

    public void setFechaEntrega(Instant fechaEntrega) {
        this.fechaEntrega = fechaEntrega;
    }

    public Credencial fechaEntrega(Instant fechaEntrega) {
        this.fechaEntrega = fechaEntrega;
        return this;
    }

    public String getEmitidoPor() {
        return emitidoPor;
    }

    public void setEmitidoPor(String emitidoPor) {
        this.emitidoPor = emitidoPor;
    }

    public Credencial emitidoPor(String emitidoPor) {
        this.emitidoPor = emitidoPor;
        return this;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    public Credencial activo(Boolean activo) {
        this.activo = activo;
        return this;
    }

    public Instant getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Instant createdDate) {
        this.createdDate = createdDate;
    }

    public Credencial createdDate(Instant createdDate) {
        this.createdDate = createdDate;
        return this;
    }

    public Instant getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(Instant lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    public Credencial lastModifiedDate(Instant lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
        return this;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public Credencial createdBy(String createdBy) {
        this.createdBy = createdBy;
        return this;
    }

    public String getLastModifiedBy() {
        return lastModifiedBy;
    }

    public void setLastModifiedBy(String lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
    }

    public Credencial lastModifiedBy(String lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Credencial)) {
            return false;
        }
        return id != null && id.equals(((Credencial) o).id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return (
            "Credencial{" +
            "id=" +
            id +
            ", testigoId=" +
            testigoId +
            ", tipo='" +
            tipo +
            "'" +
            ", estado='" +
            estado +
            "'" +
            ", codigoVerificacion='" +
            codigoVerificacion +
            "'" +
            ", testigoMesaId=" +
            testigoMesaId +
            ", testigoComisionId=" +
            testigoComisionId +
            ", activo=" +
            activo +
            "}"
        );
    }
}
