package com.paymentgateway.crm.mpa;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.dao.MPADao;
import com.paymentgateway.commons.user.MerchantProcessingApplication;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.util.PropertiesManager;

/**
 * @author Amitosh Aanand
 *
 */
@Service
public class MPAResponseCreatorUI {

	@Autowired
	MPADao mpaDao;
	
	@Autowired
	UserDao userDao;

	@Autowired
	MPAServicesFactory servicesFactory;

	@Autowired
	MPAFileEncoder encoder;

	private static Logger logger = LoggerFactory.getLogger(MPAResponseCreatorUI.class.getName());

//	public Map<String, Object> fetchStageDataPerPayId(User user, String payId, String stage) {
//		Map<String, Object> stageData = mpaDao.fetchStageDataPerPayId(payId, stage);
//		stageData.put("stage", stage);
//		if (stage.equalsIgnoreCase("01")) {
//			if (!stageData.containsKey("director1FullName")) {
//				return createStage01Response(payId, stage);
//			} else {
//				return stageData;
//			}
//		} else if (stage.equalsIgnoreCase("03")) {
//			if (stageData.containsKey("annualTurnover")) {
//				return stageData;
//			} else {
//				String GSTR3bResponse = mpaDao.fetchGSTR3bResponse(payId);
//				if (StringUtils.isNotBlank(GSTR3bResponse)) {
//					return createStage03Response(stage, GSTR3bResponse);
//				} else {
//					return stageData;
//				}
//			}
//		} else if (stage.equalsIgnoreCase("04")) {
//			if (stageData.containsKey("merchantType")) {
//				if ((boolean) stageData.get("surcharge")) {
//					stageData.put("surcharge", "true");
//				} else {
//					stageData.put("surcharge", "false");
//				}
//				if ((boolean) stageData.get("customizedInvoiceDesign")) {
//					stageData.put("customizedInvoiceDesign", "true");
//				} else {
//					stageData.put("customizedInvoiceDesign", "false");
//				}
//				if ((boolean) stageData.get("internationalCards")) {
//					stageData.put("internationalCards", "true");
//				} else {
//					stageData.put("internationalCards", "false");
//				}
//				if ((boolean) stageData.get("expressPay")) {
//					stageData.put("expressPay", "true");
//				} else {
//					stageData.put("expressPay", "false");
//				}
//			}
//			return stageData;
//		} else if (stage.equalsIgnoreCase("06")) {
//			if (stageData.containsKey("thirdPartyForCardData")) {
//				stageData.put("isThirdPartyStore", "YES");
//			} else {
//				stageData.put("isThirdPartyStore", "NO");
//			}
//			return stageData;
//		} else if (stage.equalsIgnoreCase("08")) {
//			MerchantProcessingApplication mpaData = mpaDao.fetchMPADataByPayId(payId);
//			if(mpaData.geteSignResponseData() ==null) {
//				stageData.put("esignFlag", "NO");
//			}else {
//			JSONObject json = new JSONObject(mpaData.geteSignResponseData());
//			
//			if (!StringUtils.isEmpty(json.toString())) {
//				JSONObject dscData = json.getJSONObject("dscData");
//				
//				stageData.put("esignFlag", "YES");
//				stageData.put("uidLastFourDigits", dscData.getString("uidLastFourDigits"));
//				stageData.put("pincode", dscData.getString("pincode"));
//				stageData.put("country", dscData.getString("country"));
//				stageData.put("gender", dscData.getString("gender"));
//				stageData.put("name", dscData.getString("name"));
//				stageData.put("aadhaarType", dscData.getString("aadhaarType"));
//				stageData.put("state", dscData.getString("state"));
//				stageData.put("yob", dscData.getString("yob"));
//			}
//		}
//			return stageData;
//		}else {
//			if(stage.equalsIgnoreCase("09")) {
//
//				MerchantProcessingApplication mpaData = mpaDao.fetchMPADataByPayId(payId);
//
//				JSONObject json = new JSONObject(mpaData.geteSignResponseData());
//				JSONObject dscData = json.getJSONObject("dscData");
//				
//				stageData.put("Country", dscData.getString("country"));
//				stageData.put("Pincode", dscData.getString("pincode"));
//				stageData.put("Gender", dscData.getString("gender"));
//				stageData.put("EsignName", dscData.getString("name"));
//				stageData.put("AadhaarType", dscData.getString("aadhaarType"));
//				stageData.put("State", dscData.getString("state"));
//				stageData.put("YOB", dscData.getString("yob"));
//				stageData.put("UidLastFourDigits", dscData.getString("uidLastFourDigits"));
//				
//			}
//			return stageData;
//		}
//	}

