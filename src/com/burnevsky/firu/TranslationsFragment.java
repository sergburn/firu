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

import android.annotation.TargetApi;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.burnevsky.firu.model.Mark;
import com.burnevsky.firu.model.MarkedTranslation;
import com.burnevsky.firu.model.Translation;
import com.burnevsky.firu.model.Word;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

public class TranslationsFragment extends FiruFragmentBase implements TranslationsFragmentModel.IListener
{
    TranslationsFragmentModel mViewModel = null;

    List<SortedMap<String, Object>> mListData = new ArrayList<>();
    SimpleAdapter mAdapter;

    TextView mWordView;
    ListView mTransView;
    ImageView mStarBtn;
    private Drawable mStarredIcon, mUnstarredIcon;

    final String[] mListColumns = {"trans", "rate"};
    final int[] mListFields = {R.id.tf_textTrans, R.id.tf_rbMark};

    @SuppressWarnings("deprecation")
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void loadIcons()
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            mStarredIcon = getActivity().getDrawable(R.drawable.ic_action_important_dark);
            mUnstarredIcon = getActivity().getDrawable(R.drawable.ic_action_not_important_dark);
        }
        else
        {
            mStarredIcon = getResources().getDrawable(R.drawable.ic_action_important_dark);
            mUnstarredIcon = getResources().getDrawable(R.drawable.ic_action_not_important_dark);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        mViewModel = new TranslationsFragmentModel(mModel, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        super.onCreateView(inflater, container, savedInstanceState);

        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_translations, container, false);

        mWordView = (TextView) rootView.findViewById(R.id.laWord);
        mStarBtn = (ImageView) rootView.findViewById(R.id.btnStar);
        mTransView = (ListView) rootView.findViewById(R.id.listTranslations);

        loadIcons();

        mTransView.setItemsCanFocus(true);
        mTransView.setFocusable(false);
        mTransView.setFocusableInTouchMode(false);
        mTransView.setClickable(false);

        mStarBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                mViewModel.triggerWordInVocabulary();
            }
        });
        mStarBtn.setVisibility(View.INVISIBLE); // until vocabulary match checked

        mAdapter = new SimpleAdapter(getContext(), mListData, R.layout.marked_translation_list_item, mListColumns, mListFields)
        {
            @Override
            public View getView(final int position, View convertView, ViewGroup parent)
            {
                View view = super.getView(position, convertView, parent);
                if (view != null)
                {
                    TextView frame = (TextView) view.findViewById(R.id.tf_cover);
                    if (frame != null)
                    {
                        frame.setOnClickListener(new View.OnClickListener()
                        {
                            @Override
                            public void onClick(View v)
                            {
                                onTranslationRatingClicked(position);
                            }
                        });
                    }
                }
                return view;
            }
        };

        mAdapter.setViewBinder(new SimpleAdapter.ViewBinder()
        {
            @Override
            public boolean setViewValue(View view, Object data, String textRepresentation)
            {
                if (view.getId() == R.id.tf_rbMark)
                {
                    Integer rate = (Integer) data;
                    RatingBar rbMark = (RatingBar) view;
                    if (rate > 0)
                    {
                        rbMark.setRating(rate - 1);
                        rbMark.setVisibility(View.VISIBLE);
                    }
                    else // unfamiliar
                    {
                        rbMark.setVisibility(View.INVISIBLE);
                    }
                    return true;
                }
                else
                {
                    return false;
                }
            }
        });

        mTransView.setAdapter(mAdapter);

        Bundle args = getArguments();
        if (args != null)
        {
            Word word = args.getParcelable("word");

            if (word != null)
            {
                mWordView.setText(word.getText()); // show it immediately, it won't change
                mViewModel.loadWord(word);
            }
        }
        else
        {
            return null;
        }

        return rootView;
    }

    @Override
    public void onWordUpdated()
    {
        if (mViewModel.getWord() != null)
        {
            mStarBtn.setImageDrawable(mViewModel.getWord().isVocabularyItem() ? mStarredIcon : mUnstarredIcon);
            mStarBtn.setVisibility(View.VISIBLE);
        }
        else
        {
            mStarBtn.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onTranslationsUpdated()
    {
        mListData.clear();

        for (Translation trans : mViewModel.getTranslations())
        {
            TreeMap<String, Object> row = new TreeMap<>();
            row.put("trans", trans.getText());
            if (trans.isVocabularyItem())
            {
                row.put("rate", ((MarkedTranslation) trans).ReverseMark.toInt());
            }
            else
            {
                row.put("rate", Mark.Unfamiliar.toInt());
            }
            mListData.add(row);
        }

        mAdapter.notifyDataSetChanged();
    }

    private void onTranslationRatingClicked(final int position)
    {
        Translation translation = mViewModel.getTranslations().get(position);
        if (translation.isVocabularyItem())
        {
            final MarkedTranslation copy = new MarkedTranslation((MarkedTranslation) translation);

            if (copy.ReverseMark.toInt() == Mark.Unfamiliar.toInt())
            {
                mViewModel.selectTranslation(position);
            }
            else
            {
                mViewModel.deselectTranslation(position);

                Snackbar.make(mTransView, "Translation removed from training", Snackbar.LENGTH_INDEFINITE)
                    .setAction("Undo", new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {
                            mViewModel.selectTranslation(position, copy.ReverseMark);
                        }
                    })
                    .show();
            }
        }
        else
        {
            mViewModel.addTranslationToVocabulary(position);
        }
    }

}
