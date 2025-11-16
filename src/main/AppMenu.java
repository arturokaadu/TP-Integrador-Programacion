/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author A-monardes
 */
package main;

import services.PacienteService;
import services.HistoriaClinicaService;
import entities.Paciente;
import entities.HistoriaClinica;
import entities.TipoSangre;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

public class AppMenu {
    
    private Scanner scanner;
    private PacienteService pacienteService;
    private HistoriaClinicaService historiaClinicaService;
    
    public AppMenu() {
        this.scanner = new Scanner(System.in);
        this.pacienteService = new PacienteService();
        this.historiaClinicaService = new HistoriaClinicaService();
    }
    
    public void iniciar() {
        System.out.println("    SISTEMA DE GESTIÓN HOSPITALARIA");
        
        boolean salir = false;
        while (!salir) {
            mostrarMenuPrincipal();
            String opcion = scanner.nextLine().trim();
            
            try {
                switch (opcion) {
                    case "1":
                        menuGestionPacientes();
                        break;
                    case "2":
                        menuGestionHistoriasClinicas();
                        break;
                    case "3":
                        mostrarEstadisticas();
                        break;
                    case "0":
                        salir = true;
                        System.out.println("¡Hasta luego!");
                        break;
                    default:
                        System.out.println("❌ Opción inválida. Intente nuevamente.");
                }
            } catch (Exception e) {
                System.out.println("❌ Error: " + e.getMessage());
                System.out.println("Presione Enter para continuar...");
                scanner.nextLine();
            }
        }
    }
    
    private void mostrarMenuPrincipal() {
        System.out.println("\n--- MENÚ PRINCIPAL ---");
        System.out.println("1. Gestión de Pacientes");
        System.out.println("2. Gestión de Historias Clínicas");
        System.out.println("3. Estadísticas del Sistema");
        System.out.println("0. Salir");
        System.out.print("Seleccione una opción: ");
    }
    
    // ========== GESTIÓN DE PACIENTES ==========
    
    private void menuGestionPacientes() throws Exception {
        boolean volver = false;
        while (!volver) {
            System.out.println("\n--- GESTIÓN DE PACIENTES ---");
            System.out.println("1. Crear Paciente (con/sin Historia Clínica)");
            System.out.println("2. Modificar Paciente");
            System.out.println("3. Eliminar Paciente (lógico)");
            System.out.println("4. Recuperar Paciente eliminado");
            System.out.println("5. Buscar Paciente por ID");
            System.out.println("6. Buscar Paciente por DNI");
            System.out.println("7. Listar todos los Pacientes");
            System.out.println("8. Listar Pacientes eliminados");
            System.out.println("0. Volver al Menú Principal");
            System.out.print("Seleccione una opción: ");
            
            String opcion = scanner.nextLine().trim();
            
            switch (opcion) {
                case "1":
                    crearPaciente();
                    break;
                case "2":
                    modificarPaciente();
                    break;
                case "3":
                    eliminarPaciente();
                    break;
                case "4":
                    recuperarPaciente();
                    break;
                case "5":
                    buscarPacientePorId();
                    break;
                case "6":
                    buscarPacientePorDni();
                    break;
                case "7":
                    listarPacientes();
                    break;
                case "8":
                    listarPacientesEliminados();
                    break;
                case "0":
                    volver = true;
                    break;
                default:
                    System.out.println("❌ Opción inválida.");
            }
        }
    }
    
