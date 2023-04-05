package com.paymentgateway.crm.action;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.owasp.validator.css.CssValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.paymentgateway.commons.api.SmsControllerServiceProvider;
import com.paymentgateway.commons.user.Invoice;
import com.paymentgateway.commons.user.InvoiceTransactionDao;
import com.paymentgateway.commons.user.Merchants;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.user.UserType;
import com.paymentgateway.commons.util.AWSSESEmailService;
import com.paymentgateway.commons.util.Base64EncodeDecode;
import com.paymentgateway.commons.util.BitlyUrlShortener;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.CrmEmailer;
import com.paymentgateway.commons.util.CrmFieldConstants;
import com.paymentgateway.commons.util.CrmFieldType;
import com.paymentgateway.commons.util.CrmValidator;
import com.paymentgateway.commons.util.Currency;
import com.paymentgateway.commons.util.PromotionalPaymentType;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.QRCodeCreator;
import com.paymentgateway.commons.util.StatusType;
import com.paymentgateway.commons.util.TransactionManager;

/**
 * @author Shiva
 *
 */
public class BulkInvoiceAction extends AbstractSecureAction {

	@Autowired
	private UserDao userDao;

	@Autowired
	private CrmValidator validator;

	@Autowired
	private QRCodeCreator qRCodeCreator;

	@Autowired
	private Base64EncodeDecode base64EncodeDecode;

	@Autowired
	private SmsControllerServiceProvider smsControllerServiceProvider;

	@Autowired
	private CrmEmailer crmEmailer;

	@Autowired
	private InvoiceTransactionDao invoiceTransactionDao;

	@Autowired
	private PropertiesManager propertiesManager;
	
	@Autowired
	BitlyUrlShortener bitlyUrlShortener;
	
	@Autowired
	AWSSESEmailService awsSESEmailService;

	private static final long serialVersionUID = 7111224242758053602L;
	private static Logger logger = LoggerFactory.getLogger(BulkInvoiceAction.class.getName());

	private File csvFile;
	private String merchantPayId;
	private String subMerchantId;
	private String subUserId;
	private String url;
	private String businessName;
	private String subMerchantBusinessName;
	private Map<String, String> currencyMap;
	private List<Merchants> merchantList;
	private List<Merchants> subMerchantList;
	private List<Invoice> invoiceList;
	private Map<Long, Invoice> failedList;
	private Map<Long, String> failedListShow;
	private List<String> fileData;
	private String fileName;
	private boolean isSuperMerchant = false;
	

	int wrongCsv = 0;
	long rowCount = -1;
	int fileIsEmpty= 0;
	long storedRow;
	
	/*
	 * private List<String> invoiceNumber; private List<String> allInvoiceNoFromDB;
	 */
	
