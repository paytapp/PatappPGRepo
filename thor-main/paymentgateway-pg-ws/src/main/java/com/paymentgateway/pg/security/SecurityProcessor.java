package com.paymentgateway.pg.security;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mongodb.BasicDBObject;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.paymentgateway.commons.api.SmsSender;
import com.paymentgateway.commons.dao.FieldsDao;
import com.paymentgateway.commons.dao.MPADao;
import com.paymentgateway.commons.dao.SUFDetailDao;
import com.paymentgateway.commons.dao.UserSettingDao;
import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.kms.EncryptDecryptService;
import com.paymentgateway.commons.mongo.MongoInstance;
import com.paymentgateway.commons.user.Account;
import com.paymentgateway.commons.user.AccountCurrency;
import com.paymentgateway.commons.user.AccountCurrencyRegion;
import com.paymentgateway.commons.user.CardHolderType;
import com.paymentgateway.commons.user.ChargingDetails;
import com.paymentgateway.commons.user.ChargingDetailsDao;
import com.paymentgateway.commons.user.MerchantProcessingApplication;
import com.paymentgateway.commons.user.SUFDetail;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.user.UserSettingData;
import com.paymentgateway.commons.user.UserType;
import com.paymentgateway.commons.util.AcquirerType;
import com.paymentgateway.commons.util.AcquirerTypeService;
import com.paymentgateway.commons.util.Amount;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.ModeType;
import com.paymentgateway.commons.util.MopType;
import com.paymentgateway.commons.util.PaymentType;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.RecieptBatchGenerator;
import com.paymentgateway.commons.util.StaticDataProvider;
import com.paymentgateway.commons.util.StatusType;
import com.paymentgateway.commons.util.TransactionType;
import com.paymentgateway.commons.util.onUsOffUs;
import com.paymentgateway.pg.core.pageintegrator.GeneralValidator;
import com.paymentgateway.pg.core.security.Authenticator;
import com.paymentgateway.pg.core.util.Processor;
import com.paymentgateway.pg.history.Historian;

@Service("securityProcessor")
public class SecurityProcessor implements Processor {

	@Autowired
	private Authenticator authenticator;

	@Autowired
	private Historian historian;

	@Autowired
	private GeneralValidator generalValidator;

	@Autowired
	EncryptDecryptService encryptDecryptService;

	@Autowired
	private AcquirerTypeService acquirerTypeService;

	@Autowired
	private UserDao userDao;

	@Autowired
	private ChargingDetailsDao chargingDetailsDao;

	@Autowired
	private SmsSender smsSender;

	@Autowired
	private MongoInstance mongoInstance;

	@Autowired
	private MPADao mpaDao;

	@Autowired
	private StaticDataProvider staticDataProvider;

	@Autowired
	private PropertiesManager propertiesManager;

	@Autowired
	private RecieptBatchGenerator recieptBatchGenerator;

	@Autowired
	private SUFDetailDao sufDetailDao;

	@Autowired
	private UserSettingDao userSettingDao;

	@Autowired
	private FieldsDao fieldsDao;

	private static int smsCount = 0;

	private static Logger logger = LoggerFactory.getLogger(SecurityProcessor.class.getName());
	// private static Map<String, User> userMap = new HashMap<String, User>();
	// private static Map<String, ChargingDetails> chargingDetailsMap = new
	// HashMap<String, ChargingDetails>();
	private static BigDecimal minAmountSlab2 = new BigDecimal(
			PropertiesManager.propertiesMap.get("LimitSlab2MinAmount"));
	private static BigDecimal minAmountSlab3 = new BigDecimal(
			PropertiesManager.propertiesMap.get("LimitSlab3MinAmount"));

	public void preProcess(Fields fields) throws SystemException {

	}

	public void process(Fields fields) throws SystemException {
		// Process validations
		validate(fields);//

		fields.addDefaultFields();

		fields.logAllFields("Refined Request:");
		// Process authenticity
		authenticate(fields);

		// check super merchant condition for refund and status enquiry
		validateSuperMerchantDetails(fields);

		// check parent merchant condition for refund and status inquiry
		validateParentMerchants(fields);

		addPreviousFields(fields);

		// Check if request has already been submitted
		// checkDuplicateSubmit(fields);
		validateDupicateOrderId(fields);

		// Compare refund amount
		compareRefundAmount(fields);
		// Get applicable acquirer and respective fields
		addAcquirerFields(fields);

		addTransactionDataFields(fields);

		generalValidator.processorValidations(fields);
	}

	public void validate(Fields fields) throws SystemException {
		Processor validationProcessor = new ValidationProcessor();
		validationProcessor.preProcess(fields);
		validationProcessor.process(fields);
		validationProcessor.postProcess(fields);
	}

	public void addPreviousFields(Fields fields) throws SystemException {
		// Ideally previous fields are responsibility of history processor,
		// but we are putting it here for smart router
		String txntype = fields.get(FieldType.TXNTYPE.getName());
		if (txntype.equals(TransactionType.STATUS.getName())) {
			historian.findPreviousForStatus(fields);
		} else if (txntype.equals(TransactionType.RECO.getName())
				|| txntype.equals(TransactionType.REFUNDRECO.getName())) {
			historian.findPreviousForReco(fields);
		} else if (txntype.equals(TransactionType.VERIFY.getName())) {
			historian.findPreviousForVerify(fields);
		} else {
			historian.findPrevious(fields);
		}
		historian.populateFieldsFromPrevious(fields);
	}

	public void compareRefundAmount(Fields fields) throws SystemException {

		String txnType = fields.get(FieldType.TXNTYPE.getName());
		String origTxnType = fields.get(FieldType.INTERNAL_ORIG_TXN_TYPE.getName());

		if (StringUtils.isEmpty(origTxnType)) {
			origTxnType = "";
		}

		if (txnType.equalsIgnoreCase(TransactionType.REFUND.getName())
				&& !origTxnType.equalsIgnoreCase(TransactionType.STATUS.getName())) {

			String responseCode = fields.get(FieldType.RESPONSE_CODE.getName());
			if (responseCode.equals(ErrorType.REFUND_DENIED.getCode())) {
				throw new SystemException(ErrorType.REFUND_DENIED, "");
			} else if (responseCode.equals(ErrorType.REFUND_REJECTED.getCode())) {
				throw new SystemException(ErrorType.REFUND_REJECTED, "");
			} else if (responseCode.equals(ErrorType.TRANSACTION_NOT_FOUND.getCode())) {
				throw new SystemException(ErrorType.TRANSACTION_NOT_FOUND, "");
			}

		} else {
			return;
		}
	}

