package com.paymentgateway.crm.action;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.paymentgateway.commons.dao.DispatchSlipDao;
import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.mongo.MongoInstance;
import com.paymentgateway.commons.user.DispatchSlipDetails;
import com.paymentgateway.commons.user.Merchants;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.user.UserType;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.CrmFieldType;
import com.paymentgateway.commons.util.CrmValidator;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.StatusType;
import com.paymentgateway.commons.util.TDRStatus;
import com.paymentgateway.commons.util.TransactionType;

/**
 * @author Rahul
 *
 */
public class DispatchSlipAction extends AbstractSecureAction {

	private static final long serialVersionUID = 2662926474141829858L;
	private static Logger logger = LoggerFactory.getLogger(DispatchSlipAction.class.getName());

	@Autowired
	CrmValidator validator;

	@Autowired
	private PropertiesManager propertiesManager;

	private static final String prefix = "MONGO_DB_";
	
	@Autowired
	MongoInstance mongoInstance;

	@Autowired
	DispatchSlipDao dispatchSlipDao;

	@Autowired
	private UserDao userDao;

	// private String merchantId;
	private File csvfile;
	private String payId;
	private String merchantName;
	private User sessionUser = new User();
	private List<String> fileData;
	private String fileName;
	private String orderId;
	private String invoiceNo;
	private String courierServiceProvider;
	private String dispatchSlipNo;
	private int invalid;
	private List<Merchants> merchantList = new ArrayList<Merchants>();
	private int count;
	private int incorrectOrderId;
	private long rowCount;
	private long blankLine;
	private boolean validHeader = false;

	@SuppressWarnings("unchecked")
	public String execute() {
		
		sessionUser = (User) sessionMap.get(Constants.USER.getValue());
		if ((sessionUser.getUserType().equals(UserType.ADMIN)) || (sessionUser.getUserType().equals(UserType.SUBADMIN))
				|| (sessionUser.getUserType().equals(UserType.SUPERADMIN))) {
			setMerchantList(userDao.getMerchantList());
		} else if ((sessionUser.getUserType().equals(UserType.MERCHANT))) {
			setMerchantList(userDao.getMerchantActive(sessionUser.getEmailId()));
		} else if(sessionUser.getUserType().equals(UserType.SUBUSER)) {
			setMerchantList(userDao.getSubUserByParentPayId(sessionUser.getParentPayId()));
		}
	
		List<DispatchSlipDetails> dispatchList = new ArrayList<DispatchSlipDetails>();
		String line = "";
		rowCount = 0;
		count = 0;
		incorrectOrderId = 0;
		try {
			String fileExtension = FilenameUtils.getExtension(fileName);

			if (fileExtension.equals("xlsx") || fileExtension.equals("xls")) {
				fileData = filterExcelFile(csvfile);
			} else if (fileExtension.equals("csv")) {
				fileData = filterCsvFile(csvfile);
			}

			if (fileData.size() != 0) {
				for (int i = 0; i < fileData.size(); i++) {
					line = fileData.get(i);
					rowCount++;					
					String data[] = line.split(",");
					if (rowCount == 1) {
						continue;
					}
					
					if(data.length == 0) {
						blankLine++;
						continue;
					}
					setOrderId(data[0].trim());
					if(!checkValidOrderId(data[0].trim())) {
						incorrectOrderId++;
						//count++;
					} else {
					setInvoiceNo(data[1]);
					setCourierServiceProvider(data[2]);
					setDispatchSlipNo(data[3]);
					if (validateFileFields()) {
						Date date = new Date();
						DispatchSlipDetails dispatchSlipDetails = new DispatchSlipDetails();
						dispatchSlipDetails.setCourierServiceProvider(data[2]);
						dispatchSlipDetails.setCreatedDate(date);
						dispatchSlipDetails.setDispatchSlipNo(data[3]);
						dispatchSlipDetails.setInvoiceId(data[1]);
						dispatchSlipDetails.setOrderId(data[0].trim());
						dispatchSlipDetails.setPayId(payId);
						dispatchSlipDetails.setProcessedBy(sessionUser.getEmailId());
						dispatchList.add(dispatchSlipDetails);
						count++;
					}
				  }
				}
				rowCount--;
			}
			logger.info("total number of rows "+rowCount);
			logger.info("total BLANK lines "+blankLine);
			//rowCount--;
			
			dispatchSlipDao.insert(dispatchList);
			csvfile.delete();
		} catch (Exception exception) {
			logger.error("Exception while uploding dispatch slip file: ", exception);
		}
		csvfile = null;
		logger.info("Total Duplicate entries found in file is " + count);
		return SUCCESS;
	}

