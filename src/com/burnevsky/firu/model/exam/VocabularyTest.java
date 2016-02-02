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

package com.burnevsky.firu.model.exam;

import android.util.Log;

import com.burnevsky.firu.model.Mark;

public class VocabularyTest
{
    protected static final int KMaxHints = 3;
    protected int mHints = KMaxHints;
    protected TestResult mResult = TestResult.Incomplete;

    public TestResult getResult()
    {
        return mResult;
    }

    public int getHintsLeft()
    {
        return mHints;
    }

    public boolean isComplete()
    {
        return !mResult.equals(TestResult.Incomplete);
    }

    /** @return Whether test is finished or continues. */
    public boolean revokeHint() throws TestAlreadyCompleteException
    {
        ensureIncomplete();

        if (mHints > 0)
        {
            --mHints;
            return true;
        }
        else
        {
            finalizeTest(false);
            return false;
        }
    }

    protected void finalizeTest(boolean passed)
    {
        assert mResult == TestResult.Incomplete;

        if (!passed)
        {
            mHints = 0;
            mResult = TestResult.Failed;
        }
        else
        {
            mResult = (mHints < KMaxHints) ? TestResult.PassedWithHints : TestResult.Passed;
        }
    }

    protected void ensureIncomplete() throws TestAlreadyCompleteException
    {
        if (!mResult.equals(TestResult.Incomplete))
        {
            throw new TestAlreadyCompleteException();
        }
    }

    public static Mark updateMarkToTestResult(Mark oldMark, TestResult result)
    {
        if (oldMark != Mark.UNFAMILIAR)
        {
            switch (result)
            {
                case Passed:
                    return oldMark.upgrade();

                case PassedWithHints:
                    return Mark.WITH_HINTS;

                case Failed:
                    return Mark.YET_TO_LEARN;

                default:
                    Log.e("Mark", String.format("Unexpected test result %s in Mark::updateToTestResult", result));
                    break;
            }
        }
        return Mark.UNFAMILIAR;
    }
}
