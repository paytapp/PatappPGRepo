package com.paymentgateway.crm.action;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.paymentgateway.commons.dao.IssuerDetailsDao;
import com.paymentgateway.commons.user.IssuerDetails;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.CrmValidator;
import com.paymentgateway.commons.util.PaymentType;
import com.paymentgateway.commons.util.TDRStatus;

public class DeleteIssuerDetailAction extends AbstractSecureAction {

	private static final long serialVersionUID = 7168644763789837340L;
	private static Logger logger = LoggerFactory.getLogger(DeleteIssuerDetailAction.class.getName());

	@Autowired
	private IssuerDetailsDao issuerDetailsDao;

	private Long slabId;
	private String response;
	private String payId;
	
	private List<IssuerDetails> aaData = new ArrayList<IssuerDetails>();

	public String execute() {
		try {
			if (!StringUtils.isBlank(slabId.toString())) {
				User sessionUser = (User)sessionMap.get(Constants.USER.getValue());
				IssuerDetails slab = issuerDetailsDao.getIssuerDetailabyslabId(slabId);
				Date date = new Date();
				slab.setStatus(TDRStatus.INACTIVE);
				slab.setUpdatedDate(date);
				slab.setProcessedBy(sessionUser.getEmailId());
				slab.setAlwaysOn(false);
				issuerDetailsDao.update(slab);
				setResponse("EMI slab deleted successfully.");
				
				List<IssuerDetails> issurerDataList=new ArrayList<>();
				issurerDataList.addAll(issuerDetailsDao.getActiveAllEmiSlab());
				for(IssuerDetails id:issurerDataList){			
					id.setPaymentType(PaymentType.getpaymentName(id.getPaymentType().toUpperCase()));
				}
				setAaData(issurerDataList);
				
				return SUCCESS;
				
			} else {
				setResponse("Try again, Something went wrong!");
				return ERROR;
			}
		} catch (Exception exception) {
			logger.error("Issuer Details - Exception :" , exception);
			setResponse("Try again, Something went wrong!");
			return ERROR;
		}
	}

	public void validate() {
		CrmValidator validator = new CrmValidator();
		if (!validator.validateBlankField(slabId)) {
			setResponse("Try again, Something went wrong!");
		}
		if (!StringUtils.isBlank(payId)) {
			if (!validator.validateBlankField(payId)) {
				setResponse("Try again, Something went wrong!");
			}
		}
	}

	

	public Long getSlabId() {
		return slabId;
	}

	public void setSlabId(Long slabId) {
		this.slabId = slabId;
	}

	public String getResponse() {
		return response;
	}

	public void setResponse(String response) {
		this.response = response;
	}

	public String getPayId() {
		return payId;
	}

	public void setPayId(String payId) {
		this.payId = payId;
	}
	public List<IssuerDetails> getAaData() {
		return aaData;
	}

	public void setAaData(List<IssuerDetails> aaData) {
		this.aaData = aaData;
	}
}
