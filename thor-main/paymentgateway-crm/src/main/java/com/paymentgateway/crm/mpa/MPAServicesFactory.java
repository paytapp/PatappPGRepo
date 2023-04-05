package com.paymentgateway.crm.mpa;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.dao.MPADao;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.user.MerchantProcessingApplication;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.UserStatusType;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.Buffer;

/**
 * @author Amitosh Aanand
 *
 */

@Service
public class MPAServicesFactory {

	@Autowired
	private MPAServiceController serviceController;

	@Autowired
	private MPADao mpaDao;

	@Autowired
	private FileUploader uploader;

	@Autowired
	private MPAResponseCreatorUI responseCreatorUI;

	@Autowired
	private MPAFileEncoder encoder;

	@Autowired
	private UserDao userDao;

	private static Logger logger = LoggerFactory.getLogger(MPAServicesFactory.class.getName());

	private static JSONObject authenticationResponse;

	public JSONObject loggingIn() {
		logger.info("MPA Authentication Login started with Signzy on: " + new Date());
		try {
			authenticationResponse = new JSONObject(serviceController.authenticationLoggingIn());
			logger.info("Data retrived while consuming authenication api with Signzy as : \nid="
					+ authenticationResponse.getString("id") + "\nvalid for (in seconds)="
					+ authenticationResponse.getLong("ttl") + "\ncreated on="
					+ authenticationResponse.getString("created") + "\nuserId="
					+ authenticationResponse.getString("userId") + " \nRaw response from Signzy : "
					+ authenticationResponse);
		} catch (Exception e) {
			logger.error("Exception caught in authentication api with signzy, " , e);
		}
		return authenticationResponse;
	}

	public JSONObject getAuthenticationResponse() {
		if (authenticationResponse == null || authenticationResponse.length() == 0) {
			return loggingIn();
		} else {
			return authenticationResponse;
		}
	}

	public JSONObject eSignLoggingIn() {
		logger.info("MPA Authentication Login started with Signzy on: " + new Date());
		try {
			authenticationResponse = new JSONObject(serviceController.eSignAuthenticationLoggingIn());
			logger.info("Data retrived while consuming authenication api with Signzy as : \nid="
					+ authenticationResponse.getString("id") + "\nvalid for (in seconds)="
					+ authenticationResponse.getLong("ttl") + "\ncreated on="
					+ authenticationResponse.getString("created") + "\nuserId="
					+ authenticationResponse.getString("userId") + " \nRaw response from Signzy : "
					+ authenticationResponse);
		} catch (Exception e) {
			logger.error("Exception caught in authentication api with signzy, " , e);
		}
		return authenticationResponse;
	}

	public JSONObject getESignAuthenticationResponse() {
		if (authenticationResponse == null || authenticationResponse.length() == 0) {
			return eSignLoggingIn();
		} else {
			return authenticationResponse;
		}
	}

	public JSONObject logOut() {
		String response = "";
		logger.info("MPA log out initiated with Signzy on: " + new Date());
		try {
			response = serviceController.authenticationLogOut(authenticationResponse);
			authenticationResponse = null;
		} catch (Exception e) {
			logger.error("Exception caught, " , e);
		}
		return new JSONObject(response);
	}

	public JSONObject eSignLogOut() {
		String response = "";
		logger.info("MPA log out initiated with Signzy on: " + new Date());
		try {
			response = serviceController.eSignAuthenticationLogOut(authenticationResponse);
			authenticationResponse = null;
		} catch (Exception e) {
			logger.error("Exception caught, " , e);
		}
		return new JSONObject(response);
	}

	public Object cinByCompanyName(String companyName, User user, JSONObject authenticationResponse) {
		try {
			logger.info("Searching CIN for " + companyName + " by " + user.getUserType() + " with name "
					+ user.getBusinessName() + " and PayId " + user.getPayId());
			
			if(authenticationResponse==null){
				authenticationResponse=loggingIn();
			}
			JSONObject companyObject = serviceController.rocCompanyObject(authenticationResponse);
			
			JSONObject companyNameSearchResponse = serviceController.rocCompanyNameSearch(companyName, companyObject);
			
			return responseCreatorUI.createCinByCompanyNameResponse(companyNameSearchResponse);
		} catch (Exception e) {
			return responseCreatorUI.errorResponseCreator(Constants.ERROR_TYPE_CIN_BY_COMPANY_NAME);
		}
	}

	public JSONObject mpaIdentitiesFlow(JSONObject authenticationResponse, String serviceType, String emailId,
			String imageUrl) {
		logger.info("MPA Identities Flow API started with Signzy");
		try {
			JSONObject response = serviceController.identitiesFlow(getAuthenticationResponse(), serviceType, emailId,
					imageUrl);
			return response;
		} catch (Exception e) {
			logger.error("Exception caught, " , e);
		}
		return null;
	}

