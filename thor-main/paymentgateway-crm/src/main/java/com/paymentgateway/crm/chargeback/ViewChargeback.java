package com.paymentgateway.crm.chargeback;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.paymentgateway.commons.dao.ChargebackDao;
import com.paymentgateway.commons.user.Chargeback;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.user.UserType;
import com.paymentgateway.commons.util.ChargebackEmailCreater;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.DateCreater;
import com.paymentgateway.commons.util.TransactionManager;
import com.paymentgateway.crm.action.AbstractSecureAction;

public class ViewChargeback extends AbstractSecureAction {

	@Autowired
	private ChargebackDao chargebackDao;

	@Autowired
	private UserDao userDao;
	
	@Autowired
	private ChargebackEmailCreater chargebackEmailCreater;
	
	private static final long serialVersionUID = 4208045338885337001L;
	private static Logger logger = LoggerFactory.getLogger(ViewChargeback.class.getName());
	private Chargeback chargeback = new Chargeback();

	private List<Chargeback> aaData;
	private String payId;
	private String subMerchantPayId;
	private String chargebackType;
	private String chargebackStatus;
	private String dateTo;
	private String dateFrom;
	private String caseId;
	private String fileName;
	private String orderId;
	private InputStream fileInputStream;

	public String execute() {
		try {
			User user = (User) sessionMap.get(Constants.USER);

			/*
			 * dateTo = String.valueOf(DateCreater.formatStringToDate(dateTo));
			 * dateFrom =
			 * String.valueOf(DateCreater.formatStringToDate(dateFrom));
			 * setDateTo(DateCreater.createDateTimeFormat(dateTo =
			 * dateTo+" 23:59:59"));
			 * setDateFrom(DateCreater.createDateTimeFormat(dateFrom =
			 * dateFrom+" 00:00:00"));
			 */

			// DateFormat format = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss",
			// Locale.ENGLISH);
			// setDateFrom(format.parse(dateFrom.toString());
			// setDateTo(format.parse(dateTo.toString());
			if (StringUtils.isNotBlank(chargebackStatus) && !chargebackStatus.equalsIgnoreCase("ALL")) {
				chargebackStatus = getStatus(chargebackStatus);
			}
			setDateFrom(DateCreater.createDateTimeFormat(DateCreater.formatDateforChargeback(dateFrom, " 00:00:00")));
			setDateTo(DateCreater.createDateTimeFormat(DateCreater.formatDateforChargeback(dateTo, " 23:59:59")));

			if (user.getUserType().equals(UserType.ADMIN) || user.getUserType().equals(UserType.SUBADMIN)
					|| user.getUserType().equals(UserType.SUPERADMIN)) {
				List<Chargeback> chargebackList = chargebackDao.findAllDistinctChargeback(getDateFrom(), getDateTo(),
						getPayId(), getSubMerchantPayId(), getChargebackType(), getChargebackStatus(), user, getOrderId());
				List<Chargeback> tempChargeBack = new ArrayList<Chargeback>();
				for (Chargeback c : chargebackList) {
					if (StringUtils.isNotBlank(c.getSubMerchantId())) {

						c.setBusinessName(userDao.getBusinessNameByPayId(c.getSubMerchantId()));
						c.setSuperMerchantName(userDao.getBusinessNameByPayId(c.getPayId()));
					} else {
						c.setBusinessName(userDao.getBusinessNameByPayId(c.getPayId()));
						c.setSuperMerchantName("NA");
					}
					if (StringUtils.isEmpty(c.getCloseDate())) {
						c.setCloseDate("NA");
					}

					Date createDate = c.getCreateDate();
					Date updateDate = c.getUpdateDate();
					String targetDate = c.getTargetDate();
					SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
					String createDateString = dateFormat.format(createDate);
					String updateDateString = dateFormat.format(updateDate);
					c.setUpdateDateString(updateDateString);
					c.setCreateDateString(createDateString);
					c.setTargetDate(targetDate + " 23:59:59");
					tempChargeBack.add(c);
				}
				setAaData(tempChargeBack);

			} else if (user.getUserType().equals(UserType.MERCHANT)) {
				if (user.isSuperMerchant()) {
					List<Chargeback> chargebackList = chargebackDao.findAllDistinctChargeback(getDateFrom(),
							getDateTo(), getPayId(), getSubMerchantPayId(), getChargebackType(), getChargebackStatus(), user, getOrderId());
					List<Chargeback> tempChargeBack = new ArrayList<Chargeback>();
					for (Chargeback c : chargebackList) {
						if (StringUtils.isNotBlank(c.getSubMerchantId())) {
							c.setBusinessName(userDao.getBusinessNameByPayId(c.getSubMerchantId()));
							c.setSuperMerchantName(user.getBusinessName());
						} else {
							c.setSuperMerchantName("NA");
						}
						if (StringUtils.isEmpty(c.getCloseDate())) {
							c.setCloseDate("NA");
						}
						Date createDate = c.getCreateDate();
						Date updateDate = c.getUpdateDate();
						String targetDate = c.getTargetDate();
						SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
						String createDateString = dateFormat.format(createDate);
						String updateDateString = dateFormat.format(updateDate);
						c.setUpdateDateString(updateDateString);
						c.setCreateDateString(createDateString);
						c.setTargetDate(targetDate + " 23:59:59");
						tempChargeBack.add(c);
					}
					setAaData(tempChargeBack);
					// setPayId(user.getSuperMerchantId());
				} else if (user.isSuperMerchant() == false && StringUtils.isNotEmpty(user.getSuperMerchantId())) {

					List<Chargeback> chargebackList = chargebackDao.findAllDistinctChargeback(getDateFrom(),
							getDateTo(), getPayId(), getSubMerchantPayId(), getChargebackType(), getChargebackStatus(), user, getOrderId());

					List<Chargeback> tempChargeBack = new ArrayList<Chargeback>();
					for (Chargeback c : chargebackList) {
						c.setBusinessName(user.getBusinessName());
						c.setSuperMerchantName(userDao.getBusinessNameByPayId(user.getSuperMerchantId()));
						if (StringUtils.isEmpty(c.getCloseDate())) {
							c.setCloseDate("NA");
						}

						Date createDate = c.getCreateDate();
						Date updateDate = c.getUpdateDate();
						String targetDate = c.getTargetDate();
						SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
						String createDateString = dateFormat.format(createDate);
						String updateDateString = dateFormat.format(updateDate);
						c.setUpdateDateString(updateDateString);
						c.setCreateDateString(createDateString);
						c.setTargetDate(targetDate + " 23:59:59");

						tempChargeBack.add(c);
					}
					setAaData(tempChargeBack);
				}

				else {
					List<Chargeback> chargebackList = chargebackDao.findAllDistinctChargeback(getDateFrom(),
							getDateTo(), getPayId(), getSubMerchantPayId(), getChargebackType(), getChargebackStatus(), user, getOrderId());

					List<Chargeback> tempChargeBack = new ArrayList<Chargeback>();
					for (Chargeback c : chargebackList) {
						c.setBusinessName(user.getBusinessName());
						c.setSuperMerchantName("NA");
						if (StringUtils.isEmpty(c.getCloseDate())) {
							c.setCloseDate("NA");
						}

						Date createDate = c.getCreateDate();
						Date updateDate = c.getUpdateDate();
						String targetDate = c.getTargetDate();
						SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
						String createDateString = dateFormat.format(createDate);
						String updateDateString = dateFormat.format(updateDate);
						c.setUpdateDateString(updateDateString);
						c.setCreateDateString(createDateString);
						c.setTargetDate(targetDate + " 23:59:59");

						tempChargeBack.add(c);
					}
					setAaData(tempChargeBack);
				}

				// setAaData(chargebackDao.findChargebackByPayid(user.getPayId(),
				// getDateFrom(), getDateTo()));
			} else if (user.getUserType().equals(UserType.SUBUSER)) {

				if (user.getSubUserType().equalsIgnoreCase("normalType")) {
					User parentUser = userDao.findPayId(user.getParentPayId());

					if (parentUser.isSuperMerchant() == false
							&& StringUtils.isNotBlank(parentUser.getSuperMerchantId())) {
						payId = parentUser.getSuperMerchantId();
						subMerchantPayId = parentUser.getPayId();
					} else {
						payId = parentUser.getPayId();
					}

					List<Chargeback> chargebackList = chargebackDao.findAllDistinctChargeback(getDateFrom(),
							getDateTo(), getPayId(), getSubMerchantPayId(), getChargebackType(), getChargebackStatus(), user, getOrderId());
					List<Chargeback> tempChargeBack = new ArrayList<Chargeback>();
					for (Chargeback c : chargebackList) {
						c.setBusinessName(parentUser.getBusinessName());
						if (StringUtils.isNotEmpty(subMerchantPayId)) {
							c.setSuperMerchantName(userDao.getBusinessNameByPayId(parentUser.getSuperMerchantId()));
						} else {
							c.setSuperMerchantName("NA");
						}
						if (StringUtils.isEmpty(c.getCloseDate())) {
							c.setCloseDate("NA");
						}
						Date createDate = c.getCreateDate();
						Date updateDate = c.getUpdateDate();
						String targetDate = c.getTargetDate();
						SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
						String createDateString = dateFormat.format(createDate);
						String updateDateString = dateFormat.format(updateDate);
						c.setUpdateDateString(updateDateString);
						c.setCreateDateString(createDateString);
						c.setTargetDate(targetDate + " 23:59:59");

						tempChargeBack.add(c);
					}
					setAaData(tempChargeBack);
				} else if (user.getSubUserType().equalsIgnoreCase("eposType")) {
					User parentUser = userDao.findPayId(user.getPayId());

					if (parentUser.isSuperMerchant() == false
							&& StringUtils.isNotBlank(parentUser.getSuperMerchantId())) {
						payId = parentUser.getSuperMerchantId();
						subMerchantPayId = parentUser.getPayId();
					} else {
						payId = parentUser.getPayId();
					}

					List<Chargeback> chargebackList = chargebackDao.findAllDistinctChargeback(getDateFrom(),
							getDateTo(), getPayId(), getSubMerchantPayId(), getChargebackType(), getChargebackStatus(), user, getOrderId());
					List<Chargeback> tempChargeBack = new ArrayList<Chargeback>();
					for (Chargeback c : chargebackList) {
						c.setBusinessName(parentUser.getBusinessName());
						if (StringUtils.isNotEmpty(subMerchantPayId)) {
							c.setSuperMerchantName(userDao.getBusinessNameByPayId(parentUser.getSuperMerchantId()));
						} else {
							c.setSuperMerchantName("NA");
						}
						if (StringUtils.isEmpty(c.getCloseDate())) {
							c.setCloseDate("NA");
						}
						Date createDate = c.getCreateDate();
						Date updateDate = c.getUpdateDate();
						String targetDate = c.getTargetDate();
						SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
						String createDateString = dateFormat.format(createDate);
						String updateDateString = dateFormat.format(updateDate);
						c.setUpdateDateString(updateDateString);
						c.setCreateDateString(createDateString);
						c.setTargetDate(targetDate + " 23:59:59");
						tempChargeBack.add(c);
					}
					setAaData(tempChargeBack);
				}
			}
			// else if (user.getUserType().equals(UserType.SUBUSER)) {
			// setAaData(chargebackDao.findChargebackByPayid(user.getPayId(),
			// getDateFrom(), getDateTo()));
			// }

			return SUCCESS;

		} catch (Exception exception) {
			logger.error("Exception", exception);
			return ERROR;

		}

	}

