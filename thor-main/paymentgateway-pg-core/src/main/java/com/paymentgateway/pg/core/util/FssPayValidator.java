package com.paymentgateway.pg.core.util;

import java.util.Collection;

import org.springframework.stereotype.Service;

import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.MopType;
import com.paymentgateway.commons.util.SystemProperties;
import com.paymentgateway.commons.util.TransactionType;
import com.paymentgateway.commons.util.Validator;

@Service
public class FssPayValidator implements Validator {
    private static final String dummyCVV = "111";
    private static final String expDate = "102055";
	public void validate(Fields fields) throws SystemException {
		
		switch(TransactionType.getInstance(fields.get(FieldType.TXNTYPE.getName()))){
		case AUTHORISE:
			break;
		case ENROLL:
			validateEnrollment(fields);
			break;
		case REFUND:
			break;
		case SALE:
			break;
		case CAPTURE:
			break;
		case STATUS:
			break;
		default:
			break;
		}
		
	}//validate()
	
	public boolean isValidOrigTransactionType(String origTransactionType){		
		
		boolean valid = false;
		
		if(null != origTransactionType && (origTransactionType.equals(TransactionType.SALE.getName())
				|| origTransactionType.equals(TransactionType.AUTHORISE.getName()))){
			valid = true;
		}
		
		return valid;
	}//isValidOrigTransactionType()

	public void validateEnrollment(Fields fields) throws SystemException{
		Collection<String> mandatoryEnrollmentFields = SystemProperties.getFssenrollmandatoryfields();
		validateMandatoryFields(fields,mandatoryEnrollmentFields);
	}

	public void validateMandatoryFields(Fields fields, Collection<String> mandatoryEnrollmentFields) throws SystemException{
		String mopType = fields.get(FieldType.MOP_TYPE.getName());		
		if(null==mopType){
			throw new SystemException(ErrorType.VALIDATION_FAILED,
					FieldType.MOP_TYPE.getName() + " is a required field");
		}

		for(String key : mandatoryEnrollmentFields){
			String cardBin = fields.get(FieldType.CARD_NUMBER.getName()).replace(" ", "").replace(",", "").substring(0, 4);
			if(null == fields.get(key)){
				if (FieldType.CVV.getName().equals(key) && mopType.equals(MopType.MAESTRO.getCode())){
					fields.put(FieldType.CVV.getName(),dummyCVV);
					continue;
				}else if(FieldType.CARD_EXP_DT.getName().equals(key) && mopType.equals(MopType.MAESTRO.getCode())){
					fields.put(FieldType.CARD_EXP_DT.getName(), expDate);
					continue;
				}else if((cardBin.equals("5018") || cardBin.equals("5020") || cardBin.equals("5038") || cardBin.equals("5612") || cardBin.equals("5893") || cardBin.equals("6304") || cardBin.equals("6759") || cardBin.equals("6220") || cardBin.equals("0604")|| cardBin.equals("6390") || cardBin.equals("6799"))){
					fields.put(FieldType.CARD_EXP_DT.getName(), expDate);
					fields.put(FieldType.MOP_TYPE.getName(), MopType.MAESTRO.getCode());
					continue;
				}
				
				throw new SystemException(ErrorType.VALIDATION_FAILED,
						key + " is a required field");
			}//if
		}//for
	}
}
