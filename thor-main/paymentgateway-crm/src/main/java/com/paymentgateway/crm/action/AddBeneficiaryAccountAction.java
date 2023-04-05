package com.paymentgateway.crm.action;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.paymentgateway.commons.api.Hasher;
import com.paymentgateway.commons.api.TransactionControllerServiceProvider;
import com.paymentgateway.commons.dao.MerchantInitiatedDirectDao;
import com.paymentgateway.commons.util.Amount;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.StatusType;

/**
 * @author Pooja Pancholi
 *
 */
public class AddBeneficiaryAccountAction extends AbstractSecureAction {

	private static final long serialVersionUID = 2811346574562571717L;

	private static Logger logger = LoggerFactory.getLogger(AddBeneficiaryAccountAction.class.getName());
	
	@Autowired
	private MerchantInitiatedDirectDao merchantInitiatedDirectDao;
	
	@Autowired
	TransactionControllerServiceProvider transactionControllerServiceProvider;
	
	//IMPS
	private String payId;
	private String subMerchantPayId;
	private String beneficiaryName;
	private String beneficiaryAccountNumber;
	private String ifscCode;
	private String mobileNumber;
	private String email;
	private String response;
	private String responseMsg;
	
	//UPI
	private String vpa;

	public String execute() {
		Map<String, String> requestMap = new HashMap<>();
		Map<String, String> respMap;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
		
		try {
			String autoOrderId = "LP" + sdf.format(new Date());
			requestMap.put(FieldType.ORDER_ID.getName(), autoOrderId);
			if (StringUtils.isNotBlank(beneficiaryName)) {
				requestMap.put(FieldType.BENE_NAME.getName(), beneficiaryName);
			}
			if (StringUtils.isNotBlank(beneficiaryAccountNumber)) {
				requestMap.put(FieldType.BENE_ACCOUNT_NO.getName(), beneficiaryAccountNumber);
			}
			if (StringUtils.isNotBlank(ifscCode)) {
				requestMap.put(FieldType.IFSC_CODE.getName(), ifscCode);
			}
			if (StringUtils.isNotBlank(mobileNumber)) {
				requestMap.put(FieldType.PHONE_NO.getName(), mobileNumber);
			}
			if (StringUtils.isNotBlank(vpa)) {
				requestMap.put(FieldType.PAYER_ADDRESS.getName(), vpa);
			}
			if (StringUtils.isNotBlank(subMerchantPayId)) {
				requestMap.put(FieldType.PAY_ID.getName(), subMerchantPayId);
			} else {
				requestMap.put(FieldType.PAY_ID.getName(), payId);
			}
			
			requestMap.put(FieldType.USER_TYPE.getName(), "Merchant Initiated Direct");
			requestMap.put(FieldType.CURRENCY_CODE.getName(), "356");
			requestMap.put(FieldType.HASH.getName(), Hasher.getHash(new Fields(requestMap)));
			Map<String, String> res = merchantInitiatedDirectDao.isBeneAccountNumber(requestMap);
			if(!(res.get("Flag").equalsIgnoreCase("true"))) {
				if (StringUtils.isNotBlank(beneficiaryAccountNumber)) {
					respMap = transactionControllerServiceProvider.impsAddBeneTransferTransact(requestMap);
				}else {
					respMap = transactionControllerServiceProvider.vpaAddBeneTransferTransact(requestMap);
				}
				
				if (respMap.get(FieldType.STATUS.getName()).equalsIgnoreCase("SUCCESS")) {
					setResponse("success");
					setResponseMsg("Beneficiary has been added successfully!");
				} else {
					setResponse("failed");
					
					if(StringUtils.isNotBlank(respMap.get(FieldType.PG_TXN_MESSAGE.getName()))) {
						setResponseMsg(respMap.get(FieldType.PG_TXN_MESSAGE.getName()));
					}else {
						setResponseMsg(respMap.get(FieldType.RESPONSE_MESSAGE.getName()));
					}				
				}
			}else {
				if(res.get("Flag").equalsIgnoreCase("true")) {
					if(res.containsKey("FlagStatus")) {
						if(res.get("FlagStatus").equalsIgnoreCase("false")) {
							setResponse("failed");
							setResponseMsg(res.get(FieldType.RESPONSE_MESSAGE.getName()));
						}
					}else {
						setResponse("success");
					setResponseMsg(res.get(FieldType.RESPONSE_MESSAGE.getName()));
					}
				}
			}
		}catch(Exception e) {
			logger.error("exception " , e);
			setResponse("failed");
			setResponseMsg("Failed Due To System Error");
		}
		
		return SUCCESS;
    }
	public String getPayId() {
		return payId;
	}

	public void setPayId(String payId) {
		this.payId = payId;
	}

	public String getSubMerchantPayId() {
		return subMerchantPayId;
	}
	public void setSubMerchantPayId(String subMerchantPayId) {
		this.subMerchantPayId = subMerchantPayId;
	}
	
	public String getBeneficiaryName() {
		return beneficiaryName;
	}

	public void setBeneficiaryName(String beneficiaryName) {
		this.beneficiaryName = beneficiaryName;
	}

	public String getBeneficiaryAccountNumber() {
		return beneficiaryAccountNumber;
	}

	public void setBeneficiaryAccountNumber(String beneficiaryAccountNumber) {
		this.beneficiaryAccountNumber = beneficiaryAccountNumber;
	}

	public String getIfscCode() {
		return ifscCode;
	}

	public void setIfscCode(String ifscCode) {
		this.ifscCode = ifscCode;
	}

	public String getMobileNumber() {
		return mobileNumber;
	}

	public void setMobileNumber(String mobileNumber) {
		this.mobileNumber = mobileNumber;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getVpa() {
		return vpa;
	}

	public void setVpa(String vpa) {
		this.vpa = vpa;
	}
	public String getResponse() {
		return response;
	}
	public void setResponse(String response) {
		this.response = response;
	}
	public String getResponseMsg() {
		return responseMsg;
	}
	public void setResponseMsg(String responseMsg) {
		this.responseMsg = responseMsg;
	}
	
	
}
