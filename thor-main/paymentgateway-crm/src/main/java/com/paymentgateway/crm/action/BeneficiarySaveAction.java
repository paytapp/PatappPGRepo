package com.paymentgateway.crm.action;

import java.math.BigInteger;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.paymentgateway.commons.api.TransactionControllerServiceProvider;
import com.paymentgateway.commons.dao.BeneficiaryAccountsDao;
import com.paymentgateway.commons.user.BeneficiaryAccounts;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserType;
import com.paymentgateway.commons.util.AccountStatus;
import com.paymentgateway.commons.util.BeneficiaryTypes;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.CurrencyTypes;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.NodalPaymentTypes;
import com.paymentgateway.commons.util.SettlementTransactionType;

/**
 * @author Shaiwal
 *
 */

public class BeneficiarySaveAction extends AbstractSecureAction {

	private static final long serialVersionUID = -1485162588555225830L;

	@Autowired
	private BeneficiaryAccountsDao beneficiaryAccountsDao;
	
	@Autowired 
	private TransactionControllerServiceProvider transactionControllerServiceProvider; 

	private static Logger logger = LoggerFactory.getLogger(BeneficiarySaveAction.class.getName());

	private String acquirer;
	private int draw;
	private int length;
	private int start;
	private BigInteger recordsTotal;
	public BigInteger recordsFiltered;
	private String response;
	private List<BeneficiaryAccounts> aaData;
	private User sessionUser = new User();
	
	private String beneficiaryCd;
	private String srcAccountNo;
	private String beneName;
	private String beneAccountNo;
	private String ifscCode;
	private String paymentType;
	private String beneType;
	private String currencyCode;
	private String custId;
	private String bankName;
	
	public String execute() {
	
		logger.info("Inside save beneficiary");
		try {
			
		sessionUser = (User) sessionMap.get(Constants.USER.getValue());	
		BeneficiaryAccounts beneficiaryFromDb = beneficiaryAccountsDao.findByBeneficiaryCd(getBeneficiaryCd());
		if (beneficiaryFromDb != null){
			logger.info("Benefiary Code already present !");
			setResponse("Benefiary Code already present !");
			return SUCCESS;
		}	
			
		Date date = new Date();
		Fields fields = new Fields();
		fields.put(FieldType.CUST_ID_BENEFICIARY.getName(),getCustId());
		fields.put(FieldType.BENEFICIARY_CD.getName(),getBeneficiaryCd());
		fields.put(FieldType.BENE_NAME.getName(),getBeneName());
		fields.put(FieldType.BENE_ACCOUNT_NO.getName(),getBeneAccountNo());
		fields.put(FieldType.IFSC_CODE.getName(),getIfscCode());
		fields.put(FieldType.PAYMENT_TYPE.getName(),NodalPaymentTypes.getInstancefromCode(getPaymentType()).getName());
		fields.put(FieldType.BENE_TYPE.getName(),getBeneType());
		fields.put(FieldType.CURRENCY_CD.getName(),getCurrencyCode());
		fields.put(FieldType.CURRENCY_CODE.getName(),CurrencyTypes.getInstancefromCode(getCurrencyCode()).getName());
		fields.put(FieldType.NODAL_ACQUIRER.getName(),getAcquirer());
		fields.put(FieldType.BANK_NAME.getName(),getBankName());
		fields.put(FieldType.SRC_ACCOUNT_NO.getName(),getSrcAccountNo());
		fields.put(FieldType.TXNTYPE.getName(),SettlementTransactionType.ADD_BENEFICIARY.getName());
		Map<String,String> responseMap = new HashMap<String,String>();
		
		responseMap = transactionControllerServiceProvider.settlementTransact(fields);
		
		String responseMessage =  "";
		String rrn =  "";
		
		
		if (StringUtils.isNotBlank(responseMap.get(FieldType.RESPONSE_MESSAGE.getName()))) {
			responseMessage = responseMap.get(FieldType.RESPONSE_MESSAGE.getName());
		}
		else {
			responseMessage = "REJECTED";
		}
		
		logger.info("Inside save beneficiary , response message = " + responseMessage);
		if (StringUtils.isNotBlank(responseMap.get(FieldType.RRN.getName()))) {
			rrn = responseMap.get(FieldType.RRN.getName());
		}
		else {
			rrn = "NA";
		}
		
		logger.info("Inside save beneficiary , rrn  = " + rrn);
		
		BeneficiaryAccounts beneficiaryAccounts = new BeneficiaryAccounts();
		if (responseMessage.equalsIgnoreCase("SUCCESS")) {
			beneficiaryAccounts.setStatus(AccountStatus.ACTIVE);
			setResponse("Beneficiary Account added successfully");
		}
		else {
			beneficiaryAccounts.setStatus(AccountStatus.REJECTED);
			setResponse("Beneficiary Account cannot be added");
		}

		beneficiaryAccounts.setCustId(getCustId());
		beneficiaryAccounts.setSrcAccountNo(getSrcAccountNo());
		beneficiaryAccounts.setBeneName(getBeneName());
		beneficiaryAccounts.setBeneAccountNo(getBeneAccountNo());
		beneficiaryAccounts.setIfscCode(getIfscCode());
		beneficiaryAccounts.setPaymentType(getPaymentType());
		beneficiaryAccounts.setBeneType(BeneficiaryTypes.getInstancefromCode(getBeneType()).getCode());
		beneficiaryAccounts.setCurrencyCd(getCurrencyCode());
		beneficiaryAccounts.setAcquirer(getAcquirer());
		beneficiaryAccounts.setBeneficiaryCd(getBeneficiaryCd());
		beneficiaryAccounts.setRrn(rrn);
		beneficiaryAccounts.setResponseMessage(responseMessage);
		beneficiaryAccounts.setCreatedDate(date);
		beneficiaryAccounts.setUpdatedDate(date);
		beneficiaryAccounts.setRequestedBy(sessionUser.getEmailId());
		
		if (sessionUser.getUserType() == (UserType.ADMIN)) {
			beneficiaryAccounts.setProcessedBy(sessionUser.getEmailId());
		}

		beneficiaryAccountsDao.create(beneficiaryAccounts);
		return SUCCESS;
		
		}
		catch(Exception e) {
			logger.error("Exception = " , e);
		}
		return SUCCESS;
	}

	
	public void validate() {

	}

	public List<BeneficiaryAccounts> getAaData() {
		return aaData;
	}

	public void setAaData(List<BeneficiaryAccounts> aaData) {
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

	public String getBeneName() {
		return beneName;
	}

	public void setBeneName(String beneName) {
		this.beneName = beneName;
	}

	public String getBeneAccountNo() {
		return beneAccountNo;
	}

	public void setBeneAccountNo(String beneAccountNo) {
		this.beneAccountNo = beneAccountNo;
	}

	public String getIfscCode() {
		return ifscCode;
	}

	public void setIfscCode(String ifscCode) {
		this.ifscCode = ifscCode;
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

	public String getBankName() {
		return bankName;
	}

	public void setBankName(String bankName) {
		this.bankName = bankName;
	}

}
