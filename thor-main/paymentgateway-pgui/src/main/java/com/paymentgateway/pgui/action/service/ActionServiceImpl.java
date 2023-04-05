package com.paymentgateway.pgui.action.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.api.Hasher;
import com.paymentgateway.commons.api.TransactionControllerServiceProvider;
import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;

/**
 * @author Puneet,Rahul
 *
 */
@Service
public class ActionServiceImpl implements ActionService {
	private static Logger logger = LoggerFactory.getLogger(ActionServiceImpl.class.getName());
	public static final String SHOPIFYREQUEST = "x_";
	public static final String SEPARATOR = "~";
	
	@Autowired
	private TransactionControllerServiceProvider transactionControllerServiceProvider;

	@Override
	public Fields prepareFields(Map<String, String[]> map) throws SystemException {

		Map<String, String> requestMap = new HashMap<String, String>();
		Map<String, String> shopifyRequestMap = new HashMap<String, String>();
		Map<String, String> hashMap = new HashMap<String, String>();
		Fields fields = null;

		boolean shopifyRequest = false;
		String custName="";
		String firstName="";
		String lastName="";
		try {
			for (Entry<String, String[]> entry : map.entrySet()) {
				if (entry.getKey().startsWith(SHOPIFYREQUEST)) {
					hashMap.put(entry.getKey(),
							((String[]) entry.getValue())[0]);
					
					logger.info("Input fields  " + entry.getKey() +"  " +((String[]) entry.getValue())[0]);
					
					/*ShopifyFieldType shopifyFieldType = ShopifyFieldType  TODO
							.getInstance(entry.getKey());
					if(shopifyFieldType==null){
						continue;
					}
					if(entry.getKey().equals("x_amount")){
						shopifyRequestMap.put(shopifyFieldType.getPgName(),Amount.formatAmount((((String[]) entry.getValue())[0]),"356"));
					}else if(entry.getKey().equals("x_currency")){
						shopifyRequestMap.put(shopifyFieldType.getPgName(),Currency.getNumericCode(((String[]) entry.getValue())[0]));
					}else if(entry.getKey().equals(ShopifyFieldType.CUST_FIRST_NAME.getRequestName())){
						firstName = ((String[]) entry.getValue())[0];
					}else if(entry.getKey().equals(ShopifyFieldType.CUST_LAST_NAME.getRequestName())){
						lastName = ((String[]) entry.getValue())[0];
					}else{
					shopifyRequestMap.put(shopifyFieldType.getPgName(),
							((String[]) entry.getValue())[0]);
					}
					shopifyRequest = true;*/
				} else {
					requestMap.put(entry.getKey(),
							((String[]) entry.getValue())[0]);
				}
			}
		} catch (ClassCastException classCastException) {
			logger.error("Exception while preparing fields", classCastException);
		}

		if (shopifyRequest) {
			if(!(firstName.equals(""))){
				custName = firstName;
				if(!(lastName.equals(""))){
					custName = custName + lastName;
				}
				shopifyRequestMap.put(FieldType.CUST_NAME.getName(),custName);
			}
			
		// 	ShopifyUtil.matchHash(hashMap); TODO
			fields = new Fields(shopifyRequestMap);
			fields.removeInternalFields();

			fields.put(FieldType.INTERNAL_SHOPIFY_YN.getName(), "Y");
			fields.remove(FieldType.HASH.getName());
			fields.put(FieldType.HASH.getName(), Hasher.getHash(fields));
		}else{
			fields = new Fields(requestMap);
			fields.removeInternalFields();
		}

		return fields;
	}
	
	@Override
	public Fields prepareFieldsMerchantHosted(Map<String, String[]> map) throws SystemException {
		Map<String, String> requestMap = new HashMap<String, String>();
		String payId  = map.get(FieldType.PAY_ID.getName())[0];
		//String hash = map.get(FieldType.HASH.getName())[0];
		String encData = map.get(FieldType.ENCDATA.getName())[0];
		if(StringUtils.isBlank(encData) || StringUtils.isBlank(payId)){
			throw new SystemException(ErrorType.INVALID_INPUT, "Invalid request received");
		}
		Map<String,String> responseMap = transactionControllerServiceProvider.hostedDecrypt(payId, encData);
		String decryptedString = responseMap.get(FieldType.ENCDATA.getName());
		if(StringUtils.isBlank(decryptedString)){
			throw new SystemException(ErrorType.INVALID_INPUT, "ENCDATA Is Not Decrypted");
		}
		String[] fieldArray = decryptedString.split(SEPARATOR);
		
		Fields fields = null;
		for (String entry : fieldArray) {
			String[] namValuePair = entry.split(Constants.EQUATOR);
			if(namValuePair.length==2) {
				requestMap.put(namValuePair[0],namValuePair[1]);
			}else {
				requestMap.put(namValuePair[0],"");
			}
		}
		fields = new Fields(requestMap);
		fields.removeInternalFields();
		return fields;
	}
}
