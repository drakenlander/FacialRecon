package com.example.imagepicker.face_recognition;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.RectF;
import android.util.Pair;

import com.example.imagepicker.db.DBHelper;

import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.Map;

public class TFLiteFaceRecognition implements FaceClassifier {

    private static final int OUTPUT_SIZE = 192;
    private static final float IMAGE_MEAN = 128.0f;
    private static final float IMAGE_STD = 128.0f;

    private boolean isModelQuantized;
    private int inputSize;
    private ByteBuffer imgData;
    private Interpreter tfLite;
    private final DBHelper dbHelper;
    private HashMap<String, Recognition> registered = new HashMap<>();

    public TFLiteFaceRecognition(Context ctx) {
        this.dbHelper = new DBHelper(ctx);
    }

    public void register(String name, Recognition rec) {
        dbHelper.insertFace(name, rec.getEmbeeding());
        loadRegisteredFaces();
    }

    private void loadRegisteredFaces() {
        registered = dbHelper.getAllFaces();
    }

    private static MappedByteBuffer loadModelFile(AssetManager assets, String modelFilename) throws IOException {
        AssetFileDescriptor fileDescriptor = assets.openFd(modelFilename);
        try (FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
             FileChannel fileChannel = inputStream.getChannel()) {
            long startOffset = fileDescriptor.getStartOffset();
            long declaredLength = fileDescriptor.getDeclaredLength();
            return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
        }
    }

    public static FaceClassifier create(final AssetManager assetManager, final String modelFilename, final int inputSize, final boolean isQuantized, Context ctx) throws IOException {
        final TFLiteFaceRecognition d = new TFLiteFaceRecognition(ctx);
        d.inputSize = inputSize;

        try {
            d.tfLite = new Interpreter(loadModelFile(assetManager, modelFilename));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        d.isModelQuantized = isQuantized;
        int numBytesPerChannel = isQuantized ? 1 : 4;
        d.imgData = ByteBuffer.allocateDirect(d.inputSize * d.inputSize * 3 * numBytesPerChannel);
        d.imgData.order(ByteOrder.nativeOrder());
        d.loadRegisteredFaces();
        return d;
    }

    private Pair<Recognition, Float> findNearest(float[] emb) {
        Pair<Recognition, Float> ret = null;
        for (Recognition recognition : registered.values()) {
            final float[] knownEmb = ((float[][]) recognition.getEmbeeding())[0];
            float distance = 0;
            for (int i = 0; i < emb.length; i++) {
                float diff = emb[i] - knownEmb[i];
                distance += diff * diff;
            }
            distance = (float) Math.sqrt(distance);
            if (ret == null || distance < ret.second) {
                ret = new Pair<>(recognition, distance);
            }
        }
        return ret;
    }

    @Override
    public Recognition recognizeImage(final Bitmap bitmap, boolean storeExtra) {
        imgData.rewind();
        int[] intValues = new int[inputSize * inputSize];
        bitmap.getPixels(intValues, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
        for (int i = 0; i < inputSize; ++i) {
            for (int j = 0; j < inputSize; ++j) {
                int pixelValue = intValues[i * inputSize + j];
                if (isModelQuantized) {
                    imgData.put((byte) ((pixelValue >> 16) & 0xFF));
                    imgData.put((byte) ((pixelValue >> 8) & 0xFF));
                    imgData.put((byte) (pixelValue & 0xFF));
                } else {
                    imgData.putFloat((((pixelValue >> 16) & 0xFF) - IMAGE_MEAN) / IMAGE_STD);
                    imgData.putFloat((((pixelValue >> 8) & 0xFF) - IMAGE_MEAN) / IMAGE_STD);
                    imgData.putFloat(((pixelValue & 0xFF) - IMAGE_MEAN) / IMAGE_STD);
                }
            }
        }

        Object[] inputArray = {imgData};
        Map<Integer, Object> outputMap = new HashMap<>();
        float[][] embeddings = new float[1][OUTPUT_SIZE];
        outputMap.put(0, embeddings);

        tfLite.runForMultipleInputsOutputs(inputArray, outputMap);

        Recognition result;

        if (!registered.isEmpty()) {
            final Pair<Recognition, Float> nearest = findNearest(embeddings[0]);
            if (nearest != null) {
                final Recognition nearestRecognition = nearest.first;
                final float distance = nearest.second;

                result = new Recognition(nearestRecognition.getId(), nearestRecognition.getTitle(), distance, nearestRecognition.getLocation());
            } else {
                result = new Recognition(null, "Unknown", -1f, new RectF());
            }
        } else {
            result = new Recognition(null, "Unknown", -1f, new RectF());
        }

        if (storeExtra) {
            result.setEmbeeding(embeddings);
        }

        return result;
    }
}
