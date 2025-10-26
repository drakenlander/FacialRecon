package com.example.imagepicker;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.imagepicker.face_recognition.FaceClassifier;

import java.util.ArrayList;

public class FacesAdapter extends RecyclerView.Adapter<FacesAdapter.ViewHolder> {

    private final ArrayList<FaceClassifier.Recognition> facesList;
    private final int role;

    public FacesAdapter(ArrayList<FaceClassifier.Recognition> facesList, int role) {
        this.facesList = facesList;
        this.role = role;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.face_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FaceClassifier.Recognition recognition = facesList.get(position);
        holder.faceIdText.setText("ID: " + recognition.getId());
        holder.faceNameText.setText("Name: " + recognition.getTitle());

        if (role == 2) {
            holder.modifyButton.setVisibility(View.VISIBLE);
            holder.deleteButton.setVisibility(View.VISIBLE);
        } else {
            holder.modifyButton.setVisibility(View.GONE);
            holder.deleteButton.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return facesList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView faceIdText;
        public TextView faceNameText;
        public ImageButton modifyButton;
        public ImageButton deleteButton;

        public ViewHolder(View view) {
            super(view);
            faceIdText = view.findViewById(R.id.face_id_text);
            faceNameText = view.findViewById(R.id.face_name_text);
            modifyButton = view.findViewById(R.id.modify_button);
            deleteButton = view.findViewById(R.id.delete_button);
        }
    }
}
