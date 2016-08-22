package com.teinproductions.tein.gameoflife.files;

import android.content.res.Resources;

import com.teinproductions.tein.gameoflife.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class RLEPattern {
    private String name;
    private String filename;

    public RLEPattern() {
    }

    public RLEPattern(String name, String filename) {
        this.name = name;
        this.filename = filename;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    private static List<RLEPattern> list;

    public static List<RLEPattern> getList(Resources res) {
        if (list != null) return list;

        try {
            list = new ArrayList<>();
            BufferedReader reader = new BufferedReader(new InputStreamReader(res.openRawResource(R.raw.pattern_names)));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] strings = line.split(",");
                list.add(new RLEPattern(strings[1], strings[0]));
            }

            reader.close();
            return list;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
