package unc.edu.pe.appadopcion.ui.solicitudes.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.chip.Chip;
import java.util.List;
import unc.edu.pe.appadopcion.R;
import unc.edu.pe.appadopcion.data.model.SolicitudResponse;
import unc.edu.pe.appadopcion.utils.ImageLoader;

public class SolicitudesAdapter extends RecyclerView.Adapter<SolicitudesAdapter.ViewHolder> {

    private List<SolicitudResponse> lista;
    private final OnItemClickListener listener;
    private final boolean esRefugio; // Para saber qué nombre mostrar

    public interface OnItemClickListener {
        void onItemClick(SolicitudResponse solicitud);
    }

    public SolicitudesAdapter(List<SolicitudResponse> lista, boolean esRefugio, OnItemClickListener listener) {
        this.lista = lista;
        this.esRefugio = esRefugio;
        this.listener = listener;
    }

    public void setLista(List<SolicitudResponse> nuevaLista) {
        this.lista = nuevaLista;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_solicitud, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SolicitudResponse sol = lista.get(position);

        // Cargar Imagen
        ImageLoader.cargarPublica(holder.itemView.getContext(), sol.urlPortadaMascota, holder.ivMascotaItem, R.drawable.ic_pets);

        // Textos
        holder.tvMascotaNombre.setText(sol.nombreMascota != null ? sol.nombreMascota : "Mascota");
        holder.tvFecha.setText(sol.fechaSolicitud != null ? sol.fechaSolicitud.substring(0, 10) : "");
        holder.tvMensajeSnippet.setText(sol.mensaje != null ? sol.mensaje : "Sin mensaje.");

        // Si es refugio, muestra el nombre del adoptante; si es adoptante, muestra el nombre del refugio
        holder.tvNombresRoles.setText(esRefugio ? "De: " + sol.nombreAdoptante : "Refugio: " + sol.nombreRefugio);

        // Estado
        holder.chipEstado.setText(sol.estado);
        switch (sol.estado) {
            case "Pendiente":
                holder.chipEstado.setChipBackgroundColorResource(android.R.color.holo_orange_light);
                break;
            case "Aprobada":
                holder.chipEstado.setChipBackgroundColorResource(android.R.color.holo_green_dark);
                break;
            case "Rechazada":
                holder.chipEstado.setChipBackgroundColorResource(android.R.color.holo_red_dark);
                break;
            case "Visita Agendada":
                holder.chipEstado.setChipBackgroundColorResource(android.R.color.holo_blue_dark);
                break;
            default:
                holder.chipEstado.setChipBackgroundColorResource(android.R.color.darker_gray);
                break;
        }

        holder.itemView.setOnClickListener(v -> listener.onItemClick(sol));
    }

    @Override
    public int getItemCount() { return lista != null ? lista.size() : 0; }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivMascotaItem;
        TextView tvMascotaNombre, tvNombresRoles, tvFecha, tvMensajeSnippet;
        Chip chipEstado;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivMascotaItem = itemView.findViewById(R.id.ivMascotaItem);
            tvMascotaNombre = itemView.findViewById(R.id.tvMascotaNombre);
            tvNombresRoles = itemView.findViewById(R.id.tvNombresRoles);
            tvFecha = itemView.findViewById(R.id.tvFecha);
            tvMensajeSnippet = itemView.findViewById(R.id.tvMensajeSnippet);
            chipEstado = itemView.findViewById(R.id.chipEstado);
        }
    }
}