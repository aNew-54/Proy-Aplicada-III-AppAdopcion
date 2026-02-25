package unc.edu.pe.appadopcion.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ImageHelper {

    private static final int MAX_DIMENSION = 800; // px máximo para ancho o alto
    private static final int QUALITY       = 80;  // calidad JPEG (0–100)

    /**
     * Lee un Uri, comprime la imagen y devuelve los bytes listos para subir.
     * Retorna null si ocurre algún error.
     */
    public static byte[] uriToBytes(Context context, Uri uri) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            if (inputStream == null) return null;

            Bitmap original = BitmapFactory.decodeStream(inputStream);
            inputStream.close();

            if (original == null) return null;

            // Redimensionar si es demasiado grande
            Bitmap resized = resizeBitmap(original);

            // Comprimir a JPEG
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            resized.compress(Bitmap.CompressFormat.JPEG, QUALITY, baos);

            if (resized != original) resized.recycle();
            original.recycle();

            return baos.toByteArray();

        } catch (IOException e) {
            Log.e("ImageHelper", "Error al procesar imagen: " + e.getMessage(), e);
            return null;
        }
    }

    private static Bitmap resizeBitmap(Bitmap bitmap) {
        int width  = bitmap.getWidth();
        int height = bitmap.getHeight();

        if (width <= MAX_DIMENSION && height <= MAX_DIMENSION) return bitmap;

        float ratio;
        if (width > height) {
            ratio = (float) MAX_DIMENSION / width;
        } else {
            ratio = (float) MAX_DIMENSION / height;
        }

        int newWidth  = Math.round(width * ratio);
        int newHeight = Math.round(height * ratio);

        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
    }

    /**
     * Construye la URL pública de un archivo subido a un bucket público.
     * Para buckets privados, usa una URL firmada (no implementado aquí).
     */
    public static String buildPublicUrl(String supabaseUrl, String bucket, String uuid, String fileName) {
        return supabaseUrl + "/storage/v1/object/public/" + bucket + "/" + uuid + "/" + fileName;
    }
}