	public Boolean panVerification1(String panNumber, String name, String emailId, String type) {
		JSONObject panResponse;
		JSONObject identitiesResponse;
		String imageUrl = "";
		logger.info("Pan Verification-1 API started with Signzy with Pan Number: " + panNumber + " for: " + name);
		try {
			identitiesResponse = mpaIdentitiesFlow(getAuthenticationResponse(), type, emailId, imageUrl);
			panResponse = serviceController.panVerification1(identitiesResponse, panNumber, name);
			if (!(identitiesResponse == null || panResponse.length() < 0 || panResponse.has("error"))) {
				JSONObject response = panResponse.getJSONObject("response");
				JSONObject result = response.getJSONObject("result");
				return (Boolean) result.get("verified");
			} else {
				return false;
			}
		} catch (Exception e) {
			logger.error("Exception caught while verifying PAN details with Signzy, " , e);
		}
		return null;
	}

	public JSONObject panVerification2(JSONObject authenticationResponse, String emailId, String panNumber,
			String firstName, String middleName, String lastName, String imageUrl) {
		JSONObject panResponse;
		JSONObject identitiesResponse;
		logger.info("Pan Verification-2 API started with Signzy");
		try {
			identitiesResponse = mpaIdentitiesFlow(getAuthenticationResponse(), Constants.INDIVIDUAL_PAN, emailId,
					imageUrl);
			panResponse = new JSONObject(serviceController.panVerification2(identitiesResponse, Constants.IDENTITY,
					Constants.VERIFICATION_2, emailId, imageUrl));
			if (panResponse != null) {
				return panResponse;
			}
		} catch (Exception e) {
			logger.error("Exception caught while verifying PAN details with Signzy, " , e);
		}
		return null;
	}

	public Boolean drivingLicenseVerification(String licenceNumber, String dob, String doi, String emailId,
			String imageUrl) {
		logger.info("Driving License Verification API started with Signzy with Licence Number: " + licenceNumber
				+ " DOB: " + dob + " and DOI:" + doi);
		try {
			JSONObject identitiesResponse = mpaIdentitiesFlow(getAuthenticationResponse(), Constants.DRIVING_LICENSE,
					emailId, imageUrl);
			JSONObject dlResponse = new JSONObject(
					serviceController.drivingLicenseVerification(identitiesResponse, licenceNumber, dob, doi));
			if (!(dlResponse.length() < 0 || dlResponse.has("error"))) {
				JSONObject result = dlResponse.getJSONObject("result");
				return (Boolean) result.get("verified");
			} else {
				return false;
			}
		} catch (Exception e) {
			logger.error("Exception caught while verifying PAN details with Signzy, " , e);
		}
		return null;
	}

	public Map<String, String> simpleSearchByCin(User sessionUser, String payId, String cin, String typeOfEntity,
			String industryCategory) {
		logger.info("ROC Simple Search by Cin API started with Signzy");
		try {
			int cinAttempts = mpaDao.getCinAttemptCount(payId);
			logger.info("Total CIN API attempted: " + cinAttempts + " out of: "
					+ PropertiesManager.propertiesMap.get(Constants.MAX_CIN_ATTEMPTS) + " attempts for PayId: "
					+ payId);
			if (cinAttempts <= Integer.parseInt(PropertiesManager.propertiesMap.get(Constants.MAX_CIN_ATTEMPTS))) {
				try {
					JSONObject organizationObject = createOrganizationObjectForCin(getAuthenticationResponse(), cin);
					JSONObject cinResponse = serviceController.simpleSearchByCin(organizationObject, cin);
					mpaDao.saveCinResponseData(sessionUser, payId, cinResponse, cinAttempts, typeOfEntity,
							industryCategory);
					return responseCreatorUI.createCinResponse(cinResponse);
				} catch (Exception e) {
					return responseCreatorUI.errorResponseCreator(Constants.ERROR_TYPE_CIN);
				}
			} else {
				logger.info("Max CIN attempts exceeded for PayId: " + payId + " max nos of attempts are: "
						+ PropertiesManager.propertiesMap.get(Constants.MAX_CIN_ATTEMPTS));
				return responseCreatorUI.errorResponseCreator(Constants.ERROR_TYPE_CIN_ATTEMPT);
			}
		} catch (Exception e) {
			return responseCreatorUI.errorResponseCreator(Constants.ERROR_TYPE_TRY_AGAIN);
		}
	}

	public Map<String, String> snecs(User sessionUser, String payId, String registrationNumber, String tradingState) {
		logger.info("Shops and Establishment Certificate API started with Signzy for PayId: " + payId
				+ " with Registartion Number: " + registrationNumber + " and trading state: " + tradingState);
		try {
			JSONObject snecsResponse = serviceController.shopsAndEstablishmentCertificate(getAuthenticationResponse(),
					registrationNumber, tradingState);
			mpaDao.saveSNECSResponseData(sessionUser, payId, snecsResponse);
			return responseCreatorUI.createSNECSResponse(snecsResponse, tradingState);
		} catch (Exception e) {
			return responseCreatorUI.errorResponseCreator(Constants.ERROR_TYPE_SNECS);
		}
	}

	private JSONObject createOrganizationObjectForCin(JSONObject authenticationResponseMap, String cin) {
		return serviceController.createOrganizationObjectForCin(authenticationResponseMap, cin);
	}

