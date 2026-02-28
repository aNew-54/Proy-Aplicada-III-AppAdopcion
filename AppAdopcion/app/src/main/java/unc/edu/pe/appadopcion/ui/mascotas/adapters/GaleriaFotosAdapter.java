package unc.edu.pe.appadopcion.ui.mascotas.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import unc.edu.pe.appadopcion.R;
import unc.edu.pe.appadopcion.data.model.FotoMascotaResponse;
import unc.edu.pe.appadopcion.utils.ImageLoader;

public class GaleriaFotosAdapter extends RecyclerView.Adapter<GaleriaFotosAdapter.ViewHolder> {
    private List<FotoMascotaResponse> fotos;
    private OnFotoClickListener listener;

    public interface OnFotoClickListener { void onFotoClick(String urlFoto); }

    public GaleriaFotosAdapter(List<FotoMascotaResponse> fotos, OnFotoClickListener listener) {
        this.fotos = fotos;
        this.listener = listener;
    }

    @NonNull @Override public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_galeria_foto, parent, false));
    }

    @Override public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String url = fotos.get(position).urlImagen;
        ImageLoader.cargarPublica(holder.itemView.getContext(), url, holder.ivFoto, R.drawable.bg_registro_header);
        holder.ivFoto.setOnClickListener(v -> listener.onFotoClick(url));
    }

    @Override public int getItemCount() { return fotos.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivFoto;
        ViewHolder(View v) { super(v); ivFoto = v.findViewById(R.id.ivGridFoto); }
    }
}