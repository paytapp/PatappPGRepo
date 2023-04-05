package com.paymentgateway.crm.action;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.paymentgateway.commons.dao.BinRangeDao;
import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.user.Merchants;
import com.paymentgateway.commons.util.BinRange;
import com.paymentgateway.commons.util.CrmFieldConstants;
import com.paymentgateway.commons.util.CrmFieldType;
import com.paymentgateway.commons.util.CrmValidator;
import com.paymentgateway.crm.actionBeans.BatchResponseObject;
import com.paymentgateway.crm.actionBeans.CommanCsvReader;

public class BinRangeManeger extends AbstractSecureAction {

	@Autowired
	private BinRangeDao binRangeDao;

	@Autowired
	private CrmValidator validator;

	@Autowired
	private CommanCsvReader commanCsvReader;

	private static final long serialVersionUID = -4486785339968262228L;
	private static Logger logger = LoggerFactory.getLogger(BinRangeManeger.class.getName());
	private String fileName;
	private String csvFileName;
	private String response;
	private List<Merchants> merchantList = new LinkedList<Merchants>();
	private Map<String, String> currencyMap = new HashMap<String, String>();

	public String execute() {
		BatchResponseObject batchResponseObject = new BatchResponseObject();
		try {
			// batchFile read line by line
			batchResponseObject = commanCsvReader.csvReaderForBinRange(fileName);
			if (batchResponseObject.getBinRangeResponseList().isEmpty()) {
				addActionMessage(ErrorType.INVALID_FIELD.getResponseMessage());
			} else {
				List<BinRange> binListObj = batchResponseObject.getBinRangeResponseList();
				response = binRangeDao.insertAll(binListObj);
			}
			if (!(StringUtils.isBlank(response))) {
				setResponse((CrmFieldConstants.PROCESS_INITIATED_SUCCESSFULLY.getValue()));
				addActionMessage(response);
			}

		} catch (Exception exception) {
			logger.error("Error while processing binRange: " , exception);
			addActionMessage("Error while processing Binranges:" + exception);
		}
		return INPUT;
	}

	public String emiBinUpload() {
		BatchResponseObject batchResponseObject = new BatchResponseObject();
		try {
			// batchFile read line by line
			batchResponseObject = commanCsvReader.csvReaderForBinRange(fileName);
			if (batchResponseObject.getBinRangeResponseList().isEmpty()) {
				addActionMessage(ErrorType.INVALID_FIELD.getResponseMessage());
			} else {
				List<BinRange> binListObj = batchResponseObject.getBinRangeResponseList();
				response = binRangeDao.emiInsertAll(binListObj);
			}
			if (!(StringUtils.isBlank(response))) {
				setResponse((CrmFieldConstants.PROCESS_INITIATED_SUCCESSFULLY.getValue()));
				addActionMessage(response);
			}

		} catch (Exception exception) {
			logger.error("Error while processing binRange: " , exception);
			addActionMessage("Error while processing Binranges: " + exception);
		}
		return INPUT;
	}

	public void validate() {
		
		if (!(validator.validateBlankField(getCsvFileName()))) {
			
			String fileNameArray [] = getCsvFileName().split(("\\."));
			
			if (fileNameArray.length > 1){
				if (!fileNameArray[1].trim().equalsIgnoreCase("csv")) {
					addFieldError(CrmFieldType.FILE_NAME.getName(), validator.getResonseObject().getResponseMessage());
					setResponse("Invalid File Format Uploaded !!");
				}
			}
		}
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getResponse() {
		return response;
	}

	public void setResponse(String response) {
		this.response = response;
	}

	public List<Merchants> getMerchantList() {
		return merchantList;
	}

	public void setMerchantList(List<Merchants> merchantList) {
		this.merchantList = merchantList;
	}

	public Map<String, String> getCurrencyMap() {
		return currencyMap;
	}

	public void setCurrencyMap(Map<String, String> currencyMap) {
		this.currencyMap = currencyMap;
	}
	
	public String getCsvFileName() {
		return csvFileName;
	}
	public void setCsvFileName(String csvFileName) {
		this.csvFileName = csvFileName;
	}
}
