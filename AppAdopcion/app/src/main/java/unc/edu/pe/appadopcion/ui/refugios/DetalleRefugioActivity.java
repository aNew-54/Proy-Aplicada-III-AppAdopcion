package unc.edu.pe.appadopcion.ui.refugios;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import unc.edu.pe.appadopcion.BuildConfig;
import unc.edu.pe.appadopcion.R;
import unc.edu.pe.appadopcion.data.local.SessionManager;
import unc.edu.pe.appadopcion.data.model.MascotaResponse;
import unc.edu.pe.appadopcion.data.model.RefugioResponse;
import unc.edu.pe.appadopcion.data.repository.AppRepository;
import unc.edu.pe.appadopcion.databinding.ActivityDetalleRefugioBinding;

// IMPORTAMOS EL ADAPTADOR DEL FEED Y EL FRAGMENTO DE DETALLES
import unc.edu.pe.appadopcion.ui.feed.MascotaAdapter;
import unc.edu.pe.appadopcion.ui.mascotas.DetalleMascotaFragment;
import unc.edu.pe.appadopcion.vm.feed.DescubrirViewModel;
import unc.edu.pe.appadopcion.vm.feed.DescubrirViewModelFactory;
import unc.edu.pe.appadopcion.vm.refugios.DetalleRefugioViewModel;

public class DetalleRefugioActivity extends AppCompatActivity {

    public static final String EXTRA_ID_REFUGIO = "id_refugio";

