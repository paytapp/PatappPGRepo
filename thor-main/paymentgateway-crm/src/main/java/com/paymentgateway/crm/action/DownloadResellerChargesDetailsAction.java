/**
 * 
 */
package com.paymentgateway.crm.action;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.streaming.SXSSFCell;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.paymentgateway.commons.dao.ResellerChargesDao;
import com.paymentgateway.commons.user.ResellerCharges;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserType;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.DateCreater;
import com.paymentgateway.commons.util.PDFCreator;

/**
 * @author Amitosh Aanand, Rajit
 *
 */
public class DownloadResellerChargesDetailsAction extends AbstractSecureAction {

	private String payId;
	private String resellerId;
	private InputStream fileInputStream;
	private String fileName;
	private User sessionUser = new User();

	@Autowired
	private PDFCreator pdfCreator;

	@Autowired
	private ResellerChargesDao resellerChargesDao;

	private static final long serialVersionUID = -3592207257158972776L;
	private static Logger logger = LoggerFactory.getLogger(DownloadResellerChargesDetailsAction.class.getName());

	String dataHeading[] = { "Currency", "Mop", "Transaction Type", "Slab", "Reseller %", "Reseller Fixed Charge",
			"PG % From Reseller", "PG Fixed Charge From Reseller", "GST" };

	public String execute() {
		try {
			sessionUser = (User) sessionMap.get(Constants.USER.getValue());
			if (sessionUser.getUserType().equals(UserType.RESELLER)) {
				resellerId = sessionUser.getResellerId();
			}
			Map<String, List<ResellerCharges>> resellerChargesMap = getResellerChargesData(payId, resellerId);

			SXSSFWorkbook workbook = new SXSSFWorkbook();
			Sheet sheet = workbook.createSheet("Reseller Charges");
			int rowNumber = 0;
			int cellNumber = 0;

			Row row = sheet.createRow(rowNumber);
			XSSFCellStyle style = setCellStyle(workbook);
			sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 10));
			SXSSFCell cell = (SXSSFCell) row.createCell(cellNumber);
			cell.setCellStyle(style);
			cell.setCellValue("Reseller Charges Details");

			for (String resellerChargesMapKey : resellerChargesMap.keySet()) {
				List<ResellerCharges> resellerChargesMapList = resellerChargesMap.get(resellerChargesMapKey);

				cellNumber = 0;
				row = sheet.createRow(rowNumber += 1);
				sheet.addMergedRegion(new CellRangeAddress(rowNumber, rowNumber, 0, 9));
				cell = (SXSSFCell) row.createCell(cellNumber);
				cell.setCellStyle(style);
				cell.setCellValue(resellerChargesMapKey);

				for (ResellerCharges resellerCharges : resellerChargesMapList) {

					cellNumber = 0;
					row = sheet.createRow(rowNumber += 1);
					sheet.addMergedRegion(new CellRangeAddress(rowNumber, 0, 0, 9));
					cell = (SXSSFCell) row.createCell(cellNumber);
					cell.setCellStyle(style);

					rowNumber++;
					row = sheet.createRow(rowNumber++);
					while (cellNumber <= 8) {

						cell = (SXSSFCell) row.createCell(cellNumber);
						cell.setCellStyle(style);
						cell.setCellValue(dataHeading[cellNumber]);
						sheet.autoSizeColumn(cellNumber);
						cellNumber++;
					}

					row = sheet.createRow(rowNumber++);
					while (true) {
						cellNumber = 0;

						cell = (SXSSFCell) row.createCell(cellNumber++);
						cell.setCellStyle(style);
						cell.setCellValue(resellerCharges.getCurrency());

						cell = (SXSSFCell) row.createCell(cellNumber++);
						cell.setCellStyle(style);
						cell.setCellValue(resellerCharges.getMopType().getName());

						cell = (SXSSFCell) row.createCell(cellNumber++);
						cell.setCellStyle(style);
						cell.setCellValue(resellerCharges.getTransactionType().getName());

						cell = (SXSSFCell) row.createCell(cellNumber++);
						cell.setCellStyle(style);
						cell.setCellValue(resellerCharges.getSlabId());

						cell = (SXSSFCell) row.createCell(cellNumber++);
						cell.setCellStyle(style);
						cell.setCellValue(resellerCharges.getResellerPercentage());

						cell = (SXSSFCell) row.createCell(cellNumber++);
						cell.setCellStyle(style);
						cell.setCellValue(resellerCharges.getResellerFixedCharge());

						cell = (SXSSFCell) row.createCell(cellNumber++);
						cell.setCellStyle(style);
						cell.setCellValue(resellerCharges.getPgPercentage());

						cell = (SXSSFCell) row.createCell(cellNumber++);
						cell.setCellStyle(style);
						cell.setCellValue(resellerCharges.getPgFixedCharge());

						cell = (SXSSFCell) row.createCell(cellNumber++);
						cell.setCellStyle(style);
						cell.setCellValue(resellerCharges.getGst());

						break;
					}
				}
			}
			String FILE_EXTENSION = ".xlsx";
			DateFormat df = new SimpleDateFormat("yyyyMMddhhmmss");
			fileName = "Reseller_Charges_" + df.format(new Date()) + FILE_EXTENSION;
			File file = new File(fileName);

