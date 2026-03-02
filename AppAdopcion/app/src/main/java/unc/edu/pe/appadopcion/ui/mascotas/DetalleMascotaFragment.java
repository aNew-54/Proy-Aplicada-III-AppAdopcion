package unc.edu.pe.appadopcion.ui.mascotas;

import static android.app.Activity.RESULT_OK;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.button.MaterialButton;

import unc.edu.pe.appadopcion.R;
import unc.edu.pe.appadopcion.data.local.SessionManager;
import unc.edu.pe.appadopcion.data.model.MascotaResponse;
import unc.edu.pe.appadopcion.data.repository.AppRepository;
import unc.edu.pe.appadopcion.databinding.FragmentDetalleMascotaBinding;
import unc.edu.pe.appadopcion.ui.mascotas.adapters.GaleriaFotosAdapter;
import unc.edu.pe.appadopcion.ui.mascotas.adapters.IntervencionesAdapter;
import unc.edu.pe.appadopcion.ui.solicitudes.CrearSolicitudBottomSheet;
import unc.edu.pe.appadopcion.utils.ImageLoader;
import unc.edu.pe.appadopcion.vm.mascotas.DetalleMascotaViewModel;

public class DetalleMascotaFragment extends Fragment {

    private FragmentDetalleMascotaBinding binding;
    private SessionManager session;
    private DetalleMascotaViewModel viewModel;
    private MascotaResponse mascotaActual;
    private boolean debeOcultarMenu = false;

