package com.paymentgateway.commons.util;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.owasp.esapi.ESAPI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.user.CustomerAddress;
import com.paymentgateway.commons.user.ExceptionReport;
import com.paymentgateway.commons.user.GstRSaleReport;
import com.paymentgateway.commons.user.Invoice;
import com.paymentgateway.commons.user.InvoiceHistory;
import com.paymentgateway.commons.user.LoginHistory;
import com.paymentgateway.commons.user.MerchantDetails;
import com.paymentgateway.commons.user.Merchants;
import com.paymentgateway.commons.user.NodalAmount;
import com.paymentgateway.commons.user.PaymentOptions;
import com.paymentgateway.commons.user.RefundPreview;
import com.paymentgateway.commons.user.SUFDetail;
import com.paymentgateway.commons.user.SearchTransaction;
import com.paymentgateway.commons.user.SearchUser;
import com.paymentgateway.commons.user.TransactionSearch;
import com.paymentgateway.commons.user.TransactionSummaryReport;
import com.paymentgateway.commons.user.UserAudit;

/**
 * @author Rahul
 *
 */
@Service
public class DataEncoder {
	
	private static Logger logger = LoggerFactory.getLogger(DataEncoder.class.getName());

	public static String encodeString(String data) {
		return ESAPI.encoder().encodeForHTML(data);
	}
	

	public byte[] getByteStream(String data) throws SystemException {
		try {
			return data.getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new SystemException(ErrorType.INTERNAL_SYSTEM_ERROR, "Encoding error");
		}
	}

	public List<MerchantDetails> encodeMerchantDetailsObj(List<MerchantDetails> merchants) {
		// TODO.. code commented for live deployment
		for (MerchantDetails merchant : merchants) {
			merchant.setBusinessName(encodeString(merchant.getBusinessName()));
			merchant.setPayId(encodeString(merchant.getPayId()));
			// merchant.setResellerId(encodeString(merchant.getResellerId()));
			merchant.setEmailId(encodeString(merchant.getEmailId()));
			merchant.setMobile(encodeString(merchant.getMobile()));
			// merchant.setUserType(encodeString(merchant.getUserType()));
		}
		return merchants;
	}
	public List<UserAudit> encodeUserAuditDetailsObj(List<UserAudit> usersAuditData) {
		// TODO.. code commented for live deployment
		for (UserAudit userAuditData : usersAuditData) {
			userAuditData.setBusinessName(encodeString(userAuditData.getBusinessName()));
			userAuditData.setPayId(encodeString(userAuditData.getPayId()));
			userAuditData.setEmailId(encodeString(userAuditData.getEmailId()));
			userAuditData.setMobile(encodeString(userAuditData.getMobile()));
		}
		return usersAuditData;
	}	public List<MerchantDetails> encodeMPAMerchantDetails(List<MerchantDetails> merchants) {
		for (MerchantDetails merchant : merchants) {
			merchant.setBusinessName(encodeString(merchant.getBusinessName()));
			merchant.setPayId(encodeString(merchant.getPayId()));
		}
		return merchants;
	}
	public List<PaymentOptions> encodePaymentOptions(List<PaymentOptions> paymentOptions) {
		for(PaymentOptions paymentOption : paymentOptions) {		
			paymentOption.setPayId(encodeString(paymentOption.getPayId()));
			paymentOption.setMerchantName(encodeString(paymentOption.getMerchantName()));
		}
		return paymentOptions;
	}
	
	public List<SUFDetail> encodeSUFDetails(List<SUFDetail> sufDetails) {
		for(SUFDetail sufDetail : sufDetails) {		
			sufDetail.setPayId(encodeString(sufDetail.getPayId()));
			sufDetail.setMerchantName(encodeString(sufDetail.getMerchantName()));
		}
		return sufDetails;
	}
	
