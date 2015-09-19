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
import com.burnevsky.firu.model.Model;
import com.burnevsky.firu.model.Vocabulary;
import com.burnevsky.firu.model.Model.ModelEvent;
import com.burnevsky.firu.model.Model.ModelListener;

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

    Model mModel;

    private File mDictFile = null;

    private File mLocalVocFile = null;
    private File mBackupVocFile = null;

    /*
    static {
        System.loadLibrary("sqliteX");
    }
     */

    boolean checkPath(File p, final String header)
    {
        boolean exists = p.exists();
        Log.i("firu", "Path to " + header + ": '" + p.getAbsolutePath() + (exists ? "' exists" : "' invalid"));
        return exists;
    }

    @Override
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

        mModel = new Model();
        openDictionary();
        openVocabulary();
    }

    private void openDictionary()
    {
        mModel.openDictionary(mDictFile.getAbsolutePath(), getApplicationContext());
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

    public void importVocabulary(final Context toastContext)
    {
        final Runnable importer = new Runnable()
        {
            @Override
            public void run()
            {
                mModel.closeVocabulary();
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
                    openVocabulary();
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
                    mModel.closeVocabulary();
                    copyFile(mLocalVocFile, mBackupVocFile);
                    Toast.makeText(context, "Backup succeded", Toast.LENGTH_SHORT).show();
                }
                catch(IOException e)
                {
                    e.printStackTrace();
                    Toast.makeText(context, "Backup failed", Toast.LENGTH_SHORT).show();
                }
                finally
                {
                    openVocabulary();
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
                @Override
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

    public void resetVocabulary(final Context context)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder
        .setTitle("Vocabulary reset")
        .setMessage("All learning history will be lost, are you sure?")
        .setIcon(android.R.drawable.ic_dialog_alert)
        .setPositiveButton("Yes", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                mModel.resetVocabulary();
            }
        } )
        .setNegativeButton("No", null)
        .show();
    }

    private void openVocabulary()
    {
        mModel.openVocabulary(mLocalVocFile.getAbsolutePath(), getApplicationContext());
    }
}
