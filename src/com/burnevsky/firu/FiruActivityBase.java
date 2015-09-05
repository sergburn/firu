
package com.burnevsky.firu;

import com.burnevsky.firu.model.Dictionary;
import com.burnevsky.firu.model.Vocabulary;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

public class FiruActivityBase extends Activity implements FiruApplication.ModelListener
{
    FiruApplication mApp = null;
    Dictionary mDict = null;
    Vocabulary mVoc = null;

    Context mSelfContext = null;

    @Override
    public void onDictionaryOpen(Dictionary dict)
    {
        mDict = dict;
    }

    @Override
    public void onDictionaryClose(Dictionary dict)
    {
        mDict = null;
    }

    @Override
    public void onVocabularyOpen(Vocabulary voc)
    {
        mVoc = voc;
    }

    @Override
    public void onVocabularyReset(Vocabulary voc)
    {
        mVoc = voc;
    }

    @Override
    public void onVocabularyClose(Vocabulary voc)
    {
        mVoc = null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        mSelfContext = this;

        mApp = (FiruApplication) getApplicationContext();
        mApp.subscribeDictionary(mSelfContext, this);
        mApp.subscribeVocabulary(mSelfContext, this);
    }
}
