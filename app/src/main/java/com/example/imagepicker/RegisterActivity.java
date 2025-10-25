package com.example.imagepicker;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.imagepicker.face_recognition.FaceClassifier;
import com.example.imagepicker.face_recognition.TFLiteFaceRecognition;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class RegisterActivity extends AppCompatActivity {

    private static final String KEY_IMAGE_URI = "image_uri";
    private ImageView imageView;
    private CardView galleryCard, cameraCard;
    private Uri image_uri;
    private FaceDetector detector;
    private FaceClassifier faceClassifier;

    private final ActivityResultLauncher<Intent> galleryActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    image_uri = result.getData().getData();
                    Bitmap bitmap = getBitmapFromUri(image_uri);
                    if (bitmap != null) {
                        imageView.setImageBitmap(bitmap);
                        performFaceDetection(bitmap);
                    }
                }
            });

    private final ActivityResultLauncher<Intent> cameraActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Bitmap bitmap = getBitmapFromUri(image_uri);
                    if (bitmap != null) {
                        imageView.setImageBitmap(bitmap);
                        performFaceDetection(bitmap);
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        if (savedInstanceState != null) {
            String uriString = savedInstanceState.getString(KEY_IMAGE_URI);
            if (uriString != null) {
                image_uri = Uri.parse(uriString);
            }
        }

        imageView = findViewById(R.id.imageView2);
        galleryCard = findViewById(R.id.gallerycard);
        cameraCard = findViewById(R.id.cameracard);

        galleryCard.setOnClickListener(v -> {
            Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            galleryActivityResultLauncher.launch(galleryIntent);
        });

        cameraCard.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED) {
                    requestPermissions(new String[]{Manifest.permission.CAMERA}, 112);
                } else {
                    openCamera();
                }
            } else {
                openCamera();
            }
        });

        FaceDetectorOptions highAccuracyOpts = new FaceDetectorOptions.Builder()
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
                .build();
        detector = FaceDetection.getClient(highAccuracyOpts);

        try {
            faceClassifier = TFLiteFaceRecognition.create(getAssets(), "mobile_face_net.tflite", 112, false, this);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (image_uri != null) {
            outState.putString(KEY_IMAGE_URI, image_uri.toString());
        }
    }

    private void openCamera() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "New Picture");
        values.put(MediaStore.Images.Media.DESCRIPTION, "From the Camera");
        image_uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
        cameraActivityResultLauncher.launch(cameraIntent);
    }

    private Bitmap getBitmapFromUri(Uri uri) {
        try (InputStream inputStream = getContentResolver().openInputStream(uri)) {
            return BitmapFactory.decodeStream(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void performFaceDetection(Bitmap input) {
        Bitmap mutableBmp = input.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(mutableBmp);
        InputImage image = InputImage.fromBitmap(input, 0);

        detector.process(image)
                .addOnSuccessListener(faces -> {
                    if (!faces.isEmpty()) {
                        performFaceRecognition(faces.get(0).getBoundingBox(), input);
                        Paint p1 = new Paint();
                        p1.setColor(Color.RED);
                        p1.setStyle(Paint.Style.STROKE);
                        p1.setStrokeWidth(5);
                        canvas.drawRect(faces.get(0).getBoundingBox(), p1);
                    }
                    imageView.setImageBitmap(mutableBmp);
                })
                .addOnFailureListener(e -> Toast.makeText(RegisterActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    public void performFaceRecognition(Rect bounds, Bitmap input) {
        if (bounds.left < 0) bounds.left = 0;
        if (bounds.top < 0) bounds.top = 0;
        if (bounds.right > input.getWidth()) bounds.right = input.getWidth() - 1;
        if (bounds.bottom > input.getHeight()) bounds.bottom = input.getHeight() - 1;

        Bitmap croppedFace = Bitmap.createBitmap(input, bounds.left, bounds.top, bounds.width(), bounds.height());
        croppedFace = Bitmap.createScaledBitmap(croppedFace, 112, 112, false);

        // Get the face embedding
        FaceClassifier.Recognition recognition = faceClassifier.recognizeImage(croppedFace, true);
        showRegisterDialogue(croppedFace, recognition);
    }

    public void showRegisterDialogue(Bitmap face, FaceClassifier.Recognition recognition) {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.register_face_dialogue);

        ImageView imageView1 = dialog.findViewById(R.id.dlg_image);
        EditText editText = dialog.findViewById(R.id.dlg_input);
        Button register = dialog.findViewById(R.id.button2);

        imageView1.setImageBitmap(face);

        register.setOnClickListener(view -> {
            String name = editText.getText().toString().trim();
            if (name.isEmpty()) {
                editText.setError("Enter Name");
            } else {
                faceClassifier.register(name, recognition);
                Toast.makeText(RegisterActivity.this, "Registered " + name, Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        detector.close();
    }
}
