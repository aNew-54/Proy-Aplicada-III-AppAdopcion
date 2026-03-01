package unc.edu.pe.appadopcion.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import unc.edu.pe.appadopcion.data.local.SessionManager;
import unc.edu.pe.appadopcion.databinding.ActivityLoginBinding;
import unc.edu.pe.appadopcion.ui.main.MainActivity;
import unc.edu.pe.appadopcion.vm.auth.LoginViewModel;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private LoginViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(LoginViewModel.class);

        configurarObservadores();

        binding.btnLogin.setOnClickListener(v -> validarYLogin());
        binding.tvIrRegistro.setOnClickListener(v -> finish());
    }

    private void configurarObservadores() {
        viewModel.getIsLoading().observe(this, isLoading -> {
            if (isLoading) {
                binding.btnLogin.setEnabled(false);
                binding.btnLogin.setText("Ingresando...");
            } else {
                binding.btnLogin.setEnabled(true);
                binding.btnLogin.setText("Iniciar Sesión");
            }
        });

        viewModel.getErrorMessage().observe(this, errorMsg -> {
            if (errorMsg != null) {
                Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show();
            }
        });

        // Observar cuando el flujo de login termina con éxito
        viewModel.getLoginSuccess().observe(this, loginResult -> {
            if (loginResult != null) {
                // Pasamos los 5 datos de una vez al SessionManager
                new SessionManager(this).guardarSesion(
                        loginResult.uuid,
                        loginResult.token,
                        loginResult.rol,
                        loginResult.idRefugio,
                        loginResult.idAdoptante
                );
                irAlMain();
            }
        });
    }

    private void validarYLogin() {
        String email = binding.etEmail.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();

        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.setError("Correo inválido");
            return;
        } else {
            binding.tilEmail.setError(null);
        }

        if (password.isEmpty()) {
            binding.tilPassword.setError("Ingresa tu contraseña");
            return;
        } else {
            binding.tilPassword.setError(null);
        }

        viewModel.iniciarSesion(email, password);
    }

    private void irAlMain() {
        startActivity(new Intent(this, MainActivity.class)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}