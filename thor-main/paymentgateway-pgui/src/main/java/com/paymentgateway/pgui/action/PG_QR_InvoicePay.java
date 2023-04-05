package com.paymentgateway.pgui.action;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.ModelDriven;
import com.paymentgateway.commons.user.Invoice;
import com.paymentgateway.commons.user.InvoiceTransactionDao;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.util.Amount;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.Currency;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.pgui.action.beans.InvoiceHasher;

public class PG_QR_InvoicePay extends AbstractSecureAction implements ModelDriven<Invoice> {
	@Autowired
	private InvoiceTransactionDao InvoiceTransactionDao;

	@Autowired
	private InvoiceHasher invoiceHasher;

	@Autowired
	private UserDao userDao;

	private Invoice invoice = new Invoice();
	private static final long serialVersionUID = 6999933886992616101L;
	private static Logger logger = LoggerFactory.getLogger(InvoicePay.class.getName());
	private String svalue;
	private String invoiceUrl;
	private String hash;
	private String totalamount;
	private String businessName;
	private String enablePay;
	private String currencyName;

	public String execute() {

		try {

			invoice = InvoiceTransactionDao.findByTxnId(svalue);
			if (null == invoice) {
				return ERROR;
			}
			setInvoiceUrl(PropertiesManager.propertiesMap.get("PG_QR_URL")+svalue);
			setHash(invoiceHasher.createInvoiceHash(invoice));
			totalamount = Amount.formatAmount(invoice.getAmount(), invoice.getCurrencyCode());
			setBusinessName(userDao.findPayId(invoice.getPayId()).getBusinessName());
			setCurrencyName(Currency.getAlphabaticCode(invoice.getCurrencyCode()));
			getEnablePayNow();
		} catch (Exception exception) {
			MDC.put(Constants.CRM_LOG_USER_PREFIX.getValue(), invoice.getPayId() + svalue);
			logger.error("Exception", exception);
			return ERROR;
		}
		return SUCCESS;
	}

	private void getEnablePayNow() {
		setEnablePay("TRUE");
	}

	@Override
	public Invoice getModel() {
		return invoice;
	}

	public Invoice getInvoice() {
		return invoice;
	}

	public void setInvoice(Invoice invoice) {
		this.invoice = invoice;
	}

	public String getSvalue() {
		return svalue;
	}

	public void setSvalue(String svalue) {
		this.svalue = svalue;
	}

	public String getInvoiceUrl() {
		return invoiceUrl;
	}

	public void setInvoiceUrl(String invoiceUrl) {
		this.invoiceUrl = invoiceUrl;
	}

	public String getHash() {
		return hash;
	}

	public void setHash(String hash) {
		this.hash = hash;
	}

	public String getTotalamount() {
		return totalamount;
	}

	public void setTotalamount(String totalamount) {
		this.totalamount = totalamount;
	}

	public String getEnablePay() {
		return enablePay;
	}

	public void setEnablePay(String enablePay) {
		this.enablePay = enablePay;
	}

	public String getCurrencyName() {
		return currencyName;
	}

	public void setCurrencyName(String currencyName) {
		this.currencyName = currencyName;
	}

	public String getBusinessName() {
		return businessName;
	}

	public void setBusinessName(String businessName) {
		this.businessName = businessName;
	}

}
