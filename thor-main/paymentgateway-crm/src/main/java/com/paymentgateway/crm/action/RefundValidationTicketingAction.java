package com.paymentgateway.crm.action;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger; import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.paymentgateway.commons.dao.RefundValidationDetailsDao;
import com.paymentgateway.commons.user.RefundValidation;
import com.paymentgateway.commons.user.RefundValidationDetails;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.util.DateCreater;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.crm.mongoReports.RefundValidationTicketingData;

/**
 * @author Chandan
 *
 */
public class RefundValidationTicketingAction extends AbstractSecureAction {

	private static final long serialVersionUID = 6818947178144759312L;

	private static Logger logger = LoggerFactory.getLogger(RefundValidationTicketingAction.class.getName());
	
	private String validationMerchant;
	private String refundRequestDate;
	private InputStream fileInputStream;
	private String response;
	private List<RefundValidationDetails> aaData;
	private String filename;
	private String version;
	private String versionType;
	private String totalNumOfTxns;
	String merchantPayId;
	String totalTxns;
	String noOfTxns;
	String fileFormatDate;
	
	@Autowired
	private UserDao userdao;
	
	@Autowired
	RefundValidationTicketingData refundValidationTicketingData;
	
	@Autowired
	RefundValidationDetailsDao refundValidationDetailsDao;
	
	public String execute() {
		logger.info("Inside RefundValidationAction Class, In execute method !!");
		/*List<RefundValidationDetails> refundValidationList = new ArrayList<RefundValidationDetails>();*/
		try {
			merchantPayId = userdao.getPayIdByEmailId(validationMerchant);
			setRefundRequetDate(DateCreater.toDateTimeformatCreater(refundRequestDate));
			try {
				Date date1 = DateCreater.convertStringToDateTime(refundRequestDate);
				//if(refundValidationTicketingData.isRefundReconcilationExists(merchantPayId, refundRequestDate) == false) {
					aaData = refundValidationTicketingData.getAllData(merchantPayId, date1);
					setTotalNumOfTxns(refundValidationTicketingData.getTotalNumOfRefundTxnsStatus(merchantPayId, getRefundRequetDate()));
					logger.info("No. of records in refundValidationList : " + aaData.size()); 
				/*}
				else {						
						setResponse("Version V1 already has been created !!");
						return SUCCESS;
				}*/
			} catch (Exception exception) {
				logger.error("Inside RefundValidationTicketingAction Class, Call getData Method : ", exception);
			}
		} catch (Exception exception) {
			logger.error("Inside RefundValidationTicketingAction Class, in execute method  : ", exception);
		}
		return SUCCESS;
	}
	
