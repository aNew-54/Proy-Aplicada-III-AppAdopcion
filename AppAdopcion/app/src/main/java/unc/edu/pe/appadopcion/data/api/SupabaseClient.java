package unc.edu.pe.appadopcion.data.api;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import unc.edu.pe.appadopcion.BuildConfig;
import unc.edu.pe.appadopcion.data.local.SessionManager;
import unc.edu.pe.appadopcion.ui.auth.WelcomeActivity;

public class SupabaseClient {
    private static final String BASE_URL = BuildConfig.SUPABASE_URL + "/rest/v1/";
    private static Retrofit retrofit = null;

    private static Context appContext;

    public static void init(Context context) {
        if (appContext == null) {
            appContext = context.getApplicationContext();
        }
    }

    public static Retrofit getClient() {
        return getClient(null);
    }

    public static Retrofit getClient(String userToken) {
        String token = (userToken != null) ? userToken : BuildConfig.SUPABASE_KEY;

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(chain -> {
                    Request request = chain.request().newBuilder()
                            .addHeader("apikey", BuildConfig.SUPABASE_KEY)
                            .addHeader("Authorization", "Bearer " + token)
                            .addHeader("Content-Type", "application/json")
                            .addHeader("Prefer", "return=minimal")
                            .build();

                    Response response = chain.proceed(request);

                    // --- EL GUARDIÁN MEJORADO ---
                    if (response.code() == 401 && appContext != null) {

                        SessionManager session = new SessionManager(appContext);
                        session.cerrarSesion();

                        // Mostrar Toast en el hilo principal
                        new Handler(Looper.getMainLooper()).post(() ->
                                Toast.makeText(appContext, "Tu sesión ha expirado. Vuelve a iniciar sesión.", Toast.LENGTH_LONG).show()
                        );

                        // Enviar al usuario directamente al WelcomeActivity
                        Intent intent = new Intent(appContext, WelcomeActivity.class);
                        // Estas flags borran todo el historial de pantallas hacia atrás
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        appContext.startActivity(intent);
                    }

                    return response;
                }).build();

        return new Retrofit.Builder()
                .baseUrl(BuildConfig.SUPABASE_URL + "/")
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }
}