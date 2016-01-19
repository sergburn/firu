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

public enum Mark
{
    UNFAMILIAR(0),
    YET_TO_LEARN(1),
    WITH_HINTS(2),
    ALMOST_LEARNED(3),
    LEARNED(4);

    private int mValue;

    Mark(int value)
    {
        mValue = value;
    }

    public Mark upgrade()
    {
        if (mValue < LEARNED.toInt())
        {
            return fromInt(mValue + 1);
        }
        return this;
    }

    public Mark downgrade()
    {
        if (mValue > YET_TO_LEARN.toInt())
        {
            return fromInt(mValue - 1);
        }
        return this;
    }

    @Override
    public String toString()
    {
        switch (this)
        {
            case UNFAMILIAR:
                return "Unfamiliar";
            case YET_TO_LEARN:
                return "YetToLearn";
            case WITH_HINTS:
                return "WithHints";
            case ALMOST_LEARNED:
                return "Almost";
            case LEARNED:
                return "Learned";
            default:
                assert false;
                return "";
        }
    }

    public int toInt()
    {
        return mValue;
    }

    public static Mark fromInt(int value)
    {
        for (Mark m: values())
        {
            if (m.toInt() == value)
            {
                return m;
            }
        }
        assert false;
        return Mark.UNFAMILIAR;
    }
}
