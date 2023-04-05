package com.paymentgateway.commons.util;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.api.FileModifiedWatcher;

@Service("propertiesManager")
public class PropertiesManager {

	private static Logger logger = LoggerFactory.getLogger(PropertiesManager.class.getName());
	private static final String fileLocation = System.getenv("DTECH_PROPS");
	private static final String ymlFileLocation = System.getenv("DTECH_PROPS");
	private static final String saltPropertiesFile = "salt.properties";
	private static final String saltEncPropertiesFile = "saltEnc.properties";
	private static final String keyPropertiesFile = "key.properties";
	private static final String currencyFile = "currency.properties";
	private static final String currencyAlphabaticToNumericFile = "alphabatic-to-numeric.properties";
	private static final String emailPropertiesFile = "emailer.properties";
	public static Map<String, String> saltStore = new HashMap<String, String>();
	public static Map<String, String> saltEncStore = new HashMap<String, String>();
	private static final String currencyNameFile = "alphabatic-currencycode.properties";
	private static final String amexPropertiesFile = "amex.properties";
	private static final String subUserPermissionPropertiesFile = "subUserPermission.properties";
	private static final String subAdminPermissionPropertiesFile = "subAdminPermission.properties";
	private static final String recurringPaymentsPropertiesFile = "citrusPayRecurringPayments.properties";
	private static final String industrySubcategoryPropertiesFile = "industry_sub_category.properties";
	private static final String industryCategoryPropertiesFile = "industry_category.properties";
	private static final String pendingRequestMessagesPropertiesFile = "pendingRequestMessages.properties";
	private static final String testPropertiesFile = "test.properties";
	public static Map<String, String> propertiesMap = new HashMap<String, String>();
	public static Map<String, String> industryCategoryMap = new HashMap<String, String>();
	ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(500);
	
	public PropertiesManager() {
		if (propertiesMap.size() < 1) {

			logger.info("Getting values from application.yml");
			YamlPropertiesFactoryBean yaml = new YamlPropertiesFactoryBean();
			yaml.setResources(new FileSystemResource(System.getenv("DTECH_PROPS") + "application.yml"));
			Properties configProperty = yaml.getObject();
			Set<Object> keys = configProperty.keySet();

			for (Object key : keys) {
				PropertiesManager.propertiesMap.put((String) key, configProperty.getProperty((String) key));
			}
			FileModifiedWatcher.init(ymlFileLocation + "application.yml", 600);
		}
		// Initialize poller to update properties file when application.yml is updated
	}

	public static Map<String, String> getAllIndustryCategories() {
		
		if (!industryCategoryMap.isEmpty()) {
			return industryCategoryMap;
		}
		else {
			Map<String, String> categories = new LinkedHashMap<String, String>();
			categories = new PropertiesManager().getAllProperties(industryCategoryPropertiesFile);
			industryCategoryMap.putAll(categories);
			return categories;
			
		}
		
	}

	
	public String getKey(String key) {
		return getProperty(key, keyPropertiesFile);
	}

	public String getSystemProperty(String key) {
		return propertiesMap.get(key);
	}

	public String getEmailProperty(String key) {
		return getProperty(key, emailPropertiesFile);
	}

	public String getResetPasswordProperty(String key) {
		return getProperty(key, emailPropertiesFile);
	}

	
	
	public String getAmexProperty(String key) {
		return getProperty(key, amexPropertiesFile);
	}

	public String getSubUserPermissionProperty(String key) {
		return getProperty(key, subUserPermissionPropertiesFile);
	}

	public String getSuAdminPermissionProperty(String key) {
		return getProperty(key, subAdminPermissionPropertiesFile);
	}

	public String getAlphabaticCurrencyCode(String numericCurrencyCode) {
		return getProperty(numericCurrencyCode, currencyNameFile);
	}

	public String getNumericCurrencyCode(String alphabeticCode) {
		return getProperty(alphabeticCode, currencyAlphabaticToNumericFile);
	}

	
	public String getCitrusRecurringPaymentProperty(String key) {
		return getProperty(key, recurringPaymentsPropertiesFile);
	}

