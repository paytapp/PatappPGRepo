package com.paymentgateway.crm.action;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.ActionSupport;
import com.paymentgateway.commons.dao.ChargebackDao;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.user.Chargeback;
import com.paymentgateway.commons.user.ChargebackComment;
import com.paymentgateway.commons.util.Amount;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.CurrencyTypes;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.TransactionManager;
import com.paymentgateway.crm.actionBeans.RefundCommunicator;
import com.paymentgateway.crm.mongoReports.TxnReports;

public class ChargeBackFileDownlodAction extends ActionSupport {

	private static final long serialVersionUID = 1L;
	private static Logger logger = LoggerFactory.getLogger(ChargeBackFileDownlodAction.class.getName());

	private String payId;
	private String caseId;
	private String documentId;
	private String fileLocation;
	private String fileName;
	private String imageName;
	private InputStream fileInputStream;
	private ZipInputStream zipInputStream;
	private ChargebackComment chargebackcomment = null;
	private String downloadBtnName;

	// REFUND
	private String orderId;
	private String pgRefNum;
	private String CurrencyCode;
	private String amount;
	private String txnType;
	private String refundFlag;
	private String refundAmount;
	private String response;
	private String refundedAmount;
	private String refundAvailable;

	@Autowired
	private RefundCommunicator refundCommunicator;

	@Autowired
	private TxnReports txnReport;
	@Autowired
	ChargebackDao chargebackDao;

	private Chargeback chargeback = new Chargeback();
	
	List<FileInputStream> ListOfFileInputStream = new ArrayList<FileInputStream>();

	/*
	 * public void downloadExecute() { if
	 * (downloadBtnName.equalsIgnoreCase("CommentFile")) { execute();
	 * 
	 * } else { logger.info("executeChargebackdownloadFile");
	 * executeChargebackdownloadFile();
	 * 
	 * } }
	 */

	public Chargeback getChargeback() {
		return chargeback;
	}

	public void setChargeback(Chargeback chargeback) {
		this.chargeback = chargeback;
	}

	public String executeChargebackdownloadFile() {

		chargebackcomment = new ChargebackComment();
		// imageName = chargebackcomment.getImageFileName();
		try {
			fileLocation = PropertiesManager.propertiesMap.get("ChargebackCreateFilePath") + payId + "/" + caseId;
			// fileName = documentId + getFileExtension(getDocumentId());
			fileName = caseId + getFileExtension(imageName);
			File destFile = new File(fileLocation, fileName);
			fileInputStream = new FileInputStream(destFile);
			if (fileInputStream != null) {
				logger.info("download File SuccessFully");
			} else if (fileInputStream == null) {
				throw new SystemException("File Not Found");

			}

		} catch (FileNotFoundException ffe) {
			logger.error("File Not Found : ", ffe);
			return ERROR;
		} catch (NullPointerException npe) {
			logger.error("File Not Found : " , npe);
			return ERROR;
		} catch (SystemException se) {
			logger.error("File Not Found : " , se);
			return ERROR;
		}

		catch (Exception exception) {
			logger.error("File Not Found : ", exception);
			return ERROR;
		}

		return SUCCESS;
	}

	
	public String execute() {

		fileLocation = PropertiesManager.propertiesMap.get("ChargebackCommentFilePath") + payId + "/" + caseId + "/" + documentId;
		fileName = documentId + ".zip";

		String[] srcFiles = imageName.split(",");
		try {

			byte[] buffer = new byte[1024];
			FileOutputStream fos = new FileOutputStream(fileName);
			ZipOutputStream zos = new ZipOutputStream(fos);

			for (int i = 0; i < srcFiles.length; i++) {
				File srcFile = new File(fileLocation, srcFiles[i]);
				FileInputStream fis = new FileInputStream(srcFile);
				zos.putNextEntry(new ZipEntry(srcFile.getName()));

				int length;

				while ((length = fis.read(buffer)) > 0) {
					zos.write(buffer, 0, length);
				}
				zos.closeEntry();
			}
			File file = new File(fileName);
			fileInputStream = new FileInputStream(file);

			zos.close();
			if (fileInputStream != null) {
				logger.info("download File SuccessFully");
			} else if (fileInputStream == null) {
				throw new SystemException("File Not Found");
			}
		} catch (IOException | SystemException ioe) {
			logger.error("File Not Found : " , ioe);
			return ERROR;
		}

		return SUCCESS;
	}

