--
-- PostgreSQL database dump
--

\restrict xc5OIXGgQxx7dqi4rgNaV3qbTcFP8oYTGr1bcDvohIim6DE8YCS45PexzgmoZ2j

-- Dumped from database version 17.6
-- Dumped by pg_dump version 18.1

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- Name: public; Type: SCHEMA; Schema: -; Owner: -
--

CREATE SCHEMA public;


--
-- Name: SCHEMA public; Type: COMMENT; Schema: -; Owner: -
--

COMMENT ON SCHEMA public IS 'standard public schema';


--
-- Name: fn_actualizar_contador_favoritos(); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.fn_actualizar_contador_favoritos() RETURNS trigger
    LANGUAGE plpgsql
    AS $$BEGIN
    IF TG_OP = 'INSERT' THEN
        UPDATE mascota
        SET contadorfavoritos = COALESCE(contadorfavoritos, 0) + 1
        WHERE id_mascota = NEW.id_mascota;

    ELSIF TG_OP = 'DELETE' THEN
        UPDATE mascota
        SET contadorfavoritos = GREATEST(COALESCE(contadorfavoritos, 0) - 1, 0)
        WHERE id_mascota = OLD.id_mascota;
    END IF;

    RETURN NULL;
END;$$;


--
-- Name: fn_actualizar_contadorfavoritos(); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.fn_actualizar_contadorfavoritos() RETURNS trigger
    LANGUAGE plpgsql SECURITY DEFINER
    SET search_path TO 'public'
    AS $$
BEGIN
    IF TG_OP = 'INSERT' THEN
        UPDATE mascota
        SET contadorfavoritos = COALESCE(contadorfavoritos, 0) + 1
        WHERE id_mascota = NEW.id_mascota;

    ELSIF TG_OP = 'DELETE' THEN
        UPDATE mascota
        SET contadorfavoritos = GREATEST(COALESCE(contadorfavoritos, 0) - 1, 0)
        WHERE id_mascota = OLD.id_mascota;
    END IF;

    RETURN NULL;
END;
$$;


--
-- Name: fn_registrar_fecha_adopcion(); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.fn_registrar_fecha_adopcion() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
    IF NEW.Estado = 'Adoptado' AND OLD.Estado != 'Adoptado' THEN
        NEW.FechaAdopcion = CURRENT_DATE;
    END IF;
    RETURN NEW;
END;
$$;


--
-- Name: fn_solicitud_aprobada(); Type: FUNCTION; Schema: public; Owner: -
--

CREATE FUNCTION public.fn_solicitud_aprobada() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
    -- Caso 1: ¡Adopción Aprobada! Pasa a 'Adoptado' y registramos la fecha de hoy
    IF NEW.estadodesolicitud = 'Aprobada' THEN
        UPDATE public.mascota
        SET estado = 'Adoptado',
            fechaadopcion = CURRENT_DATE
        WHERE id_mascota = NEW.id_mascota;

    -- Caso 2: Visita Agendada. Separamos a la mascota poniéndola 'En Proceso'
    ELSIF NEW.estadodesolicitud = 'Visita Agendada' THEN
        UPDATE public.mascota
        SET estado = 'En Proceso',
            fechaadopcion = NULL -- Limpiamos por si acaso
        WHERE id_mascota = NEW.id_mascota;

    -- Caso 3: Si la rechazan (o la regresan a Pendiente por error), vuelve a estar 'Disponible'
    ELSIF NEW.estadodesolicitud IN ('Rechazada', 'Pendiente') THEN
        UPDATE public.mascota
        SET estado = 'Disponible',
            fechaadopcion = NULL -- Limpiamos la fecha de adopción
        WHERE id_mascota = NEW.id_mascota;
        
    END IF;

    RETURN NEW;
END;
$$;


SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: adoptante; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.adoptante (
    id_adoptante integer NOT NULL,
    id_usuario uuid NOT NULL,
    nombre character varying(100) NOT NULL,
    apellido character varying(100) NOT NULL,
    genero character varying(30),
    fechanacimiento date
);


--
-- Name: adoptante_id_adoptante_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.adoptante_id_adoptante_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: adoptante_id_adoptante_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.adoptante_id_adoptante_seq OWNED BY public.adoptante.id_adoptante;


--
-- Name: detallemascotavacunas; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.detallemascotavacunas (
    id_vacunasmascota integer NOT NULL,
    id_mascota integer NOT NULL,
    id_vacunabasica integer NOT NULL,
    fechaaplicacion date
);


