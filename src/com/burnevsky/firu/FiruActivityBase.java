
package com.burnevsky.firu;

import com.burnevsky.firu.model.Dictionary;
import com.burnevsky.firu.model.Model;
import com.burnevsky.firu.model.Vocabulary;

import android.annotation.SuppressLint;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

@SuppressLint("Registered")
public class FiruActivityBase extends AppCompatActivity implements Model.ModelListener
{
    FiruApplication mApp = null;
    Model mModel;

    @Override
    public void onDictionaryEvent(Dictionary dict, Model.ModelEvent event)
    {
    }

    @Override
    public void onVocabularyEvent(Vocabulary voc, Model.ModelEvent event)
    {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        mApp = (FiruApplication) getApplicationContext();

        mModel = mApp.mModel;
    }

    protected void subscribeDictionary()
    {
        mModel.subscribeDictionary(this);
    }

    protected void subscribeVocabulary()
    {
        mModel.subscribeVocabulary(this);
    }
}
