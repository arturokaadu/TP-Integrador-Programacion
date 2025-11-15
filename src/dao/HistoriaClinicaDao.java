/*
 * Implementación del patrón Data Access Object (DAO) para la entidad HistoriaClinica.
 * Implementa la interfaz GenericDao y se enfoca puramente en la persistencia de la entidad
 * HistoriaClinica, asegurando que siempre se relacione con un Paciente.
 *
 * Los métodos modificadores reciben la Connection como parámetro, permitiendo su ejecución
 * dentro de un ámbito transaccional controlado por la capa Service.
 * Inspirado en el ejemplo del profe
 */
package dao;

/**
 *
 * @author emanuelbrahim
 */

//Importa las librerias y los demas Source Packages con los que trabajara 
import config.DatabaseConnection;
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

    // --- Constantes SQL ---
    private static final String INSERT_SQL = "INSERT INTO historia_clinica (nro_historia, grupo_sanguineo, antecedentes, medicacion_actual, observaciones, eliminado, paciente_id) VALUES (?, ?, ?, ?, ?, ?, ?)";
    private static final String SELECT_BY_ID_SQL = "SELECT id, nro_historia, grupo_sanguineo, antecedentes, medicacion_actual, observaciones, eliminado FROM historia_clinica WHERE id = ? AND eliminado = FALSE";
    private static final String UPDATE_SQL = "UPDATE historia_clinica SET nro_historia = ?, grupo_sanguineo = ?, antecedentes = ?, medicacion_actual = ?, observaciones = ? WHERE id = ?";
    private static final String DELETE_SQL = "UPDATE historia_clinica SET eliminado = TRUE WHERE id = ?";
    private static final String RECOVER_SQL = "UPDATE historia_clinica SET eliminado = FALSE WHERE id = ?";
    private static final String SELECT_ALL_ACTIVE_SQL = "SELECT id, nro_historia, grupo_sanguineo, antecedentes, medicacion_actual, observaciones, eliminado FROM historia_clinica WHERE eliminado = FALSE";

    // --- CONSTANTES SQL para manejo del Borrado Lógico ---
    private static final String SELECT_ALL_DELETED_SQL = "SELECT id, nro_historia, grupo_sanguineo, antecedentes, medicacion_actual, observaciones, eliminado FROM historia_clinica WHERE eliminado = TRUE";
    private static final String COUNT_DELETED_SQL = "SELECT COUNT(*) FROM historia_clinica WHERE eliminado = TRUE";

    // --- Mapeo y Utilidades ---

    /**
     * Mapea un ResultSet (fila de la base de datos) a una entidad HistoriaClinica.
     * Incluye la lógica de conversión del String de TipoSangre de la BD al Enum de Java.
     * @param rs ResultSet de la consulta.
     * @return Objeto HistoriaClinica.
     * @throws SQLException Si ocurre un error de lectura de la base de datos.
     */
    private HistoriaClinica mapearEntidad(ResultSet rs) throws SQLException {
        long id = rs.getLong("id");
        String nroHistoria = rs.getString("nro_historia");

        TipoSangre grupoSanguineo = null;
        String dbGrupo = rs.getString("grupo_sanguineo");
        
        // Conversión segura de String (BD) a Enum (Java)
        if (dbGrupo != null && !dbGrupo.isEmpty()) {
            try {
                grupoSanguineo = TipoSangre.fromDbValue(dbGrupo);
            } catch (IllegalArgumentException e) {
                // Notificación de valor inválido en BD, pero permite que la HC se mapee
                System.err.println("Valor de TipoSangre inválido en BD: " + dbGrupo);
            }
        }

        String antecedentes = rs.getString("antecedentes");
        String medicacionActual = rs.getString("medicacion_actual");
        String observaciones = rs.getString("observaciones");
        boolean eliminado = rs.getBoolean("eliminado");

        return new HistoriaClinica(id, eliminado, nroHistoria, grupoSanguineo, antecedentes, medicacionActual, observaciones);
    }

    /**
     * Inserta una nueva Historia Clínica en la base de datos.
     * Este método es transaccional y siempre requiere un pacienteId.
     * @param entidad Objeto HistoriaClinica a insertar.
     * @param conn Conexión compartida para la transacción activa.
     * @param pacienteId ID del paciente al cual se relaciona la historia clínica.
     * @return La entidad HistoriaClinica con su ID autogenerado.
     * @throws SQLException Si falla la inserción o no se obtiene el ID.
     */
    public HistoriaClinica crear(HistoriaClinica entidad, Connection conn, long pacienteId) throws SQLException {
        if (pacienteId <= 0) {
            throw new SQLException("El pacienteId debe ser mayor a 0 para crear una Historia Clinica.");
        }

        try (PreparedStatement ps = conn.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {

            int i = 1;
            ps.setString(i++, entidad.getNroHistoria());
            ps.setString(i++, entidad.getGrupoSanguineo() != null ? entidad.getGrupoSanguineo().getValor() : null); 
            ps.setString(i++, entidad.getAntecedentes());
            ps.setString(i++, entidad.getMedicacionActual());
            ps.setString(i++, entidad.getObservaciones());
            ps.setBoolean(i++, entidad.isEliminado());
            ps.setLong(i++, pacienteId);

            int affectedRows = ps.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Fallo al crear la Historia Clinica, no se modificaron filas.");
            }

            // Obtener el ID autogenerado
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    entidad.setId(rs.getLong(1));
                } else {
                    throw new SQLException("Fallo al crear la Historia Clinica, no se obtuvo ID generado.");
                }
            }
            return entidad;
        }
    }
     @Override
    public HistoriaClinica crear(HistoriaClinica entidad, Connection conn) throws SQLException {
    throw new UnsupportedOperationException("Error, la que debes llamar es crear(HistoriaClinica, Connection, long pacienteId) en su lugar.");
        }


    /**
     * Lee una Historia Clínica por su ID (solo registros activos: eliminado = FALSE).
     * Nota: Este método maneja su propia conexión, no es transaccional.
     * @param id El ID de la Historia Clínica a buscar.
     * @return La Historia Clínica encontrada o null si no existe.
     * @throws SQLException Si ocurre un error de acceso a la base de datos.
     */
    @Override
    public HistoriaClinica leer(long id) throws SQLException {
        Connection conn = DatabaseConnection.getConnection();
        try (
            PreparedStatement ps = conn.prepareStatement(SELECT_BY_ID_SQL);
        ) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearEntidad(rs);
                }
                return null;
            }
        } finally {
            if (conn != null) {
                try { conn.close(); } catch (SQLException ignore) {}
            }
        }
    }

    /**
     * Retorna una lista de todas las Historias Clínicas que están activas (eliminado = FALSE).
     * Nota: Este método maneja su propia conexión, no es transaccional.
     * @return Lista de Historias Clínicas activas.
     * @throws SQLException Si ocurre un error de acceso a la base de datos.
     */
    @Override
    public List<HistoriaClinica> leerTodos() throws SQLException {
        List<HistoriaClinica> lista = new ArrayList<>();
        Connection conn = DatabaseConnection.getConnection();

        try (
            PreparedStatement ps = conn.prepareStatement(SELECT_ALL_ACTIVE_SQL);
            ResultSet rs = ps.executeQuery();
        ) {
            while (rs.next()) {
                lista.add(mapearEntidad(rs));
            }
            return lista;
        } finally {
            if (conn != null) {
                try { conn.close(); } catch (SQLException ignore) {}
            }
        }
    }

    /**
     * Actualiza los datos de una Historia Clínica existente.
     * Este método es transaccional.
     * @param entidad Objeto HistoriaClinica con los datos a actualizar.
     * @param conn Conexión compartida para la transacción activa.
     * @throws SQLException Si ocurre un error de acceso a la base de datos o si el ID es inválido.
     */
    @Override
    public void actualizar(HistoriaClinica entidad, Connection conn) throws SQLException {
        if (entidad.getId() <= 0) {
            throw new SQLException("El ID de la Historia Clinica es inválido para la actualización.");
        }

        try (PreparedStatement ps = conn.prepareStatement(UPDATE_SQL)) {

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
        }
    }

    /**
     * Realiza la baja lógica (Soft Delete) de una Historia Clínica, estableciendo 'eliminado = TRUE'.
     * Este método es transaccional.
     * @param id El ID de la Historia Clínica a eliminar.
     * @param conn Conexión compartida para la transacción activa.
     * @throws SQLException Si ocurre un error de acceso a la base de datos o si no se encuentra la entidad activa.
     */
    @Override
    public void eliminar(long id, Connection conn) throws SQLException {
        if (id <= 0) {
            throw new SQLException("El ID de la Historia Clinica es inválido para la eliminación.");
        }

        try (PreparedStatement ps = conn.prepareStatement(DELETE_SQL)) {
            ps.setLong(1, id);
            int rows = ps.executeUpdate();
            if (rows == 0) {
                throw new SQLException("No se encontró Historia Clinica activa con ID: " + id);
            }
        }
    }

    /**
     * Recupera una Historia Clínica eliminada lógicamente, estableciendo 'eliminado = FALSE'.
     * Este método es transaccional.
     * @param id El ID de la Historia Clínica a recuperar.
     * @param conn Conexión compartida para la transacción activa.
     * @throws SQLException Si ocurre un error de acceso a la base de datos o si no se encuentra la entidad eliminada.
     */
    @Override
    public void recuperar(long id, Connection conn) throws SQLException {
        if (id <= 0) {
            throw new SQLException("El ID de la Historia Clinica es inválido para la recuperación.");
        }

        try (PreparedStatement ps = conn.prepareStatement(RECOVER_SQL)) {
            ps.setLong(1, id);
            int rows = ps.executeUpdate();
            if (rows == 0) {
                throw new SQLException("No se encontró Historia Clinica (marcada como eliminada) con ID: " + id);
            }
        }
    }

    /**
     * Obtiene la lista completa de Historias Clínicas marcadas como eliminadas lógicamente (eliminado = TRUE).
     * @return Lista de Historias Clínicas eliminadas.
     * @throws SQLException Si ocurre un error al acceder a la base de datos.
     */
    @Override
    public List<HistoriaClinica> leerTodosEliminados() throws SQLException {
        List<HistoriaClinica> lista = new ArrayList<>();
        Connection conn = DatabaseConnection.getConnection();

        try (
            PreparedStatement ps = conn.prepareStatement(SELECT_ALL_DELETED_SQL);
            ResultSet rs = ps.executeQuery();
        ) {
            while (rs.next()) {
                lista.add(mapearEntidad(rs));
            }
            return lista;
        } finally {
            if (conn != null) {
                try { conn.close(); } catch (SQLException ignore) {}
            }
        }
    }

    /**
     * Cuenta la cantidad de registros de HistoriaClinica que están marcados como eliminados lógicamente (eliminado = TRUE).
     * @return El número de registros eliminados.
     * @throws SQLException Si ocurre un error al acceder a la base de datos.
     */
    @Override
    public long contarEliminados() throws SQLException {
        Connection conn = DatabaseConnection.getConnection();

        try (
            PreparedStatement ps = conn.prepareStatement(COUNT_DELETED_SQL);
            ResultSet rs = ps.executeQuery();
        ) {
            if (rs.next()) {
                return rs.getLong(1);
            }
            return 0;
        } finally {
            if (conn != null) {
                try { conn.close(); } catch (SQLException ignore) {}
            }
        }
    }

    /**
     * Lee una Historia Clínica por su ID, incluyendo registros eliminados (ignora 'eliminado = FALSE').
     * Útil para verificación administrativa o en casos donde se necesita acceder al registro completo.
     * Nota: Este método maneja su propia conexión, no es transaccional.
     * @param id El ID de la Historia Clínica a buscar.
     * @return La Historia Clínica (activa o eliminada) o null si no existe.
     * @throws SQLException Si ocurre un error de acceso a la base de datos.
     */
    public HistoriaClinica leerConEliminados(long id) throws SQLException {
        final String SELECT_BY_ID_ALL_SQL = "SELECT id, nro_historia, grupo_sanguineo, antecedentes, medicacion_actual, observaciones, eliminado FROM historia_clinica WHERE id = ?";

        Connection conn = DatabaseConnection.getConnection();

        try (
            PreparedStatement ps = conn.prepareStatement(SELECT_BY_ID_ALL_SQL);
        ) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearEntidad(rs);
                }
                return null;
            }
        } finally {
            if (conn != null) {
                try { conn.close(); } catch (SQLException ignore) {}
            }
        }
    }
}
