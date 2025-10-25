package com.example.imagepicker.db;

import java.util.ArrayList;
import java.util.HashMap;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.imagepicker.face_recognition.FaceClassifier;

public class DBHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "MyFaces.db";
    public static final String FACE_TABLE_NAME = "faces";
    public static final String FACE_COLUMN_ID = "id";
    public static final String FACE_COLUMN_NAME = "name";
    public static final String FACE_COLUMN_EMBEDDING = "embedding";

    public static final String USERS_TABLE_NAME = "users";
    public static final String USERS_COLUMN_ID = "id";
    public static final String USERS_COLUMN_USERNAME = "username";
    public static final String USERS_COLUMN_PASSWORD = "password";



    public DBHelper(Context context) {
        super(context, DATABASE_NAME , null, 2);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
                "create table faces " +
                        "(id integer primary key, name text,embedding text)"
        );
        db.execSQL(
                "create table users " +
                        "(id integer primary key, username text, password text)"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS faces");
        db.execSQL("DROP TABLE IF EXISTS users");
        onCreate(db);
    }

    public boolean insertFace (String name, Object embedding) {
        float[][] floatList = (float[][]) embedding;
        String embeddingString = "";
        for(Float f: floatList[0]){
            embeddingString += f.toString()+",";
        }
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(FACE_COLUMN_NAME, name);
        contentValues.put(FACE_COLUMN_EMBEDDING, embeddingString);
        db.insert("faces", null, contentValues);
        return true;
    }

    public boolean insertUser(String username, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(USERS_COLUMN_USERNAME, username);
        contentValues.put(USERS_COLUMN_PASSWORD, password); // WARNING: Storing plain text password
        db.insert(USERS_TABLE_NAME, null, contentValues);
        return true;
    }

    public boolean checkUser(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from users where username = ? and password = ?", new String[]{username, password});
        boolean exists = res.getCount() > 0;
        res.close();
        return exists;
    }

    public Cursor getData(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from faces where id="+id+"", null );
        return res;
    }

    public int numberOfRows(){
        SQLiteDatabase db = this.getReadableDatabase();
        int numRows = (int) DatabaseUtils.queryNumEntries(db, FACE_TABLE_NAME);
        return numRows;
    }

    public boolean updateFace (Integer id, String name, String embedding) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(FACE_COLUMN_NAME, name);
        contentValues.put(FACE_COLUMN_EMBEDDING, embedding);
        db.update(FACE_TABLE_NAME, contentValues, "id = ? ", new String[] { Integer.toString(id) } );
        return true;
    }

    public Integer deleteFace (Integer id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(FACE_TABLE_NAME,
                "id = ? ",
                new String[] { Integer.toString(id) });
    }

    @SuppressLint("Range")
    public HashMap<String, FaceClassifier.Recognition> getAllFaces() {
        //hp = new HashMap();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from faces", null );
        res.moveToFirst();

        HashMap<String, FaceClassifier.Recognition> registered = new HashMap<>();
        while(res.isAfterLast() == false){
            String embeddingString = res.getString(res.getColumnIndex(FACE_COLUMN_EMBEDDING));
            String[] stringList = embeddingString.split(",");
            if (stringList.length == 0) {
                res.moveToNext();
                continue;
            }
            ArrayList<Float> embeddingFloat = new ArrayList<>();
            for (String s : stringList) {
                if(!s.isEmpty()) {
                    embeddingFloat.add(Float.parseFloat(s));
                }
            }
            float[][] bigArray = new float[1][embeddingFloat.size()];
            float[] floatArray = new float[embeddingFloat.size()];
            for (int i = 0; i < embeddingFloat.size(); i++) {
                floatArray[i] = embeddingFloat.get(i);
            }
            bigArray[0] = floatArray;
            FaceClassifier.Recognition recognition = new FaceClassifier.Recognition(res.getString(res.getColumnIndex(FACE_COLUMN_NAME)),bigArray);
            registered.putIfAbsent(recognition.getTitle(),recognition);
            res.moveToNext();
        }
        Log.d("tryRL","rl="+registered.size());
        return registered;
    }
}