	public Map<String, String> fetchGstByPan(User user, String payId, String businessPan, String companyEmailId,
			String tradingState) {
		try {
			logger.info("GST based on PAN Number search started with Signzy for PayId: " + payId
					+ ", with business PAN as: " + businessPan + ", State: " + tradingState + ", and company email id: "
					+ companyEmailId);
			Boolean gstVerification = false;
			JSONObject response = serviceController.serachGstByPanNumber(getAuthenticationResponse(), businessPan,
					companyEmailId, tradingState);
			if (response.length() > 0 && response.has("result")) {
				JSONObject result = response.getJSONObject("result");
				JSONArray gstnRecords = result.getJSONArray("gstnRecords");
				JSONObject gstObject = (JSONObject) gstnRecords.get(0);
				String gstValue = gstObject.getString("gstin");

				if (!StringUtils.isEmpty(gstValue))
					gstVerification = true;
			}
			mpaDao.savePanToGstResponseData(user, payId, response, gstVerification);
			return responseCreatorUI.createPanToGstResponse(response);
		} catch (Exception e) {
			return responseCreatorUI.errorResponseCreator(Constants.ERROR_TYPE_PAN_TO_GSTIN);
		}
	}

	public Map<String, String> fetchDetailsByEBill(User sessionUser, String payId, String directorNo,
			String consumerNumber, String electricityProvider) {
		logger.info("Extracting electricity  bill for Director #1 with CA No " + consumerNumber + " by "
				+ sessionUser.getUserType() + "with PayId: " + payId + " and business name: "
				+ sessionUser.getBusinessName());
		try {
			JSONObject response = serviceController.extractElectricityBillByCANo(getAuthenticationResponse(),
					consumerNumber, electricityProvider);
			mpaDao.saveElectricityBillResponseData(sessionUser, directorNo, payId, response);
			return responseCreatorUI.createElectricityBillResponse(directorNo, response);
		} catch (Exception e) {
			return responseCreatorUI.errorResponseCreator(Constants.ERROR_TYPE_ELECTRICTY);
		}
	}

	public String base64Encoder(File filePath, String fileContentType) {
		return encoder.base64Encoder(filePath, fileContentType);
	}

	public Map<String, String> processChequeImage(User user, String payId) {
		logger.info("Processing Cheque Image for PayId: " + payId + " by " + user.getUserType());
		try {
			Object[] verificationData = null;
			String encodedData = mpaDao.fetchImageBase64(payId, Constants.IMAGE_CHEQUE, null);
			Boolean verified = false;
			JSONObject response = serviceController.uploadImageForExtraction(getAuthenticationResponse(), encodedData);
			if (response != null) {
				JSONObject file = response.getJSONObject("file");
				logger.info("Cheque image uploaded for extraction for PayId: " + payId);
				if (file != null) {
					JSONObject identitiesResponse = serviceController.identitiesFlow(getAuthenticationResponse(),
							Constants.CHEQUE, user.getEmailId(), file.getString("directURL"));
					if (identitiesResponse != null) {
						JSONObject chequeExtractionResponse = serviceController
								.cancelledChequeExtraction(identitiesResponse);
						if (chequeExtractionResponse != null) {
							if (chequeExtractionResponse.length() < 0 || chequeExtractionResponse.has("error")) {
								verified = false;
							} else {
								logger.info("Cheque data extracted and sent for bank account verification for PayId: "
										+ payId);
								verificationData = extractAccountDetailsAndVerify(user, payId,
										chequeExtractionResponse);
								verified = (Boolean) verificationData[0];
							}
							if (verified) {
								logger.info("Bank account verified for PayId: " + payId);
								mpaDao.saveChequeExtractionResponseData(user, payId, chequeExtractionResponse);
								return responseCreatorUI
										.createChequeExtractionResponse((JSONObject) verificationData[1]);
							} else {
								return responseCreatorUI
										.errorResponseCreator(Constants.ERROR_TYPE_CHEQUE_EXTRACTION_BANK_VERIFICATION);
							}
						} else {
							return responseCreatorUI.errorResponseCreator(Constants.ERROR_TYPE_TRY_AGAIN);
						}
					} else {
						return responseCreatorUI.errorResponseCreator(Constants.ERROR_TYPE_TRY_AGAIN);
					}
				} else {
					return responseCreatorUI.errorResponseCreator(Constants.ERROR_TYPE_TRY_AGAIN);
				}
			} else {
				return responseCreatorUI.errorResponseCreator(Constants.ERROR_TYPE_TRY_AGAIN);
			}
		} catch (JSONException | SystemException e) {
			logger.error("Exception caught, " , e);
			return responseCreatorUI.errorResponseCreator(Constants.ERROR_TYPE_CHEQUE_EXTRACTION_BANK_VERIFICATION);
		}
	}