	public String updateCloseStatus() {
		try {
			User user = (User) sessionMap.get(Constants.USER.getValue());
			setDateFrom(DateCreater.createDateTimeFormat(DateCreater.formatDateforChargeback(dateFrom, " 00:00:00")));
			setDateTo(DateCreater.createDateTimeFormat(DateCreater.formatDateforChargeback(dateTo, " 23:59:59")));

			if (user.getUserType().equals(UserType.ADMIN) || user.getUserType().equals(UserType.SUBADMIN)) {
				Date dNow = new Date();
				String dateNow = DateCreater.formatDateForDb(dNow);
				Chargeback chargeback = chargebackDao.findByCaseId(caseId);
				chargeback.setId(TransactionManager.getNewTransactionId());
				chargeback.setStatus("Closed");
				chargeback.setCloseDate(dateNow);
				chargeback.setUpdateDate(dNow);
				if (user.getUserType().equals(UserType.ADMIN)) {
					chargeback.setChargebackStatus(Constants.CLOSED_BY_ADMIN.getValue());
				} else {
					chargeback.setChargebackStatus(Constants.CLOSED_BY_SUBADMIN.getValue());
				}
				chargebackDao.UpdateData(chargeback);
				
				//sending email to merchant & Team
				
				User txnUser = null;
				
				if(StringUtils.isNotBlank(chargeback.getSubMerchantId())){
					txnUser=userDao.findPayId(chargeback.getSubMerchantId());
				}else{
					txnUser = userDao.findPayId(chargeback.getPayId());
				}
				
				chargebackEmailCreater.sendChargebackClosedEmail(chargeback, txnUser);
				
				List<Chargeback> chargebackList = chargebackDao.findAllDistinctChargeback(getDateFrom(), getDateTo(),
						"ALL", "ALL", "ALL", "ALL", user, getOrderId());
				List<Chargeback> tempChargeBack = new ArrayList<Chargeback>();
				for (Chargeback c : chargebackList) {
					if (StringUtils.isNotBlank(c.getSubMerchantId())) {

						c.setBusinessName(userDao.getBusinessNameByPayId(c.getSubMerchantId()));
						c.setSuperMerchantName(userDao.getBusinessNameByPayId(c.getPayId()));
					} else {
						c.setBusinessName(userDao.getBusinessNameByPayId(c.getPayId()));
						c.setSuperMerchantName("NA");
					}
					if (StringUtils.isEmpty(c.getCloseDate())) {
						c.setCloseDate("NA");
					}
					tempChargeBack.add(c);
				}
				setAaData(tempChargeBack);
			}

		} catch (Exception exception) {
			logger.error("Exception", exception);
			return ERROR;
		}

		return SUCCESS;
	}


