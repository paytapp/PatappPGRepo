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
import com.paymentgateway.commons.dao.BeneficiaryAccountsDao;
import com.paymentgateway.commons.user.BeneficiaryAccounts;
import com.paymentgateway.commons.user.NodalTransactions;
import com.paymentgateway.commons.user.TransactionSearch;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.user.UserType;
import com.paymentgateway.commons.util.Amount;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.CrmValidator;
import com.paymentgateway.commons.util.CurrencyTypes;
import com.paymentgateway.commons.util.DataEncoder;
import com.paymentgateway.commons.util.DateCreater;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.NodalPaymentTypes;
import com.paymentgateway.commons.util.SettlementTransactionType;
import com.paymentgateway.crm.mongoReports.NodalTxnReports;
import com.paymentgateway.crm.mongoReports.TxnReports;

/**
 * @author Shaiwal
 *
 */

public class NodalTransactionsAction extends AbstractSecureAction {


	/**
	 * 
	 */
	private static final long serialVersionUID = 6547819794132886172L;

	@Autowired
	private CrmValidator validator;

	@Autowired
	private DataEncoder encoder;

	@Autowired
	private UserDao userDao;
	
	@Autowired
	private BeneficiaryAccountsDao beneficiaryAccountsDao;
	
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
	private List<BeneficiaryAccounts> aaData;
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
	
	@Autowired
	private NodalTxnReports txnReports;
	
	
	public String execute() {
		
		
		logger.info("Inside NodalTransactionsAction");
		try {
			
		BeneficiaryAccounts beneficiaryFromDb = beneficiaryAccountsDao.findByBeneficiaryCd(getBeneficiaryCd());
			
		if (StringUtils.isBlank(beneficiaryFromDb.getBeneficiaryCd())){
			setResponse("Benefiary Code not found in database !");
			return SUCCESS;
		}	
		
		CurrencyTypes currencyTypes = CurrencyTypes.getInstancefromCode(getCurrencyCode());
		String amountInPaise = Amount.formatAmount(amount, currencyTypes.getName());	
		
		Fields fields = new Fields();
		fields.put(FieldType.CUSTOMER_ID.getName(),getCustId());
		fields.put(FieldType.BENEFICIARY_CD.getName(),getBeneficiaryCd());
		fields.put(FieldType.BENE_ACCOUNT_NO.getName(),getBeneAccountNo());
		fields.put(FieldType.PAYMENT_TYPE.getName(),NodalPaymentTypes.getInstancefromName(getPaymentType()).getName());
		fields.put(FieldType.CURRENCY_CD.getName(),currencyTypes.getCode());
		fields.put(FieldType.CURRENCY_CODE.getName(),currencyTypes.getName());
		fields.put(FieldType.NODAL_ACQUIRER.getName(),getAcquirer());
		fields.put(FieldType.SRC_ACCOUNT_NO.getName(),getSrcAccountNo());
		fields.put(FieldType.TXNTYPE.getName(),SettlementTransactionType.FUND_TRANSFER.getName());
		fields.put(FieldType.PRODUCT_DESC.getName(),getComments());
		fields.put(FieldType.AMOUNT.getName(),amountInPaise);
		fields.put(FieldType.BENE_NAME.getName(),beneficiaryFromDb.getBeneName());
		logger.info("Inside NodalTransactionsAction fields = " +fields.getFieldsAsString());
		Map<String,String> responseMap = new HashMap<String,String>();
		
		responseMap = transactionControllerServiceProvider.settlementTransact(fields);
		
		String responseMessage =  "";
		String transactionId =  "";
		
		
		if (StringUtils.isNotBlank(responseMap.get(FieldType.RESPONSE_MESSAGE.getName()))) {
			responseMessage = responseMap.get(FieldType.RESPONSE_MESSAGE.getName());
		}
		else {
			responseMessage = "REJECTED";
		}
		
		logger.info("Inside save beneficiary , response message = " + responseMessage);
		if (StringUtils.isNotBlank(responseMap.get(FieldType.TXN_ID.getName()))) {
			transactionId = responseMap.get(FieldType.TXN_ID.getName());
		}
		else {
			transactionId = "NA";
		}
		
		logger.info("Inside Nodal Transactions action , initiate nodal transaction , transactionId  = " + transactionId);
		
		if (responseMessage.equalsIgnoreCase("SUCCESS")) {
			setResponse("Nodal Transaction successful");
		}
		else {
			setResponse("Nodal Transaction failed");
		}
		
		}
		catch(Exception e) {
			logger.error("Exception in initiating nodal transaction " , e);
			return SUCCESS;
		}

		return SUCCESS;
	}
	
	public String getAcquirerDataForNodal() {
		
		return SUCCESS;
	}
	
	// To display page without using token
	@SuppressWarnings("unchecked")
	public String displayList() {
		return INPUT;
	}


	public String getActiveBeneficiaries() {
		
		logger.info("Inside getActiveBeneficiaries , execute()");
		int totalCount;
		sessionUser = (User) sessionMap.get(Constants.USER.getValue());
		List<BeneficiaryAccounts> beneficiaryAccountsList = new ArrayList<BeneficiaryAccounts>();
		
		try {
			if (StringUtils.isNotBlank(acquirer)) {
				
				if (acquirer.equalsIgnoreCase("")) {
					acquirer = "ALL";
				}
			}
			else {
				acquirer = "ALL";
			}
			setAcquirer(acquirer);
			if (sessionUser.getUserType().equals(UserType.ADMIN ) || sessionUser.getUserType().equals(UserType.SUBADMIN)) {
				
				if (acquirer.equalsIgnoreCase("ALL")) {
					
				}
				else {
					beneficiaryAccountsList = beneficiaryAccountsDao.getActiveBeneficiaryAccountsListByAcquirer(acquirer);
				}
				totalCount = beneficiaryAccountsList.size();
				BigInteger bigInt = BigInteger.valueOf(totalCount);
				setRecordsTotal(bigInt);
				if (getLength() == -1) {
					setLength(getRecordsTotal().intValue());
				}

				aaData = beneficiaryAccountsList;
				recordsFiltered = recordsTotal;
				
				return SUCCESS;
			}


		} catch (Exception exception) {
			logger.error("Exception", exception);
			return SUCCESS;
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


}
