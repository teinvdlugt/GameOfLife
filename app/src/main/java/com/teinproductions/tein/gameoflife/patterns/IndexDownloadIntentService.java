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

    public IndexDownloadIntentService() {
        super("name");
    }

    public IndexDownloadIntentService(String name) {
        super(name);
    }

    @SuppressWarnings("TryFinallyCanBeTryWithResources")
    @Override
    protected void onHandleIntent(Intent intent) {
        android.os.Debug.waitForDebugger();

        DefaultPatternDbHelper dbHelper = new DefaultPatternDbHelper(this);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        try {
            String file = downloadFile(BASE_URL);
            Document doc = Jsoup.parse(file);

            List<String> fileNames = getSupportFiles(doc);

            db.delete(DefaultPatternContract.DefaultPatternEntry.TABLE_NAME, null, null);
            ContentValues values = new ContentValues();
            for (String fileName : fileNames) {
                values.put(DefaultPatternContract.DefaultPatternEntry.COLUMN_NAME_URL, fileName);
                db.insert(DefaultPatternContract.DefaultPatternEntry.TABLE_NAME, null, values);
            }
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
     * @param doc Jsoup Document to parse the file names from
     * @return A {@code List<String>} containing the files with supported extension
     */
    private List<String> getSupportFiles(Document doc) {
        Element ul = doc.getElementsByTag("ul").first();
        Elements lis = ul.children();
        lis.remove(0); // First entry is "Parent Directory"
        List<String> result = new ArrayList<>();

        for (Element li : lis) {
            String fileName = li.text().trim();
            if (fileName.endsWith(SUPPORTED_FILE_EXT)) {
                result.add(fileName);
            }
        }

        return result;
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

    public static HttpURLConnection establishConnection(String urlStr) throws IOException {
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
