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

// Importamos el ViewModel desde tu paquete 'vm'
import unc.edu.pe.appadopcion.vm.auth.LoginViewModel;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private LoginViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 1. Instanciamos el ViewModel asociado a esta Activity
        viewModel = new ViewModelProvider(this).get(LoginViewModel.class);

        // 2. Configuramos cómo reacciona la UI a los datos del ViewModel
        configurarObservadores();

        // 3. Asignamos los eventos de los botones
        binding.btnLogin.setOnClickListener(v -> validarYLogin());
        binding.tvIrRegistro.setOnClickListener(v -> finish()); // vuelve al Welcome
    }

    private void configurarObservadores() {
        // Observar el estado de carga para bloquear/desbloquear el botón
        viewModel.getIsLoading().observe(this, isLoading -> {
            if (isLoading) {
                binding.btnLogin.setEnabled(false);
                binding.btnLogin.setText("Ingresando...");
            } else {
                binding.btnLogin.setEnabled(true);
                binding.btnLogin.setText("Iniciar Sesión");
            }
        });

        // Observar si ocurre algún error de red o de credenciales
        viewModel.getErrorMessage().observe(this, errorMsg -> {
            if (errorMsg != null) {
                Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show();
            }
        });

        // Observar cuando el flujo de login (Auth + Rol) termina con éxito
        viewModel.getLoginSuccess().observe(this, authResponse -> {
            if (authResponse != null) {
                int idRefugio = viewModel.getIdRefugio().getValue() != null
                        ? viewModel.getIdRefugio().getValue() : -1;
                int idAdoptante = viewModel.getIdAdoptante().getValue() != null
                        ? viewModel.getIdAdoptante().getValue() : -1;

                new SessionManager(this).guardarSesion(
                        authResponse.getUser().getId(),
                        authResponse.getAccessToken(),
                        authResponse.getRol(),
                        idRefugio,
                        idAdoptante// ← ahora guarda el id real del refugio
                );
                irAlMain();
            }
        });
    }

    private void validarYLogin() {
        String email = binding.etEmail.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();

        // Las validaciones de UI se quedan aquí, es su responsabilidad
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

        // Le pasamos la responsabilidad de la red al ViewModel
        viewModel.iniciarSesion(email, password);
    }

    private void irAlMain() {
        startActivity(new Intent(this, MainActivity.class)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null; // Evitamos fugas de memoria
    }
}