
package com.burnevsky.firu;

import com.burnevsky.firu.model.Dictionary;
import com.burnevsky.firu.model.Model;
import com.burnevsky.firu.model.Vocabulary;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

public class FiruFragmentBase extends Fragment implements Model.ModelListener
{
    FiruApplication mApp = null;
    final Model mModel;

    FiruFragmentBase(Context appContext)
    {
        mApp = (FiruApplication) appContext;
        mModel = mApp.mModel;
    }

    @Override
    public void onDictionaryEvent(Dictionary dict, Model.ModelEvent event)
    {
    }

    @Override
    public void onVocabularyEvent(Vocabulary voc, Model.ModelEvent event)
    {
    }

    protected void subscribeDictionary()
    {
        mModel.subscribeDictionary(this);
    }

    protected void subscribeVocabulary()
    {
        mModel.subscribeVocabulary(this);
    }

    protected void invalidateOptionsMenu()
    {
        Activity act = getActivity();
        if (act != null)
        {
            act.invalidateOptionsMenu();
        }
    }

    protected void makeToast(CharSequence text, int duration)
    {
        Activity act = getActivity();
        if (act != null)
        {
            Toast.makeText(act, text, duration).show();
        }
    }

    protected void hideKeyboard()
    {
        Activity act = getActivity();
        if (act != null)
        {
            View view = act.getCurrentFocus();
            if (view != null)
            {
                InputMethodManager imm = (InputMethodManager) act.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }
    }
}
