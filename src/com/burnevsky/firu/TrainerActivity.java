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
import java.util.Arrays;
import java.util.List;

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
import android.os.SystemClock;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.support.v7.widget.GridLayout;
import android.util.TypedValue;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.burnevsky.firu.model.DictionaryID;
import com.burnevsky.firu.model.Mark;
import com.burnevsky.firu.model.Model;
import com.burnevsky.firu.model.exam.ReverseExam;
import com.burnevsky.firu.model.exam.ReverseExamBuilder;
import com.burnevsky.firu.model.exam.ReverseTest;
import com.burnevsky.firu.model.exam.TestAlreadyCompleteException;
import com.burnevsky.firu.model.exam.TestResult;

public class TrainerActivity extends FiruActivityBase
{
    private TextView mTransText = null;
    private TextView mWordText = null;
    private Drawable mOkIcon = null, mPassedIcon = null, mFailIcon = null, mLifeIcon = null;
    private ImageView mHintButton = null, mNextButton = null;
    private final List<ImageView> mImgLives = new ArrayList<>();
    private TextView mExamProgress = null;
    private GridLayout mKeyboard = null;
    private Button mEnter = null;
    private final ArrayList<Button> mKeys = new ArrayList<>();
    private RatingBar mMarkRating = null;

    private final char[] mAnswerTemplate = new char[32]; // enough for any word, I guess

    private static final String[] TRAINER_KEYBOARD_LINES = {
        "qwertyuiopå",
        "asdfghjklöä",
        "__zxcvbnm__"
    };

    private static final int TRAINER_KEYBOARD_LINE_LENGTH = 11;
    private static final int TRAINER_KEYBOARD_LINES_NUM = TRAINER_KEYBOARD_LINES.length;

    private static final long TRAINER_CORRECTION_DELAY = 500;
    private static final long TRAINER_MAX_TYPO_DELAY = 500;

    private static class Data extends Fragment
    {
        public ReverseExam mExam = null;
        public ReverseTest mTest = null;
        public boolean mErrorState;

        public String mInputText = null;

        // User must enter word without typing mistakes to Pass the test
        // (otherwise she can use try-and-error approach)
        // However, after 1st typo takes away 1 life, which counted as 1 hint,
        // further typos are not counted.
        // This way typos don't let mark to upgrade to rates Well-known and Learned
        public boolean mForgiveFurtherMistakes = false;

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

    // TODO: setting
    private boolean mShowWordLength = true;

    public TrainerActivity()
    {
        Arrays.fill(mAnswerTemplate, '\u2022');
    }

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
                        .setTitle("Reverse exam")
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
        new BuildReverseExam().execute();
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

    class BuildReverseExam extends AsyncTask<Mark, Void, ReverseExam>
    {
        @Override
        protected ReverseExam doInBackground(Mark... param)
        {
            return (mModel.getVocabulary() != null) ? ReverseExamBuilder.buildExam(mModel.getVocabulary()) : null;
        }

