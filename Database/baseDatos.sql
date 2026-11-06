DROP DATABASE IF EXISTS sistema_reservas;
CREATE DATABASE IF NOT EXISTS sistema_reservas;
USE sistema_reservas;
CREATE TABLE IF NOT EXISTS usuario (
    id        VARCHAR(15)  NOT NULL,
    clave     VARCHAR(255)  NOT NULL,
    rol       VARCHAR(15)  NOT NULL,
    nombre    VARCHAR(100) NOT NULL,
    telefono  VARCHAR(15)  NULL,
    PRIMARY KEY (id),
    CHECK (rol IN ('ADMIN', 'FUNCIONARIO'))
);
CREATE TABLE IF NOT EXISTS categoria (
    id           VARCHAR(10)  NOT NULL,
    descripcion  VARCHAR(100) NOT NULL,
    PRIMARY KEY (id)
);
CREATE TABLE IF NOT EXISTS recurso (
    num_activo    VARCHAR(15)  NOT NULL,
    id_categoria  VARCHAR(10)  NOT NULL,
    descripcion   VARCHAR(100) NOT NULL,
    PRIMARY KEY (num_activo),
    FOREIGN KEY (id_categoria) REFERENCES categoria(id)
);
CREATE TABLE IF NOT EXISTS reserva (
    id_reserva      VARCHAR(10)  NOT NULL,
    actividad       VARCHAR(100) NOT NULL,
    fecha           DATE         NOT NULL,
    hora_inicio     TIME         NOT NULL,
    hora_fin        TIME         NOT NULL,
    id_funcionario  VARCHAR(15)  NOT NULL,
    estado          VARCHAR(15)  NOT NULL,
    PRIMARY KEY (id_reserva),
    FOREIGN KEY (id_funcionario) REFERENCES usuario(id),
    CHECK (estado IN ('ACTIVA', 'CANCELADA')),
    CHECK (hora_fin > hora_inicio)
);
CREATE TABLE IF NOT EXISTS reserva_categoria (
    id_reserva    VARCHAR(10) NOT NULL,
    id_categoria  VARCHAR(10) NOT NULL,
    PRIMARY KEY (id_reserva, id_categoria),
    FOREIGN KEY (id_reserva) REFERENCES reserva(id_reserva) ON DELETE CASCADE,
    FOREIGN KEY (id_categoria) REFERENCES categoria(id)
);
CREATE TABLE IF NOT EXISTS reserva_recurso (
    id_reserva  VARCHAR(10) NOT NULL,
    num_activo  VARCHAR(15) NOT NULL,
    PRIMARY KEY (id_reserva, num_activo),
    FOREIGN KEY (id_reserva) REFERENCES reserva(id_reserva) ON DELETE CASCADE,
    FOREIGN KEY (num_activo) REFERENCES recurso(num_activo)
);
INSERT INTO usuario (id, clave, rol, nombre, telefono) VALUES
    ('admin', 'hola123', 'ADMIN',       'Administrador Principal', NULL),
    ('111',   '111',     'FUNCIONARIO', 'Andrea Cordero',          '60032423'),
    ('222',   '222',     'FUNCIONARIO', 'Emily Benavides',         '72223694'),
    ('333',   '333',     'FUNCIONARIO', 'Jose Pablo Sanchez',      '85692541');

INSERT INTO categoria (id, descripcion) VALUES
    ('CAT-000001', 'Sala para 10 personas'),
    ('CAT-000002', 'Laptop Windows 11'),
    ('CAT-000003', 'Proyector');
INSERT INTO recurso (num_activo, id_categoria, descripcion) VALUES
    ('34343',  'CAT-000001', 'Sala 1 primer piso'),
    ('283102', 'CAT-000001', 'Sala 2 primer piso'),
    ('111',    'CAT-000001', 'Sala 3 segundo piso'),
    ('238715', 'CAT-000002', 'Laptop #238715'),
    ('45238',  'CAT-000002', 'Laptop #45238'),
    ('452784', 'CAT-000003', 'Proyector Epson #452784'),
    ('1234',   'CAT-000003', 'Proyector #1234');
INSERT INTO reservas (id_reserva, actividad, fecha, hora_inicio, hora_fin, id_funcionario, estado) VALUES
    ('RES-000001', 'Sesion de Junta Directiva',          '2026-08-03', '09:00:00', '11:00:00', '111', 'ACTIVA'),
    ('RES-000002', 'Charla tecnica',                     '2026-08-04', '08:00:00', '09:00:00', '222', 'ACTIVA'),
    ('RES-000003', 'Reunion de Control',                 '2026-08-05', '14:00:00', '16:00:00', '111', 'ACTIVA'),
    ('RES-000004', 'Entrevista',                         '2026-08-06', '10:00:00', '11:00:00', '333', 'ACTIVA'),
    ('RES-000005', 'Mantenimiento de equipos',           '2026-08-09', '07:00:00', '08:00:00', '222', 'ACTIVA'),
    ('RES-000006', 'Reunion clientes (semana anterior)', '2026-07-30', '10:00:00', '12:00:00', '111', 'ACTIVA'),
    ('RES-000007', 'Reunion cancelada',                  '2026-08-04', '09:00:00', '10:00:00', '222', 'CANCELADA'),
    ('RES-000008', 'Reserva de sala y laptop',           '2026-09-19', '08:00:00', '09:30:00', '111', 'ACTIVA');
INSERT INTO reserva_categoria (id_reserva, id_categoria) VALUES
    ('RES-000001', 'CAT-000003'),
    ('RES-000002', 'CAT-000002'),
    ('RES-000003', 'CAT-000001'),
    ('RES-000003', 'CAT-000002'),
    ('RES-000004', 'CAT-000001'),
    ('RES-000005', 'CAT-000003'),
    ('RES-000006', 'CAT-000001'),
    ('RES-000007', 'CAT-000002'),
    ('RES-000008', 'CAT-000001'),
    ('RES-000008', 'CAT-000002');

INSERT INTO reserva_recurso (id_reserva, num_activo) VALUES
    ('RES-000001', '452784'),
    ('RES-000002', '238715'),
    ('RES-000003', '34343'),
    ('RES-000003', '45238'),
    ('RES-000004', '34343'),
    ('RES-000005', '452784'),
    ('RES-000006', '34343'),
    ('RES-000007', '238715'),
    ('RES-000008', '34343'),
    ('RES-000008', '238715');
##Para que todos tengamos el mismo user y contraseña
CREATE USER IF NOT EXISTS 'reservas_app'@'localhost' IDENTIFIED BY 'Reservas2026!';
GRANT ALL PRIVILEGES ON sistema_reservas.* TO 'reservas_app'@'localhost';