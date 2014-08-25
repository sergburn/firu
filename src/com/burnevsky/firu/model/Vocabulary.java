package com.burnevsky.firu.model;

import java.util.List;

public class Vocabulary {
    
    // Pass the connection String to the base class.
    public Vocabulary(String connectionString)
    {
    }

    public static Vocabulary Open(String connectionString)
    {
        Vocabulary self = new Vocabulary(connectionString);

        self.upgrade();

        return self;
    }

    private void upgrade()
    {
/*	
        int requiredDbVersion = 0;
        // add upgrade code here
        if (dbUpdate.DatabaseSchemaVersion < requiredDbVersion)
        {
            dbUpdate.DatabaseSchemaVersion = requiredDbVersion;
            dbUpdate.Execute();
        }
*/        
    }
    
    public class Word extends WordBase {

	private String mSourceLang;
	
	public Word(String word, String sourceLang) {
	    super(word);
	    mSourceLang = sourceLang;
	}
	
	public String getSourceLang() {
	    return mSourceLang;
	}
	
    }
    
    public class Translation extends TranslationBase {

	private String mTargetLang;
	public Mark ForwardMark = Mark.YetToLearn;
	public Mark ReverseMark = Mark.YetToLearn;

	public Translation(WordBase w, String ts, String targetLang) {
	    super(w, ts);
	    mTargetLang = targetLang;
	}
	
	public String getTargetLang() {
	    return mTargetLang;
	}
    }	
    
    public Word addWord(WordBase dictWord, List<TranslationBase> translations, Dictionary.Information dictInfo)
    {
        Word w = new Word (dictWord.getText(), dictInfo.SourceLanguage);
        for (TranslationBase dt : translations)
        {
            Translation t = new Translation(w, dt.getText(), dictInfo.TargetLanguage);
            t.ForwardMark = Mark.YetToLearn;
            t.ReverseMark = Mark.YetToLearn;
            //w.Translations.Add(t);
        }
        //Words.InsertOnSubmit(w);
        return w;
    }
    
    public List<Translation> getTranslations(WordBase word) {
	return null;
    }

}
