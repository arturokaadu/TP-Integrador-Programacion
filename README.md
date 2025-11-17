#  Sistema de Gestión de Paciente e Historia Clínica

# Trabajo Práctico Integrador - Programación 2

###  Descripción del Proyecto

Este proyecto tiene como objetivo implementar un **Sistema CRUD** completo basado en el dominio **Paciente → Historia Clínica**, aplicando arquitectura en capas, persistencia con JDBC, validaciones de negocio y manejo transaccional.

El diseño fue realizado previamente mediante un **diagrama UML** que guía la implementación.

 ### [Enlace al video](https://drive.google.com/file/d/15E2SXZcr1WYjByjG0MWyZCKWZzaSg0Ck/view?usp=drive_link)

###  Objetivos Académicos

* Arquitectura en capas definida y creada en proyecto

* Diagrama UML completo (entidades, DAO, services, config, relaciones 1→1)

* Configuración de conexión a base de datos (DatabaseConnection)

* Manejo de transacciones con `TransactionManager`

* Entidades principales creadas: `Paciente`, `HistoriaClinica`, `TipoSangre`

* Interfaces genéricas para DAO y Service (`GenericDAO`, `GenericService`)

* DAO y Services creados e implementados (ej: `PacienteDao`, `PacienteService`)

##  Requisitos del Sistema

| Componente | Versión Requerida | 
 | ----- | ----- | 
| Java JDK | 17 o superior | 
| MySQL | 8.0 o superior | 
| Gradle | 8.12 (incluido wrapper) | 
| Sistema Operativo | Windows, Linux o macOS | 

##  Creación de la Base de Datos

Antes de ejecutar la aplicación, es necesario crear la base de datos **`tfi_bd1`**, el usuario de desarrollo y las tablas.
Para esto, se incluyen tres scripts SQL en el directorio `/sql`:

1. **`01_esquema.sql`** → Crea la base `tfi_bd1` y sus tablas, con claves primarias, foráneas e índices necesarios (incluye la relación 1→1 entre `paciente` e `historia_clinica`). Además, se agregó en el campo `id` de ambas tablas la propiedad `AUTO_INCREMENT` para que MySQL genere automáticamente los identificadores únicos de cada registro y se facilite la inserción de nuevos datos desde el código Java.

2. **`02_catalogos.sql`** → Crea y carga las tablas semilla.

3. **`03_carga_masiva.sql`** → Inserta datos de prueba (pacientes e historias clínicas).

También debe ejecutarse el siguiente script para crear el usuario de conexión:

```sql
CREATE USER IF NOT EXISTS 'dev'@'localhost' IDENTIFIED BY 'Grupo54Dev';
GRANT ALL PRIVILEGES ON tfi_bd1.* TO 'dev'@'localhost';
FLUSH PRIVILEGES;
```
##  Arquitectura y Estructura del Proyecto

El código está organizado en una arquitectura de cinco capas lógicas.

### Estructura del Repositorio

```bash
/src/main/java/com/tfi/app
 ├── config/
 │    ├── DatabaseConnection.java
 │    └── TransactionManager.java
 ├── entities/
 │    ├── Base.java
 │    ├── Paciente.java
 │    ├── HistoriaClinica.java
 │    └── TipoSangre.java
 ├── dao/
 │    ├── GenericDao.java
 │    ├── PacienteDao.java
 │    └── HistoriaClinicaDao.java
 ├── services/
 │    ├── GenericService.java
 │    ├── PacienteService.java
 │    └── HistoriaClinicaService.java
 └── main/
      ├── AppMenu.java
      └── Main.java

/sql/
 ├── 01_esquema.sql
 ├── 02_catalogos.sql
 └── 03_carga_masiva.sql
 ```

###  Descripción de las Capas

| Capa | Propósito | Componentes Clave | 
 | ----- | ----- | ----- | 
| **Config** | Manejo de la conexión y control de la transaccionalidad. Evita instanciación directa (constructores privados). | `DatabaseConnection`, `TransactionManager` (implementa `AutoCloseable`). | 
| **Entities** | Entidades de negocio, aplicando POO (herencia de `Base`, encapsulamiento, composición). | `Paciente`, `HistoriaClinica`, `Base`, `TipoSangre` (Enum). | 
| **DAO** | Acceso y persistencia de datos (CRUD) con JDBC. Reconstrucción de la relación 1→1 con `LEFT JOIN`. | `GenericDAO<T>`, `PacienteDao`, `HistoriaClinicaDao`. | 
| **Services** | **Orquestador** del sistema. Contiene la lógica de negocio, validaciones y control transaccional (llama al `TransactionManager`). | `GenericService<T>`, `PacienteService`, `HistoriaClinicaService`. | 
| **Main** | Capa de Presentación. Punto de entrada y menú interactivo en la consola. Solo utiliza la capa Service. | `Main`, `AppMenu`. | 

## Cómo Ejecutar el Sistema
En primer lugar, corresponde ejecutar los archivos SQL según lo previamente indicado y, posteriormente, se procede con la ejecución de la aplicación desarrollada en Java.
###  Explicación del Flujo de la Aplicación con un ejemplo de agregar paciente

1. **Inicio:** `Main` inicializa y lanza el `AppMenu`.

2. **Interacción:** El usuario selecciona una opción (e.g., Crear Paciente).

3. **Control:** `AppMenu` llama al método correspondiente en el `PacienteService`.

4. **Lógica y Transacción:** El `PacienteService`:

   * Ejecuta validaciones de negocio (e.g., DNI no duplicado, campos no nulos).

   * Utiliza el `TransactionManager` para abrir una transacción de base de datos.

   * Llama a `PacienteDao` e `HistoriaClinicaDao` para persistir los datos.

   * Si todas las operaciones son exitosas, llama a `commit()`.

   * Si ocurre una excepción (Error DB, fallo de validación), llama a `rollback()` y lanza la excepción.

##  Autores

Emanuel Aaron Brahim Pollini - Comisión 12
Arturo Alonso Kaadú - Comisión 15
Agustin Monardes Casas - Comisión 8
Belén Yarde Buller - Comisión 3

##  Licencia

Este proyecto fue desarrollado exclusivamente para fines académicos del curso de Programación 2.   