	public String captured() {
		logger.info("Inside RefundValidationAction Class, In captured method ++++++++++++!!");
		StringBuilder strBuilder = new StringBuilder();
		List<RefundValidation> refundValidationList = new ArrayList<RefundValidation>();
		fileFormatDate = DateCreater.formatSettleDate(getRefundRequetDate());
		try {
			merchantPayId = userdao.getPayIdByEmailId(validationMerchant);
			setRefundRequetDate(DateCreater.toDateTimeformatCreater(refundRequestDate));
			noOfTxns = "0";
			try {
				//Date date1 = DateCreater.convertStringToDateTime(refundRequestDate);
				//if(refundValidationDetailsDao.getVersionListByRequestDateVersion(merchantPayId, date1, "V1").size() == 0) {
				if(refundValidationTicketingData.isRefundReconcilationExists(merchantPayId, refundRequestDate) == false) {
					refundValidationList = refundValidationTicketingData.getCapturedData(merchantPayId, refundRequestDate);
					//noOfTxns = String.valueOf(refundValidationList.size());
					if(refundValidationList != null) {
						logger.info("No. of records in refundValidationList : " + refundValidationList.size());
					}
				}
				else {						
						//setResponse("Version V1 already has been created !!");
						return SUCCESS;
				}
			} catch (Exception exception) {
				logger.error("Inside RefundValidationTicketingAction Class, Call getCapturedData Method : ", exception);
			}	
			
			for (RefundValidation refundValidation : refundValidationList) {
				
				String seperator = "|";				
				
				strBuilder.append(refundValidation.getOrderId());
				strBuilder.append(seperator);
				strBuilder.append(refundValidation.getRefundTag());
				strBuilder.append(seperator);
				//strBuilder.append(Amount.removeDecimalAmount(refundValidation.getRefundAmt(),doc.getString(FieldType.CURRENCY_CODE.toString())));
				strBuilder.append(refundValidation.getRefundAmt());
				strBuilder.append(seperator);
				strBuilder.append(refundValidation.getBankTxnId());
				strBuilder.append(seperator);
				strBuilder.append(refundValidation.getRefundStatus());
				strBuilder.append(seperator);
				strBuilder.append(refundValidation.getRefundProcessDate());
				strBuilder.append(seperator);
				strBuilder.append(refundValidation.getRefundBankTxnId().toString());
				strBuilder.append(seperator);
				strBuilder.append(refundValidation.getCancelTxnDate());
				strBuilder.append(seperator);
				//strBuilder.append(Amount.removeDecimalAmount(doc.getString(FieldType.AMOUNT.toString()),doc.getString(FieldType.CURRENCY_CODE.toString())));
				strBuilder.append(refundValidation.getBookingTxnAmt());
				strBuilder.append(seperator);
				strBuilder.append(refundValidation.getCancelTxnId().toString());
				strBuilder.append(seperator);
				if(StringUtils.isBlank(refundValidation.getRemarks())) {
					strBuilder.append("null");
				} else {
					strBuilder.append(refundValidation.getRemarks());
				}
				strBuilder.append("\r\n");
			}
			logger.info("Batch has been created successfully !!");
			if(refundValidationList.size() > 0) {
				String FILE_EXTENSION = ".txt";
				String merchantName = "";
				merchantName = userdao.getBusinessNameByEmailId(getValidationMerchant());
				String path = PropertiesManager.propertiesMap.get("RefundValidationFilePath");
				DateFormat df = new SimpleDateFormat("yyyyMMddhhmmss");
				filename = "refundvalidation_PAYMENT_GATEWAY_NBDR_" + fileFormatDate + "_V1" + FILE_EXTENSION;
				File file = new File(path + filename);
				file.createNewFile();
				FileWriter writer = new FileWriter(file);
				writer.write(strBuilder.toString());
				writer.close();
				List<String> lines = FileUtils.readLines(file);
			    noOfTxns=String.valueOf(lines.size());
				RefundValidationDetails refundValidationDetails = new RefundValidationDetails();
				refundValidationDetails.setCreatedDate(new Date());
				refundValidationDetails.setUpdatedDate(new Date());
				refundValidationDetails.setFileVersion("V1");
				refundValidationDetails.setFileName(filename);
				refundValidationDetails.setPayId(merchantPayId);
				refundValidationDetails.setTotalTxns(totalTxns);
				refundValidationDetails.setNoOfTxns(noOfTxns);
				refundValidationDetails.setRefundRequestDate(DateCreater.convertStringToDateTime(refundRequestDate));
				refundValidationDetails.setVersionType("Captured");
				refundValidationDetailsDao.create(refundValidationDetails);
				
				setResponse("File has been saved successfully on disk !!");
			} else {
				setResponse("There is no data available in selected date range !!");
				return INPUT;
			}
			logger.info("File has been saved successfully on disk !!");		
			
		} catch (Exception exception) {
			logger.error("Inside RefundValidationTicketingAction Class, in captured method  : ", exception);
		}
		
		return SUCCESS;
	}
	
