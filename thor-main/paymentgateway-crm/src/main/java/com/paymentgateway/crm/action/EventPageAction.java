package com.paymentgateway.crm.action;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.paymentgateway.commons.dao.EventPagesDao;
import com.paymentgateway.commons.mongo.MongoInstance;
import com.paymentgateway.commons.user.EventPages;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.TDRStatus;

/**
 * @author Rahul
 *
 */

public class EventPageAction extends AbstractSecureAction {

	private static final long serialVersionUID = 2029518782067064214L;
	private static Logger logger = LoggerFactory.getLogger(IssuerDetailsAction.class.getName());

	/*
	 * @Autowired EventPagesDao eventPageDao;
	 * 
	 * @Autowired MongoInstance mongoInstance;
	 */

	@Autowired
	PropertiesManager propertiesManager;

	@Autowired
	private UserDao userDao;

	@Autowired
	// private IssuerDetailsDao issuerDetailsDao;

	private String merchantId;
	private File csvfile;
	private String payId;
	private String uniqueNo;
	private String name;
	private String emailId;
	private String mobileNo;
	private String address;
	private String amount;
	private String remarks;
	private List<EventPages> aaData = new ArrayList<EventPages>();
	private User sessionUser = new User();
	private BufferedReader br;

	@SuppressWarnings("unchecked")
	public String execute() {
		sessionUser = (User) sessionMap.get(Constants.USER.getValue());
		List<EventPages> eventObjList=new ArrayList<EventPages>();
		String line = "";
		long rowCount = 0;
		int count = 0;
		try {

			if (csvfile != null) {
				br = new BufferedReader(new FileReader(csvfile));
				while ((line = br.readLine()) != null) {
					rowCount++;
					String data[] = line.split(",");
					if (rowCount == 1) {
						continue;
					}

					Date date = new Date();
					EventPages eventPages = new EventPages();
					eventPages.setPayId(payId);
					eventPages.setUniqueNo(data[0]);
					eventPages.setName(data[1]);
					eventPages.setEmailId(data[2]);
					eventPages.setMobileNo(data[3]);
					eventPages.setAddress(data[4]);
					eventPages.setAmount(data[5]);
					eventPages.setRemarks(data[6]);

					eventPages.setCreatedDate(date);
					eventPages.setUpdatedDate(date);
					eventPages.setStatus(TDRStatus.ACTIVE);
					eventPages.setRequestedBy(sessionUser.getEmailId());
					
					eventObjList.add(eventPages);
					
					
					
				}
				//String message = eventPageDao.insertAll(eventObjList);
			}
			
			
			csvfile.delete();

		} catch (Exception exception) {
			logger.error("Exception while uploding Issuer file: ", exception);
		}
		// csvfile=null;
		setAaData(new ArrayList<>(count));
		logger.info("Total Duplicate entries found in file is " + count);
		return SUCCESS;
	}

	/*
	 * public String editEMIDetail() {
	 * issuerDetailsDao.editEmiSlab(getRateOfInterest(), getSlabId(),
	 * getAlwaysOnOff()); setAaData(issuerDetailsDao.getActiveAllEmiSlab()); return
	 * SUCCESS; }
	 */

	/*
	 * public String fetchAllActiveEmiSlab() {
	 * 
	 * setAaData(eventPageDao.getActiveAllEmiSlab()); return SUCCESS; }
	 */

	/*
	 * public String fecthEmiSlabByFilter() {
	 * 
	 * String issuerBank; if(issuerName.equals("ALL")) { issuerBank = "ALL";
	 * 
	 * } else { issuerBank = AcquirerType.getAcquirerName(issuerName); }
	 * 
	 * setAaData(issuerDetailsDao.getAllEmiSlabByPayIdAndIssuerName(payId,
	 * issuerBank)); return SUCCESS; }
	 */

	

	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}

	public String getRemarks() {
		return remarks;
	}

	public String getMerchantId() {
		return merchantId;
	}

	public void setMerchantId(String merchantId) {
		this.merchantId = merchantId;
	}

	public File getCsvfile() {
		return csvfile;
	}

	public void setCsvfile(File csvfile) {
		this.csvfile = csvfile;
	}

	public String getPayId() {
		return payId;
	}

	public void setPayId(String payId) {
		this.payId = payId;
	}

	public String getUniqueNo() {
		return uniqueNo;
	}

	public void setUniqueNo(String uniqueNo) {
		this.uniqueNo = uniqueNo;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEmailId() {
		return emailId;
	}

	public void setEmailId(String emailId) {
		this.emailId = emailId;
	}

	public String getMobileNo() {
		return mobileNo;
	}

	public void setMobileNo(String mobileNo) {
		this.mobileNo = mobileNo;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getAmount() {
		return amount;
	}

	public void setAmount(String amount) {
		this.amount = amount;
	}

	public List<EventPages> getAaData() {
		return aaData;
	}

	public void setAaData(List<EventPages> aaData) {
		this.aaData = aaData;
	}

}
