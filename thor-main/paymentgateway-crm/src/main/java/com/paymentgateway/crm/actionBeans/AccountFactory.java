package com.paymentgateway.crm.actionBeans;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.dao.RatesDefaultDao;
import com.paymentgateway.commons.dao.RouterConfigurationDao;
import com.paymentgateway.commons.kms.EncryptDecryptService;
import com.paymentgateway.commons.user.Account;
import com.paymentgateway.commons.user.AccountCurrency;
import com.paymentgateway.commons.user.ChargingDetails;
import com.paymentgateway.commons.user.Mop;
import com.paymentgateway.commons.user.MopTransaction;
import com.paymentgateway.commons.user.Payment;
import com.paymentgateway.commons.user.RatesDefault;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserType;
import com.paymentgateway.commons.util.CrmFieldConstants;
import com.paymentgateway.commons.util.MopType;
import com.paymentgateway.commons.util.PaymentType;
import com.paymentgateway.commons.util.TDRStatus;
import com.paymentgateway.commons.util.TransactionType;
import com.paymentgateway.commons.util.onUsOffUs;

/**
 * @author Puneet
 *
 */
@Service
public class AccountFactory {

	@Autowired
	EncryptDecryptService encryptDecryptService;

	@Autowired
	private RouterConfigurationDao routerConfigDao;

	@Autowired
	private RatesDefaultDao ratesDefaultDao;

	private ChargingDetailsMaintainer maintainChargingDetails = new ChargingDetailsMaintainer();
	private Account account = new Account();
	private Set<Payment> savedPaymentSet = new HashSet<Payment>();
	private Payment presentPayment = null;
	private Mop presentMop = null;
	private MopTransaction presentMoptxn = null;

	public AccountFactory() {

	}

	public Account editAccount(Account oldAccount, String mapString, String payId) {
		this.account = oldAccount;
		String acquirerName = oldAccount.getAcquirerName();
		List<String> mappedCurrency = getCurrencyList();
		savedPaymentSet = account.getPayments();
		if (mapString.equals("")) {
			String[] dummytokens = { "" };
			removeMapping(dummytokens, acquirerName, payId, account.getId(), routerConfigDao);
			return account;
		}
		String[] tokens = mapString.split(",");
		// remove old mappings
		removeMapping(tokens, acquirerName, payId, account.getId(), routerConfigDao);
		// new mappings added
		addMapping(tokens, acquirerName, payId, mappedCurrency, routerConfigDao);
		return account;
	}

	public void removeMapping(String[] tokens, String acquirer, String payId, Long accountId,
			RouterConfigurationDao routerConfigDao) {
		Set<Payment> paymentSet = account.getPayments();
		String[] savedTokens = account.getMappedString().split(CrmFieldConstants.COMMA.getValue());

		for (String savedToken : savedTokens) {
			boolean isPresent = false;
			for (String token : tokens) {
				if (token.equals(savedToken)) {
					isPresent = true;
				}
			}
			// disable charging detail
			if (!isPresent) {
				removeToken(paymentSet, savedToken);
				account.disableChargingDetail(savedToken, routerConfigDao);
			}
		}
	}

	public void removeToken(Set<Payment> paymentSet, String savedToken) {
		String[] splittedToken = savedToken.split("-");

		if (checkPaymentType(paymentSet, savedToken)) {
			if (checkMopType(savedToken)) {
				if (splittedToken.length == 3) {
					if (checkTxnType(savedToken)) {
						presentMop.removeTransactionType(presentMoptxn);
						if (presentMop.getMopTransactionTypes().isEmpty()) {
							presentPayment.removeMop(presentMop);
						}
					}
				} else {
					// IN case of WL and NB
					presentPayment.removeMop(presentMop);
				}
			}
			if (presentPayment.getMops().isEmpty()) {
				account.removePayment(presentPayment);
			}
		}
	}

