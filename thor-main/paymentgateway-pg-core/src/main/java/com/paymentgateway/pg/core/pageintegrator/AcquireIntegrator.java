package com.paymentgateway.pg.core.pageintegrator;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.StatusType;
import com.paymentgateway.pg.core.util.Processor;

/**
 * @author Rahul
 *
 */
public class AcquireIntegrator implements Processor {

	@Autowired
	private AcquirerCustomizerFactory AcquirerCustomizerFactory;
	
	@Override
	public void preProcess(Fields fields) throws SystemException {
	}

	@Override
	public void process(Fields fields) throws SystemException {
		String responseCode = fields.get(FieldType.RESPONSE_CODE.getName());
		if(!StringUtils.isEmpty(responseCode) && !responseCode.equals(ErrorType.SUCCESS.getCode())){
			if(fields.get(FieldType.STATUS.getName()).equals(StatusType.PENDING.getName())){
				fields.put(FieldType.STATUS.getName(), StatusType.ERROR.getName());
			}
//			responseCreator.ResponsePost(fields);
			return;
		}

		Customizer customizer = AcquirerCustomizerFactory.instance(fields);
		customizer.integrate(fields);
	}

	@Override
	public void postProcess(Fields fields) throws SystemException {

	}
}
