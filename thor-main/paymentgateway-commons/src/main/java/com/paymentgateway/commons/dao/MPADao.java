package com.paymentgateway.commons.dao;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.NoResultException;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.HibernateException;
import org.hibernate.ObjectNotFoundException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.exception.DataAccessLayerException;
import com.paymentgateway.commons.user.MPAMerchant;
import com.paymentgateway.commons.user.MerchantProcessingApplication;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.util.MPAStatusType;
import com.sun.xml.bind.v2.runtime.unmarshaller.XsiNilLoader.Array;

/**
 * @author Amitosh Aanand
 *
 */
@Service
public class MPADao extends HibernateAbstractDao {

	@Autowired
	private UserDao userDao;

	private static Logger logger = LoggerFactory.getLogger(MPADao.class.getName());

	private static final String getStage00Data = "select new map (companyName as companyName, typeOfEntity as typeOfEntity, cin as cin, "
			+ "dateOfIncorporation as dateOfIncorporation, businessPan as businessPan, companyRegisteredAddress as companyRegisteredAddress, "
			+ "tradingAddress1 as tradingAddress1, tradingCountry as tradingCountry, "
			+ "tradingState as tradingState, tradingPin as tradingPin, gstin as gstin, companyPhone as companyPhone, "
			+ "companyWebsite as companyWebsite, companyEmailId as companyEmailId, businessEmailForCommunication as businessEmailForCommunication, "
			+ "mpaSavedStage as mpaSavedStage, registrationNumber as registrationNumber,"
			+ " industryCategory as industryCategory,  typeOfEntity as typeOfEntity) "
			+ "from MerchantProcessingApplication MPA where MPA.payId = :payId";

	private static final String getStage01Data = "select new map (typeOfEntity as typeOfEntity, contactName as contactName, "
			+ "contactMobile as contactMobile, contactEmail as contactEmail, contactLandline as contactLandline,"
			+ "director1Image as director1Image, director1FullName as director1FullName, director1Pan as director1Pan, director1Email as director1Email, "
			+ "director1Mobile as director1Mobile, director1Landline as director1Landline, director1Address as director1Address, "
			+ "director2Image as director2Image, director2FullName as director2FullName, director2Pan as director2Pan, director2Email as director2Email, "
			+ "director2Mobile as director2Mobile, director2Landline as director2Landline, director2Address as director2Address, "
			+ "merchantSupportEmailId as merchantSupportEmailId, merchantSupportMobileNumber as merchantSupportMobileNumber, merchantSupportLandLine as merchantSupportLandLine) "
			+ "from MerchantProcessingApplication MPA where MPA.payId = :payId";

	private static final String getStage02Data = "select new map (accountNumber as accountNumber, accountIfsc as accountIfsc, "
			+ "accountHolderName as accountHolderName,accountMobileNumber as accountMobileNumber, typeOfEntity as typeOfEntity) "
			+ "from MerchantProcessingApplication MPA where MPA.payId = :payId";

	private static final String getStage03Data = "select new map (annualTurnover as annualTurnover,  annualTurnoverOnline as annualTurnoverOnline, "
			+ " percentageCC as percentageCC,  percentageDC as percentageDC,  percentageDomestic as percentageDomestic,  "
			+ "percentageInternational as percentageInternational,  percentageCD as percentageCD,  "
			+ "percentageNeftOrImpsOrRtgs as percentageNeftOrImpsOrRtgs,  percentageNB as percentageNB,  percentageUP as percentageUP,  "
			+ "percentageWL as percentageWL,  percentageEM as percentageEM, typeOfEntity as typeOfEntity) "
			+ "from MerchantProcessingApplication MPA where MPA.payId = :payId";

	private static final String getStage04Data = "select new map (surcharge AS surcharge, "
			+ "integrationType AS integrationType, customizedInvoiceDesign AS customizedInvoiceDesign, "
			+ "internationalCards AS internationalCards, expressPay AS expressPay, expressPayParameter AS expressPayParameter, "
			+ "allowDuplicateSaleOrderId AS allowDuplicateSaleOrderId, allowDuplicateRefundOrderId AS allowDuplicateRefundOrderId, "
			+ "allowDuplicateSaleOrderIdInRefund AS allowDuplicateSaleOrderIdInRefund, "
			+ "allowDuplicateRefundOrderIdSale AS allowDuplicateRefundOrderIdSale, merchantType AS merchantType, typeOfEntity as typeOfEntity) "
			+ "from MerchantProcessingApplication MPA where MPA.payId = :payId";

	private static final String getStage05Data = "select new map (technicalContactName as technicalContactName, "
			+ "technicalContactMobile as technicalContactMobile, technicalContactEmail as technicalContactEmail, "
			+ "technicalContactLandline as technicalContactLandline, serverDetails as serverDetails, serverCompanyName as serverCompanyName, "
			+ "serverCompanyLandline as serverCompanyLandline, serverCompanyAddress as serverCompanyAddress, "
			+ "serverCompanyMobile as serverCompanyMobile, operatingSystem as operatingSystem, "
			+ "backendTechnology as backendTechnology, applicationServerTechnology as applicationServerTechnology, "
			+ "productionServerIp as productionServerIp, merchantType AS merchantType, typeOfEntity as typeOfEntity) "
			+ "from MerchantProcessingApplication MPA where MPA.payId = :payId";

	private static final String getStage06Data = "select new map (thirdPartyForCardData as thirdPartyForCardData,"
			+ " refundsAllowed as refundsAllowed, typeOfEntity as typeOfEntity) from MerchantProcessingApplication MPA where MPA.payId = :payId";

	private static final String getStage07Data = "select new map (typeOfEntity as typeOfEntity, dateOfIncorporation as dateOfIncorporation)"
			+ " from MerchantProcessingApplication MPA where MPA.payId = :payId";

	private static final String getStage08Data = "select new map (typeOfEntity as typeOfEntity, dateOfIncorporation as dateOfIncorporation, eSignResponseData as eSignResponseData)"
			+ " from MerchantProcessingApplication MPA where MPA.payId = :payId";

