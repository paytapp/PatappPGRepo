package com.paymentgateway.crm.mpa;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.paymentgateway.commons.api.Hasher;
import com.paymentgateway.commons.api.TransactionControllerServiceProvider;
import com.paymentgateway.commons.dao.MPADao;
import com.paymentgateway.commons.dao.MPAMerchantDao;
import com.paymentgateway.commons.dao.MerchantGridViewService;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.user.MPAMerchant;
import com.paymentgateway.commons.user.MerchantDetails;
import com.paymentgateway.commons.user.MerchantProcessingApplication;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.user.UserType;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.DataEncoder;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.MPAStatusType;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.StatusType;
import com.paymentgateway.commons.util.TransactionManager;
import com.paymentgateway.commons.util.UserStatusType;
import com.paymentgateway.crm.action.AbstractSecureAction;


public class MPADataAction extends AbstractSecureAction {

	private static final long serialVersionUID = -5165632139168904717L;

	@Autowired
	private MPAServicesFactory mpaServicesFactory;

	@Autowired
	private UserDao userDao;
	
	@Autowired
	private MPAMerchantService mpaMerchantService;
	
	@Autowired
	private MPADao mpaDao;
	
	@Autowired
	private MPAMerchantDao mpaMerchantDao;
	
	@Autowired
	private DataEncoder encoder;
	
	@Autowired
	private MerchantGridViewService merchantGridViewService;
	
	@Autowired
	private TransactionControllerServiceProvider transactionControllerServiceProvider;
	
	private List<MerchantDetails> aaData;
	
	
	private User sessionUser = new User();
	private String payId;
	private String merchantStatus;
	private String statusComment;
	private String businessType;
	private String byWhom;
	private File[] file;
	private String[] fileName;
	private String responseStatus;
	private String response;
	private String responseMsg;
	private String beneName;
	private String beneAccountNumber;
	private String beneIfsc;
	private String phoneNo;
	
	private MerchantProcessingApplication mpaDataByPayId = new MerchantProcessingApplication();
	MPAMerchant mpaMerchant = new MPAMerchant();

	private static Logger logger = LoggerFactory.getLogger(MPADataAction.class.getName());

	public String execute() {
		logger.info("Inside execute(), MPADataAction");
		sessionUser = (User) sessionMap.get(Constants.USER.getValue());
		User merchant = userDao.findPayId(getPayId());
		try {
			MPAMerchant mpa = mpaMerchantDao.findByPayId(getPayId());
			if(sessionUser.getUserType().equals(UserType.ADMIN)){
				mpa.setCheckerMakerType("Admin");
			}else {
				if(sessionUser.getPayId().equals(mpa.getMakerPayId())) {
					mpa.setCheckerMakerType("Maker");
				}else if(sessionUser.getPayId().equals(mpa.getCheckerPayId())) {
					mpa.setCheckerMakerType("Checker");
				}
			}
			if(merchant.isMpaOnlineFlag()==false) {
				mpa.setIsMpaOnlineFlag("NO");
			}else {
				mpa.setIsMpaOnlineFlag("YES");
			}
			setMpaMerchant(mpa);
			
		} catch (Exception e) {
			logger.error("Exception caught, " , e);
			return ERROR;
		}
		return SUCCESS;
	}
	
	public String getMPAMerchantData() {
		sessionUser = (User) sessionMap.get(Constants.USER.getValue());
		try {	
			if (StringUtils.isBlank(getMerchantStatus())) {
				return SUCCESS;
			}
			if(sessionUser.getUserType().equals(UserType.ADMIN)){
				
					aaData = encoder.encodeMerchantDetailsObj(mpaMerchantDao.getAllMPAMerchants(getBusinessType(),getMerchantStatus(),getByWhom()));
			}else if(sessionUser.getUserType().equals(UserType.SUBADMIN)) {
					aaData = encoder.encodeMerchantDetailsObj(mpaMerchantDao.getMPAMerchantsBasedOnCheckerMaker(getMerchantStatus(),getByWhom(),getBusinessType(),sessionUser.getPermissionType(),sessionUser.getPayId()));
					
			}
			
			return SUCCESS;
		} catch (Exception exception) {
			logger.error("Exception", exception);
			return ERROR;
		}
	}
	
	public String fetchMPADataByPayId() {
		logger.info("Inside fetchMPADataByPayId(), MPADataAction");
		try {
			MerchantProcessingApplication mpaData = mpaServicesFactory.getMPADataByPayId(getPayId());
		//	mpaData.seteSignResponse(new JSONObject(mpaData.geteSignResponseData()));
			setMpaDataByPayId(mpaData);

		} catch (Exception e) {
			logger.error("Exception caught, " , e);
			return ERROR;
		}
		return SUCCESS;
	}
	
