/*
 * DAO para la entidad HistoriaClinica.
 * Implementa la interfaz GenericDao y usa TransactionManager para la conexión.
 * NO incluye la relación con Paciente; solo maneja la entidad HistoriaClinica en sí.
 */
package dao;

import config.DatabaseConnection;
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

    // Constantes SQL
    private static final String INSERT_SQL = "INSERT INTO historia_clinica (nro_historia, grupo_sanguineo, antecedentes, medicacion_actual, observaciones, eliminado) VALUES (?, ?, ?, ?, ?, ?)";
    private static final String SELECT_BY_ID_SQL = "SELECT id, nro_historia, grupo_sanguineo, antecedentes, medicacion_actual, observaciones, eliminado FROM historia_clinica WHERE id = ? AND eliminado = FALSE";
    private static final String UPDATE_SQL = "UPDATE historia_clinica SET nro_historia = ?, grupo_sanguineo = ?, antecedentes = ?, medicacion_actual = ?, observaciones = ? WHERE id = ?";
    private static final String DELETE_SQL = "UPDATE historia_clinica SET eliminado = TRUE WHERE id = ?";
    private static final String RECOVER_SQL = "UPDATE historia_clinica SET eliminado = FALSE WHERE id = ?"; // NUEVO SQL para recuperación
    private static final String SELECT_ALL_ACTIVE_SQL = "SELECT id, nro_historia, grupo_sanguineo, antecedentes, medicacion_actual, observaciones, eliminado FROM historia_clinica WHERE eliminado = FALSE";
    
    // El DAO siempre exige estar dentro de una transacción para obtener la conexión
    private static final boolean REQUIRED_TRANSACTION = true;

    /**
     * Mapea un ResultSet a un objeto HistoriaClinica.
     * @param rs ResultSet de la consulta.
     * @return Objeto HistoriaClinica.
     * @throws SQLException Si hay un error al leer los datos.
     */
    private HistoriaClinica mapResultSetToEntity(ResultSet rs) throws SQLException {
        long id = rs.getLong("id");
        String nroHistoria = rs.getString("nro_historia");
        // Mapeo del ENUM:
        TipoSangre grupoSanguineo = TipoSangre.fromDbValue(rs.getString("grupo_sanguineo")); 
        String antecedentes = rs.getString("antecedentes");
        String medicacionActual = rs.getString("medicacion_actual");
        String observaciones = rs.getString("observaciones");
        boolean eliminado = rs.getBoolean("eliminado");

        return new HistoriaClinica(id, eliminado, nroHistoria, grupoSanguineo, antecedentes, medicacionActual, observaciones);
    }

    @Override
    public HistoriaClinica crearEntidad(HistoriaClinica entidad) throws SQLException {
        // Usamos try-with-resources. Si hay una transacción activa, getConnection() devuelve la conexión del ThreadLocal.
        // Si no hay transacción activa (mal uso), TransactionManager lanzará IllegalStateException.
        try (Connection conn = TransactionManager.getConnection(REQUIRED_TRANSACTION);
             PreparedStatement ps = conn.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {

            int i = 1;
            ps.setString(i++, entidad.getNroHistoria());
            // Guardamos el valor String del ENUM en la BD
            ps.setString(i++, entidad.getGrupoSanguineo().getValor()); 
            ps.setString(i++, entidad.getAntecedentes());
            ps.setString(i++, entidad.getMedicacionActual());
            ps.setString(i++, entidad.getObservaciones());
            ps.setBoolean(i++, entidad.isEliminado());

            int affectedRows = ps.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Fallo al crear la Historia Clinica, no se modificaron filas.");
            }

            // Obtener el ID autogenerado
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    entidad.setId(rs.getLong(1));
                    System.out.println("DEBUG: HistoriaClinica creada con ID: " + entidad.getId());
                } else {
                    throw new SQLException("Fallo al crear la Historia Clinica, no se obtuvo ID generado.");
                }
            }
        } // Connection/PreparedStatement se cierra automáticamente (si no es del ThreadLocal) o se mantiene abierta (si es del ThreadLocal)
        return entidad;
    }

    @Override
    public HistoriaClinica leerEntidad(long id) throws SQLException {
        // Usamos try-with-resources para asegurar que la conexión se cierre si no hay transacción activa.
        try (Connection conn = TransactionManager.getConnection(false); 
             PreparedStatement ps = conn.prepareStatement(SELECT_BY_ID_SQL)) {
            
            ps.setLong(1, id);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToEntity(rs);
                }
            }
        }
        return null; // Retorna null si no se encuentra la entidad
    }

    @Override
    public void actualizarEntidad(HistoriaClinica entidad) throws SQLException {
        if (entidad.getId() <= 0) {
            throw new SQLException("El ID de la Historia Clinica es inválido para la actualización.");
        }
        
        try (Connection conn = TransactionManager.getConnection(REQUIRED_TRANSACTION);
             PreparedStatement ps = conn.prepareStatement(UPDATE_SQL)) {

            int i = 1;
            ps.setString(i++, entidad.getNroHistoria());
            ps.setString(i++, entidad.getGrupoSanguineo().getValor());
            ps.setString(i++, entidad.getAntecedentes());
            ps.setString(i++, entidad.getMedicacionActual());
            ps.setString(i++, entidad.getObservaciones());
            ps.setLong(i++, entidad.getId()); // WHERE id = ?

            ps.executeUpdate();
            
        } // Cierre automático/gestionado por TransactionManager
    }

    @Override
    public void eliminarEntidad(long id) throws SQLException {
        if (id <= 0) {
            throw new SQLException("El ID de la Historia Clinica es inválido para la eliminación.");
        }
        // Baja LÓGICA: se establece 'eliminado = TRUE'
        try (Connection conn = TransactionManager.getConnection(REQUIRED_TRANSACTION);
             PreparedStatement ps = conn.prepareStatement(DELETE_SQL)) {
            
            ps.setLong(1, id);
            ps.executeUpdate();
            
        } // Cierre automático/gestionado por TransactionManager
    }

    /**
     * Recupera lógicamente una entidad por su ID (marca 'eliminado' como false).
     * @param id El ID de la entidad a recuperar.
     * @throws SQLException Si ocurre un error de acceso a la base de datos.
     */
    @Override
    public void recuperarEntidad(long id) throws SQLException {
        if (id <= 0) {
            throw new SQLException("El ID de la Historia Clinica es inválido para la recuperación.");
        }
        // Recuperación LÓGICA: se establece 'eliminado = FALSE'
        try (Connection conn = TransactionManager.getConnection(REQUIRED_TRANSACTION);
             PreparedStatement ps = conn.prepareStatement(RECOVER_SQL)) {
            
            ps.setLong(1, id);
            ps.executeUpdate();
            
        } // Cierre automático/gestionado por TransactionManager
    }


    @Override
    public List<HistoriaClinica> leerTodo() throws SQLException {
        List<HistoriaClinica> lista = new ArrayList<>();
        // Baja LÓGICA: solo trae registros con 'eliminado = FALSE'
        try (Connection conn = TransactionManager.getConnection(false);
             PreparedStatement ps = conn.prepareStatement(SELECT_ALL_ACTIVE_SQL);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                lista.add(mapResultSetToEntity(rs));
            }
        }
        return lista;
    }
    
    /**
     * Retorna una HistoriaClinica, incluyendo registros eliminados lógicamente.
     * Usado internamente para ciertas operaciones de Service (como reasignación).
     * @param id ID de la entidad.
     * @return HistoriaClinica encontrada (activa o eliminada), o null.
     * @throws SQLException Si ocurre un error de acceso a la base de datos.
     */
     public HistoriaClinica leerEntidadConEliminados(long id) throws SQLException {
        final String SELECT_BY_ID_ALL_SQL = "SELECT id, nro_historia, grupo_sanguineo, antecedentes, medicacion_actual, observaciones, eliminado FROM historia_clinica WHERE id = ?";
        try (Connection conn = TransactionManager.getConnection(false);
             PreparedStatement ps = conn.prepareStatement(SELECT_BY_ID_ALL_SQL)) {

            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToEntity(rs);
                }
            }
        }
        return null;
    }
}