	public void validateSuperMerchantDetails(Fields fields) throws SystemException {
		String txntype = fields.get(FieldType.TXNTYPE.getName());
		if (txntype.equals(TransactionType.STATUS.getName()) || txntype.equals(TransactionType.REFUND.getName())) {

			User user = null;
			if (PropertiesManager.propertiesMap.get(Constants.USE_STATIC_DATA.getValue()) != null
					&& PropertiesManager.propertiesMap.get(Constants.USE_STATIC_DATA.getValue())
							.equalsIgnoreCase(Constants.Y_FLAG.getValue())) {

				user = staticDataProvider.getUserData(fields.get(FieldType.SUB_MERCHANT_ID.getName()));
			} else {
				user = userDao.findPayId(fields.get(FieldType.PAY_ID.getName()));
			}
			if (user != null && StringUtils.isNotBlank(user.getSuperMerchantId())) {
				fields.put(FieldType.PAY_ID.getName(), user.getSuperMerchantId());
				fields.put(FieldType.SUB_MERCHANT_ID.getName(), user.getPayId());
				fields.put(FieldType.IS_SUB_MERCHANT.getName(), "Y");
			}
		}

	}

	public void validateParentMerchants(Fields fields) throws SystemException {
		String txntype = fields.get(FieldType.TXNTYPE.getName());
		if (txntype.equals(TransactionType.STATUS.getName()) || txntype.equals(TransactionType.REFUND.getName())) {

			User user = null;
			user = userDao.findPayId(fields.get(FieldType.PAY_ID.getName()));

			if (user != null && user.getUserType() == UserType.PARENTMERCHANT) {
				Fields newFields = fieldsDao.getPreviousForPgRefNum(fields.get(FieldType.PG_REF_NUM.getName()));
				fields.put(FieldType.PARENT_PAY_ID.getName(), newFields.get(FieldType.PARENT_PAY_ID.getName()));
				fields.put(FieldType.PAY_ID.getName(), newFields.get(FieldType.PAY_ID.getName()));
				fields.put(FieldType.IS_PARENT_MERCHANT.getName(), "Y");
			}
		}

	}

	public void checkDuplicateSubmit(Fields fields) throws SystemException {

		if (StringUtils.isNotBlank(fields.get(FieldType.TXNTYPE.getName()))
				&& StringUtils.isBlank(fields.get(FieldType.ACQ_ID.getName()))) {

			if (fields.get(FieldType.TXNTYPE.getName()).equalsIgnoreCase(TransactionType.ENROLL.getName())
					|| fields.get(FieldType.TXNTYPE.getName()).equalsIgnoreCase(TransactionType.SALE.getName())) {
				historian.validateDuplicateSubmit(fields);
			}

		}

	}

	public void validateDupicateOrderId(Fields fields) throws SystemException {

		String origTxnType = fields.get(FieldType.INTERNAL_ORIG_TXN_TYPE.getName());
		if (StringUtils.isEmpty(origTxnType)) {
			origTxnType = "";
		}

		if (((fields.get(FieldType.TXNTYPE.getName()).equals(TransactionType.NEWORDER.getName())))
				|| ((fields.get(FieldType.TXNTYPE.getName()).equals(TransactionType.REFUND.getName()))
						&& (!origTxnType.equalsIgnoreCase(TransactionType.STATUS.getName())))) {
			historian.validateDuplicateOrderId(fields);
		}
		if (!origTxnType.equalsIgnoreCase(TransactionType.STATUS.getName())) {
			if (((fields.get(FieldType.TXNTYPE.getName()).equals(TransactionType.SALE.getName())))
					|| ((fields.get(FieldType.TXNTYPE.getName()).equals(TransactionType.ENROLL.getName())))) {
				if (StringUtils.isNotBlank(fields.get(FieldType.PG_REF_NUM.getName()))
						&& StringUtils.isNotBlank(fields.get(FieldType.ACQ_ID.getName()))) {
					historian.validateDuplicateOrderId(fields);
				}
			}
		}
	}

