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

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.burnevsky.firu.model.Dictionary;
import com.burnevsky.firu.model.Vocabulary;
import com.burnevsky.firu.model.Word;

public class SearchActivity extends FiruActivityBase implements SearchView.OnQueryTextListener, OnItemClickListener
{
    private final static int MAX_WORDS_IN_RESULT = 20;

    final Handler mHandler = new Handler();

    ListView mWordsListView = null;
    TextView mCountText = null;
    MenuItem mExportVocMenu, mClearVocMenu = null;
    SearchView mInputText = null;

    DictionarySearch mSearchTask = null;
    DictionaryCounter mCountTask = null;

    private String mSharedText;

    @Override
    public void onVocabularyOpen(Vocabulary voc)
    {
        super.onVocabularyOpen(voc);
        invalidateOptionsMenu();
        Toast.makeText(mSelfContext, "Vocabulary has " + String.valueOf(mVoc.getTotalWords()) + " words", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onVocabularyReset(Vocabulary voc)
    {
        super.onVocabularyReset(voc);
        invalidateOptionsMenu();
        Toast.makeText(mSelfContext, "Vocabulary is empty now", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onVocabularyClose(Vocabulary voc)
    {
        invalidateOptionsMenu();
        super.onVocabularyClose(voc);
    }

    @Override
    public void onDictionaryOpen(Dictionary dict)
    {
        super.onDictionaryOpen(dict);
        showTotalWordsCount();
        if (mSharedText != null)
        {
            // At the moment only one word search is supported
            String words[] = mSharedText.trim().split("\\W+");
            for (String m : words)
            {
                Log.d("firu", m);
            }
            if (words.length > 0)
            {
                mInputText.setQuery(words[0], true);
            }
        }
    }

    @Override
    public void onDictionaryClose(Dictionary dict)
    {
        super.onDictionaryClose(dict);
        showTotalWordsCount();
    }

    class DictionarySearch extends AsyncTask<String, Void, List<Word>>
    {
        @Override
        protected List<Word> doInBackground(String... param)
        {
            return (mDict != null) ? mDict.searchWords(param[0], MAX_WORDS_IN_RESULT) : null;
        }

        @Override
        protected void onPostExecute(List<Word> result)
        {
            if (result != null)
            {
                ArrayAdapter<Word> adapter = new ArrayAdapter<Word>(mSelfContext, android.R.layout.simple_list_item_1, result);
                mWordsListView.setAdapter(adapter);

                if (result.size() > 0)
                {
                    hideKeyboard();
                }
            }
            else
            {
                mWordsListView.setAdapter(null);
            }
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
            return (mDict != null) ? mDict.countWords(param[0]) : 0;
        }

        @Override
        protected void onPostExecute(Integer result)
        {
            mCountText.setText("Found " + result + " words");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        mInputText = (SearchView) findViewById(R.id.searchWord);
        mInputText.setOnQueryTextListener(this);

        mWordsListView = (ListView) findViewById(R.id.wordList);
        mWordsListView.setOnItemClickListener(this);
        mCountText = (TextView) findViewById(R.id.laCount);

        Intent intent = getIntent();
        if (intent.getAction() == Intent.ACTION_SEND)
        {
            mSharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
        }
    }

    private void showTotalWordsCount()
    {
        if (mDict != null)
        {
            mCountText.setText("Total count " + String.valueOf(mDict.getTotalWords()) + " words");
        }
        else
        {
            mCountText.setText("Dictionary not open");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.search_activity_actions, menu);
        mExportVocMenu = menu.findItem(R.id.action_backup_voc);
        mClearVocMenu = menu.findItem(R.id.action_reset_voc);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu)
    {
        mExportVocMenu.setEnabled(mVoc != null);
        mClearVocMenu.setEnabled(mVoc != null && mVoc.getTotalWords() > 0);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch(item.getItemId())
        {
            case R.id.action_settings:
                Intent intent = new Intent(this, TrainerActivity.class);
                startActivity(intent);
                return true;

            case R.id.action_import_voc:
                mApp.importVocabulary(mSelfContext);
                return true;

            case R.id.action_backup_voc:
                mApp.exportVocabulary(mSelfContext);
                return true;

            case R.id.action_reset_voc:
                mApp.resetVocabulary(mSelfContext);
                return true;

            case R.id.action_show_stats:
                Intent intent2 = new Intent(this, StatActivity.class);
                startActivity(intent2);
                return true;

            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onQueryTextSubmit(String _query)
    {
        String query = _query.trim();
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
        if (newText == null || newText.isEmpty())
        {
            mWordsListView.setAdapter(null);
            showTotalWordsCount();
        }
        return false;
    }

    /** Called when the user clicks the Send button */
    public void showTranslation(AdapterView<?> parent, View view, int position, long id)
    {
        Word word = (Word) parent.getItemAtPosition(position);
        TranslationsActivity.showDictWord(this, word);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id)
    {
        showTranslation(parent, view, position, id);
    }

    private void hideKeyboard()
    {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
        mInputText.clearFocus();
    }
}
