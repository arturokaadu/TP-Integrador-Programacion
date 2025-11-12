-- ===============================================
-- CARGA MASIVA DE DATOS
-- ===============================================
USE tfi_bd1;

-- 1. LIMPIEZA SEGURA DE TABLAS (Creadas en Etapa 1)
-- Se desactiva temporalmente la revisión de FK para permitir el TRUNCATE
-- de la tabla 'paciente' (padre) sin errores.
SET FOREIGN_KEY_CHECKS = 0;

-- Se vacian las tablas para asegurar un inicio desde cero.
TRUNCATE TABLE historia_clinica;
TRUNCATE TABLE paciente;

-- Se reactiva la revisión de FK.
SET FOREIGN_KEY_CHECKS = 1;

-- 2. CONFIGURACIÓN DE LA SESIÓN
-- Se aumenta el límite de recursión de MySQL solo para esta sesión.
-- Esto es necesario para superar el límite de 1000 y llegar a 200,000. De lo contrario genera el error de 1001 recursions
SET @@cte_max_recursion_depth = 200001;

-- 4. CARGA MASIVA DE PACIENTES (Tabla Padre)
-- Se usa WITH RECURSIVE para generar 200,000 números
INSERT INTO paciente (id, nombre, apellido, dni, fecha_nacimiento, eliminado)
WITH RECURSIVE NumberGenerator (n) AS (
  SELECT 1
  UNION ALL
  -- Usamos <= para incluir el 200,000
  SELECT n + 1 FROM NumberGenerator WHERE n <= 200000
)
-- JOIN con el operador % (módulo) para ciclar los nombres
SELECT
  g.n,
  sn.nombre,  -- Nombre desde la tabla semilla unida
  sa.apellido, -- Apellido desde la tabla semilla unida
  CONCAT('DNI-', g.n),
  DATE_SUB(CURDATE(), INTERVAL FLOOR(18 + (RAND(g.n * 3) * 62)) YEAR),
  0
FROM
  NumberGenerator g
  -- Unimos con nombres: (g.n % 100) da un número entre 0-99. Le sumamos 1
  JOIN semilla_nombres sn ON sn.id = (g.n % 100) + 1
  -- Unimos con apellidos: (g.n + 20 % 100) para desfasar los apellidos
  JOIN semilla_apellidos sa ON sa.id = ((g.n + 20) % 100) + 1
WHERE g.n <= 200000; -- Aseguramos el límite aquí también
  
-- 5. CARGA MASIVA DE HISTORIAS (Tabla Hija)
-- Se inserta una historia clínica por cada paciente creado en el paso anterior
INSERT INTO historia_clinica (id, nro_historia, grupo_sanguineo, paciente_id, eliminado)
SELECT
  p.id + 200000, -- ID único para la historia
  CONCAT('HC-', p.id), -- Nro. de historia único garantizado
  ELT(FLOOR(1 + RAND(p.id) * 8), 'A+', 'A-', 'B+', 'B-', 'AB+', 'AB-', 'O+', 'O-'), -- Grupo al azar
  p.id, -- FK que apunta al paciente
  0
FROM
  paciente p;

-- 6. VERIFICACIÓN
-- Se muestran todos los conteos necesarios
SELECT 'paciente' AS Tabla, COUNT(*) AS Total FROM paciente
UNION ALL
SELECT 'historia_clinica' AS Tabla, COUNT(*) AS Total FROM historia_clinica;

SELECT COUNT(*) AS pacientes_con_historias_duplicadas
FROM (
    SELECT paciente_id, COUNT(*) AS num_historias
    FROM historia_clinica
    GROUP BY paciente_id
    HAVING num_historias > 1
) AS conteo_duplicados;

SELECT grupo_sanguineo, COUNT(*) AS Cantidad
FROM historia_clinica
GROUP BY grupo_sanguineo;