	public Map<String, Object> fetchStageDataPerPayId(User user, String payId, String stage) {
		boolean isMpaOnlineFlag = true;
		if(stage.equalsIgnoreCase("05") || stage.equalsIgnoreCase("06")) {
			User mpaUser = userDao.findPayId(payId);
			isMpaOnlineFlag = mpaUser.isMpaOnlineFlag();
		}
		Map<String, Object> stageData = mpaDao.fetchStageDataPerPayId(payId, stage, isMpaOnlineFlag);
		stageData.put("stage", stage);
		if (stage.equalsIgnoreCase("01")) {
			if (!stageData.containsKey("director1FullName")) {
				return createStage01Response(payId, stage);
			} else {
				return stageData;
			}
		} else if (stage.equalsIgnoreCase("03")) {
			if (stageData.containsKey("annualTurnover")) {
				return stageData;
			} else {
				String GSTR3bResponse = mpaDao.fetchGSTR3bResponse(payId);
				if (StringUtils.isNotBlank(GSTR3bResponse)) {
					return createStage03Response(stage, GSTR3bResponse);
				} else {
					return stageData;
				}
			}
		} 
//		else if (stage.equalsIgnoreCase("04")) {
//			if (stageData.containsKey("merchantType")) {
//				if ((boolean) stageData.get("surcharge")) {
//					stageData.put("surcharge", "true");
//				} else {
//					stageData.put("surcharge", "false");
//				}
//				if ((boolean) stageData.get("customizedInvoiceDesign")) {
//					stageData.put("customizedInvoiceDesign", "true");
//				} else {
//					stageData.put("customizedInvoiceDesign", "false");
//				}
//				if ((boolean) stageData.get("internationalCards")) {
//					stageData.put("internationalCards", "true");
//				} else {
//					stageData.put("internationalCards", "false");
//				}
//				if ((boolean) stageData.get("expressPay")) {
//					stageData.put("expressPay", "true");
//				} else {
//					stageData.put("expressPay", "false");
//				}
//			}
//			return stageData;
//		} 
//		else if (stage.equalsIgnoreCase("04")) {
//			if (stageData.containsKey("thirdPartyForCardData")) {
//				stageData.put("isThirdPartyStore", "YES");
//			} else {
//				stageData.put("isThirdPartyStore", "NO");
//			}
//			return stageData;
//		} 
		else if (stage.equalsIgnoreCase("05")	&& isMpaOnlineFlag == true) {
			MerchantProcessingApplication mpaData = mpaDao.fetchMPADataByPayId(payId);
			if(mpaData.geteSignResponseData() ==null) {
				stageData.put("esignFlag", "NO");
			}else {
			JSONObject json = new JSONObject(mpaData.geteSignResponseData());
			
			if (!StringUtils.isEmpty(json.toString())) {
				JSONObject dscData = json.getJSONObject("dscData");
				
				stageData.put("esignFlag", "YES");
				stageData.put("uidLastFourDigits", dscData.getString("uidLastFourDigits"));
				stageData.put("pincode", dscData.getString("pincode"));
				stageData.put("country", dscData.getString("country"));
				stageData.put("gender", dscData.getString("gender"));
			  	stageData.put("name", dscData.getString("name"));
				stageData.put("aadhaarType", dscData.getString("aadhaarType"));
				stageData.put("state", dscData.getString("state"));
				stageData.put("yob", dscData.getString("yob"));
			}
		}
			return stageData;
		} else if (stage.equalsIgnoreCase("07") && isMpaOnlineFlag == true) {
			MerchantProcessingApplication mpaData = mpaDao.fetchMPADataByPayId(payId);

			if (mpaData.geteSignResponseData() != null) {
				JSONObject json = new JSONObject(mpaData.geteSignResponseData());
				JSONObject dscData = json.getJSONObject("dscData");

				stageData.put("Country", dscData.getString("country"));
				stageData.put("Pincode", dscData.getString("pincode"));
				stageData.put("Gender", dscData.getString("gender"));
				stageData.put("EsignName", dscData.getString("name"));
				stageData.put("AadhaarType", dscData.getString("aadhaarType"));
				stageData.put("State", dscData.getString("state"));
				stageData.put("YOB", dscData.getString("yob"));
				stageData.put("UidLastFourDigits", dscData.getString("uidLastFourDigits"));
			} else {// using this in case of skipping esign stage
				stageData = mpaDao.fetchStageDataPerPayId(payId, "06", isMpaOnlineFlag);
				stageData.put("stage", "07");
			}
		} else if(stage.equalsIgnoreCase("08") && isMpaOnlineFlag == true) { // using this in case of skipping esign stage
			stageData = mpaDao.fetchStageDataPerPayId(payId, "06", isMpaOnlineFlag);
			stageData.put("stage", "07");
		}
			return stageData;
		}
	

	private Map<String, Object> createStage01Response(String payId, String stage) {
		List<MerchantProcessingApplication> mpaList = mpaDao.fetchMPADataPerPayId(payId);
		String cinResponse = null;
		String typeOfEntity = null;
		for (MerchantProcessingApplication mpa : mpaList) {
			cinResponse = mpa.getCinResponse();
			typeOfEntity = mpa.getTypeOfEntity();
		}
		if (StringUtils.isBlank(cinResponse)) {
			Map<String, Object> mpaMap = new HashMap<String, Object>();
			mpaMap.put("stage", stage);
			mpaMap.put("typeOfEntity", typeOfEntity);
			return mpaMap;
		} else {
			return processCinResponseForStage01(new JSONObject(cinResponse), stage, typeOfEntity);
		}
	}

