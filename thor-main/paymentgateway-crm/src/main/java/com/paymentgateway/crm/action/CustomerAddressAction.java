package com.paymentgateway.crm.action;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.paymentgateway.commons.dao.TransactionSearchServiceMongo;
import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.user.CustomerAddress;
import com.paymentgateway.commons.util.CrmFieldConstants;
import com.paymentgateway.commons.util.CrmFieldType;
import com.paymentgateway.commons.util.CrmValidator;
import com.paymentgateway.commons.util.DataEncoder;



public class CustomerAddressAction extends AbstractSecureAction {

	private static final long serialVersionUID = 2845115077638261715L;


	private static Logger logger = LoggerFactory.getLogger(CustomerAddressAction.class.getName());
	
		
	private String orderId;
	private CustomerAddress aaData;
	private String status;
	private String custName;
	private String custPhone;
	private String custEmail;
	private String datefrom;
	private String amount;
	private String currency;
	private String productDesc;
	private String internalRequestDesc;
	private String transactionId;
	private String cardNumber;
	private String paymentMethod;
	private String mopType;
	private String internalCardIssusserBank;
	private String internalCardIssusserCountry;

	//To display message recieved from PG
	private String pgTxnMessage;

    //used to display the request string on jsp
	private Boolean showRequestFlag;

	//used to display the request string on jsp
	private Boolean transactionAuthenticationFlag;
	@Autowired
	private TransactionSearchServiceMongo transactionSearchServiceMongo;
	// to show fraud reason exclusively on popup
	private String responseMsg;
	
	public String execute() {
		DataEncoder encoder = new DataEncoder();
	
		try {						
			setAaData(encoder.encodeCustomerAddressObj(transactionSearchServiceMongo.getDumyCustAddress (
					getTransactionId())));
			return SUCCESS;
		} catch (Exception exception) {
			logger.error("Exception", exception);
			return ERROR;
		}
	}

	public void validate(){
		CrmValidator validator = new CrmValidator();
		
		if(validator.validateBlankField(getTransactionId())){
		}
		else if(!validator.validateField(CrmFieldType.TRANSACTION_ID, getTransactionId())){
			addFieldError(CrmFieldType.TRANSACTION_ID.getName(), ErrorType.INVALID_FIELD.getResponseMessage());
		}
			
		if(validator.validateBlankField(getCardNumber()) || getCardNumber().equals("Net-Banking Transaction") 
				|| getCardNumber().equals("Wallet Transaction")|| getCardNumber().equals(CrmFieldConstants.NOT_AVAILABLE.getValue())){
		}
		else if(!validator.validateField(CrmFieldType.CARD_NUMBER_MASK, getCardNumber())){
        	addFieldError(CrmFieldType.CARD_NUMBER_MASK.getName(), ErrorType.INVALID_FIELD.getResponseMessage());
		}
	
		if(validator.validateBlankField(getAmount())){
		}
        else if(!validator.validateField(CrmFieldType.AMOUNT, getAmount())){
        	addFieldError(CrmFieldType.CURRENCY.getName(), ErrorType.INVALID_FIELD.getResponseMessage());
		}
		
		if(validator.validateBlankField(getOrderId())){
		}
		else if(!validator.validateField(CrmFieldType.ORDER_ID, getOrderId())){
			addFieldError(CrmFieldType.ORDER_ID.getName(), ErrorType.INVALID_FIELD.getResponseMessage());
		}
		
		if(validator.validateBlankField(getStatus())){
		}
		else if(!validator.validateField(CrmFieldType.TXN_STATUS, getStatus())){
        	addFieldError(CrmFieldType.TXN_STATUS.getName(), ErrorType.INVALID_FIELD.getResponseMessage());
		}
		
		if(validator.validateBlankField(getTransactionId())){
		}
		else if(!validator.validateField(CrmFieldType.TRANSACTION_ID, getTransactionId())){
			addFieldError(CrmFieldType.TRANSACTION_ID.getName(), ErrorType.INVALID_FIELD.getResponseMessage());
		}
		
		if(validator.validateBlankField(getOrderId())){
		}
		else if(!validator.validateField(CrmFieldType.ORDER_ID, getOrderId())){
			addFieldError(CrmFieldType.ORDER_ID.getName(), ErrorType.INVALID_FIELD.getResponseMessage());
		}

		//TODO remove hardcoding by changing fieldName on jsp and other places
		if(validator.validateBlankField(getCustEmail()) || (getCustEmail().equals(CrmFieldConstants.NOT_AVAILABLE.getValue()))){
		}
        else if(!validator.validateField(CrmFieldType.CUSTOMER_EMAIL_ID, getCustEmail())){
        	addFieldError("custEmail", ErrorType.INVALID_FIELD.getResponseMessage());
		}		
		if(validator.validateBlankField(getDatefrom())){
	        }
	    else if(!validator.validateField(CrmFieldType.DATE_FROM, getDatefrom())){
	       	addFieldError("datefrom", ErrorType.INVALID_FIELD.getResponseMessage());
		}
		if(validator.validateBlankField(getCustPhone())){
		}
		else if(!validator.validateField(CrmFieldType.MOBILE, getCustPhone())){
        	addFieldError("custPhone", ErrorType.INVALID_FIELD.getResponseMessage());
		}

		if(validator.validateBlankField(getCustName())){
		}
		else if(!validator.validateField(CrmFieldType.FIRSTNAME, getCustName())){
        	addFieldError("custName", ErrorType.INVALID_FIELD.getResponseMessage());
		}

		if(validator.validateBlankField(getProductDesc())){			
		}else if(!validator.validateField(CrmFieldType.GENERAL_STRING,getProductDesc())){
			addFieldError("productDesc", ErrorType.INVALID_FIELD.getResponseMessage());
		}

		//request description is an echo back field we can skip validation for this
		if(validator.validateBlankField(getInternalRequestDesc())){
		}
		//End hard coded fields
	}

