
package com.burnevsky.firu;

import java.util.ArrayList;
import java.util.List;

import com.burnevsky.firu.model.DictionaryID;
import com.burnevsky.firu.model.IDictionary;
import com.burnevsky.firu.model.Model;
import com.burnevsky.firu.model.Word;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;

public class MainActivity
extends FiruActivityBase
implements SearchFragment.OnTranslationSelectedListener, SearchTransFragment.OnWordSelectedListener
{
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private ScrollView mDrawer;
    public String mViewTitle;
    private boolean mTransSearchActive;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawer = (ScrollView) findViewById(R.id.left_drawer);

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.drawer_open, R.string.drawer_close)
        {
            @Override
            public void onDrawerClosed(View view)
            {
                super.onDrawerClosed(view);
                getSupportActionBar().setTitle(mViewTitle);
            }

            @Override
            public void onDrawerOpened(View drawerView)
            {
                hideKeyboard();
                super.onDrawerOpened(drawerView);
                mViewTitle = getSupportActionBar().getTitle().toString();
                getSupportActionBar().setTitle("Firu");
            }
        };

        // Set the drawer toggle as the DrawerListener
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        View btnSearch = findViewById(R.id.rlWords);
        btnSearch.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                showSearchUi("");
            }
        });

        View btnRevSearch = findViewById(R.id.rlTrans);
        btnRevSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSearchTransUi();
            }
        });

        findViewById(R.id.rlTrainer).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mApp.startNextExam(MainActivity.this);
                mDrawerLayout.closeDrawer(mDrawer);
            }
        });

        findViewById(R.id.dropArrow).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                toggleVocabularyControls();
            }
        });

        findViewById(R.id.imgStats).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intent2 = new Intent(MainActivity.this, StatActivity.class);
                startActivity(intent2);
            }
        });

        findViewById(R.id.imgUpload).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                mApp.exportVocabulary(MainActivity.this);
            }
        });

        findViewById(R.id.imgDownload).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                mApp.importVocabulary(MainActivity.this);
            }
        });

        findViewById(R.id.imgReset).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                mApp.resetVocabulary(MainActivity.this);
            }
        });

        findViewById(R.id.imgSettings).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                mApp.resetVocabulary(MainActivity.this);
            }
        });

        findViewById(R.id.rlVocControls).setVisibility(View.GONE);

        setTextViewText(R.id.txtWordsBadge, "");
        setTextViewText(R.id.txtTransBadge, "");
        setTextViewText(R.id.txtTrainerBadge, "");

        String sharedText = "";
        Intent intent = getIntent();
        if (intent.getAction() == Intent.ACTION_SEND)
        {
            sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
        }

        showSearchUi(sharedText);
    }

    @Override
    public void onBackPressed()
    {
        if (mDrawerLayout.isDrawerOpen(mDrawer))
        {
            mDrawerLayout.closeDrawer(mDrawer);
        }
        else
        {
            super.onBackPressed();
        }
    }

    @Override
    public void onDictionaryEvent(DictionaryID dictionaryID, Model.ModelEvent event)
    {
        super.onDictionaryEvent(dictionaryID, event);
        if (event == Model.ModelEvent.MODEL_EVENT_OPENED ||
            event == Model.ModelEvent.MODEL_EVENT_READY)
        {
            IDictionary dict = mModel.getDictionary(dictionaryID);
            if (dictionaryID == DictionaryID.UNIVERSAL)
            {
                setTextViewText(R.id.txtWordsBadge, getShortIntString(dict.getTotalWords()));
                setTextViewText(R.id.txtTransBadge, getShortIntString(dict.getTotalTranslations()));
            }
            else if (dictionaryID == DictionaryID.VOCABULARY)
            {
                setTextViewText(R.id.txtTrainerBadge, getShortIntString(dict.getTotalTranslations()));
            }
        }
        else
        {
            if (dictionaryID == DictionaryID.UNIVERSAL)
            {
                setTextViewText(R.id.txtWordsBadge, "");
                setTextViewText(R.id.txtTransBadge, "");
            }
            else if (dictionaryID == DictionaryID.VOCABULARY)
            {
                setTextViewText(R.id.txtTrainerBadge, "");
            }
        }
    }

    static private String getShortIntString(int value)
    {
        if (value > 1000)
            return String.valueOf(value / 1000) + "k";
        else
            return String.valueOf(value);
    }

    private void setTextViewText(int id, CharSequence text)
    {
        TextView txtWordsBadge = (TextView) findViewById(id);
        if (txtWordsBadge != null)
        {
            txtWordsBadge.setText(text);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig)
    {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState)
    {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if (mDrawerToggle.onOptionsItemSelected(item))
        {
            return true;
        }

        switch(item.getItemId())
        {
            case R.id.action_switch:
                if (mTransSearchActive)
                    showSearchUi("");
                else
                    showSearchTransUi();
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu)
    {
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onTranslationSelected(ArrayList<Word> allMatches, int selection)
    {
        TranslationsActivity.showWords(this, allMatches, selection);

        // TODO Show in a side fragment on large screens
    }

    @Override
    public void onWordSelected(List<Word> allMatches, int selection)
    {
        TranslationsActivity.showWord(this, allMatches.get(selection));

        // TODO Show in a side fragment on large screens
    }

    private void showSearchUi(String intentText)
    {
        SearchFragment frag = new SearchFragment();

        Bundle args = new Bundle();
        args.putString(Intent.EXTRA_TEXT, intentText);
        frag.setArguments(args);

        getSupportFragmentManager()
        .beginTransaction()
        .replace(R.id.content_frame, frag)
        .commit();

        mTransSearchActive = false;
        setWindowTitle("Search words");
        mDrawerLayout.closeDrawer(mDrawer);
    }

    private void showSearchTransUi()
    {
        SearchTransFragment frag = new SearchTransFragment();

        getSupportFragmentManager()
        .beginTransaction()
        .replace(R.id.content_frame, frag)
        .commit();

        mTransSearchActive = true;
        setWindowTitle("Search translations");
        mDrawerLayout.closeDrawer(mDrawer);
    }

    private void setWindowTitle(String title)
    {
        mViewTitle = title;
        getSupportActionBar().setTitle(mViewTitle);
    }

    private void toggleVocabularyControls()
    {
        View vocControls = findViewById(R.id.rlVocControls);
        vocControls.setVisibility((vocControls.getVisibility() == View.VISIBLE) ? View.GONE : View.VISIBLE);
    }
}
