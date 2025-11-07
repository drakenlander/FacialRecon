package com.example.imagepicker;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.imagepicker.db.DBHelper;
import com.example.imagepicker.face_recognition.FaceClassifier;

import java.util.ArrayList;

public class FacesAdapter extends RecyclerView.Adapter<FacesAdapter.ViewHolder> {

    private final ArrayList<FaceClassifier.Recognition> facesList;
    private final int role;
    private final Context context;

    public FacesAdapter(Context context, ArrayList<FaceClassifier.Recognition> facesList, int role) {
        this.context = context;
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
        holder.faceCifText.setText("CIF: " + recognition.getCif());
        holder.faceMajorText.setText("Major: " + recognition.getMajor());
        holder.faceSemesterText.setText("Semester: " + recognition.getSemester());

        if (role == 2) {
            holder.modifyButton.setVisibility(View.VISIBLE);
            holder.deleteButton.setVisibility(View.VISIBLE);

            holder.modifyButton.setOnClickListener(v -> {
                DBHelper dbHelper = new DBHelper(context);
                dbHelper.deleteFace(recognition.getId());

                Intent intent = new Intent(context, RegisterActivity.class);
                intent.putExtra("face_name", recognition.getTitle());
                context.startActivity(intent);

                facesList.remove(position);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, facesList.size());
            });

            holder.deleteButton.setOnClickListener(v -> {
                new AlertDialog.Builder(context)
                        .setTitle("Delete Face")
                        .setMessage("Are you sure you want to delete this face?")
                        .setPositiveButton("Yes", (dialog, which) -> {
                            DBHelper dbHelper = new DBHelper(context);
                            dbHelper.deleteFace(recognition.getId());
                            facesList.remove(position);
                            notifyItemRemoved(position);
                            notifyItemRangeChanged(position, facesList.size());
                            Toast.makeText(context, "Face deleted successfully", Toast.LENGTH_SHORT).show();
                        })
                        .setNegativeButton("No", null)
                        .show();
            });

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
        public TextView faceCifText;
        public TextView faceMajorText;
        public TextView faceSemesterText;
        public ImageButton modifyButton;
        public ImageButton deleteButton;

        public ViewHolder(View view) {
            super(view);
            faceIdText = view.findViewById(R.id.face_id_text);
            faceNameText = view.findViewById(R.id.face_name_text);
            faceCifText = view.findViewById(R.id.face_cif_text);
            faceMajorText = view.findViewById(R.id.face_major_text);
            faceSemesterText = view.findViewById(R.id.face_semester_text);
            modifyButton = view.findViewById(R.id.modify_button);
            deleteButton = view.findViewById(R.id.delete_button);
        }
    }
}
