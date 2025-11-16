/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 * @author A-monardes
 */

package services;

import dao.PacienteDao;
import entities.Paciente;
import entities.HistoriaClinica;
import entities.TipoSangre;
import config.TransactionManager;
import config.DatabaseConnection;
import dao.HistoriaClinicaDao;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

public class PacienteService implements GenericService<Paciente> {

    private PacienteDao pacienteDao;

    public PacienteService() {
        this.pacienteDao = new PacienteDao();
    }

    // IMPLEMENTACIÓN DE GenericService 

    @Override
    public void insertar(Paciente paciente) throws Exception {
        try (TransactionManager tm = new TransactionManager(DatabaseConnection.getConnection())) {
            tm.startTransaction();
            
            try {
                // Validaciones básicas
                validarPaciente(paciente);
                
                // Verificar DNI único
                if (pacienteDao.buscarPorDni(paciente.getDni()) != null) {
                    throw new SQLException("Ya existe un paciente con DNI: " + paciente.getDni());
                }
                
                pacienteDao.crear(paciente, tm.getConnection());
                tm.commit();
                System.out.println("✅ Transacción completada - Paciente creado");
                
            } catch (Exception e) {
                tm.rollback();
                System.out.println("❌ Transacción revertida - Rollback realizado");
                throw e;
            }
            
        } catch (SQLException e) {
            throw new Exception("Error al crear paciente: " + e.getMessage(), e);
        }
    }

    @Override
    public void actualizar(Paciente paciente) throws Exception {
        try (TransactionManager tm = new TransactionManager(DatabaseConnection.getConnection())) {
            tm.startTransaction();
            
            try {
                // Validaciones básicas
                validarPaciente(paciente);
                
                // Verificar que el paciente existe
                Paciente existente = pacienteDao.leer(paciente.getId());
                if (existente == null) {
                    throw new SQLException("No existe paciente con ID: " + paciente.getId());
                }
                
                // Verificar DNI único (excluyendo el paciente actual)
                Paciente porDni = pacienteDao.buscarPorDni(paciente.getDni());
                if (porDni != null && porDni.getId() != paciente.getId()) {
                    throw new SQLException("Ya existe otro paciente con DNI: " + paciente.getDni());
                }
                
                pacienteDao.actualizar(paciente, tm.getConnection());
                tm.commit();
                System.out.println("✅ Transacción completada - Paciente actualizado");
                
            } catch (Exception e) {
                tm.rollback();
                System.out.println("❌ Transacción revertida - Rollback realizado");
                throw e;
            }
            
        } catch (SQLException e) {
            throw new Exception("Error al actualizar paciente: " + e.getMessage(), e);
        }
    }

    @Override
    public void eliminar(Long id) throws Exception {
        try (TransactionManager tm = new TransactionManager(DatabaseConnection.getConnection())) {
            tm.startTransaction();
            
            try {
                if (id <= 0) {
                    throw new IllegalArgumentException("ID de paciente inválido: " + id);
                }                
                // Verificar que existe antes de eliminar
                Paciente existente = pacienteDao.leer(id);
                if (existente == null) {
                    throw new SQLException("No existe paciente con ID: " + id);
                }
                
                pacienteDao.eliminar(id, tm.getConnection());
                tm.commit();
                System.out.println("✅ Transacción completada - Paciente eliminado");
                
            } catch (Exception e) {
                tm.rollback();
                System.out.println("❌ Transacción revertida - Rollback realizado");
                throw e;
            }
            
        } catch (SQLException e) {
            throw new Exception("Error al eliminar paciente: " + e.getMessage(), e);
        }
    }

