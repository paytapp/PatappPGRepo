package com.paymentgateway.commons.util;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.dao.CustomerIdGeneratorDao;

@Service
public class CustomerIdGenerator {
	
	private static Logger logger = LoggerFactory.getLogger(CustomerIdGenerator.class.getName());
	
	@Autowired
	private CustomerIdGeneratorDao customerIdGeneratorDao;
	

 	String initialNumeric = "00000001";
 	String newCustomerId = null;
 	
 	public synchronized String getNewCustomerId(){
 		try{
 			Map<String,String> customerIdDetail = customerIdGeneratorDao.fetchLastCustomerId();
 			
 			if(customerIdDetail.isEmpty()){
 				newCustomerId = initialNumeric;
 				customerIdGeneratorDao.insertLatestCustomerId(newCustomerId);
 			}else{
 				
 				String customerId = customerIdDetail.get(FieldType.CUSTOMER_ID.getName());
 				long newNumSeries = Long.valueOf(customerId)+1;
					newCustomerId = String.valueOf(String.format("%08d", newNumSeries));
 				
 				customerIdGeneratorDao.updateLatestCustomerId(customerId,newCustomerId);
 			}
 		
 		}catch(Exception e){
 			logger.error("Exception " , e);
 		}
		return newCustomerId;
 	}

}
