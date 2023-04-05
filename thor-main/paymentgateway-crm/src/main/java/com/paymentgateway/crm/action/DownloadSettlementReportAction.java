package com.paymentgateway.crm.action;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.paymentgateway.commons.user.Merchants;
import com.paymentgateway.commons.user.TransactionSearch;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.DateCreater;

public class DownloadSettlementReportAction extends AbstractSecureAction {

	private static final long serialVersionUID = -8129011751068997117L;
	private static Logger logger = LoggerFactory.getLogger(DownloadSettlementReportAction.class.getName());

	private List<Merchants> merchantList = new LinkedList<Merchants>();
	private Map<String, String> currencyMap = new HashMap<String, String>();
	private String currency;
	private String saleDate;
	private String merchantPayId;
	/*private String status;
	private String txnType;*/
	private String acquirer;
	private InputStream fileInputStream;
	private String filename;
	private int count=1;
	private String file_extention;
	private User sessionUser = new User();
	private String cardHolderType;
	
	public static final BigDecimal ONE_HUNDRED = new BigDecimal(100);

	@Autowired
	private SummaryReportQuery settlementReport;
		
	@SuppressWarnings("resource")
	public String execute() {
		
		if (acquirer == null || acquirer.isEmpty()) {
			acquirer = "ALL";
		}

		if (currency == null || currency.isEmpty()) {
			currency = "ALL";
		}
		
		/*status = StatusType.SETTLED.getName();
		txnType= TransactionType.RECO.getName();*/
		sessionUser = (User) sessionMap.get(Constants.USER.getValue());
		List<TransactionSearch> transactionList = new ArrayList<TransactionSearch>();
		setSaleDate(DateCreater.toDateTimeformatCreater(saleDate));
		transactionList = settlementReport.downloadSettlementReport( merchantPayId,currency, saleDate,sessionUser , acquirer);
		
			StringBuilder strBuilder = new StringBuilder();
			for (TransactionSearch transaction : transactionList) {
				
			    String seperator = "|";
				strBuilder.append(transaction.getPgRefNum());
				strBuilder.append(seperator);
				strBuilder.append(transaction.getAmount());
				strBuilder.append(seperator);
				strBuilder.append(transaction.getDateFrom());
				strBuilder.append(seperator);
				strBuilder.append(transaction.getOrderId());
				strBuilder.append(seperator);
				strBuilder.append(transaction.getPaymentMethods());
				strBuilder.append("\r\n");
		}		

		try {
			String FILE_EXTENSION = ".txt";
			DateFormat df = new SimpleDateFormat("yyyyMMddhhmmss");
			filename = "Settlement_PAYMENT_GATEWAY_NBDR_" + DateCreater.formatSaleDate(getSaleDate()) + "_V1" + FILE_EXTENSION;
			File file = new File(filename);
			file.createNewFile();  			
			 
			//Write Content
			FileWriter writer = new FileWriter(file);
			writer.write(strBuilder.toString());
			writer.close();
			fileInputStream = new FileInputStream(file);
			addActionMessage(filename + " written successfully on disk.");
		
		} catch (Exception exception) {
			logger.error("Exception", exception);
		}
		
		
		      return SUCCESS;
		}
		  
	public InputStream getFileInputStream() {
		return fileInputStream;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}


	public String getCardHolderType() {
		return cardHolderType;
	}

	public void setCardHolderType(String cardHolderType) {
		this.cardHolderType = cardHolderType;
	}

	public String getMerchantPayId() {
		return merchantPayId;
	}

	public void setMerchantPayId(String merchantPayId) {
		this.merchantPayId = merchantPayId;
	}

	public String getAcquirer() {
		return acquirer;
	}

	public void setAcquirer(String acquirer) {
		this.acquirer = acquirer;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}
	
	public String getSaleDate() {
		return saleDate;
	}
	
	public void setSaleDate(String saleDate) {
		this.saleDate = saleDate;
	}
}