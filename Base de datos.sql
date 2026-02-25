-- ============================================================
-- BASE DE DATOS COMPLETA - APP ADOPCIÓN DE MASCOTAS
-- Supabase / PostgreSQL
-- Borrar todo y recrear desde cero
-- ============================================================

-- ── 0. LIMPIAR TODO ─────────────────────────────────────────
DROP TABLE IF EXISTS SolicitudAdopcion   CASCADE;
DROP TABLE IF EXISTS Favorito            CASCADE;
DROP TABLE IF EXISTS DetalleMascotaVacunas CASCADE;
DROP TABLE IF EXISTS IntervencionMedica  CASCADE;
DROP TABLE IF EXISTS FotoMascota         CASCADE;
DROP TABLE IF EXISTS Mascota             CASCADE;
DROP TABLE IF EXISTS VacunaBasica        CASCADE;
DROP TABLE IF EXISTS Raza                CASCADE;
DROP TABLE IF EXISTS Especie             CASCADE;
DROP TABLE IF EXISTS Adoptante           CASCADE;
DROP TABLE IF EXISTS Refugio             CASCADE;
DROP TABLE IF EXISTS Usuario             CASCADE;
DROP TABLE IF EXISTS Mensaje             CASCADE; -- vieja, reemplazada por SolicitudAdopcion

-- Borrar funciones/triggers si existen
DROP FUNCTION IF EXISTS fn_actualizar_contador_favoritos()  CASCADE;
DROP FUNCTION IF EXISTS fn_crear_usuario_trigger()          CASCADE;

-- ── 1. TABLAS MAESTRAS ──────────────────────────────────────