	public List<GstRSaleReport> encodegstRSaleReport(List<GstRSaleReport> gstRsaleactions) {

		for (GstRSaleReport gstRsaleaction : gstRsaleactions) {
			// gstRsaleaction.setAcquirer(encodeString(gstRsaleaction.getac));
			/*
			 * transaction.setApprovedAmount(encodeString(transaction
			 * .getApprovedAmount())); transaction.setBusinessName(encodeString(transaction
			 * .getBusinessName())); transaction.setCaptureTxnId(encodeString(transaction
			 * .getCaptureTxnId()));
			 * transaction.setCardNo(encodeString(transaction.getCardNo()));
			 * transaction.setChargebackAmount(encodeString(transaction
			 * .getChargebackAmount()));
			 * transaction.setCurrencyCode(encodeString(transaction .getCurrencyCode()));
			 * transaction.setCustomerEmail(encodeString(transaction .getCustomerEmail()));
			 * transaction.setCustomerName(encodeString(transaction .getCustomerName()));
			 * transaction.setInternalRequestDesc(encodeString(transaction
			 * .getInternalRequestDesc()));
			 * transaction.setMopType(encodeString(transaction.getMopType()));
			 * transaction.setNetAmount(encodeString(transaction.getNetAmount()));
			 * transaction.setOid(encodeString(transaction.getOid()));
			 * transaction.setOrderId(encodeString(transaction.getOrderId()));
			 * transaction.setOrigTransactionId(encodeString(transaction
			 * .getOrigTransactionId()));
			 * transaction.setOrigTxnDate(encodeString(transaction .getOrigTxnDate()));
			 * transaction.setPayId(encodeString(transaction.getPayId()));
			 * transaction.setPaymentMethod(encodeString(transaction .getPaymentMethod()));
			 * transaction.setProductDesc(encodeString(transaction .getProductDesc()));
			 * transaction .setRefundDate(encodeString(transaction.getRefundDate()));
			 * transaction.setRefundedAmount(encodeString(transaction
			 * .getRefundedAmount())); transaction.setResponseMsg(encodeString(transaction
			 * .getResponseMsg())); transaction
			 * .setServiceTax(encodeString(transaction.getServiceTax())); transaction
			 * .setSettleDate(encodeString(transaction.getSettleDate()));
			 * transaction.setStatus(encodeString(transaction.getStatus()));
			 * transaction.setTdr(encodeString(transaction.getTdr()));
			 * transaction.setTransactionId(encodeString(transaction .getTransactionId()));
			 * transaction.setTxnDate(encodeString(transaction.getTxnDate()));
			 * transaction.setTxnType(encodeString(transaction.getTxnType()));
			 * transaction.setInternalCardIssusserBank(encodeString(transaction.
			 * getInternalCardIssusserBank()));
			 * transaction.setInternalCardIssusserCountry(encodeString(transaction.
			 * getInternalCardIssusserCountry()));
			 * transaction.setPgTxnMessage(encodeString(transaction.getPgTxnMessage()));
			 * transaction.setPayId(encodeString(transaction.getPayId()));
			 * transaction.setAggregatorName(encodeString(transaction.getAggregatorName()));
			 */

		}
		return gstRsaleactions;
	}

	public List<TransactionSummaryReport> encodeTransactionSummary(List<TransactionSummaryReport> transactions) {

		for (TransactionSummaryReport transaction : transactions) {
			transaction.setAcquirer(encodeString(transaction.getAcquirer()));
			transaction.setApprovedAmount(encodeString(transaction.getApprovedAmount()));
			transaction.setBusinessName(encodeString(transaction.getBusinessName()));
			transaction.setCaptureTxnId(encodeString(transaction.getCaptureTxnId()));
			transaction.setCardNo(encodeString(transaction.getCardNo()));
			transaction.setChargebackAmount(encodeString(transaction.getChargebackAmount()));
			transaction.setCurrencyCode(encodeString(transaction.getCurrencyCode()));
			transaction.setCustomerEmail(encodeString(transaction.getCustomerEmail()));
			transaction.setCustomerName(encodeString(transaction.getCustomerName()));
			transaction.setInternalRequestDesc(encodeString(transaction.getInternalRequestDesc()));
			transaction.setMopType(encodeString(transaction.getMopType()));
			transaction.setNetAmount(encodeString(transaction.getNetAmount()));
			transaction.setOid(encodeString(transaction.getOid()));
			transaction.setOrderId(encodeString(transaction.getOrderId()));
			transaction.setOrigTransactionId(encodeString(transaction.getOrigTransactionId()));
			transaction.setOrigTxnDate(encodeString(transaction.getOrigTxnDate()));
			transaction.setPayId(encodeString(transaction.getPayId()));
			transaction.setPaymentMethod(encodeString(transaction.getPaymentMethod()));
			transaction.setProductDesc(encodeString(transaction.getProductDesc()));
			transaction.setRefundDate(encodeString(transaction.getRefundDate()));
			transaction.setRefundedAmount(encodeString(transaction.getRefundedAmount()));
			transaction.setResponseMsg(encodeString(transaction.getResponseMsg()));
			transaction.setServiceTax(encodeString(transaction.getServiceTax()));
			transaction.setSettleDate(encodeString(transaction.getSettleDate()));
			transaction.setStatus(encodeString(transaction.getStatus()));
			transaction.setTdr(encodeString(transaction.getTdr()));
			transaction.setTransactionId(encodeString(transaction.getTransactionId()));
			transaction.setTxnDate(encodeString(transaction.getTxnDate()));
			transaction.setTxnType(encodeString(transaction.getTxnType()));
			transaction.setInternalCardIssusserBank(encodeString(transaction.getInternalCardIssusserBank()));
			transaction.setInternalCardIssusserCountry(encodeString(transaction.getInternalCardIssusserCountry()));
			transaction.setPgTxnMessage(encodeString(transaction.getPgTxnMessage()));
			transaction.setPayId(encodeString(transaction.getPayId()));
			transaction.setAggregatorName(encodeString(transaction.getAggregatorName()));
		}
		return transactions;
	}

