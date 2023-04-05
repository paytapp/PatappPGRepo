package com.paymentgateway.crm.action;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;

import com.itextpdf.text.Image;
import com.paymentgateway.commons.api.Hasher;
import com.paymentgateway.commons.dao.ENachDao;
import com.paymentgateway.commons.dao.FieldsDao;
import com.paymentgateway.commons.dao.SearchUserService;
import com.paymentgateway.commons.dao.UpiAutoPayDao;
import com.paymentgateway.commons.dao.UserSettingDao;
import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.user.Account;
import com.paymentgateway.commons.user.Merchants;
import com.paymentgateway.commons.user.PermissionType;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.user.UserSettingData;
import com.paymentgateway.commons.user.UserType;
import com.paymentgateway.commons.util.AutoPayFrequency;
import com.paymentgateway.commons.util.Base64EncodeDecode;
import com.paymentgateway.commons.util.BusinessType;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.CrmFieldConstants;
import com.paymentgateway.commons.util.CrmFieldType;
import com.paymentgateway.commons.util.Currency;
import com.paymentgateway.commons.util.DateCreater;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.Frequency;
import com.paymentgateway.commons.util.MakerCheckerObj;
import com.paymentgateway.commons.util.PromotionalPaymentType;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.SubAdmin;
import com.paymentgateway.crm.actionBeans.CurrencyMapProvider;
import com.paymentgateway.crm.mongoReports.TxnReports;
import com.paymentgateway.pg.core.pageintegrator.GeneralValidator;

/**
 * @author Puneet
 * 
 */

public class ForwardAction extends AbstractSecureAction {

	private static Logger logger = LoggerFactory.getLogger(ForwardAction.class.getName());

	@Autowired
	private UserDao userDao;

	@Autowired
	private FieldsDao fieldsDao;

	@Autowired
	private ENachDao eNachDao;

	@Autowired
	private SearchUserService searchUserService;

	@Autowired
	private Base64EncodeDecode base64EncodeDecode;

	@Autowired
	private TxnReports txnReports;

	@Autowired
	private GeneralValidator generalValidator;

	@Autowired
	PropertiesManager propertiesManager;
	
	@Autowired
	private UserSettingDao userSettingDao;

	@Autowired
	private UpiAutoPayDao upiAutoPayDao;

	private static final long serialVersionUID = -6411665872667971425L;
	private List<SubAdmin> subAdminList = new ArrayList<SubAdmin>();
	private List<MakerCheckerObj> checkerMakeList = new ArrayList<MakerCheckerObj>();
	private List<Merchants> merchantList = new ArrayList<Merchants>();
	private Map<String, String> currencyMap = new LinkedHashMap<String, String>();
	private Map<String, String> industryTypes = new TreeMap<String, String>();
	private List<Merchants> subMerchantList = new ArrayList<Merchants>();
	private List<Merchants> subMerchantsForQr = new ArrayList<Merchants>();
	private User sessionUser = null;
	private List<User> userActivelist = new ArrayList<User>();
	private boolean editingpermission;
	private List<PromotionalPaymentType> invoiceSearchPromotionalPaymentType;
	private Map<String, String> statusType = new HashMap<String, String>();
	private boolean showEpos;
	private List<PermissionType> listPermissionType;
	private List<PermissionType> subUserPermissionType;
	private String ORDER_ID;
	private String salt;
	private boolean superMerchantFlag;
	private boolean subMerchantFlag;
	private boolean merchantInitiatedDirectFlag;
	private boolean accountVerificationFlag;
	private boolean eNachReportFlag;
	private boolean virtualAccountFlag;
	private boolean vpaVerificationFlag;
	private boolean bookingReportFlag;
	private boolean customerQrFlag;
	private boolean capturedMerchantFlag;
	private boolean upiAutoPayReportFlag;
	private String payId;

	private Map<String, String> aaData = new HashMap<String, String>();

