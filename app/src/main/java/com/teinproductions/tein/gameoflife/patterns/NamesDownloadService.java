package com.teinproductions.tein.gameoflife.patterns;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import com.teinproductions.tein.gameoflife.db.DefaultPatternContract;
import com.teinproductions.tein.gameoflife.db.DefaultPatternDbHelper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

public class NamesDownloadService extends IntentService {
    public static final String PROGRESS_BAR_MAX = "progressBarMax";
    public static final String PROGRESS_BAR_PROGRESS = "progressBarProgress";

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public NamesDownloadService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        android.os.Debug.waitForDebugger();

        DefaultPatternDbHelper dbHelper = new DefaultPatternDbHelper(this);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor c = null;
        List<String> fileNames = new ArrayList<>();

        try {
            c = db.query(DefaultPatternContract.DefaultPatternEntry.TABLE_NAME,
                    new String[]{DefaultPatternContract.DefaultPatternEntry.COLUMN_NAME_URL},
                    DefaultPatternContract.DefaultPatternEntry.COLUMN_NAME_NAME + " IS NULL", null, null, null, null);
            c.moveToFirst();
            int urlIndex = c.getColumnIndex(DefaultPatternContract.DefaultPatternEntry.COLUMN_NAME_URL);
            do {
                fileNames.add(c.getString(urlIndex));
            } while (c.moveToNext());
        } catch (CursorIndexOutOfBoundsException | SQLiteException e) {
            e.printStackTrace();
        } finally {
            if (c != null) c.close();
        }

        int progressBarMax = fileNames.size();

        Intent progressIntent = new Intent(this, IndexDownloadProgressBroadcastReceiver.class);
        progressIntent.putExtra(PROGRESS_BAR_MAX, progressBarMax);
        ContentValues values = new ContentValues();

        for (int i = 0; i < fileNames.size(); i++) {
            String fileName = fileNames.get(i);
            try {
                HttpURLConnection conn = IndexDownloadIntentService.establishConnection(IndexDownloadIntentService.BASE_URL + fileName);
                InputStream is = conn.getInputStream();
                BufferedReader buff = new BufferedReader(new InputStreamReader(is));

                String line;
                while ((line = buff.readLine()) != null) {
                    int index = line.indexOf("#D Name:");
                    if (index != -1) {
                        // Update database
                        String patternName = line.substring(index + 9);
                        values.put(DefaultPatternContract.DefaultPatternEntry.COLUMN_NAME_URL, fileName);
                        values.put(DefaultPatternContract.DefaultPatternEntry.COLUMN_NAME_NAME, patternName);
                        db.update(DefaultPatternContract.DefaultPatternEntry.TABLE_NAME, values,
                                DefaultPatternContract.DefaultPatternEntry.COLUMN_NAME_URL + "=?", new String[]{fileName});

                        // Show progress in notification
                        progressIntent.putExtra(PROGRESS_BAR_PROGRESS, i);
                        sendBroadcast(progressIntent);
                        break;
                    }
                }

                conn.disconnect();
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        db.close();
    }
}