	public void addMapping(String[] tokens, String acquirerName, String payId, List<String> mappedCurrency,
			RouterConfigurationDao routerConfigDao) {
		for (String token : tokens) {
			String[] splitedToken = token.split("-");
			if (!checkPaymentType(savedPaymentSet, token)) {

				Payment payment = new Payment();
				payment.setPaymentType(PaymentType.getInstance(splitedToken[0]));

				Mop newMop = new Mop();
				newMop.setMopType(MopType.getmop(splitedToken[1]));

				payment.addMop(newMop);

				if (splitedToken.length == 3) {
					MopTransaction newMopTxn = new MopTransaction();
					newMopTxn.setTransactionType(TransactionType.getInstanceFromCode(splitedToken[2]));
					newMop.addMopTransaction(newMopTxn);
				}
				account.addPayment(payment);
				addChargingDetail(acquirerName, payId, token, mappedCurrency, routerConfigDao);
				continue;
			}
			// if mop not present
			if (!checkMopType(token)) {

				Mop newMop = new Mop();
				if (splitedToken.length == 3) {
					MopTransaction newMopTxn = new MopTransaction();
					newMopTxn.setTransactionType(TransactionType.getInstanceFromCode(splitedToken[2]));
					newMop.addMopTransaction(newMopTxn);
				}

				newMop.setMopType(MopType.getmop(splitedToken[1]));
				presentPayment.addMop(newMop);
				addChargingDetail(acquirerName, payId, token, mappedCurrency, routerConfigDao);
				continue;
			}

			if (!checkTxnType(token)) { // if txntype not present
				if (splitedToken.length == 2) {
					continue;
				}
				MopTransaction newMopTxn = new MopTransaction();
				newMopTxn.setTransactionType(TransactionType.getInstanceFromCode(splitedToken[2]));
				presentMop.addMopTransaction(newMopTxn);
				addChargingDetail(acquirerName, payId, token, mappedCurrency, routerConfigDao);
			}
		}
	}

	public void addChargingDetail(String acquirerName, String payId, String token, List<String> selectedCurrency,
			RouterConfigurationDao routerConfigDao) {
		for (String currencyCode : selectedCurrency) {

			List<ChargingDetails> cdList = new ArrayList<ChargingDetails>();
			cdList = maintainChargingDetails.createChargingDetail(acquirerName, payId, token, currencyCode);

			for (ChargingDetails cd : cdList) {

				RatesDefault acqDefRate = ratesDefaultDao.findAcqDefRatesByChargingDetails(cd, UserType.ACQUIRER);
				RatesDefault merchantDefRate = ratesDefaultDao.findMerchantDefRatesByChargingDetails(cd,
						UserType.MERCHANT);

				if (acqDefRate != null && merchantDefRate != null) {

					if ((Double.valueOf(merchantDefRate.getMerchantTdr().toString())
							- Double.valueOf(acqDefRate.getAcqTdr().toString()) >= 0)
							&& (Double.valueOf(merchantDefRate.getMerchantSuf().toString())
									- Double.valueOf(acqDefRate.getAcqSuf().toString()) >= 0)) {

						cd.setBankTDR(Double.valueOf(acqDefRate.getAcqTdr().toString()));
						cd.setBankFixCharge(
								Double.valueOf(acqDefRate.getAcqSuf().toString()));
						cd.setMerchantTDR(
								Double.valueOf(merchantDefRate.getMerchantTdr().toString()));
						cd.setMerchantFixCharge(
								Double.valueOf(merchantDefRate.getMerchantSuf().toString()));
						
						Double pgTdr = new BigDecimal(cd.getMerchantTDR() - cd.getBankTDR()).setScale(2,BigDecimal.ROUND_HALF_DOWN).doubleValue();
						Double pgFc =  new BigDecimal(cd.getMerchantFixCharge()
								- cd.getBankFixCharge()).setScale(2,BigDecimal.ROUND_HALF_DOWN).doubleValue();
						
						cd.setPgTDR(pgTdr);
						cd.setPgFixCharge(pgFc);

					}

				}

				account.addChargingDetail(cd);

				// Don't Create Router Configuration for On US txn when mapping is done
				if (cd.getAcquiringMode().equals(onUsOffUs.ON_US)) {
					routerConfigDao.updateRouterByCD(cd, "created", false);
				} else {

				}
				routerConfigDao.updateRouterByCD(cd, "created", true);

			}
			/*
			 * account.addChargingDetail(
			 * maintainChargingDetails.createChargingDetail(acquirerName, payId, token,
			 * currencyCode));
			 */
		}
	}

