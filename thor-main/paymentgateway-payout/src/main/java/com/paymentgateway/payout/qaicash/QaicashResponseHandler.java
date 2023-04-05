package com.paymentgateway.payout.qaicash;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.dao.UserSettingDao;
import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.user.PayoutAcquirer;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.user.UserSettingData;
import com.paymentgateway.commons.util.Amount;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.StatusType;
import com.paymentgateway.payout.merchantApi.VendorPayoutDao;
import com.paymentgateway.payout.merchantApi.VendorPayoutHandler;

@Service
public class QaicashResponseHandler {

	@Autowired
	private VendorPayoutHandler vendorPayoutHandler;

	@Autowired
	private QaicashService qaicashService;

	@Autowired
	private UserDao userDao;

	@Autowired
	private VendorPayoutDao vendorPayoutDao;

	@Autowired
	private UserSettingDao userSettingDao;

	private static Logger logger = LoggerFactory.getLogger(QaicashResponseHandler.class);

	public void handleTransactionResponse(String response, Fields fields) {
		logger.info("final Response Qaicash transaction >> {} OrderId >> {}", response,
				fields.get(FieldType.ORDER_ID.getName()));
		try {
			if (StringUtils.isBlank(response)) {

				fields.put(FieldType.STATUS.getName(), ErrorType.ACQUIRER_ERROR.getResponseMessage());
				fields.put(FieldType.PG_TXN_MESSAGE.getName(), ErrorType.ACQUIRER_ERROR.getCode());
				fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.ACQUIRER_ERROR.getResponseMessage());
				fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.ACQUIRER_ERROR.getCode());

			} else {
				String status = null;
				ErrorType errorType = null;

				Transaction transaction = new Transaction(response);

				if (transaction.getStatus().equalsIgnoreCase("SUCCESS")) {
					status = StatusType.CAPTURED.getName();
					errorType = ErrorType.SUCCESS;

				} else if (transaction.getStatus().equalsIgnoreCase("APPROVED")) {
					status = StatusType.PROCESSING.getName();
					errorType = ErrorType.PROCESSING;
				}
				 else if (transaction.getStatus().equalsIgnoreCase("HELD")) {
						status = StatusType.PROCESSING.getName();
						errorType = ErrorType.PROCESSING;
					} 
				else {
					status = StatusType.FAILED.getName();
					errorType = ErrorType.FAILED;
				}

				fields.put(FieldType.STATUS.getName(), status);
				fields.put(FieldType.RESPONSE_MESSAGE.getName(), errorType.getResponseMessage());
				fields.put(FieldType.RESPONSE_CODE.getName(), errorType.getCode());

				if (StringUtils.isNotBlank(transaction.getNotes())) {
					fields.put(FieldType.PG_TXN_MESSAGE.getName(), transaction.getNotes());
				} else {
					fields.put(FieldType.PG_TXN_MESSAGE.getName(), errorType.getResponseMessage());
				}

				fields.put(FieldType.RRN.getName(), "NA");
				fields.put(FieldType.UTR_NO.getName(), "NA");

				if (StringUtils.isNotBlank(transaction.getTransactionId())) {
					fields.put(FieldType.ACQ_ID.getName(), transaction.getTransactionId());
				}

			}
			fields.put(FieldType.PG_REF_NUM.getName(), fields.get(FieldType.TXN_ID.getName()));

		} catch (Exception e) {
			logger.info("Exception in handleTransactionResponse() ", e);
		} finally {
			fields.remove(FieldType.ADF1.getName());
			fields.remove(FieldType.ADF1.getName());
		}

	}

	public void handleStatusEnquiryTransactionResponse(String response, Fields fields) throws SystemException {
		try {
			if (StringUtils.isNotBlank(response)) {

				String status = null;
				ErrorType errorType = null;

				Transaction transaction = new Transaction(response);

				if (StringUtils.isNotBlank(transaction.getStatus())
						&& transaction.getStatus().equalsIgnoreCase("SUCCESS")) {
					status = StatusType.CAPTURED.getName();
					errorType = ErrorType.SUCCESS;

				} else if (StringUtils.isNotBlank(transaction.getStatus())
						&& transaction.getStatus().equalsIgnoreCase("APPROVED")) {
					status = StatusType.PROCESSING.getName();
					errorType = ErrorType.PROCESSING;
				} else {
					status = StatusType.FAILED.getName();
					errorType = ErrorType.FAILED;
				}

				fields.put(FieldType.STATUS.getName(), status);
				fields.put(FieldType.RESPONSE_MESSAGE.getName(), errorType.getResponseMessage());
				fields.put(FieldType.RESPONSE_CODE.getName(), errorType.getCode());

				if (StringUtils.isNotBlank(transaction.getNotes())) {
					fields.put(FieldType.PG_TXN_MESSAGE.getName(), transaction.getNotes());
				} else {
					fields.put(FieldType.PG_TXN_MESSAGE.getName(), transaction.getStatus());
				}

				fields.put(FieldType.RRN.getName(), "NA");
				fields.put(FieldType.UTR_NO.getName(), "NA");

				if (StringUtils.isNotBlank(transaction.getTransactionId())) {
					fields.put(FieldType.ACQ_ID.getName(), transaction.getTransactionId());
				}

			} else {
				logger.info("Empty Response Status Enquiry, Qaicash handleStatusEnquiryTransactionResponse() {} ",
						response);
				throw new SystemException(
						"Empty Response Status Enquiry, Qaicash handleStatusEnquiryTransactionResponse()");
			}

		} catch (Exception e) {
			logger.info("Exception in handleStatusEnquiryTransactionResponse() ", e);
		}

	}

	public void handleQaicashCallback(String reqJson, Fields fields) throws SystemException {
		logger.info("Inside handleQaicashCallback");
		try {

			Transaction transaction = new Transaction(reqJson);

			if (StringUtils.isBlank(transaction.getOrderId())) {
				throw new SystemException("Merchant Order Id not found in qaicash callback");
			}

			vendorPayoutHandler.populatePayoutFields(fields, transaction.getOrderId(), PayoutAcquirer.QAICASH.name());
		
			if (!fields.getFields().isEmpty()) {
				vendorPayoutHandler.getQaicashMappingData(fields);
				
				// validate HMAC in Payout Response
				boolean isHmacValid = qaicashService.checkCallbackHmac(fields,transaction);
				if (!isHmacValid) {
					
					logger.info("HMAC received in callabck response is incorrect , TxnId =  {}" , transaction.getOrderId());
					throw new SystemException("Invalid HMAC received");
				}
				
				// validate Amount in Payout Response
				
				String amtFrmCallback = transaction.getAmount();
				String amtFrmFields = fields.get(FieldType.AMOUNT.getName());
				
				if (!amtFrmCallback.equalsIgnoreCase(amtFrmFields)) {
					logger.info("Qaicash Payout Amount in callback {} does not match Amount in Fields {} for Txn Id {}",amtFrmCallback,amtFrmFields,fields.get(FieldType.PG_REF_NUM.getName()));
				}
				
				boolean isFinalStatus = qaicashService.payoutStatusCallbackRespones(fields, reqJson);

				String payId = fields.get(FieldType.PAY_ID.getName());

				if (!isFinalStatus) {

					User user = userDao.findPayId(payId);

					if (StringUtils.isNotBlank(user.getSuperMerchantId())) {
						fields.put(FieldType.SUB_MERCHANT_ID.getName(), fields.get(FieldType.PAY_ID.getName()));
						fields.put(FieldType.PAY_ID.getName(), user.getSuperMerchantId());
					}

					try {

						if (fields.contains(FieldType.USER_TYPE.getName())) {
							if (StringUtils.isNotBlank(fields.get(FieldType.USER_TYPE.getName())) && (fields
									.get(FieldType.USER_TYPE.getName()).equalsIgnoreCase("Merchant Initiated Direct")
									|| fields.get(FieldType.USER_TYPE.getName())
											.equalsIgnoreCase("Merchant Initiated Indirect"))) {
								if (fields.contains(FieldType.STATUS.getName())) {
									if (!((fields.get(FieldType.STATUS.getName())
											.equalsIgnoreCase(StatusType.CAPTURED.getName()))
											|| (fields.get(FieldType.STATUS.getName())
													.equalsIgnoreCase(StatusType.TIMEOUT.getName())
													|| (fields.get(FieldType.STATUS.getName())
															.equalsIgnoreCase(StatusType.PROCESSING.getName()))))) {
										vendorPayoutDao.UpdateForClosing(fields);
									}
								}
							}
						}
					} catch (Exception exception) {
						logger.error("Exception in updating closing collection", exception);
					} finally {
						removeSubMerchantId(fields);
					}
				}

				fields.put(FieldType.AMOUNT.getName(), Amount.formatAmount(fields.get(FieldType.AMOUNT.getName()),
						fields.get(FieldType.CURRENCY_CODE.getName())));

				UserSettingData userSetting = userSettingDao.fetchDataUsingPayId(payId);

				// callback to merchant
				if (userSetting.isAllowPayoutStatusEnquiryCallbackFlag()) {
					if (StringUtils.isNotBlank(userSetting.getPayoutCallbackUrl())) {
						vendorPayoutHandler.payoutCallbackToMerchant(fields, userSetting);
					} else {
						// do nothing, just logging
						logger.info("Payout Status enquiry, Callback url is empty");
					}
				} else {
					// just logging
					logger.info("Payout Status enquiry, Callback flag not active");
				}

			} else {
				throw new SystemException("No Transaction Found");
			}

		} catch (Exception e) {
			logger.info("Exception in communicateFonePaisaTransactionStatusEnq() ", e);
			throw new SystemException(e.getMessage());
		}

	}

	private void removeSubMerchantId(Fields fields) {
		if (StringUtils.isNotBlank(fields.get(FieldType.SUB_MERCHANT_ID.getName()))) {
			logger.info("Found Sub_Merchant ID " + fields.get(FieldType.SUB_MERCHANT_ID.getName()));

			fields.put(FieldType.PAY_ID.getName(), fields.get(FieldType.SUB_MERCHANT_ID.getName()));
			fields.remove(FieldType.SUB_MERCHANT_ID.getName());
		}
	}
}
