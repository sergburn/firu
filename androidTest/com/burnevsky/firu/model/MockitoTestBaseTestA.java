package com.burnevsky.firu.model;

import android.test.AndroidTestCase;

import org.junit.Before;
import org.mockito.MockitoAnnotations;

public class MockitoTestBaseTestA //extends AndroidTestCase
{
    @Before
    public void initMocks()
    {
        MockitoAnnotations.initMocks(this);
        //System.setProperty("dexmaker.dexcache", getContext().getCacheDir().getPath());
    }
}
