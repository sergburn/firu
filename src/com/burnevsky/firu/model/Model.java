/*******************************************************************************
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2015 Sergey Burnevsky (sergey.burnevsky at gmail.com)
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

package com.burnevsky.firu.model;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class Model
{
    private final static String TAG = "firu/Model";

    public Model(DictionaryFactory factory)
    {
        mFactory = factory;
    }

    private final DictionaryFactory mFactory;

    private Map<DictionaryID, IDictionary> mDictionaries = new TreeMap<>();

    private final Handler mHandler = new Handler(Looper.getMainLooper()); // main thread

    private final ModelListenersManager mModelListeners = new ModelListenersManager();

    public IDictionary getDictionary(DictionaryID dictionaryID)
    {
        return mDictionaries.get(dictionaryID);
    }

    /** @return List of open dictionaries, including Vocabulary */
    public Collection<IDictionary> getDictionaries()
    {
        return mDictionaries.values();
    }

    public Vocabulary getVocabulary()
    {
        return (Vocabulary) getDictionary(DictionaryID.VOCABULARY);
    }

    public enum ModelEvent
    {
        MODEL_EVENT_NONE,
        /** Sent to subscribers when dictionary is opened */
        MODEL_EVENT_OPENED,
        /** Sent to new subscribers if dictionary is already opened */
        MODEL_EVENT_READY,
        /** Sent to subscribers if dictionary can't be opened */
        MODEL_EVENT_FAILURE,
        /** Sent to subscribers when dictionary is closed */
        MODEL_EVENT_CLOSED,
    }

    public interface ModelListener
    {
        void onDictionaryEvent(DictionaryID dictionaryID, ModelEvent event);
    }

    /** Subscribes to any dictionary, including Vocabulary */
    public void subscribeDictionaryEvents(final ModelListener listener)
    {
        mModelListeners.addListener(listener);

        for (IDictionary dict : getDictionaries())
        {
            final DictionaryID dictionaryID = dict.getDictID();
            mHandler.post(new Runnable()
            {
                @Override
                public void run()
                {
                    mModelListeners.notifyListener(listener, dictionaryID, ModelEvent.MODEL_EVENT_READY);
                }
            });
        }
    }

    public void openDictionary(final DictionaryID dictionaryID)
    {
        Log.v(TAG, String.format("openDictionary %s", dictionaryID.toString()));

        class DictionaryOpener extends AsyncTask<Void, Void, IDictionary>
        {
            @Override
            protected IDictionary doInBackground(Void... voids)
            {
                try
                {
                    return doOpen(dictionaryID);
                }
                catch (Exception e)
                {
                    Log.e(TAG, "Failed to load dictionary");
                    return null;
                }
            }

            private IDictionary doOpen(final DictionaryID dictID) throws Exception
            {
                switch (dictionaryID)
                {
                    case UNIVERSAL:
                        return mFactory.newDictionary();
                    case VOCABULARY:
                        return mFactory.newVocabulary();
                    default:
                        throw new Exception(String.format("Unsupported dictionary ID (%d)", dictID.getID()));
                }
            }

            @Override
            protected void onPostExecute(IDictionary result)
            {
                mDictionaries.remove(dictionaryID);
                if (result != null)
                {
                    Log.i(TAG,
                        String.format("Dictionary %s: totalWordCount: %d", dictionaryID.toString(), result.getTotalWords()));
                    mDictionaries.put(dictionaryID, result);
                    mModelListeners.notifyAllListeners(dictionaryID, ModelEvent.MODEL_EVENT_OPENED);
                }
                else
                {
                    mModelListeners.notifyAllListeners(dictionaryID, ModelEvent.MODEL_EVENT_FAILURE);
                }
            }
        }

        if (mDictionaries.get(dictionaryID) == null)
        {
            new DictionaryOpener().execute();
        }
    }

    public void resetVocabulary()
    {
        final Vocabulary voc = (Vocabulary) mDictionaries.get(DictionaryID.VOCABULARY);

        class VocabularyCleaner extends AsyncTask<Void, Void, Void>
        {
            @Override
            protected void onPreExecute()
            {
                if (voc != null)
                {
                    Log.i("firu", "Vocabulary pre-reset");
                    mModelListeners.notifyAllListeners(DictionaryID.VOCABULARY, ModelEvent.MODEL_EVENT_CLOSED);
                }
            }

            @Override
            protected Void doInBackground(Void... params)
            {
                if (voc != null)
                {
                    voc.clearAll();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void result)
            {
                if (voc != null)
                {
                    Log.i("firu", "Vocabulary reset");
                    mModelListeners.notifyAllListeners(DictionaryID.VOCABULARY, ModelEvent.MODEL_EVENT_OPENED);
                }
            }
        }

        new VocabularyCleaner().execute();
    }

    public void closeDictionary(DictionaryID dictionaryID)
    {
        if (mDictionaries.get(dictionaryID) != null)
        {
            mModelListeners.notifyAllListeners(dictionaryID, ModelEvent.MODEL_EVENT_CLOSED);
            mDictionaries.remove(dictionaryID);
        }
    }

    private class ModelListenersManager
    {
        // alias
        private class ModelListenerRef extends WeakReference<ModelListener>
        {
            public ModelListenerRef(ModelListener r)
            {
                super(r);
            }
        }

        private List<ModelListenerRef> mListeners = new ArrayList<>();

        void addListener(final ModelListener listener)
        {
            assert listener != null;
            for (Iterator<ModelListenerRef> iter = mListeners.iterator(); iter.hasNext();)
            {
                ModelListener l = iter.next().get();
                if (l == null)
                {
                    iter.remove();
                }
                else if (l == listener)
                {
                    return;
                }
            }
            mListeners.add(new ModelListenerRef(listener));
        }

        void notifyListener(ModelListener listener, DictionaryID dbType, ModelEvent event)
        {
            listener.onDictionaryEvent(dbType, event);
        }

        void notifyAllListeners(DictionaryID dbType, ModelEvent event)
        {
            for (Iterator<ModelListenerRef> iter = mListeners.iterator(); iter.hasNext();)
            {
                ModelListener listener = iter.next().get();
                if (listener != null)
                {
                    notifyListener(listener, dbType, event);
                }
                else
                {
                    iter.remove();
                }
            }
        }
    }
}
