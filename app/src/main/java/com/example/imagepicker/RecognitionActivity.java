package com.example.imagepicker;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.imagepicker.face_recognition.FaceClassifier;
import com.example.imagepicker.face_recognition.TFLiteFaceRecognition;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;

import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class RecognitionActivity extends AppCompatActivity {
    public static final int PERMISSION_CODE = 100;
    private static final String KEY_IMAGE_URI = "image_uri";
    ImageView imageView;
    CardView galleryCard,cameraCard;
    Uri image_uri;

    // High-accuracy landmark detection and face classification
    FaceDetectorOptions highAccuracyOpts =
            new FaceDetectorOptions.Builder()
                    .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
                    .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_NONE)
                    .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_NONE)
                    .build();

    FaceDetector detector;
    FaceClassifier faceClassifier;

    ActivityResultLauncher<Intent> galleryActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        image_uri = result.getData().getData();
                        Bitmap bitmap = getBitmapFromUri(image_uri);
                        if (bitmap != null) {
                            imageView.setImageBitmap(bitmap);
                            performFaceDetection(bitmap);
                        }
                    }
                }
            });

    ActivityResultLauncher<Intent> cameraActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        if (image_uri != null) {
                            Bitmap bitmap = getBitmapFromUri(image_uri);
                            if (bitmap != null) {
                                imageView.setImageBitmap(bitmap);
                                performFaceDetection(bitmap);
                            }
                        } else {
                            Toast.makeText(RecognitionActivity.this, "Error: Image URI is missing.", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recognition);

        if (savedInstanceState != null) {
            String uriString = savedInstanceState.getString(KEY_IMAGE_URI);
            if (uriString != null) {
                image_uri = Uri.parse(uriString);
            }
        }

        imageView = findViewById(R.id.imageView2);
        galleryCard = findViewById(R.id.gallerycard);
        cameraCard = findViewById(R.id.cameracard);

        galleryCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                galleryActivityResultLauncher.launch(galleryIntent);
            }
        });

        cameraCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    String[] permissionsToRequest;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        permissionsToRequest = new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_MEDIA_IMAGES};
                    } else {
                        permissionsToRequest = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
                    }

                    boolean allPermissionsGranted = true;
                    for (String permission : permissionsToRequest) {
                        if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                            allPermissionsGranted = false;
                            break;
                        }
                    }

                    if (allPermissionsGranted) {
                        openCamera();
                    } else {
                        requestPermissions(permissionsToRequest, 112);
                    }
                } else {
                    openCamera();
                }
            }
        });

        detector = FaceDetection.getClient(highAccuracyOpts);

        try {
            faceClassifier = TFLiteFaceRecognition.create(getAssets(), "mobile_face_net.tflite", 112, false);
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
        if (uri == null) {
            Toast.makeText(this, "Error processing image: uri is null", Toast.LENGTH_LONG).show();
            return null;
        }
        try {
            // First, decode with inJustDecodeBounds=true to check dimensions
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            InputStream imageStream = getContentResolver().openInputStream(uri);
            BitmapFactory.decodeStream(imageStream, null, options);
            if (imageStream != null) {
                imageStream.close();
            }

            // Calculate inSampleSize
            options.inSampleSize = calculateInSampleSize(options, 1024, 1024);

            // Decode bitmap with inSampleSize set
            options.inJustDecodeBounds = false;
            InputStream inputStream = getContentResolver().openInputStream(uri);
            Bitmap image = BitmapFactory.decodeStream(inputStream, null, options);
            if (inputStream != null) {
                inputStream.close();
            }

            if (image == null) {
                return null;
            }

            // Handle rotation
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                InputStream exifInputStream = getContentResolver().openInputStream(uri);
                if (exifInputStream != null) {
                    ExifInterface exifInterface = new ExifInterface(exifInputStream);
                    int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
                    exifInputStream.close();

                    Matrix matrix = new Matrix();
                    switch (orientation) {
                        case ExifInterface.ORIENTATION_ROTATE_90:
                            matrix.setRotate(90);
                            break;
                        case ExifInterface.ORIENTATION_ROTATE_180:
                            matrix.setRotate(180);
                            break;
                        case ExifInterface.ORIENTATION_ROTATE_270:
                            matrix.setRotate(270);
                            break;
                        default:
                            // No rotation needed
                            return image;
                    }
                    return Bitmap.createBitmap(image, 0, 0, image.getWidth(), image.getHeight(), matrix, true);
                }
            }
            return image;
        } catch (Throwable e) {
            e.printStackTrace();
            final String message = e.getMessage();
            runOnUiThread(() -> Toast.makeText(this, "Error processing image: " + message, Toast.LENGTH_LONG).show());
            Log.e("ImageProcessingError", "Error in getBitmapFromUri", e);
            return null;
        }
    }

    private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    Canvas canvas;

    public void performFaceDetection(Bitmap input) {
        Bitmap mutableBmp = input.copy(Bitmap.Config.ARGB_8888, true);
        canvas = new Canvas(mutableBmp);
        InputImage image = InputImage.fromBitmap(mutableBmp, 0);

        Task<List<Face>> result =
                detector.process(image)
                        .addOnSuccessListener(
                                new OnSuccessListener<List<Face>>() {
                                    @Override
                                    public void onSuccess(List<Face> faces) {
                                        if (faces.isEmpty()) {
                                            imageView.setImageBitmap(input);
                                            return;
                                        }

                                        for (Face face : faces) {
                                            Rect bounds = face.getBoundingBox();

                                            Paint p1 = new Paint();
                                            p1.setColor(Color.RED);
                                            p1.setStyle(Paint.Style.STROKE);
                                            p1.setStrokeWidth(5);

                                            // Pass a copy of the bounds to prevent modification of the original.
                                            performFaceRecognition(new Rect(bounds), mutableBmp);

                                            canvas.drawRect(bounds, p1);
                                        }

                                        imageView.setImageBitmap(mutableBmp);
                                    }
                                })
                        .addOnFailureListener(
                                new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        // Task failed with an exception
                                        // ...
                                    }
                                });
    }

    public void performFaceRecognition(Rect bounds, Bitmap input) {
        try {
            if (bounds.top < 0) {
                bounds.top = 0;
            }

            if (bounds.left < 0) {
                bounds.left = 0;
            }

            if (bounds.right > input.getWidth()) {
                bounds.right = input.getWidth();
            }

            if (bounds.bottom > input.getHeight()) {
                bounds.bottom = input.getHeight();
            }

            if (bounds.width() <= 0 || bounds.height() <= 0) {
                return;
            }

            Bitmap croppedFace = Bitmap.createBitmap(input, bounds.left, bounds.top, bounds.width(), bounds.height());

            croppedFace = Bitmap.createScaledBitmap(croppedFace, 112, 112, false);

            FaceClassifier.Recognition recognition = faceClassifier.recognizeImage(croppedFace,false);

            if (recognition != null && recognition.getTitle() != null && recognition.getDistance() < 1) {
                Paint p1 = new Paint();
                p1.setColor(Color.WHITE);
                p1.setTextSize(60);

                canvas.drawText(recognition.getTitle(), bounds.left, bounds.top, p1);
            }
        } catch (Throwable e) {
            e.printStackTrace();
            final String message = e.getMessage();
            runOnUiThread(() -> Toast.makeText(this, "Error during face recognition: " + message, Toast.LENGTH_LONG).show());
            Log.e("FaceRecognitionError", "Error in performFaceRecognition", e);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        detector.close();
    }
}