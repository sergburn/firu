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

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

/**
 *
 */
public abstract class DictionaryBase implements IDictionary
{
    protected static final String WORDS_TABLE = "words";
    protected static final String TRANSLATIONS_TABLE = "translations";

    protected SQLiteDatabase mDatabase = null;
    protected int mTotalWords = 0;
    protected int mTotalTranslations = 0;
    protected DictionaryID mDictID = DictionaryID.UNDEFINED;

    public DictionaryBase(DictionaryID dictID)
    {
        mDictID = dictID;
    }

    @Override
    public DictionaryID getDictID()
    {
        return mDictID;
    }

    @Override
    public int getTotalWords()
    {
        return mTotalWords;
    }

    @Override
    public int getTotalTranslations()
    {
        return mTotalTranslations;
    }

    @Override
    public int countWords(String startsWith)
    {
        if (mDatabase == null) return 0;

        Cursor c = mDatabase.query(WORDS_TABLE,
            new String[] { "count(*)" },
            "text LIKE '" + startsWith + "%'", // most probably should use collated index
            null, null, null, null, null);

        int count = 0;
        if (c.moveToNext())
        {
            count = c.getInt(0);
        }
        c.close();
        return count;
    }

    protected int countWords()
    {
        if (mDatabase == null) return 0;

        Cursor c = mDatabase.query(WORDS_TABLE,
            new String[] { "count(*)" },
            null, null, null, null, null, null);

        int count = 0;
        if (c.moveToNext())
        {
            count = c.getInt(0);
        }
        c.close();
        return count;
    }

    protected int countTranslations()
    {
        if (mDatabase == null) return 0;

        Cursor c = mDatabase.query(TRANSLATIONS_TABLE,
            new String[] { "count(*)" },
            null, null, null, null, null, null);

        int count = 0;
        if (c.moveToNext())
        {
            count = c.getInt(0);
        }
        c.close();
        return count;
    }

    protected abstract Word readWord(Cursor c);
    protected abstract String[] getWordColumns();
    protected abstract String getWordMatchQuery(final Text text);

    @Override
    public Word findWord(Text text)
    {
        Word word = null;

        Cursor c = mDatabase.query(WORDS_TABLE, getWordColumns(),
            getWordMatchQuery(text),
            null, null, null, null, String.valueOf(1));

        if (c.moveToFirst())
        {
            word = readWord(c);
            c.close();
            loadTranslations(word);
        }
        else
        {
            c.close();
        }

        return word;
    }

    @Override
    public List<Word> searchWords(String startsWith, int numMaximum)
    {
        List<Word> list = new ArrayList<>();
        Cursor c = mDatabase.query(WORDS_TABLE, getWordColumns(),
            // Should use collated index, but SQLite can't use indexes with collation, so this is binary match
            "text LIKE '" + startsWith + "%'",
            null, null, null,
            "text ASC",
            String.valueOf(numMaximum));

        while (c.moveToNext())
        {
            Word w = readWord(c);
            list.add(w);
        }

        return list;
    }

    protected abstract Translation readTranslation(Cursor c);
    protected abstract String[] getTranslationColumns();

    @Override
    public void loadTranslations(Word word)
    {
        if (word.getDictID() != getDictID())
        {
            throw new IllegalArgumentException("This word is not from this dictionary");
        }

        List<Translation> list = new ArrayList<>();
        Cursor c = mDatabase.query(TRANSLATIONS_TABLE, getTranslationColumns(),
            "word_id = " + word.getID(),
            null, null, null, null, null);
        while (c.moveToNext())
        {
            list.add(readTranslation(c));
        }
        c.close();
        word.mTranslations = list;
    }
}