	@SuppressWarnings("unchecked")
	public String execute() {

		statusType.put("Active", "Active");
		statusType.put("Pending", "Pending");
		setStatusType(statusType);
		CurrencyMapProvider currencyMapProvider = new CurrencyMapProvider();
		try {
			sessionUser = (User) sessionMap.get(Constants.USER.getValue());
			UserSettingData userSettings=(UserSettingData) sessionMap.get(Constants.USER_SETTINGS);

			if (sessionUser.getUserType().equals(UserType.ADMIN)
					|| sessionUser.getUserType().equals(UserType.SUPERADMIN)
					|| sessionUser.getUserType().equals(UserType.SUBADMIN)
					|| sessionUser.getUserType().equals(UserType.RESELLER)
					|| sessionUser.getUserType().equals(UserType.AGENT)
					|| sessionUser.getUserType().equals(UserType.ACQUIRER)) {
				if (sessionUser.getUserType().equals(UserType.RESELLER)) {
					merchantList = new UserDao().getActiveResellerMerchants(sessionUser.getResellerId());
				} else if (sessionUser.getUserType().equals(UserType.ACQUIRER)) {
					String merchantPayId = sessionUser.getPayId();
					List<User> userlist = new ArrayList<User>();
					userlist = new UserDao().getUserActiveList();
					for (User user : userlist) {
						Merchants merchant = new Merchants();
						merchant.setEmailId(user.getEmailId());
						merchant.setPayId(user.getPayId());
						merchant.setBusinessName(user.getBusinessName());
						userActivelist.add(user);
						Set<Account> accountSet = user.getAccounts();
						Iterator<Account> accountDetails = accountSet.iterator();
						while (accountDetails.hasNext()) {
							Account account = accountDetails.next();
							if (merchantPayId.equals(account.getAcquirerPayId())) {
								merchantList.add(merchant);
							}

						}
					}
				} else {

					List<PromotionalPaymentType> merchantPaymentList = new ArrayList<PromotionalPaymentType>();
					merchantPaymentList.add(PromotionalPaymentType.INVOICE_PAYMENT);
					merchantPaymentList.add(PromotionalPaymentType.PROMOTIONAL_PAYMENT);

					setInvoiceSearchPromotionalPaymentType(merchantPaymentList);

					merchantList = new UserDao().getMerchantActiveList();
					setIndustryTypes(BusinessType.getIndustryCategoryList());
					setShowEpos(false);
					if (sessionUser.getUserType().equals(UserType.ADMIN)
							|| sessionUser.getUserType().equals(UserType.SUBADMIN)) {
						setShowEpos(true);
						setSubUserPermissionType(PermissionType.getSubUserAccessPrevilageType());
						setListPermissionType(PermissionType.getSubUserPermissionType());
						// setMerchantList(userDao.getAllStatusMerchantList());
					}

				}
				currencyMap = currencyMapProvider.currencyMap(sessionUser);

				Map<String, String> permissionsMap = new HashMap<>();
				permissionsMap = propertiesManager.getAllProperties("salt.properties");
				salt = permissionsMap.get(sessionUser.getPayId());

			} else if (sessionUser.getUserType().equals(UserType.MERCHANT)) {
				List<PromotionalPaymentType> merchantPaymentList = new ArrayList<PromotionalPaymentType>();
				merchantPaymentList.add(PromotionalPaymentType.INVOICE_PAYMENT);
				merchantPaymentList.add(PromotionalPaymentType.PROMOTIONAL_PAYMENT);

				setInvoiceSearchPromotionalPaymentType(merchantPaymentList);

				if (sessionUser.isSuperMerchant()) {
					setSubMerchantList(userDao.getSubMerchantListBySuperPayId(sessionUser.getPayId()));
					for (Merchants merch : subMerchantList) {
						UserSettingData merchantSettings=userSettingDao.fetchDataUsingPayId(merch.getPayId());
						if (merchantSettings.isCustomerQrFlag()) {
							subMerchantsForQr.add(merch);
						}
					}
					if (userSettings.isEposMerchant()) {
						setShowEpos(true);
					}

					setSubUserPermissionType(PermissionType.getSubUserAccessPrevilageType());
					setListPermissionType(PermissionType.getSubUserPermissionTypeForSuperMerchant());
				} else {
					/*
					 * if (StringUtils.isNotBlank(sessionUser.getSuperMerchantId())) { if
					 * (userDao.findPayId(sessionUser.getSuperMerchantId()).isEposMerchant()) {
					 * setShowEpos(true); } } else { if
					 * (userDao.findPayId(sessionUser.getPayId()).isEposMerchant()) {
					 * setShowEpos(true); } }
					 */

					setShowEpos(true);
					setSubUserPermissionType(PermissionType.getSubUserAccessPrevilageType());
					setListPermissionType(PermissionType.getSubUserPermissionType());
				}
				setCapturedMerchantFlag(userSettings.isCapturedMerchantFlag());
				setBookingReportFlag(userSettings.isBookingRecord());
				setCustomerQrFlag(userSettings.isCustomerQrFlag());
				setMerchantInitiatedDirectFlag(userSettings.isMerchantInitiatedDirectFlag());
				seteNachReportFlag(userSettings.iseNachReportFlag());
				setVirtualAccountFlag(userSettings.isVirtualAccountFlag());
				setAccountVerificationFlag(userSettings.isAccountVerificationFlag());
				setVpaVerificationFlag(userSettings.isVpaVerificationFlag());
				setUpiAutoPayReportFlag(userSettings.isUpiAutoPayReportFlag());

				currencyMap = currencyMapProvider.currencyMap(sessionUser);

				// For Sub Merchant
				if (!sessionUser.isSuperMerchant() && StringUtils.isNotBlank(sessionUser.getSuperMerchantId())) {
					User superMerchant = userDao.findPayId(sessionUser.getSuperMerchantId());
					if (superMerchant != null) {
						currencyMap = currencyMapProvider.currencyMap(superMerchant);
					}
				}
				Merchants merchant = new Merchants();
				merchant.setEmailId(sessionUser.getEmailId());
				merchant.setPayId(sessionUser.getPayId());
				merchant.setBusinessName(sessionUser.getBusinessName());
				merchantList.add(merchant);
				if (currencyMap.isEmpty()) {
					addFieldError(CrmFieldType.DEFAULT_CURRENCY.getName(),
							ErrorType.UNMAPPED_CURRENCY_ERROR.getResponseMessage());
					addActionMessage("No currency mapped!!");
					return INPUT;
				}

				Map<String, String> permissionsMap = new HashMap<>();
				permissionsMap = propertiesManager.getAllProperties("salt.properties");
				salt = permissionsMap.get(sessionUser.getPayId());

			} else if (sessionUser.getUserType().equals(UserType.SUBUSER)) {

				List<PromotionalPaymentType> merchantPaymentList = new ArrayList<PromotionalPaymentType>();
				merchantPaymentList.add(PromotionalPaymentType.INVOICE_PAYMENT);

				setInvoiceSearchPromotionalPaymentType(merchantPaymentList);

				Merchants merchant = new Merchants();
				String parentMerchantPayId = sessionUser.getParentPayId();
				User parentMerchant = userDao.findPayId(parentMerchantPayId);

				/*
				 * currencyMap = currencyMapProvider.currencyMap(sessionUser);
				 * 
				 * // For Sub Merchant if (!sessionUser.isSuperMerchant() &&
				 * StringUtils.isNotBlank(sessionUser.getSuperMerchantId())) { User
				 * superMerchant = userDao.findPayId(sessionUser.getSuperMerchantId()); if
				 * (superMerchant != null) { currencyMap =
				 * currencyMapProvider.currencyMap(superMerchant); } } Merchants merchant = new
				 * Merchants(); merchant.setEmailId(sessionUser.getEmailId());
				 * merchant.setPayId(sessionUser.getPayId());
				 * merchant.setBusinessName(sessionUser.getBusinessName());
				 * merchantList.add(merchant); if (currencyMap.isEmpty()) {
				 */

				if (!parentMerchant.isSuperMerchant() && StringUtils.isNotBlank(parentMerchant.getSuperMerchantId())) {

					User superMerchant = userDao.findPayId(parentMerchant.getSuperMerchantId());
					merchant.setMerchant(parentMerchant);
					merchantList.add(merchant);
					setSubMerchantFlag(true);
					currencyMap = currencyMapProvider.currencyMap(superMerchant);

				} else if (parentMerchant.isSuperMerchant()
						&& StringUtils.isNotBlank(parentMerchant.getSuperMerchantId())) {
					if (sessionUser.getSubUserType().equals("normalType")) {
						setSubMerchantList(userDao.getSubMerchantListBySuperPayId(parentMerchant.getPayId()));
						for (Merchants merch : subMerchantList) {
							UserSettingData merchantSettings=userSettingDao.fetchDataUsingPayId(merch.getPayId());
							if (merchantSettings.isCustomerQrFlag()) {
								subMerchantsForQr.add(merch);
							}
						}
						setSuperMerchantFlag(true);
					} else {
						setSuperMerchantFlag(false);
					}

				} else if (parentMerchant.isSuperMerchant()
						&& StringUtils.isNotBlank(parentMerchant.getSuperMerchantId())) {
					if (sessionUser.getSubUserType().equals("normalType")) {
						setSubMerchantList(userDao.getSubMerchantListBySuperPayId(parentMerchant.getPayId()));
						setSuperMerchantFlag(true);
					} else {
						setSuperMerchantFlag(false);
					}

					merchant.setMerchant(parentMerchant);
					merchantList.add(merchant);

					currencyMap = currencyMapProvider.currencyMap(sessionUser);
				} else {

					merchant.setMerchant(parentMerchant);
					merchantList.add(merchant);
					currencyMap = currencyMapProvider.currencyMap(sessionUser);
				}

				if (currencyMap.isEmpty()) {
					addFieldError(CrmFieldType.DEFAULT_CURRENCY.getName(),
							ErrorType.UNMAPPED_CURRENCY_ERROR.getResponseMessage());
					addActionMessage("No currency mapped!!");
					return INPUT;
				}

			}
			return INPUT;
		} catch (Exception exception) {
			logger.error("Exception", exception);
			return ERROR;
		}
	}

	@SuppressWarnings("unchecked")
	public String allMerchants() {
		try {
			sessionUser = (User) sessionMap.get(Constants.USER.getValue());
			if (sessionUser.getUserType().equals(UserType.ADMIN) || sessionUser.getUserType().equals(UserType.SUBADMIN)
					|| sessionUser.getUserType().equals(UserType.SUPERADMIN)
					|| sessionUser.getUserType().equals(UserType.RESELLER)
					|| sessionUser.getUserType().equals(UserType.ACQUIRER)) {
				if (sessionUser.getUserType().equals(UserType.RESELLER)) {
					merchantList = new UserDao().getActiveResellerMerchants(sessionUser.getResellerId());
				} else {
					merchantList = new UserDao().getMerchantList();
				}
				// set currencies
				currencyMap = Currency.getAllCurrency();
			} else if (sessionUser.getUserType().equals(UserType.MERCHANT)) {
				Merchants merchant = new Merchants();
				merchant.setMerchant(sessionUser);
				merchantList.add(merchant);
				// set currencies
				currencyMap = Currency.getSupportedCurreny(sessionUser);
			} else if (sessionUser.getUserType().equals(UserType.SUBUSER)) {
				Merchants merchant = new Merchants();
				User user = new User();
				UserDao userDao = new UserDao();
				user = userDao.findPayId(sessionUser.getParentPayId());
				merchant.setMerchant(user);
				merchantList.add(merchant);
				// set currencies
				currencyMap = Currency.getSupportedCurreny(sessionUser);
			} else if (sessionUser.getUserType().equals(UserType.AGENT)) {
				Merchants merchant = new Merchants();
				User user = new User();
				UserDao userDao = new UserDao();
				user = userDao.findPayId(sessionUser.getPayId());
				merchant.setMerchant(user);
				merchantList.add(merchant);
				// set currencies
				currencyMap = Currency.getSupportedCurreny(sessionUser);
			}

			return INPUT;
		} catch (Exception exception) {
			logger.error("Exception", exception);
			return ERROR;
		}
	}

