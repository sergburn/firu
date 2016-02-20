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
import com.burnevsky.firu.model.Text;
import com.burnevsky.firu.model.Vocabulary;
import com.burnevsky.firu.model.Word;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class ForwardExam extends Exam
{
    private static final int K_NUM_TESTS = 7;
    private static final int K_NUM_UNLEARNED = 6;
    private static final int K_NUM_VARIANTS_PER_TEST = 5;

    List<String> mVariants = new ArrayList<>();

    ForwardExam(Vocabulary voc)
    {
        super(voc);
    }

    public ForwardTest nextTest()
    {
        if (getTestsToGo() > 0)
        {
            ExamChallenge challenge = mChallenges.get(mNextTest++);
            return new ForwardTest(mVoc, challenge.mWord, challenge.mTranslation, mVariants);
        }
        else
        {
            return null;
        }
    }

    public static class Builder
    {
        /** Should be run in other thread */
        public static ForwardExam buildExam(Vocabulary voc)
        {
            ForwardExam exam = new ForwardExam(voc);

            // Select some learned words
            exam.selectWords(Mark.LEARNED, Mark.LEARNED, K_NUM_TESTS - K_NUM_UNLEARNED, WordSelection.SELECT_FORWARD_MARKS);

            // Select words with translations that are not completely learned yet
            exam.selectWords(Mark.YET_TO_LEARN, Mark.ALMOST_LEARNED, K_NUM_TESTS, WordSelection.SELECT_FORWARD_MARKS);

            // Now shuffle them
            Collections.shuffle(exam.mChallenges, mRand);

            // Build list of variants, excluding translations of words in the test
            List<Long> exceptionList = new ArrayList<>();
            for (ExamChallenge challenge: exam.mChallenges)
            {
                exceptionList.add(challenge.mWord.getID());
            }
            List<Long> transIds = voc.selectTranslations(Mark.YET_TO_LEARN, false, exceptionList);

            // Select randomly
            List<Long> selectedTransIds = new ArrayList<>();
            Exam.selectRandomItems(K_NUM_VARIANTS_PER_TEST * exam.mChallenges.size(), transIds, selectedTransIds);

            // Load translation texts
            for (Long transId : selectedTransIds)
            {
                MarkedTranslation t = voc.loadTranslation(transId);
                exam.mVariants.add(t.getText());
            }

            return exam;
        }
    }
}