--
-- Name: detallemascotavacunas_id_vacunasmascota_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.detallemascotavacunas_id_vacunasmascota_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: detallemascotavacunas_id_vacunasmascota_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.detallemascotavacunas_id_vacunasmascota_seq OWNED BY public.detallemascotavacunas.id_vacunasmascota;


--
-- Name: especie; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.especie (
    id_especie integer NOT NULL,
    nombreespecie character varying(50) NOT NULL
);


--
-- Name: especie_id_especie_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.especie_id_especie_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: especie_id_especie_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.especie_id_especie_seq OWNED BY public.especie.id_especie;


--
-- Name: favorito; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.favorito (
    id_favorito integer NOT NULL,
    id_mascota integer NOT NULL,
    id_adoptante integer NOT NULL,
    fechaagregado timestamp with time zone DEFAULT CURRENT_TIMESTAMP
);


--
-- Name: favorito_id_favorito_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.favorito_id_favorito_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: favorito_id_favorito_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.favorito_id_favorito_seq OWNED BY public.favorito.id_favorito;


--
-- Name: fotomascota; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.fotomascota (
    id_fotomascota integer NOT NULL,
    id_mascota integer NOT NULL,
    urlimagen text NOT NULL,
    orden integer DEFAULT 0
);


--
-- Name: fotomascota_id_fotomascota_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.fotomascota_id_fotomascota_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: fotomascota_id_fotomascota_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.fotomascota_id_fotomascota_seq OWNED BY public.fotomascota.id_fotomascota;


--
-- Name: intervencionmedica; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.intervencionmedica (
    id_intervencionmedica integer NOT NULL,
    id_mascota integer NOT NULL,
    titulointervencion character varying(150) NOT NULL,
    descripcionintervencion text,
    fechaintervencion date
);


--
-- Name: intervencionmedica_id_intervencionmedica_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.intervencionmedica_id_intervencionmedica_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: intervencionmedica_id_intervencionmedica_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.intervencionmedica_id_intervencionmedica_seq OWNED BY public.intervencionmedica.id_intervencionmedica;


--
-- Name: mascota; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.mascota (
    id_mascota integer NOT NULL,
    id_refugio integer NOT NULL,
    id_raza integer,
    nombremascota character varying(100) NOT NULL,
    edadanios integer DEFAULT 0,
    edadmeses integer DEFAULT 0,
    urlportadamascota text,
    fechaadopcion date,
    genero character varying(20),
    temperamento character varying(255),
    historia text,
    estado character varying(30) DEFAULT 'Disponible'::character varying,
    contadorfavoritos integer DEFAULT 0,
    fecharegistro timestamp with time zone DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT mascota_estado_check CHECK (((estado)::text = ANY ((ARRAY['Disponible'::character varying, 'Adoptado'::character varying, 'En Proceso'::character varying])::text[]))),
    CONSTRAINT mascota_genero_check CHECK (((genero)::text = ANY ((ARRAY['Macho'::character varying, 'Hembra'::character varying])::text[])))
);


--
-- Name: mascota_id_mascota_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.mascota_id_mascota_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: mascota_id_mascota_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.mascota_id_mascota_seq OWNED BY public.mascota.id_mascota;


--
-- Name: raza; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.raza (
    id_raza integer NOT NULL,
    id_especie integer NOT NULL,
    nombreraza character varying(100) NOT NULL
);


--
-- Name: raza_id_raza_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.raza_id_raza_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: raza_id_raza_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.raza_id_raza_seq OWNED BY public.raza.id_raza;


--
-- Name: refugio; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.refugio (
    id_refugio integer NOT NULL,
    id_usuario uuid NOT NULL,
    nombre character varying(100) NOT NULL,
    descripcion text,
    urlportada text
);


--
-- Name: refugio_id_refugio_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.refugio_id_refugio_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: refugio_id_refugio_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.refugio_id_refugio_seq OWNED BY public.refugio.id_refugio;


--
-- Name: solicitudadopcion; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.solicitudadopcion (
    id_solicitud integer NOT NULL,
    id_refugio integer NOT NULL,
    id_mascota integer NOT NULL,
    id_adoptante integer NOT NULL,
    mensaje text,
    fechadesolicitud timestamp with time zone DEFAULT CURRENT_TIMESTAMP,
    estadodesolicitud character varying(30) DEFAULT 'Pendiente'::character varying,
    fechavisita timestamp with time zone,
    notasrefugio text,
    CONSTRAINT solicitudadopcion_estadodesolicitud_check CHECK (((estadodesolicitud)::text = ANY ((ARRAY['Pendiente'::character varying, 'Aprobada'::character varying, 'Rechazada'::character varying, 'Visita Agendada'::character varying])::text[])))
);


