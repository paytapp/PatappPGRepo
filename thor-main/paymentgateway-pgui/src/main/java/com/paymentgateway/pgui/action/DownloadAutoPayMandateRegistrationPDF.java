package com.paymentgateway.pgui.action;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.paymentgateway.commons.dao.UpiAutoPayDao;
import com.paymentgateway.commons.util.PDFCreator;

/**
 * @author Rajit
 */
public class DownloadAutoPayMandateRegistrationPDF {

	@Autowired
	private PDFCreator pdfCreator;

	@Autowired
	private UpiAutoPayDao upiAutoPayDao;

	private static Logger logger = LoggerFactory.getLogger(DownloadAutoPayMandateRegistrationPDF.class.getName());

	private String orderId;

	private InputStream fileInputStream;
	private String fileName;

	public InputStream downloadUpiAutoPayPdfHandler(HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		try {
			logger.info("creating autoPay mandate PDF file");

			Map<String, String> registrationDetails = upiAutoPayDao.getAutoPayMandateDetailsByOrderId(orderId);
			DateFormat df = new SimpleDateFormat("yyyyMMddhhmmss");
			fileName = "Upi_AutoPay_Mandate_" + df.format(new Date()) + ".pdf";
			File file = new File(fileName);

			fileInputStream = pdfCreator.createAutoPayRegistrationPdf(registrationDetails, file);
		} catch (Exception ex) {
			logger.info("Exception caught " + ex);
			String path = request.getContextPath();
			logger.info(path);
			if (!StringUtils.isNotBlank(path) && !path.contains("pgui")) {
				String resultPath = request.getScheme() + "://" + request.getHeader("Host") + "/pgui/jsp/error";
				response.sendRedirect(resultPath);
			}
			response.sendRedirect("error");
		}
		return fileInputStream;
	}

	public String getOrderId() {
		return orderId;
	}

	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public InputStream getFileInputStream() {
		return fileInputStream;
	}

	public void setFileInputStream(InputStream fileInputStream) {
		this.fileInputStream = fileInputStream;
	}
}
