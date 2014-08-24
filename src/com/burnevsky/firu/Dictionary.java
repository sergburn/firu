package com.burnevsky.firu;

import java.util.List;

public class Dictionary {

    private boolean mInBulkOps = false;

    /*
     * public Table<Word> Words; // should be map of language public
     * Table<Translation> Translations; // should be map of language pair public
     * Table<Information> Info;
     */
    
    public Information Description;

    public class Information
    {
        public String OriginalFile;
        public String OriginalFormat;
        public String Name;
        public String SourceLanguage;
        public String TargetLanguage;
    }
    
    Dictionary(String connectionString) {
    }

    public static Dictionary open(String connectionString) {
	Dictionary self = new Dictionary(connectionString);
	/*
	 * if (!self.DatabaseExists()) { self.CreateDatabase(); }
	 */
	self.upgrade();
	/*
	 * self.TotalWords = self.Words.Count(); self.TotalTranslations =
	 * self.Translations.Count(); self.Description =
	 * self.Info.FirstOrDefault();
	 */
	return self;
    }

    private void upgrade() {
    }

    public int getTotalWords() {
	return 0;
    }

    public int getTotalTranslations() {
	return 0;
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

	public Translation(WordBase w, String ts, String targetLang) {
	    super(w, ts);
	    mTargetLang = targetLang;
	}
    }

    public WordBase AddWord(String word, List<String> translations, boolean submit) {
	WordBase w = new WordBase(word);
	for (String ts : translations) {
	    TranslationBase t = new TranslationBase(w, ts);
	    // Translations.InsertOnSubmit(t);
	    // w.Translations.Add(t);
	}
	// Words.InsertOnSubmit(w);
	if (!mInBulkOps) {
	    //submitChanges();
	}
	return w;
    }

    public List<WordBase> searchWords(String startsWith) {
	/*
	 * var words = from w in Words where w.Text.StartsWith(startsWith)
	 * select w;
	 * 
	 * return words.ToList<Word>();
	 */
	return null;
    }
    
    public List<Translation> getTranslations(WordBase w) {
	return null;
    }
    

}
