/*
 * DAO para la entidad Paciente.
 * Implementa GenericDao y maneja la relación 1:1 con HistoriaClinica.
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

    // Constantes SQL
    // Nota: fk_historia_clinica es la clave foránea a la tabla historia_clinica
    private static final String INSERT_SQL = "INSERT INTO paciente (nombre, apellido, dni, fecha_nacimiento, fk_historia_clinica, eliminado) VALUES (?, ?, ?, ?, ?, ?)";
    private static final String SELECT_BY_ID_SQL = "SELECT p.*, hc.nro_historia, hc.grupo_sanguineo, hc.antecedentes, hc.medicacion_actual, hc.observaciones, hc.eliminado as hc_eliminado FROM paciente p LEFT JOIN historia_clinica hc ON p.fk_historia_clinica = hc.id WHERE p.id = ? AND p.eliminado = FALSE";
    private static final String UPDATE_SQL = "UPDATE paciente SET nombre = ?, apellido = ?, dni = ?, fecha_nacimiento = ?, fk_historia_clinica = ? WHERE id = ?";
    private static final String DELETE_SQL = "UPDATE paciente SET eliminado = TRUE WHERE id = ?";
    private static final String RECOVER_SQL = "UPDATE paciente SET eliminado = FALSE WHERE id = ?"; // NUEVO SQL para recuperación
    private static final String SELECT_ALL_ACTIVE_SQL = "SELECT p.*, hc.nro_historia, hc.grupo_sanguineo, hc.antecedentes, hc.medicacion_actual, hc.observaciones, hc.eliminado as hc_eliminado FROM paciente p LEFT JOIN historia_clinica hc ON p.fk_historia_clinica = hc.id WHERE p.eliminado = FALSE";
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final boolean REQUIRED_TRANSACTION = true;

    /**
     * Mapea un ResultSet a un objeto Paciente, incluyendo la HistoriaClinica asociada (si existe).
     * @param rs ResultSet de la consulta.
     * @return Objeto Paciente.
     * @throws SQLException Si hay un error al leer los datos.
     */
    private Paciente mapResultSetToEntity(ResultSet rs) throws SQLException {
        // 1. Mapear Paciente
        long id = rs.getLong("id");
        String nombre = rs.getString("nombre");
        String apellido = rs.getString("apellido");
        String dni = rs.getString("dni");
        
        // Manejo de fechas
        LocalDate fechaNacimiento = null;
        if (rs.getDate("fecha_nacimiento") != null) {
            fechaNacimiento = rs.getDate("fecha_nacimiento").toLocalDate();
        }
        
        boolean eliminado = rs.getBoolean("eliminado");
        
        Paciente paciente = new Paciente(id, eliminado, nombre, apellido, dni, fechaNacimiento);
        
        // 2. Mapear HistoriaClinica si existe
        long fkHistoriaClinica = rs.getLong("fk_historia_clinica");
        
        // Verificar si la columna fk_historia_clinica es NULL
        if (rs.wasNull() || fkHistoriaClinica == 0) {
            paciente.setHistoriaClinica(null);
        } else {
            // Asumiendo que la consulta SELECT_BY_ID_SQL o SELECT_ALL_ACTIVE_SQL 
            // trae los campos de HistoriaClinica (JOIN).
            
            // Los campos de HC tienen el mismo nombre en el RS que en la tabla HC
            // Por ejemplo: id (de Paciente), nro_historia, grupo_sanguineo, etc.
            
            // Nota: Para la HC, se necesita el ID que es la FK
            String nroHistoria = rs.getString("nro_historia");
            TipoSangre grupoSanguineo = TipoSangre.fromDbValue(rs.getString("grupo_sanguineo")); 
            String antecedentes = rs.getString("antecedentes");
            String medicacionActual = rs.getString("medicacion_actual");
            String observaciones = rs.getString("observaciones");
            boolean hcEliminado = rs.getBoolean("hc_eliminado"); // Alias para evitar colisión con paciente.eliminado

            HistoriaClinica hc = new HistoriaClinica(fkHistoriaClinica, hcEliminado, nroHistoria, grupoSanguineo, antecedentes, medicacionActual, observaciones);
            paciente.setHistoriaClinica(hc);
        }

        return paciente;
    }

    @Override
    public Paciente crearEntidad(Paciente entidad) throws SQLException {
        try (Connection conn = TransactionManager.getConnection(REQUIRED_TRANSACTION);
             PreparedStatement ps = conn.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {

            int i = 1;
            ps.setString(i++, entidad.getNombre());
            ps.setString(i++, entidad.getApellido());
            ps.setString(i++, entidad.getDni());
            
            // Manejo de LocalDate
            if (entidad.getFechaNacimiento() != null) {
                ps.setDate(i++, java.sql.Date.valueOf(entidad.getFechaNacimiento()));
            } else {
                ps.setNull(i++, java.sql.Types.DATE);
            }
            
            // Manejo de la FK (HistoriaClinica)
            if (entidad.getHistoriaClinica() != null && entidad.getHistoriaClinica().getId() > 0) {
                ps.setLong(i++, entidad.getHistoriaClinica().getId());
            } else {
                ps.setNull(i++, java.sql.Types.BIGINT);
            }
            
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
        } // Cierre automático/gestionado por TransactionManager
        return entidad;
    }

    @Override
    public Paciente leerEntidad(long id) throws SQLException {
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
            
            // Manejo de LocalDate
            if (entidad.getFechaNacimiento() != null) {
                ps.setDate(i++, java.sql.Date.valueOf(entidad.getFechaNacimiento()));
            } else {
                ps.setNull(i++, java.sql.Types.DATE);
            }
            
            // Manejo de la FK (HistoriaClinica)
            if (entidad.getHistoriaClinica() != null && entidad.getHistoriaClinica().getId() > 0) {
                ps.setLong(i++, entidad.getHistoriaClinica().getId());
            } else {
                ps.setNull(i++, java.sql.Types.BIGINT);
            }
            
            ps.setLong(i++, entidad.getId()); // WHERE id = ?

            ps.executeUpdate();
            
        } // Cierre automático/gestionado por TransactionManager
    }

    @Override
    public void eliminarEntidad(long id) throws SQLException {
        if (id <= 0) {
            throw new SQLException("El ID del Paciente es inválido para la eliminación.");
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
            throw new SQLException("El ID del Paciente es inválido para la recuperación.");
        }
        // Recuperación LÓGICA: se establece 'eliminado = FALSE'
        try (Connection conn = TransactionManager.getConnection(REQUIRED_TRANSACTION);
             PreparedStatement ps = conn.prepareStatement(RECOVER_SQL)) {
            
            ps.setLong(1, id);
            ps.executeUpdate();
            
        } // Cierre automático/gestionado por TransactionManager
    }


    @Override
    public List<Paciente> leerTodo() throws SQLException {
        List<Paciente> lista = new ArrayList<>();
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
     * Busca un paciente por su número de DNI.
     * @param dni El DNI a buscar.
     * @return Paciente encontrado (activo), o null.
     * @throws SQLException Si ocurre un error de acceso a la base de datos.
     */
    public Paciente buscarPorDni(String dni) throws SQLException {
        final String SELECT_BY_DNI_SQL = "SELECT p.*, hc.nro_historia, hc.grupo_sanguineo, hc.antecedentes, hc.medicacion_actual, hc.observaciones, hc.eliminado as hc_eliminado FROM paciente p LEFT JOIN historia_clinica hc ON p.fk_historia_clinica = hc.id WHERE p.dni = ? AND p.eliminado = FALSE";
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