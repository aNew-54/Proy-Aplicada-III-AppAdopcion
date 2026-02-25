package unc.edu.pe.appadopcion.ui.main;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import unc.edu.pe.appadopcion.R;
import unc.edu.pe.appadopcion.data.local.SessionManager;
import unc.edu.pe.appadopcion.databinding.ActivityMainBinding;
import unc.edu.pe.appadopcion.ui.auth.WelcomeActivity;
import unc.edu.pe.appadopcion.ui.descubrir.DescubrirFragment;
import unc.edu.pe.appadopcion.ui.mapa.RefugiosFragment;
import unc.edu.pe.appadopcion.ui.perfil.PerfilFragment;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        session = new SessionManager(this);

        // Pantalla inicial: siempre Descubrir
        if (savedInstanceState == null) {
            cargarFragment(new DescubrirFragment());
        }

        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_descubrir) {
                cargarFragment(new DescubrirFragment());
                return true;
            }
            if (id == R.id.nav_refugios) {
                cargarFragment(new RefugiosFragment());
                return true;
            }
            if (id == R.id.nav_perfil) {
                cargarFragment(new PerfilFragment());
                return true;
            }
            return false;
        });
    }

    private void cargarFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();
    }

    /** Llamado desde PerfilFragment cuando el usuario cierra sesión */
    public void cerrarSesion() {
        session.cerrarSesion();
        startActivity(new Intent(this, WelcomeActivity.class)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}