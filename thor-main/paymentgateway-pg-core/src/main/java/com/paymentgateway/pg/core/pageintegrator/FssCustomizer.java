package com.paymentgateway.pg.core.pageintegrator;

import org.slf4j.Logger; import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.opensymphony.xwork2.Action;
import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.StatusType;
import com.paymentgateway.pg.core.util.RequestCreator;
import com.paymentgateway.pg.core.util.ResponseCreator;

/**
 * @author Sunil
 *
 */
@Service
public class FssCustomizer implements Customizer {
	private static Logger logger = LoggerFactory.getLogger(FssCustomizer.class.getName()); 
	
	@Autowired
	private RequestCreator requestCreator;
	
	@Autowired
	private ResponseCreator responseCreator;
	
	@Override
	public String integrate(Fields fields) {
		fields.logAllFields("All Response fields Recieved");

		String responseCode = fields.get(FieldType.RESPONSE_CODE.getName());
		logger.info("FssCustomizer Response" +   responseCode);

		if (fields.get(FieldType.STATUS.getName()) == StatusType.ENROLLED.getName()) {
		//	requestCreator.EnrollRequest(fields);						
		//	return Action.NONE;
		}

		if (null == responseCode || !responseCode.equals(ErrorType.SUCCESS.getCode())) {			
		//	requestCreator.InvalidRequest(fields);
		//	return Action.NONE;
		}
		else{
		//	EmailBuilder emailBuilder = new EmailBuilder();  TODO
			try {
		//		emailBuilder.transactionEmailer(fields,UserType.MERCHANT.toString());
			} catch (Exception exception) {
				logger.error("Exception", exception);
			}
			
//			responseCreator.ResponsePost(fields);
		}
		return Action.NONE;
	}
}
