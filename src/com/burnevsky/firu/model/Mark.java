package com.burnevsky.firu.model;

import com.burnevsky.firu.model.test.TestResult;

import android.util.Log;

public class Mark {

    private int mValue;
    
    public Mark(int value) {
	mValue = value;
    }
    
    private static final int UnfamiliarValue = 0;
    private static final int YetToLearnValue = 1;
    private static final int WithHintsValue = 2;
    private static final int AlmostLearnedValue = 3;
    private static final int LearnedValue = 4;

    public static Mark Unfamiliar = new Mark(UnfamiliarValue);
    public static Mark YetToLearn = new Mark(YetToLearnValue);
    public static Mark WithHints = new Mark(WithHintsValue);
    public static Mark AlmostLearned = new Mark(AlmostLearnedValue);
    public static Mark Learned = new Mark(LearnedValue);

    public void upgrade() {
	if (mValue < LearnedValue) {
	    mValue += 1;
	}
    }

    public void downgrade() {
	if (mValue > YetToLearnValue) {
	    mValue -= 1;
	}
    }

    public void updateToTestResult(TestResult result) {
	int oldValue = mValue;
	switch (result) {
	    case Passed:
		if (mValue == YetToLearnValue)
		    mValue = AlmostLearnedValue;
		else
		    upgrade();
		break;

	    case PassedWithHints:
		mValue = WithHintsValue;
		break;

	    case Failed:
		mValue = YetToLearnValue;
		break;

	    default:
		Log.e("Mark",
			String.format(
				"Unexpected test result %s in Mark::updateToTestResult",
				result));
		break;
	}
	Log.d("Mark",
		String.format("Mark changed from %d to %d", oldValue, mValue));
    }

    public String toString() {
	switch (mValue) {
	    case YetToLearnValue:
		return "learning";
	    case WithHintsValue:
		return "hints needed";
	    case AlmostLearnedValue:
		return "well known";
	    case LearnedValue:
		return "learned";
	    default:
		return "<none>";
	}
    }
}
