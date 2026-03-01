package unc.edu.pe.appadopcion.ui.refugios;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import unc.edu.pe.appadopcion.R;
import unc.edu.pe.appadopcion.data.model.RefugioResponse;
import unc.edu.pe.appadopcion.databinding.ItemRefugioBinding;

public class RefugioAdapter extends ListAdapter<RefugioResponse, RefugioAdapter.ViewHolder> {

    public interface OnRefugioClickListener {
        void onClick(RefugioResponse refugio);
    }

    private final OnRefugioClickListener listener;

    public RefugioAdapter(OnRefugioClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<RefugioResponse> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<RefugioResponse>() {
                @Override
                public boolean areItemsTheSame(@NonNull RefugioResponse a, @NonNull RefugioResponse b) {
                    return a.idRefugio == b.idRefugio;
                }
                @Override
                public boolean areContentsTheSame(@NonNull RefugioResponse a, @NonNull RefugioResponse b) {
                    return a.nombre != null && a.nombre.equals(b.nombre)
                            && a.mascotasDisponibles == b.mascotasDisponibles;
                }
            };

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemRefugioBinding binding = ItemRefugioBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(getItem(position), listener);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemRefugioBinding binding;

        ViewHolder(ItemRefugioBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(RefugioResponse refugio, OnRefugioClickListener listener) {
            binding.tvRefugioNombre.setText(refugio.nombre != null ? refugio.nombre : "Sin nombre");
            binding.tvRefugioDireccion.setText(refugio.direccion != null ? refugio.direccion : "Sin direccion");
            binding.tvRefugioMascotas.setText(refugio.mascotasDisponibles + " mascotas");

            // Cargar imagen de portada
            if (refugio.urlPortada != null && !refugio.urlPortada.isEmpty()) {
                String url = refugio.urlPortada.startsWith("http")
                        ? refugio.urlPortada
                        : unc.edu.pe.appadopcion.BuildConfig.SUPABASE_URL
                        + "/storage/v1/object/public/" + refugio.urlPortada;

                Glide.with(binding.getRoot().getContext())
                        .load(url)
                        .centerCrop()
                        .placeholder(R.drawable.ic_launcher_background)
                        .into(binding.imgRefugioPortada);
            } else {
                binding.imgRefugioPortada.setImageResource(R.drawable.ic_launcher_background);
            }

            binding.getRoot().setOnClickListener(v -> listener.onClick(refugio));
        }
    }
}