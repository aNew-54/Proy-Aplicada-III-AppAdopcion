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

    @GET("rest/v1/usuario")
    Call<List<UsuarioRequest>> obtenerUsuarioPorId(@Query("id_usuario") String eqUuid);

    @PATCH("rest/v1/usuario")
    Call<Void> actualizarUsuario(
            @Query("id_usuario") String eqUuid,
            @Body UsuarioRequest usuario);


    // ════════════════════════════════════════════════════════
    // ADOPTANTE
    // ════════════════════════════════════════════════════════

    @POST("rest/v1/adoptante")
    Call<Void> crearPerfilAdoptante(@Body AdoptanteRequest adoptante);

    @GET("rest/v1/adoptante")
    Call<List<AdoptanteRequest>> obtenerAdoptantePorUuid(@Query("id_usuario") String eqUuid);

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

    @GET("rest/v1/refugio")
    Call<List<RefugioRequest>> obtenerRefugioPorUuid(@Query("id_usuario") String eqUuid);

    @GET("rest/v1/vista_refugios_completa")
    Call<List<RefugioResponse>> obtenerRefugioCompleto(@Query("id_usuario") String eqUuid);

    @GET("rest/v1/vista_refugios_completa")
    Call<List<RefugioResponse>> obtenerTodosRefugios();

    @GET("rest/v1/vista_refugios_completa")
    Call<List<RefugioResponse>> obtenerRefugioPorId(@Query("id_refugio") String eqId);

    @PATCH("rest/v1/refugio")
    Call<Void> actualizarRefugio(
            @Query("id_usuario") String eqUuid,
            @Body RefugioRequest refugio);


    // ════════════════════════════════════════════════════════
    // MASCOTAS
    // ════════════════════════════════════════════════════════

    @GET("rest/v1/vista_mascotas_completa")
    Call<List<MascotaResponse>> obtenerMascotasDisponibles(
            @Query("estado")  String eqDisponible,
            @Query("order")   String orden,
            @Query("limit")   String limit,
            @Query("offset")  String offset
    );

    @GET("rest/v1/vista_mascotas_completa")
    Call<List<MascotaResponse>> obtenerMascotaPorId(@Query("id_mascota") String eqId);

    @GET("rest/v1/vista_mascotas_completa")
    Call<List<MascotaResponse>> buscarMascotasPorNombre(
            @Query("nombremascota") String ilikeName,
            @Query("estado")        String eqEstado,
            @Query("order")         String orden
    );

    @GET("rest/v1/vista_mascotas_completa")
    Call<List<MascotaResponse>> filtrarMascotasPorEspecie(
            @Query("id_especie") String eqEspecie,
            @Query("estado")     String eqEstado,
            @Query("order")      String orden,
            @Query("limit")      String limit,
            @Query("offset")     String offset
    );

    @GET("rest/v1/vista_mascotas_completa")
    Call<List<MascotaResponse>> filtrarMascotasPorRaza(
            @Query("id_raza") String eqRaza,
            @Query("estado")  String eqEstado,
            @Query("order")   String orden
    );

    @GET("rest/v1/vista_mascotas_completa")
    Call<List<MascotaResponse>> obtenerMascotasDeRefugio(
            @Query("id_refugio") String eqRefugio,
            @Query("order")      String orden
    );

    @GET("rest/v1/vista_mascotas_completa")
    Call<List<MascotaResponse>> filtrarPorGenero(
            @Query("genero")  String eqGenero,
            @Query("estado")  String eqEstado,
            @Query("order")   String orden
    );

    @POST("rest/v1/mascota")
    Call<Void> crearMascota(@Body MascotaRequest mascota);

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

    @DELETE("rest/v1/fotomascota")
    Call<Void> eliminarFotosDeMascota(@Query("id_mascota") String eqId);


    // ════════════════════════════════════════════════════════
    // FAVORITOS
    // ════════════════════════════════════════════════════════

    @GET("rest/v1/favorito")
    Call<List<FavoritoResponse>> obtenerFavoritos(
            @Query("id_adoptante") String eqAdoptante,
            @Query("select")       String select
    );

    @GET("rest/v1/favorito")
    Call<List<FavoritoResponse>> verificarFavorito(
            @Query("id_mascota")   String eqMascota,
            @Query("id_adoptante") String eqAdoptante
    );

    @POST("rest/v1/favorito")
    Call<Void> agregarFavorito(@Body FavoritoRequest favorito);

    @DELETE("rest/v1/favorito")
    Call<Void> eliminarFavorito(
            @Query("id_mascota")   String eqMascota,
            @Query("id_adoptante") String eqAdoptante
    );

    @GET("rest/v1/vista_mascotas_completa")
    Call<List<MascotaResponse>> mascotaMasPopularDeRefugio(
            @Query("id_refugio") String eqRefugio,
            @Query("order")      String orderDesc,
            @Query("limit")      String limit
    );


    // ════════════════════════════════════════════════════════
    // SOLICITUDES DE ADOPCIÓN
    // ════════════════════════════════════════════════════════

    @POST("rest/v1/solicitudadopcion")
    Call<Void> crearSolicitud(@Body SolicitudRequest solicitud);

    @GET("rest/v1/solicitudadopcion")
    Call<List<SolicitudResponse>> obtenerSolicitudesAdoptante(
            @Query("id_adoptante") String eqAdoptante,
            @Query("order")        String orden
    );

    @GET("rest/v1/solicitudadopcion")
    Call<List<SolicitudResponse>> obtenerSolicitudesRefugio(
            @Query("id_refugio") String eqRefugio,
            @Query("order")      String orden
    );

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

    @GET("rest/v1/raza")
    Call<List<RazaResponse>> obtenerRazasPorEspecie(@Query("id_especie") String eqEspecie);

    @GET("rest/v1/vista_mascotas_completa")
    Call<List<MascotaResponse>> obtenerRazasConMascotas(
            @Query("select")  String select,
            @Query("estado")  String eqEstado,
            @Query("order")   String orden
    );

    @GET("rest/v1/vacunabasica")
    Call<List<VacunaResponse>> obtenerVacunasPorEspecie(@Query("id_especie") String eqEspecie);


    // ════════════════════════════════════════════════════════
    // HISTORIAL MÉDICO
    // ════════════════════════════════════════════════════════

    @GET("rest/v1/vista_vacunas_mascota")
    Call<List<VacunaMascotaResponse>> obtenerVacunasDeMascota(@Query("id_mascota") String eqId);

    @GET("rest/v1/intervencionmedica")
    Call<List<IntervencionResponse>> obtenerIntervencionesDeMascota(@Query("id_mascota") String eqId);

    @POST("rest/v1/mascota")
    Call<List<MascotaResponse>> crearMascotaConRetorno(
            @Header("Prefer") String prefer,
            @Body MascotaRequest mascota
    );

    @POST("rest/v1/fotomascota")
    Call<Void> agregarFotoMascota(@Body FotoMascotaRequest foto);

    @POST("rest/v1/detallemascotavacunas")
    Call<Void> agregarVacunaMascota(@Body VacunaMascotaRequest vacuna);

    @DELETE("rest/v1/detallemascotavacunas")
    Call<Void> eliminarVacunasMascota(@Query("id_mascota") String eqId);

    @POST("rest/v1/intervencionmedica")
    Call<Void> agregarIntervencion(@Body IntervencionRequest intervencion);

    @DELETE("rest/v1/intervencionmedica")
    Call<Void> eliminarIntervencionesMascota(@Query("id_mascota") String eqId);

    @PUT("storage/v1/object/{bucket}/{path}")
    Call<Void> subirImagen(
            @Header("Content-Type") String contentType,
            @Path("bucket") String bucket,
            @Path(value = "path", encoded = true) String path,
            @Body RequestBody imagen
    );
}