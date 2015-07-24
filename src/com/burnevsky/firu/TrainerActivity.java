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

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.burnevsky.firu.model.Dictionary;
import com.burnevsky.firu.model.Mark;
import com.burnevsky.firu.model.Vocabulary;
import com.burnevsky.firu.model.test.ReverseExam;
import com.burnevsky.firu.model.test.ReverseTest;
import com.burnevsky.firu.model.test.TestAlreadyCompleteException;
import com.burnevsky.firu.model.test.TestResult;

public class TrainerActivity extends Activity
{
    Context mSelfContext = null;
    int mKeyBoardHeight = 0;
    View mLayout = null;
    TextView mMarkText = null;
    TextView mTransText = null;
    EditText mWordEdit = null;
    Drawable mGoodIcon = null, mPassedIcon = null, mFailIcon = null, mLifeIcon = null;
    ImageView mHintButton = null, mNextButton = null;
    List<ImageView> mImgLives = new ArrayList<ImageView>();
    ProgressBar mExamProgress = null;
    GridLayout mKeyboard = null;
    ImageButton mBackspace = null;
    Button mEnter = null;
    ArrayList<Button> mKeys = new ArrayList<Button>();

    ReverseExam mExam = null;
    ReverseTest mTest = null;

    private enum State
    {
        STATE_INITIAL, // mTest == null
        STATE_MAKING_NORMAL_EXAM,
        STATE_MAKING_REVIEW_EXAM,
        STATE_TEST_ONGOING,
        STATE_TEST_FINISHED,
        STATE_EXAM_FINISHED
    };
    State mState = State.STATE_INITIAL;

    FiruApplication mApp = null;
    Dictionary mDict = null;
    Vocabulary mVoc = null;
    FiruApplication.ModelListener mModelListener = null;

    class ModelListener implements FiruApplication.ModelListener
    {
        @Override
        public void onVocabularyOpen(Vocabulary voc)
        {
            mVoc = voc;
            startNormalExam();
        }

        @Override
        public void onVocabularyReset(Vocabulary voc)
        {
            // TODO: cancel test (however this event should never happen during test)
        }

        @Override
        public void onVocabularyClose(Vocabulary voc)
        {
            mVoc = null;
        }

        @Override
        public void onDictionaryOpen(Dictionary dict)
        {
            mDict = dict;
        }

        @Override
        public void onDictionaryClose(Dictionary dict)
        {
            mDict = null;
        }
    }

    private void startNormalExam()
    {
        changeState(State.STATE_MAKING_NORMAL_EXAM);
        new ReverseExamBuilder().execute(Mark.AlmostLearned);
    }

    private void startReviewExam()
    {
        changeState(State.STATE_MAKING_REVIEW_EXAM);
        new ReverseExamBuilder().execute(Mark.Learned);
    }

    private void onExamUnavailable()
    {
        if (mState == State.STATE_MAKING_NORMAL_EXAM)
        {
            onNormalExamUnavailable();
        }
        else if (mState == State.STATE_MAKING_REVIEW_EXAM)
        {
            onReviewExamUnavailable();
        }
    }