	public String others() {
		logger.info("Inside RefundValidationAction Class, In others method !!");
		StringBuilder strBuilder = new StringBuilder();
		List<RefundValidation> refundValidationList = new ArrayList<RefundValidation>();
		fileFormatDate = DateCreater.formatSettleDate(getRefundRequetDate());
		try {
			if(getValidationMerchant().contains(",")) {
				String[] arrValidationMerchant = getValidationMerchant().split(",");
				setValidationMerchant(arrValidationMerchant[arrValidationMerchant.length - 1]);
			}
			if(getRefundRequetDate().contains(",")) {
				String[] arrRefundRequetDate = getRefundRequetDate().split(",");
				setValidationMerchant(arrRefundRequetDate[arrRefundRequetDate.length - 1]);
			}
			merchantPayId = userdao.getPayIdByEmailId(getValidationMerchant().trim());
			setRefundRequetDate(DateCreater.toDateTimeformatCreater(refundRequestDate));
			try {
				Date date1 = DateCreater.formatStringToDate(refundRequestDate);
				//if(refundValidationDetailsDao.getVersionListByRequestDate(date1, "V1").size() == 0) {
					refundValidationList = refundValidationTicketingData.getOthersData(merchantPayId, refundRequestDate);
					
					noOfTxns = String.valueOf(refundValidationList.size());
					logger.info("No. of records in refundValidationList : " + refundValidationList.size()); 
				/*}
				else {						
						setResponse("Version V1 already has been created !!");
						return SUCCESS;
				}*/
			} catch (Exception exception) {
				logger.error("Inside RefundValidationTicketingAction Class, Call getOthersData Method : ", exception);
			}	
			
			for (RefundValidation refundValidation : refundValidationList) {
				
				String seperator = "|";				
				
				strBuilder.append(refundValidation.getOrderId());
				strBuilder.append(seperator);
				strBuilder.append(refundValidation.getRefundTag());
				strBuilder.append(seperator);
				//strBuilder.append(Amount.removeDecimalAmount(refundValidation.getRefundAmt(),doc.getString(FieldType.CURRENCY_CODE.toString())));
				strBuilder.append(refundValidation.getRefundAmt());
				strBuilder.append(seperator);
				strBuilder.append(refundValidation.getBankTxnId());
				strBuilder.append(seperator);
				strBuilder.append(refundValidation.getRefundStatus());
				strBuilder.append(seperator);
				strBuilder.append(refundValidation.getRefundProcessDate());
				strBuilder.append(seperator);
				strBuilder.append(refundValidation.getRefundBankTxnId().toString());
				strBuilder.append(seperator);
				strBuilder.append(refundValidation.getCancelTxnDate());
				strBuilder.append(seperator);
				//strBuilder.append(Amount.removeDecimalAmount(doc.getString(FieldType.AMOUNT.toString()),doc.getString(FieldType.CURRENCY_CODE.toString())));
				strBuilder.append(refundValidation.getBookingTxnAmt());
				strBuilder.append(seperator);
				strBuilder.append(refundValidation.getCancelTxnId().toString());
				strBuilder.append(seperator);
				if(StringUtils.isBlank(refundValidation.getRemarks())) {
					strBuilder.append("null");
				} else {
					strBuilder.append(refundValidation.getRemarks());
				}
				strBuilder.append("\r\n");
			}
			logger.info("Batch has been created successfully !!");
				String FILE_EXTENSION = ".txt";
				filename = "refundvalidation_PAYMENT_GATEWAY_OTHERS_NBDR_" + fileFormatDate + FILE_EXTENSION;
				
				File file = new File(filename);
				file.createNewFile();
				FileWriter writer = new FileWriter(file);
				writer.write(strBuilder.toString());
				writer.close();
				fileInputStream = new FileInputStream(file);
				
			
		} catch (Exception exception) {
			logger.error("Inside RefundValidationTicketingAction Class, in others method  : ", exception);
		}
		
		return SUCCESS;
	}
	
