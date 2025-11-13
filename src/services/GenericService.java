package services; // Usando el paquete 'services'

/**
 *
 * @author emanuelbrahim
 */

import java.util.List;

/**
 * Interfaz genérica para la Capa de Servicio.
 * Define el contrato de las operaciones de negocio que manejan la entidad T.
 * Esta capa aplica validaciones, reglas de negocio y coordina las transacciones.
 * * Inspirado en el ejemplo del profe
 * * @param <T> Tipo de la entidad de negocio (e.g., Paciente, HistoriaClinica).
 */
public interface GenericService<T> {

    /**
     * Inserta una nueva entidad, aplicando validaciones de negocio.
     * Coordina las operaciones transaccionales.
     * @param entidad Entidad a insertar.
     * @throws Exception Si falla la validación o la persistencia.
     */
    void insertar(T entidad) throws Exception;

    /**
     * Actualiza una entidad existente, aplicando validaciones de negocio.
     * Coordina las operaciones transaccionales.
     * @param entidad Entidad con los datos actualizados.
     * @throws Exception Si falla la validación o la persistencia.
     */
    void actualizar(T entidad) throws Exception;

    /**
     * Realiza la eliminación lógica (Soft Delete) de una entidad.
     * @param id ID de la entidad a eliminar (de tipo Long).
     * @throws Exception Si id es inválido o no existe la entidad.
     */
    void eliminar(Long id) throws Exception;

    /**
     * Obtiene una entidad por su ID (solo registros activos).
     * @param id ID de la entidad a buscar (de tipo Long).
     * @return Entidad encontrada o null.
     * @throws Exception Si id es inválido o hay error de BD.
     */
    T getById(Long id) throws Exception;

    /**
     * Obtiene una lista de todas las entidades activas (eliminado=FALSE).
     * @return Lista de entidades activas.
     * @throws Exception Si hay error de BD.
     */
    List<T> getAll() throws Exception;
    
    /**
     * Obtiene una lista de todas las entidades que han sido eliminadas lógicamente.
     * @return Lista de entidades eliminadas.
     * @throws Exception Si hay error de BD.
     */
    List<T> getAllDeleted() throws Exception;

    /**
     * Cuenta el número total de registros eliminados.
     * @return Cantidad de registros eliminados.
     * @throws Exception Si hay error de BD.
     */
    long countDeleted() throws Exception;

    /**
     * Recupera una entidad eliminada lógicamente.
     * @param id ID de la entidad a recuperar (de tipo Long).
     * @throws Exception Si id es inválido o no existe la entidad.
     */
    void recuperar(Long id) throws Exception;
}