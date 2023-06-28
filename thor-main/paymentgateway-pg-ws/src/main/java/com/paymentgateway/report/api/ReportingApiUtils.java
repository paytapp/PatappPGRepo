package com.paymentgateway.report.api;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.api.Hasher;
import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.user.TransactionSearch;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.util.CrmFieldConstants;
import com.paymentgateway.commons.util.DateCreater;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.StatusType;
import com.paymentgateway.commons.util.TransactionType;
import com.paymentgateway.commons.util.TxnReport;

@Service
public class ReportingApiUtils {

	@Autowired
	public TxnReport txnReports;

	@Autowired
	private UserDao userDao;

	@Autowired
	PropertiesManager propertiesManager;

	private static Logger logger = LoggerFactory.getLogger(ReportingApiUtils.class.getName());

	public boolean validateHash(Fields fields) {
		String fieldHash = fields.remove(FieldType.HASH.getName());

		try {
			logger.info("Hash from Merchant  == " + fieldHash);
			String hash = Hasher.getHash(fields);
			logger.info("Calculated Hash == " + hash);
			if (!hash.equals(fieldHash)) {
				logger.info("Hash Mismatch , Calculated hash ==  " + hash + " Merchant hash == " + fieldHash
						+ " Txn Id " + fields.get(FieldType.TXN_ID.getName()));
				return false;
			}
			fields.put(FieldType.HASH.getName(), fieldHash);
			return true;
		} catch (Exception e) {
			logger.error("exception in hash validation ", e);
		}
		return false;
	}

