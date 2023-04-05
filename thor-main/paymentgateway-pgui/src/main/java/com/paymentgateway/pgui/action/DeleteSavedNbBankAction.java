package com.paymentgateway.pgui.action;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.paymentgateway.commons.user.NBToken;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.oneclick.TokenManager;

public class DeleteSavedNbBankAction extends AbstractSecureAction {

	
	private static final long serialVersionUID = 2381916597419642461L;
	private static Logger logger = LoggerFactory.getLogger(DeleteSavedNbBankAction.class.getName());

	@Autowired
	private TokenManager tokenManager;

	@Autowired
	private UserDao userDao;

	private String tokenId;
	private Map<String, NBToken> nbTokenMap = new HashMap<String, NBToken>();

	public String execute() {
		Fields fields = (Fields) sessionMap.get(Constants.FIELDS.getValue());
		try {
			if (null != tokenId) {
				fields.put(FieldType.TOKEN_ID.getName(), getTokenId());
				tokenManager.removeSavedNbBank(fields, userDao.findPayId(fields.get(FieldType.PAY_ID.getName())));
				nbTokenMap = tokenManager.getAllBank(fields, userDao.findPayId(fields.get(FieldType.PAY_ID.getName())));
				if (nbTokenMap.isEmpty()) {
					sessionMap.put(Constants.NB_TOKEN.getValue(), "NA");
					// setVpaTokenMap("NA");
				} else {
					sessionPut(Constants.NB_TOKEN.getValue(), nbTokenMap);
				}

			}
			return SUCCESS;
		} catch (Exception exception) {
			logger.error("Exception", exception);
			return ERROR;
		}

	}

	public String getTokenId() {
		return tokenId;
	}

	public void setTokenId(String tokenId) {
		this.tokenId = tokenId;
	}

	public Map<String, NBToken> getNbTokenMap() {
		return nbTokenMap;
	}

	public void setNbTokenMap(Map<String, NBToken> nbTokenMap) {
		this.nbTokenMap = nbTokenMap;
	}
	
	
}