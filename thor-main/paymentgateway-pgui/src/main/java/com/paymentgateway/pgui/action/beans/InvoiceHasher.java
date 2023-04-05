package com.paymentgateway.pgui.action.beans;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.api.Hasher;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.user.EPOSTransaction;
import com.paymentgateway.commons.user.Invoice;
import com.paymentgateway.commons.util.Amount;
import com.paymentgateway.commons.util.ConfigurationConstants;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.TxnType;

@Service
public class InvoiceHasher {

	@Autowired
	Hasher hasher;

	public String createInvoiceHash(Invoice invoice) throws SystemException {

		StringBuilder allFields = new StringBuilder();
		Map<String, String> invoiceMap = new HashMap<String, String>();

		invoiceMap.put(FieldType.PAY_ID.getName(), invoice.getPayId());
		invoiceMap.put(FieldType.ORDER_ID.getName(), invoice.getInvoiceId());
		invoiceMap.put(FieldType.AMOUNT.getName(),
				Amount.formatAmount(invoice.getTotalAmount(), invoice.getCurrencyCode()));
		invoiceMap.put(FieldType.TXNTYPE.getName(), "SALE");
		if (StringUtils.isNotBlank(invoice.getName())) {
			invoiceMap.put(FieldType.CUST_NAME.getName(), invoice.getName());
		} else {
			invoiceMap.put(FieldType.CUST_NAME.getName(), "");
		}
		if (StringUtils.isNotBlank(invoice.getAddress())) {
			invoiceMap.put(FieldType.CUST_STREET_ADDRESS1.getName(), invoice.getAddress());
		} else {
			invoiceMap.put(FieldType.CUST_STREET_ADDRESS1.getName(), "");
		}
		if (StringUtils.isNotBlank(invoice.getZip())) {
			invoiceMap.put(FieldType.CUST_ZIP.getName(), invoice.getZip());
		} else {
			invoiceMap.put(FieldType.CUST_ZIP.getName(), "");
		}

		if (StringUtils.isNotBlank(invoice.getPhone())) {
			invoiceMap.put(FieldType.CUST_PHONE.getName(), invoice.getPhone());
		} else {
			invoiceMap.put(FieldType.CUST_PHONE.getName(), "");
		}
		if (StringUtils.isNotBlank(invoice.getEmail())) {
			invoiceMap.put(FieldType.CUST_EMAIL.getName(), invoice.getEmail());
		} else {
			invoiceMap.put(FieldType.CUST_EMAIL.getName(), "");
		}
		if (StringUtils.isNotBlank(invoice.getProductDesc())) {
			invoiceMap.put(FieldType.PRODUCT_DESC.getName(), invoice.getProductDesc());
		} else {
			invoiceMap.put(FieldType.PRODUCT_DESC.getName(), "");
		}
		if (StringUtils.isNotBlank(invoice.getProductName())) {
			invoiceMap.put(FieldType.PRODUCT_NAME.getName(), invoice.getProductName());
		} else {
			invoiceMap.put(FieldType.PRODUCT_NAME.getName(), "");
		}
		if (StringUtils.isNotBlank(invoice.getQuantity())) {
			invoiceMap.put(FieldType.QUANTITY.getName(), invoice.getQuantity());
		} else {
			invoiceMap.put(FieldType.QUANTITY.getName(), "");
		}
		if (StringUtils.isNotBlank(invoice.getSubMerchantId())) {
			invoiceMap.put(FieldType.SUB_MERCHANT_ID.getName(), invoice.getSubMerchantId());
		} else {
			invoiceMap.put(FieldType.SUB_MERCHANT_ID.getName(), "");
		}
		invoiceMap.put(FieldType.CURRENCY_CODE.getName(), invoice.getCurrencyCode());
		invoiceMap.put(FieldType.RETURN_URL.getName(), invoice.getReturnUrl());

		if (StringUtils.isNotBlank(invoice.getUDF11()))
			invoiceMap.put(FieldType.UDF11.getName(), invoice.getUDF11());
		if (StringUtils.isNotBlank(invoice.getUDF12()))
			invoiceMap.put(FieldType.UDF12.getName(), invoice.getUDF12());
		if (StringUtils.isNotBlank(invoice.getUDF13()))
			invoiceMap.put(FieldType.UDF13.getName(), invoice.getUDF13());
		if (StringUtils.isNotBlank(invoice.getUDF14()))
			invoiceMap.put(FieldType.UDF14.getName(), invoice.getUDF14());
		if (StringUtils.isNotBlank(invoice.getUDF15()))
			invoiceMap.put(FieldType.UDF15.getName(), invoice.getUDF15());
		if (StringUtils.isNotBlank(invoice.getUDF16()))
			invoiceMap.put(FieldType.UDF16.getName(), invoice.getUDF16());
		if (StringUtils.isNotBlank(invoice.getUDF17()))
			invoiceMap.put(FieldType.UDF17.getName(), invoice.getUDF17());
		if (StringUtils.isNotBlank(invoice.getUDF18()))
			invoiceMap.put(FieldType.UDF18.getName(), invoice.getUDF18());

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

	public String createEposHash(EPOSTransaction epos) throws SystemException {
		StringBuilder allFields = new StringBuilder();
		Map<String, String> eposMap = new HashMap<String, String>();

		if (StringUtils.isNotBlank(epos.getCUST_MOBILE())) {
			eposMap.put(FieldType.CUST_PHONE.getName(), epos.getCUST_MOBILE());
		} else {
			eposMap.put(FieldType.CUST_PHONE.getName(), "");
		}

		if (StringUtils.isNotBlank(epos.getCUST_EMAIL())) {
			eposMap.put(FieldType.CUST_EMAIL.getName(), epos.getCUST_EMAIL());
		} else {
			eposMap.put(FieldType.CUST_EMAIL.getName(), "");
		}
		eposMap.put(FieldType.CUST_NAME.getName(), epos.getCUST_NAME());
		eposMap.put(FieldType.PAY_ID.getName(), epos.getPAY_ID());
		eposMap.put(FieldType.EPOS_PAYMENT_OPTION.getName(), epos.getEPOS_PAYMENT_OPTION());
		eposMap.put(FieldType.ORDER_ID.getName(), epos.getINVOICE_ID());
		eposMap.put(FieldType.AMOUNT.getName(), Amount.formatAmount(epos.getAMOUNT(), epos.getCURRENCY_CODE()));
		eposMap.put(FieldType.CURRENCY_CODE.getName(), epos.getCURRENCY_CODE());
		eposMap.put(FieldType.RETURN_URL.getName(), epos.getRETURN_URL());
		eposMap.put(FieldType.TXNTYPE.getName(), TxnType.SALE.name());
		eposMap.put(FieldType.CUST_STREET_ADDRESS1.getName(), "");
		eposMap.put(FieldType.CUST_ZIP.getName(), "");
		eposMap.put(FieldType.PRODUCT_DESC.getName(), "");
		if (StringUtils.isNotBlank(epos.getUDF11())) {
			eposMap.put("UDF11", epos.getUDF11());
		} else {
			eposMap.put("UDF11", "");
		}
		if (StringUtils.isNotBlank(epos.getUDF12())) {
			eposMap.put("UDF12", epos.getUDF12());
		} else {
			eposMap.put("UDF12", "");
		}
		if (StringUtils.isNotBlank(epos.getUDF13())) {
			eposMap.put("UDF13", epos.getUDF13());
		} else {
			eposMap.put("UDF13", "");
		}
		if (StringUtils.isNotBlank(epos.getUDF14())) {
			eposMap.put("UDF14", epos.getUDF14());
		} else {
			eposMap.put("UDF14", "");
		}
		if (StringUtils.isNotBlank(epos.getUDF15())) {
			eposMap.put("UDF15", epos.getUDF15());
		} else {
			eposMap.put("UDF15", "");
		}
		if (StringUtils.isNotBlank(epos.getUDF16())) {
			eposMap.put("UDF16", epos.getUDF16());
		} else {
			eposMap.put("UDF16", "");
		}
		if (StringUtils.isNotBlank(epos.getUDF17())) {
			eposMap.put("UDF17", epos.getUDF17());
		} else {
			eposMap.put("UDF17", "");
		}
		if (StringUtils.isNotBlank(epos.getUDF18())) {
			eposMap.put("UDF18", epos.getUDF18());
		} else {
			eposMap.put("UDF18", "");
		}

		Map<String, String> sortedMap = new TreeMap<String, String>(eposMap);
		for (String key : sortedMap.keySet()) {
			allFields.append(ConfigurationConstants.FIELD_SEPARATOR.getValue());
			allFields.append(key);
			allFields.append(ConfigurationConstants.FIELD_EQUATOR.getValue());
			allFields.append(sortedMap.get(key));
		}

		String salt = (new PropertiesManager()).getSalt(epos.getPAY_ID());
		allFields.deleteCharAt(0);
		allFields.append(salt);
		return hasher.getHash(allFields.toString());
	}
}