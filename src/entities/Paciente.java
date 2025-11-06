/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package entities;

/**
 *
 * @author artur
 */

import java.time.LocalDate;

public class Paciente {
    private Long id;
    private boolean eliminado;
    private String nombre;            
    private String apellido;         
    private String dni;             
    private LocalDate fechaNacimiento;
    private HistoriaClinica historiaClinica; // 1 a 1 unidireccional
    
   // public Paciente() {}
    
    public Paciente(String nombre, String apellido, String dni) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.dni = dni;
        this.eliminado = false;
    }
    
    
    // Getters y setters
    
}