    private void crearPaciente() throws Exception {
        System.out.println("\n--- CREAR NUEVO PACIENTE ---");
        
        // Datos básicos del paciente
        System.out.print("Nombre: ");
        String nombre = scanner.nextLine().trim().toUpperCase();
        
        System.out.print("Apellido: ");
        String apellido = scanner.nextLine().trim().toUpperCase();
        
        System.out.print("DNI: ");
        String dni = scanner.nextLine().trim().toUpperCase();
        
        System.out.print("Fecha de nacimiento (YYYY-MM-DD) [opcional]: ");
        String fechaStr = scanner.nextLine().trim();
        LocalDate fechaNacimiento = null;
        if (!fechaStr.isEmpty()) {
            try {
                fechaNacimiento = PacienteService.validarFechaNacimiento(fechaStr);
            } catch (DateTimeParseException e) {
                System.out.println("❌ Formato de fecha inválido. Usar YYYY-MM-DD");
                return;
            }
        }
        
        // Crear paciente
        Paciente paciente = new Paciente();
        paciente.setNombre(nombre);
        paciente.setApellido(apellido);
        paciente.setDni(dni);
        paciente.setFechaNacimiento(fechaNacimiento);
        
        // Preguntar si quiere crear historia clínica
        System.out.print("¿Desea crear una Historia Clínica para este paciente? (S/N): ");
        String crearHC = scanner.nextLine().trim().toUpperCase();
        
        if (crearHC.equals("S") || crearHC.equals("SI")) {
            HistoriaClinica historiaClinica = capturarDatosHistoriaClinica();
            pacienteService.crearPacienteConHistoriaOpcional(paciente, historiaClinica);
            System.out.println("✅ Paciente creado exitosamente con Historia Clínica");
        } else {
            pacienteService.insertar(paciente);
            System.out.println("✅ Paciente creado exitosamente sin Historia Clínica");
        }
        
        System.out.println("Presione Enter para continuar...");
        scanner.nextLine();
    }
    
    private void modificarPaciente() throws Exception {
        System.out.println("\n--- MODIFICAR PACIENTE ---");
        
        System.out.print("ID del paciente a modificar: ");
        Long id = Long.parseLong(scanner.nextLine().trim());
        
        Paciente paciente = pacienteService.getById(id);
        if (paciente == null) {
            System.out.println("❌ No se encontró paciente con ID: " + id);
            return;
        }
        
        System.out.println("Paciente actual: " + paciente);
        System.out.println("Deje en blanco para mantener el valor actual");
        
        System.out.print("Nuevo Nombre [" + paciente.getNombre() + "]: ");
        String nombre = scanner.nextLine().trim();
        if (!nombre.isEmpty()) paciente.setNombre(nombre.toUpperCase());
        
        System.out.print("Nuevo Apellido [" + paciente.getApellido() + "]: ");
        String apellido = scanner.nextLine().trim();
        if (!apellido.isEmpty()) paciente.setApellido(apellido.toUpperCase());
        
        System.out.print("Nuevo DNI [" + paciente.getDni() + "]: ");
        String dni = scanner.nextLine().trim();
        if (!dni.isEmpty()) paciente.setDni(dni.toUpperCase());
        
        System.out.print("Nueva Fecha Nacimiento (YYYY-MM-DD) [" + 
            (paciente.getFechaNacimiento() != null ? paciente.getFechaNacimiento() : "vacío") + "]: ");
        String fechaStr = scanner.nextLine().trim();
        if (!fechaStr.isEmpty()) {
            try {
                paciente.setFechaNacimiento(PacienteService.validarFechaNacimiento(fechaStr));
            } catch (DateTimeParseException e) {
                System.out.println("❌ Formato de fecha inválido");
                return;
            }
        }
        
        pacienteService.actualizar(paciente);
        System.out.println("✅ Paciente modificado exitosamente");
        
        System.out.println("Presione Enter para continuar...");
        scanner.nextLine();
    }
    
    private void eliminarPaciente() throws Exception {
        System.out.println("\n--- ELIMINAR PACIENTE ---");
        
        System.out.print("ID del paciente a eliminar: ");
        Long id = Long.parseLong(scanner.nextLine().trim());
        
        Paciente paciente = pacienteService.getById(id);
        if (paciente == null) {
            System.out.println("❌ No se encontró paciente con ID: " + id);
            return;
        }
        
        System.out.println("¿Está seguro de eliminar al paciente: " + paciente.getNombre() + " " + paciente.getApellido() + "? (S/N): ");
        String confirmar = scanner.nextLine().trim().toUpperCase();
        
        if (confirmar.equals("S") || confirmar.equals("SI")) {
            pacienteService.eliminar(id);
            System.out.println("✅ Paciente eliminado exitosamente (eliminación lógica)");
        } else {
            System.out.println("❌ Eliminación cancelada");
        }
        
        System.out.println("Presione Enter para continuar...");
        scanner.nextLine();
    }
    
