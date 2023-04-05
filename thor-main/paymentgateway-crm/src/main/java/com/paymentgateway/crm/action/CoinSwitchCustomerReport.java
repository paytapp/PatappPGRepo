package com.paymentgateway.crm.action;

import java.math.BigInteger;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.paymentgateway.commons.user.CoinSwitchCustomer;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserType;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.PropertiesManager;

/**
 * @auther Sandeep Sharma
 */

public class CoinSwitchCustomerReport extends AbstractSecureAction {
	private static final long serialVersionUID = -8041540244833088769L;

	private static Logger logger = LoggerFactory.getLogger(CoinSwitchCustomerReport.class.getName());

	@Autowired
	private CoinSwitchCustomerAndTxnDataDao coinSwitchCustomerAndTxnDataDao;

	private List<CoinSwitchCustomer> aaData;
	private String custName;
	private String custEmail;
	private String custPhone;
	private String virtualAccountNo;
	private String status;
	private int draw;
	private int start;
	private int length;
	private BigInteger recordsTotal;
	public BigInteger recordsFiltered;

	private User sessionUser = new User();

	public String execute() {
		int totalCount;
		sessionUser = (User) sessionMap.get(Constants.USER.getValue());
		try {
			if (sessionUser.getUserType().equals(UserType.ADMIN)
					|| sessionUser.getUserType().equals(UserType.SUBADMIN)) {
				totalCount = coinSwitchCustomerAndTxnDataDao.fetchCustomerCount(custName, custEmail, custPhone,
						virtualAccountNo, status);
				BigInteger bigInt = BigInteger.valueOf(totalCount);
				setRecordsTotal(bigInt);
				if (getLength() == -1) {
					setLength(getRecordsTotal().intValue());
				}

				setAaData(coinSwitchCustomerAndTxnDataDao.fetchCustomerData(custName, custEmail, custPhone,
						virtualAccountNo, status, getStart(), getLength()));
				recordsFiltered = recordsTotal;

			}
			if (sessionUser.getUserType().equals(UserType.MERCHANT) && sessionUser.getPayId()
					.equals(PropertiesManager.propertiesMap.get("CoinSwitch_Merchant_PayId"))) {

				totalCount = coinSwitchCustomerAndTxnDataDao.fetchCustomerCount(custName, custEmail, custPhone,
						virtualAccountNo, status);
				BigInteger bigInt = BigInteger.valueOf(totalCount);
				setRecordsTotal(bigInt);
				if (getLength() == -1) {
					setLength(getRecordsTotal().intValue());
				}

				setAaData(coinSwitchCustomerAndTxnDataDao.fetchCustomerData(custName, custEmail, custPhone,
						virtualAccountNo, status, getStart(), getLength()));
				recordsFiltered = recordsTotal;

			}
		} catch (Exception e) {
			logger.error("Exception in getting coin switch userbase >> ", e);
		}

		return SUCCESS;
	}

	public List<CoinSwitchCustomer> getAaData() {
		return aaData;
	}

	public void setAaData(List<CoinSwitchCustomer> aaData) {
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
