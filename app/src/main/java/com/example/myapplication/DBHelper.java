package com.example.myapplication;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class DBHelper extends SQLiteOpenHelper {
    public DBHelper(@Nullable Context context) {
        super(context,"Userdata.db",null,1);
    }

    @Override
    public void onCreate(SQLiteDatabase DB) {
        DB.execSQL("create Table AuthToken(token TEXT primary key)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase DB, int i, int i1) {
        DB.execSQL("drop Table if exists AuthToken");
    }

    public Boolean savetoken(String token){
        SQLiteDatabase DB = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("token", token);

        long result = DB.insert("AuthToken",null,contentValues);

        if(result == 1){
            return false;
        }else{
            return true;
        }
    }

    public Boolean deleteToken(String token){
        SQLiteDatabase DB = this.getWritableDatabase();
        Cursor cursor = DB.rawQuery("Select * from AuthToken where token = ?", new String[]{token});

        if(cursor.getCount() > 0){
            long result = DB.delete("AuthToken", "token=?", new String[]{token});
            if(result == -1){
                return false;
            }else{
                return true;
            }
            }else{
            return false;
        }
    }

    public String getToken(){
        SQLiteDatabase DB = this.getWritableDatabase();
        Cursor cursor = DB.rawQuery("SELECT token FROM AuthToken", null);
        if(cursor.moveToLast())
        {
            return cursor.getString(0);
        }
        return "";
    }
}
