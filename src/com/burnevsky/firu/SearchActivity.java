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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.burnevsky.firu.model.Dictionary;
import com.burnevsky.firu.model.Vocabulary;
import com.burnevsky.firu.model.Word;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

public class SearchActivity extends Activity implements SearchView.OnQueryTextListener, OnItemClickListener
{
    private final static int MAX_WORDS_IN_RESULT = 20;

    Context mSelfContext = null;
    Dictionary mDict = null;
    Vocabulary mVoc = null;
    ListView mWordsListView = null;

    TextView mCountText = null;

    class DictionaryOpener extends AsyncTask<Void, Void, Dictionary>
    {
        private String mDbName = null;
        private Context mContext = null;

        public DictionaryOpener(String dbName, Context context)
        {
            mDbName = dbName;
            mContext = context;
        }

        @Override
        protected Dictionary doInBackground(Void... voids)
        {
            return Dictionary.open(mDbName, mContext);
        }

        @Override
        protected void onPostExecute(Dictionary result)
        {
            mDict = result;
            Log.i("firu", "totalWordCount: " + String.valueOf(mDict.getTotalWords()));
            mCountText.setText("Total count " + String.valueOf(mDict.getTotalWords()) + " words");

            FiruApplication app = (FiruApplication) getApplicationContext(); 
            app.mDict = result;
        }
    };

    class VocabularyOpener extends AsyncTask<Void, Void, Vocabulary>
    {
        private String mDbName = null;
        private Context mContext = null;

        public VocabularyOpener(String dbName, Context context)
        {
            mDbName = dbName;
            mContext = context;
        }

        @Override
        protected Vocabulary doInBackground(Void... voids)
        {
            return Vocabulary.open(mDbName, mContext);
        }

        @Override
        protected void onPostExecute(Vocabulary result)
        {
            mVoc = result;
            Log.i("firu", "totalWordCount: " + String.valueOf(mVoc.getTotalWords()));

            FiruApplication app = (FiruApplication) getApplicationContext(); 
            app.mVoc = result;
        }
    };

    class DictionarySearch extends AsyncTask<String, Void, List<Word>>
    {
        @Override
        protected List<Word> doInBackground(String... param)
        {
            return mDict.searchWords(param[0], MAX_WORDS_IN_RESULT);
        }

        @Override
        protected void onPostExecute(List<Word> result)
        {
            ArrayAdapter<Word> adapter = new ArrayAdapter<Word>(mSelfContext,
                    android.R.layout.simple_list_item_1, result);
            mWordsListView.setAdapter(adapter);
            mSearchTask = null;
        }

        @Override
        protected void onCancelled(List<Word> result)
        {
            mSearchTask = null;
        }
    };

    class DictionaryCounter extends AsyncTask<String, Void, Integer>
    {
        @Override
        protected Integer doInBackground(String... param)
        {
            return mDict.countWords(param[0]);
        }

        @Override
        protected void onPostExecute(Integer result)
        {
            mCountText.setText("Found " + result + " words");
        }
    };

    DictionarySearch mSearchTask = null;
    DictionaryCounter mCountTask = null;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        SearchView searchWord = (SearchView) findViewById(R.id.searchWord);
        searchWord.setOnQueryTextListener(this);

        mWordsListView = (ListView) findViewById(R.id.wordList);
        mWordsListView.setOnItemClickListener(this);
        mCountText = (TextView) findViewById(R.id.laCount);
        mSelfContext = this;
        
        FiruApplication app = (FiruApplication) getApplicationContext();
        if (app.mDictPathExists)
        {
            new DictionaryOpener(app.mDictPath + File.separator + "dictionary.sqlite", this).execute();
        }
        else
        {
            new DictionaryOpener("dictionary.sqlite", this).execute();
        }
        
        new VocabularyOpener("vocabulary.sqlite", this).execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.search_activity_actions, menu);
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
            Intent intent = new Intent(this, TrainerActivity.class);
            //intent.putExtra(TrainerActivity.INTENT_EXTRA_WORD, word);

            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onQueryTextSubmit(String query)
    {
        Log.i("firu", "onQueryTextSubmit: " + query);

        if (mSearchTask == null)
        {
            mSearchTask = new DictionarySearch();
            mSearchTask.execute(query);

            new DictionaryCounter().execute(query);
        }
        else
        {
            Log.i("firu", "Another search is running or pending");
        }
        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText)
    {
        Log.i("firu", "onQueryTextChange: " + newText);
        if (newText == null || newText.isEmpty())
        {
            mWordsListView.setAdapter(null);
            mCountText.setText("Total count " + mDict.getTotalWords() + " words");
        }
        return false;
    }

    /** Called when the user clicks the Send button */
    public void showTranslation(AdapterView<?> parent, View view, int position, long id)
    {
        Word word = (Word) parent.getItemAtPosition(position);

        Intent intent = new Intent(this, TranslationsActivity.class);
        intent.putExtra(TranslationsActivity.INTENT_EXTRA_WORD, word);

        startActivity(intent);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id)
    {
        // TODO Auto-generated method stub
        showTranslation(parent, view, position, id);
    }
}
