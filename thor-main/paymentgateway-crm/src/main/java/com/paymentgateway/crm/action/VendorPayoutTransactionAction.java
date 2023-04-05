package com.paymentgateway.crm.action;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.math.BigInteger;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.paymentgateway.commons.dao.MPADao;
import com.paymentgateway.commons.user.MerchantProcessingApplication;
import com.paymentgateway.commons.user.Merchants;
import com.paymentgateway.commons.user.PermissionType;
import com.paymentgateway.commons.user.Permissions;
import com.paymentgateway.commons.user.Roles;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.user.UserType;
import com.paymentgateway.commons.user.VendorPayouts;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.crm.mongoReports.TxnReports;

/**
 * @author Rajit
 */

public class VendorPayoutTransactionAction extends AbstractSecureAction {

	private static final long serialVersionUID = -3988109417890117050L;
	private static Logger logger = LoggerFactory.getLogger(VendorPayoutTransactionAction.class.getName());

	@Autowired
	private TxnReports txnReports;

	@Autowired
	private MPADao mpaDao;

	@Autowired
	private UserDao userDao;

	private String pgRefNum;
	private String orderId;
	private String skuCode;
	private String categoryCode;
	private String merchantPayId;
	private String subMerchantPayId;
	private String vendorPayId;
	private String paymentMethod;
	private String currency;
	private String date;
	private String txnType;
	private int length;
	private int start;
	private List<VendorPayouts> aaData;
	private String filename;
	private String fileName;
	private InputStream fileInputStream;
	private BigInteger recordsTotal;
	public BigInteger recordsFiltered;
	private Set<String> orderIdSet;

	private User sessionUser = new User();

