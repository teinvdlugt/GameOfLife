package com.teinproductions.tein.gameoflife.patterns;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;

import com.teinproductions.tein.gameoflife.R;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.FileOutputStream;
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
    public static final String BROADCAST_ACTION = "com.teinproductions.tein.gameoflife.INDEX_DOWNLOAD_DONE";

    public IndexDownloadIntentService() {
        super("name");
    }

    public IndexDownloadIntentService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        try {
            // Download the file index
            String file = downloadFile(BASE_URL);

            // Parse the file index
            Document doc = Jsoup.parse(file);

            // Get files names with .lif extension
            List<String> fileNames = parseSupportedFileNames(doc);

            // Save file names to disk
            saveFileNames(this, fileNames);

            // TODO Parse and save pattern names, descriptions etc.

            // Send broadcast that the job is done
            LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(BROADCAST_ACTION));
        } catch (IOException ignored) {
        }
    }

    private static List<String> parseSupportedFileNames(Document doc) {
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

    private void saveFileNames(Context context, List<String> fileNames) throws IOException {
        // Construct file contents
        StringBuilder sb = new StringBuilder();
        for (String str : fileNames) {
            sb.append(str).append("\n");
        }

        FileOutputStream fos = context.openFileOutput(FILE_NAMES_FILE, Context.MODE_PRIVATE);
        fos.write(sb.toString().getBytes());
        fos.close();
    }

    public static String downloadFile(String urlStr) throws IOException {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setReadTimeout(10000);
        conn.setConnectTimeout(20000);
        conn.setRequestMethod("GET");
        conn.setDoInput(true);
        conn.connect();
        int responseCode = conn.getResponseCode();

        if (responseCode < 200 || responseCode >= 400)
            throw new IOException("response code was " + responseCode);

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
}

class IndexDownloadDoneBroadcastReceiver extends BroadcastReceiver {
    private static final int NOTIFICATION_ID = 1;

    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setContentTitle("Indexing done")
                .setAutoCancel(true)
                .setSmallIcon(R.mipmap.ic_launcher);
        ((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE)).notify(NOTIFICATION_ID, builder.build());
    }
}