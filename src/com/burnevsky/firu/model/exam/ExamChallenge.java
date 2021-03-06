/*******************************************************************************
 * The MIT License (MIT)
 *
 * Copyright (c) 2015 Sergey Burnevsky (sergey.burnevsky at gmail.com)
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

package com.burnevsky.firu.model.exam;

import android.os.Parcel;
import android.os.Parcelable;

import com.burnevsky.firu.model.Mark;
import com.burnevsky.firu.model.MarkedTranslation;
import com.burnevsky.firu.model.Word;

public class ExamChallenge implements Parcelable
{
    ExamChallenge(Word w, MarkedTranslation t, Exam.TestDirection direction)
    {
        mWord = w;
        mTranslation = t;
        mDirection = direction;
    }

    public Word mWord;
    public MarkedTranslation mTranslation;
    public Exam.TestDirection mDirection;

    public Mark getMark()
    {
        return (mDirection == Exam.TestDirection.FORWARD_TEST) ?
                mTranslation.ForwardMark : mTranslation.ReverseMark;
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
        dest.writeInt(mDirection.ordinal());
        dest.writeParcelable(mWord, flags);
        dest.writeParcelable(mTranslation, flags);
    }

    private ExamChallenge(Parcel in)
    {
        mDirection = (in.readInt() == Exam.TestDirection.FORWARD_TEST.ordinal()) ?
                Exam.TestDirection.FORWARD_TEST : Exam.TestDirection.REVERSE_TEST;
        mWord = in.readParcelable(Word.class.getClassLoader());
        mTranslation = in.readParcelable(MarkedTranslation.class.getClassLoader());
    }

    public static final Parcelable.Creator<ExamChallenge> CREATOR = new Parcelable.Creator<ExamChallenge>()
    {
        @Override
        public ExamChallenge createFromParcel(Parcel in)
        {
            return new ExamChallenge(in);
        }

        @Override
        public ExamChallenge[] newArray(int size)
        {
            return new ExamChallenge[size];
        }
    };
}
