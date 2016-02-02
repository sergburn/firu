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

import com.burnevsky.firu.model.Vocabulary;

import java.util.ArrayList;

public class ReverseExam
{
    Vocabulary mVoc;

    private int mNextTest = 0;

    ArrayList<ReverseExamChallenge> mChallenges = new ArrayList<>();

    ReverseExam(Vocabulary voc)
    {
        mVoc = voc;
    }

    public int getTestsCount()
    {
        return mChallenges.size();
    }

    public int getTestsToGo()
    {
        return mChallenges.size() - mNextTest;
    }

    public int getExamProgress()
    {
        if (mChallenges.size() > 0)
        {
            return mNextTest * 100 / mChallenges.size();
        }
        else
        {
            return 100;
        }
    }

    public ReverseTest nextTest()
    {
        if (getTestsToGo() > 0)
        {
            ReverseExamChallenge challenge = mChallenges.get(mNextTest++);
            return new ReverseTest(mVoc, challenge.mTranslation, challenge.mWord);
        }
        else
        {
            return null;
        }
    }

    public ArrayList<ReverseExamChallenge> getResults()
    {
        return new ArrayList<>(mChallenges.subList(0, mNextTest));
    }
}
