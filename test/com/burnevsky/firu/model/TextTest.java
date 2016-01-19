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

import android.support.v7.view.menu.ListMenuItemView;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

class TextTestBase
{
    static final String TEXT_TEXT = "text";
    static final String TEXT_LANG = "lang";
    static final int LANG_CODE = LangUtil.lang2Int(TEXT_LANG);
    static final DictionaryID DICTIONARY_ID = DictionaryID.UNIVERSAL;
    static final long WORD_ID = 234;
    static final long ENTRY_ID = 123;
    static final long TRANS_START_ID = 11;
    static final long TRANS_COUNT = 11;
    static final Mark FWD_MARK = Mark.YET_TO_LEARN;
    static final Mark REV_MARK = Mark.LEARNED;

    final Text mText = new Text(TEXT_TEXT, TEXT_LANG);
    final DictionaryEntry mEntry = new DictionaryEntry(DICTIONARY_ID, ENTRY_ID, mText);
    final Translation mTrans = new Translation(DICTIONARY_ID, ENTRY_ID, WORD_ID, mText);
    final Word mWord = new Word(DICTIONARY_ID, WORD_ID, mText);
    final List<Translation> mWordTranslations = new ArrayList<>();

    void fillWordTranslations()
    {
        for (int i = 0; i < TRANS_COUNT; i++)
        {
            MarkedTranslation translation =
                new MarkedTranslation(DictionaryID.VOCABULARY, i + TRANS_START_ID, WORD_ID, mText);
            translation.ForwardMark = FWD_MARK;
            translation.ReverseMark = REV_MARK;
            mWordTranslations.add(translation);
            mWord.addTranslation(translation);
        }
    }

    void assertTextFields(final Text text)
    {
        assertEquals(TEXT_TEXT, text.getText());
        assertEquals(TEXT_LANG, text.getLang());
    }

    void assertEntryFields(final DictionaryEntry entry)
    {
        assertEquals(DICTIONARY_ID, entry.getDictID());
        assertEquals(ENTRY_ID, entry.getID());
    }

    void assertTranslationFields(final Translation translation)
    {
        assertEquals(WORD_ID, translation.getWordID());
        assertEntryFields(translation);
        assertTextFields(translation);
    }

    void assertMarkedTranslationFields(final MarkedTranslation translation, long id)
    {
        assertEquals(FWD_MARK, translation.ForwardMark);
        assertEquals(REV_MARK, translation.ReverseMark);
        assertEquals(WORD_ID, translation.getWordID());
        assertEquals(DictionaryID.VOCABULARY, translation.getDictID());
        assertEquals(id, translation.getID());
        assertTextFields(translation);
    }

    void assertWordFields(final Word word)
    {
        assertEquals(DICTIONARY_ID, word.getDictID());
        assertEquals(WORD_ID, word.getID());
        assertTextFields(word);
    }
}

public class TextTest extends TextTestBase
{
    @Before
    public void setUp()
    {
        fillWordTranslations();
    }

    @Test
    public void testText()
    {
        assertTextFields(mText);
        assertEquals(TEXT_TEXT, mText.toString());
        assertEquals(LANG_CODE, mText.getLangCode());

        assertEquals(-1, Text.findMatch(mText, null));
        assertEquals(0, Text.findMatch(mText, mWordTranslations));

        List<Translation> list = new ArrayList<>(mWordTranslations);
        Text sample = new Text(TEXT_LANG, TEXT_LANG);
        list.add(new Translation(sample));
        assertEquals(list.size() - 1, Text.findMatch(sample, list));

        list.clear();
        assertEquals(-1, Text.findMatch(mText, list));
    }

    @Test
    public void testDictionaryEntry()
    {
        assertEntryFields(mEntry);
        assertEquals(TEXT_TEXT, mEntry.toString());
        assertEquals(LANG_CODE, mEntry.getLangCode());

        assertFalse(mEntry.isVocabularyItem());

        DictionaryEntry newEntry = new DictionaryEntry(DictionaryID.VOCABULARY, 123, mText);
        assertTrue(newEntry.isVocabularyItem());

        newEntry.unlink();
        assertEquals(DictionaryID.UNDEFINED, newEntry.getDictID());
        assertEquals(0, newEntry.getID());
        assertFalse(newEntry.isVocabularyItem());
    }

    @Test
    public void testTranslation()
    {
        assertTranslationFields(mTrans);
        assertEquals(TEXT_TEXT, mTrans.toString());
        assertEquals(LANG_CODE, mTrans.getLangCode());

        assertFalse(mTrans.isVocabularyItem());

        Translation copy = new Translation(mTrans);
        assertTranslationFields(copy);

        Translation newTrans = new Translation(DictionaryID.VOCABULARY, 123, 234, mText);
        assertTrue(newTrans.isVocabularyItem());

        newTrans.unlink();
        assertEquals(DictionaryID.UNDEFINED, newTrans.getDictID());
        assertEquals(0, newTrans.getID());
        assertEquals(0, newTrans.getWordID());
        assertFalse(newTrans.isVocabularyItem());

        assertTrue(Translation.isAllVocabularyItems(null));
        assertTrue(Translation.isAllVocabularyItems(mWordTranslations));

        List<Translation> list = new ArrayList<>(mWordTranslations);
        list.add(mTrans);
        assertFalse(Translation.isAllVocabularyItems(list));
        list.clear();
        assertTrue(Translation.isAllVocabularyItems(list));
    }

    @Test
    public void testWord()
    {
        assertWordFields(mWord);
        assertEquals(TEXT_TEXT, mWord.toString());
        assertEquals(LANG_CODE, mWord.getLangCode());

        assertEquals(false, mWord.isVocabularyItem());

        Word newWord = new Word(DictionaryID.VOCABULARY, 234, mText);
        assertTrue(newWord.isVocabularyItem());

        newWord.unlink();
        assertEquals(DictionaryID.UNDEFINED, newWord.getDictID());
        assertEquals(0, newWord.getID());
        assertFalse(newWord.isVocabularyItem());

        List<Translation> list = mWord.getTranslations();
        assertNotNull(list);
        assertEquals(TRANS_COUNT, list.size());
        for (int i = 0; i < TRANS_COUNT; i++)
        {
            assertTrue(list.get(i) instanceof MarkedTranslation);
            MarkedTranslation at = (MarkedTranslation) list.get(i);
            assertNotNull(at);
            assertMarkedTranslationFields(at, i + TRANS_START_ID);
        }

        newWord = new Word(mText);
        assertTextFields(newWord);
        assertEquals(DictionaryID.UNDEFINED, newWord.getDictID());
        assertEquals(0, newWord.getID());
        assertEquals(0, newWord.getTranslations().size());
        assertFalse(newWord.isVocabularyItem());
    }
}
