package com.paymentgateway.pg.core.util;

public enum LocaleLanguageType {

	ENGLISH		("English", "en"),
	URDU		("Urdu" , "ur"),
	SPANISH		("Spanish", "es"),
	FRENCH	    ("French", "fr"),
	GERMAN		("German", "ge"),
	HINDI		("Hindi", "hn"),
	PUNJABI		("Punjabi", "pn"),
	MARATHI		("Marathi", "ma"),
	GUJRATI	    ("Gujrati", "gj"),
	BENGALI		("Bengali", "bn"),
	TAMIL		("Tamil", "ta"),
	TELUGU	    ("Telugu", "te"),
	MALAYALAM	("Malayalam" , "ml"),
	ARABIC		("Arabic" , "ar"),
	KANNADA		("Kannada", "ka");
	
	
	private final String name;
	private final String code;
	
	private LocaleLanguageType(String name, String code){
		this.name = name;
		this.code = code;
	}

	public String getName() {
		return name;
	}

	public String getCode() {
		return code;
	}
	
	public static String getLocaleLangauge(String langCode) {
		String language = null;
		if (null != langCode) {
			for (LocaleLanguageType lang : LocaleLanguageType.values()) {
				if (langCode.equalsIgnoreCase(lang.getCode().toString())) {
					language = lang.getName();
					break;
				}
			}
		}
		return language;
	}
}
