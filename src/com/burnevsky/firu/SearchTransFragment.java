
package com.burnevsky.firu;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import com.burnevsky.firu.SearchFragment.OnTranslationSelectedListener;
import com.burnevsky.firu.model.Dictionary;
import com.burnevsky.firu.model.Translation;
import com.burnevsky.firu.model.Word;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.SimpleAdapter;
import android.widget.AdapterView.OnItemClickListener;

public class SearchTransFragment extends FiruFragmentBase implements SearchView.OnQueryTextListener, OnItemClickListener
{
    private final static int MAX_ITEMS_IN_RESULT = 100;

    private static final String[] mListColumns = {"word", "trans", "rate"};
    private static final int[] mListFields = {R.id.sta_textWord, R.id.sta_textTrans};

    private SearchView mInputText = null;
    private ListView mTransList = null;
    private ProgressBar mProgress = null;

    private List<Word> mMatchWords = new ArrayList<Word>();

    public interface OnWordSelectedListener
    {
        void onWordSelected(List<Word> allMatches, int selection);
    }

    private OnWordSelectedListener mCallback;

    SearchTransFragment(Context appContext, OnWordSelectedListener listener)
    {
        super(appContext);
        mCallback = listener;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        subscribeDictionary();

        super.onCreateView(inflater, container, savedInstanceState);

        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_search_trans, container, false);

        mInputText = (SearchView) rootView.findViewById(R.id.sta_searchWord);
        mInputText.setOnQueryTextListener(this);

        mTransList = (ListView) rootView.findViewById(R.id.sta_transList);
        mTransList.setOnItemClickListener(this);

        mProgress = (ProgressBar) rootView.findViewById(R.id.sta_progress);

        return rootView;
    }

    @Override
    public boolean onQueryTextSubmit(String _query)
    {
        String query = _query.trim();
        Log.i("firu", "onQueryTextSubmit: " + query);

        new DictionarySearch().execute(query);
        mProgress.setVisibility(View.VISIBLE);
        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText)
    {
        if (newText == null || newText.isEmpty())
        {
            mTransList.setAdapter(null);
        }
        return false;
    }

    @Override
    protected void hideKeyboard()
    {
        super.hideKeyboard();
        mInputText.clearFocus();
    }

    class DictionarySearch extends AsyncTask<String, Void, List<Word>>
    {
        @Override
        protected List<Word> doInBackground(String... param)
        {
            return
                (mModel.getDictionary() != null) ?
                    mModel.getDictionary().searchWordsByTranslations(param[0], MAX_ITEMS_IN_RESULT) :
                        null;
        }

        @Override
        protected void onPostExecute(List<Word> result)
        {
            mProgress.setVisibility(View.GONE);
            showListData(result);
        }
    };

    private void showListData(List<Word> list)
    {
        mMatchWords.clear();
        if (list != null)
        {
            List<SortedMap<String, Object>> data = new ArrayList<SortedMap<String,Object>>();

            for (Word w : list)
            {
                for (Translation t : w.translations)
                {
                    TreeMap<String, Object> row = new TreeMap<String, Object>();
                    row.put("word", w.getText());
                    row.put("trans", t.getText());
                    data.add(row);

                    mMatchWords.add(w); // one for each list item!
                }
            }

            mTransList.setAdapter(new SimpleAdapter(getActivity(), data,
                R.layout.translation_word_list_item, mListColumns, mListFields));

            if (list.size() > 0)
            {
                hideKeyboard();
            }
        }
        else
        {
            mTransList.setAdapter(null);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id)
    {
        mCallback.onWordSelected(mMatchWords.subList(position, position+1), 0);
    }
}
