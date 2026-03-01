package unc.edu.pe.appadopcion.ui.feed;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import unc.edu.pe.appadopcion.R;
import unc.edu.pe.appadopcion.databinding.ItemMascotaBinding;
import unc.edu.pe.appadopcion.data.model.MascotaResponse;
import unc.edu.pe.appadopcion.utils.ImageLoader;

public class MascotaAdapter extends RecyclerView.Adapter<MascotaAdapter.MascotaViewHolder> {

    private List<MascotaResponse> mascotas = new ArrayList<>();
    private final boolean esAdoptante;
    private final String tokenUsuario; // <--- NUEVA VARIABLE
    private final OnMascotaClickListener listener;

    private final Set<Integer> favoritosSeleccionados = new HashSet<>();

    public interface OnMascotaClickListener {
        void onMascotaClick(MascotaResponse mascota); // <-- CAMBIO AQUÍ
        void onRefugioClick(int idRefugio);
        void onFavoritoClick(MascotaResponse mascota, int posicion, boolean agregarAFavoritos);
    }

    // --- CONSTRUCTOR ACTUALIZADO ---
    public MascotaAdapter(boolean esAdoptante, String tokenUsuario, OnMascotaClickListener listener) {
        this.esAdoptante = esAdoptante;
        this.tokenUsuario = tokenUsuario;
        this.listener = listener;
    }

    public void setMascotas(List<MascotaResponse> nuevasMascotas) {
        this.mascotas = nuevasMascotas;
        notifyDataSetChanged();
    }

    // Método opcional para inicializar los favoritos que vienen de la BD al abrir la app
    public void setFavoritosIniciales(List<Integer> idsFavoritos) {
        favoritosSeleccionados.clear();
        favoritosSeleccionados.addAll(idsFavoritos);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MascotaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemMascotaBinding binding = ItemMascotaBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new MascotaViewHolder(binding);

    }

    @Override
    public void onBindViewHolder(@NonNull MascotaViewHolder holder, int position) {
        holder.bind(mascotas.get(position));
    }

    @Override
    public int getItemCount() {
        return mascotas.size();
    }

    class MascotaViewHolder extends RecyclerView.ViewHolder {
        private final ItemMascotaBinding binding;

        public MascotaViewHolder(ItemMascotaBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(MascotaResponse mascota) {
            binding.tvNombreMascota.setText(mascota.nombre);
            binding.tvNombreRefugio.setText(mascota.nombreRefugio);
            binding.tvDireccionRefugio.setText(mascota.direccionRefugio);
            binding.tvRazaSexo.setText(mascota.nombreRaza + " / " + mascota.genero);
            binding.tvHistoriaResumen.setText(mascota.historia != null ? mascota.historia : "Sin historia registrada.");

            // 1. Cargar foto principal de la mascota
            ImageLoader.cargarPublica(itemView.getContext(), mascota.urlPortada,
                    binding.ivMascotaFoto, R.drawable.ic_pets);

            // 2. LÓGICA PARA LA FOTO DE PERFIL DEL REFUGIO
            if (mascota.perfilRefugio != null && !mascota.perfilRefugio.isEmpty()) {
                ImageLoader.cargarPublica(itemView.getContext(), mascota.perfilRefugio,
                        binding.ivRefugioLogo, R.drawable.ic_person);
            } else {
                // Fallback de seguridad: Si no tiene foto de perfil, usamos la portada
                ImageLoader.cargarPublica(itemView.getContext(), mascota.portadaRefugio,
                        binding.ivRefugioLogo, R.drawable.ic_person);
            }

            // --- Lógica del Corazón ---
            if (esAdoptante) {
                binding.btnFavorito.setVisibility(View.VISIBLE);
                boolean esFavorito = favoritosSeleccionados.contains(mascota.idMascota);
                actualizarIconoCorazon(esFavorito);

                binding.btnFavorito.setOnClickListener(v -> {
                    boolean actualmenteFavorito = favoritosSeleccionados.contains(mascota.idMascota);
                    if (actualmenteFavorito) {
                        favoritosSeleccionados.remove(mascota.idMascota);
                    } else {
                        favoritosSeleccionados.add(mascota.idMascota);
                    }
                    actualizarIconoCorazon(!actualmenteFavorito);
                    listener.onFavoritoClick(mascota, getAdapterPosition(), !actualmenteFavorito);
                });
            } else {
                binding.btnFavorito.setVisibility(View.GONE);
            }

            // Clics
            binding.getRoot().setOnClickListener(v -> listener.onMascotaClick(mascota));
            binding.ivRefugioLogo.setOnClickListener(v -> listener.onRefugioClick(mascota.idRefugio));
            binding.tvNombreRefugio.setOnClickListener(v -> listener.onRefugioClick(mascota.idRefugio));
        }

        private void actualizarIconoCorazon(boolean esFavorito) {
            if (esFavorito) {
                binding.btnFavorito.setImageResource(R.drawable.ic_favorite);
                // Usamos el color rojo/rosado característico
                binding.btnFavorito.setColorFilter(android.graphics.Color.parseColor("#E91E63"));
            } else {
                binding.btnFavorito.setImageResource(R.drawable.ic_heart_outline);
                // Tono gris para que no resalte cuando está vacío
                binding.btnFavorito.setColorFilter(android.graphics.Color.parseColor("#757575"));
            }
        }


    }
}