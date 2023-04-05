package com.paymentgateway.pgui.action;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.dao.FieldsDao;
import com.paymentgateway.commons.dao.UpiAutoPayDao;
import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.util.AutoPayFrequency;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.PaymentType;
import com.paymentgateway.commons.util.StatusType;
import com.paymentgateway.commons.util.TransactionManager;
import com.paymentgateway.commons.util.TransactionType;
import com.paymentgateway.pg.core.util.IciciUpiAutoPayUtil;
import com.paymentgateway.pg.core.util.ResponseCreator;

/**
 * @author Rajit
 */

@Service
public class IciciUpiAutoPayResponseAction {

	@Autowired
	private IciciUpiAutoPayUtil iciciUpiAutoPayUtil;

	@Autowired
	private UpiAutoPayDao upiAutoPayDao;

	@Autowired
	private UserDao userDao;

	@Autowired
	FieldsDao fieldsDao;

	@Autowired
	private ResponseCreator responseCreator;

	private static Logger logger = LoggerFactory.getLogger(IciciUpiAutoPayResponseAction.class);

	private String message;
	private String startDate;
	private String endDate;
	private String tenure;
	private String custEmail;
	private String custMobile;
	private String payId;
	private String returnUrl;
	private String orderId;
	private String payerVPA;
	private String frequency;
	private String monthlyAmount;
	private String amount;
	private String totalAmount;
	private String merchantLogo;

	Map<String, String> aaData = new HashMap<String, String>();



