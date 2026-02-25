package unc.edu.pe.appadopcion.ui.perfil;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import unc.edu.pe.appadopcion.R;
import unc.edu.pe.appadopcion.data.model.SolicitudResponse;

/**
 * Adapter para la lista de solicitudes enviadas por el adoptante.
 * Muestra: estado, fecha, y un resumen del mensaje.
 */
public class SolicitudesAdoptanteAdapter extends RecyclerView.Adapter<SolicitudesAdoptanteAdapter.ViewHolder> {

    private final List<SolicitudResponse> lista;

    public SolicitudesAdoptanteAdapter(List<SolicitudResponse> lista) {
        this.lista = lista;
    }

    @NonNull @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_solicitud_adoptante, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SolicitudResponse s = lista.get(position);
        holder.tvEstado.setText(getEmojiEstado(s.estado) + " " + s.estado);
        holder.tvFecha.setText(s.fechaSolicitud != null
                ? s.fechaSolicitud.substring(0, 10) : "—");
        holder.tvMensaje.setText(s.mensaje != null ? s.mensaje : "Sin mensaje");
    }

    @Override public int getItemCount() { return lista.size(); }

    private String getEmojiEstado(String estado) {
        if (estado == null) return "⏳";
        switch (estado) {
            case "Aprobada":        return "✅";
            case "Rechazada":       return "❌";
            case "Visita Agendada": return "📅";
            default:                return "⏳";
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvEstado, tvFecha, tvMensaje;
        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvEstado  = itemView.findViewById(R.id.tvEstado);
            tvFecha   = itemView.findViewById(R.id.tvFecha);
            tvMensaje = itemView.findViewById(R.id.tvMensaje);
        }
    }
}