package com.paymentgateway.crm.action;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.paymentgateway.commons.dao.ResellerChargesDao;
import com.paymentgateway.commons.user.AccountCurrencyRegion;
import com.paymentgateway.commons.user.CardHolderType;
import com.paymentgateway.commons.user.Merchants;
import com.paymentgateway.commons.user.ResellerCharges;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.MopType;
import com.paymentgateway.commons.util.PaymentType;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.TransactionType;
import com.paymentgateway.crm.actionBeans.ResellerChargesObject;

/**
 * @author Amitosh Aanand
 *
 */
@SuppressWarnings("unchecked")
public class ResellerChargesUpdateAction extends AbstractSecureAction {

	private static final long serialVersionUID = -2896202621570405334L;

	@Autowired
	private UserDao userDao;

	@Autowired
	private PropertiesManager propertiesManager;

	@Autowired
	private ResellerChargesDao resellerChargesDao;

	private String merchantList;
	private String resellerList;
	private String paymentType;
	private String paymentRegion;
	private String slab;
	private boolean showMerchant;
	private boolean showSaveButton;
	private String allDetails;
	private String response;
	private String chargeFrom;
	private String merchantName;
	private String cardHolderType;
	private boolean chargingDetailsDataFlag;
	private String minSlab;
	private String maxSlab;

	public List<Merchants> listMerchant = new ArrayList<Merchants>();
	private Map<String, List<ResellerChargesObject>> resellerChargesDataMap = new LinkedHashMap<String, List<ResellerChargesObject>>();
	private static Logger logger = LoggerFactory.getLogger(ResellerChargesUpdateAction.class.getName());
	public List<Merchants> listReseller = new ArrayList<Merchants>();
	public List<String> listPaymentType = new ArrayList<String>();