	public void addTransactionDataFields(Fields fields) throws SystemException {

		// Return for STATUS check
		if ((fields.get(FieldType.TXNTYPE.getName()).equals(TransactionType.STATUS.getName()))
				|| (fields.get(FieldType.INTERNAL_ORIG_TXN_TYPE.getName()).equals(TransactionType.STATUS.getName()))) {

			return;
		}
		// Zero Surcharge / TDR for Refund Transactions
		if ((fields.get(FieldType.TXNTYPE.getName()).equals(TransactionType.REFUND.getName()))) {

			fields.put(FieldType.ACQUIRER_TDR_SC.getName(), "0.00");
			fields.put(FieldType.ACQUIRER_GST.getName(), "0.00");
			fields.put(FieldType.PG_TDR_SC.getName(), "0.00");
			fields.put(FieldType.PG_GST.getName(), "0.00");

			fields.put(FieldType.RESELLER_CHARGES.getName(), "0.00");
			fields.put(FieldType.RESELLER_GST.getName(), "0.00");

			fields.put(FieldType.PG_RESELLER_CHARGE.getName(), "0.00");
			fields.put(FieldType.PG_RESELLER_GST.getName(), "0.00");

			fields.put(FieldType.MERCHANT_TDR_SC.getName(), "0.00");
			fields.put(FieldType.MERCHANT_GST.getName(), "0.00");

			return;
		}

		// Calculate values only for Sale Transactions
		if (!(fields.get(FieldType.TXNTYPE.getName()).equals(TransactionType.ENROLL.getName()))) {
			if (!(fields.get(FieldType.TXNTYPE.getName()).equals(TransactionType.SALE.getName()))) {
				return;
			}

		}

		User user = null;

		// Decide whether to use static usermap or get data from DB
		if (PropertiesManager.propertiesMap.get(Constants.USE_STATIC_DATA.getValue()) != null
				&& PropertiesManager.propertiesMap.get(Constants.USE_STATIC_DATA.getValue())
						.equalsIgnoreCase(Constants.Y_FLAG.getValue())) {

			user = staticDataProvider.getUserData(fields.get(FieldType.PAY_ID.getName()));

		} else {
			user = userDao.findPayId(fields.get(FieldType.PAY_ID.getName()));

		}

		UserSettingData userSetting = userSettingDao.fetchDataUsingPayId((fields.get(FieldType.PAY_ID.getName())));
		// code for part settlement
		boolean partSettleFlag = checkDailyLimit(user, fields, userSetting);

		if (partSettleFlag) {
			fields.put(FieldType.PART_SETTLE.getName(), Constants.N_FLAG.getValue());
		} else if (userSetting.isAllowPartSettle()) {
			fields.put(FieldType.PART_SETTLE.getName(), Constants.Y_FLAG.getValue());
		} else {
			fields.put(FieldType.PART_SETTLE.getName(), Constants.N_FLAG.getValue());
		}
		// End of part settlement
		if (StringUtils.isBlank(fields.get(FieldType.SLAB_ID.getName()))) {
			String slabId = "";
			BigDecimal txnAmount = new BigDecimal(Amount.toDecimal(fields.get(FieldType.AMOUNT.getName()),
					fields.get(FieldType.CURRENCY_CODE.getName())));

			if (txnAmount.compareTo(minAmountSlab3) >= 0) {
				slabId = "03";
			} else if (txnAmount.compareTo(minAmountSlab2) >= 0) {
				slabId = "02";
			} else {
				slabId = "01";
			}
			fields.put(FieldType.SLAB_ID.getName(), slabId);
		}
		String amountString = Amount.toDecimal(fields.get(FieldType.AMOUNT.getName()),
				fields.get(FieldType.CURRENCY_CODE.getName()));

		BigDecimal st = new BigDecimal(PropertiesManager.propertiesMap.get("SERVICE_TAX"));
		BigDecimal amount = new BigDecimal(amountString);

		String payId = fields.get(FieldType.PAY_ID.getName());
		String paymentType = fields.get(FieldType.PAYMENT_TYPE.getName());
		String acquirer = fields.get(FieldType.ACQUIRER_TYPE.getName());
		String mopType = fields.get(FieldType.MOP_TYPE.getName());
		String transactionType = fields.get(FieldType.TXNTYPE.getName());
		if (transactionType.equalsIgnoreCase(TransactionType.ENROLL.getName())
				|| transactionType.equalsIgnoreCase(TransactionType.SALE.getName())) {

			transactionType = TransactionType.SALE.getCode();
		}
		String currency = fields.get(FieldType.CURRENCY_CODE.getName());

		ChargingDetails chargingDetails = null;
		onUsOffUs acquiringMode = onUsOffUs.valueOf(fields.get(FieldType.ACQUIRER_MODE.getName()));
		AccountCurrencyRegion paymentsRegion = AccountCurrencyRegion
				.valueOf(fields.get(FieldType.PAYMENTS_REGION.getName()));
		CardHolderType cardHolder = CardHolderType.valueOf(fields.get(FieldType.CARD_HOLDER_TYPE.getName()));

		if (PropertiesManager.propertiesMap.get(Constants.USE_STATIC_DATA.getValue()) != null
				&& PropertiesManager.propertiesMap.get(Constants.USE_STATIC_DATA.getValue())
						.equalsIgnoreCase(Constants.Y_FLAG.getValue())) {

			chargingDetails = staticDataProvider.getChargingDetailsData(payId, paymentType, acquirer, mopType,
					transactionType, currency, fields.get(FieldType.PAYMENTS_REGION.getName()),
					fields.get(FieldType.CARD_HOLDER_TYPE.getName()), fields.get(FieldType.ACQUIRER_MODE.getName()),
					fields.get(FieldType.SLAB_ID.getName()));

		}

		else {

			PaymentType paymentTypeIns = PaymentType.getInstanceUsingCode(paymentType);
			MopType mopTypeIns = MopType.getmop(mopType);
			TransactionType transactionTypeIns = TransactionType.getInstanceFromCode(transactionType);
			String acquirerName = AcquirerType.getAcquirerName(acquirer);

			if (paymentTypeIns != null && mopTypeIns != null && transactionTypeIns != null
					&& StringUtils.isNotBlank(acquirerName)) {
				chargingDetails = chargingDetailsDao.findChargingDetail(mopTypeIns, paymentTypeIns, transactionTypeIns,
						acquirerName, currency, payId, acquiringMode, paymentsRegion, cardHolder,
						fields.get(FieldType.SLAB_ID.getName()));

			} else {
				throw new SystemException(ErrorType.CHARGINGDETAIL_NOT_FETCHED,
						"Unable to calculate TDR for this transaction due to missing charging Details");
			}
		}

		// Flag to select charge (TDR or FC) whichever is higher for merchant
		boolean chargesFlag = chargingDetails.isChargesFlag();

		BigDecimal acquirerTdr = null;
		BigDecimal acquirerGst = null;
		BigDecimal pgTdr = null;
		BigDecimal pgGst = null;
		BigDecimal merchantTdr = null;
		BigDecimal merchantGst = null;
		BigDecimal resellerTdr = null;
		BigDecimal resellerGst = null;

		// for SUF changes
		BigDecimal sufTdr = null;
		BigDecimal sufGst = null;
		List<SUFDetail> sufCharge = new ArrayList<SUFDetail>();

		AcquirerType acqType = AcquirerType.getInstancefromCode(acquirer);
		String gstCalculationSlab = "0";
		switch (acqType) {
		case BOB:
			gstCalculationSlab = PropertiesManager.propertiesMap.get("BOBGSTSLAB");
			break;
		case FSSPAY:
			gstCalculationSlab = PropertiesManager.propertiesMap.get("FSSPAYGSTSLAB");
			break;
		case BILLDESK:
			gstCalculationSlab = PropertiesManager.propertiesMap.get("BILLDESKGSTSLAB");
			break;
		case IDFC_FIRSTDATA:
			gstCalculationSlab = PropertiesManager.propertiesMap.get("IDFCGSTSLAB");
			break;
		case IDBIBANK:
			gstCalculationSlab = PropertiesManager.propertiesMap.get("IDBIGSTSLAB");
			break;
		case PAYU:
			gstCalculationSlab = PropertiesManager.propertiesMap.get("PAYUGSTSLAB");
			break;
		case IDFCUPI:
			gstCalculationSlab = PropertiesManager.propertiesMap.get("IDFCUPIGSTSLAB");
			break;
		case YESBANKCB:
			gstCalculationSlab = PropertiesManager.propertiesMap.get("YESBANKCBGSTSLAB");
			break;
		case KOTAK:
			gstCalculationSlab = PropertiesManager.propertiesMap.get("KOTAKGSTSLAB");
			break;
		case FSS:
			gstCalculationSlab = PropertiesManager.propertiesMap.get("FSSGSTSLAB");
			break;
		case ICICIUPI:
			gstCalculationSlab = PropertiesManager.propertiesMap.get("ICICIUPIGSTSLAB");
			break;
		case APEXPAY:
			gstCalculationSlab = PropertiesManager.propertiesMap.get("APEXPAYSTSLAB");
			break;
		case IPINT:
			gstCalculationSlab = PropertiesManager.propertiesMap.get("IPINTGSTSLAB");
		default:
			break;

		}
		if (StringUtils.isBlank(gstCalculationSlab)) {
			gstCalculationSlab = "0";
		}

		if (chargingDetails.isAllowFixCharge()) {

			acquirerTdr = amount.multiply(new BigDecimal(chargingDetails.getBankTDR()).divide(BigDecimal.valueOf(100)));
			acquirerTdr = acquirerTdr.add(new BigDecimal(chargingDetails.getBankFixCharge()));
			int acqChargeCompareResult = acquirerTdr.compareTo(new BigDecimal(chargingDetails.getMaxChargeAcquirer()));
			if ((acqChargeCompareResult > 0)) {
				acquirerTdr = new BigDecimal(chargingDetails.getMaxChargeAcquirer());
			}

			if (gstCalculationSlab.equalsIgnoreCase("0")) {
				acquirerTdr = acquirerTdr.setScale(2, BigDecimal.ROUND_HALF_UP);
				acquirerGst = acquirerTdr.multiply(st.divide(BigDecimal.valueOf(100)));
				acquirerGst = acquirerGst.setScale(2, BigDecimal.ROUND_HALF_UP);
			} else {

				acquirerGst = acquirerTdr.multiply(st.divide(BigDecimal.valueOf(100)));
				acquirerGst = acquirerGst.setScale(2, BigDecimal.ROUND_HALF_UP);
				acquirerTdr = acquirerTdr.setScale(2, BigDecimal.ROUND_HALF_UP);
			}

			// Select higher charge between Percentage value and Fix Charge value is higher
			// charge flag is selected
			if (chargesFlag) {

				BigDecimal merchantTdrVal = amount
						.multiply(new BigDecimal(chargingDetails.getMerchantTDR()).divide(BigDecimal.valueOf(100)));
				BigDecimal merchantFcVal = new BigDecimal(chargingDetails.getMerchantFixCharge());

				if (merchantTdrVal.compareTo(merchantFcVal) > 0) {
					merchantTdr = merchantTdrVal;
				} else {
					merchantTdr = merchantFcVal;
				}

			} else {
				// Condition for MSEDCL
				if (fields.get(FieldType.PAY_ID.getName())
						.equalsIgnoreCase(propertiesManager.propertiesMap.get("MSEDCL_PAY_ID"))
						&& fields.get(FieldType.PAYMENT_TYPE.getName())
								.equalsIgnoreCase(PaymentType.DEBIT_CARD.getCode())
						&& StringUtils.isNotBlank(fields.get(FieldType.INTERNAL_CARD_ISSUER_BANK.getName()))
						&& fields.get(FieldType.INTERNAL_CARD_ISSUER_BANK.getName()).equalsIgnoreCase("SBI")) {
					merchantTdr = amount
							.multiply(new BigDecimal(propertiesManager.propertiesMap.get("MSEDCL_DC_SBI_RATE"))
									.divide(BigDecimal.valueOf(100)));
				} else {
					merchantTdr = amount
							.multiply(new BigDecimal(chargingDetails.getMerchantTDR()).divide(BigDecimal.valueOf(100)));
				}

			}

			merchantTdr = merchantTdr.setScale(2, BigDecimal.ROUND_HALF_UP);

			merchantGst = merchantTdr.multiply(st.divide(BigDecimal.valueOf(100)));
			merchantGst = merchantGst.setScale(2, BigDecimal.ROUND_HALF_UP);

			// Changes for Reseller

			if (StringUtils.isNotBlank(fields.get(FieldType.RESELLER_ID.getName()))) {
				resellerTdr = amount
						.multiply(new BigDecimal(chargingDetails.getResellerTDR()).divide(BigDecimal.valueOf(100)));
				resellerTdr = resellerTdr.add(new BigDecimal(chargingDetails.getResellerFixCharge()));

				resellerTdr = resellerTdr.setScale(2, BigDecimal.ROUND_HALF_UP);
				resellerGst = resellerTdr.multiply(st.divide(BigDecimal.valueOf(100)));
				resellerGst = resellerGst.setScale(2, BigDecimal.ROUND_HALF_UP);
			} else {
				resellerTdr = BigDecimal.ZERO;
				resellerTdr = resellerTdr.setScale(2, BigDecimal.ROUND_HALF_UP);
				resellerGst = BigDecimal.ZERO;
				resellerGst = resellerGst.setScale(2, BigDecimal.ROUND_HALF_UP);
			}

			pgTdr = merchantTdr.subtract(acquirerTdr).subtract(resellerTdr);
			pgGst = merchantGst.subtract(acquirerGst).subtract(resellerGst);
			pgTdr = pgTdr.setScale(2, BigDecimal.ROUND_HALF_UP);
			pgGst = pgGst.setScale(2, BigDecimal.ROUND_HALF_UP);

			int merchantChargeCompareResult = merchantTdr
					.compareTo(new BigDecimal(chargingDetails.getMaxChargeMerchant()));

			if ((merchantChargeCompareResult > 0)) {
				merchantTdr = new BigDecimal(chargingDetails.getMaxChargeMerchant());
				merchantGst = merchantTdr.multiply(st.divide(BigDecimal.valueOf(100)));

				pgTdr = merchantTdr.subtract(acquirerTdr).subtract(resellerTdr);
				pgGst = merchantGst.subtract(acquirerGst).subtract(resellerGst);

				merchantGst = merchantGst.setScale(2, BigDecimal.ROUND_HALF_UP);
				merchantTdr = merchantTdr.setScale(2, BigDecimal.ROUND_HALF_UP);
				pgTdr = pgTdr.setScale(2, BigDecimal.ROUND_HALF_UP);
				pgGst = pgGst.setScale(2, BigDecimal.ROUND_HALF_UP);
			}

		} else {

			acquirerTdr = amount.multiply(new BigDecimal(chargingDetails.getBankTDR()).divide(BigDecimal.valueOf(100)));
			int acqChargeCompareResult = acquirerTdr.compareTo(new BigDecimal(chargingDetails.getMaxChargeAcquirer()));
			if ((acqChargeCompareResult > 0)) {
				acquirerTdr = new BigDecimal(chargingDetails.getMaxChargeAcquirer());
			}

			if (gstCalculationSlab.equalsIgnoreCase("0")) {
				acquirerTdr = acquirerTdr.setScale(2, BigDecimal.ROUND_HALF_UP);
				acquirerGst = acquirerTdr.multiply(st.divide(BigDecimal.valueOf(100)));
				acquirerGst = acquirerGst.setScale(2, BigDecimal.ROUND_HALF_UP);
			} else {

				acquirerGst = acquirerTdr.multiply(st.divide(BigDecimal.valueOf(100)));
				acquirerGst = acquirerGst.setScale(2, BigDecimal.ROUND_HALF_UP);
				acquirerTdr = acquirerTdr.setScale(2, BigDecimal.ROUND_HALF_UP);
			}

			// Changes for Reseller

			if (StringUtils.isNotBlank(fields.get(FieldType.RESELLER_ID.getName()))) {
				resellerTdr = amount
						.multiply(new BigDecimal(chargingDetails.getResellerTDR()).divide(BigDecimal.valueOf(100)));
				resellerTdr = resellerTdr.setScale(2, BigDecimal.ROUND_HALF_UP);
				resellerGst = resellerTdr.multiply(st.divide(BigDecimal.valueOf(100)));
				resellerGst = resellerGst.setScale(2, BigDecimal.ROUND_HALF_UP);
			} else {
				resellerTdr = BigDecimal.ZERO;
				resellerTdr = resellerTdr.setScale(2, BigDecimal.ROUND_HALF_UP);
				resellerGst = BigDecimal.ZERO;
				resellerGst = resellerGst.setScale(2, BigDecimal.ROUND_HALF_UP);
			}

			// Condition for MSEDCL
			if (fields.get(FieldType.PAY_ID.getName())
					.equalsIgnoreCase(propertiesManager.propertiesMap.get("MSEDCL_PAY_ID"))
					&& fields.get(FieldType.PAYMENT_TYPE.getName()).equalsIgnoreCase(PaymentType.DEBIT_CARD.getCode())
					&& StringUtils.isNotBlank(fields.get(FieldType.INTERNAL_CARD_ISSUER_BANK.getName()))
					&& fields.get(FieldType.INTERNAL_CARD_ISSUER_BANK.getName()).equalsIgnoreCase("SBI")) {
				merchantTdr = amount.multiply(new BigDecimal(propertiesManager.propertiesMap.get("MSEDCL_DC_SBI_RATE"))
						.divide(BigDecimal.valueOf(100)));
			} else {
				merchantTdr = amount
						.multiply(new BigDecimal(chargingDetails.getMerchantTDR()).divide(BigDecimal.valueOf(100)));
			}
			merchantTdr = merchantTdr.setScale(2, BigDecimal.ROUND_HALF_UP);
			merchantGst = merchantTdr.multiply(st.divide(BigDecimal.valueOf(100)));
			merchantGst = merchantGst.setScale(2, BigDecimal.ROUND_HALF_UP);

			pgTdr = merchantTdr.subtract(acquirerTdr).subtract(resellerTdr);
			pgGst = merchantGst.subtract(acquirerGst).subtract(resellerGst);
			pgTdr = pgTdr.setScale(2, BigDecimal.ROUND_HALF_UP);
			pgGst = pgGst.setScale(2, BigDecimal.ROUND_HALF_UP);

			int merchantChargeCompareResult = merchantTdr
					.compareTo(new BigDecimal(chargingDetails.getMaxChargeMerchant()));

			if ((merchantChargeCompareResult > 0)) {
				merchantTdr = new BigDecimal(chargingDetails.getMaxChargeMerchant());
				merchantGst = merchantTdr.multiply(st.divide(BigDecimal.valueOf(100)));

				pgTdr = merchantTdr.subtract(acquirerTdr).subtract(resellerTdr);
				pgGst = merchantGst.subtract(acquirerGst).subtract(resellerGst);

				pgTdr = pgTdr.setScale(2, BigDecimal.ROUND_HALF_UP);
				pgGst = pgGst.setScale(2, BigDecimal.ROUND_HALF_UP);
				merchantGst = merchantGst.setScale(2, BigDecimal.ROUND_HALF_UP);
				merchantTdr = merchantTdr.setScale(2, BigDecimal.ROUND_HALF_UP);

			}

		}

		fields.put(FieldType.RESELLER_CHARGES.getName(), resellerTdr.toString());
		fields.put(FieldType.RESELLER_GST.getName(), resellerGst.toString());

		fields.put(FieldType.ACQUIRER_TDR_SC.getName(), acquirerTdr.toString());
		fields.put(FieldType.ACQUIRER_GST.getName(), acquirerGst.toString());

		fields.put(FieldType.MERCHANT_TDR_SC.getName(), merchantTdr.toString());
		fields.put(FieldType.MERCHANT_GST.getName(), merchantGst.toString());

		fields.put(FieldType.PG_TDR_SC.getName(), pgTdr.toString());
		fields.put(FieldType.PG_GST.getName(), pgGst.toString());

		sufCharge = sufDetailDao.fetchSufChargeByPayId(fields.get(FieldType.PAY_ID.getName()));
		sufTdr = getSufCharge(fields.get(FieldType.PAY_ID.getName()),
				fields.get(FieldType.INTERNAL_ORIG_TXN_TYPE.getName()),
				PaymentType.getpaymentName(fields.get(FieldType.PAYMENT_TYPE.getName())),
				MopType.getmopName(fields.get(FieldType.MOP_TYPE.getName())),
				Amount.toDecimal(fields.get(FieldType.AMOUNT.getName()), fields.get(FieldType.CURRENCY.getName())),
				sufCharge, fields.get(FieldType.PAYMENTS_REGION.getName()));
		sufTdr = sufTdr.setScale(2, BigDecimal.ROUND_HALF_UP);
		sufGst = sufTdr.multiply(st.divide(BigDecimal.valueOf(100)));
		sufGst = sufGst.setScale(2, BigDecimal.ROUND_HALF_UP);
		fields.put(FieldType.SUF_TDR.getName(), sufTdr.toString());
		fields.put(FieldType.SUF_GST.getName(), sufGst.toString());

	}

