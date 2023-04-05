package com.paymentgateway.crm.action;

import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger; import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.paymentgateway.commons.util.BusinessType;
import com.paymentgateway.commons.util.CrmFieldType;
import com.paymentgateway.commons.util.CrmValidator;

/**
 * @author Puneet
 *
 */

public class FetchIndustrySubCategory extends AbstractSecureAction {
	private static Logger logger = LoggerFactory.getLogger(FetchIndustrySubCategory.class.getName());
	@Autowired
	private CrmValidator validator;

	private static final long serialVersionUID = -452094231699163577L;

	private List<String> subCategories = new LinkedList<String>();
	private String industryCategory;

	public String execute(){
		try{
			setSubCategories(BusinessType.getIndustrySubcategory(industryCategory));
		}catch(Exception exception){
			logger.error("Exception", exception);
		}
		return SUCCESS;
	}
	public void validate() {

		if ((validator.validateBlankField(getIndustryCategory()))) {
			addFieldError(CrmFieldType.INDUSTRY_CATEGORY.getName(), validator
					.getResonseObject().getResponseMessage());
		} else if (!(validator.validateField(CrmFieldType.INDUSTRY_CATEGORY,getIndustryCategory()))) {
			addFieldError(CrmFieldType.INDUSTRY_CATEGORY.getName(), validator
					.getResonseObject().getResponseMessage());
		}
		}

	public List<String> getSubCategories() {
		return subCategories;
	}
	public void setSubCategories(List<String> subCategories) {
		this.subCategories = subCategories;
	}

	public String getIndustryCategory() {
		return industryCategory;
	}

	public void setIndustryCategory(String industryCategory) {
		this.industryCategory = industryCategory;
	}

}
