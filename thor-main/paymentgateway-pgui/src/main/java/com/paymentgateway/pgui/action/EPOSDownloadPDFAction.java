package com.paymentgateway.pgui.action;

import java.io.File;
import java.io.InputStream;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.struts2.interceptor.ServletRequestAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.paymentgateway.commons.dao.EPOSTransactionDao;
import com.paymentgateway.commons.user.EPOSTransaction;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.PDFCreator;

/**
 * @author Amitosh Aanand
 *
 */
public class EPOSDownloadPDFAction extends AbstractSecureAction implements ServletRequestAware {

	@Autowired
	private PDFCreator pdfCreator;

	@Autowired
	EPOSTransactionDao eposTransactionsDao;

	private String orderId;

	private static Logger logger = LoggerFactory.getLogger(EPOSDownloadPDFAction.class.getName());
	private HttpServletRequest httpRequest;
	public InputStream fileInputStream;
	public String fileName;

	private static final long serialVersionUID = -537920751314753582L;

	public String execute() {
		try {
			logger.info("Inside execute(), DownloadEPOSPDFAction");
			if (StringUtils.isBlank(orderId)) {
				orderId = (String) sessionMap.get(Constants.ORDER_ID.getValue());
			}
			EPOSTransaction epos = eposTransactionsDao.findByInvoiceId(orderId);
			fileName = "INVOICE_" + orderId + ".pdf";
			File file = new File(fileName);
			if (epos != null) {
				fileInputStream = pdfCreator.createEposPdf(epos, file);
			}
			return SUCCESS;
		} catch (Exception e) {
			logger.error("Exception caught while creating EPOS PDF, " + e);
			return ERROR;
		}
	}

	public void setServletRequest(HttpServletRequest hReq) {
		this.httpRequest = hReq;
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
