package com.paymentgateway.icici.composite.api;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.StatusType;
import com.paymentgateway.commons.util.TransactionManager;

@Service
public class IciciApiTransformer {

	private Transaction transaction = null;

	public IciciApiTransformer(Transaction transaction) {
		this.transaction = transaction;
	}

	public void updateResponse(Fields fields) {

		String status = null;
		ErrorType errorType = null;
		String pgTxnMsg = null;

		if ((StringUtils.isNotBlank(transaction.getActCode())) && ((transaction.getActCode()).equalsIgnoreCase("0"))) {
			status = StatusType.CAPTURED.getName();
			errorType = ErrorType.SUCCESS;
			pgTxnMsg = ErrorType.SUCCESS.getResponseMessage();
		} else {
			if (StringUtils.isNotBlank(transaction.getActCode())) {
				IciciResultType resultInstance = IciciResultType.getInstanceFromName(transaction.getActCode());

				if (resultInstance != null) {
					status = resultInstance.getStatusCode();
					errorType = ErrorType.getInstanceFromCode(resultInstance.getPaymentGatewayCode());
					pgTxnMsg = resultInstance.getMessage();
				} else {
					status = StatusType.DECLINED.getName();
					errorType = ErrorType.getInstanceFromCode("004");
					pgTxnMsg = "Transaction Declined by bank";
				}

			} else {
				status = StatusType.REJECTED.getName();
				errorType = ErrorType.REJECTED;
				pgTxnMsg = ErrorType.REJECTED.getResponseMessage();

			}
		}

		fields.put(FieldType.STATUS.getName(), status);
		fields.put(FieldType.RESPONSE_MESSAGE.getName(), errorType.getResponseMessage());
		fields.put(FieldType.RESPONSE_CODE.getName(), errorType.getResponseCode());

		fields.put(FieldType.RRN.getName(), transaction.getRrn());
		fields.put(FieldType.PG_RESP_CODE.getName(), transaction.getActCode());

		if (StringUtils.isNotBlank(transaction.getResponse()))
			fields.put(FieldType.PG_TXN_MESSAGE.getName(), transaction.getResponse());
		else
			fields.put(FieldType.PG_TXN_MESSAGE.getName(), status);

		if (StringUtils.isNotBlank(transaction.getBeneName()))
			fields.put(FieldType.BENE_NAME.getName(), transaction.getBeneName());

	}

	public void updateBeneResponse(Fields fields, String response) {

		fields.remove(FieldType.REQUEST_TYPE.getName());

		JSONObject responseJson = new JSONObject(response);
		if (responseJson.has("Response")) {

			if (responseJson.getString("Response").equalsIgnoreCase("Success")) {
				fields.put(FieldType.STATUS.getName(), ErrorType.SUCCESS.name());
				if (responseJson.has("Message"))
					fields.put(FieldType.RESPONSE_MESSAGE.getName(), responseJson.getString("Message"));
				fields.put(FieldType.PG_RESP_CODE.getName(), ErrorType.SUCCESS.getCode());
				if (responseJson.has("BNF_ID"))
					fields.put(FieldType.ACQ_ID.getName(), String.valueOf(responseJson.get("BNF_ID")));
			} else if (responseJson.getString("Response").equalsIgnoreCase("failure")) {

				if (Boolean.valueOf(fields.get(FieldType.BENE_DEFAULT.getName()))) {
					fields.put(FieldType.BENE_DEFAULT.getName(), "false");
				}
				fields.put(FieldType.STATUS.getName(), StatusType.FAILED.getName());
				if (responseJson.has("Message"))
					fields.put(FieldType.RESPONSE_MESSAGE.getName(), responseJson.getString("Message"));
				if (responseJson.has("ErrorCode"))
					fields.put(FieldType.PG_RESP_CODE.getName(), String.valueOf(responseJson.get("ErrorCode")));

			}
		} else {
			if (Boolean.valueOf(fields.get(FieldType.BENE_DEFAULT.getName()))) {
				fields.put(FieldType.BENE_DEFAULT.getName(), "false");
			}
			if (responseJson.has("response")) {
				IciciResultType resultInstance = IciciResultType
						.getInstanceFromName(String.valueOf(responseJson.getInt("response")));
				fields.put(FieldType.STATUS.getName(), resultInstance.getStatusCode());
				fields.put(FieldType.RESPONSE_MESSAGE.getName(), resultInstance.getMessage());
				fields.put(FieldType.PG_RESP_CODE.getName(), resultInstance.getPaymentGatewayCode());
			}
		}
	}

	public void updateRegResponse(Fields fields, String response) {

		fields.remove(FieldType.REQUEST_TYPE.getName());

		JSONObject responseJson = new JSONObject(response);
		if (responseJson.has("Response")) {

			if (responseJson.getString("Response").equalsIgnoreCase("Success")) {
				fields.put(FieldType.STATUS.getName(), ErrorType.SUCCESS.name());
				if (responseJson.has("Status"))
					fields.put(FieldType.RESPONSE_MESSAGE.getName(), responseJson.getString("Status"));
				fields.put(FieldType.PG_RESP_CODE.getName(), ErrorType.SUCCESS.getCode());
			} else if (responseJson.getString("Response").equalsIgnoreCase("failure")) {
				fields.put(FieldType.STATUS.getName(), StatusType.FAILED.getName());
				if (responseJson.has("Status"))
					fields.put(FieldType.RESPONSE_MESSAGE.getName(), responseJson.getString("Status"));
				if (responseJson.has("ResponseCode"))
					fields.put(FieldType.PG_RESP_CODE.getName(), String.valueOf(responseJson.get("ResponseCode")));

			}
		} else {

			if (responseJson.has("response")) {
				IciciResultType resultInstance = IciciResultType
						.getInstanceFromName(String.valueOf(responseJson.getInt("response")));
				fields.put(FieldType.STATUS.getName(), resultInstance.getStatusCode());
				fields.put(FieldType.RESPONSE_MESSAGE.getName(), resultInstance.getMessage());
				fields.put(FieldType.PG_RESP_CODE.getName(), resultInstance.getPaymentGatewayCode());
			}
		}

	}