--
-- Name: solicitudadopcion_id_solicitud_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.solicitudadopcion_id_solicitud_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: solicitudadopcion_id_solicitud_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.solicitudadopcion_id_solicitud_seq OWNED BY public.solicitudadopcion.id_solicitud;


--
-- Name: usuario; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.usuario (
    id_usuario uuid NOT NULL,
    correo character varying(100) NOT NULL,
    telefono character varying(20),
    rol character varying(20) DEFAULT 'Adoptante'::character varying NOT NULL,
    fecharegistro timestamp with time zone DEFAULT CURRENT_TIMESTAMP,
    urlimagenusuario text,
    direccion text,
    latitud double precision,
    longitud double precision,
    CONSTRAINT usuario_rol_check CHECK (((rol)::text = ANY ((ARRAY['Adoptante'::character varying, 'Refugio'::character varying, 'Admin'::character varying])::text[])))
);


--
-- Name: vacunabasica; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.vacunabasica (
    id_vacunabasica integer NOT NULL,
    id_especie integer NOT NULL,
    nombrevacuna character varying(100) NOT NULL
);


--
-- Name: vacunabasica_id_vacunabasica_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.vacunabasica_id_vacunabasica_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: vacunabasica_id_vacunabasica_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.vacunabasica_id_vacunabasica_seq OWNED BY public.vacunabasica.id_vacunabasica;


--
-- Name: vista_adoptante_completo; Type: VIEW; Schema: public; Owner: -
--

CREATE VIEW public.vista_adoptante_completo AS
 SELECT a.id_adoptante,
    a.nombre,
    a.apellido,
    a.genero,
    a.fechanacimiento,
    u.id_usuario,
    u.correo,
    u.telefono,
    u.direccion,
    u.latitud,
    u.longitud,
    u.urlimagenusuario AS fotoperfil,
    u.fecharegistro,
    count(DISTINCT f.id_favorito) AS totalfavoritos,
    count(DISTINCT s.id_solicitud) AS totalsolicitudes
   FROM (((public.adoptante a
     JOIN public.usuario u ON ((a.id_usuario = u.id_usuario)))
     LEFT JOIN public.favorito f ON ((a.id_adoptante = f.id_adoptante)))
     LEFT JOIN public.solicitudadopcion s ON ((a.id_adoptante = s.id_adoptante)))
  GROUP BY a.id_adoptante, a.nombre, a.apellido, a.genero, a.fechanacimiento, u.id_usuario, u.correo, u.telefono, u.direccion, u.latitud, u.longitud, u.urlimagenusuario, u.fecharegistro;


--
-- Name: vista_mascotas_completa; Type: VIEW; Schema: public; Owner: -
--

CREATE VIEW public.vista_mascotas_completa AS
 SELECT m.id_mascota,
    m.nombremascota,
    m.edadanios,
    m.edadmeses,
    m.urlportadamascota,
    m.genero,
    m.temperamento,
    m.historia,
    m.estado,
    m.contadorfavoritos,
    m.fecharegistro,
    r.id_raza,
    r.nombreraza,
    e.id_especie,
    e.nombreespecie,
    ref.id_refugio,
    ref.nombre AS nombrerefugio,
    u.direccion AS direccionrefugio,
    u.latitud AS latitudrefugio,
    u.longitud AS longitudrefugio,
    ref.urlportada AS portadarefugio,
    u.urlimagenusuario AS perfilrefugio,
    u.telefono AS telefonorefugio
   FROM ((((public.mascota m
     LEFT JOIN public.raza r ON ((m.id_raza = r.id_raza)))
     LEFT JOIN public.especie e ON ((r.id_especie = e.id_especie)))
     LEFT JOIN public.refugio ref ON ((m.id_refugio = ref.id_refugio)))
     LEFT JOIN public.usuario u ON ((ref.id_usuario = u.id_usuario)));


--
-- Name: vista_refugios_completa; Type: VIEW; Schema: public; Owner: -
--

CREATE VIEW public.vista_refugios_completa AS
 SELECT ref.id_refugio,
    ref.nombre,
    u.direccion,
    u.latitud,
    u.longitud,
    ref.descripcion,
    ref.urlportada,
    u.telefono,
    u.correo,
    u.urlimagenusuario AS fotoperfil,
    u.id_usuario,
    count(m.id_mascota) FILTER (WHERE ((m.estado)::text = 'Disponible'::text)) AS mascotasdisponibles
   FROM ((public.refugio ref
     JOIN public.usuario u ON ((ref.id_usuario = u.id_usuario)))
     LEFT JOIN public.mascota m ON ((ref.id_refugio = m.id_refugio)))
  GROUP BY ref.id_refugio, ref.nombre, u.direccion, u.latitud, u.longitud, ref.descripcion, ref.urlportada, u.telefono, u.correo, u.urlimagenusuario, u.id_usuario;


