package com.paymentgateway.crm.action;

import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paymentgateway.commons.api.Hasher;
import com.paymentgateway.commons.api.TransactionControllerServiceProvider;
import com.paymentgateway.commons.dao.ImpsDao;
import com.paymentgateway.commons.dao.MerchantInitiatedDirectDao;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.user.ImpsDownloadObject;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.user.UserType;
import com.paymentgateway.commons.util.Amount;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.DateCreater;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.StatusType;

public class MerchantInitiatedReport extends AbstractSecureAction {

	private static final long serialVersionUID = 2811346574562571717L;

	private static Logger logger = LoggerFactory.getLogger(MerchantInitiatedReport.class.getName());

	@Autowired
	private UserDao userDao;

	@Autowired
	private ImpsDao impsDao;

	@Autowired
	private MerchantInitiatedDirectDao merchantInitiatedDirectDao;

	@Autowired
	TransactionControllerServiceProvider transactionControllerServiceProvider;

	// Report
	String payId;
	String subMerchantPayId;
	private String txnId;
	private String dateTo;
	private String dateFrom;
	private String txnType;
	private String status;
	private String orderId;
	private String beneAccountNumber;
	private InputStream fileInputStream;
	private String filename;
	private List<ImpsDownloadObject> aaData;
	private String userType;
	private String response;
	private String responseMsg;
	private String amount;
	private String payerAddress;
	private String rrn;
	private int draw;
	private int length;
	private int start;
	private BigInteger recordsTotal;
	public BigInteger recordsFiltered;
	private String updateRrn;
	private String updateStatus;
	private String acquirerName;
	private String finalStatus;
	private String bankName;
	private String virtualAccNo;

	public static final BigDecimal ONE_HUNDRED = new BigDecimal(100);

	private User sessionUser = new User();

	public String execute() {
		int totalCount;
		setDateFrom(DateCreater.toDateTimeformatCreater(dateFrom));
		setDateTo(DateCreater.formDateTimeformatCreater(dateTo));
		logger.info("Inside merchantInitiatedDirectReport()");
		sessionUser = (User) sessionMap.get(Constants.USER.getValue());
		try {
			String payIdReport = "";
			String subMerchantPayIdReport = "";

			User userByPayId = null;

			if (sessionUser.getUserType().equals(UserType.SUBUSER)) {
				User user = null;
				if (StringUtils.isNotBlank(sessionUser.getParentPayId())) {
					user = userDao.findPayId(sessionUser.getParentPayId());
					if (user.isSuperMerchant() == false && StringUtils.isNotBlank(user.getSuperMerchantId())) {
						payIdReport = user.getSuperMerchantId();
						subMerchantPayIdReport = user.getPayId();
					} else {
						if (StringUtils.isNotBlank(subMerchantPayId)) {
							payIdReport = payId;
							subMerchantPayIdReport = subMerchantPayId;
						} else {
							payIdReport = payId;
						}
					}
				}
			} else if (sessionUser.isSuperMerchant() == false
					&& StringUtils.isNotBlank(sessionUser.getSuperMerchantId())) {
				payIdReport = sessionUser.getSuperMerchantId();
				subMerchantPayIdReport = sessionUser.getPayId();
			} else if (StringUtils.isNotBlank(subMerchantPayId) && !subMerchantPayId.equalsIgnoreCase("ALL")) {
				userByPayId = userDao.findPayId(subMerchantPayId);
				payIdReport = userByPayId.getSuperMerchantId();
				subMerchantPayIdReport = subMerchantPayId;

			} else {
				payIdReport = payId;
				subMerchantPayIdReport = subMerchantPayId;
			}

			totalCount = merchantInitiatedDirectDao.merchantInitiatedDirectReportCount(dateFrom, dateTo, payIdReport,
					subMerchantPayIdReport, status, orderId, beneAccountNumber, payerAddress, rrn, txnType, sessionUser,
					acquirerName, finalStatus);
			BigInteger bigInt = BigInteger.valueOf(totalCount);
			setRecordsTotal(bigInt);
			if (getLength() == -1) {
				setLength(getRecordsTotal().intValue());
			}

			setAaData(merchantInitiatedDirectDao.merchantInitiatedDirectReportDataView(dateFrom, dateTo, payIdReport,
					subMerchantPayIdReport, status, orderId, beneAccountNumber, payerAddress, rrn, txnType, sessionUser,
					getStart(), getLength(), acquirerName, finalStatus));
			recordsFiltered = recordsTotal;

			return SUCCESS;

		} catch (Exception e) {
			logger.error("exception ", e);
		}
		return SUCCESS;
	}

