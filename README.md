# Sistema de Gestión de Paciente e Historia Clinica

# Trabajo Práctico Integrador - Programación 2

### Descripción del Proyecto
Este proyecto tiene como objetivo implementar un sistema CRUD basado en el dominio **Paciente → Historia Clínica**, aplicando arquitectura en capas, persistencia con JDBC, validaciones de negocio y manejo transaccional.
El diseño fue realizado previamente mediante un **diagrama UML** que guía la implementación.

### Objetivos Académicos

-- Arquitectura en capas definida y creada en proyecto
-- Diagrama UML completo (entidades, DAO, services, config, relaciones 1→1)
-- Configuración de conexión a base de datos (DatabaseConnection)
-- Manejo de transacciones con `TransactionManager`
-- Entidades principales creadas: `Paciente`, `HistoriaClinica`, `TipoSangre`
-- Interfaces genéricas para DAO y Service (`GenericDAO`, `GenericService`)
-- DAO y Services


## Requisitos del Sistema

| Componente | Versión Requerida |
|------------|-------------------|
| Java JDK | 17 o superior |
| MySQL | 8.0 o superior |
| Gradle | 8.12 (incluido wrapper) |
| Sistema Operativo | Windows, Linux o macOS |

## Creación de la Base de Datos

Antes de ejecutar la aplicación, es necesario crear la base de datos **`tfi_bd1`**, el usuario de desarrollo y las tablas.
Para esto, se incluyen tres scripts SQL en el directorio `/sql`:

1. **`01_esquema.sql`** → Crea la base `tfi_bd1` y sus tablas, con claves primarias, foráneas e índices necesarios (incluye la relación 1→1 entre `paciente` e `historia_clinica`).
2. **`02_catalogos.sql`** → Crea y carga las tablas semilla.
3. **`03_carga_masiva.sql`** → Inserta datos de prueba (pacientes e historias clínicas).

También debe ejecutarse el siguiente script para crear el usuario de conexión:

```sql
CREATE USER IF NOT EXISTS 'dev'@'localhost' IDENTIFIED BY 'Grupo54Dev';
GRANT ALL PRIVILEGES ON tfi_bd1.* TO 'dev'@'localhost';
FLUSH PRIVILEGES;
```

Una vez creada la base y el usuario, se puede probar la conexión ejecutando TestConnection.
Una conexión exitosa devuelve lo siguiente por consola:
```shell
Conexion exitosa a la base de datos
Usuario conectado: dev@localhost
Base de datos: tfi_bd1
URL: jdbc:mysql://localhost:3306/tfi_bd1
Driver: MySQL Connector/J vmysql-connector-j-8.4.0 (Revision: 1c3f5c149e0bfe31c7fbeb24e2d260cd890972c4)
BUILD SUCCESSFUL (total time: 0 seconds)
```