	public String getIndustryType() {
		try {
			sessionUser = (User) sessionMap.get(Constants.USER.getValue());
			if (sessionUser.getUserType().equals(UserType.SUBADMIN)) {
				setEditingpermission(sessionUser.isEditPermission());
			}
			Map<String, String> industryCategoryLinkedMap = BusinessType.getIndustryCategoryList();
			industryTypes.putAll(industryCategoryLinkedMap);
			return INPUT;
		} catch (Exception exception) {
			logger.error("Exception ", exception);
			return ERROR;
		}
	}

	public String getSubAdminList() {
		try {
			sessionUser = (User) sessionMap.get(Constants.USER.getValue());
			List<MakerCheckerObj> checkerMakeListData = new ArrayList<MakerCheckerObj>();
			if (sessionUser.getUserType().equals(UserType.ADMIN)) {
				setSubAdminList(searchUserService.getAgentsList(sessionUser.getPayId()));

				for (SubAdmin subAdmin : subAdminList) {
					MakerCheckerObj makerCheckerObj = new MakerCheckerObj();

					makerCheckerObj.setPermissionType(subAdmin.getPermissionType());
					makerCheckerObj.setName(subAdmin.getAgentFirstName() + " " + subAdmin.getAgentLastName());
					makerCheckerObj.setPayId(subAdmin.getPayId());
					checkerMakeListData.add(makerCheckerObj);
				}
			}
			Map<String, String> industryCategoryLinkedMap = BusinessType.getIndustryCategoryList();
			industryTypes.putAll(industryCategoryLinkedMap);
			setCheckerMakeList(checkerMakeListData);
			return INPUT;
		} catch (Exception exception) {
			logger.error("Exception ", exception);
			return ERROR;
		}
	}

