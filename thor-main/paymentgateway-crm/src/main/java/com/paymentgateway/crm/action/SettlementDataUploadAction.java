package com.paymentgateway.crm.action;

import java.math.BigInteger;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.paymentgateway.commons.dao.HibernateSessionProvider;
import com.paymentgateway.commons.dao.SettlementDataUploadDao;
import com.paymentgateway.commons.user.SettlementDataUpload;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.DateCreater;
import com.paymentgateway.commons.util.SettledTransactionDataService;
import com.paymentgateway.commons.util.TDRStatus;

/**
 * @author Shaiwal
 *
 */

public class SettlementDataUploadAction extends AbstractSecureAction {


	private static final long serialVersionUID = 4747578787024374486L;

	private static Logger logger = LoggerFactory.getLogger(SettlementDataUploadAction.class.getName());

	private int draw;
	private int length;
	private int start;
	private BigInteger recordsTotal;
	public BigInteger recordsFiltered;
	private String response;
	private List<SettlementDataUpload> aaData;
	private User sessionUser = new User();
	private String fromDate;
	private String toDate;
	
	@Autowired
	private SettlementDataUploadDao  settlementDataUploadDao;
	
	@Autowired
	private SettledTransactionDataService settledTransactionDataService;
	
	@SuppressWarnings("deprecation")
	public String execute() {
		
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.ENGLISH);
		