	private Map<String, Object> processCinResponseForStage01(JSONObject cinResponse, String stage,
			String typeOfEntity) {
		Map<String, Object> mpaMap = new HashMap<String, Object>();
		if (cinResponse.length() < 0 || cinResponse.has("error")) {
			mpaMap.put("ERROR", "Improper CIN Data");
		} else {
			if (cinResponse.has("result")) {
				mpaMap.clear();
				JSONObject result = cinResponse.getJSONObject("result");
				if (result.has("directorDetails")) {
					JSONArray directorDetailsArray = result.getJSONArray("directorDetails");
					for (int i = 1; i <= directorDetailsArray.length(); i++) {
						JSONObject directorDetails = directorDetailsArray.getJSONObject(i - 1);
						if (directorDetails.has("address")
								&& (StringUtils.isNotBlank(directorDetails.getString("address")))) {
							mpaMap.put("director" + i + "Address", directorDetails.getString("address"));
						} else {
							mpaMap.put("director" + i + "Address", "");
						}
						if (directorDetails.has("name")
								&& (StringUtils.isNotBlank(directorDetails.getString("name")))) {
							mpaMap.put("director" + i + "FullName", directorDetails.getString("name"));
						} else {
							mpaMap.put("director" + i + "FullName", "");
						}
						if (directorDetails.has("pan") && (StringUtils.isNotBlank(directorDetails.getString("pan")))) {
							mpaMap.put("director" + i + "Pan", directorDetails.getString("pan"));
						} else {
							mpaMap.put("director" + i + "Pan", "");
						}
					}
				} else {
					mpaMap.put("ERROR", "Oops something went wrong!!! \nPlease try again.");
					return mpaMap;
				}
			} else {
				mpaMap.put("ERROR", "Oops something went wrong!!! \nPlease try again.");
				return mpaMap;
			}
			mpaMap.put("stage", stage);
			mpaMap.put("typeOfEntity", typeOfEntity);
		}
		return mpaMap;
	}

	private Map<String, Object> createStage03Response(String stage, String gSTR3bResponse) {
		return processResponseForStage03(new JSONObject(gSTR3bResponse), stage);
	}

	private Map<String, Object> processResponseForStage03(JSONObject gstr3bSummaryResponse, String stage) {
		Map<String, Object> mpaMap = new HashMap<String, Object>();
		Double annualTurnover = 0.0;
		if (gstr3bSummaryResponse.has("result")) {
			JSONObject result = gstr3bSummaryResponse.getJSONObject("result");
			if (result.has("data")) {
				JSONObject data = result.getJSONObject("data");
				if (data.has("supplyDetails")) {
					JSONObject supplyDetails = data.getJSONObject("supplyDetails");
					if (supplyDetails.has("outwardTaxableSuppliesNonZero")) {
						JSONObject outwardTaxableSuppliesNonZero = supplyDetails
								.getJSONObject("outwardTaxableSuppliesNonZero");
						annualTurnover += Double.valueOf(outwardTaxableSuppliesNonZero.getString("totalTaxableValue"));
					} else {
						mpaMap.put("ERROR", "Oops something went wrong!!! \\nPlease contact Administrator");
						return mpaMap;
					}
					if (supplyDetails.has("outwardTaxableSuppliesZero")) {
						JSONObject outwardTaxableSuppliesZero = supplyDetails
								.getJSONObject("outwardTaxableSuppliesZero");
						annualTurnover += Double.valueOf(outwardTaxableSuppliesZero.getString("totalTaxableValue"));
					} else {
						mpaMap.put("ERROR", "Oops something went wrong!!! \\nPlease contact Administrator");
						return mpaMap;
					}
					if (supplyDetails.has("otherOutwardSupplies")) {
						JSONObject otherOutwardSupplies = supplyDetails.getJSONObject("otherOutwardSupplies");
						annualTurnover += Double.valueOf(otherOutwardSupplies.getString("totalTaxableValue"));
					} else {
						mpaMap.put("ERROR", "Oops something went wrong!!! \\nPlease contact Administrator");
						return mpaMap;
					}
					if (supplyDetails.has("outwardSuppliesNonGst")) {
						JSONObject outwardSuppliesNonGst = supplyDetails.getJSONObject("outwardSuppliesNonGst");
						annualTurnover += Double.valueOf(outwardSuppliesNonGst.getString("totalTaxableValue"));
					} else {
						mpaMap.put("ERROR", "Oops something went wrong!!! \\nPlease contact Administrator");
						return mpaMap;
					}
					// handle interStateSupplies into annual turnover
					// handle interStateSupplies into annual turnover
					// handle interStateSupplies into annual turnover
					// handle interStateSupplies into annual turnover
					// handle interStateSupplies into annual turnover
				}
			} else {
				mpaMap.put("ERROR", "Oops something went wrong!!! \\nPlease contact Administrator");
				return mpaMap;
			}
		} else {
			mpaMap.put("ERROR", "Oops something went wrong!!! \\nPlease contact Administrator");
			return mpaMap;
		}
		mpaMap.put("stage", stage);
		mpaMap.put("annualTurnover", String.valueOf(roundOffToHundred(annualTurnover * 12)));
		return mpaMap;
	}

