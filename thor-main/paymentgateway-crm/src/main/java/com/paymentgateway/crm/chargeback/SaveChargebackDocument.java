package com.paymentgateway.crm.chargeback;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger; import org.slf4j.LoggerFactory;

import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.crm.action.AbstractSecureAction;

public class SaveChargebackDocument extends AbstractSecureAction {

	private static final long serialVersionUID = 5656742582073736246L;
	private static Logger logger = LoggerFactory.getLogger(SaveChargebackDocument.class.getName());	
	public String SaveFile(String saveName, String filename, File controlFile,String payId,String documentId ) {
		String destPath;
		String saveFilename;
		
		/// format for storing doc is payid/caseid/document.jpg
		destPath = PropertiesManager.propertiesMap.get("ChargebackCreateFilePath") + payId +"/"+ saveName;
		
		//saveFilename = documentId + getFileExtension(filename);
		saveFilename = saveName + getFileExtension(filename);
		File destFile = new File(destPath, saveFilename);
		try {
			FileUtils.copyFile(controlFile, destFile); 
			
		} catch (Exception exception) {
			logger.error("Exception", exception);
		}
		return SUCCESS;

	}
	private String getFileExtension(String name) {
		if (name.toLowerCase().endsWith(".pdf")) {
			return ".pdf";
		} 
		else if(name.toLowerCase().endsWith(".csv")) {
			return ".csv";
		}
		else if(name.toLowerCase().endsWith(".png")) {
			return ".png";
		}
		
		else {
			return ".jpg";
		}
	}


}
