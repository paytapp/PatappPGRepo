package com.paymentgateway.crm.action;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger; import org.slf4j.LoggerFactory;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.springframework.beans.factory.annotation.Autowired;

import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.user.Merchants;
import com.paymentgateway.commons.user.SearchTransaction;
import com.paymentgateway.commons.user.SummaryPayoutData;
import com.paymentgateway.commons.user.TransactionCountSearch;
import com.paymentgateway.commons.user.TransactionSearch;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.user.UserType;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.CrmFieldConstants;
import com.paymentgateway.commons.util.CrmFieldType;
import com.paymentgateway.commons.util.CrmValidator;
import com.paymentgateway.commons.util.DateCreater;
import com.paymentgateway.crm.actionBeans.SummaryPayoutReportService;

public class SummaryPayoutReportAction extends AbstractSecureAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = -165007383083425138L;

	private static Logger logger = LoggerFactory.getLogger(SummaryPayoutReportAction.class.getName());

	private String dateFrom;
	private String dateTo;
	public String paymentType;
	public String acquirer;
	private String merchant;
	private InputStream fileInputStream;
	private String filename;
	private List <SummaryPayoutData> summaryPayoutDataList = new ArrayList<SummaryPayoutData>();


	@Autowired 
	private SummaryPayoutReportService summaryPayoutReportService;

	@Autowired
	private UserDao userDao;

	public String execute() {

		try {

			 int secondpart = 0;
			 int merchantlistSize = 0;
			 int reportSize = 0;
			 
			 List<String> acquirerList = new ArrayList<String>();
			 List<String> paymentTypeList = new ArrayList<String>();
			 List<String> merchantList = new ArrayList<String>();
			 
			 
			 
			if (StringUtils.isBlank(acquirer)) {
				return SUCCESS;
			}
			
			for (String acq : acquirer.split(",")) {
				
				if (StringUtils.isNotBlank(acq)) {
					acquirerList.add(acq.replace(" ", ""));
				}
				
			}
			
			if (StringUtils.isBlank(paymentType)) {
				paymentType = "ALL";
			}
			
			if (paymentType.equalsIgnoreCase("ALL")) {
				
				paymentTypeList.add("CCDC");
				paymentTypeList.add("UP");
			}
			else {
				paymentTypeList.add(paymentType);
			}
			
			setDateFrom(DateCreater.toDateTimeformatCreater(dateFrom));
			setDateTo(DateCreater.formDateTimeformatCreater(dateTo));

			User sessionUser = (User) sessionMap.get(Constants.USER.getValue());
			if (sessionUser.getUserType().equals(UserType.SUPERADMIN)
					|| sessionUser.getUserType().equals(UserType.ADMIN)
					|| sessionUser.getUserType().equals(UserType.SUBADMIN)
					|| sessionUser.getUserType().equals(UserType.ASSOCIATE)) {

				String merchantPayId = null;

				if (merchant.equalsIgnoreCase("ALL")) {
					merchantPayId ="ALL";
					List<Merchants> userList = new ArrayList<Merchants>();
					userList = userDao.getActiveMerchantList();
					
					for (Merchants user : userList) {
						merchantList.add(user.getPayId());
					}

				} else {
					
					User user = userDao.findPayIdByEmail(merchant);
					merchantPayId = user.getPayId();
					merchantList.add(merchantPayId);
				}
				
				summaryPayoutDataList = summaryPayoutReportService.getTransactionCount(dateFrom, dateTo, merchantPayId,
						paymentType, acquirer);
				
				Comparator<SummaryPayoutData> comp = (SummaryPayoutData a, SummaryPayoutData b) -> {

					if (a.getBusinessName().compareTo(b.getBusinessName()) > 0) {
						return 1;
					} else if (a.getBusinessName().compareTo(b.getBusinessName()) < 0) {
						return -1;
					} else {
						return 0;
					}
				};

				Collections.sort(summaryPayoutDataList, comp);
				
				 reportSize = 10 + merchantList.size();
				 secondpart = 3 + merchantList.size();
				 merchantlistSize =  merchantList.size();
			}
			
			Collections.sort(merchantList);
			
			ArrayList<String> dataArrayList = new ArrayList<String>();
			
			
			
			
			for (String acquirer : acquirerList) {
				for (String payType : paymentTypeList) {
					
					List <SummaryPayoutData> summaryPayoutDataListTemp = new ArrayList<SummaryPayoutData>();
					int cumNoOfTxn = 0;
					for (SummaryPayoutData summaryPayoutData :summaryPayoutDataList) {
						if(summaryPayoutData.getAcquirerName().equalsIgnoreCase(acquirer.replace(" ", "")) && summaryPayoutData.getPaymentype().equalsIgnoreCase(payType)) {
							summaryPayoutDataListTemp.add(summaryPayoutData);
							cumNoOfTxn = cumNoOfTxn + Integer.valueOf(summaryPayoutData.getNoOfTxn());
						}
					}
					
					if (summaryPayoutDataListTemp.size()<1) {
						continue;
					}
					
					
					StringBuilder sb = new StringBuilder();
					
					sb.append(acquirer);
					sb.append(";");
					sb.append(payType);
					sb.append(";");
					sb.append(String.valueOf(cumNoOfTxn));
					sb.append(";");
					
					// Total over a row
					BigDecimal cumGstPg = BigDecimal.ZERO;
					BigDecimal cumTotalPgFee = BigDecimal.ZERO;
					BigDecimal cumAcquirerBaseAmt = BigDecimal.ZERO;
					BigDecimal cumGstAcquirer = BigDecimal.ZERO;
					BigDecimal cumTotalAcquirerFee = BigDecimal.ZERO;
					BigDecimal cumNodalPayout = BigDecimal.ZERO;
					BigDecimal cumMerchantDiffAmt = BigDecimal.ZERO;
					BigDecimal cumPgBaseAmt = BigDecimal.ZERO;
					
					
					for (SummaryPayoutData summaryPayoutData :summaryPayoutDataListTemp) {

						
						sb.append(summaryPayoutData.getMerchantDiffAmt());
						sb.append(";");
						
						BigDecimal cumMerchantDiffAmtThis = new BigDecimal(summaryPayoutData.getMerchantDiffAmt());
						cumMerchantDiffAmt = cumMerchantDiffAmt.add(cumMerchantDiffAmtThis);
						
						BigDecimal cumPgBaseAmtThis = new BigDecimal(summaryPayoutData.getPgBaseAmt());
						cumPgBaseAmt = cumPgBaseAmt.add(cumPgBaseAmtThis);
						
						BigDecimal cumGstPgThis = new BigDecimal(summaryPayoutData.getGstPg());
						cumGstPg = cumGstPg.add(cumGstPgThis);
						
						BigDecimal cumTotalPgFeeThis = new BigDecimal(summaryPayoutData.getTotalPgFee());
						cumTotalPgFee = cumTotalPgFee.add(cumTotalPgFeeThis);
						
						BigDecimal cumAcquirerBaseAmtThis = new BigDecimal(summaryPayoutData.getAcquirerBaseAmt());
						cumAcquirerBaseAmt = cumAcquirerBaseAmt.add(cumAcquirerBaseAmtThis);
						
						BigDecimal cumGstAcquirerThis = new BigDecimal(summaryPayoutData.getGstAcquirer());
						cumGstAcquirer = cumGstAcquirer.add(cumGstAcquirerThis);
						
						BigDecimal cumTotalAcquirerFeeThis = new BigDecimal(summaryPayoutData.getTotalAcquirerFee());
						cumTotalAcquirerFee = cumTotalAcquirerFee.add(cumTotalAcquirerFeeThis);
						
					}
					
					sb.append(String.valueOf(cumPgBaseAmt.setScale(2, RoundingMode.HALF_DOWN)));
					sb.append(";");
					
					sb.append(String.valueOf(cumGstPg.setScale(2, RoundingMode.HALF_DOWN)));
					sb.append(";");
					
					sb.append(String.valueOf(cumTotalPgFee.setScale(2, RoundingMode.HALF_DOWN)));
					sb.append(";");
					
					sb.append(String.valueOf(cumAcquirerBaseAmt.setScale(2, RoundingMode.HALF_DOWN)));
					sb.append(";");
					
					sb.append(String.valueOf(cumGstAcquirer.setScale(2, RoundingMode.HALF_DOWN)));
					sb.append(";");
					
					sb.append(String.valueOf(cumTotalAcquirerFee.setScale(2, RoundingMode.HALF_DOWN)));
					sb.append(";");
					
					cumNodalPayout = cumNodalPayout.add(cumMerchantDiffAmt).add(cumTotalPgFee).add(cumTotalAcquirerFee).setScale(2, RoundingMode.HALF_DOWN);
					
					sb.append(String.valueOf(cumNodalPayout.setScale(2, RoundingMode.HALF_DOWN)));
					dataArrayList.add(sb.toString());
					
				}
			}
			
			StringBuilder sbEmpty = new StringBuilder();
			for (int i=0;i<reportSize;i++) {
				
				sbEmpty.append("");
				sbEmpty.append(";");
				
			}
			
			dataArrayList.add(sbEmpty.toString());
						// Total over complete table
						int totalNoOfTxn = 0;
						BigDecimal totalGstPg = BigDecimal.ZERO;
						BigDecimal totalTotalPgFee = BigDecimal.ZERO;
						BigDecimal totalAcquirerBaseAmt = BigDecimal.ZERO;
						BigDecimal totalGstAcquirer = BigDecimal.ZERO;
						BigDecimal totalTotalAcquirerFee = BigDecimal.ZERO;
						BigDecimal totalNodalPayout = BigDecimal.ZERO;
						List<String> totalMerchantDiffAmtList = new ArrayList<String>();
						BigDecimal totalPgBaseAmt = BigDecimal.ZERO;
						
						
						for (String dataArray :dataArrayList) {
							
							String [] dataArraySplit = dataArray.split(";");
							if (dataArraySplit.length < 1) {
								continue;
							}
							totalNoOfTxn = totalNoOfTxn + Integer.valueOf(dataArraySplit[2]);
							
							StringBuilder merchantDiffArray= new StringBuilder();
							for(int i=1;i<=merchantlistSize;i++) {
								
								merchantDiffArray.append(dataArraySplit[i+2]);
								if (i == merchantlistSize) {
									
								}
								else {
									merchantDiffArray.append(",");
								}
								
								
							}
							totalMerchantDiffAmtList.add(merchantDiffArray.toString());
							int nextIndex = 2+merchantlistSize+1;
							
							BigDecimal pgBaseAmt = new BigDecimal(dataArraySplit[nextIndex]);
							totalPgBaseAmt = totalPgBaseAmt.add(pgBaseAmt);
							
							BigDecimal gstPg = new BigDecimal(dataArraySplit[nextIndex+1]);
							totalGstPg = totalGstPg.add(gstPg);
							
							BigDecimal totalPgFee = new BigDecimal(dataArraySplit[nextIndex+2]);
							totalTotalPgFee = totalTotalPgFee.add(totalPgFee);
							
							BigDecimal acquirerBaseAmt = new BigDecimal(dataArraySplit[nextIndex+3]);
							totalAcquirerBaseAmt = totalAcquirerBaseAmt.add(acquirerBaseAmt);
							
							BigDecimal gstAcquirer = new BigDecimal(dataArraySplit[nextIndex+4]);
							totalGstAcquirer = totalGstAcquirer.add(gstAcquirer);
							
							BigDecimal totalAcquirerFee = new BigDecimal(dataArraySplit[nextIndex+5]);
							totalTotalAcquirerFee = totalTotalAcquirerFee.add(totalAcquirerFee);
							
							BigDecimal nodalPayout = new BigDecimal(dataArraySplit[nextIndex+6]);
							totalNodalPayout = totalNodalPayout.add(nodalPayout);
							
						}
						
						
						StringBuilder sb = new StringBuilder();
						sb.append("Total");
						sb.append(";");
						sb.append("");
						sb.append(";");
						sb.append(String.valueOf(totalNoOfTxn));
						sb.append(";");
						
						for (int i = 0; i<merchantlistSize;i++) {
							
							BigDecimal merchantAmt = BigDecimal.ZERO;
							for (String merchDiffArray :totalMerchantDiffAmtList) {
								BigDecimal merchantAmtThis = new BigDecimal(merchDiffArray.split(",")[i]);
								merchantAmt= merchantAmt.add(merchantAmtThis);
							}
							
							sb.append(String.valueOf(merchantAmt.setScale(2, RoundingMode.HALF_DOWN)));
							sb.append(";");
							
						}
						
						sb.append(String.valueOf(totalPgBaseAmt.setScale(2, RoundingMode.HALF_DOWN)));
						sb.append(";");
						
						sb.append(String.valueOf(totalGstPg.setScale(2, RoundingMode.HALF_DOWN)));
						sb.append(";");
						
						
						sb.append(String.valueOf(totalTotalPgFee.setScale(2, RoundingMode.HALF_DOWN)));
						sb.append(";");
						
						sb.append(String.valueOf(totalAcquirerBaseAmt.setScale(2, RoundingMode.HALF_DOWN)));
						sb.append(";");
						
						sb.append(String.valueOf(totalGstAcquirer.setScale(2, RoundingMode.HALF_DOWN)));
						sb.append(";");
						
						sb.append(String.valueOf(totalTotalAcquirerFee.setScale(2, RoundingMode.HALF_DOWN)));
						sb.append(";");
						
						sb.append(String.valueOf(totalNodalPayout.setScale(2, RoundingMode.HALF_DOWN)));
						
						dataArrayList.add(sb.toString());
			HSSFWorkbook workbook = new HSSFWorkbook();
			Row row;
			int rownum = 1;
			// Create a blank sheet
			HSSFSheet sheet = workbook.createSheet("Payout Summary Report");

			row = sheet.createRow(0);

			row.createCell(0).setCellValue("Acquirer Name");
			row.createCell(1).setCellValue("Payment Type");
			row.createCell(2).setCellValue("No Of Transactions");
			int i = 0;
			for (String merchant : merchantList) {
				
				row.createCell(i+3).setCellValue(userDao.findPayId(merchant).getBusinessName());
				i++;
			}
			
			row.createCell(secondpart).setCellValue("Payment Gateway Base Amt");
			row.createCell(secondpart+1).setCellValue("Payment Gateway GST");
			row.createCell(secondpart+2).setCellValue("Total Payment Gateway Fee");
			row.createCell(secondpart+3).setCellValue("Acquirer Base Amt");
			row.createCell(secondpart+4).setCellValue("Acquirer GST");
			row.createCell(secondpart+5).setCellValue("Total Acquirer Fee");
			row.createCell(secondpart+6).setCellValue("Net Payout from Nodal");
			
			
			TransactionSearch transactionSearch = new TransactionSearch();

			for (String dataArray: dataArrayList) {
				row = sheet.createRow(rownum++);
				Object[] objArr = transactionSearch.summaryPayoutReportCsv(dataArray);

				int cellnum = 0;
				for (Object obj : objArr) {
					// this line creates a cell in the next column of that row
					Cell cell = row.createCell(cellnum++);
					if (obj instanceof String)
						cell.setCellValue((String) obj);
					else if (obj instanceof Integer)
						cell.setCellValue((Integer) obj);
					
				}
			}
			try {
				//String FILE_EXTENSION = ".xls";
				String FILE_EXTENSION = ".csv";
				DateFormat df = new SimpleDateFormat("yyyyMMddhhmmss");
				filename = "Payments Report" + df.format(new Date()) + FILE_EXTENSION;
				File file = new File(filename);	

				// this Writes the workbook
				FileOutputStream out = new FileOutputStream(file);
				workbook.write(out);
				out.flush();
				out.close();
				fileInputStream = new FileInputStream(file);
				addActionMessage(filename + " written successfully on disk.");
			} catch (Exception exception) {
				logger.error("Exception", exception);
			}

			return SUCCESS;

		} catch (Exception e) {
			logger.error("Exception in getting transaction summary count data " , e);

		}

		return SUCCESS;
	}

	public void validate() {
		CrmValidator validator = new CrmValidator();

		if (validator.validateBlankField(getDateFrom())) {
		} else if (!validator.validateField(CrmFieldType.DATE_FROM, getDateFrom())) {
			addFieldError(CrmFieldType.DATE_FROM.getName(), ErrorType.INVALID_FIELD.getResponseMessage());
		}

		if (validator.validateBlankField(getDateTo())) {
		} else if (!validator.validateField(CrmFieldType.DATE_TO, getDateTo())) {
			addFieldError(CrmFieldType.DATE_TO.getName(), ErrorType.INVALID_FIELD.getResponseMessage());
		}

		if (!validator.validateBlankField(getDateTo())) {
			if (DateCreater.formatStringToDate(DateCreater.formatFromDate(getDateFrom()))
					.compareTo(DateCreater.formatStringToDate(DateCreater.formatFromDate(getDateTo()))) > 0) {
				addFieldError(CrmFieldType.DATE_FROM.getName(), CrmFieldConstants.FROMTO_DATE_VALIDATION.getValue());
			} else if (DateCreater.diffDate(getDateFrom(), getDateTo()) > 31) {
				addFieldError(CrmFieldType.DATE_FROM.getName(), CrmFieldConstants.DATE_RANGE.getValue());
			}
		}

		if (validator.validateBlankField(getMerchant())
				|| getMerchant().equals(CrmFieldConstants.ALL.getValue())) {
		} else if (!validator.validateField(CrmFieldType.MERCHANT_EMAIL_ID, getMerchant())) {
			addFieldError(CrmFieldType.MERCHANT_EMAIL_ID.getName(), ErrorType.INVALID_FIELD.getResponseMessage());
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

	

	public String getPaymentType() {
		return paymentType;
	}

	public void setPaymentType(String paymentType) {
		this.paymentType = paymentType;
	}

	public String getAcquirer() {
		return acquirer;
	}

	public void setAcquirer(String acquirer) {
		this.acquirer = acquirer;
	}

	public String getMerchant() {
		return merchant;
	}

	public void setMerchant(String merchant) {
		this.merchant = merchant;
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
