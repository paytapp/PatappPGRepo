package com.paymentgateway.crm.action;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.paymentgateway.commons.user.AirSettlement;
import com.paymentgateway.commons.user.TransactionSearch;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.DateCreater;
import com.paymentgateway.crm.actionBeans.SessionUserIdentifier;
import com.paymentgateway.crm.mongoReports.AirSettlementReportData;

/**
 * @author Chandan
 *
 */

public class AirSettlementReportAction extends AbstractSecureAction {

	private static final long serialVersionUID = 5067142842901261304L;
	private static Logger logger = LoggerFactory.getLogger(AirSettlementReportAction.class.getName());
	

	private String merchantPayId;
	private String currency;
	private String saleDate;
	private InputStream fileInputStream;
	private String filename;
	
	private User sessionUser = new User();
	
	@Autowired
	private SessionUserIdentifier userIdentifier;
	
	@Autowired
	private AirSettlementReportData airSettlementReportData;
	
	public String execute() {

		if (currency == null || currency.isEmpty()) {
			currency = "ALL";
		}
		
		/*status = StatusType.SETTLED.getName();
		txnType= TransactionType.RECO.getName();*/
		sessionUser = (User) sessionMap.get(Constants.USER.getValue());
		List<AirSettlement> airSettlementList = new ArrayList<AirSettlement>();
		setSaleDate(DateCreater.toDateTimeformatCreater(saleDate));
		airSettlementList = airSettlementReportData.downloadSettlementReport( merchantPayId,currency, saleDate,sessionUser);
		
			StringBuilder strBuilder = new StringBuilder();
			for (AirSettlement airSettlement : airSettlementList) {
				
			    String seperator = "|";
				strBuilder.append(airSettlement.getPgRefNum());
				strBuilder.append(seperator);
				strBuilder.append(airSettlement.getAmount());
				strBuilder.append(seperator);
				strBuilder.append(airSettlement.getSettlementDate());
				strBuilder.append(seperator);
				strBuilder.append(airSettlement.getOrderId());
				strBuilder.append(seperator);
				strBuilder.append(airSettlement.getSaleDate());
				strBuilder.append("\r\n");
		}		

		try {
			String FILE_EXTENSION = ".txt";
			//DateFormat df = new SimpleDateFormat("yyyyMMddhhmmss");
			filename = "Settlement_Payment_Gateway_NBDR_" + DateCreater.formatSaleDate(getSaleDate()) + FILE_EXTENSION;
			File file = new File(filename);
			file.createNewFile();  			
			 
			//Write Content
			FileWriter writer = new FileWriter(file);
			writer.write(strBuilder.toString());
			writer.close();
			setFileInputStream(new FileInputStream(file));
			addActionMessage(filename + " written successfully on disk.");
		
		} catch (Exception exception) {
			logger.error("Exception", exception);
		}
		
		return SUCCESS;
	}
	
	public String getMerchantPayId() {
		return merchantPayId;
	}

	public void setMerchantPayId(String merchantPayId) {
		this.merchantPayId = merchantPayId;
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

	public InputStream getFileInputStream() {
		return fileInputStream;
	}

	public void setFileInputStream(InputStream fileInputStream) {
		this.fileInputStream = fileInputStream;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}
	
	

}
