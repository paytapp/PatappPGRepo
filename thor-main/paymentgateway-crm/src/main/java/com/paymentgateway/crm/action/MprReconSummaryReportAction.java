package com.paymentgateway.crm.action;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger; import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.paymentgateway.commons.user.MprTransaction;
import com.paymentgateway.commons.user.NodalAmount;
import com.paymentgateway.commons.user.NodalAmoutDao;
import com.paymentgateway.commons.user.TransactionCountSearch;
import com.paymentgateway.commons.user.TransactionSearch;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.user.UserType;
import com.paymentgateway.commons.util.Amount;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.DateCreater;
import com.paymentgateway.crm.actionBeans.MprSummeryReportService;

@SuppressWarnings("serial")
public class MprReconSummaryReportAction extends AbstractSecureAction {
	

	private static Logger logger = LoggerFactory.getLogger(MprReconSummaryReportAction.class.getName());

	private String dateFrom;
	private String acquirer;
	private String paymentType;
	private BigDecimal creditNodalAmount;
	private BigDecimal totalSaleTxnAmt;
	private BigDecimal totalrefundTxnAmt;
	private BigDecimal netAmount;
	private BigDecimal differenceAmount;
	private BigDecimal totalSaleMprAmount;
	private BigDecimal totalRefundMprAmount;
	private Integer totalSaleTxn;
	private Integer totalRefundTxn;
	private String reconDate;
	private Integer netTxn;
	TransactionCountSearch transactionCountSearch = new TransactionCountSearch();


	List<MprTransaction> aaData = new ArrayList<MprTransaction>();
	@Autowired
	private MprSummeryReportService mprSummaryCountService;
	
	
	@Autowired
	NodalAmoutDao nodalDao;
	
	public String execute() {
		
		Integer saleTXN=0;
		Integer refundTxn=0;
		Integer netTransaction=0;
		BigDecimal amountData = BigDecimal.ZERO;
		BigDecimal saleMprAmount = BigDecimal.ZERO;
		BigDecimal refundMprAmount = BigDecimal.ZERO;
		BigDecimal amountCC = BigDecimal.ZERO;
		BigDecimal amountUPI = BigDecimal.ZERO;
		
		try {
			
			if (StringUtils.isBlank(acquirer)) {
				acquirer = "ALL";
			}
		
			if (StringUtils.isBlank(paymentType)) {
				paymentType = "ALL";
			}
			setDateFrom(DateCreater.toDateTimeformatCreater(dateFrom));
			
			User sessionUser = (User) sessionMap.get(Constants.USER.getValue());
			if (sessionUser.getUserType().equals(UserType.SUPERADMIN) || sessionUser.getUserType().equals(UserType.ADMIN)
					|| sessionUser.getUserType().equals(UserType.SUBADMIN)
					|| sessionUser.getUserType().equals(UserType.ASSOCIATE)) {
				

				setAaData(mprSummaryCountService.getMprCountService(dateFrom, 
						acquirer,paymentType,sessionUser));
				
				
				
				if(paymentType.equals("ALL"))
				{
					 List<NodalAmount> nodalCC=nodalDao.getNodalAmountCC(acquirer,dateFrom);
					 List<NodalAmount> nodalDC=nodalDao.getNodalAmountUPI(acquirer,dateFrom);
					 
					 for (NodalAmount nodal : nodalCC) {

							reconDate = nodal.getReconDate();
							amountCC = nodal.getNodalCreditAmount();
						}
					 
					    for (NodalAmount nodal : nodalDC) {
					    	amountUPI = nodal.getNodalCreditAmount();

						}
					    creditNodalAmount = amountCC.add(amountUPI).setScale(2,
								RoundingMode.HALF_DOWN);
			 
				}
				
				else
				{
				 List<NodalAmount> nodalList=nodalDao.getNodalAmount(acquirer,paymentType,dateFrom);
				    
				for (NodalAmount nodal : nodalList) {

					reconDate = nodal.getReconDate();
					creditNodalAmount = nodal.getNodalCreditAmount();

				}
				}

				for (MprTransaction txnData : aaData) {
                   
					netTxn=txnData.getNetTxn();
					totalSaleTxn=txnData.getSaleTxn();
					totalRefundTxn=txnData.getRefundTxn();
					totalSaleMprAmount=txnData.getSaleMprAmount();
					totalRefundMprAmount=txnData.getRefundMprAmount();
					netAmount = txnData.getNetMprAmount();
    
					netTransaction=netTransaction+netTxn;
					saleTXN = saleTXN+totalSaleTxn;
					refundTxn=refundTxn+totalRefundTxn;					
					saleMprAmount = saleMprAmount.add(totalSaleMprAmount).setScale(2, RoundingMode.HALF_DOWN);
					refundMprAmount = refundMprAmount.add(totalRefundMprAmount).setScale(2, RoundingMode.HALF_DOWN);
					amountData = amountData.add(netAmount).setScale(2, RoundingMode.HALF_DOWN);

				}
				
				if(creditNodalAmount==null)
				{
					creditNodalAmount=BigDecimal.ZERO;
				}
				
			 BigDecimal	differenceAmount = amountData.subtract(creditNodalAmount).setScale(2,
				RoundingMode.HALF_DOWN);
			if(differenceAmount==null)
			{
				differenceAmount=BigDecimal.ZERO;
			}
			 
				setNetTxn(netTransaction);
				setTotalSaleTxn(saleTXN);
				setTotalRefundTxn(refundTxn);
				setDifferenceAmount(differenceAmount);
				setCreditNodalAmount(creditNodalAmount);
				setNetAmount(amountData);
				setTotalSaleMprAmount(saleMprAmount);
				setTotalRefundMprAmount(refundMprAmount);
				setReconDate(reconDate);
				    
				
				   
			}

		}
		catch(Exception e) {
			logger.error("Exception in getting transaction summary count data " , e);
		}
		
		
		return SUCCESS;
	}

