package com.paymentgateway.crm.action;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger; import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.paymentgateway.commons.api.TransactionControllerServiceProvider;
import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.user.NodalTransactions;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.DateCreater;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.SettlementTransactionType;
import com.paymentgateway.crm.mongoReports.NodalTxnReports;

/**
 * @author Shaiwal
 *
 */
public class NodalTransactionHistoryAction extends AbstractSecureAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4077683475572563293L;

	@Autowired
	private TransactionControllerServiceProvider transactionControllerServiceProvider;

	private static Logger logger = LoggerFactory.getLogger(NodalTransactionsAction.class.getName());

	private String acquirer;
	private int draw;
	private int length;
	private int start;
	private BigInteger recordsTotal;
	public BigInteger recordsFiltered;
	private String response;
	private List<NodalTransactions> aaData;
	private List<NodalTransactions> nodalTransactionsList = new ArrayList<NodalTransactions>();
	private User sessionUser = new User();

	private String beneficiaryCd;
	private String srcAccountNo;
	private String beneAccountNo;
	private String paymentType;
	private String beneType;
	private String currencyCode;
	private String custId;
	private String comments;
	private String amount;
	private String dateFrom;
	private String txnId;
	private String status;
	private String oid;
	private String dateTo;
	private String txnType;

	@Autowired
	private NodalTxnReports txnReports;

	@SuppressWarnings("unchecked")
	public String execute() {

		logger.info("Inside getNodalTransactions , ");

		logger.info("Inside TransactionSearchAction , execute()");
		int totalCount;
		sessionUser = (User) sessionMap.get(Constants.USER.getValue());
		setDateFrom(DateCreater.toDateTimeformatCreater(dateFrom));
		setDateTo(DateCreater.formDateTimeformatCreater(dateTo));
		try {
			totalCount = txnReports.searchPaymentCount(getTxnId(), getOid(), getStatus(), getPaymentType(),
					getDateFrom(), getDateTo());
			BigInteger bigInt = BigInteger.valueOf(totalCount);
			setRecordsTotal(bigInt);
			if (getLength() == -1) {
				setLength(getRecordsTotal().intValue());
			}
			nodalTransactionsList = txnReports.searchPayment(getTxnId(), getOid(), getStatus(), getPaymentType(),
					getDateFrom(), getDateTo(), getStart(), getLength());
			setAaData(nodalTransactionsList);
			recordsFiltered = recordsTotal;

		} catch (Exception exception) {
			logger.error("Exception", exception);
			return ERROR;
		}
		return SUCCESS;
	}

	public String loadData() {

		return SUCCESS;

	}

	public String refreshTransactionStatus() {

		try {
			Fields fields = new Fields();
			fields.put(FieldType.CUSTOMER_ID.getName(), getCustId());
			fields.put(FieldType.OID.getName(), getOid());
			fields.put(FieldType.NODAL_ACQUIRER.getName(), getAcquirer());
			fields.put(FieldType.TXNTYPE.getName(), SettlementTransactionType.STATUS.getName());
			Map<String, String> responseMap = new HashMap<String, String>();

			responseMap = transactionControllerServiceProvider.settlementTransact(fields);

			String responseMessage = "";
			String responseCode = "";

			if (StringUtils.isNotBlank(responseMap.get(FieldType.RESPONSE_MESSAGE.getName()))) {
				responseMessage = responseMap.get(FieldType.RESPONSE_MESSAGE.getName());
			} else {
				responseMessage = "REJECTED";
			}

			if (StringUtils.isNotBlank(responseMap.get(FieldType.RESPONSE_CODE.getName()))) {
				responseCode = responseMap.get(FieldType.RESPONSE_CODE.getName());
			} else {
				responseCode = "999";
			}
			logger.info("Inside refreshTransactionStatus , response message = " + responseMessage);
			logger.info("Inside refreshTransactionStatus , response code  = " + responseCode);

			if (responseCode.equalsIgnoreCase(ErrorType.SUCCESS.getCode())) {
				setResponse("Enquiry Status : Successful");
			} else {
				setResponse("Enquiry Status : Failed");
			}

			return SUCCESS;

		} catch (Exception e) {
			logger.error("Exception = " , e);
		}
		return SUCCESS;

	}

	public void validate() {

	}

	public List<NodalTransactions> getAaData() {
		return aaData;
	}

	public void setAaData(List<NodalTransactions> aaData) {
		this.aaData = aaData;
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

	public String getAcquirer() {
		return acquirer;
	}

	public void setAcquirer(String acquirer) {
		this.acquirer = acquirer;
	}

	public String getResponse() {
		return response;
	}

	public void setResponse(String response) {
		this.response = response;
	}

	public String getBeneficiaryCd() {
		return beneficiaryCd;
	}

	public void setBeneficiaryCd(String beneficiaryCd) {
		this.beneficiaryCd = beneficiaryCd;
	}

	public String getSrcAccountNo() {
		return srcAccountNo;
	}

	public void setSrcAccountNo(String srcAccountNo) {
		this.srcAccountNo = srcAccountNo;
	}

	public String getBeneAccountNo() {
		return beneAccountNo;
	}

	public void setBeneAccountNo(String beneAccountNo) {
		this.beneAccountNo = beneAccountNo;
	}

	public String getPaymentType() {
		return paymentType;
	}

	public void setPaymentType(String paymentType) {
		this.paymentType = paymentType;
	}

	public String getBeneType() {
		return beneType;
	}

	public void setBeneType(String beneType) {
		this.beneType = beneType;
	}

	public String getCurrencyCode() {
		return currencyCode;
	}

	public void setCurrencyCode(String currencyCode) {
		this.currencyCode = currencyCode;
	}

	public String getCustId() {
		return custId;
	}

	public void setCustId(String custId) {
		this.custId = custId;
	}

	public String getComments() {
		return comments;
	}

	public void setComments(String comments) {
		this.comments = comments;
	}

	public String getAmount() {
		return amount;
	}

	public void setAmount(String amount) {
		this.amount = amount;
	}

	public String getOid() {
		return oid;
	}

	public void setOid(String oid) {
		this.oid = oid;
	}

	public List<NodalTransactions> getNodalTransactionsList() {
		return nodalTransactionsList;
	}

	public void setNodalTransactionsList(List<NodalTransactions> nodalTransactionsList) {
		this.nodalTransactionsList = nodalTransactionsList;
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

	public String getTxnId() {
		return txnId;
	}

	public void setTxnId(String txnId) {
		this.txnId = txnId;
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

}
