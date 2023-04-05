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
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.paymentgateway.commons.dao.RouterConfigurationDao;
import com.paymentgateway.commons.user.PermissionType;
import com.paymentgateway.commons.user.RouterConfiguration;
import com.paymentgateway.commons.util.MopType;
import com.paymentgateway.commons.util.PaymentType;

/*
 * @author Rajit
*/
public class DownloadSmartRouterConfigSheet extends AbstractSecureAction {

	private static final long serialVersionUID = 2148671641328438422L;
	private static Logger logger = LoggerFactory.getLogger(DownloadSmartRouterConfigSheet.class.getName());
	
	@Autowired
	private RouterConfigurationDao routerConfigurationDao;
	
	private String payId;
	private InputStream fileInputStream;
	private String filename;
	
	String headingPart[] = { "Acquirer_Name", "Status", "Description", "Mode", "Payment Type", "Mop Type",
			"Allowed Fail Count", "Always On", "Load%", "Priority", "Retry Time", "Minimum transaction",
			"Maximum Transaction", "Acquirer Mode" };

	public String execute() {

		logger.info("inside download smart router config excel sheet ");
		
		Map<String, List<RouterConfiguration>> routerRuleDataMap = new HashMap<String, List<RouterConfiguration>>();
		
		List<RouterConfiguration> activeRouterList = new ArrayList<RouterConfiguration>();
		
		activeRouterList = routerConfigurationDao.getActiveRulesByMerchant(payId);
		
		Set<String> identifierKeySet = new HashSet<String>();

		for (RouterConfiguration routerConfiguration : activeRouterList) {

			String identifier = routerConfiguration.getPaymentType() + "-" + routerConfiguration.getMopType() + "-"
					+ routerConfiguration.getMerchant() + "-" + routerConfiguration.getTransactionType() + "-"
					+ routerConfiguration.getCurrency()+ "-"+routerConfiguration.getPaymentsRegion()+ "-"+routerConfiguration.getCardHolderType()+ "-"+routerConfiguration.getSlabId();

			identifierKeySet.add(identifier);
		}

		for (String uniqueKey : identifierKeySet) {
			List<RouterConfiguration> routerConfigurationList = new ArrayList<RouterConfiguration>();

			for (RouterConfiguration routerConfig : activeRouterList) {

				String key = routerConfig.getPaymentType() + "-" + routerConfig.getMopType() + "-"
						+ routerConfig.getMerchant() + "-" + routerConfig.getTransactionType() + "-"
						+ routerConfig.getCurrency()+ "-"+routerConfig.getPaymentsRegion()+ "-"+routerConfig.getCardHolderType()+ "-"+routerConfig.getSlabId();
				
				
				if (key.equalsIgnoreCase(uniqueKey)) {

					String paymentTypeName = PaymentType.getpaymentName(routerConfig.getPaymentType());
					String mopTypeName = MopType.getmopName(routerConfig.getMopType());

					routerConfig.setPaymentTypeName(paymentTypeName);
					routerConfig.setMopTypeName(mopTypeName);

					routerConfig.setStatusName(routerConfig.getStatusName());
					routerConfigurationList.add(routerConfig);
				}

			}

			routerRuleDataMap.put(uniqueKey, routerConfigurationList);
		}
		
		SXSSFWorkbook workbook = new SXSSFWorkbook();
		Sheet sheet = workbook.createSheet(PermissionType.SMART_ROUTER.getPermission());
		int rowNumber = 0;
		int cellNumber = 0;
		try {

			Row row = sheet.createRow(rowNumber);
			XSSFCellStyle style = setCellStyle(workbook);
			XSSFCellStyle cellDataStyle = setCellStyleData(workbook);
			
			SXSSFCell cell = (SXSSFCell) row.createCell(cellNumber);
			cell.setCellStyle(style);
			sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 13));
			cell.setCellValue(PermissionType.SMART_ROUTER.getPermission());