	public Map<String, String> responsePopUpPage(String orderid) {
		logger.info("inside upi autopay responsePopUpPage function ");
		try {

//			PrintWriter out = ServletActionContext.getResponse().getWriter();
			if (StringUtils.isNotBlank(message) && message.equalsIgnoreCase("cancel")) {
				aaData.put(FieldType.STATUS.getName(), StatusType.CANCELLED.getName());
				aaData.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.CANCELLED.getResponseMessage());
				aaData.put(FieldType.RESPONSE_CODE.getName(), ErrorType.CANCELLED.getCode());

				String txnId = TransactionManager.getNewTransactionId();
				User user = userDao.findPayId(payId);

				// for reseller sub merchant
				if (user.isSuperMerchant() == false && StringUtils.isNotBlank(user.getSuperMerchantId())
						&& StringUtils.isNotBlank(user.getResellerId())) {
					aaData.put(FieldType.PAY_ID.getName(), user.getSuperMerchantId());
					aaData.put(FieldType.SUB_MERCHANT_ID.getName(), payId);
					aaData.put(FieldType.RESELLER_ID.getName(), user.getResellerId());

				} else if (user.isSuperMerchant() == false && StringUtils.isNotBlank(user.getSuperMerchantId())) {
					// for Sub Merchant

					// super merchantId
					aaData.put(FieldType.PAY_ID.getName(), user.getSuperMerchantId());
					// sub MerchantId
					aaData.put(FieldType.SUB_MERCHANT_ID.getName(), payId);

				} else if (StringUtils.isNotBlank(user.getResellerId())) {
					// Reseller Merchant
					aaData.put(FieldType.RESELLER_ID.getName(), user.getResellerId());
					aaData.put(FieldType.PAY_ID.getName(), payId);
					aaData.put(FieldType.SUB_MERCHANT_ID.getName(), "");
				} else {
					// super merchantId
					aaData.put(FieldType.PAY_ID.getName(), payId);
					// sub MerchantId
					aaData.put(FieldType.SUB_MERCHANT_ID.getName(), "");
				}
				List<String> debitDateList = fieldsDao.getDueDateList(startDate, "", tenure,
						aaData.get(FieldType.PAY_ID.getName()), aaData.get(FieldType.SUB_MERCHANT_ID.getName()),
						"autoPay");
				StringBuilder debitDateBuilder = new StringBuilder();

				for (String date : debitDateList) {
					debitDateBuilder.append(date).append(",");
				}
				debitDateBuilder.deleteCharAt(debitDateBuilder.length() - 1);
				aaData.put(FieldType.DATE_FROM.getName(), startDate);
				aaData.put(FieldType.DATE_TO.getName(), endDate);
				aaData.put("DEBIT_DATE_LIST", debitDateBuilder.toString());

				// Total Transaction Amount that are debited from customer account
				aaData.put(Constants.AMOUNT.getValue(), amount);
				// max amount field that are debited from customer account
				aaData.put("TRANSACTION_AMOUNT", monthlyAmount);
				aaData.put(FieldType.MAX_AMOUNT.getName(), totalAmount);
				aaData.put(FieldType.TOTAL_AMOUNT.getName(), totalAmount);
				aaData.put(FieldType.PAYMENT_TYPE.getName(), PaymentType.UPI.getCode());

				aaData.put(FieldType.CUST_PHONE.getName(), custMobile);
				aaData.put(FieldType.CUST_EMAIL.getName(), custEmail);
				aaData.put(FieldType.FREQUENCY.getName(), AutoPayFrequency.getAutoPayFrequencyCode(frequency));
				aaData.put(FieldType.ORDER_ID.getName(), orderId);
				aaData.put(FieldType.PAYER_ADDRESS.getName(), payerVPA);
				aaData.put(FieldType.TENURE.getName(), tenure);

				aaData.put(FieldType.TXNTYPE.getName(), TransactionType.REGISTRATION.getName());
				aaData.put(FieldType.TXN_ID.getName(), txnId);
				aaData.put(FieldType.PG_REF_NUM.getName(), txnId);
				aaData.put(FieldType.ORIG_TXN_ID.getName(), txnId);
				aaData.put("MERCHANT_LOGO", merchantLogo);
				Fields fields = new Fields(aaData);
				upiAutoPayDao.insertAutoPayCancelledRegistrationDetail(fields);

				aaData.put(FieldType.RETURN_URL.getName(), returnUrl);

			} else if (StringUtils.isNotBlank(message) && message.equalsIgnoreCase("hashFail")) {

				aaData.put(FieldType.STATUS.getName(), StatusType.CANCELLED.getName());
				aaData.put(FieldType.RESPONSE_MESSAGE.getName(), StatusType.AUTHENTICATION_FAILED.getName());
				aaData.put(FieldType.RETURN_URL.getName(), returnUrl);
			} else if (StringUtils.isNotBlank(message) && message.equalsIgnoreCase("duplicateOrderId")) {

				aaData.put(FieldType.STATUS.getName(), StatusType.CANCELLED.getName());
				aaData.put(FieldType.RESPONSE_MESSAGE.getName(), "Duplicate Request");
				aaData.put(FieldType.RETURN_URL.getName(), returnUrl);

			} else {
				aaData = upiAutoPayDao.fetchTransactionByOrderId(orderid, TransactionType.REGISTRATION.getName());
			}
		} catch (Exception ex) {
			logger.info("exception caugh while open a response popup page ", ex);
		}
		return aaData;
	}

	public Map<String, String> getAaData() {
		return aaData;
	}

	public void setAaData(Map<String, String> aaData) {
		this.aaData = aaData;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getStartDate() {
		return startDate;
	}

	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}

	public String getEndDate() {
		return endDate;
	}

	public void setEndDate(String endDate) {
		this.endDate = endDate;
	}

	public String getTenure() {
		return tenure;
	}

	public void setTenure(String tenure) {
		this.tenure = tenure;
	}

	public String getCustEmail() {
		return custEmail;
	}

	public void setCustEmail(String custEmail) {
		this.custEmail = custEmail;
	}

	public String getCustMobile() {
		return custMobile;
	}

	public void setCustMobile(String custMobile) {
		this.custMobile = custMobile;
	}

	public String getPayId() {
		return payId;
	}

	public void setPayId(String payId) {
		this.payId = payId;
	}

	public String getReturnUrl() {
		return returnUrl;
	}

	public void setReturnUrl(String returnUrl) {
		this.returnUrl = returnUrl;
	}

	public String getOrderId() {
		return orderId;
	}

	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}

	public String getPayerVPA() {
		return payerVPA;
	}

	public void setPayerVPA(String payerVPA) {
		this.payerVPA = payerVPA;
	}

	public String getFrequency() {
		return frequency;
	}

	public void setFrequency(String frequency) {
		this.frequency = frequency;
	}

	public String getMonthlyAmount() {
		return monthlyAmount;
	}

	public void setMonthlyAmount(String monthlyAmount) {
		this.monthlyAmount = monthlyAmount;
	}

	public String getAmount() {
		return amount;
	}

	public void setAmount(String amount) {
		this.amount = amount;
	}

	public String getTotalAmount() {
		return totalAmount;
	}

	public void setTotalAmount(String totalAmount) {
		this.totalAmount = totalAmount;
	}

	public String getMerchantLogo() {
		return merchantLogo;
	}

	public void setMerchantLogo(String merchantLogo) {
		this.merchantLogo = merchantLogo;
	}

}
