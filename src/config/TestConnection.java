/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package config;

/**
 *
 * @author artur
 */
import java.sql.Connection;
// Prueba si DatabaseConnection funciona
public class TestConnection {
    public static void main(String[] args) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            System.out.println("Conexión exitosa: " + (conn != null && !conn.isClosed()));
        } catch (Exception e) {
            System.err.println("Error al conectarse: " + e.getMessage());
        }
    }
}
