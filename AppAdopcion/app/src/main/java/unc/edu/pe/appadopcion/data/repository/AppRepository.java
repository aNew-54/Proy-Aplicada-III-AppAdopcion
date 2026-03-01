package unc.edu.pe.appadopcion.data.repository;

import java.util.List;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Callback;
import unc.edu.pe.appadopcion.data.api.SupabaseApi;
import unc.edu.pe.appadopcion.data.api.SupabaseClient;
import unc.edu.pe.appadopcion.data.model.*;

/**
 * ══════════════════════════════════════════════════════════════
 * AppRepository — PUNTO DE ENTRADA ÚNICO PARA TODA LA LÓGICA
 * ══════════════════════════════════════════════════════════════
 *
 * USO DESDE UN VIEWMODEL O ACTIVITY:
 *
 *   AppRepository repo = new AppRepository(token);
 *
 *   // Ejemplo: obtener mascotas disponibles
 *   repo.obtenerMascotasDisponibles("fecharegistro.desc", 20, 0,
 *       new Callback<List<MascotaResponse>>() {
 *           @Override
 *           public void onResponse(...) { ... }
 *           @Override
 *           public void onFailure(...) { ... }
 *       });
 *
 * NOTA: Siempre pasa el token JWT del usuario (de SessionManager.getToken()).
 * Si el endpoint es público (login, registro), usa el constructor sin token.
 */
public class AppRepository {

    private final SupabaseApi api;

    /** Para endpoints que requieren autenticación (la mayoría) */
    public AppRepository(String userToken) {
        this.api = SupabaseClient.getClient(userToken).create(SupabaseApi.class);
    }

    /** Solo para registro y login */
    public AppRepository() {
        this.api = SupabaseClient.getClient().create(SupabaseApi.class);
    }

    // ════════════════════════════════════════════════════════
    // AUTH
    // ════════════════════════════════════════════════════════

    public void login(String email, String password, Callback<AuthResponse> cb) {
        api.loginUsuario(new AuthRequest(email, password)).enqueue(cb);
    }

    public void registrar(String email, String password, Callback<AuthResponse> cb) {
        api.registrarCredenciales(new AuthRequest(email, password)).enqueue(cb);
    }

    // ════════════════════════════════════════════════════════
    // USUARIO
    // ════════════════════════════════════════════════════════

    public void crearUsuario(UsuarioRequest req, Callback<Void> cb) {
        api.crearUsuarioBase(req).enqueue(cb);
    }

    public void obtenerUsuario(String uuid, Callback<List<UsuarioRequest>> cb) {
        api.obtenerUsuarioPorId("eq." + uuid).enqueue(cb);
    }

    public void actualizarUsuario(String uuid, UsuarioRequest req, Callback<Void> cb) {
        api.actualizarUsuario("eq." + uuid, req).enqueue(cb);
    }

    // ════════════════════════════════════════════════════════
    // ADOPTANTE
    // ════════════════════════════════════════════════════════

    public void crearAdoptante(AdoptanteRequest req, Callback<Void> cb) {
        api.crearPerfilAdoptante(req).enqueue(cb);
    }

    public void obtenerAdoptantePorUuid(String uuid, Callback<List<AdoptanteRequest>> cb) {
        api.obtenerAdoptantePorUuid("eq." + uuid).enqueue(cb);
    }

    /** Obtiene el perfil completo del adoptante con contadores de favoritos y solicitudes */
    public void obtenerAdoptanteCompleto(String uuid, Callback<List<AdoptanteResponse>> cb) {
        api.obtenerAdoptanteCompleto("eq." + uuid).enqueue(cb);
    }

    public void actualizarAdoptante(String uuid, AdoptanteRequest req, Callback<Void> cb) {
        api.actualizarAdoptante("eq." + uuid, req).enqueue(cb);
    }

    // ════════════════════════════════════════════════════════
    // REFUGIO
    // ════════════════════════════════════════════════════════

    public void crearRefugio(RefugioRequest req, Callback<Void> cb) {
        api.crearPerfilRefugio(req).enqueue(cb);
    }

    public void obtenerRefugioPorUuid(String uuid, Callback<List<RefugioResponse>> cb) {
        api.obtenerRefugioCompleto("eq." + uuid).enqueue(cb);
    }

    public void obtenerRefugioPorId(int idRefugio, Callback<List<RefugioResponse>> cb) {
        api.obtenerRefugioPorId("eq." + idRefugio).enqueue(cb);
    }

    public void obtenerTodosRefugios(Callback<List<RefugioResponse>> cb) {
        api.obtenerTodosRefugios().enqueue(cb);
    }

    public void actualizarRefugio(String uuid, RefugioRequest req, Callback<Void> cb) {
        api.actualizarRefugio("eq." + uuid, req).enqueue(cb);
    }

