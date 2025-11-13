/*
 * Clase para gestionar la conexión y las transacciones de manera NO estática.
 * Implementa AutoCloseable para uso seguro con try-with-resources en la capa Service,
 * asegurando el control total del ciclo de vida de la Connection (setAutoCommit/commit/rollback/cierre).
 * basada en el ejemplo del profe
 */

package config;

/**
 *
 * @author emanuelbrahim
 */

import java.sql.Connection; //Importa la libreria Java.sql connection
import java.sql.SQLException; //Importa la libreria Java.sql SQLException

// Implementamos AutoCloseable para que el bloque try-with-resources la cierre automáticamente.
public class TransactionManager implements AutoCloseable { 

    // - conn: Connection
    private Connection conn;

    // - transactionActive: boolean
    private boolean transactionActive = false;

    // + TransactionManager(conn: Connection) (Constructor)
    public TransactionManager(Connection conn) {
        if (conn == null) {
            throw new IllegalArgumentException("La conexión no puede ser nula.");
        }
        this.conn = conn;
    }

    // + startTransaction(): void
    /**
     * Inicia la transacción desactivando el auto-commit.
     */
    public void startTransaction() throws SQLException {
        if (this.conn == null || this.conn.isClosed()) {
            throw new SQLException("No se puede iniciar la transacción: conexión no disponible o cerrada.");
        }
        if (this.transactionActive) {
            throw new IllegalStateException("Esta instancia de TransactionManager ya tiene una transacción activa.");
        }
        
        this.conn.setAutoCommit(false);
        this.transactionActive = true;
        System.out.println("[DEBUG] Transacción iniciada.");
    }

    // + commit(): void
    /**
     * Confirma los cambios y cierra los recursos (restablece auto-commit y cierra la conexión).
     */
    public void commit() throws SQLException {
        if (!this.transactionActive) {
            throw new IllegalStateException("No hay transacción activa para hacer commit.");
        }
        try {
            this.conn.commit();
            System.out.println("[DEBUG] Commit realizado correctamente.");
            this.transactionActive = false; // Marcamos como inactiva tras el commit
        } finally {
            close(); // Cierra los recursos (conexión, restablece auto-commit)
        }
    }

    // + rollback(): void
    /**
     * Revierte los cambios y cierra los recursos (restablece auto-commit y cierra la conexión).
     */
    public void rollback() throws SQLException {
        if (this.conn == null || this.conn.isClosed()) {
             // La conexión ya está cerrada, no hay nada que revertir.
             return;
        }
        
        // Si hay una transacción activa (no se hizo commit), hacemos rollback
        if (this.transactionActive) {
            try {
                this.conn.rollback();
                System.out.println("[DEBUG] Rollback realizado correctamente.");
            } catch (SQLException e) {
                System.err.println("Error durante el rollback: " + e.getMessage());
                throw e; // Relanzamos el error si falla el rollback
            } finally {
                this.transactionActive = false; // Marcamos como inactiva tras el rollback
            }
        }
        // Llamamos a close para restablecer autoCommit y cerrar la conexión
        close();
    }

    // @Override
    // + close(): void (Ahora usa @Override para AutoCloseable, cumpliendo el mismo objetivo)
    /**
     * Implementación de AutoCloseable.
     * Siempre restablece el auto-commit y cierra la conexión, manejando el estado.
     */
    @Override
    public void close() {
        if (this.conn != null) {
            try {
                // 1. Si la transacción sigue activa, es un error no manejado, intentamos rollback
                if (this.transactionActive) {
                    // System.err.println("[DEBUG] Transacción activa al cerrar. Intentando Rollback...");
                    this.conn.rollback(); // Forzamos el rollback si no se hizo ni commit ni rollback
                }
                
                // 2. Restablecer auto-commit
                if (!this.conn.getAutoCommit()) {
                    this.conn.setAutoCommit(true);
                }
                
                // 3. Cerrar la conexión
                this.conn.close();
                System.out.println("[DEBUG] Conexión cerrada y liberada.");
                
            } catch (SQLException e) {
                System.err.println("Advertencia: No se pudo cerrar la conexión o falló el rollback implícito: " + e.getMessage());
            } finally {
                // 4. Limpiar atributos
                this.conn = null;
                this.transactionActive = false;
            }
        }
    }

    // + isTransactionActive(): boolean
    /**
     * Indica si la transacción fue iniciada y está pendiente de commit o rollback.
     */
    public boolean isTransactionActive() {
        return this.transactionActive;
    }

    // + getConnection(): Connection
    /**
     * Obtiene la conexión compartida para pasarla a los DAOs.
     */
    public Connection getConnection() {
        return this.conn;
    }
}