	private boolean checkValidOrderId(String orderId) {
		
		MongoDatabase dbIns = mongoInstance.getDB();
		MongoCollection<Document> coll = dbIns.getCollection(
				PropertiesManager.propertiesMap.get(prefix + Constants.COLLECTION_NAME.getValue()));

		List<BasicDBObject> saleList = new ArrayList<BasicDBObject>();
		saleList.add(new BasicDBObject(FieldType.ORDER_ID.getName(), orderId));
		saleList.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.SALE.getName()));
		saleList.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.CAPTURED.getName()));
		BasicDBObject saleQuery = new BasicDBObject("$and", saleList);
		
		try {
			MongoCursor<Document> cursor = coll.find(saleQuery).iterator();

			if(cursor.hasNext()) {
				return true;
			}
		} catch (Exception e) {
			logger.error("Exception in checkValidOrderId in dispatch slip action", e);
		}
		return false;
	}

	@SuppressWarnings("resource")
	private List<String> filterCsvFile(File file) {
		List<String> csvData = new ArrayList<>();
		String headers = "Order ID,Invoice No. (Provide by Merchant),Courier Service Provider,Dispatch Slip No.";
		try {
			String line = "";
			BufferedReader br = new BufferedReader(new FileReader(file));
			while ((line = br.readLine()) != null) {
				if(validHeader == false && !headers.equalsIgnoreCase(line.replaceAll("\"", ""))) {
					return csvData;
				}
				csvData.add(line);
				validHeader = true;
			}
			return csvData;
		} catch (Exception e) {
			logger.error("exception " , e);
			return csvData;
		}

	}

	private List<String> filterExcelFile(File file) {
		List<String> data = new ArrayList<String>();
		int totalColCount = 0;
		try {

			FileInputStream fis = new FileInputStream(file);
			XSSFWorkbook myWorkBook = new XSSFWorkbook(fis);
			XSSFSheet mySheet = myWorkBook.getSheetAt(0);

			Iterator<Row> rowIterator = mySheet.iterator();
			while (rowIterator.hasNext()) {

				StringBuilder rowString = new StringBuilder();
				Row row = rowIterator.next();
				int j = 0;

				if (row.getRowNum() == 0) {
					totalColCount = row.getLastCellNum();
				}

				/* Iterator<Cell> cellIterator = row.iterator(); */
				while (j < totalColCount) {
					/* while (cellIterator.hasNext()) { */

					rowString.append(",");

					Cell cell = row.getCell(j);
					// Cell cell = cellIterator.next();
					if (cell == null) {
						rowString.append("");
						j++;
						continue;
					}
					switch (cell.getCellType()) {
					case Cell.CELL_TYPE_STRING:
						rowString.append(cell.getStringCellValue());
						break;
					case Cell.CELL_TYPE_NUMERIC:
						if (DateUtil.isCellDateFormatted(cell)) {
							if (cell.getColumnIndex() == 8) {
								SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");
								rowString.append(dateFormat.format(cell.getDateCellValue()));
							} else {
								SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
								System.out.print(dateFormat.format(cell.getDateCellValue()));
								rowString.append(dateFormat.format(cell.getDateCellValue()));
							}
						} else {
							Double doubleValue = cell.getNumericCellValue();
							BigDecimal bd = new BigDecimal(doubleValue.toString());
							long lonVal = bd.longValue();
							String phoneNumber = Long.toString(lonVal).trim();
							rowString.append(phoneNumber);
						}
						break;
					case Cell.CELL_TYPE_BOOLEAN:
						rowString.append(String.valueOf(cell.getBooleanCellValue()));
						break;
					case Cell.CELL_TYPE_BLANK:
						rowString.append("");
						break;
					default:
						rowString.append(",");
						break;
					}

					j++;
				}
				rowString.deleteCharAt(0);
				data.add(rowString.toString());

			}
		} catch (Exception e) {
			logger.error("exception " , e);
			return data;
		}
		return data;

	}

	public Boolean validateFileFields() {

		if (validator.validateBlankField(getOrderId())) {
		} else if (!validator.validateField(CrmFieldType.ORDER_ID, getInvoiceNo())) {
			addFieldError(CrmFieldType.ORDER_ID.getName(), ErrorType.INVALID_FIELD.getResponseMessage());
			invalid++;
			return false;
		}

		if (validator.validateBlankField(getInvoiceNo())) {
		} else if (!validator.validateField(CrmFieldType.INVOICE_NUMBER, getInvoiceNo())) {
			addFieldError(CrmFieldType.INVOICE_NUMBER.getName(), ErrorType.INVALID_FIELD.getResponseMessage());
			invalid++;
			return false;
		}
		if (validator.validateBlankField(getCourierServiceProvider())) {
		} else if (!validator.validateField(CrmFieldType.COURIER_SERVICE_PROVIDER_NO, getCourierServiceProvider())) {
			addFieldError(CrmFieldType.COURIER_SERVICE_PROVIDER_NO.getName(),
					ErrorType.INVALID_FIELD.getResponseMessage());
			invalid++;
			return false;
		}
		if (validator.validateBlankField(getDispatchSlipNo())) {
		} else if (!validator.validateField(CrmFieldType.DISPATCH_SLIP_NO, getDispatchSlipNo())) {
			addFieldError(CrmFieldType.DISPATCH_SLIP_NO.getName(), ErrorType.INVALID_FIELD.getResponseMessage());
			invalid++;
			return false;
		}
		return true;
	}

	public File getCsvfile() {
		return csvfile;
	}

	public void setCsvfile(File csvfile) {
		this.csvfile = csvfile;
	}

	public String getPayId() {
		return payId;
	}

	public void setPayId(String payId) {
		this.payId = payId;
	}

	public String getMerchantName() {
		return merchantName;
	}

	public void setMerchantName(String merchantName) {
		this.merchantName = merchantName;
	}

	public User getSessionUser() {
		return sessionUser;
	}

	public void setSessionUser(User sessionUser) {
		this.sessionUser = sessionUser;
	}

	public List<String> getFileData() {
		return fileData;
	}

	public void setFileData(List<String> fileData) {
		this.fileData = fileData;
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

	public String getInvoiceNo() {
		return invoiceNo;
	}

	public void setInvoiceNo(String invoiceNo) {
		this.invoiceNo = invoiceNo;
	}

	public String getCourierServiceProvider() {
		return courierServiceProvider;
	}

	public void setCourierServiceProvider(String courierServiceProvider) {
		this.courierServiceProvider = courierServiceProvider;
	}

	public String getDispatchSlipNo() {
		return dispatchSlipNo;
	}

	public void setDispatchSlipNo(String dispatchSlipNo) {
		this.dispatchSlipNo = dispatchSlipNo;
	}

	public int getInvalid() {
		return invalid;
	}

	public void setInvalid(int invalid) {
		this.invalid = invalid;
	}

	public List<Merchants> getMerchantList() {
		return merchantList;
	}

	public void setMerchantList(List<Merchants> merchantList) {
		this.merchantList = merchantList;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}
	
	public int getIncorrectOrderId() {
		return incorrectOrderId;
	}

	public void setIncorrectOrderId(int incorrectOrderId) {
		this.incorrectOrderId = incorrectOrderId;
	}
	public long getRowCount() {
		return rowCount;
	}

	public void setRowCount(long rowCount) {
		this.rowCount = rowCount;
	}
	public long getBlankLine() {
		return blankLine;
	}

	public void setBlankLine(long blankLine) {
		this.blankLine = blankLine;
	}
	public boolean isValidHeader() {
		return validHeader;
	}

	public void setValidHeader(boolean validHeader) {
		this.validHeader = validHeader;
	}
}