--
-- Name: vista_solicitudes_completa; Type: VIEW; Schema: public; Owner: -
--

CREATE VIEW public.vista_solicitudes_completa AS
 SELECT sa.id_solicitud,
    sa.id_refugio,
    sa.id_mascota,
    sa.id_adoptante,
    sa.mensaje,
    sa.fechadesolicitud,
    sa.estadodesolicitud,
    sa.fechavisita,
    sa.notasrefugio,
    m.nombremascota,
    m.urlportadamascota,
    r.nombre AS nombrerefugio,
    u_refugio.telefono AS telefonorefugio,
    (((a.nombre)::text || ' '::text) || (a.apellido)::text) AS nombreadoptante
   FROM ((((public.solicitudadopcion sa
     JOIN public.mascota m ON ((sa.id_mascota = m.id_mascota)))
     JOIN public.refugio r ON ((sa.id_refugio = r.id_refugio)))
     JOIN public.usuario u_refugio ON ((r.id_usuario = u_refugio.id_usuario)))
     JOIN public.adoptante a ON ((sa.id_adoptante = a.id_adoptante)));


--
-- Name: vista_vacunas_mascota; Type: VIEW; Schema: public; Owner: -
--

CREATE VIEW public.vista_vacunas_mascota WITH (security_invoker='on') AS
 SELECT dv.id_vacunasmascota,
    dv.id_mascota,
    dv.id_vacunabasica,
    dv.fechaaplicacion,
    vb.nombrevacuna
   FROM (public.detallemascotavacunas dv
     JOIN public.vacunabasica vb ON ((dv.id_vacunabasica = vb.id_vacunabasica)));


--
-- Name: adoptante id_adoptante; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.adoptante ALTER COLUMN id_adoptante SET DEFAULT nextval('public.adoptante_id_adoptante_seq'::regclass);


--
-- Name: detallemascotavacunas id_vacunasmascota; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.detallemascotavacunas ALTER COLUMN id_vacunasmascota SET DEFAULT nextval('public.detallemascotavacunas_id_vacunasmascota_seq'::regclass);


--
-- Name: especie id_especie; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.especie ALTER COLUMN id_especie SET DEFAULT nextval('public.especie_id_especie_seq'::regclass);


--
-- Name: favorito id_favorito; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.favorito ALTER COLUMN id_favorito SET DEFAULT nextval('public.favorito_id_favorito_seq'::regclass);


--
-- Name: fotomascota id_fotomascota; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.fotomascota ALTER COLUMN id_fotomascota SET DEFAULT nextval('public.fotomascota_id_fotomascota_seq'::regclass);


--
-- Name: intervencionmedica id_intervencionmedica; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.intervencionmedica ALTER COLUMN id_intervencionmedica SET DEFAULT nextval('public.intervencionmedica_id_intervencionmedica_seq'::regclass);


--
-- Name: mascota id_mascota; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.mascota ALTER COLUMN id_mascota SET DEFAULT nextval('public.mascota_id_mascota_seq'::regclass);


--
-- Name: raza id_raza; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.raza ALTER COLUMN id_raza SET DEFAULT nextval('public.raza_id_raza_seq'::regclass);


--
-- Name: refugio id_refugio; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.refugio ALTER COLUMN id_refugio SET DEFAULT nextval('public.refugio_id_refugio_seq'::regclass);


--
-- Name: solicitudadopcion id_solicitud; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.solicitudadopcion ALTER COLUMN id_solicitud SET DEFAULT nextval('public.solicitudadopcion_id_solicitud_seq'::regclass);


--
-- Name: vacunabasica id_vacunabasica; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.vacunabasica ALTER COLUMN id_vacunabasica SET DEFAULT nextval('public.vacunabasica_id_vacunabasica_seq'::regclass);


--
-- Name: adoptante adoptante_id_usuario_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.adoptante
    ADD CONSTRAINT adoptante_id_usuario_key UNIQUE (id_usuario);


--
-- Name: adoptante adoptante_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.adoptante
    ADD CONSTRAINT adoptante_pkey PRIMARY KEY (id_adoptante);


