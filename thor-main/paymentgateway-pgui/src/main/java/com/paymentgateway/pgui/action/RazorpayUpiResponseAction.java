package com.paymentgateway.pgui.action;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.api.TransactionControllerServiceProvider;
import com.paymentgateway.commons.dao.FieldsDao;
import com.paymentgateway.commons.dao.UserSettingDao;
import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.user.AccountCurrencyRegion;
import com.paymentgateway.commons.user.CardHolderType;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.user.UserSettingData;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.DateCreater;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.MopType;
import com.paymentgateway.commons.util.PaymentType;
import com.paymentgateway.commons.util.StatusType;
import com.paymentgateway.commons.util.TransactionManager;
import com.paymentgateway.commons.util.TransactionType;
import com.paymentgateway.pg.core.acquirerDoubleVerification.CheckBankResponseUsingStatusEnquiry;
import com.paymentgateway.commons.dao.UserSettingDao;
import com.paymentgateway.commons.kms.EncryptDecryptService;
import com.paymentgateway.commons.user.Account;
import com.paymentgateway.commons.user.AccountCurrency;
import com.paymentgateway.commons.util.AcquirerType;
import com.paymentgateway.commons.util.Amount;
import com.paymentgateway.commons.util.UserStatusType;


/**
 * @author Vishal
 *
 */
@Service
public class RazorpayUpiResponseAction {

	public static Map<String, User> userMap = new HashMap<String, User>();

	@Autowired
	private TransactionControllerServiceProvider transactionControllerServiceProvider;

	@Autowired
	private FieldsDao fieldsDao;

	@Autowired
	private UserDao userDao;

	@Autowired
	UserSettingDao userSettingDao;
	
	@Autowired
	private CheckBankResponseUsingStatusEnquiry checkBankResponseUsingStatusEnquiry;

	@Autowired
	private EncryptDecryptService encryptDecryptService;


	

	private static Logger logger = LoggerFactory.getLogger(RazorpayUpiResponseAction.class.getName());

	
	
	public void razorpayUpiResponseHandler(HttpServletRequest httpRequest, HttpServletResponse httpResponse, String data)
			throws IOException {

		try {
			String status=null,id=null,date=null,rrn=null,payId=null,responseMsg=null;
			JSONObject JsonData = new JSONObject(data);
			logger.info("Razorpay razorpayUpiResponseHandler " + JsonData);
			Fields fields = new Fields();
			if(JsonData!=null && JsonData.has("payload") ) {
			  JSONObject payload = JsonData.getJSONObject("payload");
			  if(payload.has("qr_code") && payload.has("payment")) {
				  JSONObject qr_code = payload.getJSONObject("qr_code");
				  if(qr_code.has("entity")) {
					  JSONObject qr_codeentity = qr_code.getJSONObject("entity");
					  if(qr_codeentity.has("description")) {
						  fields.put(FieldType.PAY_ID.getName(),qr_codeentity.getString("description"));
					  }else {
							logger.info("Razorpay razorpayUpiResponseHandler  description in payid not found" );
							
					  }
						
				  }
				  JSONObject payment = payload.getJSONObject("payment");
				  if(payment.has("entity")) {
					  JSONObject entity = payment.getJSONObject("entity");
					  if(entity.has("status") && entity.has("amount") &&   entity.has("acquirer_data") &&   entity.has("vpa") ) {
						  fields.put(FieldType.AMOUNT.getName(),entity.getInt("amount")+"");
						  status =entity.getString("status");
						  fields.put(FieldType.VPA.getName(),entity.getString("vpa"));
						  date =entity.getInt("created_at")+"";
						  id=entity.getString("id");
						  responseMsg=entity.getString("description");
						  if(entity.has("error_description") &&!entity.isNull("error_description")) {
							  fields.put(FieldType.PG_RESPONSE_MSG.getName(),entity.getString("error_description"));
						  }if(entity.has("error_code")  &&!entity.isNull("error_code")) {
							  fields.put(FieldType.PG_RESP_CODE.getName(),entity.getString("error_code"));
						  }
					  }
					  JSONObject acquirer_data = entity.getJSONObject("acquirer_data");
					  if(acquirer_data.has("rrn")) {
						  fields.put(FieldType.RRN.getName(),acquirer_data.getString("rrn"));
					  }
					logger.info("Razorpay razorpayUpiResponseHandler payId : " + fields.get(FieldType.PAY_ID.getName()));
					User user = userDao.findPayId(fields.get(FieldType.PAY_ID.getName()));
					if(user!=null) {
						if(fieldsDao.getCapturedForPgRrnAndPayId(fields.get(FieldType.PAY_ID.getName()),fields.get(FieldType.RRN.getName()))) {
							logger.info("duplicat entry payId : " +fields.get(FieldType.PAY_ID.getName()) +"rrn : "+ fields.get(FieldType.RRN.getName()));
						}else {
							Fields updatefields = new Fields();
							updatefields =updateStatusResponse(status,date,id,fields,user,responseMsg );
							logger.info("setu callback response fields to transact " + updatefields.getFieldsAsString());
							Map<String, String> resp = transactionControllerServiceProvider.transact(updatefields,
									Constants.TXN_WS_UPI_PROCESSOR.getValue());
						
						}
					}else {
						logger.info("PayId " + payload.getString("description") +"Data not found");
					}
				  }
			  }
				
			}
			
			

		} catch (Exception e) {
			logger.error("Error in razorpayUpiResponseHandler callback = ", e);

		}

	}
	
	

