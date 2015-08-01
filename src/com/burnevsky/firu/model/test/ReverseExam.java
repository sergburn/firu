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

package com.burnevsky.firu.model.test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import android.util.Log;

import com.burnevsky.firu.model.Mark;
import com.burnevsky.firu.model.Translation;
import com.burnevsky.firu.model.Vocabulary;
import com.burnevsky.firu.model.MarkedTranslation;
import com.burnevsky.firu.model.Word;

public class ReverseExam
{
    static final int K_NUM_TESTS = 7;

    private Vocabulary mVoc = null;
    ArrayList<Word> mChallenges = null;
    private int mNextTest = 0;

    public ReverseExam(Vocabulary voc, Mark maxMark)
    {
        mVoc = voc;
        mChallenges = new ArrayList<Word>();

        List<MarkedTranslation> toLearnItems = voc.selectTranslations(Mark.YetToLearn, maxMark, true);

        if ((toLearnItems.size() >= K_NUM_TESTS) ||                             // normal test
            (!maxMark.lessThan(Mark.Learned) && toLearnItems.size() > 0))       // review test
        {
            Random rand = new Random();
            for (int i = 0; (i < K_NUM_TESTS) && (toLearnItems.size()) > 0; i++)
            {
                int k = rand.nextInt(toLearnItems.size());
                MarkedTranslation t = toLearnItems.get(k);
                Word w = voc.getWord(t.getWordID());
                w.translations = new ArrayList<Translation>();
                w.translations.add(t);
                mChallenges.add(w);
                toLearnItems.remove(k);
            }
        }
        else
        {
            Log.d("firu", String.format(
                "Too few (%1$d) translations with marks in range (%2$s, %3$s)",
                toLearnItems.size(), Mark.YetToLearn, maxMark));
        }
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
            Word w = mChallenges.get(mNextTest++);
            MarkedTranslation t = (MarkedTranslation) w.translations.get(0);
            ReverseTest test = new ReverseTest(mVoc, t, w);
            return test;
        }
        else
        {
            return null;
        }
    }

    public ArrayList<Word> getResults()
    {
        return new ArrayList<Word>(mChallenges.subList(0, mNextTest));
    }
}