--
-- Name: detallemascotavacunas detallemascotavacunas_id_mascota_id_vacunabasica_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.detallemascotavacunas
    ADD CONSTRAINT detallemascotavacunas_id_mascota_id_vacunabasica_key UNIQUE (id_mascota, id_vacunabasica);


--
-- Name: detallemascotavacunas detallemascotavacunas_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.detallemascotavacunas
    ADD CONSTRAINT detallemascotavacunas_pkey PRIMARY KEY (id_vacunasmascota);


--
-- Name: especie especie_nombreespecie_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.especie
    ADD CONSTRAINT especie_nombreespecie_key UNIQUE (nombreespecie);


--
-- Name: especie especie_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.especie
    ADD CONSTRAINT especie_pkey PRIMARY KEY (id_especie);


--
-- Name: favorito favorito_id_mascota_id_adoptante_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.favorito
    ADD CONSTRAINT favorito_id_mascota_id_adoptante_key UNIQUE (id_mascota, id_adoptante);


--
-- Name: favorito favorito_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.favorito
    ADD CONSTRAINT favorito_pkey PRIMARY KEY (id_favorito);


--
-- Name: fotomascota fotomascota_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.fotomascota
    ADD CONSTRAINT fotomascota_pkey PRIMARY KEY (id_fotomascota);


--
-- Name: intervencionmedica intervencionmedica_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.intervencionmedica
    ADD CONSTRAINT intervencionmedica_pkey PRIMARY KEY (id_intervencionmedica);


--
-- Name: mascota mascota_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.mascota
    ADD CONSTRAINT mascota_pkey PRIMARY KEY (id_mascota);


--
-- Name: raza raza_id_especie_nombreraza_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.raza
    ADD CONSTRAINT raza_id_especie_nombreraza_key UNIQUE (id_especie, nombreraza);


--
-- Name: raza raza_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.raza
    ADD CONSTRAINT raza_pkey PRIMARY KEY (id_raza);


--
-- Name: refugio refugio_id_usuario_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.refugio
    ADD CONSTRAINT refugio_id_usuario_key UNIQUE (id_usuario);


--
-- Name: refugio refugio_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.refugio
    ADD CONSTRAINT refugio_pkey PRIMARY KEY (id_refugio);


--
-- Name: solicitudadopcion solicitudadopcion_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.solicitudadopcion
    ADD CONSTRAINT solicitudadopcion_pkey PRIMARY KEY (id_solicitud);


--
-- Name: usuario usuario_correo_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.usuario
    ADD CONSTRAINT usuario_correo_key UNIQUE (correo);


--
-- Name: usuario usuario_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.usuario
    ADD CONSTRAINT usuario_pkey PRIMARY KEY (id_usuario);


--
-- Name: vacunabasica vacunabasica_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.vacunabasica
    ADD CONSTRAINT vacunabasica_pkey PRIMARY KEY (id_vacunabasica);


--
-- Name: favorito trg_favoritos_contador; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER trg_favoritos_contador AFTER INSERT OR DELETE ON public.favorito FOR EACH ROW EXECUTE FUNCTION public.fn_actualizar_contadorfavoritos();


--
-- Name: mascota trg_fecha_adopcion; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER trg_fecha_adopcion BEFORE UPDATE ON public.mascota FOR EACH ROW EXECUTE FUNCTION public.fn_registrar_fecha_adopcion();


--
-- Name: solicitudadopcion trg_solicitud_aprobada; Type: TRIGGER; Schema: public; Owner: -
--

CREATE TRIGGER trg_solicitud_aprobada AFTER UPDATE OF estadodesolicitud ON public.solicitudadopcion FOR EACH ROW WHEN (((old.estadodesolicitud)::text IS DISTINCT FROM (new.estadodesolicitud)::text)) EXECUTE FUNCTION public.fn_solicitud_aprobada();


--
-- Name: adoptante adoptante_id_usuario_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.adoptante
    ADD CONSTRAINT adoptante_id_usuario_fkey FOREIGN KEY (id_usuario) REFERENCES public.usuario(id_usuario) ON DELETE CASCADE;


--
-- Name: detallemascotavacunas detallemascotavacunas_id_mascota_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.detallemascotavacunas
    ADD CONSTRAINT detallemascotavacunas_id_mascota_fkey FOREIGN KEY (id_mascota) REFERENCES public.mascota(id_mascota) ON DELETE CASCADE;


--
-- Name: detallemascotavacunas detallemascotavacunas_id_vacunabasica_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.detallemascotavacunas
    ADD CONSTRAINT detallemascotavacunas_id_vacunabasica_fkey FOREIGN KEY (id_vacunabasica) REFERENCES public.vacunabasica(id_vacunabasica) ON DELETE CASCADE;


