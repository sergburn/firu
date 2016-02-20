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
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.burnevsky.firu.model.DictionaryID;
import com.burnevsky.firu.model.Mark;
import com.burnevsky.firu.model.Model;
import com.burnevsky.firu.model.Word;
import com.burnevsky.firu.model.exam.ForwardExam;
import com.burnevsky.firu.model.exam.ForwardTest;
import com.burnevsky.firu.model.exam.TestAlreadyCompleteException;
import com.burnevsky.firu.model.exam.TestResult;

import java.util.ArrayList;
import java.util.List;

public class TrainerFwdActivity extends FiruActivityBase
{
    private TextView mChallengeText = null;
    private ListView mVariantsListView = null;
    private Drawable mOkIcon = null, mPassedIcon = null, mFailIcon = null, mLifeIcon = null;
    private ImageView mNextButton = null;
    private final List<ImageView> mImgLives = new ArrayList<>();
    private TextView mExamProgress = null;
    private RatingBar mMarkRating = null;

    private static final long TRAINER_CORRECTION_DELAY = 500;

    private static class Data extends Fragment
    {
        public ForwardExam mExam = null;
        public ForwardTest mTest = null;
        List<String> mVariants = null;
        public boolean mErrorState;

        public enum State
        {
            STATE_INITIAL, // mTest == null
            STATE_MAKING_EXAM,
            STATE_TEST_ONGOING,
            STATE_TEST_FINISHED
        }

        State mState = State.STATE_INITIAL;

