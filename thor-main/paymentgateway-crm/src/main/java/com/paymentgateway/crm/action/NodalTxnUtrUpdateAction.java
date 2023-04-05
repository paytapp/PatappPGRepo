package com.paymentgateway.crm.action;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.paymentgateway.commons.dao.NodalUtrUpdateDao;
import com.paymentgateway.commons.dao.UserSettingDao;
import com.paymentgateway.commons.email.EmailServiceProvider;
import com.paymentgateway.commons.user.NodalTransactions;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserSettingData;
import com.paymentgateway.commons.user.UserType;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.CrmFieldType;
import com.paymentgateway.commons.util.CrmValidator;
import com.paymentgateway.commons.util.DateCreater;
import com.paymentgateway.commons.util.ExcelReaderUtils;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.StatusType;

public class NodalTxnUtrUpdateAction extends AbstractSecureAction {

	/**
	 * @Mehboob Alam
	 */
	private static final long serialVersionUID = 3468692722395935105L;

	private static Logger logger = LoggerFactory.getLogger(NodalTxnUtrUpdateAction.class.getName());

	@Autowired
	private CrmValidator validator;

	@Autowired
	private NodalUtrUpdateDao nodalUtrUpdateDao;

	@Autowired
	PropertiesManager propertiesManager;

	@Autowired
	EmailServiceProvider emailServiceProvider;

	@Autowired
	private UserSettingDao userSettingDao;

	@Autowired
	private ExcelReaderUtils excelReaderUtils;

	private String payId;
	private String subMerchantId;
	private String acquirerCode;
	private String utrNo;
	private String dateFrom;
	private String dateTo;
	private String payOutDate;
	private String mopType;
	private String paymentType;
	private String response;
	private String responseMsg;

	private List<NodalTransactions> aaData = new ArrayList<NodalTransactions>();
	private User sessionUser = new User();

	private File csvFile;
	long totalSuccess = 0;
	long totalData = 0;
	boolean wrongFile = false;
	boolean idfcUpiSettlementFlag = false;

	public String execute() {

		sessionUser = (User) sessionMap.get(Constants.USER.getValue());

		setDateFrom(DateCreater.toDateTimeformatCreater(dateFrom));
		setDateTo(DateCreater.formDateTimeformatCreater(dateTo));
		int noOfTxn = 0;
		try {
			String dateForEmail = payOutDate;

			String timeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());
			payOutDate = DateCreater.formatDateTime(payOutDate + " " + timeFormat.split(" ")[1]);

			if (sessionUser.getUserType().equals(UserType.ADMIN)
					|| sessionUser.getUserType().equals(UserType.SUBADMIN)) {
				String[] acquirerCodeArray = new String[0];
				String[] mopeTypeArray = new String[0];
				String[] paymentTypeArray = new String[0];

				if (StringUtils.isNotBlank(acquirerCode) && !acquirerCode.equalsIgnoreCase("ALL")) {
					acquirerCodeArray = acquirerCode.split(",");
				}
				if (StringUtils.isNotBlank(mopType) && !mopType.equalsIgnoreCase("ALL")) {
					mopeTypeArray = mopType.split(",");
				}
				if (StringUtils.isNotBlank(paymentType) && !paymentType.equalsIgnoreCase("ALL")) {
					paymentTypeArray = paymentType.split(",");
				}

				noOfTxn = nodalUtrUpdateDao.updateSettledTxnWithUtr(dateFrom, dateTo, payOutDate, acquirerCodeArray,
						mopeTypeArray, paymentTypeArray, payId, subMerchantId, utrNo);

				if (noOfTxn == 0) {
					setResponse("failed");
					setResponseMsg("Transaction not found");
				} else {
					setResponse("success");
					setResponseMsg("UTR updated Successfully");

					// sending payment Advice email
					UserSettingData userSettingData;

					if (StringUtils.isNotBlank(subMerchantId)) {
						userSettingData = userSettingDao.fetchDataUsingPayId(subMerchantId);
					} else {
						userSettingData = userSettingDao.fetchDataUsingPayId(payId);
					}

					if (userSettingData.isPaymentAdviceFlag())
						emailServiceProvider.sendPaymentAdviseEmail(subMerchantId, payId, sessionUser.getPayId(),
								dateForEmail, "356");
				}

			} else {
				setResponse("failed");
				setResponseMsg("User Not Allowed");
			}

		} catch (Exception ex) {
			logger.error("Exception : ", ex);
			setResponse("failed");
			setResponseMsg("UTR Couldn't be Updated, Something went wrong");
		}

