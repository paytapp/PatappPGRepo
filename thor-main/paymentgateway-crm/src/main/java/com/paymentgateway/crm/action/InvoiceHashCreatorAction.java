package com.paymentgateway.crm.action;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.user.Invoice;
import com.paymentgateway.commons.user.InvoiceTransactionDao;
import com.paymentgateway.commons.util.CrmFieldType;
import com.paymentgateway.commons.util.CrmValidator;
import com.paymentgateway.crm.actionBeans.InvoiceHasher;
import com.paymentgateway.crm.dashboard.LineChartAction;

public class InvoiceHashCreatorAction extends AbstractSecureAction{
	


	@Autowired
	private InvoiceHasher invoiceHasher;
	
	@Autowired
	private CrmValidator validator;
	
	@Autowired
	private InvoiceTransactionDao InvoiceTransactionDao;
	
	private static Logger logger = LoggerFactory.getLogger(InvoiceHashCreatorAction.class.getName());
	private static final long serialVersionUID = 1127688797146457067L;
	
	private Invoice invoice = new Invoice();
	private String name;
	private String email;
	private String phone;
	private String hash;
	private String invoiceId;
	
	public String execute() {
		try {
			invoice = InvoiceTransactionDao.findByInvoiceId(invoiceId);
			
		if(StringUtils.isNotBlank(name)){
			invoice.setName(name);
		}
		if(StringUtils.isNotBlank(email)){
			invoice.setEmail(email);
		}else{
			invoice.setEmail("");
		}
		if(StringUtils.isNotBlank(phone))
		{
			invoice.setPhone(phone);
		}else{
			invoice.setPhone("");
		}
			setHash(invoiceHasher.createInvoiceHash(invoice));
			
			return SUCCESS;
			
		} catch (Exception e) {
			logger.error("Exception : " , e);
			return null;
		}
		
	
	}
	public void validate() {

		if (StringUtils.isBlank(email)) {
			addFieldError("email",
					ErrorType.INVALID_FIELD.getResponseMessage());
		} 
		if (StringUtils.isBlank(phone)) {
			addFieldError("phone",
					ErrorType.INVALID_FIELD.getResponseMessage());
		}
	}
	

	public Invoice getInvoice() {
		return invoice;
	}
	public void setInvoice(Invoice invoice) {
		this.invoice = invoice;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getPhone() {
		return phone;
	}
	public void setPhone(String phone) {
		this.phone = phone;
	}
	public String getHash() {
		return hash;
	}
	public void setHash(String hash) {
		this.hash = hash;
	}
	public String getInvoiceId() {
		return invoiceId;
	}
	public void setInvoiceId(String invoiceId) {
		this.invoiceId = invoiceId;
	}
	

}
