/**
 * Factory class to interact between Action class and DAO class
 */
package com.paymentgateway.crm.actionBeans;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.dao.MerchantSurchargeTextDao;
import com.paymentgateway.commons.user.MerchantSurchargeText;

/**
 * @author Amitosh Aanand
 *
 */
@Service
public class MerchantSurchargeTextFactory {

	@Autowired
	private MerchantSurchargeTextDao merchantSurchargeTextDao;

	private static Logger logger = LoggerFactory.getLogger(MerchantSurchargeTextFactory.class.getName());

	public List<MerchantSurchargeText> getSpecificMerchantSurchargeText(String payId, String paymentType,
			String paymentRegion, String cardHolderType, String mopType, String amountSlab) {

		logger.info("Fetching speicifc merchant surcharge text");

		StringBuilder querry = new StringBuilder("from MerchantSurchargeText MST where ");

		if (StringUtils.isNotBlank(payId) && !(payId.equalsIgnoreCase("ALL"))) {
			querry.append(" MST.payId='" + payId + "' and");
		}
		if (StringUtils.isNotBlank(paymentType) && !(paymentType.equalsIgnoreCase("ALL"))) {
			querry.append(" MST.paymentType='" + paymentType + "' and");
		}
		if (StringUtils.isNotBlank(paymentRegion) && !(paymentRegion.equalsIgnoreCase("ALL"))) {
			querry.append(" MST.paymentRegion='" + paymentRegion + "' and");
		}
		if (StringUtils.isNotBlank(cardHolderType) && !(cardHolderType.equalsIgnoreCase("ALL"))) {
			querry.append(" MST.cardHolderType='" + cardHolderType + "' and");
		}
		if (StringUtils.isNotBlank(mopType) && !(mopType.equalsIgnoreCase("ALL"))) {
			querry.append(" MST.mopType='" + mopType + "' and");
		}
		if (StringUtils.isNotBlank(amountSlab) && !(amountSlab.equalsIgnoreCase("ALL"))) {
			String amountarr[] = amountSlab.split("-");
			querry.append(" MST.minTxnAmount='" + amountarr[0] + "' and MST.maxTxnAmount='" + amountarr[1] + "' and");
		}

		querry.append(" MST.status = 'ACTIVE'");
		return merchantSurchargeTextDao.fetchSpecificSurchargeText(querry.toString());
	}

	public void createEditSurchargeText(String payId, String paymentType, String paymentRegion, String cardHolderType,
			String mopType, String amountSlab, String loginUserEmailId, String surchargeText) {
		logger.info("Creating / Editing surcharge text for PayId: " + payId);
		merchantSurchargeTextDao.createEditSurchargeTextPerPayId(payId, paymentType, paymentRegion, cardHolderType,
				mopType, amountSlab, surchargeText, loginUserEmailId);

	}

	public void deleteSurchargeText(String payId, String paymentType, String paymentRegion, String cardHolderType,
			String mopType, String amountSlab, String loginUserEmailId) {
		logger.info("Deleting surcharge text for PayId: " + payId);
		merchantSurchargeTextDao.deleteSurchargeText(payId, paymentType, paymentRegion, cardHolderType, mopType,
				amountSlab, loginUserEmailId);
	}
}