	public String postSettled() {
		logger.info("Inside RefundValidationAction Class, In postSettled method !!");
		StringBuilder strBuilder = new StringBuilder();
		List<RefundValidation> refundValidationList = new ArrayList<RefundValidation>();
		fileFormatDate = DateCreater.formatSettleDate(getRefundRequetDate());
		try {
			merchantPayId = userdao.getPayIdByEmailId(validationMerchant);
			setRefundRequetDate(DateCreater.toDateTimeformatCreater(refundRequestDate));
			try {
				//Date date1 = DateCreater.convertStringToDateTime(refundRequestDate);
				/*if(refundValidationDetailsDao.getVersionListByRequestDateVersion(date1, "V1").size() == 0) {*/
					refundValidationList = refundValidationTicketingData.getPostSettledData(merchantPayId, refundRequestDate);
					if(refundValidationList != null) {
						noOfTxns = String.valueOf(refundValidationList.size());
						logger.info("No. of records in refundValidationList : " + refundValidationList.size()); 
					}
				/*}
				else {						
						setResponse("Version V1 already has been created !!");
						return SUCCESS;
				}*/
			} catch (Exception exception) {
				logger.error("Inside RefundValidationTicketingAction Class, Call getPostSettledData Method : ", exception);
			}	
			
			for (RefundValidation refundValidation : refundValidationList) {
				
				String seperator = "|";				
				
				strBuilder.append(refundValidation.getOrderId());
				strBuilder.append(seperator);
				strBuilder.append(refundValidation.getRefundTag());
				strBuilder.append(seperator);
				//strBuilder.append(Amount.removeDecimalAmount(refundValidation.getRefundAmt(),doc.getString(FieldType.CURRENCY_CODE.toString())));
				strBuilder.append(refundValidation.getRefundAmt());
				strBuilder.append(seperator);
				strBuilder.append(refundValidation.getBankTxnId());
				strBuilder.append(seperator);
				strBuilder.append(refundValidation.getRefundStatus());
				strBuilder.append(seperator);
				strBuilder.append(refundValidation.getRefundProcessDate());
				strBuilder.append(seperator);
				strBuilder.append(refundValidation.getRefundBankTxnId().toString());
				strBuilder.append(seperator);
				strBuilder.append(refundValidation.getCancelTxnDate());
				strBuilder.append(seperator);
				//strBuilder.append(Amount.removeDecimalAmount(doc.getString(FieldType.AMOUNT.toString()),doc.getString(FieldType.CURRENCY_CODE.toString())));
				strBuilder.append(refundValidation.getBookingTxnAmt());
				strBuilder.append(seperator);
				strBuilder.append(refundValidation.getCancelTxnId().toString());
				strBuilder.append(seperator);
				if(StringUtils.isBlank(refundValidation.getRemarks())) {
					strBuilder.append("null");
				} else {
					strBuilder.append(refundValidation.getRemarks());
				}
				strBuilder.append("\r\n");
			}
			logger.info("Batch has been created successfully !!");
			if(refundValidationList.size() > 0) {
				String version = "";
				Date refundDate = DateCreater.convertStringToDateTime(refundRequestDate);
				version = refundValidationDetailsDao.getVersionListByRequestDate(refundDate).get(0).getFileVersion();
				version  = version.substring(1, 2);
				version = "V" + String.valueOf(Integer.parseInt(version) +1);
				String FILE_EXTENSION = ".txt";
				String merchantName = "";
				merchantName = userdao.getBusinessNameByEmailId(getValidationMerchant());
				//String path = "D:/home/refundValidation/";
				String path = PropertiesManager.propertiesMap.get("RefundValidationFilePath");
				DateFormat df = new SimpleDateFormat("yyyyMMddhhmmss");
				filename = "refundvalidation_PAYMENT_GATEWAY_NBDR_" + fileFormatDate + "_" + version + FILE_EXTENSION;
				
				File file = new File(path + filename);
				file.createNewFile();
				//Write Content
				FileWriter writer = new FileWriter(file);
				writer.write(strBuilder.toString());
				writer.close();
				//fileInputStream = new FileInputStream(file);
				RefundValidationDetails refundValidationDetails = new RefundValidationDetails();
				refundValidationDetails.setCreatedDate(new Date());
				refundValidationDetails.setUpdatedDate(new Date());
				refundValidationDetails.setFileVersion(version);
				refundValidationDetails.setFileName(filename);
				refundValidationDetails.setPayId(merchantPayId);
				refundValidationDetails.setTotalTxns(totalTxns);
				refundValidationDetails.setNoOfTxns(noOfTxns);
				refundValidationDetails.setRefundRequestDate(DateCreater.convertStringToDateTime(refundRequestDate));
				refundValidationDetails.setVersionType("PostSettled");
				refundValidationDetailsDao.create(refundValidationDetails);
				
				setResponse("File has been saved successfully on disk !!");
			} else {
				setResponse("There is no data available in selected date range !!");
				return SUCCESS;
			}
			logger.info("File has been saved successfully on disk !!");		
			
		} catch (Exception exception) {
			logger.error("Inside RefundValidationTicketingAction Class, in postSettled method  : ", exception);
		}
		
		return SUCCESS;
	}
	