	public String updateMPAStatus() {
		
		logger.info("Inside updateMPAStatus(), MPADataAction");
		
		sessionUser = (User) sessionMap.get(Constants.USER.getValue());
		String updatedBy = null; //sessionUser.getPermissionType();
		
		SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
		User user = new User();
		MPAMerchant mpaMerchant = new MPAMerchant();
		MerchantProcessingApplication mpaData = new MerchantProcessingApplication();
		
	try {
		user = userDao.findPayId(getPayId());
		mpaMerchant = mpaMerchantDao.findByPayId(getPayId());
		mpaData = mpaServicesFactory.getMPADataByPayId(getPayId());
		if(sessionUser.getUserType().equals(UserType.SUBADMIN)) {
			if(mpaMerchant.getCheckerPayId() != null && mpaMerchant.getCheckerPayId().equals(sessionUser.getPayId())) {
				updatedBy = "Checker";
			}else if(mpaMerchant.getCheckerPayId() != null && mpaMerchant.getMakerPayId().equals(sessionUser.getPayId())) {
				updatedBy = "Maker";
			}
		}
		mpaData.setStatus(getMerchantStatus());
		
		
			if (updatedBy !=null && updatedBy.equalsIgnoreCase("Maker")) {

				if (getMerchantStatus().equalsIgnoreCase("Approved") && (mpaMerchant.getCheckerStatus() != null || mpaMerchant.getAdminStatus() != null)
						&& (mpaMerchant.getCheckerStatus().equalsIgnoreCase("Rejected")
								|| mpaMerchant.getCheckerStatus().equalsIgnoreCase("Approved"))) {
				
				} else if (getMerchantStatus().equalsIgnoreCase("Approved")) {
					user.setUserStatus(UserStatusType.APPROVED);
					mpaMerchant.setUserStatus(MPAStatusType.APPROVED.getStatusCode());
				}

				if (getMerchantStatus().equalsIgnoreCase("Rejected") && (mpaMerchant.getCheckerStatus() != null || mpaMerchant.getAdminStatus() != null)
						&& (mpaMerchant.getCheckerStatus().equalsIgnoreCase("Approved")
								|| mpaMerchant.getCheckerStatus().equalsIgnoreCase("Rejected"))) {
				
				} else if (getMerchantStatus().equalsIgnoreCase("Rejected")) {
					user.setUserStatus(UserStatusType.REJECTED);
					mpaMerchant.setUserStatus(MPAStatusType.REJECTED.getStatusCode());
				}
				if (StringUtils.isNotBlank(mpaMerchant.getMakerComments())) {
					user.setMakerComments(user.getMakerComments() + "," + getStatusComment());
					mpaMerchant.setMakerComments(mpaMerchant.getMakerComments() + "," + getStatusComment());
				} else {
					user.setMakerComments(getStatusComment());
					mpaMerchant.setMakerComments(getStatusComment());
				}

				user.setMakerStatus(getMerchantStatus());
				user.setMakerStatusUpDate(formatter.format(new Date()));
				mpaMerchant.setMakerStatus(getMerchantStatus());
				mpaMerchant.setMakerStatusUpDate(formatter.format(new Date()));

				mpaData.setApprovedBy(user.getMakerName());
				
				userDao.update(user);
				mpaMerchantDao.update(mpaMerchant);
				mpaServicesFactory.updateMPADataByPayId(mpaData);
				merchantGridViewService.addUserInMap(user);
				
		}else if(updatedBy !=null && updatedBy.equalsIgnoreCase("Checker")) {
			
				if(getMerchantStatus().equalsIgnoreCase("Approved") && StringUtils.isEmpty(mpaMerchant.getAdminStatus())) {
					user.setUserStatus(UserStatusType.APPROVED);
					mpaMerchant.setUserStatus(MPAStatusType.APPROVED.getStatusCode());
				}
				if(getMerchantStatus().equalsIgnoreCase("Rejected") && StringUtils.isEmpty(mpaMerchant.getAdminStatus())) {
					user.setUserStatus(UserStatusType.REJECTED);
					mpaMerchant.setUserStatus(MPAStatusType.REJECTED.getStatusCode());
				}
				if(StringUtils.isNotBlank(user.getCheckerComments())) {
					user.setCheckerComments(user.getCheckerComments() + "," + getStatusComment());
					mpaMerchant.setCheckerComments(mpaMerchant.getCheckerComments() + "," + getStatusComment());
				}else {
					user.setCheckerComments(getStatusComment());
					mpaMerchant.setCheckerComments(getStatusComment());
				}
			
			user.setCheckerStatus(getMerchantStatus());
			user.setCheckerStatusUpDate(formatter.format(new Date()));
			mpaMerchant.setCheckerStatus(getMerchantStatus());
			mpaMerchant.setCheckerStatusUpDate(formatter.format(new Date()));
			
			mpaData.setReviewedBy(user.getCheckerName());
			
			userDao.update(user);
			mpaMerchantDao.update(mpaMerchant);
			mpaServicesFactory.updateMPADataByPayId(mpaData);
			merchantGridViewService.addUserInMap(user);
			
		}else if(sessionUser.getUserType().equals(UserType.ADMIN)){
			
			if(getMerchantStatus().equalsIgnoreCase("Approved")) {
				user.setUserStatus(UserStatusType.APPROVED);
				mpaMerchant.setUserStatus(MPAStatusType.APPROVED.getStatusCode());
			}
			if(getMerchantStatus().equalsIgnoreCase("Rejected")) {
				user.setUserStatus(UserStatusType.REJECTED);
				mpaMerchant.setUserStatus(MPAStatusType.REJECTED.getStatusCode());
			}
			
			String date = formatter.format(new Date());
			
			user.setAdminStatus(getMerchantStatus());
			user.setAdminStatusUpDate(date);
			user.setComments(getStatusComment());
			mpaMerchant.setAdminStatus(getMerchantStatus());
			mpaMerchant.setAdminStatusUpDate(date);
			mpaMerchant.setAdminComment(getStatusComment());
			
			if(StringUtils.isBlank(mpaMerchant.getCheckerStatus())) {
				user.setCheckerStatus(getMerchantStatus());
				user.setCheckerStatusUpDate(date);
				user.setCheckerName(sessionUser.getFirstName() + " " + sessionUser.getLastName());
				mpaMerchant.setCheckerStatus(getMerchantStatus());
				mpaMerchant.setCheckerStatusUpDate(date);
				mpaMerchant.setCheckerName(sessionUser.getFirstName() + " " + sessionUser.getLastName());
				mpaData.setReviewedBy(sessionUser.getFirstName() + " " + sessionUser.getLastName());
			}
			if(StringUtils.isBlank(mpaMerchant.getMakerStatus())) {
				user.setMakerStatus(getMerchantStatus());
				user.setMakerStatusUpDate(date);
				user.setMakerName(sessionUser.getFirstName() + " " + sessionUser.getLastName());
				mpaMerchant.setMakerStatus(getMerchantStatus());
				mpaMerchant.setMakerStatusUpDate(date);
				mpaMerchant.setMakerName(sessionUser.getFirstName() + " " + sessionUser.getLastName());
				mpaData.setApprovedBy(sessionUser.getFirstName() + " " + sessionUser.getLastName());
			}
			userDao.update(user);
			mpaMerchantDao.update(mpaMerchant);
			mpaServicesFactory.updateMPADataByPayId(mpaData);
			merchantGridViewService.addUserInMap(user);
		}

		mpaMerchantService.senEmailToCheckermaker(getPayId(),getMerchantStatus(),updatedBy,"");
			
		} catch (Exception e) {
			logger.error("Exception caught, " , e);
			return ERROR;
		}
		return SUCCESS;
	}
	
	
	public String updateSubMerchantMPAStatus() {

		logger.info("Inside updateMPAStatus(), MPADataAction");

		sessionUser = (User) sessionMap.get(Constants.USER.getValue());
		String updatedBy = sessionUser.getEmailId();

		SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
		String date = formatter.format(new Date());
		User user = new User();

		MerchantProcessingApplication mpaData = new MerchantProcessingApplication();

		try {
			user = userDao.findPayId(getPayId());

			mpaData = mpaDao.fetchMPADataByPayId(getPayId());

			user.setUpdatedBy(updatedBy);
			user.setUpdateDate(new Date());
			mpaData.setUpdatedBy(updatedBy);
			mpaData.setUpdatedDate(new Date());

			
			if (sessionUser.getUserType().equals(UserType.MERCHANT)
//					&& sessionUser.getPayId().equalsIgnoreCase(PropertiesManager.propertiesMap.get("KHADI_SUPER_MERCHANT_PAYID"))
//					&& Boolean.valueOf(PropertiesManager.propertiesMap.get("KHADI_SUB_USER_MODE"))
				) {

				if (getMerchantStatus().equalsIgnoreCase(MPAStatusType.APPROVED.getStatusName())) {
					mpaData.setStatus(MPAStatusType.APPROVED.getStatusCode());
					mpaData.setApprovedBy(updatedBy);
				}
				if (getMerchantStatus().equalsIgnoreCase(MPAStatusType.REJECTED.getStatusName())) {
					mpaData.setStatus(MPAStatusType.REJECTED.getStatusCode());
				}

				user.setAdminStatus(getMerchantStatus());
				user.setAdminStatusUpDate(date);

				user.setComments(getStatusComment());

				userDao.update(user);
				mpaDao.update(mpaData);

				setMerchantStatus(mpaData.getStatus());
				setStatusComment(mpaData.getComments());
				setByWhom(mpaData.getApprovedBy());
				setResponseStatus("Success");
				// mpaServicesFactory.updateMPADataByPayId(mpaData);
			}

			else if (sessionUser.getUserType().equals(UserType.ADMIN)) {

				if (getMerchantStatus().equalsIgnoreCase(MPAStatusType.APPROVED.getStatusName())) {
					mpaData.setApprovedBy(sessionUser.getEmailId());
					mpaData.setStatus(MPAStatusType.APPROVED.getStatusCode());
				}
				if (getMerchantStatus().equalsIgnoreCase(MPAStatusType.REJECTED.getStatusName())) {
					user.setUserStatus(UserStatusType.REJECTED);
					mpaData.setStatus(MPAStatusType.REJECTED.getStatusCode());
				}

				user.setAdminStatus(getMerchantStatus());
				user.setAdminStatusUpDate(date);

				mpaData.setComments(getStatusComment());

				userDao.update(user);
				mpaDao.update(mpaData);

				setMerchantStatus(mpaData.getStatus());
				setStatusComment(mpaData.getComments());
				setByWhom(mpaData.getApprovedBy());
				setResponseStatus("Success");
			}

			// mpaMerchantService.senEmailToCheckermaker(getPayId(),getMerchantStatus(),updatedBy,"");

		} catch (Exception e) {
			logger.error("Exception caught, " , e);
			return ERROR;
		}
		return SUCCESS;
	}
	