    private ActivityDetalleRefugioBinding binding;
    private DetalleRefugioViewModel viewModel;
    private DescubrirViewModel viewModelDesc;
    private SessionManager session;
    private MascotaAdapter adapter; // Usamos el adaptador del Feed

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_detalle_refugio);

        setSupportActionBar(binding.toolbarDetalleRefugio);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("");
        }
        binding.toolbarDetalleRefugio.setNavigationOnClickListener(v -> finish());

        session = new SessionManager(this);
        viewModel = new ViewModelProvider(this).get(DetalleRefugioViewModel.class);
        // --- CORRECCIÓN: Usar el Factory para DescubrirViewModel ---
        AppRepository repo = new AppRepository(session.getToken());
        DescubrirViewModelFactory factoryDesc = new DescubrirViewModelFactory(repo);
        viewModelDesc = new ViewModelProvider(this, factoryDesc).get(DescubrirViewModel.class);

        int idRefugio = getIntent().getIntExtra(EXTRA_ID_REFUGIO, -1);
        if (idRefugio == -1) {
            Toast.makeText(this, "Error: refugio no encontrado", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        configurarRecyclerMascotas();
        observarViewModel();
        viewModel.cargarDetalle(idRefugio, session.getToken());
    }

    private void configurarRecyclerMascotas() {
        // Inicializamos el mismo adaptador que usas en el DescubrirFragment
        adapter = new MascotaAdapter(session.esAdoptante(), session.getToken(), new MascotaAdapter.OnMascotaClickListener() {
            @Override
            public void onMascotaClick(MascotaResponse mascota) {
                // 1. Preparamos el fragmento destino
                DetalleMascotaFragment detalleFragment = new DetalleMascotaFragment();

                // 2. Empaquetamos la mascota
                Bundle bundle = new Bundle();
                bundle.putSerializable("mascota", mascota);
                bundle.putBoolean("ocultar_menu", true);
                detalleFragment.setArguments(bundle);

                // 3. El TRUCO: Usamos android.R.id.content para superponer el fragmento sobre esta Activity
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(android.R.id.content, detalleFragment)
                        .addToBackStack(null)
                        .commit();
            }

            @Override
            public void onRefugioClick(int idRefugio) {
                // Ya estamos en el perfil del refugio, no hacemos nada para evitar un bucle
            }

            @Override
            public void onFavoritoClick(MascotaResponse mascota, int posicion, boolean fueAgregado) {
                int idAdoptante = session.getIdAdoptante();
                if (idAdoptante != -1) {
                    // Llamamos al repositorio directamente para guardar el favorito en la BD
                    AppRepository repo = new AppRepository(session.getToken());
                    if (fueAgregado) {
                        repo.agregarFavorito(mascota.idMascota, idAdoptante, new Callback<Void>() {
                            @Override public void onResponse(Call<Void> call, Response<Void> r) {}
                            @Override public void onFailure(Call<Void> call, Throwable t) {}
                        });
                        Toast.makeText(DetalleRefugioActivity.this, mascota.nombre + " en favoritos ❤️", Toast.LENGTH_SHORT).show();
                    } else {
                        repo.eliminarFavorito(mascota.idMascota, idAdoptante, new Callback<Void>() {
                            @Override public void onResponse(Call<Void> call, Response<Void> r) {}
                            @Override public void onFailure(Call<Void> call, Throwable t) {}
                        });
                        Toast.makeText(DetalleRefugioActivity.this, mascota.nombre + " quitado 💔", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        binding.rvDetalleRefugioMascotas.setLayoutManager(new LinearLayoutManager(this));
        binding.rvDetalleRefugioMascotas.setAdapter(adapter);

        // Desactivar scroll interno del RecyclerView porque ya está dentro de un NestedScrollView en tu XML
        binding.rvDetalleRefugioMascotas.setNestedScrollingEnabled(false);
    }

    private void observarViewModel() {
        viewModel.getIsLoading().observe(this, loading ->
                binding.progressDetalleRefugio.setVisibility(loading ? View.VISIBLE : View.GONE));

        viewModel.getErrorMessage().observe(this, msg -> {
            if (msg != null) Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        });

        viewModel.getRefugio().observe(this, this::mostrarDatosRefugio);

        viewModel.getMascotas().observe(this, mascotas -> {
            if (mascotas != null && !mascotas.isEmpty()) {
                binding.sectionMascotasRefugio.setVisibility(View.VISIBLE);
                // Le pasamos la lista de mascotas al adaptador que ya creamos
                adapter.setMascotas(mascotas);
            } else {
                binding.sectionMascotasRefugio.setVisibility(View.GONE);
            }
        });

        if (session.esAdoptante()){
            viewModelDesc.getMisFavoritosIds().observe(this, idsFavoritos -> {
                if (adapter != null && idsFavoritos != null) {
                    adapter.setFavoritosIniciales(idsFavoritos);
                }
            });
            viewModelDesc.cargarMisFavoritos(session.getIdAdoptante());
        }


    }

    private void mostrarDatosRefugio(RefugioResponse r) {
        if (r == null) return;
        binding.tvDetalleRefugioNombre.setText(r.nombre != null ? r.nombre : "Sin nombre");
        // Actualicé este campo para que coincida con tus IDs ocultos del último XML
        if (binding.tvDetalleRefugioMascotasCount != null) {
            binding.tvDetalleRefugioMascotasCount.setText(r.mascotasDisponibles + " mascotas disponibles");
        }
        binding.tvDetalleRefugioDescripcion.setText(
                r.descripcion != null && !r.descripcion.isEmpty()
                        ? r.descripcion : "Este refugio aun no tiene descripcion.");

        if (r.telefono != null && !r.telefono.isEmpty()) {
            binding.tvDetalleRefugioTelefono.setText(r.telefono);
            // Hacer que toda la fila sea clickeable
            ((View) binding.tvDetalleRefugioTelefono.getParent()).setOnClickListener(v -> abrirWhatsApp(r.telefono));
        } else {
            binding.tvDetalleRefugioTelefono.setText("No disponible");
        }

        binding.tvDetalleRefugioCorreo.setText(
                r.correo != null && !r.correo.isEmpty() ? r.correo : "No disponible");

        if (r.latitud != null && r.longitud != null) {
            if (binding.chipDetalleRefugioUbicacion != null) {
                binding.chipDetalleRefugioUbicacion.setVisibility(View.VISIBLE);
                binding.chipDetalleRefugioUbicacion.setText(r.direccion != null ? r.direccion : "Ver en mapa");
                binding.chipDetalleRefugioUbicacion.setOnClickListener(v ->
                        abrirEnMapa(r.latitud, r.longitud, r.nombre));
            }
        }

        cargarImagen(r.urlPortada, binding.imgDetalleRefugioPortada);

        String fotoP = (r.fotoPerfil != null && !r.fotoPerfil.isEmpty()) ? r.fotoPerfil : r.urlPortada;
        if (binding.imgDetalleRefugioFotoPerfil != null) {
            cargarImagen(fotoP, binding.imgDetalleRefugioFotoPerfil);
        }
    }

    private void cargarImagen(String campo, android.widget.ImageView iv) {
        if (campo == null || campo.isEmpty()) {
            iv.setImageResource(R.drawable.ic_launcher_background);
            return;
        }
        String url = campo.startsWith("http")
                ? campo
                : BuildConfig.SUPABASE_URL + "/storage/v1/object/public/" + campo;
        Glide.with(this).load(url).centerCrop()
                .placeholder(R.drawable.ic_launcher_background).into(iv);
    }

    private void abrirWhatsApp(String telefono) {
        String numero = telefono.replaceAll("[^0-9]", "");
        try {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://wa.me/51" + numero)));
        } catch (Exception e) {
            Toast.makeText(this, "No se pudo abrir WhatsApp", Toast.LENGTH_SHORT).show();
        }
    }

    private void abrirEnMapa(double lat, double lng, String nombre) {
        Uri uri = Uri.parse("geo:" + lat + "," + lng + "?q=" + lat + "," + lng
                + "(" + Uri.encode(nombre) + ")");
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        intent.setPackage("com.google.android.apps.maps");
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        } else {
            startActivity(new Intent(Intent.ACTION_VIEW, uri));
        }
    }
}