	public Map<String, String> createCinResponse(JSONObject cinResponse) {
		Map<String, String> mpaMap = new HashMap<String, String>();
		if (cinResponse.length() < 0 || cinResponse.has("error")) {
			return errorResponseCreator(Constants.ERROR_TYPE_CIN);
		} else {
			if (cinResponse.has("result")) {
				JSONObject result = cinResponse.getJSONObject("result");

				if (result.has("companyName") && (StringUtils.isNotBlank(result.getString("companyName")))) {
					mpaMap.put("companyName", result.getString("companyName"));
				} else {
					mpaMap.put("companyName", "");
				}
				if (result.has("cin") && (StringUtils.isNotBlank(result.getString("cin")))) {
					mpaMap.put("cin", result.getString("cin"));
				} else {
					mpaMap.put("cin", "");
				}
				if (result.has("dateOfIncorporation")
						&& (StringUtils.isNotBlank(result.getString("dateOfIncorporation")))) {
					mpaMap.put("dateOfIncorporation", result.getString("dateOfIncorporation"));
				} else {
					mpaMap.put("dateOfIncorporation", "");
				}
				if (result.has("pan") && (StringUtils.isNotBlank(result.getString("pan")))) {
					mpaMap.put("businessPan", result.getString("pan"));
				} else {
					mpaMap.put("businessPan", "");
				}
				if (result.has("registeredAddress")
						&& (StringUtils.isNotBlank(result.getString("registeredAddress")))) {
					mpaMap.put("companyRegisteredAddress", result.getString("registeredAddress"));
				} else {
					mpaMap.put("companyRegisteredAddress", "");
				}
				if (result.has("emailId") && (StringUtils.isNotBlank(result.getString("emailId")))) {
					mpaMap.put("companyEmailId", result.getString("emailId"));
				} else {
					mpaMap.put("companyEmailId", "");
				}
				mpaMap.put("stage", "00");
			} else {
				return errorResponseCreator(Constants.ERROR_TYPE_TRY_AGAIN);
			}
		}
		return mpaMap;
	}

	public Map<String, String> createSNECSResponse(JSONObject snecsResponse, String tradingState) {
		Map<String, String> mpaMap = new HashMap<String, String>();
		if (snecsResponse.length() < 0 || snecsResponse.has("error")) {
			return errorResponseCreator(Constants.ERROR_TYPE_SNECS);
		} else {
			if (snecsResponse.has("result")) {
				JSONObject result = snecsResponse.getJSONObject("result");
				if (result.has("summary")) {
					JSONObject summary = result.getJSONObject("summary");
					if (summary.has("name") && (StringUtils.isNotBlank(summary.getString("name")))) {
						mpaMap.put("companyName", summary.getString("name"));
					} else {
						mpaMap.put("companyName", "");
					}
					if (summary.has("dateOfCommencement")
							&& (StringUtils.isNotBlank(summary.getString("dateOfCommencement")))) {
						mpaMap.put("dateOfIncorporation", summary.getString("dateOfCommencement"));
					} else {
						mpaMap.put("dateOfIncorporation", "");
					}
					if (summary.has("address") && (StringUtils.isNotBlank(summary.getString("address")))) {
						mpaMap.put("companyRegisteredAddress", summary.getString("address"));
					} else {
						mpaMap.put("companyRegisteredAddress", "");
					}
				} else {
					return errorResponseCreator(Constants.ERROR_TYPE_TRY_AGAIN);
				}
			} else {
				return errorResponseCreator(Constants.ERROR_TYPE_TRY_AGAIN);
			}
		}
		return mpaMap;
	}

	public Map<String, String> createPanToGstResponse(JSONObject response) {
		Map<String, String> mpaMap = new HashMap<String, String>();
		if (response.length() < 0 || response.has("error")) {
			return errorResponseCreator(Constants.ERROR_TYPE_PAN_TO_GSTIN);
		} else {
			if (response.has("result")) {
				JSONObject result = response.getJSONObject("result");
				if (result.has("gstnRecords")) {
					JSONArray gstnRecordsArray = result.getJSONArray("gstnRecords");
					JSONObject gstnRecords = gstnRecordsArray.getJSONObject(0);
					if (gstnRecords.has("gstin") && (StringUtils.isNotBlank(gstnRecords.getString("gstin")))) {
						mpaMap.put("gstin", gstnRecords.getString("gstin"));
					} else {
						mpaMap.put("gstin", "");
					}
				} else {
					return errorResponseCreator(Constants.ERROR_TYPE_TRY_AGAIN);
				}
			} else {
				return errorResponseCreator(Constants.ERROR_TYPE_TRY_AGAIN);
			}
		}
		return mpaMap;
	}

