package com.paymentgateway.commons.util;

import java.util.ArrayList;
import java.util.List;

public enum MopTypeUI {

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
	GOOGLEPAY	("GooglePay", "GP"),
	COD			("COD", "CD"),
	NEFT		("NEFT", "NEFT"),
	IMPS		("IMPS", "IMPS"),
	RTGS		("RTGS", "RTGS"),
	CRYPTO		("Crypto", "CR"),
	AAMARPAY	("AAMARPAY", "AP"),
	STATIC_UPI_QR		("STATIC UPI QR", "STATIC_UPI_QR"),
	//AMAZON_PAY_WALLET ("AmazonPayWallet", "APWL"),
	//AIRTEL_PAY_WALLET ("AirtelPayWallet", "AWL"),
	FREECHARGE_WALLET ("FreeChargeWallet", "FCWL");
	//JIO_MONEY_WALLET ("JioMoneyWallet", "JMWL"),
	//MOBIKWIK_WALLET ("MobikwikWallet", "MWL"),
	//OLAMONEY_WALLET ("OlaMoneyWallet", "OLAWL"),
	//PHONE_PAY_WALLET ("PhonePayWallet", "PPWL"),
	//PAYZAPP			("PayZappWallet", "PZP"),
	//CATHOLIC_SYRIAN_BANK	("Catholic Syrian Bank", "1094");

	private final String name;
	private final String code;

	private MopTypeUI(String name, String code) {
		this.name = name;
		this.code = code;
	}

	public String getName() {
		return name;
	}

	public String getCode() {
		return code;
	}

	public static MopTypeUI getInstance(String name) {
		MopTypeUI[] mopTypes = MopTypeUI.values();
		for (MopTypeUI mopType : mopTypes) {
			if (mopType.getName().equals(name)) {
				return mopType;
			}
		}
		return null;
	}

	public static List<MopTypeUI> getDCMops() {
		return getGetMopsFromSystemProp("DCMOP");
	}

	public static List<MopTypeUI> getCCMops() {
		return getGetMopsFromSystemProp("CCMOP");
	}

	public static List<MopTypeUI> getWLMops() {
		return getGetMopsFromSystemProp("WALLET");
	}

	public static List<MopTypeUI> getUPIMops() {
		return getGetMopsFromSystemProp("UPI");
	}
	
	public static List<MopTypeUI> getCDMops(){
		return getGetMopsFromSystemProp("COD");
	}
	
	public static List<MopTypeUI> getNTMops(){
		return getGetMopsFromSystemProp("NEFT");
	}
	
	public static List<MopTypeUI> getIMMops(){
		return getGetMopsFromSystemProp("IMPS");
	}
	
	public static List<MopTypeUI> getRGMops(){
		return getGetMopsFromSystemProp("RTGS");
	}

	public static List<MopTypeUI> getFSSUPIMops() {
		return getGetMopsFromSystemProp("FSSUPIMOP");
	}

	public static List<MopTypeUI> getFSSCCMops() {
		return getGetMopsFromSystemProp("FSSCCMOP");
	}

	public static List<MopTypeUI> getFSSDCMops() {
		return getGetMopsFromSystemProp("FSSDCMOP");
	}

	public static List<MopTypeUI> getHDFCCCMops(){
		return getGetMopsFromSystemProp("HDFCCCMOP");		
	}
	public static List<MopTypeUI> getHDFCDCMops(){
		return getGetMopsFromSystemProp("HDFCDCMOP");		
	}
	
	public static List<MopTypeUI> getIDFCUPIMops() {
		return getGetMopsFromSystemProp("IDFCUPIMOP");
	}
	public static List<MopTypeUI> getICICIUPIMops() {
		return getGetMopsFromSystemProp("ICICIUPIMOP");
	}
	public static List<MopTypeUI> getFSSPAYUPIMops(){
		return getGetMopsFromSystemProp("FSSPAYUPIMOP");		
	}
	public static List<MopTypeUI> getFSSPAYCCMops(){
		return getGetMopsFromSystemProp("FSSPAYCCMOP");		
	}
	public static List<MopTypeUI> getFSSPAYDCMops(){
		return getGetMopsFromSystemProp("FSSPAYDCMOP");		
	}
	public static List<MopTypeUI> getFSSPAYPCMops(){
		return getGetMopsFromSystemProp("FSSPCMOP");		
	}
	public static List<MopTypeUI> getFSSPAYNBMops(){
		return getGetMopsFromSystemProp("FSSPAYNBMOP");		
	}
	public static List<MopTypeUI> getFSSPAYWLMops(){
		return getGetMopsFromSystemProp("FSSPAYWLMOP");		
	}
	public static List<MopTypeUI> getBOBCCMops() {
		return getGetMopsFromSystemProp("BOBCCMOP");
	}
	public static List<MopTypeUI> getBOBDCMops() {
		return getGetMopsFromSystemProp("BOBDCMOP");
	}
	public static List<MopTypeUI> getBOBUPIMops(){
		return getGetMopsFromSystemProp("BOBUPIMOP");		
	}
	public static List<MopTypeUI> getBOBEMCCMops(){
		return getGetMopsFromSystemProp("BOBEMCCMOP");		
	}
	public static List<MopTypeUI> getBOBEMDCMops(){
		return getGetMopsFromSystemProp("BOBEMDCMOP");		
	}
	public static List<MopTypeUI> getAXISCBCCMops() {
		return getGetMopsFromSystemProp("AXISCBCCMOP");
	}