	public void updateRegStatusResponse(Fields fields, String response) {

		fields.remove(FieldType.REQUEST_TYPE.getName());

		if (StringUtils.isNotBlank(response)) {

			JSONObject responseJson = new JSONObject(response);

			if (responseJson.has("Response")) {

				if (responseJson.getString("Response").equalsIgnoreCase("Success")) {
					fields.put(FieldType.STATUS.getName(), ErrorType.SUCCESS.name());
					if (responseJson.has("Status"))
						fields.put(FieldType.RESPONSE_MESSAGE.getName(), responseJson.getString("Status"));
					fields.put(FieldType.PG_RESP_CODE.getName(), ErrorType.SUCCESS.getCode());

				} else if (responseJson.getString("Response").equalsIgnoreCase("failure")) {
					fields.put(FieldType.STATUS.getName(), StatusType.FAILED.getName());
					if (responseJson.has("Status"))
						fields.put(FieldType.RESPONSE_MESSAGE.getName(), responseJson.getString("Status"));
					if (responseJson.has("ResponseCode"))
						fields.put(FieldType.PG_RESP_CODE.getName(), String.valueOf(responseJson.get("ResponseCode")));

				}
			} else {
				if (responseJson.has("response")) {
					IciciResultType resultInstance = IciciResultType
							.getInstanceFromName(String.valueOf(responseJson.getInt("response")));
					fields.put(FieldType.STATUS.getName(), resultInstance.getStatusCode());
					fields.put(FieldType.RESPONSE_MESSAGE.getName(), resultInstance.getMessage());
					fields.put(FieldType.PG_RESP_CODE.getName(), resultInstance.getPaymentGatewayCode());
				}
			}
		}
	}

