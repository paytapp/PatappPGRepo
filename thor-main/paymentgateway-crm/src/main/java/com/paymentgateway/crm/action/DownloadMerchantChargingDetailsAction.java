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

import org.apache.commons.lang3.StringUtils;
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

import com.paymentgateway.commons.user.ChargingDetails;
import com.paymentgateway.commons.user.ChargingDetailsDao;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.util.DateCreater;
import com.paymentgateway.commons.util.PDFCreator;

public class DownloadMerchantChargingDetailsAction extends AbstractSecureAction{

	private static final long serialVersionUID = -1421647537819166712L;
	private static Logger logger = LoggerFactory.getLogger(DownloadMerchantChargingDetailsAction.class.getName());
	
	@Autowired
	private UserDao userDao;

	@Autowired
	private ChargingDetailsDao chargingDetailsDao;
	
	@Autowired
	private PDFCreator pdfCreator;
	
	private String payId;
	private InputStream fileInputStream;
	private String fileName;
	
	String dataHeading[] = {"Merchant","Currency","Mop","Transaction",/*"Slab",*/"PG TDR","PG FC",
							"Bank TDR","Bank FC","Reseller TDR","Reseller FC","Merchant TDR","Merchant FC","Merchant GST","Min Txn","Max Txn",
							"Max Charge Merchant","Max Charge Acquirer"/*,"Acquirer Mode"*/};
	
	public String execute() {
		
		try {
			String merchantName = userDao.getBusinessNameByPayId(payId);
			Map<String, List<Map<String, List<ChargingDetails>>>> acquirerTypeDataMap = getChargingData(payId,merchantName);
			
			SXSSFWorkbook workbook = new SXSSFWorkbook();
			Sheet sheet = workbook.createSheet("Charging Details");
			int rowNumber = 0;
			int cellNumber = 0;
			
			Row row = sheet.createRow(rowNumber);
			XSSFCellStyle style = setCellStyle(workbook);
			sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 15));
			SXSSFCell cell = (SXSSFCell) row.createCell(cellNumber);
			cell.setCellStyle(style);
			cell.setCellValue("Merchant's Charging Details");
			
