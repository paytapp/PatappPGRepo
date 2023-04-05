/**
 * DAO for Dynamic merchant surcharge text
 */
package com.paymentgateway.commons.dao;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.HibernateException;
import org.hibernate.ObjectNotFoundException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.paymentgateway.commons.user.AccountCurrencyRegion;
import com.paymentgateway.commons.user.CardHolderType;
import com.paymentgateway.commons.user.MerchantSurchargeText;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.util.MopType;
import com.paymentgateway.commons.util.PaymentType;
import com.paymentgateway.commons.util.PropertiesManager;

/**
 * @author Amitosh Aanand
 *
 */
@Component
public class MerchantSurchargeTextDao extends HibernateAbstractDao {

	@Autowired
	private UserDao userDao;

	public MerchantSurchargeTextDao() {
		super();
	}

	@SuppressWarnings("deprecation")
	public MerchantSurchargeText getMerchantSurchargeText(String payId, String pymentType, String accountCurrencyRegion,
			String cardHolderType, String mopType, String minAmount, String maxAmount) {
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		MerchantSurchargeText merchantSurchargeText = new MerchantSurchargeText();
		try {
			merchantSurchargeText = (MerchantSurchargeText) session
					.createQuery("from MerchantSurchargeText where payId='" + payId + "' and paymentType='" + pymentType
							+ "' and paymentRegion='" + accountCurrencyRegion + "' and cardHolderType='"
							+ cardHolderType + "' and mopType = '" + mopType + "' and minTxnAmount='" + minAmount
							+ "' and maxTxnAmount='" + maxAmount + "' and status='ACTIVE'")
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
		return merchantSurchargeText;
	}

	@SuppressWarnings("unchecked")
	public List<MerchantSurchargeText> fetchSpecificSurchargeText(String querry) {
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		List<MerchantSurchargeText> merchantSurchargeTextList = new ArrayList<MerchantSurchargeText>();
		try {
			merchantSurchargeTextList = session.createQuery(querry).getResultList();
			tx.commit();
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
		}
		session.close();
		return merchantSurchargeTextList;
	}

	public void createEditSurchargeTextPerPayId(String payId, String paymentType, String paymentRegion,
			String cardHolderType, String mopType, String amountSlab, String loginUserEmailId, String surchargeText) {
		List<PaymentType> paymentTypeList = new ArrayList<PaymentType>();
		List<MopType> mopTypeList = new ArrayList<MopType>();
		List<AccountCurrencyRegion> paymentRegionList = new ArrayList<AccountCurrencyRegion>();
		List<CardHolderType> cardHolderTypeList = new ArrayList<CardHolderType>();
		List<String> amountSlabList = new ArrayList<String>();

		if (StringUtils.isNotBlank(payId)) {
			if (StringUtils.isBlank(paymentType) || paymentType.equalsIgnoreCase("ALL")) {
				for (PaymentType payment : PaymentType.values()) {
					paymentTypeList.add(payment);
				}
			} else {
				paymentTypeList.add(PaymentType.getInstanceUsingCode(paymentType));
			}

			if (StringUtils.isBlank(mopType) || mopType.equalsIgnoreCase("ALL")) {
				if (StringUtils.isBlank(paymentType) || paymentType.equalsIgnoreCase("ALL")) {
					for (PaymentType payment : paymentTypeList) {
						//mopTypeList.addAll(userDao.getMerchantMopTypesByPaymentType(payId, payment.getName()));
					}
				} else {
					//mopTypeList.addAll(userDao.getMerchantMopTypesByPaymentType(payId, paymentType));
				}
			} else {
				mopTypeList.add(MopType.getInstance(mopType));
			}

			if (paymentType.equalsIgnoreCase("NB") || paymentType.equalsIgnoreCase("WL")
					|| paymentType.equalsIgnoreCase("UP") || paymentType.equalsIgnoreCase("CD")) {
				paymentRegionList.add(AccountCurrencyRegion.DOMESTIC);
			} else {
				if (StringUtils.isBlank(paymentRegion) || paymentRegion.equalsIgnoreCase("ALL")) {
					paymentRegionList.add(AccountCurrencyRegion.DOMESTIC);
					paymentRegionList.add(AccountCurrencyRegion.INTERNATIONAL);
				} else {
					paymentRegionList.add(AccountCurrencyRegion.valueOf(paymentRegion));
				}
			}

			if (paymentType.equalsIgnoreCase("NB") || paymentType.equalsIgnoreCase("WL")
					|| paymentType.equalsIgnoreCase("UP") || paymentType.equalsIgnoreCase("CD")) {
				cardHolderTypeList.add(CardHolderType.CONSUMER);
			} else {
				if (StringUtils.isBlank(cardHolderType) || cardHolderType.equalsIgnoreCase("ALL")) {
					cardHolderTypeList.add(CardHolderType.CONSUMER);
					cardHolderTypeList.add(CardHolderType.COMMERCIAL);
					cardHolderTypeList.add(CardHolderType.PREMIUM);
				} else {
					cardHolderTypeList.add(CardHolderType.valueOf(cardHolderType));
				}
			}

			if (StringUtils.isBlank(amountSlab) || amountSlab.equalsIgnoreCase("ALL")) {
				amountSlabList.add(PropertiesManager.propertiesMap.get("LimitSlab1MinAmount") + "-"
						+ PropertiesManager.propertiesMap.get("LimitSlab1MaxAmount"));
				amountSlabList.add(PropertiesManager.propertiesMap.get("LimitSlab2MinAmount") + "-"
						+ PropertiesManager.propertiesMap.get("LimitSlab2MaxAmount"));
				amountSlabList.add(PropertiesManager.propertiesMap.get("LimitSlab3MinAmount") + "-"
						+ PropertiesManager.propertiesMap.get("LimitSlab3MaxAmount"));
			} else {
				amountSlabList.add(amountSlab);
			}

			for (PaymentType pType : paymentTypeList) {
				for (AccountCurrencyRegion acRegion : paymentRegionList) {
					for (CardHolderType chType : cardHolderTypeList) {
						for (MopType mType : mopTypeList) {
							for (String amount : amountSlabList) {
								String amountarr[] = amount.split("-");
								MerchantSurchargeText mstfromDB = getMerchantSurchargeText(payId, pType.getName(),
										acRegion.name(), chType.name(), mType.getName(), amountarr[0], amountarr[1]);
								if (mstfromDB == null) {
									MerchantSurchargeText mst = new MerchantSurchargeText();
									Session session = HibernateSessionProvider.getSession();
									Transaction tx = session.beginTransaction();
									mst.setPayId(payId);
									mst.setPaymentType(pType.getName());
									mst.setPaymentRegion(acRegion.name());
									mst.setCardHolderType(chType.name());
									mst.setMopType(mType.getName());
									mst.setMinTxnAmount(amountarr[0]);
									mst.setMaxTxnAmount(amountarr[1]);
									mst.setCreatedBy(loginUserEmailId);
									mst.setSurchargeText(surchargeText);
									mst.setCreatedDate(new Date());
									mst.setStatus("ACTIVE");
									try {
										session.save(mst);
										tx.commit();
									} catch (ObjectNotFoundException objectNotFound) {
										handleException(objectNotFound, tx);
									} catch (HibernateException hibernateException) {
										handleException(hibernateException, tx);
									} finally {
										autoClose(session);
									}
								} else {
									Session session = HibernateSessionProvider.getSession();
									Transaction tx = session.beginTransaction();
									try {
										MerchantSurchargeText mst = new MerchantSurchargeText();
										MerchantSurchargeText merchantSurchargeTextFromDb = session
												.load(MerchantSurchargeText.class, mstfromDB.getId());
										merchantSurchargeTextFromDb.setStatus("INACTIVE");
										merchantSurchargeTextFromDb.setUpdatedDate(new Date());
										merchantSurchargeTextFromDb.setUpdatedBy(loginUserEmailId);
										mst.setPayId(payId);
										mst.setPaymentType(pType.getName());
										mst.setPaymentRegion(acRegion.name());
										mst.setCardHolderType(chType.name());
										mst.setMopType(mType.getName());
										mst.setMinTxnAmount(amountarr[0]);
										mst.setMaxTxnAmount(amountarr[1]);
										mst.setCreatedBy(loginUserEmailId);
										mst.setCreatedDate(new Date());
										mst.setSurchargeText(surchargeText);
										mst.setStatus("ACTIVE");
										session.save(mst);
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
			}
		}
	}

	public void deleteSurchargeText(String payId, String paymentType, String paymentRegion, String cardHolderType,
			String mopType, String amountSlab, String loginUserEmailId) {
		String amountarr[] = amountSlab.split("-");
		MerchantSurchargeText mstfromDB = getMerchantSurchargeText(payId, PaymentType.getpaymentName(paymentType),
				paymentRegion, cardHolderType, mopType, amountarr[0], amountarr[1]);
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		try {
			MerchantSurchargeText merchantSurchargeTextFromDb = session.load(MerchantSurchargeText.class,
					mstfromDB.getId());
			merchantSurchargeTextFromDb.setStatus("INACTIVE");
			merchantSurchargeTextFromDb.setUpdatedDate(new Date());
			merchantSurchargeTextFromDb.setUpdatedBy(loginUserEmailId);
			tx.commit();
		} catch (ObjectNotFoundException objectNotFound) {
			handleException(objectNotFound, tx);
		} catch (HibernateException hibernateException) {
			handleException(hibernateException, tx);
		} finally {
			autoClose(session);
		}
	}

	@SuppressWarnings("unchecked")
	public String getSurchargetTextByPayIdInJSON(String payId) {
		Session session = HibernateSessionProvider.getSession();
		Transaction tx = session.beginTransaction();
		List<MerchantSurchargeText> merchantSurchargeTextList = new ArrayList<MerchantSurchargeText>();
		try {
			merchantSurchargeTextList = session.createQuery(
					"SELECT paymentType, paymentRegion, cardHolderType, mopType, amountSlab from MerchantSurchargeText MST where MST.payId='"
							+ payId + "' and MST.status = 'ACTIVE'")
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
		return new Gson().toJson(merchantSurchargeTextList);
	}
}