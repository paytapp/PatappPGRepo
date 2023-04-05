package com.paymentgateway.crm.action;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.user.ChargingDetails;
import com.paymentgateway.commons.user.MapList;
import com.paymentgateway.commons.user.Merchants;
import com.paymentgateway.commons.user.PaymentOptions;
import com.paymentgateway.commons.user.PendingBulkCharges;
import com.paymentgateway.commons.user.PendingBulkUserRequest;
import com.paymentgateway.commons.user.PendingMappingRequest;
import com.paymentgateway.commons.user.RouterConfiguration;
import com.paymentgateway.commons.user.SurchargeDetails;
import com.paymentgateway.commons.user.SurchargeMappingPopulator;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.crm.actionBeans.PendingDetailsFactory;

/**
 * @author Rahul, Shaiwal, Shiva
 *
 */
public class PendingDetailsFormAction extends AbstractSecureAction {

	private static final long serialVersionUID = 2034014287760856881L;

	@Autowired
	private UserDao userDao;

	@Autowired
	private PendingDetailsFactory pendingDetailsFactory;

	private static Logger logger = LoggerFactory.getLogger(PendingDetailsFormAction.class.getName());

	public List<Merchants> listMerchant = new ArrayList<Merchants>();

	private List<ChargingDetails> tdrData = new ArrayList<ChargingDetails>();
	private List<PendingBulkCharges> bulkChargesData = new ArrayList<PendingBulkCharges>();
	private List<PendingMappingRequest> merchantMappingData = new ArrayList<PendingMappingRequest>();
	private List<PaymentOptions> paymentOptionData = new ArrayList<PaymentOptions>();
	private List<Object> reportData = new ArrayList<Object>();

	private final static String PAYMENT_OPTION = "paymentOption";
	private final static String MERCHANT_MAPPING = "merchantMapping";
	private final static String BULK_CHARGES = "bulkCharges";
	private final static String CHARGING_DETAILS = "chargingDetails";
	private final static String REPORTING = "reporting";

	private String dataFor;
	private String reportType;
	private String reportStatus;

	@SuppressWarnings("unchecked")
	public String execute() {
		setListMerchant(userDao.getMerchantList());

		try {

			if (StringUtils.isNotBlank(dataFor)) {

				switch (dataFor) {

				case PAYMENT_OPTION:
					setPaymentOptionData(pendingDetailsFactory.getPaymentOptions(sessionMap));
					break;
				case MERCHANT_MAPPING:
					setMerchantMappingData(pendingDetailsFactory.getMerchantMapping(sessionMap));
					break;
				case BULK_CHARGES:
					setBulkChargesData(pendingDetailsFactory.getPendingBulkCharges(sessionMap));
					break;
				case CHARGING_DETAILS:
					setTdrData(pendingDetailsFactory.getChargingDetails(sessionMap));
					break;
				case REPORTING:
					if (reportType.equalsIgnoreCase(PAYMENT_OPTION))
						setReportData(
								pendingDetailsFactory.getPaymentOptionsForPendingReport(sessionMap, reportStatus));
					else if (reportType.equalsIgnoreCase(MERCHANT_MAPPING))
						setReportData(
								pendingDetailsFactory.getMerchantMappingForPendingReport(sessionMap, reportStatus));
					else if (reportType.equalsIgnoreCase(BULK_CHARGES))
						setReportData(pendingDetailsFactory.getBulkChargesForPendingReport(sessionMap, reportStatus));
					else if (reportType.equalsIgnoreCase(CHARGING_DETAILS))
						setReportData(
								pendingDetailsFactory.getChargingDetailsForPendingReport(sessionMap, reportStatus));
					break;
				default:
				}

			}

			// setTdrData(pendingDetailsFactory.getPendingChargingDetails(sessionMap));

			// setSurchargeMerchantData(pendingDetailsFactory.getPendingSurchargeDetails(sessionMap));
			// setSurchargePGData(pendingDetailsFactory.getPendingPGSurchargeDetails(sessionMap));
			//
			// setUserProfileData(pendingDetailsFactory.getPendingUserProfile(sessionMap));
			// setResellerMappingData(pendingDetailsFactory.getPendingResellerMapping(sessionMap));
			// setTestData(pendingDetailsFactory.getTestData(sessionMap));
			// setSmartRouterData(pendingDetailsFactory.getPendingSmartRouterData(sessionMap));
			// setBulkUserData(pendingDetailsFactory.getPendingBulkUser(sessionMap));

			// if(surchargeMerchantData.size()==0 && surchargePGData.size() ==0
			// ){
			// }

		} catch (Exception exception) {
			logger.error("Exception", exception);
			addActionMessage(ErrorType.UNKNOWN.getResponseMessage());
		}
		return SUCCESS;
	}

	// To display page without using token
	@SuppressWarnings("unchecked")
	public String displayList() {
		setListMerchant(userDao.getMerchantList());
		return INPUT;
	}

	public void validate() {
	}

	public List<Merchants> getListMerchant() {
		return listMerchant;
	}

	public void setListMerchant(List<Merchants> listMerchant) {
		this.listMerchant = listMerchant;
	}

	public List<PendingMappingRequest> getMerchantMappingData() {
		return merchantMappingData;
	}

	public void setMerchantMappingData(List<PendingMappingRequest> merchantMappingData) {
		this.merchantMappingData = merchantMappingData;
	}

	public List<PaymentOptions> getPaymentOptionData() {
		return paymentOptionData;
	}

	public void setPaymentOptionData(List<PaymentOptions> paymentOptionData) {
		this.paymentOptionData = paymentOptionData;
	}

	public String getDataFor() {
		return dataFor;
	}

	public void setDataFor(String dataFor) {
		this.dataFor = dataFor;
	}

	public List<ChargingDetails> getTdrData() {
		return tdrData;
	}

	public void setTdrData(List<ChargingDetails> tdrData) {
		this.tdrData = tdrData;
	}

	public List<PendingBulkCharges> getBulkChargesData() {
		return bulkChargesData;
	}

	public void setBulkChargesData(List<PendingBulkCharges> bulkChargesData) {
		this.bulkChargesData = bulkChargesData;
	}

	public String getReportType() {
		return reportType;
	}

	public String getReportStatus() {
		return reportStatus;
	}

	public void setReportType(String reportType) {
		this.reportType = reportType;
	}

	public void setReportStatus(String reportStatus) {
		this.reportStatus = reportStatus;
	}

	public List<Object> getReportData() {
		return reportData;
	}

	public void setReportData(List<Object> reportData) {
		this.reportData = reportData;
	}

}
