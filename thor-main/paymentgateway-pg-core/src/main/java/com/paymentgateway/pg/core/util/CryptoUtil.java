/**
 * 
 */
package com.paymentgateway.pg.core.util;

import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.SystemConstants;

/**
 * @author setw
 *
 * Utility functions for security
 */
public class CryptoUtil {
	
	public static void truncateCardNumber(Fields fields){
		
		String cardNumber = fields.remove(FieldType.CARD_NUMBER.getName());
		if(null == cardNumber || cardNumber.contains("*") || cardNumber.length() < FieldType.CARD_NUMBER.getMinLength()
				|| cardNumber.length() > FieldType.CARD_NUMBER.getMaxLength()){
			//Already truncated or invalid card number
			return;
		}
		
		StringBuilder truncatedCardNumber = new StringBuilder();
		truncatedCardNumber.append(cardNumber.substring(0, SystemConstants.CARD_BIN_LENGTH ));
		truncatedCardNumber.append(Constants.CARD_STARS.getValue());
		truncatedCardNumber.append(cardNumber.subSequence(cardNumber.length() - SystemConstants.CARD_BIN_LENGTH + 2, cardNumber.length()));
		fields.put(FieldType.CARD_MASK.getName(), truncatedCardNumber.toString());		
	}
}
