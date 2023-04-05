package com.paymentgateway.crm.action;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.struts2.interceptor.validation.SkipValidation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.paymentgateway.commons.api.Hasher;
import com.paymentgateway.commons.api.TransactionControllerServiceProvider;
import com.paymentgateway.commons.dao.UserSettingDao;
import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.user.TransactionSearch;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.user.UserSettingData;
import com.paymentgateway.commons.user.UserType;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.CrmFieldConstants;
import com.paymentgateway.commons.util.CrmFieldType;
import com.paymentgateway.commons.util.CrmValidator;
import com.paymentgateway.commons.util.DataEncoder;
import com.paymentgateway.commons.util.DateCreater;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.crm.actionBeans.SessionUserIdentifier;
import com.paymentgateway.crm.mongoReports.TxnReports;

public class TransactionSearchAction extends AbstractSecureAction {

	@Autowired
	private CrmValidator validator;

	@Autowired
	private DataEncoder encoder;

	@Autowired
	private TxnReports txnReports;

	@Autowired
	private UserDao userDao;
	
	@Autowired
	private TransactionControllerServiceProvider transactionControllerServiceProvider;

	@Autowired
	private UserSettingDao userSettingDao;

	@Autowired
	private SessionUserIdentifier userIdentifier;
	private static Logger logger = LoggerFactory.getLogger(TransactionSearchAction.class.getName());

	private static final long serialVersionUID = -6919220389124792416L;

	private String transactionId;
	private String orderId;
	private String custId;
	private String customerEmail;
	private String merchantEmailId;
	private String paymentType;
	private String paymentRegion;
	private String cardNumber;
	private String status;
	private String rrn;
	private String currency;
	private String dateFrom;
	private String dateTo;
	private int draw;
	private int length;
	private int start;
	private String transactionType;
	private String subMerchantEmailId;
	private BigInteger recordsTotal;
	public BigInteger recordsFiltered;
	private Boolean searchFlag;
	public boolean retailMerchantFlag;
	private Set<String> orderIdSet;
	private List<TransactionSearch> aaData;
	private User sessionUser = new User();
	private String transactionFlag;
	
	private String payId;
	private String pgRefNum;
	private String response;
	private String responseMsg;