	private static final String getStage09Data = "select new map (payId as payId, companyName as companyName, typeOfEntity as typeOfEntity, "
			+ "cin as cin, dateOfIncorporation as dateOfIncorporation, businessPan as businessPan, "
			+ "companyRegisteredAddress as companyRegisteredAddress, tradingAddress1 as tradingAddress1, tradingCountry as tradingCountry, "
			+ "tradingState as tradingState, tradingPin as tradingPin, gstin as gstin, companyPhone as companyPhone, "
			+ "companyWebsite as companyWebsite, companyEmailId as companyEmailId, businessEmailForCommunication as businessEmailForCommunication, "
			+ "contactName as contactName, contactMobile as contactMobile, contactEmail as contactEmail, contactLandline as contactLandline, "
			+ "director1FullName as director1FullName, director1Pan as director1Pan, director1Email as director1Email, "
			+ "director1Mobile as director1Mobile, director1Landline as director1Landline, director1Address as director1Address, "
			+ "director2FullName as director2FullName, director2Pan as director2Pan, director2Email as director2Email, "
			+ "director2Mobile as director2Mobile, director2Landline as director2Landline, director2Address as director2Address, "
			+ "accountNumber as accountNumber, accountIfsc as accountIfsc, accountHolderName as accountHolderName, "
			+ "accountMobileNumber as accountMobileNumber, annualTurnover as annualTurnover, annualTurnoverOnline as annualTurnoverOnline, "
			+ "percentageCC as percentageCC, percentageDC as percentageDC, percentageDomestic as percentageDomestic, "
			+ "percentageInternational as percentageInternational, percentageCD as percentageCD, "
			+ "percentageNeftOrImpsOrRtgs as percentageNeftOrImpsOrRtgs, percentageNB as percentageNB, "
			+ "percentageUP as percentageUP, percentageWL as percentageWL, percentageEM as percentageEM, "
			+ "thirdPartyForCardData as thirdPartyForCardData, refundsAllowed as refundsAllowed, technicalContactName as technicalContactName, "
			+ "industryCategory as industryCategory, operatingSystem as operatingSystem, "
			+ "merchantType as merchantType, surcharge as surcharge, integrationType as integrationType, "
			+ "customizedInvoiceDesign as customizedInvoiceDesign, internationalCards as internationalCards, "
			+ "expressPay as expressPay, expressPayParameter as expressPayParameter) from MerchantProcessingApplication MPA where MPA.payId = :payId";


	public MPADao() {
		super();
	}

	public void create(MerchantProcessingApplication mpa) throws DataAccessLayerException {
		super.save(mpa);
	}

	public void delete(MerchantProcessingApplication mpa) throws DataAccessLayerException {
		super.delete(mpa);
	}

	public MerchantProcessingApplication find(Long id) throws DataAccessLayerException {
		return (MerchantProcessingApplication) super.find(MerchantProcessingApplication.class, id);
	}

	public void update(MerchantProcessingApplication mpa) throws DataAccessLayerException {
		super.saveOrUpdate(mpa);
	}

	public void save(MPAMerchant mpaMerchant) throws DataAccessLayerException {
		super.save(mpaMerchant);
	}

	@SuppressWarnings("unchecked")
	public int getCinAttemptCount(String payId) {
		List<MerchantProcessingApplication> mpaList = new ArrayList<MerchantProcessingApplication>();
		mpaList = null;
		int attemptCount = 0;
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		try {
			mpaList = session.createQuery("from MerchantProcessingApplication MPA where MPA.payId = :payId")
					.setParameter("payId", payId).setCacheable(true).getResultList();
			tx.commit();
			for (MerchantProcessingApplication mpaRaw : mpaList) {
				attemptCount = mpaRaw.getCinAttempts();
			}
			return attemptCount;
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
		}
		return 0;
	}

	public void saveCinResponseData(User sessionUser, String payId, JSONObject cinResponse, int cinAttempts,
			String typeOfEntity, String industryCategory) {
		if (!(cinResponse.length() < 0 || cinResponse.has("error"))) {
			MerchantProcessingApplication mpa = new MerchantProcessingApplication();
			MerchantProcessingApplication mpaData = getMpaDataperPayId(payId);

			if (mpaData == null) {
				mpa.setPayId(payId);
				mpa.setCreatedDate(new Date());
				mpa.setCinResponse(cinResponse.toString());
				mpa.setRequestedBy(sessionUser.getEmailId());
				mpa.setTypeOfEntity(typeOfEntity);
				mpa.setIndustryCategory(industryCategory);
				mpa.setCinAttempts(cinAttempts + 1);

				if (cinResponse.has("result")) {
					JSONObject result = cinResponse.getJSONObject("result");

					if (result.has("companyName") && (StringUtils.isNotBlank(result.getString("companyName")))) {
						mpa.setValidCompanyName(true);
					} else {
						mpa.setValidCompanyName(false);
					}
					if (result.has("cin") && (StringUtils.isNotBlank(result.getString("cin")))) {
						mpa.setValidCin(true);
					} else {
						mpa.setValidCin(false);
					}
					if (result.has("pan") && (StringUtils.isNotBlank(result.getString("pan")))) {
						mpa.setValidPan(true);
					} else {
						mpa.setValidPan(false);
					}
				}

				create(mpa);
			} else {
				Session session = HibernateSessionProvider.getSession();
				Transaction tx = session.beginTransaction();
				MerchantProcessingApplication mpaFromDb = session.load(MerchantProcessingApplication.class,
						mpaData.getId());
				mpaFromDb.setPayId(payId);
				mpaFromDb.setTypeOfEntity(typeOfEntity);
				mpaFromDb.setIndustryCategory(industryCategory);
				mpaFromDb.setCinResponse(cinResponse.toString());
				mpaFromDb.setUpdatedDate(new Date());
				mpaFromDb.setUpdatedBy(sessionUser.getEmailId());
				mpaFromDb.setCinAttempts(cinAttempts + 1);

				if (cinResponse.has("result")) {
					JSONObject result = cinResponse.getJSONObject("result");

					if (result.has("companyName") && (StringUtils.isNotBlank(result.getString("companyName")))) {
						mpaFromDb.setValidCompanyName(true);
					} else {
						mpaFromDb.setValidCompanyName(false);
					}
					if (result.has("cin") && (StringUtils.isNotBlank(result.getString("cin")))) {
						mpaFromDb.setValidCin(true);
					} else {
						mpaFromDb.setValidCin(false);
					}
					if (result.has("pan") && (StringUtils.isNotBlank(result.getString("pan")))) {
						mpaFromDb.setValidPan(true);
					} else {
						mpaFromDb.setValidPan(false);
					}
				}

				tx.commit();
				session.close();
			}
		}
	}

	@SuppressWarnings("deprecation")
	public MerchantProcessingApplication getMpaDataperPayId(String payId) {
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		MerchantProcessingApplication mpa = new MerchantProcessingApplication();
		try {
			mpa = (MerchantProcessingApplication) session
					.createQuery("from MerchantProcessingApplication where payId='" + payId + "'").setCacheable(true)
					.uniqueResult();
			tx.commit();
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
		}
		session.close();
		return mpa;
	}

	@SuppressWarnings("unchecked")
	public String fetchSavedStageNumber(String payId) {
		List<MerchantProcessingApplication> mpaList = new ArrayList<MerchantProcessingApplication>();
		mpaList = null;
		String stage = "";
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		try {
			mpaList = session.createQuery("from MerchantProcessingApplication MPA where MPA.payId = :payId")
					.setParameter("payId", payId).setCacheable(true).getResultList();
			tx.commit();
			for (MerchantProcessingApplication mpaRaw : mpaList) {
				stage = mpaRaw.getMpaSavedStage();
			}
			return stage;
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
		}
		return stage;
	}