	public String execute() {
		
		currencyMap = new HashMap<String, String>();
		merchantList = new ArrayList<Merchants>();
		invoiceList = new ArrayList<Invoice>();
		failedList = new HashMap<Long, Invoice>();
		failedListShow = new HashMap<Long, String>();
		
		/*
		 * invoiceNumber=new ArrayList<String>(); allInvoiceNoFromDB=new
		 * ArrayList<String>();
		 */
		String line = "";
		BigDecimal LocaltotalAmount;
		
		try {
			//geting filename with extension
			String fileExtension=FilenameUtils.getExtension(fileName);
		
			if(fileExtension.equals("xlsx"))
			{
				fileData=filterExcelFile(csvFile);
			}
			else if(fileExtension.equals("csv"))
			{
				fileData=filterCsvFile(csvFile);
			}else{
				setWrongCsv(1);
				return SUCCESS;
			}
			
		
			if (fileName.equals(null)) {
				fileName.replaceAll(" ", "_");
			}

			//Setting FileName for storing in DB Format(fileName_DateTime)
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
			Date fileDate = new Date();
			String newFileName = fileName.concat("_").concat(dateFormat.format(fileDate));
			
			
			//Checking Merchant using Session or Input
			User user = (User) sessionMap.get(Constants.USER);
			if (user.getUserType().equals(UserType.ADMIN) || user.getUserType().equals(UserType.SUBADMIN)
					|| user.getUserType().equals(UserType.RESELLER) || user.getUserType().equals(UserType.SUPERADMIN)) {
				if(StringUtils.isNotBlank(subMerchantId)){
					businessName = userDao.getBusinessNameByPayId(merchantPayId);
					subMerchantBusinessName=userDao.getBusinessNameByPayId(subMerchantId);
				}else{
					businessName = userDao.getBusinessNameByPayId(merchantPayId);
				}
				
				currencyMap = Currency.getSupportedCurreny(userDao.findPayId(merchantPayId));
				setCurrencyMap(currencyMap);
				merchantList = userDao.getMerchantActiveList();
			} else if (user.getUserType().equals(UserType.SUBUSER)) {
	
				subUserId=user.getPayId();
				String parentPayId = user.getParentPayId();
				User parentUser = userDao.findPayId(parentPayId);
				if(!parentUser.isSuperMerchant() && StringUtils.isNotBlank(parentUser.getSuperMerchantId())) {
					
					User superMerchant = userDao.findPayId(parentUser.getSuperMerchantId());
					merchantPayId=superMerchant.getPayId();
					businessName = superMerchant.getBusinessName();
					subMerchantBusinessName=parentUser.getBusinessName();
					subMerchantId=parentUser.getPayId();
					currencyMap = Currency.getSupportedCurreny(superMerchant);
				} else if (parentUser.getUserType().equals(UserType.MERCHANT) && parentUser.isSuperMerchant()) {
					currencyMap = Currency.getSupportedCurreny(parentUser);
					merchantPayId=user.getParentPayId();
					currencyMap = Currency.getSupportedCurreny(parentUser);
					businessName = parentUser.getBusinessName();
					setSuperMerchant(true);
					setSubMerchantList(userDao.getSubMerchantListBySuperPayId(parentUser.getPayId()));
				} else{
					currencyMap = Currency.getSupportedCurreny(parentUser);
					merchantPayId=user.getParentPayId();
					currencyMap = Currency.getSupportedCurreny(parentUser);
					businessName = parentUser.getBusinessName();
				
				}
				
				
				merchantList = userDao.getMerchantActive(parentUser.getEmailId());
				
				setCurrencyMap(currencyMap);
			} else if (user.getUserType().equals(UserType.MERCHANT)) {
				merchantList = userDao.getMerchantActive(user.getEmailId());
				
				
				if (!user.isSuperMerchant() && StringUtils.isNotBlank(user.getSuperMerchantId())) {
					
					User superMerchant = userDao.findPayId(user.getSuperMerchantId());
					merchantPayId=user.getSuperMerchantId();
					subMerchantId=user.getPayId();
					businessName = superMerchant.getBusinessName();
					subMerchantBusinessName=user.getBusinessName();
					currencyMap = Currency.getSupportedCurreny(superMerchant);
				}else{
					subMerchantList=userDao.getSubMerchantListBySuperPayId(user.getPayId());
					businessName = user.getBusinessName();
					merchantPayId=user.getPayId();
					currencyMap = Currency.getSupportedCurreny(user);
				}
				
				setCurrencyMap(currencyMap);
			}
			
			
			/*
			 * allInvoiceNoFromDB=invoiceTransactionDao.findAllInvoiceNoByPayID(
			 * merchantPayId);
			 */
			
			//Checking Data and Storing in DB
			if(fileData.size()!=0){
				if(fileData.size()==1){
					setFileIsEmpty(1);
					return SUCCESS;
				}
			for(int i=0;i<fileData.size();i++) {
				line=fileData.get(i);
				Invoice invoice = new Invoice();
				invoice.setFileName(newFileName);
				rowCount++;
				String data[] = line.split(",", -1);
				if (rowCount == 0) {
					if (/*
						 * data[0].equalsIgnoreCase("Invoice no") &&
						 */ data[0].equalsIgnoreCase("Name") && data[1].equalsIgnoreCase("Phone")
							&& data[2].equalsIgnoreCase("Email") && data[3].equalsIgnoreCase("Product Name")
							&& data[4].equalsIgnoreCase("Product Description")
							&& data[5].equalsIgnoreCase("Duration From (DD-MM-YYYY)")
							&& data[6].equalsIgnoreCase("Duration To (DD-MM-YYYY)")
							&& data[7].equalsIgnoreCase("Expiry Date(DD-MM-YYYY)")
							&& data[8].equalsIgnoreCase("Expiry Time(HH:MM)")
							&& data[9].equalsIgnoreCase("Currency Type") && data[10].equalsIgnoreCase("Quantity")
							&& data[11].equalsIgnoreCase("Amount") && data[12].equalsIgnoreCase("Service")
							&& data[13].equalsIgnoreCase("Address") && data[14].equalsIgnoreCase("Country")
							&& data[15].equalsIgnoreCase("State") && data[16].equalsIgnoreCase("City")
							&& data[17].equalsIgnoreCase("PIN") && data[18].equalsIgnoreCase("UDF11")
							&& data[19].equalsIgnoreCase("UDF12") && data[20].equalsIgnoreCase("UDF13")
							&& data[21].equalsIgnoreCase("UDF14") && data[22].equalsIgnoreCase("UDF15")
							&& data[23].equalsIgnoreCase("UDF16") && data[24].equalsIgnoreCase("UDF17")
							&& data[25].equalsIgnoreCase("UDF18")) {
						setWrongCsv(0);
						continue;
					} else {
						setWrongCsv(1);
						break;
					}
				}
				
				//Checking total column is 18 
				if (data.length > 26 || data.length < 26) {
					invoice.setEmail(data[2]);
					failedList.put(rowCount, invoice);
					failedListShow.put(rowCount, "Please Check the Columns");
					continue;
				}
				
				// Checking mandatory Field is not Empty
				if (StringUtils.isBlank(data[0])) {
					invoice.setEmail(data[2]);
					failedList.put(rowCount, invoice);
					failedListShow.put(rowCount, "Please check the Name is Empty");
					continue;
				}
				if (StringUtils.isBlank(data[1]) && StringUtils.isBlank(data[2])) {
					invoice.setName(data[0]);
					failedList.put(rowCount, invoice);
					failedListShow.put(rowCount, "Phone no. & Email Id is Empty");
					continue;
				}
				/*
				 * if(StringUtils.isBlank(data[2])){ invoice.setEmail(data[3]);
				 * failedList.put(rowCount, invoice); failedListShow.put(rowCount,
				 * "Please check the Phone is Empty"); continue; }
				 * if(StringUtils.isBlank(data[3])){ invoice.setInvoiceId(data[0]);
				 * failedList.put(rowCount, invoice); failedListShow.put(rowCount,
				 * "Please check the Email is Empty"); continue; }
				 */
				if (StringUtils.isBlank(data[3])) {
					invoice.setEmail(data[2]);
					failedList.put(rowCount, invoice);
					failedListShow.put(rowCount, "Please check the Product Name is Empty");
					continue;
				}
				if (StringUtils.isBlank(data[7])) {
					invoice.setEmail(data[2]);
					failedList.put(rowCount, invoice);
					failedListShow.put(rowCount, "Please check the Expiry Date is Empty");
					continue;
				}
				if (StringUtils.isBlank(data[8])) {
					invoice.setEmail(data[2]);
					failedList.put(rowCount, invoice);
					failedListShow.put(rowCount, "Please check the Expiry Time is Empty");
					continue;
				}
				if (StringUtils.isBlank(data[9])) {
					invoice.setEmail(data[2]);
					failedList.put(rowCount, invoice);
					failedListShow.put(rowCount, "Please check the Currency is Empty");
					continue;
				}
				if (StringUtils.isBlank(data[10])) {
					invoice.setEmail(data[2]);
					failedList.put(rowCount, invoice);
					failedListShow.put(rowCount, "Please check the Quantity is Empty");
					continue;
				}
				if (StringUtils.isBlank(data[11])) {
					invoice.setEmail(data[2]);
					failedList.put(rowCount, invoice);
					failedListShow.put(rowCount, "Please check the Amount is Empty");
					continue;
				}
				
				//Setting Data in Invoice
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				Date date = new Date();
				invoice.setBusinessName(businessName);
				if(StringUtils.isNotBlank(subMerchantBusinessName)){
					invoice.setSubMerchantbusinessName(subMerchantBusinessName);
				}
				invoice.setInvoiceType(PromotionalPaymentType.INVOICE_PAYMENT.getName());
				invoice.setInvoiceId(TransactionManager.getNewTransactionId());
				invoice.setCreateDate(sdf.format(date));
				invoice.setPayId(getMerchantPayId());
				invoice.setSubMerchantId(getSubMerchantId());
				invoice.setSubUserId(getSubUserId());
				/* invoice.setInvoiceNo(data[0]); */
				invoice.setName(data[0]);
				if (StringUtils.isNotEmpty(data[1])) {
					if(data[1].equalsIgnoreCase("0")){
						invoice.setPhone("");
					}else{
						
						invoice.setPhone(data[1]);
					}
				}
				if (StringUtils.isNotEmpty(data[2])) {
					invoice.setEmail(data[2]);
				}
				invoice.setProductName(data[3]);
				if (StringUtils.isNotEmpty(data[4])) {
					invoice.setProductDesc(data[4]);
				}
				if (StringUtils.isNotEmpty(data[5])) {
					invoice.setDurationFrom(changeFormat(data[5]));
				}
				if (StringUtils.isNotEmpty(data[6])) {
					invoice.setDurationTo(changeFormat(data[6]));
				}
				invoice.setExpiresDay(changeFormat(data[7]).concat(" ").concat(data[8]));
				invoice.setSaltKey(invoice.getInvoiceId());
				invoice.setQuantity(data[10]);
				if (StringUtils.isNotEmpty(data[13])) {
					invoice.setAddress(data[13]);
				}

				if (StringUtils.isNotEmpty(data[14])) {
					invoice.setCountry(data[14]);
				}
				if (StringUtils.isNotEmpty(data[15])) {
					invoice.setState(data[15]);
				}
				if (StringUtils.isNotEmpty(data[16])) {
					invoice.setCity(data[16]);
				}
				if (StringUtils.isNotEmpty(data[17])) {
					invoice.setZip(data[17]);
				}
				
				invoice.setStatus(StatusType.PENDING.getName());
				invoice.setAmount(data[11]);
				if (StringUtils.isNotEmpty(data[12])) {
					invoice.setServiceCharge(data[12]);
				} else {
					invoice.setServiceCharge("0.00");
				}
				double q = Double.parseDouble(invoice.getQuantity());
				BigDecimal amount = new BigDecimal(invoice.getAmount());
				BigDecimal service = new BigDecimal(invoice.getServiceCharge());
				LocaltotalAmount = ((amount.multiply(new BigDecimal(invoice.getQuantity()))
						.setScale(2, BigDecimal.ROUND_UP).add(service.setScale(2, BigDecimal.ROUND_UP))));
				String AmountInString = String.valueOf(LocaltotalAmount.setScale(2, BigDecimal.ROUND_UP));
				invoice.setTotalAmount(AmountInString);
				invoice.setReturnUrl("");
				if (invoice.getReturnUrl().isEmpty()) {
					invoice.setReturnUrl(propertiesManager.propertiesMap.get(CrmFieldConstants.INVOICE_RETURN_URL.getValue()));
				}
				
				url = propertiesManager.propertiesMap.get(CrmFieldConstants.INVOICE_URL.getValue())+ invoice.getInvoiceId();
				invoice.setLongUrl(url);
				invoice.setShortUrl(bitlyUrlShortener.createShortUrlUsingBitly(url));
				
				if(StringUtils.isNotBlank(data[18]))
					invoice.setUDF11(data[18]);
				if(StringUtils.isNotBlank(data[19]))
					invoice.setUDF12(data[19]);
				if(StringUtils.isNotBlank(data[20]))
					invoice.setUDF13(data[20]);
				if(StringUtils.isNotBlank(data[21]))
					invoice.setUDF14(data[21]);
				if(StringUtils.isNotBlank(data[22]))
					invoice.setUDF15(data[22]);
				if(StringUtils.isNotBlank(data[23]))
					invoice.setUDF16(data[23]);
				if(StringUtils.isNotBlank(data[24]))
					invoice.setUDF17(data[24]);
				if(StringUtils.isNotBlank(data[25]))
					invoice.setUDF18(data[25]);
				
				if (StringUtils.isBlank(data[9])) {
					failedList.put(rowCount, invoice);
					failedListShow.put(rowCount, "Currency");
					continue;
				} else {
					if (currencyMap.containsKey(Currency.getNumericCode(data[9].toUpperCase()))) {
						invoice.setCurrencyCode(Currency.getNumericCode(data[9].toUpperCase()));
					} else {
						failedList.put(rowCount, invoice);
						failedListShow.put(rowCount, "Wrong Currency Type");
						continue;
					}
				}

				//Genrating Qr code for Invoice id.
				BufferedImage image = qRCodeCreator.generateQRCode(invoice);
				File file = new File(invoice.getInvoiceId()+".png");
				ImageIO.write(image, "png", file);
				invoice.setQr(base64EncodeDecode.base64Encoder(file));
				file.delete();
				/*
				 * //Checking Duplicate Invoice No.
				 * if(allInvoiceNoFromDB.contains(invoice.getInvoiceNo()) ||
				 * invoiceNumber.contains(invoice.getInvoiceNo())) { failedList.put(rowCount,
				 * invoice); failedListShow.put(rowCount, "Duplicate Invoice No. Found");
				 * continue; } invoiceNumber.add(invoice.getInvoiceNo());
				 */

				// Validation
				/*
				 * if (!(validator.validateField(CrmFieldType.INVOICE_NUMBER,
				 * invoice.getInvoiceNo()))) { failedList.put(rowCount, invoice);
				 * failedListShow.put(rowCount, "Invoice No."); continue; }
				 */
				
				//Checking Validation
				if (!(validator.validateField(CrmFieldType.FIRSTNAME, invoice.getName()))) {
					failedList.put(rowCount, invoice);
					failedListShow.put(rowCount, "Name");
					continue;
				}
				if (StringUtils.isNotBlank(invoice.getPhone())) {
					if (!(invoice.getPhone().equalsIgnoreCase("0"))) {
						if (!(validator.validateField(CrmFieldType.INVOICE_PHONE, invoice.getPhone()))) {
							failedList.put(rowCount, invoice);
							failedListShow.put(rowCount, "Phone No.");
							continue;
						}
					}
				}
				if (StringUtils.isNotBlank(invoice.getEmail())) {
					if (!(validator.isValidEmailId(invoice.getEmail()))) {
						failedList.put(rowCount, invoice);
						failedListShow.put(rowCount, "Email");
						continue;
					}
				}
				if (!(validator.validateField(CrmFieldType.INVOICE_AMOUNT, invoice.getAmount()))) {
					failedList.put(rowCount, invoice);
					failedListShow.put(rowCount, "Amount");
					continue;
				}
				if (!(validator.validateField(CrmFieldType.INVOICE_AMOUNT, invoice.getServiceCharge()))) {
					failedList.put(rowCount, invoice);
					failedListShow.put(rowCount, "Surcharge");
					continue;
				}
				if (!(validator.validateField(CrmFieldType.PRODUCT_NAME, invoice.getProductName()))) {
					failedList.put(rowCount, invoice);
					failedListShow.put(rowCount, "Product Name");
					continue;
				}
				if (StringUtils.isNotBlank(invoice.getProductDesc())) {
					if (!(validator.validateField(CrmFieldType.INVOICE_DESCRIPTION, invoice.getProductDesc()))) {
						failedList.put(rowCount, invoice);
						failedListShow.put(rowCount, "Product Description");
						continue;
					}
				}
				if (!(validator.validateField(CrmFieldType.INVOICE_CURRENCY_CODE, invoice.getCurrencyCode()))) {
					failedList.put(rowCount, invoice);
					failedListShow.put(rowCount, "Currency");
					continue;
				}
				if (!(validator.validateField(CrmFieldType.INVOICE_EXPIRES_DAY, invoice.getExpiresDay()))) {
					failedList.put(rowCount, invoice);
					failedListShow.put(rowCount, "Expiry Date");
					continue;
				}
				if (!(validator.validateField(CrmFieldType.QUANTITY, invoice.getQuantity()))) {
					failedList.put(rowCount, invoice);
					failedListShow.put(rowCount, "Quantity");
					continue;
				}
				if (StringUtils.isNotBlank(invoice.getAddress())) {
					if (!(validator.validateField(CrmFieldType.ADDRESS, invoice.getAddress()))) {
						failedList.put(rowCount, invoice);
						failedListShow.put(rowCount, "Address");
						continue;
					}
				}
				if (StringUtils.isNotBlank(invoice.getCountry())) {
					if (!(validator.validateField(CrmFieldType.COUNTRY, invoice.getState()))) {
						failedList.put(rowCount, invoice);
						failedListShow.put(rowCount, "Country");
						continue;
					}
				}
				if (StringUtils.isNotBlank(invoice.getState())) {
					if (!(validator.validateField(CrmFieldType.STATE, invoice.getState()))) {
						failedList.put(rowCount, invoice);
						failedListShow.put(rowCount, "State");
						continue;
					}
				}
				if (StringUtils.isNotBlank(invoice.getCity())) {
					if (!(validator.validateField(CrmFieldType.CITY, invoice.getCity()))) {
						failedList.put(rowCount, invoice);
						failedListShow.put(rowCount, "City");
						continue;
					}
				}
				if (StringUtils.isNotBlank(invoice.getZip())) {
					if (!(validator.validateField(CrmFieldType.INVOICE_ZIP, invoice.getZip()))) {
						failedList.put(rowCount, invoice);
						failedListShow.put(rowCount, "Pin");
						continue;
					}
				}

				if (StringUtils.isNotBlank(invoice.getDurationFrom())) {
					if (!(validator.validateField(CrmFieldType.INVOICE_DURATION_FROM, invoice.getDurationFrom()))) {
						failedList.put(rowCount, invoice);
						failedListShow.put(rowCount, "Duration From");
						continue;
					}
				}

				if (StringUtils.isNotBlank(invoice.getDurationTo())) {
					if (!(validator.validateField(CrmFieldType.INVOICE_DURATION_TO, invoice.getDurationTo()))) {
						failedList.put(rowCount, invoice);
						failedListShow.put(rowCount, "Duration To");
						continue;
					}
				}
				
				invoiceList.add(invoice);
				}
			}else{
				setFileIsEmpty(1);
				return SUCCESS;
			}
			
			setFailedList(failedList);
			setFailedListShow(failedListShow);
			setRowCount(rowCount);
			setStoredRow(invoiceList.size());
			invoiceTransactionDao.createMany(invoiceList);
			
			logger.info("Total invoice added in Database " + invoiceList.size());
			
			
			// Thread For Sending Email & SMS for Invoice.
			Runnable r = new Runnable() {
				public synchronized void run() {
					List<Invoice> pendingInvoiceList = new ArrayList<>();
					pendingInvoiceList
							.addAll(invoiceTransactionDao.findAllInvoiceByStatusAndPayID(StatusType.PENDING.getName(), merchantPayId, subMerchantId, subUserId));
					/*
					 * String statusActive=invoiceTransactionDao.updateAllInvoiceStatus("Active",
					 * pendingInvoiceList);
					 * logger.info("Status Updated for all invoice "+statusActive);
					 */
					for (Invoice in : pendingInvoiceList) {
						try {
							boolean emailStatus = false, smsStatus = false;
							if (StringUtils.isNotBlank(in.getPhone())) {
								if (!(in.getPhone().equalsIgnoreCase("0"))) {
									if (smsControllerServiceProvider.invoiceSms(in.getShortUrl(), in)) {
										smsStatus = true;
										logger.info("SMS sent to " + in.getPhone());
									}
								} else {

									logger.info("SMS not sent to " + in.getPhone());
								}
							}
							if (StringUtils.isNotBlank(in.getEmail())) {
								String subject = "Payment Gateway Smart Payment Link -- Invoice ID " + in.getInvoiceId();
								String emailBody = getInvoiceBodyWithQR(in.getLongUrl(), in, in.getQr());
								if (awsSESEmailService.invoiceEmail(emailBody, subject, in.getEmail(), in.getEmail(),false)) {
									emailStatus = true;
									logger.info("Email sent to " + in.getEmail());
								} else {

									logger.info("Email not sent to " + in.getEmail());
								}
							}
							String result = invoiceTransactionDao.UpdateStatusByInvoiceId(in.getInvoiceId(), "Active",
									emailStatus, smsStatus);
							logger.info("Status Updated For invoice id " + result);
						} catch (Exception e) {
							logger.error("Exception " , e);
						}
					}
				}
			};
			Thread t = new Thread(r);
			t.start();
			
		} catch (Exception e) {
			logger.error("Exception", e);
		} finally {
			if(StringUtils.isNotBlank(csvFile.toString()))
				csvFile.delete();
		}
		return SUCCESS;
	}

