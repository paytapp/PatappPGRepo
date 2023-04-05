package com.paymentgateway.commons.dao;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.hibernate.HibernateException;
import org.hibernate.ObjectNotFoundException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.paymentgateway.commons.user.Merchants;
import com.paymentgateway.commons.user.PaymentOptions;
import com.paymentgateway.commons.user.UserDao;

/**
 * @author Amitosh
 *
 */
@Component
public class PaymentOptionsDao extends HibernateAbstractDao {

	@Autowired
	UserDao userDao;

	private static Logger logger = LoggerFactory.getLogger(PaymentOptionsDao.class.getName());

	public PaymentOptionsDao() {
		super();
	}

	@SuppressWarnings("deprecation")
	public PaymentOptions getPaymentOption(String payId) {
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		PaymentOptions paymentOptions = new PaymentOptions();
		try {
			paymentOptions = (PaymentOptions) session
					.createQuery("from PaymentOptions where payId='" + payId + "' and status='ACTIVE'")
					.setCacheable(true).uniqueResult();
			tx.commit();
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
		}
		session.close();
		return paymentOptions;
	}
	
	@SuppressWarnings("deprecation")
	public PaymentOptions getPendingPaymentOption(String payId) {
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		PaymentOptions paymentOptions = new PaymentOptions();
		try {
			paymentOptions = (PaymentOptions) session
					.createQuery("from PaymentOptions where payId='" + payId + "' and status='PENDING'")
					.setCacheable(true).uniqueResult();
			tx.commit();
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
		}
		session.close();
		return paymentOptions;
	}
	