	@SuppressWarnings("static-access")
	public String execute() {
		try {
			if (StringUtils.isNotBlank(resellerList)) {
				if (resellerList.equalsIgnoreCase("ALL")) {
					setListMerchant(userDao.getMerchantList());
				} else {
					String resellerID[] = resellerList.split(",");
					for (int i = 0; i < resellerID.length; i++) {
						listReseller.addAll(userDao.getResellerByResellerId(resellerID[i].trim()));
					}
				}
			} else {
				setResponse("Please select reseller from list");
				return SUCCESS;
			}
			
			if (StringUtils.isNotBlank(merchantList)) {
				if (merchantList.equalsIgnoreCase("ALL")) {
					setListMerchant(userDao.getMerchantList());
				} else {
					String merchantID[] = merchantList.split(",");
					for (int i = 0; i < merchantID.length; i++) {
						listMerchant.addAll(userDao.getMerchantsByPayId(merchantID[i].trim()));
					}
				}
			} else {
				setResponse("Please select merchant from list");
				return SUCCESS;
			}
			
			if(StringUtils.isNotBlank(paymentType)) {
				String paymentTypeID[] = paymentType.split(",");
				for (String pt : paymentTypeID) {
					listPaymentType.add(pt.trim());
				}
			} else {
				setResponse("Please select payment type from list");
				return SUCCESS;
			}
			
			StringBuilder sb = new StringBuilder();
			sb.append(minSlab).append("-").append(maxSlab);
			slab = sb.toString();
			
			if(paymentRegion.equalsIgnoreCase(AccountCurrencyRegion.INTERNATIONAL.toString())) {
				cardHolderType = "Commercial";
			}
			
			String[] merchantListArr = merchantList.split(",");
			String[] paymentTypeListArr = paymentType.split(",");
			String[] cardHolderTypeListArr = cardHolderType.split(",");
			
			for(String merchant : merchantListArr) {
				
				for(String paymentType : paymentTypeListArr) {
					
					if(paymentType.trim().equalsIgnoreCase(PaymentType.WALLET.getCode()) || paymentType.trim().equalsIgnoreCase(PaymentType.NET_BANKING.getCode()) 
							|| paymentType.trim().equalsIgnoreCase(PaymentType.UPI.getCode()) || paymentType.trim().equalsIgnoreCase(PaymentType.COD.getCode())) {
						cardHolderType = "Consumer";
						cardHolderTypeListArr = cardHolderType.split(",");
					}
					
					for(String cardHolderT : cardHolderTypeListArr) {
						
						setShowMerchant(true);
						setShowSaveButton(true);
						setPaymentRegion(paymentRegion);
						setChargeFrom(chargeFrom);
						setMerchantName(userDao.getBusinessNameByPayId(merchant.trim()));
						setCardHolderType(cardHolderT);
						
						
						if (paymentType.trim().equalsIgnoreCase(PaymentType.CREDIT_CARD.getCode())
								|| paymentType.trim().equalsIgnoreCase(PaymentType.DEBIT_CARD.getCode())
								|| paymentType.trim().equalsIgnoreCase(PaymentType.EMI_CC.getCode())
								|| paymentType.trim().equalsIgnoreCase(PaymentType.EMI_DC.getCode())) {

							List<String> mopList = new ArrayList<String>();
							mopList.add(MopType.VISA.getName());
							mopList.add(MopType.MASTERCARD.getName());
							mopList.add(MopType.RUPAY.getName());

							List<String> cardHolderList = new ArrayList<String>();

							if (!StringUtils.isEmpty(paymentRegion)
									&& paymentRegion.equalsIgnoreCase(AccountCurrencyRegion.DOMESTIC.toString())) {
								cardHolderList.add(cardHolderT);
								/*cardHolderList.add("Consumer");
								cardHolderList.add("Commercial");
								cardHolderList.add("Premium");*/
							} else {
								cardHolderList.add("Commercial");
							}

							/*List<String> slabList = new ArrayList<String>();
							if (slab.equalsIgnoreCase("All")) {
								slabList.add("All");
							} else {
								String slabArray[] = slab.split(",");
								for (String slb : slabArray) {
									slabList.add(slb);
								}
							}*/
						
							for (String mop : mopList) {
								List<ResellerChargesObject> objectList = new ArrayList<ResellerChargesObject>();
								for (String cardHolder : cardHolderTypeListArr) {
									//for (String slab : slabList) {
										ResellerChargesObject rco = new ResellerChargesObject();
										rco.setCardHolderType(cardHolder);
										rco.setSlab(slab);
										rco.setResellerPercentage("0.00");
										rco.setResellerFixedCharge("0.00");
										rco.setPgPercentage("0.0");
										rco.setPgFixedCharge("0.0");
										rco.setGst(propertiesManager.propertiesMap.get("SERVICE_TAX"));
										rco.setMaxChargeReseller("0.00");
										rco.setMaxChargePg("0.00");
										rco.setAllowFC(false);
										rco.setSelect(false);
										objectList.add(rco);
									//}
								}
								resellerChargesDataMap.put(/*userDao.getBusinessNameByPayId(*/merchant.trim()/*)*/+"_"+paymentType+"_"+mop, objectList);
							}
						
					} else if (paymentType.trim().equalsIgnoreCase(PaymentType.NET_BANKING.getCode())) {
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
						List<ResellerChargesObject> objectList = new ArrayList<ResellerChargesObject>();
						for (String mop : mopList) {
							for (String slab : slabList) {
								ResellerChargesObject rco = new ResellerChargesObject();
								rco.setCardHolderType(mop);
								rco.setSlab(slab);
								rco.setResellerPercentage("0.00");
								rco.setResellerFixedCharge("0.00");
								rco.setPgPercentage("0.0");
								rco.setPgFixedCharge("0.0");
								rco.setGst(propertiesManager.propertiesMap.get("SERVICE_TAX"));
								rco.setMaxChargeReseller("0.00");
								rco.setMaxChargePg("0.00");
								rco.setAllowFC(false);
								rco.setSelect(false);
								objectList.add(rco);
							}
						}
						resellerChargesDataMap.put(merchant.trim()+"_Net Banking_Consumer", objectList);
						
					} else if (paymentType.trim().equalsIgnoreCase(PaymentType.UPI.getCode())) {
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

						List<ResellerChargesObject> objectList = new ArrayList<ResellerChargesObject>();
						for (String mop : mopList) {
							for (String slab : slabList) {
								ResellerChargesObject rco = new ResellerChargesObject();
								rco.setCardHolderType(mop);
								rco.setSlab(slab);
								rco.setResellerPercentage("0.00");
								rco.setResellerFixedCharge("0.00");
								rco.setPgPercentage("0.0");
								rco.setPgFixedCharge("0.0");
								rco.setGst(propertiesManager.propertiesMap.get("SERVICE_TAX"));
								rco.setMaxChargeReseller("0.00");
								rco.setMaxChargePg("0.00");
								rco.setAllowFC(false);
								rco.setSelect(false);
								objectList.add(rco);
							}
						}
						resellerChargesDataMap.put(merchant.trim()+"_UPI_Consumer", objectList);
					} else if (paymentType.trim().equalsIgnoreCase(PaymentType.WALLET.getCode())) {
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

						List<ResellerChargesObject> objectList = new ArrayList<ResellerChargesObject>();
						for (String mop : mopList) {
							for (String slab : slabList) {
								ResellerChargesObject rco = new ResellerChargesObject();
								rco.setCardHolderType(mop);
								rco.setSlab(slab);
								rco.setResellerPercentage("0.00");
								rco.setResellerFixedCharge("0.00");
								rco.setPgPercentage("0.0");
								rco.setPgFixedCharge("0.0");
								rco.setGst(propertiesManager.propertiesMap.get("SERVICE_TAX"));
								rco.setMaxChargeReseller("0.00");
								rco.setMaxChargePg("0.00");
								rco.setAllowFC(false);
								rco.setSelect(false);
								objectList.add(rco);
							}
						}
						resellerChargesDataMap.put(merchant.trim()+"_Wallet_Consumer", objectList);
					} else if (paymentType.trim().equalsIgnoreCase(PaymentType.COD.getCode())) {
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

						List<ResellerChargesObject> objectList = new ArrayList<ResellerChargesObject>();
						for (String mop : mopList) {
							for (String slab : slabList) {
								ResellerChargesObject rco = new ResellerChargesObject();
								rco.setCardHolderType(mop);
								rco.setSlab(slab);
								rco.setResellerPercentage("0.00");
								rco.setResellerFixedCharge("0.00");
								rco.setPgPercentage("0.0");
								rco.setPgFixedCharge("0.0");
								rco.setGst(propertiesManager.propertiesMap.get("SERVICE_TAX"));
								rco.setMaxChargeReseller("0.00");
								rco.setMaxChargePg("0.00");
								rco.setAllowFC(false);
								rco.setSelect(false);
								objectList.add(rco);
							}
						}
						resellerChargesDataMap.put(merchant.trim()+"_COD_Consumer", objectList);
						//resellerChargesDataMap.put("COD - Consumer", objectList);
					}
				}
			}
			}
			
			/*setListReseller(userDao.getResellerList());
			setShowMerchant(true);
			setShowSaveButton(true);
			setPaymentRegion(paymentRegion);
			setPaymentType(paymentType);
			setChargeFrom(chargeFrom);
			setMerchantName(userDao.getBusinessNameByPayId(merchantList));
			setCardHolderType(cardHolderType);
			if (paymentType.equalsIgnoreCase(PaymentType.CREDIT_CARD.getCode())
					|| paymentType.equalsIgnoreCase(PaymentType.DEBIT_CARD.getCode())
					|| paymentType.equalsIgnoreCase(PaymentType.EMI_CC.getCode())
					|| paymentType.equalsIgnoreCase(PaymentType.EMI_DC.getCode())) {

				List<String> mopList = new ArrayList<String>();
				mopList.add(MopType.VISA.getName());
				mopList.add(MopType.MASTERCARD.getName());
				mopList.add(MopType.RUPAY.getName());

				List<String> cardHolderList = new ArrayList<String>();

				if (!StringUtils.isEmpty(paymentRegion)
						&& paymentRegion.equalsIgnoreCase(AccountCurrencyRegion.DOMESTIC.toString())) {
					cardHolderList.add(cardHolderType);
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
					List<ResellerChargesObject> objectList = new ArrayList<ResellerChargesObject>();
					for (String cardHolder : cardHolderList) {
						for (String slab : slabList) {
							ResellerChargesObject rco = new ResellerChargesObject();
							rco.setCardHolderType(cardHolder);
							rco.setSlab(slab);
							rco.setResellerPercentage("0.00");
							rco.setResellerFixedCharge("0.00");
							rco.setPgPercentage("0.0");
							rco.setPgFixedCharge("0.0");
							rco.setGst(propertiesManager.propertiesMap.get("SERVICE_TAX"));
							rco.setMaxChargeReseller("0.00");
							rco.setMaxChargePg("0.00");
							rco.setAllowFC(false);
							rco.setSelect(false);
							objectList.add(rco);
						}
					}
					resellerChargesDataMap.put(mop, objectList);
				}
			} else if (paymentType.equalsIgnoreCase(PaymentType.NET_BANKING.getCode())) {
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
				List<ResellerChargesObject> objectList = new ArrayList<ResellerChargesObject>();
				for (String mop : mopList) {
					for (String slab : slabList) {
						ResellerChargesObject rco = new ResellerChargesObject();
						rco.setCardHolderType(mop);
						rco.setSlab(slab);
						rco.setResellerPercentage("0.00");
						rco.setResellerFixedCharge("0.00");
						rco.setPgPercentage("0.0");
						rco.setPgFixedCharge("0.0");
						rco.setGst(propertiesManager.propertiesMap.get("SERVICE_TAX"));
						rco.setMaxChargeReseller("0.00");
						rco.setMaxChargePg("0.00");
						rco.setAllowFC(false);
						rco.setSelect(false);
						objectList.add(rco);
					}
				}
				resellerChargesDataMap.put("Net Banking - Consumer", objectList);
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

				List<ResellerChargesObject> objectList = new ArrayList<ResellerChargesObject>();
				for (String mop : mopList) {
					for (String slab : slabList) {
						ResellerChargesObject rco = new ResellerChargesObject();
						rco.setCardHolderType(mop);
						rco.setSlab(slab);
						rco.setResellerPercentage("0.00");
						rco.setResellerFixedCharge("0.00");
						rco.setPgPercentage("0.0");
						rco.setPgFixedCharge("0.0");
						rco.setGst(propertiesManager.propertiesMap.get("SERVICE_TAX"));
						rco.setMaxChargeReseller("0.00");
						rco.setMaxChargePg("0.00");
						rco.setAllowFC(false);
						rco.setSelect(false);
						objectList.add(rco);
					}
				}
				resellerChargesDataMap.put("UPI - Consumer", objectList);
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

				List<ResellerChargesObject> objectList = new ArrayList<ResellerChargesObject>();
				for (String mop : mopList) {
					for (String slab : slabList) {
						ResellerChargesObject rco = new ResellerChargesObject();
						rco.setCardHolderType(mop);
						rco.setSlab(slab);
						rco.setResellerPercentage("0.00");
						rco.setResellerFixedCharge("0.00");
						rco.setPgPercentage("0.0");
						rco.setPgFixedCharge("0.0");
						rco.setGst(propertiesManager.propertiesMap.get("SERVICE_TAX"));
						rco.setMaxChargeReseller("0.00");
						rco.setMaxChargePg("0.00");
						rco.setAllowFC(false);
						rco.setSelect(false);
						objectList.add(rco);
					}
				}
				resellerChargesDataMap.put("Wallet - Consumer", objectList);
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

				List<ResellerChargesObject> objectList = new ArrayList<ResellerChargesObject>();
				for (String mop : mopList) {
					for (String slab : slabList) {
						ResellerChargesObject rco = new ResellerChargesObject();
						rco.setCardHolderType(mop);
						rco.setSlab(slab);
						rco.setResellerPercentage("0.00");
						rco.setResellerFixedCharge("0.00");
						rco.setPgPercentage("0.0");
						rco.setPgFixedCharge("0.0");
						rco.setGst(propertiesManager.propertiesMap.get("SERVICE_TAX"));
						rco.setMaxChargeReseller("0.00");
						rco.setMaxChargePg("0.00");
						rco.setAllowFC(false);
						rco.setSelect(false);
						objectList.add(rco);
					}
				}
				resellerChargesDataMap.put("COD - Consumer", objectList);
			}*/
		} catch (Exception e) {
			logger.error("Exception Caught ", e);
			return ERROR;
		}
		return SUCCESS;
	}

