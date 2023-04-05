package com.paymentgateway.crm.actionBeans;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger; import org.slf4j.LoggerFactory;
import org.slf4j.Logger; import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.user.UserType;
import com.paymentgateway.commons.util.CrmFieldConstants;
import com.paymentgateway.commons.util.Currency;

/**
 * @author shashi
 *
 */

@Service
public class CurrencyMapProvider{
	
	
	@Autowired
	private UserDao userDao;
	
	private static Logger logger = LoggerFactory
			.getLogger(CurrencyMapProvider.class.getName());

	public Map<String, String> currencyMap(User user) {
		
		Map<String, String> tempMap;
		String currencyKey = user.getDefaultCurrency();
		Map<String, String> currencyMap = new LinkedHashMap<String, String>();
		if (user.getUserType().equals(UserType.ADMIN)
				|| user.getUserType().equals(UserType.RESELLER) || user.getUserType().equals(UserType.ACQUIRER)
				|| user.getUserType().equals(UserType.SUPERADMIN)||user.getUserType().equals(UserType.SUBADMIN) ||user.getUserType().equals(UserType.AGENT)) {
			tempMap = Currency.getAllCurrency();
			// set currencies
			String strKey = tempMap.get(CrmFieldConstants.INR.getValue());
			if (StringUtils.isBlank(currencyKey)) {
				if (!StringUtils.isBlank(strKey)) {
					tempMap.remove(CrmFieldConstants.INR.getValue());
					currencyMap.put(CrmFieldConstants.INR.getValue(), strKey);
					for (Entry<String, String> entry : tempMap.entrySet()) {
						try {
							currencyMap.put(entry.getKey(), entry.getValue());
						} catch (ClassCastException classCastException) {
							logger.error("Exception", classCastException);
						}
					}
					return currencyMap;
				}
			}
		}else if(user.getUserType().equals(UserType.SUBUSER) || user.getUserType().equals(UserType.SUBACQUIRER)) {
		//	User sessionUser = (User) sessionMap.get(Constants.USER.getValue());
			String parentPayId = user.getParentPayId();
			User parentUser = new UserDao().findPayId(parentPayId);
			if(StringUtils.isNoneBlank(parentUser.getSuperMerchantId())){
				String superMerchantPayId = parentUser.getSuperMerchantId();
				User superMerchantUser = new UserDao().findPayId(superMerchantPayId);
				tempMap = Currency.getSupportedCurreny(superMerchantUser);
			}else{
				tempMap = Currency.getSupportedCurreny(parentUser);
			}
			
			if (StringUtils.isBlank(currencyKey)) {
				return tempMap;
			}
		} 
		else {
			tempMap = Currency.getSupportedCurreny(user);
			if (StringUtils.isBlank(currencyKey)) {
				return tempMap;
			}
		}
		tempMap.remove(currencyKey);
		currencyMap.put(currencyKey, Currency.getAlphabaticCode(currencyKey));
		for (Entry<String, String> entry : tempMap.entrySet()) {
			try {
				currencyMap.put(entry.getKey(), entry.getValue());
			} catch (ClassCastException classCastException) {
				logger.error("Exception", classCastException);
			}
		}

		return currencyMap;
	}
}