	protected Object getMpaObj(String payId, String mpaSavedStage, boolean isMpaOnlineFlag) {
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		Object userObject = null;
		try {
			if (mpaSavedStage.equalsIgnoreCase("00")) {
				userObject = session.createQuery(getStage00Data).setParameter("payId", payId).setCacheable(true)
						.getSingleResult();
			} else if (mpaSavedStage.equalsIgnoreCase("01")) {
				userObject = session.createQuery(getStage01Data).setParameter("payId", payId).setCacheable(true)
						.getSingleResult();
			} else if (mpaSavedStage.equalsIgnoreCase("02")) {
				userObject = session.createQuery(getStage02Data).setParameter("payId", payId).setCacheable(true)
						.getSingleResult();
			} else if (mpaSavedStage.equalsIgnoreCase("03")) {
				userObject = session.createQuery(getStage03Data).setParameter("payId", payId).setCacheable(true)
						.getSingleResult();
			} 
//			else if (mpaSavedStage.equalsIgnoreCase("04")) {
//				userObject = session.createQuery(getStage04Data).setParameter("payId", payId).setCacheable(true)
//						.getSingleResult();
//			} else if (mpaSavedStage.equalsIgnoreCase("05")) {
//				userObject = session.createQuery(getStage05Data).setParameter("payId", payId).setCacheable(true)
//						.getSingleResult();
//			} 
//			else if (mpaSavedStage.equalsIgnoreCase("06")) {
//				userObject = session.createQuery(getStage06Data).setParameter("payId", payId).setCacheable(true)
//						.getSingleResult();
//			} 
			else if (mpaSavedStage.equalsIgnoreCase("04")) {
				userObject = session.createQuery(getStage07Data).setParameter("payId", payId).setCacheable(true)
						.getSingleResult();
			} else if (mpaSavedStage.equalsIgnoreCase("05")) {
				String query = "";
				if (isMpaOnlineFlag == false) {
					query = getStage09Data;
				} else {
					query = getStage08Data;
				}
				userObject = session.createQuery(query).setParameter("payId", payId).setCacheable(true)
						.getSingleResult();
			} else if (mpaSavedStage.equalsIgnoreCase("06")) {
				userObject = session.createQuery(getStage09Data).setParameter("payId", payId).setCacheable(true)
						.getSingleResult();
			}
			tx.commit();
		} catch (NoResultException noResultException) {
			return null;
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} catch (Exception e) {

		} finally {
			autoClose(session);
		}
		return userObject;
	}
	
	

	public void savePanToGstResponseData(User sessionUser, String payId, JSONObject response, Boolean gstVerification) {
		if (!(response.length() < 0 || response.has("error"))) {
			MerchantProcessingApplication mpa = new MerchantProcessingApplication();
			MerchantProcessingApplication mpaData = getMpaDataperPayId(payId);
			if (mpaData == null) {
				mpa.setPayId(payId);
				mpa.setCreatedDate(new Date());
				mpa.setPanToGstResponse(response.toString());
				mpa.setRequestedBy(sessionUser.getEmailId());
				mpa.setGstVerification(gstVerification);
				create(mpa);
			} else {
				Session session = HibernateSessionProvider.getSession();
				Transaction tx = session.beginTransaction();
				MerchantProcessingApplication mpaFromDb = session.load(MerchantProcessingApplication.class,
						mpaData.getId());
				mpaFromDb.setPayId(payId);
				mpaFromDb.setPanToGstResponse(response.toString());
				mpaFromDb.setUpdatedDate(new Date());
				mpaFromDb.setUpdatedBy(sessionUser.getEmailId());
				mpaFromDb.setGstVerification(gstVerification);
				tx.commit();
			}
		}
	}

	@SuppressWarnings("unchecked")
	public Map<String, Object> fetchStageDataPerPayId(String payId, String stage, boolean isMpaOnlineFlag) {
		Map<String, Object> mpaStageDetailsMap = null;
		Object mpaObject = getMpaObj(payId, stage, isMpaOnlineFlag);
		if (null != mpaObject) {
			mpaStageDetailsMap = (Map<String, Object>) mpaObject;
			mpaStageDetailsMap.put("stage", stage);
		} else {
			mpaStageDetailsMap = new HashMap<String, Object>();
			mpaStageDetailsMap.put("stage", stage);
		}
		while (mpaStageDetailsMap.values().remove(null))
			;
		return mpaStageDetailsMap;
	}
	

	@SuppressWarnings("unchecked")
	public List<MerchantProcessingApplication> fetchMPADataPerPayId(String payId) {
		List<MerchantProcessingApplication> mpaList = new ArrayList<MerchantProcessingApplication>();
		mpaList = null;
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		try {
			mpaList = session.createQuery("from MerchantProcessingApplication MPA where MPA.payId = :payId")
					.setParameter("payId", payId).setCacheable(true).getResultList();
			tx.commit();
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
		}
		return mpaList;
	}

	@SuppressWarnings("unchecked")
	public String fetchGSTR3bResponse(String payId) {
		List<MerchantProcessingApplication> mpaList = new ArrayList<MerchantProcessingApplication>();
		mpaList = null;
		String GSTR3bResponse = "";
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		try {
			mpaList = session.createQuery("from MerchantProcessingApplication MPA where MPA.payId = :payId")
					.setParameter("payId", payId).setCacheable(true).getResultList();
			tx.commit();
			for (MerchantProcessingApplication mpaRaw : mpaList) {
				GSTR3bResponse = mpaRaw.getGSTR3bResponse();
			}
			return GSTR3bResponse;
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
		}
		return GSTR3bResponse;
	}

	public void saveElectricityBillResponseData(User sessionUser, String payId, String directorNo,
			JSONObject response) {
		if (!(response.length() < 0 || response.has("error"))) {
			MerchantProcessingApplication mpaData = getMpaDataperPayId(payId);

			Session session = HibernateSessionProvider.getSession();
			Transaction tx = session.beginTransaction();
			MerchantProcessingApplication mpaFromDb = session.load(MerchantProcessingApplication.class,
					mpaData.getId());

			if (directorNo.equalsIgnoreCase("00")) {
				mpaFromDb.setDirector1ElectrictyResponse(response.toString());
			} else if (directorNo.equalsIgnoreCase("01")) {
				mpaFromDb.setDirector2ElectrictyResponse(response.toString());
			}

			mpaFromDb.setUpdatedDate(new Date());
			mpaFromDb.setUpdatedBy(sessionUser.getEmailId());
			tx.commit();
		}
	}

	@SuppressWarnings("unchecked")
	public String fetchGstinPerPayId(String payId) {
		List<MerchantProcessingApplication> mpaList = new ArrayList<MerchantProcessingApplication>();
		mpaList = null;
		String gstin = "";
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		try {
			mpaList = session.createQuery("from MerchantProcessingApplication MPA where MPA.payId = :payId")
					.setParameter("payId", payId).setCacheable(true).getResultList();
			tx.commit();
			for (MerchantProcessingApplication mpaRaw : mpaList) {
				gstin = mpaRaw.getGstin();
			}
			return gstin;
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
		}
		return gstin;
	}

