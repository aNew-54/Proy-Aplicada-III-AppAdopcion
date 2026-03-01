package unc.edu.pe.appadopcion.ui.favoritos;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import unc.edu.pe.appadopcion.R;
import unc.edu.pe.appadopcion.data.local.SessionManager;
import unc.edu.pe.appadopcion.data.repository.AppRepository;
import unc.edu.pe.appadopcion.databinding.FragmentFavoritosBinding;
import unc.edu.pe.appadopcion.vm.favoritos.FavoritosViewModel;

public class FavoritosFragment extends Fragment {

    private FragmentFavoritosBinding binding;
    private FavoritosViewModel viewModel;
    private FavoritosAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentFavoritosBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Inicializar sesión y repositorio (Requisito de la documentación)
        SessionManager session = new SessionManager(requireContext());
        AppRepository repo = new AppRepository(session.getToken());

        // 2. Configurar el Adaptador y el clic hacia el Detalle de Mascota
        adapter = new FavoritosAdapter(idMascota -> {

            // TODO: Falta activar la navegación a DetalleMascota
            /* Cuando se suba el gráfico de navegación, se debe descomentar esto y colocar el ID correcto:
            Bundle bundle = new Bundle();
            bundle.putInt("idMascota", idMascota);
            androidx.navigation.Navigation.findNavController(view).navigate(R.id.tu_flecha_de_navegacion, bundle);
            */

            // Prueba
            Toast.makeText(requireContext(), "¡Clic exitoso! ID de la mascota: " + idMascota, Toast.LENGTH_SHORT).show();
        });

        binding.rvFavoritos.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvFavoritos.setAdapter(adapter);

        // 3. Inicializar ViewModel
        viewModel = new ViewModelProvider(this).get(FavoritosViewModel.class);

        // 4. Observar los datos (Aquí aplicamos la regla OBLIGATORIA del Null Check)
        observarViewModel();

        // 5. Disparar la descarga de datos pasando el UUID
        viewModel.cargarMisFavoritos(repo, session.getUuid());
    }

    private void observarViewModel() {
        // Observar estado de carga (ProgressBar)
        viewModel.getCargando().observe(getViewLifecycleOwner(), isLoading -> {
            if (binding == null || !isAdded()) return; // REGLA DEL NULL CHECK
            binding.progressBarFavoritos.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });

        // Observar la lista de mascotas
        viewModel.getMascotasFavoritas().observe(getViewLifecycleOwner(), mascotas -> {
            if (binding == null || !isAdded()) return; // REGLA DEL NULL CHECK

            if (mascotas.isEmpty()) {
                // Si no hay favoritos, mostramos el mensaje y ocultamos la lista
                binding.tvSinFavoritos.setVisibility(View.VISIBLE);
                binding.rvFavoritos.setVisibility(View.GONE);
            } else {
                // Si hay favoritos, mostramos la lista y ocultamos el mensaje
                binding.tvSinFavoritos.setVisibility(View.GONE);
                binding.rvFavoritos.setVisibility(View.VISIBLE);
                adapter.setMascotas(mascotas);
            }
        });

        // Observar errores
        viewModel.getError().observe(getViewLifecycleOwner(), errorMsg -> {
            if (binding == null || !isAdded()) return; // REGLA DEL NULL CHECK
            if (errorMsg != null && !errorMsg.isEmpty()) {
                Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Evita fugas de memoria al destruir el Fragment
        binding = null;
    }
}