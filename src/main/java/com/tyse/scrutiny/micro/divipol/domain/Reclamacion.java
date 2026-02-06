package com.tyse.scrutiny.micro.divipol.domain;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.time.Instant;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

/**
 * Entidad Reclamacion - Reclamaciones electorales presentadas por testigos.
 *
 * Tipos de reclamación (Art. 165-168 Código Electoral):
 * - IRREGULARIDAD_MESA: Irregularidades en el proceso de votación
 * - EXCESO_VOTANTES: Votos superiores al potencial electoral
 * - ERROR_ARITMETICO: Errores en sumas o cómputos
 * - ERROR_NOMBRES: Discrepancias en nombres de candidatos
 * - FIRMAS_INSUFICIENTES: Falta de firmas requeridas en actas
 * - DISCREPANCIA_ACTAS: Diferencias entre actas E14/E24/E26
 * - OTRO: Otras causales de reclamación
 *
 * Estados:
 * - PRESENTADA: Reclamación registrada
 * - EN_REVISION: En proceso de análisis
 * - ACEPTADA: Reclamación aceptada y aplicada
 * - RECHAZADA: Reclamación desestimada
 */
@Table("reclamacion")
public class Reclamacion implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column("id")
    private Long id;

    @NotNull
    @Column("testigo_id")
    private Long testigoId;

    @NotNull
    @Size(max = 30)
    @Column("tipo_reclamacion")
    private String tipoReclamacion;

    @NotNull
    @Column("descripcion")
    private String descripcion;

    @NotNull
    @Size(max = 20)
    @Column("estado")
    private String estado;

    @Column("mesa_id")
    private Long mesaId;

    @Column("comision_id")
    private Long comisionId;

    @NotNull
    @Column("fecha_presentacion")
    private Instant fechaPresentacion;

    @Column("resolucion")
    private String resolucion;

    @Column("fecha_resolucion")
    private Instant fechaResolucion;

    @Size(max = 100)
    @Column("resuelta_por")
    private String resueltaPor;

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

    public Reclamacion id(Long id) {
        this.id = id;
        return this;
    }

    public Long getTestigoId() {
        return testigoId;
    }

    public void setTestigoId(Long testigoId) {
        this.testigoId = testigoId;
    }

    public Reclamacion testigoId(Long testigoId) {
        this.testigoId = testigoId;
        return this;
    }

    public String getTipoReclamacion() {
        return tipoReclamacion;
    }

    public void setTipoReclamacion(String tipoReclamacion) {
        this.tipoReclamacion = tipoReclamacion;
    }

    public Reclamacion tipoReclamacion(String tipoReclamacion) {
        this.tipoReclamacion = tipoReclamacion;
        return this;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Reclamacion descripcion(String descripcion) {
        this.descripcion = descripcion;
        return this;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public Reclamacion estado(String estado) {
        this.estado = estado;
        return this;
    }

    public Long getMesaId() {
        return mesaId;
    }

    public void setMesaId(Long mesaId) {
        this.mesaId = mesaId;
    }

    public Reclamacion mesaId(Long mesaId) {
        this.mesaId = mesaId;
        return this;
    }

    public Long getComisionId() {
        return comisionId;
    }

    public void setComisionId(Long comisionId) {
        this.comisionId = comisionId;
    }

    public Reclamacion comisionId(Long comisionId) {
        this.comisionId = comisionId;
        return this;
    }

    public Instant getFechaPresentacion() {
        return fechaPresentacion;
    }

    public void setFechaPresentacion(Instant fechaPresentacion) {
        this.fechaPresentacion = fechaPresentacion;
    }

    public Reclamacion fechaPresentacion(Instant fechaPresentacion) {
        this.fechaPresentacion = fechaPresentacion;
        return this;
    }

    public String getResolucion() {
        return resolucion;
    }

    public void setResolucion(String resolucion) {
        this.resolucion = resolucion;
    }

    public Reclamacion resolucion(String resolucion) {
        this.resolucion = resolucion;
        return this;
    }

    public Instant getFechaResolucion() {
        return fechaResolucion;
    }

    public void setFechaResolucion(Instant fechaResolucion) {
        this.fechaResolucion = fechaResolucion;
    }

    public Reclamacion fechaResolucion(Instant fechaResolucion) {
        this.fechaResolucion = fechaResolucion;
        return this;
    }

    public String getResueltaPor() {
        return resueltaPor;
    }

    public void setResueltaPor(String resueltaPor) {
        this.resueltaPor = resueltaPor;
    }

    public Reclamacion resueltaPor(String resueltaPor) {
        this.resueltaPor = resueltaPor;
        return this;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    public Reclamacion activo(Boolean activo) {
        this.activo = activo;
        return this;
    }

    public Instant getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Instant createdDate) {
        this.createdDate = createdDate;
    }

    public Reclamacion createdDate(Instant createdDate) {
        this.createdDate = createdDate;
        return this;
    }

    public Instant getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(Instant lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    public Reclamacion lastModifiedDate(Instant lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
        return this;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public Reclamacion createdBy(String createdBy) {
        this.createdBy = createdBy;
        return this;
    }

    public String getLastModifiedBy() {
        return lastModifiedBy;
    }

    public void setLastModifiedBy(String lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
    }

    public Reclamacion lastModifiedBy(String lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Reclamacion)) {
            return false;
        }
        return id != null && id.equals(((Reclamacion) o).id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return (
            "Reclamacion{" +
            "id=" +
            id +
            ", testigoId=" +
            testigoId +
            ", tipoReclamacion='" +
            tipoReclamacion +
            "'" +
            ", estado='" +
            estado +
            "'" +
            ", mesaId=" +
            mesaId +
            ", comisionId=" +
            comisionId +
            ", activo=" +
            activo +
            "}"
        );
    }
}
