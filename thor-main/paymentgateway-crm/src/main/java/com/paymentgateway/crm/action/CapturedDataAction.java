package com.paymentgateway.crm.action;

import java.math.BigInteger;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.user.TransactionSearch;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.user.UserType;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.CrmFieldConstants;
import com.paymentgateway.commons.util.CrmFieldType;
import com.paymentgateway.commons.util.CrmValidator;
import com.paymentgateway.commons.util.DateCreater;
import com.paymentgateway.crm.mongoReports.TxnReports;

public class CapturedDataAction extends AbstractSecureAction {
	
	@Autowired
	private CrmValidator validator;
	
	@Autowired
	private TxnReports txnReports;
	
	@Autowired
	private UserDao userDao;
	
	private static Logger logger = LoggerFactory.getLogger(CapturedDataAction.class.getName());

	private static final long serialVersionUID = -6919220389124792416L;
	
	private String payId;
	private String subMerchantPayId;
	private String transactionId;
	private String paymentType;
	private String orderId;
	private String currency;
	private String postSettleFlag;
	private String dateFrom;
	private String dateTo;
	private int draw;
	private int length;
	private int start;
	private BigInteger recordsTotal;
	public BigInteger recordsFiltered;
	public boolean flag = false;
	private Set<String> orderIdSet;
	
	private List<TransactionSearch> aaData;
	
	private User sessionUser = new User();
	