	public void updateBeneStatusResponse(Fields fields, String response) {

		fields.remove(FieldType.REQUEST_TYPE.getName());

		JSONObject responseJson = new JSONObject(response);
		if (responseJson.has("Response")) {

			if (responseJson.getString("Response").equalsIgnoreCase("Success")) {
				fields.put(FieldType.STATUS.getName(), ErrorType.SUCCESS.name());
				fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.SUCCESS.getResponseMessage());
				if (responseJson.has("BNF_ID"))
					fields.put(FieldType.ACQ_ID.getName(), String.valueOf(responseJson.get("BNF_ID")));
			} else if (responseJson.getString("Response").equalsIgnoreCase("failure")) {
				fields.put(FieldType.STATUS.getName(), StatusType.FAILED.getName());
				if (responseJson.has("Message"))
					fields.put(FieldType.RESPONSE_MESSAGE.getName(), responseJson.getString("Message"));
				if (responseJson.has("ErrorCode"))
					fields.put(FieldType.ACQ_ID.getName(), String.valueOf(responseJson.get("ErrorCode")));
			}
		} else {
			if (responseJson.has("response")) {
				IciciResultType resultInstance = IciciResultType
						.getInstanceFromName(String.valueOf(responseJson.getInt("response")));
				fields.put(FieldType.STATUS.getName(), resultInstance.getStatusCode());
				fields.put(FieldType.RESPONSE_MESSAGE.getName(), resultInstance.getMessage());
				fields.put(FieldType.PG_RESP_CODE.getName(), resultInstance.getPaymentGatewayCode());
			}
		}
	}

	public void updateTransactionResponse(Fields fields, String response) throws JSONException, SystemException {

		fields.remove(FieldType.REQUEST_TYPE.getName());
		if (StringUtils.isNotBlank(response)) {
			JSONObject responseJson = new JSONObject(response);

			fields.put(FieldType.TRANSACTION_OF.getName(), FieldType.CIB_TRANSACTION.getName());

			if (responseJson.has("RESPONSE")) {

				if (responseJson.getString("RESPONSE").equalsIgnoreCase("Success")) {

					if (responseJson.getString("STATUS").equalsIgnoreCase("SUCCESS")) {

						fields.put(FieldType.STATUS.getName(), StatusType.CAPTURED.getName());
						if (responseJson.has("UNIQUEID"))
							fields.put(FieldType.PG_REF_NUM.getName(), String.valueOf(responseJson.get("UNIQUEID")));
						if (responseJson.has("STATUS"))
							fields.put(FieldType.PG_TXN_STATUS.getName(), responseJson.getString("STATUS"));
						fields.put(FieldType.PG_TXN_MESSAGE.getName(), Constants.TRANSACTION_SUCCESS);
						if (responseJson.has("REQID"))
							fields.put(FieldType.REQID.getName(), (String) responseJson.get("REQID"));
						if (responseJson.has("UTRNUMBER"))
							fields.put(FieldType.UTR_NO.getName(), (String) responseJson.get("UTRNUMBER"));

					} else if (responseJson.getString("STATUS").equalsIgnoreCase("FAILURE")) {

						fields.put(FieldType.STATUS.getName(), StatusType.FAILED.getName());
						if (responseJson.has("UNIQUEID"))
							fields.put(FieldType.PG_REF_NUM.getName(), String.valueOf(responseJson.get("UNIQUEID")));
						if (responseJson.has("STATUS"))
							fields.put(FieldType.PG_TXN_STATUS.getName(), responseJson.getString("STATUS"));
						fields.put(FieldType.PG_TXN_MESSAGE.getName(), Constants.TRANSACTION_FAILED);
						if (responseJson.has("REQID"))
							fields.put(FieldType.REQID.getName(), (String) responseJson.get("REQID"));
						if (responseJson.has("UTRNUMBER"))
							fields.put(FieldType.UTR_NO.getName(), (String) responseJson.get("UTRNUMBER"));

					} else if (responseJson.getString("STATUS").equalsIgnoreCase("PENDING")) {

						fields.put(FieldType.STATUS.getName(), StatusType.PROCESSING.getName());
						if (responseJson.has("UNIQUEID"))
							fields.put(FieldType.PG_REF_NUM.getName(), String.valueOf(responseJson.get("UNIQUEID")));
						if (responseJson.has("STATUS"))
							fields.put(FieldType.PG_TXN_STATUS.getName(), responseJson.getString("STATUS"));
						fields.put(FieldType.PG_TXN_MESSAGE.getName(), Constants.TRANSACTION_PENDING);
						if (responseJson.has("REQID"))
							fields.put(FieldType.REQID.getName(), (String) responseJson.get("REQID"));
						if (responseJson.has("UTRNUMBER"))
							fields.put(FieldType.UTR_NO.getName(), (String) responseJson.get("UTRNUMBER"));

					} else if (responseJson.getString("STATUS").equalsIgnoreCase("Pending For Processing")) {

						fields.put(FieldType.STATUS.getName(), StatusType.PROCESSING.getName());
						if (responseJson.has("UNIQUEID"))
							fields.put(FieldType.PG_REF_NUM.getName(), String.valueOf(responseJson.get("UNIQUEID")));
						if (responseJson.has("STATUS"))
							fields.put(FieldType.PG_TXN_STATUS.getName(), responseJson.getString("STATUS"));
						fields.put(FieldType.PG_TXN_MESSAGE.getName(), Constants.TRANSACTION_PROCESSING);
						if (responseJson.has("REQID"))
							fields.put(FieldType.REQID.getName(), (String) responseJson.get("REQID"));
						if (responseJson.has("UTRNUMBER"))
							fields.put(FieldType.UTR_NO.getName(), (String) responseJson.get("UTRNUMBER"));

					} else if (responseJson.getString("STATUS").equalsIgnoreCase("DUPLICATE")) {

						fields.put(FieldType.STATUS.getName(), StatusType.DUPLICATE.getName());
						if (responseJson.has("UNIQUEID"))
							fields.put(FieldType.PG_REF_NUM.getName(), String.valueOf(responseJson.get("UNIQUEID")));
						fields.put(FieldType.PG_TXN_MESSAGE.getName(), Constants.TRANSACTION_DUPICATE);
						if (responseJson.has("STATUS"))
							fields.put(FieldType.PG_TXN_STATUS.getName(), responseJson.getString("STATUS"));
						if (responseJson.has("REQID"))
							fields.put(FieldType.REQID.getName(), (String) responseJson.get("REQID"));
						if (responseJson.has("UTRNUMBER"))
							fields.put(FieldType.UTR_NO.getName(), (String) responseJson.get("UTRNUMBER"));
					}

				} else if (responseJson.getString("RESPONSE").equalsIgnoreCase("failure")) {

					fields.put(FieldType.STATUS.getName(), StatusType.FAILED.getName());
					if (responseJson.has("UTRNUMBER"))
						fields.put(FieldType.UTR_NO.getName(), responseJson.getString("UTRNUMBER"));
					if (responseJson.has("Message"))
						fields.put(FieldType.PG_TXN_MESSAGE.getName(), responseJson.getString("Message"));
					if (responseJson.has("STATUS"))
						fields.put(FieldType.PG_TXN_STATUS.getName(), responseJson.getString("STATUS"));
					if (responseJson.has("RESPONSECODE"))
						fields.put(FieldType.PG_RESP_CODE.getName(), String.valueOf(responseJson.get("RESPONSECODE")));
					if (responseJson.has("ERRORCODE"))
						fields.put(FieldType.PG_ERROR_CODE.getName(), String.valueOf(responseJson.get("ERRORCODE")));
					
					fields.put(FieldType.PG_REF_NUM.getName(), fields.get(FieldType.TXN_ID.getName()));

				}

			} else {
				if (responseJson.has("response")) {
					IciciResultType resultInstance = IciciResultType
							.getInstanceFromName(String.valueOf(responseJson.getInt("response")));
					fields.put(FieldType.STATUS.getName(), resultInstance.getStatusCode());
					fields.put(FieldType.PG_TXN_MESSAGE.getName(), resultInstance.getMessage());
					fields.put(FieldType.PG_RESP_CODE.getName(), resultInstance.getPaymentGatewayCode());
					fields.put(FieldType.PG_REF_NUM.getName(), fields.get(FieldType.TXN_ID.getName()));
				}
			}
		}else{
			fields.put(FieldType.STATUS.getName(),  StatusType.FAILED.getName());
			fields.put(FieldType.PG_TXN_MESSAGE.getName(), ErrorType.FAILED.getResponseMessage());
			fields.put(FieldType.PG_RESP_CODE.getName(), ErrorType.FAILED.getResponseCode());
			fields.put(FieldType.PG_REF_NUM.getName(), fields.get(FieldType.TXN_ID.getName()));
		}
	}

	public void updateTransactionStatusResponse(Fields fields, String response) throws JSONException, SystemException {
		//fields.put(FieldType.TXN_ID.getName(), TransactionManager.getNewTransactionId());
		fields.remove(FieldType.REQUEST_TYPE.getName());

		if(StringUtils.isNotBlank(response)){
		
		JSONObject responseJson = new JSONObject(response);

		fields.put(FieldType.TRANSACTION_OF.getName(), FieldType.CIB_TRANSACTION.getName());

		if (responseJson.has("RESPONSE")) {

			if (responseJson.getString("RESPONSE").equalsIgnoreCase("Success")) {

				if (responseJson.getString("STATUS").equalsIgnoreCase("SUCCESS")) {

					fields.put(FieldType.STATUS.getName(), StatusType.CAPTURED.getName());
					if (responseJson.has("UNIQUEID"))
						fields.put(FieldType.PG_REF_NUM.getName(), String.valueOf(responseJson.get("UNIQUEID")));
					if (responseJson.has("STATUS"))
						fields.put(FieldType.PG_TXN_STATUS.getName(), responseJson.getString("STATUS"));
					fields.put(FieldType.PG_TXN_MESSAGE.getName(), Constants.TRANSACTION_SUCCESS);
					if (responseJson.has("REQID"))
						fields.put(FieldType.REQID.getName(), (String) responseJson.get("REQID"));
					if (responseJson.has("UTRNUMBER"))
						fields.put(FieldType.UTR_NO.getName(), (String) responseJson.get("UTRNUMBER"));

				} else if (responseJson.getString("STATUS").equalsIgnoreCase("FAILURE")) {

					fields.put(FieldType.STATUS.getName(), StatusType.FAILED.getName());
					if (responseJson.has("UNIQUEID"))
						fields.put(FieldType.PG_REF_NUM.getName(), String.valueOf(responseJson.get("UNIQUEID")));
					if (responseJson.has("STATUS"))
						fields.put(FieldType.PG_TXN_STATUS.getName(), responseJson.getString("STATUS"));
					fields.put(FieldType.PG_TXN_MESSAGE.getName(), Constants.TRANSACTION_FAILED);
					if (responseJson.has("REQID"))
						fields.put(FieldType.REQID.getName(), (String) responseJson.get("REQID"));
					if (responseJson.has("UTRNUMBER"))
						fields.put(FieldType.UTR_NO.getName(), (String) responseJson.get("UTRNUMBER"));

				} else if (responseJson.getString("STATUS").equalsIgnoreCase("PENDING")) {

					fields.put(FieldType.STATUS.getName(), StatusType.PROCESSING.getName());
					if (responseJson.has("UNIQUEID"))
						fields.put(FieldType.PG_REF_NUM.getName(), String.valueOf(responseJson.get("UNIQUEID")));
					if (responseJson.has("STATUS"))
						fields.put(FieldType.PG_TXN_STATUS.getName(), responseJson.getString("STATUS"));
					fields.put(FieldType.PG_TXN_MESSAGE.getName(), Constants.TRANSACTION_PENDING);
					if (responseJson.has("REQID"))
						fields.put(FieldType.REQID.getName(), (String) responseJson.get("REQID"));
					if (responseJson.has("UTRNUMBER"))
						fields.put(FieldType.UTR_NO.getName(), (String) responseJson.get("UTRNUMBER"));

				} else if (responseJson.getString("STATUS").equalsIgnoreCase("Pending For Processing")) {

					fields.put(FieldType.STATUS.getName(), StatusType.PROCESSING.getName());
					if (responseJson.has("UNIQUEID"))
						fields.put(FieldType.PG_REF_NUM.getName(), String.valueOf(responseJson.get("UNIQUEID")));
					if (responseJson.has("STATUS"))
						fields.put(FieldType.PG_TXN_STATUS.getName(), responseJson.getString("STATUS"));
					fields.put(FieldType.PG_TXN_MESSAGE.getName(), Constants.TRANSACTION_PROCESSING);
					if (responseJson.has("REQID"))
						fields.put(FieldType.REQID.getName(), (String) responseJson.get("REQID"));
					if (responseJson.has("UTRNUMBER"))
						fields.put(FieldType.UTR_NO.getName(), (String) responseJson.get("UTRNUMBER"));

				} else if (responseJson.getString("STATUS").equalsIgnoreCase("DUPLICATE")) {

					fields.put(FieldType.STATUS.getName(), StatusType.DUPLICATE.getName());
					if (responseJson.has("UNIQUEID"))
						fields.put(FieldType.PG_REF_NUM.getName(), String.valueOf(responseJson.get("UNIQUEID")));
					fields.put(FieldType.PG_TXN_MESSAGE.getName(), Constants.TRANSACTION_DUPICATE);
					if (responseJson.has("STATUS"))
						fields.put(FieldType.PG_TXN_STATUS.getName(), responseJson.getString("STATUS"));
					if (responseJson.has("REQID"))
						fields.put(FieldType.REQID.getName(), (String) responseJson.get("REQID"));
					if (responseJson.has("UTRNUMBER"))
						fields.put(FieldType.UTR_NO.getName(), (String) responseJson.get("UTRNUMBER"));
				}

			} else if (responseJson.getString("RESPONSE").equalsIgnoreCase("failure")) {

				fields.put(FieldType.STATUS.getName(), StatusType.FAILED.getName());
				if (responseJson.has("UTRNUMBER"))
					fields.put(FieldType.UTR_NO.getName(), responseJson.getString("UTRNUMBER"));
				if (responseJson.has("Message"))
					fields.put(FieldType.PG_TXN_MESSAGE.getName(), responseJson.getString("Message"));
				if (responseJson.has("STATUS"))
					fields.put(FieldType.PG_TXN_STATUS.getName(), responseJson.getString("STATUS"));
				if (responseJson.has("RESPONSECODE"))
					fields.put(FieldType.PG_RESP_CODE.getName(), String.valueOf(responseJson.get("RESPONSECODE")));
				if (responseJson.has("ERRORCODE"))
					fields.put(FieldType.PG_ERROR_CODE.getName(), String.valueOf(responseJson.get("ERRORCODE")));

			}

		} else {
			if (responseJson.has("response")) {

				IciciResultType resultInstance = IciciResultType
						.getInstanceFromName(String.valueOf(responseJson.getInt("response")));
				fields.put(FieldType.STATUS.getName(), resultInstance.getStatusCode());
				fields.put(FieldType.PG_TXN_MESSAGE.getName(), resultInstance.getMessage());
				fields.put(FieldType.PG_RESP_CODE.getName(), resultInstance.getPaymentGatewayCode());
			}
		}
		}else{
			fields.put(FieldType.STATUS.getName(),  StatusType.FAILED.getName());
			fields.put(FieldType.PG_TXN_MESSAGE.getName(), ErrorType.FAILED.getResponseMessage());
			fields.put(FieldType.PG_RESP_CODE.getName(), ErrorType.FAILED.getResponseCode());
		}
	}

	public void updateBalanceInqResponse(Fields fields, String response) {

		fields.remove(FieldType.REQUEST_TYPE.getName());

		JSONObject responseJson = new JSONObject(response);
		if (responseJson.has("RESPONSE")) {

			if (responseJson.getString("RESPONSE").equalsIgnoreCase("SUCCESS")) {
				fields.put(FieldType.STATUS.getName(), ErrorType.SUCCESS.name());
				if (responseJson.has("DATE"))
					fields.put(FieldType.UPDATE_DATE.getName(), String.valueOf(responseJson.get("DATE")));
				if (responseJson.has("EFFECTIVEBAL"))
					fields.put(FieldType.AMOUNT.getName(), String.valueOf(responseJson.get("EFFECTIVEBAL")));
				if (responseJson.has("CURRENCY"))
					fields.put(FieldType.CURRENCY.getName(), responseJson.getString("CURRENCY"));
			} else if (responseJson.getString("RESPONSE").equalsIgnoreCase("FAILURE")) {
				fields.put(FieldType.STATUS.getName(), ErrorType.FAILED.name());
				if (responseJson.has("DATE"))
					fields.put(FieldType.UPDATE_DATE.getName(), String.valueOf(responseJson.get("DATE")));
				if (responseJson.has("EFFECTIVEBAL"))
					fields.put(FieldType.AMOUNT.getName(), String.valueOf(responseJson.get("EFFECTIVEBAL")));
				if (responseJson.has("CURRENCY"))
					fields.put(FieldType.CURRENCY.getName(), responseJson.getString("CURRENCY"));
				if (responseJson.has("MESSAGE"))
					fields.put(FieldType.RESPONSE_MESSAGE.getName(), responseJson.getString("MESSAGE"));

			}
		} else {
			if (responseJson.has("response")) {
				IciciResultType resultInstance = IciciResultType
						.getInstanceFromName(String.valueOf(responseJson.getInt("response")));
				fields.put(FieldType.STATUS.getName(), resultInstance.getStatusCode());
				fields.put(FieldType.RESPONSE_MESSAGE.getName(), resultInstance.getMessage());
				fields.put(FieldType.PG_RESP_CODE.getName(), resultInstance.getPaymentGatewayCode());
			}

		}
	}

	public void updateAccountStatementResponse(Fields fields, String response) {

		fields.remove(FieldType.REQUEST_TYPE.getName());

		if (StringUtils.isNotBlank(response)) {
			JSONObject responseJson = new JSONObject(response);

			if (responseJson.has("RESPONSE")) {

				if (responseJson.getString("RESPONSE").equalsIgnoreCase("Success")) {

					JSONObject records = new JSONObject();
					if (responseJson.has("Record")) {
						JSONArray responseRecord = new JSONArray(responseJson.get("Record").toString());

						for (int i = 0; i < responseRecord.length(); i++) {
							JSONObject jsonObject = new JSONObject(responseRecord.get(i).toString());
							records.put(String.valueOf(i), jsonObject.toString());
						}
					}

					fields.put("records", records.toString());
				}

			} else {
				if (responseJson.has("response")) {
					IciciResultType resultInstance = IciciResultType
							.getInstanceFromName(String.valueOf(responseJson.getInt("response")));
					fields.put(FieldType.STATUS.getName(), resultInstance.getStatusCode());
					fields.put(FieldType.RESPONSE_MESSAGE.getName(), resultInstance.getMessage());
					fields.put(FieldType.PG_RESP_CODE.getName(), resultInstance.getPaymentGatewayCode());
				}
			}
		}
	}

	public void updateIMPSCompositeTransactionResponse(Fields fields) throws JSONException, SystemException {

		fields.remove(FieldType.HASH.getName());

		String status = null;
		ErrorType errorType = null;
		String pgTxnMsg = null;
		if ((StringUtils.isNotBlank(transaction.getActCode())) && ((transaction.getActCode()).equalsIgnoreCase("0"))) {
			status = StatusType.CAPTURED.getName();
			errorType = ErrorType.SUCCESS;
			pgTxnMsg = ErrorType.SUCCESS.getResponseMessage();
		} else {
			if (StringUtils.isNotBlank(transaction.getActCode())) {
				IciciCompositeImpsResultType resultInstance = IciciCompositeImpsResultType
						.getInstanceFromName(transaction.getActCode());

				if (resultInstance != null) {
					status = resultInstance.getStatusCode();
					errorType = ErrorType.getInstanceFromCode(resultInstance.getPaymentGatewayCode());
					pgTxnMsg = resultInstance.getMessage();
				} else {
					status = StatusType.DECLINED.getName();
					errorType = ErrorType.getInstanceFromCode("004");
					pgTxnMsg = "Transaction Declined by bank";
				}

			} else {
				status = StatusType.REJECTED.getName();
				errorType = ErrorType.REJECTED;
				pgTxnMsg = ErrorType.REJECTED.getResponseMessage();
			}
		}
		fields.put(FieldType.PG_REF_NUM.getName(), fields.get(FieldType.TXN_ID.getName()));
		fields.put(FieldType.TXNTYPE.getName(), "IMPS");
		fields.put(FieldType.STATUS.getName(), status);
		fields.put(FieldType.RESPONSE_MESSAGE.getName(), errorType.getResponseMessage());
		fields.put(FieldType.RESPONSE_CODE.getName(), errorType.getResponseCode());

		if (StringUtils.isNotBlank(String.valueOf(transaction.isSuccess()))) {
			fields.put(FieldType.PG_TXN_STATUS.getName(), String.valueOf(transaction.isSuccess()));
		}
		if (StringUtils.isNotBlank(transaction.getRrn())) {
			fields.put(FieldType.RRN.getName(), transaction.getRrn());
			fields.put(FieldType.ACQ_ID.getName(), transaction.getRrn());
			fields.put(FieldType.UTR_NO.getName(), transaction.getRrn());
		}

		if (StringUtils.isNotBlank(transaction.getActCode())) {
			fields.put(FieldType.PG_RESP_CODE.getName(), transaction.getActCode());
		} else {
			fields.put(FieldType.PG_RESP_CODE.getName(), errorType.getResponseCode());
		}

		if (StringUtils.isNotBlank(transaction.getResponse())) {
			fields.put(FieldType.PG_TXN_MESSAGE.getName(), transaction.getResponse());
		} else {
			fields.put(FieldType.PG_TXN_MESSAGE.getName(), status);
		}

		if (StringUtils.isNotBlank(transaction.getBeneName())) {
			fields.put(FieldType.BENE_NAME.getName(), transaction.getBeneName());
		}

		if (StringUtils.isNotBlank(transaction.getTranRefNo())) {
			fields.put(FieldType.BANK_REF_NUM.getName(), transaction.getTranRefNo());
		}

	}

	public void updateUPICompositeTransactionResponse(Fields fields) throws JSONException, SystemException {

		String status = null;
		ErrorType errorType = null;
		String pgTxnMsg = null;
		if ((StringUtils.isNotBlank(transaction.getResponse()))
				&& ((transaction.getResponse()).equalsIgnoreCase("0"))) {
			status = StatusType.CAPTURED.getName();
			errorType = ErrorType.SUCCESS;
			pgTxnMsg = ErrorType.SUCCESS.getResponseMessage();
		} else {
			if (StringUtils.isNotBlank(transaction.getResponse())) {
				IciciCompositeUpiResultType resultInstance = IciciCompositeUpiResultType
						.getInstanceFromName(transaction.getActCode());

				if (resultInstance != null) {
					status = resultInstance.getStatusCode();
					errorType = ErrorType.getInstanceFromCode(resultInstance.getPaymentGatewayCode());
					pgTxnMsg = resultInstance.getMessage();
				} else {
					status = StatusType.DECLINED.getName();
					errorType = ErrorType.getInstanceFromCode("004");
					pgTxnMsg = "Transaction Declined by bank";
				}

			} else {
				status = StatusType.REJECTED.getName();
				errorType = ErrorType.REJECTED;
				pgTxnMsg = ErrorType.REJECTED.getResponseMessage();

			}
		}

		fields.put(FieldType.PG_REF_NUM.getName(), fields.get(FieldType.TXN_ID.getName()));
		fields.put(FieldType.TXNTYPE.getName(), "UPI");
		fields.put(FieldType.STATUS.getName(), status);
		fields.put(FieldType.RESPONSE_MESSAGE.getName(), errorType.getResponseMessage());
		fields.put(FieldType.RESPONSE_CODE.getName(), errorType.getResponseCode());

		if (StringUtils.isNotBlank(String.valueOf(transaction.isSuccess()))) {
			fields.put(FieldType.PG_TXN_STATUS.getName(), String.valueOf(transaction.isSuccess()));
		}
		if (StringUtils.isNotBlank(transaction.getRrn()))
			fields.put(FieldType.RRN.getName(), transaction.getRrn());

		if (StringUtils.isNotBlank(transaction.getResponse()))
			fields.put(FieldType.PG_RESP_CODE.getName(), transaction.getResponse());
		else
			fields.put(FieldType.PG_RESP_CODE.getName(), errorType.getResponseCode());

		if (StringUtils.isNotBlank(transaction.getMessage()))
			fields.put(FieldType.PG_TXN_MESSAGE.getName(), transaction.getMessage());
		else
			fields.put(FieldType.PG_TXN_MESSAGE.getName(), status);

		if (StringUtils.isNotBlank(transaction.getTranLogId()))
			fields.put(FieldType.ACQ_ID.getName(), transaction.getTranLogId());

		if (StringUtils.isNotBlank(transaction.getSeqNo()))
			fields.put(FieldType.BANK_REF_NUM.getName(), transaction.getSeqNo());

	}
	
	public void updateUPICompositeTransactionStatusEnqResponse(Fields fields) throws JSONException, SystemException {

		String status = null;
		ErrorType errorType = null;
		String pgTxnMsg = null;
		
		if((StringUtils.isNotBlank(transaction.getMobileAppData())))
		{
			JSONObject mobileData= new JSONObject(transaction.getMobileAppData());
			
			transaction.setResponse(String.valueOf(mobileData.get("original-txn-response-code")));
			transaction.setMessage(String.valueOf(mobileData.get("original-txn-message")));
		}
		
		if ((StringUtils.isNotBlank(transaction.getResponse()))
				&& ((transaction.getResponse()).equalsIgnoreCase("0"))) {
			status = StatusType.CAPTURED.getName();
			errorType = ErrorType.SUCCESS;
			pgTxnMsg = ErrorType.SUCCESS.getResponseMessage();
		} else {
			if (StringUtils.isNotBlank(transaction.getResponse())) {
				IciciCompositeUpiResultType resultInstance = IciciCompositeUpiResultType
						.getInstanceFromName(transaction.getActCode());

				if (resultInstance != null) {
					status = resultInstance.getStatusCode();
					errorType = ErrorType.getInstanceFromCode(resultInstance.getPaymentGatewayCode());
					pgTxnMsg = resultInstance.getMessage();
				} else {
					status = StatusType.DECLINED.getName();
					errorType = ErrorType.getInstanceFromCode("004");
					pgTxnMsg = "Transaction Declined by bank";
				}

			} else {
				status = StatusType.REJECTED.getName();
				errorType = ErrorType.REJECTED;
				pgTxnMsg = ErrorType.REJECTED.getResponseMessage();

			}
		}

		fields.put(FieldType.PG_REF_NUM.getName(), fields.get(FieldType.TXN_ID.getName()));
		fields.put(FieldType.TXNTYPE.getName(), "UPI");
		fields.put(FieldType.STATUS.getName(), status);
		fields.put(FieldType.RESPONSE_MESSAGE.getName(), errorType.getResponseMessage());
		fields.put(FieldType.RESPONSE_CODE.getName(), errorType.getResponseCode());

		if (StringUtils.isNotBlank(String.valueOf(transaction.isSuccess()))) {
			fields.put(FieldType.PG_TXN_STATUS.getName(), String.valueOf(transaction.isSuccess()));
		}
		if (StringUtils.isNotBlank(transaction.getRrn()))
			fields.put(FieldType.RRN.getName(), transaction.getRrn());

		if (StringUtils.isNotBlank(transaction.getResponse()))
			fields.put(FieldType.PG_RESP_CODE.getName(), transaction.getResponse());
		else
			fields.put(FieldType.PG_RESP_CODE.getName(), errorType.getResponseCode());

		if (StringUtils.isNotBlank(transaction.getMessage()))
			fields.put(FieldType.PG_TXN_MESSAGE.getName(), transaction.getMessage());
		else
			fields.put(FieldType.PG_TXN_MESSAGE.getName(), status);

		if (StringUtils.isNotBlank(transaction.getTranLogId()))
			fields.put(FieldType.ACQ_ID.getName(), transaction.getTranLogId());

		if (StringUtils.isNotBlank(transaction.getSeqNo()))
			fields.put(FieldType.BANK_REF_NUM.getName(), transaction.getSeqNo());

	}

	public Map<String, String> updateBeneAdditionTransactionResponse(Fields fields)
			throws JSONException, SystemException {

		fields.remove(FieldType.HASH.getName());

		Map<String, String> responseMap = new HashMap<String, String>();

		String status = null;
		ErrorType errorType = null;
		String pgTxnMsg = null;
		if ((StringUtils.isNotBlank(transaction.getResponse()))
				&& ((transaction.getResponse()).equalsIgnoreCase("SUCCESS"))) {
			status = ErrorType.SUCCESS.name();
			errorType = ErrorType.SUCCESS;
			pgTxnMsg = ErrorType.SUCCESS.getResponseMessage();
		} else {
			if (StringUtils.isNotBlank(transaction.getResponse())) {
				if (StringUtils.isNotBlank(fields.get(FieldType.BENE_ACCOUNT_NO.getName()))) {
					IciciCompositeImpsResultType resultInstance = IciciCompositeImpsResultType
							.getInstanceFromName(transaction.getErrorCode());

					if (resultInstance != null) {

						status = StatusType.FAILED.getName();
						errorType = ErrorType.getInstanceFromCode(resultInstance.getPaymentGatewayCode());
						pgTxnMsg = resultInstance.getMessage();

					} else {
						status = StatusType.FAILED.getName();
						errorType = ErrorType.getInstanceFromCode("022");
						pgTxnMsg = StatusType.FAILED.getName();
					}

				} else {
					IciciCompositeUpiResultType resultInstance = IciciCompositeUpiResultType
							.getInstanceFromName(transaction.getErrorCode());

					if (resultInstance != null) {

						status = StatusType.FAILED.getName();
						errorType = ErrorType.getInstanceFromCode(resultInstance.getPaymentGatewayCode());
						pgTxnMsg = resultInstance.getMessage();

					} else {
						status = StatusType.FAILED.getName();
						errorType = ErrorType.getInstanceFromCode("022");
						pgTxnMsg = StatusType.FAILED.getName();
					}

				}

			} else {
				status = StatusType.FAILED.getName();
				errorType = ErrorType.FAILED;
				pgTxnMsg = ErrorType.FAILED.getResponseMessage();
			}
		}

		if ((StringUtils.isNotBlank(transaction.getResponse()))
				&& ((transaction.getResponse()).equalsIgnoreCase("SUCCESS"))) {
			responseMap.put(FieldType.BENE_REGISTRATION.getName(), "Active");
		} else {
			responseMap.put(FieldType.BENE_REGISTRATION.getName(), "inActive");
		}

		if (StringUtils.isNotBlank(fields.get(FieldType.EMAIL.getName()))) {
			responseMap.put(FieldType.EMAIL.getName(), fields.get(FieldType.EMAIL.getName()));
		}

		if (StringUtils.isNotBlank(fields.get(FieldType.RESELLER_ID.getName()))) {
			responseMap.put(FieldType.RESELLER_ID.getName(), fields.get(FieldType.RESELLER_ID.getName()));
		}

		responseMap.put(FieldType.TXN_ID.getName(), fields.get(FieldType.TXN_ID.getName()));
		responseMap.put(FieldType.ORDER_ID.getName(), fields.get(FieldType.ORDER_ID.getName()));
		if (StringUtils.isNotBlank(fields.get(FieldType.BENE_ACCOUNT_NO.getName()))) {
			responseMap.put(FieldType.TXNTYPE.getName(), "IMPS");
			responseMap.put(FieldType.BENE_ACCOUNT_NO.getName(), fields.get(FieldType.BENE_ACCOUNT_NO.getName()));
			responseMap.put(FieldType.IFSC_CODE.getName(), fields.get(FieldType.IFSC_CODE.getName()));
			responseMap.put(FieldType.BENE_NAME.getName(), fields.get(FieldType.BENE_NAME.getName()));
		} else {
			responseMap.put(FieldType.TXNTYPE.getName(), "UPI");
			responseMap.put(FieldType.PAYER_ADDRESS.getName(), fields.get(FieldType.PAYER_ADDRESS.getName()));
		}
		responseMap.put(FieldType.STATUS.getName(), status);
		responseMap.put(FieldType.RESPONSE_MESSAGE.getName(), errorType.getResponseMessage());
		responseMap.put(FieldType.RESPONSE_CODE.getName(), errorType.getResponseCode());

		responseMap.put(FieldType.PG_RESPONSE_MSG.getName(), pgTxnMsg);
		if (StringUtils.isNotBlank(transaction.getErrorCode()))
			responseMap.put(FieldType.PG_RESP_CODE.getName(), transaction.getErrorCode());
		if (StringUtils.isNotBlank(transaction.getResponse()))
			responseMap.put(FieldType.PG_RESPONSE_STATUS.getName(), transaction.getResponse());

		if (StringUtils.isNotBlank(transaction.getBnfId()))
			responseMap.put(FieldType.ACQ_ID.getName(), transaction.getBnfId());

		if (StringUtils.isNotBlank(fields.get(FieldType.PHONE_NO.getName())))
			responseMap.put(FieldType.PHONE_NO.getName(), fields.get(FieldType.PHONE_NO.getName()));

		return responseMap;

	}

	public void updateCompositeAccountStatementResponse(Fields fields, String response) {

		if (StringUtils.isNotBlank(response)) {
			JSONObject responseJson = new JSONObject(response);

			if (responseJson.has(Constants.LASTTRID)) {
				fields.put(Constants.LASTTRID, responseJson.getString(Constants.LASTTRID));
			} else {
				fields.remove(Constants.LASTTRID);
			}

			if (responseJson.has("RESPONSE")) {

				if (responseJson.getString("RESPONSE").equalsIgnoreCase("Success")) {

					JSONObject records = null;

					if (StringUtils.isNotBlank(fields.get("records"))) {
						records = new JSONObject(fields.get("records"));
					} else {
						records = new JSONObject();
					}

					int size = records.length();

					if (responseJson.has("Record")) {

						String responseSubJson = responseJson.get("Record").toString();

						int j = 0;
						if (responseSubJson.startsWith("[")) {
							JSONArray responseRecord = new JSONArray(responseJson.get("Record").toString());
							for (int i = size; i < responseRecord.length() + size; i++) {
								JSONObject jsonObject = new JSONObject(responseRecord.get(j).toString());
								records.put(String.valueOf(i), jsonObject.toString());
								j++;
							}
						} else {
							JSONObject responseRecord = new JSONObject(responseJson.get("Record").toString());
							
							if(size!=0){
								size=size+1;
							}
							records.put(String.valueOf(size), responseRecord.toString());

						}

					}

					fields.put("records", records.toString());
				}

			} else {
				if (responseJson.has("response")) {
					IciciResultType resultInstance = IciciResultType
							.getInstanceFromName(String.valueOf(responseJson.getInt("response")));
					fields.put(FieldType.STATUS.getName(), resultInstance.getStatusCode());
					fields.put(FieldType.RESPONSE_MESSAGE.getName(), resultInstance.getMessage());
					fields.put(FieldType.PG_RESP_CODE.getName(), resultInstance.getPaymentGatewayCode());
				}
			}
		}
	}

	public void updateNEFTRTGSCompositeTransactionResponse(Fields fields) {

		String status = null;
		ErrorType errorType = null;
		String pgTxnMsg = null;
		if ((StringUtils.isNotBlank(transaction.getStatus()))
				&& ((transaction.getStatus()).equalsIgnoreCase("SUCCESS"))) {
			status = StatusType.CAPTURED.getName();
			errorType = ErrorType.SUCCESS;
			pgTxnMsg = ErrorType.SUCCESS.getResponseMessage();
		} else {
			if (StringUtils.isNotBlank(transaction.getErrorCode())) {
				IciciCompositeImpsResultType resultInstance = IciciCompositeImpsResultType
						.getInstanceFromName(transaction.getErrorCode());

				if (resultInstance != null) {
					status = resultInstance.getStatusCode();
					errorType = ErrorType.getInstanceFromCode(resultInstance.getPaymentGatewayCode());
					pgTxnMsg = resultInstance.getMessage();
				} else {
					status = StatusType.DECLINED.getName();
					errorType = ErrorType.getInstanceFromCode("004");
					pgTxnMsg = "Transaction Declined by bank";
				}

			} else {
				status = StatusType.REJECTED.getName();
				errorType = ErrorType.REJECTED;
				pgTxnMsg = ErrorType.REJECTED.getResponseMessage();
			}
		}
		fields.put(FieldType.PG_REF_NUM.getName(), fields.get(FieldType.TXN_ID.getName()));
		fields.put(FieldType.STATUS.getName(), status);
		fields.put(FieldType.RESPONSE_MESSAGE.getName(), errorType.getResponseMessage());
		fields.put(FieldType.RESPONSE_CODE.getName(), errorType.getResponseCode());

		if (StringUtils.isNotBlank(transaction.getUtr())) {
			fields.put(FieldType.RRN.getName(), transaction.getUtr());
			fields.put(FieldType.UTR_NO.getName(), transaction.getUtr());
		}

		if (StringUtils.isNotBlank(transaction.getResponseCode())) {
			fields.put(FieldType.PG_RESP_CODE.getName(), transaction.getResponseCode());
		}

		if (StringUtils.isNotBlank(transaction.getResponse())) {
			fields.put(FieldType.PG_RESPONSE_MSG.getName(), transaction.getResponse());
			fields.put(FieldType.PG_TXN_MESSAGE.getName(), transaction.getResponse());
		} else {
			fields.put(FieldType.PG_RESPONSE_MSG.getName(), status);
		}

		if (StringUtils.isNotBlank(transaction.getMessage())) {
			fields.put(FieldType.PG_TXN_MESSAGE.getName(), transaction.getMessage());
		}

		if (StringUtils.isNotBlank(transaction.getUniqueId())) {
			fields.put(FieldType.BANK_REF_NUM.getName(), transaction.getUniqueId());
		}

		if (StringUtils.isNotBlank(transaction.getReqId())) {
			fields.put(FieldType.REQID.getName(), transaction.getReqId());
		}

	}

	public Transaction getTransaction() {
		return transaction;
	}

	public void setTransaction(Transaction transaction) {
		this.transaction = transaction;
	}

}