	public List<TransactionSearch> encodeTransactionSearchObj(List<TransactionSearch> transactions) {

		
		try {
			for (TransactionSearch transaction : transactions) {
              DecimalFormat df=new DecimalFormat("0.00");
				transaction.setAmount(encodeString(transaction.getAmount()));
				transaction.setCardNumber(encodeString(transaction.getCardNumber()));
				transaction.setCurrency(encodeString(transaction.getCurrency()));
				transaction.setCustomerEmail(encodeString(transaction.getCustomerEmail()));
				transaction.setCustomerName(encodeString(transaction.getCustomerName()));
				transaction.setMerchants(encodeString(transaction.getMerchants()));
				transaction.setOrderId(encodeString(transaction.getOrderId()));
				transaction.setPayId(encodeString(transaction.getPayId()));
				transaction.setPaymentMethods(encodeString(transaction.getPaymentMethods()));
				transaction.setProductDesc(encodeString(transaction.getProductDesc()));
				transaction.setStatus(encodeString(transaction.getStatus()));
				transaction.setTxnType(encodeString(transaction.getTxnType()));
				transaction.setMopType(encodeString(transaction.getMopType()));
				transaction.setInternalCardIssusserBank(transaction.getInternalCardIssusserBank());
				transaction.setInternalCardIssusserCountry(transaction.getInternalCardIssusserCountry());
				transaction.setPaymentRegion(transaction.getPaymentRegion());
				transaction.setCardHolderType(transaction.getCardHolderType());
				transaction.setTdr_Surcharge(transaction.getTdr_Surcharge());
				transaction.setGst_charge(transaction.getGst_charge());
				transaction.setGlocalFlag(transaction.isGlocalFlag());
				transaction.setCustomFlag(transaction.getCustomFlag());
				transaction.setRrn(transaction.getRrn());
				transaction.setSubMerchantId(transaction.getSubMerchantId());
				transaction.setDeliveryStatus(transaction.getDeliveryStatus());
				transaction.setSufGst(encodeString(transaction.getSufGst()));
				transaction.setSufTdr(encodeString(transaction.getSufTdr()));
				
				
			}
		}

		catch (Exception e) {
			
			logger.error("Exception in encodeTransactionSearchObj , Exception =  " , e);
		}

		return transactions;
	}