	public String execute() {
		logger.info("inside VendorPayoutTransactionAction, execute function !!");
		try {
			sessionUser = (User) sessionMap.get(Constants.USER.getValue());

			int totalCount = 0;
			List<Merchants> vendorList = new ArrayList<Merchants>();
			List<MerchantProcessingApplication> vendorMpaList = new ArrayList<MerchantProcessingApplication>();
			if (sessionUser.getUserType().equals(UserType.MERCHANT)) {
				
				if(StringUtils.isNotEmpty(vendorPayId) && vendorPayId.contains("@")) {
					vendorPayId = userDao.findByEmailId(vendorPayId).getPayId();
				}
				
				if (vendorPayId.equalsIgnoreCase("ALL")) {

					if (StringUtils.isNotBlank(sessionUser.getSuperMerchantId())
							&& sessionUser.isSuperMerchant() == true) {
						vendorList = userDao.getSubUserList(subMerchantPayId);
						List<String> payIdList = new ArrayList<String>();
						for (Merchants vendor : vendorList) {
							payIdList.add("'" + vendor.getPayId() + "'");
						}
						vendorMpaList = mpaDao.fetchMPADataPerListPayId(payIdList);

						Set<String> pgRefVendorIdSet = new HashSet<String>();
						Object obj[] = txnReports.countVendorPayoutTransactions(pgRefNum, orderId, skuCode,
								categoryCode, sessionUser.getPayId(), vendorMpaList, paymentMethod, currency, txnType,
								date.toString(), start, length, vendorList);
						if (obj != null) {
							totalCount = (int) obj[0];
							pgRefVendorIdSet = (Set<String>) obj[1];
						}
						BigInteger bigInt = BigInteger.valueOf(totalCount);
						setRecordsTotal(bigInt);
						if (getLength() == -1) {
							setLength(getRecordsTotal().intValue());
						}

						aaData = txnReports.billingDetailsForVendorPayoutTransactions(
								txnReports.getVendorPayoutViewTransactions(orderId, skuCode, categoryCode,
										merchantPayId, paymentMethod, currency, txnType, date.toString(), start, length,
										pgRefVendorIdSet, sessionUser));
						recordsFiltered = recordsTotal;
					} else {
						
						vendorList = userDao.getSubUserList(sessionUser.getPayId());
						vendorMpaList = mpaDao.fetchMPADataPerPayId(vendorPayId);
						Set<String> pgRefVendorIdSet = new HashSet<String>();
						Object obj[] = txnReports.countVendorPayoutTransactions(pgRefNum, orderId, skuCode,
								categoryCode, sessionUser.getParentPayId(), vendorMpaList, paymentMethod, currency,
								txnType, date.toString(), start, length, vendorList);
						if (obj != null) {
							totalCount = (int) obj[0];
							pgRefVendorIdSet = (Set<String>) obj[1];
						}
						BigInteger bigInt = BigInteger.valueOf(totalCount);
						setRecordsTotal(bigInt);
						if (getLength() == -1) {
							setLength(getRecordsTotal().intValue());
						}

						aaData = txnReports.billingDetailsForVendorPayoutTransactions(
								txnReports.getVendorPayoutViewTransactions(orderId, skuCode, categoryCode,
										merchantPayId, paymentMethod, currency, txnType, date.toString(), start, length,
										pgRefVendorIdSet, sessionUser));
						recordsFiltered = recordsTotal;
					}

				} else {
					if (StringUtils.isNotBlank(sessionUser.getSuperMerchantId())
							&& sessionUser.isSuperMerchant() == true) {

						vendorList = createMerchantObject(userDao.findPayId(vendorPayId));
						vendorMpaList = mpaDao.fetchMPADataPerPayId(vendorPayId);
						Set<String> pgRefVendorIdSet = new HashSet<String>();
						Object obj[] = txnReports.countVendorPayoutTransactions(pgRefNum, orderId, skuCode,
								categoryCode, sessionUser.getPayId(), vendorMpaList, paymentMethod, currency, txnType,
								date.toString(), start, length, vendorList);
						if (obj != null) {
							totalCount = (int) obj[0];
							pgRefVendorIdSet = (Set<String>) obj[1];
						}
						BigInteger bigInt = BigInteger.valueOf(totalCount);
						setRecordsTotal(bigInt);
						if (getLength() == -1) {
							setLength(getRecordsTotal().intValue());
						}

						aaData = txnReports.billingDetailsForVendorPayoutTransactions(
								txnReports.getVendorPayoutViewTransactions(orderId, skuCode, categoryCode,
										merchantPayId, paymentMethod, currency, txnType, date.toString(), start, length,
										pgRefVendorIdSet, sessionUser));
						recordsFiltered = recordsTotal;

					} else if (StringUtils.isNotBlank(sessionUser.getSuperMerchantId())
							&& sessionUser.isSuperMerchant() == false) {

						vendorMpaList = mpaDao.fetchMPADataPerPayId(vendorPayId);
						Set<String> pgRefVendorIdSet = new HashSet<String>();
						Object obj[] = txnReports.countVendorPayoutTransactions(pgRefNum, orderId, skuCode,
								categoryCode, sessionUser.getParentPayId(), vendorMpaList, paymentMethod, currency,
								txnType, date.toString(), start, length, vendorList);
						if (obj != null) {
							totalCount = (int) obj[0];
							pgRefVendorIdSet = (Set<String>) obj[1];
						}
						BigInteger bigInt = BigInteger.valueOf(totalCount);
						setRecordsTotal(bigInt);
						if (getLength() == -1) {
							setLength(getRecordsTotal().intValue());
						}

						aaData = txnReports.billingDetailsForVendorPayoutTransactions(
								txnReports.getVendorPayoutViewTransactions(orderId, skuCode, categoryCode,
										merchantPayId, paymentMethod, currency, txnType, date.toString(), start, length,
										pgRefVendorIdSet, sessionUser));
						recordsFiltered = recordsTotal;
					}else {
						
					//	vendorList = userDao.getSubUserList(sessionUser.getPayId());
						vendorList = createMerchantObject(userDao.findPayId(vendorPayId));
						vendorMpaList = mpaDao.fetchMPADataPerPayId(vendorPayId);
						Set<String> pgRefVendorIdSet = new HashSet<String>();
						Object obj[] = txnReports.countVendorPayoutTransactions(pgRefNum, orderId, skuCode,
								categoryCode, sessionUser.getParentPayId(), vendorMpaList, paymentMethod, currency,
								txnType, date.toString(), start, length, vendorList);
						if (obj != null) {
							totalCount = (int) obj[0];
							pgRefVendorIdSet = (Set<String>) obj[1];
						}
						BigInteger bigInt = BigInteger.valueOf(totalCount);
						setRecordsTotal(bigInt);
						if (getLength() == -1) {
							setLength(getRecordsTotal().intValue());
						}

						aaData = txnReports.billingDetailsForVendorPayoutTransactions(
								txnReports.getVendorPayoutViewTransactions(orderId, skuCode, categoryCode,
										merchantPayId, paymentMethod, currency, txnType, date.toString(), start, length,
										pgRefVendorIdSet, sessionUser));
						recordsFiltered = recordsTotal;
					}

				}

			} else if (sessionUser.getUserType().equals(UserType.ADMIN)
					|| sessionUser.getUserType().equals(UserType.SUBADMIN)) {
				
				if(StringUtils.isNotEmpty(vendorPayId) && vendorPayId.contains("@")) {
					vendorPayId = userDao.findByEmailId(vendorPayId).getPayId();
				}
				
				if (merchantPayId.equalsIgnoreCase("ALL")) {
					
					vendorList = userDao.getAllSubUserList();

					List<String> payIdList = new ArrayList<String>();
					for (Merchants vendor : vendorList) {
						payIdList.add("'" + vendor.getPayId() + "'");
					}
					vendorMpaList = mpaDao.fetchMPADataPerListPayId(payIdList);
				} else {
					if (StringUtils.isNotEmpty(subMerchantPayId)) {
						
						if(subMerchantPayId.equalsIgnoreCase("ALL")) {
							vendorList = userDao.getSubUserList(merchantPayId);
						}else {
							if(StringUtils.isNotEmpty(vendorPayId)) {
								if(vendorPayId.equalsIgnoreCase("ALL")) {
									vendorList = userDao.getSubUserList(subMerchantPayId);
								}else {
									vendorList = createMerchantObject(userDao.findPayId(vendorPayId));
								}
							}
						}
					}else {
						if(vendorPayId.equalsIgnoreCase("ALL")) {
							vendorList = userDao.getSubUserList(merchantPayId);
						}else {
							if(StringUtils.isEmpty(vendorPayId)) {
								vendorList = userDao.getSubUserList(merchantPayId);
							}else {
								vendorList = createMerchantObject(userDao.findPayId(vendorPayId));
							}
						}
						
					}
			}

				totalCount = 0;
				Set<String> pgRefVendorIdSet = new HashSet<String>();
				Object obj[] = txnReports.countVendorPayoutTransactions(pgRefNum, orderId, skuCode, categoryCode,
						merchantPayId, vendorMpaList, paymentMethod, currency, txnType, date.toString(), start, length,
						vendorList);
				if (obj != null) {
					totalCount = (int) obj[0];
					pgRefVendorIdSet = (Set<String>) obj[1];
				}

				BigInteger bigInt = BigInteger.valueOf(totalCount);
				setRecordsTotal(bigInt);
				if (getLength() == -1) {
					setLength(getRecordsTotal().intValue());
				}

				aaData = txnReports.billingDetailsForVendorPayoutTransactions(
						txnReports.getVendorPayoutViewTransactions(orderId, skuCode, categoryCode, merchantPayId,
								paymentMethod, currency, txnType, date.toString(), start, length, pgRefVendorIdSet, sessionUser));
				recordsFiltered = recordsTotal;
			} else if (sessionUser.getUserType().equals(UserType.SUBUSER)) {

				if (!StringUtils.isEmpty(sessionUser.getSubUserType())
						&& sessionUser.getSubUserType().equalsIgnoreCase("ePosType")) {

					boolean hasPermissionForAll = hasPermissionForAll(sessionUser);
					if (!hasPermissionForAll) {
						vendorList = createMerchantObject(sessionUser);
					}

					merchantPayId = sessionUser.getParentPayId();
					String subUserId = sessionUser.getPayId();
					sessionUser = userDao.findPayId(sessionUser.getParentPayId());

					orderIdSet = txnReports.findBySubuserId(subUserId, merchantPayId);

				//	setMerchantPayId(sessionUser.getParentPayId());
					Set<String> pgRefVendorIdSet = new HashSet<String>();
					Object obj[] = txnReports.countVendorPayoutTransactions(pgRefNum, orderId, skuCode, categoryCode,
							merchantPayId, vendorMpaList, paymentMethod, currency, txnType, date.toString(), start,
							length, vendorList);
					if (obj != null) {
						totalCount = (int) obj[0];
						pgRefVendorIdSet = (Set<String>) obj[1];
					}
					BigInteger bigInt = BigInteger.valueOf(totalCount);
					setRecordsTotal(bigInt);
					if (getLength() == -1) {
						setLength(getRecordsTotal().intValue());
					}

					aaData = txnReports.billingDetailsForVendorPayoutTransactions(
							txnReports.getVendorPayoutViewTransactions(orderId, skuCode, categoryCode, merchantPayId,
									paymentMethod, currency, txnType, date.toString(), start, length,
									pgRefVendorIdSet, sessionUser));
					recordsFiltered = recordsTotal;

				} else if (!StringUtils.isEmpty(sessionUser.getSubUserType())
						&& sessionUser.getSubUserType().equalsIgnoreCase("normalType")) {

					boolean hasPermissionForAll = hasPermissionForAll(sessionUser);
					if (!hasPermissionForAll) {
						vendorList = createMerchantObject(sessionUser);
					} else {
						vendorList = userDao.getSubUserList(sessionUser.getParentPayId());
					}
					setMerchantPayId(sessionUser.getParentPayId());
					Set<String> pgRefVendorIdSet = new HashSet<String>();
					Object obj[] = txnReports.countVendorPayoutTransactions(pgRefNum, orderId, skuCode, categoryCode,
							merchantPayId, vendorMpaList, paymentMethod, currency, txnType, date.toString(), start,
							length, vendorList);
					if (obj != null) {
						totalCount = (int) obj[0];
						pgRefVendorIdSet = (Set<String>) obj[1];
					}
					BigInteger bigInt = BigInteger.valueOf(totalCount);
					setRecordsTotal(bigInt);
					if (getLength() == -1) {
						setLength(getRecordsTotal().intValue());
					}

					aaData = txnReports.billingDetailsForVendorPayoutTransactions(
							txnReports.getVendorPayoutViewTransactions(orderId, skuCode, categoryCode, merchantPayId,
									paymentMethod, currency, txnType, date.toString(), start, length,
									pgRefVendorIdSet, sessionUser));
					recordsFiltered = recordsTotal;

				} else if (!StringUtils.isEmpty(sessionUser.getSubUserType())
						&& sessionUser.getSubUserType().equalsIgnoreCase("vendorType")) {

					boolean hasPermissionForAll = hasPermissionForAll(sessionUser);
					if (!hasPermissionForAll) {
						vendorList = createMerchantObject(sessionUser);
					} else {
						vendorList = createMerchantObject(sessionUser);
					}

					setMerchantPayId(sessionUser.getParentPayId());
					Set<String> pgRefVendorIdSet = new HashSet<String>();
					Object obj[] = txnReports.countVendorPayoutTransactions(pgRefNum, orderId, skuCode, categoryCode,
							sessionUser.getParentPayId(), vendorMpaList, paymentMethod, currency, txnType,
							date.toString(), start, length, vendorList);
					if (obj != null) {
						totalCount = (int) obj[0];
						pgRefVendorIdSet = (Set<String>) obj[1];
					}
					BigInteger bigInt = BigInteger.valueOf(totalCount);
					setRecordsTotal(bigInt);
					if (getLength() == -1) {
						setLength(getRecordsTotal().intValue());
					}

					aaData = txnReports.billingDetailsForVendorPayoutTransactions(
							txnReports.getVendorPayoutViewTransactions(orderId, skuCode, categoryCode, merchantPayId,
									paymentMethod, currency, txnType, date.toString(), start, length,
									pgRefVendorIdSet, sessionUser));
					recordsFiltered = recordsTotal;

				}

			}

		} catch (Exception ex) {
			logger.error("Caught exception VendorPayoutTransactionAction, execute function " , ex);
			return ERROR;
		}
		return SUCCESS;
	}

