package com.paymentgateway.commons.util;

public enum TxnChannel {

	CRM("031", "Crm"), INTERNAL_BATCH_FILE("032", "Internal Batch File"), EXTERNAL_BATCH_FILE("033",
			"External Batch File"), API("034", "Api Txn"), RECURRING("035", "Recurring Txn");
	private final String code;
	private final String value;

	private TxnChannel(String code, String value) {
		this.code = code;
		this.value = value;

	}

	public static TxnChannel getInstance(String value) {
		TxnChannel[] txnChannels = TxnChannel.values();
		for (TxnChannel txnChannel : txnChannels) {
			if (txnChannel.getValue().equals(value)) {
				return txnChannel;
			}
		}
		return null;
	}

	public static TxnChannel getInstanceFromCode(String code) {
		TxnChannel[] txnChannels = TxnChannel.values();
		for (TxnChannel txnChannel : txnChannels) {
			if (txnChannel.getCode().equals(code)) {
				return txnChannel;
			}
		}

		return null;
	}

	public String getCode() {
		return code;
	}

	public String getValue() {
		return value;
	}

}
