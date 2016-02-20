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

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class ReverseExamTest
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

        when(mMockVocabulary.selectWordsByMarks(any(Mark.class), any(Mark.class), eq(true)))
            .thenReturn(null);
        ReverseExam exam = ReverseExam.Builder.buildExam(mMockVocabulary);
        assertNotNull(exam);
        assertEquals(0, exam.mChallenges.size());

        verify(mMockVocabulary, never()).selectWordsByMarks(any(Mark.class), any(Mark.class), eq(false));
    }

    @Test
    public void testReverseTest_getAnswerHint()
    {
        final String LANG = "lang";
        final String CHAL = "challenge";
        final char DUMM = 'x';

        ReverseTest test = new ReverseTest(mMockVocabulary,
            new MarkedTranslation(new Text(CHAL, LANG)),
            new Word(new Text("123", LANG)));

        assertEquals(0, test.getAnswerHint("", DUMM, false).compareTo("xxx"));
        assertEquals(0, test.getAnswerHint("", DUMM, true).compareTo("x2x"));
        assertEquals(0, test.getAnswerHint("1", DUMM, false).compareTo("1xx"));
        assertEquals(0, test.getAnswerHint("1", DUMM, true).compareTo("12x"));
        assertEquals(0, test.getAnswerHint("12", DUMM, false).compareTo("12x"));
        assertEquals(0, test.getAnswerHint("12", DUMM, true).compareTo("12x"));
        assertEquals(0, test.getAnswerHint("123", DUMM, false).compareTo("123"));
        assertEquals(0, test.getAnswerHint("123", DUMM, true).compareTo("123"));

        test = new ReverseTest(mMockVocabulary,
            new MarkedTranslation(new Text(CHAL, LANG)),
            new Word(new Text("1234", LANG)));

        assertEquals(0, test.getAnswerHint("", DUMM, false).compareTo("xxxx"));
        assertEquals(0, test.getAnswerHint("", DUMM, true).compareTo("xx3x"));
        assertEquals(0, test.getAnswerHint("1", DUMM, false).compareTo("1xxx"));
        assertEquals(0, test.getAnswerHint("1", DUMM, true).compareTo("1x3x"));
        assertEquals(0, test.getAnswerHint("12", DUMM, false).compareTo("12xx"));
        assertEquals(0, test.getAnswerHint("12", DUMM, true).compareTo("123x"));
        assertEquals(0, test.getAnswerHint("123", DUMM, false).compareTo("123x"));
        assertEquals(0, test.getAnswerHint("123", DUMM, true).compareTo("123x"));
        assertEquals(0, test.getAnswerHint("1234", DUMM, false).compareTo("1234"));
        assertEquals(0, test.getAnswerHint("1234", DUMM, true).compareTo("1234"));
    }
}
