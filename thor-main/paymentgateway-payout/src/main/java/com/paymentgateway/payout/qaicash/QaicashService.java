package com.paymentgateway.payout.qaicash;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.StatusType;
import com.paymentgateway.payout.apexPay.Constants;
import com.paymentgateway.payout.merchantApi.VendorPayoutDao;
import com.paymentgateway.pg.core.util.QaicashUtil;

@Service
public class QaicashService {

	private static Logger logger = LoggerFactory.getLogger(QaicashService.class);

	@Autowired
	private QaicashRequestCreator qaicashRequestCreator;

	@Autowired
	private QaicashComunicator qaicashComunicator;

	@Autowired
	private QaicashResponseHandler qaicashResponseHandler;

	@Autowired
	private VendorPayoutDao vendorPayoutDao;

	@Autowired
	private UserDao userDao;

	@Autowired
	private Fields field;

	@Autowired
	private QaicashUtil qaicashUtil;

	public void payoutTransaction(Fields fields, JSONObject adfFields, User user) {

		logger.info("inside payoutTransaction() :: Qaicash");

		try {
			if (fields.get(FieldType.TXNTYPE.getName()).equalsIgnoreCase("RTGS")
					|| fields.get(FieldType.TXNTYPE.getName()).equalsIgnoreCase("NEFT")
					|| fields.get(FieldType.TXNTYPE.getName()).equalsIgnoreCase("UPI")) {
				// no txntype given by bank
				logger.info("Qaicash do not have RTGS , NEFT or UPI Txn Type ");
				fields.put(FieldType.STATUS.getName(), StatusType.REJECTED_BY_PG.getName());
				fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.INVALID_TXN_TYPE.getCode());
				fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.INVALID_TXN_TYPE.getResponseMessage());
				fields.put(FieldType.PG_TXN_MESSAGE.getName(), ErrorType.INVALID_TXN_TYPE.getResponseMessage());
				fields.put(FieldType.IS_STATUS_FINAL.getName(), "Y");

			} else {

				String requestPayload = qaicashRequestCreator.createTransactionRequest(fields, adfFields);
				StringBuilder url = new StringBuilder();
				url.append(adfFields.getString(Constants.ADF_1));
				url.append("/");
				url.append(fields.get(FieldType.ADF1.getName()));
				url.append("/payout/preapproved");
				// insert Closing Amount
				if ((StringUtils.isNotBlank(fields.get(FieldType.USER_TYPE.getName())))
						&& ((fields.get(FieldType.USER_TYPE.getName()).equalsIgnoreCase("Merchant Initiated Direct"))
								|| (fields.get(FieldType.USER_TYPE.getName())
										.equalsIgnoreCase("Merchant Initiated Indirect")))) {
					logger.info("Insertion from Payout");
					vendorPayoutDao.insertUpdateForClosing(fields);
				}

				String response = qaicashComunicator.communication(requestPayload, url.toString(), fields);
				qaicashResponseHandler.handleTransactionResponse(response, fields);

				if (!fields.get(FieldType.STATUS.getName()).equals(StatusType.CAPTURED.getName())) {
					fields.put(FieldType.STATUS.getName(), StatusType.PROCESSING.getName());
					fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.PROCESSING.getCode());
					fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.PROCESSING.getInternalMessage());
				}

				String payId = fields.get(FieldType.PAY_ID.getName());

				if (user == null)
					user = userDao.findPayId(payId);

				fields.put(FieldType.VIRTUAL_AC_CODE.getName(), user.getVirtualAccountNo());

				if (StringUtils.isNotBlank(user.getSuperMerchantId())) {
					fields.put(FieldType.SUB_MERCHANT_ID.getName(), fields.get(FieldType.PAY_ID.getName()));
					fields.put(FieldType.PAY_ID.getName(), user.getSuperMerchantId());
				}

				if (fields.contains("flagBulk")) {
					fields.remove("flagBulk");
					field.updateIciciIMPSBulkTransaction(fields);
				} else {
					field.insertIciciCompositeFields(fields);
				}

				if (StringUtils.isNotBlank(fields.get(FieldType.SUB_MERCHANT_ID.getName()))) {
					if (StringUtils.isNotBlank(fields.get(FieldType.SUB_MERCHANT_ID.getName()))) {
						logger.info("Found Sub_Merchant ID " + fields.get(FieldType.SUB_MERCHANT_ID.getName()));

						fields.put(FieldType.PAY_ID.getName(), fields.get(FieldType.SUB_MERCHANT_ID.getName()));
						fields.remove(FieldType.SUB_MERCHANT_ID.getName());
					}
				}

				fields.remove(FieldType.VIRTUAL_AC_CODE.getName());
			}

		} catch (Exception e) {
			logger.info("exception in payoutTransaction() ", e);
		}

	}

	public boolean payoutStatus(Fields fields, JSONObject adfFields) {
		try {
			String url = qaicashRequestCreator.createStatusEnqRequest(fields, adfFields);

			String response = qaicashComunicator.statusEnquiry(url, fields);

			boolean isFinalStatus = vendorPayoutDao.checkTxnFinalStatus(fields);

			qaicashResponseHandler.handleStatusEnquiryTransactionResponse(response, fields);

			field.updateIciciIMPSTransaction(fields);
			return isFinalStatus;
		} catch (Exception e) {
			logger.info("Exception in payoutStatus() ", e);
		}
		return false;
	}

	public boolean payoutStatusCallbackRespones(Fields fields, String responseJson) {
		try {

			boolean isFinalStatus = vendorPayoutDao.checkTxnFinalStatus(fields);
			qaicashResponseHandler.handleTransactionResponse(responseJson, fields);

			field.updateIciciIMPSTransaction(fields);
			return isFinalStatus;
		} catch (Exception e) {
			logger.info("Exception in payoutStatus() ", e);
		}
		return false;
	}

	public boolean checkCallbackHmac(Fields fields, Transaction transaction) {
		try {

			StringBuilder hmacString = new StringBuilder();

			if (StringUtils.isNotBlank(transaction.getOrderId())) {
				hmacString.append(transaction.getOrderId());
				hmacString.append("|");
			}

			if (StringUtils.isNotBlank(transaction.getTransactionId())) {
				hmacString.append(transaction.getTransactionId());
				hmacString.append("|");
			}

			if (StringUtils.isNotBlank(transaction.getDateCreated())) {
				hmacString.append(transaction.getDateCreated());
				hmacString.append("|");
			}

			if (StringUtils.isNotBlank(transaction.getAmount())) {
				hmacString.append(transaction.getAmount());
				hmacString.append("|");
			}

				hmacString.append("INR");
				hmacString.append("|");

			if (StringUtils.isNotBlank(transaction.getStatus())) {
				hmacString.append(transaction.getStatus());
				hmacString.append("|");
			}

			if (StringUtils.isNotBlank(transaction.getDateUpdated())) {
				hmacString.append(transaction.getDateUpdated());
				hmacString.append("|");
			}

			if (StringUtils.isNotBlank(transaction.getUserId())) {
				hmacString.append(transaction.getUserId());
			}

			String responseHmac = transaction.getMessageAuthenticationCode();
			String calculatedHmac = qaicashUtil.HMAC_SHA256(fields.get(FieldType.ADF2.getName()),
					hmacString.toString());

			if (responseHmac.equalsIgnoreCase(calculatedHmac)) {
				logger.info("Response and calculated HMAC matches for Qaicash Callback , TxnId = {}",
						transaction.getOrderId());
				return true;
			} else {
				logger.info(
						"Response and calculated hmac do not match for Qaicash Callback , TxnId = {} , Response HMAC = {}  Calculated HMAC = {}",
						transaction.getOrderId(), transaction.getMessageAuthenticationCode(), calculatedHmac);
				return false;
			}

		} catch (Exception e) {
			logger.info("Exception in checkCallbackHmac  ", e);
		}
		return false;
	}

}
