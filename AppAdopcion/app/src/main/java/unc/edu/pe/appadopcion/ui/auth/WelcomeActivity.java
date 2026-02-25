package unc.edu.pe.appadopcion.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomsheet.BottomSheetDialog;

import unc.edu.pe.appadopcion.R;
import unc.edu.pe.appadopcion.data.local.SessionManager;
import unc.edu.pe.appadopcion.databinding.ActivityWelcomeBinding;
import unc.edu.pe.appadopcion.ui.main.MainActivity;

public class WelcomeActivity extends AppCompatActivity {

    private ActivityWelcomeBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityWelcomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Si ya hay sesión activa, saltar directo al main
        SessionManager session = new SessionManager(this);
        if (session.isLoggedIn()) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        binding.btnIrLogin.setOnClickListener(v ->
                startActivity(new Intent(this, LoginActivity.class)));

        binding.btnIrRegistro.setOnClickListener(v -> mostrarModalRol());
    }

    private void mostrarModalRol() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View sheetView = LayoutInflater.from(this).inflate(R.layout.bottom_sheet_seleccion_rol, null);
        dialog.setContentView(sheetView);

        sheetView.findViewById(R.id.btnElegirAdoptante).setOnClickListener(v -> {
            dialog.dismiss();
            startActivity(new Intent(this, RegistroAdoptanteActivity.class));
        });

        sheetView.findViewById(R.id.btnElegirRefugio).setOnClickListener(v -> {
            dialog.dismiss();
            startActivity(new Intent(this, RegistroRefugioActivity.class));
        });

        dialog.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}