	private List<String> filterCsvFile(File file) throws IOException {
		List<String> csvData=new ArrayList<>();
		BufferedReader br = null;
		try{
			String line="";
			 br= new BufferedReader(new FileReader(file));
			while((line = br.readLine()) != null)
			{
				csvData.add(line);
			}
			return csvData;
			
		}
		catch (Exception e) {
			logger.error("exception " , e);
			return csvData;
		}
		finally{
			if(br!=null)
				br.close();
		}
		
		
	}

	private List<String> filterExcelFile(File file) {
		List<String> data=new ArrayList<String>();
		int totalColCount = 0;
		try{
			
			FileInputStream fis = new FileInputStream(file);
			XSSFWorkbook myWorkBook = new XSSFWorkbook (fis);
			XSSFSheet mySheet = myWorkBook.getSheetAt(0);

			Iterator<Row> rowIterator = mySheet.iterator();
			while (rowIterator.hasNext()) {
				
				StringBuilder rowString = new StringBuilder();
				Row row = rowIterator.next();
				int j=0;
				
				if(row.getRowNum()==0)
				{
					totalColCount=row.getLastCellNum();
				}
				
		
            /*Iterator<Cell> cellIterator = row.iterator();*/
            while (j<totalColCount) {
            /*while (cellIterator.hasNext()) {*/
            	
            	rowString.append(",");
            
            	Cell cell = row.getCell(j);
                //Cell cell = cellIterator.next();
            	if(cell==null){
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
                		 if(cell.getColumnIndex()==8){
                			 SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");
                             rowString.append(dateFormat.format(cell.getDateCellValue()));
                		 }else{
                				 SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
                                 rowString.append(dateFormat.format(cell.getDateCellValue()));
                		 }
                	 }else{
	                	Double doubleValue = cell.getNumericCellValue();
	                    BigDecimal bd = new BigDecimal(doubleValue.toString());
	                    long lonVal = bd.longValue();
	                    String number = Long.toString(lonVal).trim();
                 		rowString.append(number);
                	 }
                    break;
                case Cell.CELL_TYPE_BOOLEAN:
                		rowString.append(String.valueOf(cell.getBooleanCellValue()));
                    break;
                case Cell.CELL_TYPE_BLANK:
                		rowString.append("");
                	break;
                default :
                	rowString.append(",");
                	break;
                }
              
                j++;
            }
            	rowString.deleteCharAt(0);
            	data.add(rowString.toString());
            	
        }
		}catch (Exception e) {
			
			logger.error("exception " , e);
			return data;
		}
		
