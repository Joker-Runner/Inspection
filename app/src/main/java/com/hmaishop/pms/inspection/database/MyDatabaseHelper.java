package com.hmaishop.pms.inspection.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.hmaishop.pms.inspection.util.Constants;

/**
 * Created by Joker_Runner on 7/22 0022.
 */
public class MyDatabaseHelper extends SQLiteOpenHelper {
    private Context mContext;

    public MyDatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
        mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(Constants.CREATE_LAT_LNG_TABLE);
        sqLiteDatabase.execSQL(Constants.CREATE_SUB_TASK_TABLE);
        sqLiteDatabase.execSQL(Constants.CREATE_TASK_TABLE);
        sqLiteDatabase.execSQL(Constants.CREATE_PHOTO_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