--
-- Name: favorito favorito_id_adoptante_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.favorito
    ADD CONSTRAINT favorito_id_adoptante_fkey FOREIGN KEY (id_adoptante) REFERENCES public.adoptante(id_adoptante) ON DELETE CASCADE;


--
-- Name: favorito favorito_id_mascota_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.favorito
    ADD CONSTRAINT favorito_id_mascota_fkey FOREIGN KEY (id_mascota) REFERENCES public.mascota(id_mascota) ON DELETE CASCADE;


--
-- Name: fotomascota fotomascota_id_mascota_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.fotomascota
    ADD CONSTRAINT fotomascota_id_mascota_fkey FOREIGN KEY (id_mascota) REFERENCES public.mascota(id_mascota) ON DELETE CASCADE;


--
-- Name: intervencionmedica intervencionmedica_id_mascota_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.intervencionmedica
    ADD CONSTRAINT intervencionmedica_id_mascota_fkey FOREIGN KEY (id_mascota) REFERENCES public.mascota(id_mascota) ON DELETE CASCADE;


--
-- Name: mascota mascota_id_raza_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.mascota
    ADD CONSTRAINT mascota_id_raza_fkey FOREIGN KEY (id_raza) REFERENCES public.raza(id_raza) ON DELETE RESTRICT;


--
-- Name: mascota mascota_id_refugio_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.mascota
    ADD CONSTRAINT mascota_id_refugio_fkey FOREIGN KEY (id_refugio) REFERENCES public.refugio(id_refugio) ON DELETE CASCADE;


--
-- Name: raza raza_id_especie_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.raza
    ADD CONSTRAINT raza_id_especie_fkey FOREIGN KEY (id_especie) REFERENCES public.especie(id_especie) ON DELETE CASCADE;


--
-- Name: refugio refugio_id_usuario_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.refugio
    ADD CONSTRAINT refugio_id_usuario_fkey FOREIGN KEY (id_usuario) REFERENCES public.usuario(id_usuario) ON DELETE CASCADE;


--
-- Name: solicitudadopcion solicitudadopcion_id_adoptante_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.solicitudadopcion
    ADD CONSTRAINT solicitudadopcion_id_adoptante_fkey FOREIGN KEY (id_adoptante) REFERENCES public.adoptante(id_adoptante) ON DELETE CASCADE;


--
-- Name: solicitudadopcion solicitudadopcion_id_mascota_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.solicitudadopcion
    ADD CONSTRAINT solicitudadopcion_id_mascota_fkey FOREIGN KEY (id_mascota) REFERENCES public.mascota(id_mascota) ON DELETE CASCADE;


--
-- Name: solicitudadopcion solicitudadopcion_id_refugio_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.solicitudadopcion
    ADD CONSTRAINT solicitudadopcion_id_refugio_fkey FOREIGN KEY (id_refugio) REFERENCES public.refugio(id_refugio) ON DELETE CASCADE;


--
-- Name: vacunabasica vacunabasica_id_especie_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.vacunabasica
    ADD CONSTRAINT vacunabasica_id_especie_fkey FOREIGN KEY (id_especie) REFERENCES public.especie(id_especie) ON DELETE CASCADE;


--
-- Name: fotomascota Permitir eliminar fotos; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY "Permitir eliminar fotos" ON public.fotomascota FOR DELETE USING (true);


--
-- Name: intervencionmedica Permitir eliminar intervenciones; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY "Permitir eliminar intervenciones" ON public.intervencionmedica FOR DELETE USING (true);


--
-- Name: detallemascotavacunas Permitir eliminar vacunas; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY "Permitir eliminar vacunas" ON public.detallemascotavacunas FOR DELETE USING (true);


--
-- Name: adoptante; Type: ROW SECURITY; Schema: public; Owner: -
--

ALTER TABLE public.adoptante ENABLE ROW LEVEL SECURITY;

--
-- Name: detallemascotavacunas; Type: ROW SECURITY; Schema: public; Owner: -
--

ALTER TABLE public.detallemascotavacunas ENABLE ROW LEVEL SECURITY;

--
-- Name: especie; Type: ROW SECURITY; Schema: public; Owner: -
--

ALTER TABLE public.especie ENABLE ROW LEVEL SECURITY;

--
-- Name: favorito; Type: ROW SECURITY; Schema: public; Owner: -
--

ALTER TABLE public.favorito ENABLE ROW LEVEL SECURITY;

