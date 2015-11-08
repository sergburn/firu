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

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.androidplot.pie.PieChart;
import com.androidplot.pie.PieRenderer;
import com.androidplot.pie.PieWidget;
import com.androidplot.pie.Segment;
import com.androidplot.pie.SegmentFormatter;
import com.burnevsky.firu.model.Dictionary;
import com.burnevsky.firu.model.Mark;
import com.burnevsky.firu.model.Vocabulary;
/*
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.PointLabelFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYSeries;
import com.androidplot.xy.XYStepMode;
 */

public class StatActivity extends FiruActivityBase
{
    private TextView mTotal;

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
                showPie();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stat);

        mTotal = (TextView) findViewById(R.id.textTotal);

        //showXYplot();
        //findViewById(R.id.mySimpleXYPlot).setVisibility(View.INVISIBLE);

        //showPie();
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
        int id = item.getItemId();
        if (id == R.id.action_settings)
        {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showPie()
    {
        PieChart pie = (PieChart) findViewById(R.id.mySimplePieChart);

        PieWidget pw = pie.getPieWidget();
        if (pw != null)
        {
            Paint bp = pw.getBackgroundPaint();
            if (bp != null)
            {
                bp.setColor(Color.WHITE);
            }
            else
            {
                bp = new Paint();
                bp.setColor(Color.WHITE);
                //pw.setBackgroundPaint(bp);
            }
        }

        SegmentFormatter sf0 = new SegmentFormatter();
        sf0.configure(getApplicationContext(), R.xml.pie_segment_formatter);
        sf0.getFillPaint().setColor(Color.WHITE);

        SegmentFormatter sf1 = new SegmentFormatter();
        sf1.configure(getApplicationContext(), R.xml.pie_segment_formatter);
        sf1.getFillPaint().setColor(Color.RED);

        SegmentFormatter sf2 = new SegmentFormatter();
        sf2.configure(getApplicationContext(), R.xml.pie_segment_formatter);
        sf2.getFillPaint().setColor(Color.YELLOW);

        SegmentFormatter sf3 = new SegmentFormatter();
        sf3.configure(getApplicationContext(), R.xml.pie_segment_formatter);
        sf3.getFillPaint().setColor(Color.CYAN);

        SegmentFormatter sf4 = new SegmentFormatter();
        sf4.configure(getApplicationContext(), R.xml.pie_segment_formatter);
        sf4.getFillPaint().setColor(Color.GREEN);

        Map<Mark, SegmentFormatter> styles = new TreeMap<>();
        styles.put(Mark.Unfamiliar, sf0);
        styles.put(Mark.YetToLearn, sf1);
        styles.put(Mark.WithHints, sf2);
        styles.put(Mark.AlmostLearned, sf3);
        styles.put(Mark.Learned, sf4);

        Vocabulary.LearningStats stats = mLearningStats;
        if (stats == null || stats.TotalTranslationsCount == 0)
        {
            stats = new Vocabulary.LearningStats();
            stats.ReverseMarksDistribution.put(Mark.Unfamiliar, 100);
            stats.TotalTranslationsCount = 100;
        }

        for (Mark mark : styles.keySet())
        {
            float portion = 0.0f;
            if (stats.ReverseMarksDistribution.containsKey(mark))
            {
                portion = (float) stats.ReverseMarksDistribution.get(mark) / (float) stats.TotalTranslationsCount;
            }
            if (portion > 0.01) // > 1%
            {
                pie.addSegment(new Segment(mark.toString(), portion), styles.get(mark));
            }
        }

        if (pie.getRenderer(PieRenderer.class) != null) // null, if no segments added?
        {
            pie.getRenderer(PieRenderer.class).setDonutSize(0.25f, PieRenderer.DonutMode.PERCENT);
        }
        pie.redraw();

        mTotal.setText(String.valueOf(stats.TotalTranslationsCount));
    }
    /*
    private void showXYplot()
    {
        // initialize our XYPlot reference:
        plot = (XYPlot) findViewById(R.id.mySimpleXYPlot);

        // Create a couple arrays of y-values to plot:
        Number[] series1Numbers = {1, 8, 5, 2, 7, 4};
        Number[] series2Numbers = {4, 6, 3, 8, 2, 10};

        // Turn the above arrays into XYSeries':
        XYSeries series1 = new SimpleXYSeries(
                Arrays.asList(series1Numbers),          // SimpleXYSeries takes a List so turn our array into a List
                SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, // Y_VALS_ONLY means use the element index as the x value
                "Series1");                             // Set the display title of the series

        // Create a formatter to use for drawing a series using LineAndPointRenderer
        // and configure it from xml:
        LineAndPointFormatter series1Format = new LineAndPointFormatter();
        series1Format.setPointLabelFormatter(new PointLabelFormatter());
        series1Format.configure(getApplicationContext(),
                R.xml.line_point_formatter_with_plf1);

        // add a new series' to the xyplot:
        plot.addSeries(series1, series1Format);

        /*
        // same as above:

        XYSeries series2 = new SimpleXYSeries(
                Arrays.asList(series2Numbers),
                SimpleXYSeries.ArrayFormat.Y_VALS_ONLY,
                "Series2");

        LineAndPointFormatter series2Format = new LineAndPointFormatter();
        series2Format.setPointLabelFormatter(new PointLabelFormatter());
        series2Format.configure(getApplicationContext(),
                R.xml.line_point_formatter_with_plf2);

        LineAndPointFormatter series2Format = new LineAndPointFormatter(Color.RED, Color.GREEN, Color.BLUE, null);

        plot.addSeries(series2, series2Format);
     */
    /*
        // reduce the number of range labels
        plot.setTicksPerDomainLabel(1);
        plot.setDomainStep(XYStepMode.INCREMENT_BY_VAL, 1);

        plot.setTicksPerRangeLabel(1);
        plot.setRangeStep(XYStepMode.INCREMENT_BY_VAL, 2);

        plot.getGraphWidget().setDomainLabelOrientation(0);
    }
     */
}