	private Object[] extractAccountDetailsAndVerify(User user, String payId, JSONObject chequeExtractionResponse) {
		Object[] obj = new Object[2];
		Boolean verified = false;
		JSONObject bankAccountVerificationResponse;
		try {
			logger.info("Verifying bank account using details obtained from Cheque Image for PayId: " + payId + " by "
					+ user.getUserType());
			bankAccountVerificationResponse = new JSONObject(
					serviceController.verifyBankAccount(getAuthenticationResponse(), chequeExtractionResponse));

			JSONObject result = null;
			JSONObject bankTransfer = null;

			if (bankAccountVerificationResponse.has("result")) {
				result = bankAccountVerificationResponse.getJSONObject("result");
			}
			String active = "";
			if (result.has("active") && (StringUtils.isNotBlank(result.getString("active")))) {
				active = result.getString("active");
			}

			if (result.has("bankTransfer")) {
				bankTransfer = result.getJSONObject("bankTransfer");
			}
			String response = "";
			if (bankTransfer.has("response") && (StringUtils.isNotBlank(bankTransfer.getString("response")))) {
				response = bankTransfer.getString("response");
			}

			if (active.equalsIgnoreCase("yes") && response.equalsIgnoreCase("Transaction Successful")) {
				verified = true;
			}
			mpaDao.saveBankAccountTransferResponseData(user, payId, bankAccountVerificationResponse, verified);
		} catch (Exception e) {
			logger.error("Exception caught while verifying Bank Account, " , e);
			obj[0] = verified;
			obj[1] = null;
			return obj;
		}
		obj[0] = verified;
		if (verified) {
			obj[1] = bankAccountVerificationResponse;
		} else {
			obj[1] = null;
		}
		return obj;
	}

	public Map<String, String> processDrivingLicenseImage(User user, String payId, String directorNumber) {
		logger.info(
				"Extracting informations from Driving License image for PayId: " + payId + " by " + user.getUserType());
		try {
			String encodedData = "";
			encodedData = mpaDao.fetchImageBase64(payId, Constants.IMAGE_DRIVING_LICENSE, directorNumber);
			if (StringUtils.isBlank(encodedData)) {
				return responseCreatorUI.errorResponseCreator(Constants.ERROR_TYPE_UPLOAD);
			}
			JSONObject response = serviceController.uploadImageForExtraction(getAuthenticationResponse(), encodedData);
			if (response.has("file")) {
				JSONObject file = response.getJSONObject("file");
				if (file.has("directURL")) {
					String URL = file.getString("directURL");
					JSONObject identitiesResponse = serviceController.identitiesFlow(getAuthenticationResponse(),
							Constants.DRIVING_LICENSE, user.getEmailId(), URL);
					if (identitiesResponse != null) {
						JSONObject dlExtractionResponse = serviceController
								.drivingLicenseExtraction(identitiesResponse);
						mpaDao.saveDrivingLicenseResponse(user, payId, directorNumber, dlExtractionResponse);
						if (verifyDrivingLicense(user, payId, directorNumber, encodedData, dlExtractionResponse)) {
							return responseCreatorUI.createDLExtractionResponse(dlExtractionResponse, directorNumber);
						} else {
							return responseCreatorUI
									.errorResponseCreator(Constants.ERROR_TYPE_DRIVING_LICENSE_EXTRACTION);
						}
					} else {
						return responseCreatorUI.errorResponseCreator(Constants.ERROR_TYPE_TRY_AGAIN);
					}
				} else {
					return responseCreatorUI.errorResponseCreator(Constants.ERROR_TYPE_TRY_AGAIN);
				}
			} else {
				return responseCreatorUI.errorResponseCreator(Constants.ERROR_TYPE_TRY_AGAIN);
			}
		} catch (Exception e) {
			logger.error("Exception caught, " , e);
			return responseCreatorUI.errorResponseCreator(Constants.ERROR_TYPE_DRIVING_LICENSE_EXTRACTION);
		}
	}

	private Boolean verifyDrivingLicense(User user, String payId, String directorNumber, String encodedData,
			JSONObject dlExtractionResponse) {
		try {
			JSONObject response = serviceController.uploadImageForExtraction(getAuthenticationResponse(), encodedData);
			if (response.has("file")) {
				JSONObject file = response.getJSONObject("file");
				JSONObject identitiesResponse = serviceController.identitiesFlow(getAuthenticationResponse(),
						Constants.DRIVING_LICENSE, user.getEmailId(), file.getString("directURL"));
				Boolean verificationResponse = serviceController.verifyDrivingLicense(identitiesResponse,
						dlExtractionResponse);
				return verificationResponse;
			} else {
				return false;
			}
		} catch (Exception e) {
			logger.info("Exception caught while verifying Driving License data, " , e);
			return false;
		}
	}