	private boolean hasPermissionForAll(User sessionUser2) {
		Set<Roles> subUserRoles = sessionUser2.getRoles();
		for (Roles roles : subUserRoles) {
			Set<Permissions> permissions = roles.getPermissions();
			for (Permissions permission : permissions) {
				if (permission.getPermissionType().equals(PermissionType.SUB_USER_ALL)) {
					return true;
				}
			}
		}
		return false;
	}

	public String downloadVendorPayoutTransactions() {

		logger.info("inside VendorPayoutTransactionAction, downloadVendorPayoutTransactions function !!");

//		StringBuilder date = new StringBuilder();
//		date.append(date);
//		date.append(" 23:59:59");

		sessionUser = (User) sessionMap.get(Constants.USER.getValue());

		List<Merchants> vendorList = new ArrayList<Merchants>();
		List<MerchantProcessingApplication> vendorMpaList = new ArrayList<MerchantProcessingApplication>();
		List<VendorPayouts> vendorPayoutList = new ArrayList<VendorPayouts>();

		if (sessionUser.getUserType().equals(UserType.MERCHANT)) {
			int totalCount = 0;
			if (vendorPayId.equalsIgnoreCase("ALL")) {

				if (StringUtils.isNotBlank(sessionUser.getSuperMerchantId())
						&& sessionUser.isSuperMerchant() == true) {
					vendorList = userDao.getSubUserList(subMerchantPayId);
					List<String> payIdList = new ArrayList<String>();
					for (Merchants vendor : vendorList) {
						payIdList.add("'" + vendor.getPayId() + "'");
					}
					vendorMpaList = mpaDao.fetchMPADataPerListPayId(payIdList);

					Set<String> pgRefVendorIdSet = new HashSet<String>();
					Object obj[] = txnReports.countVendorPayoutTransactions(pgRefNum, orderId, skuCode,
							categoryCode, sessionUser.getPayId(), vendorMpaList, paymentMethod, currency, txnType,
							date.toString(), start, length, vendorList);
					if (obj != null) {
						totalCount = (int) obj[0];
						pgRefVendorIdSet = (Set<String>) obj[1];
					}
					BigInteger bigInt = BigInteger.valueOf(totalCount);
					setRecordsTotal(bigInt);
					if (getLength() == -1) {
						setLength(getRecordsTotal().intValue());
					}

					vendorPayoutList = txnReports.billingDetailsForVendorPayoutTransactions(
							txnReports.getVendorPayoutViewTransactions(orderId, skuCode, categoryCode,
									merchantPayId, paymentMethod, currency, txnType, date.toString(), start, length,
									pgRefVendorIdSet, sessionUser));
					recordsFiltered = recordsTotal;
				} else {
					
					vendorList = userDao.getSubUserList(sessionUser.getPayId());
					vendorMpaList = mpaDao.fetchMPADataPerPayId(vendorPayId);
					Set<String> pgRefVendorIdSet = new HashSet<String>();
					Object obj[] = txnReports.countVendorPayoutTransactions(pgRefNum, orderId, skuCode,
							categoryCode, sessionUser.getParentPayId(), vendorMpaList, paymentMethod, currency,
							txnType, date.toString(), start, length, vendorList);
					if (obj != null) {
						totalCount = (int) obj[0];
						pgRefVendorIdSet = (Set<String>) obj[1];
					}
					BigInteger bigInt = BigInteger.valueOf(totalCount);
					setRecordsTotal(bigInt);
					if (getLength() == -1) {
						setLength(getRecordsTotal().intValue());
					}

					vendorPayoutList = txnReports.billingDetailsForVendorPayoutTransactions(
							txnReports.getVendorPayoutViewTransactions(orderId, skuCode, categoryCode,
									merchantPayId, paymentMethod, currency, txnType, date.toString(), start, length,
									pgRefVendorIdSet, sessionUser));
					recordsFiltered = recordsTotal;
				}

			} else {
				if (StringUtils.isNotBlank(sessionUser.getSuperMerchantId())
						&& sessionUser.isSuperMerchant() == true) {

					vendorList = createMerchantObject(userDao.findPayId(vendorPayId));
					vendorMpaList = mpaDao.fetchMPADataPerPayId(vendorPayId);
					Set<String> pgRefVendorIdSet = new HashSet<String>();
					Object obj[] = txnReports.countVendorPayoutTransactions(pgRefNum, orderId, skuCode,
							categoryCode, sessionUser.getPayId(), vendorMpaList, paymentMethod, currency, txnType,
							date.toString(), start, length, vendorList);
					if (obj != null) {
						totalCount = (int) obj[0];
						pgRefVendorIdSet = (Set<String>) obj[1];
					}
					BigInteger bigInt = BigInteger.valueOf(totalCount);
					setRecordsTotal(bigInt);
					if (getLength() == -1) {
						setLength(getRecordsTotal().intValue());
					}

					vendorPayoutList = txnReports.billingDetailsForVendorPayoutTransactions(
							txnReports.getVendorPayoutViewTransactions(orderId, skuCode, categoryCode,
									merchantPayId, paymentMethod, currency, txnType, date.toString(), start, length,
									pgRefVendorIdSet, sessionUser));
					recordsFiltered = recordsTotal;

				} else if (StringUtils.isNotBlank(sessionUser.getSuperMerchantId())
						&& sessionUser.isSuperMerchant() == false) {

					vendorList = createMerchantObject(userDao.findPayId(vendorPayId));
					vendorMpaList = mpaDao.fetchMPADataPerPayId(vendorPayId);
					Set<String> pgRefVendorIdSet = new HashSet<String>();
					Object obj[] = txnReports.countVendorPayoutTransactions(pgRefNum, orderId, skuCode,
							categoryCode, sessionUser.getParentPayId(), vendorMpaList, paymentMethod, currency,
							txnType, date.toString(), start, length, vendorList);
					if (obj != null) {
						totalCount = (int) obj[0];
						pgRefVendorIdSet = (Set<String>) obj[1];
					}
					BigInteger bigInt = BigInteger.valueOf(totalCount);
					setRecordsTotal(bigInt);
					if (getLength() == -1) {
						setLength(getRecordsTotal().intValue());
					}

					vendorPayoutList = txnReports.billingDetailsForVendorPayoutTransactions(
							txnReports.getVendorPayoutViewTransactions(orderId, skuCode, categoryCode,
									merchantPayId, paymentMethod, currency, txnType, date.toString(), start, length,
									pgRefVendorIdSet, sessionUser));
					recordsFiltered = recordsTotal;
				}else {
					
				//	vendorList = userDao.getSubUserList(sessionUser.getPayId());
					vendorList = createMerchantObject(userDao.findPayId(vendorPayId));
					vendorMpaList = mpaDao.fetchMPADataPerPayId(vendorPayId);
					Set<String> pgRefVendorIdSet = new HashSet<String>();
					Object obj[] = txnReports.countVendorPayoutTransactions(pgRefNum, orderId, skuCode,
							categoryCode, sessionUser.getParentPayId(), vendorMpaList, paymentMethod, currency,
							txnType, date.toString(), start, length, vendorList);
					if (obj != null) {
						totalCount = (int) obj[0];
						pgRefVendorIdSet = (Set<String>) obj[1];
					}
					BigInteger bigInt = BigInteger.valueOf(totalCount);
					setRecordsTotal(bigInt);
					if (getLength() == -1) {
						setLength(getRecordsTotal().intValue());
					}

					vendorPayoutList = txnReports.billingDetailsForVendorPayoutTransactions(
							txnReports.getVendorPayoutViewTransactions(orderId, skuCode, categoryCode,
									merchantPayId, paymentMethod, currency, txnType, date.toString(), start, length,
									pgRefVendorIdSet, sessionUser));
					recordsFiltered = recordsTotal;
				}

			}

		} else if (sessionUser.getUserType().equals(UserType.ADMIN)
				|| sessionUser.getUserType().equals(UserType.SUBADMIN)) {
			if (merchantPayId.equalsIgnoreCase("ALL")) {
				
				vendorList = userDao.getAllSubUserList();

				List<String> payIdList = new ArrayList<String>();
				for (Merchants vendor : vendorList) {
					payIdList.add("'" + vendor.getPayId() + "'");
				}
				vendorMpaList = mpaDao.fetchMPADataPerListPayId(payIdList);
			} else {
				if (StringUtils.isNotEmpty(subMerchantPayId)) {
					
					if(subMerchantPayId.equalsIgnoreCase("ALL")) {
						vendorList = userDao.getSubUserList(merchantPayId);
					}else {
						if(vendorPayId.equalsIgnoreCase("ALL")) {
							vendorList = userDao.getSubUserList(subMerchantPayId);
						}else {
							vendorList = createMerchantObject(userDao.findPayId(vendorPayId));
						}
					}
				}else {
					if(vendorPayId.equalsIgnoreCase("ALL")) {
						vendorList = userDao.getSubUserList(merchantPayId);
					}else {
						vendorList = createMerchantObject(userDao.findPayId(vendorPayId));
					}
					
				}				
			}

			int totalCount = 0;
			Set<String> pgRefVendorIdSet = new HashSet<String>();
			Object obj[] = txnReports.countVendorPayoutTransactions(pgRefNum, orderId, skuCode, categoryCode,
					merchantPayId, vendorMpaList, paymentMethod, currency, txnType, date.toString(), start, length,
					vendorList);
			if (obj != null) {
				totalCount = (int) obj[0];
				pgRefVendorIdSet = (Set<String>) obj[1];
			}

			BigInteger bigInt = BigInteger.valueOf(totalCount);
			setRecordsTotal(bigInt);
			if (getLength() == -1) {
				setLength(getRecordsTotal().intValue());
			}

			vendorPayoutList = txnReports.billingDetailsForVendorPayoutTransactions(
					txnReports.getVendorPayoutViewTransactions(orderId, skuCode, categoryCode, merchantPayId,
							paymentMethod, currency, txnType, date.toString(), start, length, pgRefVendorIdSet,sessionUser));
			recordsFiltered = recordsTotal;

		} else if (sessionUser.getUserType().equals(UserType.SUBUSER)) {
			int totalCount = 0;
			if (!StringUtils.isEmpty(sessionUser.getSubUserType())
					&& sessionUser.getSubUserType().equalsIgnoreCase("ePosType")) {

				boolean hasPermissionForAll = hasPermissionForAll(sessionUser);
				if (!hasPermissionForAll) {
					vendorList = createMerchantObject(sessionUser);
				}

				merchantPayId = sessionUser.getParentPayId();
				String subUserId = sessionUser.getPayId();
				sessionUser = userDao.findPayId(sessionUser.getParentPayId());

				orderIdSet = txnReports.findBySubuserId(subUserId, merchantPayId);

			//	setMerchantPayId(sessionUser.getParentPayId());
				Set<String> pgRefVendorIdSet = new HashSet<String>();
				Object obj[] = txnReports.countVendorPayoutTransactions(pgRefNum, orderId, skuCode, categoryCode,
						merchantPayId, vendorMpaList, paymentMethod, currency, txnType, date.toString(), start, length,
						vendorList);
				if (obj != null) {
					totalCount = (int) obj[0];
					pgRefVendorIdSet = (Set<String>) obj[1];
				}
				BigInteger bigInt = BigInteger.valueOf(totalCount);
				setRecordsTotal(bigInt);
				if (getLength() == -1) {
					setLength(getRecordsTotal().intValue());
				}

				vendorPayoutList = txnReports.billingDetailsForVendorPayoutTransactions(
						txnReports.getVendorPayoutViewTransactions(orderId, skuCode, categoryCode, merchantPayId,
								paymentMethod, currency, txnType, date.toString(), start, length, pgRefVendorIdSet, sessionUser));
				recordsFiltered = recordsTotal;

			} else if (!StringUtils.isEmpty(sessionUser.getSubUserType())
					&& sessionUser.getSubUserType().equalsIgnoreCase("normalType")) {

				boolean hasPermissionForAll = hasPermissionForAll(sessionUser);
				if (!hasPermissionForAll) {
					vendorList = createMerchantObject(sessionUser);
				} else {
					vendorList = userDao.getSubUserList(sessionUser.getParentPayId());
				}
				setMerchantPayId(sessionUser.getParentPayId());
				Set<String> pgRefVendorIdSet = new HashSet<String>();
				Object obj[] = txnReports.countVendorPayoutTransactions(pgRefNum, orderId, skuCode, categoryCode,
						merchantPayId, vendorMpaList, paymentMethod, currency, txnType, date.toString(), start, length,
						vendorList);
				if (obj != null) {
					totalCount = (int) obj[0];
					pgRefVendorIdSet = (Set<String>) obj[1];
				}
				BigInteger bigInt = BigInteger.valueOf(totalCount);
				setRecordsTotal(bigInt);
				if (getLength() == -1) {
					setLength(getRecordsTotal().intValue());
				}

				vendorPayoutList = txnReports.billingDetailsForVendorPayoutTransactions(
						txnReports.getVendorPayoutViewTransactions(orderId, skuCode, categoryCode, merchantPayId,
								paymentMethod, currency, txnType, date.toString(), start, length, pgRefVendorIdSet, sessionUser));
				recordsFiltered = recordsTotal;

			} else if (!StringUtils.isEmpty(sessionUser.getSubUserType())
					&& sessionUser.getSubUserType().equalsIgnoreCase("vendorType")) {

				boolean hasPermissionForAll = hasPermissionForAll(sessionUser);
				if (!hasPermissionForAll) {
					vendorList = createMerchantObject(sessionUser);
				} else {
					vendorList = createMerchantObject(sessionUser);
				}

				setMerchantPayId(sessionUser.getParentPayId());
				Set<String> pgRefVendorIdSet = new HashSet<String>();
				Object obj[] = txnReports.countVendorPayoutTransactions(pgRefNum, orderId, skuCode, categoryCode,
						sessionUser.getParentPayId(), vendorMpaList, paymentMethod, currency, txnType, date.toString(),
						start, length, vendorList);
				if (obj != null) {
					totalCount = (int) obj[0];
					pgRefVendorIdSet = (Set<String>) obj[1];
				}
				BigInteger bigInt = BigInteger.valueOf(totalCount);
				setRecordsTotal(bigInt);
				if (getLength() == -1) {
					setLength(getRecordsTotal().intValue());
				}

				vendorPayoutList = txnReports.billingDetailsForVendorPayoutTransactions(
						txnReports.getVendorPayoutViewTransactions(orderId, skuCode, categoryCode, merchantPayId,
								paymentMethod, currency, txnType, date.toString(), start, length, pgRefVendorIdSet, sessionUser));
				recordsFiltered = recordsTotal;

			}
		}

		logger.info("List generated successfully for downloadVendorPayoutTransactionReport");

		SXSSFWorkbook wb = new SXSSFWorkbook(100);
		Row row;
		int rownum = 1;
		Sheet sheet = wb.createSheet("Vendor Payout Transaction Report");

		if (sessionUser.getUserType().equals(UserType.MERCHANT)) {

			row = sheet.createRow(0);

			row.createCell(0).setCellValue("Sr No");
			row.createCell(1).setCellValue("Vendor Name");
			row.createCell(2).setCellValue("Vendor Pay Id");
			row.createCell(3).setCellValue("Txn Type");
			row.createCell(4).setCellValue("Txn Id");
			row.createCell(5).setCellValue("Pg Ref Num");
			row.createCell(6).setCellValue("Order Id");
			row.createCell(7).setCellValue("Date");
			row.createCell(8).setCellValue("Vendor Payout Date");
			row.createCell(9).setCellValue("Payment Method");
			row.createCell(10).setCellValue("Payment Region");
			row.createCell(11).setCellValue("Card Holder Type");
			row.createCell(12).setCellValue("Card Mask");
			row.createCell(13).setCellValue("Cust Name");
			row.createCell(14).setCellValue("Cust Email");
			row.createCell(15).setCellValue("Cust Mobile");
			row.createCell(16).setCellValue("Payment Cycle");
			row.createCell(17).setCellValue("Base Amount");
			row.createCell(18).setCellValue("TDR / Surcharge");
			row.createCell(19).setCellValue("GST");
			row.createCell(20).setCellValue("Total Amount");
			row.createCell(21).setCellValue("Merchant Amount");

			for (VendorPayouts vnedorPayout : vendorPayoutList) {

				row = sheet.createRow(rownum++);
				vnedorPayout.setSrNo(String.valueOf(rownum - 1));

				Object[] objArr = vnedorPayout.DownloadVendorPayoutTransactionReportForMerchant();

				int cellnum = 0;
				for (Object obj : objArr) {
					// this line creates a cell in the next column of that row
					Cell cell = row.createCell(cellnum++);
					if (obj instanceof String)
						cell.setCellValue((String) obj);
					else if (obj instanceof Integer)
						cell.setCellValue((Integer) obj);
				}
			}

		} else {

			row = sheet.createRow(0);

			row.createCell(0).setCellValue("Sr No");
			row.createCell(1).setCellValue("Merchant");
			row.createCell(2).setCellValue("Vendor Name");
			row.createCell(3).setCellValue("Vendor Pay Id");
			row.createCell(4).setCellValue("Txn Type");
			row.createCell(5).setCellValue("Txn Id");
			row.createCell(6).setCellValue("Pg Ref Num");
			row.createCell(7).setCellValue("Order Id");
			row.createCell(8).setCellValue("Date");
			row.createCell(9).setCellValue("Vendor Payout Date");
			row.createCell(10).setCellValue("Payment Method");
			row.createCell(11).setCellValue("Payment Region");
			row.createCell(12).setCellValue("Card Holder Type");
			row.createCell(13).setCellValue("Card Mask");
			row.createCell(14).setCellValue("Cust Name");
			row.createCell(15).setCellValue("Cust Email");
			row.createCell(16).setCellValue("Cust Mobile");
			row.createCell(17).setCellValue("Payment Cycle");
			row.createCell(18).setCellValue("Base Amount");
			row.createCell(19).setCellValue("TDR / Surcharge");
			row.createCell(20).setCellValue("GST");
			row.createCell(21).setCellValue("Total Amount");
			row.createCell(22).setCellValue("Merchant Amount");

			for (VendorPayouts vnedorPayout : vendorPayoutList) {

				row = sheet.createRow(rownum++);
				vnedorPayout.setSrNo(String.valueOf(rownum - 1));

				Object[] objArr = vnedorPayout.DownloadVendorPayoutTransactionReport();

				int cellnum = 0;
				for (Object obj : objArr) {
					// this line creates a cell in the next column of that row
					Cell cell = row.createCell(cellnum++);
					if (obj instanceof String)
						cell.setCellValue((String) obj);
					else if (obj instanceof Integer)
						cell.setCellValue((Integer) obj);
				}
			}

		}
		try {
			/*
			 * String FILE_EXTENSION = ".xlsx"; DateFormat df = new
			 * SimpleDateFormat("yyyyMMddhhmmss"); filename =
			 * "Vendor_Payout_Transactions_Report_" + df.format(new Date()) +
			 * FILE_EXTENSION; File file = new File(filename);
			 * 
			 * // this Writes the workbook FileOutputStream out = new
			 * FileOutputStream(file); wb.write(out); out.flush(); out.close();
			 * wb.dispose(); fileInputStream = new FileInputStream(file);
			 * addActionMessage(filename + " written successfully on disk.");
			 */

			String FILE_EXTENSION = ".xlsx";
			DateFormat df = new SimpleDateFormat("yyyyMMddhhmmss");
			filename = "VendorPayoutTransactionsReport_" + df.format(new Date()) + FILE_EXTENSION;
			File file = new File(filename);

			// this Writes the workbook
			FileOutputStream out = new FileOutputStream(file);
			wb.write(out);
			out.flush();
			out.close();
			wb.dispose();
			fileInputStream = new FileInputStream(file);
			addActionMessage(filename + " written successfully on disk.");

		} catch (Exception exception) {
			logger.error("Exception while download vendorPayoutTransaction Report", exception);
			return ERROR;
		}
		logger.info("Files generated successfully for DownloadVendorPayoutTransactionReport ");
		return SUCCESS;
	}

