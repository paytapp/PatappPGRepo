package com.paymentgateway.commons.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.paymentgateway.commons.user.Account;
import com.paymentgateway.commons.user.ChargingDetails;
import com.paymentgateway.commons.user.ChargingDetailsDao;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.util.AcquirerType;
import com.paymentgateway.commons.util.Currency;
import com.paymentgateway.commons.util.MopType;
import com.paymentgateway.commons.util.PaymentType;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.TDRStatus;

/**
 * @author Puneet, Amitosh
 *
 */

@Component
public class ChargingDetailsFactory {
	private static Logger logger = LoggerFactory.getLogger(ChargingDetailsFactory.class.getName());

	@Autowired
	private UserDao userDao;

	public Map<String, Object> getChargingDetailsMap(String merchantEmailId, String acquirerCode, String paymentsRegion,
			String acquiringMode, String cardHolderType, String paymentType) {

		Session session = null;
		Map<String, Object> chargingMap = new HashMap<>();
		chargingMap.put("regionType", paymentsRegion);

		try {
			User merchant = userDao.find(merchantEmailId);
			Account account = null;
			Set<Account> accounts = merchant.getAccounts();
			if (accounts == null || accounts.size() == 0) {
				logger.info("No account found for Pay ID = " + merchant.getPayId());
			} else {
				for (Account accountThis : accounts) {
					if (accountThis.getAcquirerName()
							.equalsIgnoreCase(AcquirerType.getInstancefromCode(acquirerCode).getName())) {
						account = accountThis;
						break;
					}
				}
			}
			// Get the charging details

			if (null != account) {
				session = HibernateSessionProvider.getSession();
				Transaction tx = session.beginTransaction();
				session.load(account, account.getId());
				Set<ChargingDetails> data = account.getChargingDetails();
				// logger.info("inside ChargingDetailsFactory data = " + data);
				List<ChargingDetails> chargingDetailsListRaw = new ArrayList<ChargingDetails>();

				PaymentType paymentName = PaymentType.getInstanceIgnoreCase(paymentType);

				for (ChargingDetails cDetail : data) {
					for (MopType moptype : MopType.values()) {
						if (!((cDetail.getPaymentsRegion() == null) || (cDetail.getAcquiringMode() == null)
								|| cDetail.getCardHolderType() == null)) {

							if (cDetail.getStatus().equals(TDRStatus.ACTIVE)
									&& cDetail.getPaymentType().toString().equals(paymentName.toString())
									&& cDetail.getPaymentsRegion().toString().equals(paymentsRegion)
									&& cDetail.getAcquiringMode().toString().equals(acquiringMode)
									&& cDetail.getMopType().toString().equals(moptype.toString())
									&& cDetail.getCardHolderType().toString().equals(cardHolderType)) {
								cDetail.setMerchantServiceTax(
										Double.valueOf(PropertiesManager.propertiesMap.get("SERVICE_TAX")));
								chargingDetailsListRaw.add(cDetail);
							}
						}
					}
				}

				// Group Charging Details by MopType
				Map<MopType, List<ChargingDetails>> chargingDetailsByMop = new HashMap<MopType, List<ChargingDetails>>();

				if (chargingDetailsListRaw.size() != 0) {

					for (ChargingDetails cd : chargingDetailsListRaw) {

						List<ChargingDetails> cdList = new ArrayList<ChargingDetails>();

						if (chargingDetailsByMop.get(cd.getMopType()) != null
								&& chargingDetailsByMop.get(cd.getMopType()).size() > 0) {

							cdList = chargingDetailsByMop.get(cd.getMopType());
							cdList.add(cd);
							chargingDetailsByMop.put(cd.getMopType(), cdList);
						} else {
							cdList.add(cd);
							chargingDetailsByMop.put(cd.getMopType(), cdList);
						}
					}

					List<Object> chargingDetailsList = new ArrayList<Object>();
					// Prepare charging Details Map by group with 3 slabs
					for (Entry<MopType, List<ChargingDetails>> entry : chargingDetailsByMop.entrySet()) {
						List<ChargingDetails> cdList = entry.getValue();
						Map<String, Object> tdrFcDetailMap = new TreeMap<String, Object>();
						Map<String, Object> limitDetailMap = new TreeMap<String, Object>();
						for (ChargingDetails chargingDetails : cdList) {
							List<String> tdrFcDetailSlab = new ArrayList<String>();
							//tdrFcDetailSlab.add(String.valueOf(chargingDetails.getPgTDR()));
							//tdrFcDetailSlab.add(String.valueOf(chargingDetails.getPgFixCharge()));
							tdrFcDetailSlab.add(String.valueOf(chargingDetails.getMerchantTDR()));
							tdrFcDetailSlab.add(String.valueOf(chargingDetails.getMerchantFixCharge()));
							tdrFcDetailSlab.add(String.valueOf(chargingDetails.getBankTDR()));
							tdrFcDetailSlab.add(String.valueOf(chargingDetails.getBankFixCharge()));
							tdrFcDetailSlab.add(String.valueOf(chargingDetails.getResellerTDR()));
							tdrFcDetailSlab.add(String.valueOf(chargingDetails.getResellerFixCharge()));
							

							if (chargingDetails.getSlabId().equalsIgnoreCase("01")) {
								tdrFcDetailMap.put("slab1", tdrFcDetailSlab);
							} else if (chargingDetails.getSlabId().equalsIgnoreCase("02")) {
								tdrFcDetailMap.put("slab2", tdrFcDetailSlab);
							} else if (chargingDetails.getSlabId().equalsIgnoreCase("03")) {
								tdrFcDetailMap.put("slab3", tdrFcDetailSlab);
							}

							List<String> limitDetailSlab = new ArrayList<String>();
							limitDetailSlab.add(String.valueOf(chargingDetails.getMinTxnAmount()));
							limitDetailSlab.add(String.valueOf(chargingDetails.getMaxTxnAmount()));

							if (chargingDetails.getSlabId().equalsIgnoreCase("01")) {
								limitDetailMap.put("limitDetailSlab1", limitDetailSlab);
							} else if (chargingDetails.getSlabId().equalsIgnoreCase("02")) {
								limitDetailMap.put("limitDetailSlab2", limitDetailSlab);
							} else if (chargingDetails.getSlabId().equalsIgnoreCase("03")) {
								limitDetailMap.put("limitDetailSlab3", limitDetailSlab);
							}
						}

						if (cdList.size() > 0) {
							ChargingDetails chargingDetails = cdList.get(0);
							Map<String, Object> chargingDetailsMapRaw = new LinkedHashMap<String, Object>();
							chargingDetailsMapRaw.put("currency",
									Currency.getAlphabaticCode(chargingDetails.getCurrency()));
							chargingDetailsMapRaw.put("mopType", chargingDetails.getMopType().getName());
							chargingDetailsMapRaw.put("transactionType", chargingDetails.getTransactionType().name());
							chargingDetailsMapRaw.put("tdrFcDetail", tdrFcDetailMap);
							chargingDetailsMapRaw.put("merchantGST",
									String.valueOf(chargingDetails.getMerchantServiceTax()));
							chargingDetailsMapRaw.put("limitDetail", limitDetailMap);
							chargingDetailsMapRaw.put("allowFC", chargingDetails.isAllowFixCharge());
							chargingDetailsMapRaw.put("chargesFlag", chargingDetails.isChargesFlag());
							if ((cdList.get(0).getMaxChargeMerchant()) == (cdList.get(1).getMaxChargeMerchant())
									&& (cdList.get(0).getMaxChargeMerchant()) == (cdList.get(2)
											.getMaxChargeMerchant())) {
								chargingDetailsMapRaw.put("maxChargeMerchant", chargingDetails.getMaxChargeMerchant());
							} else {
								chargingDetailsMapRaw.put("maxChargeMerchant", "0.0");
							}
							if ((cdList.get(0).getMaxChargeAcquirer()) == (cdList.get(1).getMaxChargeAcquirer())
									&& (cdList.get(0).getMaxChargeMerchant()) == (cdList.get(2)
											.getMaxChargeMerchant())) {
								chargingDetailsMapRaw.put("maxChargeAcquirer", chargingDetails.getMaxChargeAcquirer());
							} else {
								chargingDetailsMapRaw.put("maxChargeAcquirer", "0.0");
							}
							// chargingDetailsMapRaw.put("merchantTDR", chargingDetails.getMerchantTDR());
							chargingDetailsList.add(chargingDetailsMapRaw);
						}
					}
					chargingMap.put(paymentName.getName(), chargingDetailsList);
					tx.commit();
				}
			}
		} finally {
			HibernateSessionProvider.closeSession(session);
		}
		/*
		 * if (chargingDetailsMap.isEmpty()) {
		 * 
		 * chargingDetailsMap.put(paymentType, chargingDetailsList);
		 * 
		 * return createDummyChargingDetails(userDao.getPayIdByEmailId(merchantEmailId),
		 * acquirerCode, paymentsRegion, acquiringMode, cardHolderType,
		 * PaymentType.getInstanceIgnoreCase(paymentType));
		 * 
		 * }
		 */
		return chargingMap;
	}

