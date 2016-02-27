/*******************************************************************************
 * The MIT License (MIT)
 *
 * Copyright (c) 2016 Sergey Burnevsky (sergey.burnevsky at gmail.com)
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

import com.burnevsky.firu.model.Mark;
import com.burnevsky.firu.model.MarkedTranslation;
import com.burnevsky.firu.model.Vocabulary;
import com.burnevsky.firu.model.Word;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Exam
{
    public static Random mRand = new Random();

    protected int mNextTest = 0;
    protected Vocabulary mVoc;
    protected ArrayList<ExamChallenge> mChallenges = new ArrayList<>();

    enum TestDirection
    {
        REVERSE_TEST,
        FORWARD_TEST
    }

    protected TestDirection mDirection;

    public Exam(Vocabulary voc, TestDirection direction)
    {
        mVoc = voc;
        mDirection = direction;
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

    public ArrayList<ExamChallenge> getResults()
    {
        return new ArrayList<>(mChallenges.subList(0, mNextTest));
    }

    // Selects words without duplicates
    protected void selectWords(final Mark min, final Mark max, final int maxCount)
    {
        List<Word> words = mVoc.selectWordsByMarks(min, max, (mDirection == TestDirection.REVERSE_TEST));

        // TODO: use Exam.selectRandomItems();

        while (words != null &&
                mChallenges.size() < maxCount &&
                words.size() > 0)
        {
            int k = mRand.nextInt(words.size());
            Word w = words.get(k);

            boolean exists = false;
            for (ExamChallenge c : mChallenges)
            {
                if (c.mWord.getID() == w.getID())
                {
                    exists = true;
                    break;
                }
            }

            if (!exists)
            {
                List<MarkedTranslation> translations = mVoc.getTranslations(w.getID(), min, max);
                if (translations.size() > 0)
                {
                    int j = mRand.nextInt(translations.size());

                    mChallenges.add(new ExamChallenge(w, translations.get(j), mDirection));
                }
            }
            words.remove(k);
        }
    }

    static <T> void selectRandomItems(int count, final List<T> variants, List<T> selection)
    {
        List<Integer> usedVariants = new ArrayList<>();
        while (!variants.isEmpty() &&
                (selection.size() < count) &&
                (usedVariants.size() < variants.size()))
        {
            int k = mRand.nextInt(variants.size());
            if (!usedVariants.contains(k))
            {
                selection.add(variants.get(k));
                usedVariants.add(k);
            }
        }
    }
}
