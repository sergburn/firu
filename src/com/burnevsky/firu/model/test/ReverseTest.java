package com.burnevsky.firu.model.test;

public class ReverseTest extends VocabularyTest
{
    private String mChallenge = "large";
    private String mAnswer = "iso";
    
    public String getChallenge()
    {
        return mChallenge;
    }
    
    public String getAnswer()
    {
        if (mResult != TestResult.Incomplete)
        {
            return mAnswer;
        }
        return null;
    }
    
    public void unlockAnswer()
    {
        if (mResult == TestResult.Incomplete)
        {
            finalizeTest(false);
        }
    }
    
    /** @return guess followed by the next letter in answer, if answer starts with guess and is longer;
     *          guess - otherwise */
    public String getHint(String guess) throws TestAlreadyCompleteException
    {
        ensureIncomplete();

        if (mAnswer.startsWith(guess))
        {
            revokeHint();
            if (guess.length() < mAnswer.length())
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
            return true;
        }
        else
        {
            return revokeHint();
        }
    }
}