	public Map<String, String> getGSTROtp(User user, String payId, String gstnUsername) {
		try {
			logger.info("GSTR OTP Request initialized by " + user.getUserType() + " for PayId: " + payId);
			String gstin = mpaDao.fetchGstinPerPayId(payId);
			if (StringUtils.isNotBlank(gstin)) {
				JSONObject gstrOtpResponse = serviceController.initializeGSTROTPRequest(getAuthenticationResponse(),
						gstin, gstnUsername);
				if (gstrOtpResponse.length() < 0 || gstrOtpResponse.has("error")) {
					return responseCreatorUI.errorResponseCreator(Constants.ERROR_TYPE_GSTR3B);
				} else {
					if (gstrOtpResponse.has("result")) {
						JSONObject result = gstrOtpResponse.getJSONObject("result");
						if (result.has("appKey")) {
							return responseCreatorUI.otpSentResponseCreator(result.getString(Constants.APP_KEY));
						} else {
							return responseCreatorUI.errorResponseCreator(Constants.ERROR_TYPE_GSTR3B);
						}
					} else {
						return responseCreatorUI.errorResponseCreator(Constants.ERROR_TYPE_GSTR3B);
					}
				}
			} else {
				return responseCreatorUI.errorResponseCreator(Constants.ERROR_TYPE_GSTIN_UNAVAILABLE);
			}
		} catch (Exception e) {
			logger.error("Exception caught, " , e);
			return responseCreatorUI.errorResponseCreator(Constants.ERROR_TYPE_TRY_AGAIN);
		}
	}

	public Map<String, String> invokeGstr3b(User user, String payId, String gstinUsername, String gstinOtp,
			String appKey) {
		try {
			logger.info("GSTR AuthToken Request initialized by " + user.getUserType() + " for PayId: " + payId);
			String gstin = mpaDao.fetchGstinPerPayId(payId);
			if (StringUtils.isNotBlank(gstin)) {
				JSONObject gstrAuthTokenResponse = serviceController.initializeGSTRAuthTokenRequest(
						getAuthenticationResponse(), gstin, gstinOtp, gstinUsername, appKey);
				if (gstrAuthTokenResponse != null) {
					JSONObject gstr3bSummaryResponse = serviceController.initializeGSTR3bSummaryRequest(gstin,
							gstinUsername, getAuthenticationResponse(), appKey, gstrAuthTokenResponse, 0);
					if (gstr3bSummaryResponse != null) {
						mpaDao.saveGSTR3bResponseData(user, payId, gstr3bSummaryResponse);
						return responseCreatorUI.createGSTR3bSummaryResponse(gstr3bSummaryResponse);
					} else {
						return responseCreatorUI.errorResponseCreator(Constants.ERROR_TYPE_TRY_AGAIN);
					}
				} else {
					return responseCreatorUI.errorResponseCreator(Constants.ERROR_TYPE_TRY_AGAIN);
				}
			} else {
				return responseCreatorUI.errorResponseCreator(Constants.ERROR_TYPE_GSTIN_UNAVAILABLE);
			}
		} catch (Exception e) {
			logger.error("Exception caught, " , e);
			return responseCreatorUI.errorResponseCreator(Constants.ERROR_TYPE_TRY_AGAIN);
		}
	}

	public Map<String, String> createBusinessPanErrorResponse() {
		return responseCreatorUI.errorResponseCreator(Constants.ERROR_TYPE_BUSINESS_PAN);
	}

	public Map<String, String> individualPanVerification(String panNumber, String directorName, String directorEmail) {
		if (panVerification1(panNumber, directorName, directorEmail, "individualPan")) {
			return responseCreatorUI.createSuccessPanVerificationResponse();
		} else {
			return responseCreatorUI.errorResponseCreator(Constants.ERROR_TYPE_INDIVIDUAL_PAN);
		}
	}

	public Map<String, String> businessPanVerification(String panNumber, String companyName, String companyEmailId) {
		boolean businessPanValidation = panVerification1(panNumber, companyName, companyEmailId, "businessPan");
		if (businessPanValidation) {
			return responseCreatorUI.createSuccessPanVerificationResponse();
		} else {
			return createBusinessPanErrorResponse();
		}
	}

	public void performFinalStage(User user, String payId) {
		List<MerchantProcessingApplication> mpaDataFromDb = mpaDao.fetchMPADataPerPayId(payId);
		if (mpaDataFromDb != null) {
			for (MerchantProcessingApplication mpaData : mpaDataFromDb) {
				if (mpaData.getTypeOfEntity().equalsIgnoreCase("Private Limited")
						|| mpaData.getTypeOfEntity().equalsIgnoreCase("Public Limited")) {
					String cin = mpaData.getCin();
					//entityNegativeList(user, payId, cin);
//					String cinResponse = mpaData.getCinResponse();
					List<String> din = responseCreatorUI.extractDinFromCinResponse(new JSONObject("{}"));
					//tempcode
					din = new ArrayList<>();
					directorNegativeList(user, payId, din, cin);
				} else {
					logger.info(
							"Business is neither Private Limited nor Public Limited, hence we do not have CIN to GET DIN and perform negative check");
				}
			}
		} else {
			logger.error("No data present to perform final stage task for PayId: " + payId);
		}
		eSignLogOut();
		logOut();
	}

