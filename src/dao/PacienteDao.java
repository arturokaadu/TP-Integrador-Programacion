/*
 * Implementación del patrón Data Access Object (DAO) para la entidad Paciente.
 * Se encarga de la persistencia de los objetos Paciente en la base de datos,
 * incluyendo el mapeo de la relación 1:1 con HistoriaClinica.
 */
package dao;

import config.TransactionManager;
import entities.Paciente;
import entities.HistoriaClinica;
import entities.TipoSangre;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class PacienteDao implements GenericDao<Paciente> {

    // --- Constantes SQL para Operaciones CRUD ---

    /** SQL para la inserción de un nuevo Paciente. */
    private static final String INSERT_SQL = "INSERT INTO paciente (nombre, apellido, dni, fecha_nacimiento, fk_historia_clinica, eliminado) VALUES (?, ?, ?, ?, ?, ?)";
    
    /** * SQL para leer un Paciente activo por ID. 
     * Utiliza LEFT JOIN para traer los datos de HistoriaClinica asociados.
     * hc.eliminado se utiliza como alias 'hc_eliminado' para evitar colisión de nombres.
     */
    private static final String SELECT_BY_ID_SQL = "SELECT p.*, hc.nro_historia, hc.grupo_sanguineo, hc.antecedentes, hc.medicacion_actual, hc.observaciones, hc.eliminado as hc_eliminado FROM paciente p LEFT JOIN historia_clinica hc ON p.fk_historia_clinica = hc.id WHERE p.id = ? AND p.eliminado = FALSE";
    
    /** SQL para la actualización de los campos principales del Paciente y su FK de HistoriaClinica. */
    private static final String UPDATE_SQL = "UPDATE paciente SET nombre = ?, apellido = ?, dni = ?, fecha_nacimiento = ?, fk_historia_clinica = ? WHERE id = ?";
    
    /** SQL para la eliminación lógica del Paciente (marcando 'eliminado' como TRUE). */
    private static final String DELETE_SQL = "UPDATE paciente SET eliminado = TRUE WHERE id = ?";
    
    /** SQL para la recuperación lógica del Paciente (marcando 'eliminado' como FALSE). */
    private static final String RECOVER_SQL = "UPDATE paciente SET eliminado = FALSE WHERE id = ?"; 
    
    /** * SQL para seleccionar todos los Pacientes activos.
     * Incluye LEFT JOIN con HistoriaClinica para obtener la información completa.
     */
    private static final String SELECT_ALL_ACTIVE_SQL = "SELECT p.*, hc.nro_historia, hc.grupo_sanguineo, hc.antecedentes, hc.medicacion_actual, hc.observaciones, hc.eliminado as hc_eliminado FROM paciente p LEFT JOIN historia_clinica hc ON p.fk_historia_clinica = hc.id WHERE p.eliminado = FALSE";
    
    // --- Utilidades y Configuración ---

    /** Formateador estándar para convertir fechas a y desde la base de datos (yyyy-MM-dd). */
    private static final DateTimeFormatter FORMATO_FECHA_DB = DateTimeFormatter.ofPattern("yyyy-MM-dd"); // <-- CAMBIO DE NOMBRE
    
    /** Bandera que indica si los métodos de modificación requieren estar dentro de una transacción. */
    private static final boolean REQUIRED_TRANSACTION = true;

    /**
     * Mapea un ResultSet (resultado de la consulta DB) a un objeto Paciente.
     * Incluye la lógica para mapear la HistoriaClinica asociada (si existe a través del LEFT JOIN).
     * * @param rs ResultSet de la consulta SQL ejecutada.
     * @return Objeto Paciente completamente poblado.
     * @throws SQLException Si hay un error al leer los datos del ResultSet.
     */
    private Paciente mapResultSetToEntity(ResultSet rs) throws SQLException {
        // 1. Mapear datos de Paciente
        long id = rs.getLong("id");
        String nombre = rs.getString("nombre");
        String apellido = rs.getString("apellido");
        String dni = rs.getString("dni");
        
        // Manejo seguro de fecha de nacimiento (puede ser nula)
        LocalDate fechaNacimiento = null;
        if (rs.getDate("fecha_nacimiento") != null) {
            fechaNacimiento = rs.getDate("fecha_nacimiento").toLocalDate();
        }
        
        boolean eliminado = rs.getBoolean("eliminado");
        
        Paciente paciente = new Paciente(id, eliminado, nombre, apellido, dni, fechaNacimiento);
        
        // 2. Mapear HistoriaClinica si la FK no es nula
        long fkHistoriaClinica = rs.getLong("fk_historia_clinica");
        
        // La conexión puede ser NULL o 0 si el LEFT JOIN no encontró coincidencia
        if (rs.wasNull() || fkHistoriaClinica == 0) {
            paciente.setHistoriaClinica(null);
        } else {
            // Mapear campos de HistoriaClinica (que vienen con el JOIN)
            String nroHistoria = rs.getString("nro_historia");
            
            // Mapeo seguro para TipoSangre (maneja valores NULL o vacíos de la DB)
            TipoSangre grupoSanguineo = null;
            String dbGrupoSanguineo = rs.getString("grupo_sanguineo");
            if (dbGrupoSanguineo != null && !dbGrupoSanguineo.isEmpty()) {
                 grupoSanguineo = TipoSangre.fromDbValue(dbGrupoSanguineo); 
            }
            
            String antecedentes = rs.getString("antecedentes");
            String medicacionActual = rs.getString("medicacion_actual");
            String observaciones = rs.getString("observaciones");
            // Usamos el alias 'hc_eliminado' del SQL
            boolean hcEliminado = rs.getBoolean("hc_eliminado"); 

            HistoriaClinica hc = new HistoriaClinica(fkHistoriaClinica, hcEliminado, nroHistoria, grupoSanguineo, antecedentes, medicacionActual, observaciones);
            paciente.setHistoriaClinica(hc);
        }

        return paciente;
    }

    /**
     * Inserta un nuevo objeto Paciente en la base de datos.
     * Requiere una transacción activa para asegurar la integridad.
     * * @param entidad Objeto Paciente a insertar.
     * @return El objeto Paciente con su ID generado por la base de datos asignado.
     * @throws SQLException Si ocurre un error de acceso a la base de datos.
     */
    @Override
    public Paciente crearEntidad(Paciente entidad) throws SQLException {
        // Obtenemos la conexión, asegurando que haya una transacción (REQUIRED_TRANSACTION = true)
        try (Connection conn = TransactionManager.getConnection(REQUIRED_TRANSACTION);
             PreparedStatement ps = conn.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {

            int i = 1;
            ps.setString(i++, entidad.getNombre());
            ps.setString(i++, entidad.getApellido());
            ps.setString(i++, entidad.getDni());
            
            // Manejo seguro de fecha de nacimiento
            if (entidad.getFechaNacimiento() != null) {
                ps.setDate(i++, java.sql.Date.valueOf(entidad.getFechaNacimiento()));
            } else {
                ps.setNull(i++, java.sql.Types.DATE);
            }
            
            // Manejo seguro de la FK (HistoriaClinica) <-- INICIO CAMBIO
            HistoriaClinica hc = entidad.getHistoriaClinica();
            if (hc != null && hc.getId() > 0) {
                ps.setLong(i++, hc.getId());
            } else {
                ps.setNull(i++, java.sql.Types.BIGINT);
            } // <-- FIN CAMBIO
            
            ps.setBoolean(i++, entidad.isEliminado());

            int affectedRows = ps.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Fallo al crear el Paciente, no se modificaron filas.");
            }

            // Obtener el ID autogenerado
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    entidad.setId(rs.getLong(1));
                    System.out.println("DEBUG: Paciente creado con ID: " + entidad.getId());
                } else {
                    throw new SQLException("Fallo al crear el Paciente, no se obtuvo ID generado.");
                }
            }
        } // Cierre automático de recursos y gestión de conexión por TransactionManager
        return entidad;
    }

    /**
     * Lee un Paciente activo por su ID.
     * No requiere transacción (REQUIRED_TRANSACTION = false) ya que es una operación de solo lectura.
     * * @param id El ID del Paciente a buscar.
     * @return El objeto Paciente si se encuentra (activo), o null.
     * @throws SQLException Si ocurre un error de acceso a la base de datos.
     */
    @Override
    public Paciente leerEntidad(long id) throws SQLException {
        // Obtenemos la conexión sin requerir una transacción
        try (Connection conn = TransactionManager.getConnection(false);
             PreparedStatement ps = conn.prepareStatement(SELECT_BY_ID_SQL)) {
            
            ps.setLong(1, id);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToEntity(rs);
                }
            }
        }
        return null; // Retorna null si no se encuentra la entidad o está eliminada
    }

    /**
     * Actualiza un Paciente existente en la base de datos.
     * Requiere una transacción activa.
     * * @param entidad Objeto Paciente a actualizar.
     * @throws SQLException Si ocurre un error de acceso a la base de datos o el ID es inválido.
     */
    @Override
    public void actualizarEntidad(Paciente entidad) throws SQLException {
        if (entidad.getId() <= 0) {
            throw new SQLException("El ID del Paciente es inválido para la actualización.");
        }
        
        try (Connection conn = TransactionManager.getConnection(REQUIRED_TRANSACTION);
             PreparedStatement ps = conn.prepareStatement(UPDATE_SQL)) {

            int i = 1;
            ps.setString(i++, entidad.getNombre());
            ps.setString(i++, entidad.getApellido());
            ps.setString(i++, entidad.getDni());
            
            // Manejo seguro de fecha de nacimiento
            if (entidad.getFechaNacimiento() != null) {
                ps.setDate(i++, java.sql.Date.valueOf(entidad.getFechaNacimiento()));
            } else {
                ps.setNull(i++, java.sql.Types.DATE);
            }
            
            // Manejo seguro de la FK (HistoriaClinica) <-- INICIO CAMBIO
            HistoriaClinica hc = entidad.getHistoriaClinica();
            if (hc != null && hc.getId() > 0) {
                ps.setLong(i++, hc.getId());
            } else {
                ps.setNull(i++, java.sql.Types.BIGINT);
            } // <-- FIN CAMBIO
            
            ps.setLong(i++, entidad.getId()); // Parámetro para la cláusula WHERE

            ps.executeUpdate();
            
        } // Cierre automático/gestionado por TransactionManager
    }

    /**
     * Realiza la eliminación lógica de un Paciente por su ID, marcando 'eliminado' como TRUE.
     * Requiere una transacción activa.
     * * @param id El ID del Paciente a eliminar.
     * @throws SQLException Si ocurre un error de acceso a la base de datos o el ID es inválido.
     */
    @Override
    public void eliminarEntidad(long id) throws SQLException {
        if (id <= 0) {
            throw new SQLException("El ID del Paciente es inválido para la eliminación.");
        }
        
        try (Connection conn = TransactionManager.getConnection(REQUIRED_TRANSACTION);
             PreparedStatement ps = conn.prepareStatement(DELETE_SQL)) {
            
            ps.setLong(1, id);
            ps.executeUpdate();
            
        } // Cierre automático/gestionado por TransactionManager
    }
    
    /**
     * Recupera lógicamente una entidad por su ID, marcando 'eliminado' como FALSE.
     * Requiere una transacción activa.
     * * @param id El ID de la entidad a recuperar.
     * @throws SQLException Si ocurre un error de acceso a la base de datos o el ID es inválido.
     */
    @Override
    public void recuperarEntidad(long id) throws SQLException {
        if (id <= 0) {
            throw new SQLException("El ID del Paciente es inválido para la recuperación.");
        }
        
        try (Connection conn = TransactionManager.getConnection(REQUIRED_TRANSACTION);
             PreparedStatement ps = conn.prepareStatement(RECOVER_SQL)) {
            
            ps.setLong(1, id);
            ps.executeUpdate();
            
        } // Cierre automático/gestionado por TransactionManager
    }


    /**
     * Retorna una lista de todos los Pacientes activos (eliminado = FALSE).
     * No requiere transacción.
     * * @return Una lista de objetos Paciente.
     * @throws SQLException Si ocurre un error de acceso a la base de datos.
     */
    @Override
    public List<Paciente> leerTodo() throws SQLException {
        List<Paciente> lista = new ArrayList<>();
        // Obtenemos la conexión sin requerir una transacción
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
     * Busca un paciente activo por su número de DNI.
     * * @param dni El DNI del Paciente a buscar.
     * @return El objeto Paciente encontrado (activo), o null si no existe.
     * @throws SQLException Si ocurre un error de acceso a la base de datos.
     */
    public Paciente buscarPorDni(String dni) throws SQLException {
        final String SELECT_BY_DNI_SQL = "SELECT p.*, hc.nro_historia, hc.grupo_sanguineo, hc.antecedentes, hc.medicacion_actual, hc.observaciones, hc.eliminado as hc_eliminado FROM paciente p LEFT JOIN historia_clinica hc ON p.fk_historia_clinica = hc.id WHERE p.dni = ? AND p.eliminado = FALSE";
        // Obtenemos la conexión sin requerir una transacción
        try (Connection conn = TransactionManager.getConnection(false);
             PreparedStatement ps = conn.prepareStatement(SELECT_BY_DNI_SQL)) {

            ps.setString(1, dni);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToEntity(rs);
                }
            }
        }
        return null;
    }
}