package com.burnevsky.firu;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.SimpleAdapter;

import com.burnevsky.firu.model.Mark;
import com.burnevsky.firu.model.Word;
import com.burnevsky.firu.model.exam.Exam;
import com.burnevsky.firu.model.exam.ReverseExam;
import com.burnevsky.firu.model.exam.ExamChallenge;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

public class ExamResultActivity extends FiruActivityBase implements OnItemClickListener
{
    private final static String INTENT_EXTRA_REV_EXAM = "com.burnevsk.firu.reverse_exam";

    private List<ExamChallenge> mSortedTests;

    public static void showExamResults(Activity caller, Exam exam)
    {
        Intent intent = new Intent(caller, ExamResultActivity.class);
        intent.putParcelableArrayListExtra(ExamResultActivity.INTENT_EXTRA_REV_EXAM, exam.getResults());
        caller.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exam_result);

        Intent intent = getIntent();
        mSortedTests = intent.getParcelableArrayListExtra(INTENT_EXTRA_REV_EXAM);

        Collections.sort(mSortedTests, new Comparator<ExamChallenge>()
        {
            @Override
            public int compare(ExamChallenge lhs, ExamChallenge rhs)
            {
                return lhs.getMark().toInt() - rhs.getMark().toInt(); // ascending order
            }
        });

        List<SortedMap<String, Object>> data = new ArrayList<>();

        for (ExamChallenge test : mSortedTests)
        {
            TreeMap<String, Object> row = new TreeMap<>();
            row.put("word", test.mWord.getText());
            row.put("trans", test.mTranslation.getText());
            row.put("rate", markToRate(test.getMark()));
            data.add(row);
        }

        String[] columns = {"word", "trans", "rate"};
        int[] fields = {R.id.era_textWord, R.id.era_textTrans, R.id.era_rbMark};

        SimpleAdapter mAdapter = new SimpleAdapter(this, data, R.layout.marked_word_translation_list_item, columns, fields);
        mAdapter.setViewBinder(new SimpleAdapter.ViewBinder()
		{
            @Override
            public boolean setViewValue(View view, Object data, String textRepresentation)
			{
                if (view.getId() == R.id.era_rbMark)
				{
                    Integer rate = (Integer) data;
                    RatingBar rbMark = (RatingBar) view;
                    rbMark.setRating(rate);
                    return true;
                }
				else
				{
                    return false;
                }
            }
        });

        ListView listView = (ListView) findViewById(R.id.era_listTranslations);
        listView.setAdapter(mAdapter);
        listView.setOnItemClickListener(this);
    }

    public static int markToRate(Mark mark)
    {
        int i = mark.toInt();
        return i <= Mark.YET_TO_LEARN.toInt() ? 0 : i - 1;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.exam_result, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id)
        {
            case R.id.action_moveon:
                startNextExam();
                return true;
            case R.id.action_home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id)
    {
        ArrayList<Word> words = new ArrayList<>();

        for (ExamChallenge test : mSortedTests)
        {
            words.add(test.mWord);
        }

        TranslationsActivity.showWords(this, words, position);
    }

    private void startNextExam()
    {
        finish();
        mApp.startNextExam(this);
    }
}
