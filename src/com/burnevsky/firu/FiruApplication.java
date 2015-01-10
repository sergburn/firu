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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.burnevsky.firu.model.Dictionary;
import com.burnevsky.firu.model.Vocabulary;

import android.app.AlertDialog;
import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

public class FiruApplication extends Application
{
    private final static String FIRU_FOLDER = "firu";
    public final static String DICT_DB_NAME = "dictionary.sqlite";
    public final static String VOC_DB_NAME = "vocabulary.sqlite";

    final Handler mHandler = new Handler();

    private Dictionary mDict = null;
    private Vocabulary mVoc = null;
    
    private enum ModelEvent
    {
        MODEL_EVENT_NONE,
        MODEL_EVENT_DICT_OPEN,
        MODEL_EVENT_DICT_CLOSE,
        MODEL_EVENT_VOC_OPEN,
        MODEL_EVENT_VOC_CLOSE,
        MODEL_EVENT_VOC_RESET,
    }
    
    private File mDictFile = null;
    
    private File mLocalVocFile = null;
    private File mBackupVocFile = null;
    
/*    
    static {
        System.loadLibrary("sqliteX");
    }
*/
    
    public interface ModelListener 
    {
        void onDictionaryOpen(Dictionary dict);
        void onDictionaryClose(Dictionary dict);
        
        void onVocabularyOpen(Vocabulary voc);
        void onVocabularyReset(Vocabulary voc);
        void onVocabularyClose(Vocabulary voc);
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
            mListeners = new ArrayList<ModelListenerRef>();
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

        void notifyListener(ModelListener listener, ModelEvent event)
        {
            switch (event)
            {
                case MODEL_EVENT_DICT_OPEN:
                    listener.onDictionaryOpen(mDict);
                    break;
                case MODEL_EVENT_DICT_CLOSE:
                    listener.onDictionaryClose(mDict);
                    break;
                case MODEL_EVENT_VOC_OPEN:
                    listener.onVocabularyOpen(mVoc);
                    break;
                case MODEL_EVENT_VOC_RESET:
                    listener.onVocabularyReset(mVoc);
                    break;
                case MODEL_EVENT_VOC_CLOSE:
                    listener.onVocabularyClose(mVoc);
                    break;
                default:
                    if (BuildConfig.DEBUG)
                    {
                        throw new InvalidParameterException();
                    }
                    break;
            }
        }
        
        void notifyAllListeners(ModelEvent event)
        {
            for (Iterator<ModelListenerRef> iter = mListeners.iterator(); iter.hasNext();)
            {
                ModelListener listener = iter.next().get();
                if (listener != null)
                {
                    notifyListener(listener, event);
                }
                else
                {
                    iter.remove();
                }
            }
        }
    }
    
    private ModelListenersManager mModelListeners = new ModelListenersManager();
    
    boolean checkPath(File p, final String header)
    {
        boolean exists = p.exists(); 
        Log.i("firu", "Path to " + header + ": '" + p.getAbsolutePath() + (exists ? "' exists" : "' invalid"));
        return exists;
    }
    
    public void onCreate()
    {
        super.onCreate();
        
        File epub = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        checkPath(epub, "Downloads");
        
        File firu = new File(epub.getAbsolutePath() + File.separator + FIRU_FOLDER);
        checkPath(firu, "Downloads/Firu");
        
        mDictFile = new File(firu.getAbsolutePath() + File.separator + DICT_DB_NAME);
        checkPath(mDictFile, "Dictionary");
        
        mBackupVocFile = new File(firu.getAbsolutePath() + File.separator + VOC_DB_NAME);
        checkPath(mBackupVocFile, "Backup Vocabulary");

        mLocalVocFile = getDatabasePath(VOC_DB_NAME);
        checkPath(mLocalVocFile, "Local Vocabulary");
    }

    private boolean copyFile(File src, File dst) throws IOException
    {
        Log.d("firu", "Copying from '" + src.toString() + "' to '" + dst.toString());
        if (!src.getAbsolutePath().toString().equals(dst.getAbsolutePath().toString()))
        {
            InputStream is = new FileInputStream(src);
            OutputStream os = new FileOutputStream(dst);
            byte[] buff = new byte[256*1024];
            int len;
            while ((len = is.read(buff)) > 0)
            {
                os.write(buff, 0, len);
            }
            is.close();
            os.close();
        }
        return true;
    }
    
    public void subscribeDictionary(Context toastContext, final ModelListener listener)
    {
        mModelListeners.addListener(listener);
        
        if (mDict == null)
        {
            new DictionaryOpener(toastContext).execute();
        }
        else
        {
            mHandler.post(new Runnable()
            {
                @Override
                public void run()
                {
                    mModelListeners.notifyListener(listener, ModelEvent.MODEL_EVENT_DICT_OPEN);
                }
            }); 
        }
    }
    
