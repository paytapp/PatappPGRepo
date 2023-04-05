package com.paymentgateway.crm.actionBeans;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.dao.TransactionAmountService;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.util.Amount;
import com.paymentgateway.commons.util.FieldType;

/**
 * @author shashi
 *
 */
@Service
public class RefundChecker {
	
	@Autowired
	private TransactionAmountService transactionAmountService;
	
	private	float todayRefundedAmount = 0;
	private	float todayTotalCapturedAmount = 0;
	
	public boolean setAllRefundValidation(String currencyCode,
			Map<String, String> requestMap, User user, String origTxnId) throws SystemException {

		boolean refundFlag = false; 
		float refundingAmount = 0;
		float extraRefundLimit = 0;
		String refundingAmountFormatted;
		String refundingAmountRM = requestMap.get(FieldType.AMOUNT.getName());
		todayTotalCapturedAmount = transactionAmountService
				.todayCaptureAmountFatch(requestMap.get(FieldType.PAY_ID.getName()));
		todayRefundedAmount = transactionAmountService
			.todayRefundAmountFatch(requestMap.get(FieldType.PAY_ID.getName()));
		refundingAmountFormatted = Amount.toDecimal(refundingAmountRM,
				currencyCode);
		refundingAmount = Float.parseFloat(refundingAmountFormatted); // current refunding Amount
		extraRefundLimit = (user.getExtraRefundLimit()); // User add amount externally
		if (todayRefundedAmount > todayTotalCapturedAmount) {
			float refundedAmount = (todayRefundedAmount - todayTotalCapturedAmount);
			float totalRefundedLimit = refundedAmount + extraRefundLimit;
			float totalRefundableAmount = (todayTotalCapturedAmount + totalRefundedLimit)
					- todayRefundedAmount;
			if (refundingAmount <= totalRefundableAmount) {
				return refundFlag;
			} else {
				return true;
			}
		} else {
			if (refundingAmount <= todayTotalCapturedAmount
					- todayRefundedAmount) {
				return refundFlag;
			}
			if (refundingAmount > (todayTotalCapturedAmount - todayRefundedAmount)) {
				float deductFromExtraLimit = refundingAmount - (todayTotalCapturedAmount - todayRefundedAmount);
				if (deductFromExtraLimit <= extraRefundLimit) {
					return refundFlag;
				}
				return true;
			}
		}
		return  true;
	}
	
	public float getTodayRefundedAmount() {
		return todayRefundedAmount;
	}
	public void setTodayRefundedAmount(float todayRefundedAmount) {
		this.todayRefundedAmount = todayRefundedAmount;
	}
	public float getTodayTotalCapturedAmount() {
		return todayTotalCapturedAmount;
	}
	public void setTodayTotalCapturedAmount(float todayTotalCapturedAmount) {
		this.todayTotalCapturedAmount = todayTotalCapturedAmount;
	}
}