			// this Writes the workbook
			FileOutputStream out = new FileOutputStream(file);
			workbook.write(out);
			out.flush();
			out.close();
			workbook.dispose();
			fileInputStream = new FileInputStream(file);
			addActionMessage(fileName + " has written successfully.");

		} catch (

		Exception ex) {
			logger.info("Exception caught while creating Reseller charges excel file " + ex);
			return ERROR;
		}
		logger.info("Reseller charges excel file created successfully");
		return SUCCESS;
	}

	public XSSFCellStyle setCellStyle(Workbook wb) {
		XSSFCellStyle style = (XSSFCellStyle) wb.createCellStyle();
		XSSFFont font = (XSSFFont) wb.createFont();
		font.setBold(true);
		style.setFont(font);
		style.setBorderBottom(BorderStyle.THIN);
		style.setBottomBorderColor(IndexedColors.BLACK.getIndex());
		style.setBorderLeft(BorderStyle.THIN);
		style.setBottomBorderColor(IndexedColors.BLACK.getIndex());
		style.setBorderRight(BorderStyle.THIN);
		style.setBottomBorderColor(IndexedColors.BLACK.getIndex());
		style.setBorderTop(BorderStyle.THIN);
		style.setBottomBorderColor(IndexedColors.BLACK.getIndex());
		style.setAlignment(XSSFCellStyle.ALIGN_CENTER);
		return style;
	}

	public String pdfDownloader() {
		try {
			logger.info("Creating reseller charges PDF file");
			
			sessionUser = (User) sessionMap.get(Constants.USER.getValue());
			if (sessionUser.getUserType().equals(UserType.RESELLER)) {
				resellerId = sessionUser.getResellerId();
			}
			
			Map<String, List<ResellerCharges>> resellerChargesMap = getResellerChargesData(payId, resellerId);
			String currentDate = DateCreater.defaultFromDate();
			fileName = "Reseller_Charges_" + currentDate + ".pdf";
			File file = new File(fileName);
			fileInputStream = pdfCreator.creatResellerChargesPdf(resellerChargesMap, file, resellerId);
		} catch (Exception ex) {
			logger.info("Exception caught " + ex);
			return ERROR;
		}
		return SUCCESS;
	}

	public Map<String, List<ResellerCharges>> getResellerChargesData(String payId, String resellerId) {
		Map<String, List<ResellerCharges>> resellerChargesDataMap = new HashMap<String, List<ResellerCharges>>();
		try {
			List<ResellerCharges> resellerChargesList = resellerChargesDao.fetchChargesByResellerAndMerchant(payId,
					resellerId);
			Set<String> uniqueKeySet = new HashSet<String>();
			for (ResellerCharges resellerCharges : resellerChargesList) {
				String paymentType = resellerCharges.getPaymentType().getName();
				if (paymentType.equalsIgnoreCase("Net Banking") || paymentType.equalsIgnoreCase("COD")
						|| paymentType.equalsIgnoreCase("UPI") || paymentType.equalsIgnoreCase("EMI")
						|| paymentType.equalsIgnoreCase("Wallet")) {
					String uniqueKey = resellerCharges.getPaymentType().getName();
					uniqueKeySet.add(uniqueKey);
				} else {
					String uniqueKey = resellerCharges.getPaymentType().getName() + "-"
							+ resellerCharges.getPaymentsRegion().name() + "-"
							+ resellerCharges.getCardHolderType().name();
					uniqueKeySet.add(uniqueKey);
				}
			}

			for (String uniqueKey : uniqueKeySet) {
				boolean flag = false;
				List<ResellerCharges> resellerChargesObjectList = new ArrayList<ResellerCharges>();
				for (ResellerCharges resellerCharges : resellerChargesList) {
					String key = "";
					String paymentType = resellerCharges.getPaymentType().getName();
					if (paymentType.equalsIgnoreCase("Net Banking") || paymentType.equalsIgnoreCase("COD")
							|| paymentType.equalsIgnoreCase("UPI") || paymentType.equalsIgnoreCase("EMI")
							|| paymentType.equalsIgnoreCase("Wallet")) {
						key = resellerCharges.getPaymentType().getName();
						flag = true;
					} else {
						key = resellerCharges.getPaymentType().getName() + "-"
								+ resellerCharges.getPaymentsRegion().name() + "-"
								+ resellerCharges.getCardHolderType().name();
					}
					if (key != "" && uniqueKey.equalsIgnoreCase(key)) {
						resellerChargesObjectList.add(resellerCharges);
					}
				}
				if (!flag)
					resellerChargesDataMap.put("", resellerChargesObjectList);
				else
					resellerChargesDataMap.put(uniqueKey, resellerChargesObjectList);
			}

		} catch (Exception ex) {
			logger.info("Exception caught " + ex);
		}
		return resellerChargesDataMap;
	}

	public String getPayId() {
		return payId;
	}

	public void setPayId(String payId) {
		this.payId = payId;
	}

	public String getResellerId() {
		return resellerId;
	}

	public void setResellerId(String resellerId) {
		this.resellerId = resellerId;
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