	public Map<String, String> createElectricityBillResponse(String directorNo, JSONObject response) {
		Map<String, String> mpaMap = new HashMap<String, String>();
		if (response.length() < 0 || response.has("error")) {
			return errorResponseCreator(Constants.ERROR_TYPE_ELECTRICTY);
		} else {
			if (response.has("result")) {
				JSONObject result = response.getJSONObject("result");
				if (directorNo.equalsIgnoreCase("00")) {
					if (result.has("address") && (StringUtils.isNotBlank(result.getString("address")))) {
						mpaMap.put("director1Address", result.getString("address"));
					} else {
						mpaMap.put("director1Address", "");
					}
					if (result.has("name") && (StringUtils.isNotBlank(result.getString("name")))) {
						mpaMap.put("director1FullName", result.getString("name"));
					} else {
						mpaMap.put("director1FullName", "");
					}
				} else if (directorNo.equalsIgnoreCase("01")) {
					if (result.has("address") && (StringUtils.isNotBlank(result.getString("address")))) {
						mpaMap.put("director2Address", result.getString("address"));
					} else {
						mpaMap.put("director2Address", "");
					}
					if (result.has("name") && (StringUtils.isNotBlank(result.getString("name")))) {
						mpaMap.put("director2FullName", result.getString("name"));
					} else {
						mpaMap.put("director2FullName", "");
					}
				}
			} else {
				return errorResponseCreator(Constants.ERROR_TYPE_TRY_AGAIN);
			}
		}
		return null;
	}

	public Object createCinByCompanyNameResponse(JSONObject companyNameSearchResponse) {
		Map<String, String> mpaMap = new HashMap<String, String>();
		if (companyNameSearchResponse.length() < 0 || companyNameSearchResponse.has("error")) {
			return errorResponseCreator(Constants.ERROR_TYPE_CIN_BY_COMPANY_NAME);
		} else {
			if (companyNameSearchResponse.has("companyID")) {
				if (companyNameSearchResponse.has("companyID")
						&& (StringUtils.isNotBlank(companyNameSearchResponse.getString("companyID")))) {
					mpaMap.put("cin", companyNameSearchResponse.getString("companyID"));
				} else {
					mpaMap.put("cin", "");
				}
				if (companyNameSearchResponse.has("companyName")
						&& (StringUtils.isNotBlank(companyNameSearchResponse.getString("companyName")))) {
					mpaMap.put("companyName", companyNameSearchResponse.getString("companyName"));
				} else {
					mpaMap.put("companyName", "");
				}
			} else {
				return errorResponseCreator(Constants.ERROR_TYPE_COMPANY_NAME_SEARCH);
			}
		}
		return mpaMap;
	}

	public Map<String, String> createESignDataResponse(JSONObject eSignResopnseData) {
		Map<String, String> mpaMap = new HashMap<String, String>();
		if (eSignResopnseData.length() < 0 || eSignResopnseData.has("error")) {
			return errorResponseCreator(Constants.ERROR_TYPE_ESIGN_BY_AADHAAR);
		} else {
			JSONObject result = eSignResopnseData.getJSONObject("result");
			if (eSignResopnseData.has("dscData")) {
				JSONObject dscData = eSignResopnseData.getJSONObject("dscData");
				if (dscData.has("pincode") && (StringUtils.isNotBlank(dscData.getString("pincode")))) {
					mpaMap.put("pincode", dscData.getString("pincode"));
				} else {
					mpaMap.put("pincode", "");
				}
				if (dscData.has("country") && (StringUtils.isNotBlank(dscData.getString("country")))) {
					mpaMap.put("country", dscData.getString("country"));
				} else {
					mpaMap.put("country", "");
				}
				if (dscData.has("gender") && (StringUtils.isNotBlank(dscData.getString("gender")))) {
					mpaMap.put("gender", dscData.getString("gender"));
				} else {
					mpaMap.put("gender", "");
				}
				if (dscData.has("uidLastFourDigits")
						&& (StringUtils.isNotBlank(dscData.getString("uidLastFourDigits")))) {
					mpaMap.put("uidLastFourDigits", dscData.getString("uidLastFourDigits"));
				} else {
					mpaMap.put("uidLastFourDigits", "");
				}
				if (dscData.has("name") && (StringUtils.isNotBlank(dscData.getString("name")))) {
					mpaMap.put("name", dscData.getString("name"));
				} else {
					mpaMap.put("name", "");
				}
				if (dscData.has("aadhaarType") && (StringUtils.isNotBlank(dscData.getString("aadhaarType")))) {
					mpaMap.put("aadhaarType", dscData.getString("aadhaarType"));
				} else {
					mpaMap.put("aadhaarType", "");
				}
				if (dscData.has("state") && (StringUtils.isNotBlank(dscData.getString("state")))) {
					mpaMap.put("state", dscData.getString("state"));
				} else {
					mpaMap.put("state", "");
				}
				if (dscData.has("yob") && (StringUtils.isNotBlank(dscData.getString("yob")))) {
					mpaMap.put("yob", dscData.getString("yob"));
				} else {
					mpaMap.put("yob", "");
				}
				if (result.has("esignedFile") && (StringUtils.isNotBlank(result.getString("esignedFile")))) {
					mpaMap.put("esignedFile", result.getString("esignedFile"));
				} else {
					mpaMap.put("esignedFile", "");
				}
			}
		}
		logger.info("MPAResponseCreatorUI response has created : " + mpaMap.toString());
		return mpaMap;
	}

