package com.paymentgateway.pg.core.fraudPrevention.util;

import org.springframework.stereotype.Component;

import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.SystemConstants;

/**
 * @author Harpreet
 *
 */
@Component
public class FraudPreventionUtil {
	// to create truncated/masked card no
	public static String makeCardMask(Fields fields){
		String reqCardNo = fields.get(FieldType.CARD_NUMBER.getName());
		StringBuilder sb = new StringBuilder();
		sb.append(reqCardNo.substring(0, SystemConstants.CARD_BIN_LENGTH ));
		sb.append(Constants.CARD_STARS.getValue());
		sb.append(reqCardNo.subSequence(reqCardNo.length() - SystemConstants.CARD_BIN_LENGTH + 2, reqCardNo.length()));
		return sb.toString();
	}
}
