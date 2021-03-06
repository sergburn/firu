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

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.burnevsky.firu.model.DictionaryID;
import com.burnevsky.firu.model.IDictionary;
import com.burnevsky.firu.model.Model;
import com.burnevsky.firu.model.Word;

import java.util.ArrayList;
import java.util.List;

public class SearchFragment
    extends FiruFragmentBase
    implements SearchView.OnQueryTextListener, OnItemClickListener
{
    private final static int MAX_WORDS_IN_RESULT = 20;

    ListView mWordsListView = null;
    TextView mCountText = null;
    SearchView mInputText = null;

    DictionarySearch mSearchTask = null;
    DictionaryCounter mCountTask = null;

    private String mSharedText;
    private ArrayList<Word> mMatches;

    public interface OnTranslationSelectedListener
    {
        void onTranslationSelected(ArrayList<Word> allMatches, int selection);
    }

    private OnTranslationSelectedListener mCallback;

    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);
        try
        {
            mCallback = (OnTranslationSelectedListener) activity;
        }
        catch (ClassCastException e)
        {
            throw new ClassCastException(activity.toString() + " must implement OnTranslationSelectedListener");
        }
    }

    @Override
    public void onDictionaryEvent(DictionaryID dictionaryID, Model.ModelEvent event)
    {
        if (dictionaryID == DictionaryID.UNIVERSAL)
        {
            switch (event)
            {
                case MODEL_EVENT_OPENED:
                case MODEL_EVENT_READY:
                    if (mSharedText != null && mInputText.getQuery().length() == 0)
                    {
                        searchSharedWord();
                    }
                    break;

                case MODEL_EVENT_FAILURE:
                    makeToast("Can't open Dictionary", Toast.LENGTH_SHORT);

                default:
                    break;
            }
        }
    }

    private void searchSharedWord()
    {
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

    class DictionarySearch extends AsyncTask<String, Void, List<Word>>
    {
        @Override
        protected List<Word> doInBackground(String... param)
        {
            IDictionary dictionary = mModel.getDictionary(DictionaryID.UNIVERSAL);
            return (dictionary != null) ? dictionary.searchWords(param[0], MAX_WORDS_IN_RESULT) : null;
        }

        @Override
        protected void onPostExecute(List<Word> result)
        {
            showMatches(result);
            mSearchTask = null;
        }

        @Override
        protected void onCancelled(List<Word> result)
        {
            mSearchTask = null;
        }
    }

    class DictionaryCounter extends AsyncTask<String, Void, Integer>
    {
        @Override
        protected Integer doInBackground(String... param)
        {
            IDictionary dictionary = mModel.getDictionary(DictionaryID.UNIVERSAL);
            return (dictionary != null) ? dictionary.countWords(param[0]) : 0;
        }

        @Override
        protected void onPostExecute(Integer result)
        {
            mCountText.setText("Found " + result + " words");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        super.onCreateView(inflater, container, savedInstanceState);

        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_search, container, false);

        mInputText = (SearchView) rootView.findViewById(R.id.searchWord);
        mInputText.setOnQueryTextListener(this);

        mWordsListView = (ListView) rootView.findViewById(R.id.wordList);
        mWordsListView.setOnItemClickListener(this);
        mCountText = (TextView) rootView.findViewById(R.id.laCount);
        mCountText.setText("");

        Bundle args = getArguments();
        if (args != null)
        {
            mSharedText = args.getString(Intent.EXTRA_TEXT);
        }

        return rootView;
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
            mCountText.setText("");
        }
        return false;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id)
    {
        mCallback.onTranslationSelected(mMatches, position);
    }

    @Override
    protected void hideKeyboard()
    {
        mInputText.clearFocus();
        super.hideKeyboard(); // this one seems to be unnecessary
    }

    private void showMatches(List<Word> result)
    {
        if (result != null)
        {
            ArrayAdapter<Word> adapter = new ArrayAdapter<>(getActivity(), R.layout.word_list_item, result);
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
        mMatches = new ArrayList<>(result);
    }
}