	public Map<String, String> createChequeExtractionResponse(JSONObject responseData) {
		Map<String, String> mpaMap = new HashMap<String, String>();
		if (responseData.length() < 0 || responseData.has("error")) {
			return errorResponseCreator(Constants.ERROR_TYPE_CHEQUE_EXTRACTION_BANK_VERIFICATION);
		} else {
			if (responseData.has("result")) {
				JSONObject result = responseData.getJSONObject("result");
				if (result.has("bankTransfer")) {
					JSONObject bankTransfer = result.getJSONObject("bankTransfer");

					if (bankTransfer.has("beneName") && (StringUtils.isNotBlank(bankTransfer.getString("beneName")))) {
						mpaMap.put("accountHolderName", bankTransfer.getString("beneName"));
					} else {
						mpaMap.put("accountHolderName", "");
					}

					if (bankTransfer.has("beneIFSC") && (StringUtils.isNotBlank(bankTransfer.getString("beneIFSC")))) {
						mpaMap.put("accountIfsc", bankTransfer.getString("beneIFSC"));
					} else {
						mpaMap.put("accountIfsc", "");
					}

					if (bankTransfer.has("beneMobile")
							&& (StringUtils.isNotBlank(bankTransfer.getString("beneMobile")))) {
						mpaMap.put("accountMobileNumber", bankTransfer.getString("beneMobile"));
					} else {
						mpaMap.put("accountMobileNumber", "");
					}
				} else {
					return errorResponseCreator(Constants.ERROR_TYPE_TRY_AGAIN);
				}
			} else {
				return errorResponseCreator(Constants.ERROR_TYPE_TRY_AGAIN);
			}
			if (responseData.has("essentials")) {
				JSONObject essentials = responseData.getJSONObject("essentials");
				if (essentials.has("beneficiaryAccount")
						&& (StringUtils.isNotBlank(essentials.getString("beneficiaryAccount")))) {
					mpaMap.put("accountNumber", essentials.getString("beneficiaryAccount"));
				} else {
					mpaMap.put("accountNumber", "");
				}
			} else {
				return errorResponseCreator(Constants.ERROR_TYPE_UNKNOWN);
			}
		}
		return mpaMap;
	}

	public Map<String, String> otpSentResponseCreator(String appKey) {
		Map<String, String> mpaMap = new HashMap<String, String>();
		mpaMap.put("SUCCESS", "Please submit the OTP received");
		mpaMap.put("appKey", appKey);
		return mpaMap;
	}

	public Map<String, String> createGSTR3bSummaryResponse(JSONObject gstr3bSummaryResponse) {
		Map<String, String> mpaMap = new HashMap<String, String>();
		if (gstr3bSummaryResponse.length() < 0 || gstr3bSummaryResponse.has("error")) {
			return errorResponseCreator(Constants.ERROR_TYPE_GSTR3B);
		} else {
			Double annualTurnover = 0.0;
			if (gstr3bSummaryResponse.has("result")) {
				JSONObject result = gstr3bSummaryResponse.getJSONObject("result");
				if (result.has("data")) {
					JSONObject data = result.getJSONObject("data");
					if (data.has("supplyDetails")) {
						JSONObject supplyDetails = data.getJSONObject("supplyDetails");

						if (supplyDetails.has("outwardTaxableSuppliesNonZero")) {
							JSONObject outwardTaxableSuppliesNonZero = supplyDetails
									.getJSONObject("outwardTaxableSuppliesNonZero");
							annualTurnover += Double
									.valueOf((outwardTaxableSuppliesNonZero.getString("totalTaxableValue")));
						} else {
							return errorResponseCreator(Constants.ERROR_TYPE_UNKNOWN);
						}
						if (supplyDetails.has("outwardTaxableSuppliesZero")) {
							JSONObject outwardTaxableSuppliesZero = supplyDetails
									.getJSONObject("outwardTaxableSuppliesZero");
							annualTurnover += Double.valueOf(outwardTaxableSuppliesZero.getString("totalTaxableValue"));
						} else {
							return errorResponseCreator(Constants.ERROR_TYPE_UNKNOWN);
						}
						if (supplyDetails.has("otherOutwardSupplies")) {
							JSONObject otherOutwardSupplies = supplyDetails.getJSONObject("otherOutwardSupplies");
							annualTurnover += Double.valueOf(otherOutwardSupplies.getString("totalTaxableValue"));
						} else {
							return errorResponseCreator(Constants.ERROR_TYPE_UNKNOWN);
						}
						if (supplyDetails.has("outwardSuppliesNonGst")) {
							JSONObject outwardSuppliesNonGst = supplyDetails.getJSONObject("outwardSuppliesNonGst");
							annualTurnover += Double.valueOf(outwardSuppliesNonGst.getString("totalTaxableValue"));
						} else {
							return errorResponseCreator(Constants.ERROR_TYPE_UNKNOWN);
						}
						// handle interStateSupplies into annual turnover
						// handle interStateSupplies into annual turnover
						// handle interStateSupplies into annual turnover
						// handle interStateSupplies into annual turnover
						// handle interStateSupplies into annual turnover
					}
				} else {
					return errorResponseCreator(Constants.ERROR_TYPE_UNKNOWN);
				}
			} else {
				mpaMap.put("ALLOW", "allow manual entry");
				return mpaMap;
			}
			mpaMap.put("annualTurnover", String.valueOf(roundOffToHundred(annualTurnover * 12)));
			return mpaMap;
		}
	}

