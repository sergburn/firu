/*******************************************************************************
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Sergey Burnevsky
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *******************************************************************************/

package com.burnevsky.firu;

import java.util.List;

import com.burnevsky.firu.model.Dictionary;
import com.burnevsky.firu.model.Translation;
import com.burnevsky.firu.model.Vocabulary;
import com.burnevsky.firu.model.Word;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class TranslationsActivity extends Activity
{
    public final static String INTENT_EXTRA_WORD = "com.burnevsk.firu.word";

    TextView mWordView = null;
    ListView mTransView = null;
    Drawable mStarredIcon = null, mUnstarredIcon = null;
    Context mSelfContext = null;

    Word mWord = null, mVocWord = null;

    ImageButton mStarBtn = null;
    FiruApplication mApp = null;
    Dictionary mDict = null;
    Vocabulary mVoc = null;
    FiruApplication.ModelListener mModelListener = null;

    class ModelListener implements FiruApplication.ModelListener
    {
        @Override
        public void onVocabularyOpen(Vocabulary voc)
        {
            mVoc = voc;
            new VocabularyMatch().execute(mWord);
            // keep StarBtn disabled until we have result on vocabulary match
        }
        
        @Override
        public void onVocabularyReset(Vocabulary voc)
        {
        }
        
        @Override
        public void onVocabularyClose(Vocabulary voc)
        {
            mVoc = null;
        }
        
        @Override
        public void onDictionaryOpen(Dictionary dict)
        {
            mDict = dict;
            new DictionaryTranslations().execute(mWord);
        }
        
        @Override
        public void onDictionaryClose(Dictionary dict)
        {
            mDict = null;
        }
    }
    
    class DictionaryTranslations extends AsyncTask<Word, Void, List<Translation>>
    {
        @Override
        protected List<Translation> doInBackground(Word... param)
        {
            return (mDict != null) ? mDict.getTranslations(param[0]) : null;
        }

        @Override
        protected void onPostExecute(List<Translation> result)
        {
            mWord.translations = result;
            ArrayAdapter<Translation> adapter = null;
            if (result != null)
            {
                adapter = new ArrayAdapter<Translation>(mSelfContext, android.R.layout.simple_list_item_1, result);
            }
            mTransView.setAdapter(adapter);
        }
    };

    class VocabularyMatch extends AsyncTask<Word, Void, Word>
    {
        @Override
        protected Word doInBackground(Word... param)
        {
            return (mVoc != null) ? mVoc.findWord(param[0].getText(), param[0].getLangCode()) : null;
        }

        @Override
        protected void onPostExecute(Word word)
        {
            mVocWord = word;
            mStarBtn.setEnabled(mVoc != null);
            updateStarButton(mVocWord != null);
        }
    };

    class VocabularyAdd extends AsyncTask<Void, Void, Word>
    {
        @Override
        protected Word doInBackground(Void... param)
        {
            if (mVoc == null) return null;
            try
            {
                return mVoc.addWord(mWord, mWord.translations);
            }
            catch (Exception e)
            {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(Word word)
        {
            mVocWord = word;
            updateStarButton(mVocWord != null);
        }
    };

    class VocabularyRemove extends AsyncTask<Void, List<Translation>, Boolean>
    {
        @Override
        protected Boolean doInBackground(Void... param)
        {
            return (mVoc != null) ? mVoc.removeWord(mVocWord) : true;
        }

        @Override
        protected void onPostExecute(Boolean removed)
        {
            if (removed)
            {
                mVocWord = null;
            }
            updateStarButton(mVocWord != null);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_translations);
        mWordView = (TextView) findViewById(R.id.laWord);
        mStarBtn = (ImageButton) findViewById(R.id.btnStar); 
        mStarBtn.setEnabled(false); // until vocabulary is open
        mTransView = (ListView) findViewById(R.id.listTranslations);
        mStarredIcon = getResources().getDrawable(R.drawable.ic_action_important);
        mUnstarredIcon = getResources().getDrawable(R.drawable.ic_action_not_important);
        mSelfContext = this;

        mApp = (FiruApplication) getApplicationContext();
        mModelListener = new ModelListener();
        mApp.subscribeDictionary(mSelfContext, mModelListener);
        mApp.subscribeVocabulary(mSelfContext, mModelListener);
        
        Intent intent = getIntent();
        mWord = intent.getParcelableExtra(INTENT_EXTRA_WORD);
        mWordView.setText(mWord.getText());
        
        mStarBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (mVocWord == null)
                {
                    new VocabularyAdd().execute();
                }
                else
                {
                    new VocabularyRemove().execute();
                }
            }
        });
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
    
    private void updateStarButton(boolean isStarred)
    {
        mStarBtn.setImageResource(
                isStarred ?
                R.drawable.ic_action_important : 
                R.drawable.ic_action_not_important);
    }
}
