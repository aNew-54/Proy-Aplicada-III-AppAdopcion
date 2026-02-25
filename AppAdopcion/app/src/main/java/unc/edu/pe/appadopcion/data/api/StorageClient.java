package unc.edu.pe.appadopcion.data.api;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import unc.edu.pe.appadopcion.BuildConfig;

public class StorageClient {

    /**
     * Retorna un cliente Retrofit apuntando a /storage/v1/
     * usando el JWT del usuario autenticado.
     */
    public static StorageApi getApi(String userToken) {
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(chain -> {
                    Request request = chain.request().newBuilder()
                            .addHeader("apikey", BuildConfig.SUPABASE_KEY)
                            .addHeader("Authorization", "Bearer " + userToken)
                            // No ponemos Content-Type aquí — lo controlamos por llamada
                            .build();
                    return chain.proceed(request);
                })
                .build();

        return new Retrofit.Builder()
                .baseUrl(BuildConfig.SUPABASE_URL + "/storage/v1/")
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(StorageApi.class);
    }
}