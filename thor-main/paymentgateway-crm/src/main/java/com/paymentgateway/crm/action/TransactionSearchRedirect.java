package com.paymentgateway.crm.action;

import com.opensymphony.xwork2.ModelDriven;
import com.paymentgateway.commons.user.TransactionSearch;

public class TransactionSearchRedirect extends AbstractSecureAction implements
		ModelDriven<TransactionSearch> {

	private static final long serialVersionUID = 5763627063996812999L;
	private TransactionSearch transactionSearch = new TransactionSearch();

	@Override
	public String execute() {
		return SUCCESS;
	}

	public TransactionSearch getModel() {
		return transactionSearch;
	}
}
