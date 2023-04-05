package com.paymentgateway.crm.action;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.paymentgateway.commons.dao.ChargebackDao;
import com.paymentgateway.commons.dao.UserSettingDao;
import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.user.Chargeback;
import com.paymentgateway.commons.user.Merchants;
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
import com.paymentgateway.commons.util.StatusType;
import com.paymentgateway.commons.util.TransactionType;
import com.paymentgateway.crm.actionBeans.SessionUserIdentifier;
import com.paymentgateway.crm.mongoReports.TxnReports;

public class SaleTransactionSearchAction extends AbstractSecureAction {

	@Autowired
	private CrmValidator validator;

	@Autowired
	private DataEncoder encoder;

	@Autowired
	private TxnReports txnReports;

	@Autowired
	private ChargebackDao chargebackDao;

	@Autowired
	private UserDao userDao;
	
	@Autowired
	private UserSettingDao userSettingDao;


	@Autowired
	private SessionUserIdentifier userIdentifier;
	private static Logger logger = LoggerFactory.getLogger(SaleTransactionSearchAction.class.getName());

	private static final long serialVersionUID = -6919220389124792416L;
	private String pgRefNum;
	private String payId;
	private String transactionId;
	private String orderId;
	private String customerEmail;
	private String SKUCode;
	private String subMerchantEmailId;
	private String merchantEmailId;
	private String paymentType;
	private String cardNumber;
	private String status;
	private String currency;
	private String partSettleFlag;
	private String dateFrom;
	private String dateTo;
	private int draw;
	private int length;
	private int start;
	private String custId;
	private String transactionType;
	private BigInteger recordsTotal;
	public BigInteger recordsFiltered;
	public String deliveryStatus;
	public String subUserPayId;
	public boolean retailMerchantFlag;
	private Set<String> orderIdSet;
	private List<TransactionSearch> aaData;
	private User sessionUser = new User();
	private String transactionFlag;

	@SuppressWarnings("unchecked")
	public String execute() {
		List<Chargeback> oldchargeback = new ArrayList<Chargeback>();
		setTransactionType(TransactionType.SALE.getName());
		setStatus(StatusType.CAPTURED.getName());
		logger.info("Inside TransactionSearchAction , execute()");
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
				//setRetailMerchantFlag(userSettings.isRetailMerchantFlag());
				
				// Changed by Shaiwal for handling Sub User login when user setting is not present for SubUser
				if(sessionUser.getUserType().equals(UserType.SUBUSER)){
					
					String parentMerchantPayId = sessionUser.getParentPayId();
					User parentMerchant = userDao
							.findPayId(parentMerchantPayId);
					setRetailMerchantFlag(parentMerchant.isRetailMerchantFlag());
				}
				else {
					setRetailMerchantFlag(userSettings.isRetailMerchantFlag());
				}
				
			} else {
				merchantPayId = userIdentifier.getMerchantPayId(sessionUser, merchantEmailId);

				if (StringUtils.isNotBlank(subMerchantEmailId) && !subMerchantEmailId.equalsIgnoreCase("All")) {
					User mechnt = userDao.findByEmailId(subMerchantEmailId);
					if (mechnt != null) {
						subMerchantPayId = mechnt.getPayId();
						UserSettingData merchantSettings=userSettingDao.fetchDataUsingPayId(subMerchantPayId);
						setRetailMerchantFlag(merchantSettings.isRetailMerchantFlag());
					}
				}
			}

			if (StringUtils.isNotEmpty(subUserPayId) && !subUserPayId.equalsIgnoreCase("ALL")) {
				User subuser = userDao.findByEmailId(subUserPayId);
				orderIdSet = txnReports.findBySubuserId(subuser.getPayId(), sessionUser.getParentPayId());
			} else {
				orderIdSet = null;
			}

