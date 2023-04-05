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
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.paymentgateway.commons.dao.IssuerDetailsDao;
import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.mongo.MongoInstance;
import com.paymentgateway.commons.user.IssuerDetails;
import com.paymentgateway.commons.user.Merchants;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.CrmFieldType;
import com.paymentgateway.commons.util.CrmValidator;
import com.paymentgateway.commons.util.IssuerType;
import com.paymentgateway.commons.util.PaymentType;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.TDRStatus;

/**
 * @author Rahul
 *
 */

public class IssuerDetailsAction extends AbstractSecureAction {

	private static final long serialVersionUID = 2029518782067064214L;
	private static Logger logger = LoggerFactory.getLogger(IssuerDetailsAction.class.getName());

	@Autowired
	CrmValidator validator;
	
	@Autowired
	MongoInstance mongoInstance;

	@Autowired
	PropertiesManager propertiesManager;

	@Autowired
	private UserDao userDao;
	
	@Autowired
	private IssuerDetailsDao issuerDetailsDao;

	private String merchantId;
	private File csvfile;
	private String payId;
	private String merchantName;
	private String issuerName;
	private String tenure;
	private String paymentType;
	private List<IssuerDetails> aaData = new ArrayList<IssuerDetails>();
	private User sessionUser = new User();
	private List<Merchants> merchantList = new ArrayList<Merchants>();
	private String rateOfInterest;
	private Boolean alwaysOnOff;
	private Long slabId;
	private int invalid;
	private List<String> fileData;
	private String fileName;
	
