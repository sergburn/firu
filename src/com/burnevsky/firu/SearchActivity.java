package com.burnevsky.firu;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.burnevsky.firu.model.Dictionary;
import com.burnevsky.firu.model.WordBase;

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

    class DictionarySearch extends AsyncTask<String, Void, List<WordBase>>
    {
        @Override
        protected List<WordBase> doInBackground(String... param)
        {
            return mDict.searchWords(param[0], MAX_WORDS_IN_RESULT);
        }

        @Override
        protected void onPostExecute(List<WordBase> result)
        {
            ArrayAdapter<WordBase> adapter = new ArrayAdapter<WordBase>(mSelfContext,
                    android.R.layout.simple_list_item_1, result);
            mWordsListView.setAdapter(adapter);
            mSearchTask = null;
        }

        @Override
        protected void onCancelled(List<WordBase> result)
        {
            mSearchTask = null;
        }
    };

    class DictionaryCounter extends AsyncTask<String, Void, Long>
    {
        @Override
        protected Long doInBackground(String... param)
        {
            return mDict.countWords(param[0]);
        }

        @Override
        protected void onPostExecute(Long result)
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
        } else
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
        WordBase word = (WordBase) parent.getItemAtPosition(position);

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