	@SuppressWarnings("unchecked")
	public String fetchCinResponseDataPerPayId(String payId) {
		List<MerchantProcessingApplication> mpaList = new ArrayList<MerchantProcessingApplication>();
		mpaList = null;
		String cinResponse = "";
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		try {
			mpaList = session.createQuery("from MerchantProcessingApplication MPA where MPA.payId = :payId")
					.setParameter("payId", payId).setCacheable(true).getResultList();
			tx.commit();
			for (MerchantProcessingApplication mpaRaw : mpaList) {
				cinResponse = mpaRaw.getCinResponse();
			}
			return cinResponse;
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
		}
		return cinResponse;
	}

	public void saveChequeExtractionResponseData(User user, String payId, JSONObject response) {
		if (!(response.length() < 0 || response.has("error"))) {
			MerchantProcessingApplication mpaData = getMpaDataperPayId(payId);
			Session session = HibernateSessionProvider.getSession();
			Transaction tx = session.beginTransaction();
			MerchantProcessingApplication mpaFromDb = session.load(MerchantProcessingApplication.class,
					mpaData.getId());
			mpaFromDb.setChequeExtractionResponse(response.toString());
			mpaFromDb.setUpdatedDate(new Date());
			mpaFromDb.setUpdatedBy(user.getEmailId());
			tx.commit();
		}
	}

	public void saveGSTR3bResponseData(User user, String payId, JSONObject response) {
		if (!(response.length() < 0 || response.has("error"))) {
			MerchantProcessingApplication mpaData = getMpaDataperPayId(payId);
			Session session = HibernateSessionProvider.getSession();
			Transaction tx = session.beginTransaction();
			MerchantProcessingApplication mpaFromDb = session.load(MerchantProcessingApplication.class,
					mpaData.getId());
			mpaFromDb.setGSTR3bResponse(response.toString());
			mpaFromDb.setUpdatedDate(new Date());
			mpaFromDb.setUpdatedBy(user.getEmailId());
			tx.commit();
		}
	}

	public void saveStage00Data(User sessionUser, String payId, String companyName, String typeOfEntity, String cin,
			String registrationNumber, String dateOfIncorporation, String businessPan, String companyRegisteredAddress,
			String companyTradingAddress1, String companyTradingAddress2, String tradingCountry,
			String companyTradingAddressState, String companyTradingAddressPin, String gstin, String companyPhone,
			String companyWebsite, String companyEmailId, String businessEmailForCommunication, String industryCategory,
			String stage) {

		MerchantProcessingApplication mpaData = getMpaDataperPayId(payId);
		if (mpaData != null) {
			Session session = HibernateSessionProvider.getSession();
			Transaction tx = session.beginTransaction();
			MerchantProcessingApplication mpaFromDb = session.load(MerchantProcessingApplication.class,
					mpaData.getId());
			mpaFromDb.setMerchantName(userDao.getBusinessNameByPayId(payId));
			mpaFromDb.setCompanyName(companyName);
			mpaFromDb.setTypeOfEntity(typeOfEntity);
			mpaFromDb.setCin(cin);
			mpaFromDb.setRegistrationNumber(registrationNumber);
			mpaFromDb.setDateOfIncorporation(dateOfIncorporation);
			mpaFromDb.setBusinessPan(businessPan);
			mpaFromDb.setCompanyRegisteredAddress(companyRegisteredAddress);
			mpaFromDb.setTradingAddress1(companyTradingAddress1);
			// mpaFromDb.setTradingAddress2(companyTradingAddress2);
			mpaFromDb.setTradingCountry(tradingCountry);
			mpaFromDb.setTradingState(companyTradingAddressState);
			mpaFromDb.setTradingPin(companyTradingAddressPin);
			mpaFromDb.setGstin(gstin);
			mpaFromDb.setCompanyPhone(companyPhone);
			mpaFromDb.setCompanyWebsite(companyWebsite);
			mpaFromDb.setCompanyEmailId(companyEmailId);
			mpaFromDb.setIndustryCategory(industryCategory);
			mpaFromDb.setBusinessEmailForCommunication(businessEmailForCommunication);
			mpaFromDb.setUpdatedDate(new Date());
			mpaFromDb.setUpdatedBy(sessionUser.getEmailId());
			mpaFromDb.setStatus(MPAStatusType.PENDING.getStatusCode());
			if (StringUtils.isBlank(mpaFromDb.getMpaSavedStage())
					|| (Integer.parseInt(mpaFromDb.getMpaSavedStage()) == 0)) {
				mpaFromDb.setMpaSavedStage("00");
			}
			tx.commit();
		} else {
			MerchantProcessingApplication mpa = new MerchantProcessingApplication();
			mpa.setPayId(payId);
			mpa.setMerchantName(userDao.getBusinessNameByPayId(payId));
			mpa.setCompanyName(companyName);
			mpa.setTypeOfEntity(typeOfEntity);
			mpa.setCin(cin);
			mpa.setRegistrationNumber(registrationNumber);
			mpa.setDateOfIncorporation(dateOfIncorporation);
			mpa.setBusinessPan(businessPan);
			mpa.setCompanyRegisteredAddress(companyRegisteredAddress);
			mpa.setTradingAddress1(companyTradingAddress1);
			// mpa.setTradingAddress2(companyTradingAddress2);
			mpa.setTradingCountry(tradingCountry);
			mpa.setTradingState(companyTradingAddressState);
			mpa.setTradingPin(companyTradingAddressPin);
			mpa.setGstin(gstin);
			mpa.setIndustryCategory(industryCategory);
			mpa.setCompanyPhone(companyPhone);
			mpa.setCompanyWebsite(companyWebsite);
			mpa.setCompanyEmailId(companyEmailId);
			mpa.setBusinessEmailForCommunication(businessEmailForCommunication);
			mpa.setCreatedDate(new Date());
			mpa.setRequestedBy(sessionUser.getEmailId());
			mpa.setMpaSavedStage("00");
			mpa.setStatus(MPAStatusType.PENDING.getStatusCode());
			create(mpa);
		}
	}

