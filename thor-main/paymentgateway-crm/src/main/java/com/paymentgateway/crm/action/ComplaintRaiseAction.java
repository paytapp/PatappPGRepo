package com.paymentgateway.crm.action;

import java.io.File;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.paymentgateway.commons.user.Complaint;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.user.UserType;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.DateCreater;
import com.paymentgateway.crm.actionBeans.ComplaintRaisingService;

public class ComplaintRaiseAction extends AbstractSecureAction {

	/**
	 * @Alam
	 */
	private static final long serialVersionUID = 4591507660614845279L;

	private static Logger logger = LoggerFactory.getLogger(ComplaintRaiseAction.class.getName());

	@Autowired
	private ComplaintRaisingService complaintRaisingService;

	private String complaintId;
	private String merchantId;
	private String subMerchantId;
	private String createDate;
	private String updatedDate;
	private String createdBy;
	private String updatedBy;
	private String status;
	private String complaintType;
	private String comments;
	private String fileName;
	private String dateFrom;
	private String dateTo;
	private File[] uploadedFile;
	private int length;
	private int start;
	private int draw;
	private List<Complaint> aaData;
	private Complaint complaint = new Complaint();
	private String zipFileName;
	private InputStream fileInputStream;
	private String responseMsg;
	
	@Autowired
	private UserDao userDao;

	private User sessionUser = new User();

	public String execute() {
		logger.info("Inside execute()");
		try {
			String complaintForType=null;
			sessionUser = (User) sessionMap.get(Constants.USER.getValue());

			setStatus("Open");

			if (sessionUser.getUserType().equals(UserType.ADMIN)) {
				complaintForType ="MERCHANT";
				complaintId = complaintRaisingService.createComplaint(merchantId, subMerchantId, sessionUser, status, fileName,
						uploadedFile, comments, complaintType,complaintForType);
			} else if (sessionUser.getUserType().equals(UserType.SUBADMIN)) {
				complaintForType ="MERCHANT";
				complaintId = complaintRaisingService.createComplaint(merchantId, subMerchantId, sessionUser, status, fileName,
						uploadedFile, comments, complaintType,complaintForType);

			} else if (sessionUser.getUserType().equals(UserType.MERCHANT)) {
				if (sessionUser.isSuperMerchant()) {
					merchantId = sessionUser.getPayId();
				} else if (!sessionUser.isSuperMerchant() && StringUtils.isNotBlank(sessionUser.getSuperMerchantId())) {
					merchantId = sessionUser.getSuperMerchantId();
					subMerchantId = sessionUser.getPayId();
				} else {
					merchantId = sessionUser.getPayId();
				}
				
				complaintId = complaintRaisingService.createComplaint(merchantId, subMerchantId, sessionUser, status, fileName,
						uploadedFile, comments, complaintType,complaintForType);
			} else if (sessionUser.getUserType().equals(UserType.RESELLER)) {
				complaintForType ="MERCHANT";
				
				complaintId = complaintRaisingService.createComplaint(merchantId, subMerchantId, sessionUser, status, fileName,
						uploadedFile, comments, complaintType,complaintForType);
			}else  if(sessionUser.getUserType().equals(UserType.SUBUSER)) {
				complaintForType ="USER";
				complaintId = complaintRaisingService.createComplaint(merchantId, subMerchantId, sessionUser, status, fileName,
						uploadedFile, comments, complaintType,complaintForType);				
			}
		} catch (Exception ex) {
			logger.error("Exception Caught : ", ex);
		}
		return SUCCESS;
	}

	public String updateComplaint() {
		logger.info("Inside updateComplaint()");

		try {
			sessionUser = (User) sessionMap.get(Constants.USER.getValue());
			complaint=complaintRaisingService.updateComplaint(complaintId, status, comments, fileName, uploadedFile, sessionUser);
			if(complaint!=null && complaint.getMessage()!=null) {
				setResponseMsg(complaint.getMessage());
			}
		} catch (Exception ex) {
			logger.error("Exception Caught : ", ex);
		}
		return SUCCESS;
	}

