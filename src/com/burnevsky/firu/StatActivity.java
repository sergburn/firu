/*******************************************************************************
 * The MIT License (MIT)
 *
 * Copyright (c) 2015 Sergey Burnevsky
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

import java.util.Map;
import java.util.TreeMap;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.burnevsky.firu.model.Mark;
import com.burnevsky.firu.model.Vocabulary;

public class StatActivity extends FiruActivityBase
{
    Vocabulary.LearningStats mLearningStats = null;

    class VocabularyStats extends AsyncTask<Void, Void, Vocabulary.LearningStats>
    {
        @Override
        protected Vocabulary.LearningStats doInBackground(Void... param)
        {
            return mModel.getVocabulary().collectStatistics();
        }

        @Override
        protected void onPostExecute(Vocabulary.LearningStats result)
        {
            mLearningStats = result;
            if (result != null)
            {
                showStats();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stat);
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        if (mModel.getVocabulary() != null)
        {
            new VocabularyStats().execute();
        }
        else
        {
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog
            .setTitle("Vocabulary stats")
            .setMessage("Vocabulary unavailable.")
            .setIcon(android.R.drawable.ic_dialog_info)
            .setNeutralButton("Yes", new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                    finish();
                }
            } )
            .show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.stat, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        //int id = item.getItemId();
        return super.onOptionsItemSelected(item);
    }

    private void setIntegerField(int id, int value)
    {
        ((TextView)findViewById(id)).setText(String.valueOf(value));
    }

    private void setMarkField(int id, Map<Mark, Integer> map, Mark mark)
    {
        if (map.containsKey(mark))
        {
            setIntegerField(id, map.get(mark));
        }
        else
        {
            setIntegerField(id, 0);
        }
    }

    private int calculateRating(Map<Mark, Integer> map)
    {
        int rating = 0;

        Map<Mark, Integer> MARK_FACTORS = new TreeMap<>();
        MARK_FACTORS.put(Mark.WITH_HINTS, 1);
        MARK_FACTORS.put(Mark.ALMOST_LEARNED, 2);
        MARK_FACTORS.put(Mark.LEARNED, 3);

        for (Mark mark : MARK_FACTORS.keySet())
        {
            Integer statValue = map.get(mark);
            if (statValue != null)
            {
                rating += statValue * MARK_FACTORS.get(mark);
            }
        }

        return rating;
    }

    private void showStats()
    {
        setIntegerField(R.id.txtNumWords, mLearningStats.TotalWordsCount);
        setIntegerField(R.id.txtNumTrans, mLearningStats.TotalTranslationsCount);

        setIntegerField(R.id.txtFwdRating, calculateRating(mLearningStats.ForwardMarksDistribution));
        setMarkField(R.id.txtFwdYetToLearn, mLearningStats.ForwardMarksDistribution, Mark.YET_TO_LEARN);
        setMarkField(R.id.txtFwdWithHints, mLearningStats.ForwardMarksDistribution, Mark.WITH_HINTS);
        setMarkField(R.id.txtFwdAlmost, mLearningStats.ForwardMarksDistribution, Mark.ALMOST_LEARNED);
        setMarkField(R.id.txtFwdLearned, mLearningStats.ForwardMarksDistribution, Mark.LEARNED);

        setIntegerField(R.id.txtRevRating, calculateRating(mLearningStats.ReverseMarksDistribution));
        setMarkField(R.id.txtRevYetToLearn, mLearningStats.ReverseMarksDistribution, Mark.YET_TO_LEARN);
        setMarkField(R.id.txtRevWithHints, mLearningStats.ReverseMarksDistribution, Mark.WITH_HINTS);
        setMarkField(R.id.txtRevAlmost, mLearningStats.ReverseMarksDistribution, Mark.ALMOST_LEARNED);
        setMarkField(R.id.txtRevLearned, mLearningStats.ReverseMarksDistribution, Mark.LEARNED);
    }
}
