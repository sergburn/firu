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

import java.util.ArrayList;
import java.util.List;

import com.burnevsky.firu.model.Translation;
import com.burnevsky.firu.model.Word;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

public class TranslationsActivity extends FiruActivityBase
{
    private final static String INTENT_EXTRA_DICT_WORD = "com.burnevsk.firu.dict_word";
    private final static String INTENT_EXTRA_VOC_WORD = "com.burnevsk.firu.voc_word";
    private final static String INTENT_EXTRA_WORD_IDX = "com.burnevsk.firu.word_idx";

    private List<Word> mWordList = new ArrayList<>();
    private int mWordIndex = 0;
    private boolean mFromDict = false;

    private Drawable mStarredIcon, mUnstarredIcon;

    private ViewPager mPager;
    private TranslationsPagerAdapter mPagerAdapter;
    private List<TranslationsFragment> mFragments = new ArrayList<>();

    abstract class TranslationsTask<Params, Progress, Result> extends AsyncTask<Params, Progress, Result>
    {
        protected final TranslationsFragment mFragment;

        TranslationsTask(TranslationsFragment fragment)
        {
            super();
            mFragment = fragment;
        }
    }

    class DictionaryTranslations extends TranslationsTask<Word, Void, List<Translation>>
    {
        DictionaryTranslations(TranslationsFragment fragment)
        {
            super(fragment);
        }

        @Override
        protected List<Translation> doInBackground(Word... param)
        {
            return (mModel.getDictionary() != null) ? mModel.getDictionary().getTranslations(param[0]) : null;
        }

        @Override
        protected void onPostExecute(List<Translation> result)
        {
            mFragment.onDictionaryTranslations(result);
        }
    }

    class VocabularyTranslations extends TranslationsTask<Word, Void, List<Translation>>
    {
        VocabularyTranslations(TranslationsFragment fragment)
        {
            super(fragment);
        }

        @Override
        protected List<Translation> doInBackground(Word... param)
        {
            return (mModel.getVocabulary() != null) ? mModel.getVocabulary().getTranslations(param[0]) : null;
        }

        @Override
        protected void onPostExecute(List<Translation> result)
        {
            mFragment.onVocabularyTranslations(result);
        }
    }

    class VocabularyMatch extends TranslationsTask<Word, Void, Word>
    {
        VocabularyMatch(TranslationsFragment fragment)
        {
            super(fragment);
        }

        @Override
        protected Word doInBackground(Word... param)
        {
            return (mModel.getVocabulary() != null) ? mModel.getVocabulary().findWord(param[0].getText(), param[0].getLangCode()) : null;
        }

        @Override
        protected void onPostExecute(Word word)
        {
            mFragment.onVocabuaryMatch(word);
        }
    }

    class VocabularyAdd extends TranslationsTask<Word, Void, Word>
    {
        VocabularyAdd(TranslationsFragment fragment)
        {
            super(fragment);
        }

        @Override
        protected Word doInBackground(Word... param)
        {
            if (mModel.getVocabulary() == null) return null;
            try
            {
                Word w = param[0];
                return mModel.getVocabulary().addWord(w, w.translations);
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
            mFragment.onVocabularyAdd(word);
        }
    }

    class VocabularyRemove extends TranslationsTask<Word, List<Translation>, Boolean>
    {
        VocabularyRemove(TranslationsFragment fragment)
        {
            super(fragment);
        }

        @Override
        protected Boolean doInBackground(Word... param)
        {
            Word w = param[0];
            return (mModel.getVocabulary() == null) || mModel.getVocabulary().removeWord(w);
        }

        @Override
        protected void onPostExecute(Boolean removed)
        {
            if (removed)
            {
                mFragment.onVocabularyRemove();
            }
        }
    }

    private class TranslationsPagerAdapter extends FragmentPagerAdapter
    {
        public TranslationsPagerAdapter(FragmentManager fm)
        {
            super(fm);
        }

        @Override
        public Fragment getItem(int position)
        {
            Log.d("firu", "PagerAdapter::getItem");
            return mFragments.get(position);
        }

        @Override
        public int getCount()
        {
            return mFragments.size();
        }
    }

    private class TranslationsFragment extends Fragment
    {
        Word mDictWord, mVocWord;

        TextView mWordView;
        ListView mTransView;
        ImageView mStarBtn ;

        TranslationsFragment(Word word, boolean isFromDict)
        {
            if (isFromDict)
            {
                mDictWord = word;
            }
            else
            {
                mVocWord = word;
            }
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
        {
            super.onCreateView(inflater, container, savedInstanceState);

            ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_translations, container, false);

            mWordView = (TextView) rootView.findViewById(R.id.laWord);
            mStarBtn = (ImageView) rootView.findViewById(R.id.btnStar);
            mTransView = (ListView) rootView.findViewById(R.id.listTranslations);

            mStarBtn.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    if (mDictWord != null)
                    {
                        new VocabularyAdd(TranslationsFragment.this).execute(mDictWord);
                    }
                    else if (mVocWord != null)
                    {
                        new VocabularyRemove(TranslationsFragment.this).execute(mVocWord);
                    }
                }
            });

