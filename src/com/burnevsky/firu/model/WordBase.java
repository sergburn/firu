package com.burnevsky.firu.model;

import java.util.List;

public class WordBase
{
    private int mID = 0;
    private String mText;

    public WordBase(String word)
    {
        mText = word;
    }

    // For internal use by Model only
    WordBase(int id, String word)
    {
        this(word);
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

    public List<TranslationBase> getTranslations()
    {
        return null;

    }
    
    public String toString()
    {
        return getText();
    }
}
