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
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.burnevsky.firu.model.Word;

import java.util.ArrayList;
import java.util.List;

public class TranslationsActivity extends FiruActivityBase
{
    private final static String INTENT_EXTRA_WORD_LIST = "com.burnevsk.firu.word_list";
    private final static String INTENT_EXTRA_WORD_IDX = "com.burnevsk.firu.word_idx";

    private List<TranslationsFragment> mFragments = new ArrayList<>();

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

    TranslationsFragment createFragment(Word word)
    {
        TranslationsFragment f = new TranslationsFragment();

        Bundle args = new Bundle();
        args.putParcelable("word", word);
        f.setArguments(args);

        return f;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_translations);

        // Instantiate a ViewPager and a PagerAdapter.
        TranslationsPagerAdapter mPagerAdapter = new TranslationsPagerAdapter(getSupportFragmentManager());

        ViewPager mPager = (ViewPager) findViewById(R.id.ta_pager);
        mPager.setAdapter(mPagerAdapter);

        Intent intent = getIntent();
        List<Word> mWordList = intent.getParcelableArrayListExtra(INTENT_EXTRA_WORD_LIST);
        if (mWordList == null)
        {
            Log.d("firu", "TranslationsActivity: Unsupported intent given");
            finish();
        }
        int mWordIndex = intent.getIntExtra(TranslationsActivity.INTENT_EXTRA_WORD_IDX, 0);

        for (Word word : mWordList)
        {
            mFragments.add(createFragment(word));
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
        //int id = item.getItemId();
        //if (id == R.id.action_settings)
        {
        //    return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public static void showWord(Activity caller, Word word)
    {
        ArrayList<Word> list = new ArrayList<>();
        list.add(word);
        showWords(caller, list, 0);
    }

    public static void showWords(Activity caller, ArrayList<Word> words, int selection)
    {
        Intent intent = new Intent(caller, TranslationsActivity.class);
        intent.putParcelableArrayListExtra(TranslationsActivity.INTENT_EXTRA_WORD_LIST, words);
        intent.putExtra(TranslationsActivity.INTENT_EXTRA_WORD_IDX, selection);
        caller.startActivity(intent);
    }
}
