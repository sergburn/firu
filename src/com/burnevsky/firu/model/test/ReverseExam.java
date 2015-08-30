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
    private static final String TAG = ReverseExam.class.getName();
    private static final int K_NUM_TESTS = 7;
    private static final int K_NUM_UNLEARNED = 5;

    private Vocabulary mVoc = null;
    ArrayList<Word> mChallenges = null;
    private int mNextTest = 0;

    public ReverseExam(Vocabulary voc)
    {
        mVoc = voc;
        mChallenges = new ArrayList<Word>();

        Random rand = new Random();

        // All target words should be unique
        List<Long> usedWords = new ArrayList<Long>();

        // First select from translations that are not completely learned yet
        List<MarkedTranslation> translations = voc.selectTranslations(Mark.YetToLearn, Mark.AlmostLearned, true);
        selectTranslations(translations, K_NUM_UNLEARNED, rand, usedWords);

        // Next select some unlearned words
        translations = voc.selectTranslations(Mark.Learned, Mark.Learned, true);
        selectTranslations(translations, K_NUM_TESTS, rand, usedWords);

        // Now shuffle them
        for (int i = mChallenges.size() - 1; i > 0; i--)
        {
            int j = rand.nextInt(i+1);

            // swap
            if (i != j)
            {
                Word w = mChallenges.get(i);
                mChallenges.set(i, mChallenges.get(j));
                mChallenges.set(j, w);
            }
        }
    }

    private void selectTranslations(List<MarkedTranslation> items, int maxCount, Random rand, List<Long> usedWords)
    {
        while (mChallenges.size() < maxCount && items.size() > 0)
        {
            int k = rand.nextInt(items.size());
            MarkedTranslation t = items.get(k);

            if (usedWords == null ||
                !usedWords.contains(t.getWordID())) // translation of the same word
            {
                Word w = mVoc.getWord(t.getWordID());
                w.translations = new ArrayList<Translation>();
                w.translations.add(t);
                mChallenges.add(w);

                if (usedWords != null)
                {
                    usedWords.add(t.getWordID());
                }
            }
            items.remove(k);
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
