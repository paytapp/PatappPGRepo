package com.paymentgateway.crm.action;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.paymentgateway.commons.action.AbstractSecureAction;
import com.paymentgateway.commons.dao.SUFDetailDao;
import com.paymentgateway.commons.dao.UserSettingDao;
import com.paymentgateway.commons.user.Merchants;
import com.paymentgateway.commons.user.SUFDetail;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.user.UserSettingData;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.CrmValidator;
import com.paymentgateway.commons.util.DataEncoder;
import com.paymentgateway.commons.util.MopType;
import com.paymentgateway.commons.util.PaymentType;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.StatusType;

/**
 * Amitosh, Rajit, Sandeep, Shiva
 */
public class SUFDetailAction extends AbstractSecureAction {

	private static final long serialVersionUID = 5673581505948436727L;
	private static Logger logger = LoggerFactory.getLogger(SUFDetailAction.class.getName());

	@Autowired
	UserDao userDao;

	@Autowired
	CrmValidator validator;

	@Autowired
	SUFDetailDao sufDetailDao;

	@Autowired
	DataEncoder dataEncoder;
	
	@Autowired
	private UserSettingDao userSettingDao;

	@Autowired
	PropertiesManager propertiesManager;

	private String payId;
	private String subMerchantPayId;
	private String merchantName;
	private String txnType;
	private String paymentType;
	private String fixedCharge;
	private String percentageAmount;
	private String mopType;
	private String paymentRegion;
	private String slab;
	private String minSlab;
	private String maxSlab;
	private String merchantSelectionType;

	private List<String> mopList = new ArrayList<String>();

	private List<SUFDetail> aaData = new ArrayList<SUFDetail>();
	private User sessionUser = new User();
	private String loginUserEmailId;
	private String response;
	private String responseValue;

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public String execute() {

		logger.info("inside execute method for view suf charges");
		List<SUFDetail> finalList = new ArrayList<SUFDetail>();
		if (!payId.equalsIgnoreCase("ALL")) {
			try {
				// List<SUFDetail> unique = new ArrayList<SUFDetail>();
				paymentType = PaymentType.getpaymentName(paymentType);
				if (txnType.equalsIgnoreCase("eNACH")) {

					paymentRegion = "Domestic";
				}
				if (txnType.equalsIgnoreCase("Merchant Txn Email") || txnType.equalsIgnoreCase("Merchant Txn SMS")
						|| txnType.equalsIgnoreCase("Customer Txn Email")
						|| txnType.equalsIgnoreCase("Customer Txn SMS") || txnType.equalsIgnoreCase("ePOS")
						|| txnType.equalsIgnoreCase("Invoice Payments") || txnType.equalsIgnoreCase("IMPS Payout")
						|| txnType.equalsIgnoreCase("UPI Payout")) {

					paymentRegion = "Domestic";
					paymentType = "NA";
				}

				finalList=sufDetailDao.fetchDataPerPayId(payId, txnType, paymentType, paymentRegion,
						subMerchantPayId);
				/*
				 * for (SUFDetail suf : unique) {
				 * 
				 * Map<String, String> map = new HashMap<String, String>();
				 * map.put("payId", suf.getPayId()); map.put("merchantName",
				 * suf.getMerchantName()); map.put("fixedCharge",
				 * suf.getFixedCharge()); map.put("mopType", suf.getMopType());
				 * map.put("paymentType", suf.getPaymentType());
				 * map.put("percentageAmount", suf.getPercentageAmount());
				 * map.put("txnType", suf.getTxnType());
				 * map.put("paymentRegion", suf.getPaymentRegion());
				 * map.put("slab", suf.getSlab()); finalList.add(map); }
				 */
			} catch (ClassCastException cce) {
				logger.error(
						"caught ClassCast exception in execute method while fetch suf charges for single merchant from DB",
						cce);
			} catch (NullPointerException npe) {
				logger.info(
						"caught null pointer in execute method while fetch suf charges for single merchant from DB ",
						npe);
			} catch (Exception e) {
				logger.info("caught execute while fetch suf charges for single merchant from DB ", e);
			}
		} else {

			try {

				// List<SUFDetail> unique = new ArrayList<SUFDetail>();

				/*
				 * if(!paymentType.equals(PaymentType.COD.getCode())) {
				 * paymentType = PaymentType.getpaymentName(paymentType); }
				 */
				paymentType = PaymentType.getpaymentName(paymentType);
				if (txnType.equalsIgnoreCase("eNACH")) {

					paymentRegion = "Domestic";
				}
				if (txnType.equalsIgnoreCase("Merchant Txn Email") || txnType.equalsIgnoreCase("Merchant Txn SMS")
						|| txnType.equalsIgnoreCase("Customer Txn Email")
						|| txnType.equalsIgnoreCase("Customer Txn SMS") || txnType.equalsIgnoreCase("ePOS")
						|| txnType.equalsIgnoreCase("Invoice Payments") || txnType.equalsIgnoreCase("IMPS Payout")
						|| txnType.equalsIgnoreCase("UPI Payout")) {

					paymentRegion = "Domestic";
					paymentType = "NA";
				}
				finalList=sufDetailDao.fetchDataByTxnTypeAndPaymentType(txnType, paymentType, paymentRegion);
				/*
				 * if (unique != null) { for (SUFDetail suf : unique) {
				 * 
				 * Map<String, String> map = new HashMap<String, String>();
				 * map.put("payId", suf.getPayId()); map.put("merchantName",
				 * suf.getMerchantName()); map.put("fixedCharge",
				 * suf.getFixedCharge()); map.put("mopType", suf.getMopType());
				 * map.put("paymentType", suf.getPaymentType());
				 * map.put("percentageAmount", suf.getPercentageAmount());
				 * map.put("paymentRegion", suf.getPaymentRegion());
				 * map.put("txnType", suf.getTxnType()); map.put("slab",
				 * suf.getSlab()); finalList.add(map); } }
				 */
			} catch (ClassCastException cce) {
				logger.error(
						"Caught classCast exception in execute method while fetch suf charges by transactionType and paymentType from DB",
						cce);
			} catch (NullPointerException npe) {
				logger.error(
						"Caught null exception in execute method while fetch suf charges by transactionType and paymentType from DB",
						npe);
			} catch (Exception e) {
				logger.info("caught execute while fetch suf charges for single merchant from DB ", e);
			}

		}
		setAaData(finalList);

		return SUCCESS;
	}