	public String finalVersion() {
		logger.info("Inside RefundValidationAction Class, In finalVersion method !!");
		StringBuilder strBuilder = new StringBuilder();
		List<RefundValidation> refundValidationList = new ArrayList<RefundValidation>();
		fileFormatDate = DateCreater.formatSettleDate(getRefundRequetDate());
		try {
			merchantPayId = userdao.getPayIdByEmailId(validationMerchant);
			setRefundRequetDate(DateCreater.toDateTimeformatCreater(refundRequestDate));
			try {
				//Date date1 = DateCreater.convertStringToDateTime(refundRequestDate);
				/*if(refundValidationDetailsDao.getVersionListByRequestDateVersion(date1, "V1").size() == 0) {*/
					refundValidationList = refundValidationTicketingData.getFinalVersionData(merchantPayId, refundRequestDate);
					noOfTxns = String.valueOf(refundValidationList.size());
					logger.info("No. of records in refundValidationList : " + refundValidationList.size()); 
				/*}
				else {						
						setResponse("Version V1 already has been created !!");
						return SUCCESS;
				}*/
			} catch (Exception exception) {
				logger.error("Inside RefundValidationTicketingAction Class, Call getFinalVersionData Method : ", exception);
			}	
			
			for (RefundValidation refundValidation : refundValidationList) {
				
				String seperator = "|";				
				
				strBuilder.append(refundValidation.getOrderId());
				strBuilder.append(seperator);
				strBuilder.append(refundValidation.getRefundTag());
				strBuilder.append(seperator);
				//strBuilder.append(Amount.removeDecimalAmount(refundValidation.getRefundAmt(),doc.getString(FieldType.CURRENCY_CODE.toString())));
				strBuilder.append(refundValidation.getRefundAmt());
				strBuilder.append(seperator);
				strBuilder.append(refundValidation.getBankTxnId());
				strBuilder.append(seperator);
				strBuilder.append(refundValidation.getRefundStatus());
				strBuilder.append(seperator);
				strBuilder.append(refundValidation.getRefundProcessDate());
				strBuilder.append(seperator);
				strBuilder.append(refundValidation.getRefundBankTxnId().toString());
				strBuilder.append(seperator);
				strBuilder.append(refundValidation.getCancelTxnDate());
				strBuilder.append(seperator);
				//strBuilder.append(Amount.removeDecimalAmount(doc.getString(FieldType.AMOUNT.toString()),doc.getString(FieldType.CURRENCY_CODE.toString())));
				strBuilder.append(refundValidation.getBookingTxnAmt());
				strBuilder.append(seperator);
				strBuilder.append(refundValidation.getCancelTxnId().toString());
				strBuilder.append(seperator);
				if(StringUtils.isBlank(refundValidation.getRemarks())) {
					strBuilder.append("null");
				} else {
					strBuilder.append(refundValidation.getRemarks());
				}
				strBuilder.append("\r\n");
			}
			logger.info("Batch has been created successfully !!");
			if(refundValidationList.size() > 0) {
				String version = "";
				Date refundDate = DateCreater.convertStringToDateTime(refundRequestDate);
				version = refundValidationDetailsDao.getVersionListByRequestDate(refundDate).get(0).getFileVersion();
				version  = version.substring(1, 2);
				version = "V" + String.valueOf(Integer.parseInt(version) +1);
				String FILE_EXTENSION = ".txt";
				String merchantName = "";
				merchantName = userdao.getBusinessNameByEmailId(getValidationMerchant());
				//String path = "D:/home/refundValidation/";
				String path = PropertiesManager.propertiesMap.get("RefundValidationFilePath");
				DateFormat df = new SimpleDateFormat("yyyyMMddhhmmss");
				filename = "refundvalidation_PAYMENT_GATEWAY_NBDR_" + fileFormatDate + "_" + version + FILE_EXTENSION;
				
				File file = new File(path + filename);
				file.createNewFile();
				//Write Content
				FileWriter writer = new FileWriter(file);
				writer.write(strBuilder.toString());
				writer.close();
				//fileInputStream = new FileInputStream(file);
				RefundValidationDetails refundValidationDetails = new RefundValidationDetails();
				refundValidationDetails.setCreatedDate(new Date());
				refundValidationDetails.setUpdatedDate(new Date());
				refundValidationDetails.setFileVersion(version);
				refundValidationDetails.setFileName(filename);
				refundValidationDetails.setPayId(merchantPayId);
				refundValidationDetails.setTotalTxns(totalTxns);
				refundValidationDetails.setNoOfTxns(noOfTxns);
				refundValidationDetails.setRefundRequestDate(DateCreater.convertStringToDateTime(refundRequestDate));
				refundValidationDetails.setVersionType("FinalVersion");
				refundValidationDetailsDao.create(refundValidationDetails);
				
				setResponse("File has been saved successfully on disk !!");
			} else {
				setResponse("There is no data available in selected date range !!");
				return SUCCESS;
			}
			logger.info("File has been saved successfully on disk !!");		
			
		} catch (Exception exception) {
			logger.error("Inside RefundValidationTicketingAction Class, in finalVersion method  : ", exception);
		}
		
		return SUCCESS;
	}
	
	public String download() {
		logger.info("Inside RefundValidationAction Class, In download download !!");
		
		try {
			
				//String path = "D:/home/refundValidation/";
				String path = PropertiesManager.propertiesMap.get("RefundValidationFilePath");
				/*if(getFilename().contains(",")) {
					String[] arrFileName = getFilename().split(",");
					setFilename(arrFileName[arrFileName.length - 1]);
				}*/
				File file = new File(path + getFilename().trim());
				fileInputStream = new FileInputStream(file);
				
				
				/*setFilename("");
				fileInputStream.reset();*/
				
				//setResponse("File has been saved successfully on disk !!");
			
		} catch (Exception exception) {
			logger.error("Inside RefundValidationTicketingAction Class, in download method  : ", exception);
		}
		
		return SUCCESS;
	}
	
