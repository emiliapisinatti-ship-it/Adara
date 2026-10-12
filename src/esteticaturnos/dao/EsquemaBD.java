package esteticaturnos.dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class EsquemaBD {

    private static final String CREAR_CLIENTAS = """
        CREATE TABLE IF NOT EXISTS clientas (
            id            INTEGER PRIMARY KEY AUTOINCREMENT,
            nombre        TEXT    NOT NULL,
            telefono      TEXT    NOT NULL,
            observaciones TEXT,
            activo        INTEGER NOT NULL DEFAULT 1 CHECK (activo IN (0, 1))
        )""";

    private static final String CREAR_PROFESIONALES = """
        CREATE TABLE IF NOT EXISTS profesionales (
            id       INTEGER PRIMARY KEY AUTOINCREMENT,
            nombre   TEXT    NOT NULL,
            telefono TEXT    NOT NULL,
            rubro    TEXT    NOT NULL CHECK (rubro IN ('PESTANIAS', 'UNIAS')),
            activo   INTEGER NOT NULL DEFAULT 1 CHECK (activo IN (0, 1))
        )""";

    private static final String CREAR_TURNOS = """
        CREATE TABLE IF NOT EXISTS turnos (
            id             INTEGER PRIMARY KEY AUTOINCREMENT,
            id_clienta     INTEGER NOT NULL REFERENCES clientas(id),
            id_profesional INTEGER NOT NULL REFERENCES profesionales(id),
            fecha          TEXT    NOT NULL,
            hora           TEXT    NOT NULL,
            servicio       TEXT    NOT NULL,
            detalle        TEXT,
            estado         TEXT    NOT NULL DEFAULT 'PENDIENTE'
                           CHECK (estado IN ('PENDIENTE', 'REALIZADO', 'CANCELADO')),
            precio         REAL
        )""";

    private static final String CREAR_INDICE_TURNO_UNICO = """
        CREATE UNIQUE INDEX IF NOT EXISTS idx_turno_unico
        ON turnos (fecha, hora, id_profesional)
        WHERE estado <> 'CANCELADO'""";

    private EsquemaBD() {
    }

    public static void inicializar() throws SQLException {
        Connection conexion = ConexionBD.getInstancia().getConexion();
        try (Statement sentencia = conexion.createStatement()) {
            sentencia.execute(CREAR_CLIENTAS);
            sentencia.execute(CREAR_PROFESIONALES);
            sentencia.execute(CREAR_TURNOS);
            sentencia.execute(CREAR_INDICE_TURNO_UNICO);
        }
        if (estaVacia(conexion)) {
            cargarDatosDePrueba(conexion);
        }
    }

    private static boolean estaVacia(Connection conexion) throws SQLException {
        try (Statement sentencia = conexion.createStatement();
             ResultSet resultado = sentencia.executeQuery("SELECT COUNT(*) FROM clientas")) {
            return resultado.getInt(1) == 0;
        }
    }

    private static void cargarDatosDePrueba(Connection conexion) throws SQLException {
        try (Statement sentencia = conexion.createStatement()) {
            sentencia.execute("""
                INSERT INTO profesionales (nombre, telefono, rubro) VALUES
                ('Carla Ruiz', '261 555-0101', 'UNIAS'),
                ('Sofía Díaz', '261 555-0102', 'PESTANIAS')""");
            sentencia.execute("""
                INSERT INTO clientas (nombre, telefono, observaciones) VALUES
                ('Lucía Gómez',      '261 555-0187', 'Alérgica al pegamento con cianoacrilato'),
                ('Valentina Rossi',  '261 555-0244', 'Uñas quebradizas'),
                ('Camila Fernández', '261 555-0319', NULL),
                ('Martina López',    '261 555-0476', 'Prefiere turnos por la tarde'),
                ('Sofía Martínez',   '261 555-0132', NULL)""");
            sentencia.execute("""
                INSERT INTO turnos (id_clienta, id_profesional, fecha, hora, servicio, detalle, estado, precio) VALUES
                (1, 2, '2026-09-18', '10:00', 'Volumen ruso',   'Curvatura C, 10 mm',       'REALIZADO', 25000),
                (1, 1, '2026-09-25', '16:00', 'Semipermanente', 'Rosa nude con francesita', 'REALIZADO', 15000),
                (1, 1, '2026-10-02', '14:00', 'Soft gel',       NULL,                       'PENDIENTE', NULL),
                (2, 1, '2026-10-02', '10:00', 'Kapping',        'Transparente',             'PENDIENTE', NULL),
                (4, 2, '2026-10-05', '15:00', 'Lifting',        NULL,                       'PENDIENTE', NULL)""");
        }
    }
}