	private void directorNegativeList(User user, String payId, List<String> din, String cin) {
		int negativeCount = 0;
		int directorCount = 0;
		JSONObject directorNegativeJson = new JSONObject();
		JSONObject allDirectorsNegativeJson = new JSONObject();
		for (String dinNumber : din) {
			directorCount++;
			String directorNegativeListResponse = serviceController.directorNegativeList(getAuthenticationResponse(),
					dinNumber);
			if (countDirectorNegative(new JSONObject(directorNegativeListResponse))) {
				negativeCount++;
			}
			directorNegativeJson.put(String.valueOf(directorCount), new JSONObject(directorNegativeListResponse));
		}
		allDirectorsNegativeJson.put("negativeListsOfDirectors", directorNegativeJson);
		mpaDao.saveDirectorNegativeListResponse(user, payId, allDirectorsNegativeJson.toString(),
				String.valueOf(negativeCount) + "/" + directorCount);
	}

	@SuppressWarnings("unused")
	public Map<String, String> processESign(User user, String payId, String uidName) {
		logger.info("Inside processESign(), MPAServicesFactory");
		logger.info("Processing Esign pdf for PayId: " + payId + " by " + user.getUserType());
		try {
			MerchantProcessingApplication merchant = mpaDao.fetchMPADataByPayId(payId);

			String encodedData = mpaDao.fetchImageBase64(payId, Constants.IMAGE_ESIGN, null);
			JSONObject response = serviceController.uploadImageForExtraction(getESignAuthenticationResponse(),
					encodedData);
			// JSONObject response = new JSONObject(merchant.getUploadImageForExtraction());
			if (response != null) {
				JSONObject file = response.getJSONObject("file");
				if (file != null) {
					JSONObject eSignUrlResponse = serviceController.eSignUrlGenerationFlow(
							getESignAuthenticationResponse(), uidName, file.getString("directURL"));
					// JSONObject eSignUrlResponse = new
					// JSONObject(merchant.geteSignUrlGenerationFlow());
					if (eSignUrlResponse != null) {
						JSONObject result = eSignUrlResponse.getJSONObject("result");

						merchant.setEsignUrlResponse(result.getString("url"));
						merchant.seteSignDataId(result.getString("token"));
						mpaDao.update(merchant);
						Map<String, String> urlMap = new HashMap<String, String>();
						urlMap.put("url", result.getString("url"));

						return urlMap;
					} else {
						return responseCreatorUI.errorResponseCreator(Constants.ERROR_TYPE_ESIGNID_SAVE);
					}
				} else {
					return responseCreatorUI.errorResponseCreator(Constants.ERROR_TYPE_TRY_AGAIN);
				}
			} else {
				return responseCreatorUI.errorResponseCreator(Constants.ERROR_TYPE_TRY_AGAIN);
			}
		} catch (JSONException | SystemException e) {
			logger.error("Exception caught, " , e);
			return responseCreatorUI.errorResponseCreator(Constants.ERROR_TYPE_ESIGN_VERIFICATION);
		}
	}

	public void loadAgreementFile(User user, String payId) {
		logger.info("Inside loadAgreementFile(), MPAServicesFactory");

		File file = new File(PropertiesManager.propertiesMap.get(Constants.ESIGN_AGREEMENT_FILE_LOCATION),
				Constants.AGREEMENT_FILE_NAME + ".pdf");

		uploader.uploadImageFile(user, payId, file, Constants.IMAGE_ESIGN, "");
	}

	public void saveEsignResponseData(JSONObject eSignUrlResponse) {
		logger.info("Inside saveEsignResponseData(), MPAServicesFactory");
		logger.info("Response Data after URL generation : " + eSignUrlResponse);
		try {
			MerchantProcessingApplication merchant = null;
			if (eSignUrlResponse != null) {

				if (eSignUrlResponse.has("esignParameters")) {
					JSONObject esignParameters = eSignUrlResponse.getJSONObject("esignParameters");
					merchant = mpaDao.fetchMPADataByEsignId(esignParameters.getString("token"));
					mpaDao.saveESignResponse(merchant.getPayId(), eSignUrlResponse);

					logger.info("ESIGN response is saved in db : " + eSignUrlResponse);
				}
				if (eSignUrlResponse.has("result")) {
					JSONObject result = eSignUrlResponse.getJSONObject("result");
					saveEsignedFile(result.getString("esignedFile"), merchant.getPayId());
					logger.info("ESIGN file is saved : ");
				}
			} else {
				logger.info("Esign URl generation response is : " + eSignUrlResponse);
			}
		} catch (Exception e) {
			logger.error("Exception caught, " , e);
		}
	}

	public Map<String, String> getEsignResponse(String payId) {
		logger.info("inside : getEsignResponse(), getting Esign Response data ");
		try {
			MerchantProcessingApplication mpaData = mpaDao.fetchMPADataByPayId(payId);

//			if(mpaData.geteSignResponseData() != null) {
//				JSONObject eSignResopnseData = new JSONObject(mpaData.geteSignResponseData());
//				return responseCreatorUI.createESignDataResponse(eSignResopnseData);
//			}

			if (mpaData.geteSignResponseData() != null) {
				JSONObject eSignResopnseData = new JSONObject(mpaData.geteSignResponseData());
				return responseCreatorUI.createESignDataResponse(eSignResopnseData);

			} else if (mpaData.getEsignUrlResponse() != null) {
				String eSignUrlResponse = mpaData.getEsignUrlResponse();
				Map<String, String> urlMap = new HashMap<String, String>();
				urlMap.put("url", eSignUrlResponse);
				urlMap.put("aadhaarType", "");
				return urlMap;
			}

		} catch (Exception e) {
			logger.error("Exception caught while performing Entity Negative List with Signzy, " , e);
		}
		return null;
	}

