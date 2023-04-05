package com.paymentgateway.crm.action;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.paymentgateway.commons.dao.ChargingDetailsFactory;
import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.user.AccountCurrencyRegion;
import com.paymentgateway.commons.user.CardHolderType;
import com.paymentgateway.commons.user.ChargingDetails;
import com.paymentgateway.commons.user.Surcharge;
import com.paymentgateway.commons.user.SurchargeDao;
import com.paymentgateway.commons.user.TransactionSearch;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.user.UserType;
import com.paymentgateway.commons.util.AcquirerType;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.CrmFieldConstants;
import com.paymentgateway.commons.util.CrmFieldType;
import com.paymentgateway.commons.util.CrmValidator;
import com.paymentgateway.commons.util.DateCreater;
import com.paymentgateway.commons.util.MopType;
import com.paymentgateway.commons.util.PaymentType;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.TransactionType;

public class TransactionSummaryReportAction extends AbstractSecureAction {

	private static Logger logger = LoggerFactory.getLogger(TransactionSummaryReportAction.class.getName());
	private static final long serialVersionUID = -3131381841294843726L;

	private String dateFrom;
	private String dateTo;
	public String paymentMethods;
	public String acquirer;
	private String merchantEmailId;
	private String subMerchantEmailId;
	private String currency;
	private int draw;
	private int length;
	private int start;
	private String transactionType;
	private BigInteger recordsTotal;
	public BigInteger recordsFiltered;
	private String paymentsRegion;
	private String cardHolderType;
	private String mopType;
	private String pgRefNum;
	private String partSettleFlag;
	private String transactionFlag;
	private int count = 1;
	private List<TransactionSearch> aaData = new ArrayList<TransactionSearch>();
	public static final BigDecimal ONE_HUNDRED = new BigDecimal(100);
	List<Surcharge> surchargeList = new ArrayList<Surcharge>();
	SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.ENGLISH);

	@Autowired
	private SummaryReportQuery summaryReportQuery;

	@Autowired
	private SurchargeDao surchargeDao;

	@Autowired
	private UserDao userDao;

	@Autowired
	ChargingDetailsFactory cdf;

	@SuppressWarnings("unused")
	public String execute() {
		int totalCount = 0;

		logger.info("Inside search summary report Action");
		User sessionUser = (User) sessionMap.get(Constants.USER.getValue());
		List<TransactionSearch> transactionList = new ArrayList<TransactionSearch>();
		List<TransactionSearch> transactionPaginationList = new ArrayList<TransactionSearch>();
		List<TransactionSearch> transactionList1 = new ArrayList<TransactionSearch>();

		if (StringUtils.isBlank(acquirer)) {
			acquirer = "ALL";
		}

		setDateFrom(DateCreater.toDateTimeformatCreater(dateFrom));
		setDateTo(DateCreater.formDateTimeformatCreater(dateTo));

		try {

			Date date1 = format.parse(dateFrom);
			Date date2 = format.parse(dateTo);

			surchargeList = surchargeDao.findAllSurchargeByDate(date1, date2);

		} catch (Exception e) {
			logger.error("Exception " , e);
		}

		//String merchantPayId = null;
		
		String merchantPayId = "";
		String subMerchantPayId = "";
		if(sessionUser.isSuperMerchant() == false && StringUtils.isNotBlank(sessionUser.getSuperMerchantId())) {
		
			merchantPayId = sessionUser.getSuperMerchantId();
			subMerchantPayId = sessionUser.getPayId();
		
		} else if(!merchantEmailId.equalsIgnoreCase("All")) {
			
			User merchant = userDao.findPayIdByEmail(merchantEmailId);
			merchantPayId = merchant.getPayId();

			if (StringUtils.isNotBlank(subMerchantEmailId) && !subMerchantEmailId.equalsIgnoreCase("All")) {
				subMerchantPayId = userDao.getPayIdByEmailId(subMerchantEmailId);
			} else {
				subMerchantPayId = subMerchantEmailId;
			}
		} else {
			merchantPayId = merchantEmailId;
		}
		
		
		
		if (sessionUser.getUserType().equals(UserType.SUPERADMIN) || sessionUser.getUserType().equals(UserType.ADMIN)
				|| sessionUser.getUserType().equals(UserType.SUBADMIN)
				|| sessionUser.getUserType().equals(UserType.ASSOCIATE)) {

			/*if (!merchantEmailId.equalsIgnoreCase("All")) {
				User merchant = userDao.findPayIdByEmail(merchantEmailId);
				merchantPayId = merchant.getPayId();
			} else {
				merchantPayId = merchantEmailId;
			}*/
			try {
				transactionPaginationList = summaryReportQuery.summaryReport(dateFrom, dateTo, merchantPayId, subMerchantPayId,
						paymentMethods, acquirer, currency, sessionUser, getStart(), getLength(), getPaymentsRegion(),
						getCardHolderType(), pgRefNum, mopType, transactionType, partSettleFlag, transactionFlag);

				logger.info("Inside search summary report Action , transactionPaginationList Size = "
						+ transactionPaginationList.size());
				totalCount = summaryReportQuery.summaryReportRecord(dateFrom, dateTo, merchantPayId, subMerchantPayId, paymentMethods,
						acquirer, currency, sessionUser, getPaymentsRegion(), getCardHolderType(), pgRefNum, mopType,
						transactionType, transactionFlag);

				logger.info("Inside search summary report Action , totalCount = " + totalCount);
			} catch (SystemException exception) {
				logger.error("Exception", exception);
			}
			// transactionList1.addAll(findDetails(transactionPaginationList));
			BigInteger bigInt = BigInteger.valueOf(totalCount);
			setRecordsTotal(bigInt);
			if (getLength() == -1) {
				setLength(getRecordsTotal().intValue());
			}
			setAaData(transactionPaginationList);
			recordsFiltered = getRecordsTotal();
			return SUCCESS;
		} else if (sessionUser.getUserType().equals(UserType.MERCHANT)
				|| sessionUser.getUserType().equals(UserType.SUBUSER)) {

			/*if (!merchantEmailId.equalsIgnoreCase("All")) {

				User merchant = userDao.findPayIdByEmail(merchantEmailId);
				merchantPayId = merchant.getPayId();*/
				try {
					transactionPaginationList = summaryReportQuery.summaryReportMerchant(getDateFrom(), getDateTo(),
							merchantPayId, subMerchantPayId, getPaymentMethods(), getCurrency(), sessionUser, getStart(), getLength(), transactionFlag);
					
					totalCount = summaryReportQuery.summaryReportRecord(dateFrom, dateTo, merchantPayId, subMerchantPayId, paymentMethods,
							acquirer, currency, sessionUser, getPaymentsRegion(), getCardHolderType(), pgRefNum,
							mopType, transactionType, transactionFlag);
				} catch (SystemException exception) {
					logger.error("Exception", exception);
				}
				transactionList1.addAll(findDetails(transactionPaginationList));

				BigInteger bigInt = BigInteger.valueOf(totalCount);
				setRecordsTotal(bigInt);
				if (getLength() == -1) {
					setLength(getRecordsTotal().intValue());
				}
				setAaData(transactionList1);
				recordsFiltered = recordsTotal;
				return SUCCESS;
			//}
		} else if (sessionUser.getUserType().equals(UserType.RESELLER)) {
		/*	if (!merchantEmailId.equalsIgnoreCase("All")) {
				User merchant = userDao.findPayIdByEmail(merchantEmailId);
				merchantPayId = merchant.getPayId();*/
				try {
					transactionPaginationList = summaryReportQuery.summaryReport(dateFrom, dateTo, merchantPayId, subMerchantPayId,
							paymentMethods, acquirer, currency, sessionUser, getStart(), getLength(),
							getPaymentsRegion(), getCardHolderType(), pgRefNum, mopType, transactionType, partSettleFlag, transactionFlag);

					totalCount = summaryReportQuery.summaryReportRecord(dateFrom, dateTo, merchantPayId, subMerchantPayId, paymentMethods,
							acquirer, currency, sessionUser, getPaymentsRegion(), getCardHolderType(), pgRefNum,
							mopType, transactionType, transactionFlag);
				} catch (SystemException exception) {
					logger.error("Exception", exception);
				}

				transactionList1.addAll(findDetails(transactionList));

				BigInteger bigInt = BigInteger.valueOf(transactionPaginationList.size());
				setRecordsTotal(bigInt);
				if (getLength() == -1) {
					setLength(getRecordsTotal().intValue());
				}
				setAaData(transactionList1);
				recordsFiltered = recordsTotal;
				return SUCCESS;
			//}

		}

		else if (sessionUser.getUserType().equals(UserType.ACQUIRER)
				|| sessionUser.getUserType().equals(UserType.SUBACQUIRER)) {

			if (!merchantEmailId.equalsIgnoreCase("All")) {

				User merchant = userDao.findPayIdByEmail(merchantEmailId);
				merchantPayId = merchant.getPayId();

				try {
					transactionPaginationList = summaryReportQuery.summaryReport(dateFrom, dateTo, merchantPayId, subMerchantPayId,
							paymentMethods, acquirer, currency, sessionUser, getStart(), getLength(),
							getPaymentsRegion(), getCardHolderType(), pgRefNum, mopType, transactionType, partSettleFlag, transactionFlag);

					totalCount = summaryReportQuery.summaryReportRecord(dateFrom, dateTo, merchantPayId, subMerchantPayId, paymentMethods,
							acquirer, currency, sessionUser, getPaymentsRegion(), getCardHolderType(), pgRefNum,
							mopType, transactionType, transactionFlag);
				} catch (SystemException exception) {
					logger.error("Exception", exception);
				}

				transactionList1.addAll(findDetails(transactionList));

				BigInteger bigInt = BigInteger.valueOf(totalCount);
				setRecordsTotal(bigInt);
				if (getLength() == -1) {
					setLength(getRecordsTotal().intValue());
				}
				setAaData(transactionList1);
				recordsFiltered = recordsTotal;
				return SUCCESS;
			}

		}

		return SUCCESS;
	}

	public void validate() {
		CrmValidator validator = new CrmValidator();

		if (validator.validateBlankField(getCurrency())) {
		} else if (!validator.validateField(CrmFieldType.CURRENCY, getCurrency())) {
			addFieldError(CrmFieldType.CURRENCY.getName(), ErrorType.INVALID_FIELD.getResponseMessage());
		}

		if (validator.validateBlankField(getDateFrom())) {
		} else if (!validator.validateField(CrmFieldType.DATE_FROM, getDateFrom())) {
			addFieldError(CrmFieldType.DATE_FROM.getName(), ErrorType.INVALID_FIELD.getResponseMessage());
		}

		if (validator.validateBlankField(getDateTo())) {
		} else if (!validator.validateField(CrmFieldType.DATE_TO, getDateTo())) {
			addFieldError(CrmFieldType.DATE_TO.getName(), ErrorType.INVALID_FIELD.getResponseMessage());
		}

		if (!validator.validateBlankField(getDateTo())) {
			if (DateCreater.formatStringToDate(DateCreater.formatFromDate(getDateFrom()))
					.compareTo(DateCreater.formatStringToDate(DateCreater.formatFromDate(getDateTo()))) > 0) {
				addFieldError(CrmFieldType.DATE_FROM.getName(), CrmFieldConstants.FROMTO_DATE_VALIDATION.getValue());
			} else if (DateCreater.diffDate(getDateFrom(), getDateTo()) > 31) {
				addFieldError(CrmFieldType.DATE_FROM.getName(), CrmFieldConstants.DATE_RANGE.getValue());
			}
		}

		if (validator.validateBlankField(getMerchantEmailId())
				|| getMerchantEmailId().equals(CrmFieldConstants.ALL.getValue())) {
		} else if (!validator.validateField(CrmFieldType.MERCHANT_EMAIL_ID, getMerchantEmailId())) {
			addFieldError(CrmFieldType.MERCHANT_EMAIL_ID.getName(), ErrorType.INVALID_FIELD.getResponseMessage());
		}

	}

	public TdrPojo chargiesCalculation(double bankTdr, double pgFixCharge, double pgTdr, double bankFixCharge,
			double merchantFixCharge, double merchantTdr, BigDecimal merchantServiceTax, String amount) {
		TdrPojo tdrPojo = new TdrPojo();
		BigDecimal totalamount = null;

		BigDecimal bankTdr1 = new BigDecimal(bankTdr);
		BigDecimal bankFixCharge1 = new BigDecimal(bankFixCharge);
		BigDecimal pgFixCharge1 = new BigDecimal(pgFixCharge);
		BigDecimal pgTdr1 = new BigDecimal(pgTdr);
		BigDecimal merchantFixCharge1 = new BigDecimal(merchantFixCharge);
		BigDecimal merchantTdr1 = new BigDecimal(merchantTdr);
		// BigDecimal merchantServiceTax1 = new BigDecimal(merchantServiceTax) ;

		if (!StringUtils.isEmpty(amount)) {
			totalamount = new BigDecimal(amount);
		}
		// Bank tdr
		BigDecimal bankPrTdr = (totalamount.multiply(bankTdr1).divide(ONE_HUNDRED, 2, RoundingMode.HALF_DOWN));
		BigDecimal bankTdrCalculate = bankPrTdr.add(bankFixCharge1).setScale(2, RoundingMode.HALF_DOWN);
		BigDecimal totalGstOnBank = bankTdrCalculate.multiply(merchantServiceTax).divide(ONE_HUNDRED, 2,
				RoundingMode.HALF_DOWN);
		BigDecimal bankPayout = bankTdrCalculate.add(totalGstOnBank).setScale(2, RoundingMode.HALF_DOWN);

		// pg TDR
		BigDecimal pgPrTdr = (totalamount.multiply(pgTdr1).divide(ONE_HUNDRED, 2, RoundingMode.HALF_DOWN));
		BigDecimal pgTdrCalculate = pgPrTdr.add(pgFixCharge1).setScale(2, RoundingMode.HALF_DOWN);
		BigDecimal totalGstOnPG = (pgTdrCalculate.multiply(merchantServiceTax).divide(ONE_HUNDRED, 2,
				RoundingMode.HALF_DOWN));
		BigDecimal pgPayout = pgTdrCalculate.add(totalGstOnPG).setScale(2, RoundingMode.HALF_DOWN);

		// merchant TDR
		BigDecimal merchantPrTdr = (totalamount.multiply(merchantTdr1).divide(ONE_HUNDRED, 2, RoundingMode.HALF_DOWN));
		BigDecimal merchantTdrCalculate = merchantPrTdr.add(merchantFixCharge1).setScale(2, RoundingMode.HALF_DOWN);
		BigDecimal totalGstOnMerchant = (merchantTdrCalculate.multiply(merchantServiceTax).divide(ONE_HUNDRED, 2,
				RoundingMode.HALF_DOWN));
		BigDecimal merchantPayout = merchantTdrCalculate.add(totalGstOnMerchant).setScale(2, RoundingMode.HALF_DOWN);
		BigDecimal netMerchantAmount = totalamount.subtract(merchantPayout).setScale(2, RoundingMode.HALF_DOWN);

		String totalAggregatorCommisn = String.valueOf(pgPayout);
		String merchantTotalTdr = String.valueOf(merchantTdrCalculate);
		String netMerchantPayableAmount = String.valueOf(netMerchantAmount);
		String totalGstAmount = String.valueOf(totalGstOnMerchant);
		String totalAmtPaytoMerchant = String.valueOf((totalamount.subtract(pgPayout)).subtract(bankPayout));
		String totalPayoutfrmNodal = String.valueOf((totalamount.subtract(bankPayout)));

		tdrPojo.setTotalAggregatorCommisn(totalAggregatorCommisn);
		tdrPojo.setTotalGstOnMerchant(totalGstAmount);
		tdrPojo.setTotalAmtPaytoMerchant(totalAmtPaytoMerchant);
		tdrPojo.setTotalPayoutfrmNodal(totalPayoutfrmNodal);
		tdrPojo.setMerchantTdrCalculate(merchantTotalTdr);
		tdrPojo.setNetMerchantPayableAmount(netMerchantPayableAmount);
		tdrPojo.setTotalAmount(amount);
		return tdrPojo;

	}

	public List<TransactionSearch> findDetails(List<TransactionSearch> transactionList) {

		List<TransactionSearch> transactionList1 = new ArrayList<TransactionSearch>();
		Map<String, List<Surcharge>> surchargeMap = new HashMap<String, List<Surcharge>>();
		Map<String, User> userMap = new HashMap<String, User>();
		BigDecimal merchantGstAmount = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_DOWN);
		BigDecimal acquirerGstAmount = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_DOWN);
		TdrPojo tdrPojo = new TdrPojo();
		BigDecimal st = null;
		String bussinessType = null;
		String bussinessName = "";
		for (TransactionSearch transactionSearch : transactionList) {
			String payId = transactionSearch.getPayId();
			if (!StringUtils.isBlank(payId)) {

				User user = new User();

				if (userMap.get(payId) != null) {
					user = userMap.get(payId);
				} else {
					user = userDao.findPayId(payId);
					userMap.put(payId, user);
				}

				String amount = transactionSearch.getAmount();
				bussinessType = user.getIndustryCategory();
				bussinessName = user.getBusinessName();

				st = new BigDecimal(PropertiesManager.propertiesMap.get("SERVICE_TAX"));
				st = st.setScale(2, RoundingMode.HALF_DOWN);

				if (!StringUtils.isBlank(transactionSearch.getSurchargeFlag())) {
					if (transactionSearch.getSurchargeFlag().equals("Y")) {
						String txnAmount = transactionSearch.getAmount();
						String surchargeAmount = transactionSearch.getTotalAmount();
						BigDecimal nettxnAmount = new BigDecimal(txnAmount);

						PaymentType paymentType = PaymentType
								.getInstanceUsingCode(transactionSearch.getPaymentMethods());

						if (paymentType == null) {
							logger.info("Payment Type is null for Pg Ref Num " + transactionSearch.getPgRefNum());
							continue;
						}

						AcquirerType acquirerType = AcquirerType
								.getInstancefromCode(transactionSearch.getAcquirerType());

						if (acquirerType == null) {
							logger.info("acquirerType is null for Pg Ref Num " + transactionSearch.getPgRefNum());
							continue;
						}

						MopType mopType = MopType.getmop(transactionSearch.getMopType());

						if (mopType == null) {
							logger.info("mopType is null for Pg Ref Num " + transactionSearch.getPgRefNum());
							continue;
						}

						String paymentsRegion = transactionSearch.getTransactionRegion();

						if (paymentsRegion == null) {
							paymentsRegion = AccountCurrencyRegion.DOMESTIC.toString();
						}

						/*
						 * StringBuilder surchargeIdentifier = new StringBuilder();
						 * surchargeIdentifier.append(payId);
						 * surchargeIdentifier.append(paymentType.getName());
						 * surchargeIdentifier.append(acquirerType.getName());
						 * surchargeIdentifier.append(mopType.getName());
						 * surchargeIdentifier.append(paymentsRegion);
						 * 
						 * if (surchargeMap.get(surchargeIdentifier.toString()) != null &&
						 * surchargeMap.get(surchargeIdentifier.toString()).size() > 0 ) { surchargeList
						 * = surchargeMap.get(surchargeIdentifier.toString());
						 * 
						 * } else { surchargeList =
						 * surchargeDao.findSurchargeListByPayIdAcquirerNameRegion(payId,
						 * paymentType.getName(), acquirerType.getName(), mopType.getName(),
						 * paymentsRegion); surchargeMap.put(surchargeIdentifier.toString(),
						 * surchargeList); }
						 * 
						 * Surcharge surcharge = surchargeList.get(0);
						 * 
						 * if (surcharge == null) {
						 * 
						 * logger.info("surcharge is null for Pg Ref Num " +
						 * transactionSearch.getPgRefNum()); continue; }
						 */

						Date surchargeStartDate = null;
						Date surchargeEndDate = null;
						Date settlementDate = null;
						Surcharge surcharge = new Surcharge();

						try {
							for (Surcharge surchargeData : surchargeList) {

								if (AcquirerType.getInstancefromName(surchargeData.getAcquirerName()).toString()
										.equalsIgnoreCase(transactionSearch.getAcquirerType())
										&& surchargeData.getPaymentType().getCode()
												.equalsIgnoreCase(transactionSearch.getPaymentMethods())
										&& surchargeData.getMopType().getCode()
												.equalsIgnoreCase(transactionSearch.getMopType())
										&& surchargeData.getPaymentsRegion().name()
												.equalsIgnoreCase(transactionSearch.getTransactionRegion())
										&& surchargeData.getPayId().equalsIgnoreCase(transactionSearch.getPayId())) {

									surchargeStartDate = format.parse(surchargeData.getCreatedDate().toString());
									surchargeEndDate = format.parse(surchargeData.getUpdatedDate().toString());
									if (surchargeStartDate.compareTo(surchargeEndDate) == 0) {
										surchargeEndDate = new Date();
									}

									settlementDate = format.parse(transactionSearch.getDateFrom());

									if (settlementDate.compareTo(surchargeStartDate) >= 0
											&& settlementDate.compareTo(surchargeEndDate) <= 0) {
										surcharge = surchargeData;
										break;
									} else {
										continue;
									}

								}
							}
						} catch (Exception e) {
							logger.error("Exception " , e);
						}

						if (surcharge.getBankSurchargeAmountCustomer() == null
								|| surcharge.getBankSurchargePercentageCustomer() == null
								|| surcharge.getBankSurchargeAmountCommercial() == null
								|| surcharge.getBankSurchargePercentageCommercial() == null) {

							logger.info("Surcharge is null for apyid = " + transactionSearch.getPayId() + " acquirer = "
									+ transactionSearch.getAcquirerType() + " mop = " + transactionSearch.getMopType()
									+ "  paymentType = " + transactionSearch.getPaymentMethods() + "  paymentRegion = "
									+ transactionSearch.getTransactionRegion());
							continue;
						}

						BigDecimal bankSurchargeFC;
						BigDecimal bankSurchargePercent;

						if (transactionSearch.getCardHolderType() == null
								|| transactionSearch.getCardHolderType().isEmpty()) {

							bankSurchargeFC = surcharge.getBankSurchargeAmountCustomer();
							bankSurchargePercent = surcharge.getBankSurchargePercentageCustomer();
						}

						else if (transactionSearch.getCardHolderType()
								.equalsIgnoreCase(CardHolderType.CONSUMER.toString())) {

							bankSurchargeFC = surcharge.getBankSurchargeAmountCustomer();
							bankSurchargePercent = surcharge.getBankSurchargePercentageCustomer();
						} else {

							bankSurchargeFC = surcharge.getBankSurchargeAmountCommercial();
							bankSurchargePercent = surcharge.getBankSurchargePercentageCommercial();
						}

						BigDecimal netsurchargeAmount = new BigDecimal(surchargeAmount);

						BigDecimal netcalculatedSurcharge = netsurchargeAmount.subtract(nettxnAmount);
						netcalculatedSurcharge = netcalculatedSurcharge.setScale(2, RoundingMode.HALF_DOWN);

						BigDecimal gstCalculate = netcalculatedSurcharge.multiply(st).divide(((ONE_HUNDRED).add(st)), 2,
								RoundingMode.HALF_DOWN);

						BigDecimal pgSurchargeAmount;
						BigDecimal acquirerSurchargeAmount;

						if (netcalculatedSurcharge.equals(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_DOWN))) {
							pgSurchargeAmount = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_DOWN);
							acquirerSurchargeAmount = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_DOWN);
						}

						else {

							acquirerSurchargeAmount = nettxnAmount.multiply(bankSurchargePercent)
									.divide(((ONE_HUNDRED)), 2, RoundingMode.HALF_DOWN);
							acquirerSurchargeAmount = acquirerSurchargeAmount.add(bankSurchargeFC);

							pgSurchargeAmount = netcalculatedSurcharge.subtract(acquirerSurchargeAmount);
							pgSurchargeAmount = pgSurchargeAmount.subtract(gstCalculate);
							pgSurchargeAmount = pgSurchargeAmount.setScale(2, RoundingMode.HALF_DOWN);

						}

						logger.info("PG Surcharge Amount = " + pgSurchargeAmount);
						logger.info("Acquirer Surcharge Amount = " + acquirerSurchargeAmount);

						BigDecimal totalSurcharge = netcalculatedSurcharge.subtract(gstCalculate);
						BigDecimal totalAmtPaytoMerchant = netsurchargeAmount
								.subtract(gstCalculate.add(totalSurcharge));

						acquirerGstAmount = acquirerSurchargeAmount.multiply(st).divide(ONE_HUNDRED, 2,
								RoundingMode.HALF_DOWN);

						merchantGstAmount = pgSurchargeAmount.multiply(st).divide(ONE_HUNDRED, 2,
								RoundingMode.HALF_DOWN);

						String gstCalculateString = String.valueOf(gstCalculate);
						String totalSurchargeString = String.valueOf(totalSurcharge);
						String totalAmtPaytoMerchantString = String.valueOf(totalAmtPaytoMerchant);
						tdrPojo.setTotalAmtPaytoMerchant(totalAmtPaytoMerchantString);
						tdrPojo.setTotalGstOnMerchant(gstCalculateString);
						tdrPojo.setNetMerchantPayableAmount(totalAmtPaytoMerchantString);
						tdrPojo.setMerchantTdrCalculate(totalSurchargeString);
						tdrPojo.setTotalAmount(surchargeAmount);
						tdrPojo.setAcquirerSurchargeAmount(String.valueOf(acquirerSurchargeAmount));
						tdrPojo.setPgSurchargeAmount(String.valueOf(pgSurchargeAmount));
					}
				} else {

					ChargingDetails chargingDetails = cdf.getChargingDetailForReport(transactionSearch.getDateFrom(),
							payId, transactionSearch.getAcquirerType(), transactionSearch.getPaymentMethods(),
							transactionSearch.getMopType(), transactionSearch.getTxnType(),
							transactionSearch.getCurrency());
					tdrPojo = chargiesCalculation(chargingDetails.getBankTDR(), chargingDetails.getPgFixCharge(),
							chargingDetails.getPgTDR(), chargingDetails.getBankFixCharge(),
							chargingDetails.getMerchantFixCharge(), chargingDetails.getMerchantTDR(), st, amount);
				}

				if (!tdrPojo.equals(null)) {

					if (transactionSearch.getTxnType().contains(TransactionType.REFUND.getName())) {
						transactionSearch.setTxnType(TransactionType.REFUND.getName());
					} else {
						transactionSearch.setTxnType(TransactionType.SALE.getName());
					}

					transactionSearch.setTotalGstOnMerchant(tdrPojo.getTotalGstOnMerchant());
					transactionSearch.setNetMerchantPayableAmount(tdrPojo.getNetMerchantPayableAmount());
					transactionSearch.setMerchantTdrCalculate(tdrPojo.getMerchantTdrCalculate());
					transactionSearch.setAmount(tdrPojo.getTotalAmount());
					transactionSearch.setBusinessName(bussinessName);
					transactionSearch.setAcquirerSurchargeAmount(tdrPojo.getAcquirerSurchargeAmount());
					transactionSearch.setPgSurchargeAmount(tdrPojo.getPgSurchargeAmount());
					transactionSearch.setTotalGstOnMerchant(String.valueOf(merchantGstAmount));
					transactionSearch.setTotalGstOnAcquirer(String.valueOf(acquirerGstAmount));
					transactionSearch.setSrNo(count++);
				}
				transactionList1.add(transactionSearch);
			}
		}
		return transactionList1;
	}

	public String getDateFrom() {
		return dateFrom;
	}

	public void setDateFrom(String dateFrom) {
		this.dateFrom = dateFrom;
	}

	public String getDateTo() {
		return dateTo;
	}

	public void setDateTo(String dateTo) {
		this.dateTo = dateTo;
	}

	public List<TransactionSearch> getAaData() {
		return aaData;
	}

	public void setAaData(List<TransactionSearch> aaData) {
		this.aaData = aaData;
	}

	public String getAcquirer() {
		return acquirer;
	}

	public void setAcquirer(String acquirer) {
		this.acquirer = acquirer;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

	public String getMerchantEmailId() {
		return merchantEmailId;
	}

	public void setMerchantEmailId(String merchantEmailId) {
		this.merchantEmailId = merchantEmailId;
	}

	public int getDraw() {
		return draw;
	}

	public void setDraw(int draw) {
		this.draw = draw;
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

	public String getPaymentMethods() {
		return paymentMethods;
	}

	public void setPaymentMethods(String paymentMethods) {
		this.paymentMethods = paymentMethods;
	}

	public String getPaymentsRegion() {
		return paymentsRegion;
	}

	public void setPaymentsRegion(String paymentsRegion) {
		this.paymentsRegion = paymentsRegion;
	}

	public String getCardHolderType() {
		return cardHolderType;
	}

	public void setCardHolderType(String cardHolderType) {
		this.cardHolderType = cardHolderType;
	}

	public String getPgRefNum() {
		return pgRefNum;
	}

	public void setPgRefNum(String pgRefNum) {
		this.pgRefNum = pgRefNum;
	}

	public String getMopType() {
		return mopType;
	}

	public void setMopType(String mopType) {
		this.mopType = mopType;
	}

	public String getTransactionType() {
		return transactionType;
	}

	public void setTransactionType(String transactionType) {
		this.transactionType = transactionType;
	}

	public String getPartSettleFlag() {
		return partSettleFlag;
	}

	public void setPartSettleFlag(String partSettleFlag) {
		this.partSettleFlag = partSettleFlag;
	}
	public String getSubMerchantEmailId() {
		return subMerchantEmailId;
	}

	public void setSubMerchantEmailId(String subMerchantEmailId) {
		this.subMerchantEmailId = subMerchantEmailId;
	}

	public String getTransactionFlag() {
		return transactionFlag;
	}

	public void setTransactionFlag(String transactionFlag) {
		this.transactionFlag = transactionFlag;
	}
	
}
