package com.paymentgateway.crm.chargeback;

import java.io.File;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.paymentgateway.commons.dao.ChargebackDao;
import com.paymentgateway.commons.user.Chargeback;
import com.paymentgateway.commons.user.TransactionHistory;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.util.ChargebackEmailCreater;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.CrmFieldConstants;
import com.paymentgateway.commons.util.CrmFieldType;
import com.paymentgateway.commons.util.CrmValidator;
import com.paymentgateway.commons.util.DateCreater;
import com.paymentgateway.commons.util.MopType;
import com.paymentgateway.commons.util.PaymentType;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.TransactionManager;
import com.paymentgateway.crm.action.AbstractSecureAction;
import com.paymentgateway.crm.actionBeans.RefundDetailsProvider;
import com.paymentgateway.crm.chargeback.util.CaseStatus;

public class SaveChargeback extends AbstractSecureAction {

	@Autowired
	private ChargebackDao chargebackDao;
	
	@Autowired
	private UserDao userDao;
	
	@Autowired
	private RefundDetailsProvider refundDetailsProvider;
	@Autowired
	private CrmValidator validator;
	
	@Autowired
	private ChargebackEmailCreater chargebackEmailCreater;
	
	private static final long serialVersionUID = 5254839666209321240L;
	private static Logger logger = LoggerFactory.getLogger(SaveChargeback.class.getName());

	private String Id;
	private String txnId;
	private String orderId;
	private String payId;
	private String subMerchantPayId;
	private String caseId;
	private String targetDate;
	private String comments;
	private String commentedBy;
	private String chargebackType;
	private String chargebackStatus;
	private String makeComment;
	private String documentUploadFilename;
	private String Filename;
	private File image;
	private String imageFileName;
	private String merchantStatus;
	private String adminStatus;
	private BigDecimal chargebackAmount;
	private BigDecimal otherAmount;
	private String pgRefNum;
	private String holdAmountFlag;

	private String response;
	
	private TransactionHistory transDetails = new TransactionHistory();

	@SuppressWarnings("deprecation")
	public String execute() {
		Chargeback chargeback = new Chargeback();
		SaveChargebackDocument saveChargebackDocument = new SaveChargebackDocument();
		try {
			
			BigDecimal totalChargeBackAmount;
			BigDecimal chargebackamount = getChargebackAmount();
//			BigDecimal otheramount = getOtherAmount();
//			
//			if(otheramount!=null)
//				totalChargeBackAmount = chargebackamount.add(otheramount);
//			else
				totalChargeBackAmount = chargebackamount;

				User user = (User) sessionMap.get(Constants.USER);
				chargeback.setCaseId(TransactionManager.getNewTransactionId());
				if (image != null) {
					chargeback.setDocumentId(TransactionManager.getNewTransactionId());
					chargeback.setFileName(imageFileName);

				}
				chargeback.setUpdateDate(DateCreater.defaultCurrentDateTimeType());
				//chargeback.setUpdateDate(new Date());
				chargeback.setTargetDate(getTargetDate());
				chargeback.setChargebackType(getChargebackType());
				chargeback.setChargebackStatus("New");
				chargeback.setCommentedBy(user.getBusinessName());
				chargeback.setComments(comments);
				chargeback.setId(TransactionManager.getNewTransactionId());
				
				if(StringUtils.isNotBlank(holdAmountFlag) && holdAmountFlag.equalsIgnoreCase("on")) {
					chargeback.setHoldAmountFlag(true);
				}else {
					chargeback.setHoldAmountFlag(false);
				}
				// from database

				List<TransactionHistory> refundD = refundDetailsProvider.RefundProvider(pgRefNum, payId, txnId);
				refundDetailsProvider.getAllTransactions();
				transDetails = refundDetailsProvider.getTransDetails();

				chargeback.setOrderId(transDetails.getOrderId());
				chargeback.setPayId(transDetails.getPayId());
				chargeback.setSubMerchantId(transDetails.getSubMerchantPayId());
				chargeback.setTransactionId(txnId);
				chargeback.setCreateDate(DateCreater.defaultCurrentDateTimeType());
				chargeback.setCardNumber(transDetails.getCardNumber());
				chargeback.setMopType(MopType.getmopName(transDetails.getMopType()));
				// chargeback.setStatus(transDetails.getStatus());
				chargeback.setPaymentType(PaymentType.getpaymentName(transDetails.getPaymentType()));
				chargeback.setCustEmail(transDetails.getCustEmail());
				chargeback.setInternalCustIP(transDetails.getInternalCustIP());
				chargeback.setInternalCustCountryName(transDetails.getInternalCustCountryName());
				chargeback.setInternalCardIssusserBank(transDetails.getInternalCardIssusserBank());
				chargeback.setInternalCardIssusserCountry(transDetails.getInternalCardIssusserCountry());
				chargeback.setCurrencyCode(transDetails.getCurrencyCode());
				chargeback.setCurrencyNameCode(transDetails.getCurrencyNameCode());
				//chargeback.setAmount(new BigDecimal(transDetails.getAmount()));
				chargeback.setCapturedAmount(transDetails.getCapturedAmount());
				chargeback.setAuthorizedAmount(new BigDecimal(transDetails.getAuthorizedAmount()));
				chargeback.setFixedTxnFee(transDetails.getFixedTxnFee());
				chargeback.setTdr(transDetails.getTdr());
				chargeback.setServiceTax(transDetails.getServiceTax());
				chargeback.setChargebackAmount(transDetails.getChargebackAmount());
				chargeback.setNetAmount(transDetails.getNetAmount());
				chargeback.setPercentecServiceTax(transDetails.getPercentecServiceTax());
				chargeback.setMerchantTDR(transDetails.getMerchantTDR());
				chargeback.setChargebackAmount(chargebackAmount);
				chargeback.setOtherAmount(otherAmount);
				chargeback.setPgRefNum(transDetails.getPgRefNum());
				chargeback.setTotalchargebackAmount(totalChargeBackAmount);
				chargeback.setStatus(CaseStatus.OPEN.getName());
				
				Chargeback esixtingChargeback = chargebackDao.findDuplicateChargeback(orderId);
				if(esixtingChargeback == null) {
					chargebackDao.create(chargeback);
					
					//sending create chargeback email to merchnat and user
					
					User txnUser = null;
					
					if(StringUtils.isNotBlank(chargeback.getSubMerchantId())){
						txnUser=userDao.findPayId(chargeback.getSubMerchantId());
					}else{
						txnUser = userDao.findPayId(chargeback.getPayId());
					}
					
					Map<String,File> attachedFiles=new HashMap<String, File>();
					
					if (image != null && (imageFileName.toLowerCase().endsWith(".pdf")
							|| imageFileName.toLowerCase().endsWith(".jpg") || imageFileName.toLowerCase().endsWith(".png")
							|| imageFileName.toLowerCase().endsWith(".csv"))) {
						attachedFiles.put(imageFileName, image);
					}
					
					chargebackEmailCreater.sendChargebackRaisedEmail(chargeback ,txnUser, attachedFiles);
					
				}
				addActionMessage(CrmFieldConstants.GENERATED_SUCCESSFULLY.getValue());
				
				
				
				if (image != null && (imageFileName.toLowerCase().endsWith(".pdf")
						|| imageFileName.toLowerCase().endsWith(".jpg") || imageFileName.toLowerCase().endsWith(".png")
						|| imageFileName.toLowerCase().endsWith(".csv"))) {
					saveChargebackDocument.SaveFile(chargeback.getCaseId(), imageFileName, image, payId,
							chargeback.getDocumentId());
				}

				return SUCCESS;
			//}
		}

		catch (Exception exception) {
			logger.error("Exception", exception);
			// isChargebackSucess=false;
			return ERROR;

		}

	}
	