	public String getIndustrySubcategories(String category) {
		return getProperty(category, industrySubcategoryPropertiesFile);
	}
	
	public String getPendingMessages(String key) {
		return getProperty(key, pendingRequestMessagesPropertiesFile);
	}

	public String getTestParam(String key) {
		return getProperty(key, testPropertiesFile);
	}

	

	public String getSalt(String payId) {
		try {
			String salt = saltStore.get(payId);
			if (null == salt || salt.isEmpty()) {
				salt = getProperty(payId, saltPropertiesFile);
				if (null != salt && !salt.isEmpty()) {
					saltStore.put(payId, salt);
					return salt;
				}
			}
			return salt;
		} catch (Exception e) {
			e.printStackTrace();
			logger.info("Salt not found ", e);
		}
		return null;
	}

	public String getEncSalt(String payId) {
		try {
			String salt = saltEncStore.get(payId);
			if (null == salt || salt.isEmpty()) {
				salt = getProperty(payId, saltEncPropertiesFile);
				if (null != salt && !salt.isEmpty()) {
					saltEncStore.put(payId, salt);
					return salt;
				}
			}
			return salt;
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Salt not found " , e);
		}
		return null;
	}

	public boolean setKey(String key, String value) {
		return setProperty(key, value, keyPropertiesFile);
	}

	protected boolean setProperty(String key, String value, String fileName) {
		boolean result = true;
		Properties prop = new Properties();
		OutputStream output = null;
		try {
			FileInputStream input = new FileInputStream(fileLocation + fileName);
			prop.load(input);
			input.close();
			output = new FileOutputStream(fileLocation + fileName);
			prop.setProperty(key, value);
			prop.store(output, null);
		} catch (IOException ioException) {
			logger.error("Unable to update properties file = " + fileName + ", Details = " + ioException.getMessage(),
					ioException);
			result = false;
		} finally {
			if (output != null) {
				try {
					output.close();
				} catch (IOException ioException) {
					logger.error("Unable to update properties file = " + fileName + ", Details = "
							+ ioException.getMessage(), ioException);
					result = false;
				}
			}
		}
		return result;
	}

	public Map<String, String> getAllProperties(String fileName) {
		Map<String, String> responseMap = new HashMap<String, String>();
		Properties prop = new Properties();
		InputStream input = null;
		try {
			input = new FileInputStream(fileLocation + fileName);
			// load a properties file
			prop.load(input);
			for (Object key : prop.keySet()) {
				responseMap.put(key.toString(), prop.getProperty(key.toString()));
			}
		} catch (IOException ioException) {
			logger.error("Unable to update properties file = " + fileName + ", Details = " + ioException.getMessage(),
					ioException);
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException ioException) {
					logger.error("Unable to update properties file = " + fileName + ", Details = "
							+ ioException.getMessage(), ioException);
				}
			}
		}
		return responseMap;
	}// getAllProperties()

	private String getProperty(String key, String fileName) {
		Properties prop = new Properties();
		InputStream input = null;
		String value = null;
		try {
			input = new FileInputStream(fileLocation + fileName);
			prop.load(input);
			value = prop.getProperty(key);
		} catch (IOException ioException) {
			logger.error("Unable to update properties file = " + fileName + ", Details = " + ioException.getMessage(),
					ioException);
		} catch (NullPointerException npe) {
			logger.error("property file error " , npe);
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException ioException) {
					logger.error("Unable to update properties file = " + fileName + ", Details = "
							+ ioException.getMessage(), ioException);
				}
			}
		}
		propertiesMap.put(key, value);
		return value;
	}


	public static String getSaltpropertiesfile() {
		return saltPropertiesFile;
	}

	public static String getKeypropertiesfile() {
		return keyPropertiesFile;
	}

	public static String getCurrencyfile() {
		return currencyFile;
	}

	public static String getEmailpropertiesfile() {
		return emailPropertiesFile;
	}

	public static String getAmexpropertiesfile() {
		return amexPropertiesFile;
	}

	public static String getSubuserpermissionpropertiesfile() {
		return subUserPermissionPropertiesFile;
	}

	public void executorImpl(Runnable runnable) {
		executor.execute(runnable);
	}
	
	
}