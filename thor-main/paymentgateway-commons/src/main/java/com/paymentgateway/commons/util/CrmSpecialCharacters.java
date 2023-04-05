package com.paymentgateway.commons.util;

public enum CrmSpecialCharacters {

	IP              ((new String(".:")).toCharArray()),
	PASSWORD        ((new String("!@,_+/=")).toCharArray()),
	ADDRESS         ((new String("-/() .,@;:# \r\n")).toCharArray()),
	OPERATINGSYSTEM ((new String(" ,.")).toCharArray()),
	WEBSITE			((new String(":./-")).toCharArray()),
	BUSINESSMODEL	((new String(":./ -")).toCharArray()),
	BROWSER			((new String("./ -")).toCharArray()),
	BUSINESS_NAME	((new String("- /&")).toCharArray()),
	DATE            ((new String("/ -:")).toCharArray()),
	CARD_MASK       ((new String("*-X")).toCharArray()),
	SPACE           ((new String(" ")).toCharArray()),
	COMPANY         ((new String("-/() .,@;:# \r\n")).toCharArray()),
	ORDER_ID        ((new String(" @-_+/=*,.:\n\r?&")).toCharArray()),
	CATEGORY_CODE   ((new String(" @-_+/=*.:;?")).toCharArray()),
	SKU_CODE        ((new String(" @-_+/=*.:;?")).toCharArray()),
	INVOICE_NUMBER				((new String("/ -")).toCharArray()),
	TELEPHONE     				((new String("+ -")).toCharArray()),
	AMEX_SELLER_ID_MCC			((new String("/")).toCharArray()),
	ALL_SPEICIAL_CHARACTERS     ((new String("!@,-_+/=*.:;()#~ \r\n&")).toCharArray()),
	ALL_SPEICIAL_CHARACTERS_MAPPING     ((new String("!@$,-_+/=*.:;()#~ \r\n")).toCharArray()),
	SPEICIAL_CHARACTERS_FOR_TICKET((new String("~`!@#$%^&*()_+-={}[]:;'<>,.?/||&&\r\n| /")).toCharArray()),
	INDUSTRYCATEGORY            ((new String("_")).toCharArray()),
	INDUSTRYSUBCATEGORY         ((new String(" _")).toCharArray()),
	CARD_TYPE                   ((new String("_")).toCharArray()),
	INVOICE_DATE				((new String("-").toCharArray())),
	INVOICE_DATETIME			((new String("-, ,:").toCharArray())),
	DECIMAL						((new String(".").toCharArray())),
	COMMA_SEPERATED            ((new String(",")).toCharArray()),
	VPA              ((new String("@-.")).toCharArray());
	
	private final char[] permittedSpecialChars;

	private CrmSpecialCharacters(char[] permittedSpecialChars)
	{
		this.permittedSpecialChars = permittedSpecialChars;
	}

	public char[] getPermittedSpecialChars() {
		return permittedSpecialChars;
	}
}