	public void saveStage01Data(User sessionUser, String payId, String contactName, String contactMobile,
			String contactEmail, String contactLandline, String director1FullName, String director1Pan,
			Boolean director1PanVerified, String director1Email, String director1Mobile, String director1Landline,
			String director1Address, String director1DOB, String director2FullName, String director2Pan,
			Boolean director2PanVerified, String director2Email, String director2Mobile, String director2Landline,
			String director2Address, String director2DOB, String stage, String merchantSupportEmailId,
			String merchantSupportMobileNumber, String merchantSupportLandLine) {

		MerchantProcessingApplication mpaData = getMpaDataperPayId(payId);

		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		MerchantProcessingApplication mpaFromDb = session.load(MerchantProcessingApplication.class, mpaData.getId());

		mpaFromDb.setContactName(contactName);
		mpaFromDb.setContactMobile(contactMobile);
		mpaFromDb.setContactEmail(contactEmail);
		mpaFromDb.setContactLandline(contactLandline);
		mpaFromDb.setDirector1FullName(director1FullName);
		mpaFromDb.setDirector1Pan(director1Pan);
		mpaFromDb.setDirector1PanVerified(director1PanVerified);
		mpaFromDb.setDirector1Email(director1Email);
		mpaFromDb.setDirector1Mobile(director1Mobile);
		mpaFromDb.setDirector1Landline(director1Landline);
		mpaFromDb.setDirector1Address(director1Address);
		mpaFromDb.setDirector1DOB(director1DOB);
		mpaFromDb.setDirector2FullName(director2FullName);
		mpaFromDb.setDirector2Pan(director2Pan);
		mpaFromDb.setDirector2PanVerified(director2PanVerified);
		mpaFromDb.setDirector2Email(director2Email);
		mpaFromDb.setDirector2Mobile(director2Mobile);
		mpaFromDb.setDirector2Landline(director2Landline);
		mpaFromDb.setDirector2Address(director2Address);
		mpaFromDb.setDirector2DOB(director2DOB);
		mpaFromDb.setStatus(MPAStatusType.PENDING.getStatusCode());
		mpaFromDb.setUpdatedDate(new Date());
		mpaFromDb.setUpdatedBy(sessionUser.getEmailId());
		mpaFromDb.setMerchantSupportEmailId(merchantSupportEmailId);
		mpaFromDb.setMerchantSupportMobileNumber(merchantSupportMobileNumber);
		mpaFromDb.setMerchantSupportLandLine(merchantSupportLandLine);
		if (StringUtils.isBlank(mpaFromDb.getMpaSavedStage()) || (Integer.parseInt(mpaFromDb.getMpaSavedStage()) < 1)) {
			mpaFromDb.setMpaSavedStage("01");
		}
		tx.commit();
	}

	public void saveStage02Data(User user, String payId, String accountNumber, String accountIfsc,
			String accountHolderName, String accountMobileNumber, String stage) {

		MerchantProcessingApplication mpaData = getMpaDataperPayId(payId);
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		MerchantProcessingApplication mpaFromDb = session.load(MerchantProcessingApplication.class, mpaData.getId());

		mpaFromDb.setAccountNumber(accountNumber);
		mpaFromDb.setAccountIfsc(accountIfsc);
		mpaFromDb.setAccountHolderName(accountHolderName);
		mpaFromDb.setAccountMobileNumber(accountMobileNumber);
		mpaFromDb.setStatus(MPAStatusType.PENDING.getStatusCode());
		mpaFromDb.setUpdatedDate(new Date());
		mpaFromDb.setUpdatedBy(user.getEmailId());
		if (StringUtils.isBlank(mpaFromDb.getMpaSavedStage()) || (Integer.parseInt(mpaFromDb.getMpaSavedStage()) < 2)) {
			mpaFromDb.setMpaSavedStage("02");
		}
		tx.commit();
	}

	public void saveStage03Data(User user, String payId, String annualTurnover, String annualTurnoverOnline,
			String percentageCC, String percentageDC, String percentageDomestic, String percentageInternational,
			String percentageCD, String percentageNeftOrImpsOrRtgs, String percentageNB, String percentageUP,
			String percentageWL, String percentageEM, String stage) {

		MerchantProcessingApplication mpaData = getMpaDataperPayId(payId);
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		MerchantProcessingApplication mpaFromDb = session.load(MerchantProcessingApplication.class, mpaData.getId());

		mpaFromDb.setAnnualTurnover(annualTurnover);
		mpaFromDb.setAnnualTurnoverOnline(annualTurnoverOnline);
		mpaFromDb.setPercentageCC(percentageCC);
		mpaFromDb.setPercentageDC(percentageDC);
		mpaFromDb.setPercentageDomestic(percentageDomestic);
		mpaFromDb.setPercentageInternational(percentageInternational);
		mpaFromDb.setPercentageCD(percentageCD);
		mpaFromDb.setPercentageNeftOrImpsOrRtgs(percentageNeftOrImpsOrRtgs);
		mpaFromDb.setPercentageNB(percentageNB);
		mpaFromDb.setPercentageUP(percentageUP);
		mpaFromDb.setPercentageWL(percentageWL);
		mpaFromDb.setPercentageEM(percentageEM);
		mpaFromDb.setStatus(MPAStatusType.PENDING.getStatusCode());
		mpaFromDb.setUpdatedDate(new Date());
		mpaFromDb.setUpdatedBy(user.getEmailId());
		if (StringUtils.isBlank(mpaFromDb.getMpaSavedStage()) || (Integer.parseInt(mpaFromDb.getMpaSavedStage()) < 3)) {
			mpaFromDb.setMpaSavedStage("03");
		}
		tx.commit();
	}

	public void saveStage04Data(User user, String payId, Boolean surcharge, String integrationType,
			Boolean customizedInvoiceDesign, Boolean internationalCards, Boolean expressPay, String expressPayParameter,
			String allowDuplicateSaleOrderId, String allowDuplicateRefundOrderId,
			String allowDuplicateSaleOrderIdInRefund, String allowDuplicateRefundOrderIdSale, String stage,
			String merchantType) {

		MerchantProcessingApplication mpaData = getMpaDataperPayId(payId);
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		MerchantProcessingApplication mpaFromDb = session.load(MerchantProcessingApplication.class, mpaData.getId());
		mpaFromDb.setSurcharge(surcharge);
		mpaFromDb.setIntegrationType(integrationType);
		mpaFromDb.setCustomizedInvoiceDesign(customizedInvoiceDesign);
		mpaFromDb.setInternationalCards(internationalCards);
		mpaFromDb.setExpressPay(expressPay);
		mpaFromDb.setExpressPayParameter(expressPayParameter);
		mpaFromDb.setAllowDuplicateSaleOrderId(allowDuplicateSaleOrderId);
		mpaFromDb.setAllowDuplicateRefundOrderId(allowDuplicateRefundOrderId);
		mpaFromDb.setAllowDuplicateSaleOrderIdInRefund(allowDuplicateSaleOrderIdInRefund);
		mpaFromDb.setAllowDuplicateRefundOrderIdSale(allowDuplicateRefundOrderIdSale);
		mpaFromDb.setUpdatedDate(new Date());
		mpaFromDb.setUpdatedBy(user.getEmailId());
		if (StringUtils.isBlank(mpaFromDb.getMpaSavedStage()) || (Integer.parseInt(mpaFromDb.getMpaSavedStage()) < 4)) {
			mpaFromDb.setMpaSavedStage("04");
		}
		mpaFromDb.setStatus(MPAStatusType.PENDING.getStatusCode());
		mpaFromDb.setMerchantType(merchantType);
		tx.commit();
	}

