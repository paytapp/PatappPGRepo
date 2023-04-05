package com.paymentgateway.pg.core.security;

import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.util.Fields;

public interface Authenticator {

	public ErrorType checkLogin(String userId, String password);
	public void authenticate(Fields fields) throws SystemException;
	public User getUserFromPayId(Fields fields) throws SystemException;
	public User getUser(Fields fields);
	public void setUser(User user);
	public void validatePaymentOptions(Fields fields) throws SystemException;
	//public List<ChargingDetails> getSupportedChargingDetailsList();
	public void isUserExists(Fields fields) throws SystemException;
}
