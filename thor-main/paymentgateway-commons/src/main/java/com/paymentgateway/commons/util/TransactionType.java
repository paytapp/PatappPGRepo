package com.paymentgateway.commons.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.paymentgateway.commons.user.MopTransaction;

public enum TransactionType {

	NEWORDER			(0,"NEWORDER", "NEW",true),
	AUTHORISE			(1,"AUTHORISE", "AUTH",false),
	SALE				(2,"SALE", "SALE",false),
	CAPTURE				(3,"CAPTURE", "CAPT",false),
	SETTLE				(4,"SETTLE", "SETT",false),
	ENROLL				(5,"ENROLL", "ENRO",false),
	STATUS				(6,"STATUS", "STAT",true),
	REFUND				(7,"REFUND", "REFU",false), 
	ENQUIRY				(8,"ENQUIRY", "ENQY",true),
	INVALID				(9,"INVALID", "INV",false),
	RECO				(10,"RECO", "RECO",false),
	REFUNDRECO			(11,"REFUNDRECO", "RREF",false),
	VERIFY				(12,"VERIFY", "VERI",false),
	POSREQUEST			(13,"POSREQUEST", "POSR",false),
	DELIVERY			(14, "DELIVERY", "DELI", false),
	REGISTRATION		(15, "REGISTRATION", "REG", false);
	
	private final String name;
	private final String code;
	private final int id;
	private final boolean isInternal;
	private TransactionType(int id,String name, String code,boolean isInternal){
		this.name = name;
		this.code = code;
		this.id = id;
		this.isInternal = isInternal;
		
	}

	public String getName() {
		return name;
	}
	
	public String getCode(){
		return code;
	}
	public int getId() {
		return id;
	}

	public boolean isInternal() {
		return isInternal;
	}
	
	public static TransactionType getInstance(String name){
		TransactionType[] transactionTypes = TransactionType.values();
		for(TransactionType transactionType : transactionTypes){
			if(transactionType.getName().equals(name)){
				return transactionType;
			}
		}
		
		return null;
	}
	
	public static TransactionType getInstanceFromCode(String code){
		TransactionType[] transactionTypes = TransactionType.values();
		for(TransactionType transactionType : transactionTypes){
			if(transactionType.getCode().equals(code)){
				return transactionType;
			}
		}
		
		return null;
	}

	public static Set<MopTransaction> makeMopTxnSet(String[] txns){
		Set<MopTransaction> moptxns = new HashSet<MopTransaction>();
		
		for(String txnType:txns){
			MopTransaction moptxn = new MopTransaction();
			moptxn.setTransactionType(TransactionType.getInstance(txnType));
			moptxns.add(moptxn);
		}
		return moptxns;
	}
	
	public static List<TransactionType> chargableMopTxn(){
		List<TransactionType> txnTypes = new ArrayList<TransactionType>();
		
		List<String> txnTypeStringList= (List<String>) Helper.parseFields(PropertiesManager.propertiesMap.get("TXNTYPE"));	
					
		for(String txnType:txnTypeStringList){
			TransactionType txnTypeInstance = getInstanceFromCode(txnType);
			txnTypes.add(txnTypeInstance);
		}
		return txnTypes;
	}
	
	public static Map<String , String> chargableMopTxnForpayment(){
		Map<String , String> txnTypes = new HashMap<String , String>();
		
		List<String> txnTypeStringList= (List<String>) Helper.parseFields(PropertiesManager.propertiesMap.get("TXNTYPE"));	
					
		for(String txnType:txnTypeStringList){
			String txnTypeInstance = getInstanceFromCode(txnType).toString();
			txnTypes.put(txnType,txnTypeInstance);
		}
		return txnTypes;
	}
	
	public static List<TransactionType> getTransactionType(){
		List<TransactionType> transactionTypes = new ArrayList<TransactionType>();						
		for(TransactionType transactionType:TransactionType.values()){
			if(!transactionType.isInternal())
				transactionTypes.add(transactionType);
		}
	  return transactionTypes;
	}

}
