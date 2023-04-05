package com.paymentgateway.crm.action;

import java.math.BigInteger;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.user.ECollectionObject;
import com.paymentgateway.commons.user.TransactionSearch;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.user.UserType;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.CrmFieldConstants;
import com.paymentgateway.commons.util.CrmFieldType;
import com.paymentgateway.commons.util.CrmValidator;
import com.paymentgateway.commons.util.DataEncoder;
import com.paymentgateway.commons.util.DateCreater;
import com.paymentgateway.crm.actionBeans.SessionUserIdentifier;
import com.paymentgateway.crm.mongoReports.TxnReports;

public class ECollectionReportAction extends AbstractSecureAction {

	@Autowired
	private CrmValidator validator;

	@Autowired
	private DataEncoder encoder;

	@Autowired
	private TxnReports txnReports;

	@Autowired
	private UserDao userDao;

	@Autowired
	private SessionUserIdentifier userIdentifier;
	private static Logger logger = LoggerFactory.getLogger(ECollectionReportAction.class.getName());

	private static final long serialVersionUID = -6919220389124792416L;

	private String payId;
	private String subMerchantPayId;
	private String paymentMode;
	private String status;
	private String dateFrom;
	private String dateTo;
	private int draw;
	private int length;
	private int start;
	private String txnType;
	private String virtualAccountNo;

	public boolean flag = false;
	private BigInteger recordsTotal;
	public BigInteger recordsFiltered;

	private List<ECollectionObject> aaData;
	private User sessionUser = new User();

	public String execute() {

		logger.info("Inside ECollectionReportAction , execute()");
		int totalCount;
		sessionUser = (User) sessionMap.get(Constants.USER.getValue());
		setDateFrom(DateCreater.toDateTimeformatCreater(dateFrom));
		setDateTo(DateCreater.formDateTimeformatCreater(dateTo));

		try {
			String merchantPayId = "";
			String subMerchantPayIdd = "";

			User user = null;
			if(sessionUser.getUserType().equals(UserType.SUBUSER)) {
			if (StringUtils.isNotBlank(sessionUser.getParentPayId())) {
				user = userDao.findPayId(sessionUser.getParentPayId());
				if (user.isSuperMerchant() == false && StringUtils.isNotBlank(user.getSuperMerchantId())) {
					merchantPayId = user.getSuperMerchantId();
					subMerchantPayIdd = user.getPayId();
				} else {
					if (StringUtils.isNotBlank(subMerchantPayId) && !subMerchantPayId.equalsIgnoreCase("All")) {
						// subMerchantPayIdd = subMerchantPayId;
						subMerchantPayIdd = userDao.getPayIdByEmailId(subMerchantPayId);
					} else {
						merchantPayId = sessionUser.getParentPayId();
					}
				}
			}
			}else {
				if (sessionUser.isSuperMerchant() == false
						&& StringUtils.isNotBlank(sessionUser.getSuperMerchantId())) {
					merchantPayId = sessionUser.getSuperMerchantId();
					subMerchantPayIdd = sessionUser.getPayId();
				} else {
					merchantPayId = payId;

					if (StringUtils.isNotBlank(subMerchantPayId) && !subMerchantPayId.equalsIgnoreCase("All")) {
						// subMerchantPayIdd = subMerchantPayId;
						subMerchantPayIdd = userDao.getPayIdByEmailId(subMerchantPayId);
					}
				}
			}

			if (sessionUser.getUserType().equals(UserType.RESELLER)) {
				totalCount = txnReports.eCollectionCount(merchantPayId, "", getPaymentMode(), getStatus(), getTxnType(),
						getDateFrom(), getDateTo(), sessionUser);
				BigInteger bigInt = BigInteger.valueOf(totalCount);
				setRecordsTotal(bigInt);
				if (getLength() == -1) {
					setLength(getRecordsTotal().intValue());
				}

				setAaData(txnReports.eCollectionData(merchantPayId, "", getPaymentMode(), getStatus(), getTxnType(),
						getDateFrom(), getDateTo(), sessionUser, getStart(), getLength()));
				recordsFiltered = recordsTotal;
			}

			else if (sessionUser.getUserType().equals(UserType.MERCHANT)
					|| sessionUser.getUserType().equals(UserType.SUBMERCHANT)
					|| sessionUser.getUserType().equals(UserType.SUPERMERCHANT)) {
				totalCount = txnReports.eCollectionCount(merchantPayId, subMerchantPayIdd, getPaymentMode(),
						getStatus(), getTxnType(), getDateFrom(), getDateTo(), sessionUser);
				BigInteger bigInt = BigInteger.valueOf(totalCount);
				setRecordsTotal(bigInt);
				if (getLength() == -1) {
					setLength(getRecordsTotal().intValue());
				}

				setAaData(txnReports.eCollectionData(merchantPayId, subMerchantPayIdd, getPaymentMode(), getStatus(),
						getTxnType(), getDateFrom(), getDateTo(), sessionUser, getStart(), getLength()));
				if (sessionUser.isSuperMerchant() == false
						&& StringUtils.isNotBlank(sessionUser.getSuperMerchantId())) {
					flag = true;
					setFlag(flag);
				} else if (sessionUser.isSuperMerchant() == true) {
					flag = true;
					setFlag(flag);
				} else {
					flag = false;
					setFlag(flag);
				}
				recordsFiltered = recordsTotal;
			} else {
				totalCount = txnReports.eCollectionCount(merchantPayId, subMerchantPayIdd, getPaymentMode(),
						getStatus(), getTxnType(), getDateFrom(), getDateTo(), sessionUser);
				BigInteger bigInt = BigInteger.valueOf(totalCount);
				setRecordsTotal(bigInt);
				if (getLength() == -1) {
					setLength(getRecordsTotal().intValue());
				}

				setAaData(txnReports.eCollectionData(merchantPayId, subMerchantPayIdd, getPaymentMode(), getStatus(),
						getTxnType(), getDateFrom(), getDateTo(), sessionUser, getStart(), getLength()));
				recordsFiltered = recordsTotal;
			}
		} catch (Exception exception) {
			logger.error("Exception", exception);
			return ERROR;
		}
		return SUCCESS;
	}

