package com.paymentgateway.pgui.action;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.user.VpaToken;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.oneclick.TokenManager;

/**
 * @author Rahul
 *
 */
public class DeleteVpaAction extends AbstractSecureAction {

	private static final long serialVersionUID = 5255986332106653276L;
	private static Logger logger = LoggerFactory.getLogger(DeleteVpaAction.class.getName());

	@Autowired
	private TokenManager tokenManager;

	@Autowired
	private UserDao userDao;

	private String tokenId;
	private Map<String, VpaToken> vpaTokenMap = new HashMap<String, VpaToken>();

	public String execute() {
		Fields fields = (Fields) sessionMap.get(Constants.FIELDS.getValue());
		try {
			if (null != tokenId) {
				fields.put(FieldType.TOKEN_ID.getName(), getTokenId());
				tokenManager.removeSavedVPA(fields, userDao.findPayId(fields.get(FieldType.PAY_ID.getName())));
				vpaTokenMap = tokenManager.getAllVpa(fields, userDao.findPayId(fields.get(FieldType.PAY_ID.getName())));
				if (vpaTokenMap.isEmpty()) {
					sessionMap.put(Constants.VPA_TOKEN.getValue(), "NA");
					// setVpaTokenMap("NA");
				} else {
					sessionPut(Constants.VPA_TOKEN.getValue(), vpaTokenMap);
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

	public Map<String, VpaToken> getVpaTokenMap() {
		return vpaTokenMap;
	}

	public void setVpaTokenMap(Map<String, VpaToken> vpaTokenMap) {
		this.vpaTokenMap = vpaTokenMap;
	}

}
