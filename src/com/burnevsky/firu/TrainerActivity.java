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
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.text.Editable;
import android.text.Layout;
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
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.burnevsky.firu.model.Dictionary;
import com.burnevsky.firu.model.Mark;
import com.burnevsky.firu.model.Vocabulary;
import com.burnevsky.firu.model.Word;
import com.burnevsky.firu.model.test.ReverseExam;
import com.burnevsky.firu.model.test.ReverseTest;
import com.burnevsky.firu.model.test.TestAlreadyCompleteException;
import com.burnevsky.firu.model.test.TestResult;

public class TrainerActivity extends Activity
{
    private Context mSelfContext = null;
    private TextView mTransText = null;
    private TextView mWordText = null;
    private Drawable mOkIcon = null, mPassedIcon = null, mFailIcon = null, mLifeIcon = null;
    private ImageView mHintButton = null, mNextButton = null;
    private List<ImageView> mImgLives = new ArrayList<ImageView>();
    private TextView mExamProgress = null;
    private GridLayout mKeyboard = null;
    private Button mEnter = null;
    private ArrayList<Button> mKeys = new ArrayList<Button>();
    private RatingBar mMarkRating = null;

    private ReverseExam mExam = null;
    private ReverseTest mTest = null;
    private boolean mErrorState;

    // User must enter word without typing mistakes to Pass the test
    // (otherwise she can use try-and-error approach)
    // However, after 1st typo takes away 1 life, which counted as 1 hint,
    // further typos are not counted.
    // This way typos don't let mark to upgrade to rates Well-known and Learned
    private boolean mForgiveFurtherMistakes = false;

    private static final long TRAINER_CORRECTION_DELAY = 500;
    private static final long TRAINER_MAX_TYPO_DELAY = 500;

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
                if (exam.getTestsToGo() > 0)
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
        private long mLastClick = 0;

