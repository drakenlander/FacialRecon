package com.example.imagepicker;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class AttemptsAdapter extends RecyclerView.Adapter<AttemptsAdapter.ViewHolder> {

    private final List<Attempt> attemptsList;

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
        holder.attemptNameText.setText("Name: " + attempt.getPersonName());
        holder.attemptTimestampText.setText("Timestamp: " + attempt.getTimestamp());

        if (attempt.getPersonId() != null) {
            holder.attemptStatusText.setText("Status: Successful");
            holder.attemptStatusText.setTextColor(Color.GREEN);
        } else {
            holder.attemptStatusText.setText("Status: Unsuccessful");
            holder.attemptStatusText.setTextColor(Color.RED);
        }
    }

    @Override
    public int getItemCount() {
        return attemptsList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView attemptNameText;
        public TextView attemptTimestampText;
        public TextView attemptStatusText;

        public ViewHolder(View view) {
            super(view);
            attemptNameText = view.findViewById(R.id.attempt_name_text);
            attemptTimestampText = view.findViewById(R.id.attempt_timestamp_text);
            attemptStatusText = view.findViewById(R.id.attempt_status_text);
        }
    }
}