            if (mVocWord != null)
            {
                mWordView.setText(mVocWord.getText());
                // translations will be loaded when vocabulary is ready
                mStarBtn.setVisibility(View.VISIBLE); // until vocabulary is open
                updateStarButton(true);
            }
            else if (mDictWord != null)
            {
                mWordView.setText(mDictWord.getText());
                // translations will be loaded when dictionary is ready
                mStarBtn.setVisibility(View.INVISIBLE); // until vocabulary is open
            }

            if (mDictWord != null)
            {
                new DictionaryTranslations(TranslationsFragment.this).execute(mDictWord);
                new VocabularyMatch(TranslationsFragment.this).execute(mDictWord);
            }
            else if (mVocWord != null)
            {
                new VocabularyTranslations(TranslationsFragment.this).execute(mVocWord);
            }

            return rootView;
        }

        private void onDictionaryTranslations(List<Translation> result)
        {
            mDictWord.translations = result;
            fillTranslationsList(mDictWord.translations);
        }

        private void onVocabularyTranslations(List<Translation> result)
        {
            mVocWord.translations = result;
            fillTranslationsList(mVocWord.translations);
        }

        private void onVocabuaryMatch(Word word)
        {
            mVocWord = word;
            mStarBtn.setVisibility(mModel.getVocabulary() != null ? View.VISIBLE : View.INVISIBLE);
            updateStarButton(mVocWord != null);
        }

        private void onVocabularyAdd(Word word)
        {
            mVocWord = word;
            updateStarButton(mVocWord != null);
        }

        private void onVocabularyRemove()
        {
            mVocWord.unlink();
            updateStarButton(false);
        }

        private void updateStarButton(boolean isStarred)
        {
            mStarBtn.setImageDrawable(isStarred ? mStarredIcon : mUnstarredIcon);
        }

        private void fillTranslationsList(List<Translation> list)
        {
            ArrayAdapter<Translation> adapter = null;
            if (list != null)
            {
                adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, list);
            }
            Log.d("firu", "fillTranslationsList: " + list);
            mTransView.setAdapter(adapter);
        }
    }

    @SuppressWarnings("deprecation")
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void loadIcons()
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            mStarredIcon = getDrawable(R.drawable.ic_action_important_dark);
            mUnstarredIcon = getDrawable(R.drawable.ic_action_not_important_dark);
        }
        else
        {
            mStarredIcon = getResources().getDrawable(R.drawable.ic_action_important_dark);
            mUnstarredIcon = getResources().getDrawable(R.drawable.ic_action_not_important_dark);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_translations);
        loadIcons();

        // Instantiate a ViewPager and a PagerAdapter.
        mPagerAdapter = new TranslationsPagerAdapter(getSupportFragmentManager());

        mPager = (ViewPager) findViewById(R.id.ta_pager);
        mPager.setAdapter(mPagerAdapter);

        if (mModel.getVocabulary() == null ||
            mModel.getDictionary() == null)
        {
            finish();
        }

        Intent intent = getIntent();
        mWordList = intent.getParcelableArrayListExtra(INTENT_EXTRA_DICT_WORD);
        if (mWordList == null)
        {
            mWordList = intent.getParcelableArrayListExtra(INTENT_EXTRA_VOC_WORD);
            if (mWordList == null)
            {
                Log.d("firu", "TranslationsActivity: Unsupported intent given");
                finish();
            }
            else
            {
                mFromDict = false;
            }
        }
        else
        {
            mFromDict = true;
        }
        mWordIndex = intent.getIntExtra(TranslationsActivity.INTENT_EXTRA_WORD_IDX, 0);

        for (Word word : mWordList)
        {
            mFragments.add(new TranslationsFragment(word, mFromDict));
        }

        mPagerAdapter.notifyDataSetChanged();
        mPager.setCurrentItem(mWordIndex);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.translations_activity_actions, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //if (id == R.id.action_settings)
        {
        //    return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public static void showDictWord(Activity caller, Word word)
    {
        ArrayList<Word> list = new ArrayList<>();
        list.add(word);
        showDictWords(caller, list, 0);
    }

    public static void showVocWord(Activity caller, Word word)
    {
        ArrayList<Word> list = new ArrayList<>();
        list.add(word);
        showVocWords(caller, list, 0);
    }

    public static void showDictWords(Activity caller, ArrayList<Word> words, int selection)
    {
        Intent intent = new Intent(caller, TranslationsActivity.class);
        intent.putParcelableArrayListExtra(TranslationsActivity.INTENT_EXTRA_DICT_WORD, words);
        intent.putExtra(TranslationsActivity.INTENT_EXTRA_WORD_IDX, selection);
        caller.startActivity(intent);
    }

    public static void showVocWords(Activity caller, ArrayList<Word> words, int selection)
    {
        Intent intent = new Intent(caller, TranslationsActivity.class);
        intent.putParcelableArrayListExtra(TranslationsActivity.INTENT_EXTRA_VOC_WORD, words);
        intent.putExtra(TranslationsActivity.INTENT_EXTRA_WORD_IDX, selection);
        caller.startActivity(intent);
    }
}
