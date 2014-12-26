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

import java.util.LinkedList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class Dictionary extends DictionaryBase
{
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
                new String[] { "source", "sourceFormat", "name", "src_lang", "trg_lang" },
                null, null, null, null, null);

        if (!c.isLast() && c.moveToFirst())
        {
            Meta = new Information();
            Meta.OriginalFile = c.getString(0);
            Meta.OriginalFormat = c.getString(1);
            Meta.Name = c.getString(2);
            Meta.SourceLanguage = LangUtil.int2Lang(c.getInt(3));
            Meta.TargetLanguage = LangUtil.int2Lang(c.getInt(4));
        }

        mTotalWords = countWords();
    }
    
    @Override
    public List<Word> searchWords(String startsWith, int numMaximum)
    {
        List<Word> list = new LinkedList<Word>();
        Cursor c = mDatabase.query("words", 
                new String[] { "_id", "text" },
                "text LIKE '" + startsWith + "%'", // SQLite can't use indexes with collation, so this is binary match
                 null, null, null, 
                 "text ASC",
                 String.valueOf(numMaximum));
        boolean next = c.moveToFirst();
        while (next)
        {
            Word w = new Word(c.getLong(0), c.getString(1), Meta.SourceLanguage);
            list.add(w);
            next = c.moveToNext();
        }
        return list;
    }

    public List<Translation> getTranslations(Word w)
    {
        List<Translation> list = new LinkedList<Translation>();
        Cursor c = mDatabase.query("translations", 
                new String[] { "_id", "text", "word_id" },
                "word_id = " + w.getID(),
                 null, null, null, null, null);
        boolean next = c.moveToFirst();
        while (next)
        {
            Translation t = new Translation(c.getLong(0), w.getID(), c.getString(1), Meta.TargetLanguage);
            list.add(t);
            next = c.moveToNext();
        }
        return list;
    }

}
