package com.paymentgateway.commons.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.paymentgateway.commons.user.MopTransaction;

public enum TxnType {

	SALE(1, "SALE", "SALE", false), 
	REFUND(2, "REFUND", "REFUND", false);

	private final int id;
	private final String name;
	private final String code;
	private final boolean isInternal;

	private TxnType(int id, String name, String code, boolean isInternal) {
		this.name = name;
		this.code = code;
		this.id = id;
		this.isInternal = isInternal;
	}

	public String getName() {
		return name;
	}

	public String getCode() {
		return code;
	}

	public int getId() {
		return id;
	}

	public boolean isInternal() {
		return isInternal;
	}

	public static TxnType getInstance(String name) {
		TxnType[] txnType = TxnType.values();
		for (TxnType transactionType : txnType) {
			if (transactionType.getName().equals(name)) {
				return transactionType;
			}
		}

		return null;
	}

	public static TxnType getInstanceFromCode(String code) {
		TxnType[] txnType = TxnType.values();
		for (TxnType transactionType : txnType) {
			if (transactionType.getCode().equals(code)) {
				return transactionType;
			}
		}

		return null;
	}

	public static Set<MopTransaction> makeMopTxnSet(String[] txns) {
		Set<MopTransaction> moptxns = new HashSet<MopTransaction>();

		for (String txnType : txns) {
			MopTransaction moptxn = new MopTransaction();
			moptxn.setTxnType(TxnType.getInstance(txnType));
			moptxns.add(moptxn);
		}
		return moptxns;
	}

	public static List<TxnType> chargableMopTxn() {
		List<TxnType> txnTypes = new ArrayList<TxnType>();

		List<String> txnTypeStringList = (List<String>) Helper
				.parseFields(PropertiesManager.propertiesMap.get("TXNTYPE"));

		for (String txnType : txnTypeStringList) {
			TxnType txnTypeInstance = getInstanceFromCode(txnType);
			txnTypes.add(txnTypeInstance);
		}
		return txnTypes;
	}

	public static List<TxnType> gettxnType() {
		List<TxnType> txnType = new ArrayList<TxnType>();
		for (TxnType transactionType : TxnType.values()) {
			if (!transactionType.isInternal())
				txnType.add(transactionType);
		}
		return txnType;
	}
}
