package alobar.workout.db;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.apache.commons.lang3.Validate;

import java.util.ArrayList;
import java.util.List;

import alobar.workout.data.Exercise;

import static dagger.internal.Preconditions.checkNotNull;

/**
 * Repository for {@link Exercise} entities
 */
public class ExerciseRepo {

    private final SQLiteDatabase db;
    private final ContentResolver resolver;

    public ExerciseRepo(SQLiteDatabase db, ContentResolver resolver) {
        this.db = db;
        this.resolver = resolver;
    }

    public List<Exercise> all() {
        try (Cursor cursor = db.rawQuery("select * from " + DatabaseContract.Exercise.tableName, null)) {
            final int idIndex = cursor.getColumnIndexOrThrow(DatabaseContract.Exercise._ID);
            final int nameIndex = cursor.getColumnIndexOrThrow(DatabaseContract.Exercise.NAME);
            final int weightIndex = cursor.getColumnIndexOrThrow(DatabaseContract.Exercise.WEIGHT);
            List<Exercise> result = new ArrayList<>(cursor.getCount());
            while (cursor.moveToNext()) {
                result.add(new Exercise(
                        cursor.getLong(idIndex),
                        cursor.getString(nameIndex),
                        cursor.getDouble(weightIndex)
                ));
            }
            return result;
        }
    }

    public Exercise findById(long id) {
        final String query = "select * from " + DatabaseContract.Exercise.tableName +
                " where " + DatabaseContract.Exercise._ID + " = ?";
        try (Cursor cursor = db.rawQuery(query, new String[]{Long.toString(id)})) {
            if (cursor.moveToNext())
                return new Exercise(
                        cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseContract.Exercise._ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.Exercise.NAME)),
                        cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseContract.Exercise.WEIGHT))
                );
            else return null;
        }
    }

    public void save(Exercise exercise) {
        checkNotNull(exercise);
        ContentValues values = new ContentValues();
        values.put(DatabaseContract.Exercise.NAME, exercise.name);
        values.put(DatabaseContract.Exercise.WEIGHT, exercise.weight);
        long _id = exercise._id;
        if (exercise._id == 0) {
            _id = db.insert(DatabaseContract.Exercise.tableName, null, values);
            Validate.isTrue(_id != 0, "Exercise insert failed");
        } else {
            int affected = db.update(DatabaseContract.Exercise.tableName, values, DatabaseContract.Exercise._ID + " = ?", new String[]{Long.toString(exercise._id)});
            Validate.isTrue(affected == 1, "Exercise update failed");
        }
        resolver.notifyChange(DatabaseContract.Exercise.uri(_id), null);
    }

    public void deleteById(long _id) {
        Validate.isTrue(_id != 0, "Cannot delete exercise by id zero");
        int affected = db.delete(DatabaseContract.Exercise.tableName, DatabaseContract.Exercise._ID + " = ?", new String[]{Long.toString(_id)});
        Validate.isTrue(affected == 1, "Exercise delete failed");
        resolver.notifyChange(DatabaseContract.Exercise.uri(_id), null);
    }
}
