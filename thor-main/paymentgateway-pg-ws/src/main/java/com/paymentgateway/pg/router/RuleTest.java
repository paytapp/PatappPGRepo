package com.paymentgateway.pg.router;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

public class RuleTest {
	@Autowired
	private RouterRuleImpl ru;
	public void main(String[] args) {
		
		System.out.println("dao called");
		
	//	RouterRuleDao dao = new RouterRuleDao();
	
		List<RouterRuleImpl> ruleList=new RouterRuleHelper().generateOnUsRule(
				new String[]{"Yes Bank","Hdfc Bank"}, 
				new String[]{"356"},
				new String[]{"AUTHORISE","SALE"},
				new String[]{"CREDIT_CARD","DEBIT_CARD"},
				new String[]{"VISA"});
		
		System.out.println(ruleList.size());
		
		for(RouterRuleImpl rule:ruleList){
		//	dao.create(rule);
		}	
	}
}
