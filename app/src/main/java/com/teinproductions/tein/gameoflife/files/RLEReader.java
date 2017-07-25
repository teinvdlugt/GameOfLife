package com.teinproductions.tein.gameoflife.files;


import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class RLEReader {

    public static Life parse(String file) throws FileParseException {
        Life life = new Life();
        ArrayList<short[]> cells = new ArrayList<>();

        // Split file into lines
        List<String> lines = new ArrayList<>();
        String[] linesArray = file.split("\n");
        Collections.addAll(lines, linesArray);

        // Remove blank lines
        for (Iterator<String> iter = lines.iterator(); iter.hasNext(); ) {
            if (iter.next().trim().isEmpty())
                iter.remove();
        }

        // Remove comment lines
        for (Iterator<String> iter = lines.iterator(); iter.hasNext(); ) {
            if (iter.next().trim().startsWith("#"))
                iter.remove();
        }

        // Width & height line
        try {
            String xyLine = lines.get(0).trim().replace(" ", "");
            String[] args = xyLine.split(",");
            int x = -1, y = -1;
            for (String s : args) {
                String[] keyValue = s.split("=");
                if ("x".equals(keyValue[0]) || "X".equals(keyValue[0]))
                    x = Integer.parseInt(keyValue[1]);
                else if ("y".equals(keyValue[0]) || "Y".equals(keyValue[0]))
                    y = Integer.parseInt(keyValue[1]);
            }
            if (x == -1 || y == -1) {
                throw new FileParseException();
            }
            lines.remove(0);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            throw new FileParseException();
        }

        // Now all that remains is the pattern itself, lets make one line of that.
        StringBuilder rle = new StringBuilder();
        for (String line : lines)
            rle.append(line.replace(" ", ""));

        // Let's parse the pattern itself
        short x = 0, y = 0;
        while (!rle.toString().startsWith("!") && rle.length() != 0) {
            int number = extractNumber(rle);
            char type = rle.charAt(0);
            rle.delete(0, 1);

            if (type == 'o') {
                for (int i = 0; i < number; i++) {
                    cells.add(new short[]{(short) (x + i), y, 1, 0});
                }
                x += number;
            } else if (type == 'b') {
                x += number;
            } else if (type == '$') {
                y += number;
                x = 0;
            }
        }

        life.setCells(cells);
        return life;
    }

    private static int extractNumber(StringBuilder sb) {
        int digits = 0;
        while (Character.isDigit(sb.charAt(digits))) {
            digits++;
        }
        if (digits == 0) {
            return 1;
        } else {
            int result = Integer.parseInt(sb.substring(0, digits));
            sb.delete(0, digits);
            return result;
        }
    }

    private static StringBuilder trim(StringBuilder sb) {
        return new StringBuilder(sb.toString().trim());
    }

    private static boolean startsWith(StringBuilder sb, String s) {
        return sb.toString().startsWith(s);
    }
}
