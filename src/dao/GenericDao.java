/*
 * Interfaz genérica para el patrón Data Access Object (DAO).
 * Define las operaciones CRUD que deben implementar las clases DAO.
 * Incluye el parámetro Connection para participar en transacciones de Service.
 * Inspirado en el ejemplo del profe
 */
package dao;

/**
 *
 * @author emanuelbrahim
 */

import entities.Base;  //Importa el Base.java dentro de el Source Packages llamado entites
import java.sql.Connection; // llama a la libreria java.sql connection
import java.sql.SQLException; //llama a la libreria java.sql.SQLException
import java.util.List; // llama a la libreria java.util.List

public interface GenericDao<T extends Base> { //se define el GenericDao

    // Los métodos CRUD deben aceptar una Connection para participar en transacciones

    /**
     * Inserta una nueva entidad en la base de datos.
     * @param entidad Objeto entidad (Paciente o HistoriaClinica) a insertar.
     * @param conn Conexión compartida para la transacción activa.
     * @return La entidad con su ID autogenerado.
     * @throws SQLException Si ocurre un error de acceso a la base de datos.
     */
    T crear(T entidad, Connection conn) throws SQLException;

    /**
     * Lee una entidad por su ID (solo registros activos: eliminado = FALSE).
     * Nota: Este método obtiene su propia conexión, no es transaccional.
     * @param id El ID de la entidad a buscar.
     * @return La entidad encontrada o null si no existe.
     * @throws SQLException Si ocurre un error de acceso a la base de datos.
     */
    T leer(long id) throws SQLException; 

    /**
     * Retorna una lista de todas las entidades que están activas (eliminado = FALSE).
     * @return Lista de entidades activas.
     * @throws SQLException Si ocurre un error de acceso a la base de datos.
     */
    List<T> leerTodos() throws SQLException; 

    /**
     * Actualiza una entidad existente.
     * @param entidad Objeto entidad con los datos a actualizar.
     * @param conn Conexión compartida para la transacción activa.
     * @throws SQLException Si ocurre un error de acceso a la base de datos o si el ID es inválido.
     */
    void actualizar(T entidad, Connection conn) throws SQLException; 

    /**
     * Realiza la baja lógica (Soft Delete) de una entidad, estableciendo 'eliminado = TRUE'.
     * @param id El ID de la entidad a eliminar.
     * @param conn Conexión compartida para la transacción activa.
     * @throws SQLException Si ocurre un error de acceso a la base de datos o si no se encuentra la entidad activa.
     */
    void eliminar(long id, Connection conn) throws SQLException; 
    
    /**
     * Obtiene una lista de todas las entidades que han sido eliminadas lógicamente (eliminado = TRUE).
     * @return Lista de entidades eliminadas.
     * @throws SQLException Si ocurre un error de acceso a la base de datos.
     */
    List<T> leerTodosEliminados() throws SQLException; 
    
    /**
     * Cuenta el número total de registros que están marcados como eliminados lógicamente (eliminado = TRUE).
     * @return La cantidad de registros eliminados.
     * @throws SQLException Si ocurre un error de acceso a la base de datos.
     */
    long contarEliminados() throws SQLException; 
    
    /**
     * Recupera una entidad eliminada lógicamente, estableciendo 'eliminado = FALSE'.
     * @param id El ID de la entidad a recuperar.
     * @param conn Conexión compartida para la transacción activa.
     * @throws SQLException Si ocurre un error de acceso a la base de datos o si no se encuentra la entidad eliminada.
     */
    void recuperar(long id, Connection conn) throws SQLException; 
}