package com.paymentgateway.pg.core.pageintegrator;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.util.AcquirerType;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;

/**
 * @author Rahul
 *
 */
@Component
public class AcquirerCustomizerFactory {

	public AcquirerCustomizerFactory() {
	}
	
	@Autowired
	private FssCustomizer FssCustomizer;
	
	@Autowired
	private AmexCustomizer AmexCustomizer;
	
	@Autowired
	private FederalCustomizer FederalCustomizer;
	
	@Autowired
	private EzeeClickCustomizer EzeeClickCustomizer;
	
	
	public Customizer instance(Fields fields) throws SystemException{
		AcquirerType acquirer = null;
		String acquirerCode = fields.get(FieldType.ACQUIRER_TYPE.getName());

		if(StringUtils.isBlank(acquirerCode)){
			acquirer = AcquirerType.getDefault(fields);
			fields.put(FieldType.ACQUIRER_TYPE.getName(), acquirer.getCode());
			fields.put(FieldType.INTERNAL_ACQUIRER_TYPE.getName(), acquirer.getCode());
		}else{
			acquirer = AcquirerType.getInstancefromCode(acquirerCode);
		}

		switch (acquirer) {
		case FSS:
			return FssCustomizer;			
		case FEDERAL:
			return FederalCustomizer;	
		default:
			break;
		}
		
		//Ideally non reachable code
		return null;
	}
	
}