	public String execute() {
		logger.info("Inside CapturedDataAction , execute()");
		int totalCount;
		
		sessionUser = (User) sessionMap.get(Constants.USER.getValue());
		setDateFrom(DateCreater.toDateTimeformatCreater(dateFrom));
		setDateTo(DateCreater.formDateTimeformatCreater(dateTo));
		try {
			String merchantPayId = "";
			String subMerchantPayIdd = "";
			if(sessionUser.isSuperMerchant() == false && StringUtils.isNotBlank(sessionUser.getSuperMerchantId())) {
				merchantPayId = sessionUser.getSuperMerchantId();
				subMerchantPayIdd = sessionUser.getPayId();
			} else {
				merchantPayId = payId;

				if (StringUtils.isNotBlank(subMerchantPayId) && !subMerchantPayId.equalsIgnoreCase("All")) {
					//subMerchantPayIdd = subMerchantPayId;
					subMerchantPayIdd = userDao.getPayIdByEmailId(subMerchantPayId);
				}
			}
			
			if(sessionUser.getUserType().equals(UserType.MERCHANT) || sessionUser.getUserType().equals(UserType.SUBMERCHANT) || sessionUser.getUserType().equals(UserType.SUPERMERCHANT)) {
				
				totalCount = txnReports.capturedDataCount(merchantPayId, subMerchantPayIdd,getTransactionId(), getOrderId(), getPaymentType(), getCurrency(), getDateFrom(), getDateTo(), sessionUser, getStart(), getLength(),
						getPostSettleFlag(), null);
				BigInteger bigInt = BigInteger.valueOf(totalCount);
				setRecordsTotal(bigInt);
				if (getLength() == -1) {
					setLength(getRecordsTotal().intValue());
				}
				setAaData(txnReports.capturedData(merchantPayId, subMerchantPayIdd, getTransactionId(), getOrderId(), getPaymentType(), getCurrency(), getDateFrom(), getDateTo(), sessionUser, getStart(), getLength(),
						getPostSettleFlag(), null));
				if(sessionUser.isSuperMerchant() == false && StringUtils.isNotBlank(sessionUser.getSuperMerchantId())) {
					flag = true;
					setFlag(flag);
				} else if(sessionUser.isSuperMerchant() == true ) {
					flag = true;
					setFlag(flag);
				} else {
					flag = false;
					setFlag(flag);
				}
				recordsFiltered = recordsTotal;
				
			}else if(sessionUser.getUserType().equals(UserType.SUBUSER) ) {
				
				User user = userDao.findPayId(sessionUser.getParentPayId());
				
				if(user.isSuperMerchant() == false && StringUtils.isNotBlank(user.getSuperMerchantId())) {
					merchantPayId = user.getSuperMerchantId();
					subMerchantPayIdd = user.getPayId();
				}else {
					merchantPayId = user.getPayId();
				}
				
				
				if(StringUtils.isNotEmpty(sessionUser.getSubUserType()) && sessionUser.getSubUserType().equalsIgnoreCase("eposType")) {
										
					merchantPayId = sessionUser.getParentPayId();
					String subUserId = sessionUser.getPayId();

					orderIdSet = txnReports.findBySubuserId(subUserId, sessionUser.getParentPayId());
					
					boolean isPgfNumber = txnReports.getPgfNumberForeposSubuser(orderIdSet, getTransactionId());
					
					for(String eposTxnOrderId : orderIdSet) {
						if(StringUtils.isNotEmpty(getOrderId()) && eposTxnOrderId.equalsIgnoreCase(getOrderId())) {
							setOrderId(eposTxnOrderId);
							break;
						}else {
							if(!getOrderId().isEmpty())
								setOrderId(" ");
						}
					}
					if(!isPgfNumber) {
						if(!getTransactionId().isEmpty())
							setTransactionId(" ");
					}
					
					sessionUser = user; //userdao.findPayId(sessionUser.getParentPayId());
					
					totalCount = txnReports.capturedDataCount(merchantPayId, subMerchantPayIdd,getTransactionId(), getOrderId(), getPaymentType(), getCurrency(), getDateFrom(), getDateTo(), sessionUser, getStart(), getLength(),
							getPostSettleFlag(), orderIdSet);
					BigInteger bigInt = BigInteger.valueOf(totalCount);
					setRecordsTotal(bigInt);
					if (getLength() == -1) {
						setLength(getRecordsTotal().intValue());
					}
					setAaData(txnReports.capturedData(merchantPayId, subMerchantPayIdd, getTransactionId(), getOrderId(), getPaymentType(), getCurrency(), getDateFrom(), getDateTo(), sessionUser, getStart(), getLength(),
							getPostSettleFlag(), orderIdSet));
					if(sessionUser.isSuperMerchant() == false && StringUtils.isNotBlank(sessionUser.getSuperMerchantId())) {
						flag = true;
						setFlag(flag);
					} else if(sessionUser.isSuperMerchant() == true ) {
						flag = true;
						setFlag(flag);
					} else {
						flag = false;
						setFlag(flag);
					}
					recordsFiltered = recordsTotal;
					
				}else if(StringUtils.isNotEmpty(sessionUser.getSubUserType()) && sessionUser.getSubUserType().equalsIgnoreCase("normalType")) {
					
					sessionUser = user; //userdao.findPayId(sessionUser.getParentPayId());
					
					totalCount = txnReports.capturedDataCount(merchantPayId, subMerchantPayIdd,getTransactionId(), getOrderId(), getPaymentType(), getCurrency(), getDateFrom(), getDateTo(), sessionUser, getStart(), getLength(),
							getPostSettleFlag(), null);
					BigInteger bigInt = BigInteger.valueOf(totalCount);
					setRecordsTotal(bigInt);
					if (getLength() == -1) {
						setLength(getRecordsTotal().intValue());
					}
					setAaData(txnReports.capturedData(merchantPayId, subMerchantPayIdd, getTransactionId(), getOrderId(), getPaymentType(), getCurrency(), getDateFrom(), getDateTo(), sessionUser, getStart(), getLength(),
							getPostSettleFlag(), null));
					if(sessionUser.isSuperMerchant() == false && StringUtils.isNotBlank(sessionUser.getSuperMerchantId())) {
						flag = true;
						setFlag(flag);
					} else if(sessionUser.isSuperMerchant() == true ) {
						flag = true;
						setFlag(flag);
					} else {
						flag = false;
						setFlag(flag);
					}
					recordsFiltered = recordsTotal;
				}
			}
			else {
				totalCount = txnReports.capturedDataCount(merchantPayId,subMerchantPayIdd,getTransactionId(), getOrderId(), getPaymentType(), getCurrency(), getDateFrom(), getDateTo(), sessionUser, getStart(), getLength(),
						getPostSettleFlag(), null);
				BigInteger bigInt = BigInteger.valueOf(totalCount);
				setRecordsTotal(bigInt);
				if (getLength() == -1) {
					setLength(getRecordsTotal().intValue());
				}
				setAaData(txnReports.capturedData(merchantPayId, subMerchantPayIdd, getTransactionId(), getOrderId(), getPaymentType(), getCurrency(), getDateFrom(), getDateTo(), sessionUser, getStart(), getLength(),
						getPostSettleFlag(), null));
				recordsFiltered = recordsTotal;
			}
		}catch(Exception e) {
			
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
		if (validator.validateBlankField(getCurrency())) {
		} else if (!validator.validateField(CrmFieldType.CURRENCY, getCurrency())) {
			addFieldError(CrmFieldType.CURRENCY.getName(), ErrorType.INVALID_FIELD.getResponseMessage());
		}
	}
	
	public String getPayId() {
		return payId;
	}

	public void setPayId(String payId) {
		this.payId = payId;
	}
	
	public boolean isFlag() {
		return flag;
	}

	public void setFlag(boolean flag) {
		this.flag = flag;
	}

	public String getSubMerchantPayId() {
		return subMerchantPayId;
	}

	public void setSubMerchantPayId(String subMerchantPayId) {
		this.subMerchantPayId = subMerchantPayId;
	}
	public String getPostSettleFlag() {
		return postSettleFlag;
	}

	public void setPostSettleFlag(String postSettleFlag) {
		this.postSettleFlag = postSettleFlag;
	}
	public String getCurrency() {
		return currency;
	}
	public void setCurrency(String currency) {
		this.currency = currency;
	}
	public String getTransactionId() {
		return transactionId;
	}
	public void setTransactionId(String transactionId) {
		this.transactionId = transactionId;
	}
	public String getPaymentType() {
		return paymentType;
	}
	public void setPaymentType(String paymentType) {
		this.paymentType = paymentType;
	}
	public String getOrderId() {
		return orderId;
	}
	public void setOrderId(String orderId) {
		this.orderId = orderId;
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
	public List<TransactionSearch> getAaData() {
		return aaData;
	}

	public void setAaData(List<TransactionSearch> aaData) {
		this.aaData = aaData;
	}

	public Set<String> getOrderIdSet() {
		return orderIdSet;
	}

	public void setOrderIdSet(Set<String> orderIdSet) {
		this.orderIdSet = orderIdSet;
	}
	
}
