package com.teinproductions.tein.gameoflife.files;


import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

public class RLEEncoder {

    public static String constructFile(Life info, List<short[]> cells) {
        String infoStr = writeInfo(info);
        String pattern = encode(cells);
        return infoStr + pattern;
    }

    /**
     * @param info Life object containing at least a NonNull value in String field name.
     * @return Concatenation of the name, creator and comments in 'info' and the current timestamp,
     * to put in the beginning of an RLE file. Returned String has a newline at the end.
     */
    static String writeInfo(Life info) {
        StringBuilder sb = new StringBuilder();

        // Clear the name and creator of newlines (just to be sure)
        String name = info.getName().replace("\n", " ");
        String creator = null;
        if (info.getCreator() != null) creator = info.getCreator().replace("\n", " ");

        // Write the name on the first line
        sb.append("#N ").append(name).append("\n");

        // Write down the creator and timestamp
        sb.append("#O ").append(creator == null ? "" : creator + ", ")
                .append(DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG)
                        .format(Calendar.getInstance().getTime()))
                .append("\n");

        // Write down the comments
        for (String comment : info.getComments())
            sb.append("#C ").append(comment).append("\n");

        return sb.toString();
    }

    static String encode(List<short[]> c) { // TODO this code can be much optimised by combining for-loops and stuff
        if (c.isEmpty()) {
            return "x = 0, y = 0\n";
        }

        // We're going to modify the cells array, so let's make a copy
        List<short[]> cells = copyCells(c);

        // The string to return in the end:
        StringBuilder sb = new StringBuilder();

        // Clear the Life object from any non-living cells,
        // and find the min and max of x and y.
        short minX = Short.MAX_VALUE, maxX = Short.MIN_VALUE, minY = Short.MAX_VALUE, maxY = Short.MIN_VALUE;
        for (Iterator<short[]> iter = cells.iterator(); iter.hasNext(); ) {
            short[] cell = iter.next();
            if (cell[2] == 0) {
                // Cell is not alive
                iter.remove();
            } else {
                if (cell[0] < minX) minX = cell[0];
                if (cell[0] > maxX) maxX = cell[0];
                if (cell[1] < minY) minY = cell[1];
                if (cell[1] > maxY) maxY = cell[1];
            }
        }

        // Find width and height of the pattern for the first line in the encoding
        int width = maxX - minX + 1;
        int height = maxY - minY + 1;
        sb.append("x = ").append(width).append(", y = ").append(height).append("\n");

        // The cells in the Life array aren't ordered by position.
        sortCellsByPosition(cells);

        // The upper-righthand corner of the pattern must have coordinate (0,0);
        // set the cell positions to agree to that.
        for (short[] cell : cells) {
            cell[0] = (short) (cell[0] - minX);
            cell[1] = (short) (cell[1] - minY);
        }

        // Let's encode!
        int currentX = -1, currentY = 0;
        int counter = 0; // temp counter for number of adjacent cells
        for (short[] cell : cells) {
            // Stuff that deals with Y
            if (cell[1] > currentY) {
                // Move to a new line
                // Write down the alive cells
                if (counter != 0)
                    sb.append(counter == 1 ? "" : counter).append("o");
                // Write down new-line-characters
                for (int i = 0; i < cell[1] - currentY; i++)
                    sb.append("$");
                // Start a new line
                currentX = -1;
                counter = 0;
                currentY = cell[1];
            }

            // Stuff that deals with X
            if (cell[0] == currentX + 1) {
                // These cells are adjacent, so increment the counter
                counter++;
                currentX = cell[0];
            } else {
                // There is a gap of dead cells between this alive cell and the previous alive cell
                // Write down the previous sequence of adjacent alive cells, and the gap of dead cells
                // (b = dead cell, o = alive cell)
                if (counter != 0)
                    sb.append(counter == 1 ? "" : counter).append("o");
                int numOfDeadCells = cell[0] - currentX - 1;
                sb.append(numOfDeadCells == 1 ? "" : numOfDeadCells).append("b");

                // Start a new sequence of adjacent alive cells
                counter = 1;
                currentX = cell[0];
            }
        }

        // And write down the last sequence of adjacent alive cells.
        if (counter != 0)
            sb.append(counter == 1 ? "" : counter).append("o");

        // Concluding character and newline
        sb.append("!\n");
        return sb.toString();
    }

    private static ArrayList<short[]> copyCells(List<short[]> cells) {
        ArrayList<short[]> result = new ArrayList<>();
        for (short[] cell : cells) {
            result.add(new short[]{cell[0], cell[1], cell[2], cell[3]});
        }
        return result;
    }

    private static void sortCellsByPosition(List<short[]> cells) {
        // Bubble sort algorithm
        for (int i = cells.size() - 1; i > 1; i--) {
            for (int j = 0; j < i; j++) {
                int x1 = cells.get(j)[0], y1 = cells.get(j)[1];
                int x2 = cells.get(j + 1)[0], y2 = cells.get(j + 1)[1];
                if ((y2 < y1) || (y2 == y1 && x2 < x1)) {
                    // Swap the two cells
                    short[] temp = cells.get(j);
                    cells.set(j, cells.get(j + 1));
                    cells.set(j + 1, temp);
                }
            }
        }
    }
}