	public String execute() {

		logger.info("Inside TransactionSearchAction, execute()");
		int totalCount;
		sessionUser = (User) sessionMap.get(Constants.USER.getValue());
		UserSettingData userSettings=(UserSettingData) sessionMap.get(Constants.USER_SETTINGS);
		
		setDateFrom(DateCreater.toDateTimeformatCreater(dateFrom));
		setDateTo(DateCreater.formDateTimeformatCreater(dateTo));
		try {

			String merchantPayId = "";
			String subMerchantPayId = "";
			if (sessionUser.isSuperMerchant() == false && StringUtils.isNotBlank(sessionUser.getSuperMerchantId())) {
				merchantPayId = sessionUser.getSuperMerchantId();
				subMerchantPayId = sessionUser.getPayId();
			} else {
				merchantPayId = userIdentifier.getMerchantPayId(sessionUser, merchantEmailId);

				if (StringUtils.isNotBlank(subMerchantEmailId) && !subMerchantEmailId.equalsIgnoreCase("All")) {
					subMerchantPayId = userDao.getPayIdByEmailId(subMerchantEmailId);
				}
			}

			logger.info("Inside TransactionSearchAction , merchantPayId = " + merchantPayId);
			if (sessionUser.getUserType().equals(UserType.RESELLER)) {
				totalCount = txnReports.searchPaymentCount(getTransactionId(), getOrderId(), getCustomerEmail(), "",
						merchantPayId, "", getPaymentType(), getStatus(), getCurrency(), getTransactionType(),
						getDateFrom(), getDateTo(), sessionUser, "ALL", getPaymentRegion(), getSearchFlag(), "", null, false, transactionFlag, "", getRrn());
				BigInteger bigInt = BigInteger.valueOf(totalCount);
				setRecordsTotal(bigInt);
				if (getLength() == -1) {
					setLength(getRecordsTotal().intValue());
				}

				aaData = encoder.encodeTransactionSearchObj(txnReports.searchPayment(getTransactionId(), getOrderId(),
						getCustomerEmail(), "", merchantPayId, "", getPaymentType(), getStatus(), getCurrency(),
						getTransactionType(), getDateFrom(), getDateTo(), sessionUser, getStart(), getLength(), "ALL",
						getPaymentRegion(), getSearchFlag(), "", null, false, transactionFlag, "", getRrn()));
				recordsFiltered = recordsTotal;
			}

			else if (sessionUser.getUserType().equals(UserType.MERCHANT)) {

				totalCount = txnReports.searchPaymentCount(getTransactionId(), getOrderId(), getCustomerEmail(),
						"", merchantPayId, subMerchantPayId, getPaymentType(), getStatus(),
						getCurrency(), getTransactionType(), getDateFrom(), getDateTo(), sessionUser, "ALL",
						getPaymentRegion(), getSearchFlag(), "", null, false, transactionFlag, "", getRrn());
				BigInteger bigInt = BigInteger.valueOf(totalCount);
				setRecordsTotal(bigInt);
				if (getLength() == -1) {
					setLength(getRecordsTotal().intValue());
				}
				aaData = encoder.encodeTransactionSearchObj(txnReports.searchPayment(getTransactionId(), getOrderId(),
						getCustomerEmail(), "", merchantPayId, subMerchantPayId, getPaymentType(), getStatus(),
						getCurrency(), getTransactionType(), getDateFrom(), getDateTo(), sessionUser, getStart(),
						getLength(), "ALL", getPaymentRegion(), getSearchFlag(), "", null, false, transactionFlag, "", getRrn()));
				recordsFiltered = recordsTotal;

			} else if (sessionUser.getUserType().equals(UserType.SUBUSER)) {

				String subUserId = "";
				if (!userDao.isSubUserPrevilageTypeAll(sessionUser)) {
					subUserId = sessionUser.getPayId();
				}
				
				User user = userDao.findPayId(sessionUser.getParentPayId());
				
				if(user.isSuperMerchant() == false && StringUtils.isNotBlank(user.getSuperMerchantId())) {
					merchantPayId = user.getSuperMerchantId();
					subMerchantPayId = user.getPayId();
				}else {
					merchantPayId = user.getPayId();
				}
				
				if (!StringUtils.isEmpty(sessionUser.getSubUserType())
						&& sessionUser.getSubUserType().equalsIgnoreCase("ePosType")) {
					
					orderIdSet = txnReports.findBySubuserId(subUserId, sessionUser.getParentPayId());
					
					boolean isPgfNumber = txnReports.getPgfNumberForeposSubuser(orderIdSet, getTransactionId());
					
					boolean orderIdflag = false;
					for(String eposTxnOrderId : orderIdSet) {
						if(StringUtils.isNotEmpty(getOrderId()) && eposTxnOrderId.equalsIgnoreCase(getOrderId())) {
							setOrderId(eposTxnOrderId);
							orderIdflag = true;
							break;
						}
					}
					
					if(!orderIdflag) {
						if(!getOrderId().isEmpty())
							setOrderId(" ");
					}
					if(!isPgfNumber) {
						if(!getTransactionId().isEmpty())
							setTransactionId(" ");
					}
					
					sessionUser = user;
					
					totalCount = txnReports.searchPaymentCount(getTransactionId(), getOrderId(), getCustomerEmail(), "",
							merchantPayId, subMerchantPayId, getPaymentType(), getStatus(), getCurrency(),
							getTransactionType(), getDateFrom(), getDateTo(), sessionUser, "ALL", getPaymentRegion(),
							getSearchFlag(), "", orderIdSet, false, transactionFlag, "", getRrn());
					BigInteger bigInt = BigInteger.valueOf(totalCount);
					setRecordsTotal(bigInt);
					if (getLength() == -1) {
						setLength(getRecordsTotal().intValue());
					}
					aaData = encoder.encodeTransactionSearchObj(txnReports.searchPayment(getTransactionId(),
							getOrderId(), getCustomerEmail(), "", merchantPayId, subMerchantPayId, getPaymentType(),
							getStatus(), getCurrency(), getTransactionType(), getDateFrom(), getDateTo(), sessionUser,
							getStart(), getLength(), "ALL", getPaymentRegion(), getSearchFlag(), "", orderIdSet, false, transactionFlag, "", getRrn()));
					recordsFiltered = recordsTotal;

				} else if (!StringUtils.isEmpty(sessionUser.getSubUserType())
						&& sessionUser.getSubUserType().equalsIgnoreCase("normalType")) {
					
					sessionUser = user;

					totalCount = txnReports.searchPaymentCount(getTransactionId(), getOrderId(), getCustomerEmail(), "",
							merchantPayId, subMerchantPayId, getPaymentType(), getStatus(), getCurrency(),
							getTransactionType(), getDateFrom(), getDateTo(), sessionUser, "ALL", getPaymentRegion(),
							getSearchFlag(), "", null, false, transactionFlag, "", getRrn());
					BigInteger bigInt = BigInteger.valueOf(totalCount);
					setRecordsTotal(bigInt);
					if (getLength() == -1) {
						setLength(getRecordsTotal().intValue());
					}
					aaData = encoder.encodeTransactionSearchObj(txnReports.searchPayment(getTransactionId(),
							getOrderId(), getCustomerEmail(), "", merchantPayId, subMerchantPayId, getPaymentType(),
							getStatus(), getCurrency(), getTransactionType(), getDateFrom(), getDateTo(), sessionUser,
							getStart(), getLength(), "ALL", getPaymentRegion(), getSearchFlag(), "", null, false, transactionFlag, "", getRrn()));
					recordsFiltered = recordsTotal;
				}
				
				// Changed by Shaiwal for handling Sub User login when user setting is not present for SubUser
				logger.info("Session User is " + sessionUser.getUserType());
				
				if(sessionUser.getUserType().equals(UserType.SUBUSER)){
					
					String parentMerchantPayId = sessionUser.getParentPayId();
					User parentMerchant = userDao
							.findPayId(parentMerchantPayId);
					setRetailMerchantFlag(parentMerchant.isRetailMerchantFlag());
				}
				else {
					
					if (userSettings != null) {
						setRetailMerchantFlag(userSettings.isRetailMerchantFlag());
					}
					else {
						setRetailMerchantFlag(false);
					}
					
				}
				
				//setRetailMerchantFlag(userSettings.isRetailMerchantFlag());
				if (aaData == null) {
					aaData = new ArrayList<TransactionSearch>();
				}

			} else {
				totalCount = txnReports.searchPaymentCount(getTransactionId(), getOrderId(), getCustomerEmail(), "",
						merchantPayId, subMerchantPayId, getPaymentType(), getStatus(), getCurrency(),
						getTransactionType(), getDateFrom(), getDateTo(), sessionUser, "ALL", getPaymentRegion(),
						getSearchFlag(), "", null, false, transactionFlag, "", getRrn());
				BigInteger bigInt = BigInteger.valueOf(totalCount);
				setRecordsTotal(bigInt);
				if (getLength() == -1) {
					setLength(getRecordsTotal().intValue());
				}
				aaData = encoder.encodeTransactionSearchObj(txnReports.searchPayment(getTransactionId(), getOrderId(),
						getCustomerEmail(), "", merchantPayId, subMerchantPayId, getPaymentType(), getStatus(),
						getCurrency(), getTransactionType(), getDateFrom(), getDateTo(), sessionUser, getStart(),
						getLength(), "ALL", getPaymentRegion(), getSearchFlag(), "", null, false, transactionFlag, "", getRrn()));
				recordsFiltered = recordsTotal;
			}

		} catch (Exception exception) {
			logger.error("Exception", exception);
			return ERROR;
		}
		return SUCCESS;

	}
	
