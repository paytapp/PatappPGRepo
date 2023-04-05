package com.paymentgateway.pg.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.hibernate.HibernateException;
import org.hibernate.ObjectNotFoundException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.paymentgateway.commons.dao.HibernateAbstractDao;
import com.paymentgateway.commons.dao.HibernateSessionProvider;
import com.paymentgateway.commons.user.Account;
import com.paymentgateway.commons.user.Merchants;
import com.paymentgateway.commons.user.Mop;
import com.paymentgateway.commons.user.Payment;
import com.paymentgateway.commons.user.PaymentOptions;
import com.paymentgateway.commons.user.PendingMappingRequest;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;

@RestController
public class PaymentOptionUpdate extends HibernateAbstractDao {

	@Autowired
	private UserDao userDao;

	private static Logger logger = LoggerFactory.getLogger(PaymentOptionUpdate.class.getName());

	@RequestMapping(method = RequestMethod.POST, value = "/paymentOptionUpdate")
	public @ResponseBody String paymentOptionUpdate(@RequestBody String reqmap) {

		try {
			List<Merchants> allActiveMerchantList = userDao.getActiveMerchant();
			List<Merchants> merchantList = new ArrayList<Merchants>();
			List<Merchants> superMerchantList = new ArrayList<Merchants>();
			List<Merchants> subMerchantList = new ArrayList<Merchants>();
			for (Merchants merchant : allActiveMerchantList) {
				if (merchant.getIsSuperMerchant() == false && merchant.getSuperMerchantId() == null) {
					merchantList.add(merchant);
				}
				if (merchant.getIsSuperMerchant() == true && merchant.getSuperMerchantId() != null) {
					superMerchantList.add(merchant);
				}
			}
			for (Merchants normalMerchant : merchantList) {
				logger.info("normal merchant ==== " + normalMerchant.getBusinessName());
				insertMopTypeString(normalMerchant);

			}
			
			for (Merchants superMerchant : superMerchantList) {
				logger.info("supermerchant === " + superMerchant.getBusinessName());
				String superMerchantPayId = superMerchant.getPayId();
				List<PaymentOptions> supermerchantData = superMerchantRecord(superMerchantPayId);
				if (!supermerchantData.isEmpty()) {
					for (Merchants merchantcheck : allActiveMerchantList) {
						if (StringUtils.isNotBlank(merchantcheck.getSuperMerchantId())) {
							if (merchantcheck.getSuperMerchantId().equals(superMerchantPayId)
									&& merchantcheck.getIsSuperMerchant() == false) {
								subMerchantList.add(merchantcheck);
							}
						}
						for (Merchants subMerchant : subMerchantList) {
							logger.info("submerchant === " + subMerchant.getBusinessName());

							insertsubMerchantPaymentOptions(subMerchant, supermerchantData);

						}
						subMerchantList.clear();
					}
				}
				logger.info("  ");
				deletePaymentOptions(superMerchantPayId);
			}

			logger.info("API CALL");

			return "Update successfully";
		} catch (Exception exception) {
			logger.error("Exception", exception);
			return "Exception Caught";
		}

	}

//	@SuppressWarnings("unchecked")
//	public boolean checkIfMopTypeStringExist(String payID) throws Exception {
//
//		// find moptypestring is null or not with status active
//		// if null insertMopTypeString
//		String moptype = "";
//		logger.info("Fetching all active payment options");
//		Session session = HibernateSessionProvider.getSession();
//		Transaction tx = session.beginTransaction();
//		List<PaymentOptions> paymentOptionsList = new ArrayList<PaymentOptions>();
//		try {
//			paymentOptionsList = session
//					.createQuery("from PaymentOptions PO where PO.status = 'ACTIVE' and PO.payId =:payid")
//					.setParameter("payid", payID).getResultList();
//			tx.commit();
//			for (PaymentOptions mopcheck : paymentOptionsList) {
//				moptype = mopcheck.getMopTypeString();
//			}
//			if (moptype != null && !moptype.equalsIgnoreCase("")) {
//				logger.info(moptype + " with pay id = " + payID);
//				return true;
//			}
//
//		} catch (ObjectNotFoundException objectNotFound) {
//			handleException(objectNotFound, tx);
//		} catch (HibernateException hibernateException) {
//			handleException(hibernateException, tx);
//		} finally {
//			autoClose(session);
//		}
//		session.close();
//
//		return false;
//	}

