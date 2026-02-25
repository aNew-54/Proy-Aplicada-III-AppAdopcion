package unc.edu.pe.appadopcion.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import unc.edu.pe.appadopcion.data.api.SupabaseApi;
import unc.edu.pe.appadopcion.data.api.SupabaseClient;
import unc.edu.pe.appadopcion.data.local.SessionManager;
import unc.edu.pe.appadopcion.data.model.AuthRequest;
import unc.edu.pe.appadopcion.data.model.AuthResponse;
import unc.edu.pe.appadopcion.data.model.UsuarioRequest;
import unc.edu.pe.appadopcion.databinding.ActivityLoginBinding;
import unc.edu.pe.appadopcion.ui.main.MainActivity;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnLogin.setOnClickListener(v -> validarYLogin());
        binding.tvIrRegistro.setOnClickListener(v -> finish()); // vuelve al Welcome
    }

    private void validarYLogin() {
        String email    = binding.etEmail.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();

        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.setError("Correo inválido");
            return;
        } else binding.tilEmail.setError(null);

        if (password.isEmpty()) {
            binding.tilPassword.setError("Ingresa tu contraseña");
            return;
        } else binding.tilPassword.setError(null);

        binding.btnLogin.setEnabled(false);
        binding.btnLogin.setText("Ingresando...");

        SupabaseApi apiAnon = SupabaseClient.getClient().create(SupabaseApi.class);

        // PASO 1: autenticar con GoTrue
        apiAnon.loginUsuario(new AuthRequest(email, password))
                .enqueue(new Callback<AuthResponse>() {
                    @Override
                    public void onResponse(Call<AuthResponse> call, Response<AuthResponse> resp) {
                        if (resp.isSuccessful() && resp.body() != null) {
                            String uuid  = resp.body().getUser().getId();
                            String token = resp.body().getAccessToken();

                            // PASO 2: obtener rol del usuario
                            SupabaseApi apiAuth = SupabaseClient.getClient(token).create(SupabaseApi.class);
                            apiAuth.obtenerUsuarioPorId("eq." + uuid)
                                    .enqueue(new Callback<List<UsuarioRequest>>() {
                                        @Override
                                        public void onResponse(Call<List<UsuarioRequest>> call,
                                                               Response<List<UsuarioRequest>> r) {
                                            if (r.isSuccessful() && r.body() != null && !r.body().isEmpty()) {
                                                UsuarioRequest usuario = r.body().get(0);
                                                // Guardar sesión
                                                new SessionManager(LoginActivity.this)
                                                        .guardarSesion(uuid, token, usuario.getRol());
                                                irAlMain();
                                            } else {
                                                Log.e("LOGIN", "Usuario no encontrado en BD: " + r.code());
                                                Toast.makeText(LoginActivity.this,
                                                        "Error: cuenta incompleta", Toast.LENGTH_LONG).show();
                                                resetBoton();
                                            }
                                        }
                                        @Override
                                        public void onFailure(Call<List<UsuarioRequest>> call, Throwable t) {
                                            Log.e("LOGIN", "Fallo perfil: " + t.getMessage(), t);
                                            resetBoton();
                                        }
                                    });
                        } else {
                            Toast.makeText(LoginActivity.this,
                                    "Correo o contraseña incorrectos", Toast.LENGTH_LONG).show();
                            resetBoton();
                        }
                    }
                    @Override
                    public void onFailure(Call<AuthResponse> call, Throwable t) {
                        Log.e("LOGIN", "Fallo red: " + t.getMessage(), t);
                        Toast.makeText(LoginActivity.this,
                                "Error de red: " + t.getMessage(), Toast.LENGTH_LONG).show();
                        resetBoton();
                    }
                });
    }

    private void irAlMain() {
        startActivity(new Intent(this, MainActivity.class)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
    }

    private void resetBoton() {
        binding.btnLogin.setEnabled(true);
        binding.btnLogin.setText("Iniciar Sesión");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}