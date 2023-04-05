package com.paymentgateway.crm.actionBeans;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.user.Merchants;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.user.UserType;
import com.paymentgateway.commons.util.CrmFieldConstants;

@Service
public class SessionUserIdentifier {
	
	@Autowired
	private UserDao userDao;

	public String getMerchantPayId(User user, String merchantEmailId) {

		if (user.getUserType().equals(UserType.ADMIN) || user.getUserType().equals(UserType.SUBADMIN)
				|| user.getUserType().equals(UserType.SUPERADMIN)
				|| user.getUserType().equals(UserType.RESELLER) || user.getUserType().equals(UserType.ACQUIRER)
				|| user.getUserType().equals(UserType.SUBACQUIRER)) {
			if ((null == merchantEmailId) || merchantEmailId.equals(CrmFieldConstants.ALL.toString())) {
			
				return CrmFieldConstants.ALL.toString();
			} if ((null == merchantEmailId) || merchantEmailId.equals(CrmFieldConstants.ALL.toString())) {
			
				return CrmFieldConstants.ALL.toString();
			}else {
				User merchant = userDao.findPayIdByEmail(merchantEmailId);
				return merchant.getPayId();
			}
		} else if (user.getUserType().equals(UserType.SUBUSER)) {
			return user.getParentPayId();
		} else {
			return user.getPayId();
		}
	}

	public List<Merchants> getMerchantPayId1(User user, String merchantName) {
		List<Merchants> merchantsList = new ArrayList<Merchants>();
		if (user.getUserType().equals(UserType.ADMIN) || user.getUserType().equals(UserType.SUBADMIN)
				|| user.getUserType().equals(UserType.SUPERADMIN)
				|| user.getUserType().equals(UserType.RESELLER) || user.getUserType().equals(UserType.ACQUIRER)
				|| user.getUserType().equals(UserType.SUBACQUIRER)) {
			if ((null == merchantName) || merchantName.equals(CrmFieldConstants.ALL.toString())) {
				merchantsList = userDao.getMerchantActive(merchantName);
			} else {
				merchantsList = userDao.getMerchantActive(merchantName);
			}
		} /*else if (user.getUserType().equals(UserType.SUBUSER)) {
			return user.getParentPayId();
		} else {
			return user.getPayId();
		}*/
		return merchantsList;
	}

	
	
	// to use payId securely for further operations
	public static String getUserPayId(User user, String payId) {
		switch (user.getUserType()) {
		case ADMIN:
		case SUPERADMIN:
		case ACQUIRER:
		case RESELLER:
		case SUBACQUIRER:
		case SUBADMIN:
			return payId;
		case MERCHANT:
			return user.getPayId();
		case SUBUSER:
			return user.getParentPayId();
		}
		return null;
	}
}