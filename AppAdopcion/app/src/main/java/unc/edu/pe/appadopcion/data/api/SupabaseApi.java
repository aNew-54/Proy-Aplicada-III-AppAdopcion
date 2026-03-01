package unc.edu.pe.appadopcion.data.api;

import java.util.List;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

import unc.edu.pe.appadopcion.data.model.*;

/**
 * Interfaz central de la API de Supabase (PostgREST + GoTrue).
 *
 * Convención de parámetros PostgREST:
 *   @Query("campo")  "eq.valor"   → WHERE campo = valor
 *   @Query("campo")  "gt.valor"   → WHERE campo > valor
 *   @Query("select") "col1,col2"  → SELECT col1, col2
 *   @Query("order")  "campo.desc" → ORDER BY campo DESC
 *   @Query("limit")  "20"         → LIMIT 20
 *   @Query("offset") "0"          → OFFSET 0
 */
public interface SupabaseApi {

    // ════════════════════════════════════════════════════════
    // AUTH (GoTrue)
    // ════════════════════════════════════════════════════════

    @POST("auth/v1/signup")
    Call<AuthResponse> registrarCredenciales(@Body AuthRequest request);

    @POST("auth/v1/token?grant_type=password")
    Call<AuthResponse> loginUsuario(@Body AuthRequest request);


    // ════════════════════════════════════════════════════════
    // USUARIO
    // ════════════════════════════════════════════════════════

    @POST("rest/v1/usuario")
    Call<Void> crearUsuarioBase(@Body UsuarioRequest usuario);

    /** Obtener usuario por UUID */
    @GET("rest/v1/usuario")
    Call<List<UsuarioRequest>> obtenerUsuarioPorId(@Query("id_usuario") String eqUuid);

    /** Actualizar datos del usuario (foto, teléfono) */
    @PATCH("rest/v1/usuario")
    Call<Void> actualizarUsuario(
            @Query("id_usuario") String eqUuid,
            @Body UsuarioRequest usuario);


    // ════════════════════════════════════════════════════════
    // ADOPTANTE
    // ════════════════════════════════════════════════════════

    @POST("rest/v1/adoptante")
    Call<Void> crearPerfilAdoptante(@Body AdoptanteRequest adoptante);

    /** Obtener perfil adoptante por UUID de usuario */
    @GET("rest/v1/adoptante")
    Call<List<AdoptanteRequest>> obtenerAdoptantePorUuid(@Query("id_usuario") String eqUuid);

    /** Obtener vista completa del adoptante (con contadores) */
    @GET("rest/v1/vista_adoptante_completo")
    Call<List<AdoptanteResponse>> obtenerAdoptanteCompleto(@Query("id_usuario") String eqUuid);

    @PATCH("rest/v1/adoptante")
    Call<Void> actualizarAdoptante(
            @Query("id_usuario") String eqUuid,
            @Body AdoptanteRequest adoptante);


    // ════════════════════════════════════════════════════════
    // REFUGIO
    // ════════════════════════════════════════════════════════

    @POST("rest/v1/refugio")
    Call<Void> crearPerfilRefugio(@Body RefugioRequest refugio);

    /** Obtener perfil refugio por UUID de usuario */
    @GET("rest/v1/refugio")
    Call<List<RefugioRequest>> obtenerRefugioPorUuid(@Query("id_usuario") String eqUuid);

    /** Vista completa del refugio (con contador de mascotas) */
    @GET("rest/v1/vista_refugios_completa")
    Call<List<RefugioResponse>> obtenerRefugioCompleto(@Query("id_usuario") String eqUuid);

    /** Obtener todos los refugios (para la pantalla Refugios) */
    @GET("rest/v1/vista_refugios_completa")
    Call<List<RefugioResponse>> obtenerTodosRefugios();

    /** Obtener refugio por id_refugio */
    @GET("rest/v1/vista_refugios_completa")
    Call<List<RefugioResponse>> obtenerRefugioPorId(@Query("id_refugio") String eqId);

    @PATCH("rest/v1/refugio")
    Call<Void> actualizarRefugio(
            @Query("id_usuario") String eqUuid,
            @Body RefugioRequest refugio);


    // ════════════════════════════════════════════════════════
    // MASCOTAS - Vista completa (incluye raza, especie, refugio)
    // ════════════════════════════════════════════════════════