    @Override
    public Paciente getById(Long id) throws Exception {
        try {
            if (id <= 0) {
                throw new IllegalArgumentException("ID de paciente inválido: " + id);
            }
            return pacienteDao.leer(id);
        } catch (SQLException e) {
            throw new Exception("Error al buscar paciente por ID: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Paciente> getAll() throws Exception {
        try {
            return pacienteDao.leerTodos();
        } catch (SQLException e) {
            throw new Exception("Error al obtener lista de pacientes: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Paciente> getAllDeleted() throws Exception {
        try {
            return pacienteDao.leerTodosEliminados();
        } catch (SQLException e) {
            throw new Exception("Error al obtener pacientes eliminados: " + e.getMessage(), e);
        }
    }

    @Override
    public long countDeleted() throws Exception {
        try {
            return pacienteDao.contarEliminados();
        } catch (SQLException e) {
            throw new Exception("Error al contar pacientes eliminados: " + e.getMessage(), e);
        }
    }

    @Override
    public void recuperar(Long id) throws Exception {
        try (TransactionManager tm = new TransactionManager(DatabaseConnection.getConnection())) {
            tm.startTransaction();
            
            try {
                if (id <= 0) {
                    throw new IllegalArgumentException("ID de paciente inválido: " + id);
                }
                
                // Verificar que existe entre los eliminados
                List<Paciente> eliminados = pacienteDao.leerTodosEliminados();
                boolean encontrado = eliminados.stream().anyMatch(p -> p.getId() == id);
                
                if (!encontrado) {
                    throw new SQLException("No se encontró paciente eliminado con ID: " + id);
                }
                
                pacienteDao.recuperar(id, tm.getConnection());
                tm.commit();
                System.out.println("✅ Transacción completada - Paciente recuperado");
                
            } catch (Exception e) {
                tm.rollback();
                System.out.println("❌ Transacción revertida - Rollback realizado");
                throw e;
            }
            
        } catch (SQLException e) {
            throw new Exception("Error al recuperar paciente: " + e.getMessage(), e);
        }
    }

    // MÉTODOS ADICIONALES ESPECÍFICOS 

    
     //Busca paciente por DNI
     
    public Paciente buscarPorDni(String dni) throws Exception {
        try {
            if (dni == null || dni.trim().isEmpty()) {
                throw new IllegalArgumentException("DNI no puede estar vacío");
            }
            return pacienteDao.buscarPorDni(dni.trim().toUpperCase());
        } catch (SQLException e) {
            throw new Exception("Error al buscar paciente por DNI: " + e.getMessage(), e);
        }
    }

    
     //Crea paciente con Historia Clínica opcional - TRANSACCIÓN ATÓMICA
     
    public void crearPacienteConHistoriaOpcional(Paciente paciente, HistoriaClinica historiaClinica) throws Exception {
        try (TransactionManager tm = new TransactionManager(DatabaseConnection.getConnection())) {
            tm.startTransaction();
            
            try {
                // Validar paciente
                validarPaciente(paciente);
                
                // Verificar DNI único
                if (pacienteDao.buscarPorDni(paciente.getDni()) != null) {
                    throw new SQLException("Ya existe un paciente con DNI: " + paciente.getDni());
                }
                
                // Crear paciente
                Paciente pacienteCreado = pacienteDao.crear(paciente, tm.getConnection());
                System.out.println("📝 Paciente creado en transacción (pendiente de commit)");
                
                // Crear Historia Clínica si se proporciona
                if (historiaClinica != null) {
                    validarHistoriaClinica(historiaClinica);
                    
                    // Usar el DAO directamente para mantener la misma transacción
                    HistoriaClinicaDao hcDao = new HistoriaClinicaDao();
                    hcDao.crear(historiaClinica, tm.getConnection(), pacienteCreado.getId());
                    
                    // Asignar la historia clínica al paciente
                    pacienteCreado.setHistoriaClinica(historiaClinica);
                    System.out.println("📝 Historia Clínica creada en transacción (pendiente de commit)");
                }
                
                tm.commit(); // ✅ SOLO aquí se confirma TODO
                System.out.println("✅ Transacción completada - Paciente" + 
                    (historiaClinica != null ? " e Historia Clínica" : "") + " creados exitosamente");
                
            } catch (Exception e) {
                tm.rollback(); // ✅ Si algo falla, se revierte TODO
                System.out.println("❌ Transacción revertida - Rollback realizado. Nada se guardó en la BD");
                throw new Exception("Error en transacción: " + e.getMessage(), e);
            }
        }
    }

    // VALIDACIONES 

    private void validarPaciente(Paciente paciente) throws IllegalArgumentException {
        if (paciente.getNombre() == null || paciente.getNombre().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del paciente es obligatorio");
        }
        if (paciente.getApellido() == null || paciente.getApellido().trim().isEmpty()) {
            throw new IllegalArgumentException("El apellido del paciente es obligatorio");
        }
        if (paciente.getDni() == null || paciente.getDni().trim().isEmpty()) {
            throw new IllegalArgumentException("El DNI del paciente es obligatorio");
        }
        
        // Validar fecha de nacimiento (si está presente)
        if (paciente.getFechaNacimiento() != null) {
            if (paciente.getFechaNacimiento().isAfter(LocalDate.now())) {
                throw new IllegalArgumentException("La fecha de nacimiento no puede ser futura");
            }
            if (paciente.getFechaNacimiento().isBefore(LocalDate.of(1900, 1, 1))) {
                throw new IllegalArgumentException("La fecha de nacimiento no puede ser anterior a 1900");
            }
        }
    }

    private void validarHistoriaClinica(HistoriaClinica historiaClinica) throws IllegalArgumentException {
        if (historiaClinica.getNroHistoria() == null || historiaClinica.getNroHistoria().trim().isEmpty()) {
            throw new IllegalArgumentException("El número de historia clínica es obligatorio");
        }
        if (historiaClinica.getGrupoSanguineo() == null) {
            throw new IllegalArgumentException("El grupo sanguíneo es obligatorio");
        }
    }

    /**
     * Valida formato de fecha (para uso en AppMenu)
     */
    public static LocalDate validarFechaNacimiento(String fechaStr) throws DateTimeParseException {
        if (fechaStr == null || fechaStr.trim().isEmpty()) {
            return null;
        }
        return LocalDate.parse(fechaStr.trim());
    }
}
