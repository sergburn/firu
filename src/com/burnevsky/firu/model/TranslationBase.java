package com.burnevsky.firu.model;

import android.os.Parcel;
import android.os.Parcelable;

public class TranslationBase extends Object implements Parcelable
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
        dest.writeInt(mWordID);
    }

    public static final Parcelable.Creator<TranslationBase> CREATOR = new Parcelable.Creator<TranslationBase>()
    {
        public TranslationBase createFromParcel(Parcel in)
        {
            return new TranslationBase(in);
        }

        public TranslationBase[] newArray(int size)
        {
            return new TranslationBase[size];
        }
    };

    private TranslationBase(Parcel in)
    {
        mID = in.readInt();
        mText = in.readString();
        mWordID = in.readInt();
    }
}