    /**
     * Obtener todas las mascotas disponibles.
     * Usar select=* sobre la vista vista_mascotas_completa.
     */
    @GET("rest/v1/vista_mascotas_completa")
    Call<List<MascotaResponse>> obtenerMascotasDisponibles(
            @Query("estado")  String eqDisponible,   // "eq.Disponible"
            @Query("order")   String orden,           // ej: "fecharegistro.desc"
            @Query("limit")   String limit,
            @Query("offset")  String offset
    );

    /** Obtener mascota por ID */
    @GET("rest/v1/vista_mascotas_completa")
    Call<List<MascotaResponse>> obtenerMascotaPorId(@Query("id_mascota") String eqId);

    /** Buscar mascotas por nombre (ilike = case-insensitive) */
    @GET("rest/v1/vista_mascotas_completa")
    Call<List<MascotaResponse>> buscarMascotasPorNombre(
            @Query("nombremascota") String ilikeName,  // "ilike.*perro*"
            @Query("estado")        String eqEstado,
            @Query("order")         String orden
    );

    /** Filtrar mascotas por especie */
    @GET("rest/v1/vista_mascotas_completa")
    Call<List<MascotaResponse>> filtrarMascotasPorEspecie(
            @Query("id_especie") String eqEspecie,
            @Query("estado")     String eqEstado,
            @Query("order")      String orden,
            @Query("limit")      String limit,
            @Query("offset")     String offset
    );

    /** Filtrar mascotas por raza */
    @GET("rest/v1/vista_mascotas_completa")
    Call<List<MascotaResponse>> filtrarMascotasPorRaza(
            @Query("id_raza") String eqRaza,
            @Query("estado")  String eqEstado,
            @Query("order")   String orden
    );

    /** Filtrar mascotas por refugio */
    @GET("rest/v1/vista_mascotas_completa")
    Call<List<MascotaResponse>> obtenerMascotasDeRefugio(
            @Query("id_refugio") String eqRefugio,
            @Query("order")      String orden
    );

    /** Filtrar por género */
    @GET("rest/v1/vista_mascotas_completa")
    Call<List<MascotaResponse>> filtrarPorGenero(
            @Query("genero")  String eqGenero,
            @Query("estado")  String eqEstado,
            @Query("order")   String orden
    );

    /** Insertar mascota */
    @POST("rest/v1/mascota")
    Call<Void> crearMascota(@Body MascotaRequest mascota);

    /** Actualizar mascota */
    @PATCH("rest/v1/mascota")
    Call<Void> actualizarMascota(
            @Query("id_mascota") String eqId,
            @Body MascotaRequest mascota);


    // ════════════════════════════════════════════════════════
    // FOTOS DE MASCOTA
    // ════════════════════════════════════════════════════════

    @GET("rest/v1/fotomascota")
    Call<List<FotoMascotaResponse>> obtenerFotosDeMascota(@Query("id_mascota") String eqId);

    @POST("rest/v1/fotomascota")
    Call<Void> agregarFotoMascota(@Body FotoMascotaResponse foto);


    // ════════════════════════════════════════════════════════
    // FAVORITOS
    // ════════════════════════════════════════════════════════

    /** Obtener favoritos del adoptante con info completa de la mascota */
    @GET("rest/v1/favorito")
    Call<List<FavoritoResponse>> obtenerFavoritos(
            @Query("id_adoptante") String eqAdoptante,
            @Query("select")       String select   // "id_favorito,id_mascota,fechaagregado"
    );

    /** Verificar si una mascota ya está en favoritos */
    @GET("rest/v1/favorito")
    Call<List<FavoritoResponse>> verificarFavorito(
            @Query("id_mascota")   String eqMascota,
            @Query("id_adoptante") String eqAdoptante
    );

    @POST("rest/v1/favorito")
    Call<Void> agregarFavorito(@Body FavoritoRequest favorito);

    /** Eliminar favorito (por id_mascota e id_adoptante) */
    @DELETE("rest/v1/favorito")
    Call<Void> eliminarFavorito(
            @Query("id_mascota")   String eqMascota,
            @Query("id_adoptante") String eqAdoptante
    );

    /** Mascota con más favoritos del refugio (para el perfil) */
    @GET("rest/v1/vista_mascotas_completa")
    Call<List<MascotaResponse>> mascotaMasPopularDeRefugio(
            @Query("id_refugio") String eqRefugio,
            @Query("order")      String orderDesc,  // "contadorfavoritos.desc"
            @Query("limit")      String limit       // "1"
    );