	public boolean checkPaymentType(Set<Payment> paymentSet, String token) {
		boolean isPresent = false;

		Iterator<Payment> paymentItr = paymentSet.iterator();
		String[] splittedToken = token.split("-");
		while (paymentItr.hasNext()) {
			Payment currentPayment = paymentItr.next();
			if (currentPayment.getPaymentType().getName().equals(splittedToken[0])) {
				isPresent = true;
				this.presentPayment = currentPayment;
			}
		}
		return isPresent;
	}

	public boolean checkMopType(String token) {
		boolean isPresent = false;
		String[] splittedToken = token.split("-");
		Set<Mop> presentMopSet = presentPayment.getMops();
		Iterator<Mop> mopItr = presentMopSet.iterator();

		while (mopItr.hasNext()) {
			Mop currentMop = mopItr.next();
			if (currentMop.getMopType().getCode().equals(splittedToken[1])) {
				isPresent = true;
				this.presentMop = currentMop;
			}
		}
		return isPresent;
	}

	public boolean checkTxnType(String token) {
		boolean isPresent = false;
		String[] splittedToken = token.split("-");

		Set<MopTransaction> presentMopTxnSet = presentMop.getMopTransactionTypes();
		Iterator<MopTransaction> mopTxnItr = presentMopTxnSet.iterator();

		while (mopTxnItr.hasNext()) {
			MopTransaction currentMopTxn = mopTxnItr.next();
			if (currentMopTxn.getTransactionType().getCode().equals(splittedToken[2])) {
				isPresent = true;
				this.presentMoptxn = currentMopTxn;
			}
		}
		return isPresent;
	}

