package com.paymentgateway.commons.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.slf4j.Logger; import org.slf4j.LoggerFactory;

import com.mongodb.BasicDBObject;


public class DateCreater {
	private static Logger logger = LoggerFactory.getLogger(DateCreater.class.getName());
	public static DateTimeFormatter dateTimeFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

	public static String defaultFromDate() {
		SimpleDateFormat inputDateFormat = new SimpleDateFormat(CrmFieldConstants.INPUT_DATE_FORMAT.getValue());
		Date currentDate = new Date();
		try {
			return inputDateFormat.format(currentDate);
		} catch (Exception exception) {
			logger.error("Exception", exception);
			return null;
		}
	}

	public static String defaultToDate() {
		SimpleDateFormat inputDateFormat = new SimpleDateFormat(CrmFieldConstants.INPUT_DATE_FORMAT.getValue());
		Calendar cal = Calendar.getInstance();
		Date currentDate = new Date();
		try {
			cal.setTime(currentDate);
			cal.add(Calendar.DAY_OF_MONTH, -30);
			return inputDateFormat.format(cal.getTime());
		} catch (Exception exception) {
			logger.error("Exception", exception);
			return null;
		}
	}

	public static String formatFromDate(String dateFrom) {
		SimpleDateFormat inputDateFormat = new SimpleDateFormat(CrmFieldConstants.INPUT_DATE_FORMAT.getValue()); // get date format as is from front end
		//SimpleDateFormat outputDateFormat = new SimpleDateFormat(CrmFieldConstants.OUTPUT_DATE_FORMAT.getValue()); // convert date in this format

		try {
			Date fromDate = (Date)(inputDateFormat.parse(dateFrom));
			//dateFrom = outputDateFormat.format(fromDate);
			Calendar cal = Calendar.getInstance();
			cal.setTime(fromDate);
			dateFrom = cal.get(Calendar.YEAR) + "-" + (cal.get(Calendar.MONTH) + 1) + "-" +  cal.get(Calendar.DATE);

			return dateFrom;
		} catch (Exception exception) {
			logger.error("Exception", exception);
			return null;
		}
	}
	public static String formatDateforChargeback(String date, String time) {
		
		try {
			String[] parts = date.split("-");
			date= parts[2] + "-" + parts[1] +"-" +  parts[0] +time;
			return date;
		} catch (Exception exception) {
			logger.error("Exception", exception);
			return null;
		}
	}
	
	public static String dateTimeFormat(String date, String time) {
		
		try {
			String[] parts = date.split("-");
			date= parts[2] + "-" + parts[1] +"-" +  parts[0] + " "+time;
			return date;
		} catch (Exception exception) {
			logger.error("Exception", exception);
			return null;
		}
	}

	public static String dateFormatReverse(String date) {
		
		try {
			
			String startDateArr[] = date.split("-");
			int tempDate = Integer.parseInt(startDateArr[2]);
			int length = (int) (Math.log10(tempDate));
			if (length == 0) {
				startDateArr[2] = "0" + String.valueOf(tempDate);
			} else {
				startDateArr[2] = String.valueOf(tempDate);
			}
			
			StringBuilder finalDateBuilder = new StringBuilder();
			for(int i = startDateArr.length; i > 0; i--) {
				finalDateBuilder.append(startDateArr[i-1]).append("-");
			}

			finalDateBuilder.deleteCharAt(finalDateBuilder.length() - 1);
			
			return finalDateBuilder.toString();

		} catch (Exception exception) {
			logger.error("Exception", exception);
			return null;
		}
	}
	
	public static String replaceHyphenToSlashInDate(String date) {
		return date.replace("-","/");
	}
	
	
	public static String formatToDate(String dateTo) {
		SimpleDateFormat inputDateFormat = new SimpleDateFormat(CrmFieldConstants.INPUT_DATE_FORMAT.getValue()); // get date format as is from front end
		//SimpleDateFormat outputDateFormat = new SimpleDateFormat(CrmFieldConstants.OUTPUT_DATE_FORMAT.getValue()); // convert date in this format
		Calendar cal = Calendar.getInstance();

		try {
			Date toDate = inputDateFormat.parse(dateTo);
			cal.setTime(toDate);
			cal.add(Calendar.DAY_OF_MONTH, 1);
			//dateTo = outputDateFormat.format(cal.getTime());
			dateTo = cal.get(Calendar.YEAR) + "-" + (cal.get(Calendar.MONTH) + 1) + "-" +  cal.get(Calendar.DATE);
			return dateTo;
		} catch (Exception exception) {
			logger.error("Exception", exception);
			return null;
		}
	}
	
