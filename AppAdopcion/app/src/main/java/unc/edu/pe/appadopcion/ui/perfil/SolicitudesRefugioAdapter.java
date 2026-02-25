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
 * Adapter para la lista de solicitudes recibidas por el refugio.
 * Incluye callback para cuando el refugio toque "Ver detalles" (funcionalidad futura).
 */
public class SolicitudesRefugioAdapter extends RecyclerView.Adapter<SolicitudesRefugioAdapter.ViewHolder> {

    public interface OnSolicitudClickListener {
        void onVerDetalles(int idSolicitud, boolean aprobada);
    }

    private final List<SolicitudResponse> lista;
    private final OnSolicitudClickListener listener;

    public SolicitudesRefugioAdapter(List<SolicitudResponse> lista, OnSolicitudClickListener listener) {
        this.lista    = lista;
        this.listener = listener;
    }

    @NonNull @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_solicitud_refugio, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SolicitudResponse s = lista.get(position);
        holder.tvEstado.setText(s.estado != null ? s.estado : "Pendiente");
        holder.tvFecha.setText(s.fechaSolicitud != null
                ? s.fechaSolicitud.substring(0, 10) : "—");
        holder.tvFechaVisita.setText(s.fechaVisita != null
                ? "Visita: " + s.fechaVisita.substring(0, 10) : "Sin fecha de visita");

        // TODO: al hacer clic → abrir detalle de solicitud (funcionalidad futura)
        holder.itemView.setOnClickListener(v ->
                listener.onVerDetalles(s.idSolicitud, "Aprobada".equals(s.estado)));
    }

    @Override public int getItemCount() { return lista.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvEstado, tvFecha, tvFechaVisita;
        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvEstado     = itemView.findViewById(R.id.tvEstado);
            tvFecha      = itemView.findViewById(R.id.tvFecha);
            tvFechaVisita = itemView.findViewById(R.id.tvFechaVisita);
        }
    }
}