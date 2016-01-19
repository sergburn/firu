
package com.burnevsky.firu;

import com.burnevsky.firu.model.DictionaryID;
import com.burnevsky.firu.model.Model;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

@SuppressLint("Registered")
public class FiruActivityBase extends AppCompatActivity implements Model.ModelListener
{
    FiruApplication mApp = null;
    Model mModel;

    @Override
    public void onDictionaryEvent(DictionaryID dictionaryID, Model.ModelEvent event)
    {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        mApp = (FiruApplication) getApplicationContext();

        mModel = mApp.mModel;
        mModel.subscribeDictionaryEvents(this);
    }

    protected void hideKeyboard()
    {
        View view = getCurrentFocus();
        if (view != null)
        {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}