	public void addAcquirerFields(Fields fields) throws SystemException {

		if ((fields.get(FieldType.TXNTYPE.getName()).equals(TransactionType.NEWORDER.getName()))) {
			return;
		}
		String internalOrigTxnId = fields.get(FieldType.INTERNAL_ORIG_TXN_TYPE.getName());
		if (internalOrigTxnId != null) {

			if (fields.get(FieldType.INTERNAL_ORIG_TXN_TYPE.getName()).equals(TransactionType.STATUS.getName())
					|| fields.get(FieldType.INTERNAL_ORIG_TXN_TYPE.getName()).equals(TransactionType.RECO.getName())
					|| fields.get(FieldType.INTERNAL_ORIG_TXN_TYPE.getName())
							.equals(TransactionType.REFUNDRECO.getName())) {
				return;
			}
		}
		User user = authenticator.getUser(fields);

		String acquirer = fields.get(FieldType.ACQUIRER_TYPE.getName());
		if (StringUtils.isEmpty(acquirer)) {
			acquirer = fields.get(FieldType.INTERNAL_ACQUIRER_TYPE.getName());
			if (StringUtils.isEmpty(acquirer)) {
				// authenticator.validatePaymentOptions(fields);
				AcquirerType acquirerType = acquirerTypeService.getDefault(fields, user);

				acquirer = acquirerType.getCode();
				logger.info("Current Acquirer for Order Id " + fields.get(FieldType.ORDER_ID.getName()) + " is "
						+ acquirer);
				fields.put(FieldType.ACQUIRER_TYPE.getName(), acquirer);
				fields.put(FieldType.INTERNAL_ACQUIRER_TYPE.getName(), acquirer);
			} else {
				fields.put(FieldType.ACQUIRER_TYPE.getName(), acquirer);
			}
		}

		if (user == null) {
			logger.info("User is null for txn ID = " + fields.get(FieldType.TXN_ID.getName()));
		} else {
			logger.info("User payID = " + user.getPayId() + " Txn payID = " + fields.get(FieldType.PAY_ID.getName()));
		}

		Account account = null;
		Set<Account> accounts = user.getAccounts();

		if (accounts == null || accounts.size() == 0) {
			logger.info("No account found for Pay ID = " + fields.get(FieldType.PAY_ID.getName()) + " and ORDER ID = "
					+ fields.get(FieldType.ORDER_ID.getName()));
		} else {
			for (Account accountThis : accounts) {
				if (accountThis.getAcquirerName()
						.equalsIgnoreCase(AcquirerType.getInstancefromCode(acquirer).getName())) {
					account = accountThis;
					break;
				}
			}
		}

		String currencyCode = fields.get(FieldType.CURRENCY_CODE.getName());

		// Below patch added to update user when a new acquirer is onboarded and we need
		// to update user in static map
		if (null == account) {

			logger.info("Account is null for Order Id = " + fields.get(FieldType.ORDER_ID.getName()));
			logger.info("Updating user from database again to get latest data");

			user = authenticator.getUser(fields);
			if (user != null) {
				logger.info("Found updated user and added to static map !!");
			}
			accounts = user.getAccounts();

			if (accounts == null || accounts.size() == 0) {
				logger.info("No account found in updated user for Pay ID = " + fields.get(FieldType.PAY_ID.getName())
						+ " and ORDER ID = " + fields.get(FieldType.ORDER_ID.getName()));
			} else {
				for (Account accountThis : accounts) {
					if (accountThis.getAcquirerName()
							.equalsIgnoreCase(AcquirerType.getInstancefromCode(acquirer).getName())) {
						account = accountThis;
						break;
					}
				}
			}

			if (account != null) {
				logger.info("Found account details in updated user !!");
			}
		}

		if (null == account) {
			smsCount = smsCount + 1;

			String payId = fields.get(FieldType.PAY_ID.getName());

			if (smsCount % 3 == 0) {

				StringBuilder smsBody = new StringBuilder();
				smsBody.append("Alert !");
				smsBody.append("Transaction not processed for merchant  with payId = " + payId);
				if (null != user.getBusinessName()) {
					smsBody.append(" and name  = " + user.getBusinessName());
				}
				smsBody.append(" due to missing account for merchant .");
				smsBody.append("Transaction Id = " + fields.get(FieldType.TXN_ID.getName()));
				if (StringUtils.isNotBlank(fields.get(FieldType.ORDER_ID.getName()))) {
					smsBody.append(" , Order Id = " + fields.get(FieldType.ORDER_ID.getName()));
				}

				String smsSenderList = PropertiesManager.propertiesMap.get("smsAlertList");

				for (String mobile : smsSenderList.split(",")) {
					try {

						String smsInnuvisolutions = PropertiesManager.propertiesMap
								.get(Constants.SMS_INNUVIS_SOLUTIONS.getValue());
						if (StringUtils.isNotBlank(smsInnuvisolutions) && smsInnuvisolutions.equalsIgnoreCase("Y")) {
							logger.info("SMS send by InnvisSolution");
							smsSender.sendSMSByInnvisSolution(mobile, smsBody.toString());
						} else {
							logger.info("Normal SMS send");
							smsSender.sendSMS(mobile, smsBody.toString());
						}
						// smsSender.sendSMS(mobile, smsBody.toString());
					} catch (IOException e) {
						logger.error("SMS not sent for transaction failure when account is null : ", e);
					}
				}

			}
			throw new SystemException(ErrorType.NOT_APPROVED_FROM_ACQUIRER,
					"User is not approved from acquirer, PayId=" + payId + " , Acquirer = " + acquirer);
		}

		AccountCurrency accountCurrency = account.getAccountCurrency(currencyCode);
		if (null == accountCurrency) {
			fields.put(FieldType.STATUS.getName(), StatusType.INVALID.getName());
			throw new SystemException(ErrorType.CURRENCY_NOT_MAPPED, ErrorType.CURRENCY_NOT_MAPPED.getResponseMessage()
					+ user.getPayId() + " and currency code " + fields.get(FieldType.CURRENCY_CODE.getName()));
		}

		// Get MerchantId from account
		String merchantId = accountCurrency.getMerchantId();
		if (!StringUtils.isEmpty(merchantId)) {
			fields.put(FieldType.MERCHANT_ID.getName(), merchantId);
		}

		boolean nonsecure = accountCurrency.isDirectTxn();
		String txnType = fields.get(FieldType.TXNTYPE.getName());
		if (nonsecure && txnType.equals(TransactionType.ENROLL.getName())) {
			fields.put(FieldType.TXNTYPE.getName(),
					ModeType.getDefaultPurchaseTransaction(user.getModeType()).getName());
		}
		// Get Password
		if (!StringUtils.isEmpty(accountCurrency.getPassword())) {
			String decryptedPassword = encryptDecryptService.decrypt(fields.get(FieldType.PAY_ID.getName()),
					accountCurrency.getPassword());
			fields.put(FieldType.PASSWORD.getName(), decryptedPassword);
		}

		// Get key
		String encryptionKey = accountCurrency.getTxnKey();
		if (!StringUtils.isEmpty(encryptionKey)) {
			fields.put(FieldType.TXN_KEY.getName(), encryptionKey);
		}

		fields.put(FieldType.ADF1.getName(), accountCurrency.getAdf1());
		fields.put(FieldType.ADF2.getName(), accountCurrency.getAdf2());
		fields.put(FieldType.ADF3.getName(), accountCurrency.getAdf3());
		fields.put(FieldType.ADF4.getName(), accountCurrency.getAdf4());
		fields.put(FieldType.ADF5.getName(), accountCurrency.getAdf5());
		fields.put(FieldType.ADF8.getName(), accountCurrency.getAdf8());
		fields.put(FieldType.ADF9.getName(), accountCurrency.getAdf9());
		fields.put(FieldType.ADF10.getName(), accountCurrency.getAdf10());
		fields.put(FieldType.ADF11.getName(), accountCurrency.getAdf11());

		if (!StringUtils.isEmpty(accountCurrency.getAdf6())) {
			String adf6 = encryptDecryptService.decrypt(fields.get(FieldType.PAY_ID.getName()),
					accountCurrency.getAdf6());
			fields.put(FieldType.ADF6.getName(), adf6);
		}

		if (!StringUtils.isEmpty(accountCurrency.getAdf7())) {
			String adf7 = encryptDecryptService.decrypt(fields.get(FieldType.PAY_ID.getName()),
					accountCurrency.getAdf7());
			fields.put(FieldType.ADF7.getName(), adf7);
		}

		String internalTxnType = fields.get(FieldType.INTERNAL_ORIG_TXN_TYPE.getName());
		if (null == internalTxnType) {
			fields.put(FieldType.INTERNAL_ORIG_TXN_TYPE.getName(),
					ModeType.getDefaultPurchaseTransaction(user.getModeType()).getName());
		}
	}