	public ChargingDetails getSingleChargingDetail(Account account, Long charginDetailId) {
		for (ChargingDetails cDetail : account.getChargingDetails()) {
			if (cDetail.getId().equals(charginDetailId)) {
				return cDetail;
			}
		}
		return null;
	}

	// supply names of payment type and mops and code of acquirer
	public ChargingDetails getChargingDetail(String date, String payId, String acquirer, String paymentType,
			String mopType, String txnType, String currencyCode) {
		ChargingDetails detail = null;

		List<ChargingDetails> chargingDetailsList = new ChargingDetailsDao().findDetail(date, payId,
				AcquirerType.getInstancefromCode(acquirer).getName(),
				PaymentType.getInstanceUsingCode(paymentType).toString(), MopType.getmopName(mopType), currencyCode);

		Iterator<ChargingDetails> chargingDetailsItr = chargingDetailsList.iterator();

		while (chargingDetailsItr.hasNext()) {
			if (paymentType.equals(PaymentType.NET_BANKING.getName())
					|| paymentType.equals(PaymentType.WALLET.getName())) {
				detail = chargingDetailsItr.next();
				break;
			} else {
				detail = chargingDetailsItr.next();
				if (detail.getTransactionType().toString().equals(txnType)) {
					break;
				}
			}
		}
		return detail;
	}

	public ChargingDetails getChargingDetailForReport(String date, String payId, String acquirer, String paymentType,
			String mopType, String txnType, String currencyCode) {
		ChargingDetails detail = null;

		List<ChargingDetails> chargingDetailsList = new ChargingDetailsDao().findDetail(date, payId,
				AcquirerType.getInstancefromCode(acquirer).getName(),
				PaymentType.getInstanceUsingCode(paymentType).toString(), MopType.getmop(mopType).toString(),
				currencyCode, txnType);

		Iterator<ChargingDetails> chargingDetailsItr = chargingDetailsList.iterator();

		while (chargingDetailsItr.hasNext()) {
			if (paymentType.equals(PaymentType.NET_BANKING.getName())
					|| paymentType.equals(PaymentType.WALLET.getName())) {
				detail = chargingDetailsItr.next();
				break;
			} else {
				detail = chargingDetailsItr.next();
				if (detail.getTransactionType().toString().equals(txnType)) {
					break;
				}
			}
		}
		return detail;
	}
}