	public Map<String, String> base64ResponseCreator(String encodedData) {
		Map<String, String> mpaMap = new HashMap<String, String>();
		try {
			String imageDataBytes = encodedData.substring(encodedData.indexOf(",") + 1);
			byte[] bytes = Base64.decodeBase64(imageDataBytes);
			String mimeType = encoder.getImageType(bytes);

			mpaMap.put("base64", "data:" + mimeType + ";base64," + encodedData);
			return mpaMap;
		} catch (Exception e) {
			return errorResponseCreator(Constants.ERROR_TYPE_UPLOAD);
		}
	}

	public Map<String, String> base64ResponseCreatorForDirector(String encodedData, String directorNumber) {
		Map<String, String> mpaMap = new HashMap<String, String>();
		try {
			String imageDataBytes = encodedData.substring(encodedData.indexOf(",") + 1);
			byte[] bytes = Base64.decodeBase64(imageDataBytes);
			String mimeType = encoder.getImageType(bytes);
			mpaMap.put("director" + directorNumber + "Image", "data:" + mimeType + ";base64," + encodedData);
			return mpaMap;
		} catch (Exception e) {
			return errorResponseCreator(Constants.ERROR_TYPE_UPLOAD);
		}
	}

	public Map<String, String> createDLExtractionResponse(JSONObject dlExtractionResponse, String directorNumber) {
		Map<String, String> mpaMap = new HashMap<String, String>();
		if (dlExtractionResponse.length() < 0 || dlExtractionResponse.has("error")) {
			return errorResponseCreator(Constants.ERROR_TYPE_DRIVING_LICENSE_EXTRACTION);
		} else {
			if (dlExtractionResponse.has("response")) {
				JSONObject response = dlExtractionResponse.getJSONObject("response");
				if (response.has("result")) {
					JSONObject result = response.getJSONObject("result");
					if (result.has("extractionResponse")) {
						JSONObject extractionResponse = result.getJSONObject("extractionResponse");

						if (directorNumber.equalsIgnoreCase("1")) {
							if (extractionResponse.has("address")
									&& (StringUtils.isNotBlank(extractionResponse.getString("address")))) {
								mpaMap.put("director1Address", extractionResponse.getString("address"));
							} else {
								mpaMap.put("director1Address", "");
							}
						} else if (directorNumber.equalsIgnoreCase("2")) {
							if (extractionResponse.has("address")
									&& (StringUtils.isNotBlank(extractionResponse.getString("address")))) {
								mpaMap.put("director2Address", extractionResponse.getString("address"));
							} else {
								mpaMap.put("director2Address", "");
							}
						}
					} else {
						return errorResponseCreator(Constants.ERROR_TYPE_TRY_AGAIN);
					}
				} else {
					return errorResponseCreator(Constants.ERROR_TYPE_TRY_AGAIN);
				}
			} else {
				return errorResponseCreator(Constants.ERROR_TYPE_TRY_AGAIN);
			}
		}
		return mpaMap;
	}

	public Map<String, String> createSuccessPanVerificationResponse() {
		Map<String, String> mpaMap = new HashMap<String, String>();
		mpaMap.put("STATUS", "true");
		return mpaMap;
	}

	public Map<String, String> errorResponseCreator(String error) {
		Map<String, String> mpaMap = new HashMap<String, String>();
		switch (error) {
		case Constants.ERROR_TYPE_GENERIC:
			mpaMap.put("ERROR", "Improper Data");
			break;
		case Constants.ERROR_TYPE_CIN:
			mpaMap.put("ERROR", "Improper CIN Data \nMaximum attempts for CIN search are: "
					+ PropertiesManager.propertiesMap.get(Constants.MAX_CIN_ATTEMPTS));
			break;
		case Constants.ERROR_TYPE_PAN_TO_GSTIN:
			mpaMap.put("ERROR", "Improper PAN Number or State");
			break;
		case Constants.ERROR_TYPE_ELECTRICTY:
			mpaMap.put("ERROR", "Please verify CA Number, Electricity Provider and try again");
			break;
		case Constants.ERROR_TYPE_CIN_BY_COMPANY_NAME:
			mpaMap.put("ERROR", "Please provide precisely complete company name");
			break;
		case Constants.ERROR_TYPE_CHEQUE_EXTRACTION_BANK_VERIFICATION:
			mpaMap.put("ERROR", "Either cheque image is not proper or Bank account is not active");
			break;
		case Constants.ERROR_TYPE_DRIVING_LICENSE_EXTRACTION:
			mpaMap.put("ERROR", "Driving license image is not proper!!! \nPlease try again");
			break;
		case Constants.ERROR_TYPE_BUSINESS_PAN:
			mpaMap.put("ERROR", "Invalid Business PAN / State");
			break;
		case Constants.ERROR_TYPE_INDIVIDUAL_PAN:
			mpaMap.put("ERROR", "false");
			break;
		case Constants.ERROR_TYPE_CIN_ATTEMPT:
			mpaMap.put("ERROR", "Maximum attempts for CIN search reached");
			break;
		case Constants.ERROR_TYPE_UPLOAD:
			mpaMap.put("ERROR", "Oops !!! Unable to upload \nPlease try again later.");
			break;
		case Constants.ERROR_TYPE_TRY_AGAIN:
			mpaMap.put("ERROR", "Oops something went wrong!!! \nPlease try again.");
			break;
		case Constants.ERROR_TYPE_GSTR3B:
			mpaMap.put("ERROR",
					"Oops something went wrong!!! \nInvalid details submitted for processing GSTR3b summary");
			break;
		case Constants.ERROR_TYPE_UNKNOWN:
			mpaMap.put("ERROR", "Oops something went wrong!!! \nPlease contact Administrator");
			break;
		case Constants.ERROR_TYPE_COMPANY_NAME_SEARCH:
			mpaMap.put("ERROR", "Please provide company name precisely");
			break;
		case Constants.ERROR_TYPE_SNECS:
			mpaMap.put("ERROR", "Shops and Establishment Certification failed");
			break;
		case Constants.ERROR_TYPE_GSTIN_UNAVAILABLE:
			mpaMap.put("ERROR", "No GSTIN provided for the business");
			break;
		default:
			mpaMap.put("ERROR", "Please try again later");
		}
		return mpaMap;
	}