    // ════════════════════════════════════════════════════════
    // SOLICITUDES DE ADOPCIÓN
    // ════════════════════════════════════════════════════════

    @POST("rest/v1/solicitudadopcion")
    Call<Void> crearSolicitud(@Body SolicitudRequest solicitud);

    /** Solicitudes enviadas por el adoptante */
    @GET("rest/v1/solicitudadopcion")
    Call<List<SolicitudResponse>> obtenerSolicitudesAdoptante(
            @Query("id_adoptante") String eqAdoptante,
            @Query("order")        String orden
    );

    /** Solicitudes recibidas por el refugio */
    @GET("rest/v1/solicitudadopcion")
    Call<List<SolicitudResponse>> obtenerSolicitudesRefugio(
            @Query("id_refugio") String eqRefugio,
            @Query("order")      String orden
    );

    /** Actualizar estado de una solicitud (refugio aprueba/rechaza) */
    @PATCH("rest/v1/solicitudadopcion")
    Call<Void> actualizarEstadoSolicitud(
            @Query("id_solicitud") String eqId,
            @Body SolicitudRequest solicitud
    );


    // ════════════════════════════════════════════════════════
    // CATÁLOGOS (Especie, Raza, Vacunas)
    // ════════════════════════════════════════════════════════

    @GET("rest/v1/especie")
    Call<List<EspecieResponse>> obtenerEspecies();

    @GET("rest/v1/raza")
    Call<List<RazaResponse>> obtenerRazas();

    /** Razas filtradas por especie */
    @GET("rest/v1/raza")
    Call<List<RazaResponse>> obtenerRazasPorEspecie(@Query("id_especie") String eqEspecie);

    /** Razas que tienen mascotas registradas (para las etiquetas del filtro) */
    @GET("rest/v1/vista_mascotas_completa")
    Call<List<MascotaResponse>> obtenerRazasConMascotas(
            @Query("select")  String select,  // "id_raza,nombreraza,id_especie,nombreespecie"
            @Query("estado")  String eqEstado,
            @Query("order")   String orden
    );

    @GET("rest/v1/vacunabasica")
    Call<List<VacunaResponse>> obtenerVacunasPorEspecie(@Query("id_especie") String eqEspecie);


    // ════════════════════════════════════════════════════════
    // HISTORIAL MÉDICO
    // ════════════════════════════════════════════════════════

    @GET("rest/v1/detallemascotavacunas")
    Call<List<VacunaMascotaResponse>> obtenerVacunasDeMascota(@Query("id_mascota") String eqId);

    @GET("rest/v1/intervencionmedica")
    Call<List<IntervencionResponse>> obtenerIntervencionesDeMascota(@Query("id_mascota") String eqId);

    // ══════════════════════════════════════════════════════════════
    // POST MASCOTA
    // ══════════════════════════════════════════════════════════════

    @POST("rest/v1/mascota")
    Call<List<MascotaResponse>> crearMascotaConRetorno(
            @Header("Prefer") String prefer,
            @Body MascotaRequest mascota
    );


    // ── FOTOS DE MASCOTA ──────────────────────────────────────────
    @POST("rest/v1/fotomascota")
    Call<Void> agregarFotoMascota(@Body FotoMascotaRequest foto);


    // ── VACUNAS DE MASCOTA ────────────────────────────────────────
    @POST("rest/v1/detallemascotavacunas")
    Call<Void> agregarVacunaMascota(@Body VacunaMascotaRequest vacuna);


    // ── INTERVENCIONES ────────────────────────────────────────────
    @POST("rest/v1/intervencionmedica")
    Call<Void> agregarIntervencion(@Body IntervencionRequest intervencion);


    // ── STORAGE — subir imagen ────────────────────────────────────
    // nombre_bucket: el nombre exacto de tu bucket en Supabase Storage
    // path: ruta dentro del bucket, ej: "mascotas/portada_uuid.jpg"
    @PUT("storage/v1/object/{bucket}/{path}")
    Call<Void> subirImagen(
            @Header("Content-Type") String contentType,  // "image/jpeg"
            @Path("bucket") String bucket,
            @Path(value = "path", encoded = true) String path,
            @Body RequestBody imagen
    );


}