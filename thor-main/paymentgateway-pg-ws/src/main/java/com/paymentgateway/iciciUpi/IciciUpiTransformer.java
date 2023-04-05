package com.paymentgateway.iciciUpi;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.util.Amount;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.StatusType;
import com.paymentgateway.commons.util.TransactionType;

/**
 * @author Amitosh
 *
 */
@Service
public class IciciUpiTransformer {

	private Transaction transaction = null;

	private static Logger logger = LoggerFactory.getLogger(IciciUpiTransformer.class.getName());

	public void updateSaleResponse(Fields fields, String response) throws SystemException {
		JSONObject jsonResponse = new JSONObject(response);
		try {
			logger.info("Inside ICICI UPI Transformer in updateResponse method response code is  "
					+ jsonResponse.getString(Constants.RESPONSE_CODE));

			String txnType = fields.get(FieldType.TXNTYPE.getName());
			String status = "";
			ErrorType errorType = null;
			String pgTxnMsg = "";

			if (txnType.equals(TransactionType.SALE.getName())) {
				if (Boolean.valueOf(jsonResponse.getString(Constants.SUCCESS_FLAG))
						&& (jsonResponse.getString(Constants.RESPONSE_CODE)
								.equalsIgnoreCase(Constants.TRANSACTION_INITIATED_RESPONSE_CODE))) {
					status = StatusType.SENT_TO_BANK.getName();
					errorType = ErrorType.SUCCESS;
					pgTxnMsg = ErrorType.SUCCESS.getResponseMessage();
				} else {
					if (StringUtils.isNotBlank(jsonResponse.getString(Constants.RESPONSE_CODE))) {
						IciciUpiResultType resultInstance = IciciUpiResultType
								.getInstanceFromCode(jsonResponse.getString(Constants.RESPONSE_CODE));
						logger.info("Inside ICICI UPI  Transformer in updateResponse method resultInstance is "
								+ resultInstance);
						if (resultInstance != null) {
							if (resultInstance.getlPayCode() != null) {
								logger.info("Inside ICICI UPI Transformer in updateResponse method resultInstance is "
										+ resultInstance.getMessage() + (resultInstance.getlPayCode()));
								if ((resultInstance.equals(IciciUpiResultType.ICICIUPI0018))
										|| (resultInstance.equals(IciciUpiResultType.ICICIUPI0012))
										|| (resultInstance.equals(IciciUpiResultType.ICICIUPI0019))
										|| (resultInstance.equals(IciciUpiResultType.ICICIUPI0015))
										|| (resultInstance.equals(IciciUpiResultType.ICICIUPI0022))) {
									status = StatusType.REJECTED.getName();
									errorType = ErrorType.INVALID_VPA;
									pgTxnMsg = ErrorType.INVALID_VPA.getResponseMessage();
								} else {
									status = resultInstance.getStatusCode();
									errorType = ErrorType.getInstanceFromCode(resultInstance.getlPayCode());
									pgTxnMsg = resultInstance.getMessage();
								}
							} else {
								status = StatusType.REJECTED.getName();
								errorType = ErrorType.REJECTED;
								pgTxnMsg = ErrorType.REJECTED.getResponseMessage();
							}
						} else {
							status = StatusType.REJECTED.getName();
							errorType = ErrorType.REJECTED;
							pgTxnMsg = ErrorType.REJECTED.getResponseMessage();
						}
					} else {
						status = StatusType.FAILED_AT_ACQUIRER.getName();
						errorType = ErrorType.REJECTED;
						pgTxnMsg = Constants.NO_RESSPONSE_RECEIVED;
					}
				}
			}

			fields.put(FieldType.STATUS.getName(), status);
			fields.put(FieldType.RESPONSE_MESSAGE.getName(), errorType.toString().replaceAll("_", ""));
			fields.put(FieldType.RESPONSE_CODE.getName(), errorType.getResponseCode());
			fields.put(FieldType.ACQ_ID.getName(), "");
			fields.put(FieldType.RRN.getName(), "");
			if (jsonResponse.has(Constants.BANK_RRN)) {
				fields.put(FieldType.RRN.getName(), jsonResponse.getString(Constants.BANK_RRN));
			} else if (jsonResponse.has(Constants.BANK_RRN1)) {
				fields.put(FieldType.RRN.getName(), jsonResponse.getString(Constants.BANK_RRN1));
			}

			if (jsonResponse.has(Constants.RESPONSE)) {
				fields.put(FieldType.PG_RESP_CODE.getName(), jsonResponse.getString(Constants.RESPONSE));
			}

			if (jsonResponse.has(Constants.STATUS)) {
				fields.put(FieldType.PG_TXN_STATUS.getName(), jsonResponse.getString(Constants.STATUS));
			}
			fields.put(FieldType.UDF1.getName(), "");

			// fields.put(FieldType.PG_DATE_TIME.getName(), "");
		} catch (Exception e) {
			logger.error("Unknown Exception : " + e);
			throw new SystemException(ErrorType.INTERNAL_SYSTEM_ERROR, e,
					"unknown exception in UpdateResponse method for ICICI upi in ICICI UPI Transformer");
		}
	}

