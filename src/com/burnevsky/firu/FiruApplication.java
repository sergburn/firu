package com.burnevsky.firu;

import java.io.File;

import com.burnevsky.firu.model.Dictionary;

import android.app.Application;
import android.os.Environment;
import android.util.Log;

public class FiruApplication extends Application
{
    Dictionary mDict = null;
    
    String mDictPath = null;
    boolean mDictPathExists = false;
    
    public void onCreate()
    {
        super.onCreate();
        
        File d = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        mDictPath = d.getAbsolutePath();
        mDictPathExists = d.exists();
        Log.i("firu", "Path to Downloads: " + mDictPath + (mDictPathExists ? " exists" : " invalid"));
    }
}
