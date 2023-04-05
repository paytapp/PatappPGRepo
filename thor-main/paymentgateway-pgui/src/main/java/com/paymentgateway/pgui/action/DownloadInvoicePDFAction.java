package com.paymentgateway.pgui.action;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.struts2.interceptor.ServletRequestAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.core.io.ByteArrayResource;


import com.paymentgateway.commons.dao.FieldsDao;
import com.paymentgateway.commons.dao.TransactionDetailsService;
import com.paymentgateway.commons.user.CustomerAddress;
import com.paymentgateway.commons.user.TransactionSearch;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.PDFCreator;

/**
 * @author Amitosh Aanand
 *
 */
@Service
public class DownloadInvoicePDFAction{

	@Autowired
	private PDFCreator pdfCreator;
	
	@Autowired
	private TransactionDetailsService transactionServiceDao;
	
	@Autowired
	FieldsDao fieldsDao;
	
	private static final long serialVersionUID = -7217148777324445941L;
	private static Logger logger = LoggerFactory.getLogger(DownloadInvoicePDFAction.class.getName());
	private Map<String, String> responseMap = new HashMap<String, String>();
	private HttpServletRequest httpRequest;
	public InputStream fileInputStream;
	public String fileName;
	public String orderId;
	
	public void setServletRequest(HttpServletRequest hReq) {
		this.httpRequest = hReq;
	}

	public String execute( String requestOrderId) {
		try {
			setOrderId(requestOrderId);
			TransactionSearch transactionSearch=transactionServiceDao.getTransactionForInvoicePdf(orderId);
			CustomerAddress customerAddress=fieldsDao.fetchCustomerInfo(orderId);
			logger.info("Inside execute(), DownloadInvoicePDFAction");
			if (StringUtils.isNotBlank(requestOrderId)) {
				fileName = "Invoice_" + requestOrderId + ".pdf";
			} else {
				fileName = "Invoice.pdf";
			}
			File file = new File(fileName);
			fileInputStream = pdfCreator.createCustomInvoicePdf(transactionSearch,customerAddress, file);
			return "success";
		} catch (Exception e) {
			logger.error("Exception caught while creating Invoice PDF, " + e);
			return "failed";
		}
	}
	
	public ResponseEntity<ByteArrayResource> merchantInvoicePdf(String requestOrderId) {
		try {
			setOrderId(requestOrderId);
			TransactionSearch transactionSearch=transactionServiceDao.getTransactionForInvoicePdf(orderId);
			CustomerAddress customerAddress=fieldsDao.fetchCustomerInfo(orderId);
			logger.info("Inside merchantInvoicePdf(), DownloadInvoicePDFAction");
			if (StringUtils.isNotBlank(orderId)) {
				fileName = "Invoice_" + orderId + ".pdf";
			} else {
				fileName = "Invoice.pdf";
			}
			File file = new File(fileName);
			fileInputStream = pdfCreator.createCustomInvoicePdf(transactionSearch,customerAddress, file);
			
			String FILE_EXTENSION = ".pdf";

			HttpHeaders respHeaders = new HttpHeaders();
			respHeaders.setContentType(new MediaType("application", "force-download"));
			respHeaders.setCacheControl("must-revalidate, post-check=0, pre-check=0");
			respHeaders.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName);

			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			byte[] buf = new byte[131072];
			int n = 0;
			while (-1 != (n = fileInputStream.read(buf))) {
				stream.write(buf, 0, n);
			}
			return new ResponseEntity<>(new ByteArrayResource(stream.toByteArray()), respHeaders,
					HttpStatus.CREATED);			
			
			
			
		} catch (Exception e) {
			logger.error("Exception caught while creating Invoice PDF, " + e);
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}

	public InputStream getFileInputStream() {
		return fileInputStream;
	}

	public void setFileInputStream(InputStream fileInputStream) {
		this.fileInputStream = fileInputStream;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getOrderId() {
		return orderId;
	}

	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}
	
}