        public void afterTextChanged(TextView s)
        {
            final String input = mWordText.getText().toString();
            if (input.length() == 0)
            {
                showInputCorrectness(true);
            }
            else
            {
                long now = SystemClock.uptimeMillis();
                boolean fastTypingDetected = (now - mLastClick) < TrainerActivity.TRAINER_MAX_TYPO_DELAY;
                if (fastTypingDetected)
                {
                    Log.d("firu", "Fast typing interval: " + String.valueOf(now - mLastClick));
                }
                mLastClick = now;

                boolean correct = false;
                try
                {
                    correct = mTest.checkGuess(input, mForgiveFurtherMistakes || fastTypingDetected);
                    showInputCorrectness(correct);
                }
                catch (TestAlreadyCompleteException e)
                {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    return;
                }

                if (mTest.getResult() != TestResult.Incomplete)
                {
                    changeState(State.STATE_TEST_FINISHED);
                }
                else
                {
                    showTestState(); // update lives

                    if (!correct)
                    {
                        if (!mForgiveFurtherMistakes)
                        {
                            if (fastTypingDetected)
                            {
                                Toast.makeText(mSelfContext, "Typo forgiven", Toast.LENGTH_SHORT).show();
                            }
                            else
                            {
                                mForgiveFurtherMistakes = true; // hint was revoked
                            }
                        }

                        setKeyboardEnabled(false);

                        // Automatically correct shortly: remove last wrong letter
                        new Handler().postDelayed(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                if (mErrorState)
                                {
                                    mWordText.setText(input.substring(0, input.length() - 1));
                                    setKeyboardEnabled(true);
                                    afterTextChanged(mWordText);
                                }
                            }
                        }, TRAINER_CORRECTION_DELAY);
                    }
                }
            }
        }

        public void onEnter(TextView v)
        {
            try
            {
                boolean correct = mTest.checkAnswer(mWordText.getText().toString());
                showInputCorrectness(correct);
            }
            catch (TestAlreadyCompleteException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return;
            }

            if (mTest.getResult() != TestResult.Incomplete)
            {
                changeState(State.STATE_TEST_FINISHED);
            }
            else
            {
                showTestState(); // update lives
            }
        }
    };

    GuessValidator mGuessValidator = null;

    private View.OnClickListener mKeyBoardListener = new OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            Button b = (Button) v;
            mWordText.append(b.getText());
            mGuessValidator.afterTextChanged(mWordText);
        }
    };

    private void startTest(ReverseTest test)
    {
        mTest = test;
        mTransText.setText(test.getChallenge());
        mWordText.setText("");
        mForgiveFurtherMistakes = false;
        changeState(State.STATE_TEST_ONGOING);
    }

    private void setKeyboardEnabled(boolean enabled)
    {
        mEnter.setEnabled(enabled);
        for (Button k : mKeys)
        {
            k.setEnabled(enabled);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trainer);
        mWordText = (TextView) findViewById(R.id.textWord);
        mTransText = (TextView) findViewById(R.id.textTrans);
        mImgLives.add((ImageView) findViewById(R.id.imgLife1));
        mImgLives.add((ImageView) findViewById(R.id.imgLife2));
        mImgLives.add((ImageView) findViewById(R.id.imgLife3));
        mHintButton = (ImageView) findViewById(R.id.imgHint);
        mNextButton = (ImageView) findViewById(R.id.imgNext);
        mExamProgress = (TextView) findViewById(R.id.textProgress);
        mKeyboard = (GridLayout) findViewById(R.id.gridKeyboard);
        mEnter = (Button) findViewById(R.id.btnEnter);
        mMarkRating = (RatingBar) findViewById(R.id.rbMark);
        mSelfContext = this;

        mOkIcon = getResources().getDrawable(R.drawable.ic_action_accept);
        mPassedIcon = getResources().getDrawable(R.drawable.ic_action_good);
        mFailIcon = getResources().getDrawable(R.drawable.ic_action_bad);
        mLifeIcon = getResources().getDrawable(R.drawable.ic_action_favorite);

        mGuessValidator = new GuessValidator();

        mHintButton.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                useHint();
            }
        });

        mNextButton.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                nextTest();
            }
        });

        mEnter.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                mGuessValidator.onEnter(mWordText);
            }
        });

        String lines12 = "qwertyuiopåasdfghjklöä";
        String line3 = "__zxcvbnm__";

        final int BUTTON_HEIGHT = 124;
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
                    params.bottomMargin = (r < 2) ? 30 : 10;

                    Button k = new Button(mSelfContext, null, android.R.attr.buttonStyle);
                    k.setMinHeight(BUTTON_HEIGHT);
                    k.setMinWidth(80);
                    k.setMinimumHeight(BUTTON_HEIGHT);
                    k.setMinimumWidth(80);
                    k.setPadding(0, 0, 0, 0);
                    k.setOnClickListener(mKeyBoardListener);
                    k.setText(cap);
                    k.setTypeface(k.getTypeface(), 1); // bold
                    k.setTextSize(22); // sp

                    mKeyboard.addView(k, params);
                    mKeys.add(k);
                }
            }
        }
        mEnter.setMinHeight(BUTTON_HEIGHT);
        mEnter.setMinimumHeight(BUTTON_HEIGHT);

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

    void changeState(State newState)
    {
        switch (newState)
        {
            case STATE_INITIAL:
            case STATE_MAKING_NORMAL_EXAM:
            case STATE_MAKING_REVIEW_EXAM:
                setKeyboardEnabled(false);
                mHintButton.setEnabled(false);
                mNextButton.setEnabled(false);
                mMarkRating.setVisibility(View.INVISIBLE);
                showTestState();
                showExamProgress(false);
                break;
            case STATE_TEST_ONGOING:
                setKeyboardEnabled(true);
                mHintButton.setEnabled(true);
                mNextButton.setEnabled(true);
                mMarkRating.setVisibility(View.VISIBLE);
                showTestState();
                showExamProgress(true);
                break;
            case STATE_TEST_FINISHED:
                setKeyboardEnabled(false);
                mHintButton.setEnabled(false);
                showTestState();
                showInputCorrectness(true);
                showExamProgress(true);
                break;
            case STATE_EXAM_FINISHED:
                mKeyboard.setVisibility(View.INVISIBLE);
                mHintButton.setVisibility(View.INVISIBLE);
                mNextButton.setVisibility(View.INVISIBLE);
                mTransText.setText("Exam finished!");
                mWordText.setText("");
                showLifes(0);
                showExamProgress(false);
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
                showLifes(mTest.getHintsLeft());
                break;

            case Passed:
                showTestResultIcon(mPassedIcon);
                break;

            case PassedWithHints:
                showTestResultIcon(mOkIcon);
                break;

            case Failed:
                mWordText.setText(mTest.getAnswer());
                showTestResultIcon(mFailIcon);
                break;

            default:
                assert false;
                return;
        }

        mMarkRating.setRating(ExamResultActivity.markToRate(mTest.getMark()));
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
            mWordText.setError(null);
        }
        else
        {
            mWordText.setError("wrong"); // sets special icon
        }
        mErrorState = !correct;
    }

    private void nextTest()
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
                //changeState(State.STATE_EXAM_FINISHED);
                Intent intent = new Intent(this, ExamResultActivity.class);
                ArrayList<Word> testWords = mExam.getResults();
                intent.putParcelableArrayListExtra(ExamResultActivity.INTENT_EXTRA_REV_EXAM, testWords);
                startActivity(intent);
                finish();
            }
        }
    }

    private void useHint()
    {
        try
        {
            if (mTest.getHintsLeft() > 0)
            {
                String newText = mTest.getHint(mWordText.getText().toString());
                mWordText.setText(newText);
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

    private void showExamProgress(boolean isVisible)
    {
        if (mExam != null)
        {
            int currentTest = mExam.getTestsCount() - mExam.getTestsToGo();
            mExamProgress.setText(String.valueOf(currentTest) + "/" + String.valueOf(mExam.getTestsCount()));
        }
        mExamProgress.setVisibility(isVisible ? View.VISIBLE : View.INVISIBLE);
    }

    public static void startExamActivity(Activity caller)
    {
        Intent intent = new Intent(caller, TrainerActivity.class);
        caller.startActivity(intent);
    }
}
