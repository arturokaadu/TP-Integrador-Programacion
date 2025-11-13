/*
 * Clase "utilitaria" para manejar las transacciones en la base de datos.
 *
 * Piensa en una Transacción como una "Sesión de Trabajo Segura" en la DB.
 * Queremos que todas las operaciones (ej: guardar Paciente Y guardar HistoriaClínica)
 * se hagan con la MISMA conexión, como si fueran un solo paso.
 *
 * Para lograr esto, usamos ThreadLocal<Connection>, que es como un "bolsillo"
 * privado que cada hilo de ejecución (tu código haciendo una tarea) tiene para guardar
 * su conexión de DB y asegurarse de que nadie más la toque.
 */
package config;

import java.sql.Connection;
import java.sql.SQLException;

public final class TransactionManager {

    // Este es el "bolsillo" (ThreadLocal) que guarda la conexión de DB activa.
    // Una conexión para cada tarea (hilo) que esté corriendo.
    private static final ThreadLocal<Connection> THREAD_LOCAL_CONNECTION = new ThreadLocal<>();

    /**
     * Constructor privado, porque esta es una clase de utilidades (estática),
     * no queremos que nadie cree objetos de ella.
     */
    private TransactionManager() {
        throw new UnsupportedOperationException("Esta clase no se puede instanciar.");
    }

    /**
     * ¡Empezamos la Transacción! Es el Service el que llama a esto al principio de una operación grande.
     * 1. Pedimos una conexión nueva a la DB.
     * 2. Le decimos a esa conexión: "¡No guardes nada automáticamente! Espera mi permiso." (AutoCommit = false).
     * 3. Guardamos esa conexión en el "bolsillo" (ThreadLocal) para que los DAOs la encuentren.
     *
     * @throws SQLException Si hay quilombo al obtener o configurar la conexión.
     * @throws IllegalStateException Si ya hay una transacción activa (no podés empezar dos a la vez).
     */
    public static void startTransaction() throws SQLException {
        if (isTransactionActive()) {
            throw new IllegalStateException("Ya existe una transacción activa en este hilo. Ojo con llamar startTransaction dos veces.");
        }
        
        // 1. Obtener nueva conexión
        Connection connection = DatabaseConnection.getConnection();
        
        // 2. Desactivar AutoCommit (modo seguro: la DB espera el commit o rollback)
        connection.setAutoCommit(false);
        
        // 3. Almacenar en el ThreadLocal. ¡Ahora es la conexión oficial de esta tarea!
        THREAD_LOCAL_CONNECTION.set(connection);
    }

    /**
     * ¡Éxito! Confirmamos la transacción (Commit).
     * Todo lo que los DAOs hicieron (inserts, updates) se aplica definitivamente a la DB.
     * Luego, cerramos la conexión de forma limpia.
     *
     * @throws SQLException Si explota al hacer el commit o al cerrar.
     * @throws IllegalStateException Si llamas a esto sin haber llamado a startTransaction antes.
     */
    public static void commit() throws SQLException {
        Connection connection = getConnection(); // Obtenemos la conexión (y valida que exista)
        
        try {
            connection.commit(); // Aplicar cambios de forma permanente
        } finally {
            closeAndRemoveConnection(connection);
        }
    }

    /**
     * ¡Fallo! Revertimos la transacción (Rollback).
     * Esto se ejecuta si algo salió mal (ej: intentaste insertar un DNI repetido o no se pudo guardar la Historia Clínica).
     * La DB vuelve a estar **exactamente como estaba** antes de llamar a startTransaction.
     * Luego, cerramos la conexión de forma limpia.
     *
     * @throws SQLException Si explota al hacer el rollback o al cerrar.
     * @throws IllegalStateException Si llamas a esto sin haber llamado a startTransaction antes.
     */
    public static void rollback() throws SQLException {
        Connection connection = getConnection(); // Obtenemos la conexión (y valida que exista)
        
        try {
            connection.rollback(); // Deshacer todos los cambios de esta transacción
        } finally {
            closeAndRemoveConnection(connection);
        }
    }

    /**
     * El trabajo sucio: Cierra la conexión de la DB y la saca de nuestro "bolsillo" ThreadLocal.
     * Esto es clave para que no queden conexiones abiertas ni se mezclen con futuras tareas.
     *
     * @param connection La conexión a cerrar.
     */
    private static void closeAndRemoveConnection(Connection connection) {
        try {
            if (connection != null) {
                // Devolverle el control de AutoCommit a la conexión (buena práctica)
                connection.setAutoCommit(true); 
                connection.close();
            }
        } catch (SQLException e) {
            // Si falla al cerrar, solo avisamos por consola. No es crítico para la transacción.
            System.err.println("Advertencia: No se pudo cerrar la conexión de TransactionManager: " + e.getMessage());
        } finally {
            // ¡IMPORTANTE! Sacar la conexión del ThreadLocal para liberar la memoria.
            THREAD_LOCAL_CONNECTION.remove();
        }
    }

    /**
     * Retorna la conexión. Se usa cuando NO estamos obligados a estar en una transacción.
     * Si hay una transacción activa, devuelve la conexión compartida.
     * Si NO hay, pide una nueva conexión estándar (AutoCommit = true).
     *
     * ⚠️ ATENCIÓN: Si devuelve una conexión nueva (no compartida), EL CÓDIGO QUE LA LLAMA DEBE CERRARLA.
     *
     * @return La conexión JDBC (compartida o nueva).
     * @throws SQLException Si explota al obtener la conexión.
     */
    public static Connection getConnection() throws SQLException {
        Connection connection = THREAD_LOCAL_CONNECTION.get();
        if (connection == null) {
            // No hay transacción, dame una conexión normal (el DAO tendrá que cerrarla)
            return DatabaseConnection.getConnection();
        }
        return connection;
    }
    
    /**
     * Retorna la conexión. Este es el método que usan los DAOs cuando saben que tienen que
     * trabajar DENTRO de una transacción (ej: cuando el Service los llama para insertar o modificar).
     *
     * @param required Si es 'true', obligamos a que haya una transacción activa.
     * @return La conexión JDBC.
     * @throws IllegalStateException Si 'required' es true y el Service no llamó a startTransaction.
     */
    public static Connection getConnection(boolean required) throws SQLException {
        Connection connection = THREAD_LOCAL_CONNECTION.get();
        if (connection == null) {
            if (required) {
                // ¡Ups! Alguien llamó al DAO sin iniciar la transacción antes. Error de diseño.
                throw new IllegalStateException("No hay una transacción activa. Los DAOs CRUD deben ser llamados desde un Service.");
            }
            // Si no es requerida, devolvemos una conexión normal (para métodos de lectura, por ejemplo).
            return DatabaseConnection.getConnection();
        }
        return connection;
    }
    
    /**
     * ¿Estamos en el medio de una transacción?
     * @return true si la conexión está en el "bolsillo" (ThreadLocal), false si no.
     */
    public static boolean isTransactionActive() {
        return THREAD_LOCAL_CONNECTION.get() != null;
    }

}