	public String merchantInitiatedDirectReInitiated() {
		Map<String, String> requestMap = new HashMap<>();
		Map<String, String> requestMapTxnId = new HashMap<>();
		Map<String, String> respMap = null;
		try {

			requestMapTxnId = merchantInitiatedDirectDao.getMerchantInitiatedTransactionWithTxnId(txnId);
			// ImpsDownloadObject
			// impsData=impsDao.getMerchantInitiatedTransactionWithTxnId(txnId);
			boolean dailyLimit = merchantInitiatedDirectDao.isDailyLimitExceed(requestMapTxnId);
			if (requestMapTxnId != null && !dailyLimit) {

				requestMap.put(FieldType.ORDER_ID.getName(), requestMapTxnId.get(FieldType.ORDER_ID.getName()));
				if (requestMapTxnId.containsKey(FieldType.BENE_ACCOUNT_NO.getName())) {
					if (StringUtils.isNotBlank(requestMapTxnId.get(FieldType.BENE_ACCOUNT_NO.getName()))) {
						requestMap.put(FieldType.BENE_ACCOUNT_NO.getName(),
								requestMapTxnId.get(FieldType.BENE_ACCOUNT_NO.getName()));
					}
				}
				if (requestMapTxnId.containsKey(FieldType.BANK_NAME.getName())) {
					if (StringUtils.isNotBlank(requestMapTxnId.get(FieldType.BANK_NAME.getName()))) {
						requestMap.put(FieldType.BANK_NAME.getName(),
								requestMapTxnId.get(FieldType.BANK_NAME.getName()));
					}
				}
				if (requestMapTxnId.containsKey(FieldType.PAYER_ADDRESS.getName())) {
					if (StringUtils.isNotBlank(requestMapTxnId.get(FieldType.PAYER_ADDRESS.getName()))) {
						requestMap.put(FieldType.PAYER_ADDRESS.getName(),
								requestMapTxnId.get(FieldType.PAYER_ADDRESS.getName()));
					}
				}

				if (requestMapTxnId.containsKey(FieldType.BENE_NAME.getName())) {
					if (StringUtils.isNotBlank(requestMapTxnId.get(FieldType.BENE_NAME.getName()))) {
						requestMap.put(FieldType.BENE_NAME.getName(),
								requestMapTxnId.get(FieldType.BENE_NAME.getName()));
					}
				}

				if (requestMapTxnId.containsKey(FieldType.PAYER_NAME.getName())) {
					if (StringUtils.isNotBlank(requestMapTxnId.get(FieldType.PAYER_NAME.getName()))) {
						requestMap.put(FieldType.PAYER_NAME.getName(),
								requestMapTxnId.get(FieldType.PAYER_NAME.getName()));
					}
				}
				if (StringUtils.isNotBlank(requestMapTxnId.get(FieldType.REMARKS.getName()))) {
					requestMap.put(FieldType.REMARKS.getName(), requestMapTxnId.get(FieldType.REMARKS.getName()));
				}
				if (StringUtils.isNotBlank(requestMapTxnId.get(FieldType.VIRTUAL_AC_CODE.getName()))) {
					requestMap.put(FieldType.VIRTUAL_AC_CODE.getName(), requestMapTxnId.get(FieldType.VIRTUAL_AC_CODE.getName()));
				}
				
				if (StringUtils.isNotBlank(requestMapTxnId.get(FieldType.ACQUIRER_NAME.getName()))) {
					requestMap.put(FieldType.ACQUIRER_NAME.getName(), requestMapTxnId.get(FieldType.ACQUIRER_NAME.getName()));
				}

				if (StringUtils.isNotBlank(requestMapTxnId.get(FieldType.IFSC_CODE.getName()))) {
					requestMap.put(FieldType.IFSC_CODE.getName(), requestMapTxnId.get(FieldType.IFSC_CODE.getName()));
				}
				requestMap.put(FieldType.AMOUNT.getName(), requestMapTxnId.get(FieldType.AMOUNT.getName()));
				requestMap.put(FieldType.PAY_ID.getName(), requestMapTxnId.get(FieldType.PAY_ID.getName()));
				requestMap.put(FieldType.PHONE_NO.getName(), requestMapTxnId.get(FieldType.PHONE_NO.getName()));
				requestMap.put(FieldType.USER_TYPE.getName(), requestMapTxnId.get(FieldType.USER_TYPE.getName()));
				requestMap.put(FieldType.TXNTYPE.getName(), requestMapTxnId.get(FieldType.TXNTYPE.getName()));
				requestMap.put(FieldType.CURRENCY_CODE.getName(), "356");
				requestMap.put(FieldType.HASH.getName(), Hasher.getHash(new Fields(requestMap)));

				if (!requestMap.containsKey(FieldType.BENE_ACCOUNT_NO.getName())) {
					if (StringUtils.isBlank(requestMap.get(FieldType.BENE_ACCOUNT_NO.getName()))) {
						respMap = transactionControllerServiceProvider.upiTransferTransact(requestMap);
					}
				} else {
					respMap = transactionControllerServiceProvider.impsTransferTransact(requestMap);
				}
				if (respMap.get(FieldType.STATUS.getName()).equalsIgnoreCase(StatusType.CAPTURED.getName())) {
					setResponse("success");
					setResponseMsg(respMap.get(FieldType.PG_TXN_MESSAGE.getName()));
				} else {
					setResponse("failed");
					setResponseMsg(respMap.get(FieldType.PG_TXN_MESSAGE.getName()));
				}

			} else {
				if (dailyLimit) {
					setResponse("failed");
					setResponseMsg("Declined due to insufficient balance");
				}

			}
			logger.info("Inside IMPS Action");
			logger.info("response mg " + getResponseMsg());
		} catch (SystemException e) {
			logger.error("exception ", e);
			setResponse("failed");
			setResponseMsg("Failed Due To System Error");

		}
		return SUCCESS;

	}

