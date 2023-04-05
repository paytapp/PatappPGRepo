package com.paymentgateway.commons.util;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.dao.OrderIdGeneratorDao;

@Service
public class OrderIdGenerator {
	
	private static Logger logger = LoggerFactory.getLogger(OrderIdGenerator.class.getName());
	
	@Autowired
	private OrderIdGeneratorDao orderIdGeneratorDao;
	

 	String initialAlpha = "A";
 	String initialNumeric = "00001";
 	String newOrderId=null;
 	
 	public synchronized String getNewOrderId(){
 		try{
 			Map<String,String> orderIdDetail=orderIdGeneratorDao.fetchLastOrderId();
 			if(orderIdDetail.isEmpty()){
 				newOrderId=initialAlpha+initialNumeric;
 				orderIdGeneratorDao.insertLatestOrderId(newOrderId,initialAlpha,initialNumeric);
 			}else{
 				String orderId=orderIdDetail.get(FieldType.ORDER_ID.getName());
 				String alphaSeries=orderIdDetail.get(FieldType.ALPHA_SERIES.getName());
 				String numericSeries=orderIdDetail.get(FieldType.NUMERIC_SERIES.getName());
 				
 				if(numericSeries.equals("99999")){
 					char alpha=alphaSeries.charAt(0);
 					alpha++;
 					
 					if(alpha>'Z'){
 						logger.info("limit excced of order id is Z99999");
 					}
 					else{
 						alphaSeries=String.valueOf(alpha);
 						numericSeries=initialNumeric;
 						newOrderId=alphaSeries+numericSeries;
 					}
 					
 				}else{
 					long newNumSeries=Long.valueOf(numericSeries)+1;
 					numericSeries=String.valueOf(String.format("%05d", newNumSeries));
 					newOrderId=alphaSeries+numericSeries;
 				}
 				
 				if(orderId.equals(newOrderId)){
 					logger.debug("duplicate order id found "+orderId);
 				}
 				
 				orderIdGeneratorDao.updateLatestOrderId(orderId,newOrderId,alphaSeries,numericSeries);
 			}
 	
 	
 		
 		}catch(Exception e){
 			logger.error("Exception " , e);
 		}
		return newOrderId;
 	}

}
