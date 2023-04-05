package com.paymentgateway.crm.action;

import java.math.BigInteger;
import java.util.List;

import org.slf4j.Logger; import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.user.LoginHistory;
import com.paymentgateway.commons.user.LoginHistoryDao;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserType;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.CrmFieldConstants;
import com.paymentgateway.commons.util.CrmFieldType;
import com.paymentgateway.commons.util.CrmValidator;
import com.paymentgateway.commons.util.DataEncoder;

public class LoginHistoryAction extends AbstractSecureAction {
	
	@Autowired
	LoginHistoryDao loginHistoryDao;
	
	@Autowired
	DataEncoder encoder;
	
	@Autowired
	private CrmValidator validator;

	private static Logger logger = LoggerFactory.getLogger(LoginHistoryAction.class.getName());
	private static final long serialVersionUID = -7675679740385269868L;

	private List<LoginHistory> aaData;
	private String emailId;
	private int draw;
	private int length;
	private int start;
	private BigInteger recordsTotal;
	public  BigInteger recordsFiltered;

	public String execute() {
		try {
			User user = (User) sessionMap.get(Constants.USER);
			if (user.getUserType() == UserType.MERCHANT) {	
				setRecordsTotal((loginHistoryDao.countTotalFindLoginHisByUser(user.getEmailId())));
				aaData =encoder.encodeLoginHistoryObj((List<LoginHistory>) loginHistoryDao
						.findLoginHisByUser(user.getEmailId(),getDraw(), getLength(), getStart()));
				
			} else if (user.getUserType() == UserType.SUBUSER || user.getUserType() == UserType.POSMERCHANT ) {
				setRecordsTotal(loginHistoryDao.countTotalfindLoginHisUser(user.getUserType(),user.getEmailId()));
				aaData = encoder.encodeLoginHistoryObj((List<LoginHistory>) loginHistoryDao
						.findLoginHisUser(user.getEmailId(),user.getUserType(),getDraw(), getLength(), getStart(),user.getFirstName(),user.getLastName()));
				
			}else if (user.getUserType() == UserType.RESELLER) {
				if((getEmailId().equals(CrmFieldConstants.ALL_MERCHANTS.getValue()))) {
					setRecordsTotal(loginHistoryDao.countTotalfindResellerAll(user.getResellerId()));
					aaData = encoder.encodeLoginHistoryObj(loginHistoryDao.findResellerAll(user.getResellerId(),getDraw(), getLength(), getStart()));
				}
				else {
					setRecordsTotal(loginHistoryDao.countTotalfindLoginHisByUser(getEmailId()));
					aaData = encoder.encodeLoginHistoryObj((List<LoginHistory>) loginHistoryDao
							.findLoginHisByUser(getEmailId(),getDraw(), getLength(), getStart()));
				}
			}
			else if (user.getUserType() == UserType.ADMIN ){
				if( (getEmailId().equals(CrmFieldConstants.ALL_MERCHANTS.getValue()))) {
					setRecordsTotal((loginHistoryDao.countTotalAdmin()));
					if(getLength()==-1){
						setLength(getRecordsTotal().intValue());
					}
					aaData = encoder.encodeLoginHistoryObj(loginHistoryDao.findAll(getDraw(), getLength(), getStart()));
				}
				else {
					setRecordsTotal((loginHistoryDao.countTotalFindLoginHisByUser(getEmailId())));
					aaData =encoder.encodeLoginHistoryObj((List<LoginHistory>) loginHistoryDao
							.findLoginHisByUser(getEmailId(),getDraw(), getLength(), getStart()));
				}
			}	else if ( user.getUserType() == UserType.SUBADMIN){
				if( (getEmailId().equals(CrmFieldConstants.ALL_MERCHANTS.getValue()))) {
					setRecordsTotal((loginHistoryDao.countTotalfindLoginHisUser( user.getUserType(),user.getEmailId())));
					if(getLength()==-1){
						setLength(getRecordsTotal().intValue());
					}
					aaData = encoder.encodeLoginHistoryObj((List<LoginHistory>)loginHistoryDao.findLoginSubAdmin(user.getEmailId(),getDraw(), getLength(), getStart(),user.getFirstName(),user.getLastName()));
				}
				else {
					setRecordsTotal((loginHistoryDao.countTotalFindLoginHisByUser(getEmailId())));
					aaData =encoder.encodeLoginHistoryObj((List<LoginHistory>) loginHistoryDao
							.findLoginHisByUser(getEmailId(),getDraw(), getLength(), getStart()));
				}
			}else if ( user.getUserType() == UserType.AGENT){
				setRecordsTotal((loginHistoryDao.countTotalFindLoginHisByAgent(user.getEmailId())));
				aaData =encoder.encodeLoginHistoryObj((List<LoginHistory>) loginHistoryDao
						.findLoginHisByAgent(user.getEmailId(),getDraw(), getLength(), getStart()));
				
			}
			
		} catch (Exception exception) {
			logger.error("Exception", exception);			
		}
		recordsFiltered = recordsTotal;
		
		return SUCCESS;
	}

	public void validate(){

		if(validator.validateBlankField(getEmailId()) || getEmailId().equals(CrmFieldConstants.ALL_MERCHANTS.getValue()) || getEmailId().equals(CrmFieldConstants.ALL_USERS.getValue())){
		}
        else if(!validator.validateField(CrmFieldType.EMAILID, getEmailId())){
        	addFieldError(CrmFieldType. EMAILID.getName(), ErrorType.INVALID_FIELD.getResponseMessage());
		}
	}

	public List<LoginHistory> getAaData() {
		return aaData;
	}

	public void setAaData(List<LoginHistory> aaData) {
		this.aaData = aaData;
	}

	public String getEmailId() {
		return emailId;
	}

	public void setEmailId(String emailId) {
		this.emailId = emailId;
	}

	public int getDraw() {
		return draw;
	}

	public void setDraw(int draw) {
		this.draw = draw;
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

}

