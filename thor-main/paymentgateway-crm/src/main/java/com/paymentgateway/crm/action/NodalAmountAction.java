package com.paymentgateway.crm.action;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger; import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.user.NodalAmount;
import com.paymentgateway.commons.user.NodalAmoutDao;
import com.paymentgateway.commons.user.ResponseObject;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserType;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.CrmFieldConstants;
import com.paymentgateway.commons.util.CrmFieldType;
import com.paymentgateway.commons.util.CrmValidator;
import com.paymentgateway.commons.util.DataEncoder;
import com.paymentgateway.commons.util.DateCreater;
import com.paymentgateway.crm.actionBeans.CreateNodalAccount;

public class NodalAmountAction extends AbstractSecureAction {
	
     
	
	@Autowired 
	private CreateNodalAccount createNodalAmount;
	
	@Autowired
	NodalAmoutDao nodalDao;
	
	@Autowired
	private CrmValidator validator;

	private static final long serialVersionUID = 8272785394067627180L;

	private static Logger logger = LoggerFactory.getLogger(NodalAmountAction.class.getName());
	

	private String acquirer;
	private String paymentType;
	private BigDecimal nodalAmount;
	private String createDate;
	private NodalAmount nodal =new NodalAmount();
	private User sessionUser = new User();
	private ResponseObject responseObject;
	private String response;
	List<NodalAmount> aaData=new ArrayList<NodalAmount>();
	
	public String execute() {


	try {
		   DataEncoder encoder = new DataEncoder();
			sessionUser = (User) sessionMap.get(Constants.USER.getValue());

			setCreateDate(DateCreater.toDateTimeformatCreater(createDate));
			if (sessionUser.getUserType().equals(UserType.ADMIN)
					|| sessionUser.getUserType().equals(UserType.SUBADMIN)) {

		  try
	          {
				NodalAmount existNodal = nodalDao.existNodalData(acquirer, paymentType, createDate);
             
				
				if (existNodal != null) {
					if (existNodal.getAcquirer().equals(acquirer)
							&& existNodal.getPaymentType().equals(paymentType)
							&& existNodal.getReconDate().equals(createDate)) {
						    setAaData(encoder.encodeNodalObj(nodalDao.getSingleResult(acquirer, paymentType,createDate)));
						    setResponse("Nodal Amount is Already Exist");
						return SUCCESS;
					}
				}

				else {

					logger.info("Create Subadmin");
					responseObject = createNodalAmount.createNodalAmount(getNodalInstance());
		     		setAaData(encoder.encodeNodalObj(nodalDao.getSingleResult(acquirer, paymentType,createDate)));
				 	setResponse(CrmFieldConstants.NODAL_AMOUNT_SUCCESSFULLY.getValue());
					return SUCCESS;
				}
                }
				catch (Exception ex) {
					logger.info("Create Subadmin");
					responseObject = createNodalAmount.createNodalAmount(getNodalInstance());
		     		setAaData(encoder.encodeNodalObj(nodalDao.getSingleResult(acquirer, paymentType,createDate)));
				 	setResponse(CrmFieldConstants.NODAL_AMOUNT_SUCCESSFULLY.getValue());
					return SUCCESS;
				}
				
				
			}
			if (!ErrorType.SUCCESS.getResponseCode().equals(responseObject.getResponseCode())) {
				addActionMessage(responseObject.getResponseMessage());
				return SUCCESS;
			}

			return SUCCESS;
		} catch (Exception exception) {
			logger.error("Exception", exception);
			return ERROR;
		}
	}
	


	public String viewNodalData() {
		
		try
		{
		

			DataEncoder encoder = new DataEncoder();
			setAaData(encoder.encodeNodalObj(nodalDao.getNodalAmountList()));
		}
		
		catch (Exception exception) {
			logger.error("Exception", exception);
			return ERROR;
			
		}
		
		return SUCCESS;
		
	}
	
	private NodalAmount getNodalInstance()
	{
		nodal.setAcquirer(acquirer);
		nodal.setPaymentType(paymentType);
		nodal.setNodalCreditAmount(nodalAmount);
		nodal.setReconDate(createDate);
		return nodal;
	}

	
	
	
	
	
	
	
	public BigDecimal getNodalAmount() {
		return nodalAmount;
	}





	public void setNodalAmount(BigDecimal nodalAmount) {
		this.nodalAmount = nodalAmount;
	}




	public List<NodalAmount> getAaData() {
		return aaData;
	}





	public void setAaData(List<NodalAmount> aaData) {
		this.aaData = aaData;
	}





	public String getCreateDate() {
		return createDate;
	}





	public void setCreateDate(String createDate) {
		this.createDate = createDate;
	}





	public User getSessionUser() {
		return sessionUser;
	}





	public void setSessionUser(User sessionUser) {
		this.sessionUser = sessionUser;
	}





	public String getAcquirer() {
		return acquirer;
	}


	public void setAcquirer(String acquirer) {
		this.acquirer = acquirer;
	}


	public String getPaymentType() {
		return paymentType;
	}


	public void setPaymentType(String paymentType) {
		this.paymentType = paymentType;
	}




	public ResponseObject getResponseObject() {
		return responseObject;
	}


	public void setResponseObject(ResponseObject responseObject) {
		this.responseObject = responseObject;
	}


	public NodalAmount getNodal() {
		return nodal;
	}


	public void setNodal(NodalAmount nodal) {
		this.nodal = nodal;
	}




	public String getResponse() {
		return response;
	}





	public void setResponse(String response) {
		this.response = response;
	}
	
	
	
	
	
	
	
}
