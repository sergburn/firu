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

package com.burnevsky.firu.model.test;

import com.burnevsky.firu.model.Mark;
import com.burnevsky.firu.model.MarkedTranslation;
import com.burnevsky.firu.model.Vocabulary;
import com.burnevsky.firu.model.Word;

public class ReverseTest extends VocabularyTest
{
    private Vocabulary mVoc = null;
    private MarkedTranslation mChallenge = null;
    private String mAnswer = null;

    ReverseTest(Vocabulary vocabulary, MarkedTranslation challenge, Word answer)
    {
        mVoc = vocabulary;
        mAnswer = answer.getText();
        mChallenge = challenge;
    }

    public String getChallenge()
    {
        return mChallenge.getText();
    }

    public String getAnswer()
    {
        if (mResult != TestResult.Incomplete)
        {
            return mAnswer;
        }
        return null;
    }

    public Mark getMark()
    {
        return mChallenge.ReverseMark;
    }

    public void unlockAnswer()
    {
        if (mResult == TestResult.Incomplete)
        {
            finalizeTest(false);
            saveResult(mResult);
        }
    }

    /** @return guess followed by the next letter in answer, if answer starts with guess and is longer;
     *          guess - otherwise */
    public String getHint(String guess) throws TestAlreadyCompleteException
    {
        ensureIncomplete();

        if (mAnswer.startsWith(guess))
        {
            if (revokeHint() && (guess.length() < mAnswer.length()))
            {
                return mAnswer.substring(0, guess.length() + 1);
            }
        }
        return guess;
    }

    /** User is still typing the answer and thinks it is not yet complete.
     *  Even if guess is not correct, it does not affect number of hints.
     * @return true if guess is correct, false otherwise  */
    public boolean checkGuess(String guess)
    {
        return (mAnswer.startsWith(guess));
    }

    /** User thinks answer is complete and finished typing by pressing Enter/Done etc.
     * @return true if guess is correct. Test is then finished;
     *         false if guess is not correct. Test may become finished, if no more hints left, or continue otherwise. */
    public boolean checkAnswer(String guess) throws TestAlreadyCompleteException
    {
        ensureIncomplete();

        if (mAnswer.equals(guess))
        {
            finalizeTest(true);
            saveResult(mResult);
            return true;
        }
        else
        {
            return revokeHint();
        }
    }

    private void saveResult(TestResult result)
    {
        mChallenge.ReverseMark.updateToTestResult(result);
        mVoc.updateMarks(mChallenge);
    }
}
