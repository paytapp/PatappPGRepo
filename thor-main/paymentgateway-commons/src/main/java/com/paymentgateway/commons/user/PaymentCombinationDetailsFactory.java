/*
 * package com.paymentgateway.commons.user;
 * 
 * import org.slf4j.Logger; import org.slf4j.LoggerFactory; import
 * org.springframework.beans.factory.annotation.Autowired; import
 * org.springframework.stereotype.Component; import
 * org.springframework.stereotype.Service;
 * 
 * import com.paymentgateway.commons.dao.PaymentCombinationDetailsDao; import
 * com.paymentgateway.commons.util.MopType; import
 * com.paymentgateway.commons.util.PaymentType; import
 * com.paymentgateway.commons.util.TransactionType;
 * 
 *//**
	 * @author Amitosh
	 *
	 *//*
		 * @Service public class PaymentCombinationDetailsFactory {
		 * 
		 * private static Logger logger =
		 * LoggerFactory.getLogger(PaymentCombinationDetailsFactory.class.getName());
		 * 
		 * 
		 * public void createPaymentCombinationDetail(PaymentType paymentType, MopType
		 * mopType, String acquirerName, String payId, TransactionType transactionType,
		 * String currency) {
		 * 
		 * logger.info("Preparing object for inserting payment Combinations for PayId: "
		 * + payId + " Transaction Type: " + transactionType + " Acquirer: " +
		 * acquirerName + " Payment Type: " + paymentType + " and Mop Type: " +
		 * mopType);
		 * 
		 * PaymentCombinationDetails paymentCombinationDetails = new
		 * PaymentCombinationDetails();
		 * paymentCombinationDetails.setAcquirerName(acquirerName);
		 * paymentCombinationDetails.setMopType(mopType);
		 * paymentCombinationDetails.setPayId(payId);
		 * paymentCombinationDetails.setTransactionType(transactionType);
		 * paymentCombinationDetails.setPaymentType(paymentType);
		 * paymentCombinationDetails.setCurrency(currency);
		 * paymentCombinationDetails.setStatus("ACTIVE");
		 * 
		 * paymentCombinationDetailsDao.insertPaymentCombination(
		 * paymentCombinationDetails); }
		 * 
		 * public void createPaymentCombinationDetail(PaymentType paymentType, MopType
		 * mopType, String acquirerName, String payId, String currencyCode,
		 * TransactionType transactionType) {
		 * 
		 * logger.info("Preparing object for inserting payment Combinations for PayId: "
		 * + payId + " Acquirer: " + acquirerName + " Payment Type: " + paymentType +
		 * " and Mop Type: " + mopType);
		 * 
		 * PaymentCombinationDetails paymentCombinationDetails = new
		 * PaymentCombinationDetails();
		 * paymentCombinationDetails.setAcquirerName(acquirerName);
		 * paymentCombinationDetails.setMopType(mopType);
		 * paymentCombinationDetails.setPayId(payId);
		 * paymentCombinationDetails.setPaymentType(paymentType);
		 * paymentCombinationDetails.setCurrency(currencyCode);
		 * paymentCombinationDetails.setTransactionType(transactionType);
		 * paymentCombinationDetails.setStatus("ACTIVE");
		 * 
		 * paymentCombinationDetailsDao.insertPaymentCombination(
		 * paymentCombinationDetails); } }
		 */