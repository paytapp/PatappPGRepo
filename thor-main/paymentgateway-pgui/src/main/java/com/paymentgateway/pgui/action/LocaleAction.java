package com.paymentgateway.pgui.action;

import java.util.Locale;

import com.opensymphony.xwork2.ActionContext;

/**
 * @author Rahul
 *
 */
public class LocaleAction extends AbstractSecureAction {

	private static final long serialVersionUID = 8138846453007057034L;
	private String defaultLanguage;

	public String execute() {
		Locale locale = ActionContext.getContext().getLocale();
		defaultLanguage = locale.getLanguage();
		return SUCCESS;
	}

	public String getDefaultLanguage() {
		return defaultLanguage;
	}

	public void setDefaultLanguage(String defaultLanguage) {
		this.defaultLanguage = defaultLanguage;
	}
}
