package com.paymentgateway.crm.action;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.paymentgateway.commons.api.Hasher;
import com.paymentgateway.commons.dao.FieldsDao;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.CrmFieldType;
import com.paymentgateway.commons.util.EnachDCIssuerType;
import com.paymentgateway.commons.util.EnachNBIssuerType;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.Frequency;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.TransactionManager;

/**
 * @author Rajit
 **/
public class ENachAction extends AbstractSecureAction {
	
	private static Logger logger = LoggerFactory.getLogger(ENachAction.class.getName());
	private static final long serialVersionUID = 3873797008881468412L;

	@Autowired
	private FieldsDao fieldsDao;
	
	@Autowired
	private UserDao userDao;
	
	@Autowired
	PropertiesManager propertiesManager;
	
	private String paymentMode;
	private String accountNumber;
	private String accountType;
	private String ifscCode;
	private String accountHolderName;
	private String frequency;
	private String payId;
	private String subMerchantPayId;
	private String amount;
	private String maxAmount;
	private String totalAmount;
	private String debitStartDate;
	private String debitEndDate;
	private String cardNumber;
	private String expMonth;
	private String expYear;
	private String cvv;
	private String bankCode;
	private String nameOnCard;
	private String tenure;
	private String bankName;
	private String consumerMobileNo;
	private String consumerEmailId;
	private String merchantLogo;
	private String returnUrl;
	private String consumerId;
	private String merchantReturnUrl;
	private String registrationDate;

	Map<String, String> aaData = new HashMap<String, String>();