	// ---------------------- for super merchant ---------------------

	@SuppressWarnings("unchecked")
	public List<PaymentOptions> superMerchantRecord(String payID) throws Exception {

		logger.info("Fetching payment options of supermerchant");
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		List<PaymentOptions> paymentOptionsList = new ArrayList<PaymentOptions>();
		try {
			paymentOptionsList = session
					.createQuery("from PaymentOptions PO where PO.status = 'ACTIVE' and PO.payId =:payid")
					.setParameter("payid", payID).getResultList();
			tx.commit();

		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
		}
		session.close();

		return paymentOptionsList;
	}

	// ---------------------- for normal merchant ---------------------
	public void insertMopTypeString(Merchants merchant) throws Exception {

		// check if moptype is available in PendingMappingRequest table
		try {
			String moptypenew = getmoptype(merchant.getPayId());
			// if available insert into PaymentOptions moptypeString in active status
			insertMopString(moptypenew, merchant.getPayId());

		} catch (Exception e) {
			logger.error("Exception === " , e);

		}

	}

	public void insertMopString(String mopString, String payId) {
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();

		try {
			session.createQuery(
					"update PaymentOptions PO set PO.mopTypeString = :moptype where PO.payId = :payid and PO.status = 'ACTIVE'")
					.setParameter("moptype", mopString).setParameter("payid", payId).executeUpdate();
			tx.commit();
			logger.info("update for normal merchant.............. ");

		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
		}
	}

	public void insertsubMerchantPaymentOptions(Merchants merchant, List<PaymentOptions> supermerchantData)
			throws Exception {
		logger.info("insert submerchant data.... ");

		String moptype = "";
		for (PaymentOptions data : supermerchantData) {
			PaymentOptions paymentOptions = new PaymentOptions();
			Session session = HibernateSessionProvider.getSession();
			Transaction tx = session.beginTransaction();
			paymentOptions.setPayId(merchant.getPayId());
			paymentOptions.setMerchantName(merchant.getBusinessName());
			paymentOptions.setCreditCard(Boolean.valueOf(data.isCreditCard()));
			paymentOptions.setDebitCard(Boolean.valueOf(data.isDebitCard()));
			paymentOptions.setInternational(Boolean.valueOf(data.isInternational()));
			paymentOptions.setNetBanking(Boolean.valueOf(data.isNetBanking()));
			paymentOptions.setWallet(Boolean.valueOf(data.isWallet()));
			paymentOptions.setEmi(Boolean.valueOf(data.isEmi()));
			paymentOptions.setRecurringPayment(Boolean.valueOf(data.isRecurringPayment()));
			paymentOptions.setExpressPay(Boolean.valueOf(data.isExpressPay()));
			paymentOptions.setUpi(Boolean.valueOf(data.isUpi()));
			paymentOptions.setUpiQr(Boolean.valueOf(data.isUpiQr()));
			paymentOptions.setPrepaidCard(Boolean.valueOf(data.isPrepaidCard()));
			paymentOptions.setDebitCardWithPin(Boolean.valueOf(data.isDebitCardWithPin()));
			paymentOptions.setCashOnDelivery(Boolean.valueOf(data.isCashOnDelivery()));
			paymentOptions.setCreatedDate(data.getCreatedDate());
			paymentOptions.setRequestedBy(data.getRequestedBy());
			paymentOptions.setSaveVpa(data.isSaveVpa());
			paymentOptions.setStatus("ACTIVE");
			paymentOptions.setUpdateBy(data.getUpdateBy());
			paymentOptions.setUpdatedDate(data.getUpdatedDate());
			paymentOptions.setSuperMerchantId(merchant.getSuperMerchantId());
			moptype = getmoptype(merchant.getSuperMerchantId());
			paymentOptions.setMopTypeString(moptype);
			try {
				session.save(paymentOptions);
				tx.commit();
			} catch (ObjectNotFoundException objectNotFound) {
				handleException(objectNotFound, tx);
				logger.error("Exception caught : " , objectNotFound);
			} catch (HibernateException hibernateException) {
				handleException(hibernateException, tx);
				logger.error("Exception caught : " , hibernateException);
			} finally {
				autoClose(session);
				logger.info("Hibernate session for inserting new payment options data is closed");
			}

		}

	}

//	@SuppressWarnings("unchecked")
//	public String getmoptype(String emailid) throws Exception {
//		List<PendingMappingRequest> activeMappingList = new ArrayList<PendingMappingRequest>();
//		Set<String> mops = new HashSet<String>();
//		activeMappingList = null;
//		StringBuilder moptype = new StringBuilder();
//		StringBuilder moptypenew = new StringBuilder();
//		Session session = HibernateSessionProvider.getSession();
//		Transaction tx = session.beginTransaction();
//		try {
//
//			activeMappingList = session.createQuery(
//					"from PendingMappingRequest PMR where PMR.merchantEmailId = :merchantEmailId and PMR.status = 'ACTIVE' ")
//					.setParameter("merchantEmailId", emailid).getResultList();
//			tx.commit();
//
//			for (PendingMappingRequest pendingMappingRequest : activeMappingList) {
//				moptype.append(pendingMappingRequest.getMapString() + ",");
//			}
//			moptype.deleteCharAt(moptype.length() - 1);
//			logger.info(moptype.toString());
//			String[] temp = moptype.toString().split(",", moptype.length());
//			for (String pay : temp) {
//				logger.info(pay);
//				mops.add(pay);
//			}
//			logger.info("");
//			logger.info(mops.toString());
//			for (String mopString : mops) {
//				if (!mopString.equals("")) {
//					String[] temp1 = mopString.split("-");
//					moptypenew.append(temp1[0]);
//					moptypenew.append("-");
//					moptypenew.append(temp1[1]);
//					moptypenew.append(",");
//				}
//			}
//			moptypenew.deleteCharAt(moptypenew.length() - 1);
//			logger.info("");
//			logger.info(moptypenew.toString());
//
//		} catch (ObjectNotFoundException objectNotFound) {
//			handleException(objectNotFound, tx);
//		} catch (HibernateException hibernateException) {
//			handleException(hibernateException, tx);
//		} finally {
//			autoClose(session);
//		}
//		return moptypenew.toString();
//
//	}

