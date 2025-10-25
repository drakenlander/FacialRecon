package com.example.imagepicker;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.imagepicker.face_recognition.FaceClassifier;

import java.util.ArrayList;

public class FacesAdapter extends RecyclerView.Adapter<FacesAdapter.ViewHolder> {

    private final ArrayList<FaceClassifier.Recognition> facesList;

    public FacesAdapter(ArrayList<FaceClassifier.Recognition> facesList) {
        this.facesList = facesList;
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
    }

    @Override
    public int getItemCount() {
        return facesList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView faceIdText;
        public TextView faceNameText;

        public ViewHolder(View view) {
            super(view);
            faceIdText = view.findViewById(R.id.face_id_text);
            faceNameText = view.findViewById(R.id.face_name_text);
        }
    }
}
