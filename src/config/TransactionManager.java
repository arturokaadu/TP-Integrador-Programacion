/*
 * Clase utilitaria para manejar transacciones JDBC y conexiones por Hilo (ThreadLocal).
 *
 * Utiliza ThreadLocal<Connection> para asegurar que la misma conexión sea usada
 * por todas las operaciones (DAO) dentro del mismo hilo/transacción de servicio.
 */
package config;

import java.sql.Connection;
import java.sql.SQLException;

public final class TransactionManager {

    // Almacena una conexión por cada hilo de ejecución.
    private static final ThreadLocal<Connection> THREAD_LOCAL_CONNECTION = new ThreadLocal<>();

    /**
     * Constructor privado para evitar la instanciación de la clase utilitaria.
     */
    private TransactionManager() {
        throw new UnsupportedOperationException("Esta clase no se puede instanciar.");
    }

    /**
     * Inicia una nueva transacción:
     * 1. Obtiene una nueva conexión.
     * 2. Desactiva el modo AutoCommit.
     * 3. Almacena la conexión en el ThreadLocal.
     *
     * @throws SQLException Si ocurre un error al obtener o configurar la conexión.
     * @throws IllegalStateException Si ya existe una transacción activa en este hilo.
     */
    public static void startTransaction() throws SQLException {
        if (isTransactionActive()) {
            throw new IllegalStateException("Ya existe una transacción activa en este hilo.");
        }
        
        // 1. Obtener nueva conexión
        Connection connection = DatabaseConnection.getConnection();
        
        // 2. Desactivar AutoCommit
        connection.setAutoCommit(false);
        
        // 3. Almacenar en el ThreadLocal
        THREAD_LOCAL_CONNECTION.set(connection);
    }

    /**
     * Confirma la transacción y libera los recursos (cierra la conexión).
     *
     * @throws SQLException Si ocurre un error al hacer commit o al cerrar la conexión.
     * @throws IllegalStateException Si no hay una transacción activa.
     */
    public static void commit() throws SQLException {
        Connection connection = getConnection(); // Reutilizamos getConnection que valida el estado
        
        try {
            connection.commit();
        } finally {
            closeAndRemoveConnection(connection);
        }
    }

    /**
     * Revierte la transacción (rollback) y libera los recursos (cierra la conexión).
     *
     * @throws SQLException Si ocurre un error al hacer rollback o al cerrar la conexión.
     * @throws IllegalStateException Si no hay una transacción activa.
     */
    public static void rollback() throws SQLException {
        Connection connection = getConnection(); // Reutilizamos getConnection que valida el estado
        
        try {
            connection.rollback();
        } finally {
            closeAndRemoveConnection(connection);
        }
    }

    /**
     * Cierra la conexión y la remueve del ThreadLocal.
     *
     * @param connection La conexión a cerrar.
     */
    private static void closeAndRemoveConnection(Connection connection) {
        try {
            if (connection != null) {
                // Volver a activar AutoCommit antes de cerrar, como buena práctica
                connection.setAutoCommit(true); 
                connection.close();
            }
        } catch (SQLException e) {
            // Manejo de error al cerrar la conexión, solo logueamos, no relanzamos.
            System.err.println("Advertencia: Error al cerrar la conexión en TransactionManager: " + e.getMessage());
        } finally {
            // Remover la conexión del ThreadLocal para evitar fugas de memoria
            THREAD_LOCAL_CONNECTION.remove();
        }
    }

    /**
     * Retorna la conexión activa en este hilo de ejecución (dentro de una transacción)
     * o una nueva conexión con AutoCommit activado (si no hay transacción).
     *
     * IMPORTANTE: Si se llama fuera de una transacción, retorna una conexión estándar, 
     * pero esa conexión DEBE ser cerrada manualmente o con try-with-resources.
     *
     * @return La conexión JDBC.
     * @throws SQLException Si ocurre un error al obtener una nueva conexión.
     * @throws IllegalStateException Si no hay una transacción activa Y se exige su existencia.
     */
    public static Connection getConnection() throws SQLException {
        Connection connection = THREAD_LOCAL_CONNECTION.get();
        if (connection == null) {
            // Si no hay transacción activa, retorna una conexión estándar 
            // con AutoCommit=true, que DEBE ser cerrada por el llamador.
            return DatabaseConnection.getConnection();
        }
        return connection;
    }
    
    /**
     * Retorna la conexión activa. Este método es usado por los DAOs.
     * * @param required Indica si es obligatorio que exista una transacción activa.
     * @return La conexión JDBC.
     * @throws SQLException Si ocurre un error al obtener una nueva conexión.
     * @throws IllegalStateException Si required es true y no hay una transacción activa.
     */
    public static Connection getConnection(boolean required) throws SQLException {
         Connection connection = THREAD_LOCAL_CONNECTION.get();
        if (connection == null) {
            if (required) {
                // Si la conexión es requerida, lanzamos excepción.
                throw new IllegalStateException("No hay una transacción activa. Los DAOs deben ser llamados desde un Service.");
            }
            // Si no es requerida, devolvemos una nueva conexión auto-cerrable.
            return DatabaseConnection.getConnection();
        }
        return connection;
    }
    
     /**
     * Verifica si hay una transacción activa en el hilo actual.
     * * @return true si hay una conexión en el ThreadLocal, false en caso contrario.
     */
    public static boolean isTransactionActive() {
        return THREAD_LOCAL_CONNECTION.get() != null;
    }

}