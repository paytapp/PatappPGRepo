package com.paymentgateway.crm.action;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.paymentgateway.commons.dao.PendingInitiatedRefundFileDao;
import com.paymentgateway.commons.user.PendingInitiatedRefundData;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserType;
import com.paymentgateway.commons.util.Constants;

public class DownloadInitiatedRefundFileAction extends AbstractSecureAction {

	private static Logger logger = LoggerFactory.getLogger(DownloadInitiatedRefundFileAction.class.getName());

	private static final long serialVersionUID = 7693954383250680745L;

	@Autowired
	private PendingInitiatedRefundFileDao pendingInitiatedRefundFileDao;

	private String acquirerType;
	private String refundInitiatedFrom;
	private String refundInitiatedTo;
	private InputStream fileInputStream;
	private String filename;

	public String downloadNBSbiFile() {
		logger.info("inside downloadNBSbiFile()");

		Date currentDate = new Date();

		User sessionUser = (User) sessionMap.get(Constants.USER.getValue());

		List<PendingInitiatedRefundData> initiatedRefundList = new ArrayList<PendingInitiatedRefundData>();

		if (sessionUser.getUserType().equals(UserType.ADMIN) || sessionUser.getUserType().equals(UserType.SUBADMIN)) {

			if (StringUtils.isNotBlank(acquirerType) && StringUtils.isNotBlank(refundInitiatedFrom)
					&& StringUtils.isNotBlank(refundInitiatedTo)) {

				initiatedRefundList = pendingInitiatedRefundFileDao.findAllInitiatedRefund(acquirerType,
						refundInitiatedFrom, refundInitiatedTo);
				logger.info("List generated successfully for DownloadTransactionsReportAction");

			} else {
				logger.info("empty acquirerType or refundInitiated date");

			}
		}

		try {

			SimpleDateFormat fileNameFormatter = new SimpleDateFormat("dd.MM.yyyy");
			String FILE_EXTENSION = ".txt";

			filename = "BTPY_ SBI_Refund_" + fileNameFormatter.format(currentDate) + FILE_EXTENSION;

			File file = new File(filename);

			Writer writer = new BufferedWriter(new FileWriter(file));

			StringBuilder record = new StringBuilder();
			
			record.append("Txn code");
			record.append("|");
			record.append("Txn Date");
			record.append("|");
			record.append("Refund Date");
			record.append("|");
			record.append("Bank Ref No");
			record.append("|");
			record.append("Txn Amount");
			record.append("|");
			record.append("Refund Amount\n");
			
			for (PendingInitiatedRefundData data : initiatedRefundList) {

				record.append(data.getTxnCode());
				record.append("|");
				record.append(data.getTxnDate());
				record.append("|");
				record.append(data.getRefundDate());
				record.append("|");
				record.append(data.getBankRefNo());
				record.append("|");
				record.append(data.getTxnAmount());
				record.append("|");
				record.append(data.getRefundAmount()+"\n");
			}
			record.deleteCharAt(record.toString().length() -1);
			writer.write(record.toString()+"\n");
			writer.close();

			fileInputStream = new FileInputStream(file);

			file.delete();

		} catch (Exception exception) {
			logger.error("Exception", exception);
		}finally {
			
		}

		return SUCCESS;

	}

	public String getAcquirerType() {
		return acquirerType;
	}

	public void setAcquirerType(String acquirerType) {
		this.acquirerType = acquirerType;
	}

	public String getRefundInitiatedFrom() {
		return refundInitiatedFrom;
	}

	public void setRefundInitiatedFrom(String refundInitiatedFrom) {
		this.refundInitiatedFrom = refundInitiatedFrom;
	}

	public String getRefundInitiatedTo() {
		return refundInitiatedTo;
	}

	public void setRefundInitiatedTo(String refundInitiatedTo) {
		this.refundInitiatedTo = refundInitiatedTo;
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