	public String fetchTxnStatusMerchantInitiatedDirect() {
		logger.info("Inside fetchTxnStatusMerchantInitiatedDirect(), MerchantInitiatedReport");
		Map<String, String> requestMapTxnId = new HashMap<>();
		Map<String, String> respMap;
		Map<String, String> requestMapFromDb = merchantInitiatedDirectDao.getMerchantInitiatedTransactionWithTxnId(txnId);
		if (StringUtils.isNotBlank(txnId) && StringUtils.isNotBlank(txnType) && StringUtils.isNotBlank(payId)
				&& StringUtils.isNotBlank(userType) && StringUtils.isNotBlank(dateFrom)) {
			try {

				requestMapTxnId.put(FieldType.TXN_ID.getName(), txnId);
				requestMapTxnId.put(FieldType.ORDER_ID.getName(), orderId);
				requestMapTxnId.put(FieldType.TXNTYPE.getName(), txnType);
				requestMapTxnId.put(FieldType.USER_TYPE.getName(), userType);
				requestMapTxnId.put(FieldType.PAY_ID.getName(), payId);
				requestMapTxnId.put(FieldType.AMOUNT.getName(), Amount.formatAmount(amount, "356"));
				requestMapTxnId.put(FieldType.CURRENCY_CODE.getName(), "356");
				requestMapTxnId.put(FieldType.CREATE_DATE.getName(), dateFrom);

				requestMapTxnId.put(FieldType.ACQUIRER_NAME.getName(), requestMapFromDb.get(FieldType.ACQUIRER_NAME.getName()));
				requestMapTxnId.put(FieldType.VIRTUAL_AC_CODE.getName(), requestMapFromDb.get(FieldType.VIRTUAL_AC_CODE.getName()));
				requestMapTxnId.put(FieldType.HASH.getName(), Hasher.getHash(new Fields(requestMapTxnId)));
				logger.info("Orginal request for status enquiry by CRM , pay Id"
						+ requestMapTxnId.get(FieldType.PAY_ID.getName()).toString() + " and Txn Id"
						+ requestMapTxnId.get(FieldType.TXN_ID.getName()).toString());

				respMap = transactionControllerServiceProvider.MerchantDirectInitiateStatusEnq(requestMapTxnId);

				logger.info("Final Response for status enquiry by CRM , pay Id"
						+ requestMapTxnId.get(FieldType.PAY_ID.getName()).toString() + " and Txn Id"
						+ requestMapTxnId.get(FieldType.TXN_ID.getName()).toString() + " , " + respMap);
				if (respMap.get(FieldType.STATUS.getName()).equalsIgnoreCase(StatusType.CAPTURED.getName())
						|| respMap.get(FieldType.STATUS.getName()).equalsIgnoreCase(StatusType.PROCESSING.getName())) {
					setResponse("success");
					setResponseMsg(respMap.get(FieldType.PG_TXN_MESSAGE.getName()));
				} else {
					setResponse("failed");
					if (StringUtils.isNotBlank(respMap.get(FieldType.PG_TXN_MESSAGE.getName())))
						setResponseMsg(respMap.get(FieldType.PG_TXN_MESSAGE.getName()));
					else
						setResponseMsg(respMap.get(FieldType.RESPONSE_MESSAGE.getName()));
				}
			} catch (SystemException e) {
				logger.error("exception ", e);
				setResponse("failed");
				setResponseMsg("Failed Due To System Error");
			}
		}

		return SUCCESS;

	}

