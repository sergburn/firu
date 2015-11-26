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

package com.burnevsky.firu.model;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 *
 */
public abstract class DictionaryBase implements IDictionary
{
    protected SQLiteDatabase mDatabase = null;
    protected int mTotalWords = 0;
    protected int mTotalTranslations = 0;
    protected DictionaryID mDictID = DictionaryID.UNDEFINED;

    public DictionaryBase(DictionaryID dictID)
    {
        mDictID = dictID;
    }

    @Override
    public DictionaryID getDictID()
    {
        return mDictID;
    }

    @Override
    public int getTotalWords()
    {
        return mTotalWords;
    }

    @Override
    public int getTotalTranslations()
    {
        return mTotalTranslations;
    }

    @Override
    public int countWords(String startsWith)
    {
        if (mDatabase == null) return 0;

        Cursor c = mDatabase.query("words",
            new String[] { "count(*)" },
            "text LIKE '" + startsWith + "%'", // most probably should use collated index
            null, null, null, null, null);

        int count = 0;
        if (!c.isAfterLast() && c.moveToFirst())
        {
            count = c.getInt(0);
        }
        c.close();
        return count;
    }

    @Override
    public int countWords()
    {
        if (mDatabase == null) return 0;

        Cursor c = mDatabase.query("words",
            new String[] { "count(*)" },
            null, null, null, null, null, null);

        int count = 0;
        if (!c.isAfterLast() && c.moveToFirst())
        {
            count = c.getInt(0);
        }
        c.close();
        return count;
    }

    @Override
    public int countTranslations()
    {
        if (mDatabase == null) return 0;

        Cursor c = mDatabase.query("translations",
            new String[] { "count(*)" },
            null, null, null, null, null, null);

        int count = 0;
        if (!c.isAfterLast() && c.moveToFirst())
        {
            count = c.getInt(0);
        }
        c.close();
        return count;
    }
}