			logger.info("Inside TransactionSearchAction , merchantPayId = " + merchantPayId);
			if (sessionUser.getUserType().equals(UserType.RESELLER)) {
				totalCount = txnReports.transactionCount(getTransactionId(), getOrderId(), getCustomerEmail(),
						merchantPayId, subMerchantPayId, getPaymentType(), getStatus(), getCurrency(),
						getTransactionType(), getDateFrom(), getDateTo(), sessionUser, partSettleFlag, "ALL", null,
						transactionFlag, "", "");
				BigInteger bigInt = BigInteger.valueOf(totalCount);
				setRecordsTotal(bigInt);
				if (getLength() == -1) {
					setLength(getRecordsTotal().intValue());
				}

				aaData = encoder.encodeTransactionSearchObj(txnReports.transactionReport(getTransactionId(),
						getOrderId(), getCustomerEmail(), merchantPayId, subMerchantPayId, getPaymentType(),
						getStatus(), getCurrency(), getTransactionType(), getDateFrom(), getDateTo(), sessionUser,
						getStart(), getLength(), partSettleFlag, "ALL", null, transactionFlag, "", ""));
				recordsFiltered = recordsTotal;
				//setRetailMerchantFlag(userSettings.isRetailMerchantFlag());

				// Changed by Shaiwal for handling Sub User login when user setting is not present for SubUser
				if(sessionUser.getUserType().equals(UserType.SUBUSER)){
					
					String parentMerchantPayId = sessionUser.getParentPayId();
					User parentMerchant = userDao
							.findPayId(parentMerchantPayId);
					setRetailMerchantFlag(parentMerchant.isRetailMerchantFlag());
				}
				else {
					setRetailMerchantFlag(userSettings.isRetailMerchantFlag());
				}
				
				if (StringUtils.isNotEmpty(merchantPayId) && merchantPayId.equalsIgnoreCase("ALL")) {
					List<Merchants> resellerMerchants = userDao.getResellerMerchantList(sessionUser.getResellerId());
					for (Merchants merchant : resellerMerchants) {
						UserSettingData merchantSettings=userSettingDao.fetchDataUsingPayId(merchant.getPayId());
						if (merchantSettings.isRetailMerchantFlag()) {
							setRetailMerchantFlag(true);
							break;
						}
					}
				} else if (StringUtils.isNotEmpty(merchantPayId) && !merchantPayId.equalsIgnoreCase("ALL")) {
					User merchant = userDao.findPayId(merchantPayId);
					if (merchant != null){
						UserSettingData merchantSettings=userSettingDao.fetchDataUsingPayId(merchant.getPayId());
						setRetailMerchantFlag(merchantSettings.isRetailMerchantFlag());
					}
				}

			} else if (sessionUser.getUserType().equals(UserType.MERCHANT)) {

				totalCount = txnReports.transactionCount(getTransactionId(), getOrderId(), getCustomerEmail(),
						merchantPayId, subMerchantPayId, getPaymentType(), getStatus(), getCurrency(),
						getTransactionType(), getDateFrom(), getDateTo(), sessionUser, partSettleFlag, "ALL",
						orderIdSet, transactionFlag, "", "");
				BigInteger bigInt = BigInteger.valueOf(totalCount);
				setRecordsTotal(bigInt);
				if (getLength() == -1) {
					setLength(getRecordsTotal().intValue());
				}
				aaData = encoder.encodeTransactionSearchObj(txnReports
						.refundForSaleCaputureTransaction(txnReports.transactionReport(getTransactionId(), getOrderId(),
								getCustomerEmail(), merchantPayId, subMerchantPayId, getPaymentType(), getStatus(),
								getCurrency(), getTransactionType(), getDateFrom(), getDateTo(), sessionUser,
								getStart(), getLength(), partSettleFlag, "ALL", orderIdSet, transactionFlag, "", "")));
				recordsFiltered = recordsTotal;

				//setRetailMerchantFlag(userSettings.isRetailMerchantFlag());

				// Changed by Shaiwal for handling Sub User login when user setting is not present for SubUser
				if(sessionUser.getUserType().equals(UserType.SUBUSER)){
					
					String parentMerchantPayId = sessionUser.getParentPayId();
					User parentMerchant = userDao
							.findPayId(parentMerchantPayId);
					setRetailMerchantFlag(parentMerchant.isRetailMerchantFlag());
				}
				else {
					setRetailMerchantFlag(userSettings.isRetailMerchantFlag());
				}
				
			} else if (sessionUser.getUserType().equals(UserType.SUBUSER)) {
				String subUserId = "";
				if (!userDao.isSubUserPrevilageTypeAll(sessionUser)) {
					subUserId = sessionUser.getPayId();
				}

				User user = userDao.findPayId(sessionUser.getParentPayId());

				if (user.isSuperMerchant() == false && StringUtils.isNotBlank(user.getSuperMerchantId())) {
					merchantPayId = user.getSuperMerchantId();
					subMerchantPayId = user.getPayId();
				} else {
					merchantPayId = user.getPayId();
				}

				if (!StringUtils.isEmpty(sessionUser.getSubUserType())
						&& sessionUser.getSubUserType().equalsIgnoreCase("ePosType")) {

					orderIdSet = txnReports.findBySubuserId(subUserId, sessionUser.getParentPayId());
					boolean isPgfNumber = txnReports.getPgfNumberForeposSubuser(orderIdSet, getTransactionId());
					boolean orderIdflag = false;
					for (String eposTxnOrderId : orderIdSet) {
						if (StringUtils.isNotEmpty(getOrderId()) && eposTxnOrderId.equalsIgnoreCase(getOrderId())) {
							setOrderId(eposTxnOrderId);
							orderIdflag = true;
							break;
						}
					}

					if (!orderIdflag) {
						if (!getOrderId().isEmpty())
							setOrderId(" ");
					}
					if (!isPgfNumber) {
						if (!getTransactionId().isEmpty())
							setTransactionId(" ");
					}
					sessionUser = user;

					totalCount = txnReports.transactionCount(getTransactionId(), getOrderId(), getCustomerEmail(),
							merchantPayId, subMerchantPayId, getPaymentType(), getStatus(), getCurrency(),
							getTransactionType(), getDateFrom(), getDateTo(), sessionUser, partSettleFlag, "ALL",
							orderIdSet, transactionFlag, "", "");
					BigInteger bigInt = BigInteger.valueOf(totalCount);
					setRecordsTotal(bigInt);
					if (getLength() == -1) {
						setLength(getRecordsTotal().intValue());
					}
					aaData = encoder.encodeTransactionSearchObj(txnReports.refundForSaleCaputureTransaction(
							txnReports.transactionReport(getTransactionId(), getOrderId(), getCustomerEmail(),
									merchantPayId, subMerchantPayId, getPaymentType(), getStatus(), getCurrency(),
									getTransactionType(), getDateFrom(), getDateTo(), sessionUser, getStart(),
									getLength(), partSettleFlag, "ALL", orderIdSet, transactionFlag, "", "")));
					recordsFiltered = recordsTotal;

				} else if (!StringUtils.isEmpty(sessionUser.getSubUserType())
						&& sessionUser.getSubUserType().equalsIgnoreCase("normalType")) {
					sessionUser = user;

					totalCount = txnReports.transactionCount(getTransactionId(), getOrderId(), getCustomerEmail(),
							merchantPayId, subMerchantPayId, getPaymentType(), getStatus(), getCurrency(),
							getTransactionType(), getDateFrom(), getDateTo(), sessionUser, partSettleFlag, "ALL", null,
							transactionFlag, "", "");
					BigInteger bigInt = BigInteger.valueOf(totalCount);
					setRecordsTotal(bigInt);
					if (getLength() == -1) {
						setLength(getRecordsTotal().intValue());
					}
					aaData = encoder.encodeTransactionSearchObj(txnReports.refundForSaleCaputureTransaction(
							txnReports.transactionReport(getTransactionId(), getOrderId(), getCustomerEmail(),
									merchantPayId, subMerchantPayId, getPaymentType(), getStatus(), getCurrency(),
									getTransactionType(), getDateFrom(), getDateTo(), sessionUser, getStart(),
									getLength(), partSettleFlag, "ALL", null, transactionFlag, "", "")));
					recordsFiltered = recordsTotal;
				}
				//setRetailMerchantFlag(userSettings.isRetailMerchantFlag());

				// Changed by Shaiwal for handling Sub User login when user setting is not present for SubUser
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
				
				if (aaData == null) {
					aaData = new ArrayList<TransactionSearch>();
				}

			} else {
				totalCount = txnReports.transactionCount(getTransactionId(), getOrderId(), getCustomerEmail(),
						merchantPayId, subMerchantPayId, getPaymentType(), getStatus(), getCurrency(),
						getTransactionType(), getDateFrom(), getDateTo(), sessionUser, partSettleFlag, "ALL",
						orderIdSet, transactionFlag, "", "");
				BigInteger bigInt = BigInteger.valueOf(totalCount);
				setRecordsTotal(bigInt);
				if (getLength() == -1) {
					setLength(getRecordsTotal().intValue());
				}
				aaData = encoder.encodeTransactionSearchObj(txnReports
						.refundForSaleCaputureTransaction(txnReports.transactionReport(getTransactionId(), getOrderId(),
								getCustomerEmail(), merchantPayId, subMerchantPayId, getPaymentType(), getStatus(),
								getCurrency(), getTransactionType(), getDateFrom(), getDateTo(), sessionUser,
								getStart(), getLength(), partSettleFlag, "ALL", orderIdSet, transactionFlag, "", "")));
				recordsFiltered = recordsTotal;

				if (StringUtils.isNotEmpty(merchantPayId) && !merchantPayId.equalsIgnoreCase("ALL")) {
					User merchnt = userDao.findPayId(merchantPayId);
					UserSettingData merchantSettings=userSettingDao.fetchDataUsingPayId(merchnt.getPayId());
					
					setRetailMerchantFlag(merchantSettings.isRetailMerchantFlag());
				} else if (StringUtils.isNotEmpty(merchantPayId) && merchantPayId.equalsIgnoreCase("ALL")) {
					setRetailMerchantFlag(true);
				}
			}