    private ActivityResultLauncher<Intent> editMascotaLauncher;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Registrar el launcher para recibir el resultado de la edición
        editMascotaLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        // Si se editó con éxito, refrescamos los datos desde el servidor
                        if (mascotaActual != null) {
                            viewModel.refrescarDatos(mascotaActual.idMascota, session.getIdAdoptante());
                        }
                    }
                }
        );
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentDetalleMascotaBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        session = new SessionManager(requireContext());

        AppRepository repo = new AppRepository(session.getToken());
        viewModel = new ViewModelProvider(this, new ViewModelProvider.Factory() {
            @NonNull
            @Override
            public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
                return (T) new DetalleMascotaViewModel(repo);
            }
        }).get(DetalleMascotaViewModel.class);

        binding.toolbar.setNavigationOnClickListener(v -> requireActivity().onBackPressed());

        if (getArguments() != null) {
            mascotaActual = (MascotaResponse) getArguments().getSerializable("mascota");
            debeOcultarMenu = getArguments().getBoolean("ocultar_menu", false);
        }

        configurarLogicaVistas();
        observarViewModel();

        if (mascotaActual != null) {
            viewModel.cargarDatosMascota(mascotaActual, session.getIdAdoptante());
        } else {
            Toast.makeText(requireContext(), "Error al cargar la mascota", Toast.LENGTH_SHORT).show();
            requireActivity().onBackPressed();
        }
    }

    private void observarViewModel() {
        viewModel.getMascota().observe(getViewLifecycleOwner(), m -> {
            mascotaActual = m; // Actualizar la referencia local
            poblarUI(m);
        });

        viewModel.getEsFavorito().observe(getViewLifecycleOwner(), esFav -> {
            if (esFav) {
                binding.btnFavorito.setImageResource(R.drawable.ic_favorite);
                binding.btnFavorito.setColorFilter(Color.parseColor("#E91E63"));
            } else {
                binding.btnFavorito.setImageResource(R.drawable.ic_heart_outline);
                binding.btnFavorito.setColorFilter(Color.WHITE);
            }
        });

        viewModel.getFotosGaleria().observe(getViewLifecycleOwner(), fotos -> {
            binding.containerGaleria.setLayoutManager(new GridLayoutManager(requireContext(), 3));
            binding.containerGaleria.setAdapter(
                    new GaleriaFotosAdapter(fotos, this::mostrarModalFotoFull));
        });

        viewModel.getIntervenciones().observe(getViewLifecycleOwner(), intervenciones -> {
            binding.containerIntervenciones.setLayoutManager(
                    new LinearLayoutManager(requireContext()));
            binding.containerIntervenciones.setAdapter(
                    new IntervencionesAdapter(intervenciones));
        });

        viewModel.getListaVacunasUI().observe(getViewLifecycleOwner(), vacunas -> {
            binding.containerVacunas.removeAllViews();
            LayoutInflater inflater = LayoutInflater.from(requireContext());

            for (DetalleMascotaViewModel.VacunaUI v : vacunas) {
                View itemView = inflater.inflate(R.layout.item_vacuna_check, binding.containerVacunas, false);
                TextView tvNombre = itemView.findViewById(R.id.tvNombreVacuna);
                TextView tvFecha = itemView.findViewById(R.id.tvFechaVacuna);
                com.google.android.material.checkbox.MaterialCheckBox cbAplicada = itemView.findViewById(R.id.cbVacunaAplicada);
                if (v.fecha != null && !v.fecha.isEmpty()) {
                    tvFecha.setText("Aplicada: " + v.fecha);
                    tvFecha.setVisibility(View.VISIBLE);
                } else {
                    tvFecha.setVisibility(View.GONE);
                }

                tvNombre.setText(v.nombre);
                cbAplicada.setChecked(v.aplicada);
                cbAplicada.setClickable(false);
                binding.containerVacunas.addView(itemView);
            }
        });
    }

    private void poblarUI(MascotaResponse m) {
        ImageLoader.cargarPublica(requireContext(), m.urlPortada,
                binding.ivPortadaMascota, R.drawable.bg_registro_header);

        binding.tvNombreMascotaHeader.setText(m.nombre);
        binding.chipRefugio.setText("Refugio " + m.nombreRefugio);
        binding.tvHistoriaMascota.setText(m.historia != null ? m.historia : "Sin historia.");

        binding.itemEstado.tvItemTitle.setText("Estado");
        binding.itemEstado.tvItemValue.setText(m.estado);
        binding.itemEstado.ivItemIcon.setImageResource(R.drawable.ic_status);

        binding.itemGenero.tvItemTitle.setText("Género");
        binding.itemGenero.tvItemValue.setText(m.genero);
        binding.itemGenero.ivItemIcon.setImageResource(R.drawable.ic_gender);

        binding.itemTemperamento.tvItemTitle.setText("Temperamento");
        binding.itemTemperamento.tvItemValue.setText(m.temperamento != null ? m.temperamento : "No especificado");

        binding.itemEdad.tvItemTitle.setText("Edad");
        binding.itemEdad.tvItemValue.setText(m.edadAnios + " años, " + m.edadMeses + " meses");
        binding.itemEdad.ivItemIcon.setImageResource(R.drawable.ic_age);
    }

    private void configurarLogicaVistas() {
        if (session.esRefugio()) {
            binding.btnFavorito.setVisibility(View.GONE);
            if(mascotaActual != null && session.getIdRefugio() == mascotaActual.idRefugio){
                binding.btnSolicitud.setText("Editar información");
                binding.btnSolicitud.setIconResource(R.drawable.ic_edit);
                binding.btnSolicitud.setOnClickListener(v -> editarMascota(mascotaActual));
            }
            else {
                binding.btnSolicitud.setVisibility(View.GONE);
            }
        } else {
            binding.btnFavorito.setVisibility(View.VISIBLE);
            binding.btnSolicitud.setOnClickListener(v -> enviarSolicitud());
            binding.btnFavorito.setOnClickListener(v -> {
                int idAdoptante = session.getIdAdoptante();
                if (idAdoptante != -1) {
                    viewModel.toggleFavorito(idAdoptante);
                } else {
                    Toast.makeText(requireContext(),
                            "Error de sesión de adoptante", Toast.LENGTH_SHORT).show();
                }
            });
        }

        binding.toggleGroupSections.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                binding.containerDetalles.setVisibility(
                        checkedId == R.id.btnDetalles ? View.VISIBLE : View.GONE);
                binding.containerGaleria.setVisibility(
                        checkedId == R.id.btnGaleria ? View.VISIBLE : View.GONE);
                binding.containerVacunas.setVisibility(
                        checkedId == R.id.btnVacunas ? View.VISIBLE : View.GONE);
                binding.containerIntervenciones.setVisibility(
                        checkedId == R.id.btnIntervenciones ? View.VISIBLE : View.GONE);
            }
        });

        binding.chipRefugio.setOnClickListener(v -> abrirGoogleMaps());
    }

    private void abrirGoogleMaps() {
        if (mascotaActual == null
                || mascotaActual.latitudRefugio == null
                || mascotaActual.longitudRefugio == null) {
            Toast.makeText(requireContext(),
                    "Ubicación del refugio no disponible", Toast.LENGTH_SHORT).show();
            return;
        }

        String uriStr = String.format("geo:%s,%s?q=%s,%s(%s)",
                mascotaActual.latitudRefugio,
                mascotaActual.longitudRefugio,
                mascotaActual.latitudRefugio,
                mascotaActual.longitudRefugio,
                Uri.encode(mascotaActual.nombreRefugio));

        Intent mapIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uriStr));
        mapIntent.setPackage("com.google.android.apps.maps");

        if (mapIntent.resolveActivity(requireActivity().getPackageManager()) != null) {
            startActivity(mapIntent);
        } else {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://maps.google.com/?q="
                            + mascotaActual.latitudRefugio + ","
                            + mascotaActual.longitudRefugio)));
        }
    }

    private void enviarSolicitud() {
        if (mascotaActual == null) return;
        CrearSolicitudBottomSheet bottomSheet = new CrearSolicitudBottomSheet(mascotaActual);
        bottomSheet.show(requireActivity().getSupportFragmentManager(), "CrearSolicitud");
    }

    private void editarMascota(MascotaResponse m){
        Intent intent = new Intent(requireContext(), EditarMascotaActivity.class);
        intent.putExtra("mascota", m);
        editMascotaLauncher.launch(intent);
    }

    private void mostrarModalFotoFull(String urlFoto) {
        Dialog dialog = new Dialog(requireContext(),
                android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.modal_foto_full);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(
                    new ColorDrawable(Color.parseColor("#CC000000")));
        }

        ImageView      ivFullFoto        = dialog.findViewById(R.id.ivFullFoto);
        MaterialButton btnCerrarFullFoto = dialog.findViewById(R.id.btnCerrarFullFoto);

        ImageLoader.cargarPublica(requireContext(), urlFoto,
                ivFullFoto, R.drawable.bg_registro_header);

        btnCerrarFullFoto.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (debeOcultarMenu) {
            View bottomNav = requireActivity().findViewById(R.id.bottomNavigation);
            if (bottomNav != null) bottomNav.setVisibility(View.GONE);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (debeOcultarMenu) {
            View bottomNav = requireActivity().findViewById(R.id.bottomNavigation);
            if (bottomNav != null) bottomNav.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}