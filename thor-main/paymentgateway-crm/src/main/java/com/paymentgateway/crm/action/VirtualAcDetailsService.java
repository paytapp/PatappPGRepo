package com.paymentgateway.crm.action;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.user.VirtualACDetailsObject;
@Service
public class VirtualAcDetailsService {

	@Autowired
	private UserDao userDao;
	
	public List<VirtualACDetailsObject> getVirtualAccountDetails(String payId, String subMerchanntId) {
		
		User user = userDao.findPayId(payId);
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
		List<VirtualACDetailsObject> acDetailsList = new ArrayList<VirtualACDetailsObject>();
		
		//List<User> userList = null;
		List<User> suMerchantList = new ArrayList<User>();
		if(user.isSuperMerchant()) {
			String superName = user.getBusinessName();
			if(StringUtils.isNotBlank(subMerchanntId) && subMerchanntId.equalsIgnoreCase("ALL")) {
				suMerchantList = userDao.getSubMerchantsBySuperPayId(user.getSuperMerchantId());
				
				for(User submerchant : suMerchantList) {
					VirtualACDetailsObject vad = new VirtualACDetailsObject();
					
					vad.setMerchantName(superName);
					vad.setSubMerchantName(submerchant.getBusinessName());
					vad.setVirtualAccountNo(submerchant.getVirtualAccountNo());
					vad.setVirtualIfscCode(submerchant.getVirtualIfscCode());
					vad.setVirtualBeneficiaryName(submerchant.getVirtualBeneficiaryName());
					vad.setCreateDate(dateFormat.format(submerchant.getRegistrationDate()));
					
					acDetailsList.add(vad);
				}
			}else if(StringUtils.isNotBlank(subMerchanntId)){
				User submerchant = userDao.findPayId(subMerchanntId);
				
				VirtualACDetailsObject vad = new VirtualACDetailsObject();
				
				vad.setMerchantName(superName);
				vad.setSubMerchantName(submerchant.getBusinessName());
				vad.setVirtualAccountNo(submerchant.getVirtualAccountNo());
				vad.setVirtualIfscCode(submerchant.getVirtualIfscCode());
				vad.setVirtualBeneficiaryName(submerchant.getVirtualBeneficiaryName());
				vad.setCreateDate(dateFormat.format(submerchant.getRegistrationDate()));
				
				acDetailsList.add(vad);
				
			}
			
			
		}else {
			VirtualACDetailsObject vad = new VirtualACDetailsObject();
			
			vad.setMerchantName(user.getBusinessName());
			vad.setVirtualAccountNo(user.getVirtualAccountNo());
			vad.setVirtualIfscCode(user.getVirtualIfscCode());
			vad.setVirtualBeneficiaryName(user.getVirtualBeneficiaryName());
			vad.setCreateDate(dateFormat.format(user.getRegistrationDate()));
			
			
			acDetailsList.add(vad);
		}
		
		return acDetailsList;
	}
	
	
	
	public List<VirtualACDetailsObject> getAllVirtualAccountDetails() {
		
		List<VirtualACDetailsObject> acDetailsList = new ArrayList<VirtualACDetailsObject>();
		
		List<User> userList = userDao.getAllMerchantList();
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
		for(User user : userList) {
			VirtualACDetailsObject vad = new VirtualACDetailsObject();
			
			vad.setMerchantName(user.getBusinessName());
			vad.setVirtualAccountNo(user.getVirtualAccountNo());
			vad.setVirtualIfscCode(user.getVirtualIfscCode());
			vad.setVirtualBeneficiaryName(user.getVirtualBeneficiaryName());
			vad.setCreateDate(dateFormat.format(user.getRegistrationDate()));
			
			
			acDetailsList.add(vad);
		}
		
		return acDetailsList;
	}
}
