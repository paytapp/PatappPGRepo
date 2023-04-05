package com.paymentgateway.commons.util;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.commons.lang3.StringUtils;

import com.paymentgateway.commons.dao.RouterConfigurationDao;
import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.user.ChargingDetails;
import com.paymentgateway.commons.user.ChargingDetailsDao;
import com.paymentgateway.commons.user.RouterConfiguration;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;

/**
 * @author Rahul
 *
 */
public enum AcquirerType {

	FSS("FSS", "HDFC Bank"),
	HDFC("HDFC", "HDFC"),
	ICICI_FIRSTDATA("ICICIFIRSTDATA", "ICICI Bank"),
	FEDERAL("FEDERAL","FEDERAL Bank"), 
	AXISMIGS("AXISMIGS", "AXIS Bank MIGS"), 	
	BOB("BOB", "Bank of Baroda"), 
	KOTAK("KOTAK", "KOTAK Bank"),
	AXISBANKCB("AXISBANKCB", "AXIS Bank CB"), 
	IDFC_FIRSTDATA("IDFCFIRSTDATA", "IDFC Bank"),
	IDBIBANK("IDBIBANK", "IDBI Bank"), 
	FSSPAY("FSSPAY", "FSSPAY"),
	BILLDESK("BILLDESK", "BILLDESK"),
	ISGPAY("ISGPAY", "ISGPAY"),
	AXISBANK("AXISBANK","Axis Bank"),
	PAYU ("PAYU", "PAYU"),
	SAFEXPAY ("SAFEXPAY", "SAFEXPAY"),
	CASHFREE ("CASHFREE", "CASHFREE"),
	

	YESBANKCB("YESBANKCB", "YES Bank CB"),
	IDFCUPI("IDFCUPI", "IDFCUPI Bank"),
	ICICIUPI("ICICIUPI", "ICICIUPI Bank"),
	PAYPHI("PAYPHI", "PAYPHI"),
	APEXPAY("APEXPAY", "APEXPAY"),
	VEPAY("VEPAY", "VEPAY"),
	AIRPAY ("AIRPAY", "AIRPAY"),
	RAZORPAY ("RAZORPAY","RAZORPAY"),
	FONEPAISA ("FONEPAISA","FONEPAISA"),
	QAICASH ("QAICASH","QAICASH"),
	FLOXYPAY ("FLOXYPAY","FLOXYPAY"),
	DIGITALSOLUTIONS ("DIGITALSOLUTIONS","DIGITALSOLUTIONS"),
	PAYIN247 ("PAYIN247","PAYIN247"),
	GREZPAY ("GREZPAY","GREZPAY"),
	IPINT ("IPINT", "IPINT"),
	UPIGATEWAY ("UPIGATEWAY","UPIGATEWAY"),
	TOSHANIDIGITAL ("TOSHANIDIGITAL","TOSHANIDIGITAL"),
	P2PTSP("P2PTSP", "P2PTSP"),
	GLOBALPAY ("GLOBALPAY","GLOBALPAY");
	

	private final String code;
	private final String name;

	private AcquirerType(String code, String name) {
		this.code = code;
		this.name = name;
	}

	public String getCode() {
		return code;
	}

	public String getName() {
		return name;
	}

	public static AcquirerType getInstancefromCode(String acquirerCode) {
		AcquirerType acquirerType = null;

		for (AcquirerType acquirer : AcquirerType.values()) {

			if (acquirerCode.equals(acquirer.getCode().toString())) {
				acquirerType = acquirer;
				break;
			}
		}

		return acquirerType;
	}

	public static String getAcquirerName(String acquirerCode) {
		String acquirertype = null;
		if (null != acquirerCode) {
			for (AcquirerType acquirer : AcquirerType.values()) {
				if (acquirerCode.equalsIgnoreCase(acquirer.getCode().toString())) {
					acquirertype = acquirer.getName();
					break;
				}
			}
		}
		return acquirertype;
	}
	
	public static String getAcquirerCode(String acquirerName) {
		String acquirertype = null;
		if (null != acquirerName) {
			for (AcquirerType acquirer : AcquirerType.values()) {
				if (acquirerName.equalsIgnoreCase(acquirer.getName().toString())) {
					acquirertype = acquirer.getCode();
					break;
				}
			}
		}
		return acquirertype;
	}

	public static AcquirerType getInstancefromName(String acquirerName) {
		AcquirerType acquirerType = null;

		for (AcquirerType acquirer : AcquirerType.values()) {

			if (acquirerName.equalsIgnoreCase(acquirer.getName())) {
				acquirerType = acquirer;
				break;
			}
		}

		return acquirerType;
	}

	public static AcquirerType getDefault(Fields fields) throws SystemException {
		User user = new User();
		UserDao userDao = new UserDao();
		// user = userDao.findPayId(fields.get(FieldType.PAY_ID.getName()));
		user = userDao.getUserClass(fields.get(FieldType.PAY_ID.getName()));
		List<ChargingDetails> chargingDetailsList = new ArrayList<ChargingDetails>();
		chargingDetailsList = new ChargingDetailsDao().getAllActiveChargingDetails(user.getPayId());

		return getAcquirer(fields.getFields(), user, chargingDetailsList);
	}

	public static AcquirerType getDefault(Fields fields, User user) throws SystemException {
		List<ChargingDetails> chargingDetailsList = new ArrayList<ChargingDetails>();
		chargingDetailsList = new ChargingDetailsDao().getAllActiveChargingDetails(user.getPayId());
		return getAcquirer(fields.getFields(), user, chargingDetailsList);
	}