	public void saveStage05Data(User user, String payId, String technicalContactName, String technicalContactMobile,
			String technicalContactEmail, String technicalContactLandline, String serverDetails,
			String serverCompanyName, String serverCompanyLandline, String serverCompanyAddress,
			String serverCompanyMobile, String operatingSystem, String backendTechnology,
			String applicationServerTechnology, String productionServerIp, String stage) {
//		MerchantProcessingApplication mpaData = getMpaDataperPayId(payId);
//		Session session = HibernateSessionProvider.getSession();
//		Transaction tx = session.beginTransaction();
//		MerchantProcessingApplication mpaFromDb = session.load(MerchantProcessingApplication.class, mpaData.getId());
//		mpaFromDb.setTechnicalContactName(technicalContactName);
//		mpaFromDb.setTechnicalContactMobile(technicalContactMobile);
//		mpaFromDb.setTechnicalContactEmail(technicalContactEmail);
//		mpaFromDb.setTechnicalContactLandline(technicalContactLandline);
//		mpaFromDb.setServerDetails(serverDetails);
//		mpaFromDb.setServerCompanyName(serverCompanyName);
//		mpaFromDb.setServerCompanyLandline(serverCompanyLandline);
//		mpaFromDb.setServerDetails(serverDetails);
//		mpaFromDb.setServerCompanyName(serverCompanyName);
//		mpaFromDb.setServerCompanyLandline(serverCompanyLandline);
//		mpaFromDb.setServerCompanyAddress(serverCompanyAddress);
//		mpaFromDb.setServerCompanyMobile(serverCompanyMobile);
//		mpaFromDb.setOperatingSystem(operatingSystem);
//		mpaFromDb.setBackendTechnology(backendTechnology);
//		mpaFromDb.setApplicationServerTechnology(applicationServerTechnology);
//		mpaFromDb.setProductionServerIp(productionServerIp);
//		mpaFromDb.setUpdatedDate(new Date());
//		mpaFromDb.setUpdatedBy(user.getEmailId());
//		if (StringUtils.isBlank(mpaFromDb.getMpaSavedStage()) || (Integer.parseInt(mpaFromDb.getMpaSavedStage()) < 5)) {
//			mpaFromDb.setMpaSavedStage("05");
//		}
//		mpaFromDb.setStatus(MPAStatusType.PENDING.getStatusCode());
//		tx.commit();
	}

	public void saveStage06Data(User user, String payId, String thirdPartyForCardData, String refundsAllowed,
			String stage) {
//		MerchantProcessingApplication mpaData = getMpaDataperPayId(payId);
//		Session session = HibernateSessionProvider.getSession();
//		Transaction tx = session.beginTransaction();
//		MerchantProcessingApplication mpaFromDb = session.load(MerchantProcessingApplication.class, mpaData.getId());
//		mpaFromDb.setThirdPartyForCardData(thirdPartyForCardData);
//		mpaFromDb.setRefundsAllowed(refundsAllowed);
//		mpaFromDb.setUpdatedDate(new Date());
//		mpaFromDb.setUpdatedBy(user.getEmailId());
//		mpaFromDb.setStatus(MPAStatusType.PENDING.getStatusCode());
//		if (StringUtils.isBlank(mpaFromDb.getMpaSavedStage()) || (Integer.parseInt(mpaFromDb.getMpaSavedStage()) < 6)) {
//			mpaFromDb.setMpaSavedStage("06");
//		}
//		tx.commit();
	}

	public void saveStage07Data(User user, String payId, String stage) {
		MerchantProcessingApplication mpaData = getMpaDataperPayId(payId);
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		MerchantProcessingApplication mpaFromDb = session.load(MerchantProcessingApplication.class, mpaData.getId());
		mpaFromDb.setUpdatedDate(new Date());
		mpaFromDb.setUpdatedBy(user.getEmailId());
		mpaFromDb.setStatus(MPAStatusType.PENDING.getStatusCode());
		if (StringUtils.isBlank(mpaFromDb.getMpaSavedStage()) || (Integer.parseInt(mpaFromDb.getMpaSavedStage()) < 7)) {
			mpaFromDb.setMpaSavedStage("04");
		}
		tx.commit();
	}

	public void saveStage08Data(User user, String payId, String stage) {
		MerchantProcessingApplication mpaData = getMpaDataperPayId(payId);
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		MerchantProcessingApplication mpaFromDb = session.load(MerchantProcessingApplication.class, mpaData.getId());
		mpaFromDb.setUpdatedDate(new Date());
		mpaFromDb.setUpdatedBy(user.getEmailId());
		mpaFromDb.setStatus(MPAStatusType.PENDING.getStatusCode());
		if (StringUtils.isBlank(mpaFromDb.getMpaSavedStage()) || (Integer.parseInt(mpaFromDb.getMpaSavedStage()) < 8)) {
			mpaFromDb.setMpaSavedStage("05");
		}
		tx.commit();
	}

	public Boolean saveEntityNegativeListResponse(User user, String payId, JSONObject response) {
		Boolean found = false;
		if (!(response.length() < 0 || response.has("error"))) {
			try {
				JSONObject result = response.getJSONObject("result");
				JSONObject defaulterList = result.getJSONObject("defaulterList");
				found = (Boolean) defaulterList.get("found");
			} catch (Exception e) {
				logger.error("Exception caught while saving extracting data from entity negative list response, " + e);
			} finally {
				MerchantProcessingApplication mpaData = getMpaDataperPayId(payId);
				Session session = HibernateSessionProvider.getSession();
				Transaction tx = session.beginTransaction();
				MerchantProcessingApplication mpaFromDb = session.load(MerchantProcessingApplication.class,
						mpaData.getId());
				mpaFromDb.setEntityNegativeListResponse(response.toString());
				mpaFromDb.setEntityNegativeListFound(found);
				mpaFromDb.setUpdatedDate(new Date());
				mpaFromDb.setUpdatedBy(user.getEmailId());
				tx.commit();
			}
		} else {
			MerchantProcessingApplication mpaData = getMpaDataperPayId(payId);
			Session session = HibernateSessionProvider.getSession();
			Transaction tx = session.beginTransaction();
			MerchantProcessingApplication mpaFromDb = session.load(MerchantProcessingApplication.class,
					mpaData.getId());
			mpaFromDb.setEntityNegativeListResponse(response.toString());
			mpaFromDb.setEntityNegativeListFound(found);
			mpaFromDb.setUpdatedDate(new Date());
			mpaFromDb.setUpdatedBy(user.getEmailId());
			tx.commit();
		}
		return found;
	}

	public void saveDirectorNegativeListResponse(User user, String payId, String allDirectorsNegativeJson,
			String directorNegativeRatio) {
		MerchantProcessingApplication mpaData = getMpaDataperPayId(payId);
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		MerchantProcessingApplication mpaFromDb = session.load(MerchantProcessingApplication.class, mpaData.getId());
		// mpaFromDb.setDirectorNegativeListFound(directorNegativeRatio);
		mpaFromDb.setDirectorNegativeListResponse(allDirectorsNegativeJson);
		mpaFromDb.setUpdatedDate(new Date());
		mpaFromDb.setUpdatedBy(user.getEmailId());
		tx.commit();
	}

	public void saveBankAccountTransferResponseData(User user, String payId, JSONObject response,
			Boolean accountVerification) {
		if (!(response.length() < 0 || response.has("error"))) {
			MerchantProcessingApplication mpaData = getMpaDataperPayId(payId);
			Session session = HibernateSessionProvider.getSession();
			Transaction tx = session.beginTransaction();
			MerchantProcessingApplication mpaFromDb = session.load(MerchantProcessingApplication.class,
					mpaData.getId());
			mpaFromDb.setBankAccountVerificationResponse(response.toString());
			mpaFromDb.setUpdatedDate(new Date());
			mpaFromDb.setUpdatedBy(user.getEmailId());
			mpaFromDb.setAccountVerification(accountVerification);
			tx.commit();
		}
	}