    // ════════════════════════════════════════════════════════
    // MASCOTAS
    // ════════════════════════════════════════════════════════

    /**
     * Obtiene todas las mascotas disponibles.
     * @param orden  "fecharegistro.desc" | "contadorfavoritos.desc"
     * @param limit  cantidad por página (ej: 20)
     * @param offset página × limit
     */
    public void obtenerMascotasDisponibles(String orden, int limit, int offset,
                                           Callback<List<MascotaResponse>> cb) {
        api.obtenerMascotasDisponibles(
                "eq.Disponible", orden,
                String.valueOf(limit), String.valueOf(offset)
        ).enqueue(cb);
    }

    /** Obtiene una mascota por ID */
    public void obtenerMascota(int idMascota, Callback<List<MascotaResponse>> cb) {
        api.obtenerMascotaPorId("eq." + idMascota).enqueue(cb);
    }

    /**
     * Busca mascotas por nombre (búsqueda parcial).
     * @param nombre texto a buscar (ej: "luna")
     */
    public void buscarMascotasPorNombre(String nombre, String orden,
                                        Callback<List<MascotaResponse>> cb) {
        api.buscarMascotasPorNombre("ilike.*" + nombre + "*", "eq.Disponible", orden).enqueue(cb);
    }

    /** Filtra mascotas por especie (id de Especie) */
    public void filtrarPorEspecie(int idEspecie, String orden, int limit, int offset,
                                  Callback<List<MascotaResponse>> cb) {
        api.filtrarMascotasPorEspecie(
                "eq." + idEspecie, "eq.Disponible", orden,
                String.valueOf(limit), String.valueOf(offset)
        ).enqueue(cb);
    }

    /** Filtra mascotas por raza */
    public void filtrarPorRaza(int idRaza, String orden, Callback<List<MascotaResponse>> cb) {
        api.filtrarMascotasPorRaza("eq." + idRaza, "eq.Disponible", orden).enqueue(cb);
    }

    /** Filtra mascotas por género ("Macho" | "Hembra") */
    public void filtrarPorGenero(String genero, String orden, Callback<List<MascotaResponse>> cb) {
        api.filtrarPorGenero("eq." + genero, "eq.Disponible", orden).enqueue(cb);
    }

    /** Obtiene todas las mascotas de un refugio específico */
    public void obtenerMascotasDeRefugio(int idRefugio, String orden,
                                         Callback<List<MascotaResponse>> cb) {
        api.obtenerMascotasDeRefugio("eq." + idRefugio, orden).enqueue(cb);
    }

    public void crearMascota(MascotaRequest req, Callback<Void> cb) {
        api.crearMascota(req).enqueue(cb);
    }

    public void actualizarMascota(int idMascota, MascotaRequest req, Callback<Void> cb) {
        api.actualizarMascota("eq." + idMascota, req).enqueue(cb);
    }

    // ════════════════════════════════════════════════════════
    // FOTOS DE MASCOTA
    // ════════════════════════════════════════════════════════

    public void obtenerFotosMascota(int idMascota, Callback<List<FotoMascotaResponse>> cb) {
        api.obtenerFotosDeMascota("eq." + idMascota).enqueue(cb);
    }

    // ════════════════════════════════════════════════════════
    // FAVORITOS
    // ════════════════════════════════════════════════════════

    /** Obtiene los favoritos del adoptante (solo IDs, sin join) */
    public void obtenerFavoritos(int idAdoptante, Callback<List<FavoritoResponse>> cb) {
        api.obtenerFavoritos("eq." + idAdoptante, "id_favorito,id_mascota,fechaagregado").enqueue(cb);
    }

    /** Verifica si una mascota está en favoritos del adoptante */
    public void verificarEsFavorito(int idMascota, int idAdoptante,
                                    Callback<List<FavoritoResponse>> cb) {
        api.verificarFavorito("eq." + idMascota, "eq." + idAdoptante).enqueue(cb);
    }

    public void agregarFavorito(int idMascota, int idAdoptante, Callback<Void> cb) {
        api.agregarFavorito(new FavoritoRequest(idMascota, idAdoptante)).enqueue(cb);
    }

    public void eliminarFavorito(int idMascota, int idAdoptante, Callback<Void> cb) {
        api.eliminarFavorito("eq." + idMascota, "eq." + idAdoptante).enqueue(cb);
    }

    /** Mascota más popular del refugio (para el perfil) */
    public void mascotaMasPopularDeRefugio(int idRefugio, Callback<List<MascotaResponse>> cb) {
        api.mascotaMasPopularDeRefugio("eq." + idRefugio, "contadorfavoritos.desc", "1").enqueue(cb);
    }

    // ════════════════════════════════════════════════════════
    // SOLICITUDES
    // ════════════════════════════════════════════════════════

