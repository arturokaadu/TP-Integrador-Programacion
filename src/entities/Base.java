/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package entities;

/**
 *
 * @author belenyardebuller
 */
public abstract class Base {

    // Identificador único (BIGINT en la BD)
    private long id;

    // Indicador de eliminación lógica.
    private boolean eliminado;

    /**
     * Constructor completo con todos los campos
     *
     * @param id
     * @param eliminado
     */
    protected Base(long id, boolean eliminado) {
        this.id = id;
        this.eliminado = eliminado;
    }

    /**
     * Constructor por defecto: inicializa una entidad sin id ya que será
     * asignado por la BD
     */
    protected Base() {
        this.eliminado = false;
    }

    /**
     * Obtiene el id de la entidad
     *
     * @return id
     */
    public long getId() {
        return id;
    }

    /**
     * Establece el id de la entidad
     *
     * @param id
     */
    public void setId(long id) {
        this.id = id;
    }

    /**
     * Verifica el estado de eliminación de la entidad
     *
     * @return true si está eliminada, false si está activa
     */
    public boolean isEliminado() {
        return eliminado;
    }

    /**
     * Establece el estado de eliminación de la entidad
     *
     * @param eliminado true para marcar como eliminada, false para reactivar
     */
    public void setEliminado(boolean eliminado) {
        this.eliminado = eliminado;
    }
}
