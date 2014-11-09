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

import com.burnevsky.firu.model.test.TestResult;

import android.util.Log;

public class Mark
{
    private int mValue;

    public Mark(int value)
    {
        mValue = value;
    }

    private static final int UnfamiliarValue = 0;
    private static final int YetToLearnValue = 1;
    private static final int WithHintsValue = 2;
    private static final int AlmostLearnedValue = 3;
    private static final int LearnedValue = 4;

    public static Mark Unfamiliar = new Mark(UnfamiliarValue);
    public static Mark YetToLearn = new Mark(YetToLearnValue);
    public static Mark WithHints = new Mark(WithHintsValue);
    public static Mark AlmostLearned = new Mark(AlmostLearnedValue);
    public static Mark Learned = new Mark(LearnedValue);

    public void upgrade()
    {
        if (mValue < LearnedValue)
        {
            mValue += 1;
        }
    }

    public void downgrade()
    {
        if (mValue > YetToLearnValue)
        {
            mValue -= 1;
        }
    }

    public void updateToTestResult(TestResult result)
    {
        int oldValue = mValue;
        switch (result)
        {
            case Passed:
                if (mValue == YetToLearnValue)
                    mValue = AlmostLearnedValue;
                else
                    upgrade();
                break;

            case PassedWithHints:
                mValue = WithHintsValue;
                break;

            case Failed:
                mValue = YetToLearnValue;
                break;

            default:
                Log.e("Mark", String.format("Unexpected test result %s in Mark::updateToTestResult", result));
                break;
        }
        Log.d("Mark", String.format("Mark changed from %d to %d", oldValue, mValue));
    }

    public String toString()
    {
        switch (mValue)
        {
            case YetToLearnValue:
                return "learning";
            case WithHintsValue:
                return "hints needed";
            case AlmostLearnedValue:
                return "well known";
            case LearnedValue:
                return "learned";
            default:
                return "<none>";
        }
    }
    
    public int toInt()
    {
        return mValue;
    }
}