	public void validate() {

		if (!(validator.validateBlankField(getOrderId()))) {
			if (!(validator.validateField(CrmFieldType.ORDER_ID, getOrderId()))) {
				addFieldError(CrmFieldType.ORDER_ID.getName(), validator.getResonseObject().getResponseMessage());
			}
		}
		
		
		if (!(validator.validateBlankField(getImageFileName()))) {
			
			String fileNameArray [] = getImageFileName().split(("\\."));
			
			if (fileNameArray.length > 1) {
				if (!(fileNameArray[1].trim().equalsIgnoreCase("pdf") || fileNameArray[1].trim().equalsIgnoreCase("csv"))) {
					addFieldError(CrmFieldType.FILE_NAME.getName(), validator.getResonseObject().getResponseMessage());
					setResponse("Invalid File Format Uploaded !!");
				}
			}
			
			
		}
		
	}

	public String getOrderId() {
		return orderId;
	}

	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}

	public String getPayId() {
		return payId;
	}

	public void setPayId(String payId) {
		this.payId = payId;
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

	public String getChargebackStatus() {
		return chargebackStatus;
	}

	public void setChargebackStatus(String chargebackStatus) {
		this.chargebackStatus = chargebackStatus;
	}

	public String getTxnId() {
		return txnId;
	}

	public void setTxnId(String txnId) {
		this.txnId = txnId;
	}

	public String getComments() {
		return comments;
	}

	public void setComments(String comments) {
		this.comments = comments;
	}

	public String getMakeComment() {
		return makeComment;
	}

	public void setMakeComment(String makeComment) {
		this.makeComment = makeComment;
	}

	public String getDocumentUploadFilename() {
		return documentUploadFilename;
	}

	public void setDocumentUploadFilename(String documentUploadFilename) {
		this.documentUploadFilename = documentUploadFilename;
	}

	public String getFilename() {
		return Filename;
	}

	public void setFilename(String filename) {
		Filename = filename;
	}

	public File getImage() {
		return image;
	}

	public void setImage(File image) {
		this.image = image;
	}

	public String getImageFileName() {
		return imageFileName;
	}

	public void setImageFileName(String imageFileName) {
		this.imageFileName = imageFileName;
	}

	public String getId() {
		return Id;
	}

	public void setId(String id) {
		Id = id;
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

	public BigDecimal getChargebackAmount() {
		return chargebackAmount;
	}

	public void setChargebackAmount(BigDecimal chargebackAmount) {
		this.chargebackAmount = chargebackAmount;
	}

	public BigDecimal getOtherAmount() {
		return otherAmount;
	}

	public void setOtherAmount(BigDecimal otherAmount) {
		this.otherAmount = otherAmount;
	}

	public String getPgRefNum() {
		return pgRefNum;
	}

	public void setPgRefNum(String pgRefNum) {
		this.pgRefNum = pgRefNum;
	}
	public String getSubMerchantPayId() {
		return subMerchantPayId;
	}

	public void setSubMerchantPayId(String subMerchantPayId) {
		this.subMerchantPayId = subMerchantPayId;
	}

	public String getResponse() {
		return response;
	}

	public void setResponse(String response) {
		this.response = response;
	}

	public String getHoldAmountFlag() {
		return holdAmountFlag;
	}

	public void setHoldAmountFlag(String holdAmountFlag) {
		this.holdAmountFlag = holdAmountFlag;
	}

	
	
}
