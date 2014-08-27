package com.burnevsky.firu.model;

import java.util.LinkedList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class Dictionary
{
    private boolean mInBulkOps = false;
    private SQLiteOpenHelper mDbOpener = null;
    private SQLiteDatabase mDatabase = null;

    private int mTotalWords;

    /*
     * public Table<Word> Words; // should be map of language public
     * Table<Translation> Translations; // should be map of language pair public
     * Table<Information> Info;
     */

    public Information Description;

    public class Information
    {
        public String OriginalFile;
        public String OriginalFormat;
        public String Name;
        public String SourceLanguage;
        public String TargetLanguage;
    }

    Dictionary(String connectionString)
    {
    }

    public static Dictionary open(String connectionString, Context context)
    {
        Log.i("firu", "connectionString: " + connectionString);

        Dictionary self = new Dictionary(connectionString);

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
                // TODO Auto-generated method stub

            }
        };

        self.mDatabase = self.mDbOpener.getReadableDatabase();

        Cursor c = self.mDatabase.rawQuery("SELECT count(*) FROM words", null);
        if (!c.isLast() && c.moveToFirst())
        {
            self.mTotalWords = c.getInt(0);
        }

        c = self.mDatabase.query("info", new String[] { "source", "sourceFormat", "name", "src_lang", "trg_lang" },
                null, null, null, null, null);

        if (!c.isLast() && c.moveToFirst())
        {
            self.Description = self.new Information();
            self.Description.OriginalFile = c.getString(0);
            self.Description.OriginalFormat = c.getString(1);
            self.Description.Name = c.getString(2);
            self.Description.SourceLanguage = c.getString(3);
            self.Description.TargetLanguage = c.getString(4);
        }

        return self;
    }

    public int getTotalWords()
    {
        return mTotalWords;
    }

    public int getTotalTranslations()
    {
        return 0;
    }

    public class Word extends WordBase
    {
        private String mSourceLang;

        public Word(String word, String sourceLang)
        {
            super(word);
            mSourceLang = sourceLang;
        }

        Word(int id, String word, String sourceLang)
        {
            super(id, word);
            mSourceLang = sourceLang;
        }

        public String getSourceLang()
        {
            return mSourceLang;
        }
    }

    public class Translation extends TranslationBase
    {
        private String mTargetLang;

        public Translation(WordBase w, String ts, String targetLang)
        {
            super(w, ts);
            mTargetLang = targetLang;
        }

        public String getTargetLang()
        {
            return mTargetLang;
        }
    }

    public WordBase AddWord(String word, List<String> translations, boolean submit)
    {
        WordBase w = new WordBase(word);
        for (String ts : translations)
        {
            TranslationBase t = new TranslationBase(w, ts);
            // Translations.InsertOnSubmit(t);
            // w.Translations.Add(t);
        }
        // Words.InsertOnSubmit(w);
        if (!mInBulkOps)
        {
            // submitChanges();
        }
        return w;
    }

    public List<WordBase> searchWords(String startsWith, int numMaximum)
    {
        List<WordBase> list = new LinkedList<WordBase>();
        Cursor c = mDatabase.query("words", 
                new String[] { "_id", "text" },
                "text LIKE '" + startsWith + "%'", // most probably should use collated index
                 null, null, null, 
                 "text ASC",
                 String.valueOf(numMaximum));
        boolean next = c.moveToFirst();
        while (next)
        {
            Word w = new Word(c.getInt(0), c.getString(1), Description.SourceLanguage);
            list.add(w);
            next = c.moveToNext();
        }
        return list;
    }

    public long countWords(String startsWith)
    {
        Cursor c = mDatabase.query("words", 
                new String[] { "count(*)" },
                "text LIKE '" + startsWith + "%'", // most probably should use collated index
                 null, null, null, null, null);
        long count = 0;
        boolean next = c.moveToFirst();
        if (next)
        {
            count = c.getInt(0);
        }
        return count;
    }

    public List<Translation> getTranslations(WordBase w)
    {
        return null;
    }

}