	@SuppressWarnings("unchecked")
	public String execute() {
		sessionUser = (User) sessionMap.get(Constants.USER.getValue());
		merchantList = userDao.getActiveMerchantList();
		List finalList = new ArrayList();
		String line = "";
		long rowCount = 0;
		int count = 0;
		try {
			String fileExtension=FilenameUtils.getExtension(fileName);
			
			if(fileExtension.equals("xlsx") || fileExtension.equals("xls"))
			{
				fileData=filterExcelFile(csvfile);
			}
			else if(fileExtension.equals("csv"))
			{
				fileData=filterCsvFile(csvfile);
			}
			
			if(fileData.size()!=0){
				for(int i=0;i<fileData.size();i++) {
					line=fileData.get(i);
					rowCount++;
					String data[] = line.split(",");
					if (rowCount == 1) {
						continue;
					}
				setIssuerName(data[0]);
				setPaymentType(data[1]);
				setTenure(data[2]);
				setRateOfInterest(data[3]);
				if (validateFileFields()) {
				
				if (payId.equalsIgnoreCase("ALL")) {
					if (!merchantList.isEmpty()) {
						for (Merchants merchantRaw : merchantList) {

							IssuerDetails slabDetails = issuerDetailsDao.fetchEmiSlabByTenure(data[0], merchantRaw.getPayId(), data[2],
									data[1]);
							if (slabDetails == null) {
								Date date = new Date();
								IssuerDetails issuerDetails = new IssuerDetails();
								issuerDetails.setIssuerName(data[0]);
								issuerDetails.setTenure(data[2]);
								issuerDetails.setRateOfInterest(data[3]);
								issuerDetails.setPayId(merchantRaw.getPayId());
								issuerDetails.setMerchantName(userDao.getMerchantByPayId(merchantRaw.getPayId()));
								issuerDetails.setPaymentType(data[1]);
								issuerDetails.setCreatedDate(date);
								issuerDetails.setUpdatedDate(date);
								issuerDetails.setStatus(TDRStatus.ACTIVE);
								issuerDetails.setRequestedBy(sessionUser.getEmailId());
								issuerDetails.setAlwaysOn(true);
								issuerDetailsDao.create(issuerDetails);
							} else {
								count++;
								//logger.info("Duplicate EMI entry found in file "+count);
							}
						}
					}

				} else {

					IssuerDetails slabDetails = issuerDetailsDao.fetchEmiSlabByTenure(data[0], payId, data[2], data[1]);
					if (slabDetails == null) {
						Date date = new Date();
						IssuerDetails issuerDetails = new IssuerDetails();
						issuerDetails.setIssuerName(data[0]);
						issuerDetails.setTenure(data[2]);
						issuerDetails.setRateOfInterest(data[3]);
						issuerDetails.setPaymentType(data[1]);
						issuerDetails.setPayId(payId);
						issuerDetails.setMerchantName(userDao.getMerchantByPayId(payId));
						issuerDetails.setCreatedDate(date);
						issuerDetails.setUpdatedDate(date);
						issuerDetails.setStatus(TDRStatus.ACTIVE);
						issuerDetails.setRequestedBy(sessionUser.getEmailId());
						issuerDetails.setAlwaysOn(true);
						issuerDetailsDao.create(issuerDetails);
					} else {
						count++;
						//logger.info("Duplicate EMI entry found in file "+count);
					}
				}
			  }
			}
		}
			csvfile.delete();
			
		} catch (Exception exception) {
			logger.error("Exception while uploding Issuer file: ", exception);
		}
		csvfile=null;
		finalList.add(count);
		finalList.add(invalid);
		setAaData(finalList);
		logger.info("Total Duplicate entries found in file is "+count);
		return SUCCESS;
	}
	private List<String> filterCsvFile(File file) {
		List<String> csvData=new ArrayList<>();
		try{
			String line="";
			BufferedReader br = new BufferedReader(new FileReader(file));
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
                          System.out.print(dateFormat.format(cell.getDateCellValue()));
                          rowString.append(dateFormat.format(cell.getDateCellValue()));
                		 }
                	 }else{
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
	
	public String editEMIDetail() {
		List<IssuerDetails> issurerDataList=new ArrayList<>();
		issuerDetailsDao.editEmiSlab(getRateOfInterest(), getSlabId(), getAlwaysOnOff());
		issurerDataList.addAll(issuerDetailsDao.getActiveAllEmiSlab());
		for(IssuerDetails id:issurerDataList){			
			id.setPaymentType(PaymentType.getpaymentName(id.getPaymentType().toUpperCase()));
		}
		setAaData(issurerDataList);
		return SUCCESS;
	}

	public String fetchAllActiveEmiSlab() {
		List<IssuerDetails> issurerDataList=new ArrayList<>();
		issurerDataList.addAll(issuerDetailsDao.getActiveAllEmiSlab());
		for(IssuerDetails id:issurerDataList){			
			id.setPaymentType(PaymentType.getpaymentName(id.getPaymentType().toUpperCase()));
		}
		setAaData(issurerDataList);
		return SUCCESS;
	}

	public String fecthEmiSlabByFilter() {
		List<IssuerDetails> issurerList=new ArrayList<>();
		
		String issuerBank;
		if(issuerName.equals("ALL")) {
			issuerBank = "ALL";
			
		} else {
			//IssuerType.getInstancefromName(getIssuerName().toUpperCase()
			issuerBank = IssuerType.getIssuerName(issuerName);
		}
		issurerList.addAll(issuerDetailsDao.getAllEmiSlabByPayIdAndIssuerName(payId, issuerBank));
		for(IssuerDetails id:issurerList){			
			id.setPaymentType(PaymentType.getpaymentName(id.getPaymentType().toUpperCase()));
		}
		setAaData(issurerList);
		return SUCCESS;
	}
	
	public Boolean validateFileFields() {
		
		/*if (validator.validateBlankField(getMerchantName())) {
		} else if (!validator.validateField(CrmFieldType.MERCHANT_NAME, getMerchantName())) {
			addFieldError(CrmFieldType.MERCHANT_NAME.getName(),
					ErrorType.INVALID_FIELD.getResponseMessage());
		}*/
		if (validator.validateBlankField(getIssuerName())) {
		} else if (IssuerType.getInstancefromName(getIssuerName().toUpperCase()) == null || !validator.validateField(CrmFieldType.ISSUER_NAME, getIssuerName())) {
				addFieldError(CrmFieldType.ISSUER_NAME.getName(),
						ErrorType.INVALID_FIELD.getResponseMessage());
				invalid++;
				return false;
			}
				
		if (validator.validateBlankField(getPaymentType())) {
		} else if (!validator.validateField(CrmFieldType.PAYMENT_TYPE, getPaymentType())) {
			addFieldError(CrmFieldType.PAYMENT_TYPE.getName(),
					ErrorType.INVALID_FIELD.getResponseMessage());
			invalid++;
			return false;
		}
		if (validator.validateBlankField(getTenure())) {
		} else if (!validator.validateField(CrmFieldType.TENURE, getTenure())) {
			addFieldError(CrmFieldType.TENURE.getName(),
					ErrorType.INVALID_FIELD.getResponseMessage());
			invalid++;
			return false;
		}
		if (validator.validateBlankField(getRateOfInterest())) {
		} else if (!validator.validateField(CrmFieldType.RATE_OF_INTERESET, getRateOfInterest())) {
			addFieldError(CrmFieldType.RATE_OF_INTERESET.getName(),
					ErrorType.INVALID_FIELD.getResponseMessage());
			invalid++;
			return false;
		}
		return true;
	}
	
	
		public void validate() {
				
				if (!(validator.validateBlankField(getFileName()))) {
					
					String fileNameArray [] = getFileName().split(("\\."));
					
					if (fileNameArray.length > 1){
						if (!fileNameArray[1].trim().equalsIgnoreCase("csv")) {
							addFieldError(CrmFieldType.FILE_NAME.getName(), validator.getResonseObject().getResponseMessage());
						}
					}
					
					
				}
				
				if ((validator.validateBlankField(getSlabId()))) {
				} else if (!(validator.validateField(CrmFieldType.SLAB,getSlabId().toString()))) {
					addFieldError(CrmFieldType.SLAB.getName(), validator.getResonseObject().getResponseMessage());
				}
				
				if ((validator.validateBlankField(getRateOfInterest()))) {
				} else if (!(validator.validateField(CrmFieldType.RATE_OF_INTERESET,getRateOfInterest()))) {
					addFieldError(CrmFieldType.RATE_OF_INTERESET.getName(), validator.getResonseObject().getResponseMessage());
				}
				
				
			}

	public String getIssuerName() {
		return issuerName;
	}

	public void setIssuerName(String issuerName) {
		this.issuerName = issuerName;
	}
	
	public String getTenure() {
		return tenure;
	}

	public void setTenure(String tenure) {
		this.tenure = tenure;
	}

	public List<Merchants> getMerchantList() {
		return merchantList;
	}

	public void setMerchantList(List<Merchants> merchantList) {
		this.merchantList = merchantList;
	}

	public File getCsvfile() {
		return csvfile;
	}

	public void setCsvfile(File csvfile) {
		this.csvfile = csvfile;
	}

	public List<IssuerDetails> getAaData() {
		return aaData;
	}

	public void setAaData(List<IssuerDetails> aaData) {
		this.aaData = aaData;
	}

	public String getPayId() {
		return payId;
	}

	public void setPayId(String payId) {
		this.payId = payId;
	}

	public String getPaymentType() {
		return paymentType;
	}

	public void setPaymentType(String paymentType) {
		this.paymentType = paymentType;
	}

	public String getRateOfInterest() {
		return rateOfInterest;
	}

	public void setRateOfInterest(String rateOfInterest) {
		this.rateOfInterest = rateOfInterest;
	}

	public String getMerchantName() {
		return merchantName;
	}

	public void setMerchantName(String merchantName) {
		this.merchantName = merchantName;
	}
	public Boolean getAlwaysOnOff() {
		return alwaysOnOff;
	}

	public void setAlwaysOnOff(Boolean alwaysOnOff) {
		this.alwaysOnOff = alwaysOnOff;
	}
	public Long getSlabId() {
		return slabId;
	}

	public void setSlabId(Long slabId) {
		this.slabId = slabId;
	}
	public String getMerchantId() {
		return merchantId;
	}

	public void setMerchantId(String merchantId) {
		this.merchantId = merchantId;
	}
	public int getInvalid() {
		return invalid;
	}

	public void setInvalid(int invalid) {
		this.invalid = invalid;
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
	
	
}
