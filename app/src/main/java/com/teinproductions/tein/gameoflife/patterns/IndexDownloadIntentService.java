package com.teinproductions.tein.gameoflife.patterns;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import com.teinproductions.tein.gameoflife.db.DefaultPatternContract;
import com.teinproductions.tein.gameoflife.db.DefaultPatternDbHelper;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


public class IndexDownloadIntentService extends IntentService {
    public static final String BASE_URL = "http://gameoflife.netau.net/patterns/";
    public static final String SUPPORTED_FILE_EXT = "105.lif";
    public static final String FILE_NAMES_FILE = "file_names";

    public static final String PROGRESS_BAR_MAX = "progressBarMax";
    public static final String PROGRESS_BAR_PROGRESS = "progressBarProgress";

    public IndexDownloadIntentService() {
        super("name");
    }

    public IndexDownloadIntentService(String name) {
        super(name);
    }

    private int progressBarMax;

    @SuppressWarnings("TryFinallyCanBeTryWithResources")
    @Override
    protected void onHandleIntent(Intent intent) {
        android.os.Debug.waitForDebugger();

        DefaultPatternDbHelper dbHelper = new DefaultPatternDbHelper(this);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        try {
            String file = downloadFile(BASE_URL);
            Document doc = Jsoup.parse(file);

            db.delete(DefaultPatternContract.DefaultPatternEntry.TABLE_NAME, null, null);

            List<String> fileNames = saveSupportedFiles(db, doc);

            progressBarMax = fileNames.size();

            savePatternNames(db, fileNames);
        } catch (IOException | SQLiteException e) {
            e.printStackTrace();
        } finally {
            db.close();
        }
    }

    /**
     * Parse the file names with supported extension, return a list of those file names
     * and save them to the passed database.
     *
     * @param db  Database to save the file names to.
     * @param doc Jsoup Document to parse the file names from
     * @return A {@code List<String>} containing the files with supported extension
     */
    private List<String> saveSupportedFiles(SQLiteDatabase db, Document doc) {
        Element ul = doc.getElementsByTag("ul").first();
        Elements lis = ul.children();
        lis.remove(0); // First entry is "Parent Directory"
        List<String> result = new ArrayList<>();

        ContentValues values = new ContentValues();

        for (Element li : lis) {
            String fileName = li.text().trim();
            if (fileName.endsWith(SUPPORTED_FILE_EXT)) {
                result.add(fileName);
                values.put(DefaultPatternContract.DefaultPatternEntry.COLUMN_NAME_URL, fileName);
                db.insert(DefaultPatternContract.DefaultPatternEntry.TABLE_NAME, null, values);
            }
        }

        return result;
    }

    @SuppressWarnings("TryFinallyCanBeTryWithResources")
    private void savePatternNames(SQLiteDatabase db, List<String> fileNames) throws IOException {
        // Create reusable objects instead of allocating many objects in for-loop
        Intent progressIntent = new Intent(this, IndexDownloadProgressBroadcastReceiver.class);
        progressIntent.putExtra(PROGRESS_BAR_MAX, progressBarMax);
        ContentValues values = new ContentValues();

        for (int i = 0; i < fileNames.size(); i++) {
            String fileName = fileNames.get(i);
            HttpURLConnection conn = establishConnection(BASE_URL + fileName);
            InputStream is = conn.getInputStream();
            try {
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
                                DefaultPatternContract.DefaultPatternEntry.COLUMN_NAME_URL + "=" + fileName, null);

                        // Show progress in notification
                        progressIntent.putExtra(PROGRESS_BAR_PROGRESS, i);
                        sendBroadcast(progressIntent);
                        break;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                conn.disconnect();
                is.close();
            }
        }
    }

    public static String downloadFile(String urlStr) throws IOException {
        HttpURLConnection conn = establishConnection(urlStr);

        InputStream is = conn.getInputStream();
        InputStreamReader reader = new InputStreamReader(is);
        BufferedReader bufferedReader = new BufferedReader(reader);

        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            sb.append(line);
        }

        is.close();
        conn.disconnect();
        return sb.toString();
    }

    private static HttpURLConnection establishConnection(String urlStr) throws IOException {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setReadTimeout(15000);
        conn.setConnectTimeout(20000);
        conn.setRequestMethod("GET");
        conn.setDoInput(true);
        conn.connect();

        int responseCode = conn.getResponseCode();
        if (responseCode < 200 || responseCode >= 400)
            throw new IOException("Response code was " + responseCode);

        return conn;
    }
}