	public String chargeBackRefundProcess() {
		try {
			JSONObject json = new JSONObject();
			json.put(FieldType.ORDER_ID.getName(), getOrderId());
			json.put(FieldType.PAY_ID.getName(), getPayId());
			json.put(FieldType.PG_REF_NUM.getName(), getPgRefNum());
			json.put(FieldType.CURRENCY_CODE.getName(), CurrencyTypes.getNamefromCode(getCurrencyCode()));
			json.put(FieldType.TXNTYPE.getName(), getTxnType());
			json.put(FieldType.REFUND_FLAG.getName(), getRefundFlag());
			json.put(FieldType.AMOUNT.getName(), Amount.formatAmount(getRefundAmount(), getCurrencyCode()));
			json.put(FieldType.REFUND_ORDER_ID.getName(), "LP" + TransactionManager.getNewTransactionId());

			logger.info("call RefundCommunicator for refund");
			response = (refundCommunicator.communicator(json));
			JSONObject jsonobject = new JSONObject(response);
			response = (String) jsonobject.get(FieldType.RESPONSE_MESSAGE.getName());

			if (caseId != null)
				setChargeback(chargebackDao.findbyId(getCaseId()));
			if (response.equals(Constants.SUCCESS.getValue())) {
				chargeback.setChargebackStatus("Refunded");
				chargeback.setId(TransactionManager.getNewTransactionId());
				chargebackDao.UpdateData(chargeback);
			}
			else{
				logger.info("Exception ChargebackRefundAction");
				return ERROR;
			}
			logger.info("refund API  response received from pg ws " + response);
		} catch (SystemException e) {
			logger.info("Exception Caught in ManualRefundProccessAction " + e);
			return ERROR;
		}
		return SUCCESS;
	}

	/*
	 * public void DownlodChargeBackFile(String payId, String fileName, String
	 * CaseId, String documentId) throws IOException { fileLocation =
	 * PropertiesManager.propertiesMap.get("DocumentPath") + payId + "/" +
	 * caseId; sorucepath = documentId + getFileExtension(getDocumentId()); File
	 * destFile = new File(fileLocation, sorucepath);
	 * 
	 * 
	 * fileInputStream = new FileInputStream(destFile);
	 * 
	 * 
	 * 
	 * 
	 * 
	 * FileOutputStream os = new FileOutputStream(destFile);
	 * 
	 * BufferedOutputStream bf = new BufferedOutputStream(os);
	 * 
	 * bf.flush(); fileInputStream = new FileInputStream(destFile); bf.close();
	 * 
	 * 
	 * }
	 */

	private String getFileExtension(String name) {
		if (name.toLowerCase().endsWith(".pdf")) {
			return ".pdf";
		} else {
			return ".csv";
		}

	}
	
//	public String[] getFileNameArray() {
//		return fileNameArray;
//	}
//
//	public void setFileNameArray(String[] fileNameArray) {
//		this.fileNameArray = fileNameArray;
//	}
	
	
	public String getPayId() {
		return payId;
	}

	public ZipInputStream getZipInputStream() {
		return zipInputStream;
	}

	public void setZipInputStream(ZipInputStream zipInputStream) {
		this.zipInputStream = zipInputStream;
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

	public String getDocumentId() {
		return documentId;
	}

	public void setDocumentId(String documentId) {
		this.documentId = documentId;
	}

	public String getFileLocation() {
		return fileLocation;
	}

	public void setFileLocation(String fileLocation) {
		this.fileLocation = fileLocation;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public InputStream getFileInputStream() {
		return fileInputStream;
	}

	public void setFileInputStream(InputStream fileInputStream) {
		this.fileInputStream = fileInputStream;
	}

	public String getImageName() {
		return imageName;
	}

	public void setImageName(String imageName) {
		this.imageName = imageName;
	}

	public String getDownloadBtnName() {
		return downloadBtnName;
	}

	public void setDownloadBtnName(String downloadBtnName) {
		this.downloadBtnName = downloadBtnName;
	}

	public String getOrderId() {
		return orderId;
	}

	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}

	public String getPgRefNum() {
		return pgRefNum;
	}

	public void setPgRefNum(String pgRefNum) {
		this.pgRefNum = pgRefNum;
	}

	public String getCurrencyCode() {
		return CurrencyCode;
	}

	public void setCurrencyCode(String currencyCode) {
		CurrencyCode = currencyCode;
	}

	public String getAmount() {
		return amount;
	}

	public void setAmount(String amount) {
		this.amount = amount;
	}

	public String getTxnType() {
		return txnType;
	}

	public void setTxnType(String txnType) {
		this.txnType = txnType;
	}

	public String getRefundFlag() {
		return refundFlag;
	}

	public void setRefundFlag(String refundFlag) {
		this.refundFlag = refundFlag;
	}

	public String getRefundAmount() {
		return refundAmount;
	}

	public void setRefundAmount(String refundAmount) {
		this.refundAmount = refundAmount;
	}

	public String getResponse() {
		return response;
	}

	public void setResponse(String response) {
		this.response = response;
	}

	public String getRefundedAmount() {
		return refundedAmount;
	}

	public void setRefundedAmount(String refundedAmount) {
		this.refundedAmount = refundedAmount;
	}

	public String getRefundAvailable() {
		return refundAvailable;
	}

	public void setRefundAvailable(String refundAvailable) {
		this.refundAvailable = refundAvailable;
	}

}
