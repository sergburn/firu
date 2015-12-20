
package com.burnevsky.firu.model;

import java.lang.ref.WeakReference;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import com.burnevsky.firu.BuildConfig;
import com.burnevsky.firu.model.Model.ModelEvent;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

public class Model
{
    private Map<DictionaryID, IDictionary> mDictionaries = new TreeMap<>() ;

    private final Handler mHandler = new Handler();

    private final ModelListenersManager mModelListeners = new ModelListenersManager();

    public IDictionary getDictionary(DictionaryID dictionaryID)
    {
        return mDictionaries.get(dictionaryID);
    }

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

    public void subscribeDictionary(final DictionaryID dictionaryID, final ModelListener listener)
    {
        mModelListeners.addListener(listener);

        if (mDictionaries.get(dictionaryID) != null)
        {
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

    public void openDictionary(final String dbPath, final Context appContext)
    {
        class DictionaryOpener extends AsyncTask<Void, Void, Dictionary>
        {
            @Override
            protected Dictionary doInBackground(Void... voids)
            {
                return new Dictionary(dbPath, appContext);
            }

            @Override
            protected void onPostExecute(Dictionary result)
            {
                mDictionaries.put(DictionaryID.UNIVERSAL, result);
                if (result != null)
                {
                    Log.i("firu", "Dictionary: totalWordCount: " + String.valueOf(result.getTotalWords()));
                    mModelListeners.notifyAllListeners(DictionaryID.UNIVERSAL, ModelEvent.MODEL_EVENT_OPENED);
                }
                else
                {
                    mModelListeners.notifyAllListeners(DictionaryID.UNIVERSAL, ModelEvent.MODEL_EVENT_FAILURE);
                }
            }
        }

        new DictionaryOpener().execute();
    }

    public void openVocabulary(final String dbPath, final Context appContext)
    {
        class VocabularyOpener extends AsyncTask<Void, Void, Vocabulary>
        {
            @Override
            protected Vocabulary doInBackground(Void... voids)
            {
                return new Vocabulary(dbPath, appContext);
            }

            @Override
            protected void onPostExecute(Vocabulary result)
            {
                mDictionaries.put(DictionaryID.VOCABULARY, result);
                if (result != null)
                {
                    Log.i("firu", "Vocabulary: totalWordCount: " + String.valueOf(result.getTotalWords()));
                    mModelListeners.notifyAllListeners(DictionaryID.VOCABULARY, ModelEvent.MODEL_EVENT_OPENED);
                }
                else
                {
                    mModelListeners.notifyAllListeners(DictionaryID.VOCABULARY, ModelEvent.MODEL_EVENT_FAILURE);
                }
            }
        }

        new VocabularyOpener().execute();
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