	public void saveChequeBase64(User user, String payId, String chequeBase64) {
		if (chequeBase64.length() > 0) {
			MerchantProcessingApplication mpaData = getMpaDataperPayId(payId);
			Session session = HibernateSessionProvider.getSession();
			Transaction tx = session.beginTransaction();
			MerchantProcessingApplication mpaFromDb = session.load(MerchantProcessingApplication.class,
					mpaData.getId());
			mpaFromDb.setChequeBase64(chequeBase64);
			mpaFromDb.setUpdatedDate(new Date());
			mpaFromDb.setUpdatedBy(user.getEmailId());
			tx.commit();
		}

	}

	public void saveDLBase64(User user, String payId, String base64, String directorNumber) {
		if (base64.length() > 0) {
			MerchantProcessingApplication mpaData = getMpaDataperPayId(payId);
			Session session = HibernateSessionProvider.getSession();
			Transaction tx = session.beginTransaction();
			MerchantProcessingApplication mpaFromDb = session.load(MerchantProcessingApplication.class,
					mpaData.getId());
			if (directorNumber.equalsIgnoreCase("1")) {
				mpaFromDb.setDirector1DrivingLicenseBase64(base64);
			} else if (directorNumber.equalsIgnoreCase("2")) {
				mpaFromDb.setDirector2DrivingLicenseBase64(base64);
			}
			mpaFromDb.setUpdatedDate(new Date());
			mpaFromDb.setUpdatedBy(user.getEmailId());
			tx.commit();
		}
	}

	public void saveDirectorBase64(User user, String payId, String base64, String directorNumber) {
		if (base64.length() > 0) {
			MerchantProcessingApplication mpaData = getMpaDataperPayId(payId);
			Session session = HibernateSessionProvider.getSession();
			Transaction tx = session.beginTransaction();
			MerchantProcessingApplication mpaFromDb = session.load(MerchantProcessingApplication.class,
					mpaData.getId());
			if (directorNumber.equalsIgnoreCase("1")) {
				mpaFromDb.setDirector1Image(base64);
			} else if (directorNumber.equalsIgnoreCase("2")) {
				mpaFromDb.setDirector2Image(base64);
			}
			mpaFromDb.setUpdatedDate(new Date());
			mpaFromDb.setUpdatedBy(user.getEmailId());
			tx.commit();
		}
	}

	public void saveLogoBase64(User user, String payId, String base64) {
		if (base64.length() > 0) {
			MerchantProcessingApplication mpaData = getMpaDataperPayId(payId);
			Session session = HibernateSessionProvider.getSession();
			Transaction tx = session.beginTransaction();
			MerchantProcessingApplication mpaFromDb = session.load(MerchantProcessingApplication.class,
					mpaData.getId());
			mpaFromDb.setCustomizedInvoiceBase64(base64);
			mpaFromDb.setUpdatedDate(new Date());
			mpaFromDb.setUpdatedBy(user.getEmailId());
			tx.commit();
		}
	}

	public void saveRefundPolicyBase64(User user, String payId, String base64) {
		if (base64.length() > 0) {
			MerchantProcessingApplication mpaData = getMpaDataperPayId(payId);
			Session session = HibernateSessionProvider.getSession();
			Transaction tx = session.beginTransaction();
			MerchantProcessingApplication mpaFromDb = session.load(MerchantProcessingApplication.class,
					mpaData.getId());
			mpaFromDb.setRefundPolicyBase64(base64);
			mpaFromDb.setUpdatedDate(new Date());
			mpaFromDb.setUpdatedBy(user.getEmailId());
			tx.commit();
		}
	}

	public void saveESignBase64(User user, String payId, String eSignBase64) {
		if (eSignBase64.length() > 0) {
			MerchantProcessingApplication mpaData = getMpaDataperPayId(payId);
			Session session = HibernateSessionProvider.getSession();
			Transaction tx = session.beginTransaction();
			MerchantProcessingApplication mpaFromDb = session.load(MerchantProcessingApplication.class,
					mpaData.getId());
			mpaFromDb.seteSignBase64(eSignBase64);
			mpaFromDb.setUpdatedDate(new Date());
			mpaFromDb.setUpdatedBy(user.getEmailId());
			tx.commit();
		}

	}

	@SuppressWarnings("unchecked")
	public String fetchImageBase64(String payId, String fileContentType, String directorNumber) {
		List<MerchantProcessingApplication> mpaList = new ArrayList<MerchantProcessingApplication>();
		mpaList = null;
		String base64 = "";
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		try {
			mpaList = session.createQuery("from MerchantProcessingApplication MPA where MPA.payId = :payId")
					.setParameter("payId", payId).setCacheable(true).getResultList();
			tx.commit();
			if (fileContentType.equalsIgnoreCase("CHEQUE")) {
				for (MerchantProcessingApplication mpaRaw : mpaList) {
					base64 = mpaRaw.getChequeBase64();
				}
			} else if (fileContentType.equalsIgnoreCase("DRIVING_LICENSE")) {
				for (MerchantProcessingApplication mpaRaw : mpaList) {
					if (directorNumber.equalsIgnoreCase("1")) {
						base64 = mpaRaw.getDirector1DrivingLicenseBase64();
					} else if (directorNumber.equalsIgnoreCase("2")) {
						base64 = mpaRaw.getDirector2DrivingLicenseBase64();
					}
				}
			} else if (fileContentType.equalsIgnoreCase("LOGO")) {
				for (MerchantProcessingApplication mpaRaw : mpaList) {
					base64 = mpaRaw.getCustomizedInvoiceBase64();
				}
			} else if (fileContentType.equalsIgnoreCase("ESIGN")) {
				for (MerchantProcessingApplication mpaRaw : mpaList) {
					base64 = mpaRaw.geteSignBase64();
				}
			}
			return base64;
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
		}
		return base64;
	}

	public void saveDrivingLicenseResponse(User user, String payId, String directorNumber, JSONObject response) {
		if (!(response.length() < 0 || response.has("error"))) {
			MerchantProcessingApplication mpaData = getMpaDataperPayId(payId);
			Session session = HibernateSessionProvider.getSession();
			Transaction tx = session.beginTransaction();
			MerchantProcessingApplication mpaFromDb = session.load(MerchantProcessingApplication.class,
					mpaData.getId());
			if (directorNumber.equalsIgnoreCase("1")) {
				mpaFromDb.setDirector1DrivingLicenseResponse(response.toString());
			} else if (directorNumber.equalsIgnoreCase("2")) {
				mpaFromDb.setDirector2DrivingLicenseResponse(response.toString());
			}
			mpaFromDb.setUpdatedDate(new Date());
			mpaFromDb.setUpdatedBy(user.getEmailId());
			tx.commit();
		}
	}

