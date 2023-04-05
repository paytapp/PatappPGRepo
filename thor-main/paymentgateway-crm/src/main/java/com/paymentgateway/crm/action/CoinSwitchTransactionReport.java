package com.paymentgateway.crm.action;

import java.math.BigInteger;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.paymentgateway.commons.user.CoinSwitchTransactionObject;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserType;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.PropertiesManager;

/**
 * @auther Sandeep Sharma
 */

public class CoinSwitchTransactionReport extends AbstractSecureAction {

	private static final long serialVersionUID = -2714047590814426057L;

	private static Logger logger = LoggerFactory.getLogger(CoinSwitchTransactionReport.class.getName());

	@Autowired
	private CoinSwitchCustomerAndTxnDataDao coinSwitchCustomerAndTxnDataDao;

	private List<CoinSwitchTransactionObject> aaData;
	private String custName;
	private String custEmail;
	private String custPhone;
	private String virtualAccountNo;
	private String pgRefNo;
	private String rrn;
	private String txnType;
	private String purpose;
	private String status;
	private int draw;
	private int start;
	private int length;
	private String dateTo;
	private String dateFrom;
	private int totalCount;

	private BigInteger recordsTotal;
	public BigInteger recordsFiltered;

	private User sessionUser = new User();

	public String execute() {
		int totalCount;
		sessionUser = (User) sessionMap.get(Constants.USER.getValue());
		try {
			if (sessionUser.getUserType().equals(UserType.ADMIN)
					|| sessionUser.getUserType().equals(UserType.SUBADMIN)) {
				totalCount = coinSwitchCustomerAndTxnDataDao.fetchCustomerTransactionCount(custName, custEmail,
						custPhone, virtualAccountNo, status, rrn, pgRefNo, purpose, txnType, dateFrom, dateTo);
				BigInteger bigInt = BigInteger.valueOf(totalCount);
				setRecordsTotal(bigInt);
				if (getLength() == -1) {
					setLength(getRecordsTotal().intValue());
				}

				setAaData(coinSwitchCustomerAndTxnDataDao.fetchCustomerTransaction(custName, custEmail, custPhone,
						virtualAccountNo, status, rrn, pgRefNo, purpose, txnType, dateFrom, dateTo, getStart(),
						getLength()));
				recordsFiltered = recordsTotal;

			}
			if (sessionUser.getUserType().equals(UserType.MERCHANT) && sessionUser.getPayId()
					.equals(PropertiesManager.propertiesMap.get("CoinSwitch_Merchant_PayId"))) {

				totalCount = coinSwitchCustomerAndTxnDataDao.fetchCustomerTransactionCount(custName, custEmail,
						custPhone, virtualAccountNo, status, rrn, pgRefNo, purpose, txnType, dateFrom, dateTo);
				BigInteger bigInt = BigInteger.valueOf(totalCount);
				setRecordsTotal(bigInt);
				if (getLength() == -1) {
					setLength(getRecordsTotal().intValue());
				}

				setAaData(coinSwitchCustomerAndTxnDataDao.fetchCustomerTransaction(custName, custEmail, custPhone,
						virtualAccountNo, status, rrn, pgRefNo, purpose, txnType, dateFrom, dateTo, getStart(),
						getLength()));
				recordsFiltered = recordsTotal;

			}
		} catch (Exception e) {
			logger.error("Exception in getting coin switch transactionDateBase >> ", e);
		}

		return SUCCESS;
	}

	public List<CoinSwitchTransactionObject> getAaData() {
		return aaData;
	}

	public void setAaData(List<CoinSwitchTransactionObject> aaData) {
		this.aaData = aaData;
	}

	public String getCustName() {
		return custName;
	}

	public void setCustName(String custName) {
		this.custName = custName;
	}

	public String getCustEmail() {
		return custEmail;
	}

	public void setCustEmail(String custEmail) {
		this.custEmail = custEmail;
	}

	public String getCustPhone() {
		return custPhone;
	}

	public void setCustPhone(String custPhone) {
		this.custPhone = custPhone;
	}

	public String getVirtualAccountNo() {
		return virtualAccountNo;
	}

	public void setVirtualAccountNo(String virtualAccountNo) {
		this.virtualAccountNo = virtualAccountNo;
	}

	public String getPgRefNo() {
		return pgRefNo;
	}

	public void setPgRefNo(String pgRefNo) {
		this.pgRefNo = pgRefNo;
	}

	public String getRrn() {
		return rrn;
	}

	public void setRrn(String rrn) {
		this.rrn = rrn;
	}

	public String getTxnType() {
		return txnType;
	}

	public void setTxnType(String txnType) {
		this.txnType = txnType;
	}

	public String getPurpose() {
		return purpose;
	}

	public void setPurpose(String purpose) {
		this.purpose = purpose;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public int getDraw() {
		return draw;
	}

	public void setDraw(int draw) {
		this.draw = draw;
	}

	public int getStart() {
		return start;
	}

	public void setStart(int start) {
		this.start = start;
	}

	public int getLength() {
		return length;
	}

	public void setLength(int length) {
		this.length = length;
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

	public int getTotalCount() {
		return totalCount;
	}

	public void setTotalCount(int totalCount) {
		this.totalCount = totalCount;
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

}