	public void authenticate(Fields fields) throws SystemException {
		User user = null;

		// Decide whether to use static usermap or get data from DAO
		if (PropertiesManager.propertiesMap.get(Constants.USE_STATIC_DATA.getValue()) != null
				&& PropertiesManager.propertiesMap.get(Constants.USE_STATIC_DATA.getValue())
						.equalsIgnoreCase(Constants.Y_FLAG.getValue())) {

			user = staticDataProvider.getUserData(fields.get(FieldType.PAY_ID.getName()));
		} else {
			user = userDao.findPayId(fields.get(FieldType.PAY_ID.getName()));

		}

		authenticator.setUser(user);
		authenticator.authenticate(fields);
	}

	public void postProcess(Fields fields) {
		fields.removeSecureFieldsSubmitted();
		if ((fields.get(FieldType.TXNTYPE.getName()).equals(TransactionType.REFUND.getName()))) {
			if (StringUtils.isBlank(fields.get(FieldType.REFUND_FLAG.getName()))) {
				fields.put(FieldType.REFUND_FLAG.getName(), "C");
			}
		}
	}

	public boolean checkDailyLimit(User user, Fields fields, UserSettingData userSetting) {
		try {

			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			BigDecimal decimalAmount;
			String stringAmount;
			BigDecimal annualTurnover;

			// Checking Allow Part Settle
			if (userSetting.isAllowPartSettle()) {

				MerchantProcessingApplication MPAData = new MerchantProcessingApplication();
				MPAData = mpaDao.fetchMPADataByPayId(user.getPayId());

				MongoDatabase dbIns = mongoInstance.getDB();
				MongoCollection<Document> coll = dbIns.getCollection(PropertiesManager.propertiesMap
						.get("MONGO_DB_" + Constants.PART_SETTLE_LIMIT_COLLECTION.getValue()));

				BasicDBObject finalQuery = new BasicDBObject(FieldType.PAY_ID.getName(), user.getPayId())
						.append(FieldType.CREATE_DATE.getName(), sdf.format(new Date()));
				FindIterable<Document> iterDoc = coll.find(finalQuery);
				MongoCursor<Document> cursor = iterDoc.iterator();

				stringAmount = Amount.toDecimal(fields.get(FieldType.TOTAL_AMOUNT.getName()),
						fields.get(FieldType.CURRENCY_CODE.getName()));
				decimalAmount = new BigDecimal(stringAmount).setScale(2, BigDecimal.ROUND_HALF_UP);
				annualTurnover = new BigDecimal(MPAData.getAnnualTurnover());

				boolean isAllowsubtract = userSetting.isAllowSubtractValue();

				// Checking Extra Turnover
				if (!isAllowsubtract) {
					annualTurnover = annualTurnover.add(annualTurnover.divide(new BigDecimal("100"))
							.multiply(new BigDecimal(userSetting.getDeviation())));
				} else {
					annualTurnover = annualTurnover.subtract(annualTurnover.divide(new BigDecimal("100"))
							.multiply(new BigDecimal(userSetting.getDeviation())));
				}

				// Getting Daily Limit
				BigDecimal dailyAmount = annualTurnover.divide(new BigDecimal("365"), 0, BigDecimal.ROUND_DOWN);

				// First Data will be Created.If Data is not found in DB(Daily new entry) else
				// update the amount of new Transaction
				if (!cursor.hasNext()) {
					Document doc = new Document();
					doc.put(FieldType.PAY_ID.getName(), user.getPayId());
					doc.put(FieldType.CREATE_DATE.getName(), sdf.format(new Date()));
					doc.put("TOTAL_AMOUNT", String.valueOf(decimalAmount));
					doc.put("DAILY_LIMIT", String.valueOf(dailyAmount));
					doc.put("TOTAL_TRANSACTIONS", String.valueOf(1));
					coll.insertOne(doc);
					return true;
				} else {
					while (cursor.hasNext()) {
						Document dbobj = cursor.next();
						BigDecimal DBAmount = new BigDecimal(dbobj.getString("TOTAL_AMOUNT"));
						int TransactionCount = Integer.parseInt(dbobj.getString("TOTAL_TRANSACTIONS")) + 1;
						DBAmount = DBAmount.add(decimalAmount);

						if (dailyAmount.compareTo(DBAmount) == 1) {
							Bson filter = new Document(FieldType.PAY_ID.getName(), user.getPayId())
									.append(FieldType.CREATE_DATE.getName(), sdf.format(new Date()));
							Bson newValue = new Document("TOTAL_AMOUNT", String.valueOf(DBAmount))
									.append("DAILY_LIMIT", String.valueOf(dailyAmount))
									.append("TOTAL_TRANSACTIONS", String.valueOf(TransactionCount));

							Bson updateOperationDocument = new Document("$set", newValue);
							coll.updateOne(filter, updateOperationDocument);
							logger.info("update total daily limit");
							return true;
						} else {
							return false;
						}
					}
				}
			} else {
				return false;
			}
		} catch (Exception e) {
			logger.error("exception ", e);
		}
		return false;
	}// End of method