	public static List<MopTypeUI> getAXISCBDCMops() {
		return getGetMopsFromSystemProp("AXISCBDCMOP");
	}
	public static List<MopTypeUI> getKOTAKCCMops() {
		return getGetMopsFromSystemProp("KOTAKCCMOP");
	}
	public static List<MopTypeUI> getKOTAKDCMops() {
		return getGetMopsFromSystemProp("KOTAKDCMOP");
	}
	public static List<MopTypeUI> getPAYUCCMops(){
		return getGetMopsFromSystemProp("PAYUCCMOP");		
	}
	public static List<MopTypeUI> getPAYUDCMops(){
		return getGetMopsFromSystemProp("PAYUDCMOP");		
	}
	public static List<MopTypeUI> getPAYUNBMops(){
		return getGetMopsFromSystemProp("PAYUNBMOP");		
	}
	public static List<MopTypeUI> getPAYUWLMops(){
		return getGetMopsFromSystemProp("PAYUWLMOP");		
	}
	public static List<MopTypeUI> getPAYUUPMops(){
		return getGetMopsFromSystemProp("PAYUUPMOP");		
	}
	
	public static List<MopTypeUI> getLYRACCMOPS(){
		return getGetMopsFromSystemProp("LYRACCMOP");		
	}
	public static List<MopTypeUI> getLYRADCMOPS(){
		return getGetMopsFromSystemProp("LYRADCMOP");		
	}
	public static List<MopTypeUI> getLYRADIRECTDCMOPS(){
		return getGetMopsFromSystemProp("LYRADIRECTDCMOP");		
	}
	public static List<MopTypeUI> getLYRANBMOPS(){
		return getGetMopsFromSystemProp("LYRANBMOP");		
	}
	public static List<MopTypeUI> getLYRAWLMOPS(){
		return getGetMopsFromSystemProp("LYRAWLMOP");		
	}
	public static List<MopTypeUI> getLYRAUPMOPS(){
		return getGetMopsFromSystemProp("LYRAUPMOP");		
	}
	public static List<MopTypeUI> getAKONTOPAYCCMOPS(){
		return getGetMopsFromSystemProp("AKONTOPAYCCMOP");		
	}
	public static List<MopTypeUI> getAKONTOPAYDCMOPS(){
		return getGetMopsFromSystemProp("AKONTOPAYDCMOP");		
	}
	public static List<MopTypeUI> getAKONTOPAYNBMOPS(){
		return getGetMopsFromSystemProp("AKONTOPAYNBMOP");		
	}
	public static List<MopTypeUI> getAKONTOPAYWLMOPS(){
		return getGetMopsFromSystemProp("AKONTOPAYWLMOP");		
	}
	public static List<MopTypeUI> getAKONTOPAYUPMOPS(){
		return getGetMopsFromSystemProp("AKONTOPAYUPMOP");		
	}
	public static List<MopTypeUI> getSBICCMops(){
		return getGetMopsFromSystemProp("SBICCMOP");		
	}
	public static List<MopTypeUI> getSBIDCMops(){
		return getGetMopsFromSystemProp("SBIDCMOP");		
	}
	public static List<MopTypeUI> getSBINBMops(){
		return getGetMopsFromSystemProp("SBINBMOP");		
	}
	public static List<MopTypeUI> getFIRSTDATADCMops() {
		return getGetMopsFromSystemProp("FIRSTDATADCMOP");
	}

	public static List<MopTypeUI> getFIRSTDATACCMops() {
		return getGetMopsFromSystemProp("FIRSTDATACCMOP");
	}

