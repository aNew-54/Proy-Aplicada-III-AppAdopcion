package unc.edu.pe.appadopcion.data.repository;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import unc.edu.pe.appadopcion.data.api.SupabaseApi;
import unc.edu.pe.appadopcion.data.api.SupabaseClient;
import unc.edu.pe.appadopcion.data.model.*;

public class AuthRepository {
    private SupabaseApi api;

    public AuthRepository() {
        api = SupabaseClient.getClient().create(SupabaseApi.class);
    }

    // Método para crear las credenciales (SignUp)
    public void registrarCredenciales(AuthRequest req, Callback<AuthResponse> callback) {
        api.registrarCredenciales(req).enqueue(callback);
    }

    // Método para crear el registro en la tabla 'usuario'
    public void crearUsuarioBase(UsuarioRequest req, Callback<Void> callback) {
        api.crearUsuarioBase(req).enqueue(callback);
    }

    // Método para crear el perfil del adoptante
    public void crearPerfilAdoptante(AdoptanteRequest req, Callback<Void> callback) {
        api.crearPerfilAdoptante(req).enqueue(callback);
    }

    // Método para crear el perfil del refugio
    public void crearPerfilRefugio(RefugioRequest req, Callback<Void> callback) {
        api.crearPerfilRefugio(req).enqueue(callback);
    }

    public void login(AuthRequest req, Callback<AuthResponse> callback) {
        api.loginUsuario(req).enqueue(callback);
    }

    public void obtenerUsuario(String uuid, Callback<List<UsuarioRequest>> callback) {
        // Supabase usa sintaxis "eq.valor" para filtrar
        api.obtenerUsuarioPorId("eq." + uuid).enqueue(callback);
    }
}