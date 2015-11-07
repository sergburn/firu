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

import java.security.InvalidParameterException;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class Vocabulary extends DictionaryBase
{
    private static final String TAG = Vocabulary.class.getName();

    public Vocabulary(String connectionString, Context context)
    {
        SQLiteOpenHelper dbOpener = new SQLiteOpenHelper(context, connectionString, null, 1)
        {
            @Override
            public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
            {
            }

            @Override
            public void onCreate(SQLiteDatabase db)
            {
                db.execSQL("CREATE TABLE words ("
                    + "_id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, "
                    + "text TEXT NOT NULL, "
                    + "lang TINYINT NOT NULL);");

                db.execSQL("CREATE UNIQUE INDEX idx_words_text on words (text ASC);");

                db.execSQL("CREATE TABLE translations ("
                    + "_id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, "
                    + "text TEXT NOT NULL, "
                    + "word_id INTEGER NOT NULL, "
                    + "fmark TINYINT DEFAULT (0), "
                    + "rmark TINYINT DEFAULT (0), "
                    + "lang TINYINT NOT NULL);");

                db.execSQL("CREATE INDEX FK_translations_words on translations (word_id ASC);");
            };
        };

        mDatabase = dbOpener.getWritableDatabase();

        mTotalWords = countWords();
    }


    public static class LearningStats
    {
        public Map<Mark, Integer> ForwardMarksDistribution = new TreeMap<Mark, Integer>();
        public Map<Mark, Integer> ReverseMarksDistribution = new TreeMap<Mark, Integer>();
        public int TotalTranslationsCount = 0;
        /*
        public LearningStats()
        {
            ForwardMarksDistribution = new HashMap<Mark, Integer>();
            ReverseMarksDistribution = new HashMap<Mark, Integer>();
            TotalTranslationsCount = 0;
        }
         */
    }

    private String[] getWordSelect()
    {
        return new String[] { "_id", "text", "lang" };
    }

    private Word readWord(Cursor c)
    {
        return new Word(
            c.getLong(0),
            c.getString(1),
            LangUtil.int2Lang(c.getInt(2)));
    }

    private String[] getTranslationSelect()
    {
        return new String[] { "_id", "text", "word_id", "fmark", "rmark", "lang" };
    }

    private MarkedTranslation readTranslation(Cursor c)
    {
        MarkedTranslation mt = new MarkedTranslation(
            c.getLong(0),
            c.getLong(2),
            c.getString(1),
            LangUtil.int2Lang(c.getInt(5)));
        mt.ForwardMark = new Mark(c.getInt(3));
        mt.ReverseMark = new Mark(c.getInt(4));
        return mt;
    }

    @Override
    public List<Word> searchWords(String startsWith, int numMaximum)
    {
        List<Word> list = new LinkedList<Word>();
        Cursor c = mDatabase.query("words",
            getWordSelect(),
            "text LIKE '" + startsWith + "%'", // most probably should use collated index
            null, null, null,
            "text ASC",
            String.valueOf(numMaximum));
        boolean next = c.moveToFirst();
        while (next)
        {
            Word w = readWord(c);
            list.add(w);
            next = c.moveToNext();
        }
        return list;
    }

    public List<Translation> getTranslations(final Word word)
    {
        return getTranslations(word, Mark.YetToLearn, Mark.Learned);
    }

    public List<Translation> getTranslations(final Word word, final Mark min, final Mark max)
    {
        List<Translation> list = new LinkedList<Translation>();
        Cursor c = mDatabase.query("translations",
            getTranslationSelect(),
            "word_id = ? AND rmark >= ? AND rmark <= ?",
            new String[] { String.valueOf(word.getID()), String.valueOf(min.toInt()), String.valueOf(max.toInt()) },
            null, null,
            "text ASC",
            null);
        boolean next = c.moveToFirst();
        while (next)
        {
            MarkedTranslation t = readTranslation(c);
            list.add(t);
            next = c.moveToNext();
        }
        return list;
    }

    public Word findWord(String text, int langCode)
    {
        Cursor c = mDatabase.query("words",
            getWordSelect(),
            "(lower(text) LIKE '" + text.toLowerCase(Locale.US) + "') AND " + "(lang = " + langCode + ")",
            null, null, null, null, String.valueOf(1));
        if (c.moveToFirst())
        {
            return readWord(c);
        }
        return null;
    }

    public Word getWord(long wordId)
    {
        Cursor c = mDatabase.query("words",
            getWordSelect(),
            "_id = " + String.valueOf(wordId),
            null, null, null, null, null);
        if (c.moveToFirst())
        {
            return readWord(c);
        }
        return null;
    }

    public Word addWord(Word dictWord, List<Translation> translations) throws Exception
    {
        if (translations.size() < 1)
        {
            throw new InvalidParameterException("Attempt to add word without translations");
        }

        long word_id = 0;
        mDatabase.beginTransaction();
        try
        {
            Cursor c = mDatabase.query("words",
                new String[] { "_id" },
                "(lower(text) LIKE '" + dictWord.getText().toLowerCase(Locale.US) + "') AND " +
                    "(lang = " + dictWord.getLangCode() + ")",
                    null, null, null, null, String.valueOf(1));
            boolean next = c.moveToFirst();
            if (next)
            {
                throw new Exception("The word " + dictWord.getText() + " already exists, ID " + String.valueOf(c.getInt(0)));
            }

            ContentValues wordValues = new ContentValues();
            wordValues.put("text", dictWord.getText());
            wordValues.put("lang", dictWord.getLangCode());
            word_id = mDatabase.insertOrThrow("words", null, wordValues);

            for (Translation dt : translations)
            {
                MarkedTranslation t = new MarkedTranslation(dt);

                ContentValues transValues = new ContentValues();
                transValues.put("word_id", word_id);
                transValues.put("text", t.getText());
                transValues.put("lang", t.getLangCode());
                transValues.put("fmark", t.ForwardMark.toInt());
                transValues.put("rmark", t.ReverseMark.toInt());
                mDatabase.insertOrThrow("translations", null, transValues);
            }

            mDatabase.setTransactionSuccessful();
        }
        catch (SQLiteConstraintException e)
        {
            Log.e(TAG, "Exception in addWord: " + e.getMessage());
            throw e;
        }
        finally
        {
            mDatabase.endTransaction();
        }
        mTotalWords = countWords();
        return new Word(word_id, dictWord.getText(), dictWord.getLang());
    }

    public boolean removeWord(Word word)
    {
        boolean ok = false;
        mDatabase.beginTransaction();
        try
        {
            mDatabase.delete("translations", "word_id = " + String.valueOf(word.getID()), null);
            mDatabase.delete("words", "_id = " + String.valueOf(word.getID()), null);
            mDatabase.setTransactionSuccessful();
            ok = true;
        }
        catch (SQLiteConstraintException e)
        {
            Log.e(TAG, "Exception in removeWord: " + e.getMessage());
            throw e;
        }
        finally
        {
            mDatabase.endTransaction();
        }
        mTotalWords = countWords();
        return ok;
    }

    public void updateMarks(MarkedTranslation trans)
    {
        mDatabase.beginTransaction();
        try
        {
            ContentValues transValues = new ContentValues();
            transValues.put("fmark", trans.ForwardMark.toInt());
            transValues.put("rmark", trans.ReverseMark.toInt());
            mDatabase.update("translations", transValues, "_id = " + String.valueOf(trans.getID()), null);

            mDatabase.setTransactionSuccessful();
        }
        catch (SQLiteConstraintException e)
        {
            Log.e(TAG, "Exception in updateMarks: " + e.getMessage());
            throw e;
        }
        finally
        {
            mDatabase.endTransaction();
        }
    }

    public void clearAll()
    {
        mDatabase.beginTransaction();
        try
        {
            mDatabase.delete("translations", null, null);
            mDatabase.delete("words", null, null);

            mDatabase.setTransactionSuccessful();
            mTotalWords = 0;
        }
        catch (SQLiteConstraintException e)
        {
            Log.e(TAG, "Exception in clearAll: " + e.getMessage());
            throw e;
        }
        finally
        {
            mDatabase.endTransaction();
        }
    }

    public List<MarkedTranslation> selectTranslations(Mark min, Mark max, boolean reverse)
    {
        List<MarkedTranslation> list = new LinkedList<MarkedTranslation>();
        try
        {
            Cursor c = mDatabase.query("translations",
                getTranslationSelect(),
                "rmark >= " + String.valueOf(min.toInt()) + " AND rmark <= " + String.valueOf(max.toInt()),
                null, null, null, null, null);
            boolean next = c.moveToFirst();
            while (next)
            {
                MarkedTranslation t = readTranslation(c);
                list.add(t);
                next = c.moveToNext();
            }
            Log.d(TAG, String.format("Loaded %d translations with marks in [%d..%d]",
                c.getCount(), min.toInt(), max.toInt()));
        }
        catch (Exception e)
        {
            Log.e(TAG, "Exception in selectTranslations: " + e.getMessage());
            //throw e;
        }
        return list;
    }

    public List<Word> selectWordsByMarks(final Mark min, final Mark max)
    {
        List<Word> list = new LinkedList<Word>();
        try
        {
            // select distinct w.* from words as w join translations as t on w._id = t.word_id where t.rmark < 4;

            Cursor c = mDatabase.rawQuery(
                "select distinct w.* from words as w join translations as t on w._id = t.word_id where t.rmark >= ? and t.rmark <= ?;",
                new String[] { String.valueOf(min.toInt()), String.valueOf(max.toInt()) }
                );
            boolean next = c.moveToFirst();
            while (next)
            {
                Word w = readWord(c);
                list.add(w);
                next = c.moveToNext();
            }
            Log.d(TAG, String.format("Loaded %d words with marks in [%d..%d]",
                c.getCount(), min.toInt(), max.toInt()));
        }
        catch (Exception e)
        {
            Log.e(TAG, "Exception in selectWordsByMarks: " + e.getMessage());
            //throw e;
        }
        return list;
    }

    private void setMarkStat(Map<Mark, Integer> map, Mark mark)
    {
        if (map.containsKey(mark))
        {
            map.put(mark, map.get(mark) + 1);
        }
        else
        {
            map.put(mark, 1);
        }
    }

    public LearningStats collectStatistics()
    {
        LearningStats stats = new LearningStats();
        try
        {
            Cursor c = mDatabase.query("translations",
                getTranslationSelect(),
                null,
                null, null, null, null, null);
            boolean next = c.moveToFirst();
            while (next)
            {
                MarkedTranslation t = readTranslation(c);

                setMarkStat(stats.ForwardMarksDistribution, t.ForwardMark);
                setMarkStat(stats.ReverseMarksDistribution, t.ReverseMark);
                stats.TotalTranslationsCount++;

                next = c.moveToNext();
            }
        }
        catch (Exception e)
        {
            Log.e(TAG, "Exception in collectStatistics: " + e.getMessage());
            //throw e;
        }
        return stats;
    }
}