--
-- Name: fotomascota; Type: ROW SECURITY; Schema: public; Owner: -
--

ALTER TABLE public.fotomascota ENABLE ROW LEVEL SECURITY;

--
-- Name: intervencionmedica; Type: ROW SECURITY; Schema: public; Owner: -
--

ALTER TABLE public.intervencionmedica ENABLE ROW LEVEL SECURITY;

--
-- Name: mascota; Type: ROW SECURITY; Schema: public; Owner: -
--

ALTER TABLE public.mascota ENABLE ROW LEVEL SECURITY;

--
-- Name: adoptante pol_adoptante_insert; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY pol_adoptante_insert ON public.adoptante FOR INSERT WITH CHECK ((auth.uid() = id_usuario));


--
-- Name: adoptante pol_adoptante_select; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY pol_adoptante_select ON public.adoptante FOR SELECT USING ((auth.uid() = id_usuario));


--
-- Name: adoptante pol_adoptante_update; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY pol_adoptante_update ON public.adoptante FOR UPDATE USING ((auth.uid() = id_usuario));


--
-- Name: especie pol_especie_select; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY pol_especie_select ON public.especie FOR SELECT USING ((auth.role() = 'authenticated'::text));


--
-- Name: favorito pol_favorito_delete; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY pol_favorito_delete ON public.favorito FOR DELETE USING ((id_adoptante IN ( SELECT adoptante.id_adoptante
   FROM public.adoptante
  WHERE (adoptante.id_usuario = auth.uid()))));


--
-- Name: favorito pol_favorito_insert; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY pol_favorito_insert ON public.favorito FOR INSERT WITH CHECK ((id_adoptante IN ( SELECT adoptante.id_adoptante
   FROM public.adoptante
  WHERE (adoptante.id_usuario = auth.uid()))));


--
-- Name: favorito pol_favorito_select; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY pol_favorito_select ON public.favorito FOR SELECT USING ((id_adoptante IN ( SELECT adoptante.id_adoptante
   FROM public.adoptante
  WHERE (adoptante.id_usuario = auth.uid()))));


--
-- Name: fotomascota pol_fotomascota_insert; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY pol_fotomascota_insert ON public.fotomascota FOR INSERT WITH CHECK ((id_mascota IN ( SELECT m.id_mascota
   FROM (public.mascota m
     JOIN public.refugio r ON ((m.id_refugio = r.id_refugio)))
  WHERE (r.id_usuario = auth.uid()))));


--
-- Name: fotomascota pol_fotomascota_select; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY pol_fotomascota_select ON public.fotomascota FOR SELECT USING ((auth.role() = 'authenticated'::text));


--
-- Name: intervencionmedica pol_intervencion_insert; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY pol_intervencion_insert ON public.intervencionmedica FOR INSERT WITH CHECK ((id_mascota IN ( SELECT mascota.id_mascota
   FROM public.mascota
  WHERE (mascota.id_refugio IN ( SELECT refugio.id_refugio
           FROM public.refugio
          WHERE (refugio.id_usuario = auth.uid()))))));


--
-- Name: intervencionmedica pol_intervencion_select; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY pol_intervencion_select ON public.intervencionmedica FOR SELECT USING ((auth.role() = 'authenticated'::text));


--
-- Name: mascota pol_mascota_delete; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY pol_mascota_delete ON public.mascota FOR DELETE USING ((id_refugio IN ( SELECT refugio.id_refugio
   FROM public.refugio
  WHERE (refugio.id_usuario = auth.uid()))));


--
-- Name: mascota pol_mascota_insert; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY pol_mascota_insert ON public.mascota FOR INSERT WITH CHECK ((id_refugio IN ( SELECT refugio.id_refugio
   FROM public.refugio
  WHERE (refugio.id_usuario = auth.uid()))));


--
-- Name: mascota pol_mascota_select_public; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY pol_mascota_select_public ON public.mascota FOR SELECT USING ((auth.role() = 'authenticated'::text));


--
-- Name: mascota pol_mascota_update; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY pol_mascota_update ON public.mascota FOR UPDATE USING ((id_refugio IN ( SELECT refugio.id_refugio
   FROM public.refugio
  WHERE (refugio.id_usuario = auth.uid()))));


--
-- Name: raza pol_raza_select; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY pol_raza_select ON public.raza FOR SELECT USING ((auth.role() = 'authenticated'::text));


--
-- Name: refugio pol_refugio_insert; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY pol_refugio_insert ON public.refugio FOR INSERT WITH CHECK ((auth.uid() = id_usuario));