	public Account addAccountCurrency(Account account, AccountCurrency[] selectedAccountCurrency, User acquirer,
			String payId) {
		if (null == selectedAccountCurrency || selectedAccountCurrency.length == 0) {
			return account;
		}
		Set<AccountCurrency> accountCurrencySet = account.getAccountCurrencySet();
		for (AccountCurrency accountCurrencyFE : selectedAccountCurrency) {
			boolean flag = false;
			Iterator<AccountCurrency> accountCurrencySetItrator = account.getAccountCurrencySet().iterator();
			while (accountCurrencySetItrator.hasNext()) {
				AccountCurrency accountCurrency = accountCurrencySetItrator.next();
				if (accountCurrency.getCurrencyCode().equals(accountCurrencyFE.getCurrencyCode())) {
					flag = true;
					// edit password and other details
					accountCurrency.setMerchantId(accountCurrencyFE.getMerchantId());
					accountCurrency.setPassword(encryptDecryptService.encrypt(payId,accountCurrencyFE.getPassword()));
					accountCurrency.setTxnKey(accountCurrencyFE.getTxnKey());
					accountCurrency.setAdf1(accountCurrencyFE.getAdf1());
					accountCurrency.setAdf2(accountCurrencyFE.getAdf2());
					accountCurrency.setAdf3(accountCurrencyFE.getAdf3());
					accountCurrency.setAdf4(accountCurrencyFE.getAdf4());
					accountCurrency.setAdf5(accountCurrencyFE.getAdf5());
					accountCurrency.setAdf8(accountCurrencyFE.getAdf8());
					accountCurrency.setAdf9(accountCurrencyFE.getAdf9());
					accountCurrency.setAdf10(accountCurrencyFE.getAdf10());
					accountCurrency.setAdf11(accountCurrencyFE.getAdf11());

					/*
					 * if (accountCurrencyFE.getAdf1() != null
					 * &&!accountCurrencyFE.getAdf1().trim().isEmpty()) {
					 * accountCurrency.setAdf1(encryptDecryptService.encrypt(accountCurrencyFE.
					 * getAdf1())); }
					 * 
					 * if (accountCurrencyFE.getAdf2() != null
					 * &&!accountCurrencyFE.getAdf2().trim().isEmpty()) {
					 * accountCurrency.setAdf2(encryptDecryptService.encrypt(accountCurrencyFE.
					 * getAdf2())); }
					 * 
					 * if (accountCurrencyFE.getAdf3() != null
					 * &&!accountCurrencyFE.getAdf3().trim().isEmpty()) {
					 * accountCurrency.setAdf3(encryptDecryptService.encrypt(accountCurrencyFE.
					 * getAdf3())); }
					 * 
					 * if (accountCurrencyFE.getAdf4() != null
					 * &&!accountCurrencyFE.getAdf4().trim().isEmpty()) {
					 * accountCurrency.setAdf4(encryptDecryptService.encrypt(accountCurrencyFE.
					 * getAdf4())); }
					 * 
					 * if (accountCurrencyFE.getAdf5() != null
					 * &&!accountCurrencyFE.getAdf5().trim().isEmpty()) {
					 * accountCurrency.setAdf5(encryptDecryptService.encrypt(accountCurrencyFE.
					 * getAdf5())); }
					 */

					if (accountCurrencyFE.getAdf6() != null && !accountCurrencyFE.getAdf6().trim().isEmpty()) {
						accountCurrency.setAdf6(encryptDecryptService.encrypt(payId,accountCurrencyFE.getAdf6()));
					}

					if (accountCurrencyFE.getAdf7() != null && !accountCurrencyFE.getAdf7().trim().isEmpty()) {
						accountCurrency.setAdf7(encryptDecryptService.encrypt(payId,accountCurrencyFE.getAdf7()));
					}

					/*
					 * if (accountCurrencyFE.getAdf8() != null
					 * &&!accountCurrencyFE.getAdf8().trim().isEmpty()) {
					 * accountCurrency.setAdf8(encryptDecryptService.encrypt(accountCurrencyFE.
					 * getAdf8())); }
					 * 
					 * if (accountCurrencyFE.getAdf9() != null
					 * &&!accountCurrencyFE.getAdf9().trim().isEmpty()) {
					 * accountCurrency.setAdf9(encryptDecryptService.encrypt(accountCurrencyFE.
					 * getAdf9())); }
					 * 
					 * if (accountCurrencyFE.getAdf10() != null
					 * &&!accountCurrencyFE.getAdf10().trim().isEmpty()) {
					 * accountCurrency.setAdf10(encryptDecryptService.encrypt(accountCurrencyFE.
					 * getAdf10())); }
					 * 
					 * if (accountCurrencyFE.getAdf11() != null
					 * &&!accountCurrencyFE.getAdf11().trim().isEmpty()) {
					 * accountCurrency.setAdf11(encryptDecryptService.encrypt(accountCurrencyFE.
					 * getAdf11())); }
					 */

					accountCurrency.setDirectTxn(accountCurrencyFE.isDirectTxn());
				}
			}
			if (!flag) {
				AccountCurrency newAccountCurrency = new AccountCurrency();
				newAccountCurrency.setCurrencyCode(accountCurrencyFE.getCurrencyCode());

				newAccountCurrency.setMerchantId(accountCurrencyFE.getMerchantId());

				if (StringUtils.isNotBlank(accountCurrencyFE.getPassword())) {
					newAccountCurrency.setPassword(encryptDecryptService.encrypt(payId,accountCurrencyFE.getPassword()));
				} else {
					newAccountCurrency.setPassword("");
				}

				newAccountCurrency.setAdf1(accountCurrencyFE.getAdf1());
				newAccountCurrency.setAdf2(accountCurrencyFE.getAdf2());
				newAccountCurrency.setAdf3(accountCurrencyFE.getAdf3());
				newAccountCurrency.setAdf4(accountCurrencyFE.getAdf4());
				newAccountCurrency.setAdf5(accountCurrencyFE.getAdf5());
				newAccountCurrency.setAdf8(accountCurrencyFE.getAdf8());
				newAccountCurrency.setAdf9(accountCurrencyFE.getAdf9());
				newAccountCurrency.setAdf10(accountCurrencyFE.getAdf10());
				newAccountCurrency.setAdf11(accountCurrencyFE.getAdf11());

				/*
				 * if (accountCurrencyFE.getAdf1() != null
				 * &&!accountCurrencyFE.getAdf1().trim().isEmpty()) {
				 * newAccountCurrency.setAdf1(encryptDecryptService.encrypt(accountCurrencyFE
				 * .getAdf1())); }
				 * 
				 * if (accountCurrencyFE.getAdf2() != null
				 * &&!accountCurrencyFE.getAdf2().trim().isEmpty()) {
				 * newAccountCurrency.setAdf2(encryptDecryptService.encrypt(accountCurrencyFE
				 * .getAdf2())); }
				 * 
				 * if (accountCurrencyFE.getAdf3() != null
				 * &&!accountCurrencyFE.getAdf3().trim().isEmpty()) {
				 * newAccountCurrency.setAdf3(encryptDecryptService.encrypt(accountCurrencyFE
				 * .getAdf3())); }
				 * 
				 * if (accountCurrencyFE.getAdf4() != null
				 * &&!accountCurrencyFE.getAdf4().trim().isEmpty()) {
				 * newAccountCurrency.setAdf4(encryptDecryptService.encrypt(accountCurrencyFE
				 * .getAdf4())); }
				 * 
				 * if (accountCurrencyFE.getAdf5() != null
				 * &&!accountCurrencyFE.getAdf5().trim().isEmpty()) {
				 * newAccountCurrency.setAdf5(encryptDecryptService.encrypt(accountCurrencyFE
				 * .getAdf5())); }
				 */

				if (accountCurrencyFE.getAdf6() != null && !accountCurrencyFE.getAdf6().trim().isEmpty()) {
					newAccountCurrency.setAdf6(encryptDecryptService.encrypt(payId,accountCurrencyFE.getAdf6()));
				}

				if (accountCurrencyFE.getAdf7() != null && !accountCurrencyFE.getAdf7().trim().isEmpty()) {
					newAccountCurrency.setAdf7(encryptDecryptService.encrypt(payId,accountCurrencyFE.getAdf7()));
				}

				/*
				 * if (accountCurrencyFE.getAdf8() != null
				 * &&!accountCurrencyFE.getAdf8().trim().isEmpty()) {
				 * newAccountCurrency.setAdf8(encryptDecryptService.encrypt(accountCurrencyFE
				 * .getAdf8())); }
				 * 
				 * if (accountCurrencyFE.getAdf9() != null
				 * &&!accountCurrencyFE.getAdf9().trim().isEmpty()) {
				 * newAccountCurrency.setAdf9(encryptDecryptService.encrypt(accountCurrencyFE
				 * .getAdf9())); }
				 * 
				 * if (accountCurrencyFE.getAdf10() != null
				 * &&!accountCurrencyFE.getAdf10().trim().isEmpty()) {
				 * newAccountCurrency.setAdf10(encryptDecryptService.encrypt(
				 * accountCurrencyFE.getAdf10())); }
				 * 
				 * if (accountCurrencyFE.getAdf11() != null
				 * &&!accountCurrencyFE.getAdf11().trim().isEmpty()) {
				 * newAccountCurrency.setAdf11(encryptDecryptService.encrypt(
				 * accountCurrencyFE.getAdf11())); }
				 */

				newAccountCurrency.setTxnKey(accountCurrencyFE.getTxnKey());
				newAccountCurrency.setDirectTxn(accountCurrencyFE.isDirectTxn());
				newAccountCurrency.setAcqPayId(acquirer.getPayId());
				accountCurrencySet.add(newAccountCurrency);
				// add charging detail
				// get existing charging details and add them to account
				Set<Payment> paymentSet = account.getPayments();

				for (Payment payment : paymentSet) {
					Set<Mop> mops = payment.getMops();
					for (Mop mop : mops) {
						if ((payment.getPaymentType().equals(PaymentType.WALLET))) {
							Set<MopTransaction> mopTxnSet = mop.getMopTransactionTypes();
							for (MopTransaction mopTxn : mopTxnSet) {

								List<ChargingDetails> newChargingDetailsList = new ArrayList<ChargingDetails>();

								newChargingDetailsList = maintainChargingDetails.createChargingDetail(
										payment.getPaymentType(), mop.getMopType(), acquirer.getBusinessName(), payId,
										accountCurrencyFE.getCurrencyCode());

								for (ChargingDetails newChargingDetails : newChargingDetailsList) {

									RatesDefault acqDefRate = ratesDefaultDao
											.findAcqDefRatesByChargingDetails(newChargingDetails, UserType.ACQUIRER);
									RatesDefault merchantDefRate = ratesDefaultDao
											.findMerchantDefRatesByChargingDetails(newChargingDetails,
													UserType.MERCHANT);

									if (acqDefRate != null && merchantDefRate != null) {

										if ((Double.valueOf(merchantDefRate.getMerchantTdr().toString())
												- Double.valueOf(acqDefRate.getAcqTdr().toString()) >= 0)
												&& (Double.valueOf(merchantDefRate.getMerchantSuf().toString())
														- Double.valueOf(acqDefRate.getAcqSuf().toString()) >= 0)) {

											newChargingDetails
													.setBankTDR(Double.valueOf(acqDefRate.getAcqTdr().toString()));
											newChargingDetails.setBankFixCharge(
													Double.valueOf(acqDefRate.getAcqSuf().toString()));
											newChargingDetails.setMerchantTDR(
													Double.valueOf(merchantDefRate.getMerchantTdr().toString()));
											newChargingDetails.setMerchantFixCharge(
													Double.valueOf(merchantDefRate.getMerchantSuf().toString()));
											
											Double pgTdr = new BigDecimal(newChargingDetails.getMerchantTDR() - newChargingDetails.getBankTDR()).setScale(2,BigDecimal.ROUND_HALF_DOWN).doubleValue();
											Double pgFc =  new BigDecimal(newChargingDetails.getMerchantFixCharge()
													- newChargingDetails.getBankFixCharge()).setScale(2,BigDecimal.ROUND_HALF_DOWN).doubleValue();
											
											newChargingDetails.setPgTDR(pgTdr);
											newChargingDetails.setPgFixCharge(pgFc);

										}

									}

									account.addChargingDetail(newChargingDetails);

									if (newChargingDetails.getAcquiringMode().equals(onUsOffUs.ON_US)) {
										routerConfigDao.updateRouterByCD(newChargingDetails, "created", false);
									} else {
										routerConfigDao.updateRouterByCD(newChargingDetails, "created", true);
									}

								}

							}
						} else if (payment.getPaymentType().equals(PaymentType.NET_BANKING)) {

							List<ChargingDetails> newChargingDetailsList = new ArrayList<ChargingDetails>();

							newChargingDetailsList = maintainChargingDetails.createChargingDetail(
									payment.getPaymentType(), mop.getMopType(), acquirer.getBusinessName(), payId,
									accountCurrencyFE.getCurrencyCode());

							for (ChargingDetails newChargingDetails : newChargingDetailsList) {

								RatesDefault acqDefRate = ratesDefaultDao
										.findAcqDefRatesByChargingDetails(newChargingDetails, UserType.ACQUIRER);
								RatesDefault merchantDefRate = ratesDefaultDao
										.findMerchantDefRatesByChargingDetails(newChargingDetails, UserType.MERCHANT);

								if (acqDefRate != null && merchantDefRate != null) {

									if ((Double.valueOf(merchantDefRate.getMerchantTdr().toString())
											- Double.valueOf(acqDefRate.getAcqTdr().toString()) >= 0)
											&& (Double.valueOf(merchantDefRate.getMerchantSuf().toString())
													- Double.valueOf(acqDefRate.getAcqSuf().toString()) >= 0)) {

										newChargingDetails
												.setBankTDR(Double.valueOf(acqDefRate.getAcqTdr().toString()));
										newChargingDetails.setBankFixCharge(
												Double.valueOf(acqDefRate.getAcqSuf().toString()));
										newChargingDetails.setMerchantTDR(
												Double.valueOf(merchantDefRate.getMerchantTdr().toString()));
										newChargingDetails.setMerchantFixCharge(
												Double.valueOf(merchantDefRate.getMerchantSuf().toString()));

										Double pgTdr = new BigDecimal(newChargingDetails.getMerchantTDR() - newChargingDetails.getBankTDR()).setScale(2,BigDecimal.ROUND_HALF_DOWN).doubleValue();
										Double pgFc =  new BigDecimal(newChargingDetails.getMerchantFixCharge()
												- newChargingDetails.getBankFixCharge()).setScale(2,BigDecimal.ROUND_HALF_DOWN).doubleValue();
										
										newChargingDetails.setPgTDR(pgTdr);
										newChargingDetails.setPgFixCharge(pgFc);

									}

								}

								account.addChargingDetail(newChargingDetails);
								if (newChargingDetails.getAcquiringMode().equals(onUsOffUs.ON_US)) {
									routerConfigDao.updateRouterByCD(newChargingDetails, "created", false);
								} else {
									routerConfigDao.updateRouterByCD(newChargingDetails, "created", true);
								}
							}

						}
						// end
						else {
							Set<MopTransaction> mopTxnSet = mop.getMopTransactionTypes();
							for (MopTransaction mopTxn : mopTxnSet) {

								List<ChargingDetails> newChargingDetailsList = new ArrayList<ChargingDetails>();

								newChargingDetailsList = maintainChargingDetails.createChargingDetail(
										payment.getPaymentType(), mop.getMopType(), acquirer.getBusinessName(), payId,
										accountCurrencyFE.getCurrencyCode());

								for (ChargingDetails newChargingDetails : newChargingDetailsList) {

									RatesDefault acqDefRate = ratesDefaultDao
											.findAcqDefRatesByChargingDetails(newChargingDetails, UserType.ACQUIRER);
									RatesDefault merchantDefRate = ratesDefaultDao
											.findMerchantDefRatesByChargingDetails(newChargingDetails,
													UserType.MERCHANT);

									if (acqDefRate != null && merchantDefRate != null) {

										if ((Double.valueOf(merchantDefRate.getMerchantTdr().toString())
												- Double.valueOf(acqDefRate.getAcqTdr().toString()) >= 0)
												&& (Double.valueOf(merchantDefRate.getMerchantSuf().toString())
														- Double.valueOf(acqDefRate.getAcqSuf().toString()) >= 0)) {
											
											newChargingDetails
													.setBankTDR(Double.valueOf(acqDefRate.getAcqTdr().toString()));
											newChargingDetails.setBankFixCharge(
													Double.valueOf(acqDefRate.getAcqSuf().toString()));
											newChargingDetails.setMerchantTDR(
													Double.valueOf(merchantDefRate.getMerchantTdr().toString()));
											newChargingDetails.setMerchantFixCharge(
													Double.valueOf(merchantDefRate.getMerchantSuf().toString()));
											
											Double pgTdr = new BigDecimal(newChargingDetails.getMerchantTDR() - newChargingDetails.getBankTDR()).setScale(2,BigDecimal.ROUND_HALF_DOWN).doubleValue();
											Double pgFc =  new BigDecimal(newChargingDetails.getMerchantFixCharge()
													- newChargingDetails.getBankFixCharge()).setScale(2,BigDecimal.ROUND_HALF_DOWN).doubleValue();
											
											newChargingDetails.setPgTDR(pgTdr);
											newChargingDetails.setPgFixCharge(pgFc);

										}

									}

									account.addChargingDetail(newChargingDetails);
									if (newChargingDetails.getAcquiringMode().equals(onUsOffUs.ON_US)) {
										routerConfigDao.updateRouterByCD(newChargingDetails, "created", false);
									} else {
										routerConfigDao.updateRouterByCD(newChargingDetails, "created", true);
									}
								}

							}
						}
					}
				}

			}
		}
		return account;
	}

