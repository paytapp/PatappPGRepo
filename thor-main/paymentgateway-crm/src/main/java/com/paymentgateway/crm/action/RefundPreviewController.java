package com.paymentgateway.crm.action;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger; import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.paymentgateway.commons.user.RefundPreview;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.CrmFieldConstants;
import com.paymentgateway.commons.util.CrmValidator;
import com.paymentgateway.commons.util.DataEncoder;
import com.paymentgateway.commons.util.DateCreater;
import com.paymentgateway.crm.actionBeans.SessionUserIdentifier;
import com.paymentgateway.crm.mongoReports.RefundPreviewData;


/**
 * @author Chandan
 *
 */

public class RefundPreviewController extends AbstractSecureAction{

	private static final long serialVersionUID = -7605908796224313941L;
	private static Logger logger = LoggerFactory.getLogger(RefundPreviewController.class.getName());

	private String merchant;
	private String dateFrom;
	//private String dateTo;
	
	private BigInteger recordsTotal;
	private BigInteger recordsFiltered;
	private int length;
	private int start;
	
	private String response;
	
	private List<RefundPreview> aaData ;
	private User sessionUser = new User();
	
	@Autowired
	private DataEncoder encoder;
	
	@Autowired
	private CrmValidator validator;
	
	@Autowired
	private SessionUserIdentifier userIdentifier;
	
	@Autowired
	private RefundPreviewData refundPreviewData;
	
	public String execute() {
		logger.info("Inside Refund Preview Controller !!");
		if(!getMerchant().equals(CrmFieldConstants.SELECT_MERCHANT.getValue().toString())) { 
		sessionUser = (User) sessionMap.get(Constants.USER.getValue());
		setDateFrom(DateCreater.toDateTimeformatCreater(dateFrom));
		//setDateTo(DateCreater.formDateTimeformatCreater(dateTo));
		try {
		String merchantPayId = userIdentifier.getMerchantPayId(sessionUser, getMerchant());
		//boolean isExistReco = refundPreviewData.isExistReco(merchantPayId, getDateFrom());
		/*if(refundPreviewData.isExistReco(merchantPayId, getDateFrom(),
						getDateTo()))
		{*/
			try {	
					aaData = encoder.encodeRefundPreviewObj(refundPreviewData.getData(merchantPayId, getDateFrom()));
					
					return SUCCESS;
	
			} catch (Exception exception) {
				logger.error("Exception in RefundPreviewController Get Data : ", exception);
				return ERROR;
			}
		/*}
			else {
				aaData =new ArrayList<RefundPreview>();														
				setResponse("Please Select Valid Date Range !!");
				return SUCCESS;
		}*/
			} catch (Exception exception) {
				logger.error("Exception in RefundPreviewController isExistReco : ", exception);
				return ERROR;
			}
	}
		else {
			aaData =new ArrayList<RefundPreview>();
			setResponse("Please select a merchant !!");
			return SUCCESS;
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

	public BigInteger getRecordsTotal() {
		return recordsTotal;
	}

	public void setRecordsTotal(BigInteger recordsTotal) {
		this.recordsTotal = recordsTotal;
	}

	public BigInteger getRecordsFiltered() {
		return recordsFiltered;
	}

	public void setRecordsFiltered(BigInteger recordsFiltered) {
		this.recordsFiltered = recordsFiltered;
	}

	public int getLength() {
		return length;
	}

	public void setLength(int length) {
		this.length = length;
	}

	public int getStart() {
		return start;
	}

	public void setStart(int start) {
		this.start = start;
	}

	
	public List<RefundPreview> getAaData() {
		return aaData;
	}


	public void setAaData(List<RefundPreview> aaData) {
		this.aaData = aaData;
	}


	public String getResponse() {
		return response;
	}


	public void setResponse(String response) {
		this.response = response;
	}


	/*public void validate() {
		logger.info("Inside validate");
	}*/
}
