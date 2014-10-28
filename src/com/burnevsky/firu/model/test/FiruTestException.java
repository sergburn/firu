package com.burnevsky.firu.model.test;

public class FiruTestException extends Exception
{
    private static final long serialVersionUID = 8852271766860391932L;

    public FiruTestException() {
    }

    public FiruTestException(String detailMessage) {
        super(detailMessage);
    }

    public FiruTestException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public FiruTestException(Throwable throwable) {
        super(throwable);
    }
}
