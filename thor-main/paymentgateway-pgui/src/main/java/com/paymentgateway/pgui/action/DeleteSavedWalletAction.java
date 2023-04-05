package com.paymentgateway.pgui.action;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.user.WLToken;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.oneclick.TokenManager;

public class DeleteSavedWalletAction extends AbstractSecureAction {

	
	private static final long serialVersionUID = -6011112885385742107L;
	private static Logger logger = LoggerFactory.getLogger(DeleteSavedWalletAction.class.getName());

	@Autowired
	private TokenManager tokenManager;

	@Autowired
	private UserDao userDao;

	private String tokenId;
	private Map<String, WLToken> wlTokenMap = new HashMap<String, WLToken>();

	public String execute() {
		Fields fields = (Fields) sessionMap.get(Constants.FIELDS.getValue());
		try {
			if (null != tokenId) {
				fields.put(FieldType.TOKEN_ID.getName(), getTokenId());
				tokenManager.removeSavedWallet(fields, userDao.findPayId(fields.get(FieldType.PAY_ID.getName())));
				wlTokenMap = tokenManager.getAllWallet(fields, userDao.findPayId(fields.get(FieldType.PAY_ID.getName())));
				if (wlTokenMap.isEmpty()) {
					sessionMap.put(Constants.WL_TOKEN.getValue(), "NA");
					// setVpaTokenMap("NA");
				} else {
					sessionPut(Constants.WL_TOKEN.getValue(), wlTokenMap);
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

	public Map<String, WLToken> getWlTokenMap() {
		return wlTokenMap;
	}

	public void setWlTokenMap(Map<String, WLToken> wlTokenMap) {
		this.wlTokenMap = wlTokenMap;
	}
	
	
	
}
	
	
