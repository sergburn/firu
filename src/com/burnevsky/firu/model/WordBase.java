package com.burnevsky.firu.model;

import java.util.List;

import android.os.Parcel;
import android.os.Parcelable;

public class WordBase extends Object implements Parcelable
{
    private int mID = 0;
    private String mText;
    private List<TranslationBase> mTranslations;

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

    // Parcelable

    @Override
    public int describeContents()
    {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeInt(mID);
        dest.writeString(mText);
        dest.writeList(mTranslations);
    }

    public static final Parcelable.Creator<WordBase> CREATOR = new Parcelable.Creator<WordBase>()
    {
        public WordBase createFromParcel(Parcel in)
        {
            return new WordBase(in);
        }

        public WordBase[] newArray(int size)
        {
            return new WordBase[size];
        }
    };

    private WordBase(Parcel in)
    {
        mID = in.readInt();
        mText = in.readString();
        in.readList(mTranslations, null);
    }
}
