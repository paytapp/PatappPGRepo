package com.paymentgateway.crm.action;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger; import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.ModelDriven;
import com.paymentgateway.commons.action.AbstractSecureAction;
import com.paymentgateway.commons.user.DynamicPaymentPage;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserType;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.CrmFieldConstants;
import com.paymentgateway.commons.util.CrmFieldType;
import com.paymentgateway.commons.util.CrmValidator;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.pg.core.util.DynamicPageUpdator;
import com.paymentgateway.pg.core.util.FetchDynamicPaymentPage;

/**
 * @author Isha
 */
public class PaymentPageLayoutAction extends AbstractSecureAction implements ModelDriven<DynamicPaymentPage> {

	@Autowired
	DynamicPageUpdator DPUpdator;
	
	private static final long serialVersionUID = -1100818649773578599L;
	private static Logger logger = LoggerFactory.getLogger(PaymentPageLayoutAction.class.getName());

	@Autowired
	DynamicPaymentPage dynamicPaymentPage;

	@Autowired
	FetchDynamicPaymentPage fetchDynamicPage;
	@Autowired
	private CrmValidator validator;

	private String payId;
	private File userLogo;
	private String userLogoFileName;

	public String execute() {

		try {
			User sessionUser = (User) sessionMap.get(Constants.USER.getValue());
			DPUpdator.updateUserDetails(dynamicPaymentPage, sessionUser.getPayId());

			if (userLogo != null && (userLogoFileName.toLowerCase().endsWith(".jpg")
					|| userLogoFileName.toLowerCase().endsWith(".png"))) {
				SaveFile(userLogoFileName, userLogo);
			}
			addActionMessage(CrmFieldConstants.DETAILS_SAVED_SUCCESSFULLY.getValue());
		} catch (Exception exception) {
			logger.error("Exception", exception);
			return ERROR;
		}

		return SUCCESS;

	}

	public String SetDefaultPaymentPage() {

		try {
			User sessionUser = (User) sessionMap.get(Constants.USER.getValue());
			setDynamicPaymentPage(fetchDynamicPage.SetDefault(sessionUser.getPayId()));
			addActionMessage(CrmFieldConstants.PLEASE_SAVE.getValue());
			return SUCCESS;
		} catch (Exception exception) {
			logger.error("Exception", exception);
			return ERROR;
		}

	}

	private String SaveFile(String filename, File controlFile) {
		String saveFilename;

		String destPath = PropertiesManager.propertiesMap.get("PaymentPagePath");
		String logoFormat = PropertiesManager.propertiesMap.get("PaymentPageLogoFormat");
		User sessionUser = (User) sessionMap.get(Constants.USER.getValue());
		if (sessionUser.getUserType().equals(UserType.ADMIN) || sessionUser.getUserType().equals(UserType.SUPERADMIN)) {
		} else {
			payId = sessionUser.getPayId();
		}

		try {

			if (destPath != null && logoFormat != null) {
				saveFilename = payId + logoFormat;
				File destFile = new File(destPath, saveFilename);
				FileUtils.copyFile(controlFile, destFile);
			} else {
				return ERROR;
			}

		} catch (Exception exception) {
			logger.error("Exception", exception);
		}
		return SUCCESS;

	}

	public void validate() {

		if ((validator.validateBlankField(getPayId()))) {
			addFieldError(CrmFieldType.PAY_ID.getName(), validator.getResonseObject().getResponseMessage());
		} else if (!(validator.validateField(CrmFieldType.PAY_ID, getPayId()))) {
			addFieldError(CrmFieldType.PAY_ID.getName(), validator.getResonseObject().getResponseMessage());
		}
		if ((validator.validateBlankField(getUserLogoFileName()))) {
			addFieldError(CrmFieldType.COMPANY_NAME.getName(), validator.getResonseObject().getResponseMessage());
		} else if (!(validator.validateField(CrmFieldType.COMPANY_NAME, getUserLogoFileName()))) {
			addFieldError(CrmFieldType.COMPANY_NAME.getName(), validator.getResonseObject().getResponseMessage());
		}
	}

	@Override
	public DynamicPaymentPage getModel() {

		return dynamicPaymentPage;
	}

	public String getPayId() {
		return payId;
	}

	public void setPayId(String payId) {
		this.payId = payId;
	}

	public DynamicPaymentPage getDynamicPaymentPage() {
		return dynamicPaymentPage;
	}

	public void setDynamicPaymentPage(DynamicPaymentPage dynamicPaymentPage) {
		this.dynamicPaymentPage = dynamicPaymentPage;
	}

	public File getUserLogo() {
		return userLogo;
	}

	public void setUserLogo(File userLogo) {
		this.userLogo = userLogo;
	}

	public String getUserLogoFileName() {
		return userLogoFileName;
	}

	public void setUserLogoFileName(String userLogoFileName) {
		this.userLogoFileName = userLogoFileName;
	}

}