	public List<Invoice> encodeInvoiceSearchObj(List<Invoice> invoices) {
		for (Invoice invoice : invoices) {
			invoice.setAddress(encodeString(invoice.getAddress()));
			invoice.setAmount(encodeString(invoice.getAmount()));
			invoice.setBusinessName(encodeString(invoice.getBusinessName()));
			invoice.setCountry(encodeString(invoice.getCountry()));
			invoice.setCurrencyCode(encodeString(invoice.getCurrencyCode()));
			invoice.setEmail(encodeString(invoice.getEmail()));
			invoice.setStatus(encodeString(invoice.getStatus()));
			invoice.setExpiresDay(encodeString(invoice.getExpiresDay()));
			invoice.setExpiresHour(encodeString(invoice.getExpiresDay()));
			invoice.setInvoiceId(encodeString(invoice.getInvoiceId()));
			invoice.setInvoiceNo(encodeString(invoice.getInvoiceNo()));
			invoice.setName(encodeString(invoice.getName()));
			invoice.setPayId(encodeString(invoice.getPayId()));
			invoice.setPhone(encodeString(invoice.getPhone()));
			invoice.setProductDesc(encodeString(invoice.getProductDesc()));
			invoice.setProductName(encodeString(invoice.getProductName()));
			invoice.setQuantity(encodeString(invoice.getQuantity()));
			invoice.setReturnUrl(encodeString(invoice.getReturnUrl()));
			invoice.setSaltKey(encodeString(invoice.getSaltKey()));
			invoice.setServiceCharge(encodeString(invoice.getServiceCharge()));
			invoice.setState(encodeString(invoice.getState()));
			invoice.setTotalAmount(encodeString(invoice.getTotalAmount()));
			invoice.setZip(encodeString(invoice.getZip()));
			invoice.setQuantity(encodeString(invoice.getQuantity()));
			invoice.setCity(encodeString(invoice.getCity()));
			invoice.setSubMerchantId(encodeString(invoice.getSubMerchantId()));
			invoice.setSubUserId(encodeString(invoice.getSubUserId()));
			invoice.setSubMerchantbusinessName(encodeString(invoice.getSubMerchantbusinessName()));
			invoice.setUDF11(encodeString(invoice.getUDF11()));
			invoice.setUDF12(encodeString(invoice.getUDF12()));
			invoice.setUDF13(encodeString(invoice.getUDF13()));
			invoice.setUDF14(encodeString(invoice.getUDF14()));
			invoice.setUDF15(encodeString(invoice.getUDF15()));
			invoice.setUDF16(encodeString(invoice.getUDF16()));
			invoice.setUDF17(encodeString(invoice.getUDF17()));
			invoice.setUDF18(encodeString(invoice.getUDF18()));
			
		}
		return invoices;
	}

