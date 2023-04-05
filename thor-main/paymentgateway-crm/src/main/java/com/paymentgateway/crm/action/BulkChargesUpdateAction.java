package com.paymentgateway.crm.action;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.paymentgateway.commons.dao.ChargingDetailsFactory;
import com.paymentgateway.commons.dao.HibernateSessionProvider;
import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.user.Account;
import com.paymentgateway.commons.user.AccountCurrencyRegion;
import com.paymentgateway.commons.user.CardHolderType;
import com.paymentgateway.commons.user.ChargingDetails;
import com.paymentgateway.commons.user.ChargingDetailsDao;
import com.paymentgateway.commons.user.Merchants;
import com.paymentgateway.commons.user.PendingBulkCharges;
import com.paymentgateway.commons.user.PermissionType;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.user.UserType;
import com.paymentgateway.commons.util.AcquirerType;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.Currency;
import com.paymentgateway.commons.util.MopType;
import com.paymentgateway.commons.util.PaymentType;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.TDRStatus;
import com.paymentgateway.commons.util.TransactionType;
import com.paymentgateway.commons.util.onUsOffUs;
import com.paymentgateway.crm.actionBeans.ChargingDetailsMaintainer;

public class BulkChargesUpdateAction extends AbstractSecureAction {

	@Autowired
	private UserDao userDao;

	@Autowired
	private ChargingDetailsDao chargingDetailsDao;

	@Autowired
	private PropertiesManager propertiesManager;

	@Autowired
	private ChargingDetailsMaintainer editChargingDetails;

	@Autowired
	private ChargingDetailsFactory chargingDetailProvider;

	private static Logger logger = LoggerFactory.getLogger(BulkChargesUpdateAction.class.getName());
	private static final long serialVersionUID = -6879974923614009981L;

	public List<Merchants> listMerchant = new ArrayList<Merchants>();
	private String businessType;

	private String merchantList;
	private String acquirer;
	private String paymentType;
	private String paymentRegion;
	private String onOff;
	private String slab;
	private boolean showMerchant;
	private boolean showSaveButton;
	private String allDetails;
	private String response;
	private String responseStatus;
	private long id;
	private String operation;
	private Map<String, Object> aaData = new HashMap<String, Object>();

	private Map<String, List<BulkChargesObject>> bulkDataMap = new HashMap<String, List<BulkChargesObject>>();

	public String execute() {

		try {

			setListMerchant(userDao.getMerchantList());
			setShowMerchant(false);
			setShowSaveButton(false);
		}

		catch (Exception e) {
		}

		return INPUT;
	}

	public String update() {

		try {

			setShowMerchant(true);
			setShowSaveButton(true);

			setListMerchant(userDao.getMerchantList());
			setAcquirer(acquirer);
			setOnOff(onOff);
			setPaymentRegion(paymentRegion);
			setPaymentType(paymentType);

			if (paymentType.equalsIgnoreCase(PaymentType.CREDIT_CARD.getCode())
					|| paymentType.equalsIgnoreCase(PaymentType.DEBIT_CARD.getCode())
					|| paymentType.equalsIgnoreCase(PaymentType.EMI_CC.getCode())
					|| paymentType.equalsIgnoreCase(PaymentType.EMI_DC.getCode())) {

				List<String> mopList = new ArrayList<String>();
				List<MopType> mopListObj = new ArrayList<MopType>();
				mopListObj = createList();
				for (MopType mt : mopListObj) {
					if (mt != null) {
						mopList.add(mt.toString());
					}
				}
				/*
				 * mopList.add(MopType.VISA.getName());
				 * mopList.add(MopType.MASTERCARD.getName());
				 * mopList.add(MopType.RUPAY.getName());
				 */

				List<String> cardHolderList = new ArrayList<String>();

				if (!StringUtils.isEmpty(paymentRegion)
						&& paymentRegion.equalsIgnoreCase(AccountCurrencyRegion.DOMESTIC.toString())) {
					cardHolderList.add("Consumer");
					cardHolderList.add("Commercial");
					cardHolderList.add("Premium");
				} else {
					cardHolderList.add("Commercial");
				}

				List<String> slabList = new ArrayList<String>();

				if (slab.equalsIgnoreCase("All")) {

					slabList.add("All");
				} else {

					String slabArray[] = slab.split(",");

					for (String slb : slabArray) {
						slabList.add(slb);
					}
				}

				for (String mop : mopList) {

					List<BulkChargesObject> objectList = new ArrayList<BulkChargesObject>();

					for (String cardHolder : cardHolderList) {

						for (String slab : slabList) {

							BulkChargesObject bco = new BulkChargesObject();

							bco.setCardHolderType(cardHolder);
							bco.setSlab(slab);
							bco.setAcquirerTdr("0.00");
							bco.setAcquirerSuf("0.00");
							bco.setResellerTdr("0.00");
							bco.setResellerFC("0.00");
							bco.setMerchantTdr("0.00");
							bco.setMerchantSuf("0.00");
							bco.setGst("18.00");
							bco.setMaxChargeAcquirer("0.00");
							bco.setMaxChargeMerchant("0.00");
							bco.setAllowFC(false);
							bco.setChargesFlag(false);
							bco.setSelect(false);

							objectList.add(bco);
						}
					}

					bulkDataMap.put(mop, objectList);
				}
			}

			else if (paymentType.equalsIgnoreCase(PaymentType.NET_BANKING.getCode())) {

				List<String> mopList = new ArrayList<String>();

				if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("NBMOP"))) {

					String nbMop = propertiesManager.propertiesMap.get("NBMOP");
					String nbArray[] = nbMop.split(",");

					for (String mop : nbArray) {

						if (StringUtils.isNotBlank(mop)) {

							if (StringUtils.isNotBlank(MopType.getmopName(mop))) {
								mopList.add(MopType.getmopName(mop));
							}

						}

					}

				} else {
					mopList.add("No Banks Configured");
				}

				List<String> slabList = new ArrayList<String>();

				if (slab.equalsIgnoreCase("All")) {

					slabList.add("All");
				} else {

					String slabArray[] = slab.split(",");

					for (String slb : slabArray) {
						slabList.add(slb);
					}
				}

				List<BulkChargesObject> objectList = new ArrayList<BulkChargesObject>();
				for (String mop : mopList) {

					for (String slab : slabList) {

						BulkChargesObject bco = new BulkChargesObject();

						bco.setCardHolderType(mop);
						bco.setSlab(slab);
						bco.setPgTdr("0.00");
						bco.setPgSuf("0.00");
						bco.setAcquirerTdr("0.00");
						bco.setAcquirerSuf("0.00");
						bco.setResellerTdr("0.00");
						bco.setResellerFC("0.00");
						bco.setMerchantTdr("0.00");
						bco.setMerchantSuf("0.00");
						bco.setGst("18.00");
						bco.setMaxChargeAcquirer("0.00");
						bco.setMaxChargeMerchant("0.00");
						bco.setAllowFC(false);
						bco.setChargesFlag(false);
						bco.setSelect(false);

						objectList.add(bco);
					}

				}
				bulkDataMap.put("Net Banking - Consumer", objectList);

			}

