package com.burnevsky.firu.model;

import com.burnevsky.firu.model.test.TestResult;
import com.burnevsky.firu.model.test.VocabularyTest;

import org.junit.Test;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.is;

public class MarkTest
{
    @Test
    public void testConstants()
    {
        assertEquals(0, Mark.UNFAMILIAR.toInt());
        assertEquals(1, Mark.YET_TO_LEARN.toInt());
        assertEquals(2, Mark.WITH_HINTS.toInt());
        assertEquals(3, Mark.ALMOST_LEARNED.toInt());
        assertEquals(4, Mark.LEARNED.toInt());

        assertEquals(Mark.UNFAMILIAR, Mark.fromInt(0));
        assertEquals(Mark.YET_TO_LEARN, Mark.fromInt(1));
        assertEquals(Mark.WITH_HINTS, Mark.fromInt(2));
        assertEquals(Mark.ALMOST_LEARNED, Mark.fromInt(3));
        assertEquals(Mark.LEARNED, Mark.fromInt(4));
    }

    @Test
    public void testUpgrade() throws Exception
    {
        Mark mark = Mark.UNFAMILIAR.upgrade();
        assertEquals(Mark.YET_TO_LEARN, mark);
        mark = mark.upgrade();
        assertEquals(Mark.WITH_HINTS, mark);
        mark = mark.upgrade();
        assertEquals(Mark.ALMOST_LEARNED, mark);
        mark = mark.upgrade();
        assertEquals(Mark.LEARNED, mark);
        mark = mark.upgrade();
        assertEquals(Mark.LEARNED, mark);
        // doesn't go above that
    }

    @Test
    public void testDowngrade() throws Exception
    {
        Mark mark = Mark.LEARNED.downgrade();
        assertEquals(Mark.ALMOST_LEARNED, mark);
        mark = mark.downgrade();
        assertEquals(Mark.WITH_HINTS, mark);
        mark = mark.downgrade();
        assertEquals(Mark.YET_TO_LEARN, mark);
        mark = mark.downgrade();
        assertEquals(Mark.YET_TO_LEARN, mark);
        // doesn't go below that (i.e. to UNFAMILIAR)
    }

    @Test
    public void testUpdateToTestResult() throws Exception
    {
        assertEquals(Mark.UNFAMILIAR,
            VocabularyTest.updateMarkToTestResult(Mark.UNFAMILIAR, TestResult.Passed));

        assertEquals(Mark.WITH_HINTS,
            VocabularyTest.updateMarkToTestResult(Mark.YET_TO_LEARN, TestResult.Passed));

        assertEquals(Mark.ALMOST_LEARNED,
            VocabularyTest.updateMarkToTestResult(Mark.WITH_HINTS, TestResult.Passed));

        assertEquals(Mark.LEARNED,
            VocabularyTest.updateMarkToTestResult(Mark.ALMOST_LEARNED, TestResult.Passed));

        assertEquals(Mark.LEARNED,
            VocabularyTest.updateMarkToTestResult(Mark.LEARNED, TestResult.Passed));

        assertEquals(Mark.YET_TO_LEARN,
            VocabularyTest.updateMarkToTestResult(Mark.LEARNED, TestResult.Failed));

        assertEquals(Mark.WITH_HINTS,
            VocabularyTest.updateMarkToTestResult(Mark.LEARNED, TestResult.PassedWithHints));

        assertEquals(Mark.YET_TO_LEARN,
            VocabularyTest.updateMarkToTestResult(Mark.ALMOST_LEARNED, TestResult.Failed));

        assertEquals(Mark.WITH_HINTS,
            VocabularyTest.updateMarkToTestResult(Mark.ALMOST_LEARNED, TestResult.PassedWithHints));

        assertEquals(Mark.YET_TO_LEARN,
            VocabularyTest.updateMarkToTestResult(Mark.WITH_HINTS, TestResult.Failed));

        assertEquals(Mark.WITH_HINTS,
            VocabularyTest.updateMarkToTestResult(Mark.WITH_HINTS, TestResult.PassedWithHints));
    }
}