	public String getOrderId() {
		return orderId;
	}

	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}

	public CustomerAddress getAaData() {
		return aaData;
	}

	public void setAaData(CustomerAddress aaData) {
		this.aaData = aaData;
	}

	public String getCustName() {
		return custName;
	}

	public void setCustName(String custName) {
		this.custName = custName;
	}

	public String getCustPhone() {
		return custPhone;
	}

	public void setCustPhone(String custPhone) {
		this.custPhone = custPhone;
	}

	public String getCustEmail() {
		return custEmail;
	}

	public void setCustEmail(String custEmail) {
		this.custEmail = custEmail;
	}

	public String getDatefrom() {
		return datefrom;
	}

	public void setDatefrom(String datefrom) {
		this.datefrom = datefrom;
	}

	public String getAmount() {
		return amount;
	}

	public void setAmount(String amount) {
		this.amount = amount;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

	public String getProductDesc() {
		return productDesc;
	}

	public void setProductDesc(String productDesc) {
		this.productDesc = productDesc;
	}

	public String getInternalRequestDesc() {
		return internalRequestDesc;
	}

	public void setInternalRequestDesc(String internalRequestDesc) {
		this.internalRequestDesc = internalRequestDesc;
	}

	public String getTransactionId() {
		return transactionId;
	}

	public void setTransactionId(String transactionId) {
		this.transactionId = transactionId;
	}

	public String getCardNumber() {
		return cardNumber;
	}

	public void setCardNumber(String cardNumber) {
		this.cardNumber = cardNumber;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Boolean getShowRequestFlag() {
		return showRequestFlag;
	}

	public void setShowRequestFlag(Boolean showRequestFlag) {
		this.showRequestFlag = showRequestFlag;
	}

	public Boolean getTransactionAuthenticationFlag() {
		return transactionAuthenticationFlag;
	}

	public void setTransactionAuthenticationFlag(
			Boolean transactionAuthenticationFlag) {
		this.transactionAuthenticationFlag = transactionAuthenticationFlag;
	}

	public String getPaymentMethod() {
		return paymentMethod;
	}

	public void setPaymentMethod(String paymentMethod) {
		this.paymentMethod = paymentMethod;
	}

	public String getMopType() {
		return mopType;
	}

	public void setMopType(String mopType) {
		this.mopType = mopType;
	}

	public String getInternalCardIssusserBank() {
		return internalCardIssusserBank;
	}

	public void setInternalCardIssusserBank(String internalCardIssusserBank) {
		this.internalCardIssusserBank = internalCardIssusserBank;
	}

	public String getInternalCardIssusserCountry() {
		return internalCardIssusserCountry;
	}

	public void setInternalCardIssusserCountry(String internalCardIssusserCountry) {
		this.internalCardIssusserCountry = internalCardIssusserCountry;
	}

	public String getPgTxnMessage() {
		return pgTxnMessage;
	}

	public void setPgTxnMessage(String pgTxnMessage) {
		this.pgTxnMessage = pgTxnMessage;
	}

	public String getResponseMsg() {
		return responseMsg;
	}
	
	public void setResponseMsg(String responseMsg) {
		this.responseMsg = responseMsg;
	}
}
