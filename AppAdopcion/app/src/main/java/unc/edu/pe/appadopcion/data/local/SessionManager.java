package unc.edu.pe.appadopcion.data.local;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Gestiona la sesión del usuario en SharedPreferences.
 *
 * USO:
 *   SessionManager session = new SessionManager(context);
 *   session.guardarSesion(uuid, token, rol);
 *   session.isLoggedIn();
 *   session.getToken();
 *   session.getRol();
 *   session.getUuid();
 *   session.cerrarSesion();
 */
public class SessionManager {

    private static final String PREF_NAME = "AppAdopcionSession";
    private static final String KEY_UUID = "uuid";
    private static final String KEY_TOKEN = "token";
    private static final String KEY_ROL = "rol";
    private static final String KEY_LOGGED_IN = "isLoggedIn";

    private final SharedPreferences prefs;

    public SessionManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void guardarSesion(String uuid, String token, String rol) {
        prefs.edit()
                .putBoolean(KEY_LOGGED_IN, true)
                .putString(KEY_UUID, uuid)
                .putString(KEY_TOKEN, token)
                .putString(KEY_ROL, rol)
                .apply();
    }

    public boolean isLoggedIn()  { return prefs.getBoolean(KEY_LOGGED_IN, false); }
    public String  getUuid()     { return prefs.getString(KEY_UUID, null); }
    public String  getToken()    { return prefs.getString(KEY_TOKEN, null); }
    public String  getRol()      { return prefs.getString(KEY_ROL, null); }
    public boolean esAdoptante() { return "Adoptante".equals(getRol()); }
    public boolean esRefugio()   { return "Refugio".equals(getRol()); }

    public void cerrarSesion() {
        prefs.edit().clear().apply();
    }
}