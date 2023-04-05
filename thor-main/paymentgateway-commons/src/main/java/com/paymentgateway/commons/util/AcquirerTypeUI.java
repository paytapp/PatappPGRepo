	package com.paymentgateway.commons.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.paymentgateway.commons.dao.RouterConfigurationDao;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.user.AccountCurrencyRegion;
import com.paymentgateway.commons.user.CardHolderType;
import com.paymentgateway.commons.user.ChargingDetails;
import com.paymentgateway.commons.user.ChargingDetailsDao;
import com.paymentgateway.commons.user.RouterConfiguration;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;

/**
 * @author Rahul
 *
 */
public enum AcquirerTypeUI {

	FSS("FSS", "HDFC Bank"),
	HDFC("HDFC", "HDFC"),
	//ICICI_FIRSTDATA("ICICIFIRSTDATA", "ICICI Bank"),
	//FEDERAL("FEDERAL","FEDERAL Bank"), 
	//AXISMIGS("AXISMIGS", "AXIS Bank MIGS"), 	
	BOB("BOB", "Bank of Baroda"), 
	//KOTAK("KOTAK", "KOTAK Bank"),
	AXISBANKCB("AXISBANKCB", "AXIS Bank CB"), 
	//IDFC_FIRSTDATA("IDFCFIRSTDATA", "IDFC Bank"),
	//IDBIBANK("IDBIBANK", "IDBI Bank"), 
	FSSPAY("FSSPAY", "FSSPAY"),
	//BILLDESK("BILLDESK", "BILLDESK"),
	ISGPAY("ISGPAY", "ISGPAY"),
	AXISBANK("AXISBANK","Axis Bank"),
	PAYU ("PAYU", "PAYU"),
	SAFEXPAY ("SAFEXPAY", "SAFEXPAY"),
	CASHFREE ("CASHFREE", "CASHFREE"),

	//YESBANKCB("YESBANKCB", "YES Bank CB"),
	//IDFCUPI("IDFCUPI", "IDFCUPI Bank"),
	//ICICIUPI("ICICIUPI", "ICICIUPI Bank"),
	PAYPHI("PAYPHI", "PAYPHI");
	//APEXPAY("APEXPAY", "APEXPAY"),
	//VEPAY("VEPAY", "VEPAY"),
	//AIRPAY ("AIRPAY", "AIRPAY"),
	//RAZORPAY ("RAZORPAY","RAZORPAY"),
	//FONEPAISA ("FONEPAISA","FONEPAISA"),
	//QAICASH ("QAICASH","QAICASH"),
	//FLOXYPAY ("FLOXYPAY","FLOXYPAY"),
	//IPINT ("IPINT", "IPINT"),
	//P2PTSP("P2PTSP", "P2PTSP"),
	//UPIGATEWAY ("UPIGATEWAY","UPIGATEWAY"),
	//TOSHANIDIGITAL ("TOSHANIDIGITAL","TOSHANIDIGITAL"),
	//GLOBALPAY ("GLOBALPAY","GLOBALPAY");
	//DIGITALSOLUTIONS ("DIGITALSOLUTIONS","DIGITALSOLUTIONS"),
	//PAYIN247 ("PAYIN247","PAYIN247"),
	//GREZPAY ("GREZPAY","GREZPAY");

		private final String code;
		private final String name;

		private AcquirerTypeUI(String code, String name){
			this.code = code;
			this.name = name;
		}

		public String getCode() {
			return code;
		}

		public String getName() {
			return name;
		}

		public static AcquirerTypeUI getInstancefromCode(String acquirerCode){	
			AcquirerTypeUI acquirerType = null;

			for(AcquirerTypeUI acquirer:AcquirerTypeUI.values()){

				if(acquirerCode.equals(acquirer.getCode().toString())){
					acquirerType=acquirer;
					break;
				}
			}

			return acquirerType;		
		}	
		
		
		
		public static AcquirerTypeUI getInstanceUsingCode(String acquirer1) {
			AcquirerTypeUI acquirer = null;
			if (null != acquirer1) {
				for (AcquirerTypeUI pay : AcquirerTypeUI.values()) {
					if (acquirer1.equals(pay.getCode().toString())) {
						acquirer = pay;
						break;
					}
				}
			}
			return acquirer;
		}
		
		
		
