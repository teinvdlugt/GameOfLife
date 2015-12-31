package com.teinproductions.tein.gameoflife.patterns;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

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

    public static final String PROGRESS_BAR_MAX = "progressBarMax";
    public static final String PROGRESS_BAR_PROGRESS = "progressBarProgress";

    public IndexDownloadIntentService() {
        super("name");
    }

    public IndexDownloadIntentService(String name) {
        super(name);
    }

    private int progressBarMax;

    @Override
    protected void onHandleIntent(Intent intent) {
        try {
            // android.os.Debug.waitForDebugger();

            // Download the file index
            String file = downloadFile(BASE_URL);

            // Parse the file index
            Document doc = Jsoup.parse(file);

            // Get file names of files with .lif extension
            List<String> fileNames = parseSupportedFileNames(doc);

            progressBarMax = fileNames.size();

            // Get the names of the patterns
            List<String> patternNames = parsePatternNames(fileNames);

            // Save file names to disk
            savePatterns(this, fileNames, patternNames);

            // Send broadcast that the job is done
            LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(BROADCAST_ACTION));
        } catch (IOException e) {
            e.printStackTrace();
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

    private List<String> parsePatternNames(List<String> fileNames) throws IOException {
        List<String> patternNames = new ArrayList<>();

        Intent progressIntent = new Intent(this, IndexDownloadProgressBroadcastReceiver.class);
        progressIntent.putExtra(PROGRESS_BAR_MAX, progressBarMax);

        for (int i = 0; i < fileNames.size(); i++) {
            String fileName = fileNames.get(i);
            try {
                HttpURLConnection conn = establishConnection(BASE_URL + fileName);
                InputStream is = conn.getInputStream();
                BufferedReader buff = new BufferedReader(new InputStreamReader(is));

                String line;
                while ((line = buff.readLine()) != null) {
                    int index = line.indexOf("#D Name:");
                    if (index != -1) {
                        patternNames.add(line.substring(index + 9));
                        progressIntent.putExtra(PROGRESS_BAR_PROGRESS, i);
                        sendBroadcast(progressIntent);
                        Log.d("1984", "parsePatternNames: " + line);
                        break;
                    }
                }

                conn.disconnect();
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
                patternNames.add(fileName);
            }
        }

        return patternNames;
    }

    private void savePatterns(Context context, List<String> fileNames, List<String> patternNames) throws IOException {
        // Construct file contents
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < fileNames.size(); i++) {
            sb.append(fileNames.get(i)).append(",").append(patternNames.get(i)).append("\n");
        }

        FileOutputStream fos = context.openFileOutput(FILE_NAMES_FILE, Context.MODE_PRIVATE);
        fos.write(sb.toString().getBytes());
        fos.close();
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
            throw new IOException("response code was " + responseCode);

        return conn;
    }
}
