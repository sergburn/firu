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

import com.burnevsky.firu.model.Mark;
import com.burnevsky.firu.model.MarkedTranslation;
import com.burnevsky.firu.model.Text;
import com.burnevsky.firu.model.Vocabulary;
import com.burnevsky.firu.model.Word;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ForwardTest extends VocabularyTest
{
    Vocabulary mVoc = null;
    Word mChallenge = null;
    MarkedTranslation mAnswer = null;
    List<String> mVariants = new ArrayList<>();

    final static int K_NUM_VARIANTS = 5;

    ForwardTest(Vocabulary vocabulary, Word challenge, MarkedTranslation answer, List<String> variants)
    {
        mVoc = vocabulary;
        mAnswer = answer;
        mChallenge = challenge;

        Exam.selectRandomItems(K_NUM_VARIANTS-1, variants, mVariants);

        int k = Exam.mRand.nextInt(mVariants.size()+1); // may be < K_NUM_VARIANTS
        mVariants.add(k, mAnswer.getText());
    }

    public String getChallenge()
    {
        return mChallenge.getText();
    }

    public String getAnswer()
    {
        if (mResult != TestResult.Incomplete)
        {
            return mAnswer.getText();
        }
        return null;
    }

    public List<String> getVariants()
    {
        return mVariants;
    }

    public Mark getMark()
    {
        return mAnswer.ForwardMark;
    }

    /** User selects one of offered variants
     * @return true if guess is correct. Test is then finished;
     *         false if guess is not correct. Test may become finished, if no more hints left, or continue otherwise. */
    public boolean checkAnswer(String guess) throws TestAlreadyCompleteException
    {
        ensureIncomplete();

        if (mAnswer.getText().equals(guess))
        {
            finalizeTest(true);
            saveResult(mResult);
            return true;
        }
        else
        {
            revokeHint();
            return false;
        }
    }

    protected void saveResult(TestResult result)
    {
        mAnswer.ForwardMark = VocabularyTest.updateMarkToTestResult(mAnswer.ForwardMark, result);
        mVoc.updateMarks(mAnswer);
    }
}
