/*******************************************************************************
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2016 Sergey Burnevsky (sergey.burnevsky at gmail.com)
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

public class MarkedTranslation extends Translation
{
    public Mark ForwardMark = Mark.YET_TO_LEARN;
    public Mark ReverseMark = Mark.YET_TO_LEARN;

    public MarkedTranslation(Text text)
    {
        super(text);
    }

    MarkedTranslation(DictionaryID dictID, long id, long wordId, Text text)
    {
        super(dictID, id, wordId, text);
    }

    MarkedTranslation(long id, long wordId, Text text)
    {
        super(DictionaryID.VOCABULARY, id, wordId, text);
    }

    public MarkedTranslation(MarkedTranslation other)
    {
        super(other);
        ForwardMark = other.ForwardMark;
        ReverseMark = other.ReverseMark;
    }

    // Parcelable

    private MarkedTranslation(Parcel in)
    {
        super(in);
        ForwardMark = Mark.fromInt(in.readInt());
        ReverseMark = Mark.fromInt(in.readInt());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        super.writeToParcel(dest, flags);
        dest.writeInt(ForwardMark.toInt());
        dest.writeInt(ReverseMark.toInt());
    }

    public static final Parcelable.Creator<MarkedTranslation> CREATOR = new Parcelable.Creator<MarkedTranslation>()
        {
        @Override
        public MarkedTranslation createFromParcel(Parcel in)
        {
            return new MarkedTranslation(in);
        }

        @Override
        public MarkedTranslation[] newArray(int size)
        {
            return new MarkedTranslation[size];
        }
        };
}
