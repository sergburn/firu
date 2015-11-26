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

import android.annotation.SuppressLint;
import android.os.Parcel;
import android.os.Parcelable;

@SuppressLint("ParcelCreator")
public class DictionaryEntry implements Parcelable
{
    protected long mID = 0;
    protected String mText;
    protected String mLang;
    protected DictionaryID mDictID = DictionaryID.UNDEFINED;

    public DictionaryEntry()
    {
        super();
    }

    // For internal use by Model only
    DictionaryEntry(String text, String targetLang)
    {
        mText = text;
        mLang = targetLang;
    }

    // For internal use by Model only
    DictionaryEntry(DictionaryID dictID, long id, String text, String targetLang)
    {
        this(text, targetLang);
        mDictID = dictID;
        mID = id;
    }

    public long getID()
    {
        return mID;
    }

    public void unlink()
    {
        mID = 0;
        mDictID = DictionaryID.UNDEFINED;
    }

    public String getText()
    {
        return mText;
    }

    public String getLang()
    {
        return mLang;
    }

    public int getLangCode()
    {
        return LangUtil.lang2Int(mLang);
    }

    public DictionaryID getDictID()
    {
        return mDictID;
    }

    @Override
    public String toString()
    {
        return getText();
    }

    @Override
    public int describeContents()
    {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeLong(mID);
        dest.writeString(mText);
        dest.writeString(mLang);
        dest.writeValue(mDictID);
    }

    protected DictionaryEntry(Parcel in)
    {
        mID = in.readLong();
        mText = in.readString();
        mLang = in.readString();
        mDictID = (DictionaryID) in.readValue(null);
    }
}