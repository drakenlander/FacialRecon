package com.example.imagepicker;

import android.Manifest;
import android.app.Activity;
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
import androidx.exifinterface.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.imagepicker.db.DBHelper;
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

public class RecognitionActivity extends AppCompatActivity {

    private static final String KEY_IMAGE_URI = "image_uri";
    private ImageView imageView;
    private Uri image_uri;
    private FaceDetector detector;
    private FaceClassifier faceClassifier;
    private DBHelper dbHelper;
    private Canvas canvas;

    private final ActivityResultLauncher<Intent> galleryActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    if (result.getData() != null) {
                        image_uri = result.getData().getData();
                        Bitmap bitmap = getBitmapFromUri(image_uri);
                        if (bitmap != null) {
                            imageView.setImageBitmap(bitmap);
                            performFaceDetection(bitmap);
                        }
                    }
                }
            });

    private final ActivityResultLauncher<Intent> cameraActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    if (image_uri != null) {
                        Bitmap bitmap = getBitmapFromUri(image_uri);
                        if (bitmap != null) {
                            imageView.setImageBitmap(bitmap);
                            performFaceDetection(bitmap);
                        }
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recognition);

        dbHelper = new DBHelper(this);

        if (savedInstanceState != null) {
            String uriString = savedInstanceState.getString(KEY_IMAGE_URI);
            if (uriString != null) {
                image_uri = Uri.parse(uriString);
            }
        }

        imageView = findViewById(R.id.imageView2);
        CardView galleryCard = findViewById(R.id.gallerycard);
        CardView cameraCard = findViewById(R.id.cameracard);

        galleryCard.setOnClickListener(view -> {
            Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            galleryActivityResultLauncher.launch(galleryIntent);
        });

        cameraCard.setOnClickListener(view -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED) {
                    String[] permission = {Manifest.permission.CAMERA};
                    requestPermissions(permission, 112);
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
            e.printStackTrace();
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

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (image_uri != null) {
            outState.putString(KEY_IMAGE_URI, image_uri.toString());
        }
    }

    private Bitmap getBitmapFromUri(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            if (inputStream != null) {
                inputStream.close();
            }
            return rotateBitmap(bitmap, uri);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private Bitmap rotateBitmap(Bitmap bitmap, Uri uri) throws IOException {
        InputStream inputStream = getContentResolver().openInputStream(uri);
        ExifInterface exifInterface = new ExifInterface(inputStream);
        int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
        Matrix matrix = new Matrix();
        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                matrix.postRotate(90);
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                matrix.postRotate(180);
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                matrix.postRotate(270);
                break;
            default:
                return bitmap;
        }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    public void performFaceDetection(Bitmap input) {
        Bitmap mutableBmp = input.copy(Bitmap.Config.ARGB_8888, true);
        canvas = new Canvas(mutableBmp);
        InputImage image = InputImage.fromBitmap(mutableBmp, 0);

        detector.process(image)
                .addOnSuccessListener(faces -> {
                    if (faces.isEmpty()) {
                        imageView.setImageBitmap(input);
                        return;
                    }
                    for (Face face : faces) {
                        Rect bounds = face.getBoundingBox();
                        performFaceRecognition(new Rect(bounds), mutableBmp);
                        Paint p1 = new Paint();
                        p1.setColor(Color.RED);
                        p1.setStyle(Paint.Style.STROKE);
                        p1.setStrokeWidth(5);
                        canvas.drawRect(bounds, p1);
                    }
                    imageView.setImageBitmap(mutableBmp);
                })
                .addOnFailureListener(e -> Toast.makeText(RecognitionActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    public void performFaceRecognition(Rect bound, Bitmap bmp) {
        if (bound.left < 0) bound.left = 0;
        if (bound.top < 0) bound.top = 0;
        if (bound.right > bmp.getWidth()) bound.right = bmp.getWidth() - 1;
        if (bound.bottom > bmp.getHeight()) bound.bottom = bmp.getHeight() - 1;

        Bitmap cropped = Bitmap.createBitmap(bmp, bound.left, bound.top, bound.width(), bound.height());
        cropped = Bitmap.createScaledBitmap(cropped, 112, 112, false);
        final FaceClassifier.Recognition recognition = faceClassifier.recognizeImage(cropped, true);

        Log.d("tryFR", "Recognition: " + recognition.getTitle() + ", " + recognition.getDistance());

        if (recognition != null && !"Unknown".equals(recognition.getTitle()) && recognition.getDistance() < 1) {
            dbHelper.insertAttempt(Integer.parseInt(recognition.getId()), recognition.getTitle());
            Paint p1 = new Paint();
            p1.setColor(Color.WHITE);
            p1.setTextSize(60);
            canvas.drawText(recognition.getTitle() + " (" + recognition.getDistance() + ")", bound.left, bound.top, p1);
            Toast.makeText(this, "Attempt logged for " + recognition.getTitle(), Toast.LENGTH_SHORT).show();
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Face Not Recognized");
            builder.setMessage("Please enter your name to log the attempt:");
            final EditText input = new EditText(this);
            builder.setView(input);
            builder.setPositiveButton("OK", (dialog, which) -> {
                String name = input.getText().toString();
                if (!name.isEmpty()) {
                    dbHelper.insertAttempt(null, name);
                    Toast.makeText(this, "Attempt logged for " + name, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Name cannot be empty.", Toast.LENGTH_SHORT).show();
                }
            });
            builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
            builder.show();
        }
    }
}