	public String refresh() {
		logger.info("Inside RefundValidationAction Class, In refresh download !!");
		List<RefundValidationDetails> refundValidationDetailsList = new ArrayList<RefundValidationDetails>() ;
			
			
		//String versionType = "";
		try {			
				//String path = "D:/home/refundValidation/";
				String path = PropertiesManager.propertiesMap.get("RefundValidationFilePath");
				/*if(getFilename().contains(",")) {
					String[] arrFileName = getFilename().split(",");
					setFilename(arrFileName[arrFileName.length - 1]);
				}*/
				File file = new File(path + getFilename().trim());
				String newFileName = "";
				if(getFilename().contains("_")) {
					String[] arrVersion = getFilename().split("_");
					String version = "", newVersion = "";
					version = arrVersion[arrVersion.length - 1].replace(".txt", "");
					newVersion = version + "_1";					
					newFileName = getFilename().replace(version, newVersion);
				}
				File newFile = new File(path + newFileName);
				if(newFile.exists()) {
					newFile = new File(path + createNewFileName(newFileName));
				}
				newFile.createNewFile();
				copyFileUsingStream(file, newFile);
				
				merchantPayId = userdao.getPayIdByEmailId(validationMerchant);
				setRefundRequetDate(DateCreater.toDateTimeformatCreater(refundRequestDate));
				Date dtRefundRequest = DateCreater.convertStringToDateTime(refundRequestDate);
				refundValidationDetailsList = refundValidationDetailsDao.getVersionListByRequestDateVersion(merchantPayId, dtRefundRequest, getVersion());
				/*if(refundValidationDetailsList.size() > 0) {
					versionType =  refundValidationDetailsList.get(0).getVersionType();
				}*/
				if(versionType.equals("Captured")) {
					postRefresh(refundValidationDetailsList.get(0));
				} else if(versionType.equals("PostSettled")) {
					postRefresh(refundValidationDetailsList.get(0));
				} else if(versionType.equals("FinalVersion")) {
					finalVersionRefresh(refundValidationDetailsList.get(0));
				}
				
				//fileInputStream = new FileInputStream(file);
				
				setResponse("File has been refreshed successfully !!");
			
		} catch (Exception exception) {
			logger.error("Inside RefundValidationTicketingAction Class, in refresh method  : ", exception);
		}
		
		return SUCCESS;
	}
	
	public void postRefresh(RefundValidationDetails refundValidationDetails) {
		logger.info("Inside RefundValidationAction Class, In postRefresh method !!");
		StringBuilder strBuilder = new StringBuilder();
		List<RefundValidation> refundValidationList = new ArrayList<RefundValidation>();
		
		try {
			merchantPayId = userdao.getPayIdByEmailId(validationMerchant);
			//setRefundRequetDate(DateCreater.toDateTimeformatCreater(refundRequestDate));
			try {
				//Date date1 = DateCreater.convertStringToDateTime(refundRequestDate);
				/*if(refundValidationDetailsDao.getVersionListByRequestDateVersion(date1, "V1").size() == 0) {*/
					refundValidationList = refundValidationTicketingData.getPostSettledData(merchantPayId, refundRequestDate);
					noOfTxns = String.valueOf(refundValidationList.size());
					logger.info("No. of records in refundValidationList : " + refundValidationList.size()); 
				/*}
				else {						
						setResponse("Version V1 already has been created !!");
						return SUCCESS;
				}*/
			} catch (Exception exception) {
				logger.error("Inside RefundValidationTicketingAction Class, Call getPostSettledData Method : ", exception);
			}	
			
			for (RefundValidation refundValidation : refundValidationList) {
				
				String seperator = "|";				
				
				strBuilder.append(refundValidation.getOrderId());
				strBuilder.append(seperator);
				strBuilder.append(refundValidation.getRefundTag());
				strBuilder.append(seperator);
				//strBuilder.append(Amount.removeDecimalAmount(refundValidation.getRefundAmt(),doc.getString(FieldType.CURRENCY_CODE.toString())));
				strBuilder.append(refundValidation.getRefundAmt());
				strBuilder.append(seperator);
				strBuilder.append(refundValidation.getBankTxnId());
				strBuilder.append(seperator);
				strBuilder.append(refundValidation.getRefundStatus());
				strBuilder.append(seperator);
				strBuilder.append(refundValidation.getRefundProcessDate());
				strBuilder.append(seperator);
				strBuilder.append(refundValidation.getRefundBankTxnId().toString());
				strBuilder.append(seperator);
				strBuilder.append(refundValidation.getCancelTxnDate());
				strBuilder.append(seperator);
				//strBuilder.append(Amount.removeDecimalAmount(doc.getString(FieldType.AMOUNT.toString()),doc.getString(FieldType.CURRENCY_CODE.toString())));
				strBuilder.append(refundValidation.getBookingTxnAmt());
				strBuilder.append(seperator);
				strBuilder.append(refundValidation.getCancelTxnId().toString());
				strBuilder.append(seperator);
				if(StringUtils.isBlank(refundValidation.getRemarks())) {
					strBuilder.append("null");
				} else {
					strBuilder.append(refundValidation.getRemarks());
				}
				strBuilder.append("\r\n");
			}
			logger.info("Batch has been created successfully !!");
			if(refundValidationList.size() > 0) {
				//String path = "D:/home/refundValidation/";
				String path = PropertiesManager.propertiesMap.get("RefundValidationFilePath");
				File file = new File(path + filename);
				//Write Content
				FileWriter writer = new FileWriter(file, true);
				writer.write(strBuilder.toString());
				writer.close();
				try {
				    List<String> lines = FileUtils.readLines(file);
				    refundValidationDetails.setNoOfTxns(String.valueOf(lines.size()));
					refundValidationDetailsDao.update(refundValidationDetails);
				} catch (IOException e) {
				    // TODO Auto-generated catch block
					logger.error("Inside RefundValidationTicketingAction Class, in postRefresh method, getLines  : ", e);
				}
				
				
				//setResponse("File has been saved successfully on disk !!");
			} /*else {
				setResponse("There is no data available in selected date range !!");
			}
			logger.info("File has been saved successfully on disk !!");		*/
			
		} catch (Exception exception) {
			logger.error("Inside RefundValidationTicketingAction Class, in postRefresh method  : ", exception);
		}
	}
	
