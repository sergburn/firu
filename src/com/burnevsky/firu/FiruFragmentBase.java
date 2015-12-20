
package com.burnevsky.firu;

import com.burnevsky.firu.model.Dictionary;
import com.burnevsky.firu.model.DictionaryID;
import com.burnevsky.firu.model.IDictionary;
import com.burnevsky.firu.model.Model;
import com.burnevsky.firu.model.Vocabulary;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

public class FiruFragmentBase extends Fragment implements Model.ModelListener
{
    FiruApplication mApp = null;
    Model mModel;

    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);
        mApp = (FiruApplication) activity.getApplicationContext();
        mModel = mApp.mModel;
    }

    @Override
    public void onDictionaryEvent(DictionaryID dictionaryID, Model.ModelEvent event)
    {
    }

    protected void subscribeDictionary()
    {
        mModel.subscribeDictionary(DictionaryID.UNIVERSAL, this);
    }

    protected void subscribeVocabulary()
    {
        mModel.subscribeDictionary(DictionaryID.VOCABULARY, this);
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
