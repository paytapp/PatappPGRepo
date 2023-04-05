package com.paymentgateway.crm.actionBeans;

import java.util.Map;

import org.slf4j.Logger; import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.util.Amount;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.crm.action.AbstractSecureAction;

/**
 * @author Shashi
 *
 */
public class RefundLimitUpdater extends AbstractSecureAction {
	/**
	 * 
	 */
	
	@Autowired
	private UserDao userDao;
	
	private static final long serialVersionUID = 6077024655866344650L;
	private static Logger logger = LoggerFactory.getLogger(RefundLimitUpdater.class.getName());
	public void extraRefundLimitUpdate(String currencyCode,
			Map<String, String> requestMap, User user, String origTxnId,
			String payId, float todayRefundedAmount,
			float todayTotalCapturedAmount) {
		float refundingAmount = 0;
		float newExtraRefundLimitUpdate = 0;
		float extraRefundLimit = 0;
		String refundingAmountDecimal;
		String refundingAmountRM = requestMap.get(FieldType.AMOUNT.getName());
		User user_ = userDao.find(user.getEmailId());
		try{
		refundingAmountDecimal = Amount.toDecimal(refundingAmountRM,
				currencyCode);
		refundingAmount = Float.parseFloat(refundingAmountDecimal); // current refunding Amount
		extraRefundLimit = (user.getExtraRefundLimit()); // User add amount externally
	
			float refundedAmount = (todayRefundedAmount-todayTotalCapturedAmount);
			float totalRefundedLimit = refundedAmount+extraRefundLimit;
			float totalRefundableAmount = (todayTotalCapturedAmount + totalRefundedLimit)
					- todayRefundedAmount;
			if (refundingAmount <= totalRefundableAmount) {
			newExtraRefundLimitUpdate = totalRefundableAmount - refundingAmount;
			//in case any attribute of user is updated by another resource
			user_.setExtraRefundLimit(newExtraRefundLimitUpdate);
			userDao.update(user_);
		} else {
			if(refundingAmount  > (todayTotalCapturedAmount-todayRefundedAmount)){
				float deductFromExtraLimit = refundingAmount-(todayTotalCapturedAmount-todayRefundedAmount);
				if(deductFromExtraLimit <= extraRefundLimit){
					newExtraRefundLimitUpdate = extraRefundLimit - deductFromExtraLimit;
					user_.setExtraRefundLimit(newExtraRefundLimitUpdate);
					userDao.update(user_);
				} 
		}
	}
		}catch (Exception exception) {
			logger.error("Exception", exception);	
			}
}
}