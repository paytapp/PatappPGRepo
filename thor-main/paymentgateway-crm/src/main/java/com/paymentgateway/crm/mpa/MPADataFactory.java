package com.paymentgateway.crm.mpa;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.dao.CheckerMakerDao;
import com.paymentgateway.commons.dao.MPADao;
import com.paymentgateway.commons.dao.MPAMerchantDao;
import com.paymentgateway.commons.user.CheckerMaker;
import com.paymentgateway.commons.user.MPAMerchant;
import com.paymentgateway.commons.user.MerchantProcessingApplication;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.user.UserType;
import com.paymentgateway.commons.util.MPAStatusType;

/**
 * @author Amitosh Aanand
 *
 */
@Service
public class MPADataFactory {

	@Autowired
	private MPADao mpaDao;

	@Autowired
	private UserDao userDao;

	@Autowired
	private MPAMerchantDao mpaMerchantDao;

	@Autowired
	private CheckerMakerDao checkerMakerDao;

	@Autowired
	private MPAResponseCreatorUI responseCreatorUI;

	@Autowired
	private MPAServicesFactory servicesFactory;

	@Autowired
	private MPAMerchantService mpaMerchantService;

	private static Logger logger = LoggerFactory.getLogger(MPADataFactory.class.getName());

	public Map<String, Object> fetchSavedStageData(User user, String payId, String stage) {
		if (StringUtils.isBlank(stage)) {
			String cinResponse = null;
			try {
				List<MerchantProcessingApplication> mpaList = mpaDao.fetchMPADataPerPayId(payId);
				if (mpaList.size() != 0) {
					for (MerchantProcessingApplication mpaRaw : mpaList) {
						stage = mpaRaw.getMpaSavedStage();
						cinResponse = mpaRaw.getCinResponse();
					}
				}
			} catch (Exception e) {
				stage = "NA";
			}
			if (StringUtils.isBlank(stage) || stage.equalsIgnoreCase("NA")) {
				Map<String, Object> stageData = new HashMap<String, Object>();
				stageData.put("stage", "00");
				if (StringUtils.isNotBlank(cinResponse)) {
					return responseCreatorUI.createStage00Response(new JSONObject(cinResponse));
				}
				while (stageData.values().remove(null))
					;
				return stageData;
			} else {
				int nextStage = Integer.parseInt(stage) + 1;
				stage = "0" + nextStage;
			}
		} else {
			if (Integer.parseInt(stage) > 7) {
				Map<String, Object> stageData = new HashMap<String, Object>();
				stageData.put("ERROR", "Thank you for your time ! \n All your data has been saved successfully...");
				logger.info("Fetching invalid stage #" + stage + " for PayId: " + payId);
				while (stageData.values().remove(null))
					;
				return stageData;
			}
			logger.info("Fetching stage #" + stage + " for PayId: " + payId);
			return responseCreatorUI.fetchStageDataPerPayId(user, payId, stage);
		}
		logger.info("Fetching stage #" + stage + " for PayId: " + payId);
		return responseCreatorUI.fetchStageDataPerPayId(user, payId, stage);
	}

	public Map<String, Object> saveStage00Data(User user, String payId, String companyName, String typeOfEntity,
			String cin, String registrationNumber, String dateOfIncorporation, String businessPan,
			String companyRegisteredAddress, String companyTradingAddress1, String companyTradingAddress2,
			String companyTradingAddressCity, String companyTradingAddressState, String companyTradingAddressPin,
			String gstin, String companyPhone, String companyWebsite, String companyEmailId,
			String businessEmailForCommunication, String industryCategory, String stage) {
		logger.info("Saving stage 00 data in DB for PayId: " + payId);
		mpaDao.saveStage00Data(user, payId, companyName, typeOfEntity, cin, registrationNumber, dateOfIncorporation,
				businessPan, companyRegisteredAddress, companyTradingAddress1, companyTradingAddress2,
				companyTradingAddressCity, companyTradingAddressState, companyTradingAddressPin, gstin, companyPhone,
				companyWebsite, companyEmailId, businessEmailForCommunication, industryCategory, stage);
		mpaDao.savempaStage00InUser(user, payId);
		setCheckerMaker(user, industryCategory, payId);
		int nextStage = Integer.parseInt(mpaDao.fetchSavedStageNumber(payId)) + 1;
		stage = "0" + nextStage;
		return fetchSavedStageData(user, payId, stage);
	}

