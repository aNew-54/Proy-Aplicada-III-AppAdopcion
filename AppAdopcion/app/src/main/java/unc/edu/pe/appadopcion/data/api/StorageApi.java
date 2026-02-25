package unc.edu.pe.appadopcion.data.api;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
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
}