	public List<LoginHistory> encodeLoginHistoryObj(List<LoginHistory> histories) {
		for (LoginHistory loginHistory : histories) {
			String loginDate = loginHistory.getTimeStamp();
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
			Date changedate = null;
			try {
				changedate = simpleDateFormat.parse(loginDate);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			String formattedDate = sdf.format(changedate);

			loginHistory.setBrowser(encodeString(loginHistory.getBrowser()));
			loginHistory.setBusinessName(encodeString(loginHistory.getBusinessName()));
			loginHistory.setEmailId(encodeString(loginHistory.getEmailId()));
			loginHistory.setFailureReason(encodeString(loginHistory.getFailureReason()));
			loginHistory.setIp(encodeString(loginHistory.getIp()));
			loginHistory.setOs(encodeString(loginHistory.getOs()));
			loginHistory.setTimeStamp(formattedDate);
			loginHistory.setId(loginHistory.getId());
		}
		return histories;
	}

	public List<Merchants> encodeMerchantsObj(List<Merchants> merchants) {
		for (Merchants merchant : merchants) {
			merchant.setBusinessName(encodeString(merchant.getBusinessName()));
			merchant.setEmailId(encodeString(merchant.getEmailId()));
			merchant.setFirstName(encodeString(merchant.getFirstName()));
			merchant.setLastName(encodeString(merchant.getLastName()));
			merchant.setMobile(encodeString(merchant.getMobile()));
			merchant.setPayId(encodeString(merchant.getPayId()));
		}
		return merchants;
	}

	public List<SubAdmin> encodeAgentsObj(List<SubAdmin> agents) {
		for (SubAdmin subAdmin : agents) {

			subAdmin.setAgentEmailId(encodeString(subAdmin.getAgentEmailId()));
			subAdmin.setAgentFirstName(encodeString(subAdmin.getAgentFirstName()));
			subAdmin.setAgentLastName(encodeString(subAdmin.getAgentLastName()));
			subAdmin.setAgentMobile(encodeString(subAdmin.getAgentMobile()));
			subAdmin.setPayId(encodeString(subAdmin.getPayId()));
		}
		return agents;
	}
	
	public List<Agent> encodenewAgentsObj(List<Agent> agents) {
		for (Agent agent : agents) {
			
			agent.setAgentEmailId(encodeString(agent.getAgentEmailId()));
			agent.setAgentFirstName(encodeString(agent.getAgentFirstName()));
			agent.setAgentLastName(encodeString(agent.getAgentLastName()));
			agent.setAgentMobile(encodeString(agent.getAgentMobile()));
			agent.setPayId(encodeString(agent.getPayId()));
		}
		return agents;
	}

	public List<Acquirer> encodeAcquirersObj(List<Acquirer> acquirers) {
		for (Acquirer acquirer : acquirers) {

			acquirer.setAcquirerEmailId(encodeString(acquirer.getAcquirerEmailId()));
			acquirer.setAcquirerFirstName(encodeString(acquirer.getAcquirerFirstName()));
			acquirer.setAcquirerLastName(encodeString(acquirer.getAcquirerLastName()));
			acquirer.setAcquirerBusinessName(encodeString(acquirer.getAcquirerBusinessName()));
			acquirer.setPayId(encodeString(acquirer.getPayId()));
			acquirer.setAcquirerAccountNo(encodeString(acquirer.getAcquirerAccountNo()));
		}
		return acquirers;
	}

	public List<NodalAmount> encodeNodalObj(List<NodalAmount> nodalList) {
		for (NodalAmount nodal : nodalList) {

			nodal.setAcquirer(nodal.getAcquirer());
			nodal.setPaymentType(nodal.getPaymentType());
			nodal.setNodalCreditAmount(nodal.getNodalCreditAmount());
			nodal.setReconDate((nodal.getReconDate()));
			nodal.setCreateDate(nodal.getCreateDate());

		}
		return nodalList;
	}
	
	
	public NodalAmount encodeNodalObjData(NodalAmount nodalAmount) {
		
		    NodalAmount nodal=new NodalAmount();
			nodal.setAcquirer(nodalAmount.getAcquirer());
			nodal.setPaymentType(nodalAmount.getPaymentType());
			nodal.setNodalCreditAmount(nodalAmount.getNodalCreditAmount());
			nodal.setReconDate((nodalAmount.getReconDate()));
			nodal.setCreateDate(nodalAmount.getCreateDate());

		
		     return nodal;
	}
	
	
	public List<SearchUser> encodeSearchUserObj(List<SearchUser> users) {
		for (SearchUser searchUser : users) {
			searchUser.setEmailId(encodeString(searchUser.getEmailId()));
			searchUser.setFirstName(encodeString(searchUser.getFirstName()));
			searchUser.setLastName(encodeString(searchUser.getLastName()));
			searchUser.setPhone(encodeString(searchUser.getPhone()));
		}
		return users;
	}

	public CustomerAddress encodeCustomerAddressObj(CustomerAddress customerAddress) {

		customerAddress.setCustCity(encodeString(customerAddress.getCustCity()));
		customerAddress.setCustCountry(encodeString(customerAddress.getCustCountry()));
		customerAddress.setCustName(encodeString(customerAddress.getCustName()));
		customerAddress.setCustPhone(encodeString(customerAddress.getCustPhone()));
		customerAddress.setCustShipCity(encodeString(customerAddress.getCustShipCity()));
		customerAddress.setCustShipName(encodeString(customerAddress.getCustShipName()));
		customerAddress.setCustShipCountry(encodeString(customerAddress.getCustShipCountry()));
		customerAddress.setCustShipState(encodeString(customerAddress.getCustShipState()));
		customerAddress.setCustShipStreetAddress1(encodeString(customerAddress.getCustShipStreetAddress1()));
		customerAddress.setCustShipStreetAddress2(encodeString(customerAddress.getCustShipStreetAddress2()));
		customerAddress.setCustShipZip(encodeString(customerAddress.getCustShipZip()));
		customerAddress.setCustState(encodeString(customerAddress.getCustState()));
		customerAddress.setCustStreetAddress1(encodeString(customerAddress.getCustStreetAddress1()));
		customerAddress.setCustStreetAddress2(encodeString(customerAddress.getCustStreetAddress2()));
		customerAddress.setCustZip(encodeString(customerAddress.getCustZip()));

		return customerAddress;

	}

	public List<BinRange> encodeBinRange(List<BinRange> binRangDisplay) {
		for (BinRange binRange : binRangDisplay) {

			binRange.setBinCodeLow(encodeString(binRange.getBinCodeLow()));
			binRange.setBinCodeHigh(encodeString(binRange.getBinCodeHigh()));
			binRange.setBinRangeLow(encodeString(binRange.getBinRangeLow()));
			binRange.setBinRangeHigh(encodeString(binRange.getBinRangeHigh()));
			// binRange.setCardType(encodeString(binRange.getCardType().toString()));
			binRange.setGroupCode(encodeString(binRange.getGroupCode()));
			binRange.setIssuerBankName(encodeString(binRange.getIssuerBankName()));
			binRange.setIssuerCountry(encodeString(binRange.getIssuerCountry()));
			// binRange.setMopType(encodeString(binRange.getMopType()));
			binRange.setProductName(encodeString(binRange.getProductName()));
			// binRange.setRfu1(encodeString(binRange.getRfu1()));
		}
		return binRangDisplay;
	}

	public List<ExceptionReport> encodeExceptionReportObj(List<ExceptionReport> exceptionReports) {
		for (ExceptionReport exceptionReport : exceptionReports) {
			exceptionReport.setPgRefNo(encodeString(exceptionReport.getPgRefNo()));
			// exceptionReport.setTxnId(encodeString(exceptionReport.getTxnId()));
			exceptionReport.setOrderId(encodeString(exceptionReport.getOrderId()));
			exceptionReport.setAcqId(encodeString(exceptionReport.getAcqId()));
		}
		return exceptionReports;
	}

	public List<RefundPreview> encodeRefundPreviewObj(List<RefundPreview> refundPreviews) {
		for (RefundPreview refundPreview : refundPreviews) {
			refundPreview.setPgRefNo(encodeString(refundPreview.getPgRefNo()));
			refundPreview.setRefundFlag(encodeString(refundPreview.getRefundFlag()));
			refundPreview.setAmount(encodeString(refundPreview.getAmount()));
			refundPreview.setOrderId(encodeString(refundPreview.getOrderId()));
			refundPreview.setPayId(encodeString(refundPreview.getPayId()));
			refundPreview.setOid(encodeString(refundPreview.getOid()));
		}
		return refundPreviews;
	}
	

	public List<SearchTransaction> encodeSearchTransactionObj(List<SearchTransaction> SearchTxn) {

		for (SearchTransaction transaction : SearchTxn) {
			transaction.setTransactionId(transaction.getTransactionId());
			transaction.setPgRefNum(encodeString(transaction.getPgRefNum()));
			transaction.setMerchant(encodeString(transaction.getMerchant()));
			transaction.setOrderId(encodeString(transaction.getOrderId()));
			transaction.settDate(encodeString(transaction.gettDate()));
			transaction.setPaymentType(encodeString(transaction.getPaymentType()));
			transaction.setMopType(encodeString(transaction.getMopType()));
			transaction.setTxnType(encodeString(transaction.getTxnType()));
			transaction.setCardNum(encodeString(transaction.getCardNum()));
			transaction.setStatus(encodeString(transaction.getStatus()));
			transaction.setAmount(encodeString(transaction.getAmount()));
			transaction.setTotalAmount(encodeString(transaction.getTotalAmount()));
			transaction.setCustName(encodeString(transaction.getCustName()));
			transaction.setRrn(encodeString(transaction.getRrn()));
		    transaction.setAcqId(encodeString(transaction.getAcqId()));
			transaction.setPgResponseMessage(encodeString(transaction.getPgResponseMessage()));
			transaction.setAcquirerTxnMessage(encodeString(transaction.getAcquirerTxnMessage()));
			transaction.setRefund_txn_id(encodeString(transaction.getRefund_txn_id()));
			transaction.setResponseCode(encodeString(transaction.getResponseCode()));
			transaction.setCustName(encodeString(transaction.getCustName()));
			
			if(StringUtils.isBlank(transaction.getCardMask())) {
				transaction.setCardMask(encodeString("NA"));
			}else {
				transaction.setCardMask(encodeString("NA"));
			}
		}
		return SearchTxn;
	}
	public List<InvoiceHistory> encodeInvoiceHistoryObj(List<InvoiceHistory> invoiceHistory) {
		for (InvoiceHistory in : invoiceHistory) {
			in.setFileName(encodeString(in.getFileName()));
			in.setDate(encodeString(in.getDate()));
			in.setSuccess((in.getSuccess()));
			in.setTotalUnsent(in.getTotalUnsent());
			in.setTotalRecords(in.getTotalRecords());
		}
		return invoiceHistory;
	}
	
}