    private void recuperarPaciente() throws Exception {
        System.out.println("\n--- RECUPERAR PACIENTE ELIMINADO ---");
        
        System.out.print("ID del paciente a recuperar: ");
        Long id = Long.parseLong(scanner.nextLine().trim());
        
        // Verificar que existe entre los eliminados
        List<Paciente> eliminados = pacienteService.getAllDeleted();
        boolean encontrado = eliminados.stream().anyMatch(p -> p.getId() == id);
        
        if (!encontrado) {
            System.out.println("❌ No se encontró paciente eliminado con ID: " + id);
            return;
        }
        
        pacienteService.recuperar(id);
        System.out.println("✅ Paciente recuperado exitosamente");
        
        System.out.println("Presione Enter para continuar...");
        scanner.nextLine();
    }
    
    private void buscarPacientePorId() throws Exception {
        System.out.println("\n--- BUSCAR PACIENTE POR ID ---");
        
        System.out.print("ID del paciente: ");
        Long id = Long.parseLong(scanner.nextLine().trim());
        
        Paciente paciente = pacienteService.getById(id);
        if (paciente != null) {
            System.out.println("✅ Paciente encontrado:");
            System.out.println(paciente);
            if (paciente.getHistoriaClinica() != null) {
                System.out.println("Historia Clínica asociada: " + paciente.getHistoriaClinica());
            }
        } else {
            System.out.println("❌ No se encontró paciente con ID: " + id);
        }
        
        System.out.println("Presione Enter para continuar...");
        scanner.nextLine();
    }
    
    private void buscarPacientePorDni() throws Exception {
        System.out.println("\n--- BUSCAR PACIENTE POR DNI ---");
        
        System.out.print("DNI del paciente: ");
        String dni = scanner.nextLine().trim().toUpperCase();
        
        Paciente paciente = pacienteService.buscarPorDni(dni);
        if (paciente != null) {
            System.out.println("✅ Paciente encontrado:");
            System.out.println(paciente);
            if (paciente.getHistoriaClinica() != null) {
                System.out.println("Historia Clínica asociada: " + paciente.getHistoriaClinica());
            }
        } else {
            System.out.println("❌ No se encontró paciente con DNI: " + dni);
        }
        
        System.out.println("Presione Enter para continuar...");
        scanner.nextLine();
    }
    
    private void listarPacientes() throws Exception {
        System.out.println("\n--- LISTA DE TODOS LOS PACIENTES ---");
        
        List<Paciente> pacientes = pacienteService.getAll();
        long totalEliminados = pacienteService.countDeleted();
        
        if (pacientes.isEmpty()) {
            System.out.println("No hay pacientes activos en el sistema.");
        } else {
            System.out.println("Pacientes activos (" + pacientes.size() + "):");
            for (Paciente p : pacientes) {
                System.out.println("  " + p);
            }
        }
        
        System.out.println("\nTotal de pacientes eliminados: " + totalEliminados);
        
        System.out.println("Presione Enter para continuar...");
        scanner.nextLine();
    }
    
    private void listarPacientesEliminados() throws Exception {
        System.out.println("\n--- LISTA DE PACIENTES ELIMINADOS ---");
        
        List<Paciente> eliminados = pacienteService.getAllDeleted();
        
        if (eliminados.isEmpty()) {
            System.out.println("No hay pacientes eliminados en el sistema.");
        } else {
            System.out.println("Pacientes eliminados (" + eliminados.size() + "):");
            for (Paciente p : eliminados) {
                System.out.println("  " + p);
            }
        }
        
        System.out.println("Presione Enter para continuar...");
        scanner.nextLine();
    }
    