CREATE TABLE Especie (
    id_especie   SERIAL PRIMARY KEY,
    NombreEspecie VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE Raza (
    id_raza   SERIAL PRIMARY KEY,
    id_especie INT NOT NULL REFERENCES Especie(id_especie) ON DELETE CASCADE,
    NombreRaza VARCHAR(100) NOT NULL,
    UNIQUE(id_especie, NombreRaza)
);

CREATE TABLE VacunaBasica (
    id_vacunabasica SERIAL PRIMARY KEY,
    id_especie      INT NOT NULL REFERENCES Especie(id_especie) ON DELETE CASCADE,
    NombreVacuna    VARCHAR(100) NOT NULL
);

-- ── 2. USUARIOS ─────────────────────────────────────────────

CREATE TABLE Usuario (
    id_usuario       UUID PRIMARY KEY,  -- = auth.users.id
    Correo           VARCHAR(100) UNIQUE NOT NULL,
    Telefono         VARCHAR(20),
    Rol              VARCHAR(20)  NOT NULL CHECK (Rol IN ('Adoptante', 'Refugio', 'Admin'))
                                  DEFAULT 'Adoptante',
    FechaRegistro    TIMESTAMPTZ  DEFAULT CURRENT_TIMESTAMP,
    URLImagenUsuario TEXT         -- ruta relativa en bucket 'avatars'
);

CREATE TABLE Adoptante (
    id_adoptante    SERIAL PRIMARY KEY,
    id_usuario      UUID UNIQUE NOT NULL REFERENCES Usuario(id_usuario) ON DELETE CASCADE,
    Nombre          VARCHAR(100) NOT NULL,
    Apellido        VARCHAR(100) NOT NULL,
    Genero          VARCHAR(30),
    FechaNacimiento DATE
);

CREATE TABLE Refugio (
    id_refugio  SERIAL PRIMARY KEY,
    id_usuario  UUID UNIQUE NOT NULL REFERENCES Usuario(id_usuario) ON DELETE CASCADE,
    Nombre      VARCHAR(100) NOT NULL,
    Direccion   TEXT         NOT NULL,
    Latitud     NUMERIC(10, 8),
    Longitud    NUMERIC(11, 8),
    Descripcion TEXT,
    URLPortada  TEXT          -- URL pública del bucket 'refugio-covers'
);

-- ── 3. MASCOTAS ─────────────────────────────────────────────

CREATE TABLE Mascota (
    id_mascota          SERIAL PRIMARY KEY,
    id_refugio          INT  NOT NULL REFERENCES Refugio(id_refugio)  ON DELETE CASCADE,
    id_raza             INT  REFERENCES Raza(id_raza)                 ON DELETE RESTRICT,
    NombreMascota       VARCHAR(100) NOT NULL,
    EdadAnios           INT     DEFAULT 0,
    EdadMeses           INT     DEFAULT 0,
    URLPortadaMascota   TEXT,   -- URL pública bucket 'mascotas'
    FechaAdopcion       DATE,
    Genero              VARCHAR(20) CHECK (Genero IN ('Macho', 'Hembra')),
    Temperamento        VARCHAR(255),
    Historia            TEXT,
    Estado              VARCHAR(30) CHECK (Estado IN ('Disponible', 'Adoptado', 'En Proceso'))
                                    DEFAULT 'Disponible',
    ContadorFavoritos   INT     DEFAULT 0,  -- cache para ordenar por popularidad
    FechaRegistro       TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

-- Fotos adicionales de una mascota (galería)
CREATE TABLE FotoMascota (
    id_fotomascota SERIAL PRIMARY KEY,
    id_mascota     INT NOT NULL REFERENCES Mascota(id_mascota) ON DELETE CASCADE,
    URLImagen      TEXT NOT NULL,
    Orden          INT  DEFAULT 0  -- para ordenar la galería
);

-- Historial médico
CREATE TABLE DetalleMascotaVacunas (
    id_vacunasmascota SERIAL PRIMARY KEY,
    id_mascota        INT NOT NULL REFERENCES Mascota(id_mascota)      ON DELETE CASCADE,
    id_vacunabasica   INT NOT NULL REFERENCES VacunaBasica(id_vacunabasica) ON DELETE CASCADE,
    FechaAplicacion   DATE,
    UNIQUE(id_mascota, id_vacunabasica)
);

CREATE TABLE IntervencionMedica (
    id_intervencionmedica  SERIAL PRIMARY KEY,
    id_mascota             INT  NOT NULL REFERENCES Mascota(id_mascota) ON DELETE CASCADE,
    TituloIntervencion     VARCHAR(150) NOT NULL,
    DescripcionIntervencion TEXT,
    FechaIntervencion      DATE
);

-- ── 4. INTERACCIONES ────────────────────────────────────────

CREATE TABLE Favorito (
    id_favorito  SERIAL PRIMARY KEY,
    id_mascota   INT NOT NULL REFERENCES Mascota(id_mascota)    ON DELETE CASCADE,
    id_adoptante INT NOT NULL REFERENCES Adoptante(id_adoptante) ON DELETE CASCADE,
    FechaAgregado TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(id_mascota, id_adoptante)
);

-- Reemplaza la tabla Mensaje — sistema de solicitudes de adopción
CREATE TABLE SolicitudAdopcion (
    id_solicitud        SERIAL PRIMARY KEY,
    id_refugio          INT  NOT NULL REFERENCES Refugio(id_refugio)    ON DELETE CASCADE,
    id_mascota          INT  NOT NULL REFERENCES Mascota(id_mascota)    ON DELETE CASCADE,
    id_adoptante        INT  NOT NULL REFERENCES Adoptante(id_adoptante) ON DELETE CASCADE,
    Mensaje             TEXT,               -- mensaje del adoptante al refugio
    FechaDeSolicitud    TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    EstadodeSolicitud   VARCHAR(30) CHECK (EstadodeSolicitud IN
                            ('Pendiente', 'Aprobada', 'Rechazada', 'Visita Agendada'))
                            DEFAULT 'Pendiente',
    FechaVisita         TIMESTAMPTZ,         -- cuando el refugio agenda la visita
    NotasRefugio        TEXT                -- notas internas del refugio
);

-- ── 5. VISTAS ÚTILES (para la app) ──────────────────────────

-- Vista pública de mascotas con info de raza, especie y refugio
CREATE OR REPLACE VIEW vista_mascotas_completa AS
SELECT
    m.id_mascota,
    m.NombreMascota,
    m.EdadAnios,
    m.EdadMeses,
    m.URLPortadaMascota,
    m.Genero,
    m.Temperamento,
    m.Historia,
    m.Estado,
    m.ContadorFavoritos,
    m.FechaRegistro,
    r.id_raza,
    r.NombreRaza,
    e.id_especie,
    e.NombreEspecie,
    ref.id_refugio,
    ref.Nombre        AS NombreRefugio,
    u.Direccion     AS DireccionRefugio,
    u.Latitud       AS LatitudRefugio,
    u.Longitud      AS LongitudRefugio,
    ref.URLPortada    AS PortadaRefugio,
    u.Telefono        AS TelefonoRefugio
FROM Mascota m
LEFT JOIN Raza    r   ON m.id_raza    = r.id_raza
LEFT JOIN Especie e   ON r.id_especie = e.id_especie
LEFT JOIN Refugio ref ON m.id_refugio = ref.id_refugio
LEFT JOIN Usuario u   ON ref.id_usuario = u.id_usuario;

-- Vista de refugios con su información de usuario (ACTUALIZADA)
CREATE OR REPLACE VIEW vista_refugios_completa AS
SELECT
    ref.id_refugio,
    ref.Nombre,
    u.Direccion,   -- AHORA VIENE DE USUARIO
    u.Latitud,     -- AHORA VIENE DE USUARIO
    u.Longitud,    -- AHORA VIENE DE USUARIO
    ref.Descripcion,
    ref.URLPortada,
    u.Telefono,
    u.Correo,
    u.URLImagenUsuario AS FotoPerfil,
    u.id_usuario,
    COUNT(m.id_mascota) FILTER (WHERE m.Estado = 'Disponible') AS MascotasDisponibles
FROM Refugio ref
JOIN Usuario u ON ref.id_usuario = u.id_usuario
LEFT JOIN Mascota m ON ref.id_refugio = m.id_refugio
GROUP BY ref.id_refugio, ref.Nombre, u.Direccion, u.Latitud, u.Longitud,
         ref.Descripcion, ref.URLPortada, u.Telefono, u.Correo,
         u.URLImagenUsuario, u.id_usuario;

-- Vista de perfil adoptante completo (ACTUALIZADA)
CREATE OR REPLACE VIEW vista_adoptante_completo AS
SELECT
    a.id_adoptante,
    a.Nombre,
    a.Apellido,
    a.Genero,
    a.FechaNacimiento,
    u.id_usuario,
    u.Correo,
    u.Telefono,
    u.Direccion,   -- NUEVO: Agregado a la vista
    u.Latitud,     -- NUEVO: Agregado a la vista
    u.Longitud,    -- NUEVO: Agregado a la vista
    u.URLImagenUsuario AS FotoPerfil,
    u.FechaRegistro,
    COUNT(DISTINCT f.id_favorito)    AS TotalFavoritos,
    COUNT(DISTINCT s.id_solicitud)   AS TotalSolicitudes
FROM Adoptante a
JOIN Usuario u ON a.id_usuario = u.id_usuario
LEFT JOIN Favorito f ON a.id_adoptante = f.id_adoptante
LEFT JOIN SolicitudAdopcion s ON a.id_adoptante = s.id_adoptante
GROUP BY a.id_adoptante, a.Nombre, a.Apellido, a.Genero, a.FechaNacimiento,
         u.id_usuario, u.Correo, u.Telefono, u.Direccion, u.Latitud, u.Longitud, u.URLImagenUsuario, u.FechaRegistro;

-- ── 6. FUNCIONES Y TRIGGERS ─────────────────────────────────

-- Trigger: mantener ContadorFavoritos actualizado automáticamente
CREATE OR REPLACE FUNCTION fn_actualizar_contador_favoritos()
RETURNS TRIGGER AS $$
BEGIN
    IF TG_OP = 'INSERT' THEN
        UPDATE Mascota
        SET ContadorFavoritos = ContadorFavoritos + 1
        WHERE id_mascota = NEW.id_mascota;
    ELSIF TG_OP = 'DELETE' THEN
        UPDATE Mascota
        SET ContadorFavoritos = GREATEST(ContadorFavoritos - 1, 0)
        WHERE id_mascota = OLD.id_mascota;
    END IF;
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_favoritos_contador
AFTER INSERT OR DELETE ON Favorito
FOR EACH ROW EXECUTE FUNCTION fn_actualizar_contador_favoritos();

-- Trigger: cuando una mascota pasa a 'Adoptado', registrar fecha de adopción
CREATE OR REPLACE FUNCTION fn_registrar_fecha_adopcion()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.Estado = 'Adoptado' AND OLD.Estado != 'Adoptado' THEN
        NEW.FechaAdopcion = CURRENT_DATE;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_fecha_adopcion
BEFORE UPDATE ON Mascota
FOR EACH ROW EXECUTE FUNCTION fn_registrar_fecha_adopcion();

-- Trigger: cuando una solicitud se aprueba, poner la mascota 'En Proceso'
CREATE OR REPLACE FUNCTION fn_solicitud_aprobada()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.EstadodeSolicitud = 'Aprobada' AND OLD.EstadodeSolicitud != 'Aprobada' THEN
        UPDATE Mascota
        SET Estado = 'En Proceso'
        WHERE id_mascota = NEW.id_mascota;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_solicitud_aprobada
AFTER UPDATE ON SolicitudAdopcion
FOR EACH ROW EXECUTE FUNCTION fn_solicitud_aprobada();

-- ── 7. RLS - ROW LEVEL SECURITY ─────────────────────────────

ALTER TABLE Usuario          ENABLE ROW LEVEL SECURITY;
ALTER TABLE Adoptante        ENABLE ROW LEVEL SECURITY;
ALTER TABLE Refugio          ENABLE ROW LEVEL SECURITY;
ALTER TABLE Mascota          ENABLE ROW LEVEL SECURITY;
ALTER TABLE FotoMascota      ENABLE ROW LEVEL SECURITY;
ALTER TABLE Favorito         ENABLE ROW LEVEL SECURITY;
ALTER TABLE SolicitudAdopcion ENABLE ROW LEVEL SECURITY;
ALTER TABLE DetalleMascotaVacunas ENABLE ROW LEVEL SECURITY;
ALTER TABLE IntervencionMedica    ENABLE ROW LEVEL SECURITY;

-- Usuario: solo gestiona su propio registro
CREATE POLICY pol_usuario_insert ON Usuario FOR INSERT WITH CHECK (auth.uid() = id_usuario);
CREATE POLICY pol_usuario_select ON Usuario FOR SELECT USING (auth.uid() = id_usuario);
CREATE POLICY pol_usuario_update ON Usuario FOR UPDATE USING (auth.uid() = id_usuario);

-- Adoptante: solo gestiona su propio perfil
CREATE POLICY pol_adoptante_insert ON Adoptante FOR INSERT WITH CHECK (auth.uid() = id_usuario);
CREATE POLICY pol_adoptante_select ON Adoptante FOR SELECT USING (auth.uid() = id_usuario);
CREATE POLICY pol_adoptante_update ON Adoptante FOR UPDATE USING (auth.uid() = id_usuario);

-- Refugio: solo gestiona su propio perfil
CREATE POLICY pol_refugio_insert ON Refugio FOR INSERT WITH CHECK (auth.uid() = id_usuario);
CREATE POLICY pol_refugio_select ON Refugio FOR SELECT USING (auth.uid() = id_usuario);
CREATE POLICY pol_refugio_update ON Refugio FOR UPDATE USING (auth.uid() = id_usuario);

-- Refugio público: cualquier usuario autenticado puede VER refugios
CREATE POLICY pol_refugio_select_public ON Refugio FOR SELECT USING (auth.role() = 'authenticated');

-- Mascota: cualquier usuario autenticado puede ver mascotas disponibles
CREATE POLICY pol_mascota_select_public ON Mascota
    FOR SELECT USING (auth.role() = 'authenticated');

-- Mascota: solo el refugio dueño puede insertar/modificar
CREATE POLICY pol_mascota_insert ON Mascota
    FOR INSERT WITH CHECK (
        id_refugio IN (SELECT id_refugio FROM Refugio WHERE id_usuario = auth.uid())
    );
CREATE POLICY pol_mascota_update ON Mascota
    FOR UPDATE USING (
        id_refugio IN (SELECT id_refugio FROM Refugio WHERE id_usuario = auth.uid())
    );
CREATE POLICY pol_mascota_delete ON Mascota
    FOR DELETE USING (
        id_refugio IN (SELECT id_refugio FROM Refugio WHERE id_usuario = auth.uid())
    );

-- FotoMascota: público para ver, solo refugio dueño para modificar
CREATE POLICY pol_fotomascota_select ON FotoMascota FOR SELECT USING (auth.role() = 'authenticated');
CREATE POLICY pol_fotomascota_insert ON FotoMascota
    FOR INSERT WITH CHECK (
        id_mascota IN (
            SELECT m.id_mascota FROM Mascota m
            JOIN Refugio r ON m.id_refugio = r.id_refugio
            WHERE r.id_usuario = auth.uid()
        )
    );

-- Favorito: adoptante solo gestiona sus propios favoritos
CREATE POLICY pol_favorito_select ON Favorito
    FOR SELECT USING (
        id_adoptante IN (SELECT id_adoptante FROM Adoptante WHERE id_usuario = auth.uid())
    );
CREATE POLICY pol_favorito_insert ON Favorito
    FOR INSERT WITH CHECK (
        id_adoptante IN (SELECT id_adoptante FROM Adoptante WHERE id_usuario = auth.uid())
    );
CREATE POLICY pol_favorito_delete ON Favorito
    FOR DELETE USING (
        id_adoptante IN (SELECT id_adoptante FROM Adoptante WHERE id_usuario = auth.uid())
    );

-- SolicitudAdopcion: adoptante puede crear/ver las suyas; refugio puede ver/actualizar las de sus mascotas
CREATE POLICY pol_solicitud_insert ON SolicitudAdopcion
    FOR INSERT WITH CHECK (
        id_adoptante IN (SELECT id_adoptante FROM Adoptante WHERE id_usuario = auth.uid())
    );
CREATE POLICY pol_solicitud_select_adoptante ON SolicitudAdopcion
    FOR SELECT USING (
        id_adoptante IN (SELECT id_adoptante FROM Adoptante WHERE id_usuario = auth.uid())
    );
CREATE POLICY pol_solicitud_select_refugio ON SolicitudAdopcion
    FOR SELECT USING (
        id_refugio IN (SELECT id_refugio FROM Refugio WHERE id_usuario = auth.uid())
    );
CREATE POLICY pol_solicitud_update_refugio ON SolicitudAdopcion
    FOR UPDATE USING (
        id_refugio IN (SELECT id_refugio FROM Refugio WHERE id_usuario = auth.uid())
    );

-- DetalleMascotaVacunas e IntervencionMedica: público para ver, solo refugio para modificar
CREATE POLICY pol_vacunas_select ON DetalleMascotaVacunas FOR SELECT USING (auth.role() = 'authenticated');
CREATE POLICY pol_intervencion_select ON IntervencionMedica FOR SELECT USING (auth.role() = 'authenticated');

-- Tablas maestras: cualquier autenticado puede leer
ALTER TABLE Especie      ENABLE ROW LEVEL SECURITY;
ALTER TABLE Raza         ENABLE ROW LEVEL SECURITY;
ALTER TABLE VacunaBasica ENABLE ROW LEVEL SECURITY;
CREATE POLICY pol_especie_select      ON Especie      FOR SELECT USING (auth.role() = 'authenticated');
CREATE POLICY pol_raza_select         ON Raza         FOR SELECT USING (auth.role() = 'authenticated');
CREATE POLICY pol_vacunabasica_select ON VacunaBasica FOR SELECT USING (auth.role() = 'authenticated');

-- ── LIMPIAR POLICIES DE STORAGE SI EXISTEN ──

DROP POLICY IF EXISTS "avatars_insert_own"            ON storage.objects;
DROP POLICY IF EXISTS "avatars_select_own"            ON storage.objects;
DROP POLICY IF EXISTS "refugio_covers_insert_own"     ON storage.objects;
DROP POLICY IF EXISTS "refugio_covers_select_public"  ON storage.objects;
DROP POLICY IF EXISTS "mascotas_insert_own"           ON storage.objects;
DROP POLICY IF EXISTS "mascotas_select_public"        ON storage.objects;

-- ── 8. STORAGE BUCKETS ──────────────────────────────────────

INSERT INTO storage.buckets (id, name, public) VALUES
    ('avatars',          'avatars',          false),
    ('refugio-covers',   'refugio-covers',   true),
    ('mascotas',         'mascotas',         true)   -- fotos de mascotas públicas
ON CONFLICT DO NOTHING;

-- Políticas de storage
CREATE POLICY "avatars_insert_own" ON storage.objects
    FOR INSERT WITH CHECK (bucket_id = 'avatars' AND auth.uid()::text = (storage.foldername(name))[1]);
CREATE POLICY "avatars_select_own" ON storage.objects
    FOR SELECT USING (bucket_id = 'avatars' AND auth.uid()::text = (storage.foldername(name))[1]);

CREATE POLICY "refugio_covers_insert_own" ON storage.objects
    FOR INSERT WITH CHECK (bucket_id = 'refugio-covers' AND auth.uid()::text = (storage.foldername(name))[1]);
CREATE POLICY "refugio_covers_select_public" ON storage.objects
    FOR SELECT USING (bucket_id = 'refugio-covers');

CREATE POLICY "mascotas_insert_own" ON storage.objects
    FOR INSERT WITH CHECK (bucket_id = 'mascotas' AND auth.uid()::text = (storage.foldername(name))[1]);
CREATE POLICY "mascotas_select_public" ON storage.objects
    FOR SELECT USING (bucket_id = 'mascotas');

-- ── 9. DATOS SEMILLA (Especies y razas básicas) ──────────────

INSERT INTO Especie (NombreEspecie) VALUES
    ('Perro'), ('Gato'), ('Conejo'), ('Ave'), ('Otro');

INSERT INTO Raza (id_especie, NombreRaza) VALUES
    (1, 'Mestizo'),     (1, 'Labrador'),    (1, 'Golden Retriever'),
    (1, 'Bulldog'),     (1, 'Poodle'),      (1, 'Beagle'),
    (1, 'Pastor Alemán'),(1, 'Chihuahua'),  (1, 'Pitbull'),
    (2, 'Mestizo'),     (2, 'Siamés'),      (2, 'Persa'),
    (2, 'Maine Coon'),  (2, 'Ragdoll'),
    (3, 'Mestizo'),     (3, 'Holland Lop'), (3, 'Mini Rex'),
    (4, 'Loro'),        (4, 'Canario'),     (4, 'Periquito'),
    (5, 'Otro');

INSERT INTO VacunaBasica (id_especie, NombreVacuna) VALUES
    (1, 'Parvovirus'),       (1, 'Moquillo'),        (1, 'Rabia'),
    (1, 'Hepatitis Canina'), (1, 'Leptospirosis'),
    (2, 'Triple Felina'),    (2, 'Rabia'),            (2, 'Leucemia Felina'),
    (3, 'Mixomatosis'),      (3, 'Enfermedad Hemorrágica Viral');


ALTER TABLE Usuario
ADD COLUMN Direccion TEXT,
ADD COLUMN Latitud DOUBLE PRECISION,
ADD COLUMN Longitud DOUBLE PRECISION;

ALTER COLUMN Refugio
DELETE COLUMN latitud
DELETE COLUMN longitud
DELETE COLUMN direccion