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
    private static final String[] WORD_COLUMNS = new String[]{"_id", "text", "lang"};
    private static final String[] TRANSLATION_COLUMNS = new String[]{"_id", "text", "word_id", "fmark", "rmark", "lang"};

    public Vocabulary(String connectionString, Context context)
    {
        super(DictionaryID.VOCABULARY);

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
            }
        };

        mDatabase = dbOpener.getWritableDatabase();

        mTotalWords = countWords();
        mTotalTranslations = countTranslations();
    }

    public static class LearningStats
    {
        public final Map<Mark, Integer> ForwardMarksDistribution = new TreeMap<>();
        public final Map<Mark, Integer> ReverseMarksDistribution = new TreeMap<>();
        public int TotalTranslationsCount = 0;
    }

    @Override
    protected Word readWord(Cursor c)
    {
        return new Word(
            getDictID(),
            c.getLong(0),
            new Text(c.getString(1), LangUtil.int2Lang(c.getInt(2))));
    }

    @Override
    protected String[] getWordColumns()
    {
        return WORD_COLUMNS;
    }

    @Override
    protected String getWordMatchQuery(final Text text)
    {
        return "(lower(text) LIKE '" + text.getText().toLowerCase(Locale.US) + "') AND (lang = " +
            text.getLangCode() + ")";
    }

    @Override
    protected MarkedTranslation readTranslation(Cursor c)
    {
        MarkedTranslation mt = new MarkedTranslation(
            getDictID(),
            c.getLong(0),
            c.getLong(2),
            new Text(c.getString(1), LangUtil.int2Lang(c.getInt(5))));
        mt.ForwardMark = new Mark(c.getInt(3));
        mt.ReverseMark = new Mark(c.getInt(4));
        return mt;
    }

    @Override
    protected String[] getTranslationColumns()
    {
        return TRANSLATION_COLUMNS;
    }

    public List<MarkedTranslation> getTranslations(final long wordId, final Mark min, final Mark max)
    {
        List<MarkedTranslation> list = new ArrayList<>();
        Cursor c = mDatabase.query(TRANSLATIONS_TABLE, TRANSLATION_COLUMNS,
            "word_id = ? AND rmark >= ? AND rmark <= ?",
            new String[] { String.valueOf(wordId), String.valueOf(min.toInt()), String.valueOf(max.toInt()) },
            null, null,
            "text ASC",
            null);
        boolean next = c.moveToFirst();
        while (next)
        {
            list.add(readTranslation(c));
            next = c.moveToNext();
        }
        c.close();
        return list;
    }

    /** Adds given word and translations to the vocabulary.
     *  @param wordText The word to add.
     *              If same Text already exists as Word, then translations added to it.
     *  @param translations Translation to associate with this word.
     *                      If some translations already exist, they are ignored.
     *  @return New Word instance with all its translations as it exists in the database.
     *  */
    public Word addWord(final Text wordText, final List<? extends Text> translations) throws Exception
    {
        if (translations == null || translations.size() < 1)
        {
            throw new IllegalArgumentException("Attempt to add word without translations");
        }

        Word newWord;

        mDatabase.beginTransaction();
        try
        {
            long word_id;

            Word existingWord = findWord(wordText);
            if (existingWord != null)
            {
                word_id = existingWord.getID();
            }
            else
            {
                ContentValues wordValues = new ContentValues();
                wordValues.put("text", wordText.getText());
                wordValues.put("lang", wordText.getLangCode());
                word_id = mDatabase.insertOrThrow(WORDS_TABLE, null, wordValues);
            }

            newWord = new Word(getDictID(), word_id, wordText);

            doAddTranslations(newWord, translations);

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
        mTotalTranslations = countTranslations();
        return newWord;
    }

    public Word addTranslations(final Word word, final List<? extends Text> translations) throws Exception
    {
        if (translations == null || translations.size() < 1)
        {
            throw new IllegalArgumentException("Translation list is null or empty");
        }

        if (word.getID() == 0 || word.getDictID() != DictionaryID.VOCABULARY)
        {
            throw new IllegalArgumentException("Given word does not belong to vocabulary");
        }

        Word newWord = new Word(word);
        mDatabase.beginTransaction();
        try
        {
            doAddTranslations(newWord, translations);
            mDatabase.setTransactionSuccessful();
        }
        catch (SQLiteConstraintException e)
        {
            Log.e(TAG, "Exception in addTranslations: " + e.getMessage());
            throw e;
        }
        finally
        {
            mDatabase.endTransaction();
        }
        mTotalTranslations = countTranslations();
        return newWord;
    }

    private void doAddTranslations(Word word, final List<? extends Text> translations)
    {
        loadTranslations(word);

        for (Text transText : translations)
        {
            MarkedTranslation mt;

            int matchIndex = Translation.findMatch(transText, word.getTranslations());
            if (matchIndex < 0) // not found
            {
                ContentValues transValues = new ContentValues();
                transValues.put("word_id", word.getID());
                transValues.put("text", transText.getText());
                transValues.put("lang", transText.getLangCode());
                transValues.put("fmark", Mark.YetToLearn.toInt());
                transValues.put("rmark", Mark.YetToLearn.toInt());

                long trans_id = mDatabase.insertOrThrow(TRANSLATIONS_TABLE, null, transValues);

                mt = new MarkedTranslation(DictionaryID.VOCABULARY, trans_id, word.getID(), transText);
                word.addTranslation(mt);
            }
        }
    }

    public boolean removeWord(Word word) throws Exception
    {
        boolean ok = false;
        mDatabase.beginTransaction();
        try
        {
            mDatabase.delete(TRANSLATIONS_TABLE, "word_id = " + String.valueOf(word.getID()), null);
            mDatabase.delete(WORDS_TABLE, "_id = " + String.valueOf(word.getID()), null);
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
            mDatabase.update(TRANSLATIONS_TABLE, transValues, "_id = " + String.valueOf(trans.getID()), null);

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
            mDatabase.delete(TRANSLATIONS_TABLE, null, null);
            mDatabase.delete(WORDS_TABLE, null, null);

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

    public List<Word> selectWordsByMarks(final Mark min, final Mark max)
    {
        List<Word> list = new LinkedList<>();
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
            Cursor c = mDatabase.query(TRANSLATIONS_TABLE, TRANSLATION_COLUMNS,
                null, null, null, null, null, null);
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
