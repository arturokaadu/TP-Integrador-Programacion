/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author A-monardes
 */
package main;
//Clase principal que inicia la aplicación del Sistema de Gestión Hospitalaria.
public class Main {
    
    /**
     * Método principal - punto de entrada de la aplicación
     * @param args Argumentos de línea de comandos (no utilizados)
     */
    public static void main(String[] args) {
        try {
            System.out.println("🚀 Iniciando Sistema de Gestión Hospitalaria...");
            AppMenu appMenu = new AppMenu();
            appMenu.iniciar();
            
        } catch (Exception e) {
            System.err.println("❌ Error crítico al iniciar la aplicación: " + e.getMessage());
            e.printStackTrace();
            System.out.println("Presione Enter para salir...");
            try {
                System.in.read();
            } catch (Exception ex) {
            }
        }
    }
}
