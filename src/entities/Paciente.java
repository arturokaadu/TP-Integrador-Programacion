/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package entities;

import java.time.LocalDate;
import java.util.Objects;

/**
 *
 * @author belenyardebuller
 */
/**
 * Entidad A: Paciente Contiene referencia unidireccional 1->1 a
 * HistoriaClinica. Tabla: paciente
 */
public class Paciente extends Base {

    private String nombre;
    private String apellido;
    private String dni;
    private LocalDate fechaNacimiento;
    private HistoriaClinica historiaClinica; // Relación 1->1 unidireccional

    /**
     * Constructor completo para reconstruir un Paciente desde la BD
     *
     * @param id
     * @param eliminado
     * @param nombre
     * @param apellido
     * @param dni
     * @param fechaNacimiento
     */
    public Paciente(long id, boolean eliminado, String nombre,
            String apellido, String dni, LocalDate fechaNacimiento) {
        super(id, eliminado);
        this.nombre = Objects.requireNonNull(nombre, "parámetro nombre es obligatorio");
        this.apellido = Objects.requireNonNull(apellido, "parámetro apellido es obligatorio");
        this.dni = Objects.requireNonNull(dni, "parámetro dni es obligatorio");
        this.fechaNacimiento = fechaNacimiento;
    }

    /**
     * Constructor por defecto para crear un nuevo Paciente sin id
     *
     */
    public Paciente() {
        super();
    }

    /**
     * Obtiene el nombre del Paciente
     *
     * @return nombre
     */
    public String getNombre() {
        return nombre;
    }

    /**
     * Establece el nombre del Paciente
     *
     * @param nombre
     */
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    /**
     * Obtiene el apellido del Paciente
     *
     * @return apellido
     */
    public String getApellido() {
        return apellido;
    }

    /**
     * Establece el apellido del Paciente
     *
     * @param apellido
     */
    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    /**
     * Obtiene el dni del Paciente
     *
     * @return dni
     */
    public String getDni() {
        return dni;
    }

    /**
     * Establece el dni del Paciente
     *
     * @param dni
     */
    public void setDni(String dni) {
        this.dni = dni;
    }

    /**
     * Obtiene la fecha de nacimiento del Paciente
     *
     * @return fechaNacimiento
     */
    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }

    /**
     * Establece la fecha de nacimiento del Paciente
     *
     * @param fechaNacimiento
     */
    public void setFechaNacimiento(LocalDate fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }

    /**
     * Obtiene la HistoriaClinica del Paciente
     *
     * @return historiaClinica
     */
    public HistoriaClinica getHistoriaClinica() {
        return historiaClinica;
    }

    /**
     * Asocia o desasocia una HistoriaClinica al Paciente.
     *
     * @param historiaClinica
     */
    public void setHistoriaClinica(HistoriaClinica historiaClinica) {
        // Se implementa la asociación unidireccional mediante un setter
        this.historiaClinica = historiaClinica;
    }

    @Override
    public String toString() {
        return "Paciente{"
                + "id=" + getId()
                + ", nombre='" + nombre + '\''
                + ", apellido='" + apellido + '\''
                + ", dni='" + dni + '\''
                + ", fechaNacimiento=" + fechaNacimiento
                + ", historiaClinica=" + (historiaClinica != null ? "HC#" + historiaClinica.getId() : "null")
                + ", eliminado=" + isEliminado()
                + '}';
    }

    /**
     * Igualdad semántica por DNI. Compara si dos personas son iguales según el
     * número de DNI.
     *
     * @param o
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Paciente)) {
            return false;
        }
        Paciente paciente = (Paciente) o;
        return Objects.equals(dni, paciente.dni);
    }

    /**
     * Hash code basado en DNI. Consistente con equals(): personas con mismo DNI
     * tienen mismo hash.
     *
     * @return Objects.hash(dni)
     */
    @Override
    public int hashCode() {
        return Objects.hash(dni);
    }
}
