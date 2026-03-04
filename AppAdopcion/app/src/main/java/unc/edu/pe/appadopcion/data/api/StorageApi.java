package unc.edu.pe.appadopcion.data.api;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface StorageApi {

    /**
     * Sube un archivo al bucket especificado.
     * La ruta del archivo dentro del bucket es: {uuid}/{nombreArchivo}
     * Ejemplo: "avatars/377845a3-.../profile.jpg"
     */
    @POST("object/{bucket}/{uuid}/{fileName}")
    Call<Void> uploadFile(
            @Path("bucket")   String bucket,
            @Path("uuid")     String uuid,
            @Path("fileName") String fileName,
            @Header("Content-Type") String contentType,
            @Body RequestBody fileBody
    );

    /**
     * Sube o SOBREESCRIBE un archivo existente.
     * Usar en edición de perfil cuando ya puede haber una foto previa.
     * x-upsert: true → Supabase sobreescribe sin error 409.
     */
    @Headers("x-upsert: true")
    @POST("object/{bucket}/{folder}/{filename}")
    Call<Void> upsertFile(
            @Path("bucket")   String bucket,
            @Path("folder")   String folder,
            @Path("filename") String filename,
            @Header("Content-Type") String contentType,
            @Body okhttp3.RequestBody body
    );
}