	public String udpateValues() {
		try {
			String currency = "356";
			TransactionType transactionType = TransactionType.SALE;
			//String[] paymentTypeIns = paymentType.split("\\,");
			AccountCurrencyRegion acr = null;
			if (paymentRegion.equalsIgnoreCase("DOMESTIC")) {
				acr = AccountCurrencyRegion.DOMESTIC;
			} else {
				acr = AccountCurrencyRegion.INTERNATIONAL;
			}
			List<String> resellerIdList = new ArrayList<>();
			if (resellerList.equalsIgnoreCase("All")) {
				List<Merchants> resellerList = userDao.getResellerList();
				for (Merchants reseller : resellerList) {
					resellerIdList.add(reseller.getResellerId());
				}
			} else {
				String resellerListArray[] = resellerList.split(",");
				for (String resellerId : resellerListArray) {
					resellerIdList.add(resellerId);
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
			List<String> paymentTypeList = new ArrayList<String>();
			
				String paymentTypeListArray[] = paymentType.split(",");
				for (String pT : paymentTypeListArray) {
					paymentTypeList.add(/*PaymentType.getInstanceUsingCode*/pT);
				}
			
			String allDetailsArray[] = allDetails.replace(" ;", ";").replace("_", ",").split(";");
			StringBuilder successfulUpdate = new StringBuilder();
			StringBuilder unsuccessfulUpdate = new StringBuilder();
			
			
			
			
			for (String details : allDetailsArray) {
				
				paymentTypeList.clear();
				payIdList.clear();
				String detailsArray[] = details.split(",");
				User user = userDao.findPayId(detailsArray[7]);
				payIdList.add(detailsArray[7]);
				paymentTypeList.add(detailsArray[8]);
				
			for (String payId : payIdList) {
				//User user = userDao.findPayId(payId);
				for (String resellerId : resellerIdList) {
					for(String paymentType : paymentTypeList) {
						if(paymentType.equalsIgnoreCase("Net Banking") 
								|| paymentType.equalsIgnoreCase("Wallet")
								|| paymentType.equalsIgnoreCase("UPI")
								|| paymentType.equalsIgnoreCase("COD")) {
							paymentType = PaymentType.getCodeUsingInstance(paymentType);
						}
					//User user = userDao.findPayId(payId);
					//for (String details : allDetailsArray) {
						//String detailsArray[] = details.split(",");
						CardHolderType cardHolderType = null;
						MopType mopType = null;
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
								// For other payment modes, default type is consumer
								cardHolderType = CardHolderType.CONSUMER;
								mopType = MopType.getInstanceIgnoreCase(detailsArray[0]);
							}
						}
						if (paymentType.equalsIgnoreCase(PaymentType.CREDIT_CARD.getCode())
								|| paymentType.equalsIgnoreCase(PaymentType.DEBIT_CARD.getCode())
								|| paymentType.equalsIgnoreCase(PaymentType.PREPAID_CARD.getCode())
								|| paymentType.equalsIgnoreCase(PaymentType.EMI_CC.getCode())
								|| paymentType.equalsIgnoreCase(PaymentType.EMI_DC.getCode())) {
							mopType = MopType.getInstanceIgnoreCase(detailsArray[9]);
						}
						// Populate Slab List
						List<String> slabList = new ArrayList<String>();
						if (detailsArray[1].equalsIgnoreCase("ALL")) {
							slabList.add("0.01-1000.00");
							slabList.add("1000.01-2000.00");
							slabList.add("2000.01-1000000.00");
						} else {
							String slbArray[] = detailsArray[1].split(",");
							for (String item : slbArray) {
								slabList.add(item);
							}
						}
						for (String slb : slabList) {
							String slabArray[] = slb.split("-");
							String minTxnAmount = slabArray[0];
							String maxTxnAmount = slabArray[1];
							String slabId = null;
							slabId = slb;
							/*if (minTxnAmount.equalsIgnoreCase("0.01")) {
								slabId = "01";
							} else if (minTxnAmount.equalsIgnoreCase("1000.01")) {
								slabId = "02";
							} else {
								slabId = "03";
							}*/
							ResellerCharges rCFromDB = resellerChargesDao.findDetailForUpdate(payId, resellerId, slabId,
									PaymentType.getInstanceUsingCode(paymentType), mopType, transactionType, acr, cardHolderType, currency);
							if (rCFromDB != null) {
								updateBulkData(slb, detailsArray, user, false, rCFromDB, mopType, cardHolderType,
										resellerId, chargeFrom, paymentType);
							} else {
								updateBulkData(slb, detailsArray, user, true, rCFromDB, mopType, cardHolderType,
										resellerId, chargeFrom, paymentType);
							}
						}
					}
					
				}
				}
			successfulUpdate.append(", " + user.getBusinessName());
			}
			StringBuilder finalResponse = new StringBuilder();
			if (successfulUpdate.length() > 0) {
				finalResponse.append("Charges updated successfully for: " + successfulUpdate.substring(2) + "\n");
			}
			if (unsuccessfulUpdate.length() > 0) {
				finalResponse.append("Charges not updated for: " + unsuccessfulUpdate.substring(2) + "\n");
			}
			setResponse(finalResponse.toString());
			return SUCCESS;
		} catch (Exception e) {
			logger.error("Exception in updating reseller charges", e);
			setResponse("Unable to update reseller charges");
			return ERROR;
		}
	}

