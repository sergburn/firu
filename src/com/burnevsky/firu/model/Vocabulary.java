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

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class Vocabulary extends DictionaryBase
{
    public Vocabulary(String connectionString)
    {
    }

    public static Vocabulary open(String connectionString, Context context)
    {
        Vocabulary self = new Vocabulary(connectionString);

        self.mDbOpener = new SQLiteOpenHelper(context, connectionString, null, 1)
        {
            @Override
            public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
            {
                // TODO Auto-generated method stub

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

        self.mDatabase = self.mDbOpener.getWritableDatabase();
        
        self.mTotalWords = self.countWords();

        return self;
    }

    public class MarkedTranslation extends Translation
    {
	public Mark ForwardMark = Mark.YetToLearn;
	public Mark ReverseMark = Mark.YetToLearn;

	public MarkedTranslation(Word w, String text, String targetLang)
	{
	    super(w, text, targetLang);
	}

        public MarkedTranslation(Translation trans)
        {
            super(trans.getWordID(), trans.getText(), trans.getLang());
        }
    }	
    
    @Override
    public List<Word> searchWords(String startsWith, int numMaximum)
    {
        List<Word> list = new LinkedList<Word>();
        Cursor c = mDatabase.query("words", 
                new String[] { "_id", "text", "lang" },
                "text LIKE '" + startsWith + "%'", // most probably should use collated index
                 null, null, null, 
                 "text ASC",
                 String.valueOf(numMaximum));
        boolean next = c.moveToFirst();
        while (next)
        {
            Word w = new Word(
                    c.getLong(0), 
                    c.getString(1), 
                    LangUtil.int2Lang(c.getInt(2)));
            list.add(w);
            next = c.moveToNext();
        }
        return list;
    }

    public Word addWord(Word dictWord, List<Translation> translations) throws Exception
    {
        long word_id = 0;
        
        if (translations.size() < 1)
        {
            throw new InvalidParameterException("Attempt to add word without translations");
        }
        
        mDatabase.beginTransaction();
        try
        {
            Cursor c = mDatabase.query("words", 
                    new String[] { "_id" },
                    "(lower(text) LIKE '" + dictWord.getText().toLowerCase() + "') AND " + 
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
            word_id = mDatabase.insertOrThrow("word", null, wordValues);
            
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
            Log.d("firu.model", "Exception in addWord: " + e.getMessage());
            throw e;
        }
        finally
        {
            mDatabase.endTransaction();
        }
        
        return new Word(word_id, dictWord.getText(), dictWord.getLang());
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
        finally
        {
            mDatabase.endTransaction();
        }
    }
    
    public List<Translation> getTranslations(Word word)
    {
	return null;
    }

}