	public String statusFileUpload() {
		sessionUser = (User) sessionMap.get(Constants.USER.getValue());
		MPAMerchant mpaMerchant = mpaMerchantDao.findByPayId(getPayId());
		String updatedBy = null;
		if (sessionUser.getUserType().equals(UserType.SUBADMIN)) {
			if (mpaMerchant.getCheckerPayId() != null && mpaMerchant.getCheckerPayId().equals(sessionUser.getPayId())) {
				updatedBy = "Checker";
			} else if (mpaMerchant.getCheckerPayId() != null && mpaMerchant.getMakerPayId().equals(sessionUser.getPayId())) {
				updatedBy = "Maker";
			}
		}
		
		try {
			String uploadedBy = "";
			if(updatedBy !=null && updatedBy.equalsIgnoreCase("Checker")) {
				uploadedBy = Constants.CHECKER_FILES.getValue();
				mpaMerchant.setCheckerFileName(fileName[0]);
			}else if(updatedBy != null && updatedBy.equalsIgnoreCase("Maker")) {
				uploadedBy = Constants.MAKER_FILES.getValue();
				mpaMerchant.setMakerFileName(fileName[0]);
			}else if(sessionUser.getUserType().equals(UserType.ADMIN)){
				uploadedBy = Constants.ADMIN_FILES.getValue();
				mpaMerchant.setAdminFileName(fileName[0]);
			}
			
			if (file!= null) {
				
				String[] fileNameArray= fileName[0].split(",");
				for(int i=0;i<file.length;i++){
					
					mpaMerchantService.SaveFile(uploadedBy, fileNameArray[i], file[i],getPayId());
				} 				
				mpaMerchantDao.update(mpaMerchant);
			}
		} catch (Exception exception) {
			logger.error("Exception", exception);
			return ERROR;
		}
		return SUCCESS;
	}	
	
