/*******************************************************************************
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Sergey Burnevsky
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *******************************************************************************/

package com.burnevsky.firu.model;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;
import android.util.Log;

public class Dictionary extends DictionaryBase
{
    private static final String[] TRANSLATION_COLUMNS = new String[]{"_id", "text", "word_id"};
    private static final String[] WORD_COLUMNS = new String[]{"_id", "text"};
    private static final String[] INFO_COLUMNS = new String[]{"source", "sourceFormat", "name", "src_lang", "trg_lang"};

    public Information Meta;

    public class Information
    {
        public String OriginalFile;
        public String OriginalFormat;
        public String Name;
        public String SourceLanguage;
        public String TargetLanguage;
    }

    public Dictionary(String connectionString, Context context)
    {
        super(DictionaryID.UNIVERSAL); // TODO: It should be dependent on something

        Log.i("firu", "connectionString: " + connectionString);

        SQLiteOpenHelper dbOpener = new SQLiteOpenHelper(context, connectionString, null, 1)
        {
            @Override
            public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
            {
            }

            @Override
            public void onCreate(SQLiteDatabase db)
            {
            }
        };

        mDatabase = dbOpener.getReadableDatabase();

        Cursor c = mDatabase.query("info",
            INFO_COLUMNS,
            null, null, null, null, null);

        if (c.moveToNext())
        {
            Meta = new Information();
            Meta.OriginalFile = c.getString(0);
            Meta.OriginalFormat = c.getString(1);
            Meta.Name = c.getString(2);
            Meta.SourceLanguage = LangUtil.int2Lang(c.getInt(3));
            Meta.TargetLanguage = LangUtil.int2Lang(c.getInt(4));
        }
        c.close();

        mTotalWords = countWords();
        mTotalTranslations = countTranslations();
    }

    @Override
    protected Word readWord(Cursor c)
    {
        return new Word(getDictID(), c.getLong(0), new Text(c.getString(1), Meta.SourceLanguage));
    }

    @Override
    protected String[] getWordColumns()
    {
        return WORD_COLUMNS;
    }

    @Override
    protected String getWordMatchQuery(Text text)
    {
        return "lower(text) LIKE '" + text.getText().toLowerCase(Locale.US) + "'";
    }

    @Override
    protected Translation readTranslation(Cursor c)
    {
        return readTranslation(c, 0);
    }

    @Override
    protected String[] getTranslationColumns()
    {
        return TRANSLATION_COLUMNS;
    }

    @NonNull
    private Translation readTranslation(Cursor c, int firstColumn)
    {
        return new Translation(getDictID(),
            c.getLong(firstColumn), c.getLong(firstColumn + 2),
            new Text(c.getString(firstColumn + 1), Meta.TargetLanguage));
    }

    public List<Word> searchWordsByTranslations(String matchText, int numMaximum)
    {
        String pattern0 = matchText + "%";
        String pattern1 = "% " + matchText + "%";

        Word w = null;
        List<Word> list = new LinkedList<>();
        Cursor c = mDatabase.rawQuery(
            "SELECT w._id, w.text, t._id, t.text, t.word_id " +
                "FROM words AS w JOIN translations AS t ON w._id = t.word_id " +
                "WHERE t.text LIKE ? OR t.text LIKE ? " +
                "ORDER BY w._id LIMIT ?;",
            new String[]{pattern0, pattern1, String.valueOf(numMaximum)}
        );
        int transColumnsStart = c.getColumnIndex("t._id");
        boolean next = c.moveToFirst();
        while (next)
        {
            if (w == null || w.getID() != c.getLong(0))
            {
                if (w != null)
                {
                    list.add(w);
                }
                w = readWord(c);
            }
            Translation t = readTranslation(c, transColumnsStart);
            w.addTranslation(t);
            next = c.moveToNext();
        }
        if (w != null)
        {
            list.add(w);
        }
        c.close();
        return list;
    }

}
