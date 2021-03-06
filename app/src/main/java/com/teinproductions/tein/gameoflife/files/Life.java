package com.teinproductions.tein.gameoflife.files;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Life implements Serializable {

    private String name;
    private String creator;
    private List<String> comments;
    /**
     * 0: x position
     * 1: y position
     * 2: alive [0|1]
     * 3: num of neighbours
     */
    private ArrayList<short[]> cells;
    private String originalFile;
    /**
     * Width and height of the pattern, specified in an RLE file as "x = ..., y = ..."
     * Or -1 if they are not known.
     */
    private int width = -1, height = -1;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public List<String> getComments() {
        return comments;
    }

    public void setComments(List<String> comments) {
        this.comments = comments;
    }

    public void addComment(String comment) {
        if (comments == null) comments = new ArrayList<>();
        comments.add(comment);
    }

    public ArrayList<short[]> getCells() {
        return cells;
    }

    public void setCells(ArrayList<short[]> cells) {
        this.cells = cells;
    }

    public String getOriginalFile() {
        return originalFile;
    }

    public void setOriginalFile(String originalFile) {
        this.originalFile = originalFile;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }
}
