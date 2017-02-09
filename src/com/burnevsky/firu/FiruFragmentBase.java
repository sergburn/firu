
package com.burnevsky.firu;

import com.burnevsky.firu.model.DictionaryID;
import com.burnevsky.firu.model.Model;

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
    private InputMethodManager mInputManager;

    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);
        mInputManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        mApp = (FiruApplication) activity.getApplicationContext();
        mModel = mApp.mModel;
        mModel.subscribeDictionaryEvents(this);
    }

    @Override
    public void onDictionaryEvent(DictionaryID dictionaryID, Model.ModelEvent event)
    {
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
                mInputManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }
    }
}