	@SuppressWarnings({ "static-access" })
	public String eNachMandateRequest() {

		try {
			logger.info("inside eNachMandate forwardAction");

			Fields fields = new Fields();
			String[] commaSplitData = getORDER_ID().split("\\,");
			StringBuilder response = new StringBuilder();
			boolean flag = false;
			for (String responseData : commaSplitData) {
				if (!flag) {
					responseData = responseData.replaceAll(":", "");
					if (StringUtils.isNotBlank(responseData) && !responseData.equalsIgnoreCase("null")) {
						response.append("ORDER_ID" + "=:" + responseData);
					} else {
						response.append("ORDER_ID" + "=:" + " ");
					}
					response.append(",");
					flag = true;
				} else {
					String[] colonSplit = responseData.split("=:");
					colonSplit[0] = colonSplit[0].replace("?", "");
					try {
						response.append(colonSplit[0] + "=:" + colonSplit[1].trim());
					} catch (ArrayIndexOutOfBoundsException aiob) {
						response.append(colonSplit[0] + "=:" + " ");
					} catch (NullPointerException npe) {
						response.append(colonSplit[0] + "=:" + " ");
					}
					response.append(",");
				}
			}
			response.deleteCharAt(response.length() - 1);
			String[] commaSplit = response.toString().split("\\,");
			for (String data : commaSplit) {

				String[] dataArr = data.split("\\=:");
				try {
					fields.put(dataArr[0], dataArr[1]);
				} catch (ArrayIndexOutOfBoundsException aiob) {
					fields.put(dataArr[0], "");
				}

			}
//			Map<String, String> validateFields = ValidateRequestFields(fields);
			Map<String, String> validateFields = ValidateFieldsForEMandateSignRequest(fields);

			if (validateFields.get(FieldType.RESPONSE_CODE.getName())
					.equalsIgnoreCase(ErrorType.SUCCESS.getResponseCode())) {

				String merchantHash = fields.get(FieldType.HASH.getName());
				fields.remove(FieldType.HASH.getName());
				String calculatedHash = Hasher.getHash(fields);

				if (merchantHash.equals(calculatedHash)) {

					logger.info("merchant hash matched");
					User user = userDao.findPayId(fields.get(FieldType.PAY_ID.getName()));

					// for reseller sub merchant
					if (user.isSuperMerchant() == false && StringUtils.isNotBlank(user.getSuperMerchantId())
							&& StringUtils.isNotBlank(user.getResellerId())) {
						aaData.put(FieldType.PAY_ID.getName(), user.getSuperMerchantId());
						aaData.put(FieldType.SUB_MERCHANT_ID.getName(), fields.get(FieldType.PAY_ID.getName()));
						aaData.put(FieldType.RESELLER_ID.getName(), user.getResellerId());

					} else if (user.isSuperMerchant() == false && StringUtils.isNotBlank(user.getSuperMerchantId())) {
						// for Sub Merchant

						// super merchantId
						aaData.put(FieldType.PAY_ID.getName(), user.getSuperMerchantId());
						// sub MerchantId
						aaData.put(FieldType.SUB_MERCHANT_ID.getName(), fields.get(FieldType.PAY_ID.getName()));

					} else if (StringUtils.isNotBlank(user.getResellerId())) {
						// Reseller Merchant
						aaData.put(FieldType.RESELLER_ID.getName(), user.getResellerId());
						aaData.put(FieldType.PAY_ID.getName(), fields.get(FieldType.PAY_ID.getName()));
						aaData.put(FieldType.SUB_MERCHANT_ID.getName(), "");

					} else {
						// super merchantId
						aaData.put(FieldType.PAY_ID.getName(), fields.get(FieldType.PAY_ID.getName()));
						// sub MerchantId
						aaData.put(FieldType.SUB_MERCHANT_ID.getName(), "");
					}

					StringBuilder base64EncodeImage = new StringBuilder();
					String finalLogoLocation = null;
					File imageLocation = new File(
							propertiesManager.propertiesMap.get(Constants.LOGO_FILE_UPLOAD_LOCATION.getValue()) + "/"
									+ aaData.get(FieldType.PAY_ID.getName()));
					if (!imageLocation.exists()) {
						logger.info("no such a directory for merchant logo ");
						base64EncodeImage.append(
								propertiesManager.propertiesMap.get(Constants.ICICI_ENACH_PAYMENT_GATEWAY_LOGO.getValue()));
					} else {

						String contents[] = imageLocation.list();
						finalLogoLocation = imageLocation.toString() + "/" + contents[0];
						if (contents[0].contains(".png")) {
							base64EncodeImage.append("data:image/png;base64,");
							base64EncodeImage.append(base64EncodeDecode.base64Encoder(new File(finalLogoLocation)));
						} else {
							base64EncodeImage.append("data:image/jpg;base64,");
							base64EncodeImage.append(base64EncodeDecode.base64Encoder(new File(finalLogoLocation)));
						}
					}

					boolean duplicateFlag = eNachDao.checkDuplicateOrderIdForRegistration(fields.get("ORDER_ID"),
							aaData.get(FieldType.PAY_ID.getName()), aaData.get(FieldType.SUB_MERCHANT_ID.getName()));

					if (!duplicateFlag) {

						String currentDate = DateCreater.defaultFromDate();
						fields.put(FieldType.DATE_FROM.getName(), currentDate);
						fields.put(FieldType.DATE_TO.getName(), currentDate);

						String totalAmount = String
								.valueOf(new BigDecimal(fields.get(FieldType.MONTHLY_AMOUNT.getName()))
										.multiply(new BigDecimal(fields.get(FieldType.TENURE.getName())))
										.setScale(2, RoundingMode.HALF_DOWN));

						SimpleDateFormat sdf = new SimpleDateFormat(CrmFieldConstants.INPUT_DATE_FORMAT.getValue());
						Date date = sdf.parse(fields.get(FieldType.DATE_FROM.getName()));
						sdf = new SimpleDateFormat(CrmFieldConstants.OUTPUT_DATE_FORMAT.getValue());

						List<String> startEndDebitDate = null;

						String debitDuration;
						if (StringUtils.isNotBlank(aaData.get(FieldType.SUB_MERCHANT_ID.getName())))
							debitDuration = userDao
									.getDebitDurationByPayId(aaData.get(FieldType.SUB_MERCHANT_ID.getName()));
						else
							debitDuration = userDao.getDebitDurationByPayId(aaData.get(FieldType.PAY_ID.getName()));

						LocalDate startDATE = LocalDate.parse(sdf.format(date));
						if (debitDuration != null)
							startDATE = startDATE.plusDays(Long.parseLong(debitDuration));
						else
							startDATE = startDATE.plusDays(30);

						startEndDebitDate = eNachDao.getDueDateList(startDATE.toString(),
								fields.get(FieldType.TENURE.getName()), aaData.get(FieldType.PAY_ID.getName()),
								aaData.get(FieldType.SUB_MERCHANT_ID.getName()),
								fields.get(FieldType.FREQUENCY.getName()));

						String startDate = startEndDebitDate.get(0);
						String startDateArr[] = startDate.split("-");
						int tempDate = Integer.parseInt(startDateArr[2]);
						int length = (int) (Math.log10(tempDate));
						if (length == 0) {
							startDateArr[2] = "0" + String.valueOf(tempDate);
						} else {
							startDateArr[2] = String.valueOf(tempDate);
						}

						StringBuilder finalDateBuilder = new StringBuilder();
						for (int i = startDateArr.length; i > 0; i--) {
							finalDateBuilder.append(startDateArr[i - 1]).append("-");
						}

						finalDateBuilder.deleteCharAt(finalDateBuilder.length() - 1);
						startDate = finalDateBuilder.toString();

						String endDate = startEndDebitDate.get(startEndDebitDate.size() - 1);
						String endDateArr[] = endDate.split("-");
						tempDate = Integer.parseInt(endDateArr[2]);
						length = (int) (Math.log10(tempDate));
						if (length == 0) {
							endDateArr[2] = "0" + String.valueOf(tempDate);
						} else {
							endDateArr[2] = String.valueOf(tempDate);
						}

						finalDateBuilder = new StringBuilder();
						for (int i = endDateArr.length; i > 0; i--) {
							finalDateBuilder.append(endDateArr[i - 1]).append("-");
						}

						finalDateBuilder.deleteCharAt(finalDateBuilder.length() - 1);
						endDate = finalDateBuilder.toString();

						aaData.put("DEBIT_END_DATE",
								(DateCreater.dateFormatReverse(startEndDebitDate.get(startEndDebitDate.size() - 1))));
						aaData.put("DEBIT_START_DATE", (DateCreater.dateFormatReverse(startEndDebitDate.get(0))));

						aaData.put(FieldType.REGISTRATION_DATE.getName(), sdf.format(date));

						aaData.put("CONSUMER_ID", fields.get("ORDER_ID"));
						aaData.put("MERCHANT_NAME",
								userDao.getBusinessNameByPayId(fields.get(FieldType.PAY_ID.getName())));
						aaData.put(FieldType.TENURE.getName(), fields.get(FieldType.TENURE.getName()));
						aaData.put(FieldType.PAY_ID.getName(), fields.get(FieldType.PAY_ID.getName()));
						aaData.put(FieldType.FREQUENCY.getName(),
								Frequency.getFrequencyName(fields.get(FieldType.FREQUENCY.getName())));

						aaData.put(FieldType.AMOUNT.getName(),
								String.valueOf(new BigDecimal(fields.get(FieldType.AMOUNT.getName())).setScale(2,
										BigDecimal.ROUND_HALF_UP)));
						aaData.put(FieldType.TOTAL_AMOUNT.getName(),
								String.valueOf(new BigDecimal(totalAmount).setScale(2, BigDecimal.ROUND_HALF_UP)));
						aaData.put(FieldType.MONTHLY_AMOUNT.getName(),
								String.valueOf(new BigDecimal(fields.get(FieldType.MONTHLY_AMOUNT.getName()))
										.setScale(2, BigDecimal.ROUND_HALF_UP)));

						aaData.put("CUSTOMER_MOBILE", fields.get("CUST_MOBILE"));
						aaData.put("CUSTOMER_EMAIL", fields.get(FieldType.CUST_EMAIL.getName()));
						aaData.put(FieldType.RETURN_URL.getName(),
								propertiesManager.propertiesMap.get(Constants.ICICI_ENACH_REPONSE.getValue()));
						aaData.put("MERCHANT_RETURN_URL", fields.get(FieldType.RETURN_URL.getName()));

						aaData.put("LOGO", base64EncodeImage.toString());
						aaData.put(FieldType.RESPONSE.getName(), SUCCESS);

					} else {
						aaData.put(FieldType.RESPONSE.getName(), ErrorType.DUPLICATE_ORDER_ID.getResponseMessage());
						aaData.put(FieldType.RETURN_URL.getName(),
								propertiesManager.propertiesMap.get(Constants.ICICI_ENACH_REPONSE.getValue()));
						aaData.put("MERCHANT_RETURN_URL", fields.get(FieldType.RETURN_URL.getName()));
						logger.info("merchant send a duplicate order id");
					}

				} else {
					logger.info("merchant hash not match");
					StringBuilder hashMessage = new StringBuilder("Merchant hash = ");
					hashMessage.append(merchantHash);
					hashMessage.append(", Calculated Hash = ");
					hashMessage.append(calculatedHash);
					MDC.put(FieldType.INTERNAL_CUSTOM_MDC.getName(), fields.getCustomMDC());
					logger.error(hashMessage.toString());

					aaData.put(FieldType.RESPONSE.getName(), Constants.TRANSACTIONSTATE_N.getValue());
					aaData.put(FieldType.RETURN_URL.getName(),
							propertiesManager.propertiesMap.get(Constants.ICICI_ENACH_REPONSE.getValue()));
					aaData.put("MERCHANT_RETURN_URL", fields.get(FieldType.RETURN_URL.getName()));
				}

			} else {

				switch (validateFields.get(FieldType.RESPONSE_MESSAGE.getName())) {

				case "Duplicate Order ID":
					aaData.put(FieldType.RESPONSE.getName(), "Duplicate order Id");
					break;
				case "No Order ID Available":
				case "Invalid Order ID":
					aaData.put(FieldType.RESPONSE.getName(), "INVALID REQUEST");
					break;
				case "Invalid Request ID":
					aaData.put(FieldType.RESPONSE.getName(), "Invalid Request ID");
					break;
				case "Invalid End Date":
					aaData.put(FieldType.RESPONSE.getName(), "Invalid End Date");
					break;
				case "Invalid Start Date":
					aaData.put(FieldType.RESPONSE.getName(), "Invalid Start Date");
					break;
				case "Invalid Amount":
					aaData.put(FieldType.RESPONSE.getName(), "Invalid Amount");
					break;
				case "Invalid Monthly Amount":
					aaData.put(FieldType.RESPONSE.getName(), "Invalid Monthly Amount");
					break;
				case "Invalid Frequency":
					aaData.put(FieldType.RESPONSE.getName(), "Invalid Frequency");
					break;
				case "Invalid Tenure":
					aaData.put(FieldType.RESPONSE.getName(), "Invalid Tenure");
					break;
				case "Invalid Merchant ID":
					aaData.put(FieldType.RESPONSE.getName(), "Invalid Merchant ID");
					break;
				case "Invalid Customer Mobile":
					aaData.put(FieldType.RESPONSE.getName(), "Invalid Customer Mobile");
					break;
				case "Invalid Customer Email":
					aaData.put(FieldType.RESPONSE.getName(), "Invalid Customer Email");
					break;
				default:
					break;
				}
				aaData.put(FieldType.RETURN_URL.getName(),
						propertiesManager.propertiesMap.get(Constants.ICICI_ENACH_REPONSE.getValue()));
				aaData.put("MERCHANT_RETURN_URL", fields.get(FieldType.RETURN_URL.getName()));
			}

		} catch (Exception ex) {
			logger.info("Exception caught in eNach mandate registration ", ex);
		}
		return SUCCESS;
	}