	public String updateStatus() {
		logger.info("Inside updateStatus(), MerchantInitiatedReport");
		try {
			String updatePayId = "";
			String updateSubmerchantId = "";

			if (StringUtils.isNotBlank(payId)) {
				sessionUser = userDao.findPayId(payId);
			}
			if (sessionUser.isSuperMerchant() == false && StringUtils.isNotBlank(sessionUser.getSuperMerchantId())) {
				updatePayId = sessionUser.getSuperMerchantId();
				updateSubmerchantId = sessionUser.getPayId();
			} else {
				updatePayId = sessionUser.getPayId();
			}

			String acqName = merchantInitiatedDirectDao.getTransactionAcquirerName(txnId);
			merchantInitiatedDirectDao.updateStatus(updatePayId, updateSubmerchantId, amount, txnId, status,
					updateStatus, updateRrn, dateFrom);
			if (!(status.equalsIgnoreCase(updateStatus))) {
				if ((!(status.equalsIgnoreCase("Timeout") || status.equalsIgnoreCase(StatusType.PROCESSING.getName()))
						&& updateStatus.equalsIgnoreCase("Captured"))
						|| (status.equalsIgnoreCase("Captured") && !updateStatus.equalsIgnoreCase("Timeout"))) {
					merchantInitiatedDirectDao.updateClosingCollection(updatePayId, updateSubmerchantId, amount, status,
							updateStatus, dateFrom, acqName);
				} else {
					if (((status.equalsIgnoreCase("Timeout")
							|| status.equalsIgnoreCase(StatusType.PROCESSING.getName()))
							&& !updateStatus.equalsIgnoreCase("Captured"))
							|| (!status.equalsIgnoreCase("Captured") && updateStatus.equalsIgnoreCase("Timeout"))) {
						merchantInitiatedDirectDao.updateClosingCollection(updatePayId, updateSubmerchantId, amount,
								status, updateStatus, dateFrom, acqName);
					}
				}
			}
			setResponse("success");
			setResponseMsg("Status Updated");
		} catch (Exception e) {
			logger.error("exception cought in updateStatus", e);
		}
		return SUCCESS;
	}

