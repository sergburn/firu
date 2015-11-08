
package com.burnevsky.firu.model;

import java.lang.ref.WeakReference;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.burnevsky.firu.BuildConfig;
import com.burnevsky.firu.model.Model.ModelEvent;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

public class Model
{
    private Dictionary mDict = null;
    private Vocabulary mVoc = null;

    private final Handler mHandler = new Handler();

    private final ModelListenersManager mModelListeners = new ModelListenersManager();


    public Dictionary getDictionary()
    {
        return mDict;
    }

    public Vocabulary getVocabulary()
    {
        return mVoc;
    }

    public enum ModelEvent
    {
        MODEL_EVENT_OPENED,
        MODEL_EVENT_READY,
        MODEL_EVENT_FAILURE,
        MODEL_EVENT_CLOSED,
    }

    public interface ModelListener
    {
        void onDictionaryEvent(Dictionary dict, ModelEvent event);
        void onVocabularyEvent(Vocabulary voc, ModelEvent event);
    }

    public void subscribeDictionary(final ModelListener listener)
    {
        mModelListeners.addListener(listener);

        if (mDict != null)
        {
            mHandler.post(new Runnable()
            {
                @Override
                public void run()
                {
                    mModelListeners.notifyListener(listener, DbType.MODEL_DB_DICT, ModelEvent.MODEL_EVENT_READY);
                }
            });
        }
    }

    public void subscribeVocabulary(final ModelListener listener)
    {
        mModelListeners.addListener(listener);

        if (mVoc != null)
        {
            mHandler.post(new Runnable()
            {
                @Override
                public void run()
                {
                    mModelListeners.notifyListener(listener, DbType.MODEL_DB_VOC, ModelEvent.MODEL_EVENT_READY);
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
                mDict = result;
                if (result != null)
                {
                    Log.i("firu", "Dictionary: totalWordCount: " + String.valueOf(mDict.getTotalWords()));
                    mModelListeners.notifyAllListeners(DbType.MODEL_DB_DICT, ModelEvent.MODEL_EVENT_OPENED);
                }
                else
                {
                    mModelListeners.notifyAllListeners(DbType.MODEL_DB_DICT, ModelEvent.MODEL_EVENT_FAILURE);
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
                mVoc = result;
                if (result != null)
                {
                    Log.i("firu", "Vocabulary: totalWordCount: " + String.valueOf(mVoc.getTotalWords()));
                    mModelListeners.notifyAllListeners(DbType.MODEL_DB_VOC, ModelEvent.MODEL_EVENT_OPENED);
                }
                else
                {
                    mModelListeners.notifyAllListeners(DbType.MODEL_DB_VOC, ModelEvent.MODEL_EVENT_FAILURE);
                }
            }
        }

        new VocabularyOpener().execute();
    }

    public void resetVocabulary()
    {
        class VocabularyCleaner extends AsyncTask<Void, Void, Void>
        {
            @Override
            protected Void doInBackground(Void... params)
            {
                if (mVoc != null)
                {
                    mVoc.clearAll();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void result)
            {
                if (mVoc != null)
                {
                    Log.i("firu", "Vocabulary reset");
                    mModelListeners.notifyAllListeners(DbType.MODEL_DB_VOC, ModelEvent.MODEL_EVENT_READY);
                }
            }
        }

        new VocabularyCleaner().execute();
    }

    private void closeDictionary()
    {
        if (mDict != null)
        {
            mModelListeners.notifyAllListeners(DbType.MODEL_DB_DICT, ModelEvent.MODEL_EVENT_CLOSED);
            mDict = null;
        }
    }

    public void closeVocabulary()
    {
        if (mVoc != null)
        {
            mModelListeners.notifyAllListeners(DbType.MODEL_DB_VOC, ModelEvent.MODEL_EVENT_CLOSED);
            mVoc = null;
        }
    }

    private enum DbType
    {
        MODEL_DB_NONE,
        MODEL_DB_DICT,
        MODEL_DB_VOC,
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

        private List<ModelListenerRef> mListeners = null;

        ModelListenersManager()
        {
            mListeners = new ArrayList<>();
        }

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

        void notifyListener(ModelListener listener, DbType dbType, ModelEvent event)
        {
            switch (dbType)
            {
                case MODEL_DB_DICT:
                    listener.onDictionaryEvent(mDict, event);
                    break;
                case MODEL_DB_VOC:
                    listener.onVocabularyEvent(mVoc, event);
                    break;
                default:
                    if (BuildConfig.DEBUG)
                    {
                        throw new InvalidParameterException();
                    }
                    break;
            }
        }

        void notifyAllListeners(DbType dbType, ModelEvent event)
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