	private boolean countDirectorNegative(JSONObject response) {
		if (!(response.length() < 0 || response.has("error"))) {
			try {
				if (response.has("result")) {
					JSONObject result = response.getJSONObject("result");
					if (result.has("defaulterList")) {
						JSONObject defaulterList = result.getJSONObject("defaulterList");
						if (defaulterList.has("found")) {
							return (Boolean) defaulterList.get("found");
						} else {
							return false;
						}
					} else {
						return false;
					}
				} else {
					return false;
				}
			} catch (Exception e) {
				logger.error("Exception caught while saving extracting data from entity negative list response, " , e);
				return false;
			}
		} else {
			return false;
		}
	}

	public Boolean entityNegativeList(User user, String payId, String cin) {
		logger.info("Performing Entity Negative List for CIN: " + cin);
		try {
			JSONObject entityNegativeListResponse = new JSONObject(
					serviceController.entityNegativeList(getAuthenticationResponse(), cin));
			return mpaDao.saveEntityNegativeListResponse(user, payId, entityNegativeListResponse);
		} catch (Exception e) {
			logger.error("Exception caught while performing Entity Negative List with Signzy, " , e);
		}
		return null;
	}

	public List<MerchantProcessingApplication> fetchAllPendingMPA() {
		logger.info("fetching all under review MPA cases");
		List<MerchantProcessingApplication> pendingMPA = new ArrayList<MerchantProcessingApplication>();
		pendingMPA = mpaDao.fetchAllPendingMPA();
		if (pendingMPA.isEmpty()) {
			return null;
		} else {
			return pendingMPA;
		}

	}

	public Map<String, String> approveMPAByPayId(String payId, User user) {
		try {
			mpaDao.approveMPAbyPayId(payId, user);
			return responseCreatorUI.mpaApproved();
		} catch (Exception e) {
			logger.error("Exception caught while approving MPA for merchant with PayId: " + payId);
			return responseCreatorUI.errorResponseCreator(Constants.ERROR_TYPE_TRY_AGAIN);
		}
	}

	public Map<String, String> rejectMPAbyPayId(String payId, User sessionUser) {
		try {
			mpaDao.rejectMPAbyPayId(payId, sessionUser);
			return responseCreatorUI.mpaRejected();
		} catch (Exception e) {
			logger.error("Exception caught while approving MPA for merchant with PayId: " + payId);
			return responseCreatorUI.errorResponseCreator(Constants.ERROR_TYPE_TRY_AGAIN);
		}
	}

	public MerchantProcessingApplication getMPADataByPayId(String payId) {
		logger.info("Getting  MPA Data for payId : " + payId);

		MerchantProcessingApplication MPAData = new MerchantProcessingApplication();
		MPAData = mpaDao.fetchMPADataByPayId(payId);
		if (MPAData.geteSignResponseData() != null) {
			JSONObject esignResponseData = new JSONObject(MPAData.geteSignResponseData());
			JSONObject dscData = esignResponseData.getJSONObject("dscData");

			MPAData.setEsignAadhaarType(dscData.getString("aadhaarType"));
			MPAData.setEsignCountry(dscData.getString("country"));
			MPAData.setEsignGender(dscData.getString("gender"));
			MPAData.setEsignName(dscData.getString("name"));
			MPAData.setEsignPincode(dscData.getString("pincode"));
			MPAData.setEsignState(dscData.getString("state"));
			MPAData.setEsignUidLastFourDigits(dscData.getString("uidLastFourDigits"));
			MPAData.setEsignYOB(dscData.getString("yob"));
		}
		return MPAData;
	}

	public void updateMPADataByPayId(MerchantProcessingApplication MPAData) {
		logger.info("Updating  MPA Data by payId ");
		try {
			mpaDao.update(MPAData);
		} catch (Exception ex) {
			logger.error("Exception caught while Updating MPA for merchant : ", ex);
		}
	}

	public void saveEsignedFile(String sourceFileUrl, String payId) {
		logger.info("Saving Esigned file for payId " + payId);
		try {
			File destFile = new File(PropertiesManager.propertiesMap.get(Constants.MPA_FILE_UPLOAD_LOCATION) + "//"
					+ payId + "//" + Constants.ESIGN_FILE_NAME, Constants.ESIGN_FILE_NAME + "_" + payId + ".pdf");

			FileUtils.copyURLToFile(new URL(sourceFileUrl), destFile);

		} catch (Exception e) {
			logger.error("Exception caught while saving Esigned file, " , e);
		}
	}

