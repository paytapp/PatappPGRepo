
package com.paymentgateway.crm.action;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.InputStream;

import com.paymentgateway.commons.dao.RouterConfigurationDao;
import com.paymentgateway.commons.user.RouterConfiguration;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.util.DateCreater;
import com.paymentgateway.commons.util.MopType;
import com.paymentgateway.commons.util.PDFCreator;
import com.paymentgateway.commons.util.PaymentType;

/**
 * @author Rajit
 */

public class DownloadSmartRouterPDFAction extends AbstractSecureAction {

	private static final long serialVersionUID = -9059913301424555228L;
	private static Logger logger = LoggerFactory.getLogger(DownloadSmartRouterPDFAction.class.getName());
	
	@Autowired
	private PDFCreator pdfCreator;
	
	@Autowired
	private UserDao userDao;
	
	@Autowired
	private RouterConfigurationDao routerConfigurationDao;
	
	private String payId;
	private InputStream fileInputStream;
	private String fileName;
	
	public String execute() {
		
		try {
			logger.info("Inside execute(), DownloadSmartRouterPDFAction");
			Map<String, List<RouterConfiguration>> routerRuleDataMap = new HashMap<String, List<RouterConfiguration>>();
			
			List<RouterConfiguration> activeRouterList = new ArrayList<RouterConfiguration>();
			
			String merchantName = userDao.getBusinessNameByPayId(payId);
			
			activeRouterList = routerConfigurationDao.getActiveRulesByMerchant(payId);
			
			Set<String> identifierKeySet = new HashSet<String>();

			for (RouterConfiguration routerConfiguration : activeRouterList) {

				String identifier = routerConfiguration.getPaymentType() + "-" + routerConfiguration.getMopType() + "-"
						+ routerConfiguration.getMerchant() + "-" + routerConfiguration.getTransactionType() + "-"
						+ routerConfiguration.getCurrency()+ "-"+routerConfiguration.getPaymentsRegion()+ "-"+routerConfiguration.getCardHolderType()+ "-"+routerConfiguration.getSlabId();

				identifierKeySet.add(identifier);
			}

			for (String uniqueKey : identifierKeySet) {
				List<RouterConfiguration> routerConfigurationList = new ArrayList<RouterConfiguration>();

				for (RouterConfiguration routerConfig : activeRouterList) {

					String key = routerConfig.getPaymentType() + "-" + routerConfig.getMopType() + "-"
							+ routerConfig.getMerchant() + "-" + routerConfig.getTransactionType() + "-"
							+ routerConfig.getCurrency()+ "-"+routerConfig.getPaymentsRegion()+ "-"+routerConfig.getCardHolderType()+ "-"+routerConfig.getSlabId();
					
					
					if (key.equalsIgnoreCase(uniqueKey)) {

						String paymentTypeName = PaymentType.getpaymentName(routerConfig.getPaymentType());
						String mopTypeName = MopType.getmopName(routerConfig.getMopType());

						routerConfig.setPaymentTypeName(paymentTypeName);
						routerConfig.setMopTypeName(mopTypeName);

						routerConfig.setStatusName(routerConfig.getStatusName());
						routerConfigurationList.add(routerConfig);
					}

				}
				routerRuleDataMap.put(uniqueKey, routerConfigurationList);
			}
			String currentDate = DateCreater.defaultFromDate();
			fileName = "Smart_Router_"+currentDate+".pdf";
			File file = new File(fileName);
			fileInputStream = pdfCreator.creatorSmartRouterPdf(routerRuleDataMap, file, merchantName);
			return SUCCESS;
		} catch (Exception e) {
			logger.error("Exception caught while creating smart router PDF, " , e);
			return ERROR;
		}
	}
	
	public String getPayId() {
		return payId;
	}
	public void setPayId(String payId) {
		this.payId = payId;
	}
	public InputStream getFileInputStream() {
		return fileInputStream;
	}
	public void setFileInputStream(InputStream fileInputStream) {
		this.fileInputStream = fileInputStream;
	}
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
}
