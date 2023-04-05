package com.paymentgateway.crypto.scrambler;

import com.paymentgateway.commons.exception.SystemException;

public interface ScramblerService {

	public Scrambler getScrambler(String payId) throws SystemException;
	public Scrambler getScramblerWithKey(String keyId);
	public Scrambler getHostedScrambler(String payId) throws SystemException;
}
