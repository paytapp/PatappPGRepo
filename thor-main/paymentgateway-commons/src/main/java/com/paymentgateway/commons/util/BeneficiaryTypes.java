package com.paymentgateway.commons.util;

/**
 * @author Shaiwal
 *
 */
public enum BeneficiaryTypes {

	// NodalPaymentTypes
	/*Dealer("Dealer", "Dealer"), 
	Vendor("Vendor", "Vendor"),*/
	Other("O", "Other") ;
	private final String code;
	private final String name;

	private BeneficiaryTypes(String code, String name) {
		this.code = code;
		this.name = name;
	}

	public String getCode() {
		return code;
	}

	public String getName() {
		return name;
	}

	public static BeneficiaryTypes getInstancefromCode(String acquirerCode) {
		BeneficiaryTypes acquirerType = null;

		for (BeneficiaryTypes acquirer : BeneficiaryTypes.values()) {

			if (acquirerCode.equals(acquirer.getCode().toString())) {
				acquirerType = acquirer;
				break;
			}
		}

		return acquirerType;
	}


	public static BeneficiaryTypes getInstancefromName(String acquirerName) {
		BeneficiaryTypes acquirerType = null;

		for (BeneficiaryTypes acquirer : BeneficiaryTypes.values()) {

			if (acquirerName.equals(acquirer.getName())) {
				acquirerType = acquirer;
				break;
			}
		}

		return acquirerType;
	}

}