	public void updateBulkData(String slb, String detailsArray[], User merchant, boolean newEntry,
			ResellerCharges rcFromDB, MopType mopType, CardHolderType cardHolderType, String resellerId, String chargeFrom, String paymentType) {
		try {
			PaymentType paymentTypeIns = PaymentType.getInstanceUsingCode(paymentType);
			AccountCurrencyRegion acr = null;
			if (paymentRegion.equalsIgnoreCase("DOMESTIC")) {
				acr = AccountCurrencyRegion.DOMESTIC;
			} else {
				acr = AccountCurrencyRegion.INTERNATIONAL;
			}
			String currency = "356";

			String resellerPercentage = detailsArray[2];
			String resellerFC = detailsArray[3];
			String pgPercentage = detailsArray[4];
			String pgFC = detailsArray[5];
			String serviceTax = detailsArray[6];

			User sessionUser = (User) sessionMap.get(Constants.USER.getValue());
			String loginUserEmailId = sessionUser.getEmailId();
			String userType = sessionUser.getUserType().name().toString();

			/*String slabArray[] = slb.split("-");

			String minTxnAmount = slabArray[0];

			String slabId = null;

			if (minTxnAmount.equalsIgnoreCase("0.01")) {
				slabId = "01";
			} else if (minTxnAmount.equalsIgnoreCase("1000.01")) {
				slabId = "02";
			} else {
				slabId = "03";
			}*/

			if (newEntry == false) {
				ResellerCharges resellerCharges = SerializationUtils.clone(rcFromDB);
				resellerCharges.setResellerPercentage(resellerPercentage);
				resellerCharges.setResellerFixedCharge(resellerFC);
				resellerCharges.setPgPercentage(pgPercentage);
				resellerCharges.setPgFixedCharge(pgFC);
				resellerCharges.setGst(serviceTax);
				resellerCharges.setMopType(mopType);
				resellerCharges.setResellerId(resellerId);
				resellerCharges.setChargeFrom(chargeFrom);
				resellerChargesDao.editChargingDetail(merchant.getEmailId(), resellerCharges, rcFromDB, true,
						userType, loginUserEmailId);
			}

			if (newEntry == true) {
				ResellerCharges newResellerCharges = new ResellerCharges();
				newResellerCharges.setResellerPercentage(resellerPercentage);
				newResellerCharges.setResellerFixedCharge(resellerFC);
				newResellerCharges.setPgPercentage(pgPercentage);
				newResellerCharges.setPgFixedCharge(pgFC);
				newResellerCharges.setMopType(mopType);
				newResellerCharges.setResellerId(resellerId);
				newResellerCharges.setGst(serviceTax);
				newResellerCharges.setCardHolderType(cardHolderType);				
				newResellerCharges.setTransactionType(TransactionType.SALE);
				newResellerCharges.setCurrency(currency);
				newResellerCharges.setMerchantPayId(merchant.getPayId());
				newResellerCharges.setPaymentsRegion(acr);
				newResellerCharges.setPaymentType(paymentTypeIns);
				newResellerCharges.setSlabId(slb);
				newResellerCharges.setStatus("ACTIVE");
				newResellerCharges.setCreatedBy(sessionUser.getEmailId());
				newResellerCharges.setCreatedDate(new Date());
				newResellerCharges.setChargeFrom(chargeFrom);
				resellerChargesDao.create(newResellerCharges);
			}
		} catch (Exception exception) {
			setResponse("Unable to update reseller charges");
			logger.error("Exception", exception);
		}
	}

