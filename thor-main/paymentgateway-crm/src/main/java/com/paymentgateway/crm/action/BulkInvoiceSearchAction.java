package com.paymentgateway.crm.action;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.struts2.interceptor.validation.SkipValidation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.paymentgateway.commons.user.Invoice;
import com.paymentgateway.commons.user.InvoiceHistory;
import com.paymentgateway.commons.user.InvoiceTransactionDao;
import com.paymentgateway.commons.user.Merchants;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.user.UserType;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.CrmFieldType;
import com.paymentgateway.commons.util.CrmValidator;
import com.paymentgateway.commons.util.StatusType;

/**
 * @author Shiva
 *
 */
public class BulkInvoiceSearchAction extends AbstractSecureAction {

	private static Logger logger = LoggerFactory.getLogger(BulkInvoiceSearchAction.class.getName());

	@Autowired
	CrmValidator validator;

	@Autowired
	InvoiceTransactionDao transactionDao;

	@Autowired
	UserDao userDao;

	private static final long serialVersionUID = -7395607356282489429L;

	private String dateFrom;
	private String dateTo;
	List<Invoice> dataList;
	List<Invoice> aaData;
	List<InvoiceHistory> invoiceHistories;
	List<String> fileNames;
	private String filename;
	private String filenames;
	private String merchantPayId;
	private String subMerchantId;
	private String subUserId;
	private InputStream fileInputStream;

	public String execute() {
		logger.info("Inside execute(); BulkInvoiceSearchAction");
		User sessionUser = (User) sessionMap.get(Constants.USER.getValue());
		try {

			fileNames = new ArrayList<>();
			dataList = new ArrayList<>();
			invoiceHistories = new ArrayList<>();

			if (sessionUser.getUserType().equals(UserType.SUBUSER)) {

				// currencyMap = Currency.getSupportedCurreny(user);
				String parentPayId = sessionUser.getParentPayId();
				User parentUser = userDao.findPayId(parentPayId);
				String subUserId = "";
				if (!userDao.isSubUserPrevilageTypeAll(sessionUser)) {
					subUserId = sessionUser.getPayId();
				}
				if (!parentUser.isSuperMerchant() && StringUtils.isNotBlank(parentUser.getSuperMerchantId())) {
					merchantPayId = parentUser.getSuperMerchantId();
					subMerchantId = parentUser.getPayId();
				} else {
					merchantPayId = parentUser.getPayId();
				}
			} else if (sessionUser.getUserType().equals(UserType.MERCHANT)) {

				if (!sessionUser.isSuperMerchant() && StringUtils.isNotBlank(sessionUser.getSuperMerchantId())) {

					merchantPayId = sessionUser.getSuperMerchantId();
					subMerchantId = sessionUser.getPayId();
				}

			}

			aaData = transactionDao.filterFileByDate(dateFrom, dateTo, merchantPayId, subMerchantId, subUserId);

			for (Invoice in : aaData) {

				try {
					if (StringUtils.isNotBlank(in.getFileName())) {
						if (!(fileNames.contains(in.getFileName()))) {
							fileNames.add(in.getFileName());
						}
						dataList.add(in);
					}
				} catch (Exception e) {
					logger.error("error " , e);
				}

			}
			for (int i = 0; i < fileNames.size(); i++) {
				{
					long totalRecords = 0;
					long totalSuccess = 0;
					long totalUnsent = 0;
					long totalPending = 0;

					InvoiceHistory invoiceHistory = new InvoiceHistory();
					invoiceHistory.setFileName(fileNames.get(i));

					for (Invoice dl : dataList) {

						if (dl.getFileName().equalsIgnoreCase(invoiceHistory.getFileName())) {
							totalRecords++;
							invoiceHistory.setDate(dl.getCreateDate());
							invoiceHistory.setBusinessName(dl.getBusinessName());
							invoiceHistory.setSubMerchantbusinessName(dl.getSubMerchantbusinessName());
							if (dl.getStatus().equalsIgnoreCase("Active")) {
								totalSuccess++;
							} else if (dl.getStatus().equalsIgnoreCase(StatusType.PENDING.getName())) {
								totalPending++;
							}

							if (!(dl.isEmailStatus() || dl.isSmsStatus())) {
								totalUnsent++;
							}

						}
					}
					invoiceHistory.setSuccess(totalSuccess);
					invoiceHistory.setTotalPending(totalPending);
					invoiceHistory.setTotalRecords(totalRecords);
					invoiceHistory.setTotalUnsent(totalUnsent);
					invoiceHistories.add(invoiceHistory);
				}

			}

			setInvoiceHistories(invoiceHistories);
		} catch (Exception e) {
			logger.info("exception " + e);
		}
		return SUCCESS;
	}