	@SuppressWarnings("static-access")
	public String execute() {

		try {
			logger.info("Inside ENachAction execute function ");
			
			StringBuilder sb = new StringBuilder();
			String merchantId = PropertiesManager.propertiesMap.get(Constants.ICICI_ENACH_ACQUIRER_ID.getValue());
			String salt = PropertiesManager.propertiesMap.get(Constants.ICICI_ENACH_ACQUIRER_SALT.getValue());
			String itemId = PropertiesManager.propertiesMap.get(Constants.ICICI_ENACH_ITEM_ID.getValue());
			String deviceId = PropertiesManager.propertiesMap.get(Constants.ICICI_ENACH_DEVICE_ID.getValue());
			String amountType = PropertiesManager.propertiesMap.get(Constants.ICICI_ENACH_AMOUNT_TYPE.getValue());
			
			String currency = "INR";
			String pgRefNum = TransactionManager.getNewTransactionId();
			String txnId = TransactionManager.getNewTransactionId();
			String hash ="";
			String comAmt = "0";
			String month = "";
			String year = "";
			
			SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");
//			Date startDate = sdf.parse(debitStartDate);
//			Date endDate = sdf.parse(debitEndDate);
			Date regDate = sdf.parse(registrationDate);
			sdf = new SimpleDateFormat("dd-MM-yyyy");
//			debitStartDate = sdf.format(startDate);
//			debitEndDate = sdf.format(endDate);
			registrationDate = sdf.format(regDate);
			
			frequency = Frequency.getFrequencyCode(frequency);
			
			//T557156|ENI159471793|1|5857188112|257|9823394322|ysachitk@gmail.com|29-12-2020|12-01-2021|99999|M|MNTH|||||6916391056YFEHXG
			if (paymentMode.equalsIgnoreCase("netBanking") || paymentMode.equalsIgnoreCase("cards")) {
				
				cardNumber = "";
				expYear = "";
				expMonth = "";
				cvv = "";
				nameOnCard = "";
				
				sb.append(merchantId);
				sb.append("|");
				sb.append(pgRefNum);
				sb.append("|");
				sb.append(amount/*Amount.removeDecimalAmount(totalAmount, "356")*/);
				//sb.append("1"); total Amount
				sb.append("|");
				sb.append(accountNumber);
				sb.append("|");
				sb.append(txnId);
				sb.append("|");
				sb.append(consumerMobileNo);
				sb.append("|");
				sb.append(consumerEmailId);
				sb.append("|");
				//sb.append(DateTimeFormatter.ofPattern("dd-MM-yyyy", Locale.ENGLISH).format(debitStartDate));
				sb.append(registrationDate);
				sb.append("|");
				//sb.append(DateTimeFormatter.ofPattern("dd-MM-yyyy", Locale.ENGLISH).format(debitEndDate));
				sb.append(debitEndDate);
				sb.append("|");
				sb.append(totalAmount/*Amount.removeDecimalAmount(amount, "356")*/);
				//sb.append("1"); debit Amount
				sb.append("|");
				sb.append(amountType);
				sb.append("|");
				sb.append(frequency);
				sb.append("|");
				// cardNumber
				sb.append("|");
				// expMonth
				sb.append("|");
				// expYear
				sb.append("|");
				// cvv
				sb.append("|");
				sb.append(salt);
			}
			
			logger.info("pipe separated data : "+sb.toString());
			hash = Hasher.getHash(sb.toString());
			logger.info("hash genrated with pipe separated data : "+hash);
			
			User user = userDao.findPayId(payId);
			
			//for reseller sub merchant
			if(user.isSuperMerchant() == false && StringUtils.isNotBlank(user.getSuperMerchantId()) 
					&& StringUtils.isNotBlank(user.getResellerId())) {
				aaData.put(FieldType.PAY_ID.getName(), user.getSuperMerchantId());
				aaData.put(FieldType.SUB_MERCHANT_ID.getName(), payId);
				aaData.put(FieldType.RESELLER_ID.getName(), user.getResellerId());
			
			} else if (user.isSuperMerchant() == false && StringUtils.isNotBlank(user.getSuperMerchantId())) {
				//for Sub Merchant
				
				//super merchantId
				aaData.put(FieldType.PAY_ID.getName(), user.getSuperMerchantId());
				//sub MerchantId
				aaData.put(FieldType.SUB_MERCHANT_ID.getName(), payId);
			
			} else if(StringUtils.isNotBlank(user.getResellerId())) { 
				// Reseller Merchant
				aaData.put(FieldType.RESELLER_ID.getName(), user.getResellerId());
				aaData.put(FieldType.PAY_ID.getName(), payId);
				
			} else {
				//super merchantId
				aaData.put(FieldType.PAY_ID.getName(), payId);
				//sub MerchantId
				aaData.put(FieldType.SUB_MERCHANT_ID.getName(), "");
			}
			
			aaData.put(CrmFieldType.MERCHANT_ID.getName(), merchantId);
			aaData.put("SALT", salt);
			aaData.put("START_DATE", registrationDate);
			aaData.put("END_DATE", debitEndDate);
			aaData.put("DEBIT_START_DATE", debitStartDate);
			aaData.put(CrmFieldType.CURRENCY.getName(), currency);
			
			//Total Transaction Amount that are debited from customer account
			aaData.put(Constants.AMOUNT.getValue(), amount);
			//aaData.put(Constants.AMOUNT.getValue(), "1");
			//max amount field that are debited from customer account
			aaData.put("TRANSACTION_AMOUNT", maxAmount);
			aaData.put(FieldType.MAX_AMOUNT.getName(), totalAmount);
			//aaData.put(FieldType.MAX_AMOUNT.getName(), "1");
			aaData.put(FieldType.TOTAL_AMOUNT.getName(), totalAmount);
			aaData.put("PAYMENT_MODE", paymentMode);
			aaData.put("ACCOUNT_NUMBER", accountNumber);
			aaData.put(CrmFieldType.IFSC_CODE.getName(), ifscCode);
			aaData.put("ITEM_ID", itemId);
			aaData.put("ACCOUNT_TYPE", accountType);
			aaData.put("COM_AMT", comAmt);
			aaData.put("DEVICE_ID", deviceId);
			aaData.put("ACCOUNT_HOLDER_NAME", accountHolderName);
			aaData.put("CONSUMER_MOBILE_NO", consumerMobileNo);
			aaData.put("CONSUMER_EMAIL_ID", consumerEmailId);
			aaData.put("AMOUNT_TYPE", amountType);
			aaData.put(FieldType.FREQUENCY.getName(), frequency);
			aaData.put("CONSUMER_ID", consumerId);
			aaData.put("CARD_NUMBER", cardNumber);
			aaData.put("EXP_MONTH", month);
			aaData.put("EXP_YEAR", year);
			aaData.put("CVV", cvv);
			aaData.put("BANK_CODE", bankCode);
			aaData.put("Name_On_Card", nameOnCard);
			aaData.put("TENURE", tenure);
			if(paymentMode.equalsIgnoreCase("netBanking")) {
				aaData.put("BANK_NAME", EnachNBIssuerType.getIssuerName(bankCode));
			} else {
				aaData.put("BANK_NAME", EnachDCIssuerType.getIssuerName(bankCode));
			}
			//aaData.put(FieldType.MOP_TYPE.getName(), mopType);
			//aaData.put("MERCHANT_LOGO", merchantLogo);
			aaData.put("PAYMENTGATEWAY_LOGO", propertiesManager.propertiesMap.get(Constants.ICICI_ENACH_POP_LOGO.getValue()));
			aaData.put(FieldType.RETURN_URL.getName(), returnUrl);
			aaData.put("MERCHANT_RETURN_URL", merchantReturnUrl);
			aaData.put(FieldType.TXNTYPE.getName(), "Registration");
			aaData.put(FieldType.PG_REF_NUM.getName(), pgRefNum);
			aaData.put(FieldType.TXN_ID.getName(), txnId);
			aaData.put(Constants.TOKEN.getValue(), hash);
			Fields fields = new Fields(aaData);
			
			fieldsDao.insertEnachRegistrationDetail(fields);
		} catch (Exception ex) {
			logger.error("exception caught in eNachAction exception ",ex);
			return ERROR;
		}

		return SUCCESS;
	}
	
