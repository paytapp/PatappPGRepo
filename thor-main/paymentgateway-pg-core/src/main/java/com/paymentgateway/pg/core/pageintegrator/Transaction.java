package com.paymentgateway.pg.core.pageintegrator;

public class Transaction {
	public static final char QUOTE = '"';
	public static final char COMMA = ',';
	public static final char COLON = ':';
	
	public Transaction() {
		super();
	}

	public void appendJsonField(String name, String value, StringBuilder request) {
		if(null == value || value.isEmpty()){
			return;
		}

		if(request.length() > 0){
			//Append comma
			request.append(COMMA);
		}

		//Append name
		request.append(QUOTE);
		request.append(name);
		request.append(QUOTE);

		//Append colon
		request.append(COLON);

		//Append value
		request.append(QUOTE);
		request.append(value);
		request.append(QUOTE);
	}
	
	public void appendJsonField(String name, boolean flag, StringBuilder request) {

		if(request.length() > 0){
			//Append comma
			request.append(COMMA);
		}
		//Append name
		request.append(QUOTE);
		request.append(name);
		request.append(QUOTE);
		//Append colon
		request.append(COLON);
		//Append value
		request.append(flag);
	}
}