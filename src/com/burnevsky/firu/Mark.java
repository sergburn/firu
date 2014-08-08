package com.burnevsky.firu;

import android.util.Log;

public class Mark {

	public static final int UknownWord = 0;
	public static final int YetToLearn = 1;
	public static final int WithHints = 2;
	public static final int AlmostLearned = 3;
	public static final int Learned = 4;

    public static int Upgrade(int current)
    {
        if (current < Learned)
        {
            return current + 1;
        }
        return current;
    }

    public static int Downgrade(int current)
    {
        if (current > YetToLearn)
        {
            return current - 1;
        }
        return current;
    }

    public static int UpdateToTestResult(int current, Test.Result result)
    {
        int updated = current;
        switch (result)
        {
            case Test.Result.Passed:
                if (current == YetToLearn)
                    updated = AlmostLearned;
                else
                    updated = Upgrade(current);
                break;

            case Test.PassedWithHints:
                updated = WithHints;
                break;

            case Test.Failed:
                updated = YetToLearn;
                break;

            default:
                Log.e("Mark", String.format("Unexpected test result %s in Mark::updateToTestResult", result));
                break;
        }
        Log.d("Mark", String.format("Mark changed from %d to %d", current, updated));
        return updated;
    }

    public static String ToString(int mark)
    {
        switch (mark)
        {
            case YetToLearn:
                return "learning";
            case WithHints:
                return "hints needed";
            case AlmostLearned:
                return "well known";
            case Learned:
                return "learned";
            default:
                return "<none>";
        }
    }
}
