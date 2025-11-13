/*
 * Implementación del patrón Data Access Object (DAO) para la entidad HistoriaClinica.
 * Implementa la interfaz GenericDao y utiliza TransactionManager para la gestión de conexiones.
 * NO incluye la relación con Paciente; solo maneja la persistencia de la entidad HistoriaClinica en sí.
 */
package dao;

import config.TransactionManager;
import entities.HistoriaClinica;
import entities.TipoSangre;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class HistoriaClinicaDao implements GenericDao<HistoriaClinica> {

    // --- Constantes SQL para Operaciones CRUD ---
    
    /** SQL para la inserción de un nuevo registro de HistoriaClinica. */
    private static final String INSERT_SQL = "INSERT INTO historia_clinica (nro_historia, grupo_sanguineo, antecedentes, medicacion_actual, observaciones, eliminado) VALUES (?, ?, ?, ?, ?, ?)";
    
    /** SQL para leer una HistoriaClinica activa por ID. */
    private static final String SELECT_BY_ID_SQL = "SELECT id, nro_historia, grupo_sanguineo, antecedentes, medicacion_actual, observaciones, eliminado FROM historia_clinica WHERE id = ? AND eliminado = FALSE";
    
    /** SQL para la actualización de todos los campos de una HistoriaClinica. */
    private static final String UPDATE_SQL = "UPDATE historia_clinica SET nro_historia = ?, grupo_sanguineo = ?, antecedentes = ?, medicacion_actual = ?, observaciones = ? WHERE id = ?";
    
    /** SQL para la eliminación lógica de una HistoriaClinica (marcando 'eliminado' como TRUE). */
    private static final String DELETE_SQL = "UPDATE historia_clinica SET eliminado = TRUE WHERE id = ?";
    
    /** SQL para la recuperación lógica de una HistoriaClinica (marcando 'eliminado' como FALSE). */
    private static final String RECOVER_SQL = "UPDATE historia_clinica SET eliminado = FALSE WHERE id = ?"; 
    
    /** SQL para seleccionar todas las Historias Clínicas activas (eliminado = FALSE). */
    private static final String SELECT_ALL_ACTIVE_SQL = "SELECT id, nro_historia, grupo_sanguineo, antecedentes, medicacion_actual, observaciones, eliminado FROM historia_clinica WHERE eliminado = FALSE";
    
    // --- Utilidades y Configuración ---

    /** Bandera que indica si los métodos de modificación requieren estar dentro de una transacción activa. */
    private static final boolean REQUIRED_TRANSACTION = true;

    /**
     * Mapea un ResultSet (resultado de la consulta DB) a un objeto HistoriaClinica.
     * @param rs ResultSet de la consulta SQL ejecutada.
     * @return Objeto HistoriaClinica completamente poblado.
     * @throws SQLException Si hay un error al leer los datos del ResultSet.
     */
    private HistoriaClinica mapResultSetToEntity(ResultSet rs) throws SQLException {
        long id = rs.getLong("id");
        String nroHistoria = rs.getString("nro_historia");
        
        // Mapeo seguro para TipoSangre (usa la función de mapeo del ENUM)
        TipoSangre grupoSanguineo = null;
        String dbGrupo = rs.getString("grupo_sanguineo");
        if (dbGrupo != null && !dbGrupo.isEmpty()) {
            grupoSanguineo = TipoSangre.fromDbValue(dbGrupo);
        }
        
        String antecedentes = rs.getString("antecedentes");
        String medicacionActual = rs.getString("medicacion_actual");
        String observaciones = rs.getString("observaciones");
        boolean eliminado = rs.getBoolean("eliminado");

        return new HistoriaClinica(id, eliminado, nroHistoria, grupoSanguineo, antecedentes, medicacionActual, observaciones);
    }

    /**
     * Inserta un nuevo objeto HistoriaClinica en la base de datos.
     * Requiere una transacción activa.
     */
    @Override
    public HistoriaClinica crearEntidad(HistoriaClinica entidad) throws SQLException {
        boolean txActive = TransactionManager.isTransactionActive();
        Connection conn = TransactionManager.getConnection(REQUIRED_TRANSACTION);
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            ps = conn.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS);
            int i = 1;
            ps.setString(i++, entidad.getNroHistoria());
            ps.setString(i++, entidad.getGrupoSanguineo() != null ? entidad.getGrupoSanguineo().getValor() : null);
            ps.setString(i++, entidad.getAntecedentes());
            ps.setString(i++, entidad.getMedicacionActual());
            ps.setString(i++, entidad.getObservaciones());
            ps.setBoolean(i++, entidad.isEliminado());

            int affected = ps.executeUpdate();
            if (affected == 0) {
                throw new SQLException("Fallo al crear la Historia Clinica, no se modificaron filas.");
            }

            rs = ps.getGeneratedKeys();
            if (rs.next()) {
                entidad.setId(rs.getLong(1));
                System.out.println("DEBUG: HistoriaClinica creada con ID: " + entidad.getId());
            } else {
                throw new SQLException("Fallo al crear la Historia Clinica, no se obtuvo ID generado.");
            }

            return entidad;
        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException ignore) {}
            if (ps != null) try { ps.close(); } catch (SQLException ignore) {}
            if (!txActive) {
                try { conn.close(); } catch (SQLException ignore) {}
            }
        }
    }

    /**
     * Lee una HistoriaClinica activa por su ID.
     * No requiere transacción.
     */
    @Override
    public HistoriaClinica leerEntidad(long id) throws SQLException {
        boolean txActive = TransactionManager.isTransactionActive();
        Connection conn = TransactionManager.getConnection(false);
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            ps = conn.prepareStatement(SELECT_BY_ID_SQL);
            ps.setLong(1, id);
            rs = ps.executeQuery();
            if (rs.next()) {
                return mapResultSetToEntity(rs);
            }
            return null;
        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException ignore) {}
            if (ps != null) try { ps.close(); } catch (SQLException ignore) {}
            if (!txActive) {
                try { conn.close(); } catch (SQLException ignore) {}
            }
        }
    }

    /**
     * Actualiza una HistoriaClinica existente en la base de datos.
     * Requiere una transacción activa.
     */
    @Override
    public void actualizarEntidad(HistoriaClinica entidad) throws SQLException {
        if (entidad.getId() <= 0) {
            throw new SQLException("El ID de la Historia Clinica es inválido para la actualización.");
        }

        boolean txActive = TransactionManager.isTransactionActive();
        Connection conn = TransactionManager.getConnection(REQUIRED_TRANSACTION);
        PreparedStatement ps = null;

        try {
            ps = conn.prepareStatement(UPDATE_SQL);
            int i = 1;
            ps.setString(i++, entidad.getNroHistoria());
            ps.setString(i++, entidad.getGrupoSanguineo() != null ? entidad.getGrupoSanguineo().getValor() : null);
            ps.setString(i++, entidad.getAntecedentes());
            ps.setString(i++, entidad.getMedicacionActual());
            ps.setString(i++, entidad.getObservaciones());
            ps.setLong(i++, entidad.getId());

            int rows = ps.executeUpdate();
            if (rows == 0) {
                throw new SQLException("No se pudo actualizar la Historia Clinica con ID: " + entidad.getId());
            }
        } finally {
            if (ps != null) try { ps.close(); } catch (SQLException ignore) {}
            if (!txActive) {
                try { conn.close(); } catch (SQLException ignore) {}
            }
        }
    }

    /**
     * Elimina lógicamente una HistoriaClinica por su ID (soft delete).
     * Requiere transacción activa.
     */
    @Override
    public void eliminarEntidad(long id) throws SQLException {
        if (id <= 0) {
            throw new SQLException("El ID de la Historia Clinica es inválido para la eliminación.");
        }

        boolean txActive = TransactionManager.isTransactionActive();
        Connection conn = TransactionManager.getConnection(REQUIRED_TRANSACTION);
        PreparedStatement ps = null;

        try {
            ps = conn.prepareStatement(DELETE_SQL);
            ps.setLong(1, id);
            int rows = ps.executeUpdate();
            if (rows == 0) {
                throw new SQLException("No se encontró Historia Clinica con ID: " + id);
            }
        } finally {
            if (ps != null) try { ps.close(); } catch (SQLException ignore) {}
            if (!txActive) {
                try { conn.close(); } catch (SQLException ignore) {}
            }
        }
    }

    /**
     * Recupera lógicamente una HistoriaClinica por su ID (undo soft delete).
     * Requiere transacción activa.
     */
    @Override
    public void recuperarEntidad(long id) throws SQLException {
        if (id <= 0) {
            throw new SQLException("El ID de la Historia Clinica es inválido para la recuperación.");
        }

        boolean txActive = TransactionManager.isTransactionActive();
        Connection conn = TransactionManager.getConnection(REQUIRED_TRANSACTION);
        PreparedStatement ps = null;

        try {
            ps = conn.prepareStatement(RECOVER_SQL);
            ps.setLong(1, id);
            int rows = ps.executeUpdate();
            if (rows == 0) {
                throw new SQLException("No se encontró Historia Clinica con ID: " + id);
            }
        } finally {
            if (ps != null) try { ps.close(); } catch (SQLException ignore) {}
            if (!txActive) {
                try { conn.close(); } catch (SQLException ignore) {}
            }
        }
    }

    /**
     * Retorna una lista de todas las Historias Clínicas activas (eliminado = FALSE).
     * No requiere transacción.
     */
    @Override
    public List<HistoriaClinica> leerTodo() throws SQLException {
        List<HistoriaClinica> lista = new ArrayList<>();
        boolean txActive = TransactionManager.isTransactionActive();
        Connection conn = TransactionManager.getConnection(false);
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            ps = conn.prepareStatement(SELECT_ALL_ACTIVE_SQL);
            rs = ps.executeQuery();
            while (rs.next()) {
                lista.add(mapResultSetToEntity(rs));
            }
            return lista;
        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException ignore) {}
            if (ps != null) try { ps.close(); } catch (SQLException ignore) {}
            if (!txActive) {
                try { conn.close(); } catch (SQLException ignore) {}
            }
        }
    }

    /**
     * Lee una HistoriaClinica por id incluyendo eliminados (no usa flag eliminado).
     * Útil para auditoría o recuperación.
     */
    public HistoriaClinica leerEntidadConEliminados(long id) throws SQLException {
        final String SELECT_BY_ID_ALL_SQL = "SELECT id, nro_historia, grupo_sanguineo, antecedentes, medicacion_actual, observaciones, eliminado FROM historia_clinica WHERE id = ?";
        boolean txActive = TransactionManager.isTransactionActive();
        Connection conn = TransactionManager.getConnection(false);
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            ps = conn.prepareStatement(SELECT_BY_ID_ALL_SQL);
            ps.setLong(1, id);
            rs = ps.executeQuery();
            if (rs.next()) {
                return mapResultSetToEntity(rs);
            }
            return null;
        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException ignore) {}
            if (ps != null) try { ps.close(); } catch (SQLException ignore) {}
            if (!txActive) {
                try { conn.close(); } catch (SQLException ignore) {}
            }
        }
    }
}
