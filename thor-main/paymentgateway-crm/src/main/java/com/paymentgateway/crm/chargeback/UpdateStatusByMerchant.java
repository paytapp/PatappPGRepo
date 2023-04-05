package com.paymentgateway.crm.chargeback;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.paymentgateway.commons.dao.ChargebackDao;
import com.paymentgateway.commons.user.Chargeback;
import com.paymentgateway.commons.user.ChargebackComment;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserType;
import com.paymentgateway.commons.util.ChargebackEmailCreater;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.TransactionManager;
import com.paymentgateway.crm.action.AbstractSecureAction;

public class UpdateStatusByMerchant extends AbstractSecureAction {

	@Autowired
	private ChargebackDao chargebackDao;
	
	@Autowired
	private ChargebackEmailCreater chargebackEmailCreater;

	private static final long serialVersionUID = 8763650020568453333L;
	private static Logger logger = LoggerFactory.getLogger(UpdateStatusByMerchant.class.getName());

	private String chargebackStatus;
	private String caseId;
	private String targetDate;
	private Date todaydate;
	private BigDecimal capturedAmount;
	private BigDecimal authorizedAmount;
	private Date updateDate;
	private String commentedBy;
	private String chargebackType;
	private String payId;
	private String transactionId;
	private String custEmail;
	private String userType;
	private String merchantStatus;
	private String adminStatus;
	private String currencyNameCode;
	private BigDecimal amount;
	private BigDecimal chargebackAmount;
	private String pgRefNum;
	private String orderId;
	
	

	public String execute() {
		Chargeback chargeback = new Chargeback();
		ChargebackDao dao = new ChargebackDao();
		try {	
			todaydate = new Date();

			User user = (User) sessionMap.get(Constants.USER.getValue());
			if (user.getUserType().equals(UserType.ADMIN) || user.getUserType().equals(UserType.SUBADMIN)
					|| user.getUserType().equals(UserType.MERCHANT) || user.getUserType().equals(UserType.SUBUSER)) {
				SimpleDateFormat dateformate = new SimpleDateFormat("dd-MM-yyyy");
				String date = dateformate.format(todaydate);
				Date targetdate = dateformate.parse(getTargetDate());
				Date currentdate = dateformate.parse(date);
				String targetDateArray[] = targetDate.split(" ");
				setTargetDate(targetDateArray[0]);
				if (targetdate.compareTo(currentdate) > 0 || targetdate.equals(currentdate)) {
                   Set<ChargebackComment> commentSet = null;
                   
                   User sessionUser = (User) sessionMap.get(Constants.USER.getValue());
       			chargeback = dao.findByCaseId(caseId);
       			ChargebackComment chargebackComment = new ChargebackComment();
       			chargebackComment.setCommentId(TransactionManager.getNewTransactionId());
       			commentSet = chargeback.getChargebackComments();
       			commentSet.add(chargebackComment);
       			chargeback.setChargebackComments(commentSet);
       			
					chargeback.setId(TransactionManager.getNewTransactionId());
					chargeback.setAuthorizedAmount(authorizedAmount);
					chargeback.setAmount(amount);
					chargeback.setCapturedAmount(capturedAmount.toString());
					chargeback.setUpdateDate(todaydate);
					chargeback.setTargetDate(targetDate);
					chargeback.setCreateDate(getCreateDate(caseId));
					// chargeback.setCommentedBy(commentedBy);
					chargeback.setChargebackType(chargebackType);
					chargeback.setPayId(payId);
					chargeback.setTransactionId(transactionId);
					chargeback.setCustEmail(custEmail);
					chargeback.setChargebackStatus(chargebackStatus);
					chargeback.setCaseId(caseId);
			        chargeback.setAdminStatus(adminStatus);
			        chargeback.setMerchantStatus(merchantStatus);
			        chargeback.setCurrencyNameCode(currencyNameCode);
			        chargeback.setChargebackAmount(chargebackAmount);
			        chargeback.setOrderId(orderId);
			        chargeback.setPgRefNum(pgRefNum);
			        chargeback.getChargebackComments();
			        chargeback.setAmount(amount);
					if (chargebackStatus.contains("Accepted")){
						chargeback.setStatus("Accepted");
					}else if (chargebackStatus.contains("Refunded")){
						chargeback.setStatus("Refunded");
					}else if (chargebackStatus.contains("Rejected")){
						chargeback.setStatus("Rejected");
					}else if (chargebackStatus.contains("Closed")){
						chargeback.setStatus("Closed");
					}
			chargebackDao.UpdateData(chargeback);
			
			
			//email to team

			chargebackEmailCreater.sendChargebackAccptedRejectEmail(chargeback ,sessionUser);
			
				}
			}

		} catch (Exception exception) {
			logger.error("Exception", exception);
			return ERROR;
		}
		return SUCCESS;

	}

