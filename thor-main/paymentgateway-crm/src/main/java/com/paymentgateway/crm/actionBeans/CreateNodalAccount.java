package com.paymentgateway.crm.actionBeans;

import java.util.Date;

import org.slf4j.Logger; import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.user.NodalAmount;
import com.paymentgateway.commons.user.NodalAmoutDao;
import com.paymentgateway.commons.user.ResponseObject;

@Service
public class CreateNodalAccount {
	

	
	@Autowired
	NodalAmoutDao nodalDao;

	
	private static Logger logger = LoggerFactory.getLogger(CreateNodalAccount.class.getName());
	
	public ResponseObject createNodalAmount(NodalAmount nodal) throws SystemException {
		logger.info("Inside new nodal account ");
		
		ResponseObject responseActionObject = new ResponseObject();
		try {
			Date date = new Date();
			nodal.setAcquirer(nodal.getAcquirer());
			nodal.setPaymentType(nodal.getPaymentType());
			nodal.setNodalCreditAmount(nodal.getNodalCreditAmount());
			nodal.setCreateDate(nodal.getCreateDate());
			nodal.setCreateDate(date);

			nodalDao.create(nodal);
			responseActionObject.setResponseCode(ErrorType.SUCCESS.getResponseCode());
		    return responseActionObject;
	}
		catch (Exception e) {
			responseActionObject.setResponseCode(ErrorType.USER_UNAVAILABLE.getResponseCode());
		 return	responseActionObject;
		}

		
}
}
