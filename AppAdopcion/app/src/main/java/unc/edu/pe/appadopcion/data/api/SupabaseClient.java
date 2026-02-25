package unc.edu.pe.appadopcion.data.api; // Asegúrate de que esta sea la ruta correcta de tu carpeta

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import java.io.IOException;

import unc.edu.pe.appadopcion.BuildConfig; // <-- IMPORTANTE: Faltaba importar tu BuildConfig

public class SupabaseClient {
    private static final String BASE_URL = BuildConfig.SUPABASE_URL + "/rest/v1/";
    private static Retrofit retrofit = null;

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
                            .addHeader("Prefer", "return=minimal") // Evita errores 406
                            .build();
                    return chain.proceed(request);
                }).build();

        return new Retrofit.Builder()
                .baseUrl(BuildConfig.SUPABASE_URL + "/")
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }
}