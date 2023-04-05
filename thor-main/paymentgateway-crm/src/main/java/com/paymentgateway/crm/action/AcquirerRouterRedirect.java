package com.paymentgateway.crm.action;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.paymentgateway.commons.user.Merchants;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.util.Currency;
import com.paymentgateway.commons.util.MopType;
import com.paymentgateway.commons.util.PaymentType;
import com.paymentgateway.commons.util.TransactionType;

/**
 * @author Puneet
 *
 */
public class AcquirerRouterRedirect extends AbstractSecureAction {
	private static Logger logger = LoggerFactory.getLogger(AcquirerRouterRedirect.class.getName());
	private static final long serialVersionUID = -3118366599992623506L;
	private List<TransactionType> transactionTypeList = new ArrayList<TransactionType>();
	private Map<String, String> currencyMap = Currency.getAllCurrency();
	private List<Merchants> merchantList = new ArrayList<Merchants>();
	private Set<MopType> mopList = new LinkedHashSet<MopType>();
	private Set<PaymentType> paymentTypeList = new LinkedHashSet<PaymentType>(); 
	
	@Autowired
	private UserDao userDao;
	
	public String execute(){
		try {
		transactionTypeList.add(TransactionType.AUTHORISE);
		transactionTypeList.add(TransactionType.SALE);
	    merchantList = userDao.getMerchantActiveList();
		mopList.addAll(MopType.getCCMops());
		mopList.addAll(MopType.getDCMops());
		mopList.addAll(MopType.getUPIMops());
		mopList.addAll(MopType.getNBMops());
		mopList.addAll(MopType.getWLMops());
		mopList.addAll(MopType.getCODMops());
		paymentTypeList.addAll(PaymentType.getAcqRouterPaymentTypeList());
		return SUCCESS;
		} catch(Exception exception){
			logger.error("Exception ", exception);
			return ERROR;
		}
	}

	public Map<String, String> getCurrencyMap() {
		return currencyMap;
	}
	public void setCurrencyMap(Map<String, String> currencyMap) {
		this.currencyMap = currencyMap;
	}

	public List<Merchants> getMerchantList() {
		return merchantList;
	}

	public void setMerchantList(List<Merchants> merchantList) {
		this.merchantList = merchantList;
	}

	public Set<MopType> getMopList() {
		return mopList;
	}

	public void setMopList(Set<MopType> mopList) {
		this.mopList = mopList;
	}

	public Set<PaymentType> getPaymentTypeList() {
		return paymentTypeList;
	}

	public void setPaymentTypeList(Set<PaymentType> paymentTypeList) {
		this.paymentTypeList = paymentTypeList;
	}

	public List<TransactionType> getTransactionTypeList() {
		return transactionTypeList;
	}

	public void setTransactionTypeList(List<TransactionType> transactionTypeList) {
		this.transactionTypeList = transactionTypeList;
	}
	
}
