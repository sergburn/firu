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

import android.os.AsyncTask;
import android.util.Log;

import com.burnevsky.firu.model.DictionaryID;
import com.burnevsky.firu.model.IDictionary;
import com.burnevsky.firu.model.Mark;
import com.burnevsky.firu.model.MarkedTranslation;
import com.burnevsky.firu.model.Model;
import com.burnevsky.firu.model.Text;
import com.burnevsky.firu.model.Translation;
import com.burnevsky.firu.model.Vocabulary;
import com.burnevsky.firu.model.Word;

import java.util.ArrayList;
import java.util.List;

public class TranslationsFragmentModel
{
    interface IListener
    {
        void onVocabularyCheckCompleted();
        void onWordUpdated();
        void onTranslationsUpdated();
    }

    Model mModel = null;
    private IListener mListener = null;

    private Word mWord = null;
    private List<Translation> mAllTranslations = new ArrayList<>();
    private Word mLastRemovedWord = null;

    public TranslationsFragmentModel(Model model, IListener listener)
    {
        mModel = model;
        mListener = listener;
    }

    public Word getWord()
    {
        return mWord;
    }

    public List<? extends Translation> getTranslations()
    {
        return mAllTranslations.subList(0, mAllTranslations.size()); // copy of the list
    }

    class FindWord extends AsyncTask<Text, Void, Word>
    {
        DictionaryID mDictionaryID;

        public FindWord(DictionaryID dictionaryId)
        {
            mDictionaryID = dictionaryId;
        }

        @Override
        protected Word doInBackground(Text... param)
        {
            IDictionary dictionary = mModel.getDictionary(mDictionaryID);
            if (dictionary != null)
            {
                return dictionary.findWord(param[0]);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Word word)
        {
            if (word != null)
            {
                if (word.isVocabularyItem())
                {
                    mWord = word;
                    mListener.onWordUpdated();
                }
                mergeTranslations(word.getTranslations());
                mListener.onTranslationsUpdated();
            }

            if (mDictionaryID == DictionaryID.VOCABULARY)
            {
                mListener.onVocabularyCheckCompleted();
            }
        }
    }

    class LoadTranslations extends AsyncTask<Word, Void, Word>
    {
        @Override
        protected Word doInBackground(Word... param)
        {
            Word word = param[0];
            IDictionary dictionary = mModel.getDictionary(word.getDictID());
            if (dictionary != null)
            {
                dictionary.loadTranslations(word);
            }
            return word;
        }

        @Override
        protected void onPostExecute(Word result)
        {
            mergeTranslations(result.getTranslations());
            mListener.onTranslationsUpdated();
        }
    }

    class VocabularyAdd extends AsyncTask<Object, Void, Word>
    {
        void start(Text word, List<? extends Text> translations)
        {
            execute(word, translations);
        }

