package com.paymentgateway.crm.action;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.paymentgateway.commons.dao.CheckerMakerDao;
import com.paymentgateway.commons.dao.MPAMerchantDao;
import com.paymentgateway.commons.user.CheckerMaker;
import com.paymentgateway.commons.user.MPAMerchant;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;

public class CheckerMakerAction extends AbstractSecureAction{
	
	private static final long serialVersionUID = -4367210401961027582L;
	
	private static Logger logger = LoggerFactory.getLogger(CheckerMakerAction.class.getName());

	@Autowired
	private CheckerMakerDao CheckerMakerDao;
	@Autowired
	private UserDao userDao;
	@Autowired
	private MPAMerchantDao mpaMerchantDao;
	
	List<CheckerMaker> aaData = new ArrayList<CheckerMaker>();
	
	private String industryCategory;
	private String checkerPayId;
	private String makerPayId; 
	private String checkerName;
	private String makerName;
	
	public String execute() {

			CheckerMaker checkerMaker = new CheckerMaker();
		try {
			List<CheckerMaker> checkerMakerList = CheckerMakerDao.findAllChekerMaker();
			for(CheckerMaker checkermaker : checkerMakerList) {
				if(checkermaker.getIndustryCategory().equalsIgnoreCase(getIndustryCategory()))
					return ERROR;
			}
			List<User> merchantList = userDao.getMerchantsByIndustryCatagory(getIndustryCategory());
			
			List<MPAMerchant> mpaMerchantList = mpaMerchantDao.getMerchantsByIndustryCatagory(getIndustryCategory());
			List<User> subAdminList = userDao.getAllSubAdmin();
			for(User subAdmin : subAdminList) {
				if(subAdmin.getPayId().equals(checkerPayId)) {
					setCheckerName(subAdmin.getFirstName()+ " "+subAdmin.getLastName());
				}else if(subAdmin.getPayId().equals(makerPayId)) {
					setMakerName(subAdmin.getFirstName()+ " "+subAdmin.getLastName());
				}
			}
			
			for(User merchant : merchantList) {
				merchant.setCheckerName(getCheckerName());
				merchant.setMakerName(getMakerName());
				merchant.setMakerPayId(getMakerPayId());
				merchant.setCheckerPayId(getCheckerPayId());
				userDao.update(merchant);
			}
			for(MPAMerchant mpaMerchant : mpaMerchantList) {
				mpaMerchant.setCheckerName(getCheckerName());
				mpaMerchant.setMakerName(getMakerName());
				mpaMerchant.setMakerPayId(getMakerPayId());
				mpaMerchant.setCheckerPayId(getCheckerPayId());
				mpaMerchantDao.update(mpaMerchant);
			}
			
			checkerMaker.setIndustryCategory(getIndustryCategory());
			checkerMaker.setCheckerName(getCheckerName());
			checkerMaker.setMakerName(getMakerName());
			checkerMaker.setMakerPayId(getMakerPayId());
			checkerMaker.setCheckerPayId(getCheckerPayId());

			CheckerMakerDao.create(checkerMaker);

		} catch (Exception ex) {
			logger.error("Exception", ex);
			return ERROR;
		}
		return SUCCESS;
	}
	
	public String editCheckerMaker() {
		
		CheckerMaker checkerMaker = new CheckerMaker();
		try {
			List<User> subAdminList = userDao.getAllSubAdmin();
			List<User> merchantList = userDao.getMerchantsByIndustryCatagory(getIndustryCategory());
			List<MPAMerchant> mpaMerchantList = mpaMerchantDao.getMerchantsByIndustryCatagory(getIndustryCategory());
			
			
			for(User subAdmin : subAdminList) {
				if(subAdmin.getPayId().equals(checkerPayId)) {
					setCheckerName(subAdmin.getFirstName()+ " "+subAdmin.getLastName());
				}else if(subAdmin.getPayId().equals(makerPayId)) {
					setMakerName(subAdmin.getFirstName()+ " "+subAdmin.getLastName());
				}
			}
			
			for(User merchant : merchantList) {
				merchant.setCheckerName(checkerName);
				merchant.setMakerName(makerName);
				merchant.setCheckerPayId(checkerPayId);
				merchant.setMakerPayId(makerPayId);
				userDao.update(merchant);
			}
			for(MPAMerchant mpaMerchant : mpaMerchantList) {
				mpaMerchant.setCheckerName(checkerName);
				mpaMerchant.setMakerName(makerName);
				mpaMerchant.setCheckerPayId(checkerPayId);
				mpaMerchant.setMakerPayId(makerPayId);
				mpaMerchantDao.update(mpaMerchant);
			}
			checkerMaker.setIndustryCategory(getIndustryCategory());
			checkerMaker.setCheckerName(getCheckerName());
			checkerMaker.setMakerName(getMakerName());
			checkerMaker.setMakerPayId(getMakerPayId());
			checkerMaker.setCheckerPayId(getCheckerPayId());

			CheckerMakerDao.update(checkerMaker);
			
		} catch (Exception ex) {
			logger.error("Exception", ex);
			return ERROR;
		}
		return SUCCESS;
	}
	
	public String findAllCheckerMaker() {
		
		try {
			setAaData(CheckerMakerDao.findAllChekerMaker());
			logger.info("List of All checker And Maker "+getAaData().toString());
		} catch (Exception exception) {
			logger.error("Exception", exception);
			return ERROR;
		}
		return SUCCESS;
	}
	
	public String deletCheckerMaker() {
		
		try {
			int result = CheckerMakerDao.deleteByCategory(getIndustryCategory());
			if(result == 0)
				return ERROR;
			else
				setAaData(CheckerMakerDao.findAllChekerMaker());
			logger.info("List of All checker And Maker "+getAaData().toString());
		} catch (Exception exception) {
			logger.error("Exception", exception);
			return ERROR;
		}
		return SUCCESS;
	}
	
	
	
	public List<CheckerMaker> getAaData() {
		return aaData;
	}

	public void setAaData(List<CheckerMaker> aaData) {
		this.aaData = aaData;
	}

	public String getIndustryCategory() {
		return industryCategory;
	}

	public void setIndustryCategory(String industryCategory) {
		this.industryCategory = industryCategory;
	}

	public String getCheckerPayId() {
		return checkerPayId;
	}
	public void setCheckerPayId(String checkerPayId) {
		this.checkerPayId = checkerPayId;
	}
	public String getMakerPayId() {
		return makerPayId;
	}
	public void setMakerPayId(String makerPayId) {
		this.makerPayId = makerPayId;
	}
	public String getCheckerName() {
		return checkerName;
	}
	public void setCheckerName(String checkerName) {
		this.checkerName = checkerName;
	}
	public String getMakerName() {
		return makerName;
	}
	public void setMakerName(String makerName) {
		this.makerName = makerName;
	}
		

}