	public String updateAllStatus() {
		logger.info("Inside updateAllStatus(), MerchantInitiatedReport");
		String txnArr[] = null;
		try {
			if (StringUtils.isNotBlank(txnId) && StringUtils.isNotBlank(updateStatus)) {
				txnArr = txnId.split(",");
				for (String txn : txnArr) {
					merchantInitiatedDirectDao.getUpdateTransactionByTxnId(txn, updateStatus);
				}
				setResponse("success");
				setResponseMsg("Status Updated");
			}
		} catch (Exception e) {
			logger.error("exception cought in updateAllStatus", e);
		}
		return SUCCESS;
	}

	public String getPayId() {
		return payId;
	}

	public void setPayId(String payId) {
		this.payId = payId;
	}

	public String getSubMerchantPayId() {
		return subMerchantPayId;
	}

	public void setSubMerchantPayId(String subMerchantPayId) {
		this.subMerchantPayId = subMerchantPayId;
	}

	public String getTxnId() {
		return txnId;
	}

	public void setTxnId(String txnId) {
		this.txnId = txnId;
	}

	public String getDateTo() {
		return dateTo;
	}

	public void setDateTo(String dateTo) {
		this.dateTo = dateTo;
	}

	public String getDateFrom() {
		return dateFrom;
	}

	public void setDateFrom(String dateFrom) {
		this.dateFrom = dateFrom;
	}

	public String getTxnType() {
		return txnType;
	}

	public void setTxnType(String txnType) {
		this.txnType = txnType;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public InputStream getFileInputStream() {
		return fileInputStream;
	}

	public void setFileInputStream(InputStream fileInputStream) {
		this.fileInputStream = fileInputStream;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public List<ImpsDownloadObject> getAaData() {
		return aaData;
	}

	public void setAaData(List<ImpsDownloadObject> aaData) {
		this.aaData = aaData;
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

	public String getOrderId() {
		return orderId;
	}

	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}

	public String getBeneAccountNumber() {
		return beneAccountNumber;
	}

	public void setBeneAccountNumber(String beneAccountNumber) {
		this.beneAccountNumber = beneAccountNumber;
	}

	public String getUserType() {
		return userType;
	}

	public void setUserType(String userType) {
		this.userType = userType;
	}

	public String getAmount() {
		return amount;
	}

	public void setAmount(String amount) {
		this.amount = amount;
	}

	public String getPayerAddress() {
		return payerAddress;
	}

	public void setPayerAddress(String payerAddress) {
		this.payerAddress = payerAddress;
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

	public String getRrn() {
		return rrn;
	}

	public void setRrn(String rrn) {
		this.rrn = rrn;
	}

	public String getUpdateRrn() {
		return updateRrn;
	}

	public void setUpdateRrn(String updateRrn) {
		this.updateRrn = updateRrn;
	}

	public String getUpdateStatus() {
		return updateStatus;
	}

	public void setUpdateStatus(String updateStatus) {
		this.updateStatus = updateStatus;
	}

	public String getFinalStatus() {
		return finalStatus;
	}

	public void setFinalStatus(String finalStatus) {
		this.finalStatus = finalStatus;
	}

	public String getAcquirerName() {
		return acquirerName;
	}

	public void setAcquirerName(String acquirerName) {
		this.acquirerName = acquirerName;
	}

	public String getBankName() {
		return bankName;
	}

	public void setBankName(String bankName) {
		this.bankName = bankName;
	}

	public String getVirtualAccNo() {
		return virtualAccNo;
	}

	public void setVirtualAccNo(String virtualAccNo) {
		this.virtualAccNo = virtualAccNo;
	}

}
