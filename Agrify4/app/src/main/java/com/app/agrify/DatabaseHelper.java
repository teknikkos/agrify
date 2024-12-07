package com.app.agrify;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    // Database name and version
    private static final String DATABASE_NAME = "just1ncantiler0_Agri.fy";
    private static final int DATABASE_VERSION = 4;

    // Table and columns
    public static final String TABLE_NAME = "table_Field";
    public static final String COL_ID = "ID"; // Changed column name to _id
    public static final String COL_FIELD_NAME = "Name";
    public static final String COL_SOILTYPEID = "Soil_Type_ID";
    public static final String COL_CROP = "Crop_ID";
    public static final String COL_DATEPLANTED = "Date_Planted";
    public static final String COL_LATITUDE = "field_Latitude";
    public static final String COL_LONGTITUDE = "field_Longtitude";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create the products table
        String createTable = "CREATE TABLE " + TABLE_NAME + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_FIELD_NAME + " TEXT, " +
                COL_SOILTYPEID + " INTEGER, " +
                COL_CROP + " TEXT, " +
                COL_DATEPLANTED + " DATE, " +
                COL_LATITUDE + " DOUBLE, " +
                COL_LONGTITUDE + " DOUBLE)";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop the old table if it exists
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    // Insert a new product into the database
    public boolean insertData(String fieldName, String soilType, String crop, String datePlanted, String latitude, String longitude) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_FIELD_NAME, fieldName);
        contentValues.put(COL_SOILTYPEID, soilType);
        contentValues.put(COL_CROP, crop);
        contentValues.put(COL_DATEPLANTED, datePlanted);
        contentValues.put(COL_LATITUDE, latitude);
        contentValues.put(COL_LONGTITUDE, longitude);

        // Insert the new row
        long result = db.insert(TABLE_NAME, null, contentValues);
        return result != -1; // If result is -1, insertion failed
    }

    public Cursor getAllData() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT ID, Soil_Type_ID, Crop_ID, Date_Planted, field_Latitude, field_Longtitude FROM table_fields", null);
    }

    public boolean deleteData(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete("table_fields", "ID = ?", new String[]{String.valueOf(id)}) > 0;
    }

}