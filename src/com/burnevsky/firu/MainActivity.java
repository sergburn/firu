
package com.burnevsky.firu;

import java.util.ArrayList;
import java.util.List;

import com.burnevsky.firu.model.Word;

import android.app.Fragment;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v4.view.ScrollingView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;

public class MainActivity
extends FiruActivityBase
implements SearchFragment.OnTranslationSelectedListener, SearchTransFragment.OnWordSelectedListener
{
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private ScrollView mDrawer;
    MenuItem mExportVocMenu, mClearVocMenu = null;
    public String mViewTitle;

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
                super.onDrawerOpened(drawerView);
                mViewTitle = getSupportActionBar().getTitle().toString();
                getSupportActionBar().setTitle("Firu");
            }
        };

        // Set the drawer toggle as the DrawerListener
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //getSupportActionBar().setHomeButtonEnabled(true);

        View btnSearch = findViewById(R.id.textView1);
        btnSearch.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                showSearchUi();
            }
        });

        View btnRevSearch = findViewById(R.id.textView2);
        btnRevSearch.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                showSearchTransUi();
            }
        });

        showSearchUi();
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
            case R.id.action_settings:
                Intent intent = new Intent(this, TrainerActivity.class);
                startActivity(intent);
                return true;

            case R.id.action_import_voc:
                mApp.importVocabulary(this);
                return true;

            case R.id.action_backup_voc:
                mApp.exportVocabulary(this);
                return true;

            case R.id.action_reset_voc:
                mApp.resetVocabulary(this);
                return true;

            case R.id.action_show_stats:
                Intent intent2 = new Intent(this, StatActivity.class);
                startActivity(intent2);
                return true;

            case R.id.action_rev_search:
                Intent intent3 = new Intent(this, SearchTransFragment.class);
                startActivity(intent3);
                return true;

            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.search_activity_actions, menu);
        mExportVocMenu = menu.findItem(R.id.action_backup_voc);
        mClearVocMenu = menu.findItem(R.id.action_reset_voc);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu)
    {
        mExportVocMenu.setEnabled(mModel.getVocabulary() != null);
        mClearVocMenu.setEnabled(mModel.getVocabulary() != null && mModel.getVocabulary().getTotalWords() > 0);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onTranslationSelected(ArrayList<Word> allMatches, int selection)
    {
        TranslationsActivity.showDictWords(this, allMatches, selection);

        // TODO Show in a side fragment on large screens
    }

    @Override
    public void onWordSelected(List<Word> allMatches, int selection)
    {
        TranslationsActivity.showDictWord(this, allMatches.get(selection));

        // TODO Show in a side fragment on large screens
    }

    private void showSearchUi()
    {
        SearchFragment frag = new SearchFragment(getApplicationContext(), MainActivity.this);

        getFragmentManager()
        .beginTransaction()
        .replace(R.id.content_frame, frag)
        .commit();

        setWindowTitle("Search words");
        mDrawerLayout.closeDrawer(mDrawer);
    }

    private void showSearchTransUi()
    {
        SearchTransFragment frag = new SearchTransFragment(getApplicationContext(), MainActivity.this);

        getFragmentManager()
        .beginTransaction()
        .replace(R.id.content_frame, frag)
        .commit();

        setWindowTitle("Search translations");
        mDrawerLayout.closeDrawer(mDrawer);
    }

    private void setWindowTitle(String title)
    {
        mViewTitle = title;
        getSupportActionBar().setTitle(mViewTitle);
    }
}
