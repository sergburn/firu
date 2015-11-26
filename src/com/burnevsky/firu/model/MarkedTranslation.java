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

public class MarkedTranslation extends Translation
{
    public Mark ForwardMark = Mark.YetToLearn;
    public Mark ReverseMark = Mark.YetToLearn;

    public MarkedTranslation(Word w, String text, String targetLang)
    {
        super(w, text, targetLang);
    }

    MarkedTranslation(DictionaryID dictID, long id, long wordId, String text, String targetLang)
    {
        super(dictID, id, wordId, text, targetLang);
    }

    public MarkedTranslation(Translation trans)
    {
        super(trans.getWordID(), trans.getText(), trans.getLang());
    }

    // Parcelable

    private MarkedTranslation(Parcel in)
    {
        super(in);
        ForwardMark = new Mark(in.readInt());
        ReverseMark = new Mark(in.readInt());
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