	public String verifyAccountDetails() {

		Map<String, String> requestMap = new HashMap<>();
		Map<String, String> respMap;

		try {
			if (StringUtils.isNotBlank(payId)) {
				requestMap.put(FieldType.PAY_ID.getName(), payId);
			}else{
				sessionUser = (User) sessionMap.get(Constants.USER.getValue());
				requestMap.put(FieldType.PAY_ID.getName(), sessionUser.getPayId());
			}
			logger.info("request PAYID for MPA bene verification : "+requestMap.get(FieldType.PAY_ID.getName()));
			
			requestMap.put(FieldType.ORDER_ID.getName(), TransactionManager.getNewTransactionId());

			if (StringUtils.isNotBlank(beneAccountNumber)) {
				requestMap.put(FieldType.BENE_ACCOUNT_NO.getName(), beneAccountNumber);
				requestMap.put(FieldType.BENE_NAME.getName(), beneName);
				requestMap.put(FieldType.IFSC_CODE.getName(), beneIfsc);
			}
			
			if(StringUtils.isNotBlank(phoneNo)){
				requestMap.put(FieldType.PHONE_NO.getName(), phoneNo);
			}

			requestMap.put(FieldType.CURRENCY_CODE.getName(), "356");
			requestMap.put(FieldType.MPA_FLAG.getName(), Constants.Y.name());
			
			requestMap.put(FieldType.HASH.getName(), Hasher.getHash(new Fields(requestMap)));
			
			
			
			respMap = transactionControllerServiceProvider.beneVerificationTransact(requestMap);
			if (respMap.get(FieldType.STATUS.getName()).equalsIgnoreCase(StatusType.VERIFIED.getName())) {
				setResponse("success");
				setBeneName(respMap.get(FieldType.BENE_NAME.getName()));
				setResponseMsg("Bank Account Successfully Verified");
			} else if (respMap.get(FieldType.STATUS.getName()).equalsIgnoreCase(StatusType.PENDING.getName())) {
				setResponse("success");
				setResponseMsg("Bank Account Verification is Pending");
			} else {
				setResponse("failed");
				setResponseMsg("Bank Account Not Verified");
			}

		} catch (SystemException e) {
			logger.error("exception " , e);
			setResponse("failed");
			setResponseMsg("Failed Due To System Error");

		}
		return SUCCESS;

	}

