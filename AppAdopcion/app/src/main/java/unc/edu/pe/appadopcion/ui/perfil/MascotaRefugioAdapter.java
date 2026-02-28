package unc.edu.pe.appadopcion.ui.perfil;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import unc.edu.pe.appadopcion.R;
import unc.edu.pe.appadopcion.data.model.MascotaResponse;

public class MascotaRefugioAdapter extends RecyclerView.Adapter<MascotaRefugioAdapter.ViewHolder> {

    public interface OnMascotaClickListener {
        void onClick(MascotaResponse mascota);
    }

    private final List<MascotaResponse> lista;
    private final OnMascotaClickListener listener;

    public MascotaRefugioAdapter(List<MascotaResponse> lista, OnMascotaClickListener listener) {
        this.lista    = lista;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_mascota_refugio, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MascotaResponse mascota = lista.get(position);

        holder.tvNombre.setText(mascota.nombre);
        holder.tvRaza.setText(mascota.nombreRaza != null ? mascota.nombreRaza : "Sin raza");
        holder.tvEdad.setText(formatearEdad(mascota.edadAnios, mascota.edadMeses));
        holder.tvEstado.setText(mascota.estado != null ? mascota.estado : "---");
        holder.tvGenero.setText(mascota.genero != null ? mascota.genero : "---");

        // Color del chip de estado
        int colorEstado = mascota.estado != null && mascota.estado.equals("Disponible")
                ? R.color.md_theme_primary
                : R.color.red_error;
        holder.tvEstado.setTextColor(holder.itemView.getContext().getColor(colorEstado));

        // Imagen de portada
        if (mascota.urlPortada != null && !mascota.urlPortada.isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(mascota.urlPortada)
                    .centerCrop()
                    .placeholder(R.drawable.ic_pets)
                    .into(holder.ivFoto);
        } else {
            holder.ivFoto.setImageResource(R.drawable.ic_pets);
        }

        holder.itemView.setOnClickListener(v -> listener.onClick(mascota));
    }

    @Override
    public int getItemCount() { return lista.size(); }

    private String formatearEdad(int anios, int meses) {
        if (anios > 0 && meses > 0) return anios + " a " + meses + " m";
        if (anios > 0)              return anios + " año" + (anios > 1 ? "s" : "");
        if (meses > 0)              return meses + " mes" + (meses > 1 ? "es" : "");
        return "Recién nacido";
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivFoto;
        TextView tvNombre, tvRaza, tvEdad, tvEstado, tvGenero;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivFoto    = itemView.findViewById(R.id.ivFotoMascota);
            tvNombre  = itemView.findViewById(R.id.tvNombreMascota);
            tvRaza    = itemView.findViewById(R.id.tvRaza);
            tvEdad    = itemView.findViewById(R.id.tvEdad);
            tvEstado  = itemView.findViewById(R.id.tvEstado);
            tvGenero  = itemView.findViewById(R.id.tvGenero);
        }
    }
}