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
import com.burnevsky.firu.model.Vocabulary.MarkedTranslation;
import com.burnevsky.firu.model.Word;

public class ReverseExam
{
    static final int K_NUM_TESTS = 5;

    private Vocabulary mVoc = null;
    List<Word> mChallenges = null;
    private int mCurrentTest = 0;

    public ReverseExam(Vocabulary voc, Mark maxMark)
    {
        mVoc = voc;
        mChallenges = new ArrayList<Word>();

        List<Vocabulary.MarkedTranslation> toLearnItems = voc.selectTranslations(Mark.YetToLearn, maxMark, true);

        if ((toLearnItems.size() >= K_NUM_TESTS) ||                             // normal test
                (!maxMark.lessThan(Mark.Learned) && toLearnItems.size() > 0))       // review test
        {
            for (int i = 0; (i < K_NUM_TESTS) && (toLearnItems.size()) > 0; i++)
            {
                int k = new Random().nextInt(toLearnItems.size());
                Vocabulary.MarkedTranslation t = toLearnItems.get(k);
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

    public int getTestsToGo()
    {
        return mChallenges.size() - mCurrentTest;
    }

    public int getExamProgress()
    {
        if (mChallenges.size() > 0)
        {
            return mCurrentTest * 100 / mChallenges.size();
        }
        else
        {
            return 100;
        }
    }

    public ReverseTest nextTest()
    {
        if (getTestsToGo() > 1)
        {
            Word w = mChallenges.get(++mCurrentTest);
            MarkedTranslation t = (MarkedTranslation) w.translations.get(0);
            ReverseTest test = new ReverseTest(mVoc, t, w);
            return test;
        }
        else
        {
            return null;
        }
    }
}
