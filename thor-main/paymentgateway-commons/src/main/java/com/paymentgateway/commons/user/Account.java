package com.paymentgateway.commons.user;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Proxy;
import org.springframework.util.StringUtils;

import com.paymentgateway.commons.dao.RouterConfigurationDao;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.util.CrmFieldConstants;
import com.paymentgateway.commons.util.DateCreater;
import com.paymentgateway.commons.util.MopType;
import com.paymentgateway.commons.util.PaymentType;
import com.paymentgateway.commons.util.TDRStatus;
import com.paymentgateway.commons.util.TransactionType;

@Entity
@Proxy(lazy= false)@Cache(usage=CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class Account  implements Serializable,Comparable<Account>{

	private static final long serialVersionUID = 1799371834204950674L;

	
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private Long id;
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	private String merchantId;
	private String password;	
	private String acquirerPayId; 
	private boolean primaryStatus;
	private boolean PrimaryNetbankingStatus;
	private String txnKey;
	private String acquirerName;
	@OneToMany(targetEntity=Payment.class,fetch = FetchType.EAGER,cascade = CascadeType.ALL,orphanRemoval=true)
	private Set<Payment> payments = new HashSet<Payment>();

	@Cache(usage=CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
	@OneToMany(targetEntity=AccountCurrency.class,fetch = FetchType.EAGER,cascade = CascadeType.ALL,orphanRemoval=true)
	private Set<AccountCurrency> accountCurrencySet = new HashSet<AccountCurrency>();

	@OneToMany(targetEntity=ChargingDetails.class,fetch = FetchType.EAGER,cascade = CascadeType.ALL)
	private Set<ChargingDetails> chargingDetails = new HashSet<ChargingDetails>();

	@Transient
	private static final String seprator = "-";

	public Account(){
		
	}

	public AccountCurrency getAccountCurrency(String currencyCode) throws SystemException{
		AccountCurrency accountCurrency = null;
		for(AccountCurrency accountCurrencyInst:getAccountCurrencySet()){
			if(accountCurrencyInst.getCurrencyCode().equals(currencyCode)){
				accountCurrency = accountCurrencyInst;
			}
		}		
		return accountCurrency;
	}

	public void disableChargingDetail(String token,RouterConfigurationDao routerConfigDao){       
		
	//	List<ChargingDetails> removedCdList = new ArrayList<ChargingDetails>();
		
        String[] splittedToken = token.split(seprator);
		Set<ChargingDetails> chargingDetailSet = getChargingDetails();
		Iterator<ChargingDetails> chargingDetailIterator = chargingDetailSet.iterator();
		while(chargingDetailIterator.hasNext()){
			ChargingDetails detail = chargingDetailIterator.next();
			if(detail.getStatus().equals(TDRStatus.INACTIVE)){
				continue;
			}

			if(detail.getPaymentType().equals(PaymentType.getInstance(splittedToken[0]))){
				if(detail.getMopType().equals(MopType.getmop(splittedToken[1]))){
					if(splittedToken.length==2){
						detail.setUpdatedDate(new Date());
						detail.setStatus(TDRStatus.INACTIVE);
						routerConfigDao.updateRouterByCD(detail,"removed",true);
					//	removedCdList.add(detail);
						continue;
					}
					if(detail.getTransactionType().equals(TransactionType.getInstanceFromCode(splittedToken[2]))){
						detail.setUpdatedDate(new Date());
						detail.setStatus(TDRStatus.INACTIVE);
						routerConfigDao.updateRouterByCD(detail,"removed",true);
					//	removedCdList.add(detail);
					}
				}
			}			
		}	
		
	//	return removedCdList;
	}

    public String getMappedString(){
    	StringBuilder savedMappingString = new StringBuilder();
    	for(Payment savedPayment:payments){
    		Set<Mop> mops = savedPayment.getMops();
    		for(Mop mop:mops){    			
    			if(!(savedPayment.getPaymentType().getCode().equals(PaymentType.NET_BANKING.getCode()))){
    				Set<MopTransaction> mopTxnSet = mop.getMopTransactionTypes();
    				for(MopTransaction mopTxn:mopTxnSet){
    					TransactionType txnType = mopTxn.getTransactionType();
    					if(!StringUtils.isEmpty(savedMappingString)){
    						savedMappingString.append(CrmFieldConstants.COMMA.getValue());
    					}
    					savedMappingString.append(savedPayment.getPaymentType().getName());
    					savedMappingString.append(seprator);
    					savedMappingString.append(mop.getMopType().getCode());
        				savedMappingString.append(seprator);
        				savedMappingString.append(txnType.getCode());
        			}
    			}else{
    				if(!StringUtils.isEmpty(savedMappingString)){
    					savedMappingString.append(CrmFieldConstants.COMMA.getValue());
    				}
    				savedMappingString.append(savedPayment.getPaymentType().getName());
					savedMappingString.append(seprator);
					savedMappingString.append(mop.getMopType().getCode());
    			}
    		}
    	}
    	return savedMappingString.toString();
	}

    public ChargingDetails getChargingDetails(String date,String paymentType,String mopType,String txnType,String currencyCode){
		//Set<ChargingDetails> chargingDetails = getChargingDetails();
		if(!chargingDetails.isEmpty()){
			Iterator<ChargingDetails> itr = chargingDetails.iterator();
			while(itr.hasNext()) {
				ChargingDetails chargingDetail = itr.next();
				if(null == chargingDetail.getUpdatedDate()) {
					/*if(chargingDetail.getCreatedDate().compareTo(DateCreater.formatStringToDateTime(date)) == -1 && DateCreater.currentDateTime().compareTo(DateCreater.formatStringToDateTime(date)) == 1) {*/
					if(null != chargingDetail.getCreatedDate()) {
						if(DateCreater.formatStringToDateTime(date).getTime() >= chargingDetail.getCreatedDate().getTime()  && DateCreater.formatStringToDateTime(date).getTime() <= DateCreater.currentDateTime().getTime()) {
							if(chargingDetail.getPaymentType().getName().equals(paymentType) && chargingDetail.getMopType().getName().equals(mopType) && chargingDetail.getTransactionType().getName().equals(txnType)) {
								return chargingDetail;
							}
						}
					}
				}
				else {
					if(null != chargingDetail.getCreatedDate()) {
						if(DateCreater.formatStringToDateTime(date).getTime() >= chargingDetail.getCreatedDate().getTime()  && DateCreater.formatStringToDateTime(date).getTime() <= chargingDetail.getUpdatedDate().getTime()) {
							if(DateCreater.currentDateTime() == DateCreater.formatStringToDateTime(date)) {
								if(chargingDetail.getPaymentType().getName().equals(paymentType) && chargingDetail.getMopType().getName().equals(mopType) && chargingDetail.getTransactionType().getName().equals(txnType)) {
									return chargingDetail;
								}
							}
						}
					}
				}												
			}
		}
		
		return null;
	}

    @Override
	public int compareTo(Account compareAccount) {
    	return compareAccount.getAcquirerPayId().compareToIgnoreCase(this.acquirerPayId);
	}
	public String getMerchantId() {
		return merchantId;
	}

	public void setMerchantId(String merchantId) {
		this.merchantId = merchantId;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getAcquirerPayId() {
		return acquirerPayId;
	}

	public void setAcquirerPayId(String acquirerPayId) {
		this.acquirerPayId = acquirerPayId;
	}

	public Set<Payment> getPayments() {
		return payments;
	}

	public void setPayments(Set<Payment> payments) {
		this.payments = payments;
	}
	
	public void addPayment(Payment payment){
		payments.add(payment);
	}
	
	public void removePayment(Payment payment){
		payments.remove(payment);
	}
	
	public void addChargingDetail(ChargingDetails chargingDetail){
		chargingDetails.add(chargingDetail);
	}
	
	public void removeChargingDetail(ChargingDetails chargingDetail){
		chargingDetails.remove(chargingDetail);
	}

	public boolean isPrimaryStatus() {
		return primaryStatus;
	}

	public void setPrimaryStatus(boolean primaryStatus) {
		this.primaryStatus = primaryStatus;
	}

	public boolean isPrimaryNetbankingStatus() {
		return PrimaryNetbankingStatus;
	}

	public void setPrimaryNetbankingStatus(boolean primaryNetbankingStatus) {
		PrimaryNetbankingStatus = primaryNetbankingStatus;
	}

	public String getTxnKey() {
		return txnKey;
	}

	public void setTxnKey(String txnKey) {
		this.txnKey = txnKey;
	}

	public String getAcquirerName() {
		return acquirerName;
	}

	public void setAcquirerName(String acquirerName) {
		this.acquirerName = acquirerName;
	}	
	
	
	public Set<ChargingDetails> getChargingDetails() {
		return chargingDetails;
	}

	public void setChargingDetails(Set<ChargingDetails> chargingDetails) {
		this.chargingDetails = chargingDetails;
	}

	public Set<AccountCurrency> getAccountCurrencySet() {
		return accountCurrencySet;
	}

	public void setAccountCurrencySet(Set<AccountCurrency> accountCurrencySet) {
		this.accountCurrencySet = accountCurrencySet;
	}
	
	public void addAccountCurrency(AccountCurrency accountCurrency){
		accountCurrencySet.add(accountCurrency);
	}
	
	public void removeChargingDetail(AccountCurrency accountCurrency){
		accountCurrencySet.remove(accountCurrency);
	}
	

}
