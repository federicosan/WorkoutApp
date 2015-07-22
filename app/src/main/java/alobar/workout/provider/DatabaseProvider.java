package alobar.workout.provider;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import java.sql.SQLClientInfoException;

/**
 * Created by rob on 20/07/15.
 */
public class DatabaseProvider extends ContentProvider {

    private final static int ROUTE_EXERCISE_DIR = 1;

    private static final String TYPE_EXERCISE_DIR = ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.alobarproductions.exercise";

    private static final UriMatcher uriMatcher;
    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(DatabaseContract.AUTHORITY, DatabaseContract.Exercise.ENTITY_NAME, ROUTE_EXERCISE_DIR);
    }

    private DatabaseHelper mOpenHelper;
    private SQLiteDatabase db;

    @Override
    public boolean onCreate() {
        mOpenHelper = new DatabaseHelper(getContext());
        return true;
    }

    @Override
    public String getType(Uri uri) {
        switch (uriMatcher.match(uri)) {
            case ROUTE_EXERCISE_DIR:
                return TYPE_EXERCISE_DIR;
            default:
                throw new UnsupportedOperationException();
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor result;
        switch (uriMatcher.match(uri)) {
            case ROUTE_EXERCISE_DIR:
                result = Exercise.query(mOpenHelper.getReadableDatabase(), projection, selection, selectionArgs, sortOrder);
                break;
            default:
                throw new UnsupportedOperationException();
        }
        result.setNotificationUri(getContext().getContentResolver(), uri);
        return result;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        Uri result;
        switch (uriMatcher.match(uri)) {
            case ROUTE_EXERCISE_DIR:
                result = Exercise.insert(mOpenHelper.getWritableDatabase(), values);
                break;
            default:
                throw new UnsupportedOperationException();
        }
        getContext().getContentResolver().notifyChange(result, null);
        return result;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int affected;
        switch (uriMatcher.match(uri)) {
            case ROUTE_EXERCISE_DIR:
                affected = Exercise.delete(mOpenHelper.getWritableDatabase(), selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException();
        }
        if (affected > 0)
            getContext().getContentResolver().notifyChange(uri, null);
        return affected;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        switch (uriMatcher.match(uri)) {
            case ROUTE_EXERCISE_DIR:
                return 0;
            default:
                throw new UnsupportedOperationException();
        }
    }

    private static class Exercise {
        public static Cursor query(SQLiteDatabase db, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
            return db.query(DatabaseContract.Exercise.ENTITY_NAME, projection, selection, selectionArgs, null, null, sortOrder);
        }

        public static Uri insert(SQLiteDatabase db, ContentValues values) {
            long id = db.insert(DatabaseContract.Exercise.ENTITY_NAME, null, values);
            return id != -1 ? Uri.withAppendedPath(DatabaseContract.Exercise.CONTENT_URI, Long.toString(id)) : null;
        }

        public static int delete(SQLiteDatabase db, String whereClause, String[] whereArgs) {
            return db.delete(DatabaseContract.Exercise.ENTITY_NAME, whereClause, whereArgs);
        }
    }
}