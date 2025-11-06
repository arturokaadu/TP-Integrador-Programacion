/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package dao;

/**
 *
 * @author artur
 */
public interface GenericDao<T> {
    void crear(T entidad);
    T leer(long id);
    void actualizar(T entidad);
    void eliminar(long id);
}
