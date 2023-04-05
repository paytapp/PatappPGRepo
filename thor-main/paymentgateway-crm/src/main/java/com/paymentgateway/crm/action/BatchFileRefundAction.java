package com.paymentgateway.crm.action;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.struts2.ServletActionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.user.Merchants;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.CrmFieldConstants;
import com.paymentgateway.commons.util.CrmFieldType;
import com.paymentgateway.commons.util.CrmValidator;
import com.paymentgateway.crm.actionBeans.BatchResponseObject;
import com.paymentgateway.crm.actionBeans.CommanCsvReader;
import com.paymentgateway.crm.actionBeans.RefundProcessor;

public class BatchFileRefundAction extends AbstractSecureAction {
	/**
	 * 
	 */
	@Autowired
	private CrmValidator validator;
	
	
	private static final long serialVersionUID = 3340018960360893260L;
	private static Logger logger = LoggerFactory.getLogger(BatchFileRefundAction.class.getName());
	private String fileName;
	private String response;
	private List<Merchants> merchantList = new LinkedList<Merchants>();
	private Map<String, String> currencyMap = new HashMap<String, String>();

	public String readDataFromCsv() {
		BatchResponseObject batchResponseObject = new BatchResponseObject();
		CommanCsvReader commanCsvReader = new CommanCsvReader();
		String processorResponse;
		try {
			// batchFile read line by line
			batchResponseObject = commanCsvReader.createRefundList(fileName);
			if (batchResponseObject.getBatchTransactionList().isEmpty()) {
				addActionMessage(ErrorType.INVALID_FIELD.getResponseMessage());
			} else {
				User sessionUser = (User) sessionMap.get(Constants.USER.getValue());
				RefundProcessor refundProcessor = new RefundProcessor();
				HttpServletRequest request = ServletActionContext.getRequest();
				// RefundProcessor is used to processes, all refund process
				processorResponse = refundProcessor.processAll(batchResponseObject.getBatchTransactionList(),
						sessionUser, request.getRemoteAddr());
				response = new StringBuilder().append(batchResponseObject.getResponseMessage()).append("\tand\n")
						.append(processorResponse).toString();
				addActionMessage(response);
				if (StringUtils.isEmpty(response)) {
					setResponse((CrmFieldConstants.PROCESS_INITIATED_SUCCESSFULLY.getValue()));
					addActionMessage(response);
				}
			}
		} catch (SystemException systemException) {
			logger.error("Error while processing batch refund: " , systemException);
			setResponse(systemException.getMessage());
			addActionMessage(systemException.getMessage());
		} catch (Exception exception) {
			logger.error("Error while processing batch refund: " , exception);
			setResponse(ErrorType.REFUND_NOT_SUCCESSFULL.getResponseMessage());
			addActionMessage("Error while processing batch refund:" + exception);
		}
		return INPUT;
	}
	public void validate(){

	if ((validator.validateBlankField(getFileName()))) {
		addFieldError(CrmFieldType.FILE_NAME.getName(), validator.getResonseObject().getResponseMessage());
	} else if (!(validator.validateField(CrmFieldType.FILE_NAME, getFileName()))) {
		addFieldError(CrmFieldType.FILE_NAME.getName(), validator.getResonseObject().getResponseMessage());
	}
	if ((validator.validateBlankField(getResponse()))) {
		addFieldError(CrmFieldType.RESPONSE.getName(), validator.getResonseObject().getResponseMessage());
	} else if (!(validator.validateField(CrmFieldType.RESPONSE, getResponse()))) {
		addFieldError(CrmFieldType.RESPONSE.getName(), validator.getResonseObject().getResponseMessage());
	}
	}
	
	public String getFileName() {
		return fileName;
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

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getResponse() {
		return response;
	}

	public void setResponse(String response) {
		this.response = response;
	}

}