    private class DictionaryOpener extends AsyncTask<Void, Void, Dictionary>
    {
        Context mToastContext = null;
        
        DictionaryOpener(Context toastContext)
        {
            mToastContext = toastContext;
        }
        
        @Override
        protected Dictionary doInBackground(Void... voids)
        {
            return new Dictionary(mDictFile.getAbsolutePath(), getApplicationContext());
        }

        @Override
        protected void onPostExecute(Dictionary result)
        {
            if (result != null)
            {
                mDict = result;
                Log.i("firu", "Dictionary: totalWordCount: " + String.valueOf(mDict.getTotalWords()));
                mModelListeners.notifyAllListeners(ModelEvent.MODEL_EVENT_DICT_OPEN);
            }
            else if (mToastContext != null)
            {
                if (!mDictFile.exists())
                {
                    Toast.makeText(mToastContext, "Dictionary not found", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Toast.makeText(mToastContext, "Can't open Dictionary", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
    
    private void closeDictionary()
    {
        if (mDict != null)
        {
            mModelListeners.notifyAllListeners(ModelEvent.MODEL_EVENT_DICT_CLOSE);
            mDict = null;
        }
    }

    public void subscribeVocabulary(Context toastContext, final ModelListener listener)
    {
        mModelListeners.addListener(listener);
        
        if (mVoc == null)
        {
            new VocabularyOpener(toastContext).execute();
        }
        else
        {
            mHandler.post(new Runnable()
            {
                @Override
                public void run()
                {
                    mModelListeners.notifyListener(listener, ModelEvent.MODEL_EVENT_VOC_OPEN);
                }
            }); 
        }
    }
    
    class VocabularyOpener extends AsyncTask<Void, Void, Vocabulary>
    {
        Context mToastContext = null;

        public VocabularyOpener(Context toastContext)
        {
            mToastContext = toastContext;
        }

        @Override
        protected Vocabulary doInBackground(Void... params)
        {
            return new Vocabulary(mLocalVocFile.getAbsolutePath(), getApplicationContext());
        }

        @Override
        protected void onPostExecute(Vocabulary result)
        {
            if (result != null)
            {
                mVoc = result;
                Log.i("firu", "Vocabulary: totalWordCount: " + String.valueOf(mVoc.getTotalWords()));
                mModelListeners.notifyAllListeners(ModelEvent.MODEL_EVENT_VOC_OPEN);
            }
            else
            {
                if (mToastContext != null)
                {
                    Toast.makeText(mToastContext, "Can't open Vocabulary", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
    
    private void closeVocabulary()
    {
        if (mVoc != null)
        {
            mModelListeners.notifyAllListeners(ModelEvent.MODEL_EVENT_VOC_CLOSE);
            mVoc = null;
        }
    }

    public void importVocabulary(final Context toastContext, final OnOpenListener listener)
    {
        final Runnable importer = new Runnable()
        {
            @Override
            public void run()
            {
                mVoc.close();
                mVoc = null;
                try
                {
                    copyFile(mBackupVocFile, mLocalVocFile);
                    Toast.makeText(toastContext, "Copying succeded", Toast.LENGTH_SHORT).show();
                }
                catch(IOException e)
                {
                    e.printStackTrace();
                    Toast.makeText(toastContext, "Copying failed", Toast.LENGTH_SHORT).show();
                }
                finally
                {
                    openVocabulary(toastContext, listener);
                }
            }
        };
        
        if (mBackupVocFile.exists())
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(toastContext);
            builder
                .setTitle("Vocabulary replacement")
                .setMessage("Vocabulary found on sdCard\nDo you want to import it?")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {                            
                        Toast.makeText(toastContext, "Copying new vocabulary", Toast.LENGTH_SHORT).show();
                        mHandler.post(importer);
                    }
                } )
                .setNegativeButton("No", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        Toast.makeText(toastContext, "Using local vocabulary", Toast.LENGTH_SHORT).show();
                    }
                } )
                .show();
        }
        else
        {
            Toast.makeText(toastContext, "Vocabulary file not found", Toast.LENGTH_SHORT).show();
        }
    }
    
    public void exportVocabulary(final Context context)
    {
        final Runnable exporter = new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    copyFile(mLocalVocFile, mBackupVocFile);
                    Toast.makeText(context, "Backup succeded", Toast.LENGTH_SHORT).show();
                }
                catch(IOException e)
                {
                    e.printStackTrace();
                    Toast.makeText(context, "Backup failed", Toast.LENGTH_SHORT).show();
                }
            }
        };
        
        if (mBackupVocFile.exists())
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder
                .setTitle("Vocabulary backup")
                .setMessage("Backup found on sdCard\nDo you want to overwrite it?")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int which)
                    {
                        mHandler.post(exporter);
                    }
                } )
                .setNegativeButton("No", null)
                .show();
        }
        else
        {
            mHandler.post(exporter);
        }
    }
}