	public void finalVersionRefresh(RefundValidationDetails refundValidationDetails) {
		logger.info("Inside RefundValidationAction Class, In finalVersion method !!");
		StringBuilder strBuilder = new StringBuilder();
		List<RefundValidation> refundValidationList = new ArrayList<RefundValidation>();
		
		try {
			merchantPayId = userdao.getPayIdByEmailId(validationMerchant);
			setRefundRequetDate(DateCreater.toDateTimeformatCreater(refundRequestDate));
			try {
				//Date date1 = DateCreater.convertStringToDateTime(refundRequestDate);
				/*if(refundValidationDetailsDao.getVersionListByRequestDateVersion(date1, "V1").size() == 0) {*/
					refundValidationList = refundValidationTicketingData.getFinalVersionData(merchantPayId, refundRequestDate);
					noOfTxns = String.valueOf(refundValidationList.size());
					logger.info("No. of records in refundValidationList : " + refundValidationList.size()); 
				/*}
				else {						
						setResponse("Version V1 already has been created !!");
						return SUCCESS;
				}*/
			} catch (Exception exception) {
				logger.error("Inside RefundValidationTicketingAction Class, Call getFinalVersionData Method : ", exception);
			}	
			
			for (RefundValidation refundValidation : refundValidationList) {
				
				String seperator = "|";				
				
				strBuilder.append(refundValidation.getOrderId());
				strBuilder.append(seperator);
				strBuilder.append(refundValidation.getRefundTag());
				strBuilder.append(seperator);
				//strBuilder.append(Amount.removeDecimalAmount(refundValidation.getRefundAmt(),doc.getString(FieldType.CURRENCY_CODE.toString())));
				strBuilder.append(refundValidation.getRefundAmt());
				strBuilder.append(seperator);
				strBuilder.append(refundValidation.getBankTxnId());
				strBuilder.append(seperator);
				strBuilder.append(refundValidation.getRefundStatus());
				strBuilder.append(seperator);
				strBuilder.append(refundValidation.getRefundProcessDate());
				strBuilder.append(seperator);
				strBuilder.append(refundValidation.getRefundBankTxnId().toString());
				strBuilder.append(seperator);
				strBuilder.append(refundValidation.getCancelTxnDate());
				strBuilder.append(seperator);
				//strBuilder.append(Amount.removeDecimalAmount(doc.getString(FieldType.AMOUNT.toString()),doc.getString(FieldType.CURRENCY_CODE.toString())));
				strBuilder.append(refundValidation.getBookingTxnAmt());
				strBuilder.append(seperator);
				strBuilder.append(refundValidation.getCancelTxnId().toString());
				strBuilder.append(seperator);
				if(StringUtils.isBlank(refundValidation.getRemarks())) {
					strBuilder.append("null");
				} else {
					strBuilder.append(refundValidation.getRemarks());
				}
				strBuilder.append("\r\n");
			}
			logger.info("Batch has been created successfully !!");
			if(refundValidationList.size() > 0) {
				/*String version = "";
				Date refundDate = DateCreater.convertStringToDateTime(refundRequestDate);
				version = refundValidationDetailsDao.getVersionListByRequestDate(refundDate).get(0).getFileVersion();
				version  = version.substring(1, 2);
				version = "V" + String.valueOf(Integer.parseInt(version) +1);
				String FILE_EXTENSION = ".txt";
				String merchantName = "";
				merchantName = userdao.getBusinessNameByEmailId(getValidationMerchant());*/
				//String path = "D:/home/refundValidation/";
				String path = PropertiesManager.propertiesMap.get("RefundValidationFilePath");
				/*DateFormat df = new SimpleDateFormat("yyyyMMddhhmmss");
				filename = "refundvalidation_" + merchantName + "_NBDR_" + df.format(new Date()) + "_" + version + FILE_EXTENSION;*/
				
				File file = new File(path + filename);
				//file.createNewFile();
				//Write Content
				FileWriter writer = new FileWriter(file, true);
				writer.write(strBuilder.toString());
				writer.close();
				try {
				    List<String> lines = FileUtils.readLines(file);
				    refundValidationDetails.setNoOfTxns(String.valueOf(lines.size()));
					refundValidationDetailsDao.update(refundValidationDetails);
				} catch (IOException e) {
				    // TODO Auto-generated catch block
					logger.error("Inside RefundValidationTicketingAction Class, in finalVersionRefresh method, getLines  : ", e);
				}
				
				setResponse("File has been saved successfully on disk !!");
			} else {
				setResponse("There is no data available in selected date range !!");
			}
			logger.info("File has been saved successfully on disk !!");		
			
		} catch (Exception exception) {
			logger.error("Inside RefundValidationTicketingAction Class, in finalVersionRefresh method  : ", exception);
		}		
	}
	private void copyFileUsingStream(File source, File dest) throws IOException {
	    InputStream is = null;
	    OutputStream os = null;
	    try {
	        is = new FileInputStream(source);
	        os = new FileOutputStream(dest);
	        byte[] buffer = new byte[1024];
	        int length;
	        while ((length = is.read(buffer)) > 0) {
	            os.write(buffer, 0, length);
	        }
	    } catch (Exception exception) {
			logger.error("Inside RefundValidationTicketingAction Class, in copyFileUsingStream method  : ", exception);
		} finally {
	        is.close();
	        os.close();
	    }
	}
	
