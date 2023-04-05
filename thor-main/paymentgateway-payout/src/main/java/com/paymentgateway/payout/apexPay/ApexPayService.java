package com.paymentgateway.payout.apexPay;

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
import com.paymentgateway.payout.merchantApi.VendorPayoutDao;

@Service
public class ApexPayService {

	private static final Logger logger = LoggerFactory.getLogger(ApexPayService.class);

	@Autowired
	private ApexPayComunicator apexPayComunicator;

	@Autowired
	private ApexPayRequestCreator apexPayRequestCreator;

	@Autowired
	private ApexPayResponseHandler apexPayResponseHandler;

	@Autowired
	private ApexPayUtils apexPayUtils;

	@Autowired
	private UserDao userDao;

	@Autowired
	private Fields field;

	@Autowired
	private VendorPayoutDao vendorPayoutDao;

	public void payoutTransaction(Fields fields, JSONObject adfFields, User user) {
		logger.info("inside payoutTransaction");
		try {
			if (fields.get(FieldType.TXNTYPE.getName()).equalsIgnoreCase("UPI")) {
				// no txntype given by bank
				logger.info("Apex pay do not have UPI Txn Type ");
				fields.put(FieldType.STATUS.getName(), StatusType.REJECTED_BY_PG.getName());
				fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.INVALID_TXN_TYPE.getCode());
				fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.INVALID_TXN_TYPE.getResponseMessage());
				fields.put(FieldType.PG_TXN_MESSAGE.getName(), ErrorType.INVALID_TXN_TYPE.getResponseMessage());
				fields.put(FieldType.IS_STATUS_FINAL.getName(), "Y");

			} else {

				String requestPayload = apexPayRequestCreator.createTransactionRequest(fields, adfFields);
				String url = adfFields.getString(Constants.ADF_1);

				// insert Closing Amount
				if ((StringUtils.isNotBlank(fields.get(FieldType.USER_TYPE.getName())))
						&& ((fields.get(FieldType.USER_TYPE.getName()).equalsIgnoreCase("Merchant Initiated Direct"))
								|| (fields.get(FieldType.USER_TYPE.getName())
										.equalsIgnoreCase("Merchant Initiated Indirect")))) {
					logger.info("Insertion from UPI");
					vendorPayoutDao.insertUpdateForClosing(fields);
				}

				String response = apexPayComunicator.postCommunication(requestPayload, url, fields);
				apexPayResponseHandler.handleTransactionResponse(response, fields);

				// updating rather than capture status to Processing
				apexPayUtils.updateStatusForPayoutTransaction(fields);
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

			if (StringUtils.isNotBlank(fields.get(FieldType.SUB_MERCHANT_ID.getName())))
				apexPayUtils.removeSubMerchantId(fields);

			fields.remove(FieldType.VIRTUAL_AC_CODE.getName());

		} catch (Exception e) {
			logger.info("exception in payoutTransaction() ", e);
		}

	}

	public boolean payoutStatus(Fields fields, JSONObject adfFields) {
		try {
			String requestPayload = apexPayRequestCreator.createStatusEnqRequest(fields, adfFields);
			String url = adfFields.getString(Constants.ADF_2);

			String response = apexPayComunicator.getCommunication(requestPayload, url, fields);

			boolean isFinalStatus = vendorPayoutDao.checkTxnFinalStatus(fields);

			apexPayResponseHandler.handleStatusEnquiryResponse(response, fields);
			field.updateIciciIMPSTransaction(fields);

			return isFinalStatus;
		} catch (Exception e) {
			logger.info("Exception in payoutStatus() ", e);
		}
		return false;
	}
	
	public boolean apexPayoutCallbackResponse(Fields fields , String response) {
		try {

			boolean isFinalStatus = vendorPayoutDao.checkTxnFinalStatus(fields);

			apexPayResponseHandler.handleApexCallbackResponse(response, fields);
			field.updateIciciIMPSTransaction(fields);

			return isFinalStatus;
		} catch (Exception e) {
			logger.info("Exception in payoutStatus() ", e);
		}
		return false;
	}

	public void checkBalance(Fields fields, JSONObject adfFields) {
		try {
			String requestPayload = apexPayRequestCreator.createStatusEnqRequest(fields, adfFields);
			String url = adfFields.getString(Constants.ADF_3);

			String response = apexPayComunicator.getCommunication(requestPayload, url, fields);

			if (StringUtils.isNotBlank(response)) {
				fields.put(FieldType.AVAILABLE_BALANCE.getName(), response);
			} else {
				fields.put(FieldType.AVAILABLE_BALANCE.getName(), String.valueOf(0.00));
			}
		} catch (Exception e) {
			logger.info("Exception in checkBalance() ", e);
		}

	}

}
