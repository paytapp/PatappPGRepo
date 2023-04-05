//package com.paymentgateway.pgui.session;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.stereotype.Service;
//
//import com.paymentgateway.commons.exception.SystemException;
//import com.paymentgateway.commons.util.FieldType;
//import com.paymentgateway.commons.util.Fields;
//import com.paymentgateway.commons.util.StatusType;
//import com.paymentgateway.commons.util.TransactionManager;
//import com.paymentgateway.pg.core.util.Processor;
//
//
//@Service
//public class SessionTimeoutHandler {
//
//	private static final String transactionTypes = "NEWORDER-SALE-AUTHORISE-ENROLL";
//	private static Logger logger = LoggerFactory.getLogger(SessionTimeoutHandler.class
//			.getName());
//	
//	@Autowired
//	@Qualifier("timeoutProcessor")
//	private Processor timeoutProcessor;
//	
//	public void handleTimeOut(Fields fields) {
//		
//		String status = fields.get(FieldType.STATUS.getName());
//		if (status == null){
//			status = StatusType.TIMEOUT.getName();
//		}
//		if (transactionTypes.contains(fields.get(FieldType.TXNTYPE.getName()))
//				&& status.equals(StatusType.PENDING.getName())
//				|| status.equals(StatusType.SENT_TO_BANK.getName())
//				|| status.equals(StatusType.TIMEOUT.getName())
//				|| status.equals(StatusType.ENROLLED.getName())) {
//			
//			fields.put(FieldType.TXN_ID.getName(), TransactionManager.getNewTransactionId());
//			// call timeout processor
//			try {
//				timeoutProcessor.preProcess(fields);
//				timeoutProcessor.process(fields);
//				timeoutProcessor.postProcess(fields);
//				/*if(TaskSchedulerListener.postBackFlag){
//					postBackCreator.sendPostBack(fields);
//				}else{
//					System.out.println("postback not run");
//				}*/
//			} catch (SystemException systemException) {
//				logger.error("Error handling timeout and updating transaction : " , systemException);
//			} catch (Exception exception) {
//				logger.error("Unmapped exception handling timeout : " , exception);
//			}
//		} else {			
//				// send post back
//			/*if(TaskSchedulerListener.postBackFlag){
//				postBackCreator.sendPostBack(fields);
//			}else{
//				System.out.println("postback not run");
//			}*/
//		}
//	}
//}
