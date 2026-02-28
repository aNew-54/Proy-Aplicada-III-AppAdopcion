package unc.edu.pe.appadopcion.ui.mascotas.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import unc.edu.pe.appadopcion.R;
import unc.edu.pe.appadopcion.data.model.IntervencionResponse;

public class IntervencionesAdapter extends RecyclerView.Adapter<IntervencionesAdapter.ViewHolder> {
    private List<IntervencionResponse> lista;

    public IntervencionesAdapter(List<IntervencionResponse> lista) { this.lista = lista; }

    @NonNull @Override public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_intervencion_medica, parent, false));
    }

    @Override public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        IntervencionResponse i = lista.get(position);
        holder.tvTitulo.setText(i.titulo);
        holder.tvFecha.setText(i.fecha != null ? i.fecha : "Sin fecha");
        holder.tvDesc.setText(i.descripcion != null ? i.descripcion : "Sin descripción");
    }

    @Override public int getItemCount() { return lista.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitulo, tvFecha, tvDesc;
        ViewHolder(View v) {
            super(v);
            tvTitulo = v.findViewById(R.id.tvTituloIntervencion);
            tvFecha = v.findViewById(R.id.tvFechaIntervencion);
            tvDesc = v.findViewById(R.id.tvDescripcionIntervencion);
        }
    }
}