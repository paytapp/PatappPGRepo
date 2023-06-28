package com.paymentgateway.idfcUpi;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.util.AcquirerType;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.IdfcUpiUpiResultType;
import com.paymentgateway.commons.util.StatusType;
import com.paymentgateway.commons.util.TransactionType;

/**
 * @author VJ
 *
 */
@Service("idfcUpiTransformer")
public class IdfcUpiTransformer {
	private static Logger logger = LoggerFactory.getLogger(IdfcUpiTransformer.class.getName());
	private Transaction transaction = null;

	public IdfcUpiTransformer(Transaction transactionResponse) {
		this.transaction = transaction;
	}

	public Transaction getTransaction() {
		return transaction;
	}

	public void setTransaction(Transaction transaction) {
		this.transaction = transaction;
	}

	public void updateInvalidVpaResponse(Fields fields, String response) {

		fields.put(FieldType.STATUS.getName(), StatusType.REJECTED.getName());
		fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.INVALID_VPA.getResponseCode());
		fields.put(FieldType.PG_TXN_MESSAGE.getName(), ErrorType.INVALID_VPA.getResponseMessage());

	}

	public void updateResponse(Fields fields, Transaction transactionResponse) throws SystemException {
		try {
			logger.info(" inside IDFCUPI  idfcUpiTransformer in  updateResponse method response code is ==  "
					+ transactionResponse.getStatus());
			
			String txnType = fields.get(FieldType.TXNTYPE.getName());
			String status = "";
			ErrorType errorType = null;
			String pgTxnMsg = "";
			if (txnType.equals(TransactionType.SALE.getName())) {
				
				if (StringUtils.isNotBlank(transactionResponse.getStatus())
						&& transactionResponse.getStatus().equals(Constants.SUCCESS_RESPONSE)) {
					logger.info("IDFC UPI Success Response received");
					fields.put(FieldType.STATUS.getName(), StatusType.CAPTURED.getName());
					status = StatusType.SENT_TO_BANK.getName();
					errorType = ErrorType.SUCCESS;
					pgTxnMsg = ErrorType.SUCCESS.getResponseMessage();
				} else {
					
					if (StringUtils.isNotBlank(transactionResponse.getStatus())) {
						IdfcUpiUpiResultType resultInstance = IdfcUpiUpiResultType
								.getInstanceFromName(transactionResponse.getStatus());
						logger.info(" inside idfcUpi Transformer  in  updateResponse method resultInstance is : == "
								+ resultInstance);

						if (resultInstance != null) {
							if (resultInstance.getPaymentGatewayCode() != null) {
								logger.info(
										" inside IDFCUPI Transformer  in  updateResponse method resultInstance is for collect transaction ==  "
												+ resultInstance.getStatusName() + (resultInstance.getPaymentGatewayCode()));
								status = resultInstance.getStatusName();
								errorType = ErrorType.getInstanceFromCode(resultInstance.getPaymentGatewayCode());
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
						status = StatusType.FAILED_AT_ACQUIRER.getName();
						errorType = ErrorType.REJECTED;
						pgTxnMsg = Constants.NO_RES_RECEIVED_FROM_MERCHANT;

					}

				}
			} else if (txnType.equals(TransactionType.REFUND.getName())) {
				if (StringUtils.isNotBlank(transactionResponse.getStatus())) {
					if (transactionResponse.getStatus().equalsIgnoreCase("000")) {
						status = StatusType.CAPTURED.getName();
						errorType = ErrorType.SUCCESS;
						pgTxnMsg = ErrorType.SUCCESS.getResponseMessage();
					} else {
						if (StringUtils.isNotBlank(transactionResponse.getStatus())) {
							IdfcUpiUpiResultType resultInstance = IdfcUpiUpiResultType
									.getInstanceFromName(transactionResponse.getResponse());
							logger.info(" Inside IDFCUpi Transformer  in  updateResponse method resultInstance is : == "
									+ resultInstance);
							if (resultInstance != null) {
								if (resultInstance.getPaymentGatewayCode() != null) {
									logger.info(
											" inside idfcupi Transformer action in  updateStatusResponse method resultInstance is  for refund transaction ==  "
													+ resultInstance.getStatusName()
													+ (resultInstance.getPaymentGatewayCode()));
									status = resultInstance.getStatusName();
									errorType = ErrorType.getInstanceFromCode(resultInstance.getPaymentGatewayCode());
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
					pgTxnMsg = Constants.NO_RES_RECEIVED_FROM_MERCHANT;
				}

			} else if (txnType.equals(TransactionType.ENQUIRY.getName())) {
				
				
				if (StringUtils.isNotBlank(transactionResponse.getStatus())
						&& transactionResponse.getStatus().equals(Constants.SUCCESS_RESPONSE)) {
					logger.info("IDFC UPI Success Response received");
					status = StatusType.CAPTURED.getName();
					errorType = ErrorType.SUCCESS;
					pgTxnMsg = ErrorType.SUCCESS.getResponseMessage();
				} else {
					
					if (StringUtils.isNotBlank(transactionResponse.getStatus())) {
						IdfcUpiUpiResultType resultInstance = IdfcUpiUpiResultType
								.getInstanceFromName(transactionResponse.getStatus());
						logger.info(" inside idfcUpi Transformer  in  updateResponse method resultInstance is : == "
								+ resultInstance);

						if (resultInstance != null) {
							if (resultInstance.getPaymentGatewayCode() != null) {
								logger.info(
										" inside IDFCUPI Transformer  in  updateResponse method resultInstance is for collect transaction ==  "
												+ resultInstance.getStatusName() + (resultInstance.getPaymentGatewayCode()));
								status = resultInstance.getStatusName();
								errorType = ErrorType.getInstanceFromCode(resultInstance.getPaymentGatewayCode());
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
						status = StatusType.FAILED_AT_ACQUIRER.getName();
						errorType = ErrorType.REJECTED;
						pgTxnMsg = Constants.NO_RES_RECEIVED_FROM_MERCHANT;

					}

				}
				
			}
			
			fields.put(FieldType.STATUS.getName(), status);
			fields.put(FieldType.RESPONSE_MESSAGE.getName(), errorType.getResponseMessage().replaceAll("_", ""));
			fields.put(FieldType.RESPONSE_CODE.getName(), errorType.getResponseCode());
			fields.put(FieldType.ACQ_ID.getName(), transactionResponse.getAcq_id());
			fields.put(FieldType.RRN.getName(), transactionResponse.getRrn());
			fields.put(FieldType.PG_RESP_CODE.getName(), transactionResponse.getResponse());
			fields.put(FieldType.PG_TXN_STATUS.getName(), transactionResponse.getStatus());
			fields.put(FieldType.PG_TXN_MESSAGE.getName(), pgTxnMsg);
			fields.put(FieldType.UDF1.getName(), transactionResponse.getMerchantVpa());
			fields.put(FieldType.PG_DATE_TIME.getName(), transactionResponse.getDateTime());

		} catch (Exception e) {
			logger.error("Unknown Exception :" , e);
			throw new SystemException(ErrorType.INTERNAL_SYSTEM_ERROR, e,
					"unknown exception in UpdateResponse method for IDFC upi in IDFCUPITransformer");
		}

	}

	public void updateEnquiryResponse(Fields fields, Transaction transactionResponse) throws SystemException {
		try {

			logger.info("IDFC UPI Staus Enquiry , pg ref num == " + fields.get(FieldType.PG_REF_NUM.getName()));
			String status = "";
			ErrorType errorType = null;
			String pgTxnMsg = "";
			
			if ((StringUtils.isNotBlank(transactionResponse.getResponse())
					&& transactionResponse.getResponse().equalsIgnoreCase(Constants.SUCCESS_RESPONSE)) && (StringUtils.isNotBlank(transactionResponse.getOrgResponse())
							&& transactionResponse.getOrgResponse().equalsIgnoreCase(Constants.SUCCESS_RESPONSE)) && 
					!(transactionResponse.getOrgTxnStatus().equalsIgnoreCase("IN-PROGRESS"))) {
				logger.info("IDFC UPI Success Response received is status enquiry");
				status = StatusType.CAPTURED.getName();
				errorType = ErrorType.SUCCESS;
				pgTxnMsg = ErrorType.SUCCESS.getResponseMessage();
				
				if (StringUtils.isNotBlank(transactionResponse.getResponseMessage())) {
					pgTxnMsg = transactionResponse.getResponseMessage();
				}
				else {
					pgTxnMsg = ErrorType.SUCCESS.getResponseMessage();
				}
				
			} else {
				
				if (StringUtils.isNotBlank(transactionResponse.getStatus())) {
					IdfcUpiUpiResultType resultInstance = IdfcUpiUpiResultType
							.getInstanceFromName(transactionResponse.getResponse());

					if (resultInstance != null) {
						if (resultInstance.getPaymentGatewayCode() != null) {
							status = resultInstance.getStatusName();
							errorType = ErrorType.getInstanceFromCode(resultInstance.getPaymentGatewayCode());
							pgTxnMsg = resultInstance.getMessage();
						} else {
							status = StatusType.REJECTED.getName();
							errorType = ErrorType.REJECTED;
							if (StringUtils.isNotBlank(transactionResponse.getResponseMessage())) {
								pgTxnMsg = transactionResponse.getResponseMessage();
							}
							else {
								pgTxnMsg = ErrorType.REJECTED.getResponseMessage();
							}

						}

					} else {
						status = StatusType.REJECTED.getName();
						errorType = ErrorType.REJECTED;
						if (StringUtils.isNotBlank(transactionResponse.getResponseMessage())) {
							pgTxnMsg = transactionResponse.getResponseMessage();
						}
						else {
							pgTxnMsg = ErrorType.REJECTED.getResponseMessage();
						}
					}
				} else {
					status = StatusType.FAILED_AT_ACQUIRER.getName();
					errorType = ErrorType.REJECTED;
					if (StringUtils.isNotBlank(transactionResponse.getResponseMessage())) {
						pgTxnMsg = transactionResponse.getResponseMessage();
					}
					else {
						pgTxnMsg = ErrorType.REJECTED.getResponseMessage();
					}

				}

			}
			
			fields.put(FieldType.ACQUIRER_TYPE.getName(), AcquirerType.IDFCUPI.getCode());
			fields.put(FieldType.RESPONSE_CODE.getName(), errorType.getResponseCode());
			fields.put(FieldType.RESPONSE_MESSAGE.getName(), pgTxnMsg);
			fields.put(FieldType.PG_RESP_CODE.getName(), transactionResponse.getResponse());
			fields.put(FieldType.PG_TXN_STATUS.getName(), transactionResponse.getStatus());
			fields.put(FieldType.PG_TXN_MESSAGE.getName(), pgTxnMsg);
			fields.put(FieldType.UDF1.getName(), transactionResponse.getMerchantVpa());
			fields.put(FieldType.ACQ_ID.getName(), transactionResponse.getOrgCustRefId()); // ACQ Id should be same as RRN
			fields.put(FieldType.RRN.getName(), transactionResponse.getOrgCustRefId());
			fields.put(FieldType.PG_DATE_TIME.getName(), transactionResponse.getDateTime());
			fields.put(FieldType.AUTH_CODE.getName(), transactionResponse.getReferenceId());
			
			fields.put(FieldType.STATUS.getName(), status);
			fields.put(FieldType.RESPONSE_MESSAGE.getName(), errorType.getResponseMessage().replaceAll("_", ""));
			fields.put(FieldType.RESPONSE_CODE.getName(), errorType.getResponseCode());
			fields.put(FieldType.PG_DATE_TIME.getName(), transactionResponse.getDateTime());

		} catch (Exception e) {
			logger.error("Unknown Exception :" ,e);
		}

	}

	
}