    // ========== GESTIÓN DE HISTORIAS CLÍNICAS ==========
    
    private void menuGestionHistoriasClinicas() throws Exception {
        boolean volver = false;
        while (!volver) {
            System.out.println("\n--- GESTIÓN DE HISTORIAS CLÍNICAS ---");
            System.out.println("1. Crear Historia Clínica (para paciente existente)");
            System.out.println("2. Modificar Historia Clínica");
            System.out.println("3. Eliminar Historia Clínica (lógico)");
            System.out.println("4. Recuperar Historia Clínica eliminada");
            System.out.println("5. Buscar Historia Clínica por ID");
            System.out.println("6. Listar todas las Historias Clínicas");
            System.out.println("7. Listar Historias Clínicas eliminadas");
            System.out.println("0. Volver al Menú Principal");
            System.out.print("Seleccione una opción: ");
            
            String opcion = scanner.nextLine().trim();
            
            switch (opcion) {
                case "1":
                    crearHistoriaClinica();
                    break;
                case "2":
                    modificarHistoriaClinica();
                    break;
                case "3":
                    eliminarHistoriaClinica();
                    break;
                case "4":
                    recuperarHistoriaClinica();
                    break;
                case "5":
                    buscarHistoriaClinicaPorId();
                    break;
                case "6":
                    listarHistoriasClinicas();
                    break;
                case "7":
                    listarHistoriasClinicasEliminadas();
                    break;
                case "0":
                    volver = true;
                    break;
                default:
                    System.out.println("❌ Opción inválida.");
            }
        }
    }
    
    private HistoriaClinica capturarDatosHistoriaClinica() {
        HistoriaClinica hc = new HistoriaClinica();
        
        System.out.print("Número de Historia Clínica: ");
        hc.setNroHistoria(scanner.nextLine().trim().toUpperCase());
        
        System.out.print("Grupo Sanguíneo (A+, A-, B+, B-, AB+, AB-, O+, O-): ");
        String grupoSangreStr = scanner.nextLine().trim();
        try {
            hc.setGrupoSanguineo(HistoriaClinicaService.parseTipoSangre(grupoSangreStr));
        } catch (IllegalArgumentException e) {
            System.out.println("❌ " + e.getMessage());
            return null;
        }
        
        System.out.print("Antecedentes [opcional]: ");
        String antecedentes = scanner.nextLine().trim();
        if (!antecedentes.isEmpty()) hc.setAntecedentes(antecedentes);
        
        System.out.print("Medicación Actual [opcional]: ");
        String medicacion = scanner.nextLine().trim();
        if (!medicacion.isEmpty()) hc.setMedicacionActual(medicacion);
        
        System.out.print("Observaciones [opcional]: ");
        String observaciones = scanner.nextLine().trim();
        if (!observaciones.isEmpty()) hc.setObservaciones(observaciones);
        
        return hc;
    }
    
    private void crearHistoriaClinica() throws Exception {
        System.out.println("\n--- CREAR HISTORIA CLÍNICA ---");
        
        // Primero buscar el paciente
        System.out.print("ID del paciente al que asignar la historia clínica: ");
        Long pacienteId = Long.parseLong(scanner.nextLine().trim());
        
        // Verificar que el paciente existe
        PacienteService ps = new PacienteService();
        Paciente paciente = ps.getById(pacienteId);
        if (paciente == null) {
            System.out.println("❌ No se encontró paciente con ID: " + pacienteId);
            return;
        }
        
        System.out.println("Paciente: " + paciente.getNombre() + " " + paciente.getApellido() + " (DNI: " + paciente.getDni() + ")");
        
        // Capturar datos de la historia clínica
        HistoriaClinica hc = capturarDatosHistoriaClinica();
        if (hc == null) return;
        
        historiaClinicaService.crearHistoriaClinicaConPaciente(hc, pacienteId);
        System.out.println("✅ Historia Clínica creada exitosamente para el paciente");
        
        System.out.println("Presione Enter para continuar...");
        scanner.nextLine();
    }
    