--
-- Name: refugio pol_refugio_select; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY pol_refugio_select ON public.refugio FOR SELECT USING ((auth.uid() = id_usuario));


--
-- Name: refugio pol_refugio_select_public; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY pol_refugio_select_public ON public.refugio FOR SELECT USING ((auth.role() = 'authenticated'::text));


--
-- Name: refugio pol_refugio_update; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY pol_refugio_update ON public.refugio FOR UPDATE USING ((auth.uid() = id_usuario));


--
-- Name: solicitudadopcion pol_solicitud_insert; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY pol_solicitud_insert ON public.solicitudadopcion FOR INSERT WITH CHECK ((id_adoptante IN ( SELECT adoptante.id_adoptante
   FROM public.adoptante
  WHERE (adoptante.id_usuario = auth.uid()))));


--
-- Name: solicitudadopcion pol_solicitud_select_adoptante; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY pol_solicitud_select_adoptante ON public.solicitudadopcion FOR SELECT USING ((id_adoptante IN ( SELECT adoptante.id_adoptante
   FROM public.adoptante
  WHERE (adoptante.id_usuario = auth.uid()))));


--
-- Name: solicitudadopcion pol_solicitud_select_refugio; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY pol_solicitud_select_refugio ON public.solicitudadopcion FOR SELECT USING ((id_refugio IN ( SELECT refugio.id_refugio
   FROM public.refugio
  WHERE (refugio.id_usuario = auth.uid()))));


--
-- Name: solicitudadopcion pol_solicitud_update_refugio; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY pol_solicitud_update_refugio ON public.solicitudadopcion FOR UPDATE USING ((id_refugio IN ( SELECT refugio.id_refugio
   FROM public.refugio
  WHERE (refugio.id_usuario = auth.uid()))));


--
-- Name: usuario pol_usuario_insert; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY pol_usuario_insert ON public.usuario FOR INSERT WITH CHECK ((auth.uid() = id_usuario));


--
-- Name: usuario pol_usuario_select; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY pol_usuario_select ON public.usuario FOR SELECT USING ((auth.uid() = id_usuario));


--
-- Name: usuario pol_usuario_update; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY pol_usuario_update ON public.usuario FOR UPDATE USING ((auth.uid() = id_usuario));


--
-- Name: vacunabasica pol_vacunabasica_select; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY pol_vacunabasica_select ON public.vacunabasica FOR SELECT USING ((auth.role() = 'authenticated'::text));


--
-- Name: detallemascotavacunas pol_vacunas_select; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY pol_vacunas_select ON public.detallemascotavacunas FOR SELECT USING ((auth.role() = 'authenticated'::text));


--
-- Name: detallemascotavacunas pol_vacunasmascota_insert; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY pol_vacunasmascota_insert ON public.detallemascotavacunas FOR INSERT WITH CHECK ((id_mascota IN ( SELECT mascota.id_mascota
   FROM public.mascota
  WHERE (mascota.id_refugio IN ( SELECT refugio.id_refugio
           FROM public.refugio
          WHERE (refugio.id_usuario = auth.uid()))))));


--
-- Name: raza; Type: ROW SECURITY; Schema: public; Owner: -
--

ALTER TABLE public.raza ENABLE ROW LEVEL SECURITY;

--
-- Name: refugio; Type: ROW SECURITY; Schema: public; Owner: -
--

ALTER TABLE public.refugio ENABLE ROW LEVEL SECURITY;

--
-- Name: solicitudadopcion; Type: ROW SECURITY; Schema: public; Owner: -
--

ALTER TABLE public.solicitudadopcion ENABLE ROW LEVEL SECURITY;

--
-- Name: mascota trigger_update_contador; Type: POLICY; Schema: public; Owner: -
--

CREATE POLICY trigger_update_contador ON public.mascota FOR UPDATE TO postgres USING (true) WITH CHECK (true);


--
-- Name: usuario; Type: ROW SECURITY; Schema: public; Owner: -
--

ALTER TABLE public.usuario ENABLE ROW LEVEL SECURITY;

--
-- Name: vacunabasica; Type: ROW SECURITY; Schema: public; Owner: -
--

ALTER TABLE public.vacunabasica ENABLE ROW LEVEL SECURITY;

--
-- PostgreSQL database dump complete
--

\unrestrict xc5OIXGgQxx7dqi4rgNaV3qbTcFP8oYTGr1bcDvohIim6DE8YCS45PexzgmoZ2j

