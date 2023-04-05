package com.paymentgateway.pg.router;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.paymentgateway.commons.user.ChargingDetailsDao;


public class RouterRuleHelper {
	
	//first prepare rules for on-us..then later on of-us/*	On-Us hai ya Of-Us hai--wo v rule me append karna hai	below things need fetch from database	first priority TDR basis pe	2nd Primary Secondary	3rd Agar primary Down hai to secondary pe bhejo	4th Agar ham downtime ke according secondary acuirer search karna hai*/
	
	/*
	 * NOTE: - if TDR found null means the combination is selected by UI is invalid/Not exist in ChargingDetail entity.
	 * 
	 */
	@Autowired
	private ChargingDetailsDao chargingDetailDao;
	public void log(Object message){
		System.out.println(message);
	}
	
	public Double getBankTDR(String acquirerName,String mopType,String paymentType,String transactionType){
		
		Double bankTDR=null;
		try{
//		bankTDR=chargingDetailDao.getBankTDR(acquirerName, mopType, paymentType, transactionType);
		}catch(Exception e){
			log(e);
			log("Error with==="+acquirerName+" || "+ mopType+" || "+transactionType+" || "+paymentType);
		}
		
		return bankTDR;
	}
	
	public String getSecondaryAcquirer(){
		String dummySecAcq="DummySecondaryAcquirer";
		//fetch the dummyAcquirer from either property or Database..Yet to be decided
		
		return dummySecAcq;
	}
	
	public List<RouterRuleImpl> generateOnUsRule(String[] acquirerArr, String[] currencyArr, String[] transactionTypeArr,String[] paymentTypeArr, String[] MOPArr) {
		List<RouterRuleImpl> ruleList=new ArrayList<RouterRuleImpl>();
		RouterRuleImpl routerRuleImpl=null;
		for (String acquirer : acquirerArr) {
			for (String currencyCode : currencyArr) {
				for (String transactionType : transactionTypeArr) {
					for (String paymentType : paymentTypeArr) {
						for (String mop : MOPArr) {
							routerRuleImpl = new RouterRuleImpl();
							routerRuleImpl.setAcquirerName(acquirer);
							routerRuleImpl.setCurrencyCode(Integer.parseInt(currencyCode));
							routerRuleImpl.setTransactionType(transactionType);
							routerRuleImpl.setPaymentType(paymentType);
							routerRuleImpl.setMopType(mop);
							routerRuleImpl.setBankTdr(getBankTDR(acquirer, mop, paymentType, transactionType));
							routerRuleImpl.setOnUsOfUs("ON-US");
							ruleList.add(routerRuleImpl);
						}
					}
				}
			}
		}
		return ruleList;
	}
	
	public List<String> generateOfUsRule(String[] acquirerArr, String[] currencyArr, String[] transactionTypeArr,String[] paymentTypeArr, String[] MOPArr) {
		String rule = null;
		List<String> ruleList=new ArrayList<String>();
		String onUsOfUs = "ON-US";

		/*
		 * 
		 * 
		 * 
		 * 
		 * 
		 * 
	
		Generate Of-Us Rule on basis of Secondary Acquirer














		*/
		
		
		
		return ruleList;
	}
	
	

}


