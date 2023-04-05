package com.paymentgateway.crm.interceptor;

import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger; import org.slf4j.LoggerFactory;
import org.slf4j.Logger; import org.slf4j.LoggerFactory;
import com.opensymphony.xwork2.Action;
import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.AbstractInterceptor;

/**
 * @author Puneet
 *
 */
public class TokenValidationInterceptor extends AbstractInterceptor {

	private static Logger logger = LoggerFactory
			.getLogger(TokenValidationInterceptor.class.getName());
	private static final long serialVersionUID = 3443707353188932224L;
	public static final String FRONT_END_NAME = "token";
	public static final String SERVER_END_NAME = "customToken";
	public static final String INVALID_TOKEN_CODE = "invalid.token";

	@SuppressWarnings("rawtypes")
	@Override
	public String intercept(ActionInvocation invocation) {
		try {
			Map session = ActionContext.getContext().getSession();
			Map<String, Object> params = ActionContext.getContext().getParameters();

			Object tokenObjectFE = params.get(FRONT_END_NAME);
			String tokenFE="";
			if(null!=tokenObjectFE) {
				tokenFE = ((String[]) tokenObjectFE)[0];
			}else {
				tokenFE = (String) session.get(FRONT_END_NAME);
				 if(StringUtils.isBlank(tokenFE)) {
					 return INVALID_TOKEN_CODE;
				 }
			}

			String sessionToken = (String) session.get(SERVER_END_NAME);

			if (!tokenFE.equals(sessionToken)) {
				return INVALID_TOKEN_CODE;
			}
			return invocation.invoke();
		} catch (Exception exception) {
			logger.error("error in token validation interceptor: "+exception);
			return Action.ERROR;
		}
	}
}