	public String virtaulAccountList() {

		sessionUser = (User) sessionMap.get(Constants.USER.getValue());
		try {
			if (sessionUser.getUserType().equals(UserType.ADMIN) || sessionUser.getUserType().equals(UserType.SUBADMIN)
					|| sessionUser.getUserType().equals(UserType.RESELLER)) {
				setAaData(userDao.getVaDataList(payId, subMerchantPayId, sessionUser, virtualAccountNo));
			} else if (sessionUser.getUserType().equals(UserType.MERCHANT) || sessionUser.isSuperMerchant()) {
				setAaData(userDao.getVaDataList(payId, subMerchantPayId, sessionUser, virtualAccountNo));
			}
		} catch (Exception exception) {
			logger.error("Exception", exception);
			return SUCCESS;
		}
		return SUCCESS;
	}

	public void validate() {
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

	public List<ECollectionObject> getAaData() {
		return aaData;
	}

	public void setAaData(List<ECollectionObject> aadata) {
		this.aaData = aadata;
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

	public String getPayId() {
		return payId;
	}

	public void setPayId(String payId) {
		this.payId = payId;
	}

	public String getsubMerchantPayId() {
		return subMerchantPayId;
	}

	public void setSubMerchantPayId(String subMerchantPayId) {
		this.subMerchantPayId = subMerchantPayId;
	}

	public String getPaymentMode() {
		return paymentMode;
	}

	public void setPaymentMode(String paymentMode) {
		this.paymentMode = paymentMode;
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

	public boolean getFlag() {
		return flag;
	}

	public void setFlag(boolean flag) {
		this.flag = flag;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getTxnType() {
		return txnType;
	}

	public void setTxnType(String txnType) {
		this.txnType = txnType;
	}

	public String getVirtualAccountNo() {
		return virtualAccountNo;
	}

	public void setVirtualAccountNo(String virtualAccountNo) {
		this.virtualAccountNo = virtualAccountNo;
	}

}
