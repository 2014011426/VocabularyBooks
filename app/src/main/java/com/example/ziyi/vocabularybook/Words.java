package com.example.ziyi.vocabularybook;

import android.provider.BaseColumns;

/**
 * Created by ziyi on 2016/10/30.
 */

public class Words {
    public Words(){

    }

    public static abstract class Word implements BaseColumns {
        public static final String TABLE_NAME = "words";
        public static final String COLUMN_NAME_WORD = "word";
        public static final String COLUMN_NAME_MEANING = "meaning";
        public static final String COLUMN_NAME_SAMPLE = "sample";
    }
}
