package com.paymentgateway.crm.action;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.paymentgateway.commons.dao.MerchantAcquirerPropertiesDao;
import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.user.AccountCurrencyRegion;
import com.paymentgateway.commons.user.MerchantAcquirerProperties;
import com.paymentgateway.commons.user.Merchants;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.util.Acquirer;
import com.paymentgateway.commons.util.AcquirerType;
import com.paymentgateway.commons.util.CrmFieldType;
import com.paymentgateway.commons.util.CrmValidator;
import com.paymentgateway.commons.util.Currency;
import com.paymentgateway.commons.util.MopType;
import com.paymentgateway.commons.util.PaymentType;
import com.paymentgateway.commons.util.TransactionType;

public class MerchantMappingAction extends AbstractSecureAction {

	@Autowired
	private UserDao userDao;

	@Autowired
	private MerchantAcquirerPropertiesDao merchantAcquirerPropertiesDao;

	@Autowired
	private CrmValidator validator;

	private static Logger logger = LoggerFactory.getLogger(MerchantMappingAction.class.getName());
	private static final long serialVersionUID = 905765909007885886L;

	private int countList;
	public List<PaymentType> paymentList = new ArrayList<PaymentType>();
	public Map<String, Object> mopList = new LinkedHashMap<String, Object>();
	public List<MopType> mopListCC = new ArrayList<MopType>();
	public List<MopType> mopListDC = new ArrayList<MopType>();
	public List<MopType> mopListPC = new ArrayList<MopType>();
	public List<MopType> mopListNB = new ArrayList<MopType>();
	public List<MopType> mopListWL = new ArrayList<MopType>();
	public List<MopType> mopListUPI = new ArrayList<MopType>();
	public List<MopType> mopListMQR = new ArrayList<MopType>();
	public List<MopType> mopListEMICC = new ArrayList<MopType>();
	public List<MopType> mopListCR = new ArrayList<MopType>();
	public List<MopType> mopListEMIDC = new ArrayList<MopType>();
	public List<TransactionType> transList = new ArrayList<TransactionType>();
	public List<Merchants> listMerchant = new ArrayList<Merchants>();
	public List<Acquirer> listAcquirer = new ArrayList<Acquirer>();
	// private Map<String, String> acquirerList = new TreeMap<String, String>();
	private String merchantEmailId;
	private String acquirer;
	private Map<String, String> currencies;

	private boolean international;
	private boolean domestic;
	private boolean commercial;
	private boolean customer;
	private boolean showSave;

	@SuppressWarnings("unchecked")
	public String execute() {

		try {

			setListMerchant(userDao.getMerchantList());
			setListAcquirer(userDao.getAcquirers());
			setAcquirerMerchantProperty(merchantEmailId);
			setShowSave(false);
			if (!(acquirer == null || acquirer.equals(""))) {
				setShowSave(true);
				createList();
				setCountList(PaymentType.values().length);
			}
		} catch (Exception exception) {
			logger.error("Exception", exception);
			return ERROR;
		}
		return INPUT;
	}

	public void validate() {
		if ((validator.validateBlankField(getAcquirer()))) {
		} else if (!(validator.validateField(CrmFieldType.ACQUIRER, getAcquirer()))) {
			addFieldError(CrmFieldType.ACQUIRER.getName(), ErrorType.INVALID_FIELD_VALUE.getResponseMessage());
		}
	}

