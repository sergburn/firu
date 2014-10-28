package com.burnevsky.firu.model.test;

public class VocabularyTest
{
    protected static final int KMaxHints = 3; 
    protected int mHints = KMaxHints;
    protected TestResult mResult = TestResult.Incomplete;

    public TestResult getResult()
    {
        return mResult;
    }

    public int getHintsLeft()
    {
        return mHints;
    }

    /** @return Whether test is finished or continues. */
    protected boolean revokeHint()
    {
        assert mResult == TestResult.Incomplete;

        if (mHints > 0)
        {
            --mHints;
            return true;
        }
        else
        {
            finalizeTest(false);
            return false;
        }
    }
    
    protected void finalizeTest(boolean passed)
    {
        assert mResult == TestResult.Incomplete;

        if (!passed)
        {
            mHints = 0;
            mResult = TestResult.Failed;
        }
        else
        {
            mResult = (mHints < KMaxHints) ? TestResult.PassedWithHints : TestResult.Passed;
        }
    }
    
    protected void ensureIncomplete() throws TestAlreadyCompleteException
    {
        if (!mResult.equals(TestResult.Incomplete))
        {
            throw new TestAlreadyCompleteException();
        }
    }
}
