package com.paymentgateway.crm.chargeback;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.paymentgateway.commons.dao.ChargebackDao;
import com.paymentgateway.commons.user.Chargeback;
import com.paymentgateway.commons.user.ChargebackComment;
import com.paymentgateway.commons.user.TransactionSearch;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.crm.action.AbstractSecureAction;
import com.paymentgateway.crm.mongoReports.TxnReports;

public class ViewChargebackDetails extends AbstractSecureAction {

	@Autowired
	private ChargebackDao chargebackDao;
	
	@Autowired
	private UserDao userDao;
	@Autowired
	TxnReports txnReports;

	private static final long serialVersionUID = 576762458042745734L;
	private static Logger logger = LoggerFactory.getLogger(ViewChargebackDetails.class.getName());
	private Chargeback chargeback = new Chargeback();
	private Chargeback chargebackUpdate = new Chargeback();
	private String Id;
	private String caseId;
	private String commentsString;	
	private String commentId;
	private String createDate;
	private String messageBody;
	private String response;
	private String caseStatus;
	private String targetDate;
	private Date todaydate;
	private Date updateDate;
	private String pgRefNum;
	private String orderId;
	private String capturedAmount;
	private String chargebackStatus;
	List<TransactionSearch> report = null;

	private List<Chargeback> chargebacklist = new ArrayList<Chargeback>();

	private List<ChargebackComment> commentList = new LinkedList<>();

	
	public String execute() {
		try {
			List<TransactionSearch> list = new ArrayList<TransactionSearch>();
			list.add(new TransactionSearch(getPgRefNum(),getOrderId(), getCapturedAmount()));

			report = txnReports.refundForSaleCaputureTransaction(list);
			if (report != null) {
				for (TransactionSearch txn : report) {

					/*
					 */
					if (caseId != null)
					setChargeback(chargebackDao.findbyId(getCaseId()));
					User user=userDao.findPayId(getChargeback().getPayId());
					String buisenessName=user.getBusinessName();
					
					if(chargeback.getStatus().equalsIgnoreCase("Refunded") || chargeback.getStatus().equalsIgnoreCase("Closed")) {
						chargeback.setRefundAvailable("0.00");
					}else {
						chargeback.setRefundAvailable(String.valueOf(getChargeback().getChargebackAmount()));
					}
					chargeback.setCardNumber(getChargeback().getCardNumber());
					chargeback.setRefundedAmount(txn.getRefundedAmount());
					chargeback.setTdr(getChargeback().getTdr());
					chargeback.setBusinessName(buisenessName);
					if(getChargeback().getSubMerchantId() != null) {
						chargeback.setSubMerchantName(userDao.getBusinessNameByPayId(getChargeback().getSubMerchantId()));
					}
					
					Date createDate = chargeback.getCreateDate();
					Date updateDate = chargeback.getUpdateDate();
					String targetDate = chargeback.getTargetDate();
					SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
					String createDateString = dateFormat.format(createDate);
					String updateDateString = dateFormat.format(updateDate);
					chargeback.setUpdateDateString(updateDateString);
					chargeback.setCreateDateString(createDateString);
					chargeback.setTargetDate(targetDate + " 23:59:59");
					
					chargeback.setRefundedAmount(getChargeback().getRefundedAmount());
					}
			
				if (chargeback.getComments() != null) {
					/*
					 * byte[] allFields = Base64.decodeBase64(chargeback.getComments());
					 * commentsString = new String(allFields);
					 */
					commentsString = new String(chargeback.getComments());

				}
				Set<ChargebackComment> chargebackCommentSet = chargeback.getChargebackComments();
				Iterator<ChargebackComment> iterator = chargebackCommentSet.iterator();
				while (iterator.hasNext()) {
					ChargebackComment chargebackComment = iterator.next();
					
					if(StringUtils.isBlank(chargebackComment.getCommentBody())) {
						continue;
					}
					Date cmmntCreateDate = chargebackComment.getCreateDate();
					SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
					String cmmntDateString = dateFormat.format(cmmntCreateDate);
					chargebackComment.setCommentcreateDate(cmmntDateString);
					commentList.add(chargebackComment);
				}
				/* date$time wise sort */
				Collections.sort(commentList, new Comparator<ChargebackComment>() {
					@Override
					public int compare(ChargebackComment o1, ChargebackComment o2) {
						if (o1.getCreateDate() == null || o2.getCreateDate() == null) {
							return 0;
						}
						return o1.getCreateDate().compareTo(o2.getCreateDate());
					}
				});

			chargebacklist = chargebackDao.chargebackByList(caseId);
			
			try{
				for(Chargeback chrbck : chargebacklist) {
					Date updt = chrbck.getUpdateDate();
					SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
					String createDateString = dateFormat.format(updt);
					chrbck.setUpdateDateString(createDateString);
					
				}
			}catch(Exception ex) {
				logger.info("Exception Cought in Chargeback status list : " , ex);
			}

			logger.info("list" + chargebacklist);
			//logger.info("commentList : " + commentList.toString());

			}
			return SUCCESS;
			
			
		} catch (Exception exception) {
			logger.error("Exception", exception);
			return ERROR;

		}

	}
	
	

