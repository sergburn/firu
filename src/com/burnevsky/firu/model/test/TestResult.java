package com.burnevsky.firu.model.test;

public enum TestResult {
    
    Incomplete,
    
    // adds 1 to current rate
    Passed,
    
    // doesn't change rate if 1 or 2, demotes rate 3 to 2
    PassedWithHints,
    
    // sets current rate 1.
    Failed;
    
}