        @Override
        public void onCreate(Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);
            setRetainInstance(true);
        }
    }

    private Data mData = null;

    @Override
    public void onDictionaryEvent(DictionaryID dictionaryID, Model.ModelEvent event)
    {
        if (dictionaryID == DictionaryID.VOCABULARY)
        {
            switch (event)
            {
                case MODEL_EVENT_FAILURE:
                case MODEL_EVENT_CLOSED:
                    mData = new Data();
                    changeState(mData.mState);

                    AlertDialog.Builder dialog = new AlertDialog.Builder(this);
                    dialog
                        .setTitle("Forward exam")
                        .setMessage("Vocabulary unavailable.\nCan't start exam.")
                        .setIcon(android.R.drawable.ic_dialog_info)
                        .setNeutralButton("Yes", new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                finish();
                            }
                        })
                        .show();

                    break;

                default:
                    break;
            }
        }
    }

    private void startExam()
    {
        changeState(Data.State.STATE_MAKING_EXAM);
        new BuildForwardExam().execute();
    }

    private void onExamUnavailable()
    {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog
        .setTitle("Reverse exam")
        .setMessage("No words found in your vocabulary.\n"
            + "Add some before starting trainer.")
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

    class BuildForwardExam extends AsyncTask<Mark, Void, ForwardExam>
    {
        @Override
        protected ForwardExam doInBackground(Mark... param)
        {
            return (mModel.getVocabulary() != null) ? ForwardExam.Builder.buildExam(mModel.getVocabulary()) : null;
        }

        @Override
        protected void onPostExecute(ForwardExam exam)
        {
            if (exam != null)
            {
                if (exam.getTestsToGo() > 0)
                {
                    mData.mExam = exam;
                    startTest(exam.nextTest());
                }
                else
                {
                    onExamUnavailable();
                }
            }
            else
            {
                Toast.makeText(TrainerFwdActivity.this, "Failed to make exam", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void startTest(ForwardTest test)
    {
        mData.mTest = test;
        mData.mVariants = test.getVariants();
        changeState(Data.State.STATE_TEST_ONGOING);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_trainer_fwd);
        mChallengeText = (TextView) findViewById(R.id.textChallenge);
        mVariantsListView = (ListView) findViewById(R.id.listVariants);
        mImgLives.add((ImageView) findViewById(R.id.imgLife1));
        mImgLives.add((ImageView) findViewById(R.id.imgLife2));
        mImgLives.add((ImageView) findViewById(R.id.imgLife3));
        mNextButton = (ImageView) findViewById(R.id.imgNext);
        mExamProgress = (TextView) findViewById(R.id.textProgress);
        mMarkRating = (RatingBar) findViewById(R.id.rbMark);

        mOkIcon = getResources().getDrawable(R.drawable.ic_action_accept);
        mPassedIcon = getResources().getDrawable(R.drawable.ic_action_good);
        mFailIcon = getResources().getDrawable(R.drawable.ic_action_bad);
        mLifeIcon = getResources().getDrawable(R.drawable.ic_action_favorite);

        mNextButton.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                nextTest();
            }
        });

        mVariantsListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                validateAnswer(position);
            }
        });

        if (mModel.getVocabulary() != null)
        {
            // find the retained fragment on activity restarts
            FragmentManager fm = getFragmentManager();
            mData = (Data) fm.findFragmentByTag("data");

            // create the fragment and data the first time
            if (mData == null)
            {
                // add the fragment
                mData = new Data();
                fm.beginTransaction().add(mData, "data").commit();
                changeState(Data.State.STATE_INITIAL);
                startExam();
            }
            else
            {
                changeState(mData.mState);
            }
        }
        else
        {
            onDictionaryEvent(DictionaryID.VOCABULARY, Model.ModelEvent.MODEL_EVENT_FAILURE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        // getMenuInflater().inflate(R.menu.trainer, menu);
        return false;
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

    void changeState(Data.State newState)
    {
        switch (newState)
        {
            case STATE_INITIAL:
                mChallengeText.setText("");
            case STATE_MAKING_EXAM:
                mNextButton.setEnabled(false);
                mMarkRating.setVisibility(View.INVISIBLE);
                showVariants(null);
                showTestResult();
                showExamProgress(false);
                break;
            case STATE_TEST_ONGOING:
                mNextButton.setEnabled(true);
                mMarkRating.setVisibility(View.VISIBLE);
                mChallengeText.setText(mData.mTest.getChallenge());
                showVariants(mData.mVariants);
                showTestResult();
                showExamProgress(true);
                break;
            case STATE_TEST_FINISHED:
                List<String> answer = new ArrayList<>();
                answer.add(mData.mTest.getAnswer());
                showVariants(answer);
                showTestResult();
                showInputCorrectness(true);
                showExamProgress(true);
                break;
            default:
                break;
        }
        mData.mState = newState;
    }

    private void validateAnswer(final int position)
    {
        try
        {
            boolean correct = mData.mTest.checkAnswer(mData.mVariants.get(position));
            showInputCorrectness(correct);
        }
        catch (TestAlreadyCompleteException e)
        {
            e.printStackTrace();
            return;
        }

        if (mData.mTest.getResult() != TestResult.Incomplete)
        {
            changeState(Data.State.STATE_TEST_FINISHED);
        }
        else
        {
            showTestResult(); // update lives

            // Automatically remove wrong variant shortly
            new Handler().postDelayed(new Runnable()
            {
                @Override
                public void run()
                {
                    if (mData.mErrorState)
                    {
                        mData.mVariants.remove(position);
                        showVariants(mData.mVariants);
                        showInputCorrectness(true);
                    }
                }
            }, TRAINER_CORRECTION_DELAY);
        }
    }

    private void showVariants(List<String> variants)
    {
        if (variants != null)
        {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.word_list_item, variants);
            mVariantsListView.setAdapter(adapter);
        }
        else
        {
            mVariantsListView.setAdapter(null);
        }
    }

    void showTestResult()
    {
        if (mData.mTest != null)
        {
            switch (mData.mTest.getResult())
            {
                case Incomplete:
                    showLifes(mData.mTest.getHintsLeft());
                    break;

                case Passed:
                    showTestResultIcon(mPassedIcon);
                    break;

                case PassedWithHints:
                    showTestResultIcon(mOkIcon);
                    break;

                case Failed:
                    showTestResultIcon(mFailIcon);
                    break;

                default:
                    return;
            }

            mMarkRating.setRating(ExamResultActivity.markToRate(mData.mTest.getMark()));
        }
        else
        {
            showTestResultIcon(null);
        }
    }

    void showLifes(int lives)
    {
        mImgLives.get(0).setVisibility((lives >= 1) ? View.VISIBLE : View.INVISIBLE);
        mImgLives.get(1).setImageDrawable(mLifeIcon);
        mImgLives.get(1).setVisibility((lives >= 2) ? View.VISIBLE : View.INVISIBLE);
        mImgLives.get(2).setVisibility((lives >= 3) ? View.VISIBLE : View.INVISIBLE);
    }

    void showTestResultIcon(Drawable icon)
    {
        mImgLives.get(0).setVisibility(View.INVISIBLE);
        mImgLives.get(2).setVisibility(View.INVISIBLE);
        if (icon == null)
        {
            mImgLives.get(1).setVisibility(View.INVISIBLE);
        }
        else
        {
            mImgLives.get(1).setVisibility(View.VISIBLE);
            mImgLives.get(1).setImageDrawable(icon);
        }
    }

    void showInputCorrectness(boolean correct)
    {
        if (correct)
        {
            mChallengeText.setError(null);
        }
        else
        {
            mChallengeText.setError("wrong"); // sets special icon
        }
        mData.mErrorState = !correct;
    }

    private void nextTest()
    {
        if (mData.mTest != null && !mData.mTest.isComplete())
        {
            mData.mTest.unlockAnswer();
            changeState(Data.State.STATE_TEST_FINISHED);
        }
        else if (mData.mExam != null)
        {
            ForwardTest test = mData.mExam.nextTest();
            if (test != null)
            {
                startTest(test);
            }
            else
            {
                ExamResultActivity.showExamResults(this, mData.mExam);
                finish();
            }
        }
    }

    private void showExamProgress(boolean isVisible)
    {
        if (mData.mExam != null)
        {
            int currentTest = mData.mExam.getTestsCount() - mData.mExam.getTestsToGo();
            mExamProgress.setText(String.valueOf(currentTest) + "/" + String.valueOf(mData.mExam.getTestsCount()));
        }
        mExamProgress.setVisibility(isVisible ? View.VISIBLE : View.INVISIBLE);
    }

    public static void startExamActivity(Activity caller)
    {
        Intent intent = new Intent(caller, TrainerFwdActivity.class);
        caller.startActivity(intent);
    }
}
