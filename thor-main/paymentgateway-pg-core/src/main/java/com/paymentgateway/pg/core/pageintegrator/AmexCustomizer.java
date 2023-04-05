package com.paymentgateway.pg.core.pageintegrator;

import org.springframework.stereotype.Service;

import com.opensymphony.xwork2.Action;
import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.pg.core.util.ResponseCreator;

/**
 * @author Sunil
 *
 */
@Service
public class AmexCustomizer implements Customizer {

	@Override
	public String integrate(Fields fields) throws SystemException {

		fields.logAllFields("All Response fields Recieved");
		String responseCode = fields.get(FieldType.RESPONSE_CODE.getName());

		//Code find all the possible failure reasons and handle accordingly
		if (null == responseCode || !responseCode.equals(ErrorType.SUCCESS.getCode())) {
			ResponseCreator responseCreator = new ResponseCreator();
//			responseCreator.ResponsePost(fields);
		}
		return Action.NONE;
	}
}