		public static String getAcquirerName(String acquirerCode) {
			String acquirertype = null;
			if (null != acquirerCode) {
				for (AcquirerTypeUI acquirer : AcquirerTypeUI.values()) {
					if (acquirerCode.equals(acquirer.getCode().toString())) {
						acquirertype = acquirer.getName();
						break;
					}
				}
			}
			return acquirertype;
		}
		
		
		public static String getAcquirerNameByValues(String acquirerCode) {
			String acquirertype = null;
			if (null != acquirerCode) {
				for (AcquirerTypeUI acquirer : AcquirerTypeUI.values()) {
					String acquier=acquirer.toString();
					if (acquirerCode.equals(acquier)) {
						acquirertype = acquirer.getName();
						break;
					}
				}
			}
			return acquirertype;
		}
		
		

		public static AcquirerTypeUI getInstancefromName(String acquirerName){	
			AcquirerTypeUI acquirerType = null;

			for(AcquirerTypeUI acquirer:AcquirerTypeUI.values()){

				if(acquirerName.equals(acquirer.getName())){
					acquirerType=acquirer;
					break;
				}
			}

			return acquirerType;		
		}

		public static AcquirerTypeUI getDefault(Fields fields) throws SystemException{
			User user = new User();
			UserDao userDao = new UserDao();
			//user = userDao.findPayId(fields.get(FieldType.PAY_ID.getName()));
			user = userDao.getUserClass(fields.get(FieldType.PAY_ID.getName()));
			List<ChargingDetails> chargingDetailsList = new ArrayList<ChargingDetails>();
			chargingDetailsList = new ChargingDetailsDao().getAllActiveChargingDetails(user.getPayId());

			return getAcquirer(fields.getFields(),user,chargingDetailsList);
		}

		public static AcquirerTypeUI getDefault(Fields fields,User user) throws SystemException{
			List<ChargingDetails> chargingDetailsList = new ArrayList<ChargingDetails>();
			chargingDetailsList = new ChargingDetailsDao().getAllActiveChargingDetails(user.getPayId());
			return getAcquirer(fields.getFields(), user,chargingDetailsList);
		}
		
		public static AcquirerTypeUI getDefault(Fields fields,User user, List<ChargingDetails>  paymentOptions) throws SystemException{
			return getAcquirer(fields.getFields(), user,paymentOptions);
		}

