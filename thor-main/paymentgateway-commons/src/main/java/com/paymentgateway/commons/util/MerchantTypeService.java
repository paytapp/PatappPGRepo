package com.paymentgateway.commons.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.dao.ParentMerchantMappingDao;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.user.ParentMerchantMapping;

@Service
public class MerchantTypeService {

	@Autowired
	private ParentMerchantMappingDao parentMerchantMappingDao;

	private static Logger logger = LoggerFactory.getLogger(MerchantTypeService.class.getName());

	public String getChildMerchantPayId(String payId, String cust_category) throws SystemException {
		logger.info("Finding Child Merchant");
		String childMerchantPayId = "";

		List<ParentMerchantMapping> rulesListMerchant = new ArrayList<ParentMerchantMapping>();
		rulesListMerchant = parentMerchantMappingDao.findActiveMerchantByPayId(payId, cust_category);

		if (rulesListMerchant.size() == 0) {
			logger.info("No Child Merhcant found for parent PayId = " + payId);
		} else if (rulesListMerchant.size() > 1) {
			int randomNumber = getRandomNumber();
			int min = 1;
			int max = 0;
			for (ParentMerchantMapping parentMerchantMapping : rulesListMerchant) {
				int loadPercentage = Integer.parseInt(parentMerchantMapping.getLoad());
				min = 1 + max;
				max = max + loadPercentage;
				if (randomNumber >= min && randomNumber <= max) {
					childMerchantPayId = parentMerchantMapping.getMerchantPayId();
				}
			}
		} else {

			for (ParentMerchantMapping parentMerchantMapping : rulesListMerchant) {
				childMerchantPayId = parentMerchantMapping.getMerchantPayId();
			}
		}
		return childMerchantPayId;
	}

	private static int getRandomNumber() {
		Random rnd = new Random();
		int randomNumber = (int) (rnd.nextInt(100)) + 1;
		return randomNumber;
	}

}
