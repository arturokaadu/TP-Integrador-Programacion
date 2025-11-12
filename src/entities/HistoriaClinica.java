/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package entities;

import java.util.Objects;

/**
 *
 * @author belenyardebuller
 */
/**
 * Entidad B: HistoriaClinica Tabla: historia_clinica
 */
public class HistoriaClinica extends Base {

    private String nroHistoria;
    private TipoSangre grupoSanguineo;
    private String antecedentes;
    private String medicacionActual;
    private String observaciones;

    /**
     * Constructor completo para reconstruir una HistoriaClinica desde la BD
     *
     * @param id
     * @param eliminado
     * @param nroHistoria
     * @param grupoSanguineo
     * @param antecedentes
     * @param medicacionActual
     * @param observaciones
     */
    public HistoriaClinica(long id, boolean eliminado, String nroHistoria, TipoSangre grupoSanguineo, String antecedentes, String medicacionActual, String observaciones) {
        super(id, eliminado);
        this.nroHistoria = nroHistoria;
        this.grupoSanguineo = Objects.requireNonNull(grupoSanguineo, "grupoSanguineo es obligatorio"); // NOT NULL en BD
        this.antecedentes = antecedentes;
        this.medicacionActual = medicacionActual;
        this.observaciones = observaciones;
    }

    /**
     * Constructor para alta rápida
     *
     * @param nroHistoria
     * @param grupoSanguineo
     */
    public HistoriaClinica(String nroHistoria, TipoSangre grupoSanguineo) {
        super();
        this.nroHistoria = nroHistoria;
        this.grupoSanguineo = Objects.requireNonNull(grupoSanguineo, "grupoSanguineo es obligatorio"); // NOT NULL en BD
    }

    /**
     * Constructor por defecto para crear una nueva HistoriaClinica sin id
     *
     */
    public HistoriaClinica() {
        super();
    }

    /**
     * Obtiene el número de la Historia Clínica
     *
     * @return nroHistoria
     */
    public String getNroHistoria() {
        return nroHistoria;
    }

    /**
     * Establece el número de Historia Clínica
     *
     * @param nroHistoria
     */
    public void setNroHistoria(String nroHistoria) {
        this.nroHistoria = nroHistoria;
    }

    /**
     * Obtiene el grupo sanguíneo de la Historia Clínica
     *
     * @return grupoSanguineo
     */
    public TipoSangre getGrupoSanguineo() {
        return grupoSanguineo;
    }

    /**
     * Establece el grupo sanguíneo de la Historia Clínica
     *
     * @param grupoSanguineo
     */
    public void setGrupoSanguineo(TipoSangre grupoSanguineo) {
        this.grupoSanguineo = grupoSanguineo;
    }

    /**
     * Obtiene los antecedentes de la Historia Clínica
     *
     * @return antecedentes
     */
    public String getAntecedentes() {
        return antecedentes;
    }

    /**
     * Establece antecedentes de la Historia Clínica
     *
     * @param antecedentes
     */
    public void setAntecedentes(String antecedentes) {
        this.antecedentes = antecedentes;
    }

    /**
     * Obtiene la medicación actual de la Historia Clínica
     *
     * @return medicacionActual
     */
    public String getMedicacionActual() {
        return medicacionActual;
    }

    /**
     * Establece medicación actual de la Historia Clínica
     *
     * @param medicacionActual
     */
    public void setMedicacionActual(String medicacionActual) {
        this.medicacionActual = medicacionActual;
    }

    /**
     * Obtiene las observaciones de la Historia Clínica
     *
     * @return observaciones
     */
    public String getObservaciones() {
        return observaciones;
    }

    /**
     * Establece observaciones de la Historia Clínica
     *
     * @param observaciones
     */
    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    @Override
    public String toString() {
        return "HistoriaClinica{"
                + "id=" + getId()
                + ", nroHistoria='" + nroHistoria + '\''
                + ", grupoSanguineo=" + grupoSanguineo
                + ", eliminado=" + isEliminado()
                + '}';
    }
}