	public String getmoptype(String payId) throws Exception {
		Set<String> uniquePtypeMtypeSet = new HashSet<String>();
		StringBuilder uniquePtypeMtypeString = new StringBuilder();
		User user = userDao.findPayId(payId);
		Set<Account> accountSet = user.getAccounts();
		for (Account account : accountSet) {
			Set<Payment> paymentSet = account.getPayments();
			for (Payment payment : paymentSet) {
				Set<Mop> mopSet = payment.getMops();
				for (Mop mop : mopSet) {
					uniquePtypeMtypeSet.add(payment.getPaymentType().getName() + "-" + mop.getMopType().getCode());
				}
			}
		}

		for (String uniqueSet : uniquePtypeMtypeSet) {
			if (!uniqueSet.equals("")) {
				uniquePtypeMtypeString.append(uniqueSet + ",");
			}
		}
		if (StringUtils.isNotBlank(uniquePtypeMtypeString.toString())) {
			uniquePtypeMtypeString.deleteCharAt(uniquePtypeMtypeString.length() - 1);
		}
		System.out.println("MOPTYPE STRING == " + uniquePtypeMtypeString.toString());

		return uniquePtypeMtypeString.toString();

	}

	public void deletePaymentOptions(String payId) {
		Date updatedDate = new Date();
		logger.info("Deleting payment options for PayId : " + payId);
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		try {
			session.createQuery(
					"update PaymentOptions PO set PO.status = 'INACTIVE', PO.updateBy = 'admin@paymentgateway.com', PO.updatedDate = :updatedate where PO.payId = :payid and PO.status = 'ACTIVE'")
					.setParameter("updatedate", updatedDate).setParameter("payid", payId).executeUpdate();
			tx.commit();

		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
		}
	}

}
