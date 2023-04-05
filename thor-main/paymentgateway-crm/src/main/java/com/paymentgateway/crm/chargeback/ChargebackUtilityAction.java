package com.paymentgateway.crm.chargeback;

import java.io.File;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
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
import com.paymentgateway.crm.action.AbstractSecureAction;

public class ChargebackUtilityAction extends AbstractSecureAction{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 286091272777259861L;
	private static Logger logger = LoggerFactory.getLogger(ChargebackUtilityAction.class.getName());
	
	@Autowired
	private ChargebackUtilityService chargebackUtilityService;
	
	@Autowired
	private PropertiesManager propertiesManager;
	
	private String response;
	private String responseMsg;
	private String dateFrom;
	private String dateTo;
	private String fileName;
	private InputStream fileInputStream;
	private String utilityType;
	private File csvFile;
	private int length;
	private int start;
	private BigInteger recordsTotal;
	public BigInteger recordsFiltered;
	private User sessionUser = new User();
	private Map<String, Integer> dataValidationMap = new HashMap<String, Integer>();
	private List<RefundUtil> aaData = new ArrayList<RefundUtil>();
	private List<RefundUtil> closerData = new ArrayList<RefundUtil>();
	private List<RefundUtil> creationData = new ArrayList<RefundUtil>();
	
	
	
	public String execute() {
		logger.info("inside execute() ");
		
		if(StringUtils.isNotBlank(utilityType)) {
			if(utilityType.equalsIgnoreCase("chargebackRefund")) {
				chargebackrefund();
			} else if(utilityType.equalsIgnoreCase("chargebackCreation")) {
				ChargebackCreation();
			} else if(utilityType.equalsIgnoreCase("chargebackClosure")) {
				chargebackClosure();
			}
		} else {
			setResponse("error");
			setResponseMsg("Value of utilityType variable is Empty !");
			logger.info("Value of utilityType variable is Empty !");
		}
		
		return SUCCESS;
	}
	
