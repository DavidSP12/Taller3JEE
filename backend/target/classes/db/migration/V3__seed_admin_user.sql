-- V3__seed_admin_user.sql
-- Admin user with password 'admin123' (BCrypt hash)
INSERT INTO estudiante (nombre, apellido, email, password, fecha_registro, estado, rol)
VALUES (
    'Admin',
    'Sistema',
    'admin@estructuras.com',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    NOW(),
    'ACTIVO',
    'ADMIN'
)
ON CONFLICT (email) DO NOTHING;

-- Sample student user with password 'estudiante123' (BCrypt hash)
INSERT INTO estudiante (nombre, apellido, email, password, fecha_registro, estado, rol)
VALUES (
    'Juan',
    'Pérez',
    'juan.perez@estudiante.com',
    '$2a$10$GRLdNijSQMUvl/au9ofL.eDwmoohzzS7.rmNSJZ.0FxO1ic4BINn6',
    NOW(),
    'ACTIVO',
    'ESTUDIANTE'
)
ON CONFLICT (email) DO NOTHING;
