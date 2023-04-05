package com.paymentgateway.crm.actionBeans;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.api.Hasher;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.user.Invoice;
import com.paymentgateway.commons.util.Amount;
import com.paymentgateway.commons.util.ConfigurationConstants;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.PropertiesManager;

@Service
public class InvoiceHasher {

	@Autowired
	Hasher hasher;
	public String createInvoiceHash(Invoice invoice) throws SystemException {

		StringBuilder allFields = new StringBuilder();
		Map<String, String> invoiceMap = new HashMap<String, String>();

		invoiceMap.put(FieldType.PAY_ID.getName(), invoice.getPayId());
		invoiceMap.put(FieldType.ORDER_ID.getName(), invoice.getInvoiceId());
		invoiceMap.put(FieldType.AMOUNT.getName(), Amount.formatAmount(invoice.getTotalAmount(), invoice.getCurrencyCode()));
		invoiceMap.put(FieldType.TXNTYPE.getName(),"SALE");
		invoiceMap.put(FieldType.CUST_NAME.getName(), invoice.getName());
		if(StringUtils.isNotBlank(invoice.getAddress())){
			invoiceMap.put(FieldType.CUST_STREET_ADDRESS1.getName(), invoice.getAddress());
		}
		else{
			invoiceMap.put(FieldType.CUST_STREET_ADDRESS1.getName(), "");
		}
		if(StringUtils.isNotBlank(invoice.getZip())){
			invoiceMap.put(FieldType.CUST_ZIP.getName(), invoice.getZip());
		}
		else{
			invoiceMap.put(FieldType.CUST_ZIP.getName(), "");
		}
		
		if(StringUtils.isNotBlank(invoice.getPhone())){
			invoiceMap.put(FieldType.CUST_PHONE.getName(), invoice.getPhone());
		}
		else{
			invoiceMap.put(FieldType.CUST_PHONE.getName(), "");
		}
		if(StringUtils.isNotBlank(invoice.getEmail())){
			invoiceMap.put(FieldType.CUST_EMAIL.getName(), invoice.getEmail());
		}
		else{
			invoiceMap.put(FieldType.CUST_EMAIL.getName(), "");
		}
		if(StringUtils.isNotBlank(invoice.getProductDesc())){
			invoiceMap.put(FieldType.PRODUCT_DESC.getName(), invoice.getProductDesc());
		}
		else
		{
			invoiceMap.put(FieldType.PRODUCT_DESC.getName(), "");
		}
		invoiceMap.put(FieldType.CURRENCY_CODE.getName(), invoice.getCurrencyCode());
		invoiceMap.put(FieldType.RETURN_URL.getName(), invoice.getReturnUrl());
		
		

		Map<String, String> sortedMap = new TreeMap<String, String>(invoiceMap);
		for (String key : sortedMap.keySet()) {
			allFields.append(ConfigurationConstants.FIELD_SEPARATOR.getValue());
			allFields.append(key);
			allFields.append(ConfigurationConstants.FIELD_EQUATOR.getValue());
			allFields.append(sortedMap.get(key));
		}
		String salt = (new PropertiesManager()).getSalt(invoice.getPayId());
		allFields.deleteCharAt(0);
		allFields.append(salt);
		return hasher.getHash(allFields.toString());
	}

}