		return SUCCESS;
	}

	public String processBulkUTRUpdate() {

		String headerRow = "S. No,Merchant Name,Sub Merchant Name,Beneficiary Name,BeneficiaryAccountNumber,IFSC Code,"
				+ "Transfer Amount,Settlement Date,Transaction Mode,Remarks,Status,payId,subMerchantId,UTR";

		String fileHeader = null;

		long totalSuccess = 0;

		try {
			sessionUser = (User) sessionMap.get(Constants.USER.getValue());
			if (sessionUser.getUserType().equals(UserType.ADMIN)
					|| sessionUser.getUserType().equals(UserType.SUBADMIN)) {

				if (csvFile != null) {

					List<String> fileData = excelReaderUtils.readXlsxFile(csvFile);

					if (fileData.size() >= 2) {

						fileHeader = fileData.get(0);

						if (!fileHeader.equalsIgnoreCase(headerRow)) {
							logger.info("header mismatch processBulkUTRUpdate()");
							setWrongFile(true);
							setResponse("failed");
							setResponseMsg("Header Mismatch for File");
							return SUCCESS;
						}
						setTotalData(fileData.size()-1);
						
						String header = fileData.get(0);
						for (String row : fileData) {
							String[] col = row.split(",");
							
							if(header.equals(row)){
								continue;
							}
							
							if (col.length != 14) {
								if (col.length == 0) {
									logger.info("empty Record  " + row);
									continue;
								} else {
									logger.info("Invalid Record  " + row);

								}
								continue;
							}

							String srNo = col[0];
							String merchant = col[1];
							String subMerchant = col[2];

							String settlementDate = col[7];

							String status = col[10];
							String payId = col[11];
							String subMerchantId = col[12];

							String utr = col[13];

							if (status.equalsIgnoreCase(StatusType.CAPTURED.getName())) {

								nodalUtrUpdateDao.updateBulkSettledTxnWithUtr(settlementDate, payId, subMerchantId, utr,
										idfcUpiSettlementFlag);
								totalSuccess++;

							} else {
								logger.info("status is not captured for processBulkUTRUpdate()");
							}

						}
					}
					setTotalSuccess(totalSuccess);
					setResponse("success");
					setResponseMsg("File Processed Successfully");
					return SUCCESS;
				}
				setResponse("failed");
				setResponseMsg("File Not Found");
				return SUCCESS;
			}
			setResponse("failed");
			setResponseMsg("Login User not permit for using this action");
			return SUCCESS;

		} catch (Exception e) {
			logger.info("exception in processBulkUTRUpdate() ", e);
			setResponse("failed");
			setResponseMsg("Something Went Wrong");
			return SUCCESS;
		}

	}

	public void validate() {
		if (!(validator.validateBlankField(getUtrNo()))) {
			if (!(validator.validateField(CrmFieldType.UTR_NO, getUtrNo()))) {
				addFieldError(CrmFieldType.UTR_NO.getName(), validator.getResonseObject().getResponseMessage());
			}
		}
	}

	public String getPayId() {
		return payId;
	}

	public void setPayId(String payId) {
		this.payId = payId;
	}

	public String getSubMerchantId() {
		return subMerchantId;
	}

	public void setSubMerchantId(String subMerchantId) {
		this.subMerchantId = subMerchantId;
	}

	public String getAcquirerCode() {
		return acquirerCode;
	}

	public void setAcquirerCode(String acquirerCode) {
		this.acquirerCode = acquirerCode;
	}

	public String getUtrNo() {
		return utrNo;
	}

	public void setUtrNo(String utrNo) {
		this.utrNo = utrNo;
	}

	public User getSessionUser() {
		return sessionUser;
	}

	public void setSessionUser(User sessionUser) {
		this.sessionUser = sessionUser;
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

	public String getPayOutDate() {
		return payOutDate;
	}

	public void setPayOutDate(String payOutDate) {
		this.payOutDate = payOutDate;
	}

	public String getMopType() {
		return mopType;
	}

	public void setMopType(String mopType) {
		this.mopType = mopType;
	}

	public String getPaymentType() {
		return paymentType;
	}

	public void setPaymentType(String paymentType) {
		this.paymentType = paymentType;
	}

	public String getResponse() {
		return response;
	}

	public void setResponse(String response) {
		this.response = response;
	}

	public String getResponseMsg() {
		return responseMsg;
	}

	public void setResponseMsg(String responseMsg) {
		this.responseMsg = responseMsg;
	}

	public List<NodalTransactions> getAaData() {
		return aaData;
	}

	public void setAaData(List<NodalTransactions> aaData) {
		this.aaData = aaData;
	}

	public long getTotalSuccess() {
		return totalSuccess;
	}

	public void setTotalSuccess(long totalSuccess) {
		this.totalSuccess = totalSuccess;
	}

	public boolean isWrongFile() {
		return wrongFile;
	}

	public void setWrongFile(boolean wrongFile) {
		this.wrongFile = wrongFile;
	}

	public boolean isIdfcUpiSettlementFlag() {
		return idfcUpiSettlementFlag;
	}

	public void setIdfcUpiSettlementFlag(boolean idfcUpiSettlementFlag) {
		this.idfcUpiSettlementFlag = idfcUpiSettlementFlag;
	}

	public long getTotalData() {
		return totalData;
	}

	public void setTotalData(long totalData) {
		this.totalData = totalData;
	}

	public File getCsvFile() {
		return csvFile;
	}

	public void setCsvFile(File csvFile) {
		this.csvFile = csvFile;
	}
	

}