	public static Date formatStringToDate(String date) {		
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		try {
			Date dt = formatter.parse(date);
			return dt;
		} catch (Exception exception) {
			logger.error("Exception", exception);
			return null;
		}		
	}
	
	public static long diffDate(String date1, String date2) {		
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		try {
			Date dt1 = formatter.parse(formatFromDate(date1));
		    Date dt2 = formatter.parse(formatFromDate(date2));
		    long diff = dt2.getTime() - dt1.getTime();
		    long diffDays = diff / (24 * 60 * 60 * 1000);
			return diffDays;
		} catch (Exception exception) {
			logger.error("Exception", exception);
			return 0;
		}		
	}
	
	public static String formatDateForDb(Date date){
		SimpleDateFormat outputDateFormat = new SimpleDateFormat(CrmFieldConstants.OUTPUT_DATE_FORMAT_DB.getValue());		
		return outputDateFormat.format(date);
	}
	
	public static String defaultCurrentDateTime() {
		SimpleDateFormat inputDateFormat = new SimpleDateFormat(CrmFieldConstants.DATE_TIME_FORMAT.getValue());
		Date currentDate = new Date();
		try {
			return inputDateFormat.format(currentDate);
		} catch (Exception exception) {
			logger.error("Exception", exception);
			return null;
		}
	}
	
	//************
	
		public static Date defaultCurrentDateTimeType() {
			SimpleDateFormat inputDateFormat = new SimpleDateFormat(CrmFieldConstants.DATE_TIME_FORMAT.getValue());
			Date currentDate = new Date();
			try {
				currentDate =inputDateFormat.parse(inputDateFormat.format(currentDate));
				return currentDate;
			} catch (Exception exception) {
				logger.error("Exception", exception);
				return null;
			}
		}
		
		public static Date defaultCurrentDateTimeType(Date currentDate) {
			SimpleDateFormat inputDateFormat = new SimpleDateFormat(CrmFieldConstants.DATE_TIME_FORMAT.getValue());
			try {
				currentDate =inputDateFormat.parse(inputDateFormat.format(currentDate));
				return currentDate;
			} catch (Exception exception) {
				logger.error("Exception", exception);
				return null;
			}
		}
	
	public static String createDateTimeFormat(String date) {
		DateTimeFormatter newDateFormat = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
		try {
			String newdate = LocalDateTime.parse(date, dateTimeFormat).format(newDateFormat);
			return newdate;
		} catch (Exception exception) {
			logger.error("Exception", exception);
			return null;
		}
	}
	