	private Map<String, String> ValidateFieldsForEMandateSignRequest(Fields fields) {

		Map<String, String> validationMap = new HashMap<String, String>();

		if (fields.contains(FieldType.ORDER_ID.getName())) {

			if (fields.contains(FieldType.MONTHLY_AMOUNT.getName())
					&& StringUtils.isNotBlank(fields.get(FieldType.MONTHLY_AMOUNT.getName()))
					&& !fields.get(FieldType.MONTHLY_AMOUNT.getName()).contains("-")
					&& NumberUtils.isNumber(fields.get(FieldType.MONTHLY_AMOUNT.getName()))) {

				if (fields.contains(FieldType.FREQUENCY.getName())
						&& StringUtils.isNotBlank(fields.get(FieldType.FREQUENCY.getName()))
						&& StringUtils.isAlpha(fields.get(FieldType.FREQUENCY.getName()))) {

					if (fields.contains(FieldType.TENURE.getName())
							&& StringUtils.isNotBlank(fields.get(FieldType.TENURE.getName()))
							&& StringUtils.isNumeric(fields.get(FieldType.TENURE.getName()))) {

						if (fields.contains("CUST_MOBILE") && StringUtils.isNotBlank(fields.get("CUST_MOBILE"))
								&& StringUtils.isNumeric(fields.get("CUST_MOBILE"))) {

							if (fields.contains(FieldType.CUST_EMAIL.getName())
									&& StringUtils.isNotBlank(fields.get(FieldType.CUST_EMAIL.getName()))
									&& generalValidator.isValidEmailId(fields.get(FieldType.CUST_EMAIL.getName()))) {

								if (StringUtils.isNotBlank(fields.get(FieldType.ORDER_ID.getName()))
										&& StringUtils.isAlphanumeric(fields.get(FieldType.ORDER_ID.getName()))) {

									boolean duplicateFlag = false;
									duplicateFlag = eNachDao.checkDuplicateOrderIdForRegistration(
											fields.get(FieldType.ORDER_ID.getName()),
											fields.get(FieldType.PAY_ID.getName()),
											fields.get(FieldType.SUB_MERCHANT_ID.getName()));

									if (!duplicateFlag) {
										logger.info("all request fields are valid");
										validationMap.put(FieldType.RESPONSE_MESSAGE.getName(),
												ErrorType.SUCCESS.getResponseMessage());
										validationMap.put(FieldType.RESPONSE_CODE.getName(),
												ErrorType.SUCCESS.getResponseCode());
										return validationMap;
									} else {
										logger.info("Duplicate Order Id");
										validationMap.put(FieldType.RESPONSE_MESSAGE.getName(),
												ErrorType.DUPLICATE_ORDER_ID.getResponseMessage());
										validationMap.put(FieldType.RESPONSE_CODE.getName(),
												ErrorType.DUPLICATE_ORDER_ID.getResponseCode());
										return validationMap;
									}
								}
								logger.info("Valid ORDER_ID Required");
								validationMap.put(FieldType.RESPONSE_MESSAGE.getName(),
										ErrorType.INVALID_ORDER_ID.getResponseMessage());
								validationMap.put(FieldType.RESPONSE_CODE.getName(),
										ErrorType.INVALID_ORDER_ID.getResponseCode());
								return validationMap;
							}

							logger.info("Invalid CUST_EMAIL");
							validationMap.put(FieldType.RESPONSE_MESSAGE.getName(),
									ErrorType.INVALID_CUST_EMAIL.getResponseMessage());
							validationMap.put(FieldType.RESPONSE_CODE.getName(),
									ErrorType.INVALID_CUST_EMAIL.getResponseCode());
							return validationMap;
						}
						logger.info("Invalid CUST_MOBILE");
						validationMap.put(FieldType.RESPONSE_MESSAGE.getName(),
								ErrorType.INVALID_CUST_MOBILE.getResponseMessage());
						validationMap.put(FieldType.RESPONSE_CODE.getName(),
								ErrorType.INVALID_CUST_MOBILE.getResponseCode());
						return validationMap;
					}

					logger.info("Invalid TENURE");
					validationMap.put(FieldType.RESPONSE_MESSAGE.getName(),
							ErrorType.INVALID_TENURE.getResponseMessage());
					validationMap.put(FieldType.RESPONSE_CODE.getName(), ErrorType.INVALID_TENURE.getResponseCode());
					return validationMap;
				}
				logger.info("Invalid FREQUENCY");
				validationMap.put(FieldType.RESPONSE_MESSAGE.getName(),
						ErrorType.INVALID_FREQUENCY.getResponseMessage());
				validationMap.put(FieldType.RESPONSE_CODE.getName(), ErrorType.INVALID_FREQUENCY.getResponseCode());
				return validationMap;
			}
			logger.info("Invalid MONTHLY_AMOUNT");
			validationMap.put(FieldType.RESPONSE_MESSAGE.getName(),
					ErrorType.INVALID_MONTHLY_AMOUNT.getResponseMessage());
			validationMap.put(FieldType.RESPONSE_CODE.getName(), ErrorType.INVALID_MONTHLY_AMOUNT.getResponseCode());
			return validationMap;
		} else {
			logger.info("ORDER_ID Required");
			validationMap.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.NO_ORDER_ID.getResponseMessage());
			validationMap.put(FieldType.RESPONSE_CODE.getName(), ErrorType.NO_ORDER_ID.getResponseCode());
			return validationMap;
		}
	}

	private Map<String, String> ValidateRequestFields(Fields fields) {

		Map<String, String> validationMap = new HashMap<String, String>();

		if (fields.contains("ORDER_ID") && StringUtils.isNotBlank(fields.get("ORDER_ID"))
				|| fields.contains(FieldType.ORDER_ID.getName())
						&& StringUtils.isNotBlank(fields.get(FieldType.ORDER_ID.getName()))) {

			if (fields.contains(FieldType.AMOUNT.getName())
					&& StringUtils.isNotBlank(fields.get(FieldType.AMOUNT.getName()))
					&& !fields.get(FieldType.AMOUNT.getName()).contains("-")
					&& NumberUtils.isNumber(fields.get(FieldType.AMOUNT.getName()))) {

				if (fields.contains(FieldType.MONTHLY_AMOUNT.getName())
						&& StringUtils.isNotBlank(fields.get(FieldType.MONTHLY_AMOUNT.getName()))
						&& !fields.get(FieldType.MONTHLY_AMOUNT.getName()).contains("-")
						&& NumberUtils.isNumber(fields.get(FieldType.MONTHLY_AMOUNT.getName()))) {

					if (fields.contains(FieldType.FREQUENCY.getName())
							&& StringUtils.isNotBlank(fields.get(FieldType.FREQUENCY.getName()))
							&& StringUtils.isAlpha(fields.get(FieldType.FREQUENCY.getName()))) {

						if (fields.contains(FieldType.TENURE.getName())
								&& StringUtils.isNotBlank(fields.get(FieldType.TENURE.getName()))
								&& StringUtils.isNumeric(fields.get(FieldType.TENURE.getName()))) {

							if (fields.contains(FieldType.PAY_ID.getName())
									&& StringUtils.isNotBlank(fields.get(FieldType.PAY_ID.getName()))
									&& StringUtils.isNumeric(fields.get(FieldType.PAY_ID.getName()))) {

								if (fields.contains("CUST_MOBILE") && StringUtils.isNotBlank(fields.get("CUST_MOBILE"))
										&& StringUtils.isNumeric(fields.get("CUST_MOBILE"))) {

									if (fields.contains(FieldType.CUST_EMAIL.getName())
											&& StringUtils.isNotBlank(fields.get(FieldType.CUST_EMAIL.getName()))) {

										logger.info("all request fields are valid");
										validationMap.put(FieldType.RESPONSE_MESSAGE.getName(), SUCCESS);
										validationMap.put(FieldType.RESPONSE_CODE.getName(), "true");
										return validationMap;

									}
									logger.info("Invalid CUST_EMAIL");
									validationMap.put(FieldType.RESPONSE_MESSAGE.getName(), "Invalid Customer Email");
									validationMap.put(FieldType.RESPONSE_CODE.getName(), "false");
									return validationMap;
								}
								logger.info("Invalid CUST_MOBILE");
								validationMap.put(FieldType.RESPONSE_MESSAGE.getName(), "Invalid Customer Mobile");
								validationMap.put(FieldType.RESPONSE_CODE.getName(), "false");
								return validationMap;
							}
							logger.info("Invalid PAY_ID");
							validationMap.put(FieldType.RESPONSE_MESSAGE.getName(), "Invalid Merchant ID");
							validationMap.put(FieldType.RESPONSE_CODE.getName(), "false");
							return validationMap;
						}
						logger.info("Invalid TENURE");
						validationMap.put(FieldType.RESPONSE_MESSAGE.getName(), "Invalid Tenure");
						validationMap.put(FieldType.RESPONSE_CODE.getName(), "false");
						return validationMap;
					}
					logger.info("Invalid FREQUENCY");
					validationMap.put(FieldType.RESPONSE_MESSAGE.getName(), "Invalid Frequency");
					validationMap.put(FieldType.RESPONSE_CODE.getName(), "false");
					return validationMap;
				}
				logger.info("Invalid MONTHLY_AMOUNT");
				validationMap.put(FieldType.RESPONSE_MESSAGE.getName(), "Invalid Monthly Amount");
				validationMap.put(FieldType.RESPONSE_CODE.getName(), "false");
				return validationMap;
			}
			logger.info("Invalid AMOUNT");
			validationMap.put(FieldType.RESPONSE_MESSAGE.getName(), "Invalid Amount");
			validationMap.put(FieldType.RESPONSE_CODE.getName(), "false");
			return validationMap;
		}
		logger.info("Invalid ORDER_ID");
		validationMap.put(FieldType.RESPONSE_MESSAGE.getName(), "Invalid Request ID");
		validationMap.put(FieldType.RESPONSE_CODE.getName(), "false");
		return validationMap;
	}

	@SuppressWarnings("static-access")
	public String upiAutoPayMandateRequest() {
		try {
			logger.info("inside upiAutoPayMandateRequest forwardAction");
			Fields fields = new Fields();
			String[] commaSplitData = getORDER_ID().split("\\,");
			StringBuilder response = new StringBuilder();
			boolean flag = false;
			for (String responseData : commaSplitData) {
				if (!flag) {
					responseData = responseData.replaceAll(":", "");
					response.append(FieldType.ORDER_ID.getName() + "=:" + responseData);
					response.append(",");
					flag = true;
				} else {
					String[] colonSplit = responseData.split("=:");
					colonSplit[0] = colonSplit[0].replace("?", "");

					response.append(colonSplit[0] + "=:" + colonSplit[1].trim());
					response.append(",");
				}
			}
			response.deleteCharAt(response.length() - 1);
			String[] commaSplit = response.toString().split("\\,");

			for (String data : commaSplit) {
				String[] dataArr = data.split("\\=:");
				fields.put(dataArr[0], dataArr[1]);
			}

			Map<String, String> validateFields = ValidateRequestFields(fields);
			if (validateFields.get(FieldType.RESPONSE_CODE.getName()).equalsIgnoreCase("true")) {

				String merchantHash = fields.get("HASH");
				fields.remove("HASH");
				String calculatedHash = Hasher.getHash(fields);
				if (merchantHash.equals(calculatedHash)) {

					logger.info("merchant hash matched");
					User user = userDao.findPayId(fields.get(FieldType.PAY_ID.getName()));

					// for reseller sub merchant
					if (user.isSuperMerchant() == false && StringUtils.isNotBlank(user.getSuperMerchantId())
							&& StringUtils.isNotBlank(user.getResellerId())) {
						aaData.put(FieldType.PAY_ID.getName(), user.getSuperMerchantId());
						aaData.put(FieldType.SUB_MERCHANT_ID.getName(), fields.get(FieldType.PAY_ID.getName()));
						aaData.put(FieldType.RESELLER_ID.getName(), user.getResellerId());

					} else if (user.isSuperMerchant() == false && StringUtils.isNotBlank(user.getSuperMerchantId())) {
						// super merchantId
						aaData.put(FieldType.PAY_ID.getName(), user.getSuperMerchantId());
						// sub MerchantId
						aaData.put(FieldType.SUB_MERCHANT_ID.getName(), fields.get(FieldType.PAY_ID.getName()));

					} else if (StringUtils.isNotBlank(user.getResellerId())) {
						// Reseller Merchant
						aaData.put(FieldType.RESELLER_ID.getName(), user.getResellerId());
						aaData.put(FieldType.PAY_ID.getName(), fields.get(FieldType.PAY_ID.getName()));
						aaData.put(FieldType.SUB_MERCHANT_ID.getName(), "");

					} else {
						aaData.put(FieldType.PAY_ID.getName(), fields.get(FieldType.PAY_ID.getName()));
						aaData.put(FieldType.SUB_MERCHANT_ID.getName(), "");
					}

					StringBuilder base64EncodeImage = new StringBuilder();
					String finalLogoLocation = null;
					File imageLocation = new File(
							propertiesManager.propertiesMap.get(Constants.LOGO_FILE_UPLOAD_LOCATION.getValue()) + "/"
									+ aaData.get(FieldType.PAY_ID.getName()));
					if (!imageLocation.exists()) {
						logger.info("no such a directory for merchant logo ");
						base64EncodeImage.append(
								propertiesManager.propertiesMap.get(Constants.ICICI_ENACH_PAYMENT_GATEWAY_LOGO.getValue()));
					} else {

						String contents[] = imageLocation.list();
						finalLogoLocation = imageLocation.toString() + "/" + contents[0];
						if (contents[0].contains(".png")) {
							base64EncodeImage.append("data:image/png;base64,");
							base64EncodeImage.append(base64EncodeDecode.base64Encoder(new File(finalLogoLocation)));
						} else {
							base64EncodeImage.append("data:image/jpg;base64,");
							base64EncodeImage.append(base64EncodeDecode.base64Encoder(new File(finalLogoLocation)));
						}
					}

					boolean duplicateFlag = upiAutoPayDao.checkDuplicateOrderIdForRegistration(fields.get(FieldType.ORDER_ID.getName()),
							aaData.get(FieldType.PAY_ID.getName()), aaData.get(FieldType.SUB_MERCHANT_ID.getName()));

					if (!duplicateFlag) {

						String currentDate = DateCreater.defaultFromDate();
						fields.put(FieldType.DATE_FROM.getName(), currentDate);
						fields.put(FieldType.DATE_TO.getName(), currentDate);

						String totalAmount = String
								.valueOf(new BigDecimal(fields.get(FieldType.MONTHLY_AMOUNT.getName()))
										.multiply(new BigDecimal(fields.get(FieldType.TENURE.getName())))
										.setScale(2, RoundingMode.HALF_DOWN));

						SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
						Date date = sdf.parse(fields.get(FieldType.DATE_FROM.getName()));
						sdf = new SimpleDateFormat("yyyy-MM-dd");
						List<String> startEndDebitDate = null;

						if (fields.get(FieldType.FREQUENCY.getName()).equalsIgnoreCase("MT")) {

							startEndDebitDate = fieldsDao.getDueDateList(sdf.format(date),
									fields.get(FieldType.DATE_TO.getName()), fields.get(FieldType.TENURE.getName()),
									aaData.get(FieldType.PAY_ID.getName()),
									aaData.get(FieldType.SUB_MERCHANT_ID.getName()), "autoPay");

						} else if (fields.get(FieldType.FREQUENCY.getName()).equalsIgnoreCase("AS")) {

							startEndDebitDate = upiAutoPayDao.getDueDateListForWhenPresented(sdf.format(date),
									fields.get(FieldType.TENURE.getName()),
									aaData.get(FieldType.PAY_ID.getName()),
									aaData.get(FieldType.SUB_MERCHANT_ID.getName()));

						} else if (fields.get(FieldType.FREQUENCY.getName()).equalsIgnoreCase("QT")) {

							startEndDebitDate = upiAutoPayDao.getDueDateListForMonth(sdf.format(date),
									fields.get(FieldType.DATE_TO.getName()), fields.get(FieldType.TENURE.getName()), 3,
									aaData.get(FieldType.PAY_ID.getName()),
									aaData.get(FieldType.SUB_MERCHANT_ID.getName()), "autoPay");

						} else if (fields.get(FieldType.FREQUENCY.getName()).equalsIgnoreCase("HY")) {

							startEndDebitDate = upiAutoPayDao.getDueDateListForMonth(sdf.format(date),
									fields.get(FieldType.DATE_TO.getName()), fields.get(FieldType.TENURE.getName()), 6,
									aaData.get(FieldType.PAY_ID.getName()),
									aaData.get(FieldType.SUB_MERCHANT_ID.getName()), "autoPay");

						} else if (fields.get(FieldType.FREQUENCY.getName()).equalsIgnoreCase("YR")) {

							startEndDebitDate = upiAutoPayDao.getDueDateListForMonth(sdf.format(date),
									fields.get(FieldType.DATE_TO.getName()), fields.get(FieldType.TENURE.getName()), 12,
									aaData.get(FieldType.PAY_ID.getName()),
									aaData.get(FieldType.SUB_MERCHANT_ID.getName()), "autoPay");

						} else if (fields.get(FieldType.FREQUENCY.getName()).equalsIgnoreCase("BM")) {

							startEndDebitDate = upiAutoPayDao.getDueDateListForDays(sdf.format(date),
									fields.get(FieldType.DATE_TO.getName()), fields.get(FieldType.TENURE.getName()), 15,
									aaData.get(FieldType.PAY_ID.getName()),
									aaData.get(FieldType.SUB_MERCHANT_ID.getName()), "autoPay");

						} else if (fields.get(FieldType.FREQUENCY.getName()).equalsIgnoreCase("WK")) {

							startEndDebitDate = upiAutoPayDao.getDueDateListForDays(sdf.format(date),
									fields.get(FieldType.DATE_TO.getName()), fields.get(FieldType.TENURE.getName()), 7,
									aaData.get(FieldType.PAY_ID.getName()),
									aaData.get(FieldType.SUB_MERCHANT_ID.getName()), "autoPay");

						} else if (fields.get(FieldType.FREQUENCY.getName()).equalsIgnoreCase("DL")) {

							startEndDebitDate = upiAutoPayDao.getDueDateListForDays(sdf.format(date),
									fields.get(FieldType.DATE_TO.getName()), fields.get(FieldType.TENURE.getName()), 1,
									aaData.get(FieldType.PAY_ID.getName()),
									aaData.get(FieldType.SUB_MERCHANT_ID.getName()), "autoPay");
						} else {
							startEndDebitDate = upiAutoPayDao.getDueDateListForOneTime(sdf.format(date),
									fields.get(FieldType.DATE_TO.getName()), aaData.get(FieldType.PAY_ID.getName()),
									aaData.get(FieldType.SUB_MERCHANT_ID.getName()), "autoPay");
						}

						aaData.put("DEBIT_END_DATE", DateCreater.dateFormatReverse(startEndDebitDate.get(startEndDebitDate.size() - 1)));
						aaData.put("DEBIT_START_DATE", DateCreater.dateFormatReverse(startEndDebitDate.get(0)));

						aaData.put(FieldType.ORDER_ID.getName(), fields.get(FieldType.ORDER_ID.getName()));
						aaData.put(FieldType.MERCHANT_NAME.getName(),
								userDao.getBusinessNameByPayId(fields.get(FieldType.PAY_ID.getName())));
						aaData.put(FieldType.TENURE.getName(), fields.get(FieldType.TENURE.getName()));
						aaData.put(FieldType.PAY_ID.getName(), fields.get(FieldType.PAY_ID.getName()));
						aaData.put(FieldType.FREQUENCY.getName(),
								AutoPayFrequency.getAutoPayFrequencyName(fields.get(FieldType.FREQUENCY.getName())));
						aaData.put(FieldType.PURPOSE.getName(), fields.get(FieldType.PURPOSE.getName()));
						aaData.put(FieldType.AMOUNT.getName(),
								String.valueOf(new BigDecimal(fields.get(FieldType.AMOUNT.getName())).setScale(2,
										BigDecimal.ROUND_HALF_UP)));
						aaData.put(FieldType.TOTAL_AMOUNT.getName(),
								String.valueOf(new BigDecimal(totalAmount).setScale(2, BigDecimal.ROUND_HALF_UP)));
						aaData.put(FieldType.MONTHLY_AMOUNT.getName(),
								String.valueOf(new BigDecimal(fields.get(FieldType.MONTHLY_AMOUNT.getName()))
										.setScale(2, BigDecimal.ROUND_HALF_UP)));

						aaData.put("CUST_MOBILE", fields.get("CUST_MOBILE"));
						aaData.put(FieldType.CUST_EMAIL.getName(), fields.get(FieldType.CUST_EMAIL.getName()));

						aaData.put(FieldType.RETURN_URL.getName(),
								propertiesManager.propertiesMap.get(Constants.ICICI_ENACH_REPONSE.getValue()));

						aaData.put("MERCHANT_RETURN_URL", fields.get(FieldType.RETURN_URL.getName()));

						aaData.put("LOGO", base64EncodeImage.toString());
						aaData.put(FieldType.RESPONSE.getName(), SUCCESS);

					} else {
						aaData.put(FieldType.RESPONSE.getName(), ErrorType.DUPLICATE_ORDER_ID.getResponseMessage());
						aaData.put(FieldType.RETURN_URL.getName(),
								propertiesManager.propertiesMap.get(Constants.ICICI_ENACH_REPONSE.getValue()));
						aaData.put("MERCHANT_RETURN_URL", fields.get(FieldType.RETURN_URL.getName()));
						logger.info("merchant send a duplicate order id");
					}

				} else {
					logger.info("merchant hash not match");
					StringBuilder hashMessage = new StringBuilder("Merchant hash = ");
					hashMessage.append(merchantHash);
					hashMessage.append(", Calculated Hash = ");
					hashMessage.append(calculatedHash);
					MDC.put(FieldType.INTERNAL_CUSTOM_MDC.getName(), fields.getCustomMDC());
					logger.error(hashMessage.toString());

					aaData.put(FieldType.RESPONSE.getName(), Constants.TRANSACTIONSTATE_N.getValue());
					aaData.put(FieldType.RETURN_URL.getName(),
							propertiesManager.propertiesMap.get(Constants.ICICI_ENACH_REPONSE.getValue()));
					aaData.put("MERCHANT_RETURN_URL", fields.get(FieldType.RETURN_URL.getName()));
				}

			} else {

				switch (validateFields.get(FieldType.RESPONSE_MESSAGE.getName())) {

				case "Invalid Request ID":
					aaData.put(FieldType.RESPONSE.getName(), "Invalid Request ID");
					break;
				case "Invalid End Date":
					aaData.put(FieldType.RESPONSE.getName(), "Invalid End Date");
					break;
				case "Invalid Start Date":
					aaData.put(FieldType.RESPONSE.getName(), "Invalid Start Date");
					break;
				case "Invalid Amount":
					aaData.put(FieldType.RESPONSE.getName(), "Invalid Amount");
					break;
				case "Invalid Monthly Amount":
					aaData.put(FieldType.RESPONSE.getName(), "Invalid Monthly Amount");
					break;
				case "Invalid Frequency":
					aaData.put(FieldType.RESPONSE.getName(), "Invalid Frequency");
					break;
				case "Invalid Tenure":
					aaData.put(FieldType.RESPONSE.getName(), "Invalid Tenure");
					break;
				case "Invalid Merchant ID":
					aaData.put(FieldType.RESPONSE.getName(), "Invalid Merchant ID");
					break;
				case "Invalid Customer Mobile":
					aaData.put(FieldType.RESPONSE.getName(), "Invalid Customer Mobile");
					break;
				case "Invalid Customer Email":
					aaData.put(FieldType.RESPONSE.getName(), "Invalid Customer Email");
					break;
				default:
					break;
				}
				aaData.put(FieldType.RETURN_URL.getName(),
						propertiesManager.propertiesMap.get(Constants.ICICI_ENACH_REPONSE.getValue()));
				aaData.put("MERCHANT_RETURN_URL", fields.get(FieldType.RETURN_URL.getName()));
			}

		} catch (Exception ex) {
			logger.info("exception caught in upi AutoPay Mandate Request ", ex);
		}
		return SUCCESS;
	}

	public boolean isEditingpermission() {
		return editingpermission;
	}

	public void setEditingpermission(boolean editingpermission) {
		this.editingpermission = editingpermission;
	}

	public List<Merchants> getMerchantList() {
		return merchantList;
	}

	public void setMerchantList(List<Merchants> merchantList) {
		this.merchantList = merchantList;
	}

	public Map<String, String> getCurrencyMap() {
		return currencyMap;
	}

	public void setCurrencyMap(Map<String, String> currencyMap) {
		this.currencyMap = currencyMap;
	}

	public Map<String, String> getIndustryTypes() {
		return industryTypes;
	}

	public void setIndustryTypes(Map<String, String> industryTypes) {
		this.industryTypes = industryTypes;
	}

	public List<PromotionalPaymentType> getInvoiceSearchPromotionalPaymentType() {
		return invoiceSearchPromotionalPaymentType;
	}

	public void setInvoiceSearchPromotionalPaymentType(
			List<PromotionalPaymentType> invoiceSearchPromotionalPaymentType) {
		this.invoiceSearchPromotionalPaymentType = invoiceSearchPromotionalPaymentType;
	}

	public Map<String, String> getStatusType() {
		return statusType;
	}

	public void setStatusType(Map<String, String> statusType) {
		this.statusType = statusType;
	}

	public boolean isShowEpos() {
		return showEpos;
	}

	public void setShowEpos(boolean showEpos) {
		this.showEpos = showEpos;
	}

	public List<PermissionType> getListPermissionType() {
		return listPermissionType;
	}

	public void setListPermissionType(List<PermissionType> listPermissionType) {
		this.listPermissionType = listPermissionType;
	}

	public List<PermissionType> getSubUserPermissionType() {
		return subUserPermissionType;
	}

	public void setSubUserPermissionType(List<PermissionType> subUserPermissionType) {
		this.subUserPermissionType = subUserPermissionType;
	}

	public void setSubAdminList(List<SubAdmin> subAdminList) {
		this.subAdminList = subAdminList;
	}

	public List<MakerCheckerObj> getCheckerMakeList() {
		return checkerMakeList;
	}

	public void setCheckerMakeList(List<MakerCheckerObj> checkerMakeList) {
		this.checkerMakeList = checkerMakeList;
	}

	public List<Merchants> getSubMerchantList() {
		return subMerchantList;
	}

	public void setSubMerchantList(List<Merchants> subMerchantList) {
		this.subMerchantList = subMerchantList;
	}

	public String getORDER_ID() {
		return ORDER_ID;
	}

	public void setORDER_ID(String oRDER_ID) {
		ORDER_ID = oRDER_ID;
	}

	public Map<String, String> getAaData() {
		return aaData;
	}

	public void setAaData(Map<String, String> aaData) {
		this.aaData = aaData;
	}

	public String getSalt() {
		return salt;
	}

	public void setSalt(String salt) {
		this.salt = salt;
	}

	public boolean isSuperMerchantFlag() {
		return superMerchantFlag;
	}

	public void setSuperMerchantFlag(boolean superMerchantFlag) {
		this.superMerchantFlag = superMerchantFlag;
	}

	public boolean isSubMerchantFlag() {
		return subMerchantFlag;
	}

	public void setSubMerchantFlag(boolean subMerchantFlag) {
		this.subMerchantFlag = subMerchantFlag;
	}

	public List<Merchants> getSubMerchantsForQr() {
		return subMerchantsForQr;
	}

	public void setSubMerchantsForQr(List<Merchants> subMerchantsForQr) {
		this.subMerchantsForQr = subMerchantsForQr;
	}

	public boolean isMerchantInitiatedDirectFlag() {
		return merchantInitiatedDirectFlag;
	}

	public void setMerchantInitiatedDirectFlag(boolean merchantInitiatedDirectFlag) {
		this.merchantInitiatedDirectFlag = merchantInitiatedDirectFlag;
	}

	public boolean isAccountVerificationFlag() {
		return accountVerificationFlag;
	}

	public void setAccountVerificationFlag(boolean accountVerificationFlag) {
		this.accountVerificationFlag = accountVerificationFlag;
	}

	public boolean iseNachReportFlag() {
		return eNachReportFlag;
	}

	public void seteNachReportFlag(boolean eNachReportFlag) {
		this.eNachReportFlag = eNachReportFlag;
	}

	public boolean isVirtualAccountFlag() {
		return virtualAccountFlag;
	}

	public void setVirtualAccountFlag(boolean virtualAccountFlag) {
		this.virtualAccountFlag = virtualAccountFlag;
	}

	public boolean isVpaVerificationFlag() {
		return vpaVerificationFlag;
	}

	public void setVpaVerificationFlag(boolean vpaVerificationFlag) {
		this.vpaVerificationFlag = vpaVerificationFlag;
	}

	public boolean isBookingReportFlag() {
		return bookingReportFlag;
	}

	public void setBookingReportFlag(boolean bookingReportFlag) {
		this.bookingReportFlag = bookingReportFlag;
	}

	public boolean isCustomerQrFlag() {
		return customerQrFlag;
	}

	public void setCustomerQrFlag(boolean customerQrFlag) {
		this.customerQrFlag = customerQrFlag;
	}

	public boolean isCapturedMerchantFlag() {
		return capturedMerchantFlag;
	}

	public void setCapturedMerchantFlag(boolean capturedMerchantFlag) {
		this.capturedMerchantFlag = capturedMerchantFlag;
	}

	public boolean isUpiAutoPayReportFlag() {
		return upiAutoPayReportFlag;
	}

	public void setUpiAutoPayReportFlag(boolean upiAutoPayReportFlag) {
		this.upiAutoPayReportFlag = upiAutoPayReportFlag;
	}

	public String getPayId() {
		return payId;
	}

	public void setPayId(String payId) {
		this.payId = payId;
	}
}

