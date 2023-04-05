package com.paymentgateway.crm.action;

import org.slf4j.Logger; import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.paymentgateway.commons.dao.BeneficiaryAccountsDao;
import com.paymentgateway.commons.user.BeneficiaryAccounts;
import com.paymentgateway.commons.user.UserDao;

public class EditBeneficiaryAction extends AbstractSecureAction {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -5029111815204022566L;

	@Autowired
	private UserDao userDao;
	
	@Autowired
	private BeneficiaryAccountsDao beneficiaryAccountsDao;
	
	private static Logger logger = LoggerFactory.getLogger(EditBeneficiaryAction.class.getName());
	private String param;
	private Long frmId;
	
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
	private String status;

	@SuppressWarnings("unchecked")
	public String execute() {
		
		try {
			if (frmId != null && param != null) {
					
				BeneficiaryAccounts beneficiary = beneficiaryAccountsDao.findById(frmId);
				
				setStatus(beneficiary.getStatus().getName());
				setBeneficiaryCd(beneficiary.getBeneficiaryCd());
				setSrcAccountNo(beneficiary.getSrcAccountNo());
				setBeneName(beneficiary.getBeneName());
				setBeneAccountNo(beneficiary.getBeneAccountNo());
				setIfscCode(beneficiary.getIfscCode());
				setPaymentType(beneficiary.getPaymentType());
				setBeneType(beneficiary.getBeneType());
				setCurrencyCode(beneficiary.getCurrencyCd());
				setCustId(beneficiary.getCustId());
				setBankName(beneficiary.getBankName());
				
				return SUCCESS;

			}
		} catch (Exception exception) {
			logger.error("Exception", exception);
		}


		return SUCCESS;
	}

	public String getParam() {
		return param;
	}

	public void setParam(String param) {
		this.param = param;
	}

	

	public Long getFrmId() {
		return frmId;
	}

	public void setFrmId(Long frmId) {
		this.frmId = frmId;
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

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
	
	
	
		
}
