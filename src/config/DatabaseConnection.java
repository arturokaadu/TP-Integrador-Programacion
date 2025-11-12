/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package config;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Connection;

/**
 *
 * @author belenyardebuller
 */
/**
 * Clase utilitaria para conexión a la base de datos "tfi_bd1"
 *
 * Contiene un método estático que retorna java.sql.Connection
 *
 * Configuración por defecto: - URL: jdbc:mysql://localhost:3306/tfi_bd1
 * - Usuario: dev - Contraseña: Grupo54Dev
 *
 */
public final class DatabaseConnection {

    /**
     * URL de conexión JDBC
     */
    private static final String URL = System.getProperty("db.url", "jdbc:mysql://localhost:3306/tfi_bd1");

    /**
     * Usuario de la base de datos: rol developer
     */
    private static final String USER = System.getProperty("db.user", "dev");

    /**
     * Contraseña del usuario
     */
    private static final String PASSWORD = System.getProperty("db.password", "Grupo54Dev");

    /**
     * Bloque static para inicializar la conexión
     *
     * Acciones: 1. Carga el driver JDBC de MySQL 2. Valida que la configuración
     * sea correcta
     *
     * Si falla, lanza ExceptionInInitializerError y detiene la aplicación
     */
    static {
        try {
            // Carga explícita del driver JDBC
            Class.forName("com.mysql.cj.jdbc.Driver");

            // Validación de la configuración 
            setupValidation();
        } catch (ClassNotFoundException e) {
            // Se captura error en la carga del driver JDBC
            throw new ExceptionInInitializerError("Error en la carga de driver JDBC: " + e.getMessage());
        } catch (IllegalStateException e) {
            // Se captura el error disparado desde la función de validación setupValidation()
            throw new ExceptionInInitializerError("Error en la configuración de la base de datos: " + e.getMessage());
        }
    }

    /**
     * Constructor privado, no se puede instanciar
     */
    private DatabaseConnection() {
        throw new UnsupportedOperationException("Esta clase no se puede instanciar.");
    }

    /**
     * Retorna una nueva conexión a la base de datos.
     *
     * No hay pooling, cada llamada crea una nueva conexión. 
     * 
     * Se debe usar try-with-resources para cerrar automáticamente la conexión
     *
     * @return Conexión JDBC activa
     * @throws SQLException Si no se puede establecer la conexión
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    /**
     * Valida que los parámetros de configuración URL, USER y PASSWORD sean
     * válidos
     *
     * Reglas:URL, PASSWORD y USER no pueden ser null ni estar vacíos
     *
     * @throws IllegalStateException Si la configuración es inválida
     */
    private static void setupValidation() {
        if (URL == null || URL.trim().isEmpty()) {
            throw new IllegalStateException("URL inválida o vacía. No se ha podido validar el setup.");
        }
        if (USER == null || USER.trim().isEmpty()) {
            throw new IllegalStateException("Usuario inválido o vacío. No se ha podido validar el setup.");
        }
        if (PASSWORD == null || USER.trim().isEmpty()) {
            throw new IllegalStateException("Contraseña inválida o vacía. No se ha podido validar el setup.");
        }
    }
}
