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
import com.paymentgateway.commons.user.SUFDetail;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.util.PaymentType;
import com.paymentgateway.commons.util.UserStatusType;

/**
 * @author Amitosh, Rajit, Sandeep, shiva
 *
 */
@Component
public class SUFDetailDao extends HibernateAbstractDao {

	@Autowired
	UserDao userDao;

	private static Logger logger = LoggerFactory.getLogger(SUFDetailDao.class.getName());

	public SUFDetailDao() {
		super();
	}

	@SuppressWarnings("deprecation")
	public SUFDetail getSUFDetail(String payId, String paymentType, String txnType, String mopType,
			String paymentRegion, String slab, String subMerchantId) {
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		SUFDetail sufDetails = new SUFDetail();
		try {
			StringBuilder query = new StringBuilder(
					"from SUFDetail where payId='" + payId + "' and paymentType='" + paymentType
							+ "' and txnType='" + txnType + "' and mopType='" + mopType + "' and paymentRegion='"
							+ paymentRegion + "' and slab='" + slab + "' and status='ACTIVE'");
			if (StringUtils.isNotBlank(subMerchantId)) {
				query.append(" and subMerchantPayId = '" + subMerchantId + "'");
			}
			sufDetails = (SUFDetail) session
					.createQuery(query.toString())
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
		return sufDetails;
	}

	@SuppressWarnings({ "deprecation", "unchecked" })
	public List<SUFDetail> getSUFDetailForSlabCheck(String payId, String paymentType, String txnType, String mopType,
			String paymentRegion, String subMerchantId) {

		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		List<SUFDetail> sufDetailList = new ArrayList<SUFDetail>();
		try {
			StringBuilder query = new StringBuilder(
					"Select mopType, payId, paymentRegion, paymentType, slab, txnType from SUFDetail where payId='"
							+ payId + "' and paymentType='" + paymentType + "' and txnType='" + txnType
							+ "' and mopType='" + mopType + "' and paymentRegion='" + paymentRegion
							+ "' and status='ACTIVE'");
			if (StringUtils.isNotBlank(subMerchantId)) {
				query.append(" and subMerchantPayId = '" + subMerchantId + "'");
			}
			List<Object[]> sufListRow = session.createQuery(query.toString()).setCacheable(true).getResultList();

			for (Object[] object : sufListRow) {
				SUFDetail sufDetail = new SUFDetail();
				sufDetail.setMopType((String) object[0]);
				sufDetail.setPayId((String) object[1]);
				sufDetail.setPaymentRegion((String) object[2]);
				sufDetail.setPaymentType((String) object[3]);
				sufDetail.setSlab((String) object[4]);
				sufDetail.setTxnType((String) object[5]);
				sufDetailList.add(sufDetail);
			}

			tx.commit();
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
		}
		session.close();
		return sufDetailList;
	}

	@SuppressWarnings("unchecked")
	public String createNewSufDetail(String payId, String merchantName, String txnType, String paymentType,
			String charges, String mopType, String percentageAmount, String loginUserEmailId, String paymentRegion,
			String slab, String subMerchantId) {
		Transaction tx = null;
		Session session = null;
		String response = "";
		boolean saveAnySuf = false;

		Date createdDate = new Date();

		if (payId.equalsIgnoreCase("ALL")) {
			try {
				logger.info(
						"Inside createNewSufDetail(), SUFDetailDao; inserting new SUF Detail for all active merchants");

				List<Merchants> merchantsList = userDao.getMerchantActiveList();
				String paymentTypeCheck = "";
				String paymentTypes[] = paymentType.split(",");
				String mopTypes[] = mopType.split(",");
				String slabs[] = slab.split(",");
				if (!merchantsList.isEmpty()) {

					for (Merchants merchantRaw : merchantsList) {
						SUFDetail sufDetails = new SUFDetail();

						if (merchantRaw.getIsSuperMerchant()
								&& StringUtils.isNotBlank(merchantRaw.getSuperMerchantId())) {
							List<Merchants> subMerchantList = userDao
									.getSubMerchantListBySuperPayId(merchantRaw.getPayId());

							for (Merchants subMerchantRow : subMerchantList) {
								sufDetails=new SUFDetail();
								sufDetails.setSubMerchantPayId(subMerchantRow.getPayId());
								sufDetails.setPayId(merchantRaw.getPayId());
								sufDetails.setMerchantName(merchantRaw.getBusinessName());
								sufDetails.setSubMerchantName(subMerchantRow.getBusinessName());

								for (int i = 0; i < paymentTypes.length; i++) {
									for (int j = 0; j < mopTypes.length; j++) {
										for (int k = 0; k < slabs.length; k++) {

											if (StringUtils.isNotBlank(paymentTypes[i])
													&& !paymentTypes[i].equalsIgnoreCase("NA")) {
												paymentTypeCheck = PaymentType.getpaymentName(paymentTypes[i]);
											} else {
												paymentTypeCheck = paymentTypes[i];
											}
											response = checkSufConflictBySlab(merchantRaw.getPayId(), loginUserEmailId,
													txnType, paymentTypeCheck, mopTypes[j], paymentRegion, slabs[k],
													subMerchantRow.getPayId());

											if (response.equalsIgnoreCase("ERROR") || response.equalsIgnoreCase("Duplicate")) {
												if (!saveAnySuf) {
													saveAnySuf = false;
												}
											} else {

												saveAnySuf = true;
												/*
												 * deleteSufDetail(merchantRaw.
												 * getPayId(), loginUserEmailId,
												 * txnType,
												 * PaymentType.getpaymentName(
												 * paymentTypes[i]),
												 * mopTypes[j],
												 * paymentRegion,slabs[k]);
												 */
												session = HibernateSessionProvider.getSession();
												tx = session.beginTransaction();
												sufDetails.setCreateDate(createdDate);
												sufDetails.setRequestedBy(loginUserEmailId);
												sufDetails.setStatus("ACTIVE");
												sufDetails.setFixedCharge(charges);
												sufDetails.setTxnType(txnType);
												if (StringUtils.isNotBlank(paymentTypes[i])
														&& !paymentTypes[i].equalsIgnoreCase("NA")) {
													sufDetails.setPaymentType(
															PaymentType.getpaymentName(paymentTypes[i]));
												} else {
													sufDetails.setPaymentType(paymentTypes[i]);
												}
												sufDetails.setMopType(mopTypes[j]);
												sufDetails.setPercentageAmount(percentageAmount);
												sufDetails.setPaymentRegion(paymentRegion);
												sufDetails.setSlab(slabs[k]);

												session.save(sufDetails);
												tx.commit();
											}
										}
									}
								}
							}

						} else {
							sufDetails.setPayId(merchantRaw.getPayId());
							sufDetails.setMerchantName(merchantRaw.getBusinessName());

							for (int i = 0; i < paymentTypes.length; i++) {
								for (int j = 0; j < mopTypes.length; j++) {
									for (int k = 0; k < slabs.length; k++) {

										if (StringUtils.isNotBlank(paymentTypes[i])
												&& !paymentTypes[i].equalsIgnoreCase("NA")) {
											paymentTypeCheck = PaymentType.getpaymentName(paymentTypes[i]);
										} else {
											paymentTypeCheck = paymentTypes[i];
										}
										response = checkSufConflictBySlab(merchantRaw.getPayId(), loginUserEmailId,
												txnType, paymentTypeCheck, mopTypes[j], paymentRegion, slabs[k],
												subMerchantId);

										if (response.equalsIgnoreCase("ERROR") || response.equalsIgnoreCase("Duplicate")) {
											if (!saveAnySuf) {
												saveAnySuf = false;
											}
										} else {

											saveAnySuf = true;
											/*
											 * deleteSufDetail(merchantRaw.
											 * getPayId(), loginUserEmailId,
											 * txnType,
											 * PaymentType.getpaymentName(
											 * paymentTypes[i]), mopTypes[j],
											 * paymentRegion,slabs[k]);
											 */
											session = HibernateSessionProvider.getSession();
											tx = session.beginTransaction();
											sufDetails.setCreateDate(createdDate);
											sufDetails.setRequestedBy(loginUserEmailId);
											sufDetails.setStatus("ACTIVE");
											sufDetails.setFixedCharge(charges);
											sufDetails.setTxnType(txnType);
											if (StringUtils.isNotBlank(paymentTypes[i])
													&& !paymentTypes[i].equalsIgnoreCase("NA")) {
												sufDetails.setPaymentType(PaymentType.getpaymentName(paymentTypes[i]));
											} else {
												sufDetails.setPaymentType(paymentTypes[i]);
											}
											sufDetails.setMopType(mopTypes[j]);
											sufDetails.setPercentageAmount(percentageAmount);
											sufDetails.setPaymentRegion(paymentRegion);
											sufDetails.setSlab(slabs[k]);

											session.save(sufDetails);
											tx.commit();
										}
									}
								}
							}
						}

					}
				}
			} catch (ObjectNotFoundException objectNotFound) {
				handleException(objectNotFound, tx);
				logger.error("Exception caught, ", objectNotFound);
				return "ERROR";
			} catch (HibernateException hibernateException) {
				handleException(hibernateException, tx);
				logger.error("Exception caught, ", hibernateException);
				return "ERROR";
			} finally {
				autoClose(session);
				logger.info("Hibernate session for inserting new SUF Details data is closed");
			}
			return "SUCCESS";
		} else {
			logger.info("Inside createNewSufDetail(), SUFDetailDao; inserting new SUF Details for PayId : " + payId
					+ " Sub Merchant Id " + subMerchantId);
			try {
				String paymentTypeCheck = "";
				SUFDetail sufDetails = new SUFDetail();
				String paymentTypes[] = paymentType.split(",");
				String mopTypes[] = mopType.split(",");
				String merchant[] = merchantName.split(",");
				String merchantId[] = payId.split(",");
				String slabs[] = slab.split(",");
				String subMerchantIds[] = subMerchantId.split(",");
				

				if (StringUtils.isNotBlank(subMerchantId) && subMerchantId.equalsIgnoreCase("ALL")) {

					List<Merchants> subMerchantList = userDao.getSubMerchantListBySuperPayId(payId);

					for (Merchants subMerchantRow : subMerchantList) {
						sufDetails=new SUFDetail();
						sufDetails.setSubMerchantPayId(subMerchantRow.getPayId());
						sufDetails.setPayId(payId);
						sufDetails.setMerchantName(merchantName);
						sufDetails.setSubMerchantName(subMerchantRow.getBusinessName());

						for (int i = 0; i < paymentTypes.length; i++) {
							for (int j = 0; j < mopTypes.length; j++) {
								for (int k = 0; k < slabs.length; k++) {

									if (StringUtils.isNotBlank(paymentTypes[i])
											&& !paymentTypes[i].equalsIgnoreCase("NA")) {
										paymentTypeCheck = PaymentType.getpaymentName(paymentTypes[i]);
									} else {
										paymentTypeCheck = paymentTypes[i];
									}
									response = checkSufConflictBySlab(payId, loginUserEmailId, txnType,
											paymentTypeCheck, mopTypes[j], paymentRegion, slabs[k],
											subMerchantRow.getPayId());

									if (response.equalsIgnoreCase("ERROR") || response.equalsIgnoreCase("Duplicate")) {
										if (!saveAnySuf) {
											saveAnySuf = false;
										}
									} else {

										saveAnySuf = true;
										/*
										 * deleteSufDetail(merchantRaw.getPayId(
										 * ), loginUserEmailId, txnType,
										 * PaymentType.getpaymentName(
										 * paymentTypes[i]), mopTypes[j],
										 * paymentRegion,slabs[k]);
										 */
										session = HibernateSessionProvider.getSession();
										tx = session.beginTransaction();
										sufDetails.setCreateDate(createdDate);
										sufDetails.setRequestedBy(loginUserEmailId);
										sufDetails.setStatus("ACTIVE");
										sufDetails.setFixedCharge(charges);
										sufDetails.setTxnType(txnType);
										if (StringUtils.isNotBlank(paymentTypes[i])
												&& !paymentTypes[i].equalsIgnoreCase("NA")) {
											sufDetails.setPaymentType(PaymentType.getpaymentName(paymentTypes[i]));
										} else {
											sufDetails.setPaymentType(paymentTypes[i]);
										}
										sufDetails.setMopType(mopTypes[j]);
										sufDetails.setPercentageAmount(percentageAmount);
										sufDetails.setPaymentRegion(paymentRegion);
										sufDetails.setSlab(slabs[k]);

										session.save(sufDetails);
										tx.commit();
									}
								}
							}
						}
					}
				} else if (StringUtils.isNotBlank(subMerchantId) && !subMerchantId.equalsIgnoreCase("ALL")) {
					String[] subMerchantList = subMerchantId.split(",");

					for (String subMerchantPayId : subMerchantList) {
						
						String businessName=userDao.getBusinessNameByPayId(subMerchantPayId);
						sufDetails = new SUFDetail();
						sufDetails.setSubMerchantPayId(subMerchantPayId);
						sufDetails.setPayId(payId);
						sufDetails.setMerchantName(merchantName);
						sufDetails.setSubMerchantName(businessName);

						for (int i = 0; i < paymentTypes.length; i++) {
							for (int j = 0; j < mopTypes.length; j++) {
								for (int k = 0; k < slabs.length; k++) {

									if (StringUtils.isNotBlank(paymentTypes[i])
											&& !paymentTypes[i].equalsIgnoreCase("NA")) {
										paymentTypeCheck = PaymentType.getpaymentName(paymentTypes[i]);
									} else {
										paymentTypeCheck = paymentTypes[i];
									}
									response = checkSufConflictBySlab(payId, loginUserEmailId, txnType,
											paymentTypeCheck, mopTypes[j], paymentRegion, slabs[k],
											subMerchantPayId);

									if (response.equalsIgnoreCase("ERROR") || response.equalsIgnoreCase("Duplicate")) {
										if (!saveAnySuf) {
											saveAnySuf = false;
										}
										
									}else {

										saveAnySuf = true;
										/*
										 * deleteSufDetail(merchantRaw.getPayId(
										 * ), loginUserEmailId, txnType,
										 * PaymentType.getpaymentName(
										 * paymentTypes[i]), mopTypes[j],
										 * paymentRegion,slabs[k]);
										 */
										session = HibernateSessionProvider.getSession();
										tx = session.beginTransaction();
										sufDetails.setCreateDate(createdDate);
										sufDetails.setRequestedBy(loginUserEmailId);
										sufDetails.setStatus("ACTIVE");
										sufDetails.setFixedCharge(charges);
										sufDetails.setTxnType(txnType);
										if (StringUtils.isNotBlank(paymentTypes[i])
												&& !paymentTypes[i].equalsIgnoreCase("NA")) {
											sufDetails.setPaymentType(PaymentType.getpaymentName(paymentTypes[i]));
										} else {
											sufDetails.setPaymentType(paymentTypes[i]);
										}
										sufDetails.setMopType(mopTypes[j]);
										sufDetails.setPercentageAmount(percentageAmount);
										sufDetails.setPaymentRegion(paymentRegion);
										sufDetails.setSlab(slabs[k]);

										session.save(sufDetails);
										tx.commit();
									}
								}
							}
						}
					}
				}else{
					
					for(String merchantid : merchantId){
						
						User user=userDao.findPayId(merchantid);
						
						if(user.isSuperMerchant()){
							
							List<Merchants> subMerchantList = userDao.getSubMerchantListBySuperPayId(merchantid);

							for (Merchants subMerchantRow : subMerchantList) {
								sufDetails=new SUFDetail();
								sufDetails.setSubMerchantPayId(subMerchantRow.getPayId());
								sufDetails.setPayId(merchantid);
								sufDetails.setMerchantName(user.getBusinessName());
								sufDetails.setSubMerchantName(subMerchantRow.getBusinessName());

								for (int i = 0; i < paymentTypes.length; i++) {
									for (int j = 0; j < mopTypes.length; j++) {
										for (int k = 0; k < slabs.length; k++) {

											if (StringUtils.isNotBlank(paymentTypes[i])
													&& !paymentTypes[i].equalsIgnoreCase("NA")) {
												paymentTypeCheck = PaymentType.getpaymentName(paymentTypes[i]);
											} else {
												paymentTypeCheck = paymentTypes[i];
											}
											response = checkSufConflictBySlab(merchantid, loginUserEmailId, txnType,
													paymentTypeCheck, mopTypes[j], paymentRegion, slabs[k],
													subMerchantRow.getPayId());
											
											if (response.equalsIgnoreCase("ERROR") || response.equalsIgnoreCase("Duplicate")) {
												if (!saveAnySuf) {
													saveAnySuf = false;
												}
											} else {

												saveAnySuf = true;
												/*
												 * deleteSufDetail(merchantId[l],
												 * loginUserEmailId, txnType,
												 * PaymentType.getpaymentName(
												 * paymentTypes[i]), mopTypes[j],
												 * paymentRegion, slabs[k]);
												 */

												session = HibernateSessionProvider.getSession();
												tx = session.beginTransaction();
												
												if (StringUtils.isNotBlank(paymentTypes[i])
														&& !paymentTypes[i].equalsIgnoreCase("NA")) {
													sufDetails.setPaymentType(PaymentType.getpaymentName(paymentTypes[i]));
												} else {
													sufDetails.setPaymentType(paymentTypes[i]);
												}
												sufDetails.setMopType(mopTypes[j]);
												sufDetails.setPercentageAmount(percentageAmount);
												sufDetails.setCreateDate(createdDate);
												sufDetails.setRequestedBy(loginUserEmailId);
												sufDetails.setStatus("ACTIVE");
												sufDetails.setFixedCharge(charges);
												sufDetails.setTxnType(txnType);
												sufDetails.setPaymentRegion(paymentRegion);
												sufDetails.setSlab(slabs[k]);

												session.save(sufDetails);
												tx.commit();

											}
										}
									}
								}
							}
							
						}else{
							sufDetails=new SUFDetail();
							for (int i = 0; i < paymentTypes.length; i++) {
								for (int j = 0; j < mopTypes.length; j++) {
									for (int k = 0; k < slabs.length; k++) {
											if (StringUtils.isNotBlank(paymentTypes[i])
													&& !paymentTypes[i].equalsIgnoreCase("NA")) {
												paymentTypeCheck = PaymentType.getpaymentName(paymentTypes[i]);
											} else {
												paymentTypeCheck = paymentTypes[i];
											}
											response = checkSufConflictBySlab(merchantid, loginUserEmailId, txnType,
													paymentTypeCheck, mopTypes[j], paymentRegion, slabs[k], subMerchantId);

											if (response.equalsIgnoreCase("ERROR") || response.equalsIgnoreCase("Duplicate")) {
												if (!saveAnySuf) {
													saveAnySuf = false;
												}
											} else {

												saveAnySuf = true;
												/*
												 * deleteSufDetail(merchantId[l],
												 * loginUserEmailId, txnType,
												 * PaymentType.getpaymentName(
												 * paymentTypes[i]), mopTypes[j],
												 * paymentRegion, slabs[k]);
												 */

												session = HibernateSessionProvider.getSession();
												tx = session.beginTransaction();
												sufDetails.setPayId(merchantid);
												sufDetails.setMerchantName(user.getBusinessName());
												if (StringUtils.isNotBlank(paymentTypes[i])
														&& !paymentTypes[i].equalsIgnoreCase("NA")) {
													sufDetails.setPaymentType(PaymentType.getpaymentName(paymentTypes[i]));
												} else {
													sufDetails.setPaymentType(paymentTypes[i]);
												}
												sufDetails.setMopType(mopTypes[j]);
												sufDetails.setPercentageAmount(percentageAmount);
												sufDetails.setCreateDate(createdDate);
												sufDetails.setRequestedBy(loginUserEmailId);
												sufDetails.setStatus("ACTIVE");
												sufDetails.setFixedCharge(charges);
												sufDetails.setTxnType(txnType);
												sufDetails.setPaymentRegion(paymentRegion);
												sufDetails.setSlab(slabs[k]);

												session.save(sufDetails);
												tx.commit();

											
										}
									}
								}
							}
						}
						
					}
				}
			} catch (ObjectNotFoundException objectNotFound) {
				handleException(objectNotFound, tx);
				return "ERROR";
			} catch (HibernateException hibernateException) {
				handleException(hibernateException, tx);
				return "ERROR";
			} finally {
				autoClose(session);
			}
			if (saveAnySuf) {
				return response;
			} else {
				return response;
			}
		}
	}

	public String updateNewSufDetail(String payId, String merchantName, String txnType, String paymentType,
			String charges, String mopType, String percentageAmount, String loginUserEmailId, String paymentRegion,
			String slab, String subMerchantId) {
		Transaction tx = null;
		Session session = null;
		try {

			Date createdDate = new Date();
			SUFDetail sufDetails = new SUFDetail();
			/*
			 * boolean saveAnySuf = false;
			 * 
			 * Date createdDate = new Date(); if (payId.equalsIgnoreCase("ALL"))
			 * { try { logger.info(
			 * "Inside createNewSufDetail(), SUFDetailDao; inserting new SUF Detail for all active merchants"
			 * );
			 * 
			 * List<Merchants> merchantsList = userDao.getMerchantActiveList();
			 * String paymentTypes[] = paymentType.split(","); String mopTypes[]
			 * = mopType.split(","); String slabs[] = slab.split(","); if
			 * (!merchantsList.isEmpty()) { for (Merchants merchantRaw :
			 * merchantsList) { SUFDetail sufDetails = new SUFDetail();
			 * 
			 * sufDetails.setPayId(merchantRaw.getPayId());
			 * sufDetails.setMerchantName(merchantRaw.getBusinessName()); for
			 * (int i = 0; i < paymentTypes.length; i++) { for (int j = 0; j <
			 * mopTypes.length; j++) { for(int k = 0; k < slabs.length; k++) {
			 * 
			 * 
			 * String response = checkSufConflictBySlab(merchantRaw.getPayId(),
			 * loginUserEmailId, txnType,
			 * PaymentType.getpaymentName(paymentTypes[i]), mopTypes[j],
			 * paymentRegion,slabs[k]);
			 * 
			 * if(response.equalsIgnoreCase("ERROR")) { if(!saveAnySuf) {
			 * saveAnySuf = false; } } else {
			 * 
			 * saveAnySuf = true; deleteSufDetail(merchantRaw.getPayId(),
			 * loginUserEmailId, txnType,
			 * PaymentType.getpaymentName(paymentTypes[i]), mopTypes[j],
			 * paymentRegion,slabs[k]);
			 */ session = HibernateSessionProvider.getSession();
			tx = session.beginTransaction();
			sufDetails.setPayId(payId);
			sufDetails.setMerchantName(merchantName);
			sufDetails.setCreateDate(createdDate);
			sufDetails.setRequestedBy(loginUserEmailId);
			sufDetails.setStatus("ACTIVE");
			sufDetails.setFixedCharge(charges);
			sufDetails.setTxnType(txnType);
			sufDetails.setPaymentType(/* PaymentType.getpaymentName( */paymentType/* ) */);
			sufDetails.setMopType(mopType);
			sufDetails.setPercentageAmount(percentageAmount);
			sufDetails.setPaymentRegion(paymentRegion);
			sufDetails.setSlab(slab);
			if(StringUtils.isNotBlank(subMerchantId)){
				sufDetails.setSubMerchantPayId(subMerchantId);
				sufDetails.setSubMerchantName(userDao.getBusinessNameByPayId(subMerchantId));
			}

			session.save(sufDetails);
			tx.commit();
			/*
			 * } } } } } } } catch (ObjectNotFoundException objectNotFound) {
			 * handleException(objectNotFound, tx);
			 * logger.error("Exception caught, " + objectNotFound); return
			 * "ERROR"; } catch (HibernateException hibernateException) {
			 * handleException(hibernateException, tx);
			 * logger.error("Exception caught, " + hibernateException); return
			 * "ERROR"; } finally { autoClose(session); logger.
			 * info("Hibernate session for inserting new SUF Details data is closed"
			 * ); } return "SUCCESS"; } else { logger.
			 * info("Inside createNewSufDetail(), SUFDetailDao; inserting new SUF Details for PayId : "
			 * + payId); try { SUFDetail sufDetails = new SUFDetail(); String
			 * paymentTypes[] = paymentType.split(","); String mopTypes[] =
			 * mopType.split(","); String merchant[] = merchantName.split(",");
			 * String merchantId[] = payId.split(","); String slabs[] =
			 * slab.split(",");
			 * 
			 * for (int i = 0; i < paymentTypes.length; i++) { for (int j = 0; j
			 * < mopTypes.length; j++) { for(int k = 0; k < slabs.length; k++) {
			 * for (int l = 0; l < merchantId.length; l++) {
			 * 
			 * String response = checkSufConflictBySlab(merchantId[l],
			 * loginUserEmailId, txnType,
			 * PaymentType.getpaymentName(paymentTypes[i]), mopTypes[j],
			 * paymentRegion,slabs[k]);
			 * 
			 * if(response.equalsIgnoreCase("ERROR")) { if(!saveAnySuf) {
			 * saveAnySuf = false; } } else {
			 * 
			 * saveAnySuf = true; deleteSufDetail(merchantId[l],
			 * loginUserEmailId, txnType,
			 * PaymentType.getpaymentName(paymentTypes[i]), mopTypes[j],
			 * paymentRegion, slabs[k]);
			 * 
			 * session = HibernateSessionProvider.getSession(); tx =
			 * session.beginTransaction(); sufDetails.setPayId(merchantId[l]);
			 * sufDetails.setMerchantName(merchant[l]);
			 * sufDetails.setPaymentType(PaymentType.getpaymentName(paymentTypes
			 * [i])); sufDetails.setMopType(mopTypes[j]);
			 * sufDetails.setPercentageAmount(percentageAmount);
			 * sufDetails.setCreateDate(createdDate);
			 * sufDetails.setRequestedBy(loginUserEmailId);
			 * sufDetails.setStatus("ACTIVE");
			 * sufDetails.setFixedCharge(charges);
			 * sufDetails.setTxnType(txnType);
			 * sufDetails.setPaymentRegion(paymentRegion);
			 * sufDetails.setSlab(slabs[k]);
			 * 
			 * session.save(sufDetails); tx.commit();
			 * 
			 * } } } } }
			 */
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
			return "ERROR";
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
			return "ERROR";
		} finally {
			autoClose(session);
		}
		/* if(saveAnySuf) { */
		return "SUCCESS";
		/*
		 * } else { return "ERROR"; }
		 */
		// }
	}

	private String checkSufConflictBySlab(String payId, String loginUserEmailId, String txnType, String paymentType,
			String mopType, String paymentRegion, String slab, String subMerchantId) {

		String slabSplit[] = slab.split("-");
		Double minValue = Double.parseDouble(slabSplit[0]);
		Double maxValue = Double.parseDouble(slabSplit[1]);

		if (payId.equalsIgnoreCase("ALL")) {

			logger.info("Check active SUF Detail with slab");
			List<SUFDetail> merchantsList = getMerchantActiveList();

			if (!merchantsList.isEmpty()) {
				for (SUFDetail merchantRaw : merchantsList) {

					List<SUFDetail> sufDetailFromDb = getSUFDetailForSlabCheck(merchantRaw.getPayId(), paymentType,
							merchantRaw.getTxnType(), mopType, paymentRegion, subMerchantId);

					if (sufDetailFromDb != null) {
						for (SUFDetail sufDetails : sufDetailFromDb) {
							String slabArr[] = sufDetails.getSlab().split("-");
							Double minSlab = Double.parseDouble(slabArr[0]);
							Double maxSlab = Double.parseDouble(slabArr[1]);

							if (minValue > minSlab && minValue > maxSlab && maxValue > minSlab && maxValue > maxSlab) {
							} else {
								return "Duplicate";
							}
						}
					}
					/*
					 * if (sufDetailFromDb != null) { Session session =
					 * HibernateSessionProvider.getSession(); Transaction tx =
					 * session.beginTransaction(); try { SUFDetail sufDetails =
					 * session.load(SUFDetail.class, sufDetailFromDb.getId());
					 * sufDetails.setStatus("INACTIVE");
					 * sufDetails.setUpdateDate(updatedDate);
					 * sufDetails.setRequestedBy(loginUserEmailId); tx.commit();
					 * } catch (ObjectNotFoundException objectNotFound) {
					 * handleException(objectNotFound, tx); return "ERROR"; }
					 * catch (HibernateException hibernateException) {
					 * handleException(hibernateException, tx); return "ERROR";
					 * } finally { autoClose(session); } return "SUCCESS"; }
					 */
				}
			}
		} else {
			logger.info("Check active SUF Detail with slab for PayId : " + payId);

			List<SUFDetail> sufDetailFromDb = getSUFDetailForSlabCheck(payId, paymentType, txnType, mopType,
					paymentRegion, subMerchantId);

			if (sufDetailFromDb != null) {
				for (SUFDetail sufDetails : sufDetailFromDb) {
					String slabArr[] = sufDetails.getSlab().split("-");
					Double minSlab = Double.parseDouble(slabArr[0]);
					Double maxSlab = Double.parseDouble(slabArr[1]);
					if (minValue > minSlab && minValue > maxSlab && maxValue > minSlab && maxValue > maxSlab) {
					} else {
						return "Duplicate";
					}
				}
			}

			/*
			 * if (getSUFDetail(payId, paymentType, txnType, mopType,
			 * paymentRegion, slab) != null) { SUFDetail sufDetailsFromDb =
			 * getSUFDetail(payId, paymentType, txnType, mopType, paymentRegion,
			 * slab); if (sufDetailsFromDb != null) { Session session =
			 * HibernateSessionProvider.getSession(); Transaction tx =
			 * session.beginTransaction(); try { SUFDetail sufDetails =
			 * session.load(SUFDetail.class, sufDetailsFromDb.getId());
			 * sufDetails.setStatus("INACTIVE");
			 * sufDetails.setUpdateDate(updatedDate);
			 * sufDetails.setRequestedBy(loginUserEmailId); tx.commit(); } catch
			 * (ObjectNotFoundException objectNotFound) {
			 * handleException(objectNotFound, tx); return "ERROR"; } catch
			 * (HibernateException hibernateException) {
			 * handleException(hibernateException, tx); return "ERROR"; }
			 * finally { autoClose(session); } } }
			 */
		}
		return "SUCCESS";

	}

	public String deleteSufDetail(String payId, String loginUserEmailId, String txnType, String paymentType,
			String mopType, String paymentRegion, String slab, String subMerchantId) {
		Date updatedDate = new Date();

		if (payId.equalsIgnoreCase("ALL")) {

			logger.info("Deleting all active SUF Detail");
			List<SUFDetail> merchantsList = getMerchantActiveList();
			if (!merchantsList.isEmpty()) {
				for (SUFDetail merchantRaw : merchantsList) {
					SUFDetail sufDetailFromDb = getSUFDetail(merchantRaw.getPayId(), paymentType,
							merchantRaw.getTxnType(), mopType, paymentRegion, slab, subMerchantId);
					if (sufDetailFromDb != null) {
						Session session = HibernateSessionProvider.getSession();
						Transaction tx = session.beginTransaction();
						try {
							SUFDetail sufDetails = session.load(SUFDetail.class, sufDetailFromDb.getId());
							sufDetails.setStatus("INACTIVE");
							sufDetails.setUpdateDate(updatedDate);
							sufDetails.setRequestedBy(loginUserEmailId);
							tx.commit();
						} catch (ObjectNotFoundException objectNotFound) {
							handleException(objectNotFound, tx);
							return "ERROR";
						} catch (HibernateException hibernateException) {
							handleException(hibernateException, tx);
							return "ERROR";
						} finally {
							autoClose(session);
						}
						return "SUCCESS";
					}
				}
			}
		} else {
			logger.info("Deleting SUF Detail for PayId : " + payId);

			if (getSUFDetail(payId, paymentType, txnType, mopType, paymentRegion, slab, subMerchantId) != null) {

				SUFDetail sufDetailsFromDb = getSUFDetail(payId, paymentType, txnType, mopType, paymentRegion, slab, subMerchantId);

				if (sufDetailsFromDb != null) {
					Session session = HibernateSessionProvider.getSession();
					Transaction tx = session.beginTransaction();
					try {
						SUFDetail sufDetails = session.load(SUFDetail.class, sufDetailsFromDb.getId());
						sufDetails.setStatus("INACTIVE");
						sufDetails.setUpdateDate(updatedDate);
						sufDetails.setRequestedBy(loginUserEmailId);
						tx.commit();
					} catch (ObjectNotFoundException objectNotFound) {
						handleException(objectNotFound, tx);
						return "ERROR";
					} catch (HibernateException hibernateException) {
						handleException(hibernateException, tx);
						return "ERROR";
					} finally {
						autoClose(session);
					}
				}
			}
		}
		return "SUCCESS";
	}

	public String updateSUFDetailPerPayId(String payId, String merchantName, String txnType, String paymentType,
			String charges, String mopType, String percentageAmount, String loginUserEmailId, String paymentRegion,
			String slab, String subMerchantId) {

		String slabArr[] = slab.split("-");
		SUFDetail SUFDetailFromDb = getSUFDetail(payId,
				/* PaymentType.getpaymentName( */paymentType/* ) */, txnType, mopType, paymentRegion, slab, subMerchantId);
		String response = null;
		Date updatedDate = new Date();
		logger.info("Fetching all active SUF Detail");
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		try {
			
			SUFDetailFromDb.setStatus("INACTIVE");
			SUFDetailFromDb.setUpdateDate(updatedDate);
			SUFDetailFromDb.setRequestedBy(loginUserEmailId);
			session.saveOrUpdate(SUFDetailFromDb);
			
			response = updateNewSufDetail(payId, merchantName, txnType, paymentType, charges, mopType, percentageAmount,
					loginUserEmailId, paymentRegion, slab, subMerchantId);
			tx.commit();
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
			return response;
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
			return response;
		} finally {
			autoClose(session);
		}
		return response;
	}

	@SuppressWarnings("unchecked")
	protected List<SUFDetail> getMerchantActiveList() {
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();

		List<SUFDetail> merchantsList = new ArrayList<SUFDetail>();
		try {
			List<Object[]> merchantListRaw = session
					.createQuery("Select payId, paymentType, txnType, paymentRegion from SUFDetail U where U.status='"
							+ UserStatusType.ACTIVE + "' order by merchantName")
					.getResultList();

			for (Object[] objects : merchantListRaw) {
				SUFDetail merchant = new SUFDetail();
				merchant.setPayId((String) objects[0]);
				merchant.setPaymentType((String) objects[1]);
				merchant.setTxnType((String) objects[2]);

				merchantsList.add(merchant);
			}
			tx.commit();

			return merchantsList;
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
		}
		return merchantsList;
	}

	@SuppressWarnings("unchecked")
	public List<SUFDetail> fetchDataByTxnTypeAndPaymentType(String txnType, String paymentType, String paymentRegion) {
		logger.info("Fetching all active SUF Detail");
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		List<SUFDetail> sufDetailList = new ArrayList<SUFDetail>();
		try {
			/*
			 * sufDetailList = session.
			 * createQuery("select distinct payId from SUFDetail SUF where SUF.status = 'ACTIVE'"
			 * )
			 */
			sufDetailList = session.createQuery("from SUFDetail SUF where SUF.txnType='" + txnType + "'"
					+ " and SUF.paymentType='" + paymentType + "' and SUF.paymentRegion='" + paymentRegion
					+ "' and  SUF.status = 'ACTIVE' order by id desc").getResultList();
			tx.commit();
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
		}
		session.close();
		return sufDetailList;
	}

	@SuppressWarnings("unchecked")
	public List<SUFDetail> fetchSaleForPayId(String payId, String txnType, String mopType) {
		logger.info("Fetching all active SUF Detail");
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		List<SUFDetail> sufDetailList = new ArrayList<SUFDetail>();
		// List<String> sufDetailList = new ArrayList<String>();
		try {
			sufDetailList = session.createQuery("from SUFDetail SUF" + " where SUF.payId ='" + payId
					+ "' and SUF.txnType='" + txnType + "' and SUF.status = 'ACTIVE'").getResultList();
			tx.commit();
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
		}
		session.close();
		return sufDetailList;
	}

	@SuppressWarnings("unchecked")
	public List<SUFDetail> fetchSufChargeByPayId(String payId) {
		logger.info("Fetching all active SUF Detail");
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		List<SUFDetail> sufDetailList = new ArrayList<SUFDetail>();
		// List<String> sufDetailList = new ArrayList<String>();
		try {
			sufDetailList = session
					.createQuery("from SUFDetail SUF" + " where SUF.payId ='" + payId + "' and SUF.status = 'ACTIVE'")
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
		return sufDetailList;
	}

	@SuppressWarnings("unchecked")
	public List<SUFDetail> fetchDataPerPayId(String payId, String txnType, String paymentType, String paymentRegion, String subMerchantId) {
		logger.info("Fetching all active SUF Detail by payId");
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		List<SUFDetail> sufDetailList = new ArrayList<SUFDetail>();
		try {
			StringBuilder query = new StringBuilder("from SUFDetail where payId='" + payId + "' and txnType='" + txnType
					+ "' and paymentRegion = '" + paymentRegion + "' and paymentType='" + paymentType+"'");
			if(StringUtils.isNotBlank(subMerchantId) && !subMerchantId.equalsIgnoreCase("ALL")){
				query.append(" and subMerchantPayId = '"+subMerchantId+"'");
			}
			query.append(" and status='ACTIVE' order by id desc");
			sufDetailList = session.createQuery(query.toString()).getResultList();
			tx.commit();
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
		}
		session.close();
		return sufDetailList;
	}

	@SuppressWarnings("unchecked")
	public List<SUFDetail> getAllActiveSufDetails() {

		logger.info("Fetching all active SUF Detail");
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		List<SUFDetail> sufDetailList = new ArrayList<SUFDetail>();
		try {
			sufDetailList = session.createQuery("from SUFDetail where status='ACTIVE' order by merchantName ASC")
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

		return sufDetailList;
	}
}