	@SuppressWarnings({ "deprecation", "unchecked" })
	public List<PaymentOptions> getAllPendingPaymentOption() {
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		List<PaymentOptions> paymentOptionsList = new ArrayList<PaymentOptions>();
		try {
			paymentOptionsList = session
					.createQuery("from PaymentOptions where status='PENDING'").getResultList();
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
	
	@SuppressWarnings({ "deprecation", "unchecked" })
	public List<PaymentOptions> getAllPendingPaymentOption(String status) {
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		List<PaymentOptions> paymentOptionsList = new ArrayList<PaymentOptions>();
		try {
			StringBuilder query = new StringBuilder("from PaymentOptions where status!='PENDING' and requestBySubAdmin = true");
			
			if(StringUtils.isNotBlank(status) && !status.equalsIgnoreCase("ALL")){
				query.append(" and status = '"+status+"'");
			}else{
				query.append(" and (status = 'ACTIVE' or status = 'REJECTED')");
			}
			
			paymentOptionsList = session.createQuery(query.toString()).getResultList();
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
	
	@SuppressWarnings({ "deprecation", "unchecked" })
	public PaymentOptions getPendingPaymentOptionByPayId(String payId) {
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		PaymentOptions paymentOption = new PaymentOptions();
		try {
			
			paymentOption = (PaymentOptions) session.createQuery("from PaymentOptions where status='PENDING' and payId = :payId")
					.setParameter("payId", payId).getSingleResult();

			tx.commit();
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
		}
		session.close();
		return paymentOption;
	}
	
	@SuppressWarnings("unchecked")
	public List<PaymentOptions> getPaymentOptionBySuperMerchant(String superMerchantId) {
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		List<PaymentOptions> paymentOptionsList = new ArrayList<PaymentOptions>();
		try {
			paymentOptionsList = session
					.createQuery("from PaymentOptions where superMerchantId='" + superMerchantId + "' and status='ACTIVE'")
					.getResultList();
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

	@SuppressWarnings("unchecked")
	public void createNewPaymentOption(String payId, String merchantName, String creditCard, String debitCard,
			String international, String netBanking, String wallet, String emi, String recurringPayment,
			String expressPay, String upi, String upiQr, String mqr, String prepaidCard, String debitCardWithPin,
			String cashOnDelivery, String crypto, String loginUserEmailId,String superMerchantId, String mopTypeString, String aamarPay,String Status) {

		Date createdDate = new Date();
		if (payId.equalsIgnoreCase("ALL")) {
			logger.info(
					"Inside createNewpaymentOption(), PaymentOptionsDao; inserting new payment options for all active merchants");
//			List<Merchants> merchantsList = userDao.getMerchantActiveList();
			
			if(StringUtils.isNotBlank(Status) && Status.equals("PENDING")){
				deletePendingPaymentOptions(payId, loginUserEmailId, superMerchantId);
				
			}else{
				deletePaymentOptions(payId, loginUserEmailId, superMerchantId);
			}
			
			List<Merchants> merchantsList = userDao.getSubMerchantListBySuperPayId(superMerchantId);
			if (!merchantsList.isEmpty()) {
				for (Merchants merchantRaw : merchantsList) {
					PaymentOptions paymentOptions = new PaymentOptions();
					Session session = HibernateSessionProvider.getSession();
					Transaction tx = session.beginTransaction();
					paymentOptions.setPayId(merchantRaw.getPayId());
					paymentOptions.setMerchantName(merchantRaw.getBusinessName());
					paymentOptions.setCreditCard(Boolean.valueOf(creditCard));
					paymentOptions.setDebitCard(Boolean.valueOf(debitCard));
					paymentOptions.setInternational(Boolean.valueOf(international));
					paymentOptions.setNetBanking(Boolean.valueOf(netBanking));
					paymentOptions.setWallet(Boolean.valueOf(wallet));
					paymentOptions.setEmi(Boolean.valueOf(emi));
					paymentOptions.setRecurringPayment(Boolean.valueOf(recurringPayment));
					paymentOptions.setExpressPay(Boolean.valueOf(expressPay));
					paymentOptions.setUpi(Boolean.valueOf(upi));
					paymentOptions.setUpiQr(Boolean.valueOf(upiQr));
					paymentOptions.setMqr(Boolean.valueOf(mqr));
					paymentOptions.setPrepaidCard(Boolean.valueOf(prepaidCard));
					paymentOptions.setDebitCardWithPin(Boolean.valueOf(debitCardWithPin));
					paymentOptions.setCashOnDelivery(Boolean.valueOf(cashOnDelivery));
					paymentOptions.setAamarPay(Boolean.valueOf(aamarPay));
					paymentOptions.setCrypto(Boolean.valueOf(crypto));
					paymentOptions.setSuperMerchantId(superMerchantId);
					paymentOptions.setMopTypeString(mopTypeString);
					paymentOptions.setCreatedDate(createdDate);
					paymentOptions.setRequestedBy(loginUserEmailId);
					paymentOptions.setStatus(Status);
					if(Status.equalsIgnoreCase("PENDING")){
						paymentOptions.setRequestBySubAdmin(true);
					}
					try {
						session.save(paymentOptions);
						tx.commit();
					} catch (ObjectNotFoundException objectNotFound) {
						handleException(objectNotFound, tx);
						logger.error("Exception caught, " , objectNotFound);
					} catch (HibernateException hibernateException) {
						handleException(hibernateException, tx);
						logger.error("Exception caught, " , hibernateException);
					} finally {
						autoClose(session);
						logger.info("Hibernate session for inserting new payment options data is closed");
					}
				}
			}
		} else {
			logger.info("Inside createNewpaymentOption(), PaymentOptionsDao; inserting new payment options for PayId : "
					+ payId);
			String payIdRaw[] = payId.split(",");
			String merchantNameRaw[] = merchantName.split(",");
			
			if(StringUtils.isNotBlank(Status) && Status.equals("PENDING")){
				deletePendingPaymentOptions(payId, loginUserEmailId, null);
			}else{
				deletePaymentOptions(payId, loginUserEmailId, null);
			}
			
			for (int i = 0; i < payIdRaw.length; i++) {
				Session session = HibernateSessionProvider.getSession();
				Transaction tx = session.beginTransaction();
				PaymentOptions paymentOptions = new PaymentOptions();
				paymentOptions.setPayId(payIdRaw[i]);
				paymentOptions.setMerchantName(merchantNameRaw[i]);
				paymentOptions.setCreditCard(Boolean.valueOf(creditCard));
				paymentOptions.setDebitCard(Boolean.valueOf(debitCard));
				paymentOptions.setInternational(Boolean.valueOf(international));
				paymentOptions.setNetBanking(Boolean.valueOf(netBanking));
				paymentOptions.setWallet(Boolean.valueOf(wallet));
				paymentOptions.setEmi(Boolean.valueOf(emi));
				paymentOptions.setRecurringPayment(Boolean.valueOf(recurringPayment));
				paymentOptions.setExpressPay(Boolean.valueOf(expressPay));
				paymentOptions.setUpi(Boolean.valueOf(upi));
				paymentOptions.setUpiQr(Boolean.valueOf(upiQr));
				paymentOptions.setMqr(Boolean.valueOf(mqr));
				paymentOptions.setPrepaidCard(Boolean.valueOf(prepaidCard));
				paymentOptions.setDebitCardWithPin(Boolean.valueOf(debitCardWithPin));
				paymentOptions.setCashOnDelivery(Boolean.valueOf(cashOnDelivery));
				paymentOptions.setAamarPay(Boolean.valueOf(aamarPay));
				paymentOptions.setCrypto(Boolean.valueOf(crypto));
				paymentOptions.setSuperMerchantId(superMerchantId);
				paymentOptions.setMopTypeString(mopTypeString);
				paymentOptions.setCreatedDate(createdDate);
				paymentOptions.setRequestedBy(loginUserEmailId);
				paymentOptions.setStatus(Status);
				if(Status.equalsIgnoreCase("PENDING")){
					paymentOptions.setRequestBySubAdmin(true);
				}
				try {
					session.save(paymentOptions);
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

	}

	@SuppressWarnings("unchecked")
	public List<PaymentOptions> fetchAllActivePaymentData() {
		logger.info("Fetching all active payment options");
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		List<PaymentOptions> paymentOptionsList = new ArrayList<PaymentOptions>();
		try {
			paymentOptionsList = session.createQuery("from PaymentOptions PO where PO.status = 'ACTIVE'")
					.getResultList();
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

	@SuppressWarnings("unchecked")
	public void deletePaymentOptions(String payId, String loginUserEmailId, String superMerchantId) {
		Date updatedDate = new Date();
		if (payId.equalsIgnoreCase("ALL")) {
			logger.info("Deleting all active payment options");
			List<Merchants> merchantsList = new ArrayList<Merchants>();
			if(StringUtils.isNotBlank(superMerchantId)) {
				merchantsList = userDao.getSubMerchantListBySuperPayId(superMerchantId);
			} else {
				merchantsList = userDao.getMerchantActiveList();
			}
		
			if (!merchantsList.isEmpty()) {
				for (Merchants merchantRaw : merchantsList) {
					PaymentOptions paymentOptionFromDb = getPaymentOption(merchantRaw.getPayId());
					if (paymentOptionFromDb != null) {
						Session session = HibernateSessionProvider.getSession();
						Transaction tx = session.beginTransaction();
						try {
							PaymentOptions paymentOptions = session.load(PaymentOptions.class,
									paymentOptionFromDb.getId());
							paymentOptions.setStatus("INACTIVE");
							paymentOptions.setUpdatedDate(updatedDate);
							paymentOptions.setUpdateBy(loginUserEmailId);
							session.saveOrUpdate(paymentOptions);
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
			}
		} else {
			logger.info("Deleting payment options for PayId : " + payId);
			String payIdRaw[] = payId.split(",");
			for (int i = 0; i < payIdRaw.length; i++) {
				if (getPaymentOption(payIdRaw[i]) != null) {
					PaymentOptions paymentOptionFromDb = getPaymentOption(payIdRaw[i]);
					if (paymentOptionFromDb != null) {
						Session session = HibernateSessionProvider.getSession();
						Transaction tx = session.beginTransaction();
						try {
							PaymentOptions paymentOptions = session.load(PaymentOptions.class,
									paymentOptionFromDb.getId());
							paymentOptions.setStatus("INACTIVE");
							paymentOptions.setUpdatedDate(updatedDate);
							paymentOptions.setUpdateBy(loginUserEmailId);
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
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	public void deletePendingPaymentOptions(String payId, String loginUserEmailId, String superMerchantId) {
		Date updatedDate = new Date();
		if (payId.equalsIgnoreCase("ALL")) {
			logger.info("Deleting all active payment options");
			List<Merchants> merchantsList = new ArrayList<Merchants>();
			if(StringUtils.isNotBlank(superMerchantId)) {
				merchantsList = userDao.getSubMerchantListBySuperPayId(superMerchantId);
			} else {
				merchantsList = userDao.getMerchantActiveList();
			}
		
			if (!merchantsList.isEmpty()) {
				for (Merchants merchantRaw : merchantsList) {
					PaymentOptions paymentOptionFromDb = getPendingPaymentOption(merchantRaw.getPayId());
					if (paymentOptionFromDb != null) {
						Session session = HibernateSessionProvider.getSession();
						Transaction tx = session.beginTransaction();
						try {
							PaymentOptions paymentOptions = session.load(PaymentOptions.class,
									paymentOptionFromDb.getId());
							paymentOptions.setStatus("INACTIVE");
							paymentOptions.setUpdatedDate(updatedDate);
							paymentOptions.setUpdateBy(loginUserEmailId);
							session.saveOrUpdate(paymentOptions);
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
			}
		} else {
			logger.info("Deleting payment options for PayId : " + payId);
			String payIdRaw[] = payId.split(",");
			for (int i = 0; i < payIdRaw.length; i++) {
				if (getPendingPaymentOption(payIdRaw[i]) != null) {
					PaymentOptions paymentOptionFromDb = getPendingPaymentOption(payIdRaw[i]);
					if (paymentOptionFromDb != null) {
						Session session = HibernateSessionProvider.getSession();
						Transaction tx = session.beginTransaction();
						try {
							PaymentOptions paymentOptions = session.load(PaymentOptions.class,
									paymentOptionFromDb.getId());
							paymentOptions.setStatus("INACTIVE");
							paymentOptions.setUpdatedDate(updatedDate);
							paymentOptions.setUpdateBy(loginUserEmailId);
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
			}
		}
	}

	public void updatePaymentOptionsPerPayId(String payId, String merchantName, String creditCard, String debitCard,
			String international, String netBanking, String wallet, String emi, String recurringPayment,
			String expressPay, String upi, String upiQr,String mqr, String prepaidCard, String debitCardWithPin,
			String cashOnDelivery, String crypto, String loginUserEmailId, String superMerchantId, String mopTypeString, String aamarPay, String Status) {
		PaymentOptions paymentOptionFromDb = getPaymentOption(payId);
		Date updatedDate = new Date();
		logger.info("Fetching all active payment options");
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		try {
			PaymentOptions paymentOptionsFromDb = session.load(PaymentOptions.class, paymentOptionFromDb.getId());
			paymentOptionsFromDb.setStatus("INACTIVE");
			paymentOptionsFromDb.setUpdatedDate(updatedDate);
			paymentOptionsFromDb.setUpdateBy(loginUserEmailId);
			createNewPaymentOption(payId, merchantName, creditCard, debitCard, international, netBanking, wallet, emi,
					recurringPayment, expressPay, upi, upiQr,mqr, prepaidCard, debitCardWithPin, cashOnDelivery,crypto,
					loginUserEmailId, superMerchantId, mopTypeString,aamarPay,Status);

			tx.commit();
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
		}
	}
	
	public void updatePendingPaymentOptionsPerPayId(String payId, String merchantName, String creditCard, String debitCard,
			String international, String netBanking, String wallet, String emi, String recurringPayment,
			String expressPay, String upi, String upiQr, String mqr,String prepaidCard, String debitCardWithPin,
			String cashOnDelivery, String crypto, String loginUserEmailId, String superMerchantId, String mopTypeString,String aamarPay, String Status) {
		
		logger.info("Fetching all Pending payment options");		
		try {
			createNewPaymentOption(payId, merchantName, creditCard, debitCard, international, netBanking, wallet, emi,
					recurringPayment, expressPay, upi, upiQr, mqr, prepaidCard, debitCardWithPin, cashOnDelivery,crypto,
					loginUserEmailId, superMerchantId, mopTypeString,aamarPay,Status);

		} catch (Exception e) {
			logger.error("exception creating payment option ", e);
		}
	}

	@SuppressWarnings("unchecked")
	public void activatePaymentOptionPerPaymentType(String payId, String paymentOptionList, String loginUserEmailId, String mopTypeString) {
		Date updatedDate = new Date();
		if (payId.equalsIgnoreCase("ALL")) {
			logger.info("Deleting all active payment options");
			List<Merchants> merchantsList = userDao.getMerchantActiveList();
			if (!merchantsList.isEmpty()) {
				for (Merchants merchantRaw : merchantsList) {
					PaymentOptions paymentOptionFromDb = getPaymentOption(merchantRaw.getPayId());
					if (paymentOptionFromDb != null) {
						Session session = HibernateSessionProvider.getSession();
						Transaction tx = session.beginTransaction();
						try {
							PaymentOptions paymentOptions = session.load(PaymentOptions.class,
									paymentOptionFromDb.getId());
							activatePaymetType(paymentOptions, paymentOptionList);
							paymentOptions.setUpdatedDate(updatedDate);
							paymentOptions.setUpdateBy(loginUserEmailId);
							paymentOptions.setMopTypeString(mopTypeString);
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
			}
		}else {
			String payIdRaw[] = payId.split(",");
			for (int i = 0; i < payIdRaw.length; i++) {
				PaymentOptions paymentOptionFromDb = getPaymentOption(payIdRaw[i]);
				if (paymentOptionFromDb != null) {
					Session session = HibernateSessionProvider.getSession();
					Transaction tx = session.beginTransaction();
					try {
						PaymentOptions paymentOptions = session.load(PaymentOptions.class,
								paymentOptionFromDb.getId());
						activatePaymetType(paymentOptions, paymentOptionList);
						paymentOptions.setUpdatedDate(updatedDate);
						paymentOptions.setUpdateBy(loginUserEmailId);
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
		}
	}
	public void deactivatePaymentOptionPerPaymentType(String payId, String paymentOptionList, String loginUserEmailId, String mopTypeString) {
		Date updatedDate = new Date();
		
		if (payId.equalsIgnoreCase("ALL")) {
			logger.info("Deleting all active payment options");
			List<Merchants> merchantsList = userDao.getMerchantActiveList();
			if (!merchantsList.isEmpty()) {
				for (Merchants merchantRaw : merchantsList) {
					PaymentOptions paymentOptionFromDb = getPaymentOption(merchantRaw.getPayId());
					if (paymentOptionFromDb != null) {
						Session session = HibernateSessionProvider.getSession();
						Transaction tx = session.beginTransaction();
						try {
							PaymentOptions paymentOptions = session.load(PaymentOptions.class,
									paymentOptionFromDb.getId());
							deactivatePaymentType(paymentOptions, paymentOptionList);
							paymentOptions.setUpdatedDate(updatedDate);
							paymentOptions.setUpdateBy(loginUserEmailId);
							paymentOptions.setMopTypeString(null);
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
			}
		}else {
			String payIdRaw[] = payId.split(",");
			for (int i = 0; i < payIdRaw.length; i++) {
				PaymentOptions paymentOptionFromDb = getPaymentOption(payIdRaw[i]);
				if (paymentOptionFromDb != null) {
					Session session = HibernateSessionProvider.getSession();
					Transaction tx = session.beginTransaction();
					try {
						PaymentOptions paymentOptions = session.load(PaymentOptions.class,
								paymentOptionFromDb.getId());
						deactivatePaymentType(paymentOptions, paymentOptionList);
						paymentOptions.setUpdatedDate(updatedDate);
						paymentOptions.setUpdateBy(loginUserEmailId);
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
		}
	}
	public List<PaymentOptions> fetchPaymentOptionsPerPayId(String payId) {
		logger.info("Fetching payment options for PayId : " + payId);
		PaymentOptions paymentOptionsRaw = getPaymentOption(payId);
		List<PaymentOptions> paymentOptionsList = new ArrayList<PaymentOptions>();
		if (paymentOptionsRaw != null) {
			paymentOptionsList.add(paymentOptionsRaw);
		}
		return paymentOptionsList;
	}
	
	public List<PaymentOptions> fetchPaymentOptionsForAllSubMerchant(String superMerchantId) {
		logger.info("Fetching payment options for PayId : " + superMerchantId);
		List<PaymentOptions> paymentOptionsList = getPaymentOptionBySuperMerchant(superMerchantId);
		return paymentOptionsList;
	}
	
	public PaymentOptions activatePaymetType(PaymentOptions paymentOptions, String paymentOptionList ) {
		logger.info("Fetching payment options for PayId : " );
		
		if(StringUtils.isNotBlank(paymentOptionList) && paymentOptionList.contains("creditCard")) {
			paymentOptions.setCreditCard(true);
		}
		if(StringUtils.isNotBlank(paymentOptionList) && paymentOptionList.contains("debitCard")) {
			paymentOptions.setDebitCard(true);
		}
		if(StringUtils.isNotBlank(paymentOptionList) && paymentOptionList.contains("netBanking")) {
			paymentOptions.setNetBanking(true);
		}
		if(StringUtils.isNotBlank(paymentOptionList) && paymentOptionList.contains("wallet")) {
			paymentOptions.setWallet(true);
		}
		if(StringUtils.isNotBlank(paymentOptionList) && paymentOptionList.contains("emi")) {
			paymentOptions.setEmi(true);
		}
		if(StringUtils.isNotBlank(paymentOptionList) && paymentOptionList.contains("recurringPayment")) {
			paymentOptions.setRecurringPayment(true);
		}
		if(StringUtils.isNotBlank(paymentOptionList) && paymentOptionList.contains("expressPay")) {
			paymentOptions.setExpressPay(true);
		}
		if(StringUtils.isNotBlank(paymentOptionList) && paymentOptionList.contains("upi")) {
			paymentOptions.setUpi(true);
		}
		if(StringUtils.isNotBlank(paymentOptionList) && paymentOptionList.contains("upiQr")) {
			paymentOptions.setUpiQr(true);
		}
		if(StringUtils.isNotBlank(paymentOptionList) && paymentOptionList.contains("prepaidCard")) {
			paymentOptions.setPrepaidCard(true);
		}
		if(StringUtils.isNotBlank(paymentOptionList) && paymentOptionList.contains("debitCardWithPin")) {
			paymentOptions.setDebitCardWithPin(true);
		}
		if(StringUtils.isNotBlank(paymentOptionList) && paymentOptionList.contains("cashOnDelivery")) {
			paymentOptions.setCashOnDelivery(true);
		}
		if(StringUtils.isNotBlank(paymentOptionList) && paymentOptionList.contains("international")) {
			paymentOptions.setInternational(true);
		}
		if(StringUtils.isNotBlank(paymentOptionList) && paymentOptionList.contains("aamarPay")) {
			paymentOptions.setAamarPay(true);
		}
		
		return paymentOptions;
	}
	public PaymentOptions deactivatePaymentType(PaymentOptions paymentOptions, String paymentOptionList) {
		logger.info("Fetching payment options for PayId : " );
		
		
		if(StringUtils.isNotBlank(paymentOptionList) && paymentOptionList.contains("creditCard")) {
			paymentOptions.setCreditCard(false);
		}
		if(StringUtils.isNotBlank(paymentOptionList) && paymentOptionList.contains("debitCard")) {
			paymentOptions.setDebitCard(false);
		}
		if(StringUtils.isNotBlank(paymentOptionList) && paymentOptionList.contains("netBanking")) {
			paymentOptions.setNetBanking(false);
		}
		if(StringUtils.isNotBlank(paymentOptionList) && paymentOptionList.contains("wallet")) {
			paymentOptions.setWallet(false);
		}
		if(StringUtils.isNotBlank(paymentOptionList) && paymentOptionList.contains("emi")) {
			paymentOptions.setEmi(false);
		}
		if(StringUtils.isNotBlank(paymentOptionList) && paymentOptionList.contains("recurringPayment")) {
			paymentOptions.setRecurringPayment(false);
		}
		if(StringUtils.isNotBlank(paymentOptionList) && paymentOptionList.contains("expressPay")) {
			paymentOptions.setExpressPay(false);
		}
		if(StringUtils.isNotBlank(paymentOptionList) && paymentOptionList.contains("upi")) {
			paymentOptions.setUpi(false);
		}
		if(StringUtils.isNotBlank(paymentOptionList) && paymentOptionList.contains("upiQr")) {
			paymentOptions.setUpiQr(false);
		}
		if(StringUtils.isNotBlank(paymentOptionList) && paymentOptionList.contains("prepaidCard")) {
			paymentOptions.setPrepaidCard(false);
		}
		if(StringUtils.isNotBlank(paymentOptionList) && paymentOptionList.contains("debitCardWithPin")) {
			paymentOptions.setDebitCardWithPin(false);
		}
		if(StringUtils.isNotBlank(paymentOptionList) && paymentOptionList.contains("cashOnDelivery")) {
			paymentOptions.setCashOnDelivery(false);
		}
		if(StringUtils.isNotBlank(paymentOptionList) && paymentOptionList.contains("aamarPay")) {
			paymentOptions.setAamarPay(true);
		}
		if(StringUtils.isNotBlank(paymentOptionList) && paymentOptionList.contains("international")) {
			paymentOptions.setInternational(false);
		}
		
		return paymentOptions;
	}

	public void updatePendingPaymentOptionsStatus(PaymentOptions pendingPaymentOption, String loginUserEmailId) {
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		try {
			session.saveOrUpdate(pendingPaymentOption);
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