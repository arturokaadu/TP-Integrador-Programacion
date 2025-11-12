-- Activar modo estricto para que ENUM/constraints fallen correctamente
SET SESSION sql_mode = CONCAT(@@SESSION.sql_mode, ',STRICT_TRANS_TABLES');

-- Creación de base de datos tfi_bd1 
CREATE DATABASE IF NOT EXISTS tfi_bd1
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_0900_ai_ci;
USE tfi_bd1;


-- Idempotencia: recrear el esquema sin borrar la base completa
SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS historia_clinica;
DROP TABLE IF EXISTS paciente;
SET FOREIGN_KEY_CHECKS = 1;

-- Tabla A: PACIENTE 
CREATE TABLE paciente (
  id               BIGINT PRIMARY KEY,
  nombre           VARCHAR(80)  NOT NULL,
  apellido         VARCHAR(80)  NOT NULL,
  dni              VARCHAR(15)  NOT NULL UNIQUE,
  fecha_nacimiento DATE,
  eliminado        TINYINT(1)   NOT NULL DEFAULT 0,
  CONSTRAINT chk_paciente_eliminado CHECK (eliminado IN (0,1))
);

-- Tabla B: HISTORIA CLÍNICA 
CREATE TABLE historia_clinica (
  id                 BIGINT PRIMARY KEY,
  nro_historia       VARCHAR(20) UNIQUE, 
  grupo_sanguineo    ENUM('A+','A-','B+','B-','AB+','AB-','O+','O-') NOT NULL,
  antecedentes       TEXT,
  medicacion_actual  TEXT,
  observaciones      TEXT,
  eliminado          TINYINT(1) NOT NULL DEFAULT 0,
  paciente_id        BIGINT     NOT NULL UNIQUE,

  CONSTRAINT chk_hc_eliminado CHECK (eliminado IN (0,1)),
  CONSTRAINT fk_hist_paciente
    FOREIGN KEY (paciente_id) REFERENCES paciente(id)
    ON DELETE CASCADE
    ON UPDATE RESTRICT
); 