	public String getDateFrom() {
		return dateFrom;
	}

	public void setDateFrom(String dateFrom) {
		this.dateFrom = dateFrom;
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

	public TransactionCountSearch getTransactionCountSearch() {
		return transactionCountSearch;
	}

	public void setTransactionCountSearch(TransactionCountSearch transactionCountSearch) {
		this.transactionCountSearch = transactionCountSearch;
	}

	public List<MprTransaction> getAaData() {
		return aaData;
	}

	public void setAaData(List<MprTransaction> aaData) {
		this.aaData = aaData;
	}

	public BigDecimal getCreditNodalAmount() {
		return creditNodalAmount;
	}

	public void setCreditNodalAmount(BigDecimal creditNodalAmount) {
		this.creditNodalAmount = creditNodalAmount;
	}

	public BigDecimal getTotalSaleTxnAmt() {
		return totalSaleTxnAmt;
	}

	public void setTotalSaleTxnAmt(BigDecimal totalSaleTxnAmt) {
		this.totalSaleTxnAmt = totalSaleTxnAmt;
	}

	public BigDecimal getTotalrefundTxnAmt() {
		return totalrefundTxnAmt;
	}

	public void setTotalrefundTxnAmt(BigDecimal totalrefundTxnAmt) {
		this.totalrefundTxnAmt = totalrefundTxnAmt;
	}

	public BigDecimal getNetAmount() {
		return netAmount;
	}

	public void setNetAmount(BigDecimal netAmount) {
		this.netAmount = netAmount;
	}

	public String getReconDate() {
		return reconDate;
	}

	public void setReconDate(String reconDate) {
		this.reconDate = reconDate;
	}

	public BigDecimal getDifferenceAmount() {
		return differenceAmount;
	}

	public void setDifferenceAmount(BigDecimal differenceAmount) {
		this.differenceAmount = differenceAmount;
	}

	public BigDecimal getTotalSaleMprAmount() {
		return totalSaleMprAmount;
	}

	public void setTotalSaleMprAmount(BigDecimal totalSaleMprAmount) {
		this.totalSaleMprAmount = totalSaleMprAmount;
	}

	public BigDecimal getTotalRefundMprAmount() {
		return totalRefundMprAmount;
	}

	public void setTotalRefundMprAmount(BigDecimal totalRefundMprAmount) {
		this.totalRefundMprAmount = totalRefundMprAmount;
	}

	public Integer getTotalSaleTxn() {
		return totalSaleTxn;
	}

	public void setTotalSaleTxn(Integer totalSaleTxn) {
		this.totalSaleTxn = totalSaleTxn;
	}

	public Integer getTotalRefundTxn() {
		return totalRefundTxn;
	}

	public void setTotalRefundTxn(Integer totalRefundTxn) {
		this.totalRefundTxn = totalRefundTxn;
	}

	public Integer getNetTxn() {
		return netTxn;
	}

	public void setNetTxn(Integer netTxn) {
		this.netTxn = netTxn;
	}

	


	
}
