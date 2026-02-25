package unc.edu.pe.appadopcion.utils;

import android.content.Context;
import android.widget.ImageView;

import androidx.annotation.DrawableRes;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Path;

import unc.edu.pe.appadopcion.BuildConfig;

/**
 * Carga imágenes desde Supabase Storage.
 *
 * Uso:
 *   // Bucket PRIVADO (avatars) → genera signed URL automáticamente:
 *   ImageLoader.cargarAvatarCircular(context, token, uuid, imageView, R.drawable.ic_person);
 *
 *   // Bucket PÚBLICO (refugio-covers, mascotas) → URL directa:
 *   ImageLoader.cargarPublica(context, urlPublica, imageView, R.drawable.placeholder);
 */
public class ImageLoader {

    // Modelo interno para la request de signed URL
    static class SignedUrlRequest {
        public int expiresIn;
        SignedUrlRequest(int expiresIn) { this.expiresIn = expiresIn; }
    }

    // Modelo interno para la response
    static class SignedUrlResponse {
        public String signedURL; // La API devuelve "signedURL" (mayúsculas)
    }

    // Interfaz Retrofit solo para este endpoint
    interface StorageSignApi {
        @POST("object/sign/{bucket}/{uuid}/{fileName}")
        Call<SignedUrlResponse> sign(
                @Path("bucket")   String bucket,
                @Path("uuid")     String uuid,
                @Path("fileName") String fileName,
                @Body SignedUrlRequest body
        );
    }

    /**
     * Carga imagen desde bucket PÚBLICO directamente con Glide.
     */
    public static void cargarPublica(Context ctx, String url,
                                     ImageView view, @DrawableRes int placeholder) {
        if (url == null || url.isEmpty()) {
            view.setImageResource(placeholder);
            return;
        }
        Glide.with(ctx).load(url)
                .placeholder(placeholder).error(placeholder)
                .into(view);
    }

    /**
     * Carga avatar desde bucket PRIVADO 'avatars' de forma circular.
     * Genera signed URL válida por 1 hora.
     *
     * @param token  JWT del usuario autenticado
     * @param uuid   UUID del usuario (= nombre de carpeta en el bucket)
     */
    public static void cargarAvatarCircular(Context ctx, String token, String uuid,
                                            ImageView view, @DrawableRes int placeholder) {
        cargarDesdeAvatars(ctx, token, uuid, view, placeholder, true);
    }

    public static void cargarAvatar(Context ctx, String token, String uuid,
                                    ImageView view, @DrawableRes int placeholder) {
        cargarDesdeAvatars(ctx, token, uuid, view, placeholder, false);
    }

    private static void cargarDesdeAvatars(Context ctx, String token, String uuid,
                                           ImageView view, @DrawableRes int placeholder,
                                           boolean circular) {
        if (uuid == null || uuid.isEmpty() || token == null) {
            view.setImageResource(placeholder);
            return;
        }

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(chain -> {
                    Request req = chain.request().newBuilder()
                            .addHeader("apikey",        BuildConfig.SUPABASE_KEY)
                            .addHeader("Authorization", "Bearer " + token)
                            .build();
                    return chain.proceed(req);
                }).build();

        StorageSignApi api = new Retrofit.Builder()
                .baseUrl(BuildConfig.SUPABASE_URL + "/storage/v1/")
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(StorageSignApi.class);

        api.sign("avatars", uuid, "profile.jpg", new SignedUrlRequest(3600))
                .enqueue(new Callback<SignedUrlResponse>() {
                    @Override
                    public void onResponse(Call<SignedUrlResponse> call,
                                           Response<SignedUrlResponse> resp) {
                        if (resp.isSuccessful() && resp.body() != null
                                && resp.body().signedURL != null) {

                            // La signed URL ya incluye la ruta completa a partir de /storage/v1/
                            String fullUrl = BuildConfig.SUPABASE_URL
                                    + "/storage/v1" + resp.body().signedURL;

                            // Verificar que el contexto siga activo antes de cargar con Glide
                            if (ctx instanceof android.app.Activity) {
                                android.app.Activity a = (android.app.Activity) ctx;
                                if (a.isDestroyed() || a.isFinishing()) return;
                            }

                            Glide.with(ctx).load(fullUrl)
                                    .placeholder(placeholder).error(placeholder)
                                    .optionalTransform(circular ? new CircleCrop() : null)
                                    .into(view);
                        } else {
                            view.setImageResource(placeholder);
                        }
                    }

                    @Override
                    public void onFailure(Call<SignedUrlResponse> call, Throwable t) {
                        view.setImageResource(placeholder);
                    }
                });
    }
}