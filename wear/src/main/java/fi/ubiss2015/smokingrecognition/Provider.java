package fi.ubiss2015.smokingrecognition;

/**
 * Created by JuanCamilo on 6/12/2015.
 */

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.os.Environment;
import android.provider.BaseColumns;
import android.util.Log;

import com.aware.Aware;
import com.aware.utils.DatabaseHelper;

import java.util.HashMap;

public class Provider extends ContentProvider {

    public static final int DATABASE_VERSION = 1;

    /**
     * Provider authority: com.aware.plugin.ambient_noise.provider.ambient_noise
     */
    public static String AUTHORITY = "fi.ubiss2015.smokingrecognition.provider.smoking";

    public static final int S_DATA = 1;
    public static final int S_DATA_ID = 2;


    public static final String DATABASE_NAME = Environment.getExternalStorageDirectory() + "/Smoking/classification.db";

    public static final String[] DATABASE_TABLES = {
            "classification"
    };

    public static final String[] TABLES_FIELDS = {
            Smoking_Data._ID + " integer primary key autoincrement," +
                    Smoking_Data.TIMESTAMP + " real default -1," +
                    Smoking_Data.CLASSIFICATION + " integer default -1," +
                    Smoking_Data.PROBABILITY + " real default -1)"
    };

    public static final class Smoking_Data implements BaseColumns {
        private Smoking_Data(){};

        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/classification");
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.ubiss2015.smokingrecognition.smoking";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.ubiss2015.smokingrecognition.smoking";

        public static final String _ID = "_id";
        public static final String TIMESTAMP = "timestamp";
        public static final String CLASSIFICATION = "classification";
        public static final String PROBABILITY = "probability";
    }

    private static UriMatcher URIMatcher;
    private static HashMap<String, String> databaseMap;
    private static DatabaseHelper databaseHelper;
    private static SQLiteDatabase database;

    @Override
    public boolean onCreate() {

        AUTHORITY = getContext().getPackageName() + ".provider.general_data";

        URIMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        URIMatcher.addURI(AUTHORITY, DATABASE_TABLES[0], S_DATA);
        URIMatcher.addURI(AUTHORITY, DATABASE_TABLES[0]+"/#", S_DATA_ID);

        databaseMap = new HashMap<String, String>();
        databaseMap.put(Smoking_Data._ID, Smoking_Data._ID);
        databaseMap.put(Smoking_Data.TIMESTAMP, Smoking_Data.TIMESTAMP);
        databaseMap.put(Smoking_Data.CLASSIFICATION, Smoking_Data.CLASSIFICATION);
        databaseMap.put(Smoking_Data.PROBABILITY, Smoking_Data.PROBABILITY);

        return true;
    }

    private boolean initializeDB() {
        if (databaseHelper == null) {
            databaseHelper = new DatabaseHelper( getContext(), DATABASE_NAME, null, DATABASE_VERSION, DATABASE_TABLES, TABLES_FIELDS );
        }
        if( databaseHelper != null && ( database == null || ! database.isOpen() )) {
            database = databaseHelper.getWritableDatabase();
        }
        return( database != null && databaseHelper != null);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        if( ! initializeDB() ) {
            Log.w(AUTHORITY, "Database unavailable...");
            return 0;
        }

        int count = 0;
        switch (URIMatcher.match(uri)) {
            case S_DATA:
                count = database.delete(DATABASE_TABLES[0], selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public String getType(Uri uri) {
        switch (URIMatcher.match(uri)) {
            case S_DATA:
                return Smoking_Data.CONTENT_TYPE;
            case S_DATA_ID:
                return Smoking_Data.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues initialValues) {
        if( ! initializeDB() ) {
            Log.w(AUTHORITY, "Database unavailable...");
            return null;
        }

        ContentValues values = (initialValues != null) ? new ContentValues(
                initialValues) : new ContentValues();

        switch (URIMatcher.match(uri)) {
            case S_DATA:
                long data_id = database.insert(DATABASE_TABLES[0], "-1", values);

                if (data_id > 0) {
                    Uri new_uri = ContentUris.withAppendedId(
                            Smoking_Data.CONTENT_URI,
                            data_id);
                    getContext().getContentResolver().notifyChange(new_uri,
                            null);
                    return new_uri;
                }
                throw new SQLException("Failed to insert row into " + uri);
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        if( ! initializeDB() ) {
            Log.w(AUTHORITY, "Database unavailable...");
            return null;
        }

        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        switch (URIMatcher.match(uri)) {
            case S_DATA:
                qb.setTables(DATABASE_TABLES[0]);
                qb.setProjectionMap(databaseMap);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        try {
            Cursor c = qb.query(database, projection, selection, selectionArgs,
                    null, null, sortOrder);
            c.setNotificationUri(getContext().getContentResolver(), uri);
            return c;
        } catch (IllegalStateException e) {
            if (Aware.DEBUG)
                Log.e(Aware.TAG, e.getMessage());

            return null;
        }
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        if( ! initializeDB() ) {
            Log.w(AUTHORITY, "Database unavailable...");
            return 0;
        }

        int count = 0;
        switch (URIMatcher.match(uri)) {
            case S_DATA:
                count = database.update(DATABASE_TABLES[0], values, selection,
                        selectionArgs);
                break;
            default:

                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }
}