			else if (paymentType.equalsIgnoreCase(PaymentType.UPI.getCode())) {

				List<String> mopList = new ArrayList<String>();

				if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("UPI"))) {

					String nbMop = propertiesManager.propertiesMap.get("UPI");
					String nbArray[] = nbMop.split(",");

					for (String mop : nbArray) {

						if (StringUtils.isNotBlank(mop)) {

							if (StringUtils.isNotBlank(MopType.getmopName(mop))) {
								mopList.add(MopType.getmopName(mop));
							}

						}

					}

				} else {
					mopList.add("No Banks Configured");
				}

				List<String> slabList = new ArrayList<String>();

				if (slab.equalsIgnoreCase("All")) {

					slabList.add("All");
				} else {

					String slabArray[] = slab.split(",");

					for (String slb : slabArray) {
						slabList.add(slb);
					}
				}

				List<BulkChargesObject> objectList = new ArrayList<BulkChargesObject>();
				for (String mop : mopList) {

					for (String slab : slabList) {

						BulkChargesObject bco = new BulkChargesObject();

						bco.setCardHolderType(mop);
						bco.setSlab(slab);
						bco.setPgTdr("0.00");
						bco.setPgSuf("0.00");
						bco.setAcquirerTdr("0.00");
						bco.setAcquirerSuf("0.00");
						bco.setResellerTdr("0.00");
						bco.setResellerFC("0.00");
						bco.setMerchantTdr("0.00");
						bco.setMerchantSuf("0.00");
						bco.setGst("18.00");
						bco.setMaxChargeAcquirer("0.00");
						bco.setMaxChargeMerchant("0.00");
						bco.setAllowFC(false);
						bco.setChargesFlag(false);
						bco.setSelect(false);

						objectList.add(bco);
					}
				}

				bulkDataMap.put("UPI - Consumer", objectList);
			}

			else if (paymentType.equalsIgnoreCase(PaymentType.WALLET.getCode())) {

				List<String> mopList = new ArrayList<String>();

				if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("WALLET"))) {

					String nbMop = propertiesManager.propertiesMap.get("WALLET");
					String nbArray[] = nbMop.split(",");

					for (String mop : nbArray) {

						if (StringUtils.isNotBlank(mop)) {

							if (StringUtils.isNotBlank(MopType.getmopName(mop))) {
								mopList.add(MopType.getmopName(mop));
							}

						}

					}

				} else {
					mopList.add("No Banks Configured");
				}

				List<String> cardHolderList = new ArrayList<String>();
				cardHolderList.add("Consumer");

				List<String> slabList = new ArrayList<String>();

				if (slab.equalsIgnoreCase("All")) {

					slabList.add("All");
				} else {

					String slabArray[] = slab.split(",");

					for (String slb : slabArray) {
						slabList.add(slb);
					}
				}

				List<BulkChargesObject> objectList = new ArrayList<BulkChargesObject>();
				for (String mop : mopList) {

					for (String slab : slabList) {

						BulkChargesObject bco = new BulkChargesObject();

						bco.setCardHolderType(mop);
						bco.setSlab(slab);
						bco.setPgTdr("0.00");
						bco.setPgSuf("0.00");
						bco.setAcquirerTdr("0.00");
						bco.setAcquirerSuf("0.00");
						bco.setResellerTdr("0.00");
						bco.setResellerFC("0.00");
						bco.setMerchantTdr("0.00");
						bco.setMerchantSuf("0.00");
						bco.setGst("18.00");
						bco.setMaxChargeAcquirer("0.00");
						bco.setMaxChargeMerchant("0.00");
						bco.setAllowFC(false);
						bco.setChargesFlag(false);
						bco.setSelect(false);

						objectList.add(bco);
					}
				}

				bulkDataMap.put("Wallet - Consumer", objectList);

			}

			else if (paymentType.equalsIgnoreCase(PaymentType.COD.getCode())) {

				List<String> mopList = new ArrayList<String>();

				if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("COD"))) {

					String nbMop = propertiesManager.propertiesMap.get("COD");
					String nbArray[] = nbMop.split(",");

					for (String mop : nbArray) {

						if (StringUtils.isNotBlank(mop)) {

							if (StringUtils.isNotBlank(MopType.getmopName(mop))) {
								mopList.add(MopType.getmopName(mop));
							}

						}

					}

				} else {
					mopList.add("No Banks Configured");
				}

				List<String> cardHolderList = new ArrayList<String>();
				cardHolderList.add("Consumer");

				List<String> slabList = new ArrayList<String>();

				if (slab.equalsIgnoreCase("All")) {

					slabList.add("All");
				} else {

					String slabArray[] = slab.split(",");

					for (String slb : slabArray) {
						slabList.add(slb);
					}
				}

				List<BulkChargesObject> objectList = new ArrayList<BulkChargesObject>();
				for (String mop : mopList) {

					for (String slab : slabList) {

						BulkChargesObject bco = new BulkChargesObject();

						bco.setCardHolderType(mop);
						bco.setSlab(slab);
						bco.setPgTdr("0.00");
						bco.setPgSuf("0.00");
						bco.setAcquirerTdr("0.00");
						bco.setAcquirerSuf("0.00");
						bco.setResellerTdr("0.00");
						bco.setResellerFC("0.00");
						bco.setMerchantTdr("0.00");
						bco.setMerchantSuf("0.00");
						bco.setGst("18.00");
						bco.setMaxChargeAcquirer("0.00");
						bco.setMaxChargeMerchant("0.00");
						bco.setAllowFC(false);
						bco.setChargesFlag(false);
						bco.setSelect(false);

						objectList.add(bco);
					}
				}

				bulkDataMap.put("COD - Consumer", objectList);

			}

		}

		catch (Exception e) {
			logger.error("Exception Caught ", e);
			return ERROR;
		}

		return SUCCESS;
	}

	public String udpateValues() {

		try {

			User sessionUser = (User) sessionMap.get(Constants.USER.getValue());
			StringBuilder permissions = new StringBuilder();
			permissions.append(sessionMap.get(Constants.USER_PERMISSION.getValue()));

			String[] merchantLst = merchantList.split(",");
			for (String mrnt : merchantLst) {
				String usreEmailId = userDao.getEmailIdByPayId(mrnt);
				CardHolderType cht = null;

				// Set currency default to 356
				String currency = "356";

				// Set default transactionType to sale
				TransactionType transactionType = TransactionType.SALE;

				// Set acquirer Name

				String acquirerName = AcquirerType.getAcquirerName(acquirer);

				// Get acquiring mode
				onUsOffUs acquiringMode = null;

				if (onOff.equalsIgnoreCase("ON_US")) {

					acquiringMode = onUsOffUs.ON_US;
				} else {
					acquiringMode = onUsOffUs.OFF_US;
				}

				// Get payment Type Instance
				PaymentType paymentTypeIns = PaymentType.getInstanceUsingCode(paymentType);

				// Get Account Currency Region
				AccountCurrencyRegion acr = null;

				if (paymentRegion.equalsIgnoreCase("DOMESTIC")) {
					acr = AccountCurrencyRegion.DOMESTIC;
				} else {
					acr = AccountCurrencyRegion.INTERNATIONAL;
				}

				if (sessionUser.getUserType().equals(UserType.SUBADMIN)
						&& permissions.toString().contains(PermissionType.BULK_UPDATE_CHARGES.getPermission())) {

					PendingBulkCharges pendingBulkCharges = new PendingBulkCharges();

					pendingBulkCharges.setAllChargingDetail(allDetails);
					pendingBulkCharges.setCreatedDate(new Date());
					pendingBulkCharges.setAcquiringMode(acquiringMode);
					pendingBulkCharges.setPayId(mrnt);
					pendingBulkCharges.setPaymentsRegion(acr);
					pendingBulkCharges.setPaymentType(paymentTypeIns);
					pendingBulkCharges.setRequestedBy(sessionUser.getEmailId());
					pendingBulkCharges.setStatus(TDRStatus.PENDING);
					pendingBulkCharges.setTransactionType(transactionType);
					pendingBulkCharges.setCurrency(currency);
					pendingBulkCharges.setAcquirerName(acquirerName);

					if (StringUtils.isNotBlank(slab))
						pendingBulkCharges.setSlab(slab);
					else
						pendingBulkCharges.setSlab("ALL");

					PendingBulkCharges fetchPendingBulkDetails = chargingDetailsDao.findPendingBulkCharges(mrnt,
							pendingBulkCharges.getSlab(), paymentTypeIns, transactionType, acquirerName, acr,
							acquiringMode, currency);

					if (fetchPendingBulkDetails != null) {
						fetchPendingBulkDetails.setStatus(TDRStatus.INACTIVE);
						fetchPendingBulkDetails.setUpdatedDate(new Date());
						fetchPendingBulkDetails.setUpdateBy(sessionUser.getEmailId());
						// deleting old Pending Mapping
						chargingDetailsDao.deleteBulkCharges(fetchPendingBulkDetails);
					}

					chargingDetailsDao.insertBulkCharges(pendingBulkCharges);

					setResponse("Bulk Charges Saved & Pending for Approval");

				} else if (sessionUser.getUserType().equals(UserType.ADMIN)) {

					String allDetailsArray[] = allDetails.replace(" ;", ";").split(";");
					for (String details : allDetailsArray) {

						String detailsArray[] = details.split(",");

						if (detailsArray[0].equalsIgnoreCase("Consumer")) {

							cht = CardHolderType.CONSUMER;
						} else if (detailsArray[0].equalsIgnoreCase("Commercial")) {

							cht = CardHolderType.COMMERCIAL;

						} else if (detailsArray[0].equalsIgnoreCase("Premium")) {

							cht = CardHolderType.PREMIUM;
						} else {

							if (!paymentType.equalsIgnoreCase(PaymentType.CREDIT_CARD.getCode())
									&& !paymentType.equalsIgnoreCase(PaymentType.DEBIT_CARD.getCode())
									&& !paymentType.equalsIgnoreCase(PaymentType.PREPAID_CARD.getCode())
									&& !paymentType.equalsIgnoreCase(PaymentType.EMI_CC.getCode())
									&& !paymentType.equalsIgnoreCase(PaymentType.EMI_DC.getCode())) {

								// For other payment modes, default type is
								// consumer
								cht = CardHolderType.CONSUMER;
							}
						}
						setAaData(chargingDetailProvider.getChargingDetailsMap(usreEmailId, acquirer, paymentRegion,
								onOff, cht.toString(), PaymentType.getInstanceUsingCode(paymentType).toString()));
						if (aaData.size() == 0) {
							addActionMessage(ErrorType.CHARGINGDETAIL_NOT_FETCHED.getResponseMessage());
							return SUCCESS;
						}
						if (aaData.size() == 1 && aaData.containsKey("regionType")) {
							setResponse("Merchant Mapping not saved");
							return SUCCESS;
						}
					}

					List<String> payIdList = new ArrayList<>();

					if (merchantList.equalsIgnoreCase("All")) {

						List<Merchants> merchantList = userDao.getActiveMerchantList();

						for (Merchants merchant : merchantList) {
							payIdList.add(merchant.getPayId());
						}
					} else {

						String merchantListArray[] = merchantList.split(",");

						for (String payId : merchantListArray) {
							payIdList.add(payId);
						}

					}

					String allDetailsArray1[] = allDetails.replace(" ;", ";").split(";");
					StringBuilder successfulUpdate = new StringBuilder();
					StringBuilder unsuccessfulUpdate = new StringBuilder();
					// Loop for all selected merchants
					for (String payId : payIdList) {

						User user = userDao.findPayId(payId);
						Account account = user.getAccountUsingAcquirerCode(acquirer);

						// If acquirer is not mapped with this payment type to
						// this merchant, skip to
						// next merchant
						if (account == null) {
							unsuccessfulUpdate.append(", " + user.getBusinessName());
							continue;
						}
						// Loop for all selected slabs

						for (String details : allDetailsArray1) {

							String detailsArray[] = details.split(",");

							CardHolderType cardHolderType = null;
							MopType mopType = null;

							// First element in array is CardHolderType incase
							// of cards
							// and Mop Type incase of other payment modes

							if (detailsArray[0].equalsIgnoreCase("Consumer")) {

								cardHolderType = CardHolderType.CONSUMER;
							} else if (detailsArray[0].equalsIgnoreCase("Commercial")) {

								cardHolderType = CardHolderType.COMMERCIAL;

							} else if (detailsArray[0].equalsIgnoreCase("Premium")) {

								cardHolderType = CardHolderType.PREMIUM;
							} else {

								if (!paymentType.equalsIgnoreCase(PaymentType.CREDIT_CARD.getCode())
										&& !paymentType.equalsIgnoreCase(PaymentType.DEBIT_CARD.getCode())
										&& !paymentType.equalsIgnoreCase(PaymentType.PREPAID_CARD.getCode())
										&& !paymentType.equalsIgnoreCase(PaymentType.EMI_CC.getCode())
										&& !paymentType.equalsIgnoreCase(PaymentType.EMI_DC.getCode())) {

									// For other payment modes, default type is
									// consumer
									cardHolderType = CardHolderType.CONSUMER;
									mopType = MopType.getInstanceIgnoreCase(detailsArray[0]);

								}
							}

							if (paymentType.equalsIgnoreCase(PaymentType.CREDIT_CARD.getCode())
									|| paymentType.equalsIgnoreCase(PaymentType.DEBIT_CARD.getCode())
									|| paymentType.equalsIgnoreCase(PaymentType.PREPAID_CARD.getCode())
									|| paymentType.equalsIgnoreCase(PaymentType.EMI_CC.getCode())
									|| paymentType.equalsIgnoreCase(PaymentType.EMI_DC.getCode())) {

								mopType = MopType.getInstanceIgnoreCase(detailsArray[18]);

							}

							// Populate Slab List
							List<String> slabList = new ArrayList<String>();

							if (detailsArray[1].equalsIgnoreCase("ALL")) {

								slabList.add("0.01-1000.00");
								slabList.add("1000.01-2000.00");
								slabList.add("2000.01-1000000.00");
							}

							else {

								String slbArray[] = detailsArray[1].split(",");

								for (String item : slbArray) {
									slabList.add(item);
								}

							}

							for (String slb : slabList) {

								String slabArray[] = slb.split("-");

								String minTxnAmount = slabArray[0];

								String slabId = null;

								if (minTxnAmount.equalsIgnoreCase("0.01")) {
									slabId = "01";
								} else if (minTxnAmount.equalsIgnoreCase("1000.01")) {
									slabId = "02";
								} else {
									slabId = "03";
								}

								// Find Old Charging Detail from DB
								ChargingDetails cdFromDB = chargingDetailsDao.findDetailForUpdate(payId, slabId,
										paymentTypeIns, mopType, transactionType, acquirerName, acr, acquiringMode,
										cardHolderType, currency);

								// Update old charging detail
								if (cdFromDB != null) {
									updateBulkData(slb, detailsArray, user, onOff, false, cdFromDB, mopType,
											cardHolderType);
								}

								// Add new Charging Detail to the Set
								else {
									// unsuccessfulUpdate.append(", " +
									// user.getBusinessName());
									// continue;
									// No charging details means no mapping has
									// been done, so don't create new
									// mapping
									// updateBulkData(slb, detailsArray, user,
									// onOff, true, cdFromDB, mopType,
									// cardHolderType);
								}

							}

						}

						successfulUpdate.append(", " + user.getBusinessName());
					}
					StringBuilder finalResponse = new StringBuilder();
					if (successfulUpdate.length() > 0) {
						finalResponse
								.append("Charges updated successfully for: " + successfulUpdate.substring(2) + "\n");
					}
					if (unsuccessfulUpdate.length() > 0) {
						finalResponse.append("Charges not updated for: " + unsuccessfulUpdate.substring(2) + "\n");
					}

					setResponse(finalResponse.toString());
				}
			}

			return SUCCESS;
		}

		catch (Exception e) {

			logger.error("Exception in updating charging details in bulk", e);
			setResponse("Unable to update charging details");
			return ERROR;
		}

	}

	public String updateBulkCharges() {

		try {

			User sessionUser = (User) sessionMap.get(Constants.USER.getValue());
			StringBuilder permissions = new StringBuilder();
			permissions.append(sessionMap.get(Constants.USER_PERMISSION.getValue()));

			if (sessionUser.getUserType().equals(UserType.ADMIN)) {

				PendingBulkCharges fetchPendingBulkDetails = chargingDetailsDao.findPendingBulkChargesById(id);

				if (fetchPendingBulkDetails != null) {

					if (operation.equalsIgnoreCase("accept")) {

						onUsOffUs acquiringMode = null;
						AccountCurrencyRegion acr = null;

						setAllDetails(fetchPendingBulkDetails.getAllChargingDetail());
						setAcquirer(fetchPendingBulkDetails.getAcquirerName());
						setOnOff(String.valueOf(fetchPendingBulkDetails.getAcquiringMode()));
						acquiringMode = fetchPendingBulkDetails.getAcquiringMode();
						acr = fetchPendingBulkDetails.getPaymentsRegion();

						setPaymentRegion(String.valueOf(fetchPendingBulkDetails.getPaymentsRegion()));

						String usreEmailId = userDao.getEmailIdByPayId(fetchPendingBulkDetails.getPayId());
						CardHolderType cht = null;

						// Set currency default to 356
						String currency = "356";

						// Set default transactionType to sale
						TransactionType transactionType = TransactionType.SALE;

						// Set acquirer Name /Code
						String acquirerName = acquirer;
						String acquirerCode = AcquirerType.getAcquirerCode(acquirer);
						setAcquirer(acquirerCode);

						// Get payment Type Instance
						PaymentType paymentTypeIns = PaymentType.getInstanceUsingStringValue(paymentType);
						paymentType = PaymentType.getCodeUsingName(paymentType);

						// String allDetailsArray[] = allDetails.replace(" ;",
						// ";").split(";");
						// for (String details : allDetailsArray) {
						//
						// String detailsArray[] = details.split(",");
						//
						// if (detailsArray[0].equalsIgnoreCase("Consumer")) {
						//
						// cht = CardHolderType.CONSUMER;
						// } else if
						// (detailsArray[0].equalsIgnoreCase("Commercial")) {
						//
						// cht = CardHolderType.COMMERCIAL;
						//
						// } else if
						// (detailsArray[0].equalsIgnoreCase("Premium")) {
						//
						// cht = CardHolderType.PREMIUM;
						// } else {
						//
						// if
						// (!paymentType.equalsIgnoreCase(PaymentType.CREDIT_CARD.getCode())
						// &&
						// !paymentType.equalsIgnoreCase(PaymentType.DEBIT_CARD.getCode())
						// &&
						// !paymentType.equalsIgnoreCase(PaymentType.PREPAID_CARD.getCode())
						// &&
						// !paymentType.equalsIgnoreCase(PaymentType.EMI_CC.getCode())
						// &&
						// !paymentType.equalsIgnoreCase(PaymentType.EMI_DC.getCode()))
						// {
						//
						// // For other payment modes, default type is consumer
						// cht = CardHolderType.CONSUMER;
						// }
						// }
						// setAaData(chargingDetailProvider.getChargingDetailsMap(usreEmailId,
						// acquirer, paymentRegion, onOff,
						// cht.toString(),
						// PaymentType.getInstanceUsingCode(paymentType).toString()));
						// if(aaData.size()==0){
						// addActionMessage(ErrorType.CHARGINGDETAIL_NOT_FETCHED.getResponseMessage());
						// return SUCCESS;
						// }
						// if (aaData.size() == 1 &&
						// aaData.containsKey("regionType")) {
						// setResponse("Merchant Mapping not saved");
						// return SUCCESS;
						// }
						// }

						String allDetailsArray1[] = allDetails.replace(" ;", ";").split(";");

						User user = userDao.findPayId(fetchPendingBulkDetails.getPayId());
						Account account = user.getAccountUsingAcquirerCode(acquirerCode);

						// If acquirer is not mapped with this payment type to
						// this merchant, skip to
						// next merchant
						if (account == null) {
							chargingDetailsDao.updateBulkPendingStatus(fetchPendingBulkDetails, TDRStatus.REJECTED,
									sessionUser.getEmailId());
							setResponseStatus("failed");
							setResponse("No Acquirer Found");
							return SUCCESS;
						}
						// Loop for all selected slabs

						for (String details : allDetailsArray1) {

							String detailsArray[] = details.split(",");

							CardHolderType cardHolderType = null;
							MopType mopType = null;

							// First element in array is CardHolderType incase
							// of cards
							// and Mop Type incase of other payment modes

							if (detailsArray[0].equalsIgnoreCase("Consumer")) {

								cardHolderType = CardHolderType.CONSUMER;
							} else if (detailsArray[0].equalsIgnoreCase("Commercial")) {

								cardHolderType = CardHolderType.COMMERCIAL;

							} else if (detailsArray[0].equalsIgnoreCase("Premium")) {

								cardHolderType = CardHolderType.PREMIUM;
							} else {

								if (!paymentType.equalsIgnoreCase(PaymentType.CREDIT_CARD.getCode())
										&& !paymentType.equalsIgnoreCase(PaymentType.DEBIT_CARD.getCode())
										&& !paymentType.equalsIgnoreCase(PaymentType.PREPAID_CARD.getCode())
										&& !paymentType.equalsIgnoreCase(PaymentType.EMI_CC.getCode())
										&& !paymentType.equalsIgnoreCase(PaymentType.EMI_DC.getCode())) {

									// For other payment modes, default type is
									// consumer
									cardHolderType = CardHolderType.CONSUMER;
									mopType = MopType.getInstanceIgnoreCase(detailsArray[0]);

								}
							}

							if (paymentType.equalsIgnoreCase(PaymentType.CREDIT_CARD.getCode())
									|| paymentType.equalsIgnoreCase(PaymentType.DEBIT_CARD.getCode())
									|| paymentType.equalsIgnoreCase(PaymentType.PREPAID_CARD.getCode())
									|| paymentType.equalsIgnoreCase(PaymentType.EMI_CC.getCode())
									|| paymentType.equalsIgnoreCase(PaymentType.EMI_DC.getCode())) {

								mopType = MopType.getInstanceIgnoreCase(detailsArray[18]);

							}

							// Populate Slab List
							List<String> slabList = new ArrayList<String>();

							if (detailsArray[1].equalsIgnoreCase("ALL")) {

								slabList.add("0.01-1000.00");
								slabList.add("1000.01-2000.00");
								slabList.add("2000.01-1000000.00");
							}

							else {

								String slbArray[] = detailsArray[1].split(",");

								for (String item : slbArray) {
									slabList.add(item);
								}

							}

							for (String slb : slabList) {

								String slabArray[] = slb.split("-");

								String minTxnAmount = slabArray[0];

								String slabId = null;

								if (minTxnAmount.equalsIgnoreCase("0.01")) {
									slabId = "01";
								} else if (minTxnAmount.equalsIgnoreCase("1000.01")) {
									slabId = "02";
								} else {
									slabId = "03";
								}

								// Find Old Charging Detail from DB
								ChargingDetails cdFromDB = chargingDetailsDao.findDetailForUpdate(
										fetchPendingBulkDetails.getPayId(), slabId, paymentTypeIns, mopType,
										transactionType, acquirerName, acr, acquiringMode, cardHolderType, currency);

								// Update old charging detail
								if (cdFromDB != null) {
									updateBulkData(slb, detailsArray, user, onOff, false, cdFromDB, mopType,
											cardHolderType);
								}

								// Add new Charging Detail to the Set
								else {
									// unsuccessfulUpdate.append(", " +
									// user.getBusinessName());
									// continue;
									// No charging details means no mapping has
									// been done, so don't create new
									// mapping
									// updateBulkData(slb, detailsArray, user,
									// onOff, true, cdFromDB, mopType,
									// cardHolderType);
								}

							}

						}

						try {
							PendingBulkCharges fetchActiveBulkDetails = chargingDetailsDao.findDetailForUpdate(
									fetchPendingBulkDetails.getPayId(), fetchPendingBulkDetails.getSlab(),
									fetchPendingBulkDetails.getPaymentType(),
									fetchPendingBulkDetails.getAcquiringMode(),
									fetchPendingBulkDetails.getTransactionType(),
									fetchPendingBulkDetails.getAcquirerName(),
									fetchPendingBulkDetails.getPaymentsRegion(), fetchPendingBulkDetails.getCurrency());

							if (fetchActiveBulkDetails != null) {
								fetchActiveBulkDetails.setStatus(TDRStatus.INACTIVE);
								fetchActiveBulkDetails.setUpdatedDate(new Date());
								fetchActiveBulkDetails.setUpdateBy(sessionUser.getEmailId());
								// deleting old Pending Mapping
								chargingDetailsDao.deleteBulkCharges(fetchPendingBulkDetails);
							}
						} catch (Exception e) {
							logger.info("no pending bulk request found with details");
						}

						chargingDetailsDao.updateBulkPendingStatus(fetchPendingBulkDetails, TDRStatus.ACTIVE,
								sessionUser.getEmailId());

						setResponseStatus("Success");
						setResponse("Bulk Charges Accpted Successfully");
					} else {
						chargingDetailsDao.updateBulkPendingStatus(fetchPendingBulkDetails, TDRStatus.REJECTED,
								sessionUser.getEmailId());

						setResponseStatus("Success");
						setResponse("Bulk Charges Rejected Successfully");
					}
				} else {
					setResponseStatus("Failed");
					setResponse("No Pending Request Found with the details");
				}
			}

			return SUCCESS;
		}

		catch (Exception e) {

			logger.error("Exception in updating Bulk charging details ", e);
			setResponseStatus("Failed");
			setResponse("Unable to update Bulk charging details");
			return SUCCESS;
		}

	}

	public void updateBulkData(String slb, String detailsArray[], User merchant, String acquiringMode, boolean newEntry,
			ChargingDetails cd, MopType mopType, CardHolderType cardHolderType) {

		try {

			// Set acquirer Name
			String acquirerName = AcquirerType.getAcquirerName(acquirer);

			// Get payment Type Instance
			PaymentType paymentTypeIns = PaymentType.getInstanceUsingCode(paymentType);

			// Get Account Currency Region
			AccountCurrencyRegion acr = null;

			if (paymentRegion.equalsIgnoreCase("DOMESTIC")) {
				acr = AccountCurrencyRegion.DOMESTIC;
			} else {
				acr = AccountCurrencyRegion.INTERNATIONAL;
			}

			String currency = "356";

			Double merchantTdr = Double.valueOf(detailsArray[2]);
			Double merchantSuf = Double.valueOf(detailsArray[3]);
			Double acqTdr = Double.valueOf(detailsArray[4]);
			Double acqSuf = Double.valueOf(detailsArray[5]);

			Double resellerTdr = Double.valueOf(detailsArray[6]);
			Double resellerFC = Double.valueOf(detailsArray[7]);

			Double maxChargeMerchant = Double.valueOf(detailsArray[11]);
			Double maxChargeAcquirer = Double.valueOf(detailsArray[12]);

			boolean allowFC = false;
			boolean chargesFlag = false;

			if (detailsArray[9].toString().equalsIgnoreCase("true")) {
				allowFC = true;
			}

			if (detailsArray[10].toString().equalsIgnoreCase("true")) {
				chargesFlag = true;
			}

			// Get acquiring mode
			onUsOffUs onOff = null;

			if (acquiringMode.equalsIgnoreCase("ON_US")) {
				onOff = onUsOffUs.ON_US;
			} else {
				onOff = onUsOffUs.OFF_US;
			}

			long serviceTax = Long.valueOf("18");
			User sessionUser = (User) sessionMap.get(Constants.USER.getValue());
			String loginUserEmailId = sessionUser.getEmailId();
			String userType = sessionUser.getUserType().name().toString();

			String slabArray[] = slb.split("-");

			String minTxnAmount = slabArray[0];
			String maxTxnAmount = slabArray[1];

			String slabId = null;

			if (minTxnAmount.equalsIgnoreCase("0.01")) {
				slabId = "01";
			} else if (minTxnAmount.equalsIgnoreCase("1000.01")) {
				slabId = "02";
			} else {
				slabId = "03";
			}

			if (newEntry == false) {

				ChargingDetails chargingDetails = cd;

				chargingDetails.setMaxChargeAcquirer(maxChargeAcquirer);
				chargingDetails.setMaxChargeMerchant(maxChargeMerchant);
				chargingDetails.setAcquiringMode(onOff);
				chargingDetails.setAllowFixCharge(allowFC);
				chargingDetails.setChargesFlag(chargesFlag);
				chargingDetails.setMinTxnAmount(Double.valueOf(minTxnAmount));
				chargingDetails.setMaxTxnAmount(Double.valueOf(maxTxnAmount));
				chargingDetails.setBankTDR(acqTdr);
				chargingDetails.setBankFixCharge(acqSuf);

				chargingDetails.setResellerTDR(resellerTdr);
				chargingDetails.setResellerFixCharge(resellerFC);

				chargingDetails.setMerchantTDR(merchantTdr);
				chargingDetails.setMerchantFixCharge(merchantSuf);
				chargingDetails.setMerchantServiceTax(serviceTax);
				chargingDetails.setBankServiceTax(serviceTax);
				chargingDetails.setResellerServiceTax(serviceTax);

				editChargingDetails.editChargingDetail(merchant.getEmailId(), acquirer, chargingDetails, true, userType,
						loginUserEmailId);
			}

			if (newEntry == true) {
				User user = merchant;

				Account account = user.getAccountUsingAcquirerCode(acquirer);
				if (null == account) {
					throw new SystemException(ErrorType.ACQUIRER_NOT_FOUND,
							ErrorType.ACQUIRER_NOT_FOUND.getResponseMessage());
				}

				Session session = null;
				session = HibernateSessionProvider.getSession();
				Transaction tx = session.beginTransaction();
				session.load(account, account.getId());

				ChargingDetails newChargingDetails = new ChargingDetails();
				newChargingDetails.setAcquirerName(acquirerName);
				newChargingDetails.setAcquiringMode(onOff);
				newChargingDetails.setAllowFixCharge(allowFC);
				newChargingDetails.setChargesFlag(chargesFlag);
				newChargingDetails.setBankFixCharge(acqSuf);
				newChargingDetails.setBankServiceTax(serviceTax);
				newChargingDetails.setBankTDR(acqTdr);
				newChargingDetails.setBankTDRAFC(0.00);
				newChargingDetails.setBusinessName(user.getBusinessName());
				newChargingDetails.setCardHolderType(cardHolderType);
				newChargingDetails.setCreatedDate(new Date());
				newChargingDetails.setCurrency(currency);
				newChargingDetails.setFixChargeLimit(0.00);
				newChargingDetails.setMaxChargeAcquirer(maxChargeAcquirer);
				newChargingDetails.setMaxChargeMerchant(maxChargeMerchant);
				newChargingDetails.setMaxTxnAmount(Double.valueOf(maxTxnAmount));
				newChargingDetails.setMerchantFixCharge(merchantSuf);
				newChargingDetails.setMerchantFixChargeAFC(0.00);
				newChargingDetails.setMerchantName(user.getBusinessName());
				newChargingDetails.setMerchantServiceTax(serviceTax);
				newChargingDetails.setMerchantTDR(merchantTdr);
				newChargingDetails.setMerchantTDRAFC(0.00);
				newChargingDetails.setMinTxnAmount(Double.valueOf(minTxnAmount));
				newChargingDetails.setMopType(mopType);
				newChargingDetails.setPayId(merchant.getPayId());
				newChargingDetails.setPaymentsRegion(acr);
				newChargingDetails.setPaymentType(paymentTypeIns);
				newChargingDetails.setPgServiceTax(serviceTax);

				newChargingDetails.setResellerFixCharge(resellerFC);
				newChargingDetails.setResellerFixChargeAFC(0.00);
				newChargingDetails.setResellerServiceTax(serviceTax);
				newChargingDetails.setResellerTDR(resellerTdr);
				newChargingDetails.setResellerTDRAFC(0.00);

				newChargingDetails.setRequestedBy(sessionUser.getBusinessName());
				newChargingDetails.setSlabId(slabId);
				newChargingDetails.setStatus(TDRStatus.ACTIVE);
				newChargingDetails.setTransactionType(TransactionType.SALE);
				newChargingDetails.setUpdateBy(sessionUser.getEmailId());
				newChargingDetails.setUpdatedDate(new Date());

				account.addChargingDetail(newChargingDetails);
				session.saveOrUpdate(account);
				tx.commit();
			}

		} catch (Exception exception) {
			setResponse(ErrorType.CHARGINGDETAIL_NOT_SAVED.getResponseMessage());
			logger.error("Exception", exception);
		}

	}

	public String display() {
		return INPUT;
	}

	private List<MopType> createList() {

		List<MopType> mopList = new ArrayList<MopType>();
		try {

			AcquirerType acqType = AcquirerType.getInstancefromCode(acquirer);
			List<PaymentType> supportedPaymentTypes = PaymentType.getGetPaymentsFromSystemProp(acquirer);
			// setCurrencies(Currency.getAllCurrency());

			switch (acqType) {
			case FSS:
				// for (PaymentType pay : supportedPaymentTypes) {
				switch (PaymentType.getInstanceUsingCode(paymentType)) {
				case CREDIT_CARD:
					mopList.addAll(MopType.getFSSCCMops());
					break;
				case DEBIT_CARD:
					mopList.addAll(MopType.getFSSDCMops());
					break;
				case NET_BANKING:
					break;
				case WALLET:
					break;
				case UPI:
					mopList.addAll(MopType.getFSSUPIMops());
					break;
				default:
					break;
				}
				// }
				break;
			case HDFC:
				// for (PaymentType pay : supportedPaymentTypes) {
				switch (PaymentType.getInstanceUsingCode(paymentType)) {
				case CREDIT_CARD:
					mopList.addAll(MopType.getFSSCCMops());
					break;
				case DEBIT_CARD:
					mopList.addAll(MopType.getFSSDCMops());
					break;
				case NET_BANKING:
					break;
				case WALLET:
					break;
				case UPI:
					break;
				default:
					break;
				}
				// }
				break;
			case ICICI_FIRSTDATA:
				// for (PaymentType pay : supportedPaymentTypes) {
				switch (PaymentType.getInstanceUsingCode(paymentType)) {
				case CREDIT_CARD:
					mopList.addAll(MopType.getFIRSTDATACCMops());
					break;
				case DEBIT_CARD:
					mopList.addAll(MopType.getFIRSTDATADCMops());
					break;
				case NET_BANKING:
					break;
				case WALLET:
					break;
				default:
					break;
				}
				// }
				break;
			case IDFC_FIRSTDATA:
				// for (PaymentType pay : supportedPaymentTypes) {
				switch (PaymentType.getInstanceUsingCode(paymentType)) {
				case CREDIT_CARD:
					mopList.addAll(MopType.getFIRSTDATACCMops());
					break;
				case DEBIT_CARD:
					mopList.addAll(MopType.getFIRSTDATADCMops());
					break;
				case NET_BANKING:
					break;
				case WALLET:
					break;
				default:
					break;
				}
				// }
				break;
			case FEDERAL:
				// for (PaymentType pay : supportedPaymentTypes) {
				switch (PaymentType.getInstanceUsingCode(paymentType)) {
				case CREDIT_CARD:
					mopList.addAll(MopType.getFEDERALCCMops());
					break;
				case DEBIT_CARD:
					mopList.addAll(MopType.getFEDERALDCMops());
					break;
				case NET_BANKING:
					break;
				case WALLET:
					break;
				case UPI:
					mopList.addAll(MopType.getFEDERALUPIMops());
					break;
				default:
					break;
				}
				// }
				break;
			case BOB:
				// for (PaymentType pay : supportedPaymentTypes) {
				switch (PaymentType.getInstanceUsingCode(paymentType)) {
				case CREDIT_CARD:
					mopList.addAll(MopType.getBOBCCMops());
					break;
				case DEBIT_CARD:
					mopList.addAll(MopType.getBOBDCMops());
					break;
				case NET_BANKING:
					break;
				case WALLET:
					break;
				case UPI:
					mopList.addAll(MopType.getBOBUPIMops());
					break;
				case EMI_CC:
					mopList.addAll(MopType.getBOBEMCCMops());
					break;
				case EMI_DC:
					mopList.addAll(MopType.getBOBEMDCMops());
					break;
				case EMI:
					break;
				default:
					break;
				}
				// }
				break;
			case BILLDESK:
				// for (PaymentType pay : supportedPaymentTypes) {
				switch (PaymentType.getInstanceUsingCode(paymentType)) {
				case CREDIT_CARD:
					mopList.addAll(MopType.getBILLDESKCCMops());
					break;
				case DEBIT_CARD:
					mopList.addAll(MopType.getBILLDESKDCMops());
					break;
				case NET_BANKING:
					break;
				case WALLET:
					break;
				case UPI:
					break;
				default:
					break;
				}
				// }
				break;
			case ISGPAY:
				// for (PaymentType pay : supportedPaymentTypes) {
				switch (PaymentType.getInstanceUsingCode(paymentType)) {
				case CREDIT_CARD:
					mopList.addAll(MopType.getISGPAYCCMops());
					break;
				case DEBIT_CARD:
					mopList.addAll(MopType.getISGPAYDCMops());
					break;
				case NET_BANKING:
					break;
				case WALLET:
					break;
				case UPI:
					break;
				case EMI:
					break;
				default:
					break;
				}
				// }
				break;
			case KOTAK:
				// for (PaymentType pay : supportedPaymentTypes) {
				switch (PaymentType.getInstanceUsingCode(paymentType)) {
				case CREDIT_CARD:
					mopList.addAll(MopType.getKOTAKCCMops());
					break;
				case DEBIT_CARD:
					mopList.addAll(MopType.getKOTAKDCMops());
					break;
				case UPI:
					mopList.addAll(MopType.getKOTAKUPIMops());
					break;
				case NET_BANKING:
					break;
				case WALLET:
					break;
				default:
					break;
				}
				// }
				break;
			case IDBIBANK:
				// for (PaymentType pay : supportedPaymentTypes) {
				switch (PaymentType.getInstanceUsingCode(paymentType)) {
				case CREDIT_CARD:
					mopList.addAll(MopType.getIDBIBANKCCMops());
					break;
				case DEBIT_CARD:
					mopList.addAll(MopType.getIDBIBANKDCMops());
					break;
				case NET_BANKING:
					mopList.addAll(MopType.getIDBIBANKNB());
					break;
				case WALLET:
					break;
				case UPI:
					break;
				default:
					break;
				}
				// }
				break;
			case FSSPAY:
				// for (PaymentType pay : supportedPaymentTypes) {
				switch (PaymentType.getInstanceUsingCode(paymentType)) {
				case CREDIT_CARD:
					mopList.addAll(MopType.getFSSPAYCCMops());
					break;
				case DEBIT_CARD:
					mopList.addAll(MopType.getFSSPAYDCMops());
					break;
				case PREPAID_CARD:
					mopList.addAll(MopType.getFSSPAYPCMops());
					break;
				case NET_BANKING:
					mopList.addAll(MopType.getFSSPAYNBMops());
					break;
				case WALLET:
					mopList.addAll(MopType.getFSSPAYWLMops());
					break;
				case UPI:
					mopList.addAll(MopType.getFSSPAYUPIMops());
					break;
				default:
					break;
				}
				// }
				break;
			case YESBANKCB:
				// for(PaymentType pay:supportedPaymentTypes){
				switch (PaymentType.getInstanceUsingCode(paymentType)) {
				case CREDIT_CARD:
					mopList.addAll(MopType.getCYBERSOURCECCMops());
					break;
				case DEBIT_CARD:
					mopList.addAll(MopType.getCYBERSOURCEDCMops());
					break;
				case UPI:
					mopList.addAll(MopType.getYESBANKCBMops());
					break;
				default:
					break;
				}
				// }
				break;
			case AXISBANKCB:
				// for(PaymentType pay:supportedPaymentTypes){
				switch (PaymentType.getInstanceUsingCode(paymentType)) {
				case CREDIT_CARD:
					mopList.addAll(MopType.getAXISCBCCMops());
					break;
				case DEBIT_CARD:
					mopList.addAll(MopType.getAXISCBDCMops());
					break;
				case UPI:
					break;
				default:
					break;
				}
				// }
				break;
			case IDFCUPI:
				// for(PaymentType pay:supportedPaymentTypes){
				switch (PaymentType.getInstanceUsingCode(paymentType)) {
				case UPI:
					mopList.addAll(MopType.getIDFCUPIMops());
					break;
				default:
					break;
				}
				// }
				break;
			case ICICIUPI:
				// for(PaymentType pay:supportedPaymentTypes){
				switch (PaymentType.getInstanceUsingCode(paymentType)) {
				case UPI:
					mopList.addAll(MopType.getICICIUPIMops());
					break;
				default:
					break;
				}
				// }
				break;
			case AXISMIGS:
				// for (PaymentType pay : supportedPaymentTypes) {
				switch (PaymentType.getInstanceUsingCode(paymentType)) {
				case CREDIT_CARD:
					mopList.addAll(MopType.getMIGSCCMops());
					break;
				case DEBIT_CARD:
					mopList.addAll(MopType.getMIGSDCMops());
					break;
				case NET_BANKING:
					break;
				case WALLET:
					break;
				default:
					break;
				}
				// }
				break;
			case PAYPHI:
				// for (PaymentType pay : supportedPaymentTypes) {
				switch (PaymentType.getInstanceUsingCode(paymentType)) {
				case CREDIT_CARD:
					mopList.addAll(MopType.getPAYPHICCMops());
					break;
				case DEBIT_CARD:
					mopList.addAll(MopType.getPAYPHIDCMops());
					break;
				case NET_BANKING:
					break;
				case WALLET:
					break;
				case UPI:
					break;
				default:
					break;
				}
				// }
				break;
			case CASHFREE:
				// for (PaymentType pay : supportedPaymentTypes) {
				switch (PaymentType.getInstanceUsingCode(paymentType)) {
				case CREDIT_CARD:
					mopList.addAll(MopType.getCASHFREECCMOPMops());
					break;
				case DEBIT_CARD:
					mopList.addAll(MopType.getCASHFREEDCMOPMops());
					break;
				case NET_BANKING:
					mopList.addAll(MopType.getCASHFREENBMops());
					break;
				case WALLET:
					mopList.addAll(MopType.getCASHFREEWLMops());
					break;
				case UPI:
					mopList.addAll(MopType.getCASHFREEUPMops());
					break;
				default:
					break;
				}
				// }
				break;
			case APEXPAY:
				switch (PaymentType.getInstanceUsingCode(paymentType)) {
				case CREDIT_CARD:
					mopList.addAll(MopType.getAPEXPAYCCMOPMops());
					break;
				case DEBIT_CARD:
					mopList.addAll(MopType.getAPEXPAYDCMOPMops());
					break;
				case NET_BANKING:
					mopList.addAll(MopType.getAPEXPAYNBMops());
					break;
				case WALLET:
					mopList.addAll(MopType.getAPEXPAYWLMops());
					break;
				case UPI:
					mopList.addAll(MopType.getAPEXPAYUPMops());
					break;
				default:
					break;
				}
				break;
			case AIRPAY:
				// for (PaymentType pay : supportedPaymentTypes) {
				switch (PaymentType.getInstanceUsingCode(paymentType)) {
				case UPI:
					mopList.addAll(MopType.getAIRPAYUPMOPS());
					break;
				default:
					break;
				}
				// }
				break;
				
			case QAICASH:
				switch (PaymentType.getInstanceUsingCode(paymentType)) {
				case UPI:
					mopList.addAll(MopType.getQAICASHUPMOPS());
					break;
				case NET_BANKING:
					mopList.addAll(MopType.getQAICASHNBMops());
					break;
				default:
					break;
				}
				break;
			case GLOBALPAY:
				switch (PaymentType.getInstanceUsingCode(paymentType)) {
				case UPI:
					mopList.addAll(MopType.getGLOBALPAYUPMOPS());
					break;
				default:
					break;
				}
				break;
			case GREZPAY:
				switch (PaymentType.getInstanceUsingCode(paymentType)) {
				case UPI:
					mopList.addAll(MopType.getGREZPAYUPMOPS());
					break;
				default:
					break;
				}
				break;
			case UPIGATEWAY:
				switch (PaymentType.getInstanceUsingCode(paymentType)) {
				case UPI:
					mopList.addAll(MopType.getUPIGATEWAYUPMOPS());
					break;
				default:
					break;
				}
				break;
			case TOSHANIDIGITAL:
				switch (PaymentType.getInstanceUsingCode(paymentType)) {
				case UPI:
					mopList.addAll(MopType.getTOSHANIDIGITALUPMOPS());
					break;
				default:
					break;
				}
				break;
			case DIGITALSOLUTIONS:
				switch (PaymentType.getInstanceUsingCode(paymentType)) {
				case UPI:
					mopList.addAll(MopType.getDIGITALSOLUPMOPS());
					break;
				default:
					break;
				}
				break;
			case FLOXYPAY:
				switch (PaymentType.getInstanceUsingCode(paymentType)) {
				case UPI:
					mopList.addAll(MopType.getFLOXYPAYUPMops());
					break;
				default:
					break;
				}
				// }
				break;
			case RAZORPAY:
				for (PaymentType pay : supportedPaymentTypes) {
					switch (pay) {
					case CREDIT_CARD:
						mopList.addAll(MopType.getRAZORPAYCCMOPS());
						break;
					case DEBIT_CARD:
						mopList.addAll(MopType.getRAZORPAYDCMOPS());
						break;
					case NET_BANKING:
						mopList.addAll(MopType.getRAZORPAYNBMOPS());
						break;
					case WALLET:
						mopList.addAll(MopType.getRAZORPAYWLMOPS());
						break;
					case UPI:
						mopList.addAll(MopType.getRAZORPAYUPMOPS());
						break;
					default:
						break;
					}
				}
			case VEPAY:
				switch (PaymentType.getInstanceUsingCode(paymentType)) {
				case CREDIT_CARD:
					mopList.addAll(MopType.getVEPAYCCMOPMops());
					break;
				case DEBIT_CARD:
					mopList.addAll(MopType.getVEPAYDCMOPMops());
					break;
				case NET_BANKING:
					mopList.addAll(MopType.getVEPAYNBMops());
					break;
				case WALLET:
					mopList.addAll(MopType.getVEPAYWLMops());
					break;
				case UPI:
					mopList.addAll(MopType.getVEPAYUPMops());
					break;
				default:
					break;
				}
				break;
			case P2PTSP:
				// for(PaymentType pay:supportedPaymentTypes){
				switch (PaymentType.getInstanceUsingCode(paymentType)) {
				case MQR:
					mopList.addAll(MopType.getP2PTSPMQRMOP());
					break;
				default:
					break;
				}
				// }
				break;
			case FONEPAISA:
				switch (PaymentType.getInstanceUsingCode(paymentType)) {
				case CREDIT_CARD:
					mopList.addAll(MopType.getFONEPAISACCMOPMops());
					break;
				default:
					break;
				}
				break;
			default:
				break;
			}

		} catch (Exception e) {
			logger.error("Exception occured in bulkChargesUpdateAction , create List , exception = ", e);
		}
		return mopList;
	}

	public String getBusinessType() {
		return businessType;
	}

	public void setBusinessType(String businessType) {
		this.businessType = businessType;
	}

	public List<Merchants> getListMerchant() {
		return listMerchant;
	}

	public void setListMerchant(List<Merchants> listMerchant) {
		this.listMerchant = listMerchant;
	}

	public String getMerchantList() {
		return merchantList;
	}

	public void setMerchantList(String merchantList) {
		this.merchantList = merchantList;
	}

	public String getAcquirer() {
		return acquirer;
	}

	public void setAcquirer(String acquirer) {
		this.acquirer = acquirer;
	}

	public String getPaymentType() {
		return paymentType;
	}

	public void setPaymentType(String paymentType) {
		this.paymentType = paymentType;
	}

	public String getPaymentRegion() {
		return paymentRegion;
	}

	public void setPaymentRegion(String paymentRegion) {
		this.paymentRegion = paymentRegion;
	}

	public String getOnOff() {
		return onOff;
	}

	public void setOnOff(String onOff) {
		this.onOff = onOff;
	}

	public Map<String, List<BulkChargesObject>> getBulkDataMap() {
		return bulkDataMap;
	}

	public void setBulkDataMap(Map<String, List<BulkChargesObject>> bulkDataMap) {
		this.bulkDataMap = bulkDataMap;
	}

	public boolean isShowMerchant() {
		return showMerchant;
	}

	public void setShowMerchant(boolean showMerchant) {
		this.showMerchant = showMerchant;
	}

	public boolean isShowSaveButton() {
		return showSaveButton;
	}

	public void setShowSaveButton(boolean showSaveButton) {
		this.showSaveButton = showSaveButton;
	}

	public String getSlab() {
		return slab;
	}

	public void setSlab(String slab) {
		this.slab = slab;
	}

	public String getAllDetails() {
		return allDetails;
	}

	public void setAllDetails(String allDetails) {
		this.allDetails = allDetails;
	}

	public String getResponse() {
		return response;
	}

	public void setResponse(String response) {
		this.response = response;
	}

	public Map<String, Object> getAaData() {
		return aaData;
	}

	public void setAaData(Map<String, Object> aaData) {
		this.aaData = aaData;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getOperation() {
		return operation;
	}

	public void setOperation(String operation) {
		this.operation = operation;
	}

	public String getResponseStatus() {
		return responseStatus;
	}

	public void setResponseStatus(String responseStatus) {
		this.responseStatus = responseStatus;
	}

}