	@Deprecated
	public static Date currentDateTime() {		
		//DateFormat inputDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		try {
			Calendar cal = Calendar.getInstance();
			Date dt = cal.getTime();
			return dt;
		} catch (Exception exception) {
			logger.error("Exception", exception);
			return null;
		}		
	}
	
	public static Date formatStringToDateTime(String date) {		
		DateFormat inputDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		try {			 
			 Date dt = inputDateFormat.parse(date);
			return dt;
		} catch (Exception exception) {
			logger.error("Exception", exception);
			return null;
		}		
	}
	
	public static Date convertStringToDateTime(String date) {		
		DateFormat inputDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		try {			 
			 Date dt = inputDateFormat.parse(date);
			return dt;
		} catch (Exception exception) {
			logger.error("Exception", exception);
			return null;
		}		
	}
	
	public static String formatSaleDateTime(String date) {		
		try {
			StringBuilder sbFormatDate = new StringBuilder();
			String[] parts = date.split(" ");
			String[] dateParts = parts[0].split("-");
			sbFormatDate.append(dateParts[0]);
			sbFormatDate.append(dateParts[1]);
			sbFormatDate.append(dateParts[2]);
			if(parts.length == 2) {
				String[] timeParts = parts[1].split(":");
				sbFormatDate.append(timeParts[0]);
				sbFormatDate.append(timeParts[1]);
				sbFormatDate.append(timeParts[2]);
			}
			return sbFormatDate.toString();
		} catch (Exception exception) {
			logger.error("Exception", exception);
			return null;
		}		
	}
	
	public static String formatPgQrDateTime(String date) {		
		try {
			StringBuilder sbFormatDate = new StringBuilder();
			
			String[] dateParts = date.split("/");
			sbFormatDate.append(dateParts[0]);
			sbFormatDate.append(dateParts[1]);
			sbFormatDate.append(dateParts[2]);
			
			return sbFormatDate.toString();
		} catch (Exception exception) {
			logger.error("Exception", exception);
			return null;
		}		
	}

	//Format dd-MM-yyyy to YYYY-MM-dd
	public static String formatDateTime(String date) {		
		try {
			StringBuilder sbFormatDate = new StringBuilder();
			String[] parts = date.split(" ");
			String[] dateParts = parts[0].split("-");
			sbFormatDate.append(dateParts[2]);
			sbFormatDate.append("-");
			sbFormatDate.append(dateParts[1]);
			sbFormatDate.append("-");
			sbFormatDate.append(dateParts[0]);
			sbFormatDate.append(" ");
			sbFormatDate.append(parts[1]);
			/*if(parts.length == 2) {
				String[] timeParts = parts[1].split(":");
				sbFormatDate.append(timeParts[0]);
				sbFormatDate.append(timeParts[1]);
				sbFormatDate.append(timeParts[2]);
			}*/
			return sbFormatDate.toString();
		} catch (Exception exception) {
			logger.error("Exception", exception);
			return null;
		}		
	}
	
	public static String formatSaleDate(String date) {		
		try {
			StringBuilder sbFormatDate = new StringBuilder();
			String[] parts = date.split(" ");
			String[] dateParts = parts[0].split("-");
			sbFormatDate.append(dateParts[0]);
			sbFormatDate.append(dateParts[1]);
			sbFormatDate.append(dateParts[2]);
			return sbFormatDate.toString();
		} catch (Exception exception) {
			logger.error("Exception", exception);
			return null;
		}		
	}
	
	public static String formatSettleDate(String date) {		
		try {
			StringBuilder sbFormatDate = new StringBuilder();
			String[] parts = date.split(" ");
			String[] dateParts = parts[0].split("-");

			sbFormatDate.append(dateParts[2]);
			sbFormatDate.append(dateParts[1]);
			sbFormatDate.append(dateParts[0]);
			return sbFormatDate.toString();
		} catch (Exception exception) {
			logger.error("Exception", exception);
			return null;
		}		
	}
	
	//YYMMDD
	public static String formatFileDate(String date) {		
		try {
			StringBuilder sbFormatDate = new StringBuilder();
			String[] parts = date.split(" ");
			String[] dateParts = parts[0].split("-");

			sbFormatDate.append(dateParts[0]);
			sbFormatDate.append(dateParts[1]);
			sbFormatDate.append(dateParts[2]);
			String formatedDate = sbFormatDate.toString();
			return formatedDate.substring(2, 8);
		} catch (Exception exception) {
			logger.error("Exception", exception);
			return null;
		}		
	}
	
	public static String toDateTimeformatCreater(String date){
		String dbFormatDateTime = null;
		StringBuilder stringBuilder = new StringBuilder();
		String year = date.substring(6,10);
		stringBuilder.append(year);
		stringBuilder.append("-");
		String month= date.substring(3,5);
		stringBuilder.append(month);
		stringBuilder.append("-");
		String datec= date.substring(0,2);
		stringBuilder.append(datec);
		stringBuilder.append(CrmFieldConstants.TO_TIME_FORMAT.getValue());
		
		dbFormatDateTime=stringBuilder.toString();
		
		return dbFormatDateTime;
		
	}
	
	
	public static String formDateTimeformatCreater(String date){
		String dbFormatDateTime = null;
		StringBuilder stringBuilder = new StringBuilder();
		String year = date.substring(6,10);
		stringBuilder.append(year);
		stringBuilder.append("-");
		String month= date.substring(3,5);
		stringBuilder.append(month);
		stringBuilder.append("-");
		String datec= date.substring(0,2);
		stringBuilder.append(datec);
		stringBuilder.append(CrmFieldConstants.FROM_TIME_FORMAT.getValue());
		
		dbFormatDateTime=stringBuilder.toString();
		
		return dbFormatDateTime;
		
	}
	
	public static String formatDateReco(String recoDate){
		String dbFormatDateTime = null;
		StringBuilder stringBuilder = new StringBuilder();
		String year = recoDate.substring(0,4);
		stringBuilder.append(year);
		//stringBuilder.append("-");
		String month= recoDate.substring(5,7);
		stringBuilder.append(month);
		//stringBuilder.append("-");
		String datec= recoDate.substring(8,10);
		stringBuilder.append(datec);
		//stringBuilder.append(CrmFieldConstants.FROM_TIME_FORMAT.getValue());
		
		dbFormatDateTime=stringBuilder.toString();
		
		return dbFormatDateTime;
		
	}
	
	
	public static String formatDateSettlement(String settleDate){
		String dbFormatDateTime = null;
		StringBuilder stringBuilder = new StringBuilder();
		String year = settleDate.substring(0,4);
		stringBuilder.append(year);
		//stringBuilder.append("-");
		String month= settleDate.substring(5,7);
		stringBuilder.append(month);
		//stringBuilder.append("-");
		String datec= settleDate.substring(8,10);
		stringBuilder.append(datec);
		
		/*String hour= settleDate.substring(11,13);
		stringBuilder.append(hour);
		
		String minute= settleDate.substring(14,16);
		stringBuilder.append(minute);
		
		String second= settleDate.substring(17,19);
		stringBuilder.append(second);*/
		
		//stringBuilder.append(CrmFieldConstants.FROM_TIME_FORMAT.getValue());
		
		dbFormatDateTime=stringBuilder.toString();
		
		return dbFormatDateTime;
		
	}
	
	public static LocalDate formatStringToLocalDate(String date) {
		//DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:MM:SS");
		
		//convert String to LocalDate
		LocalDate localDate = LocalDate.parse(date, dateTimeFormat);
		
		return localDate;
	}
	
	public static String chargebackTargetDate() {

		try {
			SimpleDateFormat inputDateFormat = new SimpleDateFormat(CrmFieldConstants.INPUT_DATE_FORMAT.getValue());
			Calendar c = Calendar.getInstance();
			c.setTime(new Date());
			c.add(Calendar.DATE, 10); // Adding 10 days
			return inputDateFormat.format(c.getTime());

		} catch (Exception exception) {
			logger.error("Exception", exception);
			return null;
		}
	}
	//TODO review return type -->because our format can't be parsed in LocalDateTime
	public static String subtractHours(LocalDateTime currentStamp, long hours){
		LocalDateTime finalStamp = currentStamp.minusHours(hours);
		return finalStamp.format(dateTimeFormat);	
	}
	
	public static String subtractDays(LocalDateTime currentStamp, long days){
		LocalDateTime finalStamp = currentStamp.minusDays(days);
		return finalStamp.format(dateTimeFormat);	
	}
	
	public static String subtractWeeks(LocalDateTime currentStamp, long weeks){
		LocalDateTime finalStamp = currentStamp.minusWeeks(weeks);
		return finalStamp.format(dateTimeFormat);	
	}
	
	public static String subtractMonths(LocalDateTime currentStamp, long months){
		LocalDateTime finalStamp = currentStamp.minusMonths(months);
		return finalStamp.format(dateTimeFormat);	
	}
	
	public static LocalDateTime now(){
		return LocalDateTime.now();
	}
	
	public static String formatDate(String date){

		StringBuilder stringBuilder = new StringBuilder();
		String year = date.substring(0,4);
		stringBuilder.append(year);
		stringBuilder.append("-");
		String month= date.substring(4,6);
		stringBuilder.append(month);
		stringBuilder.append("-");
		String datec= date.substring(6,8);
		stringBuilder.append(datec);
		stringBuilder.append(" ");
		
		return stringBuilder.toString();
		
	}
	// date from yyyy-dd-mm hh:MM:ss to yyyyddmm
		public static String changeDateString(String dateString){

			StringBuilder stringBuilder = new StringBuilder();
			String dateTimePart[] = dateString.split(" ");
			String datePart[] = dateTimePart[0].split("-");
			stringBuilder.append(datePart[0]);
			stringBuilder.append(datePart[1]);
			stringBuilder.append(datePart[2]);
			
			return stringBuilder.toString();
			
		}
	//Like 05032021
		public static String formatDateForTransactionSchedule(Date date){
			SimpleDateFormat outputDateFormat = new SimpleDateFormat(CrmFieldConstants.OUTPUT_DATE_FORMAT_REQUEST.getValue());		
			return outputDateFormat.format(date);
		}
		
		public static String requestDateForAutoPay(Date date){
			SimpleDateFormat outputDateFormat = new SimpleDateFormat(CrmFieldConstants.UPI_AUTOPAY_FORMAT.getValue());		
			return outputDateFormat.format(date);
		}
		
	//format YYYY-MM-DD always
	public static List<BasicDBObject> getDateIndex(String startDate,String endDate){
		
		List<BasicDBObject> dateIndexList=new ArrayList<>();
		
		try{
		DateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
		Date dateStart = format.parse(startDate);
		Date dateEnd = format.parse(endDate);
		
		LocalDate beginingDate = dateStart.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
		LocalDate endingDate = dateEnd.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

		while (!beginingDate.isAfter(endingDate)) {
			dateIndexList.add(new BasicDBObject(FieldType.DATE_INDEX.getName(),beginingDate.toString().replaceAll("-", "")));
			beginingDate = beginingDate.plusDays(1);
		}
		
		}catch (Exception e) {
			logger.info("exception in getDateIndex() ",e);
		}
		
		logger.info("final Date Index list -> "+dateIndexList);
		return dateIndexList;
	} 
	
	public static String commonDateFormat(String date) {
		SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		SimpleDateFormat format2 = new SimpleDateFormat("dd/MM/yyyy");
		Date tempDate;
		try {
			tempDate = format1.parse(date);
			date = format2.format(tempDate);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return date;
	}
	
}