		private static AcquirerTypeUI getAcquirer(Map<String,String> fields, User user, List<ChargingDetails>  paymentOptions) throws SystemException{
			String acquirerName = "";
			PaymentType paymentType = PaymentType.getInstanceUsingCode(fields.get(FieldType.PAYMENT_TYPE.getName()));
			String mopType = fields.get(FieldType.MOP_TYPE.getName());
			String currency = fields.get(FieldType.CURRENCY_CODE.getName());
			String paymentTypeCode = fields.get(FieldType.PAYMENT_TYPE.getName());
			String payId = user.getPayId();
			String transactionType = user.getModeType().toString();
			String paymentsRegion = AccountCurrencyRegion.DOMESTIC.toString();
			String cardHolderType = CardHolderType.COMMERCIAL.toString();
			
			if(StringUtils.isEmpty(fields.get(FieldType.PAYMENT_TYPE.getName())) || StringUtils.isEmpty(mopType) || StringUtils.isEmpty(currency)){
				return null;
			}
						
			String identifier = payId + currency + paymentTypeCode + mopType + transactionType+paymentsRegion+cardHolderType;
			RouterConfigurationDao routerConfigurationDao = new RouterConfigurationDao();
			switch (paymentType){

			case CREDIT_CARD:
			case DEBIT_CARD:
				
				

				List<RouterConfiguration> rulesList = new ArrayList<RouterConfiguration>();
				rulesList = routerConfigurationDao.findActiveAcquirersByIdentifier(identifier);

				if (rulesList.size() > 1) {

					for (RouterConfiguration routerConfiguration : rulesList) {

						acquirerName = getAcquirerName(routerConfiguration.getAcquirer());

					}
				} else {

					for (RouterConfiguration routerConfiguration : rulesList) {
						acquirerName = getAcquirerName(routerConfiguration.getAcquirer());

					}

				}

				break;
				
				/*RouterRuleDao routerRuleDao = new RouterRuleDao();
				RouterRule rule = routerRuleDao.findRuleByFieldsByPayId(payId, paymentTypeCode, mopType, currency, transactionType);
				if(rule == null){
					String allPayId = "ALL MERCHANTS";
					 rule = routerRuleDao.findRuleByFieldsByPayId(allPayId, paymentTypeCode, mopType, currency, transactionType);	
				}
				if(rule == null){
					throw new SystemException(ErrorType.ROUTER_RULE_NOT_FOUND,
							ErrorType.ROUTER_RULE_NOT_FOUND.getResponseCode());
				}
				//Map<String, String> acquirerMap = rule.getAcquirerMap();
				String  acquirerString = rule.getAcquirerMap();
				
				if(mopType.equals(MopType.AMEX.getCode())){
					acquirerName = AcquirerType.AMEX.getName();
				}else if(mopType.equals(MopType.EZEECLICK.getCode())){
					acquirerName = AcquirerType.EZEECLICK.getName();
				}else{ 
					// TODO.... Generic vs specific merchant
					
					
					Collection<String> acqList= Helper.parseFields(acquirerString);
					Map<String,String> acquirerMap = new LinkedHashMap<String,String>();
					for(String acquirer:acqList){
						 String[] acquirerPreference = acquirer.split("-");
						 acquirerMap.put(acquirerPreference[0], acquirerPreference[1]);
					 }
					
				//	String split[] = StringUtils.split(acquirerString,",");
					
					String primaryAcquirer =acquirerMap.get("1");
					acquirerName = getInstancefromCode(primaryAcquirer).getName();
					
					fields.put(FieldType.ACQUIRER_TYPE.toString(), primaryAcquirer);
					System.out.println(fields);
									
					List<PaymentType> supportedPaymentTypes = PaymentType.getGetPaymentsFromSystemProp(primaryAcquirer);	
					System.out.println(supportedPaymentTypes.toString());
							
				//	primaryAcquirerString= primaryAcquirerString.substring(2);
					
					//String secondAcquirerString = split[1];
					//secondAcquirerString= secondAcquirerString.substring(2);
					
					
					
					System.out.println(primaryAcquirer);
					//mapped acquirers
					Set<Account> accounts = user.getAccounts();
					
				}
				break;*/
			case NET_BANKING:
				for(ChargingDetails detail: paymentOptions){
						if(!detail.getPaymentType().getCode().equals(PaymentType.NET_BANKING.getCode())){
							continue;
						}
						if(mopType.equals(detail.getMopType().getCode())){
							acquirerName = detail.getAcquirerName();
							break;
						}
					}
				break;
			case WALLET:
				
				break;
			case EMI:
				break;
			case RECURRING_PAYMENT:
				
				break;
			case UPI:
				

				List<RouterConfiguration> rulesListUpi = new ArrayList<RouterConfiguration>();
				rulesListUpi = routerConfigurationDao.findActiveAcquirersByIdentifier(identifier);

				if (rulesListUpi.size() > 1) {

					for (RouterConfiguration routerConfiguration : rulesListUpi) {

						acquirerName = getAcquirerName(routerConfiguration.getAcquirer());

					}
				} else {

					for (RouterConfiguration routerConfiguration : rulesListUpi) {
						acquirerName = getAcquirerName(routerConfiguration.getAcquirer());

					}

				}

				break;
				
				/*RouterRuleDao ruleDao = new RouterRuleDao();
				RouterRule rules = ruleDao.findRuleByFieldsByPayId(payId, paymentTypeCode, mopType, currency, transactionType);
				if(rules == null){
					String allPayId = "ALL MERCHANTS";
					rules = ruleDao.findRuleByFieldsByPayId(allPayId, paymentTypeCode, mopType, currency, transactionType);	
				}
				if(rules == null){
					throw new SystemException(ErrorType.ROUTER_RULE_NOT_FOUND,
							ErrorType.ROUTER_RULE_NOT_FOUND.getResponseCode());
				}
				String  acquirerList = rules.getAcquirerMap();
				Collection<String> acqList= Helper.parseFields(acquirerList);
				Map<String,String> acquirerMap = new LinkedHashMap<String,String>();
				for(String acquirer:acqList){
					 String[] acquirerPreference = acquirer.split("-");
					 acquirerMap.put(acquirerPreference[0], acquirerPreference[1]);
				 }
				
				String primaryAcquirer =acquirerMap.get("1");
				//String primaryAcquirer ="FIRSTDATA";
				acquirerName = getInstancefromCode(primaryAcquirer).getName();
				break;*/
			default:
				break;
			}
			fields.put(FieldType.ACQUIRER_TYPE.getName(), getInstancefromName(acquirerName).getCode());
			return getInstancefromName(acquirerName);
		}
		
		
	}
