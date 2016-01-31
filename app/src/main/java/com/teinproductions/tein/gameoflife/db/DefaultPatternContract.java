package com.teinproductions.tein.gameoflife.db;

import android.provider.BaseColumns;

public class DefaultPatternContract {

    public DefaultPatternContract() {}

    public static abstract class DefaultPatternEntry implements BaseColumns {
        public static final String TABLE_NAME = "default_patterns";
        public static final String COLUMN_NAME_URL = "url";
        public static final String COLUMN_NAME_NAME = "name";
        public static final String COLUMN_NAME_CONTENT = "content";
    }
}