	public List<Merchants> createMerchantObject(User user) {

		List<Merchants> merchantList = new ArrayList<Merchants>();

		Merchants merchant = new Merchants();
		merchant.setPayId(user.getPayId());
		merchant.setEmailId(user.getEmailId());
		merchant.setBusinessName(user.getBusinessName());
		if (user.getActivationDate() != null) {
			merchant.setRegistrationDate(user.getActivationDate().toString());
		}

		merchantList.add(merchant);

		return merchantList;
	}

	public String getPgRefNum() {
		return pgRefNum;
	}

	public void setPgRefNum(String pgRefNum) {
		this.pgRefNum = pgRefNum;
	}

	public String getOrderId() {
		return orderId;
	}

	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}

	public String getSkuCode() {
		return skuCode;
	}

	public void setSkuCode(String skuCode) {
		this.skuCode = skuCode;
	}

	public String getCategoryCode() {
		return categoryCode;
	}

	public void setCategoryCode(String categoryCode) {
		this.categoryCode = categoryCode;
	}

	public String getMerchantPayId() {
		return merchantPayId;
	}

	public void setMerchantPayId(String merchantPayId) {
		this.merchantPayId = merchantPayId;
	}

	public String getVendorPayId() {
		return vendorPayId;
	}

	public void setVendorPayId(String vendorPayId) {
		this.vendorPayId = vendorPayId;
	}

	public String getPaymentMethod() {
		return paymentMethod;
	}

	public void setPaymentMethod(String paymentMethod) {
		this.paymentMethod = paymentMethod;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public int getLength() {
		return length;
	}

	public void setLength(int length) {
		this.length = length;
	}

	public int getStart() {
		return start;
	}

	public void setStart(int start) {
		this.start = start;
	}

	public List<VendorPayouts> getAaData() {
		return aaData;
	}

	public void setAaData(List<VendorPayouts> aaData) {
		this.aaData = aaData;
	}

	public String getTxnType() {
		return txnType;
	}

	public void setTxnType(String txnType) {
		this.txnType = txnType;
	}

	public InputStream getFileInputStream() {
		return fileInputStream;
	}

	public void setFileInputStream(InputStream fileInputStream) {
		this.fileInputStream = fileInputStream;
	}

	public BigInteger getRecordsTotal() {
		return recordsTotal;
	}

	public void setRecordsTotal(BigInteger recordsTotal) {
		this.recordsTotal = recordsTotal;
	}

	public BigInteger getRecordsFiltered() {
		return recordsFiltered;
	}

	public void setRecordsFiltered(BigInteger recordsFiltered) {
		this.recordsFiltered = recordsFiltered;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public Set<String> getOrderIdSet() {
		return orderIdSet;
	}

	public void setOrderIdSet(Set<String> orderIdSet) {
		this.orderIdSet = orderIdSet;
	}

	public String getSubMerchantPayId() {
		return subMerchantPayId;
	}

	public void setSubMerchantPayId(String subMerchantPayId) {
		this.subMerchantPayId = subMerchantPayId;
	}

}