	public Map<String, Object> saveStage01Data(User user, String payId, String contactName, String contactMobile,
			String contactEmail, String contactLandline, String director1FullName, String director1Pan,
			String director1Email, String director1Mobile, String director1Landline, String director1Address,
			String director1DOB, String director2FullName, String director2Pan, String director2Email,
			String director2Mobile, String director2Landline, String director2Address, String director2DOB,
			String stage, String merchantSupportEmailId, String merchantSupportMobileNumber,
			String merchantSupportLandLine) {
		logger.info("Saving stage 01 data in DB for PayId: " + payId);
		Boolean director1PanVerified;
		Boolean director2PanVerified;
		if (userDao.findPayId(payId).isMpaOnlineFlag()) {
			director1PanVerified = servicesFactory.panVerification1(director1Pan, director1FullName, director1Email,
					Constants.INDIVIDUAL_PAN);

			director2PanVerified = servicesFactory.panVerification1(director2Pan, director2FullName, director2Email,
					Constants.INDIVIDUAL_PAN);
		} else {
			director1PanVerified = true;
			director2PanVerified = true;
		}
		mpaDao.saveStage01Data(user, payId, contactName, contactMobile, contactEmail, contactLandline,
				director1FullName, director1Pan, director1PanVerified, director1Email, director1Mobile,
				director1Landline, director1Address, director1DOB, director2FullName, director2Pan,
				director2PanVerified, director2Email, director2Mobile, director2Landline, director2Address,
				director2DOB, stage, merchantSupportEmailId, merchantSupportMobileNumber, merchantSupportLandLine);
		int nextStage = Integer.parseInt(mpaDao.fetchSavedStageNumber(payId)) + 1;
		stage = "0" + nextStage;
		return fetchSavedStageData(user, payId, stage);
	}

	public Map<String, Object> saveStage02Data(User user, String payId, String accountNumber, String accountIfsc,
			String accountHolderName, String accountMobileNumber, String stage) {
		logger.info("Saving stage 02 data in DB for PayId: " + payId);
		mpaDao.saveStage02Data(user, payId, accountNumber, accountIfsc, accountHolderName, accountMobileNumber, stage);
		int nextStage = Integer.parseInt(mpaDao.fetchSavedStageNumber(payId)) + 1;
		stage = "0" + nextStage;
		return fetchSavedStageData(user, payId, stage);
	}

	public Map<String, Object> saveStage03Data(User user, String payId, String annualTurnover,
			String annualTurnoverOnline, String percentageCC, String percentageDC, String percentageDomestic,
			String percentageInternational, String percentageCD, String percentageNeftOrImpsOrRtgs, String percentageNB,
			String percentageUP, String percentageWL, String percentageEM, String stage) {
		logger.info("Saving stage 03 data in DB for PayId: " + payId);
		mpaDao.saveStage03Data(user, payId, annualTurnover, annualTurnoverOnline, percentageCC, percentageDC,
				percentageDomestic, percentageInternational, percentageCD, percentageNeftOrImpsOrRtgs, percentageNB,
				percentageUP, percentageWL, percentageEM, stage);
		int nextStage = Integer.parseInt(mpaDao.fetchSavedStageNumber(payId)) + 1;
		stage = "0" + nextStage;
		return fetchSavedStageData(user, payId, stage);
	}

	public Map<String, Object> saveStage04Data(User user, String payId, String merchantType, String surcharge,
			String integrationType, String customizedInvoiceDesign, String internationalCards, String expressPay,
			String expressPayParameter, String allowDuplicateSaleOrderId, String allowDuplicateRefundOrderId,
			String allowDuplicateSaleOrderIdInRefund, String allowDuplicateRefundOrderIdSale, String stage) {
		logger.info("Saving stage 04 data in DB for PayId: " + payId);
		mpaDao.saveStage04Data(user, payId, Boolean.valueOf(surcharge), integrationType,
				Boolean.valueOf(customizedInvoiceDesign), Boolean.valueOf(internationalCards),
				Boolean.valueOf(expressPay), expressPayParameter, allowDuplicateSaleOrderId,
				allowDuplicateRefundOrderId, allowDuplicateSaleOrderIdInRefund, allowDuplicateRefundOrderIdSale, stage,
				merchantType);
		int nextStage = Integer.parseInt(mpaDao.fetchSavedStageNumber(payId)) + 1;
		stage = "0" + nextStage;
		return fetchSavedStageData(user, payId, stage);
	}

	public Map<String, Object> saveStage05Data(User user, String payId, String technicalContactName,
			String technicalContactMobile, String technicalContactEmail, String technicalContactLandline,
			String serverDetails, String serverCompanyName, String serverCompanyLandline, String serverCompanyAddress,
			String serverCompanyMobile, String operatingSystem, String backendTechnology,
			String applicationServerTechnology, String productionServerIp, String stage) {
		logger.info("Saving stage 05 data in DB for PayId: " + payId);
		mpaDao.saveStage05Data(user, payId, technicalContactName, technicalContactMobile, technicalContactEmail,
				technicalContactLandline, serverDetails, serverCompanyName, serverCompanyLandline, serverCompanyAddress,
				serverCompanyMobile, operatingSystem, backendTechnology, applicationServerTechnology,
				productionServerIp, stage);
		int nextStage = Integer.parseInt(mpaDao.fetchSavedStageNumber(payId)) + 1;
		stage = "0" + nextStage;
		return fetchSavedStageData(user, payId, stage);
	}

