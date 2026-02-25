package unc.edu.pe.appadopcion.ui.perfil;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import unc.edu.pe.appadopcion.data.local.SessionManager;

/**
 * Fragment "Yo" del bottom nav.
 * Decide qué subFragment mostrar según el rol del usuario.
 */
public class PerfilFragment extends Fragment {

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,@Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Necesitamos un contenedor vacío para meter el subfragmento
        return inflater.inflate(unc.edu.pe.appadopcion.R.layout.fragment_container_simple, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SessionManager session = new SessionManager(requireContext());

        Fragment subFragment = session.esRefugio()
                ? new RefugioPerfilFragment()
                : new AdoptantePerfilFragment();

        getChildFragmentManager()
                .beginTransaction()
                .replace(unc.edu.pe.appadopcion.R.id.subFragmentContainer, subFragment)
                .commit();
    }
}