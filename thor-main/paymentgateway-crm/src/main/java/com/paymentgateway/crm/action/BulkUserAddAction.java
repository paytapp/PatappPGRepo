package com.paymentgateway.crm.action;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.paymentgateway.commons.dao.PendingBulkUserDao;
import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.user.PendingBulkUserRequest;
import com.paymentgateway.commons.user.PermissionType;
import com.paymentgateway.commons.user.ResponseObject;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.user.UserType;
import com.paymentgateway.commons.util.BusinessType;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.CrmFieldConstants;
import com.paymentgateway.commons.util.CrmFieldType;
import com.paymentgateway.commons.util.CrmValidator;
import com.paymentgateway.commons.util.DateCreater;
import com.paymentgateway.commons.util.PendingRequestEmailProcessor;
import com.paymentgateway.commons.util.TDRStatus;
import com.paymentgateway.commons.util.UserStatusType;
import com.paymentgateway.crm.actionBeans.CreateNewUser;

/**
 * @author Shiva
 *
 */
public class BulkUserAddAction extends AbstractSecureAction {

	private static final long serialVersionUID = 8602408053134420381L;
	private static Logger logger = LoggerFactory.getLogger(BulkUserAddAction.class.getName());

	@Autowired
	private CreateNewUser createUser;

	@Autowired
	private CrmValidator validator;

	@Autowired
	private UserDao userDao;
	
	@Autowired
	private PendingBulkUserDao pendingBulkUserDao;
	
	@Autowired
	private PendingRequestEmailProcessor pendingRequestEmailProcessor;

	private File csvfile;
	int totalCol;
	private Map<Integer, String> duplicate, skipedRow;
	private String emailId;
	private String businessName;
	private String mobile;
	private String industryCategory;
	private String industrySubCategory;
	private String password;
	private int wrongCsv = 0;
	private ResponseObject responseObject = new ResponseObject();
	private UserStatusType userStatusType;
	private UserType userRoleType;
	private Map<String, String> industryCategoryList = new TreeMap<String, String>();
	private List<String> industrySubCategoryList = new ArrayList<>();
	private String response;
	private String pin;
	private String userType;

	int storedRow = 0;
	int rowCount = 0;
	Boolean loopout = false;