	public Map<String, String> processESignTest(User user, String payId, String uidName) {
		logger.info("Processing Esign pdf for PayId: " + payId + " by " + user.getUserType());
		try {
			MerchantProcessingApplication merchant = mpaDao.fetchMPADataByPayId(payId);

			logger.info("ESign image uploaded for extraction for PayId: " + payId);

			String eSignUrlResponse = merchant.getEsignUrlResponse();
			if (eSignUrlResponse != null) {
				Map<String, String> urlMap = new HashMap<String, String>();
				urlMap.put("url", eSignUrlResponse);
				return urlMap;
			} else {
				return responseCreatorUI.errorResponseCreator(Constants.ERROR_TYPE_ESIGNID_SAVE);
			}
		} catch (JSONException e) {
			logger.error("Exception caught, " , e);
			return responseCreatorUI.errorResponseCreator(Constants.ERROR_TYPE_ESIGN_VERIFICATION);
		}
	}

	public void invokeVendorEmpanelmentApi(User user, String payId, String status) {
		try {
			MerchantProcessingApplication mpaData = mpaDao.fetchMPADataByPayId(payId);
			User vendor = userDao.findPayId(payId);
			String salt = (new PropertiesManager()).getSalt(payId);

			if (StringUtils.isBlank(status)) {
				status = UserStatusType.PENDING.name();
			}

			String registrationNumber = "";
			String registeredAddress = "";
			String tradingAddress = "";
			String city = "";
			String companyEmailId = "";
			String businessEmailForCommunication = vendor.getEmailId();
			String contactMobile = vendor.getMobile();
			String companyName = "";
			String typeOfEntity = "";
			String tradingState = "";
			String tradingPin = "";
			String contactName = "";
			String companyWebsite = "";
			String businessPan = "";
			String gstin = "";
			String accountHolderName = "";
			String accountNumber = "";
			String accountIfsc = "";
			String mpaStatus = "";

			if (mpaData != null) {

				mpaStatus = mpaData.getStatus();

				if (StringUtils.isNotBlank(mpaData.getRegistrationNumber())) {
					registrationNumber = mpaData.getRegistrationNumber();
				}

				if (StringUtils.isNotBlank(mpaData.getCompanyRegisteredAddress())) {
					registeredAddress = mpaData.getCompanyRegisteredAddress();
				}

				if (StringUtils.isNotBlank(mpaData.getTradingAddress1())) {
					tradingAddress = mpaData.getTradingAddress1();
				}

				companyEmailId = mpaData.getCompanyEmailId();
				if (StringUtils.isBlank(companyEmailId)) {
					companyEmailId = vendor.getEmailId();
				}

				if (StringUtils.isBlank(mpaData.getBusinessEmailForCommunication())) {
					businessEmailForCommunication = mpaData.getContactEmail();
				}

				if (StringUtils.isNotBlank(mpaData.getContactMobile())) {
					contactMobile = mpaData.getContactMobile();
				}

				if (StringUtils.isNotBlank(mpaData.getCompanyName())) {
					companyName = mpaData.getCompanyName();
				}

				if (StringUtils.isNotBlank(mpaData.getTypeOfEntity())) {
					typeOfEntity = mpaData.getTypeOfEntity();
				}

				if (StringUtils.isNotBlank(mpaData.getTradingState())) {
					tradingState = mpaData.getTradingState();
				}

				if (StringUtils.isNotBlank(mpaData.getTradingPin())) {
					tradingPin = mpaData.getTradingPin();
				}

				if (StringUtils.isNotBlank(mpaData.getContactName())) {
					contactName = mpaData.getContactName();
				}

				if (StringUtils.isNotBlank(mpaData.getCompanyWebsite())) {
					companyWebsite = mpaData.getCompanyWebsite();
				}

				if (StringUtils.isNotBlank(mpaData.getBusinessPan())) {
					businessPan = mpaData.getBusinessPan();
				}

				if (StringUtils.isNotBlank(mpaData.getGstin())) {
					gstin = mpaData.getGstin();
				}

				if (StringUtils.isNotBlank(mpaData.getAccountHolderName())) {
					accountHolderName = mpaData.getAccountHolderName();
				}

				if (StringUtils.isNotBlank(mpaData.getAccountIfsc())) {
					accountIfsc = mpaData.getAccountIfsc();
				}

				if (StringUtils.isNotBlank(mpaData.getAccountNumber())) {
					accountNumber = mpaData.getAccountNumber();
				}
			}

			OkHttpClient client = new OkHttpClient().newBuilder().build();
			MediaType mediaType = MediaType.parse("text/plain");
			RequestBody body = new MultipartBody.Builder().setType(MultipartBody.FORM)
					.build();

			Request request = new Request.Builder()
					.build();

			Buffer buffer = new Buffer();
			request.body().writeTo(buffer);
			String log = buffer.readUtf8();

			Response response = client.newCall(request).execute();
			logger.info("Response received from Khadi emplantment API, " + response.body().string());
		} catch (IOException e) {
			logger.error("Exception in Khadi emplantment API, " , e);
		}
	}

}