	public String getTransactionStatus() {
		try {
			logger.info("inside getTransactionStatus from CRM");

			if (StringUtils.isNotBlank(pgRefNum)) {
				logger.info("inside getTransactionStatus() ");

				Map<String, String> requestMap = new HashMap<String, String>();
				requestMap.put(FieldType.PG_REF_NUM.getName(), pgRefNum);
				Map<String, String> response = transactionControllerServiceProvider.getTransactionStatus(requestMap);
				String responseStatus = response.get("STATUS");
				logger.info("Response recieved from api >>> " + response.toString());
				if (StringUtils.isNotBlank(responseStatus) && responseStatus.equalsIgnoreCase("Captured")) {
					setResponse("SUCCESS");
				} else {
					setResponse("FAILED");
				}
				if (StringUtils.isNotBlank(responseStatus)){
					setResponseMsg(responseStatus);					
				} else {
					setResponseMsg("Status not found");
				}
				
			} else {
				setResponse("FAILED");
				setResponseMsg("pgRefNum is empty");
			}

		} catch (Exception e) {
			logger.info("Exception in sendTransactionCallback() ", e);
			setResponse("FAILED");
			setResponseMsg(ErrorType.INTERNAL_SYSTEM_ERROR.getInternalMessage());
		}
		return SUCCESS;
	}
	