	public Fields updateStatusResponse(String status,String date,String id, Fields oldfields,User responseUser,String responseMsg ) throws SystemException {
		try {
			Fields fields = new Fields();
			if (status.equalsIgnoreCase("captured")) {
				fields.put(FieldType.STATUS.getName(),StatusType.CAPTURED.getName());
				fields.put(FieldType.PG_TXN_MESSAGE.getName(), responseMsg);
				fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.SUCCESS.getCode());
				fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.SUCCESS.getResponseMessage());
			
			}else {
				fields.put(FieldType.STATUS.getName(),StatusType.FAILED.getName());
				fields.put(FieldType.PG_RESP_CODE.getName(), oldfields.get(FieldType.PG_RESP_CODE.getName()));
				fields.put(FieldType.PG_RESPONSE_MSG.getName(), oldfields.get(FieldType.PG_RESPONSE_MSG.getName()));
			}

			Date dNow = new Date();
			String dateNow = DateCreater.formatDateForDb(dNow);
			
			if (responseUser != null && StringUtils.isNotBlank(responseUser.getSuperMerchantId())
					&& !responseUser.isSuperMerchant()) {
				fields.put(FieldType.PAY_ID.getName(), responseUser.getSuperMerchantId());
				fields.put(FieldType.SUB_MERCHANT_ID.getName(), responseUser.getPayId());
			} else {
				fields.put(FieldType.PAY_ID.getName(), responseUser.getPayId());
			}
			if (StringUtils.isNotBlank(responseUser.getResellerId())) {
				fields.put(FieldType.RESELLER_ID.getName(), responseUser.getResellerId());
			}
			