			for (TransactionSearch transactionSearch : aaData) {

				oldchargeback = chargebackDao.findBypgRefNum(transactionSearch.getPayId(),
						transactionSearch.getPgRefNum(), "");
				for (Chargeback chrgebacklist : oldchargeback) {

					for (TransactionSearch list : aaData)
						if (list.getPayId().equals(chrgebacklist.getPayId())
								&& list.getPgRefNum().equals(chrgebacklist.getPgRefNum())
								&& !chrgebacklist.getChargebackType().equals("Pre Arbitration")) {
							list.setBtnchargebacktext("close");
							list.setChargebackAmount(String.valueOf(chrgebacklist.getTotalchargebackAmount()));
							list.setChargebackStatus(chrgebacklist.getStatus());

						}
				}
			}

		} catch (Exception exception) {
			logger.error("Exception", exception);
			return ERROR;
		}
		return SUCCESS;

	}

	public String fetchUnsettledTransactions() {
		logger.info("Inside TransactionSearchAction , fetchUnsettledTransactions()");
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
				//setRetailMerchantFlag(userSettings.isRetailMerchantFlag());
				
				// Changed by Shaiwal for handling Sub User login when user setting is not present for SubUser
				if(sessionUser.getUserType().equals(UserType.SUBUSER)){
					
					String parentMerchantPayId = sessionUser.getParentPayId();
					User parentMerchant = userDao
							.findPayId(parentMerchantPayId);
					setRetailMerchantFlag(parentMerchant.isRetailMerchantFlag());
				}
				else {
					setRetailMerchantFlag(userSettings.isRetailMerchantFlag());
				}
				
				
			} else {
				merchantPayId = userIdentifier.getMerchantPayId(sessionUser, merchantEmailId);

				if (StringUtils.isNotBlank(subMerchantEmailId) && !subMerchantEmailId.equalsIgnoreCase("All")) {
					User mechnt = userDao.findByEmailId(subMerchantEmailId);
					if (mechnt != null) {
						subMerchantPayId = mechnt.getPayId();
						UserSettingData merchantSettings=userSettingDao.fetchDataUsingPayId(subMerchantPayId);
						setRetailMerchantFlag(merchantSettings.isRetailMerchantFlag());
					}
				}
			}

			if (StringUtils.isNotEmpty(subUserPayId) && !subUserPayId.equalsIgnoreCase("ALL")) {
				User subuser = userDao.findByEmailId(subUserPayId);
				orderIdSet = txnReports.findBySubuserId(subuser.getPayId(), sessionUser.getParentPayId());
			} else {
				orderIdSet = null;
			}
			if (sessionUser.getUserType().equals(UserType.ADMIN)
					|| sessionUser.getUserType().equals(UserType.SUBADMIN)) {
				totalCount = txnReports.unsettledTransactionCount(getTransactionId(), getOrderId(), getCustomerEmail(),
						merchantPayId, subMerchantPayId, getPaymentType(), getCurrency(),
						getDateFrom(), getDateTo(), sessionUser, partSettleFlag, "ALL",
						orderIdSet, transactionFlag, "", "");
				BigInteger bigInt = BigInteger.valueOf(totalCount);
				setRecordsTotal(bigInt);
				if (getLength() == -1) {
					setLength(getRecordsTotal().intValue());
				}
				aaData = encoder.encodeTransactionSearchObj(txnReports.refundForSaleCaputureTransaction(
						txnReports.unsettledTransactionReport(getTransactionId(), getOrderId(), getCustomerEmail(),
								merchantPayId, subMerchantPayId, getPaymentType(), getCurrency(),
								getDateFrom(), getDateTo(), sessionUser, getStart(), getLength(),
								partSettleFlag, "ALL", orderIdSet, transactionFlag, "", "")));
				recordsFiltered = recordsTotal;
			}
			
		}catch(Exception ex) {
			logger.info("Exception Caught while fetching fetchUnsettledTransactions : ", ex);
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
		if (validator.validateBlankField(getSKUCode())) {
		} else if (!validator.validateField(CrmFieldType.SKU_CODE, getSKUCode())) {
			addFieldError(CrmFieldType.SKU_CODE.getName(), ErrorType.INVALID_FIELD.getResponseMessage());
		}
//		if (validator.validateBlankField(getCategoryCode())) {
//		} else if (!validator.validateField(CrmFieldType.CATEGORY_CODE, getCategoryCode())) {
//			addFieldError(CrmFieldType.CATEGORY_CODE.getName(), ErrorType.INVALID_FIELD.getResponseMessage());
//		}

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

		if (validator.validateBlankField(getCustId())) {
		} else if (!validator.validateField(CrmFieldType.CUST_ID, getCustId())) {
			addFieldError(CrmFieldType.CUST_ID.getName(), ErrorType.INVALID_FIELD.getResponseMessage());
		}

		if (validator.validateBlankField(getStatus())) {
		} else if (!validator.validateField(CrmFieldType.TXN_STATUS, getStatus())) {
			addFieldError(CrmFieldType.TXN_STATUS.getName(), ErrorType.INVALID_FIELD.getResponseMessage());
		}

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

	public String getPgRefNum() {
		return pgRefNum;
	}

	public void setPgRefNum(String pgRefNum) {
		this.pgRefNum = pgRefNum;
	}

	public String getPayId() {
		return payId;
	}

	public void setPayId(String payId) {
		this.payId = payId;
	}

	public String getCustId() {
		return custId;
	}

	public void setCustId(String custId) {
		this.custId = custId;
	}

	public String getPartSettleFlag() {
		return partSettleFlag;
	}

	public void setPartSettleFlag(String partSettleFlag) {
		this.partSettleFlag = partSettleFlag;
	}

	public String getSubMerchantEmailId() {
		return subMerchantEmailId;
	}

	public void setSubMerchantEmailId(String subMerchantEmailId) {
		this.subMerchantEmailId = subMerchantEmailId;
	}

	public String getDeliveryStatus() {
		return deliveryStatus;
	}

	public void setDeliveryStatus(String deliveryStatus) {
		this.deliveryStatus = deliveryStatus;
	}

//	public String getCategoryCode() {
//		return categoryCode;
//	}
//
//	public void setCategoryCode(String categoryCode) {
//		this.categoryCode = categoryCode;
//	}

	public String getSKUCode() {
		return SKUCode;
	}

	public void setSKUCode(String sKUCode) {
		SKUCode = sKUCode;
	}

	public boolean isRetailMerchantFlag() {
		return retailMerchantFlag;
	}

	public void setRetailMerchantFlag(boolean retailMerchantFlag) {
		this.retailMerchantFlag = retailMerchantFlag;
	}

	public Set<String> getOrderIdSet() {
		return orderIdSet;
	}

	public void setOrderIdSet(Set<String> orderIdSet) {
		this.orderIdSet = orderIdSet;
	}

	public String getSubUserPayId() {
		return subUserPayId;
	}

	public void setSubUserPayId(String subUserPayId) {
		this.subUserPayId = subUserPayId;
	}

	public String getTransactionFlag() {
		return transactionFlag;
	}

	public void setTransactionFlag(String transactionFlag) {
		this.transactionFlag = transactionFlag;
	}

}
