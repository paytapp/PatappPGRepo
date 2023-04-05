package com.paymentgateway.pgui.action.service;

public class PgActionServiceFactory {

	public static ActionService getActionService(){
		return new ActionServiceImpl();
	}
}