	public String deleteSUFDetail() {

		String response;
		sessionUser = (User) sessionMap.get(Constants.USER.getValue());
		loginUserEmailId = sessionUser.getEmailId();

		response = sufDetailDao.deleteSufDetail(payId, loginUserEmailId, txnType, paymentType, mopType, paymentRegion, slab, subMerchantPayId);

		if (StringUtils.isNotBlank(response) && response.equalsIgnoreCase(SUCCESS)) {
			setResponse("SUF Detail Deleted Successfully");
		} else {
			setResponse("SUF Detail Not Deleted");
		}
		return SUCCESS;
	}

	@SuppressWarnings("unchecked")
	public String saveSUFDetail() {
		User user = new User();
		sessionUser = (User) sessionMap.get(Constants.USER.getValue());
		loginUserEmailId = sessionUser.getEmailId();
		String response;

		if (txnType.equalsIgnoreCase("eNACH")) {

			paymentRegion = "Domestic";
			minSlab = "1.00";
			maxSlab = "10000000.00";
			mopType = "All";

		}

		else if (txnType.equalsIgnoreCase("Merchant Txn Email") || txnType.equalsIgnoreCase("Merchant Txn SMS")
				|| txnType.equalsIgnoreCase("Customer Txn Email") || txnType.equalsIgnoreCase("Customer Txn SMS")
				|| txnType.equalsIgnoreCase("ePOS") || txnType.equalsIgnoreCase("Invoice Payments")
				|| txnType.equalsIgnoreCase("IMPS Payout") || txnType.equalsIgnoreCase("UPI Payout")) {

			paymentRegion = "Domestic";
			minSlab = "1.00";
			maxSlab = "10000000.00";
			mopType = "NA";
			paymentType = "NA";

			if (payId.equalsIgnoreCase("ALL")) {
				try {
					logger.info("Inside SufDetailAction() for checking flags");
					List<Merchants> merchantsList = userDao.getMerchantActiveList();
					if (!merchantsList.isEmpty()) {
						for (Merchants merchant : merchantsList) {
							user = userDao.findPayId(merchant.getPayId());
							UserSettingData merchantSettings=userSettingDao.fetchDataUsingPayId(merchant.getPayId());
							if (txnType.equalsIgnoreCase("Merchant Txn Email")) {
								if (!user.isTransactionEmailerFlag() || !user.isTransactionFailedMerchantEmailFlag()
										|| !user.isRefundTransactionMerchantEmailFlag()) {
									setResponse("Please activate the Merchant Txn Email Flags for "
											+ user.getBusinessName());
									setResponseValue(Constants.FAIL.getValue());
									return INPUT;
								}
							} else if (txnType.equalsIgnoreCase("Merchant Txn SMS")) {
								if (!user.isTransactionMerchantSMSFlag() || !user.isTransactionFailedMerchantSMSFlag()
										|| !user.isTransactionRefundMerchantSMSFlag()) {
									setResponse(
											"Please activate the Merchant Txn SMS Flags for " + user.getBusinessName());
									setResponseValue(Constants.FAIL.getValue());
									return INPUT;
								}
							} else if (txnType.equalsIgnoreCase("Customer Txn Email")) {
								if (!user.isTransactionCustomerEmailFlag()
										|| !user.isTransactionFailedCustomerEmailFlag()
										|| !user.isRefundTransactionCustomerEmailFlag()) {
									setResponse("Please activate the Customer Txn Email Flags for "
											+ user.getBusinessName());
									setResponseValue(Constants.FAIL.getValue());
									return INPUT;
								}
							} else if (txnType.equalsIgnoreCase("Customer Txn SMS")) {
								if (!user.isTransactionCustomerSMSFlag() || !user.isTransactionFailedCustomerSMSFlag()
										|| !user.isTransactionRefundCustomerSMSFlag()) {
									setResponse(
											"Please activate the Customer Txn SMS Flags for " + user.getBusinessName());
									setResponseValue(Constants.FAIL.getValue());
									return INPUT;
								}
							} else if (txnType.equalsIgnoreCase("ePOS")) {
								if (!merchantSettings.isEposMerchant()) {
									setResponse(user.getBusinessName() + "is not an ePOS Merchant");
									setResponseValue(Constants.FAIL.getValue());
									return INPUT;
								}
							} else if (txnType.equalsIgnoreCase("IMPS Payout")
									|| txnType.equalsIgnoreCase("UPI Payout")) {
								if (!merchantSettings.isEposMerchant()) {
									setResponse("Please activate the Payout Flag for " + user.getBusinessName());
									setResponseValue(Constants.FAIL.getValue());
									return INPUT;
								}
							}

						}
					} else {
						setResponse("No Merchant Found");
						setResponseValue(Constants.FAIL.getValue());
					}
				} catch (Exception e) {
					logger.error("Exception ", e);
					setResponse("SUF Detail Not Saved");
					setResponseValue(Constants.FAIL.getValue());
				}
			} else {

				try {
					String merchantId[] = payId.split(",");
					for (int i = 0; i < merchantId.length; i++) {
						user = userDao.findPayId(merchantId[i]);

						UserSettingData merchantSettings=userSettingDao.fetchDataUsingPayId(user.getPayId());
						
						if (txnType.equalsIgnoreCase("Merchant Txn Email")) {
							if (!user.isTransactionEmailerFlag() || !user.isTransactionFailedMerchantEmailFlag()
									|| !user.isRefundTransactionMerchantEmailFlag()) {
								setResponse(
										"Please activate the Merchant Txn Email Flags for " + user.getBusinessName());
								setResponseValue(Constants.FAIL.getValue());
								return INPUT;
							}
						} else if (txnType.equalsIgnoreCase("Merchant Txn SMS")) {
							if (!user.isTransactionMerchantSMSFlag() || !user.isTransactionFailedMerchantSMSFlag()
									|| !user.isTransactionRefundMerchantSMSFlag()) {
								setResponse("Please activate the Merchant Txn SMS Flags for " + user.getBusinessName());
								setResponseValue(Constants.FAIL.getValue());
								return INPUT;
							}
						} else if (txnType.equalsIgnoreCase("Customer Txn Email")) {
							if (!user.isTransactionCustomerEmailFlag() || !user.isTransactionFailedCustomerEmailFlag()
									|| !user.isRefundTransactionCustomerEmailFlag()) {
								setResponse(
										"Please activate the Customer Txn Email Flags for " + user.getBusinessName());
								setResponseValue(Constants.FAIL.getValue());
								return INPUT;
							}
						} else if (txnType.equalsIgnoreCase("Customer Txn SMS")) {
							if (!user.isTransactionCustomerSMSFlag() || !user.isTransactionFailedCustomerSMSFlag()
									|| !user.isTransactionRefundCustomerSMSFlag()) {
								setResponse("Please activate the Customer Txn SMS Flags for " + user.getBusinessName());
								setResponseValue(Constants.FAIL.getValue());
								return INPUT;
							}
						} else if (txnType.equalsIgnoreCase("ePOS")) {
							if (!merchantSettings.isEposMerchant()) {
								setResponse(user.getBusinessName() + "is not an ePOS Merchant");
								setResponseValue(Constants.FAIL.getValue());
								return INPUT;
							}
						} else if (txnType.equalsIgnoreCase("IMPS Payout") || txnType.equalsIgnoreCase("UPI Payout")) {
							if (!merchantSettings.isEposMerchant()) {
								setResponse("Please activate the Payout Flag for " + user.getBusinessName());
								setResponseValue(Constants.FAIL.getValue());
								return INPUT;
							}
						}
					}

				} catch (Exception e) {
					logger.error("Exception ", e);
				}

			}

		} else {

			if (!minSlab.contains(".")) {
				minSlab = minSlab + ".00";
			} else {
				String minSlabArr[] = minSlab.split("\\.");
				if (!(minSlabArr[1].length() == 2)) {
					minSlab = minSlab + "0";
				}
			}

			if (!maxSlab.contains(".")) {
				maxSlab = maxSlab + ".00";
			} else {
				String maxSlabArr[] = maxSlab.split("\\.");
				if (!(maxSlabArr[1].length() == 2)) {
					maxSlab = maxSlab + "0";
				}
			}
		}

		StringBuilder slabBuilder = new StringBuilder();
		slabBuilder.append(minSlab);
		slabBuilder.append("-");
		slabBuilder.append(maxSlab);
		slab = slabBuilder.toString();
		if (StringUtils.isBlank(fixedCharge) || fixedCharge.equalsIgnoreCase(".")) {
			fixedCharge = "0.00";
		}
		if (StringUtils.isBlank(percentageAmount) || percentageAmount.equalsIgnoreCase(".")) {
			percentageAmount = "0.00";
		}

		response = sufDetailDao.createNewSufDetail(payId, merchantName, txnType, paymentType, fixedCharge, mopType,
				percentageAmount, loginUserEmailId, paymentRegion, slab, subMerchantPayId);

		if (StringUtils.isNotBlank(response) && response.equalsIgnoreCase(SUCCESS)) {
			setResponse("SUF Detail Saved Successfully");
			setResponseValue(Constants.SUCCESS.getValue());

		} else if (StringUtils.isNotBlank(response) && response.equalsIgnoreCase(StatusType.DUPLICATE.getName())) {
			setResponse("SUF Detail Already Exist");
			setResponseValue(Constants.FAIL.getValue());

		} else {
			setResponse("SUF Detail Not Saved");
			setResponseValue(Constants.FAIL.getValue());
		}
		return SUCCESS;
	}

