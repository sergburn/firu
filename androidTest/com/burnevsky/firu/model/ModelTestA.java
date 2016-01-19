package com.burnevsky.firu.model;

import android.os.AsyncTask;
import android.os.Looper;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(AndroidJUnit4.class)
public class ModelTestA
{
    Model mTestModel;
    DictionaryFactory mFactoryMock;
    Dictionary mDictionaryMock;
    Vocabulary mVocabularyMock;

    @Before
    public void setUp()
    {
        if (Looper.myLooper() == null)
        {
            Looper.prepare();
        }

        mFactoryMock = mock(DictionaryFactory.class);
        mDictionaryMock = mock(Dictionary.class);
        mVocabularyMock = mock(Vocabulary.class);
        mTestModel = new Model(mFactoryMock);
    }

    class TestModelListener implements Model.ModelListener
    {
        private CountDownLatch mLatch = new CountDownLatch(1);

        DictionaryID mLastDictionaryID = DictionaryID.UNDEFINED;
        Model.ModelEvent mLastModelEvent = Model.ModelEvent.MODEL_EVENT_NONE;
        int mEventCount = 0;
        int mPrevEventCount = 0;

        @Override
        public void onDictionaryEvent(DictionaryID dictionaryID, Model.ModelEvent event)
        {
            mLastDictionaryID = dictionaryID;
            mLastModelEvent = event;
            mEventCount++;
            mLatch.countDown();
        }

        void prepareWait()
        {
            mLatch = new CountDownLatch(1);
            mLastDictionaryID = DictionaryID.UNDEFINED;
            mLastModelEvent = Model.ModelEvent.MODEL_EVENT_NONE;
            mPrevEventCount= mEventCount;
        }

        boolean waitEvent(int seconds) throws InterruptedException
        {
            return mLatch.await(seconds, TimeUnit.SECONDS);
        }

        void assertNoEvent() throws InterruptedException
        {
            assertFalse(waitEvent(1));
            assertEquals(mPrevEventCount, mEventCount);
        }

        void assertNewEvent(Model.ModelEvent event) throws InterruptedException
        {
            assertTrue(waitEvent(3));
            assertEquals(mPrevEventCount + 1, mEventCount);
            assertEquals(event, mLastModelEvent);
        }
    }

    TestModelListener mModelListener = new TestModelListener();