	public String execute() {
		
		User sessionUser = (User) sessionMap.get(Constants.USER.getValue());
		StringBuilder permissions = new StringBuilder();
		permissions.append(sessionMap.get(Constants.USER_PERMISSION.getValue()));
		
		String line = "";
		industryCategoryList = BusinessType.getIndustryCategoryList();

		duplicate = new HashMap<>();
		skipedRow = new HashMap<>();

		try {
			List <String> mobileList = userDao.findAllMobile();
			BufferedReader br = new BufferedReader(new FileReader(csvfile));

			while ((line = br.readLine()) != null) {
				rowCount++;
				String data[] = line.split(",");

				// Title Row Skipping
				if (rowCount == 1) {
					if (data[0].equalsIgnoreCase("EmailId") && data[1].equalsIgnoreCase("BusinessName")
							&& data[2].equalsIgnoreCase("Mobile") && data[3].equalsIgnoreCase("UserType")) {
						setWrongCsv(0);
						continue;
					} else {
						setWrongCsv(1);
						break;
					}
				}

				// Counting total columns in a row
				if (data.length < 4) {
					logger.info("Failed To Store " + data[0]);
					skipedRow.put(rowCount, data[0]);
					continue;
				}

				// Checking Duplicate email
				if (userDao.find(data[0]) != null) {
					logger.info(data[0] + " found in database");
					duplicate.put(rowCount, data[0]);
					continue;
				}
				
				
				// Checking Duplicate mobile
				if (mobileList.contains(data[2])) {
					logger.info(data[2] + " found in database");
					duplicate.put(rowCount, data[2]);
					continue;
				}

				// Checking Blank Fields
				if ((StringUtils.isBlank(data[0]) || StringUtils.isBlank(data[1]) || StringUtils.isBlank(data[2]) || StringUtils.isBlank(data[3]))) {
					logger.info("Failed To Store " + data[0]);
					skipedRow.put(rowCount, data[0]);
					continue;

				} else {
					// Storing New Data

					loopout = false;
					setEmailId(data[0]);
					setBusinessName(data[1]);
					setMobile(data[2]);
					setUserType(data[3]);
					setUserStatusType(userStatusType.PENDING);
					setUserRoleType(userRoleType.MERCHANT);
					setPin(Constants.DEFAULT_PIN.getValue());

					/*
					 * // CHECKING CATAGORY LIST for (Map.Entry<String, String> entry :
					 * industryCategoryList.entrySet()) { if
					 * (data[2].trim().equalsIgnoreCase(entry.getValue())) {
					 * setIndustryCategory(entry.getKey()); // CHECKING SUB CATEGORY
					 * industrySubCategoryList.clear();
					 * industrySubCategoryList.addAll(getIndustrySubcategoryProperty(entry.getKey())
					 * );
					 * 
					 * for (String subCat : industrySubCategoryList) { if
					 * (data[3].trim().equalsIgnoreCase(subCat)) { setIndustrySubCategory(subCat);
					 * break; } else { setIndustrySubCategory(""); } } break; } else {
					 * setIndustryCategory(""); } }
					 * 
					 * if (industryCategory.isEmpty()) { logger.info("Failed To Store " + data[0]);
					 * skipedRow.put(rowCount, data[0]); continue; } else if
					 * (industrySubCategory.isEmpty()) { logger.info("Failed To Store " + data[0]);
					 * skipedRow.put(rowCount, data[0]); continue; }
					 */
				}

				// Validating Fields
				if (!validator.validateField(CrmFieldType.MOBILE, getMobile())) {
					logger.info("Failed To Store " + data[0]);
					skipedRow.put(rowCount, getEmailId());
					continue;
				}
				if (!validator.isValidEmailId(getEmailId())) {
					logger.info("Failed To Store " + data[0]);
					skipedRow.put(rowCount, getEmailId());
					continue;
				}
				if (!validator.validateField(CrmFieldType.BUSINESS_NAME, getBusinessName())) {
					logger.info("Failed To Store " + data[0]);
					skipedRow.put(rowCount, getEmailId());
					continue;
				}
				if (StringUtils.isBlank(getUserType()) && (getUserStatusType().equals(UserType.MERCHANT.name()) 
						|| getUserStatusType().equals(UserType.SUPERMERCHANT.name()))) {
					logger.info("Failed To Store " + data[0]);
					skipedRow.put(rowCount, getEmailId());
					continue;
				}

				try {
					
					if (permissions.toString().contains(PermissionType.CREATE_BULK_USER.getPermission())
							|| sessionUser.getUserType().equals(UserType.ADMIN) || sessionUser.getUserType().equals(UserType.RESELLER)) {
						
						if(getUserType().equals(UserType.MERCHANT.name())){
							responseObject = createUser.createUser(getUserInstance(), UserType.MERCHANT, "", sessionUser,"");
						}else if(getUserType().equals(UserType.SUPERMERCHANT.name())){
							responseObject = createUser.createUser(getUserInstance(), UserType.SUPERMERCHANT, "", sessionUser,"");
						}
						
						pendingRequestEmailProcessor.BulkUserAddEmail("Active", sessionUser.getEmailId(), sessionUser.getUserType().toString(),
								PermissionType.CREATE_BULK_USER.getPermission());
						setResponse(ErrorType.SUCCESSFULLY_SAVED.getResponseMessage());
					
					} else {
						PendingBulkUserRequest pendingRequest = pendingBulkUserDao.find(getMobile());
						
						if (pendingRequest != null) {
							setResponse(CrmFieldConstants.PENDING_REQUEST_EXIST.getValue());
							//return
						}
						
						pendingBulkUserDao.create(getPendingInstance(sessionUser));
						
						pendingRequestEmailProcessor.BulkUserAddEmail("Pending", sessionUser.getEmailId(), sessionUser.getUserType().toString(), 
								PermissionType.CREATE_BULK_USER.getPermission());
						setResponse(ErrorType.BULK_USER_REQUEST_APPROVAL.getResponseMessage());
					}

					if (!ErrorType.SUCCESS.getResponseCode().equals(responseObject.getResponseCode())) {
						addActionMessage(responseObject.getResponseMessage());

					}
					if (ErrorType.SUCCESS.getResponseCode().equals(responseObject.getResponseCode())) {
						responseObject.setResponseMessage("User successfully registered");
						addActionMessage(responseObject.getResponseMessage());
					}

					logger.info("Data Stored in Database with Email " + data[0]);
					setStoredRow(++storedRow);

				} catch (Exception exception) {
					logger.error("Exception", exception);
					return ERROR;
				}
			}
			setRowCount(--rowCount);
			setDuplicate(duplicate);
			setSkipedRow(skipedRow);
			logger.info("Total numbers of data in CSV " + rowCount);
			logger.info("Total Numbers of Successully Stored in database " + storedRow);
			logger.info("Total Numbers of Failed data " + skipedRow.size() + " Toal Numbers of Duplicate Data "
					+ duplicate.size());

			br.close();

		} catch (Exception e) {
			logger.error("Exception : " , e);
			e.printStackTrace();
		}
		csvfile.delete();
		return SUCCESS;
	}

	private User getUserInstance() {
		User user = new User();
		user.setEmailId(getEmailId());
		user.setMobile(getMobile());
		user.setBusinessName(getBusinessName());
		user.setPin(getPin());
		return user;
	}
	
