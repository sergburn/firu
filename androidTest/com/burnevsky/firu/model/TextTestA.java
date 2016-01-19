/*******************************************************************************
 * The MIT License (MIT)
 *
 * Copyright (c) 2015-2016 Sergey Burnevsky (sergey.burnevsky at gmail.com)
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

import android.os.Parcel;
import android.os.Parcelable;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.SmallTest;

import com.burnevsky.firu.ExamResultActivity;
import com.burnevsky.firu.model.DictionaryEntry;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
@SmallTest
public class TextTestA extends TextTestBase
{
    @Before
    public void setUp()
    {
        fillWordTranslations();
    }

    @Test
    public void parcelText()
    {
        assertEquals(0, mText.describeContents());

        Parcel parcel = Parcel.obtain();
        mText.writeToParcel(parcel, 0);

        parcel.setDataPosition(0);

        Text text = Text.CREATOR.createFromParcel(parcel);
        assertTextFields(text);
    }

    @Test
    public void parcelDictionaryEntry()
    {
        Parcel parcel = Parcel.obtain();
        mEntry.writeToParcel(parcel, 0);

        parcel.setDataPosition(0);

        DictionaryEntry entry = DictionaryEntry.CREATOR.createFromParcel(parcel);
        assertEntryFields(entry);
    }

    @Test
    public void parcelTranslation()
    {
        Parcel parcel = Parcel.obtain();
        mTrans.writeToParcel(parcel, 0);

        parcel.setDataPosition(0);

        Translation trans = Translation.CREATOR.createFromParcel(parcel);
        assertTranslationFields(trans);
    }

    @Test
    public void parcelMarkedTranslation()
    {
        Parcel parcel = Parcel.obtain();
        mWordTranslations.get(1).writeToParcel(parcel, 0);

        parcel.setDataPosition(0);

        MarkedTranslation trans = MarkedTranslation.CREATOR.createFromParcel(parcel);
        assertMarkedTranslationFields(trans, 1 + TRANS_START_ID);
    }

    @Test
    public void parcelWord()
    {
        assertEquals(0, mText.describeContents());

        Parcel parcel = Parcel.obtain();
        mWord.writeToParcel(parcel, 0);

        final int arraySize = 5;
        Word[] words = new Word[arraySize];
        for (int i = 0; i < arraySize; i++)
        {
            words[i] = new Word(mWord);
        }

        parcel.writeTypedArray(words, 0);

        parcel.setDataPosition(0);

        Word word = Word.CREATOR.createFromParcel(parcel);
        assertWordFields(word);

        words = parcel.createTypedArray(Word.CREATOR);

        for (int i = 0; i < arraySize; i++)
        {
            Word w = words[i];
            assertWordFields(w);

            List<Translation> translations = w.getTranslations();
            for (int j = 0; j < translations.size(); i++)
            {
                assertTrue(translations.get(i) instanceof MarkedTranslation);
                assertMarkedTranslationFields(
                    (MarkedTranslation) translations.get(i), i + TRANS_START_ID);
            }
        }
    }

    <T> void testCreatorArray(Parcelable.Creator<T> creator)
    {
        T[] array = creator.newArray(3);
        assertNotNull(array);
        assertEquals(3, array.length);
    }

    @Test
    public void testCreators()
    {
        testCreatorArray(Text.CREATOR);
        testCreatorArray(DictionaryEntry.CREATOR);
        testCreatorArray(Translation.CREATOR);
        testCreatorArray(MarkedTranslation.CREATOR);
        testCreatorArray(Word.CREATOR);
    }
}