	public Map<String, Object> saveStage06Data(User user, String payId, String thirdPartyForCardData,
			String refundsAllowed, String stage) {
		logger.info("Saving stage 06 data in DB for PayId: " + payId);
		// mpaDao.saveRefundPolicyBase64(user, payId, base64);
		mpaDao.saveStage06Data(user, payId, thirdPartyForCardData, refundsAllowed, stage);
		int nextStage = Integer.parseInt(mpaDao.fetchSavedStageNumber(payId)) + 1;
		stage = "0" + nextStage;
		return fetchSavedStageData(user, payId, stage);
	}

	public Object saveStage07Data(User user, String payId, String stage, boolean isMpaOnlineFlag) {
		logger.info("Saving stage 07 data in DB for PayId: " + payId);
		mpaDao.saveStage07Data(user, payId, stage);
		if (isMpaOnlineFlag == false)
			saveMPAAppliactionData(user, payId);
		int nextStage = Integer.parseInt(mpaDao.fetchSavedStageNumber(payId)) + 1;
		stage = "0" + nextStage;
		return fetchSavedStageData(user, payId, stage);
	}

	public Object saveStage08Data(User user, String payId, String stage) {
		logger.info("Saving stage 08 data in DB for PayId: " + payId);
		mpaDao.saveStage08Data(user, payId, stage);
		saveMPAAppliactionData(user, payId);
		int nextStage = Integer.parseInt(mpaDao.fetchSavedStageNumber(payId)) + 1;
		stage = "0" + nextStage;
		return fetchSavedStageData(user, payId, stage);
	}

	public void saveMPAAppliactionData(User merchant, String payId) {
		MPAMerchant mpaMerchant = new MPAMerchant();
		SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
		try {
			if (merchant.getUserType().equals(UserType.ADMIN) || merchant.getUserType().equals(UserType.SUBADMIN) || merchant.getUserType().equals(UserType.MERCHANT) || merchant.getUserType().equals(UserType.SUBUSER)) {
				merchant = userDao.findPayId(payId);
				merchant.setMpaStage("COMPLETED");
				userDao.update(merchant);
			}
			mpaMerchant.setPayId(payId);
			mpaMerchant.setEmailId(merchant.getEmailId());
			mpaMerchant.setMobile(merchant.getMobile());
			mpaMerchant.setBusinessName(merchant.getBusinessName());
			mpaMerchant.setUserStatus(MPAStatusType.PENDING.getStatusCode());
			mpaMerchant.setUserType(merchant.getUserType());
			mpaMerchant.setRegistrationDate(merchant.getRegistrationDate());
			mpaMerchant.setFormSubmissionDate(formatter.format(new Date()));
			mpaMerchant.setMakerPayId(merchant.getMakerPayId());
			mpaMerchant.setMakerName(merchant.getMakerName());
			mpaMerchant.setMakerStatus(merchant.getMakerStatus());
			mpaMerchant.setMakerStatusUpDate(merchant.getMakerStatusUpDate());
			mpaMerchant.setCheckerPayId(merchant.getCheckerPayId());
			mpaMerchant.setCheckerName(merchant.getCheckerName());
			mpaMerchant.setCheckerStatus(merchant.getCheckerStatus());
			mpaMerchant.setIndustryCategory(merchant.getIndustryCategory());
			mpaMerchant.setCheckerStatusUpDate(merchant.getCheckerStatusUpDate());

			mpaMerchantDao.create(mpaMerchant);
			mpaMerchantService.senEmailToCheckermaker(payId, "", "", "New");

		} catch (Exception ex) {
			logger.error("Exception caught while saving MPAMerchant, " , ex);
		}
	}

	public void setCheckerMaker(User merchant, String industryCategory, String payId) {

		List<CheckerMaker> checkerMakerList = new ArrayList<CheckerMaker>();
		CheckerMaker checkerMaker = new CheckerMaker();

		try {
			checkerMakerList = checkerMakerDao.findAllChekerMaker();

			if (merchant.getUserType().equals(UserType.ADMIN) || merchant.getUserType().equals(UserType.SUBADMIN)) {
				merchant = userDao.findPayId(payId);
			}
			merchant.setIndustryCategory(industryCategory);

			for (CheckerMaker ckMk : checkerMakerList) {
				if (ckMk.getIndustryCategory().equalsIgnoreCase(industryCategory)) {
					checkerMaker = ckMk;
					break;
				}
			}
			if (checkerMaker != null) {
				merchant.setCheckerPayId(checkerMaker.getCheckerPayId());
				merchant.setMakerPayId(checkerMaker.getMakerPayId());
				merchant.setCheckerName(checkerMaker.getCheckerName());
				merchant.setMakerName(checkerMaker.getMakerName());
			}
			userDao.update(merchant);
		} catch (Exception ex) {
			logger.error("Exception caught while creating checkermaker for MPAMerchant, " , ex);
		}
	}
}