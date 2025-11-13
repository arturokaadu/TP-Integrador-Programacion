/*
 * Clase utilitaria para manejar las transacciones en la base de datos.
 *
 * Por ejemplo, en una Transaccion como una "sesión de trabajo segura" con la DB.
 * queremos que todas las operaciones (ej: guardar Paciente Y guardar HistoriaClínica)
 * se hagan con la misma conexion, como si fuera un solo paso.
 *
 * Para eso usamos un ThreadLocal<Connection>, que es como un "bolsillo"
 * privado para cada hilo, asegurando que nadie más toque esa conexión.
 */

package config;

import java.sql.Connection;
import java.sql.SQLException;

public final class TransactionManager {

    // "Bolsillo" privado por hilo de ejecución, guarda la conexión activa
    private static final ThreadLocal<Connection> THREAD_LOCAL_CONNECTION = new ThreadLocal<>();

    // Constructor privado (no se instancia)
    private TransactionManager() {
        throw new UnsupportedOperationException("Esta clase no se puede instanciar.");
    }

    /**
     * Arranca una transacción.
     * 1. Obtiene una nueva conexión.
     * 2. Desactiva el auto-commit.
     * 3. Guarda la conexión en el ThreadLocal.
     */
    public static void startTransaction() throws SQLException {
        if (isTransactionActive()) {
            throw new IllegalStateException("Ya existe una transacción activa en este hilo.");
        }

        Connection connection = DatabaseConnection.getConnection();
        connection.setAutoCommit(false);
        THREAD_LOCAL_CONNECTION.set(connection);
        System.out.println("[DEBUG] Transacción iniciada.");
    }

    /**
     * Confirma los cambios (commit) y cierra la conexión.
     */
    public static void commit() throws SQLException {
        Connection connection = getConnection();
        try {
            connection.commit();
            System.out.println("[DEBUG] Commit realizado correctamente.");
        } finally {
            closeAndRemoveConnection(connection);
        }
    }

    /**
     * Revierte los cambios (rollback) y cierra la conexión.
     */
    public static void rollback() throws SQLException {
        Connection connection = getConnection();
        try {
            connection.rollback();
            System.out.println("[DEBUG] Rollback realizado correctamente.");
        } finally {
            closeAndRemoveConnection(connection);
        }
    }

    /**
     * Cierra la conexión y limpia el ThreadLocal.
     */
    private static void closeAndRemoveConnection(Connection connection) {
        try {
            if (connection != null) {
                connection.setAutoCommit(true);
                connection.close();
                System.out.println("[DEBUG] Conexión cerrada y liberada.");
            }
        } catch (SQLException e) {
            System.err.println("Advertencia: No se pudo cerrar la conexión: " + e.getMessage());
        } finally {
            THREAD_LOCAL_CONNECTION.remove();
        }
    }

    /**
     * Retorna la conexión actual.
     * Si hay una transacción activa, devuelve la compartida.
     * Si no hay, crea una nueva (que deberá cerrarse manualmente).
     */
    public static Connection getConnection() throws SQLException {
        Connection connection = THREAD_LOCAL_CONNECTION.get();
        if (connection == null) {
            return DatabaseConnection.getConnection();
        }
        return connection;
    }

    /**
     * Versión de getConnection con control obligatorio.
     */
    public static Connection getConnection(boolean required) throws SQLException {
        Connection connection = THREAD_LOCAL_CONNECTION.get();
        if (connection == null) {
            if (required) {
                throw new IllegalStateException("No hay una transacción activa. El Service debe iniciar una antes de llamar a los DAO.");
            }
            return DatabaseConnection.getConnection();
        }
        return connection;
    }

    /**
     * Indica si hay una transacción activa en este hilo.
     */
    public static boolean isTransactionActive() {
        return THREAD_LOCAL_CONNECTION.get() != null;
    }
}