	public String viewComplaint() {
		logger.info("Inside viewComplaint()");
		try {
			sessionUser = (User) sessionMap.get(Constants.USER.getValue());
			setDateFrom(DateCreater.toDateTimeformatCreater(dateFrom));
			setDateTo(DateCreater.formDateTimeformatCreater(dateTo));
			int totalCount;

			if (sessionUser.getUserType().equals(UserType.ADMIN)) {

			} else if (sessionUser.getUserType().equals(UserType.SUBADMIN)) {

			} else if (sessionUser.getUserType().equals(UserType.MERCHANT)) {
				createdBy = sessionUser.getEmailId();
			} else if (sessionUser.getUserType().equals(UserType.RESELLER)) {
				createdBy = sessionUser.getEmailId();
			}

			totalCount = complaintRaisingService.countComplaint(dateFrom, dateTo, merchantId, subMerchantId, createdBy, complaintType,status);
			BigInteger bigInt = BigInteger.valueOf(totalCount);
			if (getLength() == -1) {
				//setLength(bigInt.intValue());
			}

			setAaData(complaintRaisingService.viewComplaint(dateFrom, dateTo, merchantId, subMerchantId, createdBy, complaintType,
					start, length,status));
		} catch (Exception ex) {
			logger.error("Exception Caught : ", ex);
		}
		return SUCCESS;
	}

	public String viewComplaintFileDetails() {
		logger.info("Inside viewComplaintFileDetails()");
		try {
			sessionUser = (User) sessionMap.get(Constants.USER.getValue());

			complaint = complaintRaisingService.viewComplaintByComplaintId(complaintId);
		} catch (Exception ex) {
			logger.error("Exception Caught : ", ex);
		}
		return SUCCESS;
	}

	
	public String viewComplaintDetailDownloadAction() {
		logger.info("Inside viewComplaintFileDetails()");
		try {
			sessionUser = (User) sessionMap.get(Constants.USER.getValue());
			complaint =complaintRaisingService.viewComplaintDetailDownloadAction(complaintId,status);
			if (complaint.getFileInputStream()!=null && complaint.getZipFileName()!=null) {
				fileInputStream = complaint.getFileInputStream();
				zipFileName = complaint.getZipFileName();
				logger.info("File Downloaded Successfully");
			} else if (fileInputStream == null) {
				logger.error("File Not Found");
				return ERROR;
			}
		} catch (Exception ex) {
			logger.error("Exception Caught : ", ex);
		}
		return SUCCESS;
	}
	public String getComplaintId() {
		return complaintId;
	}

	public void setComplaintId(String complaintId) {
		this.complaintId = complaintId;
	}

	public String getMerchantId() {
		return merchantId;
	}

	public void setMerchantId(String merchantId) {
		this.merchantId = merchantId;
	}

	public String getSubMerchantId() {
		return subMerchantId;
	}

	public void setSubMerchantId(String subMerchantId) {
		this.subMerchantId = subMerchantId;
	}

	public String getCreateDate() {
		return createDate;
	}

	public void setCreateDate(String createDate) {
		this.createDate = createDate;
	}

	public String getUpdatedDate() {
		return updatedDate;
	}

	public void setUpdatedDate(String updatedDate) {
		this.updatedDate = updatedDate;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public String getUpdatedBy() {
		return updatedBy;
	}

	public void setUpdatedBy(String updatedBy) {
		this.updatedBy = updatedBy;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getComplaintType() {
		return complaintType;
	}

	public void setComplaintType(String complaintType) {
		this.complaintType = complaintType;
	}

	public String getComments() {
		return comments;
	}

	public void setComments(String comments) {
		this.comments = comments;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public File[] getUploadedFile() {
		return uploadedFile;
	}

	public void setUploadedFile(File[] uploadedFile) {
		this.uploadedFile = uploadedFile;
	}

	public List<Complaint> getAaData() {
		return aaData;
	}

	public void setAaData(List<Complaint> aaData) {
		this.aaData = aaData;
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

	public int getDraw() {
		return draw;
	}

	public void setDraw(int draw) {
		this.draw = draw;
	}

	public Complaint getComplaint() {
		return complaint;
	}

	public void setComplaint(Complaint complaint) {
		this.complaint = complaint;
	}

	public InputStream getFileInputStream() {
		return fileInputStream;
	}

	public void setFileInputStream(InputStream fileInputStream) {
		this.fileInputStream = fileInputStream;
	}

	public String getZipFileName() {
		return zipFileName;
	}

	public void setZipFileName(String zipFileName) {
		this.zipFileName = zipFileName;
	}

	public String getResponseMsg() {
		return responseMsg;
	}
	public void setResponseMsg(String responseMsg) {
		this.responseMsg = responseMsg;
	}

	
	
}