	public void setAcquirerMerchantProperty(String merchantEmailId) {

		try {

			String merchantPayId = userDao.getPayIdByEmailId(merchantEmailId);

			MerchantAcquirerProperties merchantAcquirerProperties = merchantAcquirerPropertiesDao
					.getMerchantAcquirerProperties(merchantPayId, acquirer);
			if (merchantAcquirerProperties == null) {

				setInternational(false);
				setDomestic(false);
			}

			else if (merchantAcquirerProperties.getPaymentsRegion().equals(AccountCurrencyRegion.ALL)) {
				setInternational(true);
				setDomestic(true);
			} else if (merchantAcquirerProperties.getPaymentsRegion().equals(AccountCurrencyRegion.INTERNATIONAL)) {
				setInternational(true);
				setDomestic(false);
			} else if (merchantAcquirerProperties.getPaymentsRegion().equals(AccountCurrencyRegion.DOMESTIC)) {
				setInternational(false);
				setDomestic(true);
			} else {
				setInternational(false);
				setDomestic(false);
			}

		}

		catch (Exception e) {
			logger.error("Exception in setAcquirerMerchantProperty = ", e);
		}
	}

	private void createList() {
		try {
			AcquirerType acqType = AcquirerType.getInstancefromCode(acquirer);
			List<PaymentType> supportedPaymentTypes = PaymentType.getGetPaymentsFromSystemProp(acquirer);
			setCurrencies(Currency.getAllCurrency());

			switch (acqType) {
			case FSS:
				for (PaymentType pay : supportedPaymentTypes) {
					switch (pay) {
					case CREDIT_CARD:
						mopListCC = (MopType.getFSSCCMops());
						break;
					case DEBIT_CARD:
						mopListDC = (MopType.getFSSDCMops());
						break;
					case NET_BANKING:
						break;
					case WALLET:
						break;
					case UPI:
						mopListUPI = (MopType.getFSSUPIMops());
						break;
					default:
						break;
					}
				}
				break;
			case HDFC:
				for (PaymentType pay : supportedPaymentTypes) {
					switch (pay) {
					case CREDIT_CARD:
						mopListCC = (MopType.getFSSCCMops());
						break;
					case DEBIT_CARD:
						mopListDC = (MopType.getFSSDCMops());
						break;
					case NET_BANKING:
						break;
					case WALLET:
						break;
					case UPI:
						break;
					default:
						break;
					}
				}
				break;
			case ICICI_FIRSTDATA:
				for (PaymentType pay : supportedPaymentTypes) {
					switch (pay) {
					case CREDIT_CARD:
						mopListCC = (MopType.getFIRSTDATACCMops());
						break;
					case DEBIT_CARD:
						mopListDC = (MopType.getFIRSTDATADCMops());
						break;
					case NET_BANKING:
						break;
					case WALLET:
						break;
					default:
						break;
					}
				}
				break;
			case IDFC_FIRSTDATA:
				for (PaymentType pay : supportedPaymentTypes) {
					switch (pay) {
					case CREDIT_CARD:
						mopListCC = (MopType.getFIRSTDATACCMops());
						break;
					case DEBIT_CARD:
						mopListDC = (MopType.getFIRSTDATADCMops());
						break;
					case NET_BANKING:
						break;
					case WALLET:
						break;
					default:
						break;
					}
				}
				break;
			case FEDERAL:
				for (PaymentType pay : supportedPaymentTypes) {
					switch (pay) {
					case CREDIT_CARD:
						mopListCC = (MopType.getFEDERALCCMops());
						break;
					case DEBIT_CARD:
						mopListDC = (MopType.getFEDERALDCMops());
						break;
					case NET_BANKING:
						break;
					case WALLET:
						break;
					case UPI:
						mopListUPI = (MopType.getFEDERALUPIMops());
						break;
					default:
						break;
					}
				}
				break;
			case BOB:
				for (PaymentType pay : supportedPaymentTypes) {
					switch (pay) {
					case CREDIT_CARD:
						mopListCC = (MopType.getBOBCCMops());
						break;
					case DEBIT_CARD:
						mopListDC = (MopType.getBOBDCMops());
						break;
					case NET_BANKING:
						break;
					case WALLET:
						break;
					case UPI:
						mopListUPI = (MopType.getBOBUPIMops());
						break;
					case EMI_CC:
						mopListEMICC = (MopType.getBOBEMCCMops());
						break;
					case EMI_DC:
						mopListEMIDC = (MopType.getBOBEMDCMops());
						break;
					case EMI:
						break;
					default:
						break;
					}
				}
				break;
			case BILLDESK:
				for (PaymentType pay : supportedPaymentTypes) {
					switch (pay) {
					case CREDIT_CARD:
						mopListCC = (MopType.getBILLDESKCCMops());
						break;
					case DEBIT_CARD:
						mopListDC = (MopType.getBILLDESKDCMops());
						break;
					case NET_BANKING:
						mopListNB = (MopType.getBILLDESKNBMops());
						break;
					case WALLET:
						break;
					case UPI:
						break;
					default:
						break;
					}
				}
				break;
			case SAFEXPAY:
				for (PaymentType pay : supportedPaymentTypes) {
					switch (pay) {
					case CREDIT_CARD:
						mopListCC = (MopType.getSAFEXPAYCCMOPS());
						break;
					case DEBIT_CARD:
						mopListDC = (MopType.getSAFEXPAYDCMOPS());
						break;
					case NET_BANKING:
						mopListNB = (MopType.getSAFEXPAYNBMOPS());
						break;
					case WALLET:
						mopListWL = (MopType.getSAFEXPAYWLMOPS());
						break;
					case UPI:
						mopListUPI = (MopType.getSAFEXPAYUPMOPS());
						break;
					default:
						break;
					}
				}
			case ISGPAY:
				for (PaymentType pay : supportedPaymentTypes) {
					switch (pay) {
					case CREDIT_CARD:
						mopListCC = (MopType.getISGPAYCCMops());
						break;
					case DEBIT_CARD:
						mopListDC = (MopType.getISGPAYDCMops());
						break;
					case NET_BANKING:
						mopListNB = (MopType.getISGPAYNBMops());
						break;
					case WALLET:
						break;
					case UPI:
						mopListUPI = (MopType.getISGPAYUPMops());
						break;
					case EMI:
						break;
					default:
						break;
					}
				}
				break;
			case KOTAK:
				for (PaymentType pay : supportedPaymentTypes) {
					switch (pay) {
					case CREDIT_CARD:
						mopListCC = (MopType.getKOTAKCCMops());
						break;
					case DEBIT_CARD:
						mopListDC = (MopType.getKOTAKDCMops());
						break;
					case UPI:
						mopListUPI = (MopType.getKOTAKUPIMops());
						break;
					case NET_BANKING:
						break;
					case WALLET:
						break;
					default:
						break;
					}
				}
				break;
			case IDBIBANK:
				for (PaymentType pay : supportedPaymentTypes) {
					switch (pay) {
					case CREDIT_CARD:
						mopListCC = (MopType.getIDBIBANKCCMops());
						break;
					case DEBIT_CARD:
						mopListDC = (MopType.getIDBIBANKDCMops());
						break;
					case NET_BANKING:
						mopListNB = (MopType.getIDBIBANKNB());
						break;
					case WALLET:
						break;
					case UPI:
						break;
					default:
						break;
					}
				}
				break;
			case FSSPAY:
				for (PaymentType pay : supportedPaymentTypes) {
					switch (pay) {
					case CREDIT_CARD:
						mopListCC = (MopType.getFSSPAYCCMops());
						break;
					case DEBIT_CARD:
						mopListDC = (MopType.getFSSPAYDCMops());
						break;
					case PREPAID_CARD:
						mopListPC = (MopType.getFSSPAYPCMops());
						break;
					case NET_BANKING:
						mopListNB = (MopType.getFSSPAYNBMops());
						break;
					case WALLET:
						mopListWL = (MopType.getFSSPAYWLMops());
						break;
					case UPI:
						mopListUPI = (MopType.getFSSPAYUPIMops());
						break;
					default:
						break;
					}
				}
				break;
			case PAYU:
				for (PaymentType pay : supportedPaymentTypes) {
					switch (pay) {
					case CREDIT_CARD:
						mopListCC = (MopType.getPAYUCCMops());
						break;
					case DEBIT_CARD:
						mopListDC = (MopType.getPAYUDCMops());
						break;
					case PREPAID_CARD:
						break;
					case NET_BANKING:
						mopListNB = (MopType.getPAYUNBMops());
						break;
					case WALLET:
						mopListWL = (MopType.getPAYUWLMops());
						break;
					case UPI:
						mopListUPI = (MopType.getPAYUUPMops());
						break;
					default:
						break;
					}
				}
				break;
			case YESBANKCB:
				for (PaymentType pay : supportedPaymentTypes) {
					switch (pay) {
					case CREDIT_CARD:
						mopListCC = (MopType.getCYBERSOURCECCMops());
						break;
					case DEBIT_CARD:
						mopListDC = (MopType.getCYBERSOURCEDCMops());
						break;
					case UPI:
						mopListUPI = (MopType.getYESBANKCBMops());
						break;
					default:
						break;
					}
				}
				break;
			case AXISBANKCB:
				for (PaymentType pay : supportedPaymentTypes) {
					switch (pay) {
					case CREDIT_CARD:
						mopListCC = (MopType.getAXISCBCCMops());
						break;
					case DEBIT_CARD:
						mopListDC = (MopType.getAXISCBDCMops());
						break;
					case UPI:
						break;
					default:
						break;
					}
				}
				break;
			case IDFCUPI:
				for (PaymentType pay : supportedPaymentTypes) {
					switch (pay) {
					case UPI:
						mopListUPI = (MopType.getIDFCUPIMops());
						break;
					default:
						break;
					}
				}
				break;
			case AXISBANK:
				for (PaymentType pay : supportedPaymentTypes) {
					switch (pay) {
					case UPI:
						mopListUPI = (MopType.getAXISBANKUPIMops());
						break;
					default:
						break;
					}
				}
				break;
			case ICICIUPI:
				for (PaymentType pay : supportedPaymentTypes) {
					switch (pay) {
					case UPI:
						mopListUPI = (MopType.getICICIUPIMops());
						break;
					default:
						break;
					}
				}
				break;
			case AXISMIGS:
				for (PaymentType pay : supportedPaymentTypes) {
					switch (pay) {
					case CREDIT_CARD:
						mopListCC = (MopType.getMIGSCCMops());
						break;
					case DEBIT_CARD:
						mopListDC = (MopType.getMIGSDCMops());
						break;
					case NET_BANKING:
						break;
					case WALLET:
						break;
					default:
						break;
					}
				}
				break;
			case PAYPHI:
				for (PaymentType pay : supportedPaymentTypes) {
					switch (pay) {
					case CREDIT_CARD:
						mopListCC = (MopType.getPAYPHICCMops());
						break;
					case DEBIT_CARD:
						mopListDC = (MopType.getPAYPHIDCMops());
						break;
					case NET_BANKING:
						mopListNB = (MopType.getPAYPHINBMops());
					case WALLET:
						break;
					case UPI:
						mopListUPI = (MopType.getPAYPHIUPMops());
					default:
						break;
					}
				}
				break;
			case CASHFREE:
				for (PaymentType pay : supportedPaymentTypes) {
					switch (pay) {
					case CREDIT_CARD:
						mopListCC = (MopType.getCASHFREECCMOPMops());
						break;
					case DEBIT_CARD:
						mopListDC = (MopType.getCASHFREEDCMOPMops());
						break;
					case NET_BANKING:
						mopListNB = (MopType.getCASHFREENBMops());
						break;
					case WALLET:
						mopListWL = (MopType.getCASHFREEWLMops());
						break;
					case UPI:
						mopListUPI = (MopType.getCASHFREEUPMops());
						break;
					default:
						break;
					}
				}
				break;
			case APEXPAY:
				for (PaymentType pay : supportedPaymentTypes) {
					switch (pay) {
					case CREDIT_CARD:
						mopListCC = (MopType.getAPEXPAYCCMOPMops());
						break;
					case DEBIT_CARD:
						mopListDC = (MopType.getAPEXPAYDCMOPMops());
						break;
					case NET_BANKING:
						mopListNB = (MopType.getAPEXPAYNBMops());
						break;
					case WALLET:
						mopListWL = (MopType.getAPEXPAYWLMops());
						break;
					case UPI:
						mopListUPI = (MopType.getAPEXPAYUPMops());
						break;
					default:
						break;
					}
				}
				break;
				
			case AIRPAY:
				for (PaymentType pay : supportedPaymentTypes) {
					switch (pay) {
					case UPI:
						mopListUPI = (MopType.getAIRPAYUPMOPS());
						break;
					default:
						break;
					}
				}
				break;
			case QAICASH:
				for (PaymentType pay : supportedPaymentTypes) {
					switch (pay) {
					case UPI:
						mopListUPI = (MopType.getQAICASHUPMOPS());
						break;
					case NET_BANKING:
						mopListNB = (MopType.getQAICASHNBMops());
						break;
					default:
						break;
					}
				}
				break;
			case GLOBALPAY:
				for (PaymentType pay : supportedPaymentTypes) {
					switch (pay) {
					case UPI:
						mopListUPI = (MopType.getGLOBALPAYUPMOPS());
						break;
					default:
						break;
					}
				}
				break;
			case GREZPAY:
				for (PaymentType pay : supportedPaymentTypes) {
					switch (pay) {
					case UPI:
						mopListUPI = (MopType.getGREZPAYUPMOPS());
						break;
					default:
						break;
					}
				}
				break;
			case UPIGATEWAY:
				for (PaymentType pay : supportedPaymentTypes) {
					switch (pay) {
					case UPI:
						mopListUPI = (MopType.getUPIGATEWAYUPMOPS());
						break;
					default:
						break;
					}
				}
				break;
			case TOSHANIDIGITAL:
				for (PaymentType pay : supportedPaymentTypes) {
					switch (pay) {
					case UPI:
						mopListUPI = (MopType.getTOSHANIDIGITALUPMOPS());
						break;
					default:
						break;
					}
				}
				break;
			case P2PTSP:
				for (PaymentType pay : supportedPaymentTypes) {
					switch (pay) {

					case MQR:
						mopListMQR = (MopType.getP2PTSPMQRMOP());
						break;
					default:
						break;
					}
				}
				break;
			case RAZORPAY:
				for (PaymentType pay : supportedPaymentTypes) {
					switch (pay) {
					case CREDIT_CARD:
						mopListCC = (MopType.getRAZORPAYCCMOPS());
						break;
					case DEBIT_CARD:
						mopListDC = (MopType.getRAZORPAYDCMOPS());
						break;
					case NET_BANKING:
						mopListNB = (MopType.getRAZORPAYNBMOPS());
						break;
					case WALLET:
						mopListWL = (MopType.getRAZORPAYWLMOPS());
						break;
					case UPI:
						mopListUPI = (MopType.getRAZORPAYUPMOPS());
						break;
					default:
						break;
					}
				}
				break;
				
			case VEPAY:
				for (PaymentType pay : supportedPaymentTypes) {
					switch (pay) {
					case CREDIT_CARD:
						//mopListCC = (MopType.getVEPAYCCMOPMops());
						break;
					case DEBIT_CARD:
						//mopListDC = (MopType.getVEPAYDCMOPMops());
						break;
					case NET_BANKING:
						mopListNB = (MopType.getVEPAYNBMops());
						break;
					case WALLET:
						mopListWL = (MopType.getVEPAYWLMops());
						break;
					case UPI:
						mopListUPI = (MopType.getVEPAYUPMops());
						break;
					default:
						break;
					}
				}
				break;
				
				
				
			case FONEPAISA:
				for (PaymentType pay : supportedPaymentTypes) {
					switch (pay) {
					case CREDIT_CARD:
						mopListCC = (MopType.getFONEPAISACCMOPMops());
					default:
						break;
					}
				}
				break;
				
			case FLOXYPAY:
				for (PaymentType pay : supportedPaymentTypes) {
					switch (pay) {
					case UPI:
						mopListUPI = (MopType.getFLOXYPAYUPMops());
					default:
						break;
					}
				}
				break;
				
			case IPINT:
				for (PaymentType pay : supportedPaymentTypes) {
					switch (pay) {
					case CREDIT_CARD:
						break;
					case DEBIT_CARD:
						break;
					case NET_BANKING:
						break;
					case WALLET:
						break;
					case UPI:
						break;
					case CRYPTO:
						mopListCR = (MopType.getIpintCRMops());
						break;
					default:
						break;
					}
				}
				break;
			default:
				break;
				
			}

			if (mopListCC.size() != 0) {
				mopList.put(PaymentType.CREDIT_CARD.getName(), mopListCC);
			}
			if (mopListDC.size() != 0) {
				mopList.put(PaymentType.DEBIT_CARD.getName(), mopListDC);
			}
			if (mopListPC.size() != 0) {
				mopList.put(PaymentType.PREPAID_CARD.getName(), mopListPC);
			}
			if (mopListUPI.size() != 0) {
				mopList.put(PaymentType.UPI.getName(), mopListUPI);
			}
			if (!(mopListNB.isEmpty())) {
				// Collections.sort(mopListNB);
				mopList.put(PaymentType.NET_BANKING.getName(), mopListNB);
			}
			if (mopListWL.size() != 0) {
				mopList.put(PaymentType.WALLET.getName(), mopListWL);
			}
			if (mopListEMICC.size() != 0) {
				mopList.put(PaymentType.EMI_CC.getName(), mopListEMICC);
			}
			if (mopListEMIDC.size() != 0) {
				mopList.put(PaymentType.EMI_DC.getName(), mopListEMIDC);
			}
			if (mopListCR.size() != 0) {
				mopList.put(PaymentType.CRYPTO.getName(), mopListCR);
			}
			if (mopListMQR.size() != 0) {
				mopList.put(PaymentType.MQR.getName(), mopListMQR);
			}
			transList = TransactionType.chargableMopTxn();

		} catch (Exception e) {
			logger.error("Exception occured in MerchantMappingAction , create List , exception = ", e);
		}

	}

