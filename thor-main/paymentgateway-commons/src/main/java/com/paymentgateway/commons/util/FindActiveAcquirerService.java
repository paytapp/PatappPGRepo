package com.paymentgateway.commons.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.dao.RouterConfigurationDao;
import com.paymentgateway.commons.user.AccountCurrencyRegion;
import com.paymentgateway.commons.user.CardHolderType;
import com.paymentgateway.commons.user.ChargingDetails;
import com.paymentgateway.commons.user.RouterConfiguration;
import com.paymentgateway.commons.user.User;

@Service
public class FindActiveAcquirerService {
	
	@Autowired
	private RouterConfigurationDao routerConfigurationDao;
	
	AcquirerType getAcquirer(Map<String,String> fields, User user, List<ChargingDetails>  paymentOptions) {
		
		String acquirerName = "";
		PaymentType paymentType = PaymentType.getInstanceUsingCode(fields.get(FieldType.PAYMENT_TYPE.getName()));
		String mopType = fields.get(FieldType.MOP_TYPE.getName());
		String currency = fields.get(FieldType.CURRENCY_CODE.getName());
		String paymentTypeCode = fields.get(FieldType.PAYMENT_TYPE.getName());
		String payId = user.getPayId();
		String transactionType = user.getModeType().toString();
		String paymentsRegion = AccountCurrencyRegion.DOMESTIC.toString();
		String cardHolderType = CardHolderType.COMMERCIAL.toString();
		
		
		//TODO Check for amount based routing
		// 01 = 0.01 to 2000.00 
		// 02 = 2000.01 and above
		
		String slabValue = "";
		
		String  checkAmountString =PropertiesManager.propertiesMap.get(Constants.SWITCH_ACQUIRER_AMOUNT.getValue()); 
		
		// If no slab is set for this merchant , create a default slab with 00 as ID and limit from 0.01 to 1000000.00
		if (!checkAmountString.contains(payId)) {
			checkAmountString = "00-0.01-1000000.00-"+payId+"-ALL";
		}
		
		String[] checkAmountArray = checkAmountString.split(",");
		double transactionAmount = Double.valueOf(fields.get(FieldType.AMOUNT.getName()));
		
		for (String amountSlab : checkAmountArray) {
			
			if (!amountSlab.contains(payId)) {
				continue;
			}
			
			String[] amountSlabArray = amountSlab.split(",");
			
			double minTxnAmount = Double.valueOf(amountSlabArray[1]);
			double maxTxnAmount = Double.valueOf(amountSlabArray[2]);
			String paymentTypeSlab = "ALL";
			
			if (!StringUtils.isBlank(amountSlabArray[4])) {
				paymentTypeSlab = amountSlabArray[4];
			}
			
			
			if (!paymentTypeSlab.equalsIgnoreCase(paymentType.getCode())) {
				
				slabValue = "00";
				minTxnAmount =  Double.valueOf("0.01");
				maxTxnAmount =  Double.valueOf("1000000.00");
				 
			}
			
			if (transactionAmount >= minTxnAmount && transactionAmount <= maxTxnAmount) {
				slabValue = amountSlabArray[0];
			}
		}
		
		/*if (slabValue.equals("")) {
			slabValue = "01";
		}*/
		
		if(StringUtils.isEmpty(fields.get(FieldType.PAYMENT_TYPE.getName())) || StringUtils.isEmpty(mopType) || StringUtils.isEmpty(currency)){
			return null;
		}
					
		
		if(StringUtils.isEmpty(fields.get(FieldType.PAYMENT_TYPE.getName())) || StringUtils.isEmpty(mopType) || StringUtils.isEmpty(currency)){
			return null;
		}
					
		String identifier = payId + currency + paymentTypeCode + mopType + transactionType+paymentsRegion+cardHolderType+slabValue;
		
		List<RouterConfiguration> rulesList = new ArrayList<RouterConfiguration>();
		rulesList = routerConfigurationDao.findActiveAcquirersByIdentifier(identifier);

		if (rulesList.size() > 1) {

			for (RouterConfiguration routerConfiguration : rulesList) {

				acquirerName = AcquirerType.getAcquirerName(routerConfiguration.getAcquirer());

			}
		} else {

			for (RouterConfiguration routerConfiguration : rulesList) {
				acquirerName = AcquirerType.getAcquirerName(routerConfiguration.getAcquirer());

			}

	}
		return null;
	
	}

}
