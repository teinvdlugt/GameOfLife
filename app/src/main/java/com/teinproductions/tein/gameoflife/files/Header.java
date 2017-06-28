package com.teinproductions.tein.gameoflife.files;

public class Header implements PatternListable {
    private String text;

    public Header(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