	public String updateSUFDetail() {
		String response;
		sessionUser = (User) sessionMap.get(Constants.USER.getValue());
		loginUserEmailId = sessionUser.getEmailId();
		response = sufDetailDao.updateSUFDetailPerPayId(payId, merchantName, txnType, paymentType, fixedCharge, mopType,
				percentageAmount, loginUserEmailId, paymentRegion, slab, subMerchantPayId);

		if (StringUtils.isNotBlank(response) && response.equalsIgnoreCase(SUCCESS)) {
			setResponse("SUF Detail Updated Successfully");
			setResponseValue(Constants.SUCCESS.getValue());
		} else {
			setResponse("SUF Detail Not Updated");
			setResponseValue(Constants.FAIL.getValue());
		}
		return SUCCESS;
	}

	@SuppressWarnings({ "static-access" })
	public String getMopTypeByPaymentType() {

		List<String> finalList = new ArrayList<String>();

		/*
		 * if(paymentType.equals("eNachRegistration") ||
		 * paymentType.equals("eNachTransaction")) {
		 * 
		 * } else
		 */if (PaymentType.getInstanceUsingCode(
				paymentType) == null /*
										 * || !paymentType.equals(
										 * "eNachRegistration") ||
										 * !paymentType.equals(
										 * "eNachTransaction")
										 */) {

			return INPUT;
		}

		if (paymentType.equals("IN")) {

			List<String> mopList = new ArrayList<String>();
			String[] mopTypeCC = propertiesManager.propertiesMap.get("CCMOP").split(",");
			String[] mopTypeDC = propertiesManager.propertiesMap.get("DCMOP").split(",");

			for (int i = 0; i < mopTypeCC.length; i++) {
				mopList.add(mopTypeCC[i]);
			}
			for (int i = 0; i < mopTypeDC.length; i++) {
				if (!mopList.contains(mopTypeDC[i])) {
					mopList.add(mopTypeDC[i]);
				}
			}
			for (String mop : mopList) {
				finalList.add(MopType.getmopName(mop));
			}

		} else if (paymentType.equals("WL") || paymentType.equals("UP")) {

			paymentType = PaymentType.getpaymentName(paymentType);
			String mopType[] = propertiesManager.propertiesMap.get(paymentType.toUpperCase()).split(",");
			for (int i = 0; i < mopType.length; i++) {
				String mopTypeString = MopType.getmopName(mopType[i]);
				if (mopTypeString != "") {
					finalList.add(mopTypeString);
				}
			}

		} else if (paymentType.equals("NEFT") || paymentType.equals("RTGS") || paymentType.equals("IMPS")) {

			paymentType = PaymentType.getpaymentName(paymentType);
			finalList.add(paymentType);

		} else {
			if (paymentType.equals("EMCC") || paymentType.equals("EMDC")) {
				paymentType = paymentType.replace("EM", "");
			}
			if (paymentType.equals("CD")) {
				paymentType = PaymentType.getpaymentName(paymentType);
			}
			if (paymentType.equals("NEFT")) {
				paymentType = PaymentType.getpaymentName(paymentType);
			}
			if (paymentType.equals("IMPS")) {
				paymentType = PaymentType.getpaymentName(paymentType);
			}
			if (paymentType.equals("RTGS")) {
				paymentType = PaymentType.getpaymentName(paymentType);
			}
			if (paymentType.equals("eNachRegistration") || paymentType.equals("eNachTransaction")) {

				String[] dcMopType = propertiesManager.propertiesMap.get("DC" + "MOP").split(",");
				for (int i = 0; i < dcMopType.length; i++) {
					finalList.add(MopType.getmopName(dcMopType[i]));
				}

				String[] nbMopType = propertiesManager.propertiesMap.get("NB" + "MOP").split(",");
				for (int i = 0; i < nbMopType.length; i++) {
					finalList.add(MopType.getmopName(nbMopType[i]));
				}

			} else {
				String[] mopType = propertiesManager.propertiesMap.get(paymentType + "MOP").split(",");
				for (int i = 0; i < mopType.length; i++) {
					finalList.add(MopType.getmopName(mopType[i]));
				}
			}
		}
		setMopList(finalList);
		return SUCCESS;
	}