	public void updateRefundResponse(Fields fields, JSONObject refundResponse) {
		String status = null;
		ErrorType errorType = null;
		String pgTxnMsg = null;
		if (refundResponse.has(Constants.SUCCESS_FLAG) && refundResponse.has(Constants.STATUS)) {
			if ((Boolean.valueOf(refundResponse.getString(Constants.SUCCESS_FLAG)))
					&& (refundResponse.getString(Constants.STATUS).equalsIgnoreCase(Constants.SUCCESS))) {
				status = StatusType.CAPTURED.getName();
				errorType = ErrorType.SUCCESS;
				pgTxnMsg = ErrorType.SUCCESS.getResponseMessage();
			} else {
				if (refundResponse.has(Constants.RESPONSE)) {
					IciciUpiResultType resultInstance = IciciUpiResultType
							.getInstanceFromCode(refundResponse.getString(Constants.RESPONSE));
					if (resultInstance != null) {
						if (resultInstance.getlPayCode() != null) {
							status = resultInstance.getlPayCode();
							errorType = ErrorType.getInstanceFromCode(resultInstance.getlPayCode());
							pgTxnMsg = resultInstance.getMessage();
						} else {
							status = StatusType.REJECTED.getName();
							errorType = ErrorType.REJECTED;
							pgTxnMsg = ErrorType.REJECTED.getResponseMessage();
						}
					} else {
						status = StatusType.REJECTED.getName();
						errorType = ErrorType.REJECTED;
						pgTxnMsg = ErrorType.REJECTED.getResponseMessage();
					}
				} else {
					status = StatusType.REJECTED.getName();
					errorType = ErrorType.REJECTED;
					pgTxnMsg = ErrorType.REJECTED.getResponseMessage();
				}
			}
		} else {
			status = StatusType.REJECTED.getName();
			errorType = ErrorType.REJECTED;
			pgTxnMsg = ErrorType.REJECTED.getResponseMessage();
		}

		fields.put(FieldType.STATUS.getName(), status);
		fields.put(FieldType.RESPONSE_MESSAGE.getName(), errorType.toString().replaceAll("_", ""));
		fields.put(FieldType.RESPONSE_CODE.getName(), errorType.getResponseCode());
		fields.put(FieldType.ACQ_ID.getName(), "");
		if (refundResponse.has(Constants.BANK_RRN)) {
			fields.put(FieldType.RRN.getName(), refundResponse.getString(Constants.BANK_RRN));
		} else if (refundResponse.has(Constants.BANK_RRN1)) {
			fields.put(FieldType.RRN.getName(), refundResponse.getString(Constants.BANK_RRN1));
		}

		if (refundResponse.has(Constants.RESPONSE)) {
			fields.put(FieldType.PG_RESP_CODE.getName(), refundResponse.getString(Constants.RESPONSE));
		}

		if (refundResponse.has(Constants.STATUS)) {
			fields.put(FieldType.PG_TXN_STATUS.getName(), refundResponse.getString(Constants.STATUS));
		}
		fields.put(FieldType.PG_TXN_MESSAGE.getName(), pgTxnMsg);
		fields.put(FieldType.UDF1.getName(), "");
		// fields.put(FieldType.PG_DATE_TIME.getName(), "");
	}

