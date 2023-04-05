package com.paymentgateway.payout.FloxyPay;

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

@Service
public class FloxyPayService {

	private static Logger logger = LoggerFactory.getLogger(FloxyPayService.class);

	@Autowired
	private FloxyPayRequestCreator floxyPayRequestCreator;

	@Autowired
	private FloxyPayComunicator floxyPayComunicator;

	@Autowired
	private FloxyPayResponseHandler floxyPayResponseHandler;

	@Autowired
	private VendorPayoutDao vendorPayoutDao;

	@Autowired
	private UserDao userDao;

	@Autowired
	private Fields field;

	public void payoutTransaction(Fields fields, JSONObject adfFields, User user) {

		logger.info("inside payoutTransaction() :: FloxyPay");

		try {
			if (fields.get(FieldType.TXNTYPE.getName()).equalsIgnoreCase("RTGS")
					|| fields.get(FieldType.TXNTYPE.getName()).equalsIgnoreCase("NEFT")) {
				// no txntype given by bank
				logger.info("FloxyPay do not have RTGS & NEFT Txn Type ");
				fields.put(FieldType.STATUS.getName(), StatusType.REJECTED_BY_PG.getName());
				fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.INVALID_TXN_TYPE.getCode());
				fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.INVALID_TXN_TYPE.getResponseMessage());
				fields.put(FieldType.PG_TXN_MESSAGE.getName(), ErrorType.INVALID_TXN_TYPE.getResponseMessage());
				fields.put(FieldType.IS_STATUS_FINAL.getName(), "Y");

			} else {

				String requestPayload = floxyPayRequestCreator.createTransactionRequest(fields, adfFields);

				String url = null;

				if (StringUtils.isNotBlank(fields.get(FieldType.PAYER_ADDRESS.getName()))) {
					url = adfFields.getString(Constants.ADF_2);
				} else {
					url = adfFields.getString(Constants.ADF_1);
				}

				// insert Closing Amount
				if ((StringUtils.isNotBlank(fields.get(FieldType.USER_TYPE.getName())))
						&& ((fields.get(FieldType.USER_TYPE.getName()).equalsIgnoreCase("Merchant Initiated Direct"))
								|| (fields.get(FieldType.USER_TYPE.getName())
										.equalsIgnoreCase("Merchant Initiated Indirect")))) {
					logger.info("Insertion from Payout");
					vendorPayoutDao.insertUpdateForClosing(fields);
				}

				String response = floxyPayComunicator.communication(requestPayload, url, fields);
				floxyPayResponseHandler.handleTransactionResponse(response, fields);

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
			String requestPayload = floxyPayRequestCreator.createStatusEnqRequest(fields, adfFields);
			String url = adfFields.getString(Constants.ADF_3);

			String response = floxyPayComunicator.communication(requestPayload, url, fields);

			boolean isFinalStatus = vendorPayoutDao.checkTxnFinalStatus(fields);

			floxyPayResponseHandler.handleStatusEnquiryTransactionResponse(response, fields);

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

			floxyPayResponseHandler.handleTransactionResponse(responseJson, fields);

			field.updateIciciIMPSTransaction(fields);
			return isFinalStatus;
		} catch (Exception e) {
			logger.info("Exception in payoutStatus() ", e);
		}
		return false;
	}

}
