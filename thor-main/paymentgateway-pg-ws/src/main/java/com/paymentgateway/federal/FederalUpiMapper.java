package com.paymentgateway.federal;

import org.springframework.stereotype.Service;

import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.util.StatusType;

/**
 * @author Rahul
 *
 */
@Service
public class FederalUpiMapper {

	public StatusType getStatusType(String federalResponseCode){
		StatusType status = null;
		if(federalResponseCode.equals("00")){
			status = StatusType.CAPTURED;
		} else if(federalResponseCode.equals("U19")){
			status = StatusType.FAILED;
		} else {
			status = StatusType.REJECTED;
		}
		
		return status;
	}
	
	public ErrorType getErrorType(String federalResponseCode){
		ErrorType error = null;
		
		if(federalResponseCode.equals("00")){
			error = ErrorType.SUCCESS;
		} else if(federalResponseCode.equals("U19")){
			error = ErrorType.INVALID_REQUEST_FIELD;
		} else if(federalResponseCode.equals("002")){
			error = ErrorType.CANCELLED;
		} else if(federalResponseCode.equals("003")){
			error = ErrorType.INTERNAL_SYSTEM_ERROR;
		} else if(federalResponseCode.equals("004")){
			error = ErrorType.CANCELLED;
		} else {
			error = ErrorType.DECLINED;
		}
		
		return error;
	}

}
