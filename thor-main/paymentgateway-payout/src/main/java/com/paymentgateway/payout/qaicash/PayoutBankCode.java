package com.paymentgateway.payout.qaicash;

public enum PayoutBankCode {

	ALLAIN("Allahabad Bank", "ALLA", "ALLAIN"), BKDNIN("Dena Bank", "BKDN", "BKDNIN"),
	BNDNIN("Bandhan Bank", "BDBL", "BNDNIN"), CIUBIN("City Union Bank", "CIUB", "CIUBIN"),
	CNRBIN("Canara Bank", "CNRB", "CNRBIN"), CORPIN("Corporation Bank", "CORP", "CORPIN"),
	CSYBIN("Catholic Syrian Bank", "CSBK", "CSYBIN"), DCBLIN("DCB Bank", "DCBL", "DCBLIN"),
	DEUTIN("Deutsche Bank", "DEUT", "DEUTIN"), DLXBIN("Dhanlaxmi Bank", "DLXB", "DLXBIN"),
	FDRLIN("Federal Bank", "FDRL", "FDRLIN"), HDFCIN("HDFC Bank Limited", "HDFC", "HDFCIN"),
	HSBCIN("HSBC Bank", "HSBC", "HSBCIN"), IBKLIN("IDBI Bank", "IBKL", "IBKLIN"),
	ICICIN("ICICI Bank Limited", "ICIC", "ICICIN"), IDFBIN("IDFC First Bank", "IDFB", "IDFBIN"),
	IN0001("Nainital Bank", "NTBL", "IN0001"), IN0002("Ujjivan Small Finance Bank", "UJVN", "IN0002"),
	IN0003("Jana Small Finance Bank", "JSFB", "IN0003"), IN0004("Equitas Small Finance Bank", "ESFB", "IN0004"),
	IN0005("AU Small Finance Bank", "AUBL", "IN0005"), IN0006("Capital Small Finance Bank", "CLBL", "IN0006"),
	IN0007("Fincare Small Finance Bank", "FSFB", "IN0007"), IN0008("ESAF Small Finance Bank", "ESMF", "IN0008"),
	IN0010("Standard Chartered Bank", "SCBL", "IN0010"), IN0011("Andhra Pradesh Grameena Vikas Bank", "APGV", "IN0011"),
	IN0012("Andhra Pragathi Grameena Bank", "APGB", "IN0012"),
	IN0025("Karnataka Vikas Grameena Bank", "KVGB", "IN0025"), IN0026("Kerala Gramin Bank", "KLGB", "IN0026"),
	IN0029("Vidarbha Konkan Gramin Bank", "BKID", "IN0029"), IN0039("PAYTM PAYMENTS BANK", "PYTM", "IN0039"),
	IN0040("Airtel Payments Bank", "AIRP", "IN0040"), INDBIN("IndusInd Bank Limited", "INDB", "INDBIN"),
	IOBAIN("Indian Overseas Bank", "IOBA", "IOBAIN"), JAKAIN("Jammu & Kashmir Bank", "JAKA", "JAKAIN"),
	KARBIN("Karnataka Bank", "KARB", "KARBIN"), KKBKIN("Kotak Mahindra Bank", "KKBK", "KKBKIN"),
	KVBLIN("Karur Vysya Bank", "KVBL", "KVBLIN"), ORBCIN("Oriental Bank of Commerce", "ORBC", "ORBCIN"),
	PSIBIN("Punjab and Sind Bank", "PSIB", "PSIBIN"), RATNIN("RBL Bank", "RATN", "RATNIN"),
	SOININ("South Indian Bank", "SIBL", "SOININ"), SYNBIN("Syndicate Bank", "SYNB", "SYNBIN"),
	TMBLIN("Tamilnad Mercantile Bank", "TMBL", "TMBLIN"), UBININ("Union Bank of India", "UBIN", "UBININ"),
	VIJBIN("Vijaya Bank", "VIJB", "VIJBIN"), YESBIN("Yes Bank", "YESB", "YESBIN"),
	BSSEIN("Bassein Catholic Co-operative Bank", "BACB", "BSSEIN"), CITIIN("Citi bank", "CITI", "CITIIN"),
	KALUIN("kalupur bank", "KCCB", "KALUIN"), RBISIN("Reserve Bank", "RBIS", "RBISIN"),
	SRCBIN("Saraswat Bank", "SRCB", "SRCBIN"), SBININ("State Bank Of India", "SBIN", "SBININ"),
	BKIDIN("Bank Of India", "BKID", "BKIDIN"), DBSSIN("DBS Bank", "DBSS", "DBSSIN"),
	PUNBIN("Punjab National Bank", "PUNB", "PUNBIN"), UCBAIN("UCO Bank", "UCBA", "UCBAIN"),
	ANDBIN("Andhra Bank", "ANDB", "ANDBIN"), IDIBIN("Indian Bank", "IDIB", "IDIBIN"),
	UTBIIN("United Bank of India", "UTBI", "UTBIIN"), CBININ("Central Bank of India", "CBIN", "CBININ"),
	BARBIN("Bank Of Baroda", "BARB", "BARBIN"), MAHBIN("Bank of Maharashtra", "MAHB", "MAHBIN");

	private final String name;
	private final String code;
	private final String ifsc;

	private PayoutBankCode(String name, String code, String ifsc) {
		this.name = name;
		this.code = code;
		this.ifsc = ifsc;
	}

	public String getName() {
		return name;
	}

	public String getCode() {
		return code;
	}

	public String getIfsc() {
		return ifsc;
	}

	public static String getBankFullIFSCCodeByIFSC4C(String name) {
		String ifsc = null;
		if (null != name) {
			for (PayoutBankCode bank : PayoutBankCode.values()) {
				if (name.equalsIgnoreCase(bank.getCode().toString())) {
					ifsc = bank.getIfsc();
					break;
				}
			}
		}
		return ifsc;
	}

}
