package com.paymentgateway.crm.action;

import java.io.File;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.paymentgateway.commons.user.RefundUtil;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserType;
import com.paymentgateway.commons.util.Amount;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.DateCreater;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.crm.actionBeans.RefundUtilityService;

public class RefundUtilityAction extends AbstractSecureAction {
	
	/**
	 * @Mahbob Alam
	 */
	private static final long serialVersionUID = 7297372844919626272L;
	
	private static Logger logger = LoggerFactory.getLogger(RefundUtilityAction.class.getName());
	
	@Autowired
	private RefundUtilityService refundUtilityService;
	
	@Autowired
	private PropertiesManager propertiesManager;
	
	private String response;
	private String responseMsg;
	private String dateFrom;
	private String dateTo;
	private String fileName;
	private InputStream fileInputStream;
	private File csvFile;
	private int length;
	private int start;
	private BigInteger recordsTotal;
	public BigInteger recordsFiltered;
	private User sessionUser = new User();
	private Map<String, Integer> dataValidationMap = new HashMap<String, Integer>();
	private List<RefundUtil> aaData = new ArrayList<RefundUtil>();
	
	public String execute() {
		logger.info("inside execute() ");
		try {
			sessionUser = (User) sessionMap.get(Constants.USER.getValue());

			if (sessionUser.getUserType().equals(UserType.ADMIN)
					|| sessionUser.getUserType().equals(UserType.SUBADMIN)) {
				
				Map<String, List<String[]>> fileValidDataList = validateFileDeta(csvFile);
				
				if(fileValidDataList.isEmpty()) {
					setResponse("error");
					setResponseMsg("Something went wrong or File is Empty !");
					return SUCCESS;
				} else {
					setResponse("success");
					setResponseMsg("Your file is in process, see in report after some time");
				}
				if(!fileValidDataList.isEmpty()) {
					Runnable runnable = new Runnable() {
						@Override
						public void run() {
							try {
								refundUtilityService.generateRefundTransaction(fileValidDataList, dataValidationMap.get("totalCount"), dataValidationMap.get("totalInvalid"), sessionUser);
							} catch (Exception e) {
								logger.error("Exception while generating Transaction Report ", e);
							}
						}
					};

					propertiesManager.executorImpl(runnable);
				}
			}
		} catch (Exception e) {
			logger.error("exception ", e);
			setResponse("error");
			setResponseMsg("Something went wrong !");
		}
		return SUCCESS;
	}
	
	private Map<String, List<String[]>> validateFileDeta(File csvFile) {
		logger.info("inside validateFileDeta() ");
		List<String[]> validDataArrayList = new ArrayList<String[]>();
		List<String[]> invalidDataArrayList = new ArrayList<String[]>();
		Map<String, BigDecimal> orderIdAmountMap = new HashMap<String, BigDecimal>();
		Map<String, List<String[]>> dataReturnMap = new HashMap<String, List<String[]>>();
		try {
			int totalCount = 0;
			int totalValid = 0;
			int totalInvalid = 0;

			List<String> fileData = refundUtilityService.filterFileData(csvFile);
			for (int i = 1; i < fileData.size(); i++) {
				totalCount++;
				String dataArray[] = fileData.get(i).split(",");
				String amount = null;
				try {

					if (StringUtils.isBlank(dataArray[0]) || StringUtils.isBlank(dataArray[1])
							|| StringUtils.isBlank(dataArray[2])) {
						totalInvalid++;
						invalidDataArrayList.add(dataArray);
						continue;
					}
					if (!refundUtilityService.validatePayId(dataArray[0])) {
						totalInvalid++;
						invalidDataArrayList.add(dataArray);
						continue;
					}
					if (refundUtilityService.validateAmount(dataArray[2])) {
						amount = dataArray[2];
						if (!amount.contains(".")) {
							amount = Amount.toDecimal(amount + "00", "356");
						}
					} else {
						totalInvalid++;
						invalidDataArrayList.add(dataArray);
						continue;
					}

					BigDecimal fileAmount = new BigDecimal(amount);

					if (orderIdAmountMap.containsKey(dataArray[0] + "-" + dataArray[1])) {
						BigDecimal AvailableAmount = orderIdAmountMap.get(dataArray[0] + "-" + dataArray[1]);
						if (AvailableAmount.compareTo(fileAmount) == 0 || AvailableAmount.compareTo(fileAmount) == 1) {
							totalValid++;
							validDataArrayList.add(dataArray);
							orderIdAmountMap.put(dataArray[0] + "-" + dataArray[1],
									AvailableAmount.subtract(fileAmount));
						} else {
							totalInvalid++;
							invalidDataArrayList.add(dataArray);
							continue;
						}
					} else {
						BigDecimal AvailableAmount = refundUtilityService.validateOrderId(dataArray[0], dataArray[1]);
						if (AvailableAmount == null || AvailableAmount.compareTo(fileAmount) == -1) {
							totalInvalid++;
							invalidDataArrayList.add(dataArray);
							continue;
						} else {
							totalValid++;
							validDataArrayList.add(dataArray);
							orderIdAmountMap.put(dataArray[0] + "-" + dataArray[1],
									AvailableAmount.subtract(fileAmount));
						}

					}

				} catch (Exception ex) {
					logger.error("Some Issues in dataArray with line no : " + totalCount, ex);
					totalInvalid++;
					invalidDataArrayList.add(dataArray);
					continue;
				}
			}
			if (!validDataArrayList.isEmpty())
				dataReturnMap.put("validData", validDataArrayList);
			if (!invalidDataArrayList.isEmpty())
				dataReturnMap.put("inValidData", invalidDataArrayList);

			dataValidationMap.put("totalCount", totalCount);
			dataValidationMap.put("totalValid", totalValid);
			dataValidationMap.put("totalInvalid", totalInvalid);
			logger.info("Data Map has created ! ");
		} catch (Exception ex) {
			logger.error("Exception Caught while reading data from file", ex);
		}

		return dataReturnMap;
	}

