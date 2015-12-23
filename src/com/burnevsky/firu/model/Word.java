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
import java.util.List;

import android.os.Parcel;
import android.os.Parcelable;

public class Word extends DictionaryEntry
{
    List<Translation> mTranslations = new ArrayList<>();

    public Word(Text text)
    {
        super(text);
    }

    // For internal use by Model only
    Word(DictionaryID dictID, long id, Text text)
    {
        super(dictID, id, text);
    }

    public Word(final DictionaryEntry other)
    {
        this(other.getDictID(), other.getID(), other);
    }

    public List<Translation> getTranslations()
    {
        return new ArrayList<>(mTranslations);
    }

    @Override
    public void unlink()
    {
        super.unlink();
        for (Translation t : mTranslations)
        {
            t.unlink();
        }
    }

    public void addTranslation(Translation t)
    {
        assert (mTranslations != null);
        mTranslations.add(t);
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
        super.writeToParcel(dest, flags);
        dest.writeList(mTranslations);
    }

    private Word(Parcel in)
    {
        super(in);
        in.readList(mTranslations, MarkedTranslation.class.getClassLoader());
    }

    public static final Parcelable.Creator<Word> CREATOR = new Parcelable.Creator<Word>()
    {
        @Override
        public Word createFromParcel(Parcel in)
        {
            return new Word(in);
        }

        @Override
        public Word[] newArray(int size)
        {
            return new Word[size];
        }
    };
}
