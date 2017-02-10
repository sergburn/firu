
package com.burnevsky.firu;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.burnevsky.firu.model.DictionaryID;
import com.burnevsky.firu.model.Model;

import java.util.TreeMap;

@SuppressLint("Registered")
public class FiruActivityBase extends AppCompatActivity implements
    Model.ModelListener, ActivityCompat.OnRequestPermissionsResultCallback
{
    private static final String TAG = "firu/FiruActivityBase";

    FiruApplication mApp = null;
    Model mModel;
    Handler mHandler = new Handler();

    private InputMethodManager mInputManager;

    protected interface OnPermissionCheckCallback
    {
        void onGranted();
        void onDenied();
    }

    private TreeMap<Integer, OnPermissionCheckCallback> mPermissionTasks = new TreeMap<>();
    private int mPermissionRequestCounter = 0;

    @Override
    public void onDictionaryEvent(DictionaryID dictionaryID, Model.ModelEvent event)
    {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        mInputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        mApp = (FiruApplication) getApplicationContext();

        mModel = mApp.mModel;
        mModel.subscribeDictionaryEvents(this);
    }

    protected void hideKeyboard()
    {
        View view = getCurrentFocus();
        if (view != null)
        {
            mInputManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    protected boolean isAllPermissionsGranted(String[] permissions)
    {
        boolean allGranted = true;
        for (int i = 0; (i < permissions.length) && allGranted; i++)
        {
            allGranted &= PackageManager.PERMISSION_GRANTED ==
                ContextCompat.checkSelfPermission(this, permissions[i]);
        }
        return allGranted;
    }

    protected boolean isAllPermissionsGranted(
        String[] permissions,
        int[] results)
    {
        for (int i = 0; i < results.length; i++)
        {
            if (results[i] != PackageManager.PERMISSION_GRANTED)
            {
                Log.v(TAG, String.format("isAllPermissionsGranted: %s -> %d", permissions[i], results[i]));
                return false;
            }
        }
        return (results.length > 0);
    }

    protected void checkPermissions(String[] permissions, final OnPermissionCheckCallback callback)
    {
        if (isAllPermissionsGranted(permissions))
        {
            mHandler.post(new Runnable()
            {
                @Override
                public void run()
                {
                    callback.onGranted();
                }
            });
        }
        else
        {
            mPermissionTasks.put(++mPermissionRequestCounter, callback);
            ActivityCompat.requestPermissions(this, permissions, mPermissionRequestCounter);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] results)
    {
        final OnPermissionCheckCallback callback = mPermissionTasks.remove(requestCode);
        final boolean granted = isAllPermissionsGranted(permissions, results);
        Log.v(TAG, String.format("onRequestPermissionsResult: %b", granted));
        mHandler.post(new Runnable()
        {
            @Override
            public void run()
            {
                if (granted)
                {
                    callback.onGranted();
                }
                else
                {
                    callback.onDenied();
                }
            }
        });
    }
}
