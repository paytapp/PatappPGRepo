//package com.paymentgateway.pgui.session;
//
//import java.text.DateFormat;
//import java.text.SimpleDateFormat;
//import java.util.Calendar;
//
//import javax.servlet.http.HttpSession;
//import javax.servlet.http.HttpSessionEvent;
//import javax.servlet.http.HttpSessionListener;
//
//import org.apache.commons.lang3.StringUtils;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.BeansException;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.ApplicationContext;
//import org.springframework.context.ApplicationContextAware;
//import org.springframework.stereotype.Component;
//import org.springframework.web.context.WebApplicationContext;
//
//import com.paymentgateway.commons.user.AccountCurrencyRegion;
//import com.paymentgateway.commons.user.CardHolderType;
//import com.paymentgateway.commons.util.Constants;
//import com.paymentgateway.commons.util.FieldType;
//import com.paymentgateway.commons.util.Fields;
//
///**
// * @author Surender,Rahul
// *
// */
//
//@Component
//public class SessionEventListener implements HttpSessionListener , ApplicationContextAware{
//	private static Logger logger = LoggerFactory.getLogger(SessionEventListener.class
//			.getName());
//	
//	@Autowired
//	private SessionTimeoutHandler pguiSessionTimeoutHandler; 
//	
//	
//@Override
//public void sessionCreated(HttpSessionEvent event) {
//	// Setting the session ID and Session Time
//	HttpSession sessionObj = event.getSession();
//	DateFormat currentDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//	Calendar calobj = Calendar.getInstance();
//	String timeStamp = currentDate.format(calobj.getTime());
//	sessionObj.setAttribute("sessionCreationTime", timeStamp);
//	
//	logger.info(" session id from session listener class to get the time  " +timeStamp);
//}
//	
//	// Session Destroyed
//	@Override
//	public void sessionDestroyed(HttpSessionEvent event) {
//		
//		if (pguiSessionTimeoutHandler != null) {
//			
//		
//	 	try {
//			Object sessionObj = event.getSession();
//			HttpSession session = (HttpSession) sessionObj;
//			
//			String txnCompleteFlag = null;
//			String origTxnId = null;
//			Fields fields = null;
//
//			Object fieldsObj = session
//					.getAttribute(Constants.FIELDS.getValue());
//			if (null == fieldsObj) {
//				//return as session not found
//				 return;
//			} else {
//				fields = (Fields) fieldsObj;
//			}
//			
//			String oId = (String) session
//					.getAttribute(FieldType.OID.getName());
//			fields.put(FieldType.OID.getName(), oId);
//			
//			String orig_txnType = fields.get(FieldType.ORIG_TXNTYPE.getName());
//			String txnType = fields.get(FieldType.TXNTYPE.getName());
//			
//			if(!StringUtils.isBlank(orig_txnType)) {
//				fields.put(FieldType.ORIG_TXNTYPE.getName(), orig_txnType);
//			}
//			else {
//				fields.put(FieldType.ORIG_TXNTYPE.getName(), txnType);
//			}
//			
//			String surchargeFlag = fields.get(FieldType.SURCHARGE_FLAG.getName());
//			
//			if (!StringUtils.isBlank(surchargeFlag)) {
//				fields.put(FieldType.SURCHARGE_FLAG.getName(), surchargeFlag);
//			}
//			
//			String paymentsRegion = (String) session
//					.getAttribute(FieldType.PAYMENTS_REGION.getName());
//			
//			if (!StringUtils.isBlank(paymentsRegion)) {
//				fields.put(FieldType.PAYMENTS_REGION.getName(), paymentsRegion);
//			}
//			else {
//				fields.put(FieldType.PAYMENTS_REGION.getName(), AccountCurrencyRegion.DOMESTIC.toString());
//			}
//			
//			String cardHolderType = (String) session
//					.getAttribute(FieldType.CARD_HOLDER_TYPE.getName());
//			
//			if (!StringUtils.isBlank(cardHolderType)) {
//				fields.put(FieldType.CARD_HOLDER_TYPE.getName(), cardHolderType);
//			}
//			else {
//				fields.put(FieldType.CARD_HOLDER_TYPE.getName(), CardHolderType.CONSUMER.toString());
//			}
//			
//			Object origTxnIdObj = null;
//			origTxnIdObj = session
//					.getAttribute(FieldType.INTERNAL_ORIG_TXN_ID.getName());
//
//			if (null != origTxnIdObj) {
//				origTxnId = (String) origTxnIdObj;
//				fields.put(FieldType.INTERNAL_ORIG_TXN_ID.getName(), origTxnId);
//			} else {
//				origTxnId = fields.get(FieldType.INTERNAL_ORIG_TXN_ID.getName());
//				//Put txn id as ORIG_TXN_ID if not found
//				if(StringUtils.isEmpty(origTxnId)){					
//					fields.put(FieldType.INTERNAL_ORIG_TXN_ID.getName(),
//							fields.get(FieldType.TXN_ID.getName()));
//				}
//				
//			}
//
//			Object txnCompleteFlagObj = session
//					.getAttribute(Constants.TRANSACTION_COMPLETE_FLAG
//							.getValue());
//
//			if (null != txnCompleteFlagObj) {
//				txnCompleteFlag = (String) txnCompleteFlagObj;
//				fields.put(Constants.TRANSACTION_COMPLETE_FLAG.getValue(),
//						txnCompleteFlag);
//			} else {
//				fields.put(Constants.TRANSACTION_COMPLETE_FLAG.getValue(),
//						Constants.N_FLAG.getValue());
//			}
//			pguiSessionTimeoutHandler.handleTimeOut(fields);
//		} catch (Exception exception) {
//			logger.error("Error processing timeout " , exception);
//		} 
//	 	
//	}
//		
//		else {
//			logger.info("Duplicate timeout process request Dropped" );
//		}
//	}
//	
//	@Override
//    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
//        if (applicationContext instanceof WebApplicationContext) {
//            ((WebApplicationContext) applicationContext).getServletContext().addListener(this);
//        } else {
//            //Either throw an exception or fail gracefully, up to you
//            throw new RuntimeException("Must be inside a web application context");
//        }
//    }           
//}