    private void modificarHistoriaClinica() throws Exception {
        System.out.println("\n--- MODIFICAR HISTORIA CLÍNICA ---");
        
        System.out.print("ID de la historia clínica a modificar: ");
        Long id = Long.parseLong(scanner.nextLine().trim());
        
        HistoriaClinica hc = historiaClinicaService.getById(id);
        if (hc == null) {
            System.out.println("❌ No se encontró historia clínica con ID: " + id);
            return;
        }
        
        System.out.println("Historia Clínica actual: " + hc);
        System.out.println("Deje en blanco para mantener el valor actual");
        
        System.out.print("Nuevo Número de Historia [" + hc.getNroHistoria() + "]: ");
        String nroHistoria = scanner.nextLine().trim();
        if (!nroHistoria.isEmpty()) hc.setNroHistoria(nroHistoria.toUpperCase());
        
        System.out.print("Nuevo Grupo Sanguíneo [" + hc.getGrupoSanguineo() + "]: ");
        String grupoSangreStr = scanner.nextLine().trim();
        if (!grupoSangreStr.isEmpty()) {
            try {
                hc.setGrupoSanguineo(HistoriaClinicaService.parseTipoSangre(grupoSangreStr));
            } catch (IllegalArgumentException e) {
                System.out.println("❌ " + e.getMessage());
                return;
            }
        }
        
        System.out.print("Nuevos Antecedentes [" + (hc.getAntecedentes() != null ? hc.getAntecedentes() : "vacío") + "]: ");
        String antecedentes = scanner.nextLine().trim();
        if (!antecedentes.isEmpty()) hc.setAntecedentes(antecedentes);
        
        System.out.print("Nueva Medicación Actual [" + (hc.getMedicacionActual() != null ? hc.getMedicacionActual() : "vacío") + "]: ");
        String medicacion = scanner.nextLine().trim();
        if (!medicacion.isEmpty()) hc.setMedicacionActual(medicacion);
        
        System.out.print("Nuevas Observaciones [" + (hc.getObservaciones() != null ? hc.getObservaciones() : "vacío") + "]: ");
        String observaciones = scanner.nextLine().trim();
        if (!observaciones.isEmpty()) hc.setObservaciones(observaciones);
        
        historiaClinicaService.actualizar(hc);
        System.out.println("✅ Historia Clínica modificada exitosamente");
        
        System.out.println("Presione Enter para continuar...");
        scanner.nextLine();
    }
    
    private void eliminarHistoriaClinica() throws Exception {
        System.out.println("\n--- ELIMINAR HISTORIA CLÍNICA ---");
        
        System.out.print("ID de la historia clínica a eliminar: ");
        Long id = Long.parseLong(scanner.nextLine().trim());
        
        HistoriaClinica hc = historiaClinicaService.getById(id);
        if (hc == null) {
            System.out.println("❌ No se encontró historia clínica con ID: " + id);
            return;
        }
        
        System.out.println("¿Está seguro de eliminar la historia clínica: " + hc.getNroHistoria() + "? (S/N): ");
        String confirmar = scanner.nextLine().trim().toUpperCase();
        
        if (confirmar.equals("S") || confirmar.equals("SI")) {
            historiaClinicaService.eliminar(id);
            System.out.println("✅ Historia Clínica eliminada exitosamente (eliminación lógica)");
        } else {
            System.out.println("❌ Eliminación cancelada");
        }
        
        System.out.println("Presione Enter para continuar...");
        scanner.nextLine();
    }
    