			for (String  acquirerTypeDataMapKey : acquirerTypeDataMap.keySet()) {
				List<Map<String, List<ChargingDetails>>> chargingDetailsMapList = acquirerTypeDataMap.get(acquirerTypeDataMapKey);
				
				cellNumber = 0;
				row = sheet.createRow(rowNumber += 2);
				sheet.addMergedRegion(new CellRangeAddress(rowNumber, rowNumber, 0, 15));	
				cell = (SXSSFCell) row.createCell(cellNumber);
				cell.setCellStyle(style);
				cell.setCellValue(acquirerTypeDataMapKey);
				
				for(Map<String, List<ChargingDetails>> chargingDetailsMap : chargingDetailsMapList) {
					
					for(Map.Entry<String, List<ChargingDetails>> chargingDetailsMapData : chargingDetailsMap.entrySet()) {
						
						cellNumber = 0;
						row = sheet.createRow(rowNumber += 2);
						sheet.addMergedRegion(new CellRangeAddress(rowNumber, 0, 0, 15));
						cell = (SXSSFCell) row.createCell(cellNumber);
						cell.setCellStyle(style);
						cell.setCellValue(chargingDetailsMapData.getKey());
						
						rowNumber++;
						row = sheet.createRow(rowNumber++);
						while (cellNumber < 17) {

							cell = (SXSSFCell) row.createCell(cellNumber);
							cell.setCellStyle(style);
							cell.setCellValue(dataHeading[cellNumber]);
							sheet.autoSizeColumn(cellNumber);
							cellNumber++;
						}
						for(ChargingDetails chargingDetails : chargingDetailsMapData.getValue()) {
							
							row = sheet.createRow(rowNumber++);
							while (true) {
								cellNumber = 0;
								cell = (SXSSFCell) row.createCell(cellNumber++);
								cell.setCellStyle(style);
								cell.setCellValue(chargingDetails.getBusinessName());

								cell = (SXSSFCell) row.createCell(cellNumber++);
								cell.setCellStyle(style);
								cell.setCellValue(chargingDetails.getCurrency());

								cell = (SXSSFCell) row.createCell(cellNumber++);
								cell.setCellStyle(style);
								cell.setCellValue(chargingDetails.getMopType().getName());

								cell = (SXSSFCell) row.createCell(cellNumber++);
								cell.setCellStyle(style);
								cell.setCellValue(chargingDetails.getTransactionType().getName());

								/*cell = (SXSSFCell) row.createCell(cellNumber++);
								cell.setCellStyle(style);
								cell.setCellValue(chargingDetails.getSlabId());*/

								cell = (SXSSFCell) row.createCell(cellNumber++);
								cell.setCellStyle(style);
								cell.setCellValue(chargingDetails.getPgTDR());

								cell = (SXSSFCell) row.createCell(cellNumber++);
								cell.setCellStyle(style);
								cell.setCellValue(chargingDetails.getPgFixCharge());
								
								/*cell = (SXSSFCell) row.createCell(cellNumber++);
								cell.setCellStyle(style);
								cell.setCellValue(chargingDetails.getPgTDRAFC());*/

								cell = (SXSSFCell) row.createCell(cellNumber++);
								cell.setCellStyle(style);
								cell.setCellValue(chargingDetails.getBankTDR());
								
								cell = (SXSSFCell) row.createCell(cellNumber++);
								cell.setCellStyle(style);
								cell.setCellValue(chargingDetails.getBankFixCharge());
								
								
								cell = (SXSSFCell) row.createCell(cellNumber++);
								cell.setCellStyle(style);
								cell.setCellValue(chargingDetails.getResellerTDR());
								
								cell = (SXSSFCell) row.createCell(cellNumber++);
								cell.setCellStyle(style);
								cell.setCellValue(chargingDetails.getResellerFixCharge());
								

								cell = (SXSSFCell) row.createCell(cellNumber++);
								cell.setCellStyle(style);
								cell.setCellValue(chargingDetails.getMerchantTDR());

								cell = (SXSSFCell) row.createCell(cellNumber++);
								cell.setCellStyle(style);
								cell.setCellValue(chargingDetails.getMerchantFixCharge());

								cell = (SXSSFCell) row.createCell(cellNumber++);
								cell.setCellStyle(style);
								cell.setCellValue(chargingDetails.getMerchantServiceTax());

								cell = (SXSSFCell) row.createCell(cellNumber++);
								cell.setCellStyle(style);
								cell.setCellValue(chargingDetails.getMinTxnAmount());

								cell = (SXSSFCell) row.createCell(cellNumber++);
								cell.setCellStyle(style);
								cell.setCellValue(chargingDetails.getMaxTxnAmount());

								cell = (SXSSFCell) row.createCell(cellNumber++);
								cell.setCellStyle(style);
								cell.setCellValue(chargingDetails.getMaxChargeMerchant());
								
								cell = (SXSSFCell) row.createCell(cellNumber++);
								cell.setCellStyle(style);
								cell.setCellValue(chargingDetails.getMaxChargeAcquirer());
								
								/*cell = (SXSSFCell) row.createCell(cellNumber++);
								cell.setCellStyle(style);
								cell.setCellValue(chargingDetails.getAcquiringMode().name());*/
								break;
							}
						}
						
					}
				}
				
			}
			String FILE_EXTENSION = ".xlsx";
			DateFormat df = new SimpleDateFormat("yyyyMMddhhmmss");
			fileName = "Charging_Details" + df.format(new Date())+FILE_EXTENSION;
			File file = new File(fileName);

