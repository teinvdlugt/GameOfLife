package com.teinproductions.tein.gameoflife.files;


import java.util.List;

public class LifeUtils {

    public static void documentCells(List<short[]> cells) {
        checkCellsDocumented(cells);
        recountNeighbours(cells);
    }

    public static void checkCellsDocumented(List<short[]> cells) {
        int numOfCells = cells.size();
        for (int i = 0; i < numOfCells; i++) {
            checkNeighboursDocumented(cells, cells.get(i)[0], cells.get(i)[1]);
        }
    }

    public static void checkNeighboursDocumented(List<short[]> cells, short x, short y) {
        for (short[] neighbour : new short[][]{{(short) (x - 1), (short) (y - 1)}, {x, (short) (y - 1)},
                {(short) (x + 1), (short) (y - 1)}, {(short) (x - 1), y}, {(short) (x + 1), y},
                {(short) (x - 1), (short) (y + 1)}, {x, (short) (y + 1)}, {(short) (x + 1), (short) (y + 1)}}) {
            checkCellDocumented(cells, neighbour[0], neighbour[1]);
        }
    }

    public static void checkCellDocumented(List<short[]> cells, short x, short y) {
        for (short[] cell : cells) {
            if (cell[0] == x && cell[1] == y) {
                return;
            }
        }

        cells.add(new short[]{x, y, 0, 0});
    }

    public static void recountNeighbours(List<short[]> cells) {
        for (int i = 0; i < cells.size(); i++) {
            short[] cell = cells.get(i);
            cells.set(i, new short[]{cell[0], cell[1], cell[2], neighbours(cells, cell[0], cell[1])});
        }
    }

    public static byte neighbours(List<short[]> cells, short x, short y) {
        byte neighbours = 0;
        for (short[] cell : cells) {
            if (cell[2] == 1 && (cell[0] == x - 1 || cell[0] == x || cell[0] == x + 1) &&
                    (cell[1] == y - 1 || cell[1] == y | cell[1] == y + 1) &&
                    !(cell[0] == x && cell[1] == y))
                neighbours++;
        }
        return neighbours;
    }
}
