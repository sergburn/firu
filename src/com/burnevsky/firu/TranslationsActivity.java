package com.burnevsky.firu;

import java.util.ArrayList;
import java.util.List;

import com.burnevsky.firu.model.Dictionary;
import com.burnevsky.firu.model.TranslationBase;
import com.burnevsky.firu.model.WordBase;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class TranslationsActivity extends Activity
{
    public final static String INTENT_EXTRA_WORD = "com.burnevsk.firu.word";

    Context mSelfContext = null;
    Dictionary mDict = null;
    WordBase mWord = null;

    TextView mWordView = null;
    ListView mTransView = null;

    class DictionaryTranslations extends AsyncTask<WordBase, Void, List<TranslationBase>>
    {
        @Override
        protected List<TranslationBase> doInBackground(WordBase... param)
        {
            return mDict.getTranslations(param[0]);
        }

        @Override
        protected void onPostExecute(List<TranslationBase> result)
        {
            ArrayAdapter<TranslationBase> adapter = new ArrayAdapter<TranslationBase>(mSelfContext,
                    android.R.layout.simple_list_item_1, result);
            mTransView.setAdapter(adapter);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_translations);
        mWordView = (TextView) findViewById(R.id.laWord);
        mTransView = (ListView) findViewById(R.id.listTranslations);
        mSelfContext = this;

        FiruApplication app = (FiruApplication) getApplicationContext(); 
        mDict = app.mDict;

        Intent intent = getIntent();
        mWord = intent.getParcelableExtra(INTENT_EXTRA_WORD);
        mWordView.setText(mWord.getText());

        new DictionaryTranslations().execute(mWord);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.translations, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings)
        {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
