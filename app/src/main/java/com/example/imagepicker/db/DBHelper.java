package com.example.imagepicker.db;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.imagepicker.Attempt;
import com.example.imagepicker.face_recognition.FaceClassifier;

public class DBHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "MyFaces.db";
    private static final int DATABASE_VERSION = 6;

    public static final String FACE_TABLE_NAME = "faces";
    public static final String FACE_COLUMN_ID = "id";
    public static final String FACE_COLUMN_NAME = "name";
    public static final String FACE_COLUMN_EMBEDDING = "embedding";

    public static final String USERS_TABLE_NAME = "users";
    public static final String USERS_COLUMN_ID = "id";
    public static final String USERS_COLUMN_USERNAME = "username";
    public static final String USERS_COLUMN_PASSWORD = "password";
    public static final String USERS_COLUMN_SECURITY_QUESTION = "security_question";
    public static final String USERS_COLUMN_SECURITY_ANSWER = "security_answer";

    public static final String ATTEMPTS_TABLE_NAME = "attempts";
    public static final String ATTEMPTS_COLUMN_ID = "id";
    public static final String ATTEMPTS_COLUMN_PERSON_ID = "person_id";
    public static final String ATTEMPTS_COLUMN_PERSON_NAME = "person_name";
    public static final String ATTEMPTS_COLUMN_TIMESTAMP = "timestamp";


    public DBHelper(Context context) {
        super(context, DATABASE_NAME , null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
                "CREATE TABLE " + FACE_TABLE_NAME + " (" +
                        FACE_COLUMN_ID + " INTEGER PRIMARY KEY, " +
                        FACE_COLUMN_NAME + " TEXT, " +
                        FACE_COLUMN_EMBEDDING + " TEXT)"
        );
        db.execSQL(
                "CREATE TABLE " + USERS_TABLE_NAME + " (" +
                        USERS_COLUMN_ID + " INTEGER PRIMARY KEY, " +
                        USERS_COLUMN_USERNAME + " TEXT, " +
                        USERS_COLUMN_PASSWORD + " TEXT, " +
                        USERS_COLUMN_SECURITY_QUESTION + " TEXT, " +
                        USERS_COLUMN_SECURITY_ANSWER + " TEXT)"
        );
        db.execSQL(
                "CREATE TABLE " + ATTEMPTS_TABLE_NAME + " (" +
                        ATTEMPTS_COLUMN_ID + " INTEGER PRIMARY KEY, " +
                        ATTEMPTS_COLUMN_PERSON_ID + " INTEGER, " +
                        ATTEMPTS_COLUMN_PERSON_NAME + " TEXT, " +
                        ATTEMPTS_COLUMN_TIMESTAMP + " TEXT, " +
                        "FOREIGN KEY(" + ATTEMPTS_COLUMN_PERSON_ID + ") REFERENCES " + FACE_TABLE_NAME + "(" + FACE_COLUMN_ID + "))"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + FACE_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + USERS_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + ATTEMPTS_TABLE_NAME);
        onCreate(db);
    }

    public boolean insertFace (String name, Object embedding) {
        float[][] floatList = (float[][]) embedding;
        StringBuilder embeddingString = new StringBuilder();
        for(Float f: floatList[0]){
            embeddingString.append(f.toString()).append(",");
        }
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(FACE_COLUMN_NAME, name);
        contentValues.put(FACE_COLUMN_EMBEDDING, embeddingString.toString());
        db.insert(FACE_TABLE_NAME, null, contentValues);
        return true;
    }

    public boolean insertUser(String username, String password, String securityQuestion, String securityAnswer) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(USERS_COLUMN_USERNAME, username);
        contentValues.put(USERS_COLUMN_PASSWORD, password); // WARNING: Storing plain text password
        contentValues.put(USERS_COLUMN_SECURITY_QUESTION, securityQuestion);
        contentValues.put(USERS_COLUMN_SECURITY_ANSWER, securityAnswer);
        db.insert(USERS_TABLE_NAME, null, contentValues);
        return true;
    }

    public void insertAttempt(Integer personId, String personName) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

        if (personId != null) {
            contentValues.put(ATTEMPTS_COLUMN_PERSON_ID, personId);
        }
        contentValues.put(ATTEMPTS_COLUMN_PERSON_NAME, personName);
        contentValues.put(ATTEMPTS_COLUMN_TIMESTAMP, timestamp);
        db.insert(ATTEMPTS_TABLE_NAME, null, contentValues);
    }

    public boolean checkUser(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from users where username = ? and password = ?", new String[]{username, password});
        boolean exists = res.getCount() > 0;
        res.close();
        return exists;
    }

    public boolean checkSecurityAnswer(String username, String securityQuestion, String securityAnswer) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from users where username = ? and security_question = ? and security_answer = ?", new String[]{username, securityQuestion, securityAnswer});
        boolean exists = res.getCount() > 0;
        res.close();
        return exists;
    }

    public void updatePassword(String username, String newPassword) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(USERS_COLUMN_PASSWORD, newPassword);
        db.update(USERS_TABLE_NAME, contentValues, "username = ? ", new String[] { username } );
    }

    @SuppressLint("Range")
    public List<FaceClassifier.Recognition> getAllFacesAsList() {
        List<FaceClassifier.Recognition> faces = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("select * from " + FACE_TABLE_NAME, null);
        res.moveToFirst();

        while (!res.isAfterLast()) {
            String id = res.getString(res.getColumnIndex(FACE_COLUMN_ID));
            String name = res.getString(res.getColumnIndex(FACE_COLUMN_NAME));
            faces.add(new FaceClassifier.Recognition(id, name, -1f, null));
            res.moveToNext();
        }
        res.close();
        return faces;
    }

    @SuppressLint("Range")
    public List<Attempt> getAllAttempts() {
        List<Attempt> attempts = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("select * from " + ATTEMPTS_TABLE_NAME, null);
        res.moveToFirst();

        while (!res.isAfterLast()) {
            int id = res.getInt(res.getColumnIndex(ATTEMPTS_COLUMN_ID));
            Integer personId = res.isNull(res.getColumnIndex(ATTEMPTS_COLUMN_PERSON_ID)) ? null : res.getInt(res.getColumnIndex(ATTEMPTS_COLUMN_PERSON_ID));
            String personName = res.getString(res.getColumnIndex(ATTEMPTS_COLUMN_PERSON_NAME));
            String timestamp = res.getString(res.getColumnIndex(ATTEMPTS_COLUMN_TIMESTAMP));
            attempts.add(new Attempt(id, personId, personName, timestamp));
            res.moveToNext();
        }
        res.close();
        return attempts;
    }

    @SuppressLint("Range")
    public HashMap<String, FaceClassifier.Recognition> getAllFaces() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from " + FACE_TABLE_NAME, null );
        res.moveToFirst();

        HashMap<String, FaceClassifier.Recognition> registered = new HashMap<>();
        while(!res.isAfterLast()){
            String id = res.getString(res.getColumnIndex(FACE_COLUMN_ID));
            String name = res.getString(res.getColumnIndex(FACE_COLUMN_NAME));
            String embeddingString = res.getString(res.getColumnIndex(FACE_COLUMN_EMBEDDING));
            String[] stringList = embeddingString.split(",");
            if (stringList.length == 0) {
                res.moveToNext();
                continue;
            }
            float[][] embeedings = new float[1][stringList.length];
            for (int i = 0; i < stringList.length; i++) {
                if(!stringList[i].isEmpty()) {
                    embeedings[0][i] = Float.parseFloat(stringList[i]);
                }
            }

            FaceClassifier.Recognition recognition = new FaceClassifier.Recognition(id, name, -1f, null);
            recognition.setEmbeeding(embeedings);
            registered.put(name, recognition);
            res.moveToNext();
        }
        return registered;
    }
}
