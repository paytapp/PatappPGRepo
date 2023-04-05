package com.paymentgateway.commons.util;

import java.util.ArrayList;
import java.util.List;

public enum DocumentNameConstants {
	ARTICLE_OF_ASSOCIATION				("ArticleOfAssociation"),
	MEMORANDUM_OF_ASSOCIATION			("MemorandumOfAssociation"),
	CERTIFICATION_OF_INCORPORATION		("CertificationOfIncorporation"),
	CERTIFICATION_OF_COMMENCEMENT		("CertificationOfCommencement"),
	PAN_OF_THE_COMPANY					("PANoftheCompany"),
	IDENTIFICATION_DOCUMNETS			("IdentificationDocuments"),
	BOARD_RESOLUTION					("Boardresolution"),
	LIST_PERSONAL_DETAILS				("ListPersonalDetails"),
	ADDRESS_PROOFS						("AddressProofs"),
	BANK_STATEMENT						("BankStatement"),
	
	IDENTIFICATION_DOCUMNETS_ALLPARTNER	("IdentificationDocumentsAllPartner"),
	CERTIFIED_TRUE_COPY					("CertifiedTrueCopy"),
	LIST_OF_PARTNER						("ListOfPartners"),
	PARTNERSHIP_LETTER_SIGNED			("PartnershipLetterSigned"),
	PAN_CARD 							("PANCard"),
	ADDRESS_PROOFS_PARTNERSHIP			("AddressProofsPartnership"),
	IDENTIFICATION_DOCUMNETS_AUTHORIZED	("IdentificationDocumentsAuthorized"),
	BANK_STATEMENT_PARTNERSHIP			("BankStatementPartnership"),
	LICENSE_UNDER_SHOP					("LicenseUnderShop"),
	
	IDENTIFICATION_DOCUMNETS_PROPRIETOR	("IdentificationDocumentsProprietor"),
	PAN_CARD_PROPRIETOR					("PANCardProprietor"),
	PROOF_OF_ENTITY						("ProofofEntity"),
	ADDRESS_PROOFS_PROPRIETOR			("AddressProofsProprietor"),
	BANK_STATEMENT_PROPRIETOR			("BankStatementProprietor"),
	LICENSE_UNDER_SHOP_PROPRIETOR		("LicenseUnderShopProprietor"),

	LAWS 								("Laws"),
	GENERAL_BODY_RESOLUTION 			("GeneralBodyResolution"),
	PAN_CARD_CLUB 						("PANCardClub"),
	
	REGISTRATION_CERTIFICATE 			("RegistrationCertificate"),
	LLPA_AGREEMENT						("LLPAgreement"),
	LIST_OF_PARTNERS 					("ListOfPartners"),
	IDENTIFICATION_DOCUMNETS_LLP     	("IdentificationDocumentsLLP"),
	AUTHORIZATION_LETTER 				("AuthorizationLetter"),
	PAN_CARD_COMPANY					("PANCardCompany"),
	DIN_REGISTRATION					("DINRegistration"),
	ADDRESS_PROOFS_LLP					("AddressProofsLLP"),
	BANK_STATEMENT_LLP					("BankStatementLLP"),
	PAN_OF_LLP							("PANOfLLP"),
	LICENSE_UNDER_SHOP_LLP				("LicenseUnderShopLLP"),
	
	PASSPORT							("Passport"),
	PAN_CARD_RESIDENT					("PANCardResident"),
	DRIVING_LICENSE						("DrivingLicense"),
	BANKERS_VERIFICATION				("BankersVerification"),
	
	PAN_INTIMATION						("PANIntimation"),
	CURRENT_UTILITY_BILL				("CurrentUtilityBill"),
	MUNICIPAL_TAX						("MunicipalTax"),
	EXISTING_BANKSSTATEMENT				("ExistingBanksStatement"),
	EXISTING_BANKS_CERTIFICATE			("ExistingBanksCertificate"),
	INSURANCE_POLICY					("InsurancePolicy"),
	ANYOTHER_DOCUMENT 					("AnyOtherDocument"),
	
	RESOLUTION_FROM_BOARD				("ResolutionFromBoard"),
	SIGNATURE_AND_PHOTO					("SignatureAndPhoto"),
	CERTIFICATE_ISSUED					("CertificateIssued"),
	ATTESTED_COPY_DEED					("AttestedCopyDeed"),
	DUTY_ATTESTED_PAN					("DutyAttestedPan"),
	AUDITED_BALANCE_SHEET				("AuditedBalanceSheet"),
	SALES_TAX							("SalesTax"),
	VOIDED_CHECK						("VoidedCheck"),
	CREDIT_CARD_TURNOVER				("CreditCardTurnover")
	;
	private final String name;
	
	private DocumentNameConstants(String name){
		this.name = name;
	}

	public String getName() {
		return name;
	}
	public static List<String> getDocType(){
		List<String> docTypes = new ArrayList<String>();						
		for(DocumentNameConstants docType:DocumentNameConstants.values()){
			
			docTypes.add(docType.name);
		}
	  return docTypes;
	}
}