			for (Map.Entry<String, List<RouterConfiguration>> routerConfig : routerRuleDataMap.entrySet()) {
				cellNumber = 0;
				row = sheet.createRow(rowNumber += 2);
				sheet.addMergedRegion(new CellRangeAddress(rowNumber, 0, 0, 4));
				cell = (SXSSFCell) row.createCell(cellNumber);
				cell.setCellStyle(style);
				cell.setCellValue(routerConfig.getKey());

				rowNumber++;
				row = sheet.createRow(rowNumber++);
				while (cellNumber < 14) {

					cell = (SXSSFCell) row.createCell(cellNumber);
					cell.setCellStyle(style);
					cell.setCellValue(headingPart[cellNumber]);
					cellNumber++;
				}
				
				for (RouterConfiguration config : routerConfig.getValue()) {

					row = sheet.createRow(rowNumber++);
					while (true) {
						cellNumber = 0;
						cell = (SXSSFCell) row.createCell(cellNumber++);
						cell.setCellStyle(cellDataStyle);
						cell.setCellValue(config.getAcquirer());

						cell = (SXSSFCell) row.createCell(cellNumber++);
						cell.setCellStyle(cellDataStyle);
						cell.setCellValue(config.isCurrentlyActive());

						cell = (SXSSFCell) row.createCell(cellNumber++);
						cell.setCellStyle(cellDataStyle);
						cell.setCellValue(config.getStatusName());

						cell = (SXSSFCell) row.createCell(cellNumber++);
						cell.setCellStyle(cellDataStyle);
						cell.setCellValue(config.getMode());

						cell = (SXSSFCell) row.createCell(cellNumber++);
						cell.setCellStyle(cellDataStyle);
						cell.setCellValue(config.getPaymentType());

						cell = (SXSSFCell) row.createCell(cellNumber++);
						cell.setCellStyle(cellDataStyle);
						cell.setCellValue(config.getMopType());

						cell = (SXSSFCell) row.createCell(cellNumber++);
						cell.setCellStyle(cellDataStyle);
						cell.setCellValue(config.getAllowedFailureCount());

						cell = (SXSSFCell) row.createCell(cellNumber++);
						cell.setCellStyle(cellDataStyle);
						cell.setCellValue(config.isAlwaysOn());

						cell = (SXSSFCell) row.createCell(cellNumber++);
						cell.setCellStyle(cellDataStyle);
						cell.setCellValue(config.getLoadPercentage());

						cell = (SXSSFCell) row.createCell(cellNumber++);
						cell.setCellStyle(cellDataStyle);
						cell.setCellValue(config.getPriority());

						cell = (SXSSFCell) row.createCell(cellNumber++);
						cell.setCellStyle(cellDataStyle);
						cell.setCellValue(config.getRetryMinutes());

						cell = (SXSSFCell) row.createCell(cellNumber++);
						cell.setCellStyle(cellDataStyle);
						cell.setCellValue(config.getMinAmount());

						cell = (SXSSFCell) row.createCell(cellNumber++);
						cell.setCellStyle(cellDataStyle);
						cell.setCellValue(config.getMaxAmount());

						cell = (SXSSFCell) row.createCell(cellNumber++);
						cell.setCellStyle(cellDataStyle);
						cell.setCellValue(config.getOnUsoffUsName());
						break;
					}
				}
			}
			
			String FILE_EXTENSION = ".xlsx";
			DateFormat df = new SimpleDateFormat("yyyyMMddhhmmss");
			filename = "Smart_Router" + df.format(new Date())+FILE_EXTENSION;
			File file = new File(filename);

			// this Writes the workbook
			FileOutputStream out = new FileOutputStream(file);
			workbook.write(out);
			out.flush();
			out.close();
			workbook.dispose();
			fileInputStream = new FileInputStream(file);
			addActionMessage(filename + " written successfully on disk.");

		} catch (Exception ex) {
			logger.info("Exception caught while download smart router excel file " + ex);
		}
		logger.info("Successfully download smart router excel file ");
		return SUCCESS;
	}

	public int setHeader(Sheet sheet, Row row, SXSSFCell cell, XSSFCellStyle style, int rowNumber,
			int cellNumber) {

		int count = 0;
		row = sheet.createRow(rowNumber);
		String headingPart[] = { "Acquirer_Name", "Status", "Description", "Mode", "Payment Type", "Mop Type",
				"Allowed Fail Count", "Always On", "Load%", "Priority", "Retry Time", "Minimum transaction",
				"Maximum Transaction", "Acquirer Mode" };
		while (count < 14) {

			cell = (SXSSFCell) row.createCell(cellNumber);
			cell.setCellStyle(style);
			cell.setCellValue(headingPart[count]);
			cellNumber++;
			count++;
		}
		return rowNumber;
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
	
	public XSSFCellStyle setCellStyleData(Workbook wb) {
		XSSFCellStyle style = (XSSFCellStyle) wb.createCellStyle();
		XSSFFont font = (XSSFFont) wb.createFont();
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
	
	public String getPayId() {
		return payId;
	}

	public void setPayId(String payId) {
		this.payId = payId;
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