	public String getStatus(String chargebackStatus) {
		String status = "";
		switch (chargebackStatus) {
		case "OPEN":
			status = "Open";
			break;
		case "CLOSED":
			status = "Closed";
			break;
		case "REJECTED":
			status = "Rejected";
			break;
		case "ACCEPTED":
			status = "Accepted";
			break;
		case "REFUNDED":
			status = "Refunded";
			break;
		}

		return status;
	}

	public String downloadChargeback() {
		logger.info("Inside downloadChargeback() ");
		try {
			User user = (User) sessionMap.get(Constants.USER.getValue());
						
			dateFrom=dateFrom+" 00:00:00"; 
			dateTo=dateTo+" 23:59:59"; 

			List<Chargeback> chargebackList = chargebackDao.findAllDistinctChargeback(getDateFrom(), getDateTo(),
					getPayId(), getSubMerchantPayId(), getChargebackType(), getChargebackStatus(), user, getOrderId());

			SXSSFWorkbook wb = new SXSSFWorkbook(100);
			Row row;
			int rownum = 1;
			// Create a blank sheet
			Sheet sheet = wb.createSheet("Chargeback Report");
			row = sheet.createRow(0);

			row.createCell(0).setCellValue("Case ID");
			row.createCell(1).setCellValue("Order ID");
			row.createCell(2).setCellValue("Super Merchant");
			row.createCell(3).setCellValue("Merchant");
			row.createCell(4).setCellValue("Type");
			row.createCell(5).setCellValue("Status");
			row.createCell(6).setCellValue("Amount");
			row.createCell(7).setCellValue("Target Date");
			row.createCell(8).setCellValue("Create Date");
			row.createCell(9).setCellValue("Close Date");

			for (Chargeback chargeback : chargebackList) {
				row = sheet.createRow(rownum++);
				
				if (StringUtils.isNotBlank(chargeback.getSubMerchantId())) {

					chargeback.setBusinessName(userDao.getBusinessNameByPayId(chargeback.getSubMerchantId()));
					chargeback.setSuperMerchantName(userDao.getBusinessNameByPayId(chargeback.getPayId()));
				} else {
					chargeback.setBusinessName(userDao.getBusinessNameByPayId(chargeback.getPayId()));
					chargeback.setSuperMerchantName("NA");
				}
				if (StringUtils.isEmpty(chargeback.getCloseDate())) {
					chargeback.setCloseDate("NA");
				}
				
				SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
				String createDateString = dateFormat.format(chargeback.getCreateDate());
				
				chargeback.setCreateDateString(createDateString);
				

				Object[] objArr = chargeback.downloadCsvFields();

				int cellnum = 0;
				for (Object obj : objArr) {

					Cell cell = row.createCell(cellnum++);

					if (obj instanceof String)
						cell.setCellValue((String) obj);
					else if (obj instanceof Integer)
						cell.setCellValue((Integer) obj);

				}
			}

			String FILE_EXTENSION = ".csv";
			DateFormat df = new SimpleDateFormat("yyyyMMddhhmmss");
			fileName = "Chargeback_Report_" + df.format(new Date()) + FILE_EXTENSION;
			File file = new File(fileName);

			// this Writes the workbook
			FileOutputStream out = new FileOutputStream(file);
			wb.write(out);
			out.flush();
			out.close();
			wb.dispose();
			fileInputStream = new FileInputStream(file);

			logger.info("File generated successfully for Chargeback Report");
		} catch (Exception e) {
			logger.info("exception while downloading Chargeback file ", e);
		}
		return SUCCESS;
	}