        @Override
        protected Word doInBackground(Object... param)
        {
            Vocabulary vocabulary = (Vocabulary) mModel.getDictionary(DictionaryID.VOCABULARY);
            if (vocabulary != null)
            {
                try
                {
                    return vocabulary.addWord((Text) param[0], (List<? extends Text>) param[1]);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Word word)
        {
            if (word != null)
            {
                mWord = word;
                mAllTranslations = mWord.getTranslations();
                mListener.onWordUpdated();
                mListener.onTranslationsUpdated();
            }
        }
    }

    class VocabularyRestore extends AsyncTask<Word, Void, Word>
    {
        @Override
        protected Word doInBackground(Word... param)
        {
            Vocabulary vocabulary = (Vocabulary) mModel.getDictionary(DictionaryID.VOCABULARY);
            if (vocabulary != null)
            {
                try
                {
                    Word word = param[0];
                    return vocabulary.addWord(word, word.getTranslations());
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Word word)
        {
            if (word != null)
            {
                mWord = word;
                mAllTranslations = mWord.getTranslations();
                mListener.onWordUpdated();
                mListener.onTranslationsUpdated();
            }
        }
    }

    class VocabularyAddTranslation extends AsyncTask<Object, Void, Word>
    {
        void start(Word word, Text translation)
        {
            execute(word, translation);
        }

        @Override
        protected Word doInBackground(Object... param)
        {
            Vocabulary vocabulary = (Vocabulary) mModel.getDictionary(DictionaryID.VOCABULARY);
            if (vocabulary != null)
            {
                try
                {
                    List<Text> list = new ArrayList<>();
                    list.add((Text) param[1]);
                    return vocabulary.addTranslations((Word) param[0], list);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Word word)
        {
            if (word != null)
            {
                mWord = word;
                mListener.onWordUpdated();
                mAllTranslations = mWord.getTranslations();
                mListener.onTranslationsUpdated();
            }
        }
    }

    class VocabularyRemove extends AsyncTask<Word, Void, Boolean>
    {
        @Override
        protected Boolean doInBackground(Word... param)
        {
            Vocabulary vocabulary = (Vocabulary) mModel.getDictionary(DictionaryID.VOCABULARY);
            if (vocabulary != null)
            {
                try
                {
                    return vocabulary.removeWord(param[0]);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean removed)
        {
            if (removed)
            {
                mWord.unlink();
                mAllTranslations = mWord.getTranslations();
                mListener.onWordUpdated();
                mListener.onTranslationsUpdated();
            }
        }
    }

    class VocabularyUpdateMarks extends AsyncTask<MarkedTranslation, Void, Boolean>
    {
        @Override
        protected Boolean doInBackground(MarkedTranslation... param)
        {
            Vocabulary vocabulary = (Vocabulary) mModel.getDictionary(DictionaryID.VOCABULARY);
            if (vocabulary != null)
            {
                try
                {
                    vocabulary.updateMarks(param[0]);
                    return true;
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean updated)
        {
            if (updated)
            {
                mListener.onTranslationsUpdated();
            }
        }
    }

    public void loadWord(Word word)
    {
        mWord = word;
        mAllTranslations.clear();

        for (IDictionary dictionary : mModel.getDictionaries())
        {
            if (mWord.getDictID() == dictionary.getDictID())
            {
                new LoadTranslations().execute(mWord);
            }
            else
            {
                new FindWord(dictionary.getDictID()).execute(mWord);
            }
        }
    }

    public void addWordToVocabulary()
    {
        if (mWord != null)
        {
            if (!mWord.isVocabularyItem())
            {
                new VocabularyAdd().start(mWord, mAllTranslations);
            }
            else
            {
                Log.d("firu", "Attempt to re-add vocabulary word");
            }
        }
    }

    public void removeWordFromVocabulary()
    {
        if (mWord != null)
        {
            if (mWord.isVocabularyItem())
            {
                mLastRemovedWord = mWord;
                new VocabularyRemove().execute(mWord);
            }
            else
            {
                Log.d("firu", "Attempt to remove non-vocabulary word");
            }
        }
    }

    public void restoreLastRemovedWord()
    {
        if (mLastRemovedWord != null)
        {
            new VocabularyRestore().execute(mLastRemovedWord);
        }
        else
        {
            Log.d("firu", "No removed word to restore");
        }
    }

    public void forgetLastRemovedWord()
    {
        mLastRemovedWord = null;
    }

    public void addTranslationToVocabulary(int position)
    {
        if (mWord != null && mWord.isVocabularyItem())
        {
            new VocabularyAddTranslation().start(mWord, mAllTranslations.get(position));
        }
    }

    private void mergeTranslations(List<Translation> translations)
    {
        for (Translation trans : translations)
        {
            int matchIndex = Translation.findMatch(trans, mAllTranslations);

            if (matchIndex >= 0)
            {
                if (trans.isVocabularyItem())
                {
                    mAllTranslations.set(matchIndex, trans);
                }
            }
            else
            {
                mAllTranslations.add(trans);
            }
        }
    }

    /**
     * Removes translation from training
     */
    public void deselectTranslation(int position)
    {
        Translation trans = mAllTranslations.get(position);
        if (trans.isVocabularyItem())
        {
            MarkedTranslation mt = (MarkedTranslation) trans;
            mt.ReverseMark = Mark.Unfamiliar;
            new VocabularyUpdateMarks().execute(mt);
        }
    }

    /** Adds translation to training */
    public void selectTranslation(int position)
    {
        selectTranslation(position, Mark.YetToLearn);
    }

    /**
     * Restores translation for training
     */
    public void selectTranslation(int position, Mark mark)
    {
        Translation trans = mAllTranslations.get(position);
        if (trans.isVocabularyItem())
        {
            MarkedTranslation mt = (MarkedTranslation) trans;
            mt.ReverseMark = mark;
            new VocabularyUpdateMarks().execute(mt);
        }
    }
}
