package com.example.imagepicker;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class AttemptsAdapter extends RecyclerView.Adapter<AttemptsAdapter.ViewHolder> {

    private List<Attempt> attemptsList;

    public AttemptsAdapter(List<Attempt> attemptsList) {
        this.attemptsList = attemptsList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.attempt_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Attempt attempt = attemptsList.get(position);
        holder.attemptNameText.setText(attempt.getPersonName());
        holder.attemptTimestampText.setText("Fecha y Hora: " + attempt.getTimestamp());

        if (attempt.getPersonId() != null) {
            holder.idCardText.setVisibility(View.GONE);
            holder.intentText.setVisibility(View.GONE);
            holder.attemptStatusText.setText("Estado: Exitoso");
            holder.attemptStatusText.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.mint_green));
        } else {
            holder.idCardText.setText("Cédula: " + attempt.getIdCard());
            holder.intentText.setText("Motivo: " + attempt.getIntent());
            holder.idCardText.setVisibility(View.VISIBLE);
            holder.intentText.setVisibility(View.VISIBLE);
            holder.attemptStatusText.setText("Estado: Fallido");
            holder.attemptStatusText.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.pastel_red));
        }
    }

    @Override
    public int getItemCount() {
        return attemptsList.size();
    }

    public void updateList(List<Attempt> newList) {
        this.attemptsList = newList;
        notifyDataSetChanged();
    }

    public List<Attempt> getAttemptsList() {
        return attemptsList;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView attemptNameText;
        public TextView attemptTimestampText;
        public TextView attemptStatusText;
        public TextView idCardText;
        public TextView intentText;

        public ViewHolder(View view) {
            super(view);
            attemptNameText = view.findViewById(R.id.attempt_name_text);
            attemptTimestampText = view.findViewById(R.id.attempt_timestamp_text);
            idCardText = view.findViewById(R.id.id_card_text);
            intentText = view.findViewById(R.id.intent_text);
            attemptStatusText = view.findViewById(R.id.attempt_status_text);
        }
    }
}
