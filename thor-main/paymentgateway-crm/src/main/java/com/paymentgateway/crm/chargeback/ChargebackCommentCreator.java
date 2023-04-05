package com.paymentgateway.crm.chargeback;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.paymentgateway.commons.dao.ChargebackDao;
import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.user.Chargeback;
import com.paymentgateway.commons.user.ChargebackComment;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.util.ChargebackEmailCreater;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.TransactionManager;
import com.paymentgateway.crm.action.AbstractSecureAction;

public class ChargebackCommentCreator extends AbstractSecureAction {

	@Autowired
	ChargebackDao chargebackDao;

	@Autowired
	private ChargebackEmailCreater chargebackEmailCreater;
	
	private static final long serialVersionUID = 8269340542345930150L;
	private static Logger logger = LoggerFactory.getLogger(ChargebackCommentCreator.class);

	private String comment;
	private String caseId;
	private String chargebackStatus;
	private String response;
	private String caseStatus;
	private File[] image;
	private String imageFileName;
	private String payId;
	private String userName;

 
	public String execute() {
		String documentId = TransactionManager.getNewTransactionId();
		Set<ChargebackComment> commentSet = null;
		String commentSenderEmailId;
		Chargeback chargeback = new Chargeback();
		ChargebackDao dao = new ChargebackDao();
		SystemException systemException = new SystemException();
		StringBuilder fileNameBuilder = new StringBuilder();
		ChargebackComment chargebackComment = new ChargebackComment();

		try {
			User sessionUser = (User) sessionMap.get(Constants.USER.getValue());
			commentSenderEmailId = sessionUser.getEmailId();
			userName = CommentByUser(commentSenderEmailId);
			chargeback = dao.findByCaseId(caseId);
//			chargeback.setStatus(commentSenderEmailId);
			
			chargebackComment.setCommentId(documentId);
			chargebackComment.setCommentBody(getComment());
			chargebackComment.setCommentSenderEmailId(userName);
			chargebackComment.setDocumentId(documentId);
			commentSet = chargeback.getChargebackComments();
			commentSet.add(chargebackComment);
			chargeback.setChargebackComments(commentSet);
			
			Map<String,File> attachedFiles=new HashMap<String, File>();
			
			if (image!= null) {
				
				String[] imageFileNameArray=imageFileName.split(",");
				for(int i=0;i<image.length;i++){
					
					File imageFile=image[i];
					chargebackComment.setImageFileName(imageFileName);
					chargeback.setDocumentId(documentId);
					chargebackComment.setDocumentId(documentId);
					commentSet.add(chargebackComment);
					
					
					try {
						if (imageFile != null && (imageFileNameArray[i].toLowerCase().endsWith(".pdf")
								|| imageFileNameArray[i].toLowerCase().endsWith(".csv"))) {
							SaveFile(chargeback.getCaseId(), imageFileNameArray[i], imageFile, chargeback.getPayId(),
									chargeback.getDocumentId());
							dao.update(chargeback);
							attachedFiles.put(imageFileNameArray[i], imageFile);
	
						} else {
							throw new SystemException(ErrorType.WrongFormat, "WrongFormat");
	
						}
	
					} catch (SystemException e) {
						logger.error("WrongFormat : ", e);
						systemException.setErrorType(ErrorType.WrongFormat);
						return ERROR;
	
					}
				
				}
			} else {

				dao.update(chargeback);
			}
			//sending mail
			
			chargebackEmailCreater.sendChargebackAddCommentEmail(chargeback, chargebackComment ,sessionUser, attachedFiles);
			
			setResponse(ErrorType.COMMENT_SUCCESSFULLY_ADDED.getResponseMessage());
			Iterator<ChargebackComment> iterator = commentSet.iterator();
			List<ChargebackComment> commentList = new LinkedList<>();
			while (iterator.hasNext()) {
				ChargebackComment comments = iterator.next();
				commentList.add(comments);

			} 
		

			return SUCCESS;
		} catch (Exception exception) {
			// logger.error("WrongFormat");
			return ERROR;
		}

	}
/*
	public void validate() {
		CrmValidator validator = new CrmValidator();

		if (validator.validateBlankField(getCaseId())) {

			addFieldError(CrmFieldType.TRANSACTION_ID.getName(), validator.getResonseObject().getResponseMessage());

		} else if (!validator.validateField(CrmFieldType.TRANSACTION_ID, getCaseId())) {

			addFieldError(CrmFieldType.TRANSACTION_ID.getName(), ErrorType.INVALID_FIELD.getResponseMessage());

		}

	}*/

	public String getResponse() {
		return response;
	}

	public void setResponse(String response) {
		this.response = response;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public String getCaseId() {
		return caseId;
	}

	public void setCaseId(String caseId) {
		this.caseId = caseId;
	}

	public String getCaseStatus() {
		return caseStatus;
	}

	public void setCaseStatus(String caseStatus) {
		this.caseStatus = caseStatus;
	}


	public File[] getImage() {
		return image;
	}

	public void setImage(File[] image) {
		this.image = image;
	}

	public String getImageFileName() {
		return imageFileName;
	}

	public void setImageFileName(String imageFileName) {
		this.imageFileName = imageFileName;
	}

	public String getPayId() {
		return payId;
	}

	public void setPayId(String payId) {
		this.payId = payId;
	}

	public String getChargebackStatus() {
		return chargebackStatus;
	}

	public void setChargebackStatus(String chargebackStatus) {
		this.chargebackStatus = chargebackStatus;
	}
	// for uploading file
	public String SaveFile(String caseId, String filename, File controlFile, String payId, String documentId)
			throws SystemException {
		String destPath;
		String saveFilename;

		/// format for storing doc is payid/caseid/document.jpg
		destPath = PropertiesManager.propertiesMap.get("ChargebackCommentFilePath") + payId + "/" + caseId + "/" + documentId;

		//saveFilename = documentId +"_"+ filename;
		if (filename.toLowerCase().endsWith(".pdf") || filename.toLowerCase().endsWith(".csv")) {
			File destFile = new File(destPath, filename);

			try {
				FileUtils.copyFile(controlFile, destFile);

			} catch (Exception exception) {
				logger.error("Exception", exception);
			}

		} // if
		else {
			throw new SystemException(ErrorType.WrongFormat, "WrongFormat");

		}

		return SUCCESS;

	}

	private String getFileExtension(String name) throws SystemException {
		if (name.toLowerCase().endsWith(".pdf")) {
			return ".pdf";
		} else if (name.toLowerCase().endsWith(".csv")) {
			return ".csv";

		} else {
			return ERROR;
		}
	}

	private static String CommentByUser(String str) {
		String name[] = str.split("@");
		for (int i = 0; i < name.length; i++) {
		}
		return name[0];
	}

}
