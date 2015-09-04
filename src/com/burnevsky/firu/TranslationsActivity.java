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
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class TranslationsActivity extends FiruActivityBase
{
    public final static String INTENT_EXTRA_DICT_WORD = "com.burnevsk.firu.dict_word";
    public final static String INTENT_EXTRA_VOC_WORD = "com.burnevsk.firu.voc_word";

    TextView mWordView = null;
    ListView mTransView = null;
    ImageView mStarBtn = null;
    Drawable mStarredIcon = null, mUnstarredIcon = null;

    Word mDictWord = null, mVocWord = null;

    @Override
    public void onVocabularyOpen(Vocabulary voc)
    {
        super.onVocabularyOpen(voc);
        if (mVocWord != null)
        {
            new VocabularyTranslations().execute(mVocWord);
        }
        else if (mDictWord != null)
        {
            new VocabularyMatch().execute(mDictWord);
        }
    }

    @Override
    public void onDictionaryOpen(Dictionary dict)
    {
        super.onDictionaryOpen(dict);
        if (mDictWord != null)
        {
            new DictionaryTranslations().execute(mDictWord);
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
            mDictWord.translations = result;
            fillTranslationsList(mDictWord.translations);
        }
    };

    class VocabularyTranslations extends AsyncTask<Word, Void, List<Translation>>
    {
        @Override
        protected List<Translation> doInBackground(Word... param)
        {
            return (mVoc != null) ? mVoc.getTranslations(param[0]) : null;
        }

        @Override
        protected void onPostExecute(List<Translation> result)
        {
            mVocWord.translations = result;
            fillTranslationsList(mVocWord.translations);
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
            mStarBtn.setVisibility(mVoc != null ? View.VISIBLE : View.INVISIBLE);
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
                if (mDictWord != null)
                {
                    return mVoc.addWord(mDictWord, mDictWord.translations);
                }
                else
                {
                    return mVoc.addWord(mVocWord, mVocWord.translations);
                }
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
                mVocWord.unlink();
                updateStarButton(false);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_translations);

        mWordView = (TextView) findViewById(R.id.laWord);
        mStarBtn = (ImageView) findViewById(R.id.btnStar);
        mTransView = (ListView) findViewById(R.id.listTranslations);
        mStarredIcon = getResources().getDrawable(R.drawable.ic_action_important_dark);
        mUnstarredIcon = getResources().getDrawable(R.drawable.ic_action_not_important_dark);

        Intent intent = getIntent();
        mDictWord = intent.getParcelableExtra(INTENT_EXTRA_DICT_WORD);
        if (mDictWord != null)
        {
            mWordView.setText(mDictWord.getText());
            // translations will be loaded when dictionary is ready
            mStarBtn.setVisibility(View.INVISIBLE); // until vocabulary is open
        }
        else
        {
            mVocWord = intent.getParcelableExtra(INTENT_EXTRA_VOC_WORD);
            if (mVocWord != null)
            {
                mWordView.setText(mVocWord.getText());
                // translations will be loaded when vocabulary is ready
                mStarBtn.setVisibility(View.VISIBLE); // until vocabulary is open
                updateStarButton(true);
            }
            else
            {
                Log.d("firu", "TranslationsActivity: Unsupported intent given");
                finish();
            }
        }

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
        getMenuInflater().inflate(R.menu.translations_activity_actions, menu);
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
        /*
        mStarBtn.setImageResource(
            isStarred ?
                R.drawable.ic_action_important_dark :
                    R.drawable.ic_action_not_important_dark);
         */
        mStarBtn.setImageDrawable(isStarred ? mStarredIcon : mUnstarredIcon);
    }

    private void fillTranslationsList(List<Translation> list)
    {
        ArrayAdapter<Translation> adapter = null;
        if (list != null)
        {
            adapter = new ArrayAdapter<Translation>(mSelfContext, android.R.layout.simple_list_item_1, list);
        }
        mTransView.setAdapter(adapter);
    }

    public static void showDictWord(Activity caller, Word word)
    {
        Intent intent = new Intent(caller, TranslationsActivity.class);
        intent.putExtra(TranslationsActivity.INTENT_EXTRA_DICT_WORD, word);

        caller.startActivity(intent);
    }

    public static void showVocWord(Activity caller, Word word)
    {
        Intent intent = new Intent(caller, TranslationsActivity.class);
        intent.putExtra(TranslationsActivity.INTENT_EXTRA_VOC_WORD, word);

        caller.startActivity(intent);
    }
}
