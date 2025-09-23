-- Tabla principal de usuarios
CREATE TABLE usuario (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    apellido VARCHAR(100) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    telefono VARCHAR(20),
    activo BOOLEAN DEFAULT TRUE,
    fecha_registro TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tabla de roles por restaurante
CREATE TABLE usuario_restaurante_rol (
    id BIGSERIAL PRIMARY KEY,
    usuario_id BIGINT NOT NULL REFERENCES usuario(id) ON DELETE CASCADE,
    restaurante_id BIGINT NOT NULL, -- referencia lógica al servicio Catálogo
    rol VARCHAR(50) NOT NULL,
    CONSTRAINT uq_usuario_restaurante_rol UNIQUE (usuario_id, restaurante_id, rol)
);