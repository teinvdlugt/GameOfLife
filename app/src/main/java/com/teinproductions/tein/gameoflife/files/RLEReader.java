package com.teinproductions.tein.gameoflife.files;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class RLEReader {

    public static Life parse(String file) throws FileParseException {
        Life life = new Life();
        List<short[]> cells = new ArrayList<>();
        int width, height;

        // Split file into lines
        List<String> lines = new ArrayList<>();
        String[] linesArray = file.split("\n");
        for (String line : linesArray) {
            lines.add(line);
        }

        // Remove blank lines
        for (Iterator<String> iter = lines.iterator(); iter.hasNext(); ) {
            if (iter.next().trim().isEmpty())
                iter.remove();
        }

        // Remove comment lines TODO extract name from comment lines
        for (Iterator<String> iter = lines.iterator(); iter.hasNext(); ) {
            if (iter.next().trim().startsWith("#"))
                iter.remove();
        }

        // Width & height line
        String xyLine = lines.get(0).trim().replace(" ", "");
        int comma1 = xyLine.indexOf(",");
        width = Integer.parseInt(xyLine.substring(2, comma1));
        int comma2 = xyLine.indexOf(",", comma1 + 1);
        height = Integer.parseInt(xyLine.substring(comma1 + 3, comma2));
        lines.remove(0);

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