        @Override
        protected void onPostExecute(ReverseExam exam)
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
                Toast.makeText(TrainerActivity.this, "Failed to make exam", Toast.LENGTH_SHORT).show();
            }
        }
    }

    class GuessValidator
    {
        private long mLastClick = 0;

        private boolean isTypo()
        {
            if (mData.mInputText.length() == 0)
            {
                return false;
            }

            String subGuess = mData.mInputText.substring(0, mData.mInputText.length() - 1);
            char last = mData.mInputText.charAt(mData.mInputText.length() - 1);

            String variants = selectKeysAround(last);
            try
            {
                for (char c : variants.toCharArray())
                {
                    if (mData.mTest.checkGuess(subGuess + c, true))
                    {
                        return true;
                    }
                }
            }
            catch (TestAlreadyCompleteException e)
            {
                e.printStackTrace();
            }
            return false;
        }

        private String selectKeysAround(char last)
        {
            for (int r = 0; r < TRAINER_KEYBOARD_LINES_NUM; r++)
            {
                for (int c = 0; c < TRAINER_KEYBOARD_LINE_LENGTH; c++)
                {
                    if (TRAINER_KEYBOARD_LINES[r].charAt(c) == last)
                    {
                        String variants = selectCharsAround(TRAINER_KEYBOARD_LINES[r], c);
                        if (r > 0) // above
                        {
                            variants += selectCharsAround(TRAINER_KEYBOARD_LINES[r-1], c);
                        }
                        if (r < TRAINER_KEYBOARD_LINES_NUM - 1) // below
                        {
                            variants += selectCharsAround(TRAINER_KEYBOARD_LINES[r+1], c);
                        }
                        return variants;
                    }
                }
            }
            return "";
        }

        private String selectCharsAround(String line, int index)
        {
            int start = Math.max(0, index - 1);
            int end = Math.min(line.length(), index + 2);
            return line.substring(start, end);
        }

        public void afterTextChanged()
        {
            if (mData.mInputText.length() == 0)
            {
                showInputCorrectness(true);
            }
            else
            {
                long now = SystemClock.uptimeMillis();
                boolean fastTypingDetected = (now - mLastClick) < TrainerActivity.TRAINER_MAX_TYPO_DELAY;
                mLastClick = now;

                boolean correct = false;
                try
                {
                    correct = mData.mTest.checkGuess(mData.mInputText, true); // do not revoke hint yet
                    if (!correct && !mData.mForgiveFurtherMistakes)
                    {
                        boolean typo = fastTypingDetected && isTypo();
                        if (typo)
                        {
                            Toast.makeText(TrainerActivity.this, "Typo forgiven", Toast.LENGTH_SHORT).show();
                        }
                        else
                        {
                            mData.mTest.revokeHint();
                            mData.mForgiveFurtherMistakes = true;
                        }
                    }
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

                    if (correct)
                    {
                        if (mShowWordLength &&
                            mData.mInputText.length() == mData.mTest.getAnswerLength())
                        {
                            onEnter(); // save user one button push
                        }
                    }
                    else
                    {
                        setKeyboardEnabled(false);

                        // Automatically correct shortly: remove last wrong letter
                        new Handler().postDelayed(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                if (mData.mErrorState)
                                {
                                    onBackspace();
                                    setKeyboardEnabled(true);
                                }
                            }
                        }, TRAINER_CORRECTION_DELAY);
                    }
                }
            }
        }

        public void onEnter()
        {
            try
            {
                boolean correct = mData.mTest.checkAnswer(mData.mInputText);
                showInputCorrectness(correct);
            }
            catch (TestAlreadyCompleteException e)
            {
                // TODO Auto-generated catch block
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
            }
        }

        public void onBackspace()
        {
            mData.mInputText = mData.mInputText.substring(0, mData.mInputText.length() - 1);
            showAnswerText();
            afterTextChanged();
        }
    }

    GuessValidator mGuessValidator = null;

    private final View.OnClickListener mKeyBoardListener = new OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            Button b = (Button) v;
            mData.mInputText += b.getText();
            showAnswerText();
            mGuessValidator.afterTextChanged();
        }
    };

    private void startTest(ReverseTest test)
    {
        mData.mTest = test;
        mData.mInputText = "";
        mData.mForgiveFurtherMistakes = false;
        changeState(Data.State.STATE_TEST_ONGOING);
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
                mGuessValidator.onEnter();
            }
        });

        makeKeyboard();

        //View thisView = this.getWindow().getDecorView().findViewById(android.R.id.content);

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

    private void makeKeyboard()
    {
        DisplayMetrics display = getResources().getDisplayMetrics();

        final int BUTTON_HEIGHT_DP = 42;
        final int BUTTON_WIDTH_DP = 30;
        final int BUTTON_ROW_GAP_DP = 10;

        final int BUTTON_WIDTH = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, BUTTON_WIDTH_DP, display);

        final int pxWidth = Math.min(BUTTON_WIDTH, display.widthPixels / TRAINER_KEYBOARD_LINE_LENGTH); // no padding anywhere
        final int pxHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, BUTTON_HEIGHT_DP, display);

        for (int r = 0; r < TRAINER_KEYBOARD_LINES_NUM; r++)
        {
            GridLayout.Spec rowSpec = GridLayout.spec(r, 1.0f);
            for (int c = 0; c < TRAINER_KEYBOARD_LINE_LENGTH; c++)
            {
                char cap = TRAINER_KEYBOARD_LINES[r].charAt(c);
                if (Character.isLetter(cap))
                {
                    GridLayout.Spec colSpec = GridLayout.spec(c, 1.0f);

                    GridLayout.LayoutParams params = new GridLayout.LayoutParams(rowSpec, colSpec);
                    params.bottomMargin = (r < TRAINER_KEYBOARD_LINES_NUM - 1) ? BUTTON_ROW_GAP_DP : BUTTON_ROW_GAP_DP / 2;

                    Button k = new Button(this, null, android.R.attr.buttonStyle);
                    k.setMinHeight(pxHeight);
                    k.setMinWidth(pxWidth);
                    k.setMinimumHeight(pxHeight);
                    k.setMinimumWidth(pxWidth);
                    k.setPadding(0, 0, 0, 0);
                    k.setOnClickListener(mKeyBoardListener);
                    k.setText(String.valueOf(cap));
                    k.setTypeface(k.getTypeface(), 1); // bold
                    k.setTextSize(22); // sp
                    //k.setGravity(Gravity.CENTER);
                    k.setAllCaps(false);

                    mKeyboard.addView(k, params);
                    mKeys.add(k);
                }
            }
        }
        mEnter.setVisibility(View.GONE);
        mEnter.setMinHeight(pxHeight);
        mEnter.setMinimumHeight(pxHeight);
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
                mTransText.setText("");
            case STATE_MAKING_EXAM:
                setKeyboardEnabled(false);
                mHintButton.setEnabled(false);
                mNextButton.setEnabled(false);
                mEnter.setVisibility(View.INVISIBLE);
                mMarkRating.setVisibility(View.INVISIBLE);
                showAnswerText();
                showTestResult();
                showExamProgress(false);
                break;
            case STATE_TEST_ONGOING:
                setKeyboardEnabled(true);
                mHintButton.setEnabled(true);
                mNextButton.setEnabled(true);
                mEnter.setVisibility(mShowWordLength ? View.INVISIBLE : View.VISIBLE);
                mMarkRating.setVisibility(View.VISIBLE);
                mTransText.setText(mData.mTest.getChallenge());
                showAnswerText();
                showTestResult();
                showExamProgress(true);
                break;
            case STATE_TEST_FINISHED:
                setKeyboardEnabled(false);
                mHintButton.setEnabled(false);
                showAnswerText();
                showTestResult();
                showInputCorrectness(true);
                showExamProgress(true);
                break;
            default:
                break;
        }
        mData.mState = newState;
    }

    void showAnswerText()
    {
        if (mData.mTest != null)
        {
            String word = mData.mInputText;
            if (mShowWordLength)
            {
                word += String.valueOf(mAnswerTemplate, 0, mData.mTest.getAnswerLength() - mData.mInputText.length());
            }
            mWordText.setText(word);
        }
        else
        {
            mWordText.setText("");
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
                    mWordText.setText(mData.mTest.getAnswer());
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
            mWordText.setError(null);
        }
        else
        {
            mWordText.setError("wrong"); // sets special icon
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
            ReverseTest test = mData.mExam.nextTest();
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

    private void useHint()
    {
        try
        {
            if (mData.mTest.getHintsLeft() > 0)
            {
                mData.mInputText = mData.mTest.getHint(mData.mInputText);
                showAnswerText();
                mGuessValidator.afterTextChanged();
                showTestResult();
            }
            else
            {
                mData.mTest.unlockAnswer();
                changeState(Data.State.STATE_TEST_FINISHED);
            }
        }
        catch (TestAlreadyCompleteException e)
        {
            e.printStackTrace();
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
        Intent intent = new Intent(caller, TrainerActivity.class);
        caller.startActivity(intent);
    }
}