	public void updateEnquiryResponse(Fields fields, JSONObject enquiryResponse) {
		String status = null;
		ErrorType errorType = null;
		String pgTxnMsg = null;
		if (!fields.get(FieldType.AMOUNT.getName()).contains(".")) {
			fields.put(FieldType.AMOUNT.getName(),
					Amount.formatAmount(fields.get(FieldType.AMOUNT.getName()).replace(".", ""), "356"));
		}

		if (!fields.get(FieldType.TOTAL_AMOUNT.getName()).contains(".")) {
			fields.put(FieldType.TOTAL_AMOUNT.getName(),
					Amount.formatAmount(fields.get(FieldType.TOTAL_AMOUNT.getName()).replace(".", ""), "356"));
		}

		if (enquiryResponse.has(Constants.SUCCESS_FLAG) || enquiryResponse.has(Constants.STATUS)) {
			if (((enquiryResponse.toString().contains("status") && enquiryResponse.get("status").toString().equalsIgnoreCase("SUCCESS")) && 
					(enquiryResponse.toString().contains("success") && enquiryResponse.get("success").toString().equalsIgnoreCase("true")))) {
				status = StatusType.CAPTURED.getName();
				errorType = ErrorType.SUCCESS;
				pgTxnMsg = ErrorType.SUCCESS.getResponseMessage();
			} else {
				if (enquiryResponse.has(Constants.RESPONSE)) {
					IciciUpiResultType resultInstance = null;
					
					// Don't use result Type when only response code is 0
					if (!enquiryResponse.getString(Constants.RESPONSE).equalsIgnoreCase("0")) {
						resultInstance = IciciUpiResultType
								.getInstanceFromCode(enquiryResponse.getString(Constants.RESPONSE));
					}
					
					if (resultInstance != null) {
						if (resultInstance.getlPayCode() != null) {
							status = resultInstance.getlPayCode();
							errorType = ErrorType.getInstanceFromCode(resultInstance.getlPayCode());
							pgTxnMsg = resultInstance.getMessage();
						} else {
							status = StatusType.REJECTED.getName();
							errorType = ErrorType.REJECTED;
							pgTxnMsg = ErrorType.REJECTED.getResponseMessage();
						}
					} else {
						status = StatusType.REJECTED.getName();
						errorType = ErrorType.REJECTED;
						pgTxnMsg = ErrorType.REJECTED.getResponseMessage();
					}
				} else {
					status = StatusType.REJECTED.getName();
					errorType = ErrorType.REJECTED;
					pgTxnMsg = ErrorType.REJECTED.getResponseMessage();
				}
			}
		} else {
			status = StatusType.REJECTED.getName();
			errorType = ErrorType.REJECTED;
			pgTxnMsg = ErrorType.REJECTED.getResponseMessage();
		}
		fields.put(FieldType.STATUS.getName(), status);
		fields.put(FieldType.RESPONSE_MESSAGE.getName(), errorType.toString().replaceAll("_", ""));
		fields.put(FieldType.RESPONSE_CODE.getName(), errorType.getResponseCode());
		fields.put(FieldType.ACQ_ID.getName(), "");

		if (enquiryResponse.has(Constants.BANK_RRN)) {
			fields.put(FieldType.RRN.getName(), enquiryResponse.getString(Constants.BANK_RRN));
		} else if (enquiryResponse.has(Constants.BANK_RRN1)) {
			fields.put(FieldType.RRN.getName(), enquiryResponse.getString(Constants.BANK_RRN1));
		}

		if (enquiryResponse.has(Constants.RESPONSE)) {
			fields.put(FieldType.PG_RESP_CODE.getName(), enquiryResponse.getString(Constants.RESPONSE));
		}

		if (enquiryResponse.has(Constants.STATUS)) {
			fields.put(FieldType.PG_TXN_STATUS.getName(), enquiryResponse.getString(Constants.STATUS));
		}

		fields.put(FieldType.PG_TXN_MESSAGE.getName(), pgTxnMsg);
		fields.put(FieldType.UDF1.getName(), "");
	}

	public IciciUpiTransformer(Transaction transaction) {
		this.transaction = transaction;
	}

	public Transaction getTransaction() {
		return transaction;
	}

	public void setTransaction(Transaction transaction) {
		this.transaction = transaction;
	}

}