    private void onNormalExamUnavailable()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(mSelfContext);
        builder
        .setTitle("Reverse exam")
        .setMessage("You seem to have learned whole vocabulary!\n"
            + "Do you want to review learned words?")
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setPositiveButton("Yes", new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                    startReviewExam();
                }
            } )
            .setNegativeButton("No", new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                    finish();
                }
            } )
            .show();
    }

    private void onReviewExamUnavailable()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(mSelfContext);
        builder
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

    class ReverseExamBuilder extends AsyncTask<Mark, Void, ReverseExam>
    {
        @Override
        protected ReverseExam doInBackground(Mark... param)
        {
            return new ReverseExam(mVoc, param[0]);
        }

        @Override
        protected void onPostExecute(ReverseExam exam)
        {
            if (exam != null)
            {
                if (exam.getTestsToGo() > 1)
                {
                    mExam = exam;
                    startTest(exam.nextTest());
                }
                else
                {
                    onExamUnavailable();
                }
            }
            else
            {
                Toast.makeText(mSelfContext, "Failed to make exam", Toast.LENGTH_SHORT).show();
            }
        }
    };

    class GuessValidator
    {
        public void afterTextChanged(TextView s)
        {
            String input = mWordEdit.getText().toString();
            if (input.length() == 0)
            {
                mWordEdit.setCompoundDrawables(null, null, null, null);
            }
            else
            {
                boolean correct = mTest.checkGuess(input);
                showInputCorrectness(correct);
            }
        }

        public boolean onEnter(TextView v)
        {
            try
            {
                boolean correct = mTest.checkAnswer(mWordEdit.getText().toString());
                showInputCorrectness(correct);
                if (mTest.getResult() != TestResult.Incomplete)
                {
                    changeState(State.STATE_TEST_FINISHED);
                }
                else
                {
                    showTestState(); // update lives
                }
            }
            catch (TestAlreadyCompleteException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            return false;
        }
    };

    GuessValidator mGuessValidator = null;

    private View.OnClickListener mKeyBoardListener = new OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            Button b = (Button) v;
            mWordEdit.append(b.getText());
            mGuessValidator.afterTextChanged(mWordEdit);
        }
    };

    private void startTest(ReverseTest test)
    {
        mTest = test;
        mTransText.setText(test.getChallenge());
        mWordEdit.setText("");
        changeState(State.STATE_TEST_ONGOING);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trainer);
        mLayout = findViewById(R.id.layoutTrainer);
        mWordEdit = (EditText) findViewById(R.id.editWord);
        mTransText = (TextView) findViewById(R.id.textTrans);
        mMarkText = (TextView) findViewById(R.id.textMark);
        mImgLives.add((ImageView) findViewById(R.id.imgLife1));
        mImgLives.add((ImageView) findViewById(R.id.imgLife2));
        mImgLives.add((ImageView) findViewById(R.id.imgLife3));
        mHintButton = (ImageView) findViewById(R.id.imgHint);
        mNextButton = (ImageView) findViewById(R.id.imgNext);
        mExamProgress = (ProgressBar) findViewById(R.id.pbExamProgress);
        mKeyboard = (GridLayout) findViewById(R.id.gridKeyboard);
        mBackspace = (ImageButton) findViewById(R.id.btnBackspace);
        mEnter = (Button) findViewById(R.id.btnEnter);
        mSelfContext = this;

        mGoodIcon = getResources().getDrawable(R.drawable.ic_action_good);
        mPassedIcon = getResources().getDrawable(R.drawable.ic_action_accept);
        mFailIcon = getResources().getDrawable(R.drawable.ic_action_bad);
        mLifeIcon = getResources().getDrawable(R.drawable.ic_action_favorite);

        mGuessValidator = new GuessValidator();

        mHintButton.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                try
                {
                    if (mTest.getHintsLeft() > 0)
                    {
                        String newText = mTest.getHint(mWordEdit.getText().toString());
                        mWordEdit.setText(newText);
                        mWordEdit.setSelection(newText.length());
                        showTestState();
                    }
                    else
                    {
                        mTest.unlockAnswer();
                        changeState(State.STATE_TEST_FINISHED);
                    }
                }
                catch (TestAlreadyCompleteException e)
                {
                    e.printStackTrace();
                }
            }
        });

        mNextButton.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (mTest != null && !mTest.isComplete())
                {
                    mTest.unlockAnswer();
                    changeState(State.STATE_TEST_FINISHED);
                }
                else if (mExam != null)
                {
                    ReverseTest test = mExam.nextTest();
                    if (test != null)
                    {
                        startTest(test);
                    }
                    else
                    {
                        changeState(State.STATE_EXAM_FINISHED);
                    }
                }
            }
        });

        mEnter.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                mGuessValidator.onEnter(mWordEdit);
            }
        });

        mBackspace.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                String s = mWordEdit.getText().toString();
                if (s.length() > 0)
                {
                    mWordEdit.setText(s.substring(0, s.length()-1));
                    mGuessValidator.afterTextChanged(mWordEdit);
                }
            }
        });

        // InputMethodManager imm =
        // (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        // imm.showSoftInput(mLayout, InputMethodManager.SHOW_FORCED);

        String lines12 = "qwertyuiopåasdfghjklöä";
        String line3 = "__zxcvbnm__";

        for (int r = 0; r < 3; r++)
        {
            GridLayout.Spec rowSpec = GridLayout.spec(r);
            for (int c = 0; c < 11; c++)
            {
                String cap = "";

                if (r < 2)
                {
                    cap = lines12.substring(r*11+c, r*11+c+1);
                }
                else if (c >= 2 && c < 9)
                {
                    cap = line3.substring(c, c + 1);
                }

                if (!cap.isEmpty())
                {
                    GridLayout.Spec colSpec = GridLayout.spec(c, GridLayout.CENTER);

                    GridLayout.LayoutParams params = new GridLayout.LayoutParams(rowSpec, colSpec);

                    Button k = new Button(mSelfContext, null, android.R.attr.buttonStyle);
                    k.setMinHeight(128);
                    k.setMinWidth(6);
                    k.setMinimumHeight(128);
                    k.setMinimumWidth(6);
                    k.setPadding(0, 0, 0, 0);
                    k.setOnClickListener(mKeyBoardListener);
                    k.setText(cap);
                    k.setTypeface(k.getTypeface(), 1); // bold

                    mKeyboard.addView(k, params);
                    mKeys.add(k);
                }
            }
        }

        View thisView = this.getWindow().getDecorView().findViewById(android.R.id.content);
        thisView.addOnLayoutChangeListener(new View.OnLayoutChangeListener()
        {
            @Override
            public void onLayoutChange(View v,
                int left, int top, int right, int bottom,
                int oldLeft, int oldTop, int oldRight, int oldBottom)
            {
                if (!(left == 0 && right == 0) && (left != oldLeft || right != oldRight))
                {
                    int size = (right - left) - mKeyboard.getPaddingLeft() - mKeyboard.getPaddingRight();
                    int keyCellSize = size / 11;

                    for (Button k : mKeys)
                    {
                        k.setWidth(keyCellSize);
                    }
                }
            }
        });

        mModelListener = new ModelListener();
        mApp = (FiruApplication) getApplicationContext();
        mApp.subscribeDictionary(mSelfContext, mModelListener);
        mApp.subscribeVocabulary(mSelfContext, mModelListener);

        changeState(State.STATE_INITIAL);
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
        int id = item.getItemId();
        if (id == R.id.action_settings)
        {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @TargetApi(17)
    void showInputValue(Drawable icon)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
        {
            //mWordEdit.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, icon, null);
        }
        else
        {
            //mWordEdit.setCompoundDrawablesWithIntrinsicBounds(null, null, icon, null);
        }
    }

    void changeState(State newState)
    {
        switch (newState)
        {
            case STATE_INITIAL:
            case STATE_MAKING_NORMAL_EXAM:
            case STATE_MAKING_REVIEW_EXAM:
                mWordEdit.setEnabled(false);
                mHintButton.setEnabled(false);
                mNextButton.setEnabled(false);
                mMarkText.setVisibility(View.INVISIBLE);
                showTestState();
                break;
            case STATE_TEST_ONGOING:
                mWordEdit.setEnabled(true);
                mHintButton.setEnabled(true);
                mNextButton.setEnabled(true);
                mMarkText.setVisibility(View.VISIBLE);
                showTestState();
                mExamProgress.setProgress(mExam.getExamProgress());
                break;
            case STATE_TEST_FINISHED:
                mWordEdit.setEnabled(false);
                mHintButton.setEnabled(false);
                showTestState();
                break;
            case STATE_EXAM_FINISHED:
                mWordEdit.setVisibility(View.INVISIBLE);
                mHintButton.setVisibility(View.INVISIBLE);
                mNextButton.setVisibility(View.INVISIBLE);
                mMarkText.setVisibility(View.INVISIBLE);
                mTransText.setText("Exam finished!");
                showLifes(0);
                mExamProgress.setProgress(100);
                break;
            default:
                break;
        }
        mState = newState;
    }

    void showTestState()
    {
        if (mTest != null)
        {
            showTestResult(mTest.getResult());
        }
        else
        {
            showTestResultIcon(null);
        }
    }

    void showTestResult(TestResult result)
    {
        switch (result)
        {
            case Incomplete:
                mMarkText.setText(mTest.getMark().toString());
                showLifes(mTest.getHintsLeft());
                return;

            case Passed:
                showTestResultIcon(mPassedIcon);
                break;

            case PassedWithHints:
                showTestResultIcon(mGoodIcon);
                break;

            case Failed:
                mWordEdit.setText(mTest.getAnswer());
                showTestResultIcon(mFailIcon);
                break;

            default:
                mMarkText.setText("");
                return;
        }
        mMarkText.setText(result.toString()); // TODO: l10n
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
            mWordEdit.setError(null);
            showInputValue(mGoodIcon);
        }
        else
        {
            mWordEdit.setError("wrong"); // sets special icon
        }
    }
}
