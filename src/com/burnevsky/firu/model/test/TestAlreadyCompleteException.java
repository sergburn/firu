package com.burnevsky.firu.model.test;

public class TestAlreadyCompleteException extends FiruTestException
{
    private static final long serialVersionUID = 3291566877991130498L;

    public TestAlreadyCompleteException() {
    }

    public TestAlreadyCompleteException(String detailMessage) {
        super(detailMessage);
    }

    public TestAlreadyCompleteException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public TestAlreadyCompleteException(Throwable throwable) {
        super(throwable);
    }
}