	@SkipValidation
	public String fileDownload() {
		try {

			List<Invoice> invoices = new ArrayList<>();
			if (StringUtils.isNoneBlank(filename)) {
				invoices = transactionDao.findAllInvoiceByFileName(filename);

				SXSSFWorkbook wb = new SXSSFWorkbook(5000);
				Row row;
				int rownum = 1;
				// Create a blank sheet
				Sheet sheet = wb.createSheet("Bulk Invoice");
				row = sheet.createRow(0);

				row.createCell(0).setCellValue("Name");
				row.createCell(1).setCellValue("Phone");
				row.createCell(2).setCellValue("Email");
				row.createCell(3).setCellValue("Product Name");
				row.createCell(4).setCellValue("Product Description");
				row.createCell(5).setCellValue("Duration (From)");
				row.createCell(6).setCellValue("Duration (To)");
				row.createCell(7).setCellValue("Expiry Date");
				row.createCell(8).setCellValue("Expiry Time");
				row.createCell(9).setCellValue("Currency Type");
				row.createCell(10).setCellValue("Quantity");
				row.createCell(11).setCellValue("Amount");
				row.createCell(12).setCellValue("Service");
				row.createCell(13).setCellValue("Address");
				row.createCell(14).setCellValue("Country");
				row.createCell(15).setCellValue("State");
				row.createCell(16).setCellValue("City");
				row.createCell(17).setCellValue("Pin");
				row.createCell(18).setCellValue("Status");

				for (Invoice invo : invoices) {
					row = sheet.createRow(rownum++);

					Object[] objArr = invo.myCsvMethodDownload();

					int cellnum = 0;
					for (Object obj : objArr) {
						// this line creates a cell in the next column of that
						// row
						Cell cell = row.createCell(cellnum++);
						if (obj instanceof String)
							cell.setCellValue((String) obj);
						else if (obj instanceof Integer)
							cell.setCellValue((Integer) obj);
					}
				}

				try {
					String FILE_EXTENSION = ".csv";

					filename = filename + FILE_EXTENSION;
					File file = new File(filename);
					setFilename(filename);
					// this Writes the workbook
					FileOutputStream out = new FileOutputStream(file);
					wb.write(out);
					out.flush();
					out.close();
					wb.dispose();
					fileInputStream = new FileInputStream(file);
					setFileInputStream(fileInputStream);
					addActionMessage(filename + " written successfully.");
					logger.info("File generated successfully for Bulk Invoice");
				} catch (Exception exception) {
					logger.error("Exception", exception);
				}
			}
		} catch (Exception ex) {
			logger.error("Caught exception " , ex);
			return SUCCESS;
		}
		return SUCCESS;
	}

	public void validate() {

		if ((validator.validateBlankField(getDateFrom()))) {
			addFieldError(CrmFieldType.DATE_FROM.getName(), validator.getResonseObject().getResponseMessage());
		} else if (!(validator.validateField(CrmFieldType.DATE_FROM, getDateFrom()))) {
			addFieldError(CrmFieldType.DATE_FROM.getName(), validator.getResonseObject().getResponseMessage());
		}
		if ((validator.validateBlankField(getDateTo()))) {
			addFieldError(CrmFieldType.DATE_TO.getName(), validator.getResonseObject().getResponseMessage());
		} else if (!(validator.validateField(CrmFieldType.DATE_TO, getDateTo()))) {
			addFieldError(CrmFieldType.DATE_TO.getName(), validator.getResonseObject().getResponseMessage());
		}

	}

	public String getDateFrom() {
		return dateFrom;
	}

	public void setDateFrom(String dateFrom) {
		this.dateFrom = dateFrom;
	}

	public String getDateTo() {
		return dateTo;
	}

	public void setDateTo(String dateTo) {
		this.dateTo = dateTo;
	}

	public List<InvoiceHistory> getInvoiceHistories() {
		return invoiceHistories;
	}

	public void setInvoiceHistories(List<InvoiceHistory> invoiceHistories) {
		this.invoiceHistories = invoiceHistories;
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

	public String getFilenames() {
		return filenames;
	}

	public void setFilenames(String filenames) {
		this.filenames = filenames;
	}

	public String getMerchantPayId() {
		return merchantPayId;
	}

	public void setMerchantPayId(String merchantPayId) {
		this.merchantPayId = merchantPayId;
	}

	public String getSubMerchantId() {
		return subMerchantId;
	}

	public void setSubMerchantId(String subMerchantId) {
		this.subMerchantId = subMerchantId;
	}

	public String getSubUserId() {
		return subUserId;
	}

	public void setSubUserId(String subUserId) {
		this.subUserId = subUserId;
	}

}