	public String checkPaymentTypeMaped() {
		
		if (merchantList != null && paymentType != null) {
			setChargingDetailsDataFlag(resellerChargesDao.getChargingDetailsMap(merchantList, paymentType, paymentRegion));
			if(chargingDetailsDataFlag == true){
			} else {
				setResponse("Payment Option not Mapped");
				return SUCCESS;
			}
		}else {
			if (merchantList != null)
				setResponse("Select The merchant");
		}
		return SUCCESS;
	}
	
	public boolean isChargingDetailsDataFlag() {
		return chargingDetailsDataFlag;
	}

	public void setChargingDetailsDataFlag(boolean chargingDetailsDataFlag) {
		this.chargingDetailsDataFlag = chargingDetailsDataFlag;
	}

	public PropertiesManager getPropertiesManager() {
		return propertiesManager;
	}

	public void setPropertiesManager(PropertiesManager propertiesManager) {
		this.propertiesManager = propertiesManager;
	}

	public List<Merchants> getListMerchant() {
		return listMerchant;
	}

	public void setListMerchant(List<Merchants> listMerchant) {
		this.listMerchant = listMerchant;
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

	public String getSlab() {
		return slab;
	}

	public void setSlab(String slab) {
		this.slab = slab;
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

	public String getMerchantList() {
		return merchantList;
	}

	public void setMerchantList(String merchantList) {
		this.merchantList = merchantList;
	}

	public String getResellerList() {
		return resellerList;
	}

	public void setResellerList(String resellerList) {
		this.resellerList = resellerList;
	}

	public List<Merchants> getListReseller() {
		return listReseller;
	}

	public void setListReseller(List<Merchants> listReseller) {
		this.listReseller = listReseller;
	}

	public Map<String, List<ResellerChargesObject>> getResellerChargesDataMap() {
		return resellerChargesDataMap;
	}

	public void setResellerChargesDataMap(Map<String, List<ResellerChargesObject>> resellerChargesDataMap) {
		this.resellerChargesDataMap = resellerChargesDataMap;
	}
	public String getChargeFrom() {
		return chargeFrom;
	}
	
	public void setChargeFrom(String chargeFrom) {
		this.chargeFrom = chargeFrom;
	}
	public String getMerchantName() {
		return merchantName;
	}

	public void setMerchantName(String merchantName) {
		this.merchantName = merchantName;
	}
	public String getCardHolderType() {
		return cardHolderType;
	}

	public void setCardHolderType(String cardHolderType) {
		this.cardHolderType = cardHolderType;
	}
	public String getMinSlab() {
		return minSlab;
	}

	public void setMinSlab(String minSlab) {
		this.minSlab = minSlab;
	}

	public String getMaxSlab() {
		return maxSlab;
	}

	public void setMaxSlab(String maxSlab) {
		this.maxSlab = maxSlab;
	}
	public List<String> getListPaymentType() {
		return listPaymentType;
	}

	public void setListPaymentType(List<String> listPaymentType) {
		this.listPaymentType = listPaymentType;
	}
}