		return data;
		
		
	}

	public String changeFormat(String date) {
		String formatedDate = null;
		try {
			if (date.length() == 10) {
				SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
				Date date1 = formatter.parse(date.replaceAll("/", "-"));
				formatedDate = formatter.format(date1);
			} else {
				
				formatedDate = date;
			}
		} catch (Exception e) {
			logger.info("Date is not valid " + date);
		}
		return formatedDate;
	}
	public String getInvoiceBodyWithQR(String url, Invoice invoice, String base64String) {
		String body = null;
		StringBuilder content = new StringBuilder();
	
		
		try {
			content.append("<!DOCTYPE html><html lang=\"en\"><head><meta charset=\"UTF-8\">"
					+ "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">"
					+ "<title>Invoice</title><style>body{font-family: Arial, Helvetica, sans-serif;}</style></head>");
			content.append("<body>");
			content.append("<table width=\"350\"  height=\"100%\" bgcolor=\"#f5f5f5\" align=\"center\" style=\"padding: 20px;padding-top: 5px\">");
			content.append("<tbody><tr>");
			content.append("<td align=\"center\"><img src=\""+propertiesManager.getSystemProperty("logoForEmail")+"\" alt=\"\" style=\"padding-top: 5px\" ></td></tr>");
			content.append("<tr><td><h2 style=\"margin: 0;font-size: 15px;font-weight: 400;color: #8a8a8a;margin-bottom: 10px;\">Your Invoice</td></tr>");
			content.append("<tr><td><table width=\"400\" align=\"center\" bgcolor=\"#fff\" cellpadding=\"15\" style=\"border-collapse:collapse;background-color: #fff\">");
			content.append("<tr style=\"border-bottom: 1px solid #ddd;font-size: 14px;\">");
			content.append("<td style=\"font-weight: 600; width:200px;color: #888888;\">Name</td>");
			content.append("<td>"+invoice.getName()+"</td></tr>");
			content.append("<tr style=\"border-bottom: 1px solid #ddd;font-size: 14px;\">");
			content.append("<td style=\"font-weight: 600;color: #888888; width:200px;white-space:nowrap\">Product Name</td>");
			content.append("<td>"+invoice.getProductName()+"</td></tr>");
			content.append("<tr style=\"border-bottom: 1px solid #ddd;font-size: 14px;\">");
			content.append("<td style=\"font-weight: 600; width:200px;color: #888888;\">Mobile</td>");
			if(invoice.getPhone()!=null){
				content.append("<td>"+invoice.getPhone()+"</td></tr>");
			}else{
				content.append("<td>"+""+"</td></tr>");
			}
			content.append("<tr style=\"border-bottom: 1px solid #ddd;font-size: 14px;\">");
			content.append("<td style=\"font-weight: 600; width:200px;color: #888888;\">Email</td>");
			content.append("<td>"+invoice.getEmail()+"</td></tr>");
			content.append("<tr style=\"border-bottom: 1px solid #ddd;font-size: 14px;\">");
			content.append("<td style=\"font-weight: 600;color: #888888; width:200px;white-space:nowrap\">Amount INR</td>");
			content.append("<td>"+ invoice.getTotalAmount() +"</td></tr>");
			/*content.append("<tr style=\"border-bottom: 1px solid #ddd;font-size: 14px;\">");
			content.append("<td style=\"font-weight: 600;color: #888888; width:200px;white-space:nowrap\">Payment Link</td>");
			content.append("<td>"+url+"</td></tr>");*/
			content.append("<tr align=\"center\">");
			//content.append("<td style=\"font-weight: 600; width:200px;color: #888888;\"></td>");
			content.append("<td colspan=\"2\" style=\"border-bottom: 1px solid #ddd;\"><img src=\"data:image/jpg;base64,"+base64String+"\" width=\"150px\" height=\"150px\" alt=\"/\"></td></tr>");
			content.append("<tr align=\"center\"><td colspan=\"2\" style=\"padding: 20px 0\">");
			content.append("<a href=\""+url+"\" style=\"padding: 8px 27px;height: 30px;display:inline-block;font-size: 12px;color: #fff;font-weight: 600;border-radius: 5px;text-decoration: none;\">");
			content.append("<img src=\""+propertiesManager.getSystemProperty("emailerPayButton")+"\"/></a>");
			content.append("</td></tr></table></td></tr>");
			content.append("<tr><td><table width=\"400\" align=\"center\" style=\"border-collapse:collapse; height:auto !important; color:#000;\"><tr>");
			content.append("<td style=\"padding-top: 10px;padding-bottom: 10px;line-height: 20px;font-size: 14px;\">Thanks<br>");
			content.append("<span style=\"display: block;\">Team Payment Gateway</span></td></tr>");
			content.append("<tr><td style=\"font-size: 12px;\">");
			content.append("For any queries feel free to connect with us at +91 120 433 4884. You may also drop your query to us at "
					+"<a href=\"mailto:support@paymentgateway.com\">support@paymentgateway.com</a></td></tr>");
			content.append("<tr><td align=\"right\" style=\"font-size: 12px;padding-top: 15px;\">&copy; 2020 www.paymentgateway.com All rights reserved.</td></tr>");
			content.append("</table></td></tr></tbody></table></body></html>");
			body = content.toString();
		} catch (Exception exception) {
			logger.error("Exception", exception);
		}
		return body;
	}
	
	public String getMerchantPayId() {
		return merchantPayId;
	}

	public void setMerchantPayId(String merchantPayId) {
		this.merchantPayId = merchantPayId;
	}

	public Map<String, String> getCurrencyMap() {
		return currencyMap;
	}

	public void setCurrencyMap(Map<String, String> currencyMap) {
		this.currencyMap = currencyMap;
	}

	public List<Merchants> getMerchantList() {
		return merchantList;
	}

	public void setMerchantList(List<Merchants> merchantList) {
		this.merchantList = merchantList;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public List<Invoice> getInvoiceList() {
		return invoiceList;
	}

	public void setInvoiceList(List<Invoice> invoiceList) {
		this.invoiceList = invoiceList;
	}

	public Map<Long, Invoice> getFailedList() {
		return failedList;
	}

	public void setFailedList(Map<Long, Invoice> failedList) {
		this.failedList = failedList;
	}

	public int getWrongCsv() {
		return wrongCsv;
	}

	public void setWrongCsv(int wrongCsv) {
		this.wrongCsv = wrongCsv;
	}

	public Map<Long, String> getFailedListShow() {
		return failedListShow;
	}

	public void setFailedListShow(Map<Long, String> failedListShow) {
		this.failedListShow = failedListShow;
	}

	public long getRowCount() {
		return rowCount;
	}

	public void setRowCount(long rowCount) {
		this.rowCount = rowCount;
	}

	public long getStoredRow() {
		return storedRow;
	}

	public File getCsvFile() {
		return csvFile;
	}

	public void setCsvFile(File csvFile) {
		this.csvFile = csvFile;
	}

	public void setStoredRow(long storedRow) {
		this.storedRow = storedRow;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public List<String> getFileData() {
		return fileData;
	}

	public void setFileData(List<String> fileData) {
		this.fileData = fileData;
	}

	public int getFileIsEmpty() {
		return fileIsEmpty;
	}

	public void setFileIsEmpty(int fileIsEmpty) {
		this.fileIsEmpty = fileIsEmpty;
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

	public List<Merchants> getSubMerchantList() {
		return subMerchantList;
	}

	public void setSubMerchantList(List<Merchants> subMerchantList) {
		this.subMerchantList = subMerchantList;
	}

	public boolean isSuperMerchant() {
		return isSuperMerchant;
	}

	public void setSuperMerchant(boolean isSuperMerchant) {
		this.isSuperMerchant = isSuperMerchant;
	}
	
	
	
}