	public Map<String, String> validateFields(Fields fields) {
		Map<String, String> validationMap = new HashMap<String, String>();

		String payId = fields.get(FieldType.PAY_ID.getName());
		String dataTo = fields.get(FieldType.DATE_TO.getName());
		String dateFrom = fields.get(FieldType.DATE_FROM.getName());
		String minRange = fields.get(FieldType.MIN_RANGE.getName());
		String maxRange = fields.get(FieldType.MAX_RANGE.getName());
		String txnType = fields.get(FieldType.TXNTYPE.getName());

		String pgRefNum = fields.get(FieldType.PG_REF_NUM.getName());
		String orderId = fields.get(FieldType.ORDER_ID.getName());

		if (StringUtils.isNotBlank(pgRefNum) || StringUtils.isNotBlank(orderId)) {
			if (StringUtils.isBlank(payId)) {
				fields.put(FieldType.STATUS.getName(), StatusType.INVALID.getName());
				fields.put(FieldType.RESPONSE_MESSAGE.getName(),
						ErrorType.EMPTY_FIELD.getResponseMessage() + " --> " + FieldType.PAY_ID.getName());
				fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.EMPTY_FIELD.getResponseCode());
				logger.info("payId is missing , " + fields.get(FieldType.RESPONSE_MESSAGE.getName()));
				return fields.getFields();
			}
		} else if (StringUtils.isBlank(payId) || StringUtils.isBlank(dateFrom) || StringUtils.isBlank(dataTo)
				|| StringUtils.isBlank(txnType) || StringUtils.isBlank(minRange) || StringUtils.isBlank(maxRange)) {

			if (StringUtils.isBlank(payId)) {
				fields.put(FieldType.RESPONSE_MESSAGE.getName(),
						ErrorType.EMPTY_FIELD.getResponseMessage() + " --> " + FieldType.PAY_ID.getName());
			} else if (StringUtils.isBlank(dateFrom)) {
				fields.put(FieldType.RESPONSE_MESSAGE.getName(),
						ErrorType.EMPTY_FIELD.getResponseMessage() + " --> " + FieldType.DATE_FROM.getName());
			} else if (StringUtils.isBlank(dataTo)) {
				fields.put(FieldType.RESPONSE_MESSAGE.getName(),
						ErrorType.EMPTY_FIELD.getResponseMessage() + " --> " + FieldType.DATE_TO.getName());
			} else if (StringUtils.isBlank(txnType)) {
				fields.put(FieldType.RESPONSE_MESSAGE.getName(),
						ErrorType.EMPTY_FIELD.getResponseMessage() + " --> " + FieldType.TXNTYPE.getName());
			} else if (StringUtils.isBlank(minRange)) {
				fields.put(FieldType.RESPONSE_MESSAGE.getName(),
						ErrorType.EMPTY_FIELD.getResponseMessage() + " --> " + FieldType.MIN_RANGE.getName());
			} else if (StringUtils.isBlank(maxRange)) {
				fields.put(FieldType.RESPONSE_MESSAGE.getName(),
						ErrorType.EMPTY_FIELD.getResponseMessage() + " --> " + FieldType.MAX_RANGE.getName());
			}

			fields.put(FieldType.STATUS.getName(), StatusType.INVALID.getName());
			fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.EMPTY_FIELD.getResponseCode());
			logger.info("Mandatory field is missing , " + fields.get(FieldType.RESPONSE_MESSAGE.getName()));
			return fields.getFields();
		} else {
			int minRangeInt = 0;
			int maxRangeInt = 0;
			try {
				minRangeInt = Integer.parseInt(minRange) - 1;
				if (minRangeInt < 0) {
					fields.put(FieldType.STATUS.getName(), StatusType.INVALID.getName());
					fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.MIN_MAX_RANGE_VALUE.getResponseCode());
					fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.MIN_MAX_RANGE_VALUE.getResponseMessage()
							+ " --> " + FieldType.MIN_RANGE.getName());
					return fields.getFields();
				}
			} catch (Exception ex) {
				fields.put(FieldType.STATUS.getName(), StatusType.INVALID.getName());
				fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.INVALID_FIELD.getResponseCode());
				fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.INVALID_FIELD.getResponseMessage() + " --> "
						+ fields.get(FieldType.MIN_RANGE.getName()));
				return fields.getFields();
			}

			try {
				maxRangeInt = Integer.parseInt(maxRange);
				if (maxRangeInt < 1) {
					fields.put(FieldType.STATUS.getName(), StatusType.INVALID.getName());
					fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.MIN_MAX_RANGE_VALUE.getResponseCode());
					fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.MIN_MAX_RANGE_VALUE.getResponseMessage()
							+ " --> " + FieldType.MAX_RANGE.getName());
					return fields.getFields();
				}
			} catch (Exception ex) {
				fields.put(FieldType.STATUS.getName(), StatusType.INVALID.getName());
				fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.INVALID_FIELD.getResponseCode());
				fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.INVALID_FIELD.getResponseMessage() + " --> "
						+ fields.get(FieldType.MAX_RANGE.getName()));
				return fields.getFields();
			}

			if (maxRangeInt - minRangeInt > 0 && maxRangeInt - minRangeInt <= 100) {
			} else {
				fields.put(FieldType.STATUS.getName(), StatusType.INVALID.getName());
				fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.MIN_MAX_DATA_RANGE.getResponseMessage());
				fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.MIN_MAX_DATA_RANGE.getResponseCode());
				return fields.getFields();
			}

			if (StringUtils.isNotBlank(fields.get(FieldType.CURRENCY.getName()))) {

				if (propertiesManager.getNumericCurrencyCode(fields.get(FieldType.CURRENCY.getName())) == null) {

					fields.put(FieldType.STATUS.getName(), StatusType.INVALID.getName());
					fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.CURRENCY.getResponseMessage());
					fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.CURRENCY.getResponseCode());
					return fields.getFields();
				}
			}

			if (StringUtils.isNotBlank(dateFrom) && StringUtils.isNotBlank(dataTo)) {
				try {
					SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
					Date reqDateFrom = sdf.parse(dateFrom);
					String reqDateFromStr = sdf.format(reqDateFrom);
					Date reqDateTo = sdf.parse(dataTo);
					String reqDateToStr = sdf.format(reqDateTo);

					Calendar fromCalendar = Calendar.getInstance();
					fromCalendar.setTime(reqDateFrom);
					Calendar toCalendar = Calendar.getInstance();
					toCalendar.setTime(reqDateTo);

					Calendar currentDate = Calendar.getInstance();
					currentDate.setTime(new Date());

					if (!reqDateFromStr.equals(dateFrom)) {
						fields.put(FieldType.STATUS.getName(), StatusType.INVALID.getName());
						fields.put(FieldType.RESPONSE_MESSAGE.getName(),
								ErrorType.INVALID_DATE_FORMAT.getResponseMessage() + " for "
										+ FieldType.DATE_FROM.getName());
						fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.INVALID_DATE_FORMAT.getResponseCode());
						return fields.getFields();
					}
					if (!reqDateToStr.equals(dataTo)) {
						fields.put(FieldType.STATUS.getName(), StatusType.INVALID.getName());
						fields.put(FieldType.RESPONSE_MESSAGE.getName(),
								ErrorType.INVALID_DATE_FORMAT.getResponseMessage() + " for "
										+ FieldType.DATE_TO.getName());
						fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.INVALID_DATE_FORMAT.getResponseCode());
						return fields.getFields();
					}

					if (fromCalendar.compareTo(currentDate) > 0 || toCalendar.compareTo(currentDate) > 0) {
						fields.put(FieldType.STATUS.getName(), StatusType.INVALID.getName());
						fields.put(FieldType.RESPONSE_MESSAGE.getName(),
								ErrorType.CURRENT_DATE_LIMIT.getResponseMessage());
						fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.CURRENT_DATE_LIMIT.getResponseCode());
						return fields.getFields();
					}

					long daysDifference = getDifference(fromCalendar, toCalendar);

					if (fromCalendar.compareTo(toCalendar) > 0) {
						fields.put(FieldType.STATUS.getName(), StatusType.INVALID.getName());
						fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.TO_DATE_LIMIT.getResponseMessage());
						fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.TO_DATE_LIMIT.getResponseCode());
						return fields.getFields();
					} else if (daysDifference > 31) {
						fields.put(FieldType.STATUS.getName(), StatusType.INVALID.getName());
						fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.DATE_RANGE.getResponseMessage());
						fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.DATE_RANGE.getResponseCode());
						return fields.getFields();
					}
				} catch (ParseException e) {
					e.printStackTrace();
				}
			}
		}
		return validationMap;
	}

	public Object getData(Fields fields) {

		List<Map<String, String>> transactionList = new ArrayList<Map<String, String>>();
		Map<String, List<Map<String, String>>> returnMap = new HashMap<String, List<Map<String, String>>>();

		String pgRefNum = fields.get(FieldType.PG_REF_NUM.getName());
		String orderId = fields.get(FieldType.ORDER_ID.getName());
		String txnType = fields.get(FieldType.TXNTYPE.getName());

		if (StringUtils.isNotBlank(pgRefNum) || StringUtils.isNotBlank(orderId)) {
			transactionList = transactionReportData("", fields);
			returnMap.put("result", transactionList);
		} else if (StringUtils.isNotBlank(txnType) && (txnType.equalsIgnoreCase(TransactionType.SALE.getName())
				|| txnType.equalsIgnoreCase(TransactionType.REFUND.getName())
				|| txnType.equalsIgnoreCase(TransactionType.RECO.getName())
				|| txnType.equalsIgnoreCase(TransactionType.REFUNDRECO.getName()))) {

			transactionList = transactionReportData(txnType, fields);
			returnMap.put("result", transactionList);
		} else {
			fields.put(FieldType.STATUS.getName(), StatusType.INVALID.getName());
			fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.INVALID_TXN_TYPE.getResponseMessage());
			fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.INVALID_TXN_TYPE.getResponseCode());
			logger.info("Invalid TXNTYPE value, " + txnType);
			return fields.getFields();
		}
		return returnMap;
	}

	@SuppressWarnings("unlikely-arg-type")
	public List<Map<String, String>> transactionReportData(String txnType, Fields fields) {

		List<TransactionSearch> transactionList = new ArrayList<TransactionSearch>();
		SimpleDateFormat inputDateFormat = new SimpleDateFormat(CrmFieldConstants.DATE_TIME_FORMAT.getValue());

		String status = null;
		String pgRefNum = null;
		String orderId = null;
		String subMerchantPayId = null;
		String transactionType = null;
		int minRange = 1;
		int maxRange = 10;
		
		String paymentType = fields.get(FieldType.PAYMENT_TYPE.getName());
		String custEmail = fields.get(FieldType.CUST_EMAIL.getName());
		String currency = propertiesManager.getNumericCurrencyCode(fields.get(FieldType.CURRENCY.getName()));
		String partSettleFlag = fields.get(FieldType.PART_SETTLE.getName());
		String transactionFlag = fields.get(FieldType.TXN_CAPTURE_FLAG.getName());
		String deltaFlag = fields.get(FieldType.DELTA_REFUND_FLAG.getName());
		String toDate = DateCreater.formDateTimeformatCreater(inputDateFormat.format(new Date()));
		String fromDate = DateCreater.toDateTimeformatCreater(inputDateFormat.format(new Date()));

		if (StringUtils.isEmpty(txnType)) {
			pgRefNum = fields.get(FieldType.PG_REF_NUM.getName());
			orderId = fields.get(FieldType.ORDER_ID.getName());
		} else {
			if (txnType.equalsIgnoreCase(TransactionType.SALE.getName())) {
				transactionType = TransactionType.SALE.getName();
				status = StatusType.CAPTURED.getName();
				deltaFlag = null;
			} else if (txnType.equalsIgnoreCase(TransactionType.REFUND.getName())) {
				transactionType = TransactionType.REFUND.getName();
				status = StatusType.CAPTURED.getName();
				partSettleFlag = null;
			} else if (txnType.equalsIgnoreCase(TransactionType.REFUNDRECO.getName())) {
				transactionType = TransactionType.REFUND.getName();
				status = StatusType.SETTLED.getName();
			} else if (txnType.equalsIgnoreCase(TransactionType.RECO.getName())) {
				transactionType = TransactionType.SALE.getName();
				status = StatusType.SETTLED.getName();
			}
			minRange = Integer.parseInt(fields.get(FieldType.MIN_RANGE.getName())) - 1;
			maxRange = Integer.parseInt(fields.get(FieldType.MAX_RANGE.getName()));
			toDate = DateCreater.formDateTimeformatCreater(fields.get(FieldType.DATE_TO.getName()));
			fromDate = DateCreater.toDateTimeformatCreater(fields.get(FieldType.DATE_FROM.getName()));
		}
		String payId = "";
		User user = userDao.findPayId(fields.get(FieldType.PAY_ID.getName()));
		if (user.isSuperMerchant()) {
			payId = fields.get(FieldType.PAY_ID.getName());
			subMerchantPayId = "ALL";
		} else if (!user.isSuperMerchant() && StringUtils.isNotBlank(user.getSuperMerchantId())) {
			subMerchantPayId = user.getPayId();
			payId = user.getSuperMerchantId();
		}else if((user.getUserType().toString()).equalsIgnoreCase("RESELLER")) {
			payId = "ALL";
			subMerchantPayId = "ALL";
		}else {
			payId = fields.get(FieldType.PAY_ID.getName());
		}

		transactionList = txnReports.refundForSaleCaputureTransaction(txnReports.transactionReport(pgRefNum, orderId,
				custEmail, payId, subMerchantPayId, paymentType, status, currency, transactionType, fromDate, toDate,
				user, minRange, maxRange, partSettleFlag, "ALL", null, transactionFlag, deltaFlag));
		boolean flag = false;
		List<Map<String, String>> transactionMap = new ArrayList<Map<String, String>>();
		
		for(TransactionSearch dt : transactionList) {
			Map<String , String> transMap = new HashMap<String , String>();
			if(StringUtils.isNotEmpty(dt.getMerchants().toString())) {
				transMap.put("Merchant", dt.getMerchants().toString());
			}
			if(StringUtils.isNotEmpty(dt.getPgRefNum())) {
				transMap.put("PG REF Num", dt.getPgRefNum().toString());
			}
			if(StringUtils.isNotEmpty(dt.getOrderId())) {
				transMap.put("Order ID", dt.getOrderId().toString());
			}
			if(StringUtils.isNotEmpty(dt.getPaymentMethods())) {
				transMap.put("Payment Method", dt.getPaymentMethods().toString());
			}
			if(StringUtils.isNotEmpty(dt.getPaymentRegion())) {
				transMap.put("Payment Region", dt.getPaymentRegion().toString());
			}
			if(StringUtils.isNotEmpty(dt.getDateFrom())) {
				transMap.put("Date", dt.getDateFrom().toString());
			}
			if(StringUtils.isNotEmpty(dt.getTotalAmount())) {
				transMap.put("Total Amount", dt.getTotalAmount().toString());
			}
				if(StringUtils.isNotEmpty(dt.getTransactionId().toString())) {
					transMap.put("Txn Id", dt.getTransactionId().toString());
				}
				if(StringUtils.isNotEmpty(dt.getSubMerchantId())) {
					transMap.put("Sub-Merchant", dt.getSubMerchantId().toString());
				}
				if(StringUtils.isNotEmpty(dt.getDeliveryStatus())) {
					transMap.put("Delivery Status", dt.getDeliveryStatus().toString());
				}
				if(StringUtils.isNotEmpty(dt.getTransactionMode())) {
					transMap.put("Transaction Mode", dt.getTransactionMode().toString());
				}
				if(StringUtils.isNotEmpty(dt.getCustomerName())) {
					transMap.put("Cust Name", dt.getCustomerName().toString());
				}
				if(StringUtils.isNotEmpty(dt.getCustomerEmail())) {
					transMap.put("Customer Email", dt.getCustomerEmail().toString());
				}
				if(StringUtils.isNotEmpty(dt.getCardNumber())) {
					transMap.put("Mask", dt.getCardNumber().toString());
				}
				if(StringUtils.isNotEmpty(dt.getCardHolderType())) {
					transMap.put("Cardholder Type", dt.getCardHolderType().toString());
				}
				if(StringUtils.isNotEmpty(dt.getTxnType())) {
					transMap.put("Txn Type", dt.getTxnType().toString());
				}
				if(StringUtils.isNotEmpty(dt.getStatus())) {
					transMap.put("Status", dt.getStatus().toString());
				}
				if(StringUtils.isNotEmpty(dt.getAmount())) {
					transMap.put("Base Amount", dt.getAmount().toString());
				}
				if(StringUtils.isNotEmpty(dt.getTdr_Surcharge())) {
					transMap.put("TDR / Surcharge", dt.getTdr_Surcharge().toString());
				}
				if(StringUtils.isNotEmpty(dt.getGst_charge())) {
					transMap.put("GST", dt.getGst_charge().toString());
				}
				if(StringUtils.isNotEmpty(dt.getResellerCharges())) {
					transMap.put("Reseller Charges", dt.getResellerCharges().toString());
				}
				if(StringUtils.isNotEmpty(dt.getResellerGST())) {
					transMap.put("Reseller GST", dt.getResellerGST().toString());
				}
				if(StringUtils.isNotEmpty(dt.getTotalAmtPayable())) {
					transMap.put("Merchant Amount", dt.getTotalAmtPayable().toString());
				}
				if(StringUtils.isNotEmpty(dt.getDoctor())) {
					transMap.put("Doctor", dt.getDoctor().toString());
				}
				if(StringUtils.isNotEmpty(dt.getGlocal())) {
					transMap.put("Glocal", dt.getGlocal().toString());
				}
				if(StringUtils.isNotEmpty(dt.getPartner())) {
					transMap.put("Partner", dt.getPartner().toString());
				}
				if(StringUtils.isNotEmpty(dt.getUniqueId())) {
					transMap.put("Unique ID", dt.getUniqueId().toString());
				}
				if(StringUtils.isNotEmpty(dt.getTxnSettledType())) {
					transMap.put("Transaction Flag", dt.getTxnSettledType().toString());
				}
				if(StringUtils.isNotEmpty(dt.getPartSettle())) {
					transMap.put("Part Settled Flag", dt.getPartSettle().toString());
				}
				if(StringUtils.isNotEmpty(dt.getUDF11())) {
					transMap.put("UDF11", dt.getUDF11().toString());
				}
				if(StringUtils.isNotEmpty(dt.getUDF12())) {
					transMap.put("UDF12", dt.getUDF12().toString());
				}
				if(StringUtils.isNotEmpty(dt.getUDF13())) {
					transMap.put("UDF13", dt.getUDF13().toString());
				}
				if(StringUtils.isNotEmpty(dt.getUDF14())) {
					transMap.put("UDF14", dt.getUDF14().toString());
				}
				if(StringUtils.isNotEmpty(dt.getUDF15())) {
					transMap.put("UDF15", dt.getUDF15().toString());
				}
				if(StringUtils.isNotEmpty(dt.getUDF16())) {
					transMap.put("UDF16", dt.getUDF16().toString());
				}
				if(StringUtils.isNotEmpty(dt.getUDF17())) {
					transMap.put("UDF17", dt.getUDF17().toString());
				}
				if(StringUtils.isNotEmpty(dt.getUDF18())) {
					transMap.put("UDF18", dt.getUDF18().toString());
				}
				if(StringUtils.isNotEmpty(dt.getCategoryCode())) {
					transMap.put("Category Code", dt.getCategoryCode().toString());
				}
				if(StringUtils.isNotEmpty(dt.getSKUCode())) {
					transMap.put("SKU Code", dt.getSKUCode().toString());
				}
				if(StringUtils.isNotEmpty(dt.getRefundCycle())) {
					transMap.put("Refund Cycle", dt.getRefundCycle().toString());
				}
				if(StringUtils.isNotEmpty(dt.getProductPrice())) {
					transMap.put("Product Price", dt.getProductPrice().toString());
				}
				if(StringUtils.isNotEmpty(dt.getVendorID())) {
					transMap.put("Vendor ID", dt.getVendorID().toString());
				}
				if(StringUtils.isNotEmpty(dt.getPayOutDate())) {
					transMap.put("Payout Date", dt.getPayOutDate().toString());
				}
				if(StringUtils.isNotEmpty(dt.getUtrNo())) {
					transMap.put("UTR No", dt.getUtrNo().toString());
				}
				if(StringUtils.isNotEmpty(dt.getRefundOrderId())) {
					transMap.put("Refund Order ID", dt.getRefundOrderId().toString());
				}
				if(StringUtils.isNotEmpty(dt.getRrn())) {
					transMap.put("RRN", dt.getRrn().toString());
				}
				
			transactionMap.add(transMap);
			
		}
		return transactionMap;
	}
	
	public long getDifference(Calendar fromCalendar, Calendar toCalendar) {
		long miliSecondFromDate = fromCalendar.getTimeInMillis();
		long miliSecondToDate = toCalendar.getTimeInMillis();
		
		long diffInMilis = miliSecondToDate - miliSecondFromDate;
		long diffInDays = diffInMilis / (24 * 60 * 60 * 1000);
		
		return diffInDays;
	}
}