    public void crearSolicitud(int idRefugio, int idMascota, int idAdoptante,
                               String mensaje, Callback<Void> cb) {
        api.crearSolicitud(new SolicitudRequest(idRefugio, idMascota, idAdoptante, mensaje))
                .enqueue(cb);
    }

    public void obtenerSolicitudesAdoptante(int idAdoptante,
                                            Callback<List<SolicitudResponse>> cb) {
        api.obtenerSolicitudesAdoptante("eq." + idAdoptante, "fechadesolicitud.desc").enqueue(cb);
    }

    public void obtenerSolicitudesRefugio(int idRefugio,
                                          Callback<List<SolicitudResponse>> cb) {
        api.obtenerSolicitudesRefugio("eq." + idRefugio, "fechadesolicitud.desc").enqueue(cb);
    }

    public void aprobarSolicitud(int idSolicitud, String fechaVisita, Callback<Void> cb) {
        api.actualizarEstadoSolicitud("eq." + idSolicitud,
                new SolicitudRequest("Aprobada", fechaVisita, null)).enqueue(cb);
    }

    public void rechazarSolicitud(int idSolicitud, String notas, Callback<Void> cb) {
        api.actualizarEstadoSolicitud("eq." + idSolicitud,
                new SolicitudRequest("Rechazada", null, notas)).enqueue(cb);
    }

    public void agendarVisita(int idSolicitud, String fechaVisita, Callback<Void> cb) {
        api.actualizarEstadoSolicitud("eq." + idSolicitud,
                new SolicitudRequest("Visita Agendada", fechaVisita, null)).enqueue(cb);
    }

    // ════════════════════════════════════════════════════════
    // CATÁLOGOS
    // ════════════════════════════════════════════════════════

    public void obtenerEspecies(Callback<List<EspecieResponse>> cb) {
        api.obtenerEspecies().enqueue(cb);
    }

    public void obtenerRazas(Callback<List<RazaResponse>> cb) {
        api.obtenerRazas().enqueue(cb);
    }

    public void obtenerRazasPorEspecie(int idEspecie, Callback<List<RazaResponse>> cb) {
        api.obtenerRazasPorEspecie("eq." + idEspecie).enqueue(cb);
    }

    /**
     * Obtiene las razas que tienen mascotas publicadas actualmente.
     * Útil para mostrar solo las etiquetas de razas relevantes en el filtro.
     */
    public void obtenerRazasConMascotas(Callback<List<MascotaResponse>> cb) {
        api.obtenerRazasConMascotas(
                "id_raza,nombreraza,id_especie,nombreespecie",
                "eq.Disponible",
                "nombreraza.asc"
        ).enqueue(cb);
    }

    public void obtenerVacunasPorEspecie(int idEspecie, Callback<List<VacunaResponse>> cb) {
        api.obtenerVacunasPorEspecie("eq." + idEspecie).enqueue(cb);
    }

    // ════════════════════════════════════════════════════════
    // HISTORIAL MÉDICO
    // ════════════════════════════════════════════════════════

    public void obtenerVacunasMascota(int idMascota, Callback<List<VacunaMascotaResponse>> cb) {
        api.obtenerVacunasDeMascota("eq." + idMascota).enqueue(cb);
    }

    public void obtenerIntervencionesMascota(int idMascota, Callback<List<IntervencionResponse>> cb) {
        api.obtenerIntervencionesDeMascota("eq." + idMascota).enqueue(cb);
    }

    // ══════════════════════════════════════════════════════════════
    // AGREGAR MASCOTA
    // ══════════════════════════════════════════════════════════════

    /** Inserta mascota y devuelve el objeto creado con su id generado */
    public void crearMascotaConRetorno(MascotaRequest req, Callback<List<MascotaResponse>> cb) {
        api.crearMascotaConRetorno("return=representation", req).enqueue(cb);
    }

    /** Sube una imagen al bucket de Storage y llama al callback */
    public void subirImagen(String bucket, String path, byte[] bytes, Callback<Void> cb) {
        RequestBody body = RequestBody.create(MediaType.parse("image/jpeg"), bytes);
        api.subirImagen("image/jpeg", bucket, path, body).enqueue(cb);
    }

    /** Registra una foto en la tabla fotomascota */
    public void agregarFotoMascota(FotoMascotaRequest req, Callback<Void> cb) {
        api.agregarFotoMascota(req).enqueue(cb);
    }

    /** Registra una vacuna aplicada a la mascota */
    public void agregarVacunaMascota(VacunaMascotaRequest req, Callback<Void> cb) {
        api.agregarVacunaMascota(req).enqueue(cb);
    }

    /** Registra una intervención médica */
    public void agregarIntervencion(IntervencionRequest req, Callback<Void> cb) {
        api.agregarIntervencion(req).enqueue(cb);
    }
}