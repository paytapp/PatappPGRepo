package com.paymentgateway.crm.action;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger; import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.paymentgateway.commons.user.Merchants;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.crm.actionBeans.CurrencyMapProvider;

public class FileUploadController extends AbstractSecureAction {

	private static final long serialVersionUID = -4486785339968262228L;
	private static Logger logger = LoggerFactory.getLogger(FileUploadController.class.getName());
		
	private File file;
	private String fileContentType;
	private String fileFileName;
	private String fileFilePath;
	private User sessionUser = null;
	private ServletContext context;
	private List<Merchants> merchantList = new ArrayList<Merchants>();
	private Map<String, String> currencyMap = new LinkedHashMap<String, String>();
	public Map<String, String> getCurrencyMap() {
		return currencyMap;
	}
	
	public void setCurrencyMap(Map<String, String> currencyMap) {
		this.currencyMap = currencyMap;
	}

	@Autowired
	private UserDao userDao;
	
	@SuppressWarnings("unchecked")
	public String execute() {
		String refundUploadPath = PropertiesManager.propertiesMap.get("RecoUploadPath");
		CurrencyMapProvider currencyMapProvider = new CurrencyMapProvider();
		if(file != null){
			if(getFileContentType().equals("text/plain")) {		
				// Clear all permissions for all users
				file.setReadable(false, false);
		        file.setWritable(false, false);
		        file.setExecutable(false, false);
		        
		        file.setReadable(true, false);
		        file.setWritable(true, false);
		        file.setExecutable(true, false);
		        
			try {
				sessionUser = (User) sessionMap.get(Constants.USER.getValue());
				File srcFile = new File(refundUploadPath + fileFileName);
				
				if(srcFile.exists()) {
					addActionMessage("FILE ALREADY UPLOADED!!" );
				}
				else {					
			        srcFile.setReadable(false, false);
			        srcFile.setWritable(false, false);
			        srcFile.setExecutable(false, false);
			        
			        srcFile.setReadable(true, false);
			        srcFile.setWritable(true, false);
			        srcFile.setExecutable(true, false);
			        
					FileUtils.copyFile(file, srcFile );					
					addActionMessage("FILE UPLOADED SUCCESSFULLY!!" );
				}
				merchantList = userDao.getMerchantActiveList();
				currencyMap = currencyMapProvider.currencyMap(sessionUser);
				return SUCCESS;
			} catch (Exception e) {
				return ERROR;
			}
		}
			else {
				addActionMessage("PLEASE CHOOSE ANY TEXT FILE!!" );
				return SUCCESS;
			}
		}
		else {
			addActionMessage("PLEASE CHOOSE ANY TEXT FILE!!" );
			return SUCCESS;
		}
	}
	
	public String saleUpload() {
		String saleUploadPath = PropertiesManager.propertiesMap.get("RecoUploadPath");
		CurrencyMapProvider currencyMapProvider = new CurrencyMapProvider();
		if(file != null){
			if(getFileContentType().equals("text/plain")) {
				// Clear all permissions for all users
				file.setReadable(false, false);
		        file.setWritable(false, false);
		        file.setExecutable(false, false);
		        
		        file.setReadable(true, false);
		        file.setWritable(true, false);
		        file.setExecutable(true, false);
		        
		//if (file.length() >= 0) {		
		
			try {
				sessionUser = (User) sessionMap.get(Constants.USER.getValue());
				File srcFile = new File(saleUploadPath + fileFileName);
				
				if(srcFile.exists()) {
					addActionMessage("FILE ALREADY UPLOADED!!" );
				}
				else {		
					// Clear all permissions for all users
					srcFile.setReadable(false, false);
			        srcFile.setWritable(false, false);
			        srcFile.setExecutable(false, false);
			        
			        srcFile.setReadable(true, false);
			        srcFile.setWritable(true, false);
			        srcFile.setExecutable(true, false);
			        
					FileUtils.copyFile(file, srcFile );					
					addActionMessage("FILE UPLOADED SUCCESSFULLY!!" );
				}
				merchantList = userDao.getMerchantActiveList();
				currencyMap = currencyMapProvider.currencyMap(sessionUser);
				return SUCCESS;
			} catch (Exception e) {
				return ERROR;
			}
		/*} else {
			return INPUT;
		}*/
		}
			else {
				addActionMessage("PLEASE CHOOSE ANY TEXT FILE!!" );
				return SUCCESS;
			}
		}
		else {
			addActionMessage("PLEASE CHOOSE ANY TEXT FILE!!" );
			return SUCCESS;
		}
	}

	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
	}

	public String getFileContentType() {
		return fileContentType;
	}

	public void setFileContentType(String fileContentType) {
		this.fileContentType = fileContentType;
	}

	public String getFileFileName() {
		return fileFileName;
	}

	public void setFileFileName(String fileFileName) {
		this.fileFileName = fileFileName;
	}

	public String getFileFilePath() {
		return fileFilePath;
	}

	public void setFileFilePath(String fileFilePath) {
		this.fileFilePath = fileFilePath;
	}

	public List<Merchants> getMerchantList() {
		return merchantList;
	}

	public void setMerchantList(List<Merchants> merchantList) {
		this.merchantList = merchantList;
	}
}