	public static List<MopTypeUI> getFEDERALDCMops() {
		return getGetMopsFromSystemProp("FEDERALDCMOP");
	}

	public static List<MopTypeUI> getFEDERALUPIMops() {
		return getGetMopsFromSystemProp("FEDERALUPIMOP");
	}

	public static List<MopTypeUI> getKOTAKUPIMops() {
		return getGetMopsFromSystemProp("KOTAKUPIMOP");
	}

	public static List<MopTypeUI> getYESBANKCBMops() {
		return getGetMopsFromSystemProp("YESBANKCBMOP");
	}

	public static List<MopTypeUI> getCYBERSOURCEDCMops() {
		return getGetMopsFromSystemProp("CYBERSOURCEDCMOP");
	}

	public static List<MopTypeUI> getCYBERSOURCECCMops() {
		return getGetMopsFromSystemProp("CYBERSOURCECCMOP");
	}

	public static List<MopTypeUI> getFEDERALCCMops() {
		return getGetMopsFromSystemProp("FEDERALCCMOP");
	}

	public static List<MopTypeUI> getBARCLAYCCMops() {
		return getGetMopsFromSystemProp("BARCLAYCCMOP");
	}

	public static List<MopTypeUI> getCITRUSDCMops() {
		return getGetMopsFromSystemProp("CITRUSDCMOP");
	}

	public static List<MopTypeUI> getCITRUSCCMops() {
		return getGetMopsFromSystemProp("CITRUSCCMOP");
	}

	public static List<MopTypeUI> getAMEXCCMops() {
		return getGetMopsFromSystemProp("AMEXCCMOP");
	}

	public static List<MopTypeUI> getEZEECLICKCCMops() {
		return getGetMopsFromSystemProp("EZEECLICKCCMOP");
	}

	public static List<MopTypeUI> getPAYTMWLMops() {
		return getGetMopsFromSystemProp("PAYTMWL");
	}
	public static List<MopTypeUI> getAXISBANKUPIMops(){
		return getGetMopsFromSystemProp("AXISBANKUPIMOP");		
	}

	/*
	 * public static List<MopTypeUI> getPAYTMNBMops(){ return
	 * getGetMopsFromSystemProp("PAYTMNBMOP"); }
	 */
	
	public static List<MopTypeUI> getPAYTMCCMops(){
		return getGetMopsFromSystemProp("PAYTMCCMOP");		
	}
	public static List<MopTypeUI> getPAYTMDCMops(){
		return getGetMopsFromSystemProp("PAYTMDCMOP");		
	}
	public static List<MopTypeUI> getPAYTMUPMops(){
		return getGetMopsFromSystemProp("PAYTMUPMOP");		
	}
	
	public static List<MopTypeUI> getMOBIKWIKWLMops() {
		return getGetMopsFromSystemProp("MOBIKWIKWL");
	}

	public static List<MopTypeUI> getCITRUSNBMops() {
		return getGetMopsFromSystemProp("CITRUSNB");
	}

	public static List<MopTypeUI> getDIRECPAYNBMops() {
		return getGetMopsFromSystemProp("DIRECPAYNB");
	}

	public static List<MopTypeUI> getKOTAKNBMops() {
		return getGetMopsFromSystemProp("KOTAKNB");
	}

	public static List<MopTypeUI> getIpintCRMops(){
		return getGetMopsFromSystemProp("IPINTCRMOP");		
	}
	public static List<MopTypeUI> getSBIBANKNBMops() {
		return getGetMopsFromSystemProp("SBINB");
	}

	public static List<MopTypeUI> getALLAHABADBANKNBMops() {
		return getGetMopsFromSystemProp("ALLAHABADBANKNB");
	}

	public static List<MopTypeUI> getVIJAYABANKNBMops() {
		return getGetMopsFromSystemProp("VIJAYABANKNB");
	}

	public static List<MopTypeUI> getAXISBANKNBMops() {
		return getGetMopsFromSystemProp("AXISBANKNB");
	}

	public static List<MopTypeUI> getICICIBANKNBMops() {
		return getGetMopsFromSystemProp("ICICIBANKNB");
	}

	public static List<MopTypeUI> getCORPORATIONBANKNBMops() {
		return getGetMopsFromSystemProp("CORPORATIONBANKNB");
	}

	public static List<MopTypeUI> getSOUTHINDIANBANKNBMops() {
		return getGetMopsFromSystemProp("SOUTHINDIANBANKNB");
	}

