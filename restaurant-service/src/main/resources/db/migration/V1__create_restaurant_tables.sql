-- Tabla DIRECCION
CREATE TABLE direccion (
    id BIGSERIAL PRIMARY KEY,
    pais VARCHAR(100) NOT NULL,
    provincia VARCHAR(100),
    ciudad VARCHAR(100),
    calle VARCHAR(150),
    numero VARCHAR(20),
    latitud VARCHAR(50),
    longitud VARCHAR(50)
);

-- Tabla RESTAURANTE
CREATE TABLE restaurante (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(255) NOT NULL,
    activo BOOLEAN DEFAULT TRUE,
    direccion_id BIGINT REFERENCES direccion(id) ON DELETE SET NULL,
    telefono VARCHAR(20),
    entidad_fiscal_id BIGINT,
    horario_apertura TIME, -- Agregado según el diagrama
    horario_cierre TIME -- Agregado según el diagrama
);

-- Tabla CONFIGURACION_RESTAURANTE
CREATE TABLE configuracion_restaurante (
    id BIGSERIAL PRIMARY KEY,
    restaurante_id BIGINT NOT NULL REFERENCES restaurante(id) ON DELETE CASCADE,
    tiempo_anticipacion_minutos INT,
    min_personas_evento_largo INT,
    mapa_ancho DECIMAL,
    mapa_largo DECIMAL,
    mostrar_precios BOOLEAN DEFAULT TRUE
);

-- Tabla MESA
CREATE TABLE mesa (
    id BIGSERIAL PRIMARY KEY,
    restaurante_id BIGINT NOT NULL REFERENCES restaurante(id) ON DELETE CASCADE,
    descripcion VARCHAR(255),
    capacidad INT,
    posicion_x INT,
    posicion_y INT,
    bloqueada BOOLEAN DEFAULT FALSE
);

-- Tabla CATEGORIA_PLATO
CREATE TABLE categoria_plato (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL UNIQUE
);

-- Tabla PLATO
CREATE TABLE plato (
    id BIGSERIAL PRIMARY KEY,
    restaurante_id BIGINT NOT NULL REFERENCES restaurante(id) ON DELETE CASCADE,
    categoria_plato_id BIGINT REFERENCES categoria_plato(id) ON DELETE SET NULL,
    nombre VARCHAR(255) NOT NULL,
    descripcion TEXT,
    precio DECIMAL(10,2),
    estado VARCHAR(50),
    imagen_url VARCHAR(500)
);

-- Tabla ETIQUETA
CREATE TABLE etiqueta (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL UNIQUE
);

-- Tabla PLATO_ETIQUETA (relación N:N entre PLATO y ETIQUETA)
CREATE TABLE plato_etiqueta (
    id BIGSERIAL PRIMARY KEY,
    plato_id BIGINT NOT NULL REFERENCES plato(id) ON DELETE CASCADE,
    etiqueta_id BIGINT NOT NULL REFERENCES etiqueta(id) ON DELETE CASCADE,
    CONSTRAINT uq_plato_etiqueta UNIQUE (plato_id, etiqueta_id)
);

-- NUEVA TABLA: ETIQUETA_FILTRADO_RESTAURANTE
-- Esta tabla permite que cada restaurante tenga un conjunto específico de etiquetas
CREATE TABLE etiqueta_filtrado_restaurante (
    id BIGSERIAL PRIMARY KEY,
    restaurante_id BIGINT NOT NULL REFERENCES restaurante(id) ON DELETE CASCADE,
    etiqueta_id BIGINT NOT NULL REFERENCES etiqueta(id) ON DELETE CASCADE,
    CONSTRAINT uq_restaurante_etiqueta UNIQUE (restaurante_id, etiqueta_id)
);

-- Tabla ENTIDAD_FISCAL
CREATE TABLE entidad_fiscal (
    id BIGSERIAL PRIMARY KEY,
    cuit VARCHAR(20) NOT NULL UNIQUE,
    razon_social VARCHAR(255) NOT NULL,
    validado BOOLEAN DEFAULT FALSE
);