	public Chargeback getChargeback() {
		return chargeback;
	}

	public void setChargeback(Chargeback chargeback) {
		this.chargeback = chargeback;
	}

	public List<Chargeback> getAaData() {
		return aaData;
	}

	public void setAaData(List<Chargeback> aaData) {
		this.aaData = aaData;
	}

	public String getPayId() {
		return payId;
	}

	public void setPayId(String payId) {
		this.payId = payId;
	}

	public String getChargebackType() {
		return chargebackType;
	}

	public void setChargebackType(String chargebackType) {
		this.chargebackType = chargebackType;
	}

	public String getChargebackStatus() {
		return chargebackStatus;
	}

	public void setChargebackStatus(String chargebackStatus) {
		this.chargebackStatus = chargebackStatus;
	}

	public String getDateTo() {
		return dateTo;
	}

	public void setDateTo(String dateTo) {
		this.dateTo = dateTo;
	}

	public String getDateFrom() {
		return dateFrom;
	}

	public void setDateFrom(String dateFrom) {
		this.dateFrom = dateFrom;
	}

	public String getSubMerchantPayId() {
		return subMerchantPayId;
	}

	public void setSubMerchantPayId(String subMerchantPayId) {
		this.subMerchantPayId = subMerchantPayId;
	}

	public String getCaseId() {
		return caseId;
	}

	public void setCaseId(String caseId) {
		this.caseId = caseId;
	}

	public String getFileName() {
		return fileName;
	}

	public InputStream getFileInputStream() {
		return fileInputStream;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public void setFileInputStream(InputStream fileInputStream) {
		this.fileInputStream = fileInputStream;
	}

	public String getOrderId() {
		return orderId;
	}

	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}

}