	public static List<MopTypeUI> getKARURVYSYABANKNBMops() {
		return getGetMopsFromSystemProp("KARURVYSYABANKNB");
	}

	public static List<MopTypeUI> getKARNATAKABANKNBMops() {
		return getGetMopsFromSystemProp("KARNATAKABANKNB");
	}

	public static List<MopTypeUI> getOLAMONEYWLMops() {
		return getGetMopsFromSystemProp("OLAMONEYWL");
	}

	public static List<MopTypeUI> getMIGSDCMops() {
		return getGetMopsFromSystemProp("MIGSDCMOP");
	}

	public static List<MopTypeUI> getMIGSCCMops() {
		return getGetMopsFromSystemProp("MIGSCCMOP");
	}

	public static List<MopTypeUI> getIDBIBANKDCMops() {
		return getGetMopsFromSystemProp("IDBIBANKDCMOP");
	}

	public static List<MopTypeUI> getIDBIBANKCCMops() {
		return getGetMopsFromSystemProp("IDBIBANKCCMOP");
	}
	
	public static List<MopTypeUI> getIDBIBANKNB(){
		return getGetMopsFromSystemProp("IDBIBANKNBMOP");		
	}
	public static List<MopTypeUI> getBILLDESKCCMops(){
		return getGetMopsFromSystemProp("BILLDESKCCMOP");		
	}
	public static List<MopTypeUI> getBILLDESKDCMops(){
		return getGetMopsFromSystemProp("BILLDESKDCMOP");		
	}
	public static List<MopTypeUI> getBILLDESKNBMops(){
		return getGetMopsFromSystemProp("BILLDESKNBMOP");		
	}
	public static List<MopTypeUI> getCODMops(){
		return getGetMopsFromSystemProp("CODMOP");		
	}
	
	public static List<MopTypeUI> getISGPAYCCMops(){
		return getGetMopsFromSystemProp("ISGPAYCCMOP");		
	}
	public static List<MopTypeUI> getISGPAYCMops(){
		return getGetMopsFromSystemProp("ISGPAYDCMOP");		
	}
	public static List<MopTypeUI> getISGPAYNBMops(){
		return getGetMopsFromSystemProp("ISGPAYNBMOP");		
	}
	public static List<MopTypeUI> getISGPAYUPMops(){
		return getGetMopsFromSystemProp("ISGPAYUPMOP");		
	}
	public static List<MopTypeUI> getIDFCNBMops(){
		return getGetMopsFromSystemProp("IDFCNBMOP");		
	}
	public static List<MopTypeUI> getAPEXPAYCCMOPMops() {
		return getGetMopsFromSystemProp("APEXPAYCCMOP");
	}
	public static List<MopTypeUI> getAPEXPAYDCMOPMops() {
		return getGetMopsFromSystemProp("APEXPAYDCMOP");
	}
	public static List<MopTypeUI> getAPEXPAYNBMops() {
		return getGetMopsFromSystemProp("APEXPAYNBMOP");
	}
	public static List<MopTypeUI> getAPEXPAYWLMops() {
		return getGetMopsFromSystemProp("APEXPAYWLMOP");
	}
	public static List<MopTypeUI> getAPEXPAYUPMops() {
		return getGetMopsFromSystemProp("APEXPAYUPMOP");
	}
	public static List<MopTypeUI> getGetMopsFromSystemProp(String mopsList) {

		List<String> mopStringList = (List<String>) Helper.parseFields(PropertiesManager.propertiesMap.get(mopsList));

		List<MopTypeUI> mops = new ArrayList<MopTypeUI>();

		for (String mopCode : mopStringList) {
			MopTypeUI mop = getmop(mopCode);
			mops.add(mop);
		}
		return mops;
	}

	public static String getmopName(String mopCode) {
		MopTypeUI mopType = MopTypeUI.getmop(mopCode);
		if (mopType == null) {
			return "";
		}
		return mopType.getName();
	}

	public static MopTypeUI getmop(String mopCode) {
		MopTypeUI mopObj = null;
		if (null != mopCode) {
			for (MopTypeUI mop : MopTypeUI.values()) {
				if (mopCode.equals(mop.getCode().toString())) {
					mopObj = mop;
					break;
				}
			}
		}
		return mopObj;
	}

	public static MopTypeUI getInstanceIgnoreCase(String name) {
		MopTypeUI[] mopTypes = MopTypeUI.values();
		for (MopTypeUI mopType : mopTypes) {
			if (mopType.getName().equalsIgnoreCase(name)) {
				return mopType;
			}
		}
		return null;
	}
}
