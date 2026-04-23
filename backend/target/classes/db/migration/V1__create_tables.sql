-- V1__create_tables.sql

CREATE TABLE IF NOT EXISTS curso (
    id                  BIGSERIAL PRIMARY KEY,
    nombre              VARCHAR(255) NOT NULL,
    descripcion         TEXT,
    version             VARCHAR(50) NOT NULL,
    estado              VARCHAR(20) NOT NULL CHECK (estado IN ('ACTIVO','INACTIVO')),
    fecha_creacion      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS clase (
    id                      BIGSERIAL PRIMARY KEY,
    curso_id                BIGINT NOT NULL REFERENCES curso(id) ON DELETE CASCADE,
    numero                  INTEGER NOT NULL,
    titulo                  VARCHAR(255) NOT NULL,
    descripcion             TEXT,
    orden                   INTEGER NOT NULL,
    duracion_estimada_min   INTEGER,
    UNIQUE (curso_id, numero)
);

CREATE TABLE IF NOT EXISTS contenido (
    id              BIGSERIAL PRIMARY KEY,
    clase_id        BIGINT NOT NULL REFERENCES clase(id) ON DELETE CASCADE,
    tipo            VARCHAR(20) NOT NULL CHECK (tipo IN ('TEXT','PDF','WORD','EXCEL','PPTX','VIDEO','IMAGE','URL')),
    titulo          VARCHAR(255) NOT NULL,
    url_recurso     VARCHAR(1000),
    texto_cuerpo    TEXT,
    orden_en_clase  INTEGER NOT NULL DEFAULT 1,
    tamanio_bytes   BIGINT
);

CREATE TABLE IF NOT EXISTS estudiante (
    id              BIGSERIAL PRIMARY KEY,
    nombre          VARCHAR(255) NOT NULL,
    apellido        VARCHAR(255) NOT NULL,
    email           VARCHAR(255) NOT NULL UNIQUE,
    password        VARCHAR(255) NOT NULL,
    fecha_registro  TIMESTAMP,
    estado          VARCHAR(20) NOT NULL CHECK (estado IN ('ACTIVO','INACTIVO')),
    rol             VARCHAR(20) NOT NULL CHECK (rol IN ('ESTUDIANTE','ADMIN'))
);

CREATE TABLE IF NOT EXISTS inscripcion (
    id              BIGSERIAL PRIMARY KEY,
    estudiante_id   BIGINT NOT NULL REFERENCES estudiante(id) ON DELETE CASCADE,
    curso_id        BIGINT NOT NULL REFERENCES curso(id) ON DELETE CASCADE,
    fecha_inicio    TIMESTAMP,
    fecha_fin       TIMESTAMP,
    estado          VARCHAR(20) NOT NULL CHECK (estado IN ('EN_PROGRESO','COMPLETADO','ABANDONADO'))
);

CREATE TABLE IF NOT EXISTS progreso_clase (
    id                      BIGSERIAL PRIMARY KEY,
    inscripcion_id          BIGINT NOT NULL REFERENCES inscripcion(id) ON DELETE CASCADE,
    clase_id                BIGINT NOT NULL REFERENCES clase(id) ON DELETE CASCADE,
    estado                  VARCHAR(20) NOT NULL CHECK (estado IN ('NO_INICIADO','EN_PROGRESO','COMPLETADO')),
    fecha_inicio            TIMESTAMP,
    fecha_completado        TIMESTAMP,
    tiempo_total_segundos   BIGINT
);

CREATE TABLE IF NOT EXISTS interaccion_contenido (
    id                  BIGSERIAL PRIMARY KEY,
    inscripcion_id      BIGINT NOT NULL REFERENCES inscripcion(id) ON DELETE CASCADE,
    contenido_id        BIGINT NOT NULL REFERENCES contenido(id) ON DELETE CASCADE,
    fecha_acceso        TIMESTAMP,
    duracion_segundos   BIGINT,
    completado          BOOLEAN
);

CREATE TABLE IF NOT EXISTS evaluacion (
    id              BIGSERIAL PRIMARY KEY,
    clase_id        BIGINT NOT NULL UNIQUE REFERENCES clase(id) ON DELETE CASCADE,
    titulo          VARCHAR(255) NOT NULL,
    puntaje_maximo  INTEGER NOT NULL DEFAULT 100
);

CREATE TABLE IF NOT EXISTS pregunta (
    id                  BIGSERIAL PRIMARY KEY,
    evaluacion_id       BIGINT NOT NULL REFERENCES evaluacion(id) ON DELETE CASCADE,
    enunciado           TEXT NOT NULL,
    tipo                VARCHAR(30) NOT NULL CHECK (tipo IN ('MULTIPLE_CHOICE','VERDADERO_FALSO','ABIERTA')),
    orden               INTEGER NOT NULL,
    respuesta_correcta  VARCHAR(1000)
);

CREATE TABLE IF NOT EXISTS pregunta_opciones (
    pregunta_id BIGINT NOT NULL REFERENCES pregunta(id) ON DELETE CASCADE,
    opcion      VARCHAR(500) NOT NULL
);

CREATE TABLE IF NOT EXISTS respuesta_estudiante (
    id                  BIGSERIAL PRIMARY KEY,
    inscripcion_id      BIGINT NOT NULL REFERENCES inscripcion(id) ON DELETE CASCADE,
    evaluacion_id       BIGINT NOT NULL REFERENCES evaluacion(id) ON DELETE CASCADE,
    pregunta_id         BIGINT NOT NULL REFERENCES pregunta(id) ON DELETE CASCADE,
    respuesta           TEXT,
    correcta            BOOLEAN,
    fecha_respuesta     TIMESTAMP,
    intento             INTEGER NOT NULL DEFAULT 1
);

CREATE TABLE IF NOT EXISTS resultado_evaluacion (
    id                  BIGSERIAL PRIMARY KEY,
    inscripcion_id      BIGINT NOT NULL REFERENCES inscripcion(id) ON DELETE CASCADE,
    evaluacion_id       BIGINT NOT NULL REFERENCES evaluacion(id) ON DELETE CASCADE,
    puntaje_obtenido    DOUBLE PRECISION NOT NULL,
    puntaje_maximo      INTEGER NOT NULL,
    intento             INTEGER NOT NULL DEFAULT 1,
    fecha_realizacion   TIMESTAMP
);

CREATE TABLE IF NOT EXISTS recomendacion (
    id              BIGSERIAL PRIMARY KEY,
    inscripcion_id  BIGINT NOT NULL REFERENCES inscripcion(id) ON DELETE CASCADE,
    tipo            VARCHAR(20) NOT NULL CHECK (tipo IN ('SIGUIENTE_TEMA','REFUERZO','RECORDATORIO')),
    clase_id        BIGINT REFERENCES clase(id),
    contenido_id    BIGINT REFERENCES contenido(id),
    motivo          TEXT,
    estado          VARCHAR(20) NOT NULL CHECK (estado IN ('PENDIENTE','VISTA','DESCARTADA')),
    fecha_generada  TIMESTAMP,
    prioridad       VARCHAR(10)
);
