/*
 * Interfaz genérica para el patrón Data Access Object (DAO).
 * Define las operaciones CRUD básicas que deben implementar todas las clases DAO.
 */
package dao;

import entities.Base; //ejemplo con la entidad base
import java.sql.SQLException;
import java.util.List;

/**
 * Interfaz genérica para el patrón Data Access Object (DAO).
 * Define las operaciones CRUD básicas que deben implementar todas las clases DAO.
 * @param <T> El tipo de la entidad que el DAO manejará (debe extender de Base).
 * @author belenyardebuller
 */
public interface GenericDao<T extends Base> { 

    /**
     * Inserta o crea una nueva entidad en la base de datos.
     * @param entidad Objeto entidad a insertar. (ejemplo Paciente o HistoriaClinica)
     * @return El objeto entidad con su ID generado por la base de datos asignado.
     * @throws SQLException Si ocurre un error de acceso a la base de datos.
     */
    T crearEntidad(T entidad) throws SQLException;

    /**
     * Lee una entidad por su identificador único (ID).
     * @param id El ID de la entidad a buscar.
     * @return La entidad si se encuentra, o null si no existe.
     * @throws SQLException Si ocurre un error de acceso a la base de datos.
     */
    T leerEntidad(long id) throws SQLException; 

    /**
     * Actualiza una entidad existente en la base de datos.
     * @param entidad Objeto entidad a actualizar.
     * @throws SQLException Si ocurre un error de acceso a la base de datos.
     */
    void actualizarEntidad(T entidad) throws SQLException; 

    /**
     * Elimina lógicamente una entidad por su ID (marca 'eliminado' como true).
     * @param id El ID de la entidad a eliminar.
     * @throws SQLException Si ocurre un error de acceso a la base de datos.
     */
    void eliminarEntidad(long id) throws SQLException; 

    /**
     * Recupera una entidad eliminada lógicamente por su ID (marca 'eliminado' como false).
     * @param id El ID de la entidad a recuperar.
     * @throws SQLException Si ocurre un error de acceso a la base de datos.
     */
    void recuperarEntidad(long id) throws SQLException; 

    /**
     * Retorna una lista de todas las entidades activas (eliminado = false).
     * @return Una lista de entidades.
     * @throws SQLException Si ocurre un error de acceso a la base de datos.
     */
    List<T> leerTodo() throws SQLException; 
}