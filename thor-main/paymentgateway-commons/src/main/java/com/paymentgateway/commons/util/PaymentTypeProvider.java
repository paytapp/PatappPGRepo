package com.paymentgateway.commons.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.user.ChargingDetails;
import com.paymentgateway.commons.user.ChargingDetailsDao;

/**
 * @author Neeraj,Rahul
 *
 */
@Service("paymentTypeProvider")
public class PaymentTypeProvider {

	@Autowired
	private StaticDataProvider staticDataProvider;
	
	@Autowired
	private PropertiesManager propertiesManager;

	public PaymentTypeTransactionProvider setSupportedPaymentOptions(String payId){
		
		PaymentTypeTransactionProvider paymentTypeTransactionProvider = new PaymentTypeTransactionProvider();
		
		List<ChargingDetails> chargingDetailsList = new ArrayList<ChargingDetails>();
		
		if (propertiesManager.propertiesMap.get("useStaticData").equalsIgnoreCase("Y")) {
			
			chargingDetailsList = staticDataProvider.getChargingDetailsList(payId);
		}
		else {
			chargingDetailsList = new ChargingDetailsDao().getAllActiveChargingDetails(payId);
		}
		
		paymentTypeTransactionProvider.chargingDetailsList = chargingDetailsList;

		Set<MopType> mopListCC= new HashSet<MopType>();		
		Set<MopType> mopListDC= new HashSet<MopType>();
		Set<MopType> mopListPC= new HashSet<MopType>();
		Set<MopType> mopListCOD= new HashSet<MopType>();
		Set<MopType> mopListCRYPTO= new HashSet<MopType>();
		Set<MopType> mopListAamarpay= new HashSet<MopType>();
		//changes done by vj
		TreeSet<MopType> mopListNB=new TreeSet<MopType>();
		Set<MopType> listNb=new TreeSet<>();
		mopListNB.stream().sorted(java.util.Comparator.comparing(MopType::getName));
		//end
		Set<MopType> mopListWL= new HashSet<MopType>();
		Set<MopType> mopListEMCC= new HashSet<MopType>();
		Set<MopType> mopListEMDC= new HashSet<MopType>();
		Set<MopType> mopListCards= new HashSet<MopType>();
		Set<MopType> mopListMqr = new HashSet<MopType>();
		Set<MopType> mopListUpi= new HashSet<MopType>();
		Set<MopType> mopListAtl= new HashSet<MopType>();
		for(ChargingDetails chargingDetails:chargingDetailsList){
			//to exclude refund entries
			if(chargingDetails.getTransactionType()!=null && chargingDetails.getTransactionType().getCode().equals(TransactionType.REFUND.getCode())){
				continue;
			}
			PaymentType paymentType = chargingDetails.getPaymentType();
			switch(paymentType){
			case CREDIT_CARD:
				mopListCC.add(chargingDetails.getMopType());
				mopListCards.add(chargingDetails.getMopType());
				break;
			case DEBIT_CARD:
				mopListDC.add(chargingDetails.getMopType());
				mopListCards.add(chargingDetails.getMopType());
				break;
			case NET_BANKING:
				mopListNB.add(chargingDetails.getMopType());
				break;
			case WALLET:
				mopListWL.add(chargingDetails.getMopType());
				break;
			case UPI:
				mopListUpi.add(chargingDetails.getMopType());
				break;
			case AD:
				mopListAtl.add(chargingDetails.getMopType());
				break;	
			case PREPAID_CARD:
				mopListPC.add(chargingDetails.getMopType());
				break;
			case COD:
				mopListCOD.add(chargingDetails.getMopType());
				break;
			case CRYPTO:
				mopListCRYPTO.add(chargingDetails.getMopType());
				break;
			case EMI_CC:
				mopListEMCC.add(chargingDetails.getMopType());
				break;
			case EMI_DC:
				mopListEMDC.add(chargingDetails.getMopType());
				break;
			case AAMARPAY:
				mopListAamarpay.add(chargingDetails.getMopType());
				break;
			case MQR:
				mopListMqr.add(chargingDetails.getMopType());
				break;
			default:
				break;
			}
		}
		Map<String, Object> treeMap = new TreeMap<String, Object>();
		if(mopListCC.size()!=0){
			treeMap.put(PaymentType.CREDIT_CARD.getCode(), mopListCC);			
		}
		if(mopListDC.size()!=0){
			treeMap.put(PaymentType.DEBIT_CARD.getCode(), mopListDC);
		}
		if(mopListPC.size()!=0){
			treeMap.put(PaymentType.PREPAID_CARD.getCode(), mopListDC);
		}
		if(mopListNB.size()!=0){
			treeMap.put(PaymentType.NET_BANKING.getCode(), mopListNB);
		}
		if(mopListWL.size()!=0){
			treeMap.put(PaymentType.WALLET.getCode(), mopListWL);
		}
		if(mopListUpi.size()!=0){
			treeMap.put(PaymentType.UPI.getCode(), mopListUpi);
		}
		if(mopListAtl.size()!=0){
			treeMap.put(PaymentType.AD.getCode(), mopListAtl);
		}
		if(mopListCOD.size()!=0){
			treeMap.put(PaymentType.COD.getCode(), mopListCOD);
		}
		if(mopListCRYPTO.size()!=0){
			treeMap.put(PaymentType.CRYPTO.getCode(), mopListCRYPTO);
		}
		if(mopListEMCC.size()!=0){
			treeMap.put(PaymentType.EMI_CC.getCode(), mopListEMCC);
		}
		if(mopListEMDC.size()!=0){
			treeMap.put(PaymentType.EMI_DC.getCode(), mopListEMDC);
		}
		if(mopListCards.size()!=0){
			paymentTypeTransactionProvider.supportedCardTypeMap.put(PaymentType.CREDIT_CARD.getCode(), mopListCards);
		}
		if(mopListAamarpay.size()!=0){
			treeMap.put(PaymentType.AAMARPAY.getCode(), mopListAamarpay);
		}
		if (mopListMqr.size() != 0) {
			treeMap.put(PaymentType.MQR.getCode(), mopListMqr);
		}
		paymentTypeTransactionProvider.setSupportedPaymentTypeMap(treeMap);
		return paymentTypeTransactionProvider ; 
		
	}

}