			// this Writes the workbook
			FileOutputStream out = new FileOutputStream(file);
			workbook.write(out);
			out.flush();
			out.close();
			workbook.dispose();
			fileInputStream = new FileInputStream(file);
			addActionMessage(fileName + " has written successfully.");
			
		} catch (Exception ex) {
			logger.info("Exception caught while download Charging Details excel file " + ex);
			return ERROR;
		}
		logger.info("Successfully download Charging Details excel file ");
		return SUCCESS;
	}

	public String pdfDownloader() {
		
		try {
			String merchantName = userDao.getBusinessNameByPayId(payId);
			Map<String, List<Map<String, List<ChargingDetails>>>> acquirerTypeDataMap = getChargingData(payId,merchantName);
			
			String currentDate = DateCreater.defaultFromDate();
			fileName = "Charging_Details"+currentDate+".pdf";
			File file = new File(fileName);
			fileInputStream = pdfCreator.creatChargingDetailsPdf(acquirerTypeDataMap, file, merchantName);
		}catch (Exception ex) {
			logger.info("Exception caught " + ex);
			return ERROR;
		}
		return SUCCESS;
	}
	
	public Map<String, List<Map<String, List<ChargingDetails>>>> getChargingData(String payId,String businessName) {
		Map<String, List<Map<String, List<ChargingDetails>>>> acquirerTypeDataMap = new HashMap<String, List<Map<String, List<ChargingDetails>>>>();
		try {
			if (!StringUtils.isEmpty(payId)) {

				List<ChargingDetails> chargingDetailsList = chargingDetailsDao.getMerchantActiveChargingDetails(payId);
				Set<String> acquirerNameSet = new HashSet<String>();
				Map<String, Set<String>> acquirerTypeKeyMap = new HashMap<String, Set<String>>();

				for (ChargingDetails chargingDetails : chargingDetailsList) {

					acquirerNameSet.add(chargingDetails.getAcquirerName());
				}

				for (String aquirerName : acquirerNameSet) {
					Set<String> uniqueKeySet = new HashSet<String>();

					for (ChargingDetails chargingDetails : chargingDetailsList) {
						String paymentType = chargingDetails.getPaymentType().getName();
						if (aquirerName.equalsIgnoreCase(chargingDetails.getAcquirerName())) {

							if (paymentType.equalsIgnoreCase("Net Banking") || paymentType.equalsIgnoreCase("COD")
									|| paymentType.equalsIgnoreCase("UPI") || paymentType.equalsIgnoreCase("EMI")
									|| paymentType.equalsIgnoreCase("Wallet")) {

								String uniqueKey = chargingDetails.getPaymentType().getName();
								uniqueKeySet.add(uniqueKey);
								acquirerTypeKeyMap.put(chargingDetails.getAcquirerName(), uniqueKeySet);

							} else {

								String uniqueKey = chargingDetails.getPaymentType().getName() + "-"
										+ chargingDetails.getPaymentsRegion().name() + "-"
										+ chargingDetails.getAcquiringMode().name() + "-"
										+ chargingDetails.getCardHolderType().name();
								uniqueKeySet.add(uniqueKey);
								acquirerTypeKeyMap.put(chargingDetails.getAcquirerName(), uniqueKeySet);
							}
						}
					}
				}
				for (String acquirerType : acquirerTypeKeyMap.keySet()) {
					Set<String> uniqueKeySet = acquirerTypeKeyMap.get(acquirerType);
					Map<String, List<ChargingDetails>> chargingDetailsDataMap = new HashMap<String, List<ChargingDetails>>();
					List<Map<String, List<ChargingDetails>>> chargingDetailsDataMapList = new ArrayList<Map<String, List<ChargingDetails>>>();
					for (String uniqueKey : uniqueKeySet) {
						boolean flag=false;
						List<ChargingDetails> chargingObjectList = new ArrayList<ChargingDetails>();

						for (ChargingDetails chargingDetails : chargingDetailsList) {
							if (!acquirerType.equalsIgnoreCase(chargingDetails.getAcquirerName())) {
								continue;
							}
							String key = "";
							String paymentType = chargingDetails.getPaymentType().getName();
							if (paymentType.equalsIgnoreCase("Net Banking") || paymentType.equalsIgnoreCase("COD")
									|| paymentType.equalsIgnoreCase("UPI") || paymentType.equalsIgnoreCase("EMI")
									|| paymentType.equalsIgnoreCase("Wallet")) {
								key = chargingDetails.getPaymentType().getName();
								//flag=true;
							} else {
								key = chargingDetails.getPaymentType().getName() + "-"
										+ chargingDetails.getPaymentsRegion().name() + "-"
										+ chargingDetails.getAcquiringMode().name() + "-"
										+ chargingDetails.getCardHolderType().name();
							}
							if (key != "" && uniqueKey.equalsIgnoreCase(key)) {
								chargingDetails.setBusinessName(businessName);
								chargingObjectList.add(chargingDetails);
							}
						}
						if(flag)
							chargingDetailsDataMap.put("", chargingObjectList);
						else
							chargingDetailsDataMap.put(uniqueKey, chargingObjectList);
					}
					chargingDetailsDataMapList.add(chargingDetailsDataMap);
					acquirerTypeDataMap.put(acquirerType, chargingDetailsDataMapList);
				}
			}
		}catch (Exception ex) {
			logger.info("Exception caught " + ex);
		}
		return acquirerTypeDataMap;
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

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	
	
}