	private String createNewFileName(String currentfileName) {
		String newFileName = "";
		//String path = "D:/home/refundValidation/";
		String path = PropertiesManager.propertiesMap.get("RefundValidationFilePath");
		File file = new File(path + currentfileName);
		if(file.exists()) {
			String[] arrVersion = file.getAbsolutePath().split("_");
			String version = "", newVersion = "";
			version = arrVersion[arrVersion.length - 1].replace(".txt", "");
			//String[] arrNewVersion = version.split("_");
			newVersion = arrVersion[arrVersion.length - 2] + "_" + String.valueOf(Integer.parseInt(version) + 1);	
			String oldVersion = arrVersion[arrVersion.length - 2] + "_" + version;
			newFileName = currentfileName.replace(oldVersion, newVersion);
			File newFile =  new File(path + newFileName);
			if(newFile.exists()) {
				createNewFileName(newFileName);
			}
		}
		
		return newFileName;
	}
	
	public String getValidationMerchant() {
		return validationMerchant;
	}
	public void setValidationMerchant(String validationMerchant) {
		this.validationMerchant = validationMerchant;
	}
	public String getRefundRequetDate() {
		return refundRequestDate;
	}
	public void setRefundRequetDate(String refundRequetDate) {
		this.refundRequestDate = refundRequetDate;
	}
	public InputStream getFileInputStream() {
		return fileInputStream;
	}
	public void setFileInputStream(InputStream fileInputStream) {
		this.fileInputStream = fileInputStream;
	}
	public String getResponse() {
		return response;
	}
	public void setResponse(String response) {
		this.response = response;
	}
	public List<RefundValidationDetails> getAaData() {
		return aaData;
	}

	public void setAaData(List<RefundValidationDetails> aaData) {
		this.aaData = aaData;
	}

	public String getFilename() {
		return filename;
	}
	public void setFilename(String filename) {
		this.filename = filename;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getVersionType() {
		return versionType;
	}

	public void setVersionType(String versionType) {
		this.versionType = versionType;
	}

	public String getTotalNumOfTxns() {
		return totalNumOfTxns;
	}

	public void setTotalNumOfTxns(String totalNumOfTxns) {
		this.totalNumOfTxns = totalNumOfTxns;
	}

}