	public List<String> extractDinFromCinResponse(JSONObject cinResponse) {
		ArrayList<String> din = null;
		if (cinResponse.length() < 0 || cinResponse.has("error")) {
			return din;
		} else {
			if (cinResponse.has("result")) {
				JSONObject result = cinResponse.getJSONObject("result");
				if (result.has("directorDetails")) {
					JSONArray directorDetailsArray = result.getJSONArray("directorDetails");
					for (int i = 0; i < directorDetailsArray.length(); i++) {
						JSONObject directorDetails = directorDetailsArray.getJSONObject(i);
						if (directorDetails.has("din")) {
							din.add(directorDetails.getString("din"));
						}
					}
					return din;
				} else {
					return din;
				}
			} else {
				return din;
			}
		}
	}

	public Map<String, Object> createStage00Response(JSONObject cinResponse) {
		Map<String, Object> mpaMap = new HashMap<String, Object>();
		if (cinResponse.length() < 0 || cinResponse.has("error")) {
			mpaMap.put("ERROR", "Oops something went wrong!!! \nPlease contact Administrator");
			return mpaMap;
		} else {
			if (cinResponse.has("result")) {
				JSONObject result = cinResponse.getJSONObject("result");

				if (result.has("companyName") && (StringUtils.isNotBlank(result.getString("companyName")))) {
					mpaMap.put("companyName", result.getString("companyName"));
				} else {
					mpaMap.put("companyName", "");
				}
				if (result.has("cin") && (StringUtils.isNotBlank(result.getString("cin")))) {
					mpaMap.put("cin", result.getString("cin"));
				} else {
					mpaMap.put("cin", "");
				}
				if (result.has("dateOfIncorporation")
						&& (StringUtils.isNotBlank(result.getString("dateOfIncorporation")))) {
					mpaMap.put("dateOfIncorporation", result.getString("dateOfIncorporation"));
				} else {
					mpaMap.put("dateOfIncorporation", "");
				}
				if (result.has("pan") && (StringUtils.isNotBlank(result.getString("pan")))) {
					mpaMap.put("businessPan", result.getString("pan"));
				} else {
					mpaMap.put("businessPan", "");
				}
				if (result.has("registeredAddress")
						&& (StringUtils.isNotBlank(result.getString("registeredAddress")))) {
					mpaMap.put("companyRegisteredAddress", result.getString("registeredAddress"));
				} else {
					mpaMap.put("companyRegisteredAddress", "");
				}
				if (result.has("emailId") && (StringUtils.isNotBlank(result.getString("emailId")))) {
					mpaMap.put("companyEmailId", result.getString("emailId"));
				} else {
					mpaMap.put("companyEmailId", "");
				}
				mpaMap.put("stage", "00");
			} else {
				mpaMap.put("ERROR", "Oops something went wrong!!! \nPlease contact Administrator");
				return mpaMap;
			}
		}
		return mpaMap;
	}

	public static double roundOffToHundred(double annualTurnover) {
		if (annualTurnover < 50) {
			double low = annualTurnover - (annualTurnover % 100);
			return Math.round(low);
		}
		return Math.round(annualTurnover / 100) * 100;
	}

	public Map<String, String> mpaApproved() {
		Map<String, String> mpaMap = new HashMap<String, String>();
		mpaMap.put("SUCCESS", "MPA Approved for further processing");
		return mpaMap;
	}

	public Map<String, String> mpaRejected() {
		Map<String, String> mpaMap = new HashMap<String, String>();
		mpaMap.put("SUCCESS", "MPA Rejected for further processing");
		return mpaMap;
	}

}
