package com.teinproductions.tein.gameoflife.files;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class LifeOneOFiveInterpreter {

    public static Life parse(InputStream in) throws IOException {
        InputStreamReader isr = new InputStreamReader(in);
        BufferedReader buff = new BufferedReader(isr);

        StringBuilder file = new StringBuilder();
        String line;
        while ((line = buff.readLine()) != null) {
            file.append(line).append("\n");
        }

        return parse(file.toString());
    }

    public static Life parse(String text) {
        Life model = new Life();
        boolean readingCells = false;
        List<String> cellLines = new ArrayList<>();

        String[] lines = text.split("\n");
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            if (line.trim().isEmpty()) continue;

            if (readingCells) {
                cellLines.add(line);
                continue;
            }

            if ("#Life 1.05".equalsIgnoreCase(line) ||
                    line.startsWith("#R") || line.startsWith("#N") || line.startsWith("#r") || line.startsWith("#n")) {
                continue;
            }

            if (line.startsWith("#D") || line.startsWith("#d")) {
                model.addComment(line.substring(2, line.length()));
            } else if (line.startsWith("#P") || line.startsWith("#p")) {
                cellLines.add(line);
                readingCells = true;
            }
        }

        model.setCells(readCells(cellLines));
        model.setOriginalFile(text);
        return model;
    }

    private static ArrayList<short[]> readCells(List<String> cellLines) {
        ArrayList<short[]> cells = new ArrayList<>();

        int lastPIndex = -1;
        for (int i = 0; i < cellLines.size(); i++) {
            String line = cellLines.get(i);
            if (line.startsWith("#P") || line.startsWith("#p")) {
                if (lastPIndex != -1) {
                    cells.addAll(readCellBlock(cellLines.subList(lastPIndex, i)));
                }
                lastPIndex = i;
            }
        }

        cells.addAll(readCellBlock(cellLines.subList(lastPIndex, cellLines.size())));
        return cells;
    }

    private static List<short[]> readCellBlock(List<String> lines) {
        if (lines.size() == 0) {
            return new ArrayList<>();
        }
        if (!lines.get(0).startsWith("#P") || lines.get(0).startsWith("#p")) {
            throw new IllegalArgumentException("Cell block didn't start with a #P statement");
        }

        List<short[]> block = new ArrayList<>();

        String[] coordinates = lines.get(0).split(" ");
        // Value at 0th index is "#P"
        short pX = Short.parseShort(coordinates[1]);
        short pY = Short.parseShort(coordinates[2]);

        for (int i = 1; i < lines.size(); i++) {
            char[] chars = lines.get(i).toCharArray();
            for (int j = 0; j < chars.length; j++) {
                if (chars[j] == '*')
                    block.add(new short[]{(short) (pX + j), (short) (pY + i), 1, 0});
            }
        }

        return block;
    }
}