	@SkipValidation
	public String sendTransactionCallback() {
		try {
			logger.info("inside sendTransactionCallback");

			if (StringUtils.isNotBlank(orderId) && StringUtils.isNotBlank(payId)) {
				Map<String, String> requestMap = new HashMap<String, String>();
				requestMap.put(FieldType.PAY_ID.getName(), payId);
				requestMap.put(FieldType.ORDER_ID.getName(), orderId);
				requestMap.put(FieldType.HASH.getName(), Hasher.getHash(new Fields(requestMap)));
				
				logger.info("Sending callback from CRM for Order Id : "+orderId + " And PayId : " + payId);
				
				String responseMsg = transactionControllerServiceProvider.sendCallbackToMerchant(requestMap);

				if (StringUtils.isNotBlank(responseMsg) && responseMsg.equalsIgnoreCase("DONE")) {
					setResponse("SUCCESS");
					setResponseMsg("Callback Sent Successfully");
				} else {
					setResponse("FAILED");
					setResponseMsg("unable to send callback");
				}
			} else {
				setResponse("FAILED");
				setResponseMsg("OrderId or PayId empty");
			}

		} catch (Exception e) {
			logger.info("Exception in sendTransactionCallback() ", e);
			setResponse("FAILED");
			setResponseMsg(ErrorType.INTERNAL_SYSTEM_ERROR.getInternalMessage());
		}
		return SUCCESS;
	}

	public void validate() {

		if (validator.validateBlankField(getTransactionId())) {
		} else if (!validator.validateField(CrmFieldType.TRANSACTION_ID, getTransactionId())) {
			addFieldError(CrmFieldType.TRANSACTION_ID.getName(), ErrorType.INVALID_FIELD.getResponseMessage());
		}

		if (validator.validateBlankField(getOrderId())) {
		} else if (!validator.validateField(CrmFieldType.ORDER_ID, getOrderId())) {
			addFieldError(CrmFieldType.ORDER_ID.getName(), ErrorType.INVALID_FIELD.getResponseMessage());
		}

		if (validator.validateBlankField(getCustId())) {
		} else if (!validator.validateField(CrmFieldType.CUST_ID, getCustId())) {
			addFieldError(CrmFieldType.CUST_ID.getName(), ErrorType.INVALID_FIELD.getResponseMessage());
		}

		if (validator.validateBlankField(getCustomerEmail())) {
		} else if (!validator.validateField(CrmFieldType.CUSTOMER_EMAIL_ID, getCustomerEmail())) {
			addFieldError(CrmFieldType.CUSTOMER_EMAIL_ID.getName(), ErrorType.INVALID_FIELD.getResponseMessage());
		}

		if (validator.validateBlankField(getMerchantEmailId())
				|| getMerchantEmailId().equals(CrmFieldConstants.ALL.getValue())) {
		} else if (!validator.validateField(CrmFieldType.MERCHANT_EMAIL_ID, getMerchantEmailId())) {
			addFieldError(CrmFieldType.MERCHANT_EMAIL_ID.getName(), ErrorType.INVALID_FIELD.getResponseMessage());
		}

		if (validator.validateBlankField(getPaymentType())) {
		} else if (!validator.validateField(CrmFieldType.PAYMENT_TYPE, getPaymentType())) {
			addFieldError(CrmFieldType.PAYMENT_TYPE.getName(), ErrorType.INVALID_FIELD.getResponseMessage());
		}

		if (validator.validateBlankField(getCardNumber())) {
		} else if (!validator.validateField(CrmFieldType.CARD_NUMBER_MASK, getCardNumber())) {
			addFieldError(CrmFieldType.CARD_NUMBER_MASK.getName(), ErrorType.INVALID_FIELD.getResponseMessage());
		}

		/*
		 * if (validator.validateBlankField(getStatus())) { } else if
		 * (!validator.validateField(CrmFieldType.TXN_STATUS, getStatus())) {
		 * addFieldError(CrmFieldType.TXN_STATUS.getName(),
		 * ErrorType.INVALID_FIELD.getResponseMessage()); }
		 */

		if (validator.validateBlankField(getCurrency())) {
		} else if (!validator.validateField(CrmFieldType.CURRENCY, getCurrency())) {
			addFieldError(CrmFieldType.CURRENCY.getName(), ErrorType.INVALID_FIELD.getResponseMessage());
		}

		if (validator.validateBlankField(getDateFrom())) {
		} else if (!validator.validateField(CrmFieldType.DATE_FROM, getDateFrom())) {
			addFieldError(CrmFieldType.DATE_FROM.getName(), ErrorType.INVALID_FIELD.getResponseMessage());
		}

		if (validator.validateBlankField(getDateTo())) {
		} else if (!validator.validateField(CrmFieldType.DATE_TO, getDateTo())) {
			addFieldError(CrmFieldType.DATE_TO.getName(), ErrorType.INVALID_FIELD.getResponseMessage());
		}

		if (!validator.validateBlankField(getDateTo())) {
			if (DateCreater.formatStringToDate(DateCreater.formatFromDate(getDateFrom()))
					.compareTo(DateCreater.formatStringToDate(DateCreater.formatFromDate(getDateTo()))) > 0) {
				addFieldError(CrmFieldType.DATE_FROM.getName(), CrmFieldConstants.FROMTO_DATE_VALIDATION.getValue());
			} else if (DateCreater.diffDate(getDateFrom(), getDateTo()) > 31) {
				addFieldError(CrmFieldType.DATE_FROM.getName(), CrmFieldConstants.DATE_RANGE.getValue());
			}
		}
	}