	public MPAMerchant getMpaMerchant() {
		return mpaMerchant;
	}

	public void setMpaMerchant(MPAMerchant mpaMerchant) {
		this.mpaMerchant = mpaMerchant;
	}

	public String[] getFileName() {
		return fileName;
	}

	public void setFileName(String[] fileName) {
		this.fileName = fileName;
	}
	
	public File[] getFile() {
		return file;
	}

	public void setFile(File[] file) {
		this.file = file;
	}


	public String getBusinessType() {
		return businessType;
	}

	public void setBusinessType(String businessType) {
		this.businessType = businessType;
	}

	public String getByWhom() {
		return byWhom;
	}

	public void setByWhom(String byWhom) {
		this.byWhom = byWhom;
	}

	public List<MerchantDetails> getAaData() {
		return aaData;
	}

	public void setAaData(List<MerchantDetails> aaData) {
		this.aaData = aaData;
	}

	public String getStatusComment() {
		return statusComment;
	}


	public void setStatusComment(String statusComment) {
		this.statusComment = statusComment;
	}


	public String getMerchantStatus() {
		return merchantStatus;
	}


	public void setMerchantStatus(String merchantStatus) {
		this.merchantStatus = merchantStatus;
	}


	public String getPayId() {
		return payId;
	}

	public void setPayId(String payId) {
		this.payId = payId;
	}

	public MerchantProcessingApplication getMpaDataByPayId() {
		return mpaDataByPayId;
	}

	public void setMpaDataByPayId(MerchantProcessingApplication mpaDataByPayId) {
		this.mpaDataByPayId = mpaDataByPayId;
	}

	public String getResponseStatus() {
		return responseStatus;
	}

	public void setResponseStatus(String responseStatus) {
		this.responseStatus = responseStatus;
	}

	public String getResponse() {
		return response;
	}

	public String getResponseMsg() {
		return responseMsg;
	}

	public void setResponse(String response) {
		this.response = response;
	}

	public void setResponseMsg(String responseMsg) {
		this.responseMsg = responseMsg;
	}

	public String getBeneName() {
		return beneName;
	}

	public void setBeneName(String beneName) {
		this.beneName = beneName;
	}

	public String getBeneAccountNumber() {
		return beneAccountNumber;
	}

	public String getBeneIfsc() {
		return beneIfsc;
	}

	public void setBeneAccountNumber(String beneAccountNumber) {
		this.beneAccountNumber = beneAccountNumber;
	}

	public void setBeneIfsc(String beneIfsc) {
		this.beneIfsc = beneIfsc;
	}

	public String getPhoneNo() {
		return phoneNo;
	}

	public void setPhoneNo(String phoneNo) {
		this.phoneNo = phoneNo;
	}

}
