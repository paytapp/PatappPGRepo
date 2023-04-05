package com.paymentgateway.crm.action;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserType;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.crm.actionBeans.PgQrManualDataEntryService;

public class PgQrManualDataEntryAction extends AbstractSecureAction {

	/**
	 * @Mahboob Alam
	 */
	private static final long serialVersionUID = 7037823155434126873L;

	private static Logger logger = LoggerFactory.getLogger(PgQrManualDataEntryAction.class.getName());

	@Autowired
	private PgQrManualDataEntryService pgQrManualDataEntryService;
	
	@Autowired
	private PropertiesManager propertiesManager;
	
	private File csvFile;
	private String response;
	private String responseMsg;
	private User sessionUser;

	public String execute() {

		try {
			sessionUser = (User) sessionMap.get(Constants.USER.getValue());

			if (sessionUser.getUserType().equals(UserType.ADMIN)) {
				Runnable runnable = new Runnable() {
					@Override
					public void run() {
						try {
							pgQrManualDataEntryService.generatePgQrDataInDb(csvFile, sessionUser);
						} catch (Exception e) {
							logger.error("Exception while generating Transaction Report ", e);
						}
					}
				};

				propertiesManager.executorImpl(runnable);
				setResponse("success");
				setResponseMsg("Your Transactions are in process !");
			}
		} catch (Exception e) {
			logger.error("exception ", e);
			setResponse("error");
			setResponseMsg("Something went wrong !");
		}
		return SUCCESS;
	}

	public File getCsvFile() {
		return csvFile;
	}

	public void setCsvFile(File csvFile) {
		this.csvFile = csvFile;
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

	public User getSessionUser() {
		return sessionUser;
	}

	public void setSessionUser(User sessionUser) {
		this.sessionUser = sessionUser;
	}

}
