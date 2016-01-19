/*******************************************************************************
 * The MIT License (MIT)
 *
 * Copyright (c) 2016 Sergey Burnevsky (sergey.burnevsky at gmail.com)
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

import android.content.Context;

// This class is not final to make it possible to mock
public class DictionaryFactory
{
    public DictionaryFactory(
        Context applicationContext,
        String dictionaryPath,
        String vocabularyPath)
    {
        mApplicationContext = applicationContext;
        mDictionaryPath= dictionaryPath;
        mVocabularyPath = vocabularyPath;
    }

    private String mDictionaryPath;
    private String mVocabularyPath;
    private Context mApplicationContext;

    // Method is protected to limit access to model package
    // and let it to be mock-able on Android
    // (package-private method mocking does not work)
    protected Dictionary newDictionary()
    {
        return new Dictionary(mDictionaryPath, mApplicationContext);
    }

    protected Vocabulary newVocabulary()
    {
        return new Vocabulary(mVocabularyPath, mApplicationContext);
    }
}
