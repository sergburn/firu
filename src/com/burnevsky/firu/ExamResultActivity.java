package com.burnevsky.firu;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import com.burnevsky.firu.model.Mark;
import com.burnevsky.firu.model.Word;
import com.burnevsky.firu.model.MarkedTranslation;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.SimpleAdapter;

public class ExamResultActivity extends Activity implements OnItemClickListener
{
    public final static String INTENT_EXTRA_REV_EXAM = "com.burnevsk.firu.reverse_exam";

    private SimpleAdapter mAdapter = null;

    private Word[] mSortedTests;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exam_result);

        Intent intent = getIntent();
        ArrayList<Word> tests = intent.getParcelableArrayListExtra(INTENT_EXTRA_REV_EXAM);

        mSortedTests = new Word[tests.size()];
        tests.toArray(mSortedTests);
        Arrays.sort(mSortedTests, new Comparator<Word>()
            {
            @Override
            public int compare(Word lhs, Word rhs)
            {
                MarkedTranslation lt = (MarkedTranslation) lhs.translations.get(0);
                MarkedTranslation rt = (MarkedTranslation) rhs.translations.get(0);
                return lt.ReverseMark.toInt() - rt.ReverseMark.toInt(); // ascending order
            }
            });

        List<SortedMap<String, Object>> data = new ArrayList<SortedMap<String,Object>>();

        for (Word test : mSortedTests)
        {
            TreeMap<String, Object> row = new TreeMap<String, Object>();
            row.put("word", test.getText());
            MarkedTranslation mt = (MarkedTranslation) test.translations.get(0);
            row.put("trans", mt.getText());
            row.put("rate", markToRate(mt.ReverseMark));
            data.add(row);
        }

        String[] columns = {"word", "trans", "rate"};
        int[] fields = {R.id.era_textWord, R.id.era_textTrans, R.id.era_rbMark};

        mAdapter = new SimpleAdapter(this, data, R.layout.marked_translation_list_item, columns, fields);
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
        return i <= Mark.YetToLearn.toInt() ? 0 : i - 1;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
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
        if (id == R.id.action_moveon)
        {
            startNextExam();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id)
    {
        Word word = mSortedTests[position];
        TranslationsActivity.showVocWord(this, word);
    }

    private void startNextExam()
    {
        finish();
        TrainerActivity.startExamActivity(this);
    }
}