	// chargeback refund utility start
	public String chargebackrefund() {
		logger.info("inside chargebackrefund() ");
		try {
			sessionUser = (User) sessionMap.get(Constants.USER.getValue());

			if (sessionUser.getUserType().equals(UserType.ADMIN)
					|| sessionUser.getUserType().equals(UserType.SUBADMIN)) {
				
				Map<String, List<String[]>> fileValidDataList = validateFileDataForRefund(csvFile);
				
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
								chargebackUtilityService.generateRefundTransaction(fileValidDataList, dataValidationMap.get("totalCount"), dataValidationMap.get("totalInvalid"), sessionUser);
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
	
	private Map<String, List<String[]>> validateFileDataForRefund(File csvFile) {
		logger.info("inside validateFileDataForRefund() ");
		List<String[]> validDataArrayList = new ArrayList<String[]>();
		List<String[]> invalidDataArrayList = new ArrayList<String[]>();
		Map<String, List<String[]>> dataReturnMap = new HashMap<String, List<String[]>>();
		try {
			int totalCount = 0;
			int totalValid = 0;
			int totalInvalid = 0;

			List<String> fileData = chargebackUtilityService.filterFileData(csvFile);
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
					if (!chargebackUtilityService.validatePayId(dataArray[0])) {
						totalInvalid++;
						invalidDataArrayList.add(dataArray);
						continue;
					}
					if (chargebackUtilityService.validateAmount(dataArray[2])) {
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
					
					BigDecimal chargeBackAmount = chargebackUtilityService.validateOrderId(dataArray[0], dataArray[1]);
					if (chargeBackAmount == null || chargeBackAmount.compareTo(fileAmount) !=  0) {
						totalInvalid++;
						invalidDataArrayList.add(dataArray);
						continue;
					} else {
						totalValid++;
						validDataArrayList.add(dataArray);
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

	public String fetchChargebackRefundUtilReport() {
		logger.info("inside fetchChargebackRefundUtilReport() ");
		sessionUser = (User) sessionMap.get(Constants.USER.getValue());
		try {
			if (sessionUser.getUserType().equals(UserType.ADMIN)
					|| sessionUser.getUserType().equals(UserType.SUBADMIN)) {
				setDateFrom(DateCreater.toDateTimeformatCreater(dateFrom));
				setDateTo(DateCreater.formDateTimeformatCreater(dateTo));
				int totalCount = chargebackUtilityService.chargebackRefundUtilReportcount(dateFrom, dateTo, sessionUser);
				BigInteger bigInt = BigInteger.valueOf(totalCount);
				setRecordsTotal(bigInt);
				if (getLength() == -1) {
					setLength(getRecordsTotal().intValue());
				}
				setAaData(chargebackUtilityService.fetchChargebackRefundUtilReport(dateFrom, dateTo, sessionUser, start, length));
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
				fileInputStream = chargebackUtilityService.fetchRefundUtilReportFile(fileName, sessionUser);
			}
		} catch (Exception ex) {
			logger.info("Exception : ", ex);
		}
		return SUCCESS;
	}
	// chargeback refund utility ends
	
	
	// chargeback Creation utility start
	public String ChargebackCreation() {
		logger.info("inside ChargebackCreation() ");
		try {
			sessionUser = (User) sessionMap.get(Constants.USER.getValue());

			if (sessionUser.getUserType().equals(UserType.ADMIN)
					|| sessionUser.getUserType().equals(UserType.SUBADMIN)) {
				
				Map<String, Object> fileDataFilterMap = validateFileDataForChargebackCreation(csvFile, sessionUser);
				
				if(fileDataFilterMap.isEmpty()) {
					setResponse("error");
					setResponseMsg("Something went wrong or File is Empty !");
					return SUCCESS;
				} else {
					chargebackUtilityService.createDataReportForChargebackCreation(fileDataFilterMap, dataValidationMap, sessionUser);
					setResponse("success");
					setResponseMsg("Your file is in process, see in report after some time");
				}
			}
		} catch (Exception e) {
			logger.error("exception ", e);
			setResponse("error");
			setResponseMsg("Something went wrong !");
		}
		return SUCCESS;
	}
	
	private Map<String, Object> validateFileDataForChargebackCreation(File file, User sessionUser){
		logger.info("inside validateFileDataForChargebackCreation() ");
		List<String[]> invalidDataArrayList = new ArrayList<String[]>();
		Set<String> chargebackOrderIdSet = new HashSet<>();
		List<Document> orderIdDocumentList = new ArrayList<Document>();
		Map<String, Object> dataReturnMap = new HashMap<>();
		try {
			int totalCount = 0;
			int totalValid = 0;
			int totalInvalid = 0;

			List<String> fileData = chargebackUtilityService.filterFileData(file);
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
					
//					if(payIdSet.contains(dataArray[0]) || chargebackUtilityService.validatePayId(dataArray[0])) {
//						payIdSet.add(dataArray[0]);
//					} else {
//						totalInvalid++;
//						invalidDataArrayList.add(dataArray);
//						continue;
//					}
					if (chargebackOrderIdSet.contains(dataArray[1]) || chargebackUtilityService.isExistingChargeback(dataArray[0], dataArray[1])) {
						totalInvalid++;
						invalidDataArrayList.add(dataArray);
						continue;
					}
					
					if (chargebackUtilityService.validateAmount(dataArray[2])) {
						amount = dataArray[2];
						if (!amount.contains(".")) {
							amount = Amount.toDecimal(amount + "00", "356");
						}
					} else {
						totalInvalid++;
						invalidDataArrayList.add(dataArray);
						continue;
					}
					
					Document saleDocument = chargebackUtilityService.validateChargebackAmount(dataArray[0], dataArray[1], amount);
					if (saleDocument == null) {
						totalInvalid++;
						invalidDataArrayList.add(dataArray);
						continue;
					} else {
						totalValid++;
						chargebackOrderIdSet.add(dataArray[1]);
						saleDocument.put("chargebackAmount", amount);
						orderIdDocumentList.add(saleDocument);
					}

				} catch (Exception ex) {
					logger.error("Some Issues in dataArray with line no : " + totalCount, ex);
					totalInvalid++;
					invalidDataArrayList.add(dataArray);
					continue;
				}
			}
			
			if (!invalidDataArrayList.isEmpty())
				dataReturnMap.put("inValidData", invalidDataArrayList);
			if (!orderIdDocumentList.isEmpty())
				dataReturnMap.put("documentList", orderIdDocumentList);

			dataValidationMap.put("totalCount", totalCount);
			dataValidationMap.put("totalValid", totalValid);
			dataValidationMap.put("totalInvalid", totalInvalid);
			logger.info("Data Map has created ! ");
		} catch (Exception ex) {
			logger.error("Exception Caught while reading data from file", ex);
		}

		return dataReturnMap;
	}
	
	public String fetchChargebackCreationReport() {
		logger.info("inside fetchChargebackCreationReport() ");
		sessionUser = (User) sessionMap.get(Constants.USER.getValue());
		try {
			if (sessionUser.getUserType().equals(UserType.ADMIN)
					|| sessionUser.getUserType().equals(UserType.SUBADMIN)) {
				setDateFrom(DateCreater.toDateTimeformatCreater(dateFrom));
				setDateTo(DateCreater.formDateTimeformatCreater(dateTo));
				int totalCount = chargebackUtilityService.chargebackCreationReportcount(dateFrom, dateTo, sessionUser);
				BigInteger bigInt = BigInteger.valueOf(totalCount);
				setRecordsTotal(bigInt);
				if (getLength() == -1) {
					setLength(getRecordsTotal().intValue());
				}
				setCreationData(chargebackUtilityService.fetchchargebackCreationReport(dateFrom, dateTo, sessionUser, start, length));
				recordsFiltered = recordsTotal;
			}
		} catch (Exception ex) {
			logger.info("Exception : ", ex);
		}
		return SUCCESS;
	}
	
	public String downloadChargebackCreationReport() {
		logger.info("inside downloadRefundUtilReport() ");
		sessionUser = (User) sessionMap.get(Constants.USER.getValue());
		try {
			if (sessionUser.getUserType().equals(UserType.ADMIN)
					|| sessionUser.getUserType().equals(UserType.SUBADMIN)) {
				fileInputStream = chargebackUtilityService.fetchChargebackCreationReportFile(fileName, sessionUser);
			}
		} catch (Exception ex) {
			logger.info("Exception : ", ex);
		}
		return SUCCESS;
	}
	
	// chargeback Creation utility ends
	
	
	// chargeback Closer utility start
	public String chargebackClosure() {
		logger.info("inside chargebackClosure() ");
		try {
			sessionUser = (User) sessionMap.get(Constants.USER.getValue());

			if (sessionUser.getUserType().equals(UserType.ADMIN)
					|| sessionUser.getUserType().equals(UserType.SUBADMIN)) {
				
				List<String[]> fileDataList = validateFileDataForClosure(csvFile, sessionUser);
				
				if(fileDataList.isEmpty()) {
					setResponse("error");
					setResponseMsg("Something went wrong or File is Empty !");
					return SUCCESS;
				} else {
					chargebackUtilityService.createDataReportForClosure(fileDataList, dataValidationMap, sessionUser);
					setResponse("success");
					setResponseMsg("Your file is in process, see in report after some time");
				}
			}
		} catch (Exception e) {
			logger.error("exception ", e);
			setResponse("error");
			setResponseMsg("Something went wrong !");
		}
		return SUCCESS;
	}
	public List<String[]> validateFileDataForClosure(File file, User sessionUser) {
		logger.info("inside validateFileDataForClosure() ");
		List<String[]> orderIdList = new ArrayList<String[]>();
		Set<String> validOrderIdSet = new HashSet<String>();
		Set<String> invalidOrderIdSet = new HashSet<String>();
		try {
			int totalCount = 0;
			int totalValid = 0;
			int totalInvalid = 0;
			int totalException = 0;
			int totalClosed = 0;

			List<String> fileData = chargebackUtilityService.filterFileData(file);
			for (int i = 1; i < fileData.size(); i++) {
				String[] array = new String[3];
				totalCount++;
				String dataArray[] = fileData.get(i).split(",");
				try {

					if (StringUtils.isBlank(dataArray[0]) || StringUtils.isBlank(dataArray[1])) {
						totalInvalid++;
						array[0] = dataArray[0];
						array[1] = dataArray[1];
						array[2] = "Invalid";
						orderIdList.add(array);
						continue;
					}
					
					if(validOrderIdSet.contains(dataArray[1])) {
						totalInvalid++;
						array[0] = dataArray[0];
						array[1] = dataArray[1];
						array[2] = "Invalid";
						orderIdList.add(array);
						continue;
					}
					if(invalidOrderIdSet.contains(dataArray[1])) {
						totalInvalid++;
						array[0] = dataArray[0];
						array[1] = dataArray[1];
						array[2] = "Invalid";
						orderIdList.add(array);
						continue;
					}
					
					Boolean isOrderIdValid = chargebackUtilityService.validateOrderIdForClosure(dataArray[0], dataArray[1], sessionUser);
					if(isOrderIdValid == null) {
						totalValid++;
						totalException++;
						array[0] = dataArray[0];
						array[1] = dataArray[1];
						array[2] = "Exception";
						orderIdList.add(array);
						invalidOrderIdSet.add(dataArray[1]);
						continue;
					} else if (!isOrderIdValid) {
						totalInvalid++;
						array[0] = dataArray[0];
						array[1] = dataArray[1];
						array[2] = "Invalid";
						orderIdList.add(array);
						invalidOrderIdSet.add(dataArray[1]);
						continue;
					} else {
						totalValid++;
						totalClosed++;
						array[0] = dataArray[0];
						array[1] = dataArray[1];
						array[2] = "Closed";
						orderIdList.add(array);
						validOrderIdSet.add(dataArray[1]);
					}

				} catch (Exception ex) {
					logger.error("Some Issues in dataArray with line no : " + totalCount, ex);
					totalInvalid++;
					array[0] = dataArray[0];
					array[1] = dataArray[1];
					array[2] = "Invalid";
					orderIdList.add(array);
					continue;
				}
			}
			dataValidationMap.put("totalCount", totalCount);
			dataValidationMap.put("totalValid", totalValid);
			dataValidationMap.put("totalInvalid", totalInvalid);
			dataValidationMap.put("totalException", totalException);
			dataValidationMap.put("totalClosed", totalClosed);
			logger.info("Data Map has created ! ");
		} catch (Exception ex) {
			logger.error("Exception Caught while reading data from file", ex);
		}

		return orderIdList;
	}

	public String fetchChargebackClosureReport() {
		logger.info("inside fetchChargebackClosureReport() ");
		sessionUser = (User) sessionMap.get(Constants.USER.getValue());
		try {
			if (sessionUser.getUserType().equals(UserType.ADMIN)
					|| sessionUser.getUserType().equals(UserType.SUBADMIN)) {
				setDateFrom(DateCreater.toDateTimeformatCreater(dateFrom));
				setDateTo(DateCreater.formDateTimeformatCreater(dateTo));
				int totalCount = chargebackUtilityService.chargebackClosureReportcount(dateFrom, dateTo, sessionUser);
				BigInteger bigInt = BigInteger.valueOf(totalCount);
				setRecordsTotal(bigInt);
				if (getLength() == -1) {
					setLength(getRecordsTotal().intValue());
				}
				setCloserData(chargebackUtilityService.fetchchargebackClosureReport(dateFrom, dateTo, sessionUser, start, length));
				recordsFiltered = recordsTotal;
			}
		} catch (Exception ex) {
			logger.info("Exception : ", ex);
		}
		return SUCCESS;
	}
	
	public String downloadChargebackClosureReport() {
		logger.info("inside downloadChargebackClosureReport() ");
		sessionUser = (User) sessionMap.get(Constants.USER.getValue());
		try {
			if (sessionUser.getUserType().equals(UserType.ADMIN)
					|| sessionUser.getUserType().equals(UserType.SUBADMIN)) {
				fileInputStream = chargebackUtilityService.fetchChargebackClosureReportFile(fileName, sessionUser);
			}
		} catch (Exception ex) {
			logger.info("Exception : ", ex);
		}
		return SUCCESS;
	}
	// chargeback Closer utility ends
	
	
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
	
	public List<RefundUtil> getCloserData() {
		return closerData;
	}

	public void setCloserData(List<RefundUtil> closerData) {
		this.closerData = closerData;
	}

	public String getUtilityType() {
		return utilityType;
	}

	public void setUtilityType(String utilityType) {
		this.utilityType = utilityType;
	}

	public List<RefundUtil> getCreationData() {
		return creationData;
	}

	public void setCreationData(List<RefundUtil> creationData) {
		this.creationData = creationData;
	}

}
