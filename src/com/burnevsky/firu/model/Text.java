/*******************************************************************************
 * The MIT License (MIT)
 *
 * Copyright (c) 2015 Sergey Burnevsky
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

import java.util.List;

public class Text implements Parcelable
{
    protected String mText;
    protected String mLang;

    public Text(String text, String targetLang)
    {
        mText = text;
        mLang = targetLang;
    }

    public Text(Text other)
    {
        mText = other.getText();
        mLang = other.getLang();
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

    public static int findMatch(Text sample, List<? extends Text> list)
    {
        if (list != null)
        {
            for (int i = 0; i < list.size(); i++)
            {
                if (list.get(i).getText().compareTo(sample.getText()) == 0)
                {
                    return i;
                }
            }
        }
        return -1;
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
        dest.writeString(mText);
        dest.writeString(mLang);
    }

    protected Text(Parcel in)
    {
        mText = in.readString();
        mLang = in.readString();
    }

    public static final Parcelable.Creator<Text> CREATOR = new Parcelable.Creator<Text>()
    {
        @Override
        public Text createFromParcel(Parcel in)
        {
            return new Text(in);
        }

        @Override
        public Text[] newArray(int size)
        {
            return new Text[size];
        }
    };
}
