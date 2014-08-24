package com.burnevsky.firu;

public class TranslationBase {

    private int mID = 0;
    private String mText;
    private int mWordID;

    public TranslationBase(WordBase w, String ts) {
	// TODO Auto-generated constructor stub
	mWordID = w.getID();
	mText = ts;
    }

    public int getID() {
	return mID;
    }

    public String getText() {
	return mText;
    }

}