	public String fetchAllActiveSufDetail() {

		setAaData(sufDetailDao.getAllActiveSufDetails());
		return SUCCESS;
	}

	public String getTxnType() {
		return txnType;
	}

	public void setTxnType(String txnType) {
		this.txnType = txnType;
	}

	public List<SUFDetail> getAaData() {
		return aaData;
	}

	public void setAaData(List<SUFDetail> aaData) {
		this.aaData = aaData;
	}

	public String getLoginUserEmailId() {
		return loginUserEmailId;
	}

	public void setLoginUserEmailId(String loginUserEmailId) {
		this.loginUserEmailId = loginUserEmailId;
	}

	public String getPayId() {
		return payId;
	}

	public void setPayId(String payId) {
		this.payId = payId;
	}

	public String getMerchantName() {
		return merchantName;
	}

	public void setMerchantName(String merchantName) {
		this.merchantName = merchantName;
	}

	public String getPaymentType() {
		return paymentType;
	}

	public void setPaymentType(String paymentType) {
		this.paymentType = paymentType;
	}

	public String getPercentageAmount() {
		return percentageAmount;
	}

	public void setPercentageAmount(String percentageAmount) {
		this.percentageAmount = percentageAmount;
	}

	public String getMopType() {
		return mopType;
	}

	public void setMopType(String mopType) {
		this.mopType = mopType;
	}

	public String getFixedCharge() {
		return fixedCharge;
	}

	public void setFixedCharge(String fixedCharge) {
		this.fixedCharge = fixedCharge;
	}

	public List<String> getMopList() {
		return mopList;
	}

	public void setMopList(List<String> mopList) {
		this.mopList = mopList;
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

	public String getResponse() {
		return response;
	}

	public void setResponse(String response) {
		this.response = response;
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

	public String getResponseValue() {
		return responseValue;
	}

	public void setResponseValue(String responseValue) {
		this.responseValue = responseValue;
	}

	public String getSubMerchantPayId() {
		return subMerchantPayId;
	}

	public void setSubMerchantPayId(String subMerchantPayId) {
		this.subMerchantPayId = subMerchantPayId;
	}

	public String getMerchantSelectionType() {
		return merchantSelectionType;
	}

	public void setMerchantSelectionType(String merchantSelectionType) {
		this.merchantSelectionType = merchantSelectionType;
	}

}
