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

import android.os.Parcel;
import android.os.Parcelable;

public class Translation extends DictionaryEntry
{
    long mWordID;

    public Translation(long wordId, String text, String targetLang)
    {
        super(text, targetLang);
        mWordID = wordId;
    }

    public Translation(Word w, String text, String targetLang)
    {
        this(w.getID(), text, targetLang);
    }

    // For internal use by Model only
    Translation(long id, long wordId, String text, String targetLang)
    {
        super(id, text, targetLang);
        mWordID = wordId;
    }

    public long getWordID()
    {
        return mWordID;
    }

    // Parcelable

    public static final Parcelable.Creator<Translation> CREATOR = new Parcelable.Creator<Translation>()
    {
        public Translation createFromParcel(Parcel in)
        {
            return new Translation(in);
        }

        public Translation[] newArray(int size)
        {
            return new Translation[size];
        }
    };

    private Translation(Parcel in)
    {
        super(in);
        mWordID = in.readLong();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        super.writeToParcel(dest, flags);
        dest.writeLong(mWordID);
    }
}
