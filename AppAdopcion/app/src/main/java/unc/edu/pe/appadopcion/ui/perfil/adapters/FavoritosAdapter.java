package unc.edu.pe.appadopcion.ui.perfil.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import unc.edu.pe.appadopcion.data.model.MascotaResponse;
import unc.edu.pe.appadopcion.databinding.ItemFavoritoBinding;

public class FavoritosAdapter extends RecyclerView.Adapter<FavoritosAdapter.FavoritoViewHolder> {

    private List<MascotaResponse> listaMascotas = new ArrayList<>();
    private final OnMascotaClickListener listener;

    // Interfaz para detectar clics y enviar el ID
    public interface OnMascotaClickListener {
        void onMascotaClick(MascotaResponse mascota);
    }

    public FavoritosAdapter(OnMascotaClickListener listener) {
        this.listener = listener;
    }

    public void setMascotas(List<MascotaResponse> nuevasMascotas) {
        this.listaMascotas = nuevasMascotas;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public FavoritoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemFavoritoBinding binding = ItemFavoritoBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new FavoritoViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull FavoritoViewHolder holder, int position) {
        MascotaResponse mascota = listaMascotas.get(position);
        holder.bind(mascota);
    }

    @Override
    public int getItemCount() {
        return listaMascotas.size();
    }

    class FavoritoViewHolder extends RecyclerView.ViewHolder {
        private final ItemFavoritoBinding binding;

        public FavoritoViewHolder(ItemFavoritoBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

            binding.getRoot().setOnClickListener(v -> {
                int position = getBindingAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    listener.onMascotaClick(listaMascotas.get(position));
                }
            });
        }

        public void bind(MascotaResponse mascota) {
            binding.tvNombreMascota.setText(mascota.nombre != null ? mascota.nombre : "Sin nombre");
            binding.tvRazaMascota.setText(mascota.nombreRaza != null ? mascota.nombreRaza : "Desconocida");
            binding.tvRefugioMascota.setText(mascota.nombreRefugio != null ? mascota.nombreRefugio : "Sin refugio");

            String textoEdad = mascota.edadAnios + " años y " + mascota.edadMeses + " meses";
            binding.tvEdadMascota.setText(textoEdad);

            // Carga de la foto usando Glide 4 (como indica la documentación)
            if (mascota.urlPortada != null && !mascota.urlPortada.isEmpty()) {
                Glide.with(binding.getRoot().getContext())
                        .load(mascota.urlPortada)
                        .centerCrop()
                        .into(binding.ivFotoMascota);
            }
        }
    }
}