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

import java.util.Collections;
import java.util.List;

public class Translation extends DictionaryEntry
{
    protected long mWordID = 0;

    public Translation(Text text)
    {
        super(text);
    }

    // For internal use by Model only
    Translation(DictionaryID dictID, long id, long wordId, Text text)
    {
        super(dictID, id, text);
        mWordID = wordId;
    }

    public Translation(Translation other)
    {
        this(other.getDictID(), other.getID(), other.getWordID(), other);
    }

    public long getWordID()
    {
        return mWordID;
    }

    @Override
    public void unlink()
    {
        super.unlink();
        mWordID = 0;
    }

    /** @return true if all given Translations belong to Vocabulary.
     *          false if any of those don't. */
    public static boolean isAllVocabularyItems(List<? extends Translation> list)
    {
        if (list != null)
        {
            for (Translation t : list)
            {
                if (!t.isVocabularyItem()) return false;
            }
        }
        return true;
    }

    // Parcelable

    protected Translation(Parcel in)
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

    public static final Parcelable.Creator<Translation> CREATOR = new Parcelable.Creator<Translation>()
        {
        @Override
        public Translation createFromParcel(Parcel in)
        {
            return new Translation(in);
        }

        @Override
        public Translation[] newArray(int size)
        {
            return new Translation[size];
        }
        };
}
