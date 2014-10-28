package com.burnevsky.firu;

import java.util.ArrayList;
import java.util.List;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.text.Editable;
import android.text.Spannable;
import android.text.TextWatcher;

import com.burnevsky.firu.model.Dictionary;
import com.burnevsky.firu.model.test.ReverseTest;
import com.burnevsky.firu.model.test.TestAlreadyCompleteException;
import com.burnevsky.firu.model.test.TestResult;
import com.burnevsky.firu.model.test.VocabularyTest;

public class TrainerActivity extends Activity
{
    Context mSelfContext = null;
    Dictionary mDict = null;
    int mKeyBoardHeight = 0;
    View mLayout = null;
    TextView mMarkText = null;
    TextView mTransText = null;
    EditText mWordEdit = null;
    Drawable mGoodIcon = null, mPassedIcon = null, mFailIcon = null;
    Button mHintButton = null, mAnswerButton = null;

    ReverseTest mTest = new ReverseTest();
    final int NON_WRONG_ANSWER_LENGTH = Integer.MAX_VALUE; 
    int mWrongGuessLength = NON_WRONG_ANSWER_LENGTH;
    List<ImageView> mImgLives = new ArrayList<ImageView>();
    
    class GuessValidator implements TextWatcher, TextView.OnEditorActionListener
    {
        public void beforeTextChanged(CharSequence s, int start, int count, int after)
        {
        }

        public void onTextChanged(CharSequence s, int start, int before, int count)
        {
        }

        public void afterTextChanged(Editable s)
        {
            String input = s.toString();
            Log.d("firu", input);
            if (input.length() == 0)
            {
                mWordEdit.setCompoundDrawables(null, null, null, null);
                mWrongGuessLength = NON_WRONG_ANSWER_LENGTH;
            }
            else if (input.length() <= mWrongGuessLength)
            {
                boolean correct = mTest.checkGuess(input); 
                showInputResult(correct);
                if (correct)
                {
                    mWrongGuessLength = NON_WRONG_ANSWER_LENGTH;
                }
                else
                {
                    mWrongGuessLength = input.length();
                }
            }
        }

        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
        {
            String se = (event != null) ? event.toString() : "None";
            Log.d("firu", "EditorAction  : id " + String.valueOf(actionId) + " event " + se);

            try
            {
                boolean correct = mTest.checkAnswer(mWordEdit.getText().toString()); 
                showInputResult(correct);
                showTestState();
            }
            catch (TestAlreadyCompleteException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            return false;
        }
    };

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
        mHintButton = (Button) findViewById(R.id.btnHint);
        mAnswerButton = (Button) findViewById(R.id.btnAnswer);
        mSelfContext = this;

        mGoodIcon = getResources().getDrawable(R.drawable.ic_action_good);
        mPassedIcon = getResources().getDrawable(R.drawable.ic_action_accept);
        mFailIcon = getResources().getDrawable(R.drawable.ic_action_bad);

        GuessValidator gv = new GuessValidator();
        mWordEdit.setOnEditorActionListener(gv);
        mWordEdit.addTextChangedListener(gv);

        mHintButton.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                try
                {
                    String newText = mTest.getHint(mWordEdit.getText().toString());
                    mWordEdit.setText(newText);
                    mWordEdit.setSelection(newText.length());
                }
                catch (TestAlreadyCompleteException e)
                {
                    e.printStackTrace();
                }
                showTestState();
            }
        });

        mAnswerButton.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                mTest.unlockAnswer();
                showTestState();
            }
        });

        // InputMethodManager imm =
        // (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        // imm.showSoftInput(mLayout, InputMethodManager.SHOW_FORCED);

        FiruApplication app = (FiruApplication) getApplicationContext();
        mDict = app.mDict;

        Intent intent = getIntent();
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

    void showInputResult(boolean accepted)
    {
        if (accepted)
        {
            mWordEdit.setError(null);
            showInputValue(mGoodIcon);
        }
        else
        {
            mWordEdit.setError("wrong"); // sets special icon
        }
    }
    
    void showLifes()
    {
        int lives = mTest.getHintsLeft();
        mImgLives.get(0).setVisibility((lives >= 1) ? View.VISIBLE : View.INVISIBLE);
        mImgLives.get(1).setVisibility((lives >= 2) ? View.VISIBLE : View.INVISIBLE);
        mImgLives.get(2).setVisibility((lives >= 3) ? View.VISIBLE : View.INVISIBLE);
        mHintButton.setEnabled(lives > 0);
    }

    void showTestResultIcon(Drawable icon)
    {
        mImgLives.get(0).setVisibility(View.INVISIBLE);
        mImgLives.get(2).setVisibility(View.INVISIBLE);
        mImgLives.get(1).setVisibility(View.VISIBLE);
        mImgLives.get(1).setImageDrawable(icon);
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
    
    void showTestState()
    {
        if (mTest.getResult() != TestResult.Incomplete)
        {
            mWordEdit.setEnabled(false);
            mAnswerButton.setEnabled(false);
            mHintButton.setEnabled(false);
            mMarkText.setText(mTest.getResult().toString()); // TODO: l10n
            switch (mTest.getResult())
            {
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
                    break;
            }
        }
        else
        {
            showLifes();
        }
    }
}