	public List<TransactionSearch> getaaData() {
		return aaData;
	}

	public void setaaData(List<TransactionSearch> setaaData) {
		this.aaData = setaaData;
	}

	public String getTransactionId() {
		return transactionId;
	}

	public void setTransactionId(String transactionId) {
		this.transactionId = transactionId;
	}

	public String getOrderId() {
		return orderId;
	}

	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}

	public String getCustId() {
		return custId;
	}

	public void setCustId(String custId) {
		this.custId = custId;
	}

	public String getCustomerEmail() {
		return customerEmail;
	}

	public void setCustomerEmail(String customerEmail) {
		this.customerEmail = customerEmail;
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

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

	public String getDateFrom() {
		return dateFrom;
	}

	public void setDateFrom(String dateFrom) {
		this.dateFrom = dateFrom;
	}

	public String getDateTo() {
		return dateTo;
	}

	public void setDateTo(String dateTo) {
		this.dateTo = dateTo;
	}

	public String getMerchantEmailId() {
		return merchantEmailId;
	}

	public void setMerchantEmailId(String merchantEmailId) {
		this.merchantEmailId = merchantEmailId;
	}

	public String getPaymentType() {
		return paymentType;
	}

	public void setPaymentType(String paymentType) {
		this.paymentType = paymentType;
	}

	public int getDraw() {
		return draw;
	}

	public void setDraw(int draw) {
		this.draw = draw;
	}

	public int getLength() {
		return length;
	}

	public void setLength(int length) {
		this.length = length;
	}

	public int getStart() {
		return start;
	}

	public void setStart(int start) {
		this.start = start;
	}

	public BigInteger getRecordsTotal() {
		return recordsTotal;
	}

	public void setRecordsTotal(BigInteger recordsTotal) {
		this.recordsTotal = recordsTotal;
	}

	public BigInteger getRecordsFiltered() {
		return recordsFiltered;
	}

	public void setRecordsFiltered(BigInteger recordsFiltered) {
		this.recordsFiltered = recordsFiltered;
	}

	public String getTransactionType() {
		return transactionType;
	}

	public void setTransactionType(String transactionType) {
		this.transactionType = transactionType;
	}

	public String getSubMerchantEmailId() {
		return subMerchantEmailId;
	}

	public void setSubMerchantEmailId(String subMerchantEmailId) {
		this.subMerchantEmailId = subMerchantEmailId;
	}

	public String getPaymentRegion() {
		return paymentRegion;
	}

	public void setPaymentRegion(String paymentRegion) {
		this.paymentRegion = paymentRegion;
	}

	public Boolean getSearchFlag() {
		return searchFlag;
	}

	public void setSearchFlag(Boolean searchFlag) {
		this.searchFlag = searchFlag;
	}

	public Set<String> getOrderIdSet() {
		return orderIdSet;
	}

	public void setOrderIdSet(Set<String> orderIdSet) {
		this.orderIdSet = orderIdSet;
	}

	public boolean isRetailMerchantFlag() {
		return retailMerchantFlag;
	}

	public void setRetailMerchantFlag(boolean retailMerchantFlag) {
		this.retailMerchantFlag = retailMerchantFlag;
	}
	public String getTransactionFlag() {
		return transactionFlag;
	}

	public void setTransactionFlag(String transactionFlag) {
		this.transactionFlag = transactionFlag;
	}

	public String getPgRefNum() {
		return pgRefNum;
	}

	public void setPgRefNum(String pgRefNum) {
		this.pgRefNum = pgRefNum;
	}

	public String getResponse() {
		return response;
	}

	public void setResponse(String response) {
		this.response = response;
	}

	public String getResponseMsg() {
		return responseMsg;
	}

	public void setResponseMsg(String responseMsg) {
		this.responseMsg = responseMsg;
	}

	public String getPayId() {
		return payId;
	}

	public void setPayId(String payId) {
		this.payId = payId;
	}

	public String getRrn() {
		return rrn;
	}

	public void setRrn(String rrn) {
		this.rrn = rrn;
	}
	
}
