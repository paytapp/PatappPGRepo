package com.paymentgateway.crm.action;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.paymentgateway.commons.dao.CheckerMakerDao;
import com.paymentgateway.commons.dao.SearchUserService;
import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.user.CheckerMaker;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserType;
import com.paymentgateway.commons.util.BusinessType;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.CrmFieldType;
import com.paymentgateway.commons.util.CrmValidator;
import com.paymentgateway.commons.util.DataEncoder;
import com.paymentgateway.commons.util.MakerCheckerObj;
import com.paymentgateway.commons.util.SubAdmin;

public class SearchSubAdminAction extends AbstractSecureAction {

	/**
	 * @author Neeraj
	 */
	private static final long serialVersionUID = 5929390136391392637L;
	private static Logger logger = LoggerFactory.getLogger(SearchSubAdminAction.class.getName());
	private List<SubAdmin> aaData = new ArrayList<SubAdmin>();
	
	private String emailId;
	private String phoneNo;
	private String industryCategory;
	
	private List<MakerCheckerObj> checkerMakeList =new ArrayList<MakerCheckerObj>();
	
	private Map<String, String> industryTypes = new TreeMap<String, String>();
	
	@Autowired
	private SearchUserService searchUserService;
	@Autowired
	private CheckerMakerDao CheckerMakerDao;
	
	public String execute() {
		User sessionUser = (User) sessionMap.get(Constants.USER.getValue());
		DataEncoder encoder = new DataEncoder();
		try {
			// TODO resolve error
			if ((sessionUser.getUserType().equals(UserType.ADMIN)) || (sessionUser.getUserType().equals(UserType.SUBADMIN))) {
				setAaData(encoder.encodeAgentsObj(searchUserService.getAgentsList(sessionUser.getPayId())));

			}

		} catch (Exception exception) {
			logger.error("Exception", exception);
		}
		return SUCCESS;
	}

	
	public String fetchReviewerAndApproverList() {
		
		List<MakerCheckerObj> checkerMakeListData =new ArrayList<MakerCheckerObj>();
		
		User sessionUser = (User) sessionMap.get(Constants.USER.getValue());
		try {
			// TODO resolve error
			if ((sessionUser.getUserType().equals(UserType.ADMIN))) {
				setAaData(searchUserService.getAgentsList(sessionUser.getPayId()));
				CheckerMaker checkerMaker = null;
				if(industryCategory != null)
					checkerMaker = CheckerMakerDao.findByCategory(industryCategory);
				for(SubAdmin subAdmin : aaData) {
					MakerCheckerObj makerCheckerObj = new MakerCheckerObj();
					if(checkerMaker != null && checkerMaker.getCheckerPayId().equals(subAdmin.getPayId())){
						
					}else if(checkerMaker != null && checkerMaker.getMakerPayId().equals(subAdmin.getPayId())) {
						
					}else {
						
						makerCheckerObj.setPermissionType(subAdmin.getPermissionType());
						makerCheckerObj.setName(subAdmin.getAgentFirstName()+" "+subAdmin.getAgentLastName());
						makerCheckerObj.setPayId(subAdmin.getPayId());
						checkerMakeListData.add(makerCheckerObj);
					}
					
				}
				
			}
			Map<String,String>	industryCategoryLinkedMap = BusinessType.getIndustryCategoryList();
			industryTypes.putAll(industryCategoryLinkedMap);
			setCheckerMakeList(checkerMakeListData);
			
		} catch (Exception exception) {
			logger.error("Exception", exception);
		}
		return SUCCESS;
	}
	
	public void validate() {
		CrmValidator validator = new CrmValidator();

		if (validator.validateBlankField(getEmailId())) {
		} else if (!validator.validateField(CrmFieldType.EMAILID, getEmailId())) {
			addFieldError(CrmFieldType.EMAILID.getName(), ErrorType.INVALID_FIELD.getResponseMessage());
		}

		if (validator.validateBlankField(getPhoneNo())) {
		} else if (!validator.validateField(CrmFieldType.MOBILE, getPhoneNo())) {
			addFieldError("phoneNo", ErrorType.INVALID_FIELD.getResponseMessage());
		}
	}

	
	
	public Map<String, String> getIndustryTypes() {
		return industryTypes;
	}

	public void setIndustryTypes(Map<String, String> industryTypes) {
		this.industryTypes = industryTypes;
	}

	public List<MakerCheckerObj> getCheckerMakeList() {
		return checkerMakeList;
	}

	public void setCheckerMakeList(List<MakerCheckerObj> checkerMakeList) {
		this.checkerMakeList = checkerMakeList;
	}

	public List<SubAdmin> getAaData() {
		return aaData;
	}

	public void setAaData(List<SubAdmin> aaData) {
		this.aaData = aaData;
	}

	public String getEmailId() {
		return emailId;
	}

	public void setEmailId(String emailId) {
		this.emailId = emailId;
	}

	public String getPhoneNo() {
		return phoneNo;
	}

	public void setPhoneNo(String phoneNo) {
		this.phoneNo = phoneNo;
	}

	public String getIndustryCategory() {
		return industryCategory;
	}

	public void setIndustryCategory(String industryCategory) {
		this.industryCategory = industryCategory;
	}

}