	public Chargeback getChargebackUpdate() {
		return chargebackUpdate;
	}



	public void setChargebackUpdate(Chargeback chargebackUpdate) {
		this.chargebackUpdate = chargebackUpdate;
	}



	public String getChargebackStatus() {
		return chargebackStatus;
	}

	public void setChargebackStatus(String chargebackStatus) {
		this.chargebackStatus = chargebackStatus;
	}

	public List<Chargeback> getChargebacklist() {
		return chargebacklist;
	}

	public void setChargebacklist(List<Chargeback> chargebacklist) {
		this.chargebacklist = chargebacklist;
	}

	public String getTargetDate() {
		return targetDate;
	}

	public void setTargetDate(String targetDate) {
		this.targetDate = targetDate;
	}

	public Date getTodaydate() {
		return todaydate;
	}

	public void setTodaydate(Date todaydate) {
		this.todaydate = todaydate;
	}

	public Chargeback getChargeback() {
		return chargeback;
	}

	public void setChargeback(Chargeback chargeback) {
		this.chargeback = chargeback;
	}

	public String getCaseId() {
		return caseId;
	}

	public void setCaseId(String caseId) {
		this.caseId = caseId;
	}

	public String getCommentsString() {
		return commentsString;
	}

	public void setCommentsString(String commentsString) {
		this.commentsString = commentsString;
	}

	public String getCommentId() {
		return commentId;
	}

	public void setCommentId(String commentId) {
		this.commentId = commentId;
	}

	public String getCreateDate() {
		return createDate;
	}

	public void setCreateDate(String createDate) {
		this.createDate = createDate;
	}

	public String getMessageBody() {
		return messageBody;
	}

	public void setMessageBody(String messageBody) {
		this.messageBody = messageBody;
	}

	public String getResponse() {
		return response;
	}

	public void setResponse(String response) {
		this.response = response;
	}

	public List<ChargebackComment> getCommentList() {
		return commentList;
	}

	public void setCommentList(List<ChargebackComment> commentList) {
		this.commentList = commentList;
	}

	public String getId() {
		return Id;
	}

	public void setId(String id) {
		Id = id;
	}

	public String getCaseStatus() {
		return caseStatus;
	}

	public void setCaseStatus(String caseStatus) {
		this.caseStatus = caseStatus;
	}

	public Date getUpdateDate() {
		return updateDate;
	}

	public void setUpdateDate(Date updateDate) {
		this.updateDate = updateDate;
	}

	public String getPgRefNum() {
		return pgRefNum;
	}

	public void setPgRefNum(String pgRefNum) {
		this.pgRefNum = pgRefNum;
	}

	public String getOrderId() {
		return orderId;
	}

	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}

	public String getCapturedAmount() {
		return capturedAmount;
	}

	public void setCapturedAmount(String capturedAmount) {
		this.capturedAmount = capturedAmount;
	}
	
	}
