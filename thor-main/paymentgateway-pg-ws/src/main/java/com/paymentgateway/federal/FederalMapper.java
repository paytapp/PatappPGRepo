package com.paymentgateway.federal;

import org.springframework.stereotype.Service;

import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.StatusType;

/**
 * @author Rahul
 *
 */
@Service
public class FederalMapper {
	
	public StatusType getStatusType(String federalResponseCode, String receivedStatus){
		StatusType status = null;
		if((federalResponseCode.equals("000")) && receivedStatus.equals(Constants.Y_FLAG.getValue())){
			status = StatusType.CAPTURED;
		} else if(federalResponseCode.equals("001")){
			status = StatusType.INVALID;
		} else {
			status = StatusType.REJECTED;
		}
		
		return status;
	}
	
	public ErrorType getErrorType(String federalResponseCode, String receivedStatus){
		ErrorType error = null;
		
		if((federalResponseCode.equals("000")) && receivedStatus.equals(Constants.Y_FLAG.getValue())){
			error = ErrorType.SUCCESS;
		} else if(federalResponseCode.equals("001")){
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
	
	
	public String getMesaage(String federalResponseCode, String receivedStatus) {
		
		String msg = null;
		
		if((federalResponseCode.equals("000")) && receivedStatus.equals(Constants.Y_FLAG.getValue())){
			msg = "No Error";
		} else if(federalResponseCode.equals("001")){
			msg = "Request Field Invalid";
		} else if(federalResponseCode.equals("002")){
			msg = "System Under Maintenance ";
		} else if(federalResponseCode.equals("003")){
			msg = "Internal Server Error";
		} else if(federalResponseCode.equals("004")){
			msg = "Database Server Error";
		} else if(federalResponseCode.equals("005")){
			msg = "Protocol Error";
		} else if(federalResponseCode.equals("006")){
			msg = "Does not have Permission";
		} else if(federalResponseCode.equals("007")){
			msg = "Card Not Part of 3DS Range";
		}else {
			msg = "Rejceted";
		}
		
		return msg;
		
		
		
	}
	

}