	private PendingBulkUserRequest getPendingInstance(User sessionUser) {
		
		Date date = new Date();
		String currentDate = DateCreater.formatDateForDb(date);
		
		PendingBulkUserRequest pendingRequest = new PendingBulkUserRequest();
		pendingRequest.setEmailId(getEmailId());
		pendingRequest.setBusinessName(getBusinessName());
		/*
		 * pendingRequest.setIndustryCategory(getIndustryCategory());
		 * pendingRequest.setIndustrySubCategory(getIndustrySubCategory());
		 */
		pendingRequest.setMobileNumber(getMobile());
		pendingRequest.setRequestedBy(sessionUser.getEmailId());
		pendingRequest.setCreatedDate(currentDate);
		pendingRequest.setStatus(TDRStatus.PENDING);
		return pendingRequest;
	}

	/*
	 * public static List<String> getIndustrySubcategoryProperty(String
	 * industryCategory) { final String prefix = "INDUSTRY_SUB_CATEGORY_";
	 * List<String> subCategories = new LinkedList<String>(); String
	 * industrySubCategoryString = PropertiesManager.propertiesMap.get(prefix +
	 * industryCategory); String[] industrySubcategoryArray =
	 * industrySubCategoryString.split(","); for (String subCategoey :
	 * industrySubcategoryArray) { subCategories.add(subCategoey); } return
	 * subCategories; }
	 */
	
	private Long getRandomNumber() {

		boolean isDuplicateVirtualAccountNo = false;

		Long randomNum;

		do {
			logger.info("generating random number");
			randomNum = (long) Math.round(Math.round(Math.random() * (99999999 - 10000000) + 10000000));
			isDuplicateVirtualAccountNo = userDao.checkDuplicateVirtualAccountNo("LETZ" + randomNum);
			logger.info(
					"virtual Account is " + "LETZ" + randomNum + " found duplicate? " + isDuplicateVirtualAccountNo);
		} while (isDuplicateVirtualAccountNo);

		return randomNum;
	}
	public int getRowCount() {
		return rowCount;
	}

	public void setRowCount(int rowCount) {
		this.rowCount = rowCount;
	}

	public int getStoredRow() {
		return storedRow;
	}

	public void setStoredRow(int storedRow) {
		this.storedRow = storedRow;
	}

	public File getCsvfile() {
		return csvfile;
	}

	public void setCsvfile(File csvfile) {
		this.csvfile = csvfile;
	}

	public String getEmailId() {
		return emailId;
	}

	public void setEmailId(String emailId) {
		this.emailId = emailId;
	}

	public Map<String, String> getIndustryCategoryList() {
		return industryCategoryList;
	}

	public void setIndustryCategoryList(Map<String, String> industryCategoryList) {
		this.industryCategoryList = industryCategoryList;
	}

	public String getBusinessName() {
		return businessName;
	}

	public void setBusinessName(String businessName) {
		this.businessName = businessName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

	public String getIndustryCategory() {
		return industryCategory;
	}

	public void setIndustryCategory(String industryCategory) {
		this.industryCategory = industryCategory;
	}

	public String getIndustrySubCategory() {
		return industrySubCategory;
	}

	public void setIndustrySubCategory(String industrySubCategory) {
		this.industrySubCategory = industrySubCategory;
	}

	public UserStatusType getUserStatusType() {
		return userStatusType;
	}

	public void setUserStatusType(UserStatusType userStatusType) {
		this.userStatusType = userStatusType;
	}

	public UserType getUserRoleType() {
		return userRoleType;
	}

	public void setUserRoleType(UserType userRoleType) {
		this.userRoleType = userRoleType;
	}

	public ResponseObject getResponseObject() {
		return responseObject;
	}

	public void setResponseObject(ResponseObject responseObject) {
		this.responseObject = responseObject;
	}

	public Map<Integer, String> getDuplicate() {
		return duplicate;
	}

	public void setDuplicate(Map<Integer, String> duplicate) {
		this.duplicate = duplicate;
	}

	public Map<Integer, String> getSkipedRow() {
		return skipedRow;
	}

	public void setSkipedRow(Map<Integer, String> skipedRow) {
		this.skipedRow = skipedRow;
	}

	public int getWrongCsv() {
		return wrongCsv;
	}

	public void setWrongCsv(int wrongCsv) {
		this.wrongCsv = wrongCsv;
	}
	
	public String getResponse() {
		return response;
	}

	public void setResponse(String response) {
		this.response = response;
	}
	public String getPin() {
		return pin;
	}

	public void setPin(String pin) {
		this.pin = pin;
	}

	public String getUserType() {
		return userType;
	}

	public void setUserType(String userType) {
		this.userType = userType;
	}
}
