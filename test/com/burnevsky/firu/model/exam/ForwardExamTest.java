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

import com.burnevsky.firu.model.DictionaryID;
import com.burnevsky.firu.model.Mark;
import com.burnevsky.firu.model.MarkedTranslation;
import com.burnevsky.firu.model.Text;
import com.burnevsky.firu.model.Vocabulary;
import com.burnevsky.firu.model.Word;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ForwardExamTest
{
    Vocabulary mMockVocabulary;

    @Before
    public void setUp()
    {
        mMockVocabulary = mock(Vocabulary.class);
    }

    @Test
    public void testExamBuilder()
    {
        //when(vocabulary.getTranslations()).thenReturn();

        when(mMockVocabulary.selectWordsByMarks(any(Mark.class), any(Mark.class), eq(false)))
                .thenReturn(null);
        ForwardExam exam = ForwardExam.Builder.buildExam(mMockVocabulary);
        assertNotNull(exam);
        assertEquals(0, exam.mChallenges.size());

        verify(mMockVocabulary, never()).selectWordsByMarks(any(Mark.class), any(Mark.class), eq(true));
    }

    private void assertNoDuplicates(List<? extends Comparable> list)
    {
        Collections.sort(list);
        for (int i = 1; i < list.size(); i++)
        {
            assertNotEquals(list.get(i-1), list.get(i));
        }
    }

    @Test
    public void testForwardTestBuilder()
    {
        MarkedTranslation answer = new MarkedTranslation(new Text("answer", "lang"));

        List<String> variants = new ArrayList<>();

        ForwardTest test = new ForwardTest(mMockVocabulary, null, answer, variants);
        assertEquals(1, test.mVariants.size());

        variants.add("wrong-1");

        test = new ForwardTest(mMockVocabulary, null, answer, variants);
        assertEquals(2, test.mVariants.size());
        assertNoDuplicates(test.mVariants);

        variants.add("wrong-2");
        variants.add("wrong-3");

        test = new ForwardTest(mMockVocabulary, null, answer, variants);
        assertEquals(4, test.mVariants.size());
        assertNoDuplicates(test.mVariants);

        variants.add("wrong-4");
        variants.add("wrong-5");

        test = new ForwardTest(mMockVocabulary, null, answer, variants);
        assertEquals(ForwardTest.K_NUM_VARIANTS, test.mVariants.size());
        assertNoDuplicates(test.mVariants);

        //assertTrue(test.checkAnswer("answer"));
    }

}