	public static AcquirerType getDefault(Fields fields, User user, List<ChargingDetails> paymentOptions)
			throws SystemException {
		return getAcquirer(fields.getFields(), user, paymentOptions);
	}

	private static int getRandomNumber() {
		Random rnd = new Random();
		int randomNumber = (int) (rnd.nextInt(100)) + 1;
		return randomNumber;
	}

	private static AcquirerType getAcquirer(Map<String, String> fields, User user, List<ChargingDetails> paymentOptions)
			throws SystemException {
		String acquirerName = "";
		PaymentType paymentType = PaymentType.getInstanceUsingCode(fields.get(FieldType.PAYMENT_TYPE.getName()));
		String mopType = fields.get(FieldType.MOP_TYPE.getName());
		String currency = fields.get(FieldType.CURRENCY_CODE.getName());
		String paymentTypeCode = fields.get(FieldType.PAYMENT_TYPE.getName());
		String payId = user.getPayId();
		String transactionType = user.getModeType().toString();

		String paymentsRegion = fields.get(FieldType.PAYMENTS_REGION.getName());
		String cardHolderType = fields.get(FieldType.CARD_HOLDER_TYPE.getName());

		String slabId = "";

		boolean paymentTypeFound = false;
		
		String checkAmountString = PropertiesManager.propertiesMap.get(Constants.SWITCH_ACQUIRER_AMOUNT.getValue());

		if (!checkAmountString.contains(payId)) {
			paymentTypeFound = false;
		}
		else {
			String[] checkAmountArray = checkAmountString.split(",");
			BigDecimal transactionAmount = new BigDecimal(Amount.toDecimal(fields.get(FieldType.AMOUNT.getName()),
					fields.get(FieldType.CURRENCY_CODE.getName())));

			for (String amountSlab : checkAmountArray) {

				if (!amountSlab.contains(payId)) {
					continue;
				}

				String[] amountSlabArray = amountSlab.split("-");

				BigDecimal minTxnAmount = new BigDecimal(amountSlabArray[1]);
				BigDecimal maxTxnAmount = new BigDecimal(amountSlabArray[2]);

				if (!StringUtils.isBlank(amountSlabArray[4])) {

					if (amountSlabArray[4].equalsIgnoreCase(paymentType.getCode())) {

						minTxnAmount = new BigDecimal(amountSlabArray[1]);
						maxTxnAmount = new BigDecimal(amountSlabArray[2]);

						if (transactionAmount.compareTo(minTxnAmount) >= 0
								&& transactionAmount.compareTo(maxTxnAmount) < 1) {
							slabId = amountSlabArray[0];
							paymentTypeFound = true;
							break;
						} else {
							paymentTypeFound = false;
						}

					} else {
						paymentTypeFound = false;
					}
				}

			}
			
		}

		
		if (!paymentTypeFound) {
			slabId = "00";
		}

		if (StringUtils.isEmpty(fields.get(FieldType.PAYMENT_TYPE.getName())) || StringUtils.isEmpty(mopType)
				|| StringUtils.isEmpty(currency)) {
			return null;
		}

		String identifier = payId + currency + paymentTypeCode + mopType + transactionType + paymentsRegion
				+ cardHolderType + slabId;
		RouterConfigurationDao routerConfigurationDao = new RouterConfigurationDao();
		switch (paymentType) {

		case CREDIT_CARD:
		case DEBIT_CARD:

			List<RouterConfiguration> rulesList = new ArrayList<RouterConfiguration>();
			// need a sorted list according to priority
			rulesList = routerConfigurationDao.findActiveAcquirersByIdentifier(identifier);

			int randomNumber = getRandomNumber();
			if (rulesList.size() > 1) {

				int min = 1;
				int max = 0;
				for (RouterConfiguration routerConfiguration : rulesList) {
					int loadPercentage = routerConfiguration.getLoadPercentage();
					min = 1 + max;
					max = max + loadPercentage;
					if (randomNumber >= min && randomNumber < max) {
						acquirerName = getAcquirerName(routerConfiguration.getAcquirer());
						break;
					}
				}
			} else {

				for (RouterConfiguration routerConfiguration : rulesList) {
					acquirerName = getAcquirerName(routerConfiguration.getAcquirer());

				}
			}
			break;

		case NET_BANKING:
			for (ChargingDetails detail : paymentOptions) {
				if (!detail.getPaymentType().getCode().equals(PaymentType.NET_BANKING.getCode())) {
					continue;
				}
				if (mopType.equals(detail.getMopType().getCode())) {
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
			// need a sorted list according to priority
			rulesListUpi = routerConfigurationDao.findActiveAcquirersByIdentifier(identifier);

			int randomNumberUpi = getRandomNumber();
			if (rulesListUpi.size() > 1) {

				int min = 1;
				int max = 0;
				for (RouterConfiguration routerConfiguration : rulesListUpi) {
					int loadPercentage = routerConfiguration.getLoadPercentage();
					min = 1 + max;
					max = max + loadPercentage;
					if (randomNumberUpi >= min && randomNumberUpi < max) {
						acquirerName = getAcquirerName(routerConfiguration.getAcquirer());
						break;
					}
				}
			} else {

				for (RouterConfiguration routerConfiguration : rulesListUpi) {
					acquirerName = getAcquirerName(routerConfiguration.getAcquirer());

				}
			}
			break;

		default:
			break;
		}
		if (StringUtils.isEmpty(acquirerName)) {
			throw new SystemException(ErrorType.ACQUIRER_NOT_FOUND, ErrorType.ACQUIRER_NOT_FOUND.getResponseCode());
		}
		fields.put(FieldType.ACQUIRER_TYPE.getName(), getInstancefromName(acquirerName).getCode());
		return getInstancefromName(acquirerName);
	}

}
