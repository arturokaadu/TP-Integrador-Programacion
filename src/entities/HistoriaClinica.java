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

public class HistoriaClinica {
    private Long id;
    private boolean eliminado;
    private String nroHistoria;       
    private TipoSangre grupoSanguineo; 
    private String antecedentes;      
    private String medicacionActual;  
    private String observaciones;      
    
    //public HistoriaClinica() {}
    
    public HistoriaClinica(String nroHistoria, TipoSangre grupoSanguineo) {
        this.nroHistoria = nroHistoria;
        this.grupoSanguineo = grupoSanguineo;
        this.eliminado = false;
    }

    // Getters y setters
    
}