	public String fetchRefundUtilReport() {
		logger.info("inside fetchRefundUtilReport() ");
		sessionUser = (User) sessionMap.get(Constants.USER.getValue());
		try {
			if (sessionUser.getUserType().equals(UserType.ADMIN)
					|| sessionUser.getUserType().equals(UserType.SUBADMIN)) {
				setDateFrom(DateCreater.toDateTimeformatCreater(dateFrom));
				setDateTo(DateCreater.formDateTimeformatCreater(dateTo));
				int totalCount = refundUtilityService.refundUtilReportcount(dateFrom, dateTo, sessionUser);
				BigInteger bigInt = BigInteger.valueOf(totalCount);
				setRecordsTotal(bigInt);
				if (getLength() == -1) {
					setLength(getRecordsTotal().intValue());
				}
				setAaData(refundUtilityService.fetchRefundUtilReport(dateFrom, dateTo, sessionUser, start, length));
				recordsFiltered = recordsTotal;
			}
		} catch (Exception ex) {
			logger.info("Exception : ", ex);
		}
		return SUCCESS;
	}
	
	public String downloadRefundUtilReport() {
		logger.info("inside downloadRefundUtilReport() ");
		sessionUser = (User) sessionMap.get(Constants.USER.getValue());
		try {
			if (sessionUser.getUserType().equals(UserType.ADMIN)
					|| sessionUser.getUserType().equals(UserType.SUBADMIN)) {
				fileInputStream = refundUtilityService.fetchRefundUtilReportFile(fileName, sessionUser);
			}
		} catch (Exception ex) {
			logger.info("Exception : ", ex);
		}
		return SUCCESS;
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

	public File getCsvFile() {
		return csvFile;
	}

	public void setCsvFile(File csvFile) {
		this.csvFile = csvFile;
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

	public List<RefundUtil> getAaData() {
		return aaData;
	}

	public void setAaData(List<RefundUtil> aaData) {
		this.aaData = aaData;
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

	public BigInteger getRecordsTotal() {
		return recordsTotal;
	}

	public void setRecordsTotal(BigInteger recordsTotal) {
		this.recordsTotal = recordsTotal;
	}

	public BigInteger getRecordsFiltered() {
		return recordsFiltered;
	}

	public void setRecordsFiltered(BigInteger recordsFiltered) {
		this.recordsFiltered = recordsFiltered;
	}

	public Map<String, Integer> getDataValidationMap() {
		return dataValidationMap;
	}

	public void setDataValidationMap(Map<String, Integer> dataValidationMap) {
		this.dataValidationMap = dataValidationMap;
	}
	
}