    private void recuperarHistoriaClinica() throws Exception {
        System.out.println("\n--- RECUPERAR HISTORIA CLÍNICA ELIMINADA ---");
        
        System.out.print("ID de la historia clínica a recuperar: ");
        Long id = Long.parseLong(scanner.nextLine().trim());
        
        // Verificar que existe entre las eliminadas
        List<HistoriaClinica> eliminadas = historiaClinicaService.getAllDeleted();
        boolean encontrada = eliminadas.stream().anyMatch(hc -> hc.getId() == id);
        
        if (!encontrada) {
            System.out.println("❌ No se encontró historia clínica eliminada con ID: " + id);
            return;
        }
        
        historiaClinicaService.recuperar(id);
        System.out.println("✅ Historia Clínica recuperada exitosamente");
        
        System.out.println("Presione Enter para continuar...");
        scanner.nextLine();
    }
    
    private void buscarHistoriaClinicaPorId() throws Exception {
        System.out.println("\n--- BUSCAR HISTORIA CLÍNICA POR ID ---");
        
        System.out.print("ID de la historia clínica: ");
        Long id = Long.parseLong(scanner.nextLine().trim());
        
        HistoriaClinica hc = historiaClinicaService.getById(id);
        if (hc != null) {
            System.out.println("✅ Historia Clínica encontrada:");
            System.out.println(hc);
        } else {
            System.out.println("❌ No se encontró historia clínica con ID: " + id);
        }
        
        System.out.println("Presione Enter para continuar...");
        scanner.nextLine();
    }
    
    private void listarHistoriasClinicas() throws Exception {
        System.out.println("\n--- LISTA DE TODAS LAS HISTORIAS CLÍNICAS ---");
        
        List<HistoriaClinica> historias = historiaClinicaService.getAll();
        long totalEliminadas = historiaClinicaService.countDeleted();
        
        if (historias.isEmpty()) {
            System.out.println("No hay historias clínicas activas en el sistema.");
        } else {
            System.out.println("Historias Clínicas activas (" + historias.size() + "):");
            for (HistoriaClinica hc : historias) {
                System.out.println("  " + hc);
            }
        }
        
        System.out.println("\nTotal de historias clínicas eliminadas: " + totalEliminadas);
        
        System.out.println("Presione Enter para continuar...");
        scanner.nextLine();
    }
    
    private void listarHistoriasClinicasEliminadas() throws Exception {
        System.out.println("\n--- LISTA DE HISTORIAS CLÍNICAS ELIMINADAS ---");
        
        List<HistoriaClinica> eliminadas = historiaClinicaService.getAllDeleted();
        
        if (eliminadas.isEmpty()) {
            System.out.println("No hay historias clínicas eliminadas en el sistema.");
        } else {
            System.out.println("Historias Clínicas eliminadas (" + eliminadas.size() + "):");
            for (HistoriaClinica hc : eliminadas) {
                System.out.println("  " + hc);
            }
        }
        
        System.out.println("Presione Enter para continuar...");
        scanner.nextLine();
    }
    
    // ESTADÍSTICAS
    
    private void mostrarEstadisticas() throws Exception {
        System.out.println("\n--- ESTADÍSTICAS DEL SISTEMA ---");
        
        long totalPacientes = pacienteService.getAll().size();
        long pacientesEliminados = pacienteService.countDeleted();
        long totalHistorias = historiaClinicaService.getAll().size();
        long historiasEliminadas = historiaClinicaService.countDeleted();
        
        System.out.println("📊 RESUMEN ESTADÍSTICO:");
        System.out.println("Pacientes activos: " + totalPacientes);
        System.out.println("Pacientes eliminados: " + pacientesEliminados);
        System.out.println("Total pacientes en sistema: " + (totalPacientes + pacientesEliminados));
        System.out.println("---");
        System.out.println("Historias clínicas activas: " + totalHistorias);
        System.out.println("Historias clínicas eliminadas: " + historiasEliminadas);
        System.out.println("Total historias en sistema: " + (totalHistorias + historiasEliminadas));
        System.out.println("---");
        
        if (totalPacientes > 0) {
            double porcentajeConHC = (double) totalHistorias / totalPacientes * 100;
            System.out.printf("Pacientes con historia clínica: %.1f%%\n", porcentajeConHC);
        }
        
        System.out.println("Presione Enter para continuar...");
        scanner.nextLine();
    }
}
