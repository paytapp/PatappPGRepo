package com.paymentgateway.crm.action;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.ModelDriven;
import com.paymentgateway.commons.user.Merchants;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.CustomPage;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.crm.actionBeans.CustomPageService;


public class CustomPageSaveAction extends AbstractSecureAction implements ModelDriven<CustomPage>{
	
	@Autowired
	private CustomPageService customPageService;
	
	@Autowired
	private UserDao userDao;
	
	private static final long serialVersionUID = 3744099275295426022L;
	private static Logger logger = LoggerFactory.getLogger(CustomPageSaveAction.class.getName());
	
	private CustomPage customPage = new CustomPage();
	private String htmlString;
	private List<Merchants> merchantList = new ArrayList<Merchants>();
//	private boolean allowInvoiceFlag;
	private InputStream fileInputStream;
	private String zipFileName;
	
	User sessionUser = new User();
	
	@SuppressWarnings({ "static-access", "unlikely-arg-type", "unchecked" })
	public String execute() {
		logger.info("inside save custom page for merchant ");
		sessionUser = (User) sessionMap.get(Constants.USER.values());
		User merchant = userDao.findPayId(customPage.getPayId());
		try {
			
		customPageService.saveImage(customPage);
		String htmlString = customPageService.createHtmlForm(customPage);
		customPageService.uploadfilesInImagefolder(customPage);
		
		merchant.setCustomHtmlString(htmlString);
		userDao.update(merchant);
		String fileName = "custom.html";
		customPageService.writeToFile(htmlString,fileName);
		merchantList = userDao.getMerchantActiveList();
		customPage.setResponse("success");
		
		}catch(Exception ex) {
			logger.info("Exception caught : " + ex);
			customPage.setResponse("error");
			return ERROR;
		}
		return SUCCESS;
	}
	
	public String viewPage() throws Exception {
		
		try {
		User merchant = userDao.findPayId(customPage.getPayId());
		
		customPage.setHtmlString(merchant.getCustomHtmlString());
		
		
		} catch (Exception e) {
			logger.error("Exception while writeToFile method called : " , e);
		}
		
		return SUCCESS;
	}
	
	@SuppressWarnings({ "unlikely-arg-type", "static-access" })
	public String downloadCustomPage() {
		logger.info("inside downloadCustomPage : ");
		sessionUser = (User) sessionMap.get(Constants.USER.values());
		User merchant = userDao.findPayId(customPage.getPayId());
		String htmlStringBuilder = merchant.getCustomHtmlString();//customPageService.createHtmlForm(customPage);
		String fileName = "index.html"; //merchant.getBusinessName() + ".html";
		
		try {
			
			File htmlFile = customPageService.writeToFile(htmlStringBuilder,fileName);
	
	// 		putting all images to images folder under customFolder
			customPageService.uploadfilesInImagefolder(customPage);
			
			zipFileName = merchant.getBusinessName() + ".zip";
			fileInputStream = customPageService.createZipFile(htmlFile, zipFileName, customPage.getPayId());
			
		} catch (Exception e) {
			logger.error("Exception Cought while Downloading Custom Page : " , e);
		}
		return SUCCESS;
	}
	
	public String uploadPrivacyOrTnCFiles() {
		try {
			customPage.setTnCFilesLocation(PropertiesManager.propertiesMap.get(Constants.CUSTOM_PAGE_TNC_FILES_LOCATION.getValue()) + customPage.getPayId());

			if (customPage.getMerchantTnCFile() != null) {
				
					customPageService.uploadTnCFile(customPage.getFileName(), customPage.getMerchantTnCFile(), customPage.getTnCFilesLocation());
					
				customPage.setResponse("success");
			}else {
				customPage.setResponse("error");
			}
		
		} catch (Exception e) {
			logger.error("Exception Cought while Uploading TnC or privacy file : " , e);
			customPage.setResponse("error");
			return ERROR;
		}
		return SUCCESS;
	}
	
	public String getZipFileName() {
		return zipFileName;
	}

	public void setZipFileName(String zipFileName) {
		this.zipFileName = zipFileName;
	}

	public InputStream getFileInputStream() {
		return fileInputStream;
	}

	public void setFileInputStream(InputStream fileInputStream) {
		this.fileInputStream = fileInputStream;
	}

	public String getHtmlString() {
		return htmlString;
	}

	public void setHtmlString(String htmlString) {
		this.htmlString = htmlString;
	}
	
	public List<Merchants> getMerchantList() {
		return merchantList;
	}

	public void setMerchantList(List<Merchants> merchantList) {
		this.merchantList = merchantList;
	}

	@Override
	public CustomPage getModel() {
		return customPage;
	}
	
	
}
