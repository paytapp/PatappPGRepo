package com.paymentgateway.commons.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public enum MopType {

	// Cards
	VISA		("Visa", "VI"),
	AMEX		("Amex", "AX"),
	DISCOVER	("Discover", "DI"),
	JCB			("JCB", "JC"),
	MASTERCARD	("MasterCard", "MC"),
	MAESTRO		("Maestro", "MS"),
	DINERS		("Diners", "DN"),
	RUPAY		("Rupay", "RU"),
	EZEECLICK	("EzeeClick", "EZ"),
	UPI			("UPI", "UP"),
	UPI_QR		("UPI QR", "UPI_QR"),
	GOOGLEPAY   ("GooglePay", "GP"),
	COD			("COD", "CD"),
	CRYPTO		("Crypto", "CR"),
	AAMARPAY	("AAMARPAY", "AP"),
	QR			("QR", "QR"),
	STATIC_UPI_QR		("STATIC UPI QR", "STATIC_UPI_QR"),

	//DIRECPAY & CITRUSPAY
	AXIS_BANK ("AXIS Bank", "1005"),
	BANK_OF_BAHRAIN_AND_KUWAIT ("Bank Of Bahrain And Kuwait", "1043"),
	BANK_OF_INDIA ("Bank Of India", "1009"),
	BANK_OF_MAHARASHTRA ("Bank Of Maharashtra", "1064"),
	CANARA_BANK ("Canara Bank", "1055"),
	CENTRAL_BANK_OF_INDIA ("Central Bank Of India", "1063"),
	CITIBANK ("CitiBank", "1010"),
	CITY_UNION_BANK ("City Union Bank", "1060"),
	CORPORATION_BANK ("Corporation Bank", "1034"),
	DEUTSCHE_BANK ("Deutsche Bank", "1026"),
	DEVELOPMENT_CREDIT_BANK ("Development Credit Bank", "1040"),
	FEDERAL_BANK ("Federal Bank", "1027"), 
	HDFC_BANK ("HDFC Bank", "1004"),
	ICICI_BANK ("ICICI Bank", "1013"),
	INDIAN_BANK ("Indian Bank", "1069"),
	YESBANK_CB ("YES BANK CB", "1022"),
	INDIAN_OVERSEAS_BANK ("Indian Overseas Bank", "1049"),
	INDUSIND_BANK ("IndusInd Bank", "1054"),
	INDUSTRIAL_DEVELOPMENT_BANK_OF_INDIA ("IDBI Bank", "1003"),
	ING_VYSYA_BANK ("ING Vysya Bank", "1062"),
	JAMMU_AND_KASHMIR_BANK ("Jammu And Kashmir Bank", "1041"),
	KARNATAKA_BANK_LTD ("Karnatka Bank Ltd", "1032"),
	KARUR_VYSYA_BANK ("Karur Vysya Bank", "1048"),
	KOTAK_BANK ("Kotak Bank", "1012"),
	ORIENTAL_BANK_OF_COMMERCE ("Oriental Bank Of Commerce", "1042"),
	RATNAKAR_BANK ("Ratnakar Bank", "1053"),
	SOUTH_INDIAN_BANK ("South Indian Bank", "1045"),
	STATE_BANK_OF_BIKANER_AND_JAIPUR ("State Bank Of Bikaner And Jaipur", "1050"),
	STATE_BANK_OF_HYDERABAD("StateBank Of Hyderabad", "1039"), 
	STATE_BANK_OF_INDIA ("State Bank Of India", "1030"), 
	STATE_BANK_OF_MYSORE ("State Bank Of Mysore", "1037"), 
	STATE_BANK_OF_PATIALA ("State Bank Of Patiala", "1068"),
	STATE_BANK_OF_TRAVANCORE ("State Bank Of Travancore", "1061"),
	TAMILNAD_MERCANTILE_BANK ("Tamilnad Mercantile Bank", "1065"),
	UNION_BANK_OF_INDIA ("Union Bank Of India", "1038"),
	UNITED_BANK_OF_INDIA ("United Bank Of India", "1046"),
	VIJAYA_BANK ("Vijaya Bank", "1044"),
	YES_BANK ("Yes Bank", "1001"),
	IDFCUPI_BANK ("IdfcUpi Bank", "1006"),
	DHANA_LAKSHMI_BANK	("Dhanalakshmi Bank","1105"),
	SARASWAT_BANK ("Saraswat Bank", "1106"),
	PUNJAB_NATIONAL_BANK ("Punjab National Bank", "1107"),
	PUNJAB_AND_SIND_BANK ("Punjab and Sind Bank", "1108"),
	BANDHAN_BANK ("Bandhan Bank", "1109"),
	DENA_BANK ("Dena Bank", "1110"),
	IDFC_BANK ("IDFC Bank", "1111"),
	DBS_BANK ("DBS Bank", "1112"),
	NKGSB_BANK ("NKGSB Bank", "1113"),
	RBL_BANK ("RBL Bank", "1114"),
	SHAMRAO_VITTHAL_BANK ("Shamrao Vitthal Co-operative Bank", "1115"),
	JANTA_SAHAKARI_BANK ("Janta Sahakari Bank", "1116"),
	ALLAHABAD_BANK("Allahabad Bank", "1117"),
	JANATA_SAHKARI_BANK("Janata Sahakari Bank", "1072"),
	
	// Direcpay Only
	DEMO_BANK ("DEMO Bank", "1025"),

	//CITRUSPAY ONLY
	ANDHRA_BANK ("Andhra Bank", "1091"),
	BANK_OF_BRAODA_CORPORATE_ACCOUNTS("Bank of Baroda Corporate Accounts", "1092"),
	BANK_OF_BARODA_RETAIL_ACCOUNTS("Bank of Baroda", "1093"),
	CATHOLIC_SYRIAN_BANK("Catholic Syrian Bank", "1094"),
	LAKSHMI_VILAS_BANK_NETBANKING("Lakshmi Vilas Bank", "1095"),	
	PUNJAB_NATIONAL_BANK_CORPORATE_ACCOUNTS("Punjab National Bank Corporate Accounts", "1096"),
	STANDARD_CHARTERED_BANK("Standard Chartered Bank", "1097"),
	SYNDICATE_BANK ("Syndicate Bank", "1098"),
	AXIS_CORPORATE_BANK ("Axis Corporate Bank", "1099"),
	ICICI_CORPORATE_BANK ("ICICI Corporate Bank", "1100"),
	PNB_CORPORATE_BANK ("PNB Corporate Bank", "1101"),
	HSBC_BANK ("HSBC Bank", "1102"),
	UCO_BANK ("UCO Bank", "1103"),
	COSMOS_BANK ("COSMOS Bank", "1104"),
	
	//BillDesk
	ANDHRA_BANK_CORPORATE("Andhra Bank Corporate", "1118"),
	KARNATAKA_GRAIMA_BANK("Karnataka Graima Bank", "1119"),
	PANJAB_AND_MAHARASHTRA_CORPORATE_BANK("Panjab and Maharashtra Corporate Bank","1120"),
	TAMIL_NADU_STATE_COORPORATIVE_BANK("Tamil Nadu State Coorporative Bank","1121"),
	TJSB_BANK("TJSB Bank","1122"),
	KALYAN_JANTA_SAHAKARI_BANK ("Kalyan Janta Sahakari Bank", "1123"),
	MEHSANA_URBAN_COORPORATIVE_BANK("Mehsana Urban Coorporative Bank","1124"),
	RBL_BANK_LIMITED_COORPORATIVE_BANKING ("RBL Bank Limited Coorporative Banking", "1125"),
	SHAMRAO_VITTHAL_COORPORATIVE_BANK_CORPORATE ("Shamrao Vitthal Co-operative Bank Corporate", "1126"),
	DHANLAKSHMI_BANK_CORPORATE("Dhanlakshmi Bank Corporate","1127"),
	BASSIEN_CATHOLIC_COORPORATIVE_BANK("Bassien Catholic Coorporative Bank","1128"),
	PNB_YUVA_NETBANKING("PNB Yuva NetBanking","1129"),
	THE_KALUPUR_COMMERCIAL("The Kalupur Commercial Coorporative Bank","1130"),
	EQUITAS_SMALL_FINANCE_BANK("Equitas Small Finance Bank","1131"),
	THANE_BHARAT_SAHAKARI_BANK_LTD("Thane Bharat Sahakari Bank Ltd","1132"),
	SURYODAY_SMALL_FINANCE_BANK("Suryoday Small Finance Bank","1133"),
	ESAF_SMALL_FINANCE_BANK("ESAF Small Finance Bank","1134"),
	VARACHHA_COORPORATIVE_BANK_LIMITED("Varachha Coorporative Bank Limited","1135"),
	NORTH_EAST_SMALL_FINANCE_BANK_LTD("North East Small finance Bank Ltd","1136"),
	CORPORATION_BANK_CORPORATE("Corporation Bank Corporate", "1137"),
	BARCLAYS_CORPORATE_BANKING("Barclays Corporate Banking","1138"),
	ZOROASTRIAN_COORPORATIVE_BANK("Zoroastrian Coorporative Bank","1139"),
	AU_SMALL_FINANCE_BANK("Au Small Finance Bank","1140"),
	BANDHAN_BANK_CORPORATE_BANKING("Bandhan Bank Corporate Banking","1141"),
	FINCARE_BANK("Fincare Bank","1142"),
	ANDHRA_PRAGATHI_GRAMEENA_BANK ("Andhra Pragathi Grameena Bank", "1143"),
	SHIVALIK_MERCANTILE_COORPORATIVE_BANK_LTD("Shivalik Mercantile Coorporative Bank Ltd","1144"),
	PAYTM_PAYMENTS_BANK("PAYTM PAYMENTS BANK","1155"),
	AIRTEL_PAYMENTS_BANK("Airtel Payments Bank", "1156"),
	NAINITAL_BANK("Nainital Bank", "1157"),
	UNION_BANK_OF_INDIA_CORPORATE_BANK ("Union Bank Of India Corporate Bank", "1158"),
	
	//lyra new bank
	ROYAL_BANK_OF_SCOTLAND("Royal Bank Of Scotland","1145"),
	IDBI_BANK("IDBI Bank","1146"),
	DCB_BANK("DCB Bank","1148"),
	CSB_BANK("CSB Bank","1149"),

	// Wallet
	PAYTM_WALLET ("PaytmWallet", "PPL"),
	MOBIKWIK_WALLET ("MobikwikWallet", "MWL"),
	OLAMONEY_WALLET ("OlaMoneyWallet", "OLAWL"),
    //MATCHMOVE_WALLET ("MatchMoveWallet", "MMWL"),
    AMAZON_PAY_WALLET ("AmazonPayWallet", "APWL"),

    AIRTEL_PAY_WALLET ("AirtelPayWallet", "AWL"),
    FREECHARGE_WALLET ("FreeChargeWallet", "FCWL"),
    GOOGLE_PAY_WALLET ("GooglePayWallet", "GPWL"),
    ITZ_CASH_WALLET ("ItzCashWallet", "ICWL"), 
    JIO_MONEY_WALLET ("JioMoneyWallet", "JMWL"),
    M_PESA_WALLET ("MPesaWallet", "MPWL"),
    OXYZEN_WALLET ("OxyzenWallet", "OXWL"),
    PHONE_PAY_WALLET ("PhonePayWallet", "PPWL"),
    SBI_BUDDY_WALLET ("SbiBuddyWallet", "SBWL"),
    ZIP_CASH_WALLET  ("ZipCashWallet", "ZCWL"),
    PAYZAPP			("PayZappWallet", "PZP"),
    YES_WALLET		("YesPayWallet", "YBWL"),
    PAYCASH_WALLET	("PayCashWallet", "PCWL"),		
	
	

	//Recurring Payment
	RECURRING_INVOICE    ("Recurring Invoice", "RIN"),
	
	//Money Transfer
	NEFT			("NEFT", "NEFT"),
	IMPS			("IMPS", "IMPS"),
	RTGS			("RTGS", "RTGS");


	private final String name;
	private final String code;

	private MopType(String name, String code){
		this.name = name;
		this.code = code;
	}

	public String getName() {
		return name;
	}

	public String getCode(){
		return code;
	}

	public static MopType getInstance(String name){
		MopType[] mopTypes = MopType.values();
		for(MopType mopType : mopTypes){
			if(mopType.getName().equals(name)){
				return mopType;
			}
		}		
		return null;
	}
	
	public static Map<String, String> getMopsForPayment(String mop){
		return getGetMopsCodeValueFromSystemProp(mop);
	}
	
	public static List<MopType> getDCMops(){
		return getGetMopsFromSystemProp("DCMOP");		
	}
	public static Map<String, String> getDCMopsForPayment(){
		return getGetMopsCodeValueFromSystemProp("DCMOP");
	}
	public static List<MopType> getCCMops(){
		return getGetMopsFromSystemProp("CCMOP");
	}
	public static Map<String, String> getCCMopsForPayment(){
		return getGetMopsCodeValueFromSystemProp("CCMOP");
	}

	public static List<MopType> getNBMops(){
		return getGetMopsFromSystemProp("NBMOP");
	}
	public static Map<String, String> getNBMopsForPayment(){
		return getGetMopsCodeValueFromSystemProp("NBMOP");
	}
	
	public static List<MopType> getWLMops(){
		return getGetMopsFromSystemProp("WALLET");
	}
	public static Map<String, String> getWLMopsForPayment(){
		return getGetMopsCodeValueFromSystemProp("WALLET");
	}
	public static Map<String, String> getUPMopsForPayment(){
		return getGetMopsCodeValueFromSystemProp("UPI");
	}
	public static Map<String, String> getCRMopsForPayment(){
		return getGetMopsCodeValueFromSystemProp("CRYPTO");
	}
	public static Map<String, String> getMQRMopsForPayment() {
		return getGetMopsCodeValueFromSystemProp("MQR");
	}
	public static List<MopType> getNEFTMops(){
		return getGetMopsFromSystemProp("NEFT");
	}
	
	public static List<MopType> getIMPSMops(){
		return getGetMopsFromSystemProp("IMPS");
	}
	
	public static List<MopType> getRTGSMops(){
		return getGetMopsFromSystemProp("RTGS");
	}
	
	public static List<MopType> getIFSMops(){
		return getGetMopsFromSystemProp("IFS");
	}
	
	public static List<MopType> getUPIMops(){
		return getGetMopsFromSystemProp("UPI");
	}
	
	public static List<MopType> getCDMops(){
		return getGetMopsFromSystemProp("COD");
	}
	
	public static List<MopType> getFSSUPIMops(){
		return getGetMopsFromSystemProp("FSSUPIMOP");		
	}

	public static List<MopType> getFSSCCMops(){
		return getGetMopsFromSystemProp("FSSCCMOP");		
	}
	public static List<MopType> getFSSDCMops(){
		return getGetMopsFromSystemProp("FSSDCMOP");		
	}
	
	public static List<MopType> getHDFCCCMops(){
		return getGetMopsFromSystemProp("HDFCCCMOP");		
	}
	public static List<MopType> getHDFCDCMops(){
		return getGetMopsFromSystemProp("HDFCDCMOP");		
	}
	
	public static List<MopType> getIDFCUPIMops(){
		return getGetMopsFromSystemProp("IDFCUPIMOP");		
	}
	public static List<MopType> getAXISBANKUPIMops(){
		return getGetMopsFromSystemProp("AXISBANKUPIMOP");		
	}
	
	public static List<MopType> getICICIUPIMops(){
		return getGetMopsFromSystemProp("ICICIUPIMOP");		
	}
	
	public static List<MopType> getBOBCCMops(){
		return getGetMopsFromSystemProp("BOBCCMOP");		
	}
	public static List<MopType> getBOBDCMops(){
		return getGetMopsFromSystemProp("BOBDCMOP");		
	}
	
	public static List<MopType> getBOBUPIMops(){
		return getGetMopsFromSystemProp("BOBUPIMOP");		
	}

	public static List<MopType> getBOBEMCCMops(){
		return getGetMopsFromSystemProp("BOBEMCCMOP");		
	}
	public static List<MopType> getBOBEMDCMops(){
		return getGetMopsFromSystemProp("BOBEMDCMOP");		
	}

	public static List<MopType> getFSSPAYUPIMops(){
		return getGetMopsFromSystemProp("FSSPAYUPIMOP");		
	}
	public static List<MopType> getFSSPAYCCMops(){
		return getGetMopsFromSystemProp("FSSPAYCCMOP");		
	}
	public static List<MopType> getFSSPAYDCMops(){
		return getGetMopsFromSystemProp("FSSPAYDCMOP");		
	}
	public static List<MopType> getFSSPAYPCMops(){
		return getGetMopsFromSystemProp("FSSPCMOP");		
	}
	public static List<MopType> getFSSPAYNBMops(){
		return getGetMopsFromSystemProp("FSSPAYNBMOP");		
	}
	public static List<MopType> getFSSPAYWLMops(){
		return getGetMopsFromSystemProp("FSSPAYWLMOP");		
	}
	public static List<MopType> getSBICCMops(){
		return getGetMopsFromSystemProp("SBICCMOP");		
	}
	public static List<MopType> getSBIDCMops(){
		return getGetMopsFromSystemProp("SBIDCMOP");		
	}
	public static List<MopType> getSBINBMops(){
		return getGetMopsFromSystemProp("SBINBMOP");		
	}
	public static List<MopType> getPAYUCCMops(){
		return getGetMopsFromSystemProp("PAYUCCMOP");		
	}
	public static List<MopType> getPAYUDCMops(){
		return getGetMopsFromSystemProp("PAYUDCMOP");		
	}
	public static List<MopType> getPAYUNBMops(){
		return getGetMopsFromSystemProp("PAYUNBMOP");		
	}
	public static List<MopType> getPAYUWLMops(){
		return getGetMopsFromSystemProp("PAYUWLMOP");		
	}
	public static List<MopType> getPAYUUPMops(){
		return getGetMopsFromSystemProp("PAYUUPMOP");		
	}
	public static List<MopType> getAXISCBCCMops(){
		return getGetMopsFromSystemProp("AXISCBCCMOP");		
	}
	public static List<MopType> getAXISCBDCMops(){
		return getGetMopsFromSystemProp("AXISCBDCMOP");		
	}
	
	public static List<MopType> getKOTAKCCMops(){
		return getGetMopsFromSystemProp("KOTAKCCMOP");		
	}
	public static List<MopType> getKOTAKDCMops(){
		return getGetMopsFromSystemProp("KOTAKDCMOP");		
	}
	public static List<MopType> getKOTAKUPIMops(){
		return getGetMopsFromSystemProp("KOTAKUPIMOP");		
	}
	
	public static List<MopType> getLYRACCMOPS(){
		return getGetMopsFromSystemProp("LYRACCMOP");		
	}
	public static List<MopType> getLYRADCMOPS(){
		return getGetMopsFromSystemProp("LYRADCMOP");		
	}
	public static List<MopType> getLYRANBMOPS(){
		return getGetMopsFromSystemProp("LYRANBMOP");		
	}
	public static List<MopType> getLYRAWLMOPS(){
		return getGetMopsFromSystemProp("LYRAWLMOP");		
	}
	public static List<MopType> getLYRAUPMOPS(){
		return getGetMopsFromSystemProp("LYRAUPMOP");		
	}
	public static List<MopType> getLYRADIRECTDCMOPS(){
		return getGetMopsFromSystemProp("LYRADIRECTDCMOP");		
	}
	public static List<MopType> getAKONTOPAYCCMOPS(){
		return getGetMopsFromSystemProp("AKONTOPAYCCMOP");		
	}
	public static List<MopType> getAKONTOPAYDCMOPS(){
		return getGetMopsFromSystemProp("AKONTOPAYDCMOP");		
	}
	public static List<MopType> getAKONTOPAYNBMOPS(){
		return getGetMopsFromSystemProp("AKONTOPAYNBMOP");		
	}
	public static List<MopType> getAKONTOPAYWLMOPS(){
		return getGetMopsFromSystemProp("AKONTOPAYWLMOP");		
	}
	public static List<MopType> getAKONTOPAYUPMOPS(){
		return getGetMopsFromSystemProp("AKONTOPAYUPMOP");		
	}
	public static List<MopType> getICICIMPGSCCMops(){
		return getGetMopsFromSystemProp("ICICIMPGSCCMOP");		
	}
	public static List<MopType> getICICIMPGSDCMops(){
		return getGetMopsFromSystemProp("ICICIMPGSDCMOP");		
	}
	
	public static List<MopType> getFIRSTDATADCMops(){
		return getGetMopsFromSystemProp("FIRSTDATADCMOP");		
	}

	public static List<MopType> getFIRSTDATACCMops(){
		return getGetMopsFromSystemProp("FIRSTDATACCMOP");		
	}
	
	public static List<MopType> getFEDERALDCMops(){
		return getGetMopsFromSystemProp("FEDERALDCMOP");		
	}
	
	public static List<MopType> getFEDERALUPIMops(){
		return getGetMopsFromSystemProp("FEDERALUPIMOP");		
	}
	public static List<MopType> getYESBANKCBMops(){
		return getGetMopsFromSystemProp("YESBANKCBMOP");		
	}
	public static List<MopType> getCYBERSOURCEDCMops(){
		return getGetMopsFromSystemProp("CYBERSOURCEDCMOP");		
	}

	public static List<MopType> getCYBERSOURCECCMops(){
		return getGetMopsFromSystemProp("CYBERSOURCECCMOP");		
	}
	public static List<MopType> getFEDERALCCMops(){
		return getGetMopsFromSystemProp("FEDERALCCMOP");		
	}

	public static List<MopType> getBARCLAYCCMops(){
		return getGetMopsFromSystemProp("BARCLAYCCMOP");		
	}

	public static List<MopType> getCITRUSDCMops(){
		return getGetMopsFromSystemProp("CITRUSDCMOP");		
	}
	
	public static List<MopType> getIpintCRMops(){
		return getGetMopsFromSystemProp("IPINTCRMOP");		
	}
	public static List<MopType> getAamarPayAPMops(){
		return getGetMopsFromSystemProp("AAMARPAYAPMOP");		
	}

	public static List<MopType> getCITRUSCCMops(){
		return getGetMopsFromSystemProp("CITRUSCCMOP");		
	}

	public static List<MopType> getAMEXCCMops(){
		return getGetMopsFromSystemProp("AMEXCCMOP");		
	}
	public static List<MopType> getEZEECLICKCCMops(){
		return getGetMopsFromSystemProp("EZEECLICKCCMOP");
	}
	public static List<MopType> getPAYTMWLMops(){
		return getGetMopsFromSystemProp("PAYTMWLMOP");		
	}
	public static List<MopType> getPAYTMUPMops(){
		return getGetMopsFromSystemProp("PAYTMUPMOP");		
	}
	public static List<MopType> getPAYTMNBMops(){
		return getGetMopsFromSystemProp("PAYTMNBMOP");		
	}
	public static List<MopType> getPAYTMCCMops(){
		return getGetMopsFromSystemProp("PAYTMCCMOP");		
	}
	public static List<MopType> getPAYTMDCMops(){
		return getGetMopsFromSystemProp("PAYTMDCMOP");		
	}
	public static List<MopType> getMOBIKWIKWLMops(){
		return getGetMopsFromSystemProp("MOBIKWIKWL");		
	}
	public static List<MopType> getCITRUSNBMops(){
		return getGetMopsFromSystemProp("CITRUSNB");		
	}
	public static List<MopType> getDIRECPAYNBMops(){
		return getGetMopsFromSystemProp("DIRECPAYNB");		
	}
	public static List<MopType> getKOTAKNBMops(){
		return getGetMopsFromSystemProp("KOTAKNB");		
	}
	public static List<MopType> getSBIBANKNBMops(){
		return getGetMopsFromSystemProp("SBINB");		
	}
	public static List<MopType> getALLAHABADBANKNBMops(){
		return getGetMopsFromSystemProp("ALLAHABADBANKNB");		
	}
	public static List<MopType> getVIJAYABANKNBMops(){
		return getGetMopsFromSystemProp("VIJAYABANKNB");		
	}
	public static List<MopType> getAXISBANKNBMops(){
		return getGetMopsFromSystemProp("AXISBANKNB");		
	}
	public static List<MopType> getICICIBANKNBMops(){
		return getGetMopsFromSystemProp("ICICIBANKNB");		
	}
	public static List<MopType> getCORPORATIONBANKNBMops(){
		return getGetMopsFromSystemProp("CORPORATIONBANKNB");		
	}
	public static List<MopType> getSOUTHINDIANBANKNBMops(){
		return getGetMopsFromSystemProp("SOUTHINDIANBANKNB");		
	}
	public static List<MopType> getKARURVYSYABANKNBMops(){
		return getGetMopsFromSystemProp("KARURVYSYABANKNB");		
	}
	public static List<MopType> getKARNATAKABANKNBMops(){
		return getGetMopsFromSystemProp("KARNATAKABANKNB");		
	}
	public static List<MopType> getOLAMONEYWLMops(){
		return getGetMopsFromSystemProp("OLAMONEYWL");		
	}
	public static List<MopType> getMIGSDCMops(){
		return getGetMopsFromSystemProp("MIGSDCMOP");		
	}
	public static List<MopType> getMIGSCCMops(){
		return getGetMopsFromSystemProp("MIGSCCMOP");		
	}
	public static List<MopType> getIDBIBANKDCMops(){
		return getGetMopsFromSystemProp("IDBIBANKDCMOP");		
	}
	public static List<MopType> getIDBIBANKCCMops(){
		return getGetMopsFromSystemProp("IDBIBANKCCMOP");		
	}
	public static List<MopType> getIDBIBANKNB(){
		return getGetMopsFromSystemProp("IDBIBANKNBMOP");		
	}
	public static List<MopType> getBILLDESKNBMops(){
		return getGetMopsFromSystemProp("BILLDESKNBMOP");		
	}
	public static List<MopType> getBILLDESKCCMops(){
		return getGetMopsFromSystemProp("BILLDESKCCMOP");		
	}
	public static List<MopType> getBILLDESKDCMops(){
		return getGetMopsFromSystemProp("BILLDESKDCMOP");		
	}
	public static List<MopType> getCODMops(){
		return getGetMopsFromSystemProp("CODMOP");		
	}
	public static List<MopType> getAPMops(){
		return getGetMopsFromSystemProp("APMOP");		
	}	
	public static List<MopType> getISGPAYCCMops(){
		return getGetMopsFromSystemProp("ISGPAYCCMOP");		
	}
	public static List<MopType> getISGPAYDCMops(){
		return getGetMopsFromSystemProp("ISGPAYDCMOP");		
	}
	public static List<MopType> getISGPAYNBMops(){
		return getGetMopsFromSystemProp("ISGPAYNBMOP");		
	}
	public static List<MopType> getISGPAYUPMops(){
		return getGetMopsFromSystemProp("ISGPAYUPMOP");		
	}
	public static List<MopType> getNBIDFCMops(){
		return getGetMopsFromSystemProp("NBIDFCMOP");		
	}
	public static List<MopType> getPAYPHIDCMops(){
		return getGetMopsFromSystemProp("PAYPHIDCMOP");		
	}
	public static List<MopType> getPAYPHICCMops(){
		return getGetMopsFromSystemProp("PAYPHICCMOP");		
	}
	public static List<MopType> getPAYPHINBMops(){
		return getGetMopsFromSystemProp("PAYPHINBMOP");		
	}
	public static List<MopType> getPAYPHIUPMops(){
		return getGetMopsFromSystemProp("PAYPHIUPIMOP");		
	}
	public static List<MopType> getFREECHARGEWLMops(){
		return getGetMopsFromSystemProp("FREECHARGEWLMOP");		
	}
	public static List<MopType> getCASHFREECCMOPMops() {
		return getGetMopsFromSystemProp("CASHFREECCMOP");
	}
	public static List<MopType> getCASHFREEDCMOPMops() {
		return getGetMopsFromSystemProp("CASHFREEDCMOP");
	}
	public static List<MopType> getCASHFREENBMops() {
		return getGetMopsFromSystemProp("CASHFREENBMOP");
	}
	public static List<MopType> getCASHFREEWLMops() {
		return getGetMopsFromSystemProp("CASHFREEWLMOP");
	}
	public static List<MopType> getCASHFREEUPMops() {
		return getGetMopsFromSystemProp("CASHFREEUPMOP");
	}
	public static List<MopType> getSAFEXPAYCCMOPS(){
		return getGetMopsFromSystemProp("SAFEXPAYCCMOP");		
	}
	public static List<MopType> getSAFEXPAYDCMOPS(){
		return getGetMopsFromSystemProp("SAFEXPAYDCMOP");		
	}
	public static List<MopType> getSAFEXPAYNBMOPS(){
		return getGetMopsFromSystemProp("SAFEXPAYNBMOP");		
	}
	public static List<MopType> getSAFEXPAYWLMOPS(){
		return getGetMopsFromSystemProp("SAFEXPAYWLMOP");		
	}
	public static List<MopType> getSAFEXPAYUPMOPS(){
		return getGetMopsFromSystemProp("SAFEXPAYUPMOP");		
	}
	public static List<MopType> getphonePeWLMops() {
		return getGetMopsFromSystemProp("PHONEPEWLMOP");
	}
	public static List<MopType> getPAYGNBMops() {
		return getGetMopsFromSystemProp("PAYGNBMOP");
	}
	public static List<MopType> getPAYGWLMops() {
		return getGetMopsFromSystemProp("PAYGWLMOP");
	}
	public static List<MopType> getPAYGMops() {
		return getGetMopsFromSystemProp("PAYGUPMOP");
	}
	public static List<MopType> getAIRPAYUPMOPS(){
		return getGetMopsFromSystemProp("AIRPAYUPMOP");		
	}
	public static List<MopType> getQAICASHUPMOPS(){
		return getGetMopsFromSystemProp("QAICASHUPMOP");		
	}
	public static List<MopType> getDIGITALSOLUPMOPS(){
		return getGetMopsFromSystemProp("DIGITALSOLUTIONUPMOP");		
	}
	public static List<MopType> getQAICASHNBMops() {
		return getGetMopsFromSystemProp("QAICASHNBMOP");
	}
	public static List<MopType> getGREZPAYUPMOPS(){
		return getGetMopsFromSystemProp("GREZPAYUPMOP");		
	}
	public static List<MopType> getUPIGATEWAYUPMOPS(){
		return getGetMopsFromSystemProp("UPIGATEWAYUPMOP");		
	}
	public static List<MopType> getTOSHANIDIGITALUPMOPS(){
		return getGetMopsFromSystemProp("TOSHANIDIGITALUPMOP");		
	}
	public static List<MopType> getZAAKPAYNBMops() {
		return getGetMopsFromSystemProp("ZAAKPAYNBMOP");
	}
	public static List<MopType> getZAAKPAYGWLMops() {
		return getGetMopsFromSystemProp("ZAAKPAYWLMOP");
	}
	public static List<MopType> getZAAKPAYUPMops() {
		return getGetMopsFromSystemProp("ZAAKPAYUPMOP");
	}
	
	public static List<MopType> getAPEXPAYCCMOPMops() {
		return getGetMopsFromSystemProp("APEXPAYCCMOP");
	}
	public static List<MopType> getAPEXPAYDCMOPMops() {
		return getGetMopsFromSystemProp("APEXPAYDCMOP");
	}
	public static List<MopType> getAPEXPAYNBMops() {
		return getGetMopsFromSystemProp("APEXPAYNBMOP");
	}
	public static List<MopType> getAPEXPAYWLMops() {
		return getGetMopsFromSystemProp("APEXPAYWLMOP");
	}
	public static List<MopType> getAPEXPAYUPMops() {
		return getGetMopsFromSystemProp("APEXPAYUPMOP");
	}

	public static List<MopType> getVEPAYCCMOPMops() {
		return getGetMopsFromSystemProp("VEPAYCCMOP");
	}
	public static List<MopType> getVEPAYDCMOPMops() {
		return getGetMopsFromSystemProp("VEPAYDCMOP");
	}
	public static List<MopType> getVEPAYNBMops() {
		return getGetMopsFromSystemProp("VEPAYNBMOP");
	}
	public static List<MopType> getVEPAYWLMops() {
		return getGetMopsFromSystemProp("VEPAYWLMOP");
	}
	public static List<MopType> getVEPAYUPMops() {
		return getGetMopsFromSystemProp("VEPAYUPMOP");
	}
	
	public static List<MopType> getRAZORPAYCCMOPS(){
		return getGetMopsFromSystemProp("RAZORPAYCCMOP");		
	}
	public static List<MopType> getRAZORPAYDCMOPS(){
		return getGetMopsFromSystemProp("RAZORPAYDCMOP");		
	}
	public static List<MopType> getRAZORPAYNBMOPS(){
		return getGetMopsFromSystemProp("RAZORPAYNBMOP");		
	}
	public static List<MopType> getRAZORPAYWLMOPS(){
		return getGetMopsFromSystemProp("RAZORPAYWLMOP");		
	}
	public static List<MopType> getRAZORPAYUPMOPS(){
		return getGetMopsFromSystemProp("RAZORPAYUPMOP");		
	}
	
	public static List<MopType> getFONEPAISACCMOPMops() {
		return getGetMopsFromSystemProp("FONEPAISACCMOP");
	}
	
	public static List<MopType> getFLOXYPAYUPMops() {
		return getGetMopsFromSystemProp("FLOXYPAYUPMOP");
	}
	public static List<MopType> getGLOBALPAYUPMOPS(){
		return getGetMopsFromSystemProp("GLOBALPAYUPMOP");		
	}
	public static List<MopType> getP2PTSPMQRMOP() {
		return getGetMopsFromSystemProp("P2PTSPMQRMOP");
	}
	
	public static List<MopType> getGetMopsFromSystemProp(String mopsList){

		List<String> mopStringList= (List<String>) Helper.parseFields(PropertiesManager.propertiesMap.get(mopsList));

		List<MopType> mops = new ArrayList<MopType>();

		for(String mopCode:mopStringList){
			MopType mop = getmop(mopCode);
			mops.add(mop);
		}
		return mops;
	}
	
	public static Map<String, String> getGetMopsCodeValueFromSystemProp(String mopsList){

		List<String> mopStringList= (List<String>) Helper.parseFields(PropertiesManager.propertiesMap.get(mopsList));

		Map<String, String> mops = new HashMap<String, String>();

		for(String mopCode:mopStringList){
			String mop = getmop(mopCode).getName();
			if(!mopCode.equals("UPI_QR"))
				mops.put(mopCode,mop);
		}
		return mops;
	}
	
	public static String getmopName(String mopCode){
		MopType mopType = MopType.getmop(mopCode);		
		if(mopType == null) {
			return "";
		}
		return mopType.getName();
	}

	public static MopType getmop(String mopCode){
		MopType mopObj = null;
		if(null!=mopCode){
			for(MopType mop:MopType.values()){
				if(mopCode.equals(mop.getCode().toString())){
					mopObj=mop;
					break;
				}
			}
		}
		return mopObj;
	}	
	public static MopType getInstanceIgnoreCase(String name){
		MopType[] mopTypes = MopType.values();
		for(MopType mopType : mopTypes){
			if(mopType.getName().equalsIgnoreCase(name)){
				return mopType;
			}
		}		
		return null;
	}
	public static String getMopTypeName(String mopCode) {
		String moptType = null;
		if (null != mopCode) {
			for (MopType mop : MopType.values()) {
				if (mopCode.equals(mop.getName().toString())) {
					moptType = mop.getCode();
					break;
				}
			}
		}
		return moptType;
	}
}