	public String getPaymentMode() {
		return paymentMode;
	}
	public void setPaymentMode(String paymentMode) {
		this.paymentMode = paymentMode;
	}
	
	public String getAccountNumber() {
		return accountNumber;
	}
	public void setAccountNumber(String accountNumber) {
		this.accountNumber = accountNumber;
	}
	public String getAccountType() {
		return accountType;
	}
	public void setAccountType(String accountType) {
		this.accountType = accountType;
	}
	public String getIfscCode() {
		return ifscCode;
	}
	public void setIfscCode(String ifscCode) {
		this.ifscCode = ifscCode;
	}
	public Map<String, String> getAaData() {
		return aaData;
	}
	public void setAaData(Map<String, String> aaData) {
		this.aaData = aaData;
	}
	public String getAccountHolderName() {
		return accountHolderName;
	}
	public void setAccountHolderName(String accountHolderName) {
		this.accountHolderName = accountHolderName;
	}
	public String getConsumerMobileNo() {
		return consumerMobileNo;
	}
	public void setConsumerMobileNo(String consumerMobileNo) {
		this.consumerMobileNo = consumerMobileNo;
	}
	public String getConsumerEmailId() {
		return consumerEmailId;
	}
	public void setConsumerEmailId(String consumerEmailId) {
		this.consumerEmailId = consumerEmailId;
	}
	public String getFrequency() {
		return frequency;
	}
	public void setFrequency(String frequency) {
		this.frequency = frequency;
	}
	public String getAmount() {
		return amount;
	}
	public void setAmount(String amount) {
		this.amount = amount;
	}
	public String getPayId() {
		return payId;
	}
	public void setPayId(String payId) {
		this.payId = payId;
	}
	public String getDebitStartDate() {
		return debitStartDate;
	}
	public void setDebitStartDate(String debitStartDate) {
		this.debitStartDate = debitStartDate;
	}
	public String getDebitEndDate() {
		return debitEndDate;
	}
	public void setDebitEndDate(String debitEndDate) {
		this.debitEndDate = debitEndDate;
	}
	public String getBankCode() {
		return bankCode;
	}
	public void setBankCode(String bankCode) {
		this.bankCode = bankCode;
	}
	public String getTenure() {
		return tenure;
	}
	public void setTenure(String tenure) {
		this.tenure = tenure;
	}
	public String getBankName() {
		return bankName;
	}

	public void setBankName(String bankName) {
		this.bankName = bankName;
	}
	public String getMerchantLogo() {
		return merchantLogo;
	}

	public void setMerchantLogo(String merchantLogo) {
		this.merchantLogo = merchantLogo;
	}

	public String getReturnUrl() {
		return returnUrl;
	}

	public void setReturnUrl(String returnUrl) {
		this.returnUrl = returnUrl;
	}
	public String getConsumerId() {
		return consumerId;
	}

	public void setConsumerId(String consumerId) {
		this.consumerId = consumerId;
	}
	public String getMaxAmount() {
		return maxAmount;
	}

	public void setMaxAmount(String maxAmount) {
		this.maxAmount = maxAmount;
	}

	public String getTotalAmount() {
		return totalAmount;
	}

	public void setTotalAmount(String totalAmount) {
		this.totalAmount = totalAmount;
	}
	public String getSubMerchantPayId() {
		return subMerchantPayId;
	}

	public void setSubMerchantPayId(String subMerchantPayId) {
		this.subMerchantPayId = subMerchantPayId;
	}
	public String getMerchantReturnUrl() {
		return merchantReturnUrl;
	}

	public void setMerchantReturnUrl(String merchantReturnUrl) {
		this.merchantReturnUrl = merchantReturnUrl;
	}
	public String getRegistrationDate() {
		return registrationDate;
	}

	public void setRegistrationDate(String registrationDate) {
		this.registrationDate = registrationDate;
	}
}