/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 * @author A-monardes
 */
package services;

import dao.HistoriaClinicaDao;
import entities.HistoriaClinica;
import entities.TipoSangre;
import config.TransactionManager;
import config.DatabaseConnection;

import java.sql.SQLException;
import java.util.List;

public class HistoriaClinicaService implements GenericService<HistoriaClinica> {

    private HistoriaClinicaDao hcDao;

    public HistoriaClinicaService() {
        this.hcDao = new HistoriaClinicaDao();
    }

    //IMPLEMENTACIÓN DE GenericService

    @Override
    public void insertar(HistoriaClinica entidad) throws Exception {
        throw new UnsupportedOperationException("Use crearHistoriaClinicaConPaciente() con pacienteId en su lugar");
    }

    @Override
    public void actualizar(HistoriaClinica entidad) throws Exception {
        try (TransactionManager tm = new TransactionManager(DatabaseConnection.getConnection())) {
            tm.startTransaction();
            
            try {
                // Validaciones
                validarHistoriaClinica(entidad);
                
                // Verificar que existe
                HistoriaClinica existente = hcDao.leer(entidad.getId());
                if (existente == null) {
                    throw new SQLException("No existe historia clínica con ID: " + entidad.getId());
                }
                
                hcDao.actualizar(entidad, tm.getConnection());
                tm.commit();
                System.out.println("✅ Transacción completada - Historia Clínica actualizada");
                
            } catch (Exception e) {
                tm.rollback();
                System.out.println("❌ Transacción revertida - Rollback realizado");
                throw e;
            }
            
        } catch (SQLException e) {
            throw new Exception("Error al actualizar historia clínica: " + e.getMessage(), e);
        }
    }

    @Override
    public void eliminar(Long id) throws Exception {
        try (TransactionManager tm = new TransactionManager(DatabaseConnection.getConnection())) {
            tm.startTransaction();
            
            try {
                if (id <= 0) {
                    throw new IllegalArgumentException("ID de historia clínica inválido: " + id);
                }
                
                // Verificar que existe antes de eliminar
                HistoriaClinica existente = hcDao.leer(id);
                if (existente == null) {
                    throw new SQLException("No existe historia clínica con ID: " + id);
                }
                
                hcDao.eliminar(id, tm.getConnection());
                tm.commit();
                System.out.println("✅ Transacción completada - Historia Clínica eliminada");
                
            } catch (Exception e) {
                tm.rollback();
                System.out.println("❌ Transacción revertida - Rollback realizado");
                throw e;
            }
            
        } catch (SQLException e) {
            throw new Exception("Error al eliminar historia clínica: " + e.getMessage(), e);
        }
    }

    @Override
    public HistoriaClinica getById(Long id) throws Exception {
        try {
            if (id <= 0) {
                throw new IllegalArgumentException("ID de historia clínica inválido: " + id);
            }
            return hcDao.leer(id);
        } catch (SQLException e) {
            throw new Exception("Error al buscar historia clínica por ID: " + e.getMessage(), e);
        }
    }

    @Override
    public List<HistoriaClinica> getAll() throws Exception {
        try {
            return hcDao.leerTodos();
        } catch (SQLException e) {
            throw new Exception("Error al obtener lista de historias clínicas: " + e.getMessage(), e);
        }
    }

    @Override
    public List<HistoriaClinica> getAllDeleted() throws Exception {
        try {
            return hcDao.leerTodosEliminados();
        } catch (SQLException e) {
            throw new Exception("Error al obtener historias clínicas eliminadas: " + e.getMessage(), e);
        }
    }

    @Override
    public long countDeleted() throws Exception {
        try {
            return hcDao.contarEliminados();
        } catch (SQLException e) {
            throw new Exception("Error al contar historias clínicas eliminadas: " + e.getMessage(), e);
        }
    }

    @Override
    public void recuperar(Long id) throws Exception {
        try (TransactionManager tm = new TransactionManager(DatabaseConnection.getConnection())) {
            tm.startTransaction();
            
            try {
                if (id <= 0) {
                    throw new IllegalArgumentException("ID de historia clínica inválido: " + id);
                }            
                // Verificar que existe entre las eliminadas
                List<HistoriaClinica> eliminadas = hcDao.leerTodosEliminados();
                boolean encontrada = eliminadas.stream().anyMatch(hc -> hc.getId() == id);
                
                if (!encontrada) {
                    throw new SQLException("No se encontró historia clínica eliminada con ID: " + id);
                }
                
                hcDao.recuperar(id, tm.getConnection());
                tm.commit();
                System.out.println("✅ Transacción completada - Historia Clínica recuperada");
                
            } catch (Exception e) {
                tm.rollback();
                System.out.println("❌ Transacción revertida - Rollback realizado");
                throw e;
            }
            
        } catch (SQLException e) {
            throw new Exception("Error al recuperar historia clínica: " + e.getMessage(), e);
        }
    }

    //MÉTODOS ADICIONALES ESPECÍFICOS

    /**
     * Crea una nueva Historia Clinica asociada a un paciente existente
     * Para uso dentro de transacciones existentes
     */
    public HistoriaClinica crearHistoriaClinica(TransactionManager tm, HistoriaClinica historia, long pacienteId) throws SQLException {
        if (pacienteId <= 0) {
            throw new IllegalArgumentException("PacienteId inválido para la historia clínica.");
        }
        validarHistoriaClinica(historia);
        return hcDao.crear(historia, tm.getConnection(), pacienteId);
    }

    
     //Crea Historia Clínica con transacción propia
     
    public void crearHistoriaClinicaConPaciente(HistoriaClinica historia, long pacienteId) throws Exception {
        try (TransactionManager tm = new TransactionManager(DatabaseConnection.getConnection())) {
            tm.startTransaction();
            
            try {
                validarHistoriaClinica(historia);
                
                if (pacienteId <= 0) {
                    throw new IllegalArgumentException("PacienteId inválido: " + pacienteId);
                }
                
                hcDao.crear(historia, tm.getConnection(), pacienteId);
                tm.commit();
                System.out.println("✅ Transacción completada - Historia Clínica creada");
                
            } catch (Exception e) {
                tm.rollback();
                System.out.println("❌ Transacción revertida - Rollback realizado");
                throw e;
            }
            
        } catch (SQLException e) {
            throw new Exception("Error al crear historia clínica: " + e.getMessage(), e);
        }
    }

    //VALIDACIONES 

    private void validarHistoriaClinica(HistoriaClinica historiaClinica) throws IllegalArgumentException {
        if (historiaClinica.getNroHistoria() == null || historiaClinica.getNroHistoria().trim().isEmpty()) {
            throw new IllegalArgumentException("El número de historia clínica es obligatorio");
        }
        if (historiaClinica.getGrupoSanguineo() == null) {
            throw new IllegalArgumentException("El grupo sanguíneo es obligatorio");
        }
    }

    
     //Convierte string a TipoSangre (para uso en AppMenu)
     
    public static TipoSangre parseTipoSangre(String tipoSangreStr) throws IllegalArgumentException {
        if (tipoSangreStr == null || tipoSangreStr.trim().isEmpty()) {
            throw new IllegalArgumentException("El grupo sanguíneo no puede estar vacío");
        }
        return TipoSangre.fromDbValue(tipoSangreStr.trim().toUpperCase());
    }
}
