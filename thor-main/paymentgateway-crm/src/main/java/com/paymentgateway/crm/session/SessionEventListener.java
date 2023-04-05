package com.paymentgateway.crm.session;

import java.util.Date;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.CrmFieldConstants;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;

/**
 * @author Surender
 *
 */

public class SessionEventListener implements HttpSessionListener {
	private static Logger logger = LoggerFactory.getLogger(SessionEventListener.class.getName());
	@Autowired
	private SessionTimeoutHandler timeOutHandler;

	@Override
	public void sessionCreated(HttpSessionEvent event) {
		event.getSession().setMaxInactiveInterval(900);
		Date sessionCreationTime = new Date(event.getSession().getCreationTime());
		Date sessionLastAccessedTime = new Date(event.getSession().getLastAccessedTime());
		int sessionMaxInactiveInterval = event.getSession().getMaxInactiveInterval();
		logger.warn("Session: " + event.getSession().getId() + " createTime: " + sessionCreationTime + " lastAccess: "
				+ sessionLastAccessedTime + " with maxInactiveInterval: " + sessionMaxInactiveInterval + " created.");
		logger.warn("Session Token: " + (String) event.getSession().getAttribute("token"));
	}

	// Session Destroyed
	@Override
	public void sessionDestroyed(HttpSessionEvent event) {
		try {
			Object sessionObj = event.getSession();
			HttpSession session = (HttpSession) sessionObj;

			String txnCompleteFlag = null;
			String origTxnId = null;
			Fields fields = null;
			User user;
			UserDao userDao = new UserDao();

			Object fieldsObj = session.getAttribute(Constants.FIELDS.getValue());
			if (null == fieldsObj) {
				// saving state of user activity to database
				Object userObj = session.getAttribute(Constants.USER.getValue());
				if (null != userObj) {
					user = (User) userObj;
					user = userDao.findPayId(user.getPayId());
					String lastActionName = (String) session
							.getAttribute(CrmFieldConstants.LAST_ACTION_NAME.getValue());
					user.setLastActionName(lastActionName);
					userDao.update(user);
					session.invalidate();
					return;
				} else {
					session.invalidate();
					return;
				}

			} else {
				fields = (Fields) fieldsObj;
			}

			Object origTxnIdObj = null;
			origTxnIdObj = session.getAttribute(FieldType.INTERNAL_ORIG_TXN_ID.getName());

			if (null != origTxnIdObj) {
				origTxnId = (String) origTxnIdObj;
				fields.put(FieldType.INTERNAL_ORIG_TXN_ID.getName(), origTxnId);
			} else {
				origTxnId = fields.get(FieldType.INTERNAL_ORIG_TXN_ID.getName());
				// Put txn id as ORIG_TXN_ID if not found
				if (StringUtils.isEmpty(origTxnId)) {
					fields.put(FieldType.INTERNAL_ORIG_TXN_ID.getName(), fields.get(FieldType.TXN_ID.getName()));
				}

			}

			Object txnCompleteFlagObj = session.getAttribute(Constants.TRANSACTION_COMPLETE_FLAG.getValue());

			if (null != txnCompleteFlagObj) {
				txnCompleteFlag = (String) txnCompleteFlagObj;
				fields.put(Constants.TRANSACTION_COMPLETE_FLAG.getValue(), txnCompleteFlag);
			} else {
				fields.put(Constants.TRANSACTION_COMPLETE_FLAG.getValue(), Constants.N_FLAG.getValue());
			}
			timeOutHandler.handleTimeOut(fields);
		} catch (Exception exception) {
			logger.error("Error processing timeout " , exception);
		}
	}
}
