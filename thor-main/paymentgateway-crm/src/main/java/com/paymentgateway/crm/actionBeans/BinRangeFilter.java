package com.paymentgateway.crm.actionBeans;

import org.springframework.stereotype.Service;

import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserType;
import com.paymentgateway.commons.util.BinRangeIssuingBank;
import com.paymentgateway.commons.util.BinRangeMopType;
import com.paymentgateway.commons.util.CardsType;
import com.paymentgateway.commons.util.CrmFieldConstants;


@Service
public class BinRangeFilter {

	public String getPaymentType(User sessionUser, String cardType) {
		if (sessionUser.getUserType().equals(UserType.ADMIN) || sessionUser.getUserType().equals(UserType.SUBADMIN)) {
			if (cardType.equals(CrmFieldConstants.ALL.toString())) {
				return CrmFieldConstants.ALL.toString();
			} else {
				CardsType cardsType = CardsType.getInstance(cardType);
				return cardsType.toString();
			}

		}
		return null;
	}

	public String getMopType(User sessionUser, String mopType) {
		if (sessionUser.getUserType().equals(UserType.ADMIN) || sessionUser.getUserType().equals(UserType.SUBADMIN)) {
			if (mopType.equals(CrmFieldConstants.ALL.toString())) {
				return CrmFieldConstants.ALL.toString();
			} else {
				BinRangeMopType binRangeMopType = BinRangeMopType.getInstance(mopType);
				return binRangeMopType.toString();
			}
		}
		return null;

	}
	public String getIssuerName(User sessionUser, String issuerName) {
		if (sessionUser.getUserType().equals(UserType.ADMIN) || sessionUser.getUserType().equals(UserType.SUBADMIN)) {
			if (issuerName.equals(CrmFieldConstants.ALL.toString())) {
				return CrmFieldConstants.ALL.toString();
			} else {
				BinRangeIssuingBank binRangeIssuingBank = BinRangeIssuingBank.getInstance (issuerName);
				return binRangeIssuingBank.getCode();
			}
		}
		return null;
	}
}
