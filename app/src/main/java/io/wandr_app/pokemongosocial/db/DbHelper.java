package io.wandr_app.pokemongosocial.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by kylel on 7/16/2016.
 */
public class DbHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 4;
    public static final String DATABASE_NAME = "GoSocial.db";
    private static final String INTEGER_TYPE = " INTEGER";

    private static final String COMMA_SEP = ",";
    private static final String SQL_CREATE_ENTRIES_POST =
            "CREATE TABLE " + ThumbsContract.PostThumbsEntry.TABLE_NAME + " (" +
                    ThumbsContract.PostThumbsEntry._ID + " INTEGER PRIMARY KEY," +
                    ThumbsContract.PostThumbsEntry.COLUMN_NAME_POST_ID + INTEGER_TYPE + COMMA_SEP +
                    ThumbsContract.PostThumbsEntry.COLUMN_NAME_THUMBS_STATUS + INTEGER_TYPE +
                    " );";
    private static final String SQL_CREATE_ENTRIES_COMMENT =
            "CREATE TABLE " + ThumbsContract.CommentThumbsEntry.TABLE_NAME + " (" +
                    ThumbsContract.CommentThumbsEntry._ID + " INTEGER PRIMARY KEY," +
                    ThumbsContract.CommentThumbsEntry.COLUMN_NAME_COMMENT_ID + INTEGER_TYPE + COMMA_SEP +
                    ThumbsContract.CommentThumbsEntry.COLUMN_NAME_THUMBS_STATUS + INTEGER_TYPE +
                    " );";

    private static final String SQL_DELETE_ENTRIES_POST =
            "DROP TABLE IF EXISTS " + ThumbsContract.PostThumbsEntry.TABLE_NAME + ";";

    private static final String SQL_DELETE_ENTRIES_COMMENT =
            "DROP TABLE IF EXISTS " + ThumbsContract.CommentThumbsEntry.TABLE_NAME + ";";

    public DbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES_POST + SQL_CREATE_ENTRIES_COMMENT);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_ENTRIES_POST + SQL_DELETE_ENTRIES_COMMENT);
        onCreate(db);
    }
}