	public BigDecimal getSufCharge(String payId, String txnType, String paymentType, String mopType, String baseAmount,
			List<SUFDetail> sufCharge, String paymentRegion) {
		try {

			BigDecimal fixedCharge = null;
			BigDecimal percentageCharge = null;

			for (SUFDetail suf : sufCharge) {

				String slabArray[] = suf.getSlab().split("-");

				BigDecimal baseZero = new BigDecimal(slabArray[0]);
				BigDecimal baseOne = new BigDecimal(slabArray[1]);
				BigDecimal baseAmountBigDecimal = new BigDecimal(baseAmount);

				if (suf.getPayId().equalsIgnoreCase(payId) && suf.getTxnType().equalsIgnoreCase(txnType)
						&& suf.getPaymentType().equalsIgnoreCase(paymentType)
						&& suf.getMopType().equalsIgnoreCase(mopType)
						&& suf.getPaymentRegion().equalsIgnoreCase(paymentRegion)) {

					if ((baseAmountBigDecimal.compareTo(baseZero) == 1 || baseAmountBigDecimal.compareTo(baseZero) == 0)
							&& (baseAmountBigDecimal.compareTo(baseOne) == -1)
							|| baseAmountBigDecimal.compareTo(baseOne) == 0) {

						fixedCharge = new BigDecimal(suf.getFixedCharge());
						percentageCharge = (new BigDecimal(suf.getPercentageAmount())
								.divide(new BigDecimal(Constants.MAX_NUMBER_OF_KEYS.getValue()))
								.multiply(new BigDecimal(baseAmount))).setScale(2, RoundingMode.FLOOR);

						break;
					}
				}

			}
			if (fixedCharge == null && percentageCharge == null) {
				fixedCharge = new BigDecimal("0.00");
				percentageCharge = new BigDecimal("0.00");
			} else if (fixedCharge == null && !(percentageCharge == null)) {
				fixedCharge = new BigDecimal("0.00");
			} else if (!(fixedCharge == null) && percentageCharge == null) {
				percentageCharge = new BigDecimal("0.00");
			}
			return fixedCharge.add(percentageCharge);
		} catch (Exception ex) {
			logger.error("Exception caught while calculate suf charges for monthly invoice pdf : ", ex);
			return null;
		}
	}

}