		logger.info("Inside SettlementDataUploadAction");
		try {
			
		SettlementDataUpload settlementDataUpload = settlementDataUploadDao.findLastUpload();
		sessionUser = (User) sessionMap.get(Constants.USER.getValue());
			if (settlementDataUpload == null) {
				
				/*if (StringUtils.isNotBlank(fromDate)) {
					fromDate = DateCreater.toDateTimeformatCreater(fromDate);
				}
				if (StringUtils.isNotBlank(toDate)) {
					toDate = DateCreater.toDateTimeformatCreater(toDate);
				}*/
				
				String fromDate = "2018-10-01 00:00:00";
				Date dateFrom =  format.parse(fromDate);
				Date dateTo =  new Date();
				
				SettlementDataUpload settlementDataUploadUpdate = new SettlementDataUpload();
				settlementDataUploadUpdate.setCreatedDate(dateTo);
				settlementDataUploadUpdate.setFromDate(dateFrom);
				settlementDataUploadUpdate.setToDate(dateTo);
				settlementDataUploadUpdate.setRequestedBy(sessionUser.getEmailId());
				settlementDataUploadUpdate.setStatus(TDRStatus.PENDING.getName());
				settlementDataUploadDao.create(settlementDataUploadUpdate);
				
				DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				String dateTextFrom = dateFormat.format(dateFrom);
				String dateTextTo = dateFormat.format(dateTo);
				
				settledTransactionDataService.uploadSettledData(dateTextFrom, dateTextTo);
				setResponse("Upload Complete");
				
				Session session = null;
				Long id;
				try {
					
					SettlementDataUpload settlementDataUpdateLast = settlementDataUploadDao.findLastUpload();
					logger.info("Found old settlementDataUpdateLast ");
					session = HibernateSessionProvider.getSession();
					Transaction tx = session.beginTransaction();
					id = settlementDataUpdateLast.getId();
					session.load(settlementDataUpdateLast, settlementDataUpdateLast.getId());
					SettlementDataUpload settlementDataUpdate = (SettlementDataUpload) session.get(SettlementDataUpload.class, id);

					settlementDataUpdate.setStatus(TDRStatus.ACCEPTED.getName());
					session.update(settlementDataUpdate);
					tx.commit();
					session.close();
					logger.info("Updated old settlementDataUpdateLast ");
				} catch (HibernateException e) {
					logger.error("settlementDataUpdateLast edit failed : ", e);
					return SUCCESS;
				}
				
				return SUCCESS;
			}
			
			else {
				
				if (settlementDataUpload.getStatus().equalsIgnoreCase(TDRStatus.PENDING.getName())) {
					setResponse("Upload already in progress");
					return SUCCESS;
				}
				else {
				
					Date dateFrom;
					Date dateTo;
					
					if (StringUtils.isNotBlank(fromDate)) {
						fromDate = DateCreater.toDateTimeformatCreater(fromDate);
						dateFrom =  format.parse(fromDate);
						
					}
					else {
						dateFrom =   settlementDataUpload.getToDate();
						dateFrom.setSeconds(dateFrom.getSeconds()+1);
					}
					
					if (StringUtils.isNotBlank(toDate)) {
						toDate = DateCreater.toDateTimeformatCreater(toDate);
						dateTo =  format.parse(toDate);
					}
					else {
						dateTo =   new Date();
					}
					
					
					
					SettlementDataUpload settlementDataUploadUpdate = new SettlementDataUpload();
					settlementDataUploadUpdate.setCreatedDate(new Date());
					settlementDataUploadUpdate.setFromDate(dateFrom);
					settlementDataUploadUpdate.setToDate(dateTo);
					settlementDataUploadUpdate.setRequestedBy(sessionUser.getEmailId());
					settlementDataUploadUpdate.setStatus(TDRStatus.PENDING.getName());
					settlementDataUploadDao.create(settlementDataUploadUpdate);
					
					DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					String dateTextFrom = dateFormat.format(dateFrom);
					String dateTextTo = dateFormat.format(dateTo);
					
					settledTransactionDataService.uploadSettledData(dateTextFrom, dateTextTo);
					setResponse("Upload Complete");
					Session session = null;
					Long id;
					try {
						
						SettlementDataUpload settlementDataUpdateLast = settlementDataUploadDao.findLastUpload();
						logger.info("Found old settlementDataUpdateLast ");
						session = HibernateSessionProvider.getSession();
						Transaction tx = session.beginTransaction();
						id = settlementDataUpdateLast.getId();
						session.load(settlementDataUpdateLast, settlementDataUpdateLast.getId());
						SettlementDataUpload settlementDataUpdate = (SettlementDataUpload) session.get(SettlementDataUpload.class, id);

						settlementDataUpdate.setStatus(TDRStatus.ACCEPTED.getName());
						session.update(settlementDataUpdate);
						tx.commit();
						session.close();
						logger.info("Updated old settlementDataUpdateLast ");
					} catch (HibernateException e) {

						logger.error("settlementDataUpdateLast edit failed : ", e);
						return ERROR;
					}
					return SUCCESS;
				}
				
			}
		}
		catch(Exception e) {
			logger.error("Exception " , e);
		}
		return SUCCESS;
	}
			
		

	public String getHistory() {
		
		logger.info("Inside SettlementDataUploadAction , getHistory()");
		int totalCount;
		
		try {
			sessionUser = (User) sessionMap.get(Constants.USER.getValue());
			List<SettlementDataUpload> settlementDataUploadList = new ArrayList<SettlementDataUpload>();
			settlementDataUploadList = settlementDataUploadDao.findAll();
			
					totalCount = settlementDataUploadList.size();
					BigInteger bigInt = BigInteger.valueOf(totalCount);
					setRecordsTotal(bigInt);
					if (getLength() == -1) {
						setLength(getRecordsTotal().intValue());
					}
					aaData = settlementDataUploadList;
					recordsFiltered = recordsTotal;
					return SUCCESS;
		}
		
		catch(Exception e) {
			logger.error("Exception in getHistory " , e);
			return SUCCESS;
		}
	
		
	}
	

	
	public void validate() {

	}


	public List<SettlementDataUpload> getAaData() {
		return aaData;
	}

	public void setAaData(List<SettlementDataUpload> aaData) {
		this.aaData = aaData;
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

	public String getResponse() {
		return response;
	}

	public void setResponse(String response) {
		this.response = response;
	}



	public String getFromDate() {
		return fromDate;
	}



	public void setFromDate(String fromDate) {
		this.fromDate = fromDate;
	}

	public String getToDate() {
		return toDate;
	}

	public void setToDate(String toDate) {
		this.toDate = toDate;
	}

}