	// remove code
	public Account removeAccountCurrency(Account account, AccountCurrency[] selectedAccountCurrency) {
		Iterator<AccountCurrency> accountCurrencySetItrator = account.getAccountCurrencySet().iterator();
		Set<ChargingDetails> chargingDetails = account.getChargingDetails();

		if (null == selectedAccountCurrency || selectedAccountCurrency.length == 0) {
			selectedAccountCurrency = new AccountCurrency[] {};
		}
		while (accountCurrencySetItrator.hasNext()) {
			boolean flag = false;
			AccountCurrency accountCurrency = accountCurrencySetItrator.next();
			for (AccountCurrency accountCurrencyFE : selectedAccountCurrency) {
				if (accountCurrency.getCurrencyCode().equals(accountCurrencyFE.getCurrencyCode())) {
					flag = true;
				}
			}
			if (!flag) {
				for (ChargingDetails chargingDetail : chargingDetails) {
					if (chargingDetail.getStatus().equals(TDRStatus.ACTIVE)
							&& chargingDetail.getCurrency().equals(accountCurrency.getCurrencyCode())) {
						chargingDetail.setStatus(TDRStatus.INACTIVE);
						chargingDetail.setUpdatedDate(new Date());
						routerConfigDao.updateRouterByCD(chargingDetail, "removed", true);
					}
				}
				accountCurrencySetItrator.remove();
			}
		}
		return account;
	}

	public List<String> getCurrencyList() {
		List<String> mappedCurrency = new ArrayList<String>();
		Set<AccountCurrency> accountCurrencySet = account.getAccountCurrencySet();
		for (AccountCurrency accountCurrency : accountCurrencySet) {
			mappedCurrency.add(accountCurrency.getCurrencyCode());
		}
		return mappedCurrency;
	}
}
