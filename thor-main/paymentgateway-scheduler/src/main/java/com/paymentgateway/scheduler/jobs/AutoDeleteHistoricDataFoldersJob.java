package com.paymentgateway.scheduler.jobs;

import java.io.File;
import java.util.Arrays;

import org.apache.commons.io.FileUtils;
import org.joda.time.DateTime;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;

import com.paymentgateway.commons.util.PropertiesManager;

public class AutoDeleteHistoricDataFoldersJob extends QuartzJobBean {

	@Autowired
	PropertiesManager propertiesManager;
	private Logger logger = LoggerFactory.getLogger(AutoDeleteHistoricDataFoldersJob.class.getName());

	@Override
	protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
		DateTime previousDate1 = new DateTime().minusDays(2);
		String folderDate = previousDate1.toString("yyyy-MM-dd");
		logger.info("Deleting historic data from folders for date >>> " + folderDate);
		deleteHistoricDataFolders(folderDate);
	}

	@SuppressWarnings("static-access")
	private void deleteHistoricDataFolders(String date) {
		try {
			String fileTempLocation = propertiesManager.propertiesMap.get("REPORTS_FILE_LOCATION_URL");
			File fileAdmin = new File(fileTempLocation + "AdminCreated/" + date + "/");
			if (fileAdmin.exists()) {
				FileUtils.deleteDirectory(new File(fileAdmin.toString()));
				logger.info("Data of date " + date + " in Admin directory deleted");
			}
			File fileSubAdmin = new File(fileTempLocation + "SubAdminCreated/" + date + "/");
			if (fileSubAdmin.exists()) {
				FileUtils.deleteDirectory(new File(fileSubAdmin.toString()));
				logger.info("Data of date " + date + " in SubAdmin directory deleted");
			}
			File fileReseller = new File(fileTempLocation + "ResellerCreated/" + date + "/");
			if (fileReseller.exists()) {
				FileUtils.deleteDirectory(new File(fileReseller.toString()));
				logger.info("Data of date " + date + " in Reseller directory deleted");
			}
			File fileMerchant = new File(fileTempLocation + "MerchantCreated/" + date + "/");
			if (fileMerchant.exists()) {
				FileUtils.deleteDirectory(new File(fileMerchant.toString()));
				logger.info("Data of date " + date + " in Merchant directory deleted");
			}
			File fileSubMerchant = new File(fileTempLocation + "SubMerchantCreated/" + date + "/");
			if (fileSubMerchant.exists()) {
				FileUtils.deleteDirectory(new File(fileSubMerchant.toString()));
				logger.info("Data of date " + date + " in SubMerchant directory deleted");
			}
			File fileSuperMerchant = new File(fileTempLocation + "SuperMerchantCreated/" + date + "/");
			if (fileSuperMerchant.exists()) {
				FileUtils.deleteDirectory(new File(fileSuperMerchant.toString()));
				logger.info("Data of date " + date + " in SuperMerchant directory deleted");
			}
			File fileSubUser = new File(fileTempLocation + "SubUserCreated/" + date + "/");
			if (fileSubUser.exists()) {
				FileUtils.deleteDirectory(new File(fileSubUser.toString()));
				logger.info("Data of date " + date + " in SubUser directory deleted");
			}

		} catch (Exception e) {
			logger.error("Exception while deleting data in AutoDeleteHistoricDataFoldersJob >>> ", e);

		}
	}

}
