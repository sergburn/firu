package com.burnevsky.firu;

import java.util.List;

public class WordBase {

    private int mID;
    private String mText;
    
    public WordBase(String word) {
	// TODO Auto-generated constructor stub
    }

    public int getID()
    {
	return mID;
    }
    
    public String getText()
    {
	return mText;
    }
    
    public List<TranslationBase> getTranslations()
    {
	return null;
		
    }
    
}