	public String display() {
		return NONE;
	}

	public int getCountList() {
		return countList;
	}

	public void setCountList(int countList) {
		this.countList = countList;
	}

	public List<Merchants> getListMerchant() {
		return listMerchant;
	}

	public void setListMerchant(List<Merchants> listMerchant) {
		this.listMerchant = listMerchant;
	}

	public String getAcquirer() {
		return acquirer;
	}

	public void setAcquirer(String acquirer) {
		this.acquirer = acquirer;
	}

	public Map<String, String> getCurrencies() {
		return currencies;
	}

	public void setCurrencies(Map<String, String> currencies) {
		this.currencies = currencies;
	}

	public String getMerchantEmailId() {
		return merchantEmailId;
	}

	public void setMerchantEmailId(String merchantEmailId) {
		this.merchantEmailId = merchantEmailId;
	}

	public List<Acquirer> getListAcquirer() {
		return listAcquirer;
	}

	public void setListAcquirer(List<Acquirer> listAcquirer) {
		this.listAcquirer = listAcquirer;
	}

	public boolean isInternational() {
		return international;
	}

	public void setInternational(boolean international) {
		this.international = international;
	}

	public boolean isDomestic() {
		return domestic;
	}

	public void setDomestic(boolean domestic) {
		this.domestic = domestic;
	}

	public boolean isCommercial() {
		return commercial;
	}

	public void setCommercial(boolean commercial) {
		this.commercial = commercial;
	}

	public boolean isCustomer() {
		return customer;
	}

	public void setCustomer(boolean customer) {
		this.customer = customer;
	}

	public boolean isShowSave() {
		return showSave;
	}

	public void setShowSave(boolean showSave) {
		this.showSave = showSave;
	}
}
