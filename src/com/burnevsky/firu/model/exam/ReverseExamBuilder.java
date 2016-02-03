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

import com.burnevsky.firu.model.Mark;
import com.burnevsky.firu.model.MarkedTranslation;
import com.burnevsky.firu.model.Vocabulary;
import com.burnevsky.firu.model.Word;

import java.util.Collections;
import java.util.List;
import java.util.Random;

public class ReverseExamBuilder
{
    @SuppressWarnings("unused")
    private static final String TAG = ReverseExam.class.getName();

    private static final int K_NUM_TESTS = 7;
    private static final int K_NUM_UNLEARNED = 6;

    private static class BuildOptions
    {
        Vocabulary voc;
        Random rand;
        ReverseExam exam;
    }

    public static ReverseExam buildExam(Vocabulary voc)
    {
        BuildOptions params = new BuildOptions();
        params.voc = voc;
        params.rand = new Random();
        params.exam = new ReverseExam(voc);

        // First select words with translations that are not completely learned yet
        selectWords(Mark.YET_TO_LEARN, Mark.ALMOST_LEARNED, K_NUM_UNLEARNED, params);

        // Next select some learned words
        selectWords(Mark.LEARNED, Mark.LEARNED, K_NUM_TESTS, params);

        // Now shuffle them
        Collections.shuffle(params.exam.mChallenges, params.rand);

        return params.exam;
    }

    private static void selectWords(final Mark min, final Mark max, final int maxCount, BuildOptions options)
    {
        List<Word> words = options.voc.selectWordsByMarks(min, max);

        while (words != null &&
            options.exam.mChallenges.size() < maxCount &&
            words.size() > 0)
        {
            int k = options.rand.nextInt(words.size());
            Word w = words.get(k);

            boolean exists = false;
            for (ReverseExamChallenge c : options.exam.mChallenges)
            {
                if (c.mWord.getID() == w.getID())
                {
                    exists = true;
                    break;
                }
            }

            if (!exists)
            {
                List<MarkedTranslation> translations = options.voc.getTranslations(w.getID(), min, max);

                int j = options.rand.nextInt(translations.size());

                options.exam.mChallenges.add(new ReverseExamChallenge(w, translations.get(j)));
            }
            words.remove(k);
        }
    }
}
