package unc.edu.pe.appadopcion.ui.mascotas;

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
import unc.edu.pe.appadopcion.utils.ImageLoader;
import unc.edu.pe.appadopcion.vm.perfil.DetalleMascotaViewModel;

public class DetalleMascotaFragment extends Fragment {

    private FragmentDetalleMascotaBinding binding;
    private SessionManager session;
    private DetalleMascotaViewModel viewModel;

    private MascotaResponse mascotaActual;

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

        // --- CÓDIGO ACTUALIZADO PARA RECIBIR LA MASCOTA ---
        if (getArguments() != null) {
            mascotaActual = (MascotaResponse) getArguments().getSerializable("mascota");
        }

        configurarLogicaVistas();
        observarViewModel();

        // Asegurarse de que no sea null antes de cargar
        if (mascotaActual != null) {
            viewModel.cargarDatosMascota(mascotaActual, session.getIdAdoptante());
        } else {
            Toast.makeText(requireContext(), "Error al cargar la mascota", Toast.LENGTH_SHORT).show();
            requireActivity().onBackPressed(); // Vuelve atrás si falla
        }
    }

    private void observarViewModel() {
        viewModel.getMascota().observe(getViewLifecycleOwner(), this::poblarUI);

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
            binding.containerVacunas.removeAllViews(); // Limpiar por si acaso
            LayoutInflater inflater = LayoutInflater.from(requireContext());

            for (DetalleMascotaViewModel.VacunaUI v : vacunas) {
                // Inflamos el XML de la tarjeta de vacuna que creamos antes
                View itemView = inflater.inflate(R.layout.item_vacuna_check, binding.containerVacunas, false);

                TextView tvNombre = itemView.findViewById(R.id.tvNombreVacuna);
                com.google.android.material.checkbox.MaterialCheckBox cbAplicada = itemView.findViewById(R.id.cbVacunaAplicada);

                tvNombre.setText(v.nombre);
                cbAplicada.setChecked(v.aplicada);
                cbAplicada.setClickable(false); // Solo lectura, no queremos que el usuario lo cambie aquí

                // Lo añadimos al contenedor
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

        // --- Configurar Estado ---
        // Accedemos a las vistas DENTRO del include directamente a través de su clase Binding
        binding.itemEstado.tvItemTitle.setText("Estado");
        binding.itemEstado.tvItemValue.setText(m.estado);
        binding.itemEstado.ivItemIcon.setImageResource(R.drawable.ic_status);

        // --- Configurar Genero ---
        binding.itemGenero.tvItemTitle.setText("Género");
        binding.itemGenero.tvItemValue.setText(m.genero);
        binding.itemGenero.ivItemIcon.setImageResource(R.drawable.ic_gender);

        // --- Configurar Temperamento ---
        // (Asumo que en tu include el título dice "Temperamento" por defecto, si no, ponlo)
        binding.itemTemperamento.tvItemTitle.setText("Temperamento");
        binding.itemTemperamento.tvItemValue.setText(m.temperamento != null ? m.temperamento : "No especificado");
        // binding.itemTemperamento.ivItemIcon.setImageResource(R.drawable.lo_que_sea); // (Opcional si quieres cambiar el icono)

        // --- Configurar Edad ---
        binding.itemEdad.tvItemTitle.setText("Edad");
        binding.itemEdad.tvItemValue.setText(m.edadAnios + " años, " + m.edadMeses + " meses");
        binding.itemEdad.ivItemIcon.setImageResource(R.drawable.ic_age);
    }

    private void configurarLogicaVistas() {
        if (session.esRefugio()) {
            binding.btnFavorito.setVisibility(View.GONE);
            binding.btnSolicitud.setText("Editar información");
            binding.btnSolicitud.setIconResource(R.drawable.ic_edit);
        } else {
            binding.btnFavorito.setVisibility(View.VISIBLE);
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
        if(session.esAdoptante()){
            binding.btnSolicitud.setOnClickListener(v -> enviarSolicitud());
        }
        else {
            binding.btnSolicitud.setOnClickListener(v -> editarMascota(mascotaActual));
        }
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
        if (mascotaActual == null || mascotaActual.telefonoRefugio == null) {
            Toast.makeText(requireContext(),
                    "Teléfono no disponible", Toast.LENGTH_SHORT).show();
            return;
        }

        String numero  = mascotaActual.telefonoRefugio.replace("+", "").replace(" ", "");
        String mensaje = "Hola Refugio " + mascotaActual.nombreRefugio
                + ", estoy interesado en adoptar a " + mascotaActual.nombre
                + ". Quisiera más información.";

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(
                "https://api.whatsapp.com/send?phone=" + numero
                        + "&text=" + Uri.encode(mensaje)));

        try {
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(requireContext(),
                    "No tienes WhatsApp instalado", Toast.LENGTH_SHORT).show();
        }
    }

    private void editarMascota(MascotaResponse m){
        Intent intent = new Intent(requireContext(), EditarMascota.class);
        intent.putExtra("mascota", m);

        startActivity(intent);
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
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}