    @Test
    public void testOpenDictionary() throws Throwable
    {
        assertEquals(0, mTestModel.getDictionaries().size());
        assertNull(mTestModel.getVocabulary());
        for (DictionaryID dictId : DictionaryID.values())
        {
            assertNull(mTestModel.getDictionary(dictId));
        }

        mModelListener.prepareWait();
        mTestModel.subscribeDictionaryEvents(mModelListener);
        mModelListener.assertNoEvent();

        when(mFactoryMock.newDictionary()).thenReturn(null);
        mModelListener.prepareWait();
        mTestModel.openDictionary(DictionaryID.UNIVERSAL);
        mModelListener.assertNewEvent(Model.ModelEvent.MODEL_EVENT_FAILURE);
        assertEquals(DictionaryID.UNIVERSAL, mModelListener.mLastDictionaryID);

        assertEquals(0, mTestModel.getDictionaries().size());
        assertNull(mTestModel.getVocabulary());
        for (DictionaryID dictId : DictionaryID.values())
        {
            assertNull(mTestModel.getDictionary(dictId));
        }

        // Open dictionary

        when(mFactoryMock.newDictionary()).thenReturn(mDictionaryMock);
        mModelListener.prepareWait();
        mTestModel.openDictionary(DictionaryID.UNIVERSAL);
        mModelListener.assertNewEvent(Model.ModelEvent.MODEL_EVENT_OPENED);
        assertEquals(DictionaryID.UNIVERSAL, mModelListener.mLastDictionaryID);

        assertEquals(1, mTestModel.getDictionaries().size());
        assertNull(mTestModel.getVocabulary());
        for (DictionaryID dictId : DictionaryID.values())
        {
            if (dictId == DictionaryID.UNIVERSAL)
            {
                assertEquals(mDictionaryMock, mTestModel.getDictionary(dictId));
            }
            else
            {
                assertNull(mTestModel.getDictionary(dictId));
            }
        }

        TestModelListener listener2 = new TestModelListener();
        mModelListener.prepareWait();
        mTestModel.subscribeDictionaryEvents(listener2);
        mModelListener.assertNoEvent();
        listener2.assertNewEvent(Model.ModelEvent.MODEL_EVENT_READY);

        // Open vocabulary

        when(mFactoryMock.newVocabulary()).thenReturn(mVocabularyMock);
        mModelListener.prepareWait();
        listener2.prepareWait();
        mTestModel.openDictionary(DictionaryID.VOCABULARY);
        mModelListener.assertNewEvent(Model.ModelEvent.MODEL_EVENT_OPENED);
        assertEquals(DictionaryID.VOCABULARY, mModelListener.mLastDictionaryID);
        listener2.assertNewEvent(Model.ModelEvent.MODEL_EVENT_OPENED);
        assertEquals(DictionaryID.VOCABULARY, listener2.mLastDictionaryID);

        assertEquals(2, mTestModel.getDictionaries().size());
        for (DictionaryID dictId : DictionaryID.values())
        {
            if (dictId == DictionaryID.UNIVERSAL)
            {
                assertEquals(mDictionaryMock, mTestModel.getDictionary(dictId));
            }
            else if (dictId == DictionaryID.VOCABULARY)
            {
                assertEquals(mVocabularyMock, mTestModel.getDictionary(dictId));
            }
            else
            {
                assertNull(mTestModel.getDictionary(dictId));
            }
        }

        // Open unsupported dictionary

        mModelListener.prepareWait();
        listener2.prepareWait();
        mTestModel.openDictionary(DictionaryID.GGLE);
        mModelListener.assertNewEvent(Model.ModelEvent.MODEL_EVENT_FAILURE);
        assertEquals(DictionaryID.GGLE, mModelListener.mLastDictionaryID);
        listener2.assertNewEvent(Model.ModelEvent.MODEL_EVENT_FAILURE);
        assertEquals(DictionaryID.GGLE, listener2.mLastDictionaryID);

        listener2 = null;

        // Close dictionary

        mModelListener.prepareWait();
        mTestModel.closeDictionary(DictionaryID.UNIVERSAL);
        mModelListener.assertNewEvent(Model.ModelEvent.MODEL_EVENT_CLOSED);
        assertEquals(DictionaryID.UNIVERSAL, mModelListener.mLastDictionaryID);
        assertEquals(1, mTestModel.getDictionaries().size());
        assertNull(mTestModel.getDictionary(DictionaryID.UNIVERSAL));

        // Close vocabulary

        mModelListener.prepareWait();
        mTestModel.closeDictionary(DictionaryID.VOCABULARY);
        mModelListener.assertNewEvent(Model.ModelEvent.MODEL_EVENT_CLOSED);
        assertEquals(DictionaryID.VOCABULARY, mModelListener.mLastDictionaryID);
        assertEquals(0, mTestModel.getDictionaries().size());
        assertNull(mTestModel.getDictionary(DictionaryID.VOCABULARY));

        // Close already closed
        mModelListener.prepareWait();
        mTestModel.closeDictionary(DictionaryID.VOCABULARY);
        mModelListener.assertNoEvent();
        assertEquals(0, mTestModel.getDictionaries().size());
    }

    //@Test
    public void testResetDictionary() throws InterruptedException
    {
        when(mFactoryMock.newVocabulary()).thenReturn(mVocabularyMock);

        mModelListener.prepareWait();
        mTestModel.subscribeDictionaryEvents(mModelListener);
        mModelListener.assertNoEvent();

        mModelListener.prepareWait();
        mTestModel.resetVocabulary();
        mModelListener.assertNewEvent(Model.ModelEvent.MODEL_EVENT_CLOSED);
        mModelListener.assertNewEvent(Model.ModelEvent.MODEL_EVENT_OPENED);
        verify(mVocabularyMock).clearAll();
    }

    boolean complete = false;

    @Test
    public void testAsync() throws InterruptedException
    {

        Log.d("test", "test thread: " +
            Thread.currentThread().getId() + " " + Thread.currentThread().getName());

        Log.d("test", "main thread: " +
            Looper.getMainLooper().getThread().getId() + " " +
            Looper.getMainLooper().getThread().getName());

        final CountDownLatch latch = new CountDownLatch(1);
        class MyTask extends AsyncTask<Void, Void, Void>
        {
            @Override
            protected Void doInBackground(Void... params)
            {
                Log.d("test", "async thread: " +
                    Thread.currentThread().getId() + " " + Thread.currentThread().getName());
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid)
            {
                Log.d("test", "post thread: " +
                    Thread.currentThread().getId() + " " + Thread.currentThread().getName());
                super.onPostExecute(aVoid);
                complete = true;
                latch.countDown();
            }
        };

        new MyTask().execute();
        latch.await();
        assertTrue(complete);
    }
}