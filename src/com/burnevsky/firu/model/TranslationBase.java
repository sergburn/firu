package com.burnevsky.firu.model;

public class TranslationBase
{
    private int mID = 0;
    private String mText;
    private int mWordID;

    public TranslationBase(WordBase w, String ts)
    {
        mWordID = w.getID();
        mText = ts;
    }

    // For internal use by Model only
    TranslationBase(int id, WordBase w, String ts)
    {
        this(w, ts);
        mID = id;
    }

    public int getID()
    {
        return mID;
    }

    public String getText()
    {
        return mText;
    }

    public int getWordID()
    {
        return mWordID;
    }
}