	public Date getCreateDate(String caseId) {
		Chargeback chbk = null;
		try {
			
			chbk = chargebackDao.findByCaseId(caseId);
		
		}catch (Exception exception) {
			logger.error("Exception", exception);
		}
		return chbk.getCreateDate();
	}
	

	public BigDecimal getCapturedAmount() {
		return capturedAmount;
	}

	public void setCapturedAmount(BigDecimal capturedAmount) {
		this.capturedAmount = capturedAmount;
	}

	public BigDecimal getAuthorizedAmount() {
		return authorizedAmount;
	}

	public void setAuthorizedAmount(BigDecimal authorizedAmount) {
		this.authorizedAmount = authorizedAmount;
	}

	public Date getUpdateDate() {
		return updateDate;
	}

	public void setUpdateDate(Date updateDate) {
		this.updateDate = updateDate;
	}

	public String getCommentedBy() {
		return commentedBy;
	}

	public void setCommentedBy(String commentedBy) {
		this.commentedBy = commentedBy;
	}

	public String getChargebackType() {
		return chargebackType;
	}

	public void setChargebackType(String chargebackType) {
		this.chargebackType = chargebackType;
	}

	public String getPayId() {
		return payId;
	}

	public void setPayId(String payId) {
		this.payId = payId;
	}

	public String getTransactionId() {
		return transactionId;
	}

	public void setTransactionId(String transactionId) {
		this.transactionId = transactionId;
	}

	public String getCustEmail() {
		return custEmail;
	}

	public void setCustEmail(String custEmail) {
		this.custEmail = custEmail;
	}

	public Date getTodaydate() {
		return todaydate;
	}

	public void setTodaydate(Date todaydate) {
		this.todaydate = todaydate;
	}

	public String getChargebackStatus() {
		return chargebackStatus;
	}

	public void setChargebackStatus(String chargebackStatus) {
		this.chargebackStatus = chargebackStatus;
	}

	public String getCaseId() {
		return caseId;
	}

	public void setCaseId(String caseId) {
		this.caseId = caseId;
	}

	public String getTargetDate() {
		return targetDate;
	}

	public void setTargetDate(String targetDate) {
		this.targetDate = targetDate;
	}

	public String getUserType() {
		return userType;
	}

	public void setUserType(String userType) {
		this.userType = userType;
	}

	public String getMerchantStatus() {
		return merchantStatus;
	}

	public void setMerchantStatus(String merchantStatus) {
		this.merchantStatus = merchantStatus;
	}

	public String getAdminStatus() {
		return adminStatus;
	}

	public void setAdminStatus(String adminStatus) {
		this.adminStatus = adminStatus;
	}

	public String getCurrencyNameCode() {
		return currencyNameCode;
	}

	public void setCurrencyNameCode(String currencyNameCode) {
		this.currencyNameCode = currencyNameCode;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

	public BigDecimal getChargebackAmount() {
		return chargebackAmount;
	}

	public void setChargebackAmount(BigDecimal chargebackAmount) {
		this.chargebackAmount = chargebackAmount;
	}

	public String getPgRefNum() {
		return pgRefNum;
	}

	public void setPgRefNum(String pgRefNum) {
		this.pgRefNum = pgRefNum;
	}

	public String getOrderId() {
		return orderId;
	}

	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}

}
