package com.paymentgateway.crm.action;

import java.util.List;

import org.slf4j.Logger; import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.paymentgateway.commons.api.Hasher;
import com.paymentgateway.commons.dao.FieldsDao;
import com.paymentgateway.commons.user.RefundApprove;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.DateCreater;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.crm.actionBeans.SessionUserIdentifier;
import com.paymentgateway.crm.mongoReports.RefundApproveData;

/**
 * @author Chandan
 *
 */

public class RefundApproveAction extends AbstractSecureAction {

	private static final long serialVersionUID = 3838190575215461460L;
	private static Logger logger = LoggerFactory.getLogger(RefundApproveAction.class.getName());
	
	private String merchant;
	private String dateFrom;
	//private String dateTo;
	
	private String response;
	
	private List<RefundApprove> refundApproveList;
	private User sessionUser = new User();
	
	@Autowired
	private SessionUserIdentifier userIdentifier;
	
	@Autowired
	private RefundApproveData refundApproveData;	
	
	public String execute() {
		logger.info("Inside Refund Approve Action Class !!");
		sessionUser = (User) sessionMap.get(Constants.USER.getValue());
		setDateFrom(DateCreater.toDateTimeformatCreater(dateFrom));
		//setDateTo(DateCreater.formDateTimeformatCreater(dateTo));
		try {
			String merchantPayId = userIdentifier.getMerchantPayId(sessionUser, getMerchant());
			/*if(refundApproveData.isExistReco(merchantPayId, getDateFrom(),
							getDateTo()))
			{
				logger.info("Inside Refund Approve Action IsExistReco !!");*/
				try {			
					refundApproveData.getData(merchantPayId, getDateFrom());
					
					setResponse("Request has been initiated successfully !!");						
					return SUCCESS;
				} catch (Exception exception) {
					logger.error("Exception", exception);
					return ERROR;
				}		
			/*}
			else {
				
				return SUCCESS;
			}*/
		} catch (Exception exception) {
			logger.error("Exception", exception);
			return ERROR;
		}		
	}
	
	public String recoCheck() {
		logger.info("Inside Refund Approve Action Class, In Reco Check Method !!");
		sessionUser = (User) sessionMap.get(Constants.USER.getValue());
		setDateFrom(DateCreater.toDateTimeformatCreater(dateFrom));
		//setDateTo(DateCreater.formDateTimeformatCreater(dateTo));
		try {
			String merchantPayId = userIdentifier.getMerchantPayId(sessionUser, getMerchant());
			/*if(refundApproveData.isExistReco(merchantPayId, getDateFrom(),
							getDateTo()))
			{
				logger.info("Inside Refund Approve Action IsExistReco !!");*/
				try {			
					boolean recoCheck = refundApproveData.isRecoExistFunc(merchantPayId, getDateFrom());
					if(recoCheck) {
						setResponse("true");
					} else {
						setResponse("false");
					}
											
					return SUCCESS;
				} catch (Exception exception) {
					logger.error("Exception", exception);
					return ERROR;
				}		
			/*}
			else {
				
				return SUCCESS;
			}*/
		} catch (Exception exception) {
			logger.error("Exception", exception);
			return ERROR;
		}		
	}
	public String getMerchant() {
		return merchant;
	}

	public void setMerchant(String merchant) {
		this.merchant = merchant;
	}

	public String getDateFrom() {
		return dateFrom;
	}

	public void setDateFrom(String dateFrom) {
		this.dateFrom = dateFrom;
	}

	/*public String getDateTo() {
		return dateTo;
	}

	public void setDateTo(String dateTo) {
		this.dateTo = dateTo;
	}*/

	public String getResponse() {
		return response;
	}

	public void setResponse(String response) {
		this.response = response;
	}

	public List<RefundApprove> getRefundApproveList() {
		return refundApproveList;
	}

	public void setRefundApproveList(List<RefundApprove> refundApproveList) {
		this.refundApproveList = refundApproveList;
	}
	
	
}