	public void saveESignResponse(String payId, JSONObject response) {
		if (!(response.length() < 0)) {
			MerchantProcessingApplication mpaData = getMpaDataperPayId(payId);
			Session session = HibernateSessionProvider.getSession();
			Transaction tx = session.beginTransaction();

			JSONObject dscData = response.getJSONObject("dscData");
			MerchantProcessingApplication mpaFromDb = session.load(MerchantProcessingApplication.class,
					mpaData.getId());

			mpaFromDb.seteSignResponseData(response.toString());
			mpaFromDb.setUpdatedDate(new Date());
			mpaFromDb.setEsignAadhaarType(dscData.getString("aadhaarType"));
			mpaFromDb.setEsignCountry(dscData.getString("country"));
			mpaFromDb.setEsignGender(dscData.getString("gender"));
			mpaFromDb.setEsignName(dscData.getString("name"));
			mpaFromDb.setEsignPincode(dscData.getString("pincode"));
			mpaFromDb.setEsignState(dscData.getString("state"));
			mpaFromDb.setEsignUidLastFourDigits(dscData.getString("uidLastFourDigits"));
			mpaFromDb.setEsignYOB(dscData.getString("yob"));

			// mpaFromDb.setUpdatedBy(user.getEmailId());
			tx.commit();
		}
	}

	public MerchantProcessingApplication fetchMPADataByEsignId(String eSignDataId) {
		MerchantProcessingApplication mpaData = new MerchantProcessingApplication();
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		try {
			mpaData = (MerchantProcessingApplication) session
					.createQuery("from MerchantProcessingApplication MPA where MPA.eSignDataId = :eSignDataId")
					.setCacheable(true).setParameter("eSignDataId", eSignDataId).getSingleResult();
			tx.commit();
		} catch (Exception e) {
			logger.error("exception " + e);
			return null;
		} finally {
			autoClose(session);
		}
		return mpaData;
	}

	public void saveSNECSResponseData(User sessionUser, String payId, JSONObject response) {
		if (!(response.length() < 0 || response.has("error"))) {
			MerchantProcessingApplication mpa = new MerchantProcessingApplication();
			MerchantProcessingApplication mpaData = getMpaDataperPayId(payId);
			if (mpaData == null) {
				mpa.setPayId(payId);
				mpa.setCreatedDate(new Date());
				mpa.setSnecsResponse(response.toString());
				mpa.setRequestedBy(sessionUser.getEmailId());
				create(mpa);
			} else {
				Session session = HibernateSessionProvider.getSession();
				Transaction tx = session.beginTransaction();
				MerchantProcessingApplication mpaFromDb = session.load(MerchantProcessingApplication.class,
						mpaData.getId());
				mpaFromDb.setPayId(payId);
				mpaFromDb.setSnecsResponse(response.toString());
				mpaFromDb.setUpdatedDate(new Date());
				mpaFromDb.setUpdatedBy(sessionUser.getEmailId());
				tx.commit();
				session.close();
			}
		}

	}

	@SuppressWarnings("unchecked")
	public List<MerchantProcessingApplication> fetchAllPendingMPA() {
		List<MerchantProcessingApplication> pendingMPA = new ArrayList<MerchantProcessingApplication>();
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		try {
			pendingMPA = session
					.createQuery("select payId, merchantName, companyName, typeOfEntity, createdDate, updatedDate "
							+ "from MerchantProcessingApplication MPA where MPA.status='UNDER_REVIEW'")
					.setCacheable(true).getResultList();
			tx.commit();
			return pendingMPA;
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
		}
		return pendingMPA;
	}

	public void approveMPAbyPayId(String payId, User sessionUser) {
		MerchantProcessingApplication mpaData = getMpaDataperPayId(payId);
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		MerchantProcessingApplication mpaFromDb = session.load(MerchantProcessingApplication.class, mpaData.getId());
		mpaFromDb.setStatus(MPAStatusType.REVIEW_APPROVED.getStatusCode());
		mpaFromDb.setUpdatedDate(new Date());
		mpaFromDb.setReviewedBy(sessionUser.getEmailId());
		tx.commit();
		session.close();
	}

	public void rejectMPAbyPayId(String payId, User sessionUser) {
		MerchantProcessingApplication mpaData = getMpaDataperPayId(payId);
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		MerchantProcessingApplication mpaFromDb = session.load(MerchantProcessingApplication.class, mpaData.getId());
		mpaFromDb.setStatus(MPAStatusType.REVIEW_REJECTED.getStatusCode());
		mpaFromDb.setUpdatedDate(new Date());
		mpaFromDb.setReviewedBy(sessionUser.getEmailId());
		tx.commit();
		session.close();
	}

	public MerchantProcessingApplication fetchMPADataByPayId(String payId) {
		MerchantProcessingApplication mpaData = new MerchantProcessingApplication();
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		try {
			mpaData = (MerchantProcessingApplication) session
					.createQuery("from MerchantProcessingApplication MPA where MPA.payId = :payId")
					.setParameter("payId", payId).getSingleResult();
			tx.commit();
		} catch (Exception e) {
			logger.error("exception >>", e);
			return null;
		} finally {
			autoClose(session);
		}
		return mpaData;
	}

	public MerchantProcessingApplication createValidData(JSONObject cinResponse,
			MerchantProcessingApplication mpaData) {

		if (cinResponse.has("result")) {
			JSONObject result = cinResponse.getJSONObject("result");

			if (result.has("companyName") && (StringUtils.isNotBlank(result.getString("companyName")))) {
				mpaData.setValidCompanyName(true);
			} else {
				mpaData.setValidCompanyName(false);
			}
			if (result.has("cin") && (StringUtils.isNotBlank(result.getString("cin")))) {
				mpaData.setValidCin(true);
			} else {
				mpaData.setValidCin(false);
			}
			if (result.has("pan") && (StringUtils.isNotBlank(result.getString("pan")))) {
				mpaData.setValidPan(true);
			} else {
				mpaData.setValidPan(false);
			}
		}

		return mpaData;
	}
	
	@SuppressWarnings("unchecked")
	public List<MerchantProcessingApplication> fetchMPADataPerListPayId(List<String> listPayId) {
		List<MerchantProcessingApplication> mpaList = new ArrayList<MerchantProcessingApplication>();
		
		StringBuilder payIdBuilder = new StringBuilder();
		for(String str : listPayId) {
			payIdBuilder.append(str);
			payIdBuilder.append(",");
		}
		payIdBuilder.deleteCharAt(payIdBuilder.length()-1);
		String query = "from MerchantProcessingApplication MPA where MPA.payId IN ("+payIdBuilder.toString()+")";
		mpaList = null;
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		try {
			mpaList = session.createQuery(query).setCacheable(true).getResultList();
			tx.commit();
		} catch (NullPointerException nullPointer) {
			logger.error("null pointer exception " +nullPointer);
			return null;
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
		}
		return mpaList;
	}
	
	public void savempaStage00InUser(User sessionUser, String payId) {
		try {
		User user = userDao.findPayId(payId);
		user.setMpaStage("INPROGRESS");
		userDao.update(user);
		}catch(Exception e) {
			logger.error("Exception caught in savempaStage00InUser(), " +e);
		}
	}
}
