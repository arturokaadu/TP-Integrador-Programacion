/*
 * Implementación del patrón Data Access Object (DAO) para la entidad Paciente.
 * Se encarga de la persistencia de los objetos Paciente en la base de datos.
 *
 * Adaptado para recibir la Connection como parámetro en métodos modificadores,
 * cumpliendo con el GenericDao del UML y la consigna de transacciones.
 * Y inspirado en el ejemplo del profe 
 */
package dao;

/**
 *
 * @author emanuelbrahim
 */

//Importa las librerias y los demas Source Packages con los que trabajara 
import config.DatabaseConnection;
import entities.Paciente;
import entities.HistoriaClinica;
import entities.TipoSangre;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PacienteDao implements GenericDao<Paciente> {

    // --- Constantes SQL (Consultas con JOIN para incluir HistoriaClinica) ---
    private static final String INSERT_SQL = "INSERT INTO paciente (nombre, apellido, dni, fecha_nacimiento, fk_historia_clinica, eliminado) VALUES (?, ?, ?, ?, ?, ?)";
    // SELECT_BY_ID incluye un LEFT JOIN para obtener los datos de la HistoriaClinica asociada (si existe).
    private static final String SELECT_BY_ID_SQL = "SELECT p.*, hc.nro_historia, hc.grupo_sanguineo, hc.antecedentes, hc.medicacion_actual, hc.observaciones, hc.eliminado as hc_eliminado FROM paciente p LEFT JOIN historia_clinica hc ON p.fk_historia_clinica = hc.id WHERE p.id = ? AND p.eliminado = FALSE";
    private static final String UPDATE_SQL = "UPDATE paciente SET nombre = ?, apellido = ?, dni = ?, fecha_nacimiento = ?, fk_historia_clinica = ? WHERE id = ?";
    private static final String DELETE_SQL = "UPDATE paciente SET eliminado = TRUE WHERE id = ?";
    private static final String RECOVER_SQL = "UPDATE paciente SET eliminado = FALSE WHERE id = ?";
    private static final String SELECT_ALL_ACTIVE_SQL = "SELECT p.*, hc.nro_historia, hc.grupo_sanguineo, hc.antecedentes, hc.medicacion_actual, hc.observaciones, hc.eliminado as hc_eliminado FROM paciente p LEFT JOIN historia_clinica hc ON p.fk_historia_clinica = hc.id WHERE p.eliminado = FALSE";
    private static final String SELECT_BY_DNI_SQL = "SELECT p.*, hc.nro_historia, hc.grupo_sanguineo, hc.antecedentes, hc.medicacion_actual, hc.observaciones, hc.eliminado as hc_eliminado FROM paciente p LEFT JOIN historia_clinica hc ON p.fk_historia_clinica = hc.id WHERE p.dni = ? AND p.eliminado = FALSE";
    
    // --- CONSTANTES SQL para manejo del Borrado Lógico ---
    private static final String SELECT_ALL_DELETED_SQL = "SELECT p.*, hc.nro_historia, hc.grupo_sanguineo, hc.antecedentes, hc.medicacion_actual, hc.observaciones, hc.eliminado as hc_eliminado FROM paciente p LEFT JOIN historia_clinica hc ON p.fk_historia_clinica = hc.id WHERE p.eliminado = TRUE";
    private static final String COUNT_DELETED_SQL = "SELECT COUNT(*) FROM paciente WHERE eliminado = TRUE";


    // --- Mapeo y Utilidades ---

    /**
     * Mapea un ResultSet (fila de la base de datos) a una entidad Paciente.
     * Incluye el mapeo de la entidad HistoriaClinica asociada (FK).
     * @param rs ResultSet de la consulta.
     * @return Objeto Paciente con su HistoriaClinica asociada (si existe).
     * @throws SQLException Si ocurre un error de lectura de la base de datos.
     */
    private Paciente mapearEntidad(ResultSet rs) throws SQLException {
        // --- 1. Mapear atributos del Paciente ---
        long id = rs.getLong("id");
        String nombre = rs.getString("nombre");
        String apellido = rs.getString("apellido");
        String dni = rs.getString("dni");

        LocalDate fechaNacimiento = null;
        if (rs.getDate("fecha_nacimiento") != null) {
            fechaNacimiento = rs.getDate("fecha_nacimiento").toLocalDate(); // Conversión de java.sql.Date a java.time.LocalDate
        }

        boolean eliminado = rs.getBoolean("eliminado");

        Paciente paciente = new Paciente(id, eliminado, nombre, apellido, dni, fechaNacimiento);

        // --- 2. Mapear HistoriaClinica (Relación 1:1) ---
        long fkHistoriaClinica = rs.getLong("fk_historia_clinica");

        // Solo mapeamos HC si la FK existe y tiene datos asociados (resultado del LEFT JOIN)
        if (!rs.wasNull() && fkHistoriaClinica > 0) {
            String nroHistoria = rs.getString("nro_historia");

            TipoSangre grupoSanguineo = null;
            String dbGrupoSanguineo = rs.getString("grupo_sanguineo");

            if (dbGrupoSanguineo != null && !dbGrupoSanguineo.isEmpty()) {
                // Utiliza el método de conversión del Enum TipoSangre
                try {
                    grupoSanguineo = TipoSangre.fromDbValue(dbGrupoSanguineo);
                } catch (IllegalArgumentException e) {
                    System.err.println("Valor de TipoSangre inválido en BD para paciente " + id + ": " + dbGrupoSanguineo);
                }
            }

            String antecedentes = rs.getString("antecedentes");
            String medicacionActual = rs.getString("medicacion_actual");
            String observaciones = rs.getString("observaciones");
            // Se usa el alias 'hc_eliminado' para no confundir con p.eliminado
            boolean hcEliminado = rs.getBoolean("hc_eliminado"); 

            // Construir y enlazar la HistoriaClinica al Paciente
            HistoriaClinica hc = new HistoriaClinica(fkHistoriaClinica, hcEliminado, nroHistoria, grupoSanguineo, antecedentes, medicacionActual, observaciones);
            paciente.setHistoriaClinica(hc);
        }

        return paciente;
    }

    /**
     * Inserta un nuevo Paciente en la base de datos, incluyendo la Foreign Key a HistoriaClinica.
     * Este método es transaccional y debe ser llamado con una Connection activa.
     * @param entidad Objeto Paciente a insertar.
     * @param conn Conexión compartida para la transacción activa.
     * @return La entidad Paciente con su ID autogenerado.
     * @throws SQLException Si falla la inserción o no se obtiene el ID.
     */
    @Override
    public Paciente crear(Paciente entidad, Connection conn) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {

            int i = 1;
            ps.setString(i++, entidad.getNombre());
            ps.setString(i++, entidad.getApellido());
            ps.setString(i++, entidad.getDni());

            // 1. Manejo de Fecha de Nacimiento
            if (entidad.getFechaNacimiento() != null) {
                ps.setDate(i++, java.sql.Date.valueOf(entidad.getFechaNacimiento()));
            } else {
                ps.setNull(i++, java.sql.Types.DATE);
            }

            // 2. Manejo de la Foreign Key (FK) a HistoriaClinica
            HistoriaClinica hc = entidad.getHistoriaClinica();
            if (hc != null && hc.getId() > 0) {
                ps.setLong(i++, hc.getId()); // Setea la FK si la HC existe
            } else {
                ps.setNull(i++, java.sql.Types.BIGINT); // Setea NULL si no hay HC asociada
            }

            ps.setBoolean(i++, entidad.isEliminado());

            int affectedRows = ps.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Fallo al crear el Paciente, no se modificaron filas.");
            }

            // Obtener el ID autogenerado para el Paciente
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    entidad.setId(rs.getLong(1));
                    // System.out.println("DEBUG: Paciente creado con ID: " + entidad.getId());
                } else {
                    throw new SQLException("Fallo al crear el Paciente, no se obtuvo ID generado.");
                }
            }

            return entidad;
        }
    }

    /**
     * Lee un Paciente por su ID (solo registros activos: eliminado = FALSE).
     * Incluye los datos de la HistoriaClinica asociada a través de un LEFT JOIN.
     * Nota: Este método maneja su propia conexión, no es transaccional.
     * @param id El ID del Paciente a buscar.
     * @return El Paciente encontrado o null si no existe.
     * @throws SQLException Si ocurre un error de acceso a la base de datos.
     */
    @Override
    public Paciente leer(long id) throws SQLException {
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
                try { conn.close(); } catch (SQLException ignore) {} // Cierre de conexión local
            }
        }
    }
    
    /**
     * Retorna una lista de todos los Pacientes que están activos (eliminado = FALSE).
     * Incluye los datos de la HistoriaClinica asociada a cada paciente.
     * Nota: Este método maneja su propia conexión, no es transaccional.
     * @return Lista de Pacientes activos.
     * @throws SQLException Si ocurre un error de acceso a la base de datos.
     */
    @Override
    public List<Paciente> leerTodos() throws SQLException {
        List<Paciente> lista = new ArrayList<>();
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
                try { conn.close(); } catch (SQLException ignore) {} // Cierre de conexión local
            }
        }
    }

    /**
     * Método adicional para buscar un Paciente por su número de DNI (solo activos).
     * Nota: Este método maneja su propia conexión, no es transaccional.
     * @param dni Número de DNI del paciente a buscar.
     * @return El Paciente encontrado o null si no existe.
     * @throws SQLException Si ocurre un error de acceso a la base de datos.
     */
    public Paciente buscarPorDni(String dni) throws SQLException {
        Connection conn = DatabaseConnection.getConnection();

        try (
            PreparedStatement ps = conn.prepareStatement(SELECT_BY_DNI_SQL);
        ) {
            ps.setString(1, dni);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearEntidad(rs);
                }
                return null;
            }
        } finally {
            if (conn != null) {
                try { conn.close(); } catch (SQLException ignore) {} // Cierre de conexión local
            }
        }
    }
    
    // --- MÉTODOS DE MANEJO DE ELIMINADOS (Implementan GenericDao) ---
    
    /**
     * Obtiene la lista completa de Pacientes que están marcados como eliminados lógicamente (eliminado = TRUE).
     * @return Lista de Pacientes eliminados.
     * @throws SQLException Si ocurre un error al acceder a la base de datos.
     */
    @Override
    public List<Paciente> leerTodosEliminados() throws SQLException {
        List<Paciente> lista = new ArrayList<>();
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
                try { conn.close(); } catch (SQLException ignore) {} // Cierre de conexión local
            }
        }
    }

    /**
     * Cuenta la cantidad de registros de Paciente que están marcados como eliminados lógicamente (eliminado = TRUE).
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
            return 0; // Si no hay registros, retorna 0
        } finally {
            if (conn != null) {
                try { conn.close(); } catch (SQLException ignore) {} // Cierre de conexión local
            }
        }
    }

    /**
     * Actualiza los datos de un Paciente existente, incluyendo la posibilidad de cambiar su HistoriaClinica asociada (FK).
     * Este método es transaccional.
     * @param entidad Objeto Paciente con los datos a actualizar.
     * @param conn Conexión compartida para la transacción activa.
     * @throws SQLException Si ocurre un error de acceso a la base de datos o si el ID es inválido.
     */
    @Override
    public void actualizar(Paciente entidad, Connection conn) throws SQLException {
        if (entidad.getId() <= 0) {
            throw new SQLException("El ID del Paciente es inválido para la actualización.");
        }

        try (PreparedStatement ps = conn.prepareStatement(UPDATE_SQL)) {

            int i = 1;
            ps.setString(i++, entidad.getNombre());
            ps.setString(i++, entidad.getApellido());
            ps.setString(i++, entidad.getDni());

            // 1. Manejo de Fecha de Nacimiento
            if (entidad.getFechaNacimiento() != null) {
                ps.setDate(i++, java.sql.Date.valueOf(entidad.getFechaNacimiento()));
            } else {
                ps.setNull(i++, java.sql.Types.DATE);
            }

            // 2. Manejo de la Foreign Key (FK) a HistoriaClinica
            HistoriaClinica hc = entidad.getHistoriaClinica();
            if (hc != null && hc.getId() > 0) {
                ps.setLong(i++, hc.getId());
            } else {
                ps.setNull(i++, java.sql.Types.BIGINT);
            }

            ps.setLong(i++, entidad.getId()); // Parámetro para la cláusula WHERE

            int rows = ps.executeUpdate();
            if (rows == 0) {
                throw new SQLException("No se pudo actualizar el Paciente con ID: " + entidad.getId());
            }
        }
    }

    /**
     * Realiza la baja lógica (Soft Delete) de un Paciente, estableciendo 'eliminado = TRUE'.
     * Este método es transaccional.
     * @param id El ID del Paciente a eliminar.
     * @param conn Conexión compartida para la transacción activa.
     * @throws SQLException Si ocurre un error de acceso a la base de datos o si no se encuentra el Paciente activo.
     */
    @Override
    public void eliminar(long id, Connection conn) throws SQLException {
        if (id <= 0) {
            throw new SQLException("El ID del Paciente es inválido para la eliminación.");
        }
        try (PreparedStatement ps = conn.prepareStatement(DELETE_SQL)) {
            ps.setLong(1, id);
            int rows = ps.executeUpdate();
            if (rows == 0) {
                throw new SQLException("No se encontró Paciente activo con ID: " + id);
            }
        }
    }

    /**
     * Recupera un Paciente eliminado lógicamente, estableciendo 'eliminado = FALSE'.
     * Este método es transaccional.
     * @param id El ID del Paciente a recuperar.
     * @param conn Conexión compartida para la transacción activa.
     * @throws SQLException Si ocurre un error de acceso a la base de datos o si no se encuentra el Paciente eliminado.
     */
    @Override
    public void recuperar(long id, Connection conn) throws SQLException {
        if (id <= 0) {
            throw new SQLException("El ID del Paciente es inválido para la recuperación.");
        }
        try (PreparedStatement ps = conn.prepareStatement(RECOVER_SQL)) {
            ps.setLong(1, id);
            int rows = ps.executeUpdate();
            if (rows == 0) {
                throw new SQLException("No se encontró Paciente (marcado como eliminado) con ID: " + id);
            }
        }
    }
}