-- ===============================================
-- CREACIÓN DE CATÁLOGOS (SEMILLAS)
-- ===============================================
USE tfi_bd1;

-- 1. LIMPIEZA DE TABLAS SEMILLA
-- Se borran las tablas si ya existían para crearlas desde cero.
DROP TABLE IF EXISTS semilla_nombres;
DROP TABLE IF EXISTS semilla_apellidos;

SHOW TABLES;
DESCRIBE paciente;
DESCRIBE historia_clinica;
-- 2. CREACIÓN DE TABLAS SEMILLA (Materia Prima)

CREATE TABLE semilla_nombres (
  id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  nombre VARCHAR(80)
);

CREATE TABLE semilla_apellidos (
  id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  apellido VARCHAR(80)
);

-- 3. CARGA DE DATOS EN SEMILLAS
-- (100 nombres)
INSERT INTO semilla_nombres (nombre) VALUES
('Sofía'), ('Mateo'), ('Valentina'), ('Santiago'), ('Isabella'), ('Benjamín'), ('Camila'), ('Thiago'), ('Emma'), ('Lucas'), ('Martina'), ('Bautista'), ('Mía'), ('Joaquín'), ('Catalina'), ('Felipe'), ('Julieta'), ('Agustín'), ('Renata'), ('Bruno'), ('Victoria'), ('Dante'), ('Delfina'), ('Tomás'), ('Juana'), ('Ignacio'), ('Alma'), ('Nicolás'), ('Olivia'), ('Santino'), ('Bianca'), ('Francisco'), ('Zoe'), ('Lautaro'), ('Guadalupe'), ('Ramiro'), ('Pilar'), ('Jazmín'), ('Benicio'), ('Abril'), ('Pedro'), ('Morena'), ('Simón'), ('Candelaria'), ('León'), ('Helena'), ('Máximo'), ('Lucía'), ('Facundo'), ('Josefina'), ('Julián'), ('Ambar'), ('Tiziano'), ('Valeria'), ('Gonzalo'), ('Regina'), ('Federico'), ('Paulina'), ('Manuel'), ('Margarita'), ('Patricio'), ('Alejandro'), ('Daniel'), ('Diego'), ('Eduardo'), ('Esteban'), ('Fernando'), ('Gabriel'), ('Guillermo'), ('Javier'), ('Jorge'), ('Mariano'), ('Martín'), ('Ricardo'), ('Roberto'), ('Sergio'), ('Vicente'), ('Adriana'), ('Alejandra'), ('Andrea'), ('Carolina'), ('Claudia'), ('Cristina'), ('Daniela'), ('Fabiana'), ('Florencia'), ('Gabriela'), ('Gimena'), ('Jorgelina'), ('Karina'), ('Lorena'), ('Marcela'), ('Mariana'), ('Marina'), ('Mónica'), ('Natalia'), ('Paola'), ('Patricia'), ('David'), ('Victor');

-- (100 apellidos)
INSERT INTO semilla_apellidos (apellido) VALUES
('González'), ('Rodríguez'), ('Gómez'), ('Fernández'), ('López'), ('Díaz'), ('Martínez'), ('Pérez'), ('García'), ('Sánchez'), ('Romero'), ('Torres'), ('Álvarez'), ('Ruiz'), ('Ramírez'), ('Flores'), ('Benítez'), ('Acosta'), ('Medina'), ('Herrera'), ('Suárez'), ('Aguirre'), ('Giménez'), ('Gutiérrez'), ('Pereyra'), ('Rojas'), ('Molina'), ('Castro'), ('Ortiz'), ('Silva'), ('Núñez'), ('Luna'), ('Juárez'), ('Cabrera'), ('Ríos'), ('Ferreyra'), ('Godoy'), ('Morales'), ('Domínguez'), ('Moreno'), ('Vega'), ('Carrizo'), ('Peralta'), ('Castillo'), ('Ledesma'), ('Quiroga'), ('Vega'), ('Vera'), ('Muñoz'), ('Ojeda'), ('Ponce'), ('Sosa'), ('Ramos'), ('Vázquez'), ('Coronel'), ('Maldonado'), ('Paz'), ('Toledo'), ('Figueroa'), ('Blanco'), ('Correa'), ('Navarro'), ('Bravo'), ('Mansilla'), ('Soto'), ('Franco'), ('Ibarra'), ('Ayala'), ('Campos'), ('Cáceres'), ('Escobar'), ('Méndez'), ('Valdez'), ('Villalba'), ('Chávez'), ('Orellana'), ('Montenegro'), ('Roldán'), ('Leiva'), ('Cardozo'), ('Maidana'), ('Córdoba'), ('Bustos'), ('Farias'), ('Valenzuela'), ('Acuña'), ('Miranda'), ('Pereyra'), ('Reynoso'), ('Villanueva'), ('Paredes'), ('Arias'), ('Andrade'), ('Barrios') , ('Riquelme'), ('Solís'), ('Salazar'), ('Mendoza'), ('Russo'), ('Fontana');