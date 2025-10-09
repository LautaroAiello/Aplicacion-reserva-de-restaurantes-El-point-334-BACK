-- V1__create_reservation_tables.sql

-- Tabla RESERVA
CREATE TABLE reserva (
    id BIGSERIAL PRIMARY KEY,
    usuario_id BIGINT NOT NULL, -- FK lógica al servicio de identidad
    restaurante_id BIGINT NOT NULL, -- FK lógica al servicio de catálogo
    fecha_hora TIMESTAMP NOT NULL,
    cantidad_personas INT NOT NULL,
    estado VARCHAR(50) NOT NULL, -- Ej: PENDIENTE, CONFIRMADA, CANCELADA, FINALIZADA
    observaciones TEXT,
    tipo VARCHAR(50) NOT NULL,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tabla RESERVA_MESA (relación N:N entre RESERVA y MESA)
CREATE TABLE reserva_mesa (
    id BIGSERIAL PRIMARY KEY,
    reserva_id BIGINT NOT NULL REFERENCES reserva(id) ON DELETE CASCADE,
    mesa_id BIGINT NOT NULL, -- FK lógica al servicio de catálogo
    CONSTRAINT uq_reserva_mesa UNIQUE (reserva_id, mesa_id)
);