			fields.put(FieldType.AMOUNT.getName(), oldfields.get(FieldType.AMOUNT.getName()));
			fields.put(FieldType.TOTAL_AMOUNT.getName(), oldfields.get(FieldType.AMOUNT.getName()));
				fields.put(FieldType.VPA.getName(), oldfields.get(FieldType.VPA.getName()));
			fields.put(FieldType.TXNTYPE.getName(), TransactionType.SALE.getName());
			fields.put(FieldType.MOP_TYPE.getName(), MopType.STATIC_UPI_QR.getCode());
			fields.put(FieldType.ORIG_TXNTYPE.getName(), TransactionType.SALE.getName());
			fields.put(FieldType.PAYMENTS_REGION.getName(), AccountCurrencyRegion.DOMESTIC.toString());
			fields.put(FieldType.CARD_HOLDER_TYPE.getName(), CardHolderType.CONSUMER.toString());
			fields.put(FieldType.ACQUIRER_MODE.getName(), "OFF_US");
			fields.put(FieldType.TRANSACTION_MODE.getName(), "Direct");
			fields.put(FieldType.TXN_CAPTURE_FLAG.getName(), "Real-Time");
			fields.put(FieldType.TXN_DATE.getName(), dateNow.substring(0, 10).replace("-", ""));
			fields.put(FieldType.CURRENCY_CODE.getName(), "356");
			fields.put(FieldType.PAYMENT_TYPE.getName(), PaymentType.UPI.getCode());
			fields.put(FieldType.ACQUIRER_TYPE.getName(), AcquirerType.RAZORPAY.getName());
			
			fields.put(FieldType.INTERNAL_VALIDATE_HASH_YN.getName(), "N");
			fields.put(FieldType.INTERNAL_CUST_COUNTRY_NAME.getName(), "India");
			fields.put(FieldType.PG_DATE_TIME.getName(), date);

			fields.put(FieldType.CREATE_DATE.getName(), dateNow);
			fields.put(FieldType.UPDATE_DATE.getName(), dateNow);
			fields.put(FieldType.INSERTION_DATE.getName(), dateNow);
			fields.put(FieldType.DATE_INDEX.getName(), dateNow.substring(0, 10).replace("-", ""));
		
			fields.put(FieldType.ACQUIRER_TDR_SC.getName(), "0.00");
			fields.put(FieldType.ACQUIRER_GST.getName(), "0.00");
			fields.put(FieldType.PG_GST.getName(), "0.00");
			fields.put(FieldType.PG_TDR_SC.getName(), "0.00");
			fields.put(FieldType.MERCHANT_TDR_SC.getName(), "0.00");
			fields.put(FieldType.MERCHANT_GST.getName(), "0.00");
			fields.put(FieldType.RESELLER_CHARGES.getName(), "0.00");
			fields.put(FieldType.RESELLER_GST.getName(), "0.00");
			fields.put(FieldType.PAYER_ADDRESS.getName(), oldfields.get(FieldType.VPA.getName()));
			fields.put(FieldType.TXN_ID.getName(), TransactionManager.getNewTransactionId());
			fields.put(FieldType.PG_REF_NUM.getName(), TransactionManager.getNewTransactionId());
			fields.put(FieldType.ORIG_TXN_ID.getName(), fields.get(FieldType.PG_REF_NUM.getName()));
			fields.put(FieldType.ORDER_ID.getName(), TransactionManager.getNewTransactionId());
			fields.put(FieldType.OID.getName(), fields.get(FieldType.ORDER_ID.getName()));
			fields.put(FieldType.UDF3.getName(), oldfields.get(FieldType.VPA.getName()));
			fields.put(FieldType.RRN.getName(), oldfields.get(FieldType.RRN.getName()));
			fields.put(FieldType.ACQ_ID.getName(), oldfields.get(FieldType.RRN.getName()));
			UserSettingData merchntSettings = userSettingDao
					.fetchDataUsingPayId(oldfields.get(FieldType.PAY_ID.getName()));

			if (merchntSettings.isSurchargeFlag()) {
				fields.put(FieldType.SURCHARGE_FLAG.getName(), "Y");
			} else {
				fields.put(FieldType.SURCHARGE_FLAG.getName(), "N");
			}

			return fields;
		} catch (Exception e) {
			logger.error("Unknown Exception :", e);
			throw new SystemException(ErrorType.INTERNAL_SYSTEM_ERROR, e,
					"Unknown exception